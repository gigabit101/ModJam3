package vswe.stevesfactory.network;

import com.google.common.base.Preconditions;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.network.*;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import vswe.stevesfactory.api.StevesFactoryManagerAPI;

import java.util.function.*;

public final class NetworkHandler {

    public static final String PROTOCOL_VERSION = Integer.toString(0);
    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(StevesFactoryManagerAPI.MODID, "main_channel"))
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .simpleChannel();

    public static void register() {
        // Client to server
        registerMessage(PacketSyncProcedureGraph.class, PacketSyncProcedureGraph::encode, PacketSyncProcedureGraph::decode, PacketSyncProcedureGraph::handle);
        registerMessage(PacketSyncProcedureGroups.class, PacketSyncProcedureGroups::encode, PacketSyncProcedureGroups::decode, PacketSyncProcedureGroups::handle);
        registerMessage(PacketSyncIntakeData.class, PacketSyncIntakeData::encode, PacketSyncIntakeData::decode, PacketSyncIntakeData::handle);

        // Server to client
        registerMessage(PacketReloadComponentGroups.class, PacketReloadComponentGroups::encode, PacketReloadComponentGroups::decode, PacketReloadComponentGroups::handle);
    }

    public static void sendTo(ServerPlayerEntity player, Object msg) {
        if (!(player instanceof FakePlayer)) {
            CHANNEL.sendTo(msg, player.connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
        }
    }

    public static void sendToServer(Object msg) {
        CHANNEL.sendToServer(msg);
    }

    private static int nextID = 0;

    private static <MSG> void registerMessage(Class<MSG> messageType, BiConsumer<MSG, PacketBuffer> encoder, Function<PacketBuffer, MSG> decoder, BiConsumer<MSG, Supplier<NetworkEvent.Context>> handler) {
        Preconditions.checkState(nextID < 0xFF, "Too many messages!");
        CHANNEL.registerMessage(nextID, messageType, encoder, decoder, handler);
        nextID++;
    }
}
