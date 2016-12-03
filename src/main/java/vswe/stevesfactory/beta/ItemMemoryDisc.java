package vswe.stevesfactory.beta;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import vswe.stevesfactory.items.ItemSFM;
import vswe.stevesfactory.tiles.TileEntityManager;

import java.util.List;

/**
 * Created by Gigabit101 on 03/12/2016.
 */

//This was merged from StevesAddons https://github.com/hilburn/StevesAddons/blob/master/src/main/java/stevesaddons/items/ItemSFMDrive.java
public class ItemMemoryDisc extends ItemSFM
{
    public ItemMemoryDisc()
    {
        setUnlocalizedName("sfm.memorydisc");
        setRegistryName("memorydisc");
    }

    @Override
    public EnumActionResult onItemUseFirst(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand)
    {
        if (!world.isRemote && player.isSneaking())
        {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileEntityManager)
            {
                if (stack.hasTagCompound() && validateNBT(stack))
                {
                    te.readFromNBT(correctNBT((TileEntityManager)te, stack.getTagCompound()));
                    stack.setTagCompound(null);
                }
                else
                {
                    NBTTagCompound tagCompound = new NBTTagCompound();
                    te.writeToNBT(tagCompound);
                    tagCompound.setTag("ench", new NBTTagList());
                    stack.setTagCompound(tagCompound);
                }
                return EnumActionResult.PASS;
            }
        }
        validateNBT(stack);
        return EnumActionResult.PASS;
    }

    public static boolean validateNBT(ItemStack stack)
    {
        if (stack.hasTagCompound() && (stack.getTagCompound().getString("id").equals("TileEntityMachineManagerName") || stack.getTagCompound().getString("id").equals("TileEntityRFManager")))
            return true;
        stack.setTagCompound(null);
        return false;
    }

    private static NBTTagCompound correctNBT(TileEntityManager manager, NBTTagCompound tagCompound)
    {
        tagCompound.setInteger("x", manager.getPos().getX());
        tagCompound.setInteger("y", manager.getPos().getY());
        tagCompound.setInteger("z", manager.getPos().getZ());
        int currentFlow = manager.getFlowItems().size();
        if (currentFlow > 0)
        {
            byte version = tagCompound.getByte("ProtocolVersion");
            NBTTagList components = tagCompound.getTagList("Components", 10);
            NBTTagList newComponents = new NBTTagList();
            for (int variablesTag = 0; variablesTag < components.tagCount(); ++variablesTag)
            {
                NBTTagCompound flowComponent = components.getCompoundTagAt(variablesTag);
                NBTTagList connections = flowComponent.getTagList("Connection", 10);
                NBTTagList newConnections = new NBTTagList();
                for (int i = 0; i < connections.tagCount(); ++i)
                {
                    NBTTagCompound connection = connections.getCompoundTagAt(i);
                    if (connection.hasKey("ConnectionComponent"))
                    {
                        if (version < 9)
                        {
                            connection.setByte("ConnectionComponent", (byte)(connection.getByte("ConnectionComponent") + currentFlow));
                        } else
                        {
                            connection.setShort("ConnectionComponent", (short)(connection.getShort("ConnectionComponent") + currentFlow));
                        }
                    }
                    newConnections.appendTag(connection);
                }
                flowComponent.setTag("Connection", newConnections);
                newComponents.appendTag(flowComponent);
            }
            tagCompound.setTag("Components", newComponents);
        }
        return tagCompound;
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced)
    {
        if (stack.hasTagCompound() && validateNBT(stack))
        {
            if (stack.getTagCompound().hasKey("Author"))
            {
                list.add("Manager setup authored by:");
                list.add(stack.getTagCompound().getString("Author"));
            }
            else
            {
                int x = stack.getTagCompound().getInteger("x");
                int y = stack.getTagCompound().getInteger("y");
                int z = stack.getTagCompound().getInteger("z");
                list.add("Data stored from Manager at:");
                list.add("x: " + x + " y: " + y + " z: " + z);
            }
        }
    }
}
