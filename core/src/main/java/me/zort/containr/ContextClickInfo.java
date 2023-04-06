package me.zort.containr;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

@AllArgsConstructor
@Getter
public final class ContextClickInfo {

    private final GUI gui;
    private final Container container;
    private final Element element;
    private final Player player;
    private final ClickType clickType;
    private final ItemStack cursor;

    public void close() {
        gui.close(player);
    }

}
