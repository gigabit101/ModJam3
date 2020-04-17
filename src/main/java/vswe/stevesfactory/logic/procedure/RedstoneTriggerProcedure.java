package vswe.stevesfactory.logic.procedure;

import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import vswe.stevesfactory.api.capability.*;
import vswe.stevesfactory.api.logic.*;
import vswe.stevesfactory.api.network.INetworkController;
import vswe.stevesfactory.logic.AbstractProcedure;
import vswe.stevesfactory.setup.ModProcedures;
import vswe.stevesfactory.logic.execution.ProcedureExecutor;
import vswe.stevesfactory.ui.manager.editor.FlowComponent;
import vswe.stevesfactory.ui.manager.menu.*;
import vswe.stevesfactory.utils.IOHelper;
import vswe.stevesfactory.utils.Utils;

import java.util.*;

public class RedstoneTriggerProcedure extends AbstractProcedure implements ITrigger, IInventoryTarget, IDirectionTarget, ILogicalConjunction, IAnalogTarget {

    public static final int WATCHING = 0;
    public static final int DIRECTIONS = 0;

    public static final int RISING_EDGE_CHILD = 0;
    public static final int FALLING_EDGE_CHILD = 1;

    private List<BlockPos> watchingSources = new ArrayList<>();
    private Set<Direction> directions = EnumSet.allOf(Direction.class);
    private Type conjunction = Type.ANY;
    private int analogBegin = 1;
    private int analogEnd = 15;
    private boolean invertAnalog = false;

    // Do not cache caps because we use it once, to set listeners for redstone changes
    // However we still want the dirty check to update listeners upon data mutation
    private transient boolean dirty = true;

    public RedstoneTriggerProcedure() {
        super(ModProcedures.redstoneTrigger, 0, 2);
    }

    @Override
    public void execute(IExecutionContext context) {
        pushFrame(context, RISING_EDGE_CHILD);
    }

    @Override
    public void tick(INetworkController controller) {
        if (!dirty && this.isValid()) {
            return;
        }

        World world = controller.getControllerWorld();
        for (BlockPos watching : watchingSources) {
            TileEntity tile = world.getTileEntity(watching);
            if (tile == null) {
                continue;
            }

            LazyOptional<IRedstoneEventDispatcher> cap = tile.getCapability(CapabilityEventDispatchers.REDSTONE_EVENT_DISPATCHER_CAPABILITY);
            if (!cap.isPresent()) {
                continue;
            }
            IRedstoneEventDispatcher dispatcher = cap.orElseThrow(RuntimeException::new);

            // This holder is captured by this specific event handler
            BooleanHolder last = new BooleanHolder(dispatcher.hasSignal());
            dispatcher.subscribe(status -> {
                // If this procedure is invalid, which means it was removed from the controller, remove the event handler
                if (!this.isValid()) {
                    return true;
                }

                // Actual triggering logic
                boolean current = applyConjunction(status);
                if (current != last.value) {
                    Connection connection = successors()[current ? RISING_EDGE_CHILD : FALLING_EDGE_CHILD];
                    if (connection != null) {
                        new ProcedureExecutor(controller, world).start(connection.getDestination());
                    }
                    last.value = current;
                }
                return false;
            });
        }
        dirty = false;
    }

    private boolean applyConjunction(IRedstoneEventDispatcher.SignalStatus status) {
        boolean result = conjunction == Type.ALL;
        for (Direction direction : directions) {
            int power = status.get(direction);
            result = conjunction.combine(result, applyAnalog(power));
        }
        return result;
    }

    private boolean applyAnalog(int power) {
        return Utils.invertIf(power >= analogBegin && power <= analogEnd, invertAnalog);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public FlowComponent<RedstoneTriggerProcedure> createFlowComponent() {
        FlowComponent<RedstoneTriggerProcedure> f = FlowComponent.of(this);
        f.addMenu(new InventorySelectionMenu<>(WATCHING, I18n.format("menu.sfm.RedstoneTrigger.Watches"), I18n.format("error.sfm.RedstoneTrigger.NoWatches"), CapabilityEventDispatchers.REDSTONE_EVENT_DISPATCHER_CAPABILITY));
        f.addMenu(new RedstoneSidesMenu<>(DIRECTIONS,
                () -> conjunction == Type.ANY, () -> conjunction = Type.ANY, I18n.format("menu.sfm.IfAny"),
                () -> conjunction == Type.ALL, () -> conjunction = Type.ALL, I18n.format("menu.sfm.RequireAll"),
                I18n.format("menu.sfm.RedstoneTrigger.Sides"), I18n.format("menu.sfm.RedstoneTrigger.Sides.Info")));
        f.addMenu(new RedstoneStrengthMenu<>());
        return f;
    }

    @Override
    public CompoundNBT serialize() {
        CompoundNBT tag = super.serialize();
        tag.put("Watching", IOHelper.writeBlockPoses(watchingSources));
        tag.putIntArray("Directions", IOHelper.direction2Index(directions));
        tag.putInt("ConjunctionType", conjunction.ordinal());
        tag.putInt("AnalogBegin", analogBegin);
        tag.putInt("AnalogEnd", analogEnd);
        tag.putBoolean("InvertAnalog", invertAnalog);
        return tag;
    }

    @Override
    public void deserialize(CompoundNBT tag) {
        super.deserialize(tag);
        watchingSources = IOHelper.readBlockPoses(tag.getList("Watching", Constants.NBT.TAG_COMPOUND), new ArrayList<>());
        directions = IOHelper.index2DirectionFill(tag.getIntArray("Directions"), EnumSet.noneOf(Direction.class));
        conjunction = Type.VALUES[tag.getInt("ConjunctionType")];
        analogBegin = tag.getInt("AnalogBegin");
        analogEnd = tag.getInt("AnalogEnd");
        invertAnalog = tag.getBoolean("InvertCondition");
        markDirty();
    }

    @Override
    public List<BlockPos> getInventories(int id) {
        return watchingSources;
    }

    @Override
    public void markDirty() {
        dirty = true;
    }

    @Override
    public Set<Direction> getDirections(int id) {
        return directions;
    }

    @Override
    public Type getConjunction() {
        return conjunction;
    }

    @Override
    public void setConjunction(Type type) {
        conjunction = type;
    }

    @Override
    public int getAnalogBegin() {
        return analogBegin;
    }

    @Override
    public void setAnalogBegin(int analogBegin) {
        this.analogBegin = analogBegin;
    }

    @Override
    public int getAnalogEnd() {
        return analogEnd;
    }

    @Override
    public void setAnalogEnd(int analogEnd) {
        this.analogEnd = analogEnd;
    }

    @Override
    public boolean isInverted() {
        return invertAnalog;
    }

    @Override
    public void setInverted(boolean inverted) {
        invertAnalog = inverted;
    }

    private static final class BooleanHolder {

        public boolean value;

        public BooleanHolder(boolean value) {
            this.value = value;
        }
    }
}
