package vswe.stevesfactory.library.gui.widget.button;

import com.google.common.base.Preconditions;
import vswe.stevesfactory.library.gui.Texture;
import vswe.stevesfactory.library.gui.widget.mixin.ResizableWidgetMixin;

import java.util.function.IntConsumer;

/**
 * A ready-to-use icon button implementation that stores each mouse state texture.
 */
public class SimpleIconButton extends AbstractIconButton implements ResizableWidgetMixin {

    private Texture textureNormal;
    private Texture textureHovering;
    private IntConsumer onClick = DUMMY;

    public SimpleIconButton(Texture textureNormal, Texture textureHovering) {
        this.setTextures(textureNormal, textureHovering);
    }

    @Override
    public Texture getTextureNormal() {
        return textureNormal;
    }

    @Override
    public Texture getTextureHovered() {
        return textureHovering;
    }

    public void setTextureNormal(Texture textureNormal) {
        checkArguments(textureNormal, textureHovering);
        this.textureNormal = textureNormal;
        this.setDimensions(textureNormal.getPortionWidth(), textureNormal.getPortionHeight());
    }

    public void setTextureHovering(Texture textureHovering) {
        checkArguments(textureNormal, textureHovering);
        this.textureHovering = textureHovering;
        this.setDimensions(textureHovering.getPortionWidth(), textureHovering.getPortionHeight());
    }

    public void setTextures(Texture textureNormal, Texture textureHovering) {
        checkArguments(textureNormal, textureHovering);
        this.textureNormal = textureNormal;
        this.textureHovering = textureHovering;
        // Either one is fine, since we checked that they are the same size
        this.setDimensions(textureNormal.getPortionWidth(), textureNormal.getPortionHeight());
    }

    private static void checkArguments(Texture textureNormal, Texture textureHovering) {
        Preconditions.checkArgument(textureNormal.getPortionWidth() == textureHovering.getPortionWidth());
        Preconditions.checkArgument(textureNormal.getPortionHeight() == textureHovering.getPortionHeight());
    }

    @Override
    public boolean hasClickAction() {
        return onClick != DUMMY;
    }

    @Override
    public IntConsumer getClickAction() {
        return onClick;
    }

    @Override
    public void setClickAction(IntConsumer action) {
        onClick = action;
    }

    @Override
    public boolean onMouseClicked(double mouseX, double mouseY, int button) {
        onClick.accept(button);
        return true;
    }
}
