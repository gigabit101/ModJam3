package vswe.stevesfactory.library.gui.window;

import vswe.stevesfactory.library.gui.IOrdered;
import vswe.stevesfactory.library.gui.screen.WidgetScreen;

public interface IPopupWindow extends IWindow, IOrdered {

    boolean shouldDiscard();

    default void onAdded(WidgetScreen screen) {
    }
}
