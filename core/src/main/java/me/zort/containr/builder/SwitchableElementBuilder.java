package me.zort.containr.builder;

import com.google.common.collect.Lists;
import me.zort.containr.Container;
import me.zort.containr.ContextClickInfo;
import me.zort.containr.GUI;
import me.zort.containr.component.element.SwitchableElement;
import me.zort.containr.internal.util.QuadConsumer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class SwitchableElementBuilder<T> {

    private BiConsumer<ContextClickInfo, T> action = (i, o) -> {};
    private Function<T, ItemStack> itemFunc = (o) -> null;
    private final List<T> options = new ArrayList<>();

    @SafeVarargs
    public final SwitchableElementBuilder<T> options(T... options) {
        this.options.addAll(Lists.newArrayList(options));
        return this;
    }

    public final SwitchableElementBuilder<T> click(@NotNull BiConsumer<ContextClickInfo, T> action) {
        this.action = this.action.andThen(action);
        return this;
    }

    public final SwitchableElementBuilder<T> item(@Nullable ItemStack item) {
        return item((o) -> item);
    }

    public final SwitchableElementBuilder<T> item(@NotNull Function<T, ItemStack> itemFunc) {
        this.itemFunc = itemFunc;
        return this;
    }

    public SwitchableElement<T> build() {
        return new SwitchableElement<T>(options) {

            @Override
            public QuadConsumer<GUI, Container, Player, ClickType> action(@Nullable T newOption) {
                return (o1, o2, o3, o4) -> {
                    action.accept(new ContextClickInfo(o1, o2, this, o3, o4), newOption);
                };
            }
            @Override
            public ItemStack option(@Nullable T option) {
                return itemFunc.apply(option);
            }
        };
    }
}
