package vswe.stevesfactory.logic.procedure;

import lombok.val;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import vswe.stevesfactory.api.logic.*;
import vswe.stevesfactory.logic.AbstractProcedure;
import vswe.stevesfactory.setup.ModProcedures;
import vswe.stevesfactory.ui.manager.editor.FlowComponent;
import vswe.stevesfactory.ui.manager.menu.InvocationTargetMenu;

public class FunctionInvokeProcedure extends AbstractProcedure {

    private IProcedure target;

    public FunctionInvokeProcedure() {
        super(ModProcedures.functionInvoke);
    }

    @Override
    public void execute(IExecutionContext context) {
        context.push(target);
        pushFrame(context, 0);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public FlowComponent<FunctionInvokeProcedure> createFlowComponent() {
        val f = new FlowComponent<>(this);
        f.addMenu(new InvocationTargetMenu());
        return f;
    }

    @Override
    public CompoundNBT serializeExtra(SerializationContext ctx) {
        val tag = new CompoundNBT();
        tag.putInt("CallTarget", ctx.identify(target));
        return tag;
    }

    @Override
    public void deserializeExtra(CompoundNBT tag, DeserializationContext ctx) {
        target = ctx.retrieveNullable(tag.getInt("CallTarget"));
    }

    public IProcedure getTarget() {
        return target;
    }

    public void setTarget(IProcedure target) {
        this.target = target;
    }
}
