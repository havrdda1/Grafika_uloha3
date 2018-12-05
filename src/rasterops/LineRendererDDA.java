package rasterops;

import imagedata.Image;
import io.vavr.collection.Stream;
import org.jetbrains.annotations.NotNull;


public class LineRendererDDA<LinePixelType> implements LineRenderer<LinePixelType> {
    @Override
    public @NotNull Image<LinePixelType> render(@NotNull Image<LinePixelType> image, double x1, double y1, double x2, double y2, @NotNull LinePixelType value) {
        final double ix1 = (x1 + 1) * image.getWidth() / 2;
        final double iy1 = (-y1 + 1) * image.getHeight() / 2;
        final double ix2 = (x2 + 1) * image.getWidth() / 2;
        final double iy2 = (-y2 + 1) * image.getHeight() / 2;

        return renderRaster(image, ix1, iy1, ix2, iy2, value);
    }

    @Override
    public @NotNull Image<LinePixelType> renderRaster(@NotNull Image<LinePixelType> image, double x1, double y1, double x2, double y2, @NotNull LinePixelType value) {
        final double dx = x2 - x1;
        final double dy = y2 - y1;
        final double steps;

        steps = (Math.abs(dx) > Math.abs(dy)) ? Math.abs(dx) : Math.abs(dy);

        final double xc = (dx / steps);
        final double yc = (dy / steps);

        return Stream.rangeClosed(0, (int) steps)
                     .foldLeft(image, (currentImage, i) -> currentImage.withValue(
                             (int) (x1 + i * xc), (int) (y1 + i * yc), value)
                     );

    }
}
