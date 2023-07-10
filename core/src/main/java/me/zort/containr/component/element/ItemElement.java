package me.zort.containr.component.element;

import com.google.common.collect.Maps;
import lombok.Data;
import lombok.EqualsAndHashCode;
import me.zort.containr.Container;
import me.zort.containr.Element;
import me.zort.containr.GUI;
import me.zort.containr.internal.util.Items;
import me.zort.containr.internal.util.QuadConsumer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Deprecated
@EqualsAndHashCode(callSuper = true)
@Data
public class ItemElement extends Element {

    @Contract("_ -> new")
    @NotNull
    public static ItemElement on(Material material) {
        return on(material, (short) 0, "");
    }

    @Contract("_, _ -> new")
    @NotNull
    public static ItemElement on(Material material, short data) {
        return on(material, data, "");
    }

    @Contract("_, _, _ -> new")
    @NotNull
    public static ItemElement on(Material material, short data, String title) {
        return on(material, data, title, new String[0]);
    }

    @Contract("_, _, _, _ -> new")
    @NotNull
    public static ItemElement on(Material material, short data, String title, String[] lore) {
        return new ItemElement(material, data, title, lore);
    }

    public static ItemElement on(ItemStack item) {
        return new ItemElement(item);
    }

    private ItemStack item;

    public ItemElement(Material material) {
        this(material, (short) 0, "", new String[0]);
    }

    public ItemElement(Material material, short data) {
        this(material, data, "", new String[0]);
    }

    public ItemElement(Material material, short data, String title) {
        this(material, data, title, new String[0]);
    }

    public ItemElement(Material material, short data, String title, String[] lore) {
        this(Items.create(material, 1, data, Maps.newHashMap(), title, lore));
    }

    public ItemElement(ItemStack item) {
        this.item = item;
    }

    @Nullable
    @Override
    public ItemStack item(Player player) {
        return item;
    }

}
