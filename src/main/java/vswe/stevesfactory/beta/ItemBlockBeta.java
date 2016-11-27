package vswe.stevesfactory.beta;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

/**
 * Created by Gigabit101 on 27/11/2016.
 */
public class ItemBlockBeta extends ItemBlock
{
    public ItemBlockBeta(Block block)
    {
        super(block);
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced)
    {
        tooltip.add(TextFormatting.RED + "WIP");
        tooltip.add(TextFormatting.RED + "COMING SOON");
    }
}
