package solidsdata;

import io.vavr.collection.Array;
import io.vavr.collection.IndexedSeq;
import io.vavr.collection.Seq;
import io.vavr.collection.Stream;
import org.jetbrains.annotations.NotNull;
import transforms.Point3D;

public class CustomSolid implements Solid<Point3D, Topology> {
    private final @NotNull IndexedSeq<Point3D> vertices;
    private final @NotNull IndexedSeq<Integer> indices;
    private final Array parts;

    public CustomSolid() {

        vertices = Array.of(
                new Point3D(0.5,0,0.5),
                new Point3D(0.875,0.125,0.5),
                new Point3D(1,0.5,0.5),
                new Point3D(0.875,0.875,0.5),
                new Point3D(0.5,1,0.5),
                new Point3D(0.125,0.875,0.5),
                new Point3D(0,0.5,0.5),
                new Point3D(0.125,0.125,0.5),
                new Point3D(0.5,0.5,1),
                new Point3D(0.5,0.5,0)
        );

        indices = Stream.rangeClosed(0, 0).flatMap(
                i -> Array.of(i, i + 1, i + 1, i + 2, i + 2, i + 3, i + 3, i + 4, i + 4, i + 5, i + 5, i + 6, i + 6, i + 7, i + 7, i,
                        i, i + 8, i + 1, i + 8, i + 2, i + 8, i + 3, i + 8, i + 4, i + 8, i + 5, i + 8, i + 6, i + 8, i + 7, i + 8, i + 9, i, i + 9,
                        i + 1, i + 9, i + 2, i + 9, i + 3 , i + 9, i + 4, i + 9, i + 5, i + 9, i + 6, i + 9, i + 7)
        ).toArray();
        parts = Array.of(new Part(0, 24, Topology.LINES));
    }
    @NotNull
    @Override
    public IndexedSeq<Point3D> getVertices() {
        return vertices;
    }

    @NotNull
    @Override
    public IndexedSeq<Integer> getIndices() {
        return indices;
    }

    @NotNull
    @Override
    public Seq<Part<Topology>> getParts() {
        return parts;
    }
}
