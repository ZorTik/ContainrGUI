package me.zort.containr;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;

public final class GUIRepository {

    public static final ConcurrentMap<String, GUI> OPENED_GUIS = Maps.newConcurrentMap();
    public static final ConcurrentMap<String, LinkedList<GUI>> PREV_GUIS = Maps.newConcurrentMap();

    public static void cleanup() {
        OPENED_GUIS.keySet().removeIf(nick -> Bukkit.getPlayer(nick) == null);
        PREV_GUIS.keySet().removeIf(nick -> Bukkit.getPlayer(nick) == null);
    }

    public static boolean openPrevious(Player p) {
        boolean b = false;
        if(PREV_GUIS.containsKey(p.getName())) {
            LinkedList<GUI> list = PREV_GUIS.get(p.getName());
            if(!list.isEmpty()) {
                GUI gui = list.removeFirst();
                gui.open(p);
                b = true;
            }
        }
        if(!b) {
            p.closeInventory();
        }
        return b;
    }

    public static boolean reopenCurrent(Player p) {
        if(OPENED_GUIS.containsKey(p.getName()) && p.isOnline()) {
            OPENED_GUIS.get(p.getName()).open(p);
            return true;
        }
        return false;
    }

    public static Optional<GUI> getPrevious(Player p) {
        return getPrevious(p.getName());
    }

    public static Optional<GUI> getPrevious(String nick) {
        LinkedList<GUI> list = PREV_GUIS.getOrDefault(nick, null);
        if(list == null || list.isEmpty()) return Optional.empty();
        return Optional.of(list.getFirst());
    }

    public static boolean hasOpen(Player p) {
        try {
            if (!(p.getOpenInventory().getTopInventory().getHolder() instanceof GUI))
                return false;
        } catch(Exception e) {
            e.printStackTrace();
        }

        return hasOpen(p.getName());
    }

    public static boolean hasOpen(String nick) {
        return OPENED_GUIS.containsKey(nick);
    }

    static void add(String nick, GUI gui) {
        Optional<GUI> guiOptional;
        if((guiOptional = getPrevious(nick)).isPresent() && guiOptional.get() == gui) return;
        if(gui instanceof Rebuildable && !(gui instanceof Silent)) {
            if(!PREV_GUIS.containsKey(nick)) {
                PREV_GUIS.put(nick, Lists.newLinkedList());
            }
            PREV_GUIS.get(nick).add(gui);
        }
        OPENED_GUIS.put(nick, gui);

        gui.getContainers(true).forEach(Container::registerSources);
    }

    public static GUI remove(String nick) {
        GUI gui = OPENED_GUIS.remove(nick);
        if(gui != null)
            gui.getContainers(true).forEach(Container::unregisterSources);
        return gui;
    }

}
