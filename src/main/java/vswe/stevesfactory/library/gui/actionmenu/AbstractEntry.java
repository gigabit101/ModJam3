package vswe.stevesfactory.library.gui.actionmenu;

import net.minecraft.util.ResourceLocation;
import vswe.stevesfactory.library.gui.window.IWindow;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.widget.AbstractWidget;
import vswe.stevesfactory.library.gui.widget.mixin.LeafWidgetMixin;
import vswe.stevesfactory.utils.RenderingHelper;

import javax.annotation.Nullable;
import java.awt.*;

public abstract class AbstractEntry extends AbstractWidget implements IEntry, LeafWidgetMixin {

    public static final int MARGIN_SIDES = 2;
    public static final int HALF_MARGIN_SIDES = MARGIN_SIDES / 2;
    public static final int RENDERED_ICON_WIDTH = 8;
    public static final int RENDERED_ICON_HEIGHT = 8;

    private final ResourceLocation icon;
    private final String translationKey;

    public AbstractEntry(@Nullable ResourceLocation icon, String translationKey) {
        super();
        this.icon = icon;
        this.translationKey = translationKey;
        Dimension bounds = getDimensions();
        bounds.width = computeWidth();
        bounds.height = computeHeight();
    }

    @Override
    public void render(int mouseX, int mouseY, float particleTicks) {
        RenderEventDispatcher.onPreRender(this, mouseX, mouseY);

        int x = getAbsoluteX();
        int y = getAbsoluteY();
        int y2 = getAbsoluteYBottom();
        if (isInside(mouseX, mouseY)) {
            IWindow parent = getWindow();
            RenderingHelper.drawRect(x, y, parent.getContentX() + parent.getWidth() - parent.getBorderSize() * 2, y2, 59, 134, 255, 255);
        }

        ResourceLocation icon = getIcon();
        if (icon != null) {
            int iconX = x + MARGIN_SIDES;
            int iconY = y + MARGIN_SIDES;
            RenderingHelper.drawCompleteTexture(iconX, iconY, iconX + RENDERED_ICON_WIDTH, iconY + RENDERED_ICON_HEIGHT, icon);
        }

        int textX = x + MARGIN_SIDES + RENDERED_ICON_WIDTH + 2;
        RenderingHelper.drawTextCenteredVertically(getText(), textX, y, y2, 0xffffffff);
        RenderEventDispatcher.onPostRender(this, mouseX, mouseY);
    }

    @Nullable
    @Override
    public ResourceLocation getIcon() {
        return icon;
    }

    @Override
    public String getTranslationKey() {
        return translationKey;
    }

    @Override
    public void attach(ActionMenu actionMenu) {
        setWindow(actionMenu);
    }

    @Override
    public ActionMenu getWindow() {
        return (ActionMenu) super.getWindow();
    }

    private int computeWidth() {
        return MARGIN_SIDES + RENDERED_ICON_WIDTH + 2 + fontRenderer().getStringWidth(getText()) + MARGIN_SIDES;
    }

    private int computeHeight() {
        return MARGIN_SIDES + RENDERED_ICON_HEIGHT + MARGIN_SIDES;
    }
}
