package me.zort.containr.internal.util;

@FunctionalInterface
public interface QuadConsumer<T, U, D, A> {

    void accept(T o1, U o2, D o3, A o4);

}
