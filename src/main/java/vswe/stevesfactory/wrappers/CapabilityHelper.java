package vswe.stevesfactory.wrappers;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

/**
 * Created by Gigabit101 on 07/11/2016.
 */
public class CapabilityHelper
{
    //FLUIDS
    public static boolean hasFluidCapabilitySafe(TileEntity tileEntity)
    {
        try
        {
            if (tileEntity != null && tileEntity.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null))
            {
                return true;
            }
            return false;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    public static IFluidHandler getFluidCapabilitySafe(TileEntity tileEntity)
    {
        try
        {
            return tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    //ITEMS
    public static boolean hasItemCapabilitySafe(TileEntity tileEntity)
    {
        try
        {
            if (tileEntity != null && tileEntity.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null))
            {
                return true;
            }
            return false;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    public static IItemHandler getItemCapabilitySafe(TileEntity tileEntity)
    {
        try
        {
            return tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        }
        catch (Exception e)
        {
            return null;
        }
    }
}
