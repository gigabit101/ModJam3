package vswe.stevesfactory.library.gui.widget.button;

import vswe.stevesfactory.library.gui.widget.IWidget;

import java.util.function.IntConsumer;

public interface IButton extends IWidget {

    IntConsumer DUMMY = i -> {
    };

    boolean isHovered();

    boolean isClicked();

    boolean hasClickAction();

    IntConsumer getClickAction();

    void setClickAction(IntConsumer action);
}
