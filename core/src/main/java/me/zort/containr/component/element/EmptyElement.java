package me.zort.containr.component.element;

import me.zort.containr.Element;
import org.bukkit.entity.Player;
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

    @Nullable
    @Override
    public ItemStack item(Player player) {
        return null;
    }

}
