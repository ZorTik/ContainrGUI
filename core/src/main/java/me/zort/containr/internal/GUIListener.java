package me.zort.containr.internal;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.zort.containr.Containr;
import me.zort.containr.GUI;
import me.zort.containr.GUIRepository;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.LinkedList;
import java.util.Optional;

@RequiredArgsConstructor
@Getter
public class GUIListener implements Listener {

    private final Plugin plugin;

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        HumanEntity entity = e.getWhoClicked();
        if(!(entity instanceof Player)) {
            return;
        }
        Player p = (Player) entity;
        if(e.getClickedInventory() != null) {
            Inventory inv = e.getClickedInventory();
            if(inv.getHolder() != null && inv.getHolder() instanceof GUI) {
                e.setCancelled(true);
                GUI gui = (GUI) inv.getHolder();
                if(gui.isFrozen()) {
                    return;
                }
                if(gui.getNormalItemSlots().contains(e.getSlot())) {
                    e.setCancelled(false);
                    for(GUI.NormalEditHandler handler : gui.getNormalEditHandlers()) {
                        try {
                            handler.onEdit(p, e.getSlot());
                        } catch(Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                    return;
                }
                ClickType clickType = e.getClick();
                ItemStack item = e.getCurrentItem();
                gui.invokeElement(p, clickType, item, e.getCursor());
            } else if(GUIRepository.hasOpen(p) && e.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)) {
                // We don't want to allow players to drop items to gui with shift click.
                e.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        LinkedList<GUI> history = GUIRepository.PREV_GUIS.get(e.getPlayer().getName());
        GUI gui = null;
        if(e.getInventory() == Optional.ofNullable(GUIRepository.OPENED_GUIS.get(e.getPlayer().getName()))
                .map(GUI::getInventory).orElse(null)) {
            gui = GUIRepository.remove(e.getPlayer().getName());
        }
        if(gui != null) {
            gui.close((Player) e.getPlayer(), GUI.CloseReason.BY_PLAYER);
        }
        if(history != null) {
            history.clear();
        }
    }

    @EventHandler
    public void onInventoryCreativeEvent(InventoryCreativeEvent e) {
        onInventoryClick(e);
    }

    @EventHandler
    public void onDisable(PluginDisableEvent e) {
        Containr.unregisterSignal(e.getPlugin());
    }

}
