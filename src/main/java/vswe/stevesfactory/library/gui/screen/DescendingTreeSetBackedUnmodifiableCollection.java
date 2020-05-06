package vswe.stevesfactory.library.gui.screen;

import mcp.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
final class DescendingTreeSetBackedUnmodifiableCollection<E> extends AbstractCollection<E> {

    private final TreeSet<E> s;

    public DescendingTreeSetBackedUnmodifiableCollection(TreeSet<E> s) {
        this.s = s;
    }

    public int size() {
        return this.s.size();
    }

    public boolean isEmpty() {
        return this.s.isEmpty();
    }

    public boolean contains(Object var1) {
        return this.s.contains(var1);
    }

    public Object[] toArray() {
        return this.s.toArray();
    }

    public <T> T[] toArray(T[] a) {
        return this.s.toArray(a);
    }

    public String toString() {
        return this.s.toString();
    }

    @Override
    public Iterator<E> iterator() {
        return s.descendingIterator();
    }

    public boolean add(E e) {
        throw new UnsupportedOperationException();
    }

    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    public boolean containsAll(Collection<?> c) {
        return this.s.containsAll(c);
    }

    public boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    public void clear() {
        throw new UnsupportedOperationException();
    }

    public void forEach(Consumer<? super E> c) {
        this.s.forEach(c);
    }

    public boolean removeIf(Predicate<? super E> p) {
        throw new UnsupportedOperationException();
    }

    public Spliterator<E> spliterator() {
        return this.s.spliterator();
    }

    public Stream<E> stream() {
        return this.s.stream();
    }

    public Stream<E> parallelStream() {
        return this.s.parallelStream();
    }
}