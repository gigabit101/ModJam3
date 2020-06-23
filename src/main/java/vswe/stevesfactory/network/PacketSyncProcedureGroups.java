package vswe.stevesfactory.network;

import com.google.common.base.Preconditions;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.fml.network.NetworkEvent;
import vswe.stevesfactory.StevesFactoryManager;
import vswe.stevesfactory.blocks.FactoryManagerTileEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class PacketSyncProcedureGroups {

    public static void encode(PacketSyncProcedureGroups msg, PacketBuffer buf) {
        buf.writeResourceLocation(Objects.requireNonNull(msg.dimension.getRegistryName()));
        buf.writeBlockPos(msg.pos);
        buf.writeVarInt(msg.groups.size());
        for (String group : msg.groups) {
            buf.writeString(group);
        }
    }

    public static PacketSyncProcedureGroups decode(PacketBuffer buf) {
        DimensionType dimension = DimensionType.byName(buf.readResourceLocation());
        BlockPos pos = buf.readBlockPos();
        int size = buf.readVarInt();
        List<String> groups = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            groups.add(buf.readString(32767));
        }
        return new PacketSyncProcedureGroups(dimension, pos, groups);
    }

    public static void handle(PacketSyncProcedureGroups msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity sender = ctx.get().getSender();
            Preconditions.checkState(sender != null, "Invalid usage of a client to server packet");

            World world = sender.world;
            TileEntity tile = world.getTileEntity(msg.pos);
            if (tile instanceof FactoryManagerTileEntity) {
                FactoryManagerTileEntity manager = (FactoryManagerTileEntity) tile;
                manager.getGroups().clear();
                manager.getGroups().addAll(msg.groups);
            } else {
                StevesFactoryManager.logger.error("Received packet with invalid controller position {}!", msg);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private DimensionType dimension;
    private BlockPos pos;

    private Collection<String> groups;

    public PacketSyncProcedureGroups(DimensionType dimension, BlockPos pos, Collection<String> groups) {
        this.dimension = dimension;
        this.pos = pos;
        this.groups = groups;
    }
}
