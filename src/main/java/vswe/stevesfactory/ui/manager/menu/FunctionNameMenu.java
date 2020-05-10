package vswe.stevesfactory.ui.manager.menu;

import lombok.val;
import net.minecraft.client.resources.I18n;
import vswe.stevesfactory.library.gui.widget.NotifiedTextField;
import vswe.stevesfactory.library.gui.widget.TextField;
import vswe.stevesfactory.logic.procedure.FunctionHatProcedure;
import vswe.stevesfactory.ui.manager.editor.FlowComponent;
import vswe.stevesfactory.ui.manager.editor.Menu;

import java.util.List;

import static vswe.stevesfactory.library.gui.Render2D.fontHeight;
import static vswe.stevesfactory.library.gui.Render2D.fontRenderer;

public class FunctionNameMenu extends Menu<FunctionHatProcedure> {

    private NotifiedTextField field;

    @Override
    public void onInitialAttach() {
        super.onInitialAttach();

        field = new NotifiedTextField(80, 14);
        field.setBackgroundStyle(TextField.BackgroundStyle.RED_OUTLINE);
        field.alignCenterX(0, getWidth());
        field.alignCenterY(0, getYBottom());
        addChildren(field);
    }

    @Override
    public void renderContents(int mouseX, int mouseY, float partialTicks) {
        super.renderContents(mouseX, mouseY, partialTicks);
        fontRenderer().drawString(
                I18n.format("menu.sfm.FunctionHat.Name"),
                field.getAbsoluteX(), field.getAbsoluteY() - 2 - fontHeight(),
                0xff404040);
    }

    @Override
    public void onLinkFlowComponent(FlowComponent<FunctionHatProcedure> flowComponent) {
        super.onLinkFlowComponent(flowComponent);
        val p = getLinkedProcedure();
        field.setText(p.getFunctionName());
        field.onValueUpdated = p::setFunctionName;
    }

    @Override
    public String getHeadingText() {
        return I18n.format("menu.sfm.FunctionHat");
    }

    @Override
    public List<String> populateErrors(List<String> errors) {
        return errors;
    }
}
