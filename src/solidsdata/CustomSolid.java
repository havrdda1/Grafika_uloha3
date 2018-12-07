package solidsdata;

import io.vavr.collection.Array;
import io.vavr.collection.IndexedSeq;
import io.vavr.collection.Seq;
import io.vavr.collection.Stream;
import org.jetbrains.annotations.NotNull;
import transforms.Point3D;

public class CustomSolid implements Solid<Point3D, Topology> {
    final @NotNull IndexedSeq<Point3D> vertices;
    final @NotNull IndexedSeq<Integer> indices;
    final Array parts;

    public CustomSolid() {

        vertices = Array.of(
                new Point3D(-2,0,0),
                new Point3D(-1,0,0),
                new Point3D(-1,1,0),
                new Point3D(-2,1,0),
                new Point3D(-1.5,0.5,1.5),
                new Point3D(-0.5,0.5,1),
                new Point3D(-1.5,1.5,1),
                new Point3D(-2.5,0.5,1),
                new Point3D(-1.5,-0.5,1)
        );

        indices = Stream.rangeClosed(0, 0).flatMap(
                i -> Array.of(i, i + 1, i + 1, i + 2, i + 2, i + 3, i + 3, i, i, i + 4, i + 1, i + 4, i + 2, i + 4, i + 3, i + 4,
                        i, i + 8, i + 8, i + 1, i + 1, i + 5, i + 5, i + 2, i + 2, i + 6, i + 6, i + 3, i + 3, i + 7, i + 7, i)
        ).toArray();
        parts = Array.of(new Part(0, 16, Topology.LINES));
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
