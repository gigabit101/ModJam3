package vswe.stevesfactory.library.gui.widget;

import vswe.stevesfactory.library.gui.TextureWrapper;
import vswe.stevesfactory.library.gui.debug.ITextReceiver;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.widget.mixin.LeafWidgetMixin;

import java.awt.*;

public class Icon extends AbstractWidget implements INamedElement, LeafWidgetMixin {

    private TextureWrapper texture;

    public Icon(int x, int y, TextureWrapper texture) {
        super(x, y, texture.getPortionWidth(), texture.getPortionHeight());
        this.texture = texture;
    }

    public Icon(IWidget parent, Point location, TextureWrapper texture) {
        super(location, new Dimension(texture.getPortionWidth(), texture.getPortionHeight()));
        setParentWidget(parent);
        this.texture = texture;
    }

    @Override
    public void render(int mouseX, int mouseY, float particleTicks) {
        RenderEventDispatcher.onPreRender(this, mouseX, mouseY);
        if (isEnabled()) {
            texture.draw(getAbsoluteX(), getAbsoluteY());
        }
        RenderEventDispatcher.onPostRender(this, mouseX, mouseY);
    }

    public TextureWrapper getTexture() {
        return texture;
    }

    public void setTexture(TextureWrapper texture) {
        this.texture = texture;
    }

    @Override
    public void provideInformation(ITextReceiver receiver) {
        super.provideInformation(receiver);
        receiver.line("Texture=" + texture);
    }

    @Override
    public String getName() {
        return texture.toString();
    }
}
