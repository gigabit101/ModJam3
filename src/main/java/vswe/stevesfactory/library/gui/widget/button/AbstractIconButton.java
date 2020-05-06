package vswe.stevesfactory.library.gui.widget.button;

import com.mojang.blaze3d.systems.RenderSystem;
import vswe.stevesfactory.library.gui.Texture;
import vswe.stevesfactory.library.gui.debug.ITextReceiver;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.widget.AbstractWidget;
import vswe.stevesfactory.library.gui.widget.mixin.LeafWidgetMixin;

import java.util.function.IntConsumer;

public abstract class AbstractIconButton extends AbstractWidget implements IButton, LeafWidgetMixin {

    private boolean hovered = false;
    private boolean clicked = false;

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        preRenderEvent(mouseX, mouseY);
        RenderSystem.color3f(1F, 1F, 1F);
        Texture tex = isDisabled() ? getTextureDisabled()
                : isClicked() ? getTextureClicked()
                : isHovered() ? getTextureHovered()
                : getTextureNormal();
        tex.render(getAbsoluteX(), getAbsoluteY(), getAbsoluteXRight(), getAbsoluteYBottom(), getZLevel());
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

    public abstract Texture getTextureNormal();

    public abstract Texture getTextureHovered();

    // Optional
    public Texture getTextureClicked() {
        return getTextureHovered();
    }

    public Texture getTextureDisabled() {
        return Texture.NONE;
    }

    @Override
    public boolean hasClickAction() {
        return false;
    }

    @Override
    public IntConsumer getClickAction() {
        return DUMMY;
    }

    @Override
    public void setClickAction(IntConsumer action) {
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
    public boolean onMouseClicked(double mouseX, double mouseY, int button) {
        clicked = true;
        return true;
    }

    @Override
    public boolean onMouseReleased(double mouseX, double mouseY, int button) {
        clicked = false;
        return true;
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        hovered = isInside(mouseX, mouseY);
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
