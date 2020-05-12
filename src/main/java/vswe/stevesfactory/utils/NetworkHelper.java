package vswe.stevesfactory.utils;

import com.google.common.base.Preconditions;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullConsumer;
import vswe.stevesfactory.api.StevesFactoryManagerAPI;
import vswe.stevesfactory.api.logic.Connection;
import vswe.stevesfactory.api.logic.IExecutionContext;
import vswe.stevesfactory.api.logic.IProcedure;
import vswe.stevesfactory.api.logic.IProcedureType;
import vswe.stevesfactory.api.network.ICable;
import vswe.stevesfactory.api.network.IConnectable;
import vswe.stevesfactory.api.network.IConnectable.LinkType;
import vswe.stevesfactory.api.network.INetworkController;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public final class NetworkHelper {

    private NetworkHelper() {
    }

    public static <P extends IProcedure> P fabricateInstance(INetworkController controller, IProcedureType<P> type) {
        P p = type.createInstance();
        controller.getPGraph().addProcedure(p);
        return p;
    }

    public static IProcedure retrieveProcedureAndAdd(INetworkController controller, CompoundNBT tag) {
        IProcedure p = findTypeFor(tag).retrieveInstance(tag);
        controller.getPGraph().addProcedure(p);
        return p;
    }

    public static IProcedureType<?> findTypeFor(CompoundNBT tag) {
        ResourceLocation id = new ResourceLocation(tag.getString("ID"));
        return findTypeFor(id);
    }

    public static IProcedureType<?> findTypeFor(ResourceLocation id) {
        IProcedureType<?> p = StevesFactoryManagerAPI.getProceduresRegistry().getValue(id);
        // Not using checkNotNull here because technically the above method returns null is a registry (game state) problem
        Preconditions.checkArgument(p != null, "Unable to find a procedure registered as ${id}!");
        return p;
    }

    /**
     * Wrap a constructor of a procedure to a fabricator that constructs the procedures, and then immediately adds it to the controller.
     */
    public static <P extends IProcedure> Function<INetworkController, P> wrapConstructor(Supplier<P> constructor) {
        return controller -> {
            P p = constructor.get();
            controller.getPGraph().addProcedure(p);
            return p;
        };
    }

    public static void removeAllConnectionsFor(IProcedure procedure) {
        if (!procedure.isValid()) {
            return;
        }
        for (Connection conn : procedure.predecessors()) {
            if (conn != null) {
                conn.remove();
            }
        }
        for (Connection conn : procedure.successors()) {
            if (conn != null) {
                conn.remove();
            }
        }
    }

    public static void removeAllConnected(IProcedure start) {
        Set<IProcedure> visited = new HashSet<>();
        Queue<IProcedure> nexts = new ArrayDeque<>();
        nexts.add(start);
        while (!nexts.isEmpty()) {
            IProcedure node = nexts.remove();
            for (Connection conn : node.predecessors()) {
                IProcedure source = conn.getSource();
                if (visited.contains(source)) {
                    continue;
                }
                visited.add(source);
                nexts.add(source);
            }
            for (Connection conn : node.successors()) {
                IProcedure dest = conn.getDestination();
                if (visited.contains(dest)) {
                    continue;
                }
                visited.add(dest);
                nexts.add(dest);
            }
        }
        for (IProcedure procedure : visited) {
            procedure.invalidate();
        }
    }

    public static LinkType getLinkType(@Nullable TileEntity tile) {
        if (tile instanceof IConnectable) {
            return ((IConnectable) tile).getConnectionType();
        }
        return LinkType.DEFAULT;
    }

    public static void updateLinksFor(INetworkController controller, ICable cable) {
        for (Capability<?> cap : StevesFactoryManagerAPI.getRecognizableCapabilities()) {
            updateLinksFor(controller, cable, cap);
        }
    }

    public static void updateLinksFor(INetworkController controller, ICable cable, Capability<?> cap) {
        World world = controller.getControllerWorld();
        for (BlockPos neighbor : Utils.neighbors(cable.getPosition())) {
            TileEntity tile = world.getTileEntity(neighbor);
            if (tile == null) {
                continue;
            }
            switch (getLinkType(tile)) {
                case ALWAYS:
                    if (Utils.hasCapabilityAtAll(tile, cap)) {
                        controller.addLink(cap, neighbor);
                    } else {
                        controller.addLink(IConnectable.UNKNOWN_CONNECTION_CAPABILITY, neighbor);
                    }
                    break;
                case DEFAULT:
                    if (Utils.hasCapabilityAtAll(tile, cap)) {
                        controller.addLink(cap, neighbor);
                    }
                    break;
                case NEVER:
                    break;
            }
        }
    }

    public static <T> T obtainCustomData(IExecutionContext context, Class<T> type, Supplier<T> factory) {
        // Checked when putting value in to the ClassToInstanceMap
        @SuppressWarnings("unchecked") T res = (T) context.getCustomData().get(type);
        if (res == null) {
            res = factory.get();
            context.getCustomData().putInstance(type, res);
        }
        return res;
    }

    public static <T> void cacheDirectionalCaps(
            IExecutionContext context,
            Collection<LazyOptional<T>> target,
            Collection<BlockPos> poses,
            Collection<Direction> directions,
            Capability<T> type,
            @Nullable NonNullConsumer<LazyOptional<T>> listener
    ) {
        cacheDirectionalCaps(context, context.getController().getLinkedInventories(type), target, poses, directions, type, listener);
    }

    public static <T> void cacheDirectionalCaps(
            IExecutionContext context,
            Set<BlockPos> linkedInventories,
            Collection<LazyOptional<T>> target,
            Collection<BlockPos> poses,
            Collection<Direction> directions,
            Capability<T> type,
            @Nullable NonNullConsumer<LazyOptional<T>> listener
    ) {
        for (BlockPos pos : poses) {
            // Don't force remove non-existing connections as a more user friendly design
            // so that in case player accidentally break a cable, the settings are still preserved
            // the player can just place the cable back and everything will function properly as before
            if (!linkedInventories.contains(pos)) {
                continue;
            }
            TileEntity tile = context.getControllerWorld().getTileEntity(pos);
            if (tile == null) {
                continue;
            }
            for (Direction direction : directions) {
                LazyOptional<T> cap = tile.getCapability(type, direction);
                if (cap.isPresent()) {
                    target.add(cap);
                    if (listener != null) {
                        cap.addListener(listener);
                    }
                }
            }
        }
    }

    public static <T> void cacheCaps(
            IExecutionContext context,
            Collection<LazyOptional<T>> target,
            Collection<BlockPos> poses,
            Capability<T> capability,
            @Nullable NonNullConsumer<LazyOptional<T>> listener
    ) {
        cacheCaps(context, context.getController().getLinkedInventories(capability), target, poses, capability, listener);
    }

    public static <T> void cacheCaps(
            IExecutionContext context,
            Set<BlockPos> linkedInventories,
            Collection<LazyOptional<T>> target,
            Collection<BlockPos> poses,
            Capability<T> capability,
            @Nullable NonNullConsumer<LazyOptional<T>> listener
    ) {
        for (BlockPos pos : poses) {
            if (!linkedInventories.contains(pos)) {
                continue;
            }
            TileEntity tile = context.getControllerWorld().getTileEntity(pos);
            if (tile == null) {
                continue;
            }
            LazyOptional<T> cap = tile.getCapability(capability);
            if (cap.isPresent()) {
                target.add(cap);
                if (listener != null) {
                    cap.addListener(listener);
                }
            }
        }
    }
}
