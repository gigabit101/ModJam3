package vswe.stevesfactory.api.network;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.capabilities.Capability;
import vswe.stevesfactory.api.logic.ProcedureGraph;
import vswe.stevesfactory.logic.execution.VariableDefinition;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Set;

public interface INetworkController {

    DimensionType getDimension();

    BlockPos getPosition();

    World getControllerWorld();

    boolean isValid();

    Set<BlockPos> getConnectedCables();

    /**
     * Remove a cable from the network.
     * <p>
     * This should cause the network to be updated and revalidate its cache, including connected cables.
     *
     * @implNote Implementation may assume the position contains an {@link ICable}. If it does not, it is up to the invokers to resolve the
     * problem.
     */
    void removeCable(BlockPos cable);

    /**
     * Internal storage of linked inventories by the controller.
     * <p>
     * <b>WARNING: </b> Uses of the return value of this method should <b>never</b> modify the content of this set. Instead, they should
     * use {@link #addLink(Capability, BlockPos)} and {@link #removeLink(Capability, BlockPos)} to do so. Controllers are allowed to be
     * designed such that modification to the returning set might cause breakage.
     */
    <T> Set<BlockPos> getLinkedInventories(@Nullable Capability<T> cap);

    /**
     * Connect a inventory located at position to the network.
     * <p>
     * Note that this is not limited to "inventories", but anything with at least one capability.
     *
     * @return {@code true} if the position didn't exist already.
     */
    @SuppressWarnings("UnusedReturnValue")
    <T> boolean addLink(Capability<T> cap, BlockPos pos);

    @SuppressWarnings("UnusedReturnValue")
    <T> boolean addLinks(Capability<T> cap, Collection<BlockPos> poses);

    /**
     * @return {@code true} if the network has the position and successfully removed it.
     * @see #addLink(Capability, BlockPos)
     */
    @SuppressWarnings("UnusedReturnValue")
    <T> boolean removeLink(Capability<T> cap, BlockPos pos);

    void removeAllLinks();

    Collection<VariableDefinition<?>> getVariableDefinitions();

    ProcedureGraph getPGraph();

    Set<String> getGroups();

    /**
     * Sync command graph data.
     */
    void sync();
}
