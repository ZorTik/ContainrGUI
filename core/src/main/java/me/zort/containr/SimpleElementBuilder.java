package me.zort.containr;

import me.zort.containr.internal.util.QuadConsumer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A builder of element that is used for quicker creation
 * of basic elements. This was necessary because very first
 * way of creating elements via inheritance was very slow.
 *
 * @author ZorTik
 */
public class SimpleElementBuilder {

    public static SimpleElementBuilder b() {
        return new SimpleElementBuilder();
    }

    private Supplier<ItemStack> itemSupplier = null;
    private Consumer<ContextClickInfo> clickConsumer = (info) -> {};

    public SimpleElementBuilder item(ItemStack item) {
        return item(() -> item);
    }

    public SimpleElementBuilder item(Supplier<ItemStack> itemSupplier) {
        this.itemSupplier = itemSupplier;
        return this;
    }

    @Deprecated
    public SimpleElementBuilder action(QuadConsumer<GUI, Container, Player, ClickType> action) {
        this.clickConsumer = info -> action.accept(info.getGui(), info.getContainer(), info.getPlayer(), info.getClickType());
        return this;
    }

    public SimpleElementBuilder click(Consumer<ContextClickInfo> click) {
        this.clickConsumer = click;
        return this;
    }

    public Element build() {
        return new Element() {

            @Override
            public void click(ContextClickInfo info) {
                clickConsumer.accept(info);
            }

            @Nullable
            @Override
            public ItemStack item(Player player) {
                return itemSupplier.get();
            }
        };
    }

}
