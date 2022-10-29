package me.zort.containr;

import me.zort.containr.internal.util.QuadConsumer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class SimpleElementBuilder {

    public static SimpleElementBuilder b() {
        return new SimpleElementBuilder();
    }

    private Supplier<ItemStack> itemSupplier = null;
    private QuadConsumer<GUI, Container, Player, ClickType> action = (o1, o2, o3, o4) -> {};
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
        this.action = action;
        return this;
    }

    public SimpleElementBuilder click(Consumer<ContextClickInfo> click) {
        this.clickConsumer = click;
        return this;
    }

    public Element build() {
        return new Element() {
            @NotNull
            @Override
            public QuadConsumer<GUI, Container, Player, ClickType> action() {
                return action;
            }

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
