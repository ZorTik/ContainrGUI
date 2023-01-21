package me.zort.containr;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

@AllArgsConstructor
@Getter
public class ContextClickInfo {

    private final GUI gui;
    private final Container container;
    private final Element element;
    private final Player player;
    private final ClickType clickType;

}
