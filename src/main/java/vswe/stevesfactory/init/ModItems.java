package vswe.stevesfactory.init;

import net.minecraftforge.fml.common.registry.GameRegistry;
import vswe.stevesfactory.beta.ItemMemoryDisc;

/**
 * Created by Gigabit101 on 03/12/2016.
 */
public class ModItems
{
    public static ItemMemoryDisc memoryDisc;

    public static void init()
    {
        memoryDisc = new ItemMemoryDisc();
        GameRegistry.register(memoryDisc);
    }
}
