package vswe.stevesfactory.library.gui.window;

import com.google.common.collect.ImmutableList;
import vswe.stevesfactory.library.gui.*;
import vswe.stevesfactory.library.gui.debug.ITextReceiver;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.layout.FlowLayout;
import vswe.stevesfactory.library.gui.screen.WidgetScreen;
import vswe.stevesfactory.library.gui.widget.IWidget;
import vswe.stevesfactory.library.gui.widget.navigation.DockedWindow;
import vswe.stevesfactory.library.gui.widget.navigation.NavigationBar;
import vswe.stevesfactory.library.gui.widget.panel.Panel;

import javax.annotation.Nullable;
import java.util.List;

public abstract class AbstractDockableWindow<T extends IWidget> extends AbstractWindow implements IPopupWindow {

    private final DockingBar dockingBar;

    private final NavigationBar navigationBar;
    private final Panel<T> contentBox;
    private final List<IWidget> children;

    private boolean alive = true;
    private int order;

    public AbstractDockableWindow(@Nullable DockingBar dockingBar, int width, int height) {
        this.dockingBar = dockingBar;
        this.navigationBar = NavigationBar.standard(this);
        this.contentBox = new Panel<>();
        this.contentBox.attachWindow(this);
        this.children = ImmutableList.of(navigationBar, contentBox);

        setContents(width, height);
        FlowLayout.vertical(children, 0, 0, 0);
        populateContentBox();
        onInitialized();
    }

    protected void onInitialized() {
    }

    protected abstract void populateContentBox();

    @Override
    protected void onResize() {
        navigationBar.expandHorizontally();
        contentBox.expandHorizontally();
        contentBox.setHeight(getContentHeight() - navigationBar.getHeight());
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        RenderEventDispatcher.onPreRender(this, mouseX, mouseY);
        renderVanillaStyleBackground();
        renderChildren(mouseX, mouseY, partialTicks);
        RenderEventDispatcher.onPostRender(this, mouseX, mouseY);
    }

    public void maximize() {
        // No support by default
    }

    public void minimize() {
        DockedWindow item = new DockedWindow(this);
        dockingBar.addDockedWindow(item);
        discard();
    }

    public void restore() {
        alive = true;
        WidgetScreen.assertActive().addPopupWindow(this);
    }

    public Texture getIcon() {
        return Texture.NONE;
    }

    public String getTitle() {
        return "test";
    }

    public NavigationBar getNavigationBar() {
        return navigationBar;
    }

    public Panel<T> getContentBox() {
        return contentBox;
    }

    @Override
    public List<? extends IWidget> getChildren() {
        return children;
    }

    @Override
    public int getBorderSize() {
        return 4;
    }

    @Override
    public float getZLevel() {
        return Render2D.POPUP_WINDOW_Z;
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public boolean shouldDiscard() {
        return !alive;
    }

    public void discard() {
        alive = false;
    }

    @Override
    public void provideInformation(ITextReceiver receiver) {
        super.provideInformation(receiver);
        receiver.line("Order=" + order);
    }
}
