using FluentAssertions;
using RepairShop.Application.Abstractions;
using RepairShop.Infrastructure.Files.Images;
using SkiaSharp;
using Xunit;

namespace RepairShop.Api.IntegrationTests;

public sealed class ImageThumbnailerTests
{
    [Fact]
    public void CreatesSmallWebpPreviewFromLargeRasterImage()
    {
        using var source = new SKBitmap(new SKImageInfo(1200, 800, SKColorType.Bgra8888, SKAlphaType.Premul));
        using var canvas = new SKCanvas(source);
        canvas.Clear(SKColors.CornflowerBlue);
        using var image = SKImage.FromBitmap(source);
        using var encoded = image.Encode(SKEncodedImageFormat.Jpeg, 90);
        var thumbnailer = new SkiaImageThumbnailer();

        var result = thumbnailer.CreateWebpThumbnail(encoded.ToArray());

        result.Should().NotBeNull();
        using var decoded = SKBitmap.Decode(result!);
        decoded.Should().NotBeNull();
        decoded!.Width.Should().Be(640);
        decoded.Height.Should().BeInRange(1, 640);
    }

    [Fact]
    public void RejectsMalformedImagesWithoutThrowing()
    {
        IImageThumbnailer thumbnailer = new SkiaImageThumbnailer();

        thumbnailer.CreateWebpThumbnail("not an image"u8).Should().BeNull();
        thumbnailer.CreateWebpThumbnail(Array.Empty<byte>()).Should().BeNull();
    }
}
