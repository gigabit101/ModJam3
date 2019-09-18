package vswe.stevesfactory.logic.procedure;

import com.google.common.base.Preconditions;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.*;
import vswe.stevesfactory.api.logic.CommandGraph;
import vswe.stevesfactory.api.logic.IExecutionContext;
import vswe.stevesfactory.api.network.INetworkController;
import vswe.stevesfactory.logic.AbstractProcedure;
import vswe.stevesfactory.logic.Procedures;
import vswe.stevesfactory.logic.item.*;
import vswe.stevesfactory.ui.manager.editor.FlowComponent;
import vswe.stevesfactory.ui.manager.menu.*;
import vswe.stevesfactory.utils.IOHelper;
import vswe.stevesfactory.utils.SlotlessItemHandlerWrapper;

import java.util.ArrayList;
import java.util.List;

public class BatchedItemTransferProcedure extends AbstractProcedure implements IInventoryTarget, IDirectionTarget, IItemFilterTarget {

    public static final int SOURCE_INVENTORIES = 0;
    public static final int DESTINATION_INVENTORIES = 1;
    public static final int FILTERS = 0;

    private List<BlockPos> sourceInventories = new ArrayList<>();
    private List<Direction> sourceDirections = new ArrayList<>();
    private List<BlockPos> targetInventories = new ArrayList<>();
    private List<Direction> targetDirections = new ArrayList<>();
    private IItemFilter filter = new ItemTagFilter();

    public BatchedItemTransferProcedure(INetworkController controller) {
        super(Procedures.BATCHED_ITEM_TRANSFER.getFactory(), controller);
    }

    public BatchedItemTransferProcedure(CommandGraph graph) {
        super(Procedures.BATCHED_ITEM_TRANSFER.getFactory(), graph);
    }

    @Override
    public void execute(IExecutionContext context) {
        pushFrame(context, 0);

        if (hasError()) {
            return;
        }

        List<TileEntity> sourceTiles = new ArrayList<>(sourceInventories.size());
        for (BlockPos pos : sourceInventories) {
            TileEntity tile = context.getControllerWorld().getTileEntity(pos);
            if (tile != null) {
                sourceTiles.add(tile);
            }
        }

        List<ItemStack> availableSourceItems = new ArrayList<>();
        for (TileEntity tile : sourceTiles) {
            for (Direction direction : sourceDirections) {
                LazyOptional<IItemHandler> cap = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction);
                if (cap.isPresent()) {
                    IItemHandler handler = cap.orElseThrow(RuntimeException::new);
                    filter.extractFromInventory(availableSourceItems, handler, true);
                }
            }
        }

        List<ItemStack> takenSourceItems = new ArrayList<>(availableSourceItems.size());
        for (int i = 0; i < availableSourceItems.size(); i++) {
            // Start with no items taken. Note that this operation will only set the stack to empty, but keeping the item type
            takenSourceItems.add(ItemStack.EMPTY);
        }

        Preconditions.checkState(availableSourceItems.size() == takenSourceItems.size());

