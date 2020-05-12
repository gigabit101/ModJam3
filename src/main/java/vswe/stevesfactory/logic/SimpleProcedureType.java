package vswe.stevesfactory.logic;

import com.google.common.base.Preconditions;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;
import vswe.stevesfactory.api.logic.IProcedure;
import vswe.stevesfactory.api.logic.IProcedureType;
import vswe.stevesfactory.api.network.INetworkController;

import javax.annotation.Nonnull;
import java.util.function.*;

public class SimpleProcedureType<P extends IProcedure> extends ForgeRegistryEntry<IProcedureType<?>> implements IProcedureType<P> {

    private final Supplier<P> constructor;
    private final ResourceLocation icon;
    private String translationKey = null;

    public SimpleProcedureType(Function<IProcedureType<P>, P> constructor, ResourceLocation icon) {
        this.constructor = () -> constructor.apply(this);
        this.icon = icon;
    }

    public SimpleProcedureType(Supplier<P> constructor, ResourceLocation icon) {
        this.constructor = constructor;
        this.icon = icon;
    }

    @Override
    public P createInstance() {
        return constructor.get();
    }

    @Override
    public P retrieveInstance(CompoundNBT tag) {
        P procedure = constructor.get();
        procedure.deserialize(tag);
        return procedure;
    }

    @Override
    public ResourceLocation getIcon() {
        return icon;
    }

    @Override
    public String getTranslationKey() {
        if (translationKey == null) {
            translationKey = "logic." + getRegistryNameNonnull().toString().replace(':', '.');
        }
        return translationKey;
    }

    @Nonnull
    public ResourceLocation getRegistryNameNonnull() {
        ResourceLocation id = super.getRegistryName();
        Preconditions.checkState(id != null, "Procedure type ${this} does not have a registry name!");
        return id;
    }
}