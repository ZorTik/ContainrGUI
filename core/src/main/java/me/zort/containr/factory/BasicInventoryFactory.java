package me.zort.containr.factory;

import lombok.RequiredArgsConstructor;
import me.zort.containr.GUI;
import me.zort.containr.InventoryFactory;
import me.zort.containr.InventoryInfo;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

@RequiredArgsConstructor
public class BasicInventoryFactory implements InventoryFactory {

    private final String title;
    private final int rows;

    @Override
    public InventoryInfo createInventory(GUI gui) {
        Inventory inventory = Bukkit.createInventory(gui, rows * 9, title);
        return new InventoryInfo(inventory, title);
    }
}
