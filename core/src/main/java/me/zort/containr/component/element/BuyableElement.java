package me.zort.containr.component.element;

import lombok.Getter;
import lombok.Setter;
import me.zort.containr.Container;
import me.zort.containr.ContextClickInfo;
import me.zort.containr.Element;
import me.zort.containr.GUI;
import me.zort.containr.internal.util.QuadConsumer;
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

    @Override
    public void click(ContextClickInfo info) {
        Player o3 = info.getPlayer();
        if(owns().apply(o3) && !canOwnMulti) {
            sell().accept(o3, price);
        } else if(balance().apply(o3) >= price) {
            buy().accept(o3, price);
        }
    }

    @Nullable
    @Override
    public ItemStack item(Player player) {
        return item(player, owns().apply(player), price);
    }
}
