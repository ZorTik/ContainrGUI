package me.zort.containr.component.element;

import com.google.common.collect.Lists;
import lombok.Getter;
import me.zort.containr.Element;
import me.zort.containr.internal.util.CyclicArrayList;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public abstract class AnimatedElement extends Element {

    @Getter
    private final CyclicArrayList<ItemStack> parts;

    @Getter
    private boolean paused;

    public AnimatedElement() {
        this(Lists.newArrayList());
        this.paused = false;
    }

    public AnimatedElement(List<ItemStack> initialParts) {
        this.parts = new CyclicArrayList<>(initialParts);
    }

    public AnimatedElement paused(boolean paused) {
        this.paused = paused;
        return this;
    }

    @Nullable
    @Override
    public ItemStack item(Player player) {
        Optional<ItemStack> itemOptional = paused ? parts.getCurrent() : parts.getNext();
        return itemOptional.orElse(null);
    }

}
