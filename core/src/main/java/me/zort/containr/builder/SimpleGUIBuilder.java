package me.zort.containr.builder;

import me.zort.containr.GUI;
import me.zort.containr.component.gui.AnimatedGUI;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class SimpleGUIBuilder implements GUIBuilder<GUI> {
    private BiConsumer<GUI, Player> build = (player, gui) -> {};
    private String title = "";
    private int rows = 6;

    public SimpleGUIBuilder title(final String title) {
        this.title = title;
        return this;
    }

    public SimpleGUIBuilder rows(final int rows) {
        this.rows = rows;
        return this;
    }

    public SimpleGUIBuilder prepare(final Consumer<GUI> build) {
        this.build = (gui, player) -> build.accept(gui);
        return this;
    }

    public SimpleGUIBuilder prepare(final BiConsumer<GUI, Player> build) {
        this.build = build;
        return this;
    }

    @Override
    public GUI build() {
        return new GUI(title, rows) {
            @Override
            public void build(Player player) {
                build.accept(this, player);
            }
        };
    }

    public AnimatedGUI build(final int period, final TimeUnit unit) {
        return new AnimatedGUI(title, rows, period, unit) {
            @Override
            public void build(Player player) {
                build.accept(this, player);
            }
        };
    }
}
