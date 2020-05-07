package vswe.stevesfactory.library.gui.contextmenu;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.util.ResourceLocation;
import vswe.stevesfactory.library.gui.Render2D;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.widget.AbstractWidget;
import vswe.stevesfactory.library.gui.widget.mixin.LeafWidgetMixin;
import vswe.stevesfactory.library.gui.window.IWindow;

import javax.annotation.Nullable;
import java.awt.*;

import static vswe.stevesfactory.library.gui.Render2D.*;

public class DefaultEntry extends AbstractWidget implements IEntry, LeafWidgetMixin {

    public static final int MARGIN_SIDES = 2;
    public static final int HALF_MARGIN_SIDES = MARGIN_SIDES / 2;
    public static final int RENDERED_ICON_WIDTH = 8;
    public static final int RENDERED_ICON_HEIGHT = 8;

    private final ResourceLocation icon;
    private final String translationKey;

    public DefaultEntry(@Nullable ResourceLocation icon, String translationKey) {
        this.icon = icon;
        this.translationKey = translationKey;
        Dimension bounds = getDimensions();
        bounds.width = computeWidth();
        bounds.height = computeHeight();
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        RenderEventDispatcher.onPreRender(this, mouseX, mouseY);
        renderContents(mouseX, mouseY, partialTicks);
        RenderEventDispatcher.onPostRender(this, mouseX, mouseY);
    }

    protected void renderContents(int mouseX, int mouseY, float partialTicks) {
        int x = getAbsoluteX();
        int y = getAbsoluteY();
        int y2 = getAbsoluteYBottom();
        if (isInside(mouseX, mouseY)) {
            IWindow parent = getWindow();
            RenderSystem.disableTexture();
            beginColoredQuad();
            coloredRect(x, y, parent.getContentX() + parent.getWidth() - parent.getBorderSize() * 2, y2, getZLevel(), 0xff3b86ff);
            draw();
            RenderSystem.enableTexture();
        }

        ResourceLocation icon = getIcon();
        if (icon != null) {
            int iconX = x + MARGIN_SIDES;
            int iconY = y + MARGIN_SIDES;

            RenderSystem.enableAlphaTest();
            beginTexturedQuad();
            bindTexture(icon);
            completeTexture(iconX, iconY, iconX + RENDERED_ICON_WIDTH, iconY + RENDERED_ICON_HEIGHT, 0F);
            draw();
        }

        int textX = x + MARGIN_SIDES + RENDERED_ICON_WIDTH + 2;
        Render2D.renderVerticallyCenteredText(getText(), textX, y, y2, getZLevel(), 0xffffffff);
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
    public void attach(ContextMenu contextMenu) {
        attachWindow(contextMenu);
    }

    public ContextMenu getContextMenu() {
        return (ContextMenu) super.getWindow();
    }

    protected int computeWidth() {
        return MARGIN_SIDES + RENDERED_ICON_WIDTH + 2 + Render2D.fontRenderer().getStringWidth(getText()) + MARGIN_SIDES;
    }

    protected int computeHeight() {
        return MARGIN_SIDES + RENDERED_ICON_HEIGHT + MARGIN_SIDES;
    }

    @Override
    public boolean onMouseClicked(double mouseX, double mouseY, int button) {
        getContextMenu().discard();
        return true;
    }

    @Override
    public boolean forceAlive() {
        return false;
    }
}
