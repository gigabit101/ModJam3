package vswe.stevesfactory.setup;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.IForgeRegistry;
import vswe.stevesfactory.Config;
import vswe.stevesfactory.api.StevesFactoryManagerAPI;
import vswe.stevesfactory.api.logic.IProcedure;
import vswe.stevesfactory.api.logic.IProcedureType;
import vswe.stevesfactory.api.visibility.GUIVisibility;
import vswe.stevesfactory.logic.SimpleProcedureType;
import vswe.stevesfactory.logic.procedure.*;

import java.util.function.Supplier;

@EventBusSubscriber(modid = StevesFactoryManagerAPI.MODID, bus = Bus.MOD)
public final class ModProcedures {

    private ModProcedures() {
    }

    public static IProcedureType<IntervalTriggerProcedure> intervalTrigger;
    public static IProcedureType<RedstoneTriggerProcedure> redstoneTrigger;
    public static IProcedureType<BUDTriggerProcedure> budTrigger;
    public static IProcedureType<ItemTransferProcedure> itemTransfer;
    public static IProcedureType<ItemImportProcedure> itemImport;
    public static IProcedureType<ItemExportProcedure> itemExport;
    public static IProcedureType<CraftingProcedure> crafting;
    public static IProcedureType<FluidTransferProcedure> fluidTransfer;
    public static IProcedureType<RedstoneEmitterProcedure> redstoneEmitter;
    public static IProcedureType<SignUpdaterProcedure> signUpdater;
    public static IProcedureType<DummyBranchProcedure> sequentialBranch;
    public static IProcedureType<DummyBranchProcedure> mergeBranch;
    public static IProcedureType<FunctionHatProcedure> functionHat;
    public static IProcedureType<FunctionInvokeProcedure> functionInvoke;

    @SubscribeEvent
    public static void onProcedureRegister(RegistryEvent.Register<IProcedureType<?>> event) {
        IForgeRegistry<IProcedureType<?>> r = event.getRegistry();
        r.register(intervalTrigger = create("interval_trigger", IntervalTriggerProcedure::new, Config.COMMON.enableIntervalTrigger));
        r.register(redstoneTrigger = create("redstone_trigger", RedstoneTriggerProcedure::new, Config.COMMON.enableRedstoneTrigger));
        r.register(budTrigger = create("bud_trigger", BUDTriggerProcedure::new, Config.COMMON.enableBUDTrigger));
        r.register(itemTransfer = create("item_transfer", ItemTransferProcedure::new, Config.COMMON.enableItemTransfer));
        r.register(itemImport = create("item_import", ItemImportProcedure::new, Config.COMMON.enableItemImport));
        r.register(itemExport = create("item_export", ItemExportProcedure::new, Config.COMMON.enableItemExport));
        r.register(crafting = create("crafting", CraftingProcedure::new, Config.COMMON.enableCrafting));
        r.register(fluidTransfer = create("fluid_transfer", FluidTransferProcedure::new, Config.COMMON.enableFluidTransfer));
        r.register(redstoneEmitter = create("redstone_emitter", RedstoneEmitterProcedure::new, Config.COMMON.enableRedstoneEmitter));
        r.register(signUpdater = create("sign_updater", SignUpdaterProcedure::new, Config.COMMON.enableSignUpdater));
        r.register(sequentialBranch = create("sequential", DummyBranchProcedure::sequential, Config.COMMON.enableSequentialBranch));
        r.register(mergeBranch = create("merge", DummyBranchProcedure::merge, Config.COMMON.enableMergeBranch));
        r.register(functionHat = create("function_hat", FunctionHatProcedure::functionHat, Config.COMMON.enableFunctionHat));
        r.register(functionInvoke = create("function_invoke", FunctionInvokeProcedure::new, Config.COMMON.enableFunctionInvoke));
    }

    private static <P extends IProcedure> IProcedureType<P> create(String id, Supplier<P> constructor, ForgeConfigSpec.BooleanValue enabledGetter) {
        IProcedureType<P> p = new SimpleProcedureType<>(constructor, new ResourceLocation(StevesFactoryManagerAPI.MODID, "textures/gui/procedure_icon/" + id + ".png"));
        ResourceLocation rl = new ResourceLocation(StevesFactoryManagerAPI.MODID, id);
        p.setRegistryName(rl);
        GUIVisibility.registerEnableState(rl, enabledGetter::get);
        return p;
    }
}
