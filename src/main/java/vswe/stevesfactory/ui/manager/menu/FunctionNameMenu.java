package vswe.stevesfactory.ui.manager.menu;

import net.minecraft.client.resources.I18n;
import vswe.stevesfactory.library.gui.widget.TextField;
import vswe.stevesfactory.logic.procedure.FunctionHatProcedure;
import vswe.stevesfactory.ui.manager.editor.FlowComponent;
import vswe.stevesfactory.ui.manager.editor.Menu;

import java.util.List;

import static vswe.stevesfactory.library.gui.RenderingHelper.fontHeight;
import static vswe.stevesfactory.library.gui.RenderingHelper.fontRenderer;

public class FunctionNameMenu extends Menu<FunctionHatProcedure> {

    private TextField field;

    public FunctionNameMenu() {
        field = new TextField(0, 0, 80, 14);
        field.setBackgroundStyle(TextField.BackgroundStyle.RED_OUTLINE);
        field.alignCenterX(0, getWidth());
        field.alignCenterY(HEADING_BOX.getPortionHeight(), HEADING_BOX.getPortionHeight() + getContentHeight());
        addChildren(field);
    }

    @Override
    public void renderContents(int mouseX, int mouseY, float particleTicks) {
        super.renderContents(mouseX, mouseY, particleTicks);
        fontRenderer().drawString(
                I18n.format("menu.sfm.FunctionHat.Name"),
                field.getAbsoluteX(), field.getAbsoluteY() - 2 - fontHeight(),
                0xff404040);
    }

    @Override
    public void onLinkFlowComponent(FlowComponent<FunctionHatProcedure> flowComponent) {
        super.onLinkFlowComponent(flowComponent);
        FunctionHatProcedure p = getLinkedProcedure();
        field.setText(p.getFunctionName());
    }

    @Override
    protected void saveData() {
        FunctionHatProcedure p = getLinkedProcedure();
        p.setFunctionName(field.getText());
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
