package vswe.stevesfactory.library.gui.widget;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.ints.Int2BooleanFunction;
import vswe.stevesfactory.library.gui.Texture;
import vswe.stevesfactory.library.gui.widget.button.AbstractIconButton;

public class CallbackIconButton extends AbstractIconButton {

    public static final Int2BooleanFunction DUMMY = b -> false;

    private Texture textureNormal;
    private Texture textureHovering;
    public Int2BooleanFunction onClick = DUMMY;

    public CallbackIconButton(Texture textureNormal, Texture textureHovering) {
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
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return onClick.apply(button);
    }
}
