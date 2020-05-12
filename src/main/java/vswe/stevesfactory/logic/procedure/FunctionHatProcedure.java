package vswe.stevesfactory.logic.procedure;

import lombok.val;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import vswe.stevesfactory.api.logic.*;
import vswe.stevesfactory.api.network.INetworkController;
import vswe.stevesfactory.logic.AbstractProcedure;
import vswe.stevesfactory.setup.ModProcedures;
import vswe.stevesfactory.ui.manager.editor.FlowComponent;
import vswe.stevesfactory.ui.manager.menu.FunctionNameMenu;

public class FunctionHatProcedure extends AbstractProcedure implements ITrigger, IFunctionHat {

    private String funcName = "";

    public static FunctionHatProcedure functionHat() {
        return new FunctionHatProcedure(ModProcedures.functionHat);
    }

    public FunctionHatProcedure(IProcedureType<?> type) {
        super(type, 0, 1);
    }

    @Override
    public void execute(IExecutionContext context) {
        pushFrame(context, 0);
    }

    @Override
    public void tick(INetworkController controller) {
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public FlowComponent<FunctionHatProcedure> createFlowComponent() {
        val f = new FlowComponent<>(this);
        f.addMenu(new FunctionNameMenu());
        return f;
    }

    @Override
    public CompoundNBT serialize() {
        val tag = super.serialize();
        tag.putString("FunctionName", funcName);
        return tag;
    }

    @Override
    public void deserialize(CompoundNBT tag) {
        super.deserialize(tag);
        funcName = tag.getString("FunctionName");
    }

    @Override
    public String getFunctionName() {
        return funcName;
    }

    public void setFunctionName(String funcName) {
        this.funcName = funcName;
    }
}
