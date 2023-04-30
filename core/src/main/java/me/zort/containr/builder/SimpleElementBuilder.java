package me.zort.containr.builder;

import lombok.AccessLevel;
import lombok.Getter;
import me.zort.containr.*;
import org.bukkit.entity.Player;
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
@Getter(AccessLevel.PROTECTED)
public class SimpleElementBuilder implements ElementBuilder<Element> {

    public static SimpleElementBuilder b() {
        return new SimpleElementBuilder();
    }

    private Supplier<ItemStack> itemSupplier = null;
    private Consumer<ContextClickInfo> clickConsumer = (info) -> {};

    public final SimpleElementBuilder item(@Nullable ItemStack item) {
        return item(() -> item);
    }

    public final SimpleElementBuilder item(@NotNull Supplier<ItemStack> itemSupplier) {
        this.itemSupplier = itemSupplier;
        return this;
    }

    public final SimpleElementBuilder click(@NotNull Consumer<ContextClickInfo> click) {
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
