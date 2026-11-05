using System.Diagnostics;
using System.Text.Json;
using FluentAssertions;
using RepairShop.Application.Abstractions;
using RepairShop.Infrastructure.Files.Images;
using SkiaSharp;
using Xunit;
using Xunit.Abstractions;

namespace RepairShop.Api.IntegrationTests;

public sealed class ImageThumbnailerTests
{
    private readonly ITestOutputHelper _output;

    public ImageThumbnailerTests(ITestOutputHelper output) => _output = output;

    [Fact]
    public void CreatesSmallWebpPreviewFromLargeRasterImage()
    {
        var original = File.ReadAllBytes(Path.Combine(AppContext.BaseDirectory, "Fixtures", "smartphone-cracked-screen.jpg"));
        IImageThumbnailer thumbnailer = new SkiaImageThumbnailer();

        var result = thumbnailer.CreateWebpThumbnail(original);

        result.Should().NotBeNull();
        using var decoded = SKBitmap.Decode(result!);
        decoded.Should().NotBeNull();
        decoded!.Width.Should().BeLessThanOrEqualTo(640);
        decoded.Height.Should().BeInRange(1, 640);
        result!.Length.Should().BeLessThan(original.Length);

        var samples = new List<(double Ms, long AllocatedBytes)>();
        for (var i = 0; i < 9; i++)
        {
            var before = GC.GetTotalAllocatedBytes(precise: true);
            var timer = Stopwatch.StartNew();
            var sample = thumbnailer.CreateWebpThumbnail(original);
            timer.Stop();
            sample.Should().NotBeNull();
            samples.Add((timer.Elapsed.TotalMilliseconds, GC.GetTotalAllocatedBytes(precise: true) - before));
        }
        var orderedTimes = samples.Select(x => x.Ms).Order().ToArray();
        var orderedAllocations = samples.Select(x => x.AllocatedBytes).Order().ToArray();
        var report = new
        {
            Benchmark = "photo-thumbnail",
            Input = "Wikimedia Commons smartphone photo, 1920x1280 JPEG",
            Samples = samples.Count,
            OriginalBytes = original.Length,
            ThumbnailBytes = result.Length,
            TransferReductionPercent = Math.Round((original.Length - result.Length) * 100d / original.Length, 1),
            MedianProcessingMs = Math.Round(orderedTimes[orderedTimes.Length / 2], 3),
            MedianAllocatedBytes = orderedAllocations[orderedAllocations.Length / 2]
        };
        _output.WriteLine("FIXI_PERF_REPORT=" + JsonSerializer.Serialize(report));
    }

    [Fact]
    public void RejectsMalformedImagesWithoutThrowing()
    {
        IImageThumbnailer thumbnailer = new SkiaImageThumbnailer();

        thumbnailer.CreateWebpThumbnail("not an image"u8).Should().BeNull();
        thumbnailer.CreateWebpThumbnail(Array.Empty<byte>()).Should().BeNull();
    }
}
