package me.zort.containr.factory;

import lombok.RequiredArgsConstructor;
import me.zort.containr.GUI;
import me.zort.containr.InventoryFactory;
import me.zort.containr.InventoryInfo;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

@RequiredArgsConstructor
public class CustomInventoryFactory implements InventoryFactory {

    private final InventoryType type;
    private final String title;

    @Override
    public InventoryInfo createInventory(GUI gui) {
        Inventory inventory = Bukkit.createInventory(gui, type, title);
        return new InventoryInfo(inventory, title);
    }
}
