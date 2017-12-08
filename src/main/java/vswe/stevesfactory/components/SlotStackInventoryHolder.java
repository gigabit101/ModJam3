package vswe.stevesfactory.components;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class SlotStackInventoryHolder implements IItemBufferSubElement
{
    private ItemStack itemStack;
    private IItemHandler inventory;
    private int slot;
    private int sizeLeft;

    public SlotStackInventoryHolder(ItemStack itemStack, IItemHandler inventory, int slot)
    {
        this.itemStack = itemStack;
        this.inventory = inventory;
        this.slot = slot;
        this.sizeLeft = itemStack.stackSize;
    }

    public ItemStack getItemStack()
    {
        return itemStack;
    }


    public IItemHandler getInventory()
    {
        return inventory;
    }


    public int getSlot()
    {
        return slot;
    }

    @Override
    public void remove()
    {
        if (itemStack.stackSize == 0)
        {
            getInventory().insertItem(getSlot(), null, false);
        }
    }

    @Override
    public void onUpdate(){}

    public int getSizeLeft()
    {
        return Math.min(itemStack.stackSize, sizeLeft);
    }

    public void reduceAmount(int val)
    {
        int stackSize = itemStack.stackSize;
        
        ItemStack extractStack = inventory.extractItem(getSlot(), val, false);
        int extractSize  = (extractStack != null) ? extractStack.stackSize : 0;
        
        if(extractSize > 0 && stackSize == itemStack.stackSize)
        {
            itemStack.stackSize -= extractSize;
        }
        sizeLeft -= extractSize;
    }

    public SlotStackInventoryHolder getSplitElement(int elementAmount, int id, boolean fair)
    {
        SlotStackInventoryHolder element = new SlotStackInventoryHolder(this.itemStack, this.inventory, this.slot);
        int oldAmount = getSizeLeft();
        int amount = oldAmount / elementAmount;
        if (!fair)
        {
            int amountLeft = oldAmount % elementAmount;
            if (id < amountLeft)
            {
                amount++;
            }
        }

        element.sizeLeft = amount;
        return element;
    }
}
