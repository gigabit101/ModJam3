package vswe.stevesfactory.logic.procedure;

import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import vswe.stevesfactory.api.logic.IExecutionContext;
import vswe.stevesfactory.logic.AbstractProcedure;
import vswe.stevesfactory.setup.ModProcedures;
import vswe.stevesfactory.logic.item.DirectBufferElement;
import vswe.stevesfactory.logic.item.IItemFilter;
import vswe.stevesfactory.logic.item.ItemTraitsFilter;
import vswe.stevesfactory.ui.manager.editor.FlowComponent;
import vswe.stevesfactory.ui.manager.editor.PropertyManager;
import vswe.stevesfactory.ui.manager.menu.DirectionSelectionMenu;
import vswe.stevesfactory.ui.manager.menu.InventorySelectionMenu;
import vswe.stevesfactory.utils.IOHelper;
import vswe.stevesfactory.utils.NetworkHelper;

import java.util.*;

public class ItemImportProcedure extends AbstractProcedure implements IInventoryTarget, IDirectionTarget, IItemFilterTarget {

    public static final int INVENTORIES = 0;
    public static final int FILTER = 0;

    private List<BlockPos> inventories = new ArrayList<>();
    private Set<Direction> directions = EnumSet.noneOf(Direction.class);
    private IItemFilter filter = new ItemTraitsFilter();

    private transient List<LazyOptional<IItemHandler>> cachedCaps = new ArrayList<>();
    private transient boolean dirty = false;

    public ItemImportProcedure() {
        super(ModProcedures.itemImport);
    }

    @Override
    public void execute(IExecutionContext context) {
        pushFrame(context, 0);
        if (hasError()) {
            return;
        }

        updateCaches(context);
        Map<Item, DirectBufferElement> buffers = context.getItemBuffers(DirectBufferElement.class);
        VisitedInventories visited = NetworkHelper.obtainCustomData(context, VisitedInventories.class, VisitedInventories::new);
        for (LazyOptional<IItemHandler> cap : cachedCaps) {
            cap.ifPresent(handler -> {
                if (!visited.add(handler)) {
                    return;
                }
                filter.extractFromInventory(handler, (stack, slot) -> {
                    // If this stack is used to create the buffer, the stack count will be reset and we will lose necessary information
                    // Therefore copy the size in case it happens
                    int count = stack.getCount();
                    DirectBufferElement element = buffers.computeIfAbsent(stack.getItem(), key -> {
                        stack.setCount(0);
                        return new DirectBufferElement(stack);
                    });
                    element.stack.grow(count);
                    element.addInventory(handler, slot);
                });
            });
        }
    }

    public boolean hasError() {
        return inventories.isEmpty() || directions.isEmpty();
    }

    private void updateCaches(IExecutionContext context) {
        if (!dirty) {
            return;
        }

        cachedCaps.clear();
        NetworkHelper.cacheDirectionalCaps(
                context, cachedCaps, inventories, directions,
                CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,
                __ -> markDirty());
        dirty = false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public FlowComponent<ItemImportProcedure> createFlowComponent() {
        FlowComponent<ItemImportProcedure> f = new FlowComponent<>(this);
        f.addMenu(new InventorySelectionMenu<>(INVENTORIES, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY));
        f.addMenu(new DirectionSelectionMenu<>(INVENTORIES));
        PropertyManager.createFilterMenu(this, f, FILTER);
        return f;
    }

    @Override
    public CompoundNBT serialize() {
        CompoundNBT tag = super.serialize();
        tag.put("Inventories", IOHelper.writeBlockPoses(inventories));
        tag.putIntArray("Directions", IOHelper.direction2Index(directions));
        tag.put("Filter", IOHelper.writeItemFilter(filter));
        return tag;
    }

    @Override
    public void deserialize(CompoundNBT tag) {
        super.deserialize(tag);
        inventories = IOHelper.readBlockPoses(tag.getList("Inventories", Constants.NBT.TAG_COMPOUND), new ArrayList<>());
        directions = IOHelper.index2DirectionFill(tag.getIntArray("Directions"), EnumSet.noneOf(Direction.class));
        filter = IOHelper.readItemFilter(tag.getCompound("Filter"));
        markDirty();
    }

    @Override
    public Set<Direction> getDirections(int id) {
        return directions;
    }

    @Override
    public List<BlockPos> getInventories(int id) {
        return inventories;
    }

    @Override
    public void markDirty() {
        dirty = true;
    }

    @Override
    public IItemFilter getFilter(int id) {
        return filter;
    }

    @Override
    public void setFilter(int id, IItemFilter filter) {
        if (id == FILTER) {
            this.filter = filter;
        }
    }

    private static final class VisitedInventories extends HashSet<IItemHandler> {
    }
}
