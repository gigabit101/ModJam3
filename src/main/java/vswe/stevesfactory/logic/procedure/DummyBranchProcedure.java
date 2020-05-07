package vswe.stevesfactory.logic.procedure;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import vswe.stevesfactory.api.logic.*;
import vswe.stevesfactory.logic.AbstractProcedure;
import vswe.stevesfactory.setup.ModProcedures;
import vswe.stevesfactory.ui.manager.editor.FlowComponent;

public class DummyBranchProcedure extends AbstractProcedure {

    public static DummyBranchProcedure sequential() {
        return new DummyBranchProcedure(ModProcedures.sequentialBranch, 1, 5);
    }

    public static DummyBranchProcedure merge() {
        return new DummyBranchProcedure(ModProcedures.mergeBranch, 5, 1);
    }

    public DummyBranchProcedure(IProcedureType<?> type, int parents, int children) {
        super(type, parents, children);
    }

    @Override
    public void execute(IExecutionContext context) {
        for (int i = successors().length - 1; i >= 0; i--) {
            pushFrame(context, i);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public FlowComponent<DummyBranchProcedure> createFlowComponent() {
        return new FlowComponent<>(this);
    }
}
