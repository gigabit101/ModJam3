package vswe.stevesfactory.logic.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.Tag;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.IItemHandler;
import vswe.stevesfactory.logic.FilterType;
import vswe.stevesfactory.utils.IOHelper;
import vswe.stevesfactory.utils.Utils;

import java.util.*;
import java.util.function.BiConsumer;

public class ItemTagFilter implements IItemFilter {

    public static final String TYPE_NAME = "sfm:tag_filter";

    static {
        ItemFilters.register(TYPE_NAME, ItemTagFilter::recover);
    }

    public FilterType type = FilterType.WHITELIST;
    public int stackLimit;
    private Set<Tag<Item>> tags = new HashSet<>();

    private boolean matchingAmount;

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
    public boolean isMatchingAmount() {
        return matchingAmount;
    }

    @Override
    public void setMatchingAmount(boolean matchingAmount) {
        this.matchingAmount = matchingAmount;
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

    @Override
    public void extractFromInventory(BiConsumer<ItemStack, Integer> receiver, IItemHandler handler) {
        int total = 0;
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.extractItem(i, Integer.MAX_VALUE, true);
            if (test(stack)) {
                int need = Math.min(stack.getCount(), stackLimit - total);
                total += need;
                stack.setCount(need);
                receiver.accept(stack, i);
            }
            if (total >= stackLimit) {
                break;
            }
        }
    }

    private void extractFromInventoryNoMerge(List<ItemStack> target, IItemHandler handler) {
        int total = 0;
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.extractItem(i, Integer.MAX_VALUE, true);
            if (test(stack)) {
                int need = Math.min(stack.getCount(), stackLimit - total);
                total += need;
                stack.setCount(need);
                target.add(stack);
            }
            if (total >= stackLimit) {
                break;
            }
        }
    }

    private void extractFromInventoryMerge(List<ItemStack> target, IItemHandler handler) {
        int total = 0;
        Map<Item, ItemStack> results = new HashMap<>();
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.extractItem(i, Integer.MAX_VALUE, true);
            if (test(stack)) {
                Item item = stack.getItem();
                int need = Math.min(stack.getCount(), stackLimit - total);
                total += need;
                stack.setCount(need);
                if (results.containsKey(item)) {
                    ItemStack result = results.get(item);
                    result.grow(stack.getCount());
                } else {
                    results.put(item, stack);
                }
            }
            if (total >= stackLimit) {
                break;
            }
        }

        for (Map.Entry<Item, ItemStack> entry : results.entrySet()) {
            ItemStack stack = entry.getValue();
            target.add(stack);
        }
    }

    @Override
    public int limitFlowRate(ItemStack buffered, int existingCount) {
        return Utils.lowerBound(stackLimit - existingCount, 0);
    }

    private boolean getTypeFlag() {
        switch (type) {
            case WHITELIST: return false;
            case BLACKLIST: return true;
        }
        throw new IllegalStateException();
    }

    @Override
    public String getTypeID() {
        return TYPE_NAME;
    }

    @Override
    public void read(CompoundNBT tag) {
        type = tag.getBoolean("Blacklist") ? FilterType.BLACKLIST : FilterType.WHITELIST;
        matchingAmount = tag.getBoolean("MatchAmount");
        stackLimit = tag.getInt("StackLimit");
        tags.clear();
        IOHelper.readItemTags(tag.getList("ItemTags", Constants.NBT.TAG_STRING), tags);
    }

    @Override
    public void write(CompoundNBT tag) {
        tag.putBoolean("Blacklist", type == FilterType.BLACKLIST);
        tag.putBoolean("MatchAmount", matchingAmount);
        tag.putInt("StackLimit", stackLimit);
        tag.put("ItemTags", IOHelper.writeTags(tags));
    }

    public static ItemTagFilter recover(CompoundNBT tag) {
        ItemTagFilter filter = new ItemTagFilter();
        filter.read(tag);
        return filter;
    }
}
