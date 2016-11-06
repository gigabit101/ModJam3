package vswe.stevesfactory.blocks;


import gigabit101.AdvancedSystemManager2.blocks.TileEntityClusterElement;
import net.minecraft.block.BlockContainer;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ClusterRegistry {

    private static HashMap<Class<? extends gigabit101.AdvancedSystemManager2.blocks.TileEntityClusterElement>, gigabit101.AdvancedSystemManager2.blocks.ClusterRegistry> registry = new HashMap<Class<? extends TileEntityClusterElement>, gigabit101.AdvancedSystemManager2.blocks.ClusterRegistry>();
    private static List<gigabit101.AdvancedSystemManager2.blocks.ClusterRegistry> registryList = new ArrayList<gigabit101.AdvancedSystemManager2.blocks.ClusterRegistry>();

    protected final Class<? extends TileEntityClusterElement> clazz;
    protected final BlockContainer block;
    protected final ItemStack itemStack;
    protected gigabit101.AdvancedSystemManager2.blocks.ClusterRegistry nextSubRegistry;
    protected gigabit101.AdvancedSystemManager2.blocks.ClusterRegistry headSubRegistry;
    protected final int id;

    private ClusterRegistry(Class<? extends TileEntityClusterElement> clazz, BlockContainer block, ItemStack itemStack) {
        this.clazz = clazz;
        this.block = block;
        this.itemStack = itemStack;
        this.id = registryList.size();
    }

    public static void register(Class<? extends TileEntityClusterElement> clazz, BlockContainer block) {
        register(new gigabit101.AdvancedSystemManager2.blocks.ClusterRegistry(clazz, block, new ItemStack(block)));
    }

    public static void register(gigabit101.AdvancedSystemManager2.blocks.ClusterRegistry registryElement) {
        registryList.add(registryElement);
        gigabit101.AdvancedSystemManager2.blocks.ClusterRegistry parent = registry.get(registryElement.clazz);
        if (parent == null) {
            registry.put(registryElement.clazz, registryElement);
            registryElement.headSubRegistry = registryElement;
        }else{
            registryElement.headSubRegistry = parent;
            gigabit101.AdvancedSystemManager2.blocks.ClusterRegistry elem = parent;
            while (elem.nextSubRegistry != null) {
                elem = elem.nextSubRegistry;
            }
            elem.nextSubRegistry = registryElement;
        }
    }

    public int getId() {
        return id;
    }


    public BlockContainer getBlock() {
        return block;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public ItemStack getItemStack(int meta) {

        gigabit101.AdvancedSystemManager2.blocks.ClusterRegistry element = this.headSubRegistry;
        while (element != null) {
            if (element.isValidMeta(meta)) {
                return element.getItemStack();
            }
            element = element.nextSubRegistry;
        }
        return getItemStack();
    }

    public boolean isValidMeta(int meta) {
        return true;
    }

    public static gigabit101.AdvancedSystemManager2.blocks.ClusterRegistry get(TileEntityClusterElement tileEntityClusterElement) {
        return registry.get(tileEntityClusterElement.getClass());
    }

    public static List<gigabit101.AdvancedSystemManager2.blocks.ClusterRegistry> getRegistryList() {
        return registryList;
    }

    public boolean isChainPresentIn(List<Integer> types) {
        gigabit101.AdvancedSystemManager2.blocks.ClusterRegistry element = this.headSubRegistry;
        while (element != null) {
            if (types.contains(element.id)) {
                return true;
            }
            element = element.nextSubRegistry;
        }

        return false;
    }

    public static class ClusterRegistryAdvancedSensitive extends gigabit101.AdvancedSystemManager2.blocks.ClusterRegistry {

        public ClusterRegistryAdvancedSensitive(Class<? extends TileEntityClusterElement> clazz, BlockContainer block, ItemStack itemStack) {
            super(clazz, block, itemStack);
        }

        @Override
        public boolean isValidMeta(int meta) {
            return (itemStack.getItemDamage() & 8) == (meta & 8);
        }
    }

    public static class ClusterRegistryMetaSensitive extends gigabit101.AdvancedSystemManager2.blocks.ClusterRegistry {
        public ClusterRegistryMetaSensitive(Class<? extends TileEntityClusterElement> clazz, BlockContainer block, ItemStack itemStack) {
            super(clazz, block, itemStack);
        }

        @Override
        public boolean isValidMeta(int meta) {
            return itemStack.getItemDamage() == meta;
        }
    }
}
