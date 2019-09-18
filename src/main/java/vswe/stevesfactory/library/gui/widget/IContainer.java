package vswe.stevesfactory.library.gui.widget;

import java.util.Collection;
import java.util.Iterator;

public interface IContainer<T extends IWidget> extends IWidget {

    Collection<T> getChildren();

    void reflow();

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
