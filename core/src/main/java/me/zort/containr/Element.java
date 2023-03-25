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
public abstract class Element implements Component {

    private final String id;

    public Element() {
        this.id = RandomStringUtils.randomAlphabetic(8);
    }

    public void click(ContextClickInfo info) {}

    @Nullable
    public abstract ItemStack item(Player player);

}
