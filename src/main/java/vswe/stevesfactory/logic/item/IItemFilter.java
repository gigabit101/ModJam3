package vswe.stevesfactory.logic.item;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.items.IItemHandler;
import vswe.stevesfactory.logic.FilterType;

import java.util.List;

public interface IItemFilter {

    boolean test(ItemStack stack);

    void extractFromInventory(List<ItemStack> target, IItemHandler handler, boolean merge);

    FilterType getType();;

    void setType(FilterType type);

    void read(CompoundNBT tag);

    void write(CompoundNBT tag);

    default CompoundNBT write() {
        CompoundNBT tag = new CompoundNBT();
        write(tag);
        return tag;
    }
}
