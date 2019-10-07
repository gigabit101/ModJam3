package vswe.stevesfactory.blocks;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.*;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.util.Constants;
import org.apache.logging.log4j.Logger;
import vswe.stevesfactory.Config;
import vswe.stevesfactory.StevesFactoryManager;
import vswe.stevesfactory.api.StevesFactoryManagerAPI;
import vswe.stevesfactory.api.logic.CommandGraph;
import vswe.stevesfactory.api.logic.IProcedure;
import vswe.stevesfactory.api.network.ICable;
import vswe.stevesfactory.api.network.INetworkController;
import vswe.stevesfactory.logic.execution.ITickable;
import vswe.stevesfactory.network.*;
import vswe.stevesfactory.setup.ModBlocks;
import vswe.stevesfactory.utils.*;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.*;

public class FactoryManagerTileEntity extends BaseTileEntity implements ITickableTileEntity, INetworkController, ICable {

    private Set<BlockPos> connectedCables = new HashSet<>();
    private Map<Capability<?>, Multiset<BlockPos>> linkedInventories = new IdentityHashMap<>();

    private Set<CommandGraph> graphs = new HashSet<>();

    private int ticks;

    public FactoryManagerTileEntity() {
        super(ModBlocks.factoryManagerTileEntity);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        ticks = 0;
    }

    @Override
    public void tick() {
        assert world != null;
        if (!world.isRemote) {
            for (CommandGraph graph : graphs) {
                IProcedure hat = graph.getRoot();
                if (hat instanceof ITickable) {
                    ((ITickable) hat).tick();
                }
            }

            if (ticks == 0) {
                reload();
                ticks = Config.COMMON.rescanInterval.get();
            } else {
                ticks--;
            }
        }
    }

    private void reload() {
        search();
    }

    public void activate(PlayerEntity player) {
        assert world != null;
        Preconditions.checkState(!world.isRemote);
        StevesFactoryManager.logger.trace("Player {} activated a factory manager at {}", player, pos);

        search();

        ServerPlayerEntity client = (ServerPlayerEntity) player;
        PacketOpenManagerGUI.openFactoryManager(client, getDimension(), getPosition(), write(new CompoundNBT()));
    }

    private void search() {
        StevesFactoryManager.logger.trace("Triggered searching process on a factory manager at {}", pos);

        // Relocate all the cables to prevent the last cable, for example, from being recognized as connected
        // [manager] - [cable] - [removed cable (air)] - [unconnected cable]
        connectedCables.clear();
        linkedInventories.clear();

        addCableToNetwork(this, pos);
        search(pos, 0);
    }

    private void search(BlockPos center, int depth) {
        if (depth > Config.COMMON.maxSearchDepth.get()) {
            return;
        }
        assert world != null;
        StevesFactoryManager.logger.trace("Searching at cable {}", center);
        for (Direction direction : VectorHelper.DIRECTIONS) {
            BlockPos neighbor = center.offset(direction);
            TileEntity tile = world.getTileEntity(neighbor);
            if (tile instanceof ICable && !connectedCables.contains(neighbor)) {
                addCableToNetwork((ICable) tile, neighbor);
                // Recursive search (DFS)
                search(neighbor, depth + 1);
            }
        }
    }

    private void addCableToNetwork(ICable cable, BlockPos pos) {
        cable.addLinksFor(this);
        connectedCables.add(pos);
    }

    public void dump() {
        assert world != null;
        Logger logger = StevesFactoryManager.logger;
        logger.debug("======== Dumping Factory Manager at {} ========", pos);

        logger.debug("Connected cables:");
        for (BlockPos pos : connectedCables) {
            if (this.pos.equals(pos)) {
                logger.debug("    This controller: {}", this);
            } else {
                logger.debug("    {}: {}", pos, world.getTileEntity(pos));
            }
        }

        for (Map.Entry<Capability<?>, Multiset<BlockPos>> entry : linkedInventories.entrySet()) {
            Capability<?> cap = entry.getKey();
            Multiset<BlockPos> set = entry.getValue();
            logger.debug("Linked inventories ({}):", cap.getName());
            for (BlockPos pos : set) {
                logger.debug("    {}: {}", pos, world.getTileEntity(pos));
            }
        }

        logger.debug("======== Finished dumping Factory Manager ========");
    }

    @Override
    public DimensionType getDimension() {
        assert world != null;
        return world.getDimension().getType();
    }

    @Override
    public Set<BlockPos> getConnectedCables() {
        return connectedCables;
    }

    @Override
    public void removeCable(BlockPos cable) {
        assert world != null;
        connectedCables.remove(cable);
    }

    public <T> Multiset<BlockPos> getInventoryMultiset(Capability<T> cap) {
        Multiset<BlockPos> multiset = linkedInventories.get(cap);
        if (multiset == null) {
            Multiset<BlockPos> newSet = HashMultiset.create();
            linkedInventories.put(cap, newSet);
            return newSet;
        }
        return multiset;
    }

    @Override
    public <T> Set<BlockPos> getLinkedInventories(Capability<T> cap) {
        return getInventoryMultiset(cap).elementSet();
    }

    /**
     * @return {@code true} always. See {@link Multiset#add(Object)} for details.
     */
    @Override
    public <T> boolean addLink(Capability<T> cap, BlockPos pos) {
        StevesFactoryManager.logger.trace("Added link");
        return getInventoryMultiset(cap).add(pos);
    }

