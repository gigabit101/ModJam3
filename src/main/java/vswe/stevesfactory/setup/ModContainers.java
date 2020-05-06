package vswe.stevesfactory.setup;

import net.minecraft.client.gui.ScreenManager;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.IForgeRegistry;
import vswe.stevesfactory.api.StevesFactoryManagerAPI;
import vswe.stevesfactory.ui.intake.ItemIntakeContainer;
import vswe.stevesfactory.ui.intake.ItemIntakeGUI;
import vswe.stevesfactory.ui.manager.FactoryManagerContainer;
import vswe.stevesfactory.ui.manager.FactoryManagerGUI;

@EventBusSubscriber(modid = StevesFactoryManagerAPI.MODID, bus = Bus.MOD)
public final class ModContainers {

    private ModContainers() {
    }

    public static ContainerType<FactoryManagerContainer> factoryManagerContainer = IForgeContainerType.create(FactoryManagerContainer::new);
    public static ContainerType<ItemIntakeContainer> itemIntakeContainer = IForgeContainerType.create(ItemIntakeContainer::new);

    @SubscribeEvent
    public static void registerContainers(RegistryEvent.Register<ContainerType<?>> event) {
        IForgeRegistry<ContainerType<?>> r = event.getRegistry();
        r.register(factoryManagerContainer.setRegistryName(new ResourceLocation(StevesFactoryManagerAPI.MODID, "factory_manager")));
        r.register(itemIntakeContainer.setRegistryName(new ResourceLocation(StevesFactoryManagerAPI.MODID, "item_intake")));
    }

    public static void registerFactories() {
        ScreenManager.registerFactory(factoryManagerContainer, FactoryManagerGUI::new);
        ScreenManager.registerFactory(itemIntakeContainer, ItemIntakeGUI::new);
    }
}
