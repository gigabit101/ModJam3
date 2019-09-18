package vswe.stevesfactory.api.logic;

import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;
import vswe.stevesfactory.api.network.INetworkController;

public interface IProcedureType<P extends IProcedure> extends IForgeRegistryEntry<IProcedureType<?>> {

    /**
     * Create a blank procedure object.
     *
     * @implSpec Created instances must return the same registry name ({@link #getRegistryName()}) as this factory object.
     */
    P createInstance(INetworkController controller);

    /**
     * Retrieve a procedure object from the given compound tag. This should automatically assign the this retrieved procedure to the given
     * controller and to a newly created {@link CommandGraph}.
     */
    P retrieveInstance(INetworkController controller, CompoundNBT tag);

    /**
     * Retrieve procedure object from the given tag, and bind itself to the graph. It is very important to check the parameter graph's state
     * beforehand, since this could create invalid connection state and invalid command graphs.
     */
    P retrieveInstance(CommandGraph graph, CompoundNBT tag);

    /**
     * Get the icon used for component selection panel
     * <p>
     * This should only be called on the client side for GUI purposes only.
     */
    ResourceLocation getIcon();

    String getTranslationKey();

    default String getLocalizedName() {
        return I18n.format(getTranslationKey());
    }

    default String getLocalizedName(Object... args) {
        return I18n.format(getTranslationKey(), args);
    }
}
