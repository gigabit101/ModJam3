package vswe.stevesfactory.library.gui.widget.mixin;

import vswe.stevesfactory.library.gui.widget.IContainer;
import vswe.stevesfactory.library.gui.widget.IWidget;

public interface RelocatableContainerMixin<T extends IWidget> extends IContainer<T> {

    default void notifyChildrenForPositionChange() {
        // Prevent NPE when containers setting coordinates before child widgets get initialized
        if (getChildren() != null) {
            for (T child : getChildren()) {
                child.onParentPositionChanged();
            }
        }
    }

    @Override
    default void onParentPositionChanged() {
        notifyChildrenForPositionChange();
    }

    @Override
    default void setLocation(int x, int y) {
        IContainer.super.setLocation(x, y);
        notifyChildrenForPositionChange();
    }

    @Override
    default void setX(int x) {
        IContainer.super.setX(x);
        notifyChildrenForPositionChange();
    }

    @Override
    default void setY(int y) {
        IContainer.super.setY(y);
        notifyChildrenForPositionChange();
    }
}
