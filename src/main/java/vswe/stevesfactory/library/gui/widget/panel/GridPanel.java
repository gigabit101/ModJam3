package vswe.stevesfactory.library.gui.widget.panel;

import com.google.common.collect.ImmutableList;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.widget.AbstractContainer;
import vswe.stevesfactory.library.gui.widget.IWidget;

import java.util.Collection;

// TODO
public class GridPanel<T extends IWidget> extends AbstractContainer<T> {

    @Override
    public Collection<T> getChildren() {
        return ImmutableList.of();
    }

    @Override
    public void reflow() {
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        RenderEventDispatcher.onPreRender(this, mouseX, mouseY);
        renderChildren(mouseX, mouseY, partialTicks);
        RenderEventDispatcher.onPostRender(this, mouseX, mouseY);
    }
}
