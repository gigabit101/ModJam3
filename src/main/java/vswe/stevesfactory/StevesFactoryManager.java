package vswe.stevesfactory;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.*;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import vswe.stevesfactory.library.gui.debug.Inspections;
import vswe.stevesfactory.setup.ModBlocks;
import vswe.stevesfactory.setup.ModItems;

@Mod(StevesFactoryManager.MODID)
public class StevesFactoryManager {

    public static final String MODID = "sfm";
    public static final String NAME = "Steve's Factory Manager";
    public static final String VERSION = "3.0.0";

    public static final Logger logger = LogManager.getLogger(MODID);

    public static StevesFactoryManager instance;

    public StevesFactoryManager() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        eventBus.addListener(this::setup);
        eventBus.addListener(this::loadComplete);
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> eventBus.addListener(this::clientSetup));

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.addListener(this::serverStarting);

        ModBlocks.init();
        ModItems.init();
    }

    private void setup(final FMLCommonSetupEvent event) {
        instance = (StevesFactoryManager) ModLoadingContext.get().getActiveContainer().getMod();
    }

    private void clientSetup(final FMLClientSetupEvent event) {
    }

    private void loadComplete(final FMLLoadCompleteEvent event) {
        ModBlocks.finishLoading();
        ModItems.finishLoading();
    }

    private void serverStarting(final FMLServerStartingEvent event) {
        event.getCommandDispatcher().register(Commands.literal(MODID)
                .then(settingsCommand()));
    }

    ///////////////////////////////////////////////////////////////////////////
    // Commands
    ///////////////////////////////////////////////////////////////////////////

    private static LiteralArgumentBuilder<CommandSource> settingsCommand() {
        return Commands.literal("settings")
                .then(inspectionBoxHighlighting());
    }

    private static LiteralArgumentBuilder<CommandSource> inspectionBoxHighlighting() {
        return Commands
                .literal("InspectionBoxHighlighting")
                .executes(context -> {
                    // Query setting
                    context.getSource().sendFeedback(new StringTextComponent("Entry InspectionBoxHighlighting is currently set to: " + Inspections.enabled), true);
                    return 0;
                })
                .then(Commands
                        .argument("value", BoolArgumentType.bool())
                        .executes(context -> {
                            // Set setting
                            Inspections.enabled = BoolArgumentType.getBool(context, "value");
                            context.getSource().sendFeedback(new StringTextComponent("Entry InspectionBoxHighlighting is now set to " + Inspections.enabled), true);
                            return 0;
                        }));
    }
}
