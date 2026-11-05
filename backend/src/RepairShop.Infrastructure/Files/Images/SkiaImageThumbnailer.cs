using RepairShop.Application.Abstractions;
using SkiaSharp;

namespace RepairShop.Infrastructure.Files.Images;

/// <summary>Cross-platform WebP previews for Linux and Windows deployments.</summary>
public sealed class SkiaImageThumbnailer : IImageThumbnailer
{
    private const long MaxPixels = 16_000_000;

    public byte[]? CreateWebpThumbnail(ReadOnlySpan<byte> imageBytes, int maxDimension = 640, int quality = 75)
    {
        if (imageBytes.IsEmpty || maxDimension is < 1 or > 2048 || quality is < 1 or > 100) return null;

        using var data = SKData.CreateCopy(imageBytes.ToArray());
        using var codec = SKCodec.Create(data);
        if (codec is null) return null;

        var source = codec.Info;
        if (source.Width <= 0 || source.Height <= 0 || (long)source.Width * source.Height > MaxPixels) return null;

        var sample = Math.Max(1, Math.Min(source.Width, source.Height) / maxDimension);
        var decodeInfo = new SKImageInfo(Math.Max(1, source.Width / sample), Math.Max(1, source.Height / sample),
            SKColorType.Bgra8888, SKAlphaType.Premul);
        using var bitmap = SKBitmap.Decode(codec, decodeInfo);
        if (bitmap is null) return null;

        var scale = Math.Min(1d, Math.Min((double)maxDimension / bitmap.Width, (double)maxDimension / bitmap.Height));
        var width = Math.Max(1, (int)Math.Round(bitmap.Width * scale));
        var height = Math.Max(1, (int)Math.Round(bitmap.Height * scale));
        using var resized = bitmap.Resize(new SKImageInfo(width, height, SKColorType.Bgra8888, SKAlphaType.Premul),
            new SKSamplingOptions(SKFilterMode.Linear, SKMipmapMode.Linear));
        if (resized is null) return null;
        using var image = SKImage.FromBitmap(resized);
        using var encoded = image.Encode(SKEncodedImageFormat.Webp, quality);
        return encoded?.ToArray();
    }
}
