package vswe.stevesfactory.ui.manager.menu;

import lombok.val;
import net.minecraft.client.resources.I18n;
import vswe.stevesfactory.library.gui.layout.FlowLayout;
import vswe.stevesfactory.library.gui.widget.NotifiedTextField;
import vswe.stevesfactory.library.gui.widget.TextField;
import vswe.stevesfactory.logic.procedure.SignUpdaterProcedure;
import vswe.stevesfactory.ui.manager.editor.FlowComponent;
import vswe.stevesfactory.ui.manager.editor.Menu;

import java.util.List;

public class SignUpdaterLinesMenu extends Menu<SignUpdaterProcedure> {

    private final TextField[] textFields = new TextField[4];

    @Override
    public void onLinkFlowComponent(FlowComponent<SignUpdaterProcedure> flowComponent) {
        super.onLinkFlowComponent(flowComponent);
        val procedure = this.getLinkedProcedure();
        for (int i = 0; i < textFields.length; i++) {
            val field = new NotifiedTextField(80, 13);
            field.setBackgroundStyle(TextField.BackgroundStyle.RED_OUTLINE);
            field.setText(procedure.getTexts()[i]);
            int idx = i; // Effectively final variable for lambda capturing
            field.onValueUpdated = text -> procedure.getTexts()[idx] = text;
            textFields[i] = field;
            addChildren(field);
        }
        FlowLayout.vertical(getChildren(), 0, 0, 4);
    }

    @Override
    public String getHeadingText() {
        return I18n.format("menu.sfm.SignUpdater.Lines");
    }

    @Override
    public List<String> populateErrors(List<String> errors) {
        return errors;
    }
}
