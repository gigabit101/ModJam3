package vswe.stevesfactory.logic.procedure;

import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import vswe.stevesfactory.api.capability.CapabilitySignalReactor;
import vswe.stevesfactory.api.capability.ISignalReactor;
import vswe.stevesfactory.api.logic.*;
import vswe.stevesfactory.api.network.INetworkController;
import vswe.stevesfactory.blocks.RedstoneInputTileEntity;
import vswe.stevesfactory.logic.AbstractProcedure;
import vswe.stevesfactory.logic.Procedures;
import vswe.stevesfactory.logic.execution.ProcedureExecutor;
import vswe.stevesfactory.ui.manager.editor.FlowComponent;
import vswe.stevesfactory.ui.manager.menu.InventorySelectionMenu;
import vswe.stevesfactory.utils.IOHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class RedstoneTriggerProcedure extends AbstractProcedure implements IInventoryTarget {

    public static final int INVENTORIES = 0;

    public static final int HIGH_CHILD = 0;
    public static final int LOW_CHILD = 1;

    private List<BlockPos> watchingSources = new ArrayList<>();

    private int highSignals = 0;
    private boolean reload = true;

    public RedstoneTriggerProcedure() {
        super(Procedures.REDSTONE_TRIGGER.getFactory(), 0, 2);
    }

    @Override
    public void execute(IExecutionContext context) {
        pushFrame(context, HIGH_CHILD);
    }

    @Override
    public void tick() {
        if (reload) {
            for (BlockPos watching : watchingSources) {
                World world = getController().getControllerWorld();
                // TODO capability
                ISignalReactor tile = (ISignalReactor) world.getTileEntity(watching);
                if (tile != null) {
                    tile.subscribeEvent(this::executeHigh, this::executeLow);
                }
            }
            reload = false;
        }
    }

    public boolean isInHighSignal() {
        return highSignals > 0;
    }

    public boolean isInLowSignal() {
        return highSignals <= 0;
    }

    private void executeHigh() {
        highSignals++;
        execute(successors()[HIGH_CHILD]);
    }

    private void executeLow() {
        highSignals--;
        execute(successors()[LOW_CHILD]);
    }

    private void execute(@Nullable Connection connection) {
        if (connection != null) {
            execute(connection.getDestination());
        }
    }

    private void execute(IProcedure child) {
        INetworkController controller = getGraph().getController();
        new ProcedureExecutor(controller, controller.getControllerWorld()).start(child);
    }

    @Override
    public FlowComponent<RedstoneTriggerProcedure> createFlowComponent() {
        FlowComponent<RedstoneTriggerProcedure> f = FlowComponent.of(this, 0, 2);
        f.addMenu(new InventorySelectionMenu<>(INVENTORIES, I18n.format("gui.sfm.Menu.RedstoneTrigger.Watches"), I18n.format("error.sfm.RedstoneTrigger.NoWatches"), CapabilitySignalReactor.SIGNAL_REACTOR_CAPABILITY));
        return f;
    }

    @Override
    public CompoundNBT serialize() {
        CompoundNBT tag = super.serialize();
        tag.put("Watching", IOHelper.writeBlockPoses(watchingSources));
        return tag;
    }

    @Override
    public void deserialize(CompoundNBT tag) {
        super.deserialize(tag);
        watchingSources = IOHelper.readBlockPoses(tag.getList("Watching", Constants.NBT.TAG_COMPOUND), new ArrayList<>());
        reload = true;
    }

    @Override
    public List<BlockPos> getInventories(int id) {
        return watchingSources;
    }
}
