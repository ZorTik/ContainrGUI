package me.zort.containr;

import lombok.Getter;
import me.zort.containr.internal.util.QuadConsumer;
import org.apache.commons.lang.RandomStringUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public abstract class Element {

    private final String id;

    public Element() {
        this.id = RandomStringUtils.randomAlphabetic(8);
    }

    @Deprecated
    @NotNull
    public QuadConsumer<GUI, Container, Player, ClickType> action() {
        return (o1, o2, o3, o4) -> {};
    }

    public void click(ContextClickInfo info) {}

    @Nullable
    public abstract ItemStack item(Player player);

}
