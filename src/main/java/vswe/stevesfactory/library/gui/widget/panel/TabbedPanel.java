package vswe.stevesfactory.library.gui.widget.panel;

import net.minecraft.util.IStringSerializable;
import vswe.stevesfactory.library.collections.ReferenceList;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.widget.AbstractContainer;
import vswe.stevesfactory.library.gui.widget.IWidget;
import vswe.stevesfactory.utils.Utils;

import javax.annotation.Nullable;
import java.util.*;

public class TabbedPanel<P extends IWidget> extends AbstractContainer<IWidget> {

    private TabHorizontalList tabs;
    private P activePanel;
    private final Collection<IWidget> children;

    private List<P> panels = new ArrayList<>();

    public TabbedPanel() {
        tabs = new TabHorizontalList(0, 16);
        children = ReferenceList.of(this::getTabsPanel, this::getActivePanelRaw);
    }

    public TabbedPanel<P> addPanel(P widget) {
        String name = widget instanceof IStringSerializable ? ((IStringSerializable) widget).getName() : "";
        panels.add(widget);
        tabs.addChildren(new Tab(name));
        widget.attach(this);
        return this;
    }

    public TabbedPanel<P> addPanel(Collection<P> widgets) {
        for (P widget : widgets) {
            String name = widget instanceof IStringSerializable ? ((IStringSerializable) widget).getName() : "";
            tabs.addChildren(new Tab(name));
            panels.add(widget);
            widget.attach(this);
        }
        return this;
    }

    @Override
    public Collection<IWidget> getChildren() {
        return children;
    }

    @Override
    public void onDimensionChanged() {
        reflow();
    }

    @Override
    public void reflow() {
        tabs.expandHorizontally();
        tabs.reflow();
    }

    public HorizontalList<Tab> getTabsPanel() {
        return tabs;
    }

    @Nullable
    private P getActivePanelRaw() {
        return activePanel;
    }

    public P getActivePanel() {
        if (activePanel == null) {
            activePanel = Utils.first(panels);
        }
        return activePanel;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        RenderEventDispatcher.onPreRender(this, mouseX, mouseY);

        RenderEventDispatcher.onPostRender(this, mouseX, mouseY);
    }
}
