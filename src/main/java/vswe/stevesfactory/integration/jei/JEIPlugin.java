package vswe.stevesfactory.integration.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.MethodsReturnNonnullByDefault;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import net.minecraft.util.ResourceLocation;
import vswe.stevesfactory.api.StevesFactoryManagerAPI;
import vswe.stevesfactory.ui.intake.ItemIntakeGUI;
import vswe.stevesfactory.ui.manager.FactoryManagerGUI;

import javax.annotation.ParametersAreNonnullByDefault;

@JeiPlugin
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class JEIPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(StevesFactoryManagerAPI.MODID, "jei");
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGuiContainerHandler(FactoryManagerGUI.class, new JEIContainerHandler<>());
        registration.addGuiContainerHandler(ItemIntakeGUI.class, new JEIContainerHandler<>());
    }
}
