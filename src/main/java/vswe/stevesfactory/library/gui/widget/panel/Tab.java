package vswe.stevesfactory.library.gui.widget.panel;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.util.math.MathHelper;
import vswe.stevesfactory.library.gui.Render2D;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.screen.BackgroundRenderers;
import vswe.stevesfactory.library.gui.widget.AbstractWidget;
import vswe.stevesfactory.library.gui.widget.mixin.LeafWidgetMixin;

import javax.annotation.Nonnull;
import java.util.Objects;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static vswe.stevesfactory.library.gui.Render2D.fontRenderer;

public class Tab extends AbstractWidget implements LeafWidgetMixin {

    private String name;

    private int initialDragX = -1;

    public Tab(String name) {
        this.name = name;
        this.setBorders(3);
    }

    public String getName() {
        return name;
    }

    private void setName(String name) {
        this.name = name;
        this.setWidth(4 + fontRenderer().getStringWidth(name) + 4);
    }

    @Override
    public boolean onMouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW_MOUSE_BUTTON_LEFT) {
            initialDragX = (int) mouseX - getAbsoluteX();
            return true;
        }
        return false;
    }

    @Override
    public boolean onMouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (isDragging()) {
            setX(MathHelper.clamp((int) mouseX - initialDragX, 0, getParent().getWidth() - this.getWidth()));
            return true;
        }
        return false;
    }

    @Override
    public boolean onMouseReleased(double mouseX, double mouseY, int button) {
        initialDragX = -1;
        return true;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        RenderEventDispatcher.onPreRender(this, mouseX, mouseY);
        GlStateManager.disableTexture();
        BackgroundRenderers.drawVanillaStyle3x3(getAbsoluteX(), getAbsoluteY(), getFullWidth(), getFullHeight(), getZLevel());
        GlStateManager.enableTexture();
        Render2D.renderCenteredText(name, getAbsoluteY(), getAbsoluteYBottom(), getAbsoluteX(), getAbsoluteXRight(), getZLevel(), getTextColor());
        RenderEventDispatcher.onPostRender(this, mouseX, mouseY);
    }

    private int getTextColor() {
        return 0xffffffff;
    }

    @Nonnull
    @Override
    public TabHorizontalList getParent() {
        return (TabHorizontalList) Objects.requireNonNull(super.getParent());
    }

    public boolean isDragging() {
        return initialDragX != -1;
    }
}
