package me.zort.containr.component.element;

import me.zort.containr.Container;
import me.zort.containr.Element;
import me.zort.containr.GUI;
import me.zort.containr.internal.util.QuadConsumer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EmptyElement extends Element {

    @Contract(" -> new")
    @NotNull
    public static EmptyElement create() {
        return new EmptyElement();
    }

    @Override
    @NotNull
    public QuadConsumer<GUI, Container, Player, ClickType> action() {
        return (o1, o2, o3, o4) -> {};
    }

    @Nullable
    @Override
    public ItemStack item(Player player) {
        return null;
    }

}
