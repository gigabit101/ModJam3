package vswe.stevesfactory.logic.item;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.IItemHandler;
import vswe.stevesfactory.logic.FilterType;
import vswe.stevesfactory.utils.IOHelper;
import vswe.stevesfactory.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class ItemTraitsFilter implements IItemFilter {

    private static int TYPE_ID = ItemFilters.allocateID(ItemTraitsFilter::recover);

    private List<ItemStack> items = new ArrayList<>();

    public FilterType type = FilterType.WHITELIST;
    private boolean matchingDamage;
    private boolean matchingTag;
    private boolean matchingAmount;

    @Override
    public FilterType getType() {
        return type;
    }

    @Override
    public void setType(FilterType type) {
        this.type = type;
    }

    public List<ItemStack> getItems() {
        return items;
    }

    public boolean isMatchingDamage() {
        return matchingDamage;
    }

    public void setMatchingDamage(boolean matchingDamage) {
        this.matchingDamage = matchingDamage;
    }

    public boolean isMatchingTag() {
        return matchingTag;
    }

    public void setMatchingTag(boolean matchingTag) {
        this.matchingTag = matchingTag;
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
        for (int i = 0; i < items.size(); i++) {
            boolean b = isEqual(i, stack);
            if (b) {
                return !getTypeFlag();
            }
        }
        return getTypeFlag();
    }

    @Override
    public void extractFromInventory(List<ItemStack> target, IItemHandler handler, boolean merge) {
        Object2LongMap<Item> counts = constructResultMap();

        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.extractItem(i, Integer.MAX_VALUE, true);
            if (stack.isEmpty()) {
                continue;
            }

            boolean accepted = test(stack);
            if (accepted) {
                if (processExtractableStack(counts, stack)) {
                    continue;
                }
                if (!merge) {
                    target.add(stack);
                }
            }
        }

        if (merge) {
            for (Object2LongMap.Entry<Item> entry : counts.object2LongEntrySet()) {
                long data = entry.getLongValue();
                ItemStack stack = new ItemStack(entry.getKey(), getExtractedCount(data));
                target.add(stack);
            }
        }
    }

    @Override
    public void extractFromInventory(BiConsumer<ItemStack, Integer> receiver, IItemHandler handler) {
        // Lower end: desired count
        // Higher end: extracted count
        Object2LongMap<Item> counts = constructResultMap();

        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.extractItem(i, Integer.MAX_VALUE, true);
            if (stack.isEmpty()) {
                continue;
            }

            boolean accepted = test(stack);
            if (accepted) {
                if (processExtractableStack(counts, stack)) {
                    continue;
                }
                receiver.accept(stack, i);
            }
        }
    }

    private boolean processExtractableStack(Object2LongMap<Item> counts, ItemStack stack) {
        Item item = stack.getItem();
        long data = counts.getOrDefault(item, Integer.MAX_VALUE);
        int desired = getDesiredCount(data);
        if (desired == 0) {
            return true;
        }
        int extracted = getExtractedCount(data);

        int used = Utils.upperBound(stack.getCount(), desired);
        stack.setCount(used);

        // We put in data regardless of the filter because the extracted count record is necessary if we're merging stacks
        counts.put(item, getData(extracted + used, desired - used));
        return false;
    }

    private Object2LongMap<Item> constructResultMap() {
        // Lower end: desired count
        // Higher end: extracted count
        Object2LongMap<Item> counts = new Object2LongOpenHashMap<>();
        // For blacklist, the accepted items will not be in this map anyways
        // so we can simply not build this map if we are in blacklisting mode
        if (type == FilterType.WHITELIST) {
            for (ItemStack item : items) {
                if (!item.isEmpty()) {
                    counts.put(item.getItem(), getData(0, matchingAmount ? item.getCount() : Integer.MAX_VALUE));
                }
            }
        }
        return counts;
    }

    private int getDesiredCount(long data) {
        // Throw away the high-end bits
        return (int) data;
    }

    private int getExtractedCount(long data) {
        return (int) (data >>> 32);
    }

    private long getData(int extracted, int desired) {
        return (long) extracted << 32 | desired;
    }

    public boolean isEqual(int filterIndex, ItemStack stack) {
        ItemStack item = items.get(filterIndex);
        if (item.isEmpty()) {
            return stack.isEmpty();
        }
        return item.isItemEqual(stack)
                && (!matchingDamage || item.getDamage() == stack.getDamage())
                && (!matchingTag || item.areShareTagsEqual(stack));
    }

    public boolean doesItemMatch(int filterIndex, ItemStack stack) {
        return isEqual(filterIndex, stack) ^ getTypeFlag();
    }

    @Override
    public int limitFlowRate(ItemStack buffered, int existingCount) {
        int stackLimit = Integer.MAX_VALUE;
        for (int i = 0; i < items.size(); i++) {
            if (isEqual(i, buffered)) {
                stackLimit = items.get(i).getCount();
                break;
            }
        }
        return Utils.lowerBound(stackLimit - existingCount, 0);
    }

    private boolean getTypeFlag() {
        switch (type) {
            case WHITELIST:
                return false;
            case BLACKLIST:
                return true;
        }
        throw new IllegalStateException();
    }

    @Override
    public int getTypeID() {
        return TYPE_ID;
    }

    @Override
    public void read(CompoundNBT tag) {
        type = tag.getBoolean("Blacklist") ? FilterType.BLACKLIST : FilterType.WHITELIST;
        items = IOHelper.readItemStacks(tag.getList("Items", Constants.NBT.TAG_COMPOUND), new ArrayList<>());
        matchingDamage = tag.getBoolean("MatchDamage");
        matchingTag = tag.getBoolean("MatchTag");
        matchingAmount = tag.getBoolean("MatchAmount");
    }

    @Override
    public void write(CompoundNBT tag) {
        tag.putBoolean("Blacklist", type == FilterType.BLACKLIST);
        tag.put("Items", IOHelper.writeItemStacks(items));
        tag.putBoolean("MatchDamage", matchingDamage);
        tag.putBoolean("MatchTag", matchingTag);
        tag.putBoolean("MatchAmount", matchingAmount);
    }

    public static ItemTraitsFilter recover(CompoundNBT tag) {
        ItemTraitsFilter filter = new ItemTraitsFilter();
        filter.read(tag);
        return filter;
    }
}
