package vswe.stevesfactory.library.gui.window;

import com.google.common.collect.ImmutableList;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.widget.IWidget;
import vswe.stevesfactory.library.gui.widget.navigation.DockedWindow;
import vswe.stevesfactory.library.gui.widget.panel.HorizontalList;

import java.util.List;

public class DockingBar extends AbstractWindow {

    private final HorizontalList<DockedWindow> dockedWindows;
    private final List<IWidget> children;

    public DockingBar(int width, int height) {
        this.setBorder(width, height);
        this.dockedWindows = new HorizontalList<DockedWindow>(getContentWidth(), getContentHeight()) {
            @Override
            public int getBarHeight() {
                return 3;
            }
        };
        this.dockedWindows.attachWindow(this);
        this.children = ImmutableList.of(dockedWindows);
    }

    public void addDockedWindow(DockedWindow item) {
        int height = dockedWindows.getHeight() - dockedWindows.getBarHeight();
        item.setHeight(height);
        dockedWindows.addChildren(item);
        dockedWindows.reflow();
    }

    @Override
    public int getBorderSize() {
        return 2;
    }

    @Override
    public List<? extends IWidget> getChildren() {
        return children;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        RenderEventDispatcher.onPreRender(this, mouseX, mouseY);
        renderFlatStyleBackground();
        renderChildren(mouseX, mouseY, partialTicks);
        RenderEventDispatcher.onPostRender(this, mouseX, mouseY);
    }
}
