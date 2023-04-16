package me.zort.containr;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;

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

    /**
     * Rebuilds the GUI.
     *
     * @deprecated Use {@link #rebuild(Player)} instead.
     */
    @ApiStatus.ScheduledForRemoval
    @Deprecated
    default void rebuild() {}

    /**
     * Rebuilds the GUI.
     *
     * @param player The player for whom the GUI is being rebuilt.
     */
    default void rebuild(Player player) {}

}
