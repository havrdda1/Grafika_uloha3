package solidsdata;

import io.vavr.collection.IndexedSeq;
import io.vavr.collection.Seq;
import org.jetbrains.annotations.NotNull;

/**
 * Represents solid object
 * @param <V> Vertex Type
 * @param <T> Topology
 */

public interface Solid<V,T> {

    @NotNull IndexedSeq<V> getVertices();
    @NotNull IndexedSeq<Integer> getIndices();
    @NotNull Seq<T> getParts();

    class Part<TopologyType> {

        private final @NotNull TopologyType topology;
        private final int primitivesCount;
        private final int indexStart;

        public Part(@NotNull TopologyType topology, int primitivesCount, int indexStart) {
            this.topology = topology;
            this.primitivesCount = primitivesCount;
            this.indexStart = indexStart;
        }

        @NotNull
        public TopologyType getTopology() {
            return topology;
        }

        public int getPrimitivesCount(){
            return primitivesCount;
        }

        public int getIndexStart(){
            return indexStart;
        }
    }

}
