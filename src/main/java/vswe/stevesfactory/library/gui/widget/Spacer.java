package vswe.stevesfactory.library.gui.widget;

import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.widget.mixin.LeafWidgetMixin;

public class Spacer extends AbstractWidget implements LeafWidgetMixin {

    public Spacer(int width, int height) {
        this.setDimensions(width, height);
    }

    public Spacer(int x, int y, int width, int height) {
        this.setLocation(x, y);
        this.setDimensions(width, height);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        RenderEventDispatcher.onPreRender(this, mouseX, mouseY);
        RenderEventDispatcher.onPostRender(this, mouseX, mouseY);
    }
}
