package vswe.stevesfactory.library.gui.widget;

import vswe.stevesfactory.library.gui.TextureWrapper;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.widget.mixin.LeafWidgetMixin;
import vswe.stevesfactory.library.gui.widget.mixin.RelocatableWidgetMixin;

import java.awt.*;

public final class Icon extends AbstractWidget implements RelocatableWidgetMixin, LeafWidgetMixin {

    private TextureWrapper texture;

    public Icon(int x, int y, int width, int height, TextureWrapper texture) {
        super(x, y, width, height);
        this.texture = texture;
    }

    public Icon(Point location, Dimension dimensions, TextureWrapper texture) {
        super(location, dimensions);
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
}
