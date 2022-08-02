package me.zort.containr.component.element;

import com.google.common.collect.Lists;
import lombok.Getter;
import me.zort.containr.Element;
import me.zort.containr.util.CyclicArrayList;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public abstract class AnimatedMultiElement extends Element {

    private final CyclicArrayList<Element> parts;

    @Getter
    private boolean paused;

    public AnimatedMultiElement() {
        this(Lists.newArrayList());
        this.paused = false;
    }

    public AnimatedMultiElement(List<Element> initialList) {
        this.parts = new CyclicArrayList<>(initialList);
    }

    public AnimatedMultiElement paused(boolean paused) {
        this.paused = paused;
        return this;
    }

    @Nullable
    @Override
    public ItemStack item(Player player) {
        Optional<Element> elementOptional = paused ? parts.getCurrent() : parts.getNext();
        if(!elementOptional.isPresent()) return null;
        Element element = elementOptional.get();
        return element.item(player);
    }

}
