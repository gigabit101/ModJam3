package vswe.stevesfactory.logic;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import vswe.stevesfactory.Config;
import vswe.stevesfactory.StevesFactoryManager;
import vswe.stevesfactory.api.logic.IProcedure;
import vswe.stevesfactory.api.logic.IProcedureType;
import vswe.stevesfactory.library.gui.RenderingHelper;
import vswe.stevesfactory.logic.procedure.*;
import vswe.stevesfactory.utils.NetworkHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@EventBusSubscriber(modid = StevesFactoryManager.MODID, bus = Bus.MOD)
public final class Procedures<P extends IProcedure> {

    public static final Procedures<IntervalTriggerProcedure> INTERVAL_TRIGGER = new Procedures<>("interval_trigger", IntervalTriggerProcedure::new, Config.COMMON.enableIntervalTrigger);
    public static final Procedures<ItemTransferProcedure> ITEM_TRANSFER = new Procedures<>("item_transfer", ItemTransferProcedure::new, Config.COMMON.enableItemTransfer);
    public static final Procedures<ItemImportProcedure> ITEM_IMPORT = new Procedures<>("item_import", ItemImportProcedure::new, Config.COMMON.enableItemImport);
    public static final Procedures<ItemExportProcedure> ITEM_EXPORT = new Procedures<>("item_export", ItemExportProcedure::new, Config.COMMON.enableItemExport);
    public static final Procedures<CraftingProcedure> CRAFTING = new Procedures<>("crafting", CraftingProcedure::new, Config.COMMON.enableCrafting);

//    ITEM_CONDITION("item_condition", DummyProcedure::new),
//    FLOW_CONTROL("flow_control", DummyProcedure::new),
//    FLUID_IMPORT("fluid_import", DummyProcedure::new),
//    FLUID_EXPORT("fluid_export", DummyProcedure::new),
//    FLUID_CONDITION("fluid_condition", DummyProcedure::new),
//    REDSTONE_EMITTER("redstone_emitter", DummyProcedure::new),
//    REDSTONE_CONDITION("redstone_condition", DummyProcedure::new),
//    CRAFT_ITEM("craft_item", DummyProcedure::new),
//    FOR_EACH("for_each", DummyProcedure::new),
//    GROUP("group", DummyProcedure::new),
//    GROUP_IO("group_io", DummyProcedure::new),
//    CAMOUFLAGE("camouflage", DummyProcedure::new),
//    SIGN_UPDATER("sign_updater", DummyProcedure::new),
//    CONFIGURATIONS("configurations", DummyProcedure::new),

    public static final Map<ResourceLocation, Procedures<?>> map = new HashMap<>();

    static {
        map.put(INTERVAL_TRIGGER.getRegistryName(), INTERVAL_TRIGGER);
        map.put(ITEM_TRANSFER.getRegistryName(), ITEM_TRANSFER);
        map.put(ITEM_IMPORT.getRegistryName(), ITEM_IMPORT);
        map.put(ITEM_EXPORT.getRegistryName(), ITEM_EXPORT);
        map.put(CRAFTING.getRegistryName(), CRAFTING);
    }

    public final String id;
    public final SimpleProcedureType<P> factory;
    public final ForgeConfigSpec.BooleanValue enabled;

    private Procedures(String id, Supplier<P> rawConstructor, ForgeConfigSpec.BooleanValue enabled) {
        this.id = id;
        this.factory = new SimpleProcedureType<>(NetworkHelper.wrapConstructor(rawConstructor), rawConstructor, RenderingHelper.linkTexture("gui/component_icon", id + ".png"));
        this.factory.setRegistryName(new ResourceLocation(StevesFactoryManager.MODID, id));
        this.enabled = enabled;
    }

    public String getPathComponent() {
        return id;
    }

    public ResourceLocation getTexture() {
        return factory.getIcon();
    }

    public ResourceLocation getRegistryName() {
        return factory.getRegistryName();
    }

    public IProcedureType<?> getFactory() {
        return factory;
    }

    public boolean isEnabled() {
        return enabled.get();
    }

    @SubscribeEvent
    public static void onProcedureRegister(RegistryEvent.Register<IProcedureType<?>> event) {
        for (Procedures<?> procedure : map.values()) {
            event.getRegistry().register(procedure.getFactory());
        }
    }

    public static boolean isEnabled(IProcedureType<?> type) {
        return isEnabled(type.getRegistryName());
    }

    public static boolean isEnabled(ResourceLocation registryName) {
        return map.get(registryName).isEnabled();
    }
}
