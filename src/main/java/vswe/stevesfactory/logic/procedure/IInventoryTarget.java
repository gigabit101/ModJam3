package vswe.stevesfactory.logic.procedure;

import net.minecraft.util.math.BlockPos;

import java.util.List;

public interface IInventoryTarget {

    List<BlockPos> getInventories(int id);

    default List<BlockPos> getInventories() {
        return getInventories(0);
    }

    /**
     * Mark this data target dirty for updating internal caches. This must be called when data is mutated indirectly,
     * i.e. by mutating any references returned by getters.
     * <p>
     * Implementations are required to handle mark dirty for setters methods, such that users can call them without
     * worrying about marking the data target dirty.
     */
    void markDirty();
}
