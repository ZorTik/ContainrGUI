package me.zort.containr;

/**
 * Represents a rebuildable GUI.
 * This interface is determined to be used ONLY by implementing
 * in a GUI class since only GUI class handles it.
 * <pre>
 *     public class MyGUI extends GUI implements Rebuildable {
 *      public void rebuild() {
 *          // ...
 *      }
 *     }
 * </pre>
 *
 * @author ZorTik
 */
public interface Rebuildable {

    void rebuild();

}
