package me.zort.containr;

import lombok.Getter;
import lombok.Setter;
import me.zort.containr.util.QuadConsumer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Function;

public abstract class BuyableElement extends Element {

    @Setter
    @Getter
    private int price;
    private final boolean canOwnMulti;

    public BuyableElement(int price) {
        this(price, false);
    }

    public BuyableElement(int price, boolean canOwnMulti) {
        this.price = price;
        this.canOwnMulti = canOwnMulti;
    }

    public abstract ItemStack item(Player player, boolean owns, int price);
    public abstract Function<Player, Integer> balance();
    public abstract Function<Player, Boolean> owns();

    public abstract BiConsumer<Player, Integer> buy();
    public BiConsumer<Player, Integer> sell() {
        return (player, price) -> {};
    }

    @NotNull
    @Override
    public QuadConsumer<GUI, Container, Player, ClickType> action() {
        return (o1, o2, o3, o4) -> {
            if(owns().apply(o3) && !canOwnMulti) {
                sell().accept(o3, price);
            } else if(balance().apply(o3) >= price) {
                buy().accept(o3, price);
            }
        };
    }

    @Nullable
    @Override
    public ItemStack item(Player player) {
        return item(player, owns().apply(player), price);
    }
}
