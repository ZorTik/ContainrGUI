package me.zort.containr.examples;

import me.zort.containr.GUI;
import me.zort.containr.SimpleElementBuilder;
import me.zort.containr.component.element.ItemElement;
import me.zort.containr.internal.util.Items;
import me.zort.containr.PagedContainer;
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

    public void exampleQueue() {
        String[] pattern = {
                "#########",
                "###X#X###",
                "#X#####X#",
                "###X#X###",
                "#########",
        };
        GUI.ofPattern(pattern)
                .andTitle("Ornament with elements.")
                // Append elements to the symbol queue.
                // Indexing from top left corner.
                .addQueue("X",
                        ItemElement.on(Material.CHEST, (short) 0, "§aCrate 1"),
                        ItemElement.on(Material.CHEST, (short) 0, "§aCrate 2"),
                        ItemElement.on(Material.CHEST, (short) 0, "§aCrate 3")
                )
                .andMark("#", Items.blank(Material.GLASS))
                .build();
    }

}
