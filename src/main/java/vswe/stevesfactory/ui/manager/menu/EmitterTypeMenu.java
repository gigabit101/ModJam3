package vswe.stevesfactory.ui.manager.menu;

import lombok.val;
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
    private final Map<OperationType, RadioInput> types;

    public EmitterTypeMenu() {
        paragraph = new Paragraph(0, 20, new ArrayList<>());
        paragraph.getTextRenderer().setFontHeight(7);

        valueInput = NumberField.integerFieldRanged(33, 12, 15, 1, 15);
        valueInput.alignRight(this.getWidth());
        valueInput.setBackgroundStyle(TextField.BackgroundStyle.RED_OUTLINE);
        paragraph.setWidth(valueInput.getX() - 4 - paragraph.getX());
        paragraph.addLineSplit(I18n.format("menu.sfm.RedstoneEmitter.Type.Info"));

        types = new EnumMap<>(OperationType.class);
    }

    @Override
    public void onInitialAttach() {
        super.onInitialAttach();

        addChildren(paragraph);
        addChildren(valueInput);

        val controller = new RadioController();
        for (val type : OperationType.VALUES) {
            val box = new RadioInput(controller);
            addChildren(box);
            types.put(type, box);
        }

        FlowLayout.table(0, paragraph.getYBottom() + 2, this.getWidth(), types.values());
        for (val entry : types.entrySet()) {
            val box = entry.getValue();
            val type = entry.getKey();
            addChildren(box.makeLabel().translate(type.nameKey));
        }

        adjustMinHeight();
    }

    @Override
    public void onLinkFlowComponent(FlowComponent<RedstoneEmitterProcedure> flowComponent) {
        super.onLinkFlowComponent(flowComponent);
        val proc = getLinkedProcedure();
        for (val entry : types.entrySet()) {
            val box = entry.getValue();
            val type = entry.getKey();
            box.setCheckAction(() -> proc.setOperationType(type));
        }
        types.get(proc.getOperationType()).setCheckedAndUpdate(true);
        valueInput.setValue(proc.getValue());
        valueInput.onValueUpdated = proc::setValue;
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
