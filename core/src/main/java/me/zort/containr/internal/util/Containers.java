package me.zort.containr.internal.util;

import me.zort.containr.Container;
import me.zort.containr.StaticContainer;
import org.bukkit.inventory.Inventory;

public final class Containers {

    public static Container ofInv(Inventory inventory) {
        int width = 9;
        switch(inventory.getType()) {
            case HOPPER:
                width = 5;
                break;
            case CRAFTING:
            case DROPPER:
            case ANVIL:
                width = 3;
                break;
            case BEACON:
                width = 1;
                break;
        }
        return new StaticContainer(
                new Pair<>(0, 0),
                new Pair<>(width - 1, (inventory.getSize() / width) - 1)
        );
    }

}
