package vswe.stevesfactory.library.gui.screen;

import net.minecraft.client.gui.screen.Screen;
import vswe.stevesfactory.library.gui.widget.IWidget;
import vswe.stevesfactory.library.gui.window.IWindow;
import vswe.stevesfactory.library.gui.debug.Inspections;

class WidgetTreeInspections extends Inspections {

    private Object renderedComponent;

    public void startCycle() {
        renderedComponent = null;
    }

    public void endCycle() {
        if (renderedComponent instanceof IWidget) {
            IWidget widget = (IWidget) renderedComponent;
            renderBox(widget);
            if (Screen.hasControlDown()) {
                renderOverlayInfo(widget);
            }
        } else if (renderedComponent instanceof IWindow) {
            IWindow window = (IWindow) renderedComponent;
            renderBox(window);
            if (Screen.hasControlDown()) {
                renderOverlayInfo(window);
            }
        }
    }

    @Override
    public boolean shouldRender(IWidget target, int mx, int my) {
        renderedComponent = target;
        // We don't want to draw the component at all (not in this place, but after all components has been rendered in this frame)
        return false;
    }

    @Override
    public boolean shouldRender(IWindow target, int mx, int my) {
        renderedComponent = target;
        // Similar to widget's logic
        return false;
    }

    @Override
    public void onPreRender(IWidget widget, int mx, int my) {
        tryRender(widget, mx, my);
    }

    @Override
    public void onPreRender(IWindow window, int mx, int my) {
        tryRender(window, mx, my);
    }

    @Override
    public void onPostRender(IWidget widget, int mx, int my) {
    }

    @Override
    public void onPostRender(IWindow window, int mx, int my) {
    }
}