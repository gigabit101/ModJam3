package vswe.stevesfactory.ui.manager.menu;

import net.minecraft.client.resources.I18n;
import vswe.stevesfactory.library.gui.Render2D;
import vswe.stevesfactory.library.gui.widget.NumberField;
import vswe.stevesfactory.library.gui.widget.Paragraph;
import vswe.stevesfactory.library.gui.widget.TextField;
import vswe.stevesfactory.logic.procedure.IntervalTriggerProcedure;
import vswe.stevesfactory.ui.manager.editor.FlowComponent;
import vswe.stevesfactory.ui.manager.editor.Menu;

import java.util.ArrayList;
import java.util.List;

import static vswe.stevesfactory.library.gui.Render2D.fontRenderer;

public class IntervalMenu extends Menu<IntervalTriggerProcedure> {

    public static final int MARGIN_MIDDLE_UNIT_TEXT = 10;

    private final NumberField<Integer> interval;

    public IntervalMenu() {
        interval = NumberField.integerFieldRanged(38, 14, 1, 1, 999);
        int x = Render2D.computeCenterX(0, getWidth(), interval.getWidth() + MARGIN_MIDDLE_UNIT_TEXT + fontRenderer().getStringWidth(getUnitText()));
        interval.setValue(1);
        interval.setBackgroundStyle(TextField.BackgroundStyle.RED_OUTLINE);
        interval.setLocation(x, 50);

        int desX = interval.getX();
        Paragraph description = new Paragraph(getWidth() - x * 2, 0, new ArrayList<>());
        description.setLocation(desX, HEADING_BOX.getPortionHeight() + 8);
        description.setFitContents(true);
        description.addLineSplit(getWidth() - 4 * 2, I18n.format("menu.sfm.Interval.Info"));
        description.getTextRenderer().setFontHeight(8);

        addChildren(interval);
        addChildren(description);
    }

    @Override
    public void onLinkFlowComponent(FlowComponent<IntervalTriggerProcedure> flowComponent) {
        super.onLinkFlowComponent(flowComponent);
        IntervalTriggerProcedure procedure = getLinkedProcedure();
        interval.setValue(procedure.interval / 20);
        // Convert to ticks
        interval.onValueUpdated = seconds -> procedure.interval = seconds * 20;
    }

    @Override
    public String getHeadingText() {
        return I18n.format("menu.sfm.Interval");
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);
    }

    @Override
    public void renderContents(int mouseX, int mouseY, float partialTicks) {
        super.renderContents(mouseX, mouseY, partialTicks);
        Render2D.renderVerticallyCenteredText(
                getUnitText(),
                interval.getAbsoluteXRight() + MARGIN_MIDDLE_UNIT_TEXT,
                interval.getAbsoluteY(), interval.getAbsoluteYBottom(),
                getZLevel(),
                0xff404040);
    }

    public String getUnitText() {
        return I18n.format("gui.sfm.seconds");
    }

    public int getIntervalSeconds() {
        return interval.getValue();
    }

    public int getIntervalTicks() {
        return getIntervalSeconds() * 20;
    }

    @Override
    public List<String> populateErrors(List<String> errors) {
        return errors;
    }
}
