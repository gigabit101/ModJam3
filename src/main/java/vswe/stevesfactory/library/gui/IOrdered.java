package vswe.stevesfactory.library.gui;

import javax.annotation.Nonnull;

public interface IOrdered extends Comparable<IOrdered> {

    int getOrder();

    void setOrder(int order);

    @Override
    default int compareTo(@Nonnull IOrdered that) {
        return this.getOrder() - that.getOrder();
    }
}
