package vswe.stevesfactory.ui.manager.tool.inspector;

import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.widget.AbstractContainer;
import vswe.stevesfactory.library.gui.widget.panel.VerticalList;
import vswe.stevesfactory.ui.manager.editor.FlowComponent;
import vswe.stevesfactory.ui.manager.editor.Menu;

import java.util.ArrayList;
import java.util.List;

public class PropertiesPanel extends AbstractContainer<VerticalList<Menu<?>>> {

    private final List<VerticalList<Menu<?>>> child = new ArrayList<>();

    public PropertiesPanel() {
        this.setWidth(120);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        RenderEventDispatcher.onPreRender(this, mouseX, mouseY);
        super.renderChildren(mouseX, mouseY, partialTicks);
        RenderEventDispatcher.onPostRender(this, mouseX, mouseY);
    }

    @Override
    public List<VerticalList<Menu<?>>> getChildren() {
        return child;
    }

    @Override
    public void reflow() {
    }

    public void openFlowComponent(FlowComponent<?> target) {
        @SuppressWarnings({"rawtypes", "unchecked"}) VerticalList<Menu<?>> menusBox = (VerticalList) target.getMenusBox();
        // Hacky way to maintain the list to hold one item exactly
        child.clear();
        child.add(menusBox);
        menusBox.attach(this);
        menusBox.setDimensions(this.getDimensions());
    }
}
