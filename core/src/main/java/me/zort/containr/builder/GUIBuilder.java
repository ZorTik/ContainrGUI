package me.zort.containr.builder;

import me.zort.containr.GUI;

/**
 * A GUI builder.
 *
 * @param <T> The type of the GUI.
 * @author ZorTik
 */
public interface GUIBuilder<T extends GUI> {

    T build();

}
