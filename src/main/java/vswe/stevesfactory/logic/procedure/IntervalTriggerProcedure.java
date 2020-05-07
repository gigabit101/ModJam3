package vswe.stevesfactory.logic.procedure;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import vswe.stevesfactory.api.logic.IExecutionContext;
import vswe.stevesfactory.api.logic.ITrigger;
import vswe.stevesfactory.api.network.INetworkController;
import vswe.stevesfactory.logic.AbstractProcedure;
import vswe.stevesfactory.setup.ModProcedures;
import vswe.stevesfactory.logic.execution.ProcedureExecutor;
import vswe.stevesfactory.ui.manager.editor.FlowComponent;
import vswe.stevesfactory.ui.manager.menu.IntervalMenu;

public class IntervalTriggerProcedure extends AbstractProcedure implements ITrigger {

    public int interval = 20;

    private transient int tickCounter = 0;

    public IntervalTriggerProcedure() {
        super(ModProcedures.intervalTrigger, 0, 1);
    }

    @Override
    public void execute(IExecutionContext context) {
        pushFrame(context, 0);
    }

    @Override
    public void tick(INetworkController controller) {
        if (tickCounter >= interval) {
            new ProcedureExecutor(controller, controller.getControllerWorld()).start(this);
            tickCounter = 0;
        } else {
            tickCounter++;
        }
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public FlowComponent<IntervalTriggerProcedure> createFlowComponent() {
        FlowComponent<IntervalTriggerProcedure> f = new FlowComponent<>(this);
        f.addMenu(new IntervalMenu());
        return f;
    }

    @Override
    public CompoundNBT serialize() {
        CompoundNBT tag = super.serialize();
        tag.putInt("Interval", interval);
        return tag;
    }

    @Override
    public void deserialize(CompoundNBT tag) {
        super.deserialize(tag);
        interval = tag.getInt("Interval");
    }
}
