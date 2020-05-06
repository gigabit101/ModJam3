package vswe.stevesfactory.ui.manager.menu;

import net.minecraft.client.resources.I18n;
import vswe.stevesfactory.library.gui.layout.FlowLayout;
import vswe.stevesfactory.library.gui.widget.*;
import vswe.stevesfactory.logic.procedure.RedstoneEmitterProcedure;
import vswe.stevesfactory.logic.procedure.RedstoneEmitterProcedure.OperationType;
import vswe.stevesfactory.ui.manager.editor.FlowComponent;
import vswe.stevesfactory.ui.manager.editor.Menu;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class EmitterTypeMenu extends Menu<RedstoneEmitterProcedure> {

    private final Paragraph paragraph;
    private final NumberField<Integer> valueInput;
    private final Map<OperationType, RadioInput> type;

    public EmitterTypeMenu() {
        int y = HEADING_BOX.getPortionHeight() + 2;

        paragraph = new Paragraph(0, 20, new ArrayList<>());
        paragraph.setLocation(4, y);
        paragraph.getTextRenderer().setFontHeight(7);
        addChildren(paragraph);

        valueInput = NumberField.integerFieldRanged(33, 12, 15, 1, 15);
        valueInput.alignRight(getWidth() - 4);
        valueInput.setY(y);
        valueInput.setBackgroundStyle(TextField.BackgroundStyle.RED_OUTLINE);
        paragraph.setWidth(valueInput.getX() - 4 - paragraph.getX());
        addChildren(valueInput);

        RadioController controller = new RadioController();
        type = new EnumMap<>(OperationType.class);
        for (OperationType type : OperationType.VALUES) {
            RadioInput box = new RadioInput(controller);
            addChildren(box);
            // TODO label pos
            addChildren(box.makeLabel().translate(type.nameKey));
            this.type.put(type, box);
        }
        FlowLayout.table(4, HEADING_BOX.getPortionHeight() + 25, getWidth(), type);
    }

    @Override
    public void onLinkFlowComponent(FlowComponent<RedstoneEmitterProcedure> flowComponent) {
        super.onLinkFlowComponent(flowComponent);
        RedstoneEmitterProcedure procedure = getLinkedProcedure();
        for (Map.Entry<OperationType, RadioInput> entry : type.entrySet()) {
            RadioInput box = entry.getValue();
            OperationType type = entry.getKey();
            box.setCheckAction(() -> {
                procedure.setOperationType(type);
                paragraph.getTexts().clear();
                paragraph.addLineSplit(I18n.format("menu.sfm.RedstoneEmitter.Type.Info"));
            });
        }
        type.get(procedure.getOperationType()).check(true);
        valueInput.setValue(procedure.getValue());
        valueInput.onValueUpdated = procedure::setValue;
    }

    @Override
    public String getHeadingText() {
        return I18n.format("menu.sfm.RedstoneEmitter.Type");
    }

    @Override
    public List<String> populateErrors(List<String> errors) {
        return errors;
    }
}
