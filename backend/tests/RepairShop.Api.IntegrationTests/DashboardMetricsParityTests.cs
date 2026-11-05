using System.Diagnostics;
using System.Text.Json;
using FluentAssertions;
using Microsoft.EntityFrameworkCore.Diagnostics;
using Microsoft.Extensions.DependencyInjection;
using Npgsql;
using RepairShop.Application.Abstractions;
using Xunit.Abstractions;
using Xunit;

namespace RepairShop.Api.IntegrationTests;

[Collection(ApiCollection.Name)]
public sealed class DashboardMetricsParityTests
{
    private readonly ApiFactory _factory;
    private readonly ITestOutputHelper _output;

    public DashboardMetricsParityTests(ApiFactory factory, ITestOutputHelper output)
    {
        _factory = factory;
        _output = output;
    }

    [IntegrationFact]
    public async Task Aggregated_order_metrics_match_legacy_queries_and_reduce_database_round_trips()
    {
        var (api, login) = await ApiClient.SignupAsync(_factory);
        var user = login.GetProperty("user");
        var primaryShopId = Guid.Parse(user.GetProperty("shopId").GetString()!);
        var tech = await (await api.Post("/users", new
        {
            email = "perf-tech-" + Guid.NewGuid().ToString("N")[..8] + "@example.com",
            displayName = "Perf Technician",
            role = "Tech"
        })).DataAsync();
        var userId = Guid.Parse(tech.GetProperty("user").GetProperty("id").GetString()!);

        var overdueOrder = await api.CreateOrderAsync("Benchmark: assigned and overdue");
        await (await api.Put($"/orders/{overdueOrder.Str("id")}/plan", new
        {
            assignedTechnicianId = userId,
            priority = "Normal",
            promisedAtUtc = DateTime.UtcNow.AddDays(2)
        })).DataAsync();
        var secondOrder = await api.CreateOrderAsync("Benchmark: another open status");
        await (await api.Post($"/orders/{secondOrder.Str("id")}/status", new { status = "Diagnosing" })).DataAsync();

        var branch = await (await api.Post("/settings/branches", new
        {
            name = "Benchmark " + Guid.NewGuid().ToString("N")[..6],
            copyTemplates = true
        })).DataAsync();
        var branchId = Guid.Parse(branch.GetProperty("id").GetString()!);
        var switched = await (await api.Post("/auth/switch-shop", new { shopId = branchId })).DataAsync();
        api.UseToken(switched.Str("accessToken"));
        await api.CreateOrderAsync("Benchmark: isolated branch order");
        var branchSummary = await (await api.Get("/dashboard/summary")).DataAsync();
        branchSummary.GetProperty("totalOrders").GetInt32().Should().Be(1);
        var switchedBack = await (await api.Post("/auth/switch-shop", new { shopId = primaryShopId })).DataAsync();
        api.UseToken(switchedBack.Str("accessToken"));
        var primarySummary = await (await api.Get("/dashboard/summary")).DataAsync();
        primarySummary.GetProperty("totalOrders").GetInt32().Should().Be(2);
        primarySummary.GetProperty("overdueOrders").GetInt32().Should().Be(1);
        var anonymous = _factory.CreateClient();
        (await anonymous.GetAsync("/api/v1/dashboard/summary")).StatusCode.Should().Be(System.Net.HttpStatusCode.Unauthorized);

        using var scope = _factory.Services.CreateScope();
        var queries = scope.ServiceProvider.GetRequiredService<IReportQueries>();
        var connection = new NpgsqlConnectionStringBuilder(_factory.ConnectionString);
        using var counter = new EfCommandCounter(connection.Database ?? throw new InvalidOperationException("Integration database name is missing."));
        // Use a future promised date to satisfy the order domain invariant while
        // choosing a benchmark cutoff that counts it as overdue.
        var now = DateTime.UtcNow.AddDays(3);
        var staleBefore = now.AddDays(1);
        var readyBefore = now.AddDays(1);

        var legacy = await ReadLegacyAsync(queries, primaryShopId, userId, now, staleBefore, readyBefore);
        var legacySql = counter.Count;
        counter.Reset();
        var aggregate = await ReadAggregateAsync(queries, primaryShopId, userId, now, staleBefore, readyBefore);
        var aggregateSql = counter.Count;

        aggregate.Counts.Should().BeEquivalentTo(legacy.Counts);
        aggregate.Overdue.Should().Be(legacy.Overdue);
        aggregate.Stale.Should().Be(legacy.Stale);
        aggregate.ReadyBefore.Should().Be(legacy.ReadyBefore);
        aggregate.OpenAssigned.Should().Be(legacy.OpenAssigned);
        aggregate.Workload.Should().BeEquivalentTo(legacy.Workload);
        aggregate.Counts.Values.Sum().Should().Be(2);
        aggregate.Overdue.Should().Be(1);
        legacySql.Should().BeGreaterThan(aggregateSql);

        var branchLegacy = await ReadLegacyAsync(queries, branchId, userId, now, staleBefore, readyBefore);
        var branchAggregate = await ReadAggregateAsync(queries, branchId, userId, now, staleBefore, readyBefore);
        branchAggregate.Counts.Should().BeEquivalentTo(branchLegacy.Counts);
        branchAggregate.Counts.Values.Sum().Should().Be(1);
        branchAggregate.Overdue.Should().Be(branchLegacy.Overdue);
        branchAggregate.Stale.Should().Be(branchLegacy.Stale);
        branchAggregate.ReadyBefore.Should().Be(branchLegacy.ReadyBefore);
        branchAggregate.Workload.Should().BeEquivalentTo(branchLegacy.Workload);

        var legacySamples = new List<Sample>();
        var aggregateSamples = new List<Sample>();
        for (var i = 0; i < 9; i++)
        {
            if (i % 2 == 0)
            {
                legacySamples.Add(await MeasureAsync(() => ReadLegacyAsync(queries, primaryShopId, userId, now, staleBefore, readyBefore)));
                aggregateSamples.Add(await MeasureAsync(() => ReadAggregateAsync(queries, primaryShopId, userId, now, staleBefore, readyBefore)));
            }
            else
            {
                aggregateSamples.Add(await MeasureAsync(() => ReadAggregateAsync(queries, primaryShopId, userId, now, staleBefore, readyBefore)));
                legacySamples.Add(await MeasureAsync(() => ReadLegacyAsync(queries, primaryShopId, userId, now, staleBefore, readyBefore)));
            }
        }

        var legacyMedian = Median(legacySamples.Select(x => x.ElapsedMs));
        var aggregateMedian = Median(aggregateSamples.Select(x => x.ElapsedMs));

        var report = new
        {
            Benchmark = "dashboard-order-metrics",
            Samples = legacySamples.Count,
            SameRunnerAndDatabase = true,
            Dataset = new { PrimaryShopOrders = aggregate.Counts.Values.Sum(), BranchOrders = branchAggregate.Counts.Values.Sum() },
            Legacy = new { SqlCommands = legacySql, MedianMs = legacyMedian, MedianAllocatedBytes = Median(legacySamples.Select(x => (double)x.AllocatedBytes)) },
            Aggregated = new { SqlCommands = aggregateSql, MedianMs = aggregateMedian, MedianAllocatedBytes = Median(aggregateSamples.Select(x => (double)x.AllocatedBytes)) },
            SqlCommandReductionPercent = Math.Round((legacySql - aggregateSql) * 100d / legacySql, 1),
            MedianLatencyChangePercent = Math.Round((legacyMedian - aggregateMedian) * 100d / legacyMedian, 1)
        };
        _output.WriteLine("FIXI_PERF_REPORT=" + JsonSerializer.Serialize(report));
    }

