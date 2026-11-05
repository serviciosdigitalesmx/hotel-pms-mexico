using System.Net.Http.Headers;
using FluentAssertions;
using Microsoft.Extensions.DependencyInjection;
using RepairShop.Application.Abstractions;
using Xunit;

namespace RepairShop.Api.IntegrationTests;

[Collection(ApiCollection.Name)]
public sealed class ImageAttachmentTests
{
    private readonly ApiFactory _factory;

    public ImageAttachmentTests(ApiFactory factory) => _factory = factory;

    [IntegrationFact]
    public async Task Thumbnail_keeps_original_and_legacy_photo_falls_back_to_original()
    {
        var api = await ApiClient.LoginAsync(_factory);
        var order = await api.CreateOrderAsync();

        var original = File.ReadAllBytes(Path.Combine(AppContext.BaseDirectory, "Fixtures", "smartphone-cracked-screen.jpg"));

        using var multipart = new MultipartFormDataContent();
        using var photo = new ByteArrayContent(original);
        photo.Headers.ContentType = new MediaTypeHeaderValue("image/jpeg");
        multipart.Add(photo, "file", "device.jpg");
        var upload = await api.Http.PostAsync($"/api/v1/orders/{order.Str("id")}/attachments/upload", multipart);
        var attachment = await upload.DataAsync();

        var fileId = Guid.Parse(attachment.GetProperty("fileId").GetString()!);
        var originalUrl = attachment.GetProperty("url").GetString()!;
        var thumbnailUrl = attachment.GetProperty("thumbnailUrl").GetString()!;
        using var publicClient = _factory.CreateClient();

        using var thumbnailResponse = await publicClient.GetAsync(thumbnailUrl);
        thumbnailResponse.EnsureSuccessStatusCode();
        thumbnailResponse.Content.Headers.ContentType!.MediaType.Should().Be("image/webp");
        var thumbnailBytes = await thumbnailResponse.Content.ReadAsByteArrayAsync();
        using var thumbnail = SKBitmap.Decode(thumbnailBytes);
        thumbnail.Should().NotBeNull();
        thumbnail!.Width.Should().BeLessThanOrEqualTo(640);
        thumbnail.Height.Should().BeLessThanOrEqualTo(640);

        using var originalResponse = await publicClient.GetAsync(originalUrl);
        originalResponse.EnsureSuccessStatusCode();
        (await originalResponse.Content.ReadAsByteArrayAsync()).Should().Equal(original);

        using var scope = _factory.Services.CreateScope();
        var storedFiles = scope.ServiceProvider.GetRequiredService<IStoredFileRepository>();
        var storage = scope.ServiceProvider.GetRequiredService<IFileStorage>();
        var stored = await storedFiles.GetByIdAnyShopAsync(fileId, CancellationToken.None);
        stored.Should().NotBeNull();
        await storage.DeleteAsync(stored!.StorageKey + ".thumb.webp", CancellationToken.None);

        using var legacyResponse = await publicClient.GetAsync(thumbnailUrl);
        legacyResponse.EnsureSuccessStatusCode();
        legacyResponse.Content.Headers.ContentType!.MediaType.Should().Be("image/jpeg");
        (await legacyResponse.Content.ReadAsByteArrayAsync()).Should().Equal(original);
    }
}
