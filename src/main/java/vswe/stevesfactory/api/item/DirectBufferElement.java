package vswe.stevesfactory.api.item;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashSet;
import java.util.Set;

public class DirectBufferElement implements IItemBufferElement {

    public final Set<Pair<IItemHandler, Integer>> inventories = new HashSet<>();

    public ItemStack stack;
    public int used = 0;

    public DirectBufferElement(ItemStack stack) {
        this.stack = stack;
    }

    @SuppressWarnings("UnusedReturnValue")
    public DirectBufferElement addInventory(IItemHandler handler, int slot) {
        inventories.add(Pair.of(handler, slot));
        return this;
    }

    @Override
    public ItemStack getStack() {
        return stack;
    }

    @Override
    public void setStack(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public int getUsed() {
        return used;
    }

    @Override
    public void use(int amount) {
        used += amount;
    }

    @Override
    public void put(int amount) {
        used -= amount;
    }

    @Override
    public void cleanup() {
        if (used > 0) {
            for (Pair<IItemHandler, Integer> pair : inventories) {
                IItemHandler handler = pair.getLeft();
                int slot = pair.getRight();
                ItemStack extracted = handler.extractItem(slot, used, false);
                used -= extracted.getCount();
            }
        }
    }
}
