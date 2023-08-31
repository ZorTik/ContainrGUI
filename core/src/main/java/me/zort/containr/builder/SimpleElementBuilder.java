package me.zort.containr.builder;

import lombok.AccessLevel;
import lombok.Getter;
import me.zort.containr.*;
import me.zort.containr.internal.util.ItemBuilder;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
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

    public static @NotNull SimpleElementBuilder b() {
        return new SimpleElementBuilder();
    }

    public static @NotNull SimpleElementBuilder fromConfig(ConfigurationSection section) {
        return fromConfig(section, (item, sec) -> {});
    }

    public static @NotNull SimpleElementBuilder fromConfig(
            ConfigurationSection section,
            BiConsumer<ItemBuilder, ConfigurationSection> modifier
    ) {
        ItemBuilder item = ItemBuilder.fromConfig(section);
        return b().item(() -> {
            modifier.accept(item, section);
            return item.build();
        });
    }

    private Function<Player, ItemStack> itemFunction = null;
    private Consumer<ContextClickInfo> clickConsumer = (info) -> {};

    public final @NotNull SimpleElementBuilder item(@Nullable ItemStack item) {
        return item(() -> item);
    }

    public final @NotNull SimpleElementBuilder item(@NotNull Supplier<ItemStack> itemSupplier) {
        return item(player -> itemSupplier.get());
    }

    public final @NotNull SimpleElementBuilder item(@NotNull Function<Player, ItemStack> itemFunction) {
        this.itemFunction = itemFunction;
        return this;
    }

    public final @NotNull SimpleElementBuilder click(Runnable runnable) {
        return click(info -> runnable.run());
    }

    public final @NotNull SimpleElementBuilder click(@NotNull Consumer<ContextClickInfo> click) {
        this.clickConsumer = click;
        return this;
    }

    public final @NotNull SimpleElementBuilder addClick(@NotNull Consumer<ContextClickInfo> click) {
        Consumer<ContextClickInfo> old = this.clickConsumer;
        this.clickConsumer = info -> {
            old.accept(info);
            click.accept(info);
        };
        return this;
    }

    public @NotNull Element build() {
        return new Element() {
            @Override
            public void click(ContextClickInfo info) {
                clickConsumer.accept(info);
            }
            @Nullable
            @Override
            public ItemStack item(Player player) {
                return itemFunction.apply(player);
            }
        };
    }

}
