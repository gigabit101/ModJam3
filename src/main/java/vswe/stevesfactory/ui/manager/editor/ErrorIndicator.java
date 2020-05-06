package vswe.stevesfactory.ui.manager.editor;

import net.minecraft.client.resources.I18n;
import vswe.stevesfactory.api.logic.IErrorPopulator;
import vswe.stevesfactory.library.gui.Render2D;
import vswe.stevesfactory.library.gui.Texture;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.screen.WidgetScreen;
import vswe.stevesfactory.library.gui.widget.AbstractWidget;
import vswe.stevesfactory.library.gui.widget.mixin.LeafWidgetMixin;

import java.util.ArrayList;
import java.util.List;

public class ErrorIndicator extends AbstractWidget implements LeafWidgetMixin {

    public static ErrorIndicator error() {
        return new ErrorIndicator(I18n.format("error.sfm.Error"), ERROR, ERROR_HOVERED);
    }

    public static ErrorIndicator warning() {
        return new ErrorIndicator(I18n.format("error.sfm.Warning"), WARNING, WARNING_HOVERED);
    }

    public static final Texture ERROR = Render2D.ofFlowComponent(40, 52, 2, 10);
    public static final Texture ERROR_HOVERED = ERROR.right(2);
    public static final Texture WARNING = ERROR.right(1);
    public static final Texture WARNING_HOVERED = WARNING.right(2);

    private Texture background;
    private Texture backgroundHovered;
    private final List<String> errors = new ArrayList<>();
    private final String heading;

    private ErrorIndicator(String heading, Texture background, Texture backgroundHovered) {
        this.heading = heading;
        this.background = background;
        this.backgroundHovered = backgroundHovered;
        this.setDimensions(background.getPortionWidth(), background.getPortionHeight());
    }

    public void clearErrors() {
        errors.clear();
        errors.add(heading);
    }

    public void populateErrors(IErrorPopulator handler) {
        handler.populateErrors(errors);
    }

    public void repopulateErrors(IErrorPopulator handler) {
        clearErrors();
        handler.populateErrors(errors);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        RenderEventDispatcher.onPreRender(this, mouseX, mouseY);
        // We will always have a heading in the list
        if (errors.size() > 1) {
            if (isInside(mouseX, mouseY)) {
                backgroundHovered.render(getAbsoluteX(), getAbsoluteY());
                WidgetScreen.assertActive().scheduleTooltip(errors, mouseX, mouseY);
            } else {
                background.render(getAbsoluteX(), getAbsoluteY());
            }
        }
        RenderEventDispatcher.onPostRender(this, mouseX, mouseY);
    }
}
