package vswe.stevesfactory.blocks;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.nbt.StringNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import org.apache.logging.log4j.Logger;
import vswe.stevesfactory.Config;
import vswe.stevesfactory.StevesFactoryManager;
import vswe.stevesfactory.api.StevesFactoryManagerAPI;
import vswe.stevesfactory.api.logic.Connection;
import vswe.stevesfactory.api.logic.IProcedure;
import vswe.stevesfactory.api.logic.IProcedureType;
import vswe.stevesfactory.api.logic.ProcedureGraph;
import vswe.stevesfactory.api.network.ICable;
import vswe.stevesfactory.api.network.INetworkController;
import vswe.stevesfactory.logic.execution.VariableDefinition;
import vswe.stevesfactory.network.NetworkHandler;
import vswe.stevesfactory.network.PacketSyncProcedureGraph;
import vswe.stevesfactory.network.PacketSyncProcedureGroups;
import vswe.stevesfactory.setup.ModBlocks;
import vswe.stevesfactory.ui.manager.FactoryManagerContainer;
import vswe.stevesfactory.utils.IOHelper;
import vswe.stevesfactory.utils.NetworkHelper;
import vswe.stevesfactory.utils.Utils;

import javax.annotation.Nullable;
import java.util.*;

public class FactoryManagerTileEntity extends TileEntity implements ITickableTileEntity, INamedContainerProvider, INetworkController, ICable {

    private Set<BlockPos> connectedCables = new HashSet<>();
    private Map<String, Multiset<BlockPos>> linkedInventories = new HashMap<>();

    private ProcedureGraph graph = ProcedureGraph.create();
    private Set<VariableDefinition<?>> variableDefinitions = new HashSet<>();
    private Set<String> groups = new HashSet<>();

    private int ticks;

    public FactoryManagerTileEntity() {
        super(ModBlocks.factoryManagerTileEntity);
        groups.add("");
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
            graph.tick(this);

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
        markDirty();
    }

