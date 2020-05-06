package vswe.stevesfactory.library.collections;

import java.util.function.Supplier;

class SingletonReferenceList<E> extends ReferenceList<E> {

    private Supplier<E> supplier;

    public SingletonReferenceList(Supplier<E> supplier) {
        this.supplier = supplier;
    }

    @Override
    public E get(int index) {
        if (index != 0) {
            throw new ArrayIndexOutOfBoundsException();
        }
        return supplier.get();
    }

    @Override
    public int size() {
        return 1;
    }
}
