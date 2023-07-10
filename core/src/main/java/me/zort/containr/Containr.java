package me.zort.containr;

import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static org.bukkit.Bukkit.getServer;

public final class Containr {

    private static final List<Plugin> registrations = new ArrayList<>();
    private static GUIListener listener;

    static {
        listener = null;
    }

    private Containr() {}

    public static void init(@NotNull Plugin plugin) {
        if (!registrations.contains(plugin)) registrations.add(plugin);
        if (listener == null)
            getServer().getPluginManager().registerEvents(listener = new GUIListener(plugin), plugin);
    }

    public static void unregisterSignal(@NotNull Plugin plugin) {
        registrations.remove(plugin);

        if (listener != null && listener.getPlugin().equals(plugin)) {
            HandlerList.unregisterAll(listener);
            listener = null;
        }

        if (!registrations.isEmpty()) init(registrations.get(0));
    }

}
