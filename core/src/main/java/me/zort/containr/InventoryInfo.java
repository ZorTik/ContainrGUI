package me.zort.containr;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.inventory.Inventory;

@RequiredArgsConstructor
@Getter
public class InventoryInfo {

    private final Inventory inventory;

    /**
     * @deprecated This constructor is deprecated and should not be used. The title parameter is no longer necessary
     * as the inventory's title can be obtained directly from the Inventory instance.
     */
    @Deprecated
    public InventoryInfo(final Inventory inventory, final String title) {
        this.inventory = inventory;
    }
}
