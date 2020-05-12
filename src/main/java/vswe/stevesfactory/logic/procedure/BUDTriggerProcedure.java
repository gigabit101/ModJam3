package vswe.stevesfactory.logic.procedure;

import lombok.val;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;
import vswe.stevesfactory.api.capability.CapabilityEventDispatchers;
import vswe.stevesfactory.api.logic.*;
import vswe.stevesfactory.api.network.INetworkController;
import vswe.stevesfactory.logic.AbstractProcedure;
import vswe.stevesfactory.logic.execution.ProcedureExecutor;
import vswe.stevesfactory.setup.ModProcedures;
import vswe.stevesfactory.ui.manager.editor.FlowComponent;
import vswe.stevesfactory.ui.manager.menu.InventorySelectionMenu;
import vswe.stevesfactory.utils.IOHelper;

import java.util.ArrayList;
import java.util.List;

public class BUDTriggerProcedure extends AbstractProcedure implements ITrigger, IInventoryTarget {

    public static final int WATCHING = 0;

    private List<BlockPos> watchingSources = new ArrayList<>();
    private boolean dirty = true;

    public BUDTriggerProcedure() {
        super(ModProcedures.budTrigger, 0, 1);
    }

    @Override
    public void execute(IExecutionContext context) {
        pushFrame(context, 0);
    }

    @Override
    public void tick(INetworkController controller) {
        if (!dirty && this.isValid()) {
            return;
        }

        val world = controller.getControllerWorld();
        for (val watching : watchingSources) {
            val tile = world.getTileEntity(watching);
            if (tile == null) {
                continue;
            }

            val cap = tile.getCapability(CapabilityEventDispatchers.BUD_EVENT_DISPATCHER_CAPABILITY);
            if (!cap.isPresent()) {
                continue;
            }
            val dispatcher = cap.orElseThrow(RuntimeException::new);

            dispatcher.subscribe(pos -> {
                // Remove the listener if this procedure is invalid (removed, etc.)
                if (!this.isValid()) {
                    return true;
                }

                Connection connection = successors()[0];
                if (connection != null) {
                    new ProcedureExecutor(controller, world).start(connection.getDestination());
                }
                return false;
            });
        }
        dirty = false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public FlowComponent<BUDTriggerProcedure> createFlowComponent() {
        val f = new FlowComponent<>(this);
        f.addMenu(new InventorySelectionMenu<>(WATCHING, I18n.format("menu.sfm.BUDTrigger.Watches"), I18n.format("error.sfm.BUDTrigger.NoWatches"), CapabilityEventDispatchers.BUD_EVENT_DISPATCHER_CAPABILITY));
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
}
