package vswe.stevesfactory.library.gui.widget;

import java.util.Collection;
import java.util.Iterator;

public interface IContainer<T extends IWidget> extends IWidget {

    Collection<T> getChildren();

    void reflow();

    /**
     * Add the given children to the collection returned by {@link #getChildren()}. This should also immediately attach the widget to this
     * container widget.
     *
     * @throws UnsupportedOperationException If this container implementation does not support adding child widgets.
     */
    @SuppressWarnings("UnusedReturnValue")
    IContainer<T> addChildren(T widget);

    @SuppressWarnings("UnusedReturnValue")
    IContainer<T> addChildren(Collection<T> widgets);

    @SuppressWarnings("UnusedReturnValue")
    default IContainer<T> addChildren(Iterable<T> widgets) {
        return addChildren(widgets.iterator());
    }

    @SuppressWarnings("UnusedReturnValue")
    default IContainer<T> addChildren(Iterator<T> widgets) {
        widgets.forEachRemaining(this::addChildren);
        return this;
    }
}
