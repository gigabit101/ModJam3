package vswe.stevesfactory.api.item;

import net.minecraft.item.ItemStack;

public interface IItemBufferElement {

    ItemStack getStack();

    void setStack(ItemStack stack);

    int getUsed();

    void use(int amount);

    void put(int amount);

    void cleanup();
}
