package vswe.stevesfactory.logic.item;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.items.IItemHandler;
import vswe.stevesfactory.logic.FilterType;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.IntFunction;

public interface IItemFilter {

    boolean test(ItemStack stack);

    boolean isMatchingAmount();

    void setMatchingAmount(boolean matchingAmount);

    void extractFromInventory(List<ItemStack> target, IItemHandler handler, boolean merge);

    void extractFromInventory(BiConsumer<ItemStack, Integer> receiver, IItemHandler handler);

    FilterType getType();

    void setType(FilterType type);

    void read(CompoundNBT tag);

    void write(CompoundNBT tag);

    default CompoundNBT write() {
        CompoundNBT tag = new CompoundNBT();
        write(tag);
        return tag;
    }

    int limitFlowRate(ItemStack buffered, int existingCount);

    String getTypeID();

    final class ItemFilters {

        private ItemFilters() {
        }

        private static final Map<String, Function<CompoundNBT, IItemFilter>> deserializers = new HashMap<>();

        public static void register(String typeID, Function<CompoundNBT, IItemFilter> deserializer) {
            Preconditions.checkState(!deserializers.containsKey(typeID), "Filter type name already in use: " + typeID);
            deserializers.put(typeID, deserializer);
        }

        @Nullable
        public static Function<CompoundNBT, IItemFilter> getDeserializerFor(String typeID) {
            return deserializers.get(typeID);
        }
    }
}
