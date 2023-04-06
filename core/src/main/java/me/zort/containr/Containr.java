package me.zort.containr;

import me.zort.containr.internal.GUIListener;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import static org.bukkit.Bukkit.getServer;

public final class Containr {

    private static GUIListener listener;
    private static Plugin current;

    static {
        listener = null;
        current = null;
    }

    private Containr() {}

    public static void init(@NotNull Plugin plugin) {
        if(current != null) {
            if(!current.isEnabled() && listener != null) {
                HandlerList.unregisterAll(listener);
                listener = null;
            } else if(current.isEnabled()) {
                return;
            }
        }
        getServer().getPluginManager().registerEvents(listener = new GUIListener(), plugin);
        current = plugin;
    }

}