    public void activate(PlayerEntity player) {
        assert world != null;
        Preconditions.checkState(!world.isRemote);
        StevesFactoryManager.logger.trace("Player {} activated a factory manager at {}", player, pos);

        search();
        markDirty();

        FactoryManagerContainer.openGUI((ServerPlayerEntity) player, this);
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
        for (Direction direction : Utils.DIRECTIONS) {
            BlockPos neighbor = center.offset(direction);
            TileEntity tile = world.getTileEntity(neighbor);
            if (tile instanceof ICable && !connectedCables.contains(neighbor)) {
                ICable cable = (ICable) tile;
                if (cable.isCable()) {
                    addCableToNetwork(cable, neighbor);
                    // Recursive search (DFS)
                    search(neighbor, depth + 1);
                }
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

        for (Map.Entry<String, Multiset<BlockPos>> entry : linkedInventories.entrySet()) {
            String capName = entry.getKey();
            Multiset<BlockPos> set = entry.getValue();
            logger.debug("Linked inventories ({}):", capName);
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
        markDirty();
        assert world != null;
        connectedCables.remove(cable);
    }

    public <T> Multiset<BlockPos> getInventoryMultiset(@Nullable Capability<T> cap) {
        String capName = cap == null ? "" : cap.getName();
        Multiset<BlockPos> multiset = linkedInventories.get(capName);
        if (multiset == null) {
            Multiset<BlockPos> newSet = HashMultiset.create();
            linkedInventories.put(capName, newSet);
            return newSet;
        }
        return multiset;
    }

    @Override
    public <T> Set<BlockPos> getLinkedInventories(@Nullable Capability<T> cap) {
        return getInventoryMultiset(cap).elementSet();
    }

    /**
     * @return {@code true} always. See {@link Multiset#add(Object)} for details.
     */
    @Override
    public <T> boolean addLink(Capability<T> cap, BlockPos pos) {
        markDirty();
        StevesFactoryManager.logger.trace("Added link");
        return getInventoryMultiset(cap).add(pos);
    }

    @Override
    public <T> boolean addLinks(Capability<T> cap, Collection<BlockPos> poses) {
        markDirty();
        StevesFactoryManager.logger.trace("Added {} links", poses.size());
        return getInventoryMultiset(cap).addAll(poses);
    }

    @Override
    public void removeAllLinks() {
        markDirty();
        linkedInventories.clear();
    }

    @Override
    public Collection<VariableDefinition<?>> getVariableDefinitions() {
        return variableDefinitions;
    }

    @Override
    public ProcedureGraph getPGraph() {
        return graph;
    }

    public void setPGraph(ProcedureGraph graph) {
        this.graph.invalidateContent();
        this.graph = graph;
    }

    @Override
    public Set<String> getGroups() {
        return groups;
    }

    /**
     * @return {@code true} always. See {@link Multiset#add(Object)} for details.
     */
    @Override
    public <T> boolean removeLink(Capability<T> cap, BlockPos pos) {
        markDirty();
        StevesFactoryManager.logger.trace("Removed link");
        return getInventoryMultiset(cap).remove(pos);
    }

    @Override
    public void addLinksFor(INetworkController controller) {
        markDirty();
        NetworkHelper.updateLinksFor(controller, this);
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
    public World getControllerWorld() {
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
            NetworkHandler.sendToServer(new PacketSyncProcedureGraph(getDimension(), getPosition(), graph));
            NetworkHandler.sendToServer(new PacketSyncProcedureGroups(getDimension(), getPosition(), groups));
        }
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(pos, 0, write(new CompoundNBT()));
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        read(pkt.getNbtCompound());
    }

    @Override
    public void read(CompoundNBT compound) {
        StevesFactoryManager.logger.trace("Restoring data from NBT {}", compound);
        super.read(compound);
        readCustom(compound);
    }

    public void readCustom(CompoundNBT compound) {
        connectedCables.clear();
        IOHelper.readBlockPoses(compound.getList("ConnectedCables", Constants.NBT.TAG_COMPOUND), connectedCables);

        linkedInventories.clear();
        ListNBT serializedInventories = compound.getList("LinkedInventories", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < serializedInventories.size(); i++) {
            CompoundNBT element = serializedInventories.getCompound(i);

            ListNBT serializedPoses = element.getList("Positions", Constants.NBT.TAG_COMPOUND);
            Multiset<BlockPos> set = HashMultiset.create();
            for (int j = 0; j < serializedPoses.size(); j++) {
                set.add(NBTUtil.readBlockPos(serializedPoses.getCompound(j)));
            }

            String name = element.getString("Name");
            linkedInventories.put(name, set);
        }

        // TODO remove for release
        // Migrating from old format
        int format = compound.getInt("FormatVer");
        switch (format) {
            case 0:
                setPGraph(ProcedureGraph.create());
                ListNBT graphNBT = compound.getList("CommandGraphs", Constants.NBT.TAG_COMPOUND);
                for (int i = 0; i < graphNBT.size(); i++) {
                    CompoundNBT tag = graphNBT.getCompound(i);

                    ListNBT nodesNBT = tag.getList("Nodes", Constants.NBT.TAG_COMPOUND);
                    Int2ObjectMap<IProcedure> nodes = new Int2ObjectOpenHashMap<>();
                    for (int j = 0; j < nodesNBT.size(); j++) {
                        CompoundNBT nodeNBT = nodesNBT.getCompound(j);
                        int id = nodeNBT.getInt("ID");
                        IProcedure node;
                        {
                            CompoundNBT dataNBT = nodeNBT.getCompound("NodeData");
                            ResourceLocation loc = new ResourceLocation(dataNBT.getString("ID"));
                            IProcedureType<?> p = StevesFactoryManagerAPI.getProceduresRegistry().getValue(loc);
                            node = Objects.requireNonNull(p).retrieveInstance(dataNBT);
                        }
                        nodes.put(id, node);
                        graph.addProcedure(node);
                    }

                    ListNBT connectionNBT = tag.getList("Connections", Constants.NBT.TAG_COMPOUND);
                    for (int j = 0; j < connectionNBT.size(); j++) {
                        CompoundNBT nbt = connectionNBT.getCompound(j);
                        IProcedure from = nodes.get(nbt.getInt("From"));
                        int fromOut = nbt.getInt("FromOut");
                        IProcedure to = nodes.get(nbt.getInt("To"));
                        int toIn = nbt.getInt("ToIn");
                        Connection.create(from, fromOut, to, toIn);
                    }
                }
                return;
        }

        graph.deserialize(compound.getCompound("Procedures"));

        ListNBT serializedVarDefs = compound.getList("VarDefinitions", Constants.NBT.TAG_COMPOUND);
        variableDefinitions.clear();
        for (int i = 0; i < serializedVarDefs.size(); i++) {
            variableDefinitions.add(VariableDefinition.read(serializedVarDefs.getCompound(i)));
        }

        ListNBT serializedGroups = compound.getList("Groups", Constants.NBT.TAG_STRING);
        groups.clear();
        for (int i = 0; i < serializedGroups.size(); i++) {
            groups.add(serializedGroups.getString(i));
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        StevesFactoryManager.logger.trace("Writing data into NBT ({})", pos);
        writeCustom(compound);
        return super.write(compound);
    }

    public CompoundNBT writeCustom(CompoundNBT compound) {
        // Version 0: Singular command graphs
        // Version 1: Global procedure graphs
        compound.putInt("FormatVer", 1);

        compound.put("ConnectedCables", IOHelper.writeBlockPoses(connectedCables));

        ListNBT serializedInventories = new ListNBT();
        for (Map.Entry<String, Multiset<BlockPos>> entry : linkedInventories.entrySet()) {
            String capName = entry.getKey();
            Multiset<BlockPos> set = entry.getValue();

            ListNBT serializedPoses = new ListNBT();
            for (BlockPos pos : set) {
                serializedPoses.add(NBTUtil.writeBlockPos(pos));
            }

            CompoundNBT element = new CompoundNBT();
            element.putString("Name", capName);
            element.put("Positions", serializedPoses);
            serializedInventories.add(element);
        }
        compound.put("LinkedInventories", serializedInventories);
        compound.put("Procedures", graph.serialize());

        ListNBT serializedVarDefs = new ListNBT();
        for (VariableDefinition<?> def : variableDefinitions) {
            serializedVarDefs.add(def.write());
        }
        compound.put("VarDefinitions", serializedVarDefs);

        ListNBT serializedGroups = new ListNBT();
        for (String group : groups) {
            serializedGroups.add(StringNBT.valueOf(group));
        }
        compound.put("Groups", serializedGroups);

        return compound;
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TranslationTextComponent("gui.sfm.Title.FactoryManager");
    }

    @Override
    public Container createMenu(int i, PlayerInventory inv, PlayerEntity player) {
        return new FactoryManagerContainer(i, this);
    }
}
