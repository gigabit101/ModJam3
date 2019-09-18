package vswe.stevesfactory.logic.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.Tag;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.IItemHandler;
import vswe.stevesfactory.logic.FilterType;
import vswe.stevesfactory.utils.IOHelper;

import java.util.*;

public class ItemTagFilter implements IItemFilter {

    private Set<Tag<Item>> tags = new HashSet<>();
    public FilterType type = FilterType.WHITELIST;

    public Set<Tag<Item>> getTags() {
        return tags;
    }

    @Override
    public FilterType getType() {
        return type;
    }

    @Override
    public void setType(FilterType type) {
        this.type = type;
    }

    @Override
    public boolean test(ItemStack stack) {
        for (Tag<Item> tag : tags) {
            if (tag.contains(stack.getItem())) {
                return !getTypeFlag();
            }
        }
        return getTypeFlag();
    }

    @Override
    public void extractFromInventory(List<ItemStack> target, IItemHandler handler, boolean merge) {
        if (merge) {
            extractFromInventoryMerge(target, handler);
        } else {
            extractFromInventoryNoMerge(target, handler);
        }
    }

    private void extractFromInventoryNoMerge(List<ItemStack> target, IItemHandler handler) {
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.extractItem(i, Integer.MAX_VALUE, true);
            if (test(stack)) {
                target.add(stack);
            }
        }
    }

    private void extractFromInventoryMerge(List<ItemStack> target, IItemHandler handler) {
        Map<Item, ItemStack> results = new HashMap<>();
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.extractItem(i, Integer.MAX_VALUE, true);
            if (test(stack)) {
                Item item = stack.getItem();
                if (results.containsKey(item)) {
                    ItemStack result = results.get(item);
                    result.grow(stack.getCount());
                } else {
                    results.put(item, stack);
                }
            }
        }

        for (Map.Entry<Item, ItemStack> entry : results.entrySet()) {
            ItemStack stack = entry.getValue();
            target.add(stack);
        }
    }

    private boolean getTypeFlag() {
        switch (type) {
            case WHITELIST: return false;
            case BLACKLIST: return true;
        }
        throw new IllegalStateException();
    }

    @Override
    public void read(CompoundNBT tag) {
        type = tag.getBoolean("Blacklist") ? FilterType.BLACKLIST : FilterType.WHITELIST;
        tags.clear();
        IOHelper.readItemTags(tag.getList("ItemTags", Constants.NBT.TAG_STRING), tags);
    }

    @Override
    public void write(CompoundNBT tag) {
        tag.putBoolean("Blacklist", type == FilterType.BLACKLIST);
        tag.put("ItemTags", IOHelper.writeTags(tags));
    }

    public static ItemTagFilter recover(CompoundNBT tag) {
        ItemTagFilter filter = new ItemTagFilter();
        filter.read(tag);
        return filter;
    }
}
