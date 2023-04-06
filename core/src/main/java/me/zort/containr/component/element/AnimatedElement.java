package me.zort.containr.component.element;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.zort.containr.Element;
import me.zort.containr.internal.util.CyclicArrayList;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Getter
public abstract class AnimatedElement extends Element {

    private final CyclicArrayList<Supplier<ItemStack>> parts;
    private boolean paused;

    public AnimatedElement() {
        this(Lists.newArrayList());
        this.paused = false;
    }

    public AnimatedElement(List<ItemStack> parts) {
        CyclicArrayList<Supplier<ItemStack>> suppliers = new CyclicArrayList<>();
        for (ItemStack part : parts) {
            suppliers.add(() -> part);
        }
        this.parts = suppliers;
    }

    public AnimatedElement paused(boolean paused) {
        this.paused = paused;
        return this;
    }

    @Nullable
    @Override
    public ItemStack item(Player player) {
        Optional<Supplier<ItemStack>> itemOptional = paused ? parts.getCurrent() : parts.getNext();
        return itemOptional.map(Supplier::get).orElse(null);
    }

}
