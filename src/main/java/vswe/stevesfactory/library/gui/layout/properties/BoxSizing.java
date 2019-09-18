package vswe.stevesfactory.library.gui.layout.properties;

import vswe.stevesfactory.library.gui.widget.IWidget;
import vswe.stevesfactory.library.gui.layout.ILayoutDataProvider;

public enum BoxSizing {

    BORDER_BOX(true),
    CONTENT_BOX(true),
    PHANTOM(false),
    ;

    public final boolean flow;

    BoxSizing(boolean flow) {
        this.flow = flow;
    }

    public static boolean shouldIncludeWidget(IWidget widget) {
        if (widget instanceof ILayoutDataProvider) {
            return ((ILayoutDataProvider) widget).getBoxSizing().flow;
        }
        return false;
    }
}
