package me.zort.containr.internal;

import me.zort.containr.GUI;
import me.zort.containr.GUIRepository;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedList;

public class GUIListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        HumanEntity entity = e.getWhoClicked();
        if(!(entity instanceof Player)) {
            return;
        }
        Player p = (Player) entity;
        if(GUIRepository.hasOpen(p) && e.getClick().name().contains("SHIFT")) {
            // We don't want to allow players to drop items to gui with shift click.
            e.setCancelled(true);
            return;
        }
        if(e.getClickedInventory() != null) {
            Inventory inv = e.getClickedInventory();
            if(inv.getHolder() != null && inv.getHolder() instanceof GUI) {
                e.setCancelled(true);
                GUI gui = (GUI) inv.getHolder();
                if(gui.isFrozen()) {
                    return;
                }
                if(e.getCursor() != null && !e.getCursor().getType().equals(Material.AIR) && !gui.getNormalItemSlots().contains(e.getSlot())) {
                    // If player has item on cursor and this is not a normal item slot,
                    // he's not allowed to put it here.
                    return;
                } else if(gui.getNormalItemSlots().contains(e.getSlot())) {
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
                gui.invokeElement(p, clickType, item);
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        LinkedList<GUI> history = GUIRepository.PREV_GUIS.get(e.getPlayer().getName());
        GUI gui = GUIRepository.remove(e.getPlayer().getName());
        if(gui != null) {
            gui.handleClose((Player) e.getPlayer(), GUI.CloseReason.BY_PLAYER);
        }
        if(history != null) {
            history.clear();
        }
    }

    @EventHandler
    public void onInventoryCreativeEvent(InventoryCreativeEvent e) {
        onInventoryClick(e);
    }

}
