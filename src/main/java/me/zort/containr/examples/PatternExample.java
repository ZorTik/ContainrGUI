package me.zort.containr.examples;

import me.zort.containr.GUI;
import me.zort.containr.PagedContainer;
import me.zort.containr.SimpleElementBuilder;
import me.zort.containr.internal.util.Items;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class PatternExample {

    public void example(Player player) {
        // Load pattern from somewhere.
        String[] pattern = {
                "#########",
                "#XXXXXXX#",
                "#XXXXXXX#",
                "#XXXXXXX#",
                "#########",
        };
        GUI gui = GUI.ofPattern(pattern)
                .andTitle("Pattern inner container.")
                .andMark("X", PagedContainer.class, c -> {
                    c.clear();
                    c.appendElement(SimpleElementBuilder.b()
                            .item(Items.blank(Material.GRASS))
                            .action((g, c1, p, ct) -> {
                                p.sendMessage("This is grass element inside pattern container.");
                            })
                            .build());
                })
                .andMark("#", Items.blank(Material.GLASS))
                .build();
        gui.open(player);
    }

}
