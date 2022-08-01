package me.zort.containr;

import com.google.common.collect.Lists;
import lombok.Getter;
import me.zort.containr.util.CyclicArrayList;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public abstract class AnimatedSuppliedElement extends Element {

    @Getter
    private final CyclicArrayList<Supplier<ItemStack>> parts;

    @Getter
    private boolean paused;

    public AnimatedSuppliedElement() {
        this(Lists.newArrayList());
        this.paused = false;
    }

    public AnimatedSuppliedElement(List<Supplier<ItemStack>> initialParts) {
        this.parts = new CyclicArrayList<>(initialParts);
    }

    public AnimatedSuppliedElement paused(boolean paused) {
        this.paused = paused;
        return this;
    }

    @Nullable
    @Override
    public ItemStack item(Player player) {
        Optional<Supplier<ItemStack>> itemSupplierOptional = paused ? parts.getCurrent() : parts.getNext();
        return itemSupplierOptional.orElse(() -> null).get();
    }

}
