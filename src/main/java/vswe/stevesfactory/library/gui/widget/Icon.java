package vswe.stevesfactory.library.gui.widget;

import net.minecraft.util.IStringSerializable;
import vswe.stevesfactory.library.gui.Texture;
import vswe.stevesfactory.library.gui.debug.ITextReceiver;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.widget.mixin.LeafWidgetMixin;

public class Icon extends AbstractWidget implements LeafWidgetMixin, IStringSerializable {

    private Texture texture;

    public Icon(Texture texture) {
        this.setDimensions(texture.getPortionWidth(), texture.getPortionHeight());
        this.texture = texture;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        RenderEventDispatcher.onPreRender(this, mouseX, mouseY);
        if (isEnabled()) {
            texture.render(getAbsoluteX(), getAbsoluteY(), getAbsoluteXRight(), getAbsoluteYBottom(), getZLevel());
        }
        RenderEventDispatcher.onPostRender(this, mouseX, mouseY);
    }

    public Texture getTexture() {
        return texture;
    }

    public void setTexture(Texture texture) {
        this.texture = texture;
        this.setDimensions(texture.getPortionWidth(), texture.getPortionHeight());
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
