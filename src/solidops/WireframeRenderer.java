package solidops;

import imagedata.Image;
import io.vavr.collection.IndexedSeq;
import io.vavr.collection.Stream;
import org.jetbrains.annotations.NotNull;
import rasterops.LineRenderer;
import solidsdata.Topology;
import transforms.Mat4;
import transforms.Point3D;

import java.util.Optional;

public class WireframeRenderer<P> implements SolidRenderer<P, Point3D, Topology> {
    private final @NotNull LineRenderer<P> lineRenderer;

    public WireframeRenderer(@NotNull LineRenderer<P> lineRenderer) {
        this.lineRenderer = lineRenderer;
    }

    @NotNull
    @Override
    public Image<P> render(
            @NotNull Image<P> image,
            @NotNull IndexedSeq<Point3D> vertices,
            @NotNull IndexedSeq<Integer> indices,
            int IndexStart, int PrimitivesCount,
            @NotNull Topology topology,
            @NotNull Mat4 matTransform, @NotNull P value) {
        switch (topology) {
            case TRIANGLES:
                return Stream.rangeClosed(0, PrimitivesCount - 1)
                             .foldLeft(image,
                                     (currentImage, i) -> {
                                         renderEdge(
                                                 currentImage,
                                                 vertices.get(indices.get(
                                                         IndexStart + 3 * i
                                                 )),
                                                 vertices.get(indices.get(
                                                         IndexStart + 3 * i + 1
                                                 )),
                                                 matTransform,
                                                 value
                                         );
                                         renderEdge(
                                                 currentImage,
                                                 vertices.get(indices.get(
                                                         IndexStart + 3 * i + 1
                                                 )),
                                                 vertices.get(indices.get(
                                                         IndexStart + 3 * i + 2
                                                 )),
                                                 matTransform,
                                                 value
                                         );
                                         renderEdge(
                                                 currentImage,
                                                 vertices.get(indices.get(
                                                         IndexStart + 3 * i
                                                 )),
                                                 vertices.get(indices.get(
                                                         IndexStart + 3 * i + 2
                                                 )),
                                                 matTransform,
                                                 value
                                         );
                                         return currentImage;
                                     }
                             );

            case LINES:
                return Stream.rangeClosed(0, PrimitivesCount - 1)
                             .foldLeft(image,
                                     (currentImage, i) ->
                                             renderEdge(
                                                     currentImage,
                                                     vertices.get(indices.get(
                                                             IndexStart + 2 * i
                                                     )),
                                                     vertices.get(indices.get(
                                                             IndexStart + 2 * i + 1
                                                     )),
                                                     matTransform,
                                                     value
                                             )
                             );
        }
        return image;
    }

    private @NotNull Image<P> renderEdge(
            final @NotNull Image<P> background,
            final @NotNull Point3D p1, final @NotNull Point3D p2,
            final @NotNull Mat4 transformMat,
            final @NotNull P value
    ) {
        final Point3D p1PriorDehomogen = p1.mul(transformMat);
        final Point3D p2PriorDehomogen = p2.mul(transformMat);
        if (p1PriorDehomogen.getW() <= 0 || p2PriorDehomogen.getW() <= 0)
            return background;
        return p1PriorDehomogen.dehomog()
                               .flatMap(
                                       p1Dehomogen -> p2PriorDehomogen.dehomog()
                                                                      .flatMap(
                                                                              p2Dehomogen ->
                                                                                      Optional.of(lineRenderer.render(
                                                                                              background,
                                                                                              p1Dehomogen.getX(),
                                                                                              p1Dehomogen.getY(),
                                                                                              p2Dehomogen.getX(),
                                                                                              p2Dehomogen.getY(), value))
                                                                      )
                               )
                               .orElse(background);
    }
}