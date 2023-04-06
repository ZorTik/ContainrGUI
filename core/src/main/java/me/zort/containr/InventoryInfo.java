package me.zort.containr;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.inventory.Inventory;

@RequiredArgsConstructor
@Getter
public class InventoryInfo {

    private final Inventory inventory;
    private final String title;

}
