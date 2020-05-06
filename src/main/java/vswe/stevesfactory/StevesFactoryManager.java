package vswe.stevesfactory;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import vswe.stevesfactory.api.StevesFactoryManagerAPI;
import vswe.stevesfactory.network.NetworkHandler;
import vswe.stevesfactory.network.PacketReloadComponentGroups;
import vswe.stevesfactory.setup.ModBlocks;
import vswe.stevesfactory.setup.ModContainers;
import vswe.stevesfactory.setup.ModItems;
import vswe.stevesfactory.ui.manager.selection.ComponentGroup;

@Mod(StevesFactoryManagerAPI.MODID)
public class StevesFactoryManager {

    public static final Logger logger = LogManager.getLogger(StevesFactoryManagerAPI.MODID);

    public static StevesFactoryManager instance;

    public StevesFactoryManager() {
        instance = this;
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON_SPEC);
        {
            IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
            bus.addListener(this::setup);
            bus.addListener(this::finishLoading);
            bus.addListener(Config::onLoad);
            DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> bus.addListener(this::clientSetup));
            ModBlocks.blocks.register(bus);
            ModBlocks.tiles.register(bus);
            ModItems.items.register(bus);
        }
        {
            IEventBus bus = MinecraftForge.EVENT_BUS;
            bus.addListener(this::serverStarting);
            DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> bus.addListener(ClientEventHandler::onPlayerLoggedIn));
        }
    }

    private void setup(final FMLCommonSetupEvent event) {
        NetworkHandler.register();
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        ModContainers.registerFactories();
        ComponentGroup.reload(false);
    }

    private void finishLoading(final FMLLoadCompleteEvent event) {
        ModBlocks.blocks = null;
        ModBlocks.tiles = null;
        ModItems.items = null;
    }

    private void serverStarting(final FMLServerStartingEvent event) {
        LiteralArgumentBuilder<CommandSource> builder = Commands.literal(StevesFactoryManagerAPI.MODID)
                .then(componentGroupsCommand());
        event.getCommandDispatcher().register(builder);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Commands
    ///////////////////////////////////////////////////////////////////////////

    private static LiteralArgumentBuilder<CommandSource> componentGroupsCommand() {
        return Commands.literal("componentGroups")
                .then(Commands.literal("reload")
                        .requires(source -> source.getEntity() instanceof ServerPlayerEntity)
                        .executes(context -> {
                            ServerPlayerEntity client = context.getSource().asPlayer();
                            PacketReloadComponentGroups.reload(client);
                            return 0;
                        }))
                .then(Commands.literal("reset")
                        .requires(source -> source.getEntity() instanceof ServerPlayerEntity)
                        .executes(context -> {
                            ServerPlayerEntity client = context.getSource().asPlayer();
                            PacketReloadComponentGroups.reset(client);
                            return 0;
                        }));
    }
}
