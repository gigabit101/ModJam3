package vswe.stevesfactory.logic;

import com.google.common.base.Preconditions;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;
import vswe.stevesfactory.api.logic.*;
import vswe.stevesfactory.api.network.INetworkController;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.BiFunction;
import java.util.function.Function;

public class SimpleProcedureType<P extends IProcedure> extends ForgeRegistryEntry<IProcedureType<?>> implements IProcedureType<P> {

    private final Function<INetworkController, P> constructor;
    private final Function<CommandGraph, P> retriever;
    private final ResourceLocation icon;
    private String translationKey = null;

    public SimpleProcedureType(BiFunction<IProcedureType<P>, INetworkController, P> constructor, @Nullable Function<CommandGraph, P> retriever, ResourceLocation icon) {
        this.constructor = c -> constructor.apply(this, c);
        this.retriever = retriever;
        this.icon = icon;
    }

    public SimpleProcedureType(Function<INetworkController, P> constructor, @Nullable Function<CommandGraph, P> retriever, ResourceLocation icon) {
        this.constructor = constructor;
        this.retriever = retriever;
        this.icon = icon;
    }

    @Override
    public P createInstance(INetworkController controller) {
        return constructor.apply(controller);
    }

    private P createAndDeserialize(INetworkController controller, CompoundNBT tag) {
        P procedure = createInstance(controller);
        procedure.deserialize(tag);
        return procedure;
    }

    @Override
    public P retrieveInstance(INetworkController controller, CompoundNBT tag) {
        Preconditions.checkArgument(getRegistryNameNonnull().toString().equals(tag.getString("ID")));
        return createAndDeserialize(controller, tag);
    }

    @Override
    public P retrieveInstance(CommandGraph graph, CompoundNBT tag) {
        Preconditions.checkArgument(getRegistryNameNonnull().toString().equals(tag.getString("ID")));
        if (retriever == null) {
            return createAndDeserialize(graph.getController(), tag);
        } else {
            P procedure = retriever.apply(graph);
            procedure.deserialize(tag);
            return procedure;
        }
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
        Preconditions.checkState(id != null, "Procedure type " + this + " does not have a registry name!");
        return id;
    }
}