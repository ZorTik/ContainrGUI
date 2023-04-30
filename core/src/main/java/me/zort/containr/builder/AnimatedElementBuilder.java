package me.zort.containr.builder;

import com.google.common.collect.Lists;
import me.zort.containr.ContextClickInfo;
import me.zort.containr.component.element.AnimatedElement;
import me.zort.containr.internal.util.CyclicArrayList;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class AnimatedElementBuilder implements ElementBuilder<AnimatedElement> {

    private Consumer<ContextClickInfo> action = (i) -> {};
    private final CyclicArrayList<Supplier<ItemStack>> frames = new CyclicArrayList<>();

    public final AnimatedElementBuilder click(Consumer<ContextClickInfo> info) {
        this.action = this.action.andThen(info);
        return this;
    }

    public final AnimatedElementBuilder frames(ItemStack... frames) {
        for (ItemStack frame : frames) {
            this.frames.add(() -> frame);
        }
        return this;
    }

    @SafeVarargs
    public final AnimatedElementBuilder frames(Supplier<ItemStack>... frames) {
        this.frames.addAll(Lists.newArrayList(frames));
        return this;
    }

    @Override
    public AnimatedElement build() {
        return new AnimatedElement(frames) {
            @Override
            public void click(ContextClickInfo info) {
                action.accept(info);
            }
        };
    }
}
