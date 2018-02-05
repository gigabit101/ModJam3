package vswe.stevesfactory.wrappers;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.items.IItemHandler;

/**
 * Created by Gigabit101 on 01/12/2016.
 */
public class InventoryHelper
{
    //Gets the inventory type
    public EnumInventoryType getType(TileEntity tileEntity)
    {
        if(tileEntity instanceof IInventory)
        {
            return EnumInventoryType.IInventory;
        }
        else if(CapabilityHelper.hasItemCapabilitySafe(tileEntity))
        {
            return EnumInventoryType.Capability;
        }
        return null;
    }

    public ItemStack getStackInSlot(TileEntity tileEntity, int id)
    {
        if(getType(tileEntity) == EnumInventoryType.IInventory)
        {
            return ((IInventory) tileEntity).getStackInSlot(id);
        }
        else if(getType(tileEntity) == EnumInventoryType.Capability)
        {
            ((IItemHandler) tileEntity).getStackInSlot(id);
        }
        return null;
    }

    public boolean isItemValidForSlot(TileEntity tileEntity, ItemStack stack, int id)
    {
        if(getType(tileEntity) == EnumInventoryType.IInventory)
        {
            return ((IInventory) tileEntity).isItemValidForSlot(id, stack);
        }
        else if(getType(tileEntity) == EnumInventoryType.Capability)
        {
            return ((IItemHandler) tileEntity).insertItem(id, stack, true) != stack;
        }
        return false;
    }

    public boolean isSlotEmpty(TileEntity tileEntity, int id)
    {
        if(getType(tileEntity) == EnumInventoryType.IInventory)
        {
            return ((IInventory) tileEntity).getStackInSlot(id).isEmpty();
        }
        if(getType(tileEntity) == EnumInventoryType.Capability)
        {
            return ((IItemHandler) tileEntity).getStackInSlot(id).isEmpty();
        }
        return false;
    }

    //TODO decrease stack in slot
}