    @Override
    public <T> boolean addLinks(Capability<T> cap, Collection<BlockPos> poses) {
        StevesFactoryManager.logger.trace("Added {} links", poses.size());
        return getInventoryMultiset(cap).addAll(poses);
    }

    @Override
    public void removeAllLinks() {
        linkedInventories.clear();
    }

    @Override
    public Set<CommandGraph> getCommandGraphs() {
        return graphs;
    }

    @Override
    public boolean addCommandGraph(CommandGraph graph) {
        return graphs.add(graph);
    }

    @Override
    public boolean addCommandGraphs(Collection<CommandGraph> graphs) {
        return this.graphs.addAll(graphs);
    }

    @Override
    public boolean removeCommandGraph(CommandGraph graph) {
        return graphs.remove(graph);
    }

    @Override
    public void removeAllCommandGraphs() {
        graphs.clear();
    }

    /**
     * @return {@code true} always. See {@link Multiset#add(Object)} for details.
     */
    @Override
    public <T> boolean removeLink(Capability<T> cap, BlockPos pos) {
        StevesFactoryManager.logger.trace("Removed link");
        return getInventoryMultiset(cap).remove(pos);
    }

    @Override
    public void addLinksFor(INetworkController controller) {
        for (Capability<?> cap : StevesFactoryManagerAPI.getRecognizableCapabilities()) {
            updateLinks(controller, cap);
        }
    }

    private void updateLinks(INetworkController controller, Capability<?> cap) {
        assert world != null;
        for (BlockPos neighbor : getNeighbors()) {
            TileEntity tile = world.getTileEntity(neighbor);
            if (tile == null) {
                continue;
            }
            if (Utils.hasCapabilityAtAll(tile, cap)) {
                controller.addLink(cap, neighbor);
            }
        }
    }

    @Override
    public boolean isValid() {
        return !removed;
    }

    @Override
    public BlockPos getPosition() {
        return pos;
    }

    @Nullable
    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public boolean isCable() {
        return true;
    }

    @Override
    public void sync() {
        assert world != null;
        if (world.isRemote) {
            NetworkHandler.sendToServer(new PacketSyncCommandGraphs(graphs, getDimension(), getPosition()));
        }
    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(pos, 0, write(new CompoundNBT()));
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        read(pkt.getNbtCompound());
    }

    private static final Map<String, Capability<?>> caps;

    static {
        try {
            Field field = CapabilityManager.class.getDeclaredField("providers");
            field.setAccessible(true);
            @SuppressWarnings("unchecked") Map<String, Capability<?>> map = (Map<String, Capability<?>>) field.get(CapabilityManager.INSTANCE);
            caps = map;
        } catch (NoSuchFieldException | IllegalAccessException | ClassCastException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void read(CompoundNBT compound) {
        StevesFactoryManager.logger.trace("Restoring data from NBT {}", compound);

        super.read(compound);

        connectedCables.clear();
        IOHelper.readBlockPoses(compound.getList("ConnectedCables", Constants.NBT.TAG_COMPOUND), connectedCables);

        linkedInventories.clear();
        ListNBT serializedInventories = compound.getList("LinkedInventories", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < serializedInventories.size(); i++) {
            CompoundNBT element = serializedInventories.getCompound(i);

            // Constructed (heap) string and interned string behave differently in an IdentityHashMap
            // (CapabilityManager interns the capability name before putting them in the map)
            String capName = element.getString("Name").intern();
            // TODO use accesstransformer
//            Capability<?> cap = CapabilityManager.INSTANCE.providers.get(capName);
            Capability<?> cap = caps.get(capName);

            ListNBT serializedPoses = element.getList("Positions", Constants.NBT.TAG_COMPOUND);
            Multiset<BlockPos> set = HashMultiset.create();
            for (int j = 0; j < serializedPoses.size(); j++) {
                set.add(NBTUtil.readBlockPos(serializedPoses.getCompound(j)));
            }

            linkedInventories.put(cap, set);
        }

        ListNBT commandGraphs = compound.getList("CommandGraphs", Constants.NBT.TAG_COMPOUND);
        graphs.clear();
        for (int i = 0; i < commandGraphs.size(); i++) {
            CommandGraph graph = CommandGraph.deserializeFrom(commandGraphs.getCompound(i), this);
            graphs.add(graph);
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        StevesFactoryManager.logger.trace("Writing data into NBT ({})", pos);

        compound.put("ConnectedCables", IOHelper.writeBlockPoses(connectedCables));

        ListNBT serializedInventories = new ListNBT();
        for (Map.Entry<Capability<?>, Multiset<BlockPos>> entry : linkedInventories.entrySet()) {
            Capability<?> cap = entry.getKey();
            Multiset<BlockPos> set = entry.getValue();

            ListNBT serializedPoses = new ListNBT();
            for (BlockPos pos : set) {
                serializedPoses.add(NBTUtil.writeBlockPos(pos));
            }

            CompoundNBT element = new CompoundNBT();
            element.putString("Name", cap.getName());
            element.put("Positions", serializedPoses);
            serializedInventories.add(element);
        }
        compound.put("LinkedInventories", serializedInventories);

        ListNBT commandGraphs = new ListNBT();
        for (CommandGraph graph : graphs) {
            commandGraphs.add(graph.serialize());
        }
        compound.put("CommandGraphs", commandGraphs);

        return super.write(compound);
    }
}
