package vswe.stevesfactory.library.gui.widget;

import com.mojang.blaze3d.platform.GlStateManager;
import vswe.stevesfactory.library.gui.TextureWrapper;
import vswe.stevesfactory.library.gui.debug.ITextReceiver;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.widget.mixin.LeafWidgetMixin;

import java.awt.*;

public abstract class AbstractIconButton extends AbstractWidget implements IButton, LeafWidgetMixin {

    private boolean hovered = false;
    private boolean clicked = false;

    public AbstractIconButton(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    public AbstractIconButton(Point location, Dimension dimensions) {
        super(location, dimensions);
    }

    @Override
    public void render(int mouseX, int mouseY, float particleTicks) {
        preRenderEvent(mouseX, mouseY);
        GlStateManager.color3f(1F,1F,1F);
        TextureWrapper tex = isDisabled() ? getTextureDisabled()
                : isClicked() ? getTextureClicked()
                : isHovered() ? getTextureHovered()
                : getTextureNormal();
        tex.draw(getAbsoluteX(), getAbsoluteY(), getWidth(), getHeight());
        postRenderEvent(mouseX, mouseY);
    }

    protected void preRenderEvent(int mx, int my) {
        if (isEnabled()) {
            RenderEventDispatcher.onPreRender(this, mx, my);
        }
    }

    protected void postRenderEvent(int mx, int my) {
        if (isEnabled()) {
            RenderEventDispatcher.onPostRender(this, mx, my);
        }
    }

    public abstract TextureWrapper getTextureNormal();

    public abstract TextureWrapper getTextureHovered();

    // Optional
    public TextureWrapper getTextureClicked() {
        return getTextureHovered();
    }

    public TextureWrapper getTextureDisabled() {
        return TextureWrapper.NONE;
    }

    @Override
    public boolean isClicked() {
        return clicked;
    }

    @Override
    public boolean isHovered() {
        return hovered;
    }

    public boolean isDisabled() {
        return !isEnabled();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        clicked = true;
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        clicked = false;
        return true;
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        hovered = isInside(mouseX, mouseY);
        if (!hovered) {
            clicked = false;
        }
    }

    @Override
    public void provideInformation(ITextReceiver receiver) {
        super.provideInformation(receiver);
        receiver.line("Hovered=" + hovered);
        receiver.line("Clicked=" + clicked);
        receiver.line("NormalTexture=" + getTextureNormal());
        receiver.line("HoveredTexture=" + getTextureHovered());
    }
}
