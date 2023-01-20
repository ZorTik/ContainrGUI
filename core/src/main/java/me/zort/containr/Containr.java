package me.zort.containr;

import me.zort.containr.internal.GUIListener;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import static org.bukkit.Bukkit.getServer;

public class Containr {

    private static GUIListener LISTENER;
    private static Plugin CURRENT;

    static {
        LISTENER = null;
        CURRENT = null;
    }

    public static void init(@NotNull Plugin plugin) {
        if(CURRENT != null) {
            if(!CURRENT.isEnabled() && LISTENER != null) {
                HandlerList.unregisterAll(LISTENER);
                LISTENER = null;
            } else if(CURRENT.isEnabled()) {
                return;
            }
        }
        getServer().getPluginManager().registerEvents(LISTENER = new GUIListener(), plugin);
        CURRENT = plugin;
    }

}