    private static async Task<DashboardValues> ReadLegacyAsync(IReportQueries queries, Guid shopId, Guid userId,
        DateTime now, DateTime staleBefore, DateTime readyBefore)
    {
        var counts = await queries.CountOrdersByStatusAsync(shopId, CancellationToken.None);
        var overdue = await queries.CountOverdueAsync(shopId, now, CancellationToken.None);
        var stale = await queries.CountStaleAsync(shopId, staleBefore, CancellationToken.None);
        var readyBeforeCount = await queries.CountReadyBeforeAsync(shopId, readyBefore, CancellationToken.None);
        var openAssigned = await queries.CountOpenAssignedAsync(shopId, userId, CancellationToken.None);
        var workload = await queries.TechnicianWorkloadAsync(shopId, now, CancellationToken.None);
        return new DashboardValues(counts, overdue, stale, readyBeforeCount, openAssigned, workload);
    }

    private static async Task<DashboardValues> ReadAggregateAsync(IReportQueries queries, Guid shopId, Guid userId,
        DateTime now, DateTime staleBefore, DateTime readyBefore)
    {
        var metrics = await queries.DashboardOrderMetricsAsync(shopId, now, staleBefore, readyBefore, CancellationToken.None);
        var workload = await queries.TechnicianWorkloadAsync(shopId, metrics, CancellationToken.None);
        var openAssigned = metrics.AssignedOrders.FirstOrDefault(x => x.UserId == userId)?.OpenOrders ?? 0;
        return new DashboardValues(metrics.CountsByStatus, metrics.Overdue, metrics.Stale, metrics.ReadyBefore, openAssigned, workload);
    }

