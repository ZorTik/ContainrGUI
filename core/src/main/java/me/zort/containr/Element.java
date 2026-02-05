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

    /**
     * Post-process the created ItemStack before it is applied to the GUI.
     *
     * @param player The player for which the item has been created.
     * @param item The created item. Is not the same as the returned from {@link #item(Player)}.
     * @return The modified item.
     */
    public ItemStack postProcessItem(Player player, ItemStack item) {
        return item;
    }
}
