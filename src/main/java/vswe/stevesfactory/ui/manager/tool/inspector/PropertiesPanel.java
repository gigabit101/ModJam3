package vswe.stevesfactory.ui.manager.tool.inspector;

import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.widget.AbstractContainer;
import vswe.stevesfactory.library.gui.widget.box.LinearList;
import vswe.stevesfactory.ui.manager.editor.FlowComponent;
import vswe.stevesfactory.ui.manager.editor.Menu;

import java.util.ArrayList;
import java.util.List;

public class PropertiesPanel extends AbstractContainer<LinearList<Menu<?>>> {

    private List<LinearList<Menu<?>>> child = new ArrayList<>();

    public PropertiesPanel() {
        super(0, 0, 120, 0);
    }

    @Override
    public void render(int mouseX, int mouseY, float particleTicks) {
        RenderEventDispatcher.onPreRender(this, mouseX, mouseY);
        super.render(mouseX, mouseY, particleTicks);
        RenderEventDispatcher.onPostRender(this, mouseX, mouseY);
    }

    @Override
    public List<LinearList<Menu<?>>> getChildren() {
        return child;
    }

    @Override
    public void reflow() {
    }

    public void openFlowComponent(FlowComponent<?> target) {
        @SuppressWarnings({"rawtypes", "unchecked"}) LinearList<Menu<?>> menusBox = (LinearList) target.getMenusBox();
        // Hacky way to maintain the list to hold one item exactly
        child.clear();
        child.add(menusBox);
        menusBox.setParentWidget(this);
        menusBox.setDimensions(this.getDimensions());
    }
}
