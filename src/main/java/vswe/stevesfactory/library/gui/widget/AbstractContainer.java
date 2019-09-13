package vswe.stevesfactory.library.gui.widget;

import vswe.stevesfactory.library.gui.*;
import vswe.stevesfactory.library.gui.widget.mixin.ContainerWidgetMixin;
import vswe.stevesfactory.library.gui.widget.mixin.RelocatableContainerMixin;

import java.awt.*;
import java.util.Collection;

public abstract class AbstractContainer<T extends IWidget> extends AbstractWidget implements IContainer<T>, ContainerWidgetMixin<T>, RelocatableContainerMixin<T> {

    public AbstractContainer(IWindow window) {
        super(window);
    }

    public AbstractContainer() {
    }

    public AbstractContainer(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    public AbstractContainer(Point location, Dimension dimensions) {
        super(location, dimensions);
    }

    @Override
    public void setParentWidget(IWidget newParent) {
        super.setParentWidget(newParent);
        ContainerWidgetMixin.super.setParentWidget(newParent);
    }

    @Override
    public void setWindow(IWindow window) {
        super.setWindow(window);
        Collection<T> children = getChildren();
        if (children != null) {
            for (T child : children) {
                // Based on the docs, this will inherit a reference to window from this widget
                child.setParentWidget(this);
            }
        }
    }

    @Override
    public IContainer<T> addChildren(T widget) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IContainer<T> addChildren(Collection<T> widgets) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onParentPositionChanged() {
        super.onParentPositionChanged();
        notifyChildrenForPositionChange();
    }

    @Override
    public void onRelativePositionChanged() {
        super.onRelativePositionChanged();
        notifyChildrenForPositionChange();
    }

    public void adjustMinContent() {
        if (getChildren().isEmpty()) {
            return;
        }

        int rightmost = 0;
        int bottommost = 0;
        for (IWidget child : getChildren()) {
            int right = child.getX() + child.getWidth();
            int bottom = child.getY() + child.getHeight();
            if (right > rightmost) {
                rightmost = right;
            }
            if (bottom > bottommost) {
                bottommost = bottom;
            }
        }
        setDimensions(rightmost, bottommost);
    }

    public void fillWindow() {
        setLocation(0, 0);
        setDimensions(getWindow().getContentDimensions());
    }

    public void attachChildren() {
        for (T child : getChildren()) {
            child.setParentWidget(this);
        }
    }
}
