package vswe.stevesfactory.blocks;

import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import vswe.stevesfactory.Localization;
import vswe.stevesfactory.StevesFactoryManager;
import vswe.stevesfactory.init.ModBlocks;

import javax.annotation.Nullable;
import java.util.List;

public class ItemCluster extends ItemBlock
{
    public ItemCluster(Block block)
    {
        super(block);
        setHasSubtypes(true);
        setMaxDamage(0);
    }

    public static final String NBT_CABLE = "Cable";
    public static final String NBT_TYPES = "Types";


    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        ItemStack stack = player.getHeldItem(hand);
        NBTTagCompound compound = stack.getTagCompound();
        if (compound != null && compound.hasKey(NBT_CABLE))
        {
            NBTTagCompound cable = compound.getCompoundTag(NBT_CABLE);
            if (cable.hasKey(NBT_TYPES))
            {
                return super.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);
            }
        }

        return EnumActionResult.PASS;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn)
    {
        NBTTagCompound compound = stack.getTagCompound();
        if (compound != null && compound.hasKey(NBT_CABLE))
        {
            NBTTagCompound cable = compound.getCompoundTag(NBT_CABLE);
            byte[] types = cable.getByteArray(ItemCluster.NBT_TYPES);
            for (byte type : types)
            {
                tooltip.add(ClusterRegistry.getRegistryList().get(type).getItemStack().getDisplayName());
            }
        } else
        {
            tooltip.add(Localization.EMPTY_CLUSTER.toString());
        }
    }

    @Override
    public String getUnlocalizedName(ItemStack item)
    {
        return "tile." + StevesFactoryManager.UNLOCALIZED_START + (ModBlocks.blockCableCluster.isAdvanced(item.getItemDamage()) ? ModBlocks.CABLE_ADVANCED_CLUSTER_UNLOCALIZED_NAME : ModBlocks.CABLE_CLUSTER_UNLOCALIZED_NAME);
    }
}