        for (BlockPos pos : targetInventories) {
            TileEntity tile = context.getControllerWorld().getTileEntity(pos);
            if (tile == null) {
                continue;
            }
            for (Direction direction : targetDirections) {
                LazyOptional<IItemHandler> cap = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction);
                if (cap.isPresent()) {
                    IItemHandler handler = cap.orElseThrow(RuntimeException::new);
                    // We don't need filter here because this is just in one procedure
                    // It does not make sense to have multiple filters for one item transferring step
                    for (int i = 0; i < availableSourceItems.size(); i++) {
                        ItemStack source = availableSourceItems.get(i);
                        if (source.isEmpty()) {
                            continue;
                        }
                        int sourceStackSize = source.getCount();
                        ItemStack untaken = ItemHandlerHelper.insertItem(handler, source, false);
                        int taken = sourceStackSize - untaken.getCount();
                        takenSourceItems.set(i, new ItemStack(source.getItem(), taken));
                        availableSourceItems.set(i, untaken);
                    }
                }
            }
        }

        int flag = SlotlessItemHandlerWrapper.ITEM | SlotlessItemHandlerWrapper.DAMAGE | SlotlessItemHandlerWrapper.STACKSIZE;
        for (TileEntity tile : sourceTiles) {
            for (Direction direction : sourceDirections) {
                LazyOptional<IItemHandler> cap = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction);
                if (cap.isPresent()) {
                    SlotlessItemHandlerWrapper handler = new SlotlessItemHandlerWrapper(cap.orElseThrow(RuntimeException::new));
                    for (ItemStack stack : takenSourceItems) {
                        if (stack.isEmpty()) {
                            continue;
                        }
                        ItemStack extracted = handler.extractItem(stack, flag, false);
                        stack.shrink(extracted.getCount());
                    }
                }
            }
        }
    }

    public boolean hasError() {
        return sourceInventories.isEmpty() || sourceDirections.isEmpty() || targetInventories.isEmpty() || targetDirections.isEmpty();
    }

    @Override
    public CompoundNBT serialize() {
        CompoundNBT tag = super.serialize();

        tag.put("SourcePoses", IOHelper.writeBlockPoses(sourceInventories));
        tag.putIntArray("SourceDirections", IOHelper.direction2Index(sourceDirections));
        tag.put("TargetPoses", IOHelper.writeBlockPoses(targetInventories));
        tag.putIntArray("TargetDirections", IOHelper.direction2Index(targetDirections));
        tag.put("Filters", filter.write());

        return tag;
    }

    @Override
    public void deserialize(CompoundNBT tag) {
        super.deserialize(tag);
        sourceInventories = IOHelper.readBlockPoses(tag.getList("SourcePoses", Constants.NBT.TAG_COMPOUND), new ArrayList<>());
        sourceDirections = IOHelper.index2Direction(tag.getIntArray("SourceDirections"));
        targetInventories = IOHelper.readBlockPoses(tag.getList("TargetPoses", Constants.NBT.TAG_COMPOUND), new ArrayList<>());
        targetDirections = IOHelper.index2Direction(tag.getIntArray("TargetDirections"));
        filter = ItemTraitsFilter.recover(tag.getCompound("Filters"));
    }

    @Override
    public FlowComponent<BatchedItemTransferProcedure> createFlowComponent() {
        FlowComponent<BatchedItemTransferProcedure> f = FlowComponent.of(this);
        f.addMenu(new InventorySelectionMenu<>(SOURCE_INVENTORIES, I18n.format("gui.sfm.Menu.InventorySelection.Source")));
        f.addMenu(new InventorySelectionMenu<>(DESTINATION_INVENTORIES, I18n.format("gui.sfm.Menu.InventorySelection.Destination")));
        f.addMenu(new DirectionSelectionMenu<>(SOURCE_INVENTORIES, I18n.format("gui.sfm.Menu.TargetSides.Source")));
        f.addMenu(new DirectionSelectionMenu<>(DESTINATION_INVENTORIES, I18n.format("gui.sfm.Menu.TargetSides.Destination")));
//        f.addMenu(new ItemTraitsFilterMenu<>(FILTERS));
        f.addMenu(new ItemTagFilterMenu<>(FILTERS));
        return f;
    }

    @Override
    public List<BlockPos> getInventories(int id) {
        switch (id) {
            case SOURCE_INVENTORIES:
            default:
                return sourceInventories;
            case DESTINATION_INVENTORIES: return targetInventories;
        }
    }

    @Override
    public List<Direction> getDirections(int id) {
        switch (id) {
            case SOURCE_INVENTORIES:
            default:
                return sourceDirections;
            case DESTINATION_INVENTORIES: return targetDirections;
        }
    }

    @Override
    public IItemFilter getFilter(int id) {
        return filter;
    }
}
