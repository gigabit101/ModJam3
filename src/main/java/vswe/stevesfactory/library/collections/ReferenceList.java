package vswe.stevesfactory.library.collections;

import java.util.AbstractList;
import java.util.function.Supplier;

public abstract class ReferenceList<E> extends AbstractList<E> {

    public static <E> ReferenceList<E> of(Supplier<E> supplier) {
        return new SingletonReferenceList<>(supplier);
    }

    public static <E> ReferenceList<E> of(Supplier<E> elm1, Supplier<E> elm2) {
        return new RegularReferenceList<>(elm1, elm2);
    }

    public static <E> ReferenceList<E> of(Supplier<E>... elements) {
        return new RegularReferenceList<>(elements);
    }
}
