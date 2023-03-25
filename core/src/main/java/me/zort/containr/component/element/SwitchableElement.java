package me.zort.containr.component.element;

import com.google.common.collect.Lists;
import lombok.Getter;
import me.zort.containr.Container;
import me.zort.containr.ContextClickInfo;
import me.zort.containr.Element;
import me.zort.containr.GUI;
import me.zort.containr.internal.util.CyclicArrayList;
import me.zort.containr.internal.util.QuadConsumer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class SwitchableElement<T> extends Element {

    @Getter
    private final CyclicArrayList<T> options;

    public SwitchableElement() {
        this(Lists.newArrayList());
    }

    public SwitchableElement(List<T> options) {
        this.options = new CyclicArrayList<>(options);
    }

    public abstract QuadConsumer<GUI, Container, Player, ClickType> action(@Nullable T newOption);
    public abstract ItemStack option(@Nullable T option);

    @Override
    public void click(ContextClickInfo info) {
        next();
        action(options.getCurrent().orElse(null)).accept(
                info.getGui(),
                info.getContainer(),
                info.getPlayer(),
                info.getClickType());
    }

    public void next() {
        options.getNext();
    }

    public boolean setCurrent(T obj) {
        for(int i = 0; i < options.size(); i++) {
            T objAtIIndex = options.get(i);
            if(obj.equals(objAtIIndex)) {
                options.setCurrentPos(i);
                return true;
            }
        }
        return false;
    }

    @Nullable
    @Override
    public ItemStack item(Player player) {
        return option(options.getCurrent().orElse(null));
    }
}