    private static async Task<Sample> MeasureAsync(Func<Task<DashboardValues>> action)
    {
        var before = GC.GetTotalAllocatedBytes(precise: true);
        var stopwatch = Stopwatch.StartNew();
        await action();
        stopwatch.Stop();
        var allocated = GC.GetTotalAllocatedBytes(precise: true) - before;
        return new Sample(stopwatch.Elapsed.TotalMilliseconds, allocated);
    }

    private static double Median(IEnumerable<double> values)
    {
        var sorted = values.Order().ToArray();
        return Math.Round(sorted[sorted.Length / 2], 3);
    }

    private sealed record Sample(double ElapsedMs, long AllocatedBytes);

    private sealed record DashboardValues(
        IReadOnlyDictionary<RepairShop.Domain.RepairOrders.RepairOrderStatus, int> Counts,
        int Overdue,
        int Stale,
        int ReadyBefore,
        int OpenAssigned,
        IReadOnlyList<WorkloadRow> Workload);

    private sealed class EfCommandCounter : IObserver<DiagnosticListener>, IDisposable
    {
        private readonly string _database;
        private readonly IDisposable _allListeners;
        private IDisposable? _commands;
        private int _count;

        public EfCommandCounter(string database)
        {
            _database = database;
            _allListeners = DiagnosticListener.AllListeners.Subscribe(this);
        }

        public int Count => Volatile.Read(ref _count);
        public void Reset() => Interlocked.Exchange(ref _count, 0);

        public void OnNext(DiagnosticListener listener)
        {
            if (listener.Name == "Microsoft.EntityFrameworkCore")
                _commands = listener.Subscribe(new CommandObserver(this), name => name == "Microsoft.EntityFrameworkCore.Database.Command.CommandExecuting");
        }

        public void OnError(Exception error) { }
        public void OnCompleted() { }
        public void Dispose()
        {
            _commands?.Dispose();
            _allListeners.Dispose();
        }

        private sealed class CommandObserver(EfCommandCounter owner) : IObserver<KeyValuePair<string, object?>>
        {
            public void OnNext(KeyValuePair<string, object?> value)
            {
                if (value.Value is CommandEventData data && data.Command.Connection?.Database == owner._database)
                    Interlocked.Increment(ref owner._count);
            }

            public void OnError(Exception error) { }
            public void OnCompleted() { }
        }
    }
}
