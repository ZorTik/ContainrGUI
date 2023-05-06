package me.zort.containr.examples;

import me.zort.containr.Component;
import me.zort.containr.Element;
import me.zort.containr.GUI;
import me.zort.containr.PagedContainer;
import me.zort.containr.internal.util.ItemBuilder;
import me.zort.containr.internal.util.Items;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ServerSelectorExample {

    public void example(Player p) {
        List<String> names = new ArrayList<>();

        GUI guiInstance = Component.gui()
                .title("My cute GUI") // The GUI title
                .rows(6) // Number of GUI rows
                .prepare((gui, player) -> { // Code that is executed when the GUI is being built
                    // TODO: GUI init code
                    gui.setElement(4, Component.element()
                            .click(info -> {
                                Player whoClicked = info.getPlayer();
                                whoClicked.sendMessage("You closed the GUI!");
                                whoClicked.closeInventory();
                            })
                            .item(Items.create(Material.REDSTONE_BLOCK, "§c§lClose GUI"))
                            .build());
                    gui.setContainer(9, Component.staticContainer()
                            .size(6, 1)
                            .init(container -> {
                                Element spacerElement = Component.element(Items.blank(Material.BLACK_STAINED_GLASS_PANE)).build();
                                container.fillElement(spacerElement);
                            })
                            .build());

                    PagedContainer serversContainer = Component.pagedContainer()
                            .size(6, 2)
                            .init(container -> {
                                names.forEach(serverName -> {
                                    container.appendElement(Component.element()
                                            .click(info -> {
                                                // TODO: Join player to a server
                                            })
                                            .item(new ItemBuilder()
                                                    .withType(Material.PAPER)
                                                    .withName("§a§l" + serverName)
                                                    .withLore()
                                                    .blank()
                                                    .line("§7Click to join the server!")
                                                    .and().build())
                                            .build());
                                });
                            })
                            .build();
                    gui.setContainer(18, serversContainer);
                    gui.setElement(50, Component.element()
                            .click(info -> {
                                serversContainer.nextPage();
                                gui.update(info.getPlayer());
                            })
                            .item(Items.create(Material.ARROW, "§a§lNext page"))
                            .build());
                })
                .build();
        guiInstance.open(p);
    }

}
