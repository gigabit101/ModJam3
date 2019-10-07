package vswe.stevesfactory.utils;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.RecipeItemHelper;
import net.minecraft.util.NonNullList;

// TODO use accesstransformer
public class MyCraftingInventory extends CraftingInventory {

    private final NonNullList<ItemStack> handle = NonNullList.withSize(3 * 3, ItemStack.EMPTY);

    public MyCraftingInventory() {
        super(null, 3, 3);
    }

    @Override
    public int getSizeInventory() {
        return handle.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : handle) {
            if (stack.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return slot >= getSizeInventory() ? ItemStack.EMPTY : handle.get(slot);
    }

    @Override
    public ItemStack removeStackFromSlot(int p_70304_1_) {
        return ItemStackHelper.getAndRemove(handle, p_70304_1_);
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        return ItemStackHelper.getAndSplit(handle, slot, amount);
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        handle.set(slot, stack);
    }

    @Override
    public void clear() {
        handle.clear();
    }

    @Override
    public void fillStackedContents(RecipeItemHelper itemHelper) {
        for (ItemStack stack : handle) {
            itemHelper.accountPlainStack(stack);
        }
    }
}
