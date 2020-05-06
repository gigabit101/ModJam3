package vswe.stevesfactory.library.collections;

import java.util.function.Supplier;

public class RegularReferenceList<E> extends ReferenceList<E> {

    private final Supplier<E>[] suppliers;

    @SafeVarargs
    public RegularReferenceList(Supplier<E>... suppliers) {
        this.suppliers = suppliers;
    }

    @Override
    public E get(int index) {
        return suppliers[index].get();
    }

    @Override
    public int size() {
        return suppliers.length;
    }
}
