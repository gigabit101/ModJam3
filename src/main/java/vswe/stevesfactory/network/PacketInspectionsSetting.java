package vswe.stevesfactory.network;

import com.google.common.base.Preconditions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import vswe.stevesfactory.library.gui.debug.Inspections;

import java.util.function.Supplier;

public final class PacketInspectionsSetting {

    public static final String NAME = "inspectionsOverlay";

    public static void query(ServerPlayerEntity client) {
        NetworkHandler.sendTo(client, new PacketInspectionsSetting(Mode.QUERY, false));
    }

    public static void set(ServerPlayerEntity client, boolean value) {
        NetworkHandler.sendTo(client, new PacketInspectionsSetting(Mode.SET, value));
    }

    public enum Mode {
        QUERY, SET;

        public static final Mode[] VALUES = values();
    }

    public static void encode(PacketInspectionsSetting msg, PacketBuffer buf) {
        buf.writeInt(msg.mode.ordinal());
        buf.writeBoolean(msg.value);
    }

    public static PacketInspectionsSetting decode(PacketBuffer buf) {
        Mode mode = Mode.VALUES[buf.readInt()];
        boolean value = buf.readBoolean();
        return new PacketInspectionsSetting(mode, value);
    }

    public static void handle(PacketInspectionsSetting msg, Supplier<NetworkEvent.Context> ctx) {
        Preconditions.checkState(ctx.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT);
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> handleClient(msg, ctx));
    }

    @OnlyIn(Dist.CLIENT)
    public static void handleClient(PacketInspectionsSetting msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            switch (msg.mode) {
                case QUERY:
                    Minecraft.getInstance().player.sendMessage(new StringTextComponent(I18n.format("message.sfm.settings.query", NAME, Inspections.enabled)));
                    break;
                case SET:
                    Inspections.enabled = msg.value;
                    Minecraft.getInstance().player.sendMessage(new StringTextComponent(I18n.format("message.sfm.settings.set", NAME, Inspections.enabled)));
                    break;
            }
            ctx.get().setPacketHandled(true);
        });
    }

    private Mode mode;
    private boolean value;

    public PacketInspectionsSetting(Mode mode, boolean value) {
        this.mode = mode;
        this.value = value;
    }
}