package me.zort.containr.builder;

import me.zort.containr.Element;

public interface ElementBuilder<T extends Element> {

    T build();

}
