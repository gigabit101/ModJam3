package vswe.stevesfactory.logic.procedure;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import org.apache.commons.lang3.tuple.Pair;
import vswe.stevesfactory.api.capability.CapabilityRedstone;
import vswe.stevesfactory.api.capability.IRedstoneHandler;
import vswe.stevesfactory.api.capability.IRedstoneHandler.Type;
import vswe.stevesfactory.api.logic.IExecutionContext;
import vswe.stevesfactory.logic.AbstractProcedure;
import vswe.stevesfactory.setup.ModProcedures;
import vswe.stevesfactory.ui.manager.editor.FlowComponent;
import vswe.stevesfactory.ui.manager.menu.EmitterTypeMenu;
import vswe.stevesfactory.ui.manager.menu.InventorySelectionMenu;
import vswe.stevesfactory.ui.manager.menu.RadioOptionsMenu;
import vswe.stevesfactory.ui.manager.menu.RedstoneSidesMenu;
import vswe.stevesfactory.utils.IOHelper;
import vswe.stevesfactory.utils.NetworkHelper;

import java.util.*;

public class RedstoneEmitterProcedure extends AbstractProcedure implements IInventoryTarget, IDirectionTarget {

    public static final int EMITTERS = 0;
    public static final int SIDES = 0;

    private List<BlockPos> emitters = new ArrayList<>();
    private Set<Direction> directions = EnumSet.allOf(Direction.class);
    private IRedstoneHandler.Type signalType = IRedstoneHandler.Type.WEAK;
    private OperationType operationType = OperationType.FIXED;
    private int value = 15;

    private transient List<LazyOptional<IRedstoneHandler>> cachedRedstoneCaps = new ArrayList<>();
    private transient boolean dirty = false;

    public RedstoneEmitterProcedure() {
        super(ModProcedures.redstoneEmitter);
    }

    @Override
    public void execute(IExecutionContext context) {
        pushFrame(context, 0);
        if (hasError()) {
            return;
        }

        updateCache(context);
        for (LazyOptional<IRedstoneHandler> cap : cachedRedstoneCaps) {
            cap.ifPresent(redstone -> {
                redstone.setType(signalType);
                switch (operationType) {
                    case FIXED: {
                        redstone.setSignal(value);
                        break;
                    }
                    case TOGGLE: {
                        int signal = redstone.getSignal();
                        redstone.setSignal(signal > 0 ? 0 : value);
                        break;
                    }
                    case FORWARD: {
                        // Add to existing signal and wrap around
                        redstone.setSignal((redstone.getSignal() + value) % 16);
                        break;
                    }
                    case BACKWARD: {
                        // Subtract from existing signal and wrap around
                        int signal = redstone.getSignal() - value;
                        redstone.setSignal(signal < 0 ? signal + 16 : signal);
                        break;
                    }
                    case MIN: {
                        redstone.setSignal(Math.min(redstone.getSignal(), value));
                        break;
                    }
                    case MAX: {
                        redstone.setSignal(Math.max(redstone.getSignal(), value));
                        break;
                    }
                    case INCREASE: {
                        // Add to existing signal and cap at 15
                        redstone.setSignal(redstone.getSignal() + value);
                        break;
                    }
                    case DECREASE: {
                        // Subtract from existing signal and cap at 0
                        redstone.setSignal(redstone.getSignal() - value);
                        break;
                    }
                }
            });
        }
    }

    public boolean hasError() {
        return emitters.isEmpty() || directions.isEmpty();
    }

    private void updateCache(IExecutionContext context) {
        if (!dirty) {
            return;
        }

        cachedRedstoneCaps.clear();
        NetworkHelper.cacheDirectionalCaps(context, cachedRedstoneCaps, emitters, directions, CapabilityRedstone.REDSTONE_CAPABILITY, __ -> markDirty());
        dirty = false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public FlowComponent<RedstoneEmitterProcedure> createFlowComponent() {
        FlowComponent<RedstoneEmitterProcedure> f = new FlowComponent<>(this);
        f.addMenu(new InventorySelectionMenu<>(EMITTERS, I18n.format("menu.sfm.RedstoneEmitter.Emitters"), I18n.format("error.sfm.RedstoneEmitter.NoEmitters"), CapabilityRedstone.REDSTONE_CAPABILITY));
        f.addMenu(new RedstoneSidesMenu<>(SIDES, I18n.format("menu.sfm.RedstoneEmitter.Sides")));
        f.addMenu(new RadioOptionsMenu<>(
                I18n.format("menu.sfm.RedstoneEmitter.SignalType"),
                I18n.format("menu.sfm.RedstoneEmitter.SignalType.Info"),
                ImmutableList.of(
                        Pair.of(() -> signalType = Type.WEAK, I18n.format("menu.sfm.WeakRedstoneSignal")),
                        Pair.of(() -> signalType = Type.STRONG, I18n.format("menu.sfm.StrongRedstoneSignal"))
                ),
                signalType == Type.WEAK ? 0 : 1));
        f.addMenu(new EmitterTypeMenu());
        return f;
    }

    @Override
    public CompoundNBT serialize() {
        CompoundNBT tag = super.serialize();
        tag.put("Emitters", IOHelper.writeBlockPoses(emitters));
        tag.putIntArray("Directions", IOHelper.direction2Index(directions));
        tag.putInt("OperationType", operationType.ordinal());
        tag.putInt("SignalType", signalType.ordinal());
        tag.putInt("Value", value);
        return tag;
    }

    @Override
    public void deserialize(CompoundNBT tag) {
        super.deserialize(tag);
        emitters = IOHelper.readBlockPoses(tag.getList("Emitters", Constants.NBT.TAG_COMPOUND), new ArrayList<>());
        directions = IOHelper.index2DirectionFill(tag.getIntArray("Directions"), EnumSet.noneOf(Direction.class));
        operationType = OperationType.VALUES[tag.getInt("OperationType")];
        signalType = IRedstoneHandler.Type.VALUES[tag.getInt("SignalType")];
        value = tag.getInt("Value");
        markDirty();
    }

    @Override
    public List<BlockPos> getInventories(int id) {
        return emitters;
    }

    @Override
    public void markDirty() {
        dirty = true;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public OperationType getOperationType() {
        return operationType;
    }

    public void setOperationType(OperationType operationType) {
        this.operationType = operationType;
    }

    @Override
    public Set<Direction> getDirections(int id) {
        return directions;
    }

    public enum OperationType {
        FIXED("Fixed"),
        TOGGLE("Toggle"),
        FORWARD("Forward"),
        BACKWARD("Backward"),
        MIN("Min"),
        MAX("Max"),
        INCREASE("Increase"),
        DECREASE("Decrease");

        public final String nameKey;

        OperationType(String nameKey) {
            this.nameKey = "menu.sfm.RedstoneEmitter.Type." + nameKey;
        }

        public static final OperationType[] VALUES = values();
    }
}
