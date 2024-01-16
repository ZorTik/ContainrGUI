package me.zort.containr;

import org.bukkit.entity.Player;

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
 * @deprecated Deprecated in favor of {@link Container#refresh(Player)}
 */
@Deprecated
public interface Rebuildable {

    /**
     * Rebuilds the GUI.
     *
     * @deprecated Use {@link #rebuild(Player)} instead.
     */
    default void rebuild() {}

    /**
     * Rebuilds the GUI.
     *
     * @param player The player for whom the GUI is being rebuilt.
     */
    default void rebuild(Player player) {}

}
