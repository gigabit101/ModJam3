package vswe.stevesfactory.library.gui.widget;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.ints.Int2BooleanFunction;
import vswe.stevesfactory.library.gui.TextureWrapper;

public class CallbackIconButton extends AbstractIconButton {

    public static final Int2BooleanFunction DUMMY = b -> false;

    private TextureWrapper textureNormal;
    private TextureWrapper textureHovering;
    public Int2BooleanFunction onClick = DUMMY;

    public CallbackIconButton(TextureWrapper textureNormal, TextureWrapper textureHovering) {
        super(0, 0, 0, 0);
        this.setTextures(textureNormal, textureHovering);
    }

    @Override
    public TextureWrapper getTextureNormal() {
        return textureNormal;
    }

    @Override
    public TextureWrapper getTextureHovered() {
        return textureHovering;
    }

    public void setTextureNormal(TextureWrapper textureNormal) {
        checkArguments(textureNormal, textureHovering);
        this.textureNormal = textureNormal;
        this.setDimensions(textureNormal.getPortionWidth(), textureNormal.getPortionHeight());
    }

    public void setTextureHovering(TextureWrapper textureHovering) {
        checkArguments(textureNormal, textureHovering);
        this.textureHovering = textureHovering;
        this.setDimensions(textureHovering.getPortionWidth(), textureHovering.getPortionHeight());
    }

    public void setTextures(TextureWrapper textureNormal, TextureWrapper textureHovering) {
        checkArguments(textureNormal, textureHovering);
        this.textureNormal = textureNormal;
        this.textureHovering = textureHovering;
        // Either one is fine, since we checked that they are the same size
        this.setDimensions(textureNormal.getPortionWidth(), textureNormal.getPortionHeight());
    }

    private static void checkArguments(TextureWrapper textureNormal, TextureWrapper textureHovering) {
        Preconditions.checkArgument(textureNormal.getPortionWidth() == textureHovering.getPortionWidth());
        Preconditions.checkArgument(textureNormal.getPortionHeight() == textureHovering.getPortionHeight());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return onClick.apply(button);
    }
}
