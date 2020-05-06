package vswe.stevesfactory.library.gui.widget.panel;

import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.widget.AbstractContainer;
import vswe.stevesfactory.library.gui.widget.IWidget;

import java.util.*;

public class PositionalGridPanel<T extends IWidget> extends AbstractContainer<T> {

    private List<T> children = new ArrayList<>();
    private int cellSize;

    public PositionalGridPanel(int cellSize) {
        this.cellSize = cellSize;
    }

    @Override
    public Collection<T> getChildren() {
        return children;
    }

    @Override
    public PositionalGridPanel<T> addChildren(T widget) {
        children.add(widget);
        widget.attach(this);
        return this;
    }

    public PositionalGridPanel<T> addElement(T widget, int gridX, int gridY) {
        children.add(widget);
        widget.attach(this);
        widget.setLocation(gridX * cellSize, gridY * cellSize);
        return this;
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
