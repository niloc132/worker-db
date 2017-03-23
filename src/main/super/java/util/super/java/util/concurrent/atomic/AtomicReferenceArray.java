package java.util.concurrent.atomic;

import java.io.Serializable;
import java.lang.UnsupportedOperationException;
import java.util.Arrays;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

public class AtomicReferenceArray<E>
        extends Object
        implements Serializable {
    private final E[] array;

    public AtomicReferenceArray(E[] array) {
        this.array = array;
    }
    public AtomicReferenceArray(int length) {
        this.array = (E[]) new Object[length];
    }

    public E accumulateAndGet(int i, E x, BinaryOperator<E> accumulatorFunction) {
        throw new UnsupportedOperationException();
    }
    public boolean compareAndSet(int i, E expect, E update) {
        throw new UnsupportedOperationException();
    }
    public E get(int i) {
        return array[i];
    }
    public E getAndAccumulate(int i, E x, BinaryOperator<E> accumulatorFunction) {
        throw new UnsupportedOperationException();
    }
    public E getAndSet(int i, E newValue) {
        throw new UnsupportedOperationException();
    }
    public E getAndUpdate(int i, UnaryOperator<E> updateFunction) {
        throw new UnsupportedOperationException();
    }
    public void lazySet(int i, E newValue) {
        throw new UnsupportedOperationException();
    }
    public int length() {
        return array.length;
    }
    public void set(int i, E newValue) {
        array[i] = newValue;
    }
    public String toString() {
        return Arrays.toString(array);
    }
    public E updateAndGet(int i, UnaryOperator<E> updateFunction) {
        throw new UnsupportedOperationException();
    }
    public boolean weakCompareAndSet(int i, E expect, E update) {
        throw new UnsupportedOperationException();
    }
}