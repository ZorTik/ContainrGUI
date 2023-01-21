package me.zort.containr;

/**
 * A GUI builder.
 *
 * @param <T> The type of the GUI.
 * @author ZorTik
 */
public interface GUIBuilder<T extends GUI> {

    T build();

}
