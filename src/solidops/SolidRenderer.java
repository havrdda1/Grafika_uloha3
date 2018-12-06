package solidops;

import imagedata.Image;
import io.vavr.collection.IndexedSeq;
import org.jetbrains.annotations.NotNull;
import solidsdata.Topology;
import solidsdata.Solid;
import transforms.Mat4;

public interface SolidRenderer<P, V, T> {
    default @NotNull Image<P> render(
            @NotNull Image<P> image,
            @NotNull Solid<V, T> solid,
            @NotNull Mat4 matTransform,
            @NotNull P value
    ) {
        return solid.getParts()
                    .foldLeft(image,
                            (currentImage, part) -> render(
                                    currentImage,
                                    solid.getVertices(),
                                    solid.getIndices(),
                                    part.getIndexStart(),
                                    part.getPrimitivesCount(),
                                    part.getTopology(),
                                    matTransform,
                                    value
                            )
                    );
    }

    @NotNull Image<P> render(
            @NotNull Image<P> background,
            @NotNull IndexedSeq<V> vertices,
            @NotNull IndexedSeq<Integer> indices,
            int indexStart,
            int primitivesCount,
            @NotNull T topology,
            @NotNull Mat4 transform,
            @NotNull P value
    );
}