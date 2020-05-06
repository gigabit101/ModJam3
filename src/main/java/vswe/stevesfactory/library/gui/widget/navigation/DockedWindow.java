package vswe.stevesfactory.library.gui.widget.navigation;

import com.mojang.blaze3d.platform.GlStateManager;
import vswe.stevesfactory.library.gui.Render2D;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.screen.WidgetScreen;
import vswe.stevesfactory.library.gui.widget.AbstractWidget;
import vswe.stevesfactory.library.gui.widget.mixin.LeafWidgetMixin;
import vswe.stevesfactory.library.gui.widget.panel.HorizontalList;
import vswe.stevesfactory.library.gui.window.AbstractDockableWindow;
import vswe.stevesfactory.library.gui.window.DockingBar;

import javax.annotation.Nonnull;
import java.util.Objects;

import static vswe.stevesfactory.library.gui.Render2D.*;

public class DockedWindow extends AbstractWidget implements LeafWidgetMixin {

    public static final int TOP_LEFT_COLOR = 0xff2b2b2b;
    public static final int BOTTOM_RIGHT_COLOR = 0xffffffff;
    public static final int FILL_COLOR = 0xff5c5f61;

    private final AbstractDockableWindow<?> window;
    private String name = "";

    public DockedWindow(AbstractDockableWindow<?> window) {
        this.setDimensions(getSideMargin() * 2, 20);
        this.window = window;
        this.setName(window.getTitle());
    }

    public int getSideMargin() {
        return 4;
    }

    public int getTextColor() {
        return 0xffffffff;
    }

    public String getName() {
        return name;
    }

    private void setName(String name) {
        this.name = name;
        this.setWidth(getSideMargin() + fontRenderer().getStringWidth(name) + getSideMargin());
    }

    public void restore() {
        WidgetScreen.assertActive().defer(() -> {
            window.restore();
            HorizontalList<DockedWindow> list = getParent();
            list.removeChildren(this);
            list.reflow();
        });
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        RenderEventDispatcher.onPreRender(this, mouseX, mouseY);
        GlStateManager.disableTexture();
        beginColoredQuad();
        thickBeveledBox(getAbsoluteX(), getAbsoluteY(), getAbsoluteXRight(), getAbsoluteYBottom(), getZLevel(), 1, TOP_LEFT_COLOR, BOTTOM_RIGHT_COLOR, FILL_COLOR);
        draw();
        GlStateManager.enableTexture();
        Render2D.renderCenteredText(name, getAbsoluteY(), getAbsoluteYBottom(), getAbsoluteX(), getAbsoluteXRight(), getZLevel(), getTextColor());
        RenderEventDispatcher.onPostRender(this, mouseX, mouseY);
    }

    @Override
    public boolean onMouseClicked(double mouseX, double mouseY, int button) {
        restore();
        return true;
    }

    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    public HorizontalList<DockedWindow> getParent() {
        return (HorizontalList<DockedWindow>) Objects.requireNonNull(super.getParent());
    }

    @Override
    public DockingBar getWindow() {
        return (DockingBar) super.getWindow();
    }
}
