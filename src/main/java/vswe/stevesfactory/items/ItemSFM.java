package vswe.stevesfactory.items;

import net.minecraft.item.Item;
import vswe.stevesfactory.client.CreativeTabSFM;

/**
 * Created by Gigabit101 on 27/11/2016.
 */
public class ItemSFM extends Item
{
    public ItemSFM()
    {
        setCreativeTab(CreativeTabSFM.instance);
        setMaxStackSize(1);
    }
}
