package vswe.stevesfactory.client;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import vswe.stevesfactory.init.ModBlocks;
import vswe.stevesfactory.lib.ModInfo;

/**
 * Created by Gigabit101 on 27/11/2016.
 */
public class CreativeTabSFM extends CreativeTabs
{
    public static CreativeTabSFM instance = new CreativeTabSFM();

    public CreativeTabSFM()
    {
        super(ModInfo.MOD_ID);
    }

    @Override
    public Item getTabIconItem()
    {
        return Item.getItemFromBlock(ModBlocks.blockManager);
    }
}
