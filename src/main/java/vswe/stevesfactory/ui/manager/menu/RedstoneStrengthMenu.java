package vswe.stevesfactory.ui.manager.menu;

import net.minecraft.client.resources.I18n;
import vswe.stevesfactory.api.logic.IClientDataStorage;
import vswe.stevesfactory.api.logic.IProcedure;
import vswe.stevesfactory.library.gui.Render2D;
import vswe.stevesfactory.library.gui.layout.properties.HorizontalAlignment;
import vswe.stevesfactory.library.gui.layout.properties.Side;
import vswe.stevesfactory.library.gui.layout.properties.VerticalAlignment;
import vswe.stevesfactory.library.gui.widget.Checkbox;
import vswe.stevesfactory.library.gui.widget.NumberField;
import vswe.stevesfactory.library.gui.widget.Paragraph;
import vswe.stevesfactory.library.gui.widget.TextField.BackgroundStyle;
import vswe.stevesfactory.logic.procedure.IAnalogTarget;
import vswe.stevesfactory.ui.manager.editor.FlowComponent;
import vswe.stevesfactory.ui.manager.editor.Menu;

import java.util.ArrayList;
import java.util.List;

public class RedstoneStrengthMenu<P extends IProcedure & IClientDataStorage & IAnalogTarget> extends Menu<P> {

    private final NumberField<Integer> begin;
    private final NumberField<Integer> end;
    private final Checkbox invertCondition;
    private final Paragraph info;

    public RedstoneStrengthMenu() {
        begin = NumberField.integerFieldRanged(33, 12, 1, 1, 15);
        begin.setBackgroundStyle(BackgroundStyle.RED_OUTLINE);
        end = NumberField.integerFieldRanged(33, 12, 1, 1, 15);
        end.setBackgroundStyle(BackgroundStyle.RED_OUTLINE);
        invertCondition = new Checkbox();
        invertCondition.setDimensions(8, 8);
        info = new Paragraph(getWidth() - 10 * 2, 16, new ArrayList<>());
        info.setWidth(this.getWidth());
        info.getTextRenderer().setFontHeight(6);
        info.addLineSplit(I18n.format("menu.sfm.RedstoneTrigger.Strength.Info"));
    }

    @Override
    public void onInitialAttach() {
        super.onInitialAttach();

        addChildren(begin);
        addChildren(end);
        addChildren(invertCondition);
        addChildren(info);
        reflow();
        addChildren(invertCondition.makeLabel().translate("menu.sfm.InvertCondition"));
    }

    @Override
    public void onLinkFlowComponent(FlowComponent<P> flowComponent) {
        super.onLinkFlowComponent(flowComponent);
        P procedure = getLinkedProcedure();
        // Using method reference causes LambdaConversionError due to type erasure. This is fixed in JDK 9, which is not available to Minecraft (yet)
        // See JDK-8141508
        begin.setValue(procedure.getAnalogBegin());
        begin.onValueUpdated = begin -> procedure.setAnalogBegin(begin);
        end.setValue(procedure.getAnalogEnd());
        end.onValueUpdated = end -> procedure.setAnalogEnd(end);
        invertCondition.setChecked(procedure.isInverted());
        invertCondition.onStateChange = b -> procedure.setInverted(b);
    }

    @Override
    public void reflow() {
        begin.alignTo(info, Side.BOTTOM, HorizontalAlignment.LEFT.asUnion());
        begin.moveY(2);
        end.alignTo(info, Side.BOTTOM, HorizontalAlignment.RIGHT.asUnion());
        end.moveY(2);
        invertCondition.alignLeft(begin.getX());
        invertCondition.setY(begin.getYBottom() + 4);
//        int y = HEADING_BOX.getPortionHeight() + 20;
//        begin.alignLeft(4);
//        begin.setY(y);
//        end.alignRight(getWidth() - 4);
//        end.setY(y);
//        invertCondition.setLocation(4, y + 20);
    }

    @Override
    public void renderContents(int mouseX, int mouseY, float partialTicks) {
        super.renderContents(mouseX, mouseY, partialTicks);
        String text = I18n.format("menu.sfm.NumberRange");
        Render2D.renderVerticallyCenteredText(text, begin.getAbsoluteXRight() + 2, begin.getAbsoluteY(), begin.getAbsoluteYBottom(), getZLevel(), 0xff404040);
    }

    @Override
    public String getHeadingText() {
        return I18n.format("menu.sfm.RedstoneTrigger.Strength");
    }

    @Override
    public List<String> populateErrors(List<String> errors) {
        return errors;
    }
}
