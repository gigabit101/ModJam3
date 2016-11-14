package vswe.stevesfactory.components;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import vswe.stevesfactory.blocks.*;
import vswe.stevesfactory.tiles.*;
import vswe.stevesfactory.wrappers.CapabilityHelper;

import java.util.HashMap;
import java.util.Map;

//import net.minecraftforge.fluids.IFluidHandler;

public class SlotInventoryHolder
{
    private TileEntity inventory;
    private Map<Integer, SlotSideTarget> validSlots;
    private int sharedOption;
    private int id;

    public SlotInventoryHolder(int id, TileEntity inventory, int sharedOption)
    {
        this.id = id;
        this.inventory = inventory;
        this.sharedOption = sharedOption;
    }

    public int getId()
    {
        return id;
    }

    public IItemHandler getInventory()
    {
        return CapabilityHelper.getItemCapabilitySafe(inventory);
    }

    public IFluidHandler getTank()
    {
        return CapabilityHelper.getFluidCapabilitySafe(inventory);
    }

    public TileEntityOutput getEmitter()
    {
        return (TileEntityOutput) inventory;
    }

    public IRedstoneNode getNode()
    {
        return (IRedstoneNode) inventory;
    }

    public TileEntityInput getReceiver()
    {
        return (TileEntityInput) inventory;
    }

    public TileEntityBUD getBUD()
    {
        return (TileEntityBUD) inventory;
    }

    public TileEntityCamouflage getCamouflage()
    {
        return (TileEntityCamouflage) inventory;
    }

    public TileEntitySignUpdater getSign()
    {
        return (TileEntitySignUpdater) inventory;
    }

    public Map<Integer, SlotSideTarget> getValidSlots()
    {
        if (validSlots == null)
        {
            validSlots = new HashMap<Integer, SlotSideTarget>();
        }
        return validSlots;
    }

    public boolean isShared()
    {
        return sharedOption == 0;
    }

    public int getSharedOption()
    {
        return sharedOption;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SlotInventoryHolder that = (SlotInventoryHolder) o;

        return inventory.getPos().getX() == that.inventory.getPos().getX() && inventory.getPos().getY() == that.inventory.getPos().getY() && inventory.getPos().getZ() == that.inventory.getPos().getY();
    }

    @Override
    public int hashCode()
    {
        return inventory.hashCode();
    }

    public TileEntity getTile()
    {
        return inventory;
    }

    public ITriggerNode getTrigger()
    {
        return (ITriggerNode) inventory;
    }
}
