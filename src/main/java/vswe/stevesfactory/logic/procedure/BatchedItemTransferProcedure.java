package vswe.stevesfactory.logic.procedure;

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

import java.util.ArrayList;
import java.util.List;

public class BatchedItemTransferProcedure extends AbstractProcedure implements IInventoryTarget, IDirectionTarget, IItemFilterTarget {

    public static final int SOURCE_INVENTORIES = 0;
    public static final int DESTINATION_INVENTORIES = 1;
    public static final int FILTER = 0;

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

        List<ItemBufferElement> items = new ArrayList<>();
        for (BlockPos pos : sourceInventories) {
            TileEntity tile = context.getControllerWorld().getTileEntity(pos);
            if (tile == null) {
                continue;
            }
            for (Direction direction : sourceDirections) {
                LazyOptional<IItemHandler> cap = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction);
                if (cap.isPresent()) {
                    IItemHandler handler = cap.orElseThrow(RuntimeException::new);
                    filter.extractFromInventory((stack, slot) -> items.add(new ItemBufferElement(stack, handler, slot)), handler);
                }
            }
        }

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
                    for (ItemBufferElement buffer : items) {
                        ItemStack source = buffer.stack;
                        if (source.isEmpty()) {
                            continue;
                        }
                        int sourceStackSize = source.getCount();
                        ItemStack untaken = ItemHandlerHelper.insertItem(handler, source, false);

                        buffer.used += sourceStackSize - untaken.getCount();
                        buffer.stack = untaken;
                    }
                }
            }
        }

        for (ItemBufferElement buffer : items) {
            if (buffer.used > 0) {
                buffer.inventory.extractItem(buffer.slot, buffer.used, false);
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
        filter = ItemTagFilter.recover(tag.getCompound("Filters"));
    }

    @Override
    public FlowComponent<BatchedItemTransferProcedure> createFlowComponent() {
        FlowComponent<BatchedItemTransferProcedure> f = FlowComponent.of(this);
        f.addMenu(new InventorySelectionMenu<>(SOURCE_INVENTORIES, I18n.format("gui.sfm.Menu.InventorySelection.Source")));
        f.addMenu(new InventorySelectionMenu<>(DESTINATION_INVENTORIES, I18n.format("gui.sfm.Menu.InventorySelection.Destination")));
        f.addMenu(new DirectionSelectionMenu<>(SOURCE_INVENTORIES, I18n.format("gui.sfm.Menu.TargetSides.Source")));
        f.addMenu(new DirectionSelectionMenu<>(DESTINATION_INVENTORIES, I18n.format("gui.sfm.Menu.TargetSides.Destination")));
//        f.addMenu(new ItemTraitsFilterMenu<>(FILTERS));
        f.addMenu(new ItemTagFilterMenu<>(FILTER));
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
