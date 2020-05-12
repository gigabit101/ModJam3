package vswe.stevesfactory.logic.procedure;

import lombok.val;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.items.CapabilityItemHandler;
import vswe.stevesfactory.api.logic.IExecutionContext;
import vswe.stevesfactory.logic.AbstractProcedure;
import vswe.stevesfactory.setup.ModProcedures;
import vswe.stevesfactory.logic.fluid.SingleFluidBufferElement;
import vswe.stevesfactory.ui.manager.editor.FlowComponent;
import vswe.stevesfactory.ui.manager.menu.DirectionSelectionMenu;
import vswe.stevesfactory.ui.manager.menu.InventorySelectionMenu;
import vswe.stevesfactory.utils.IOHelper;
import vswe.stevesfactory.utils.NetworkHelper;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class FluidTransferProcedure extends AbstractProcedure implements IInventoryTarget, IDirectionTarget {

    public static final int SOURCE_TANKS = 0;
    public static final int DESTINATION_TANKS = 1;
    public static final int FILTER = 0;

    private List<BlockPos> sourceTanks = new ArrayList<>();
    private Set<Direction> sourceDirections = EnumSet.noneOf(Direction.class);
    private List<BlockPos> destinationTanks = new ArrayList<>();
    private Set<Direction> destinationDirections = EnumSet.noneOf(Direction.class);

    private transient List<LazyOptional<IFluidHandler>> cachedSourceCaps = new ArrayList<>();
    private transient List<LazyOptional<IFluidHandler>> cachedDestinationCaps = new ArrayList<>();
    private transient boolean dirty = false;

    public FluidTransferProcedure() {
        super(ModProcedures.fluidTransfer);
    }

    @Override
    public void execute(IExecutionContext context) {
        pushFrame(context, 0);
        if (hasError()) {
            return;
        }

        cacheCaps(context);

        List<SingleFluidBufferElement> buffers = new ArrayList<>();
        for (val cap : cachedSourceCaps) {
            if (cap.isPresent()) {
                val handler = cap.orElseThrow(RuntimeException::new);
                val stack = handler.drain(Integer.MAX_VALUE, FluidAction.SIMULATE);
                buffers.add(new SingleFluidBufferElement(stack, handler));
            }
        }

        for (val cap : cachedDestinationCaps) {
            if (cap.isPresent()) {
                val handler = cap.orElseThrow(RuntimeException::new);
                for (val buffer : buffers) {
                    val source = buffer.stack;
                    if (source.isEmpty()) {
                        continue;
                    }
                    int consumed = handler.fill(source, FluidAction.EXECUTE);
                    buffer.used += consumed;
                }
            }
        }

        for (val buffer : buffers) {
            if (buffer.used > 0) {
                buffer.handler.drain(buffer.used, FluidAction.EXECUTE);
            }
        }
    }

    public boolean hasError() {
        return sourceTanks.isEmpty() || sourceDirections.isEmpty()
                || destinationTanks.isEmpty() || destinationDirections.isEmpty();
    }

    private void cacheCaps(IExecutionContext context) {
        if (!dirty) {
            return;
        }

        val linkedInventories = context.getController().getLinkedInventories(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
        cachedSourceCaps.clear();
        cachedDestinationCaps.clear();
        NetworkHelper.cacheDirectionalCaps(context, linkedInventories, cachedSourceCaps, sourceTanks, sourceDirections, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, __ -> markDirty());
        NetworkHelper.cacheDirectionalCaps(context, linkedInventories, cachedDestinationCaps, destinationTanks, destinationDirections, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, __ -> markDirty());
        dirty = false;
    }


    @Override
    @OnlyIn(Dist.CLIENT)
    public FlowComponent<FluidTransferProcedure> createFlowComponent() {
        val f = new FlowComponent<>(this);
        f.addMenu(new InventorySelectionMenu<>(SOURCE_TANKS, I18n.format("menu.sfm.TankSelection.Source"), I18n.format("error.sfm.FluidTransfer.NoSrcTank"), CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY));
        f.addMenu(new InventorySelectionMenu<>(DESTINATION_TANKS, I18n.format("menu.sfm.TankSelection.Destination"), I18n.format("error.sfm.FluidTransfer.NoSrcTarget"), CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY));
        f.addMenu(new DirectionSelectionMenu<>(SOURCE_TANKS, I18n.format("menu.sfm.TargetSides.Source"), I18n.format("error.sfm.FluidTransfer.NoDestTank")));
        f.addMenu(new DirectionSelectionMenu<>(DESTINATION_TANKS, I18n.format("menu.sfm.TargetSides.Destination"), I18n.format("error.sfm.FluidTransfer.NoDestTarget")));
        return f;
    }

    @Override
    public CompoundNBT serialize() {
        CompoundNBT tag = super.serialize();
        tag.put("SourcePoses", IOHelper.writeBlockPoses(sourceTanks));
        tag.putIntArray("SourceDirections", IOHelper.direction2Index(sourceDirections));
        tag.put("TargetPoses", IOHelper.writeBlockPoses(destinationTanks));
        tag.putIntArray("TargetDirections", IOHelper.direction2Index(destinationDirections));
        return tag;
    }

    @Override
    public void deserialize(CompoundNBT tag) {
        super.deserialize(tag);
        sourceTanks = IOHelper.readBlockPoses(tag.getList("SourcePoses", Constants.NBT.TAG_COMPOUND), new ArrayList<>());
        sourceDirections = IOHelper.index2DirectionFill(tag.getIntArray("SourceDirections"), EnumSet.noneOf(Direction.class));
        destinationTanks = IOHelper.readBlockPoses(tag.getList("TargetPoses", Constants.NBT.TAG_COMPOUND), new ArrayList<>());
        destinationDirections = IOHelper.index2DirectionFill(tag.getIntArray("TargetDirections"), EnumSet.noneOf(Direction.class));
        markDirty();
    }

    @Override
    public List<BlockPos> getInventories(int id) {
        switch (id) {
            case SOURCE_TANKS:
            default:
                return sourceTanks;
            case DESTINATION_TANKS:
                return destinationTanks;
        }
    }

    @Override
    public Set<Direction> getDirections(int id) {
        switch (id) {
            case SOURCE_TANKS:
            default:
                return sourceDirections;
            case DESTINATION_TANKS:
                return destinationDirections;
        }
    }

    @Override
    public void markDirty() {
        dirty = true;
    }
}
