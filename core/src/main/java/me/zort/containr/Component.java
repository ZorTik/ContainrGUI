package me.zort.containr;

import me.zort.containr.builder.*;
import org.jetbrains.annotations.NotNull;

/**
 * Represents any component usable in the
 * Containr library.
 *
 * @author ZorTik
 */
public interface Component {

    static @NotNull SimpleGUIBuilder gui() {
        return new SimpleGUIBuilder();
    }

    static @NotNull PatternGUIBuilder gui(@NotNull String title, @NotNull String[] pattern) {
        return new PatternGUIBuilder(title, pattern);
    }

    static @NotNull ContainerBuilder<StaticContainer> staticContainer() {
        return ContainerBuilder.newBuilder(StaticContainer.class);
    }

    static @NotNull ContainerBuilder<PagedContainer> pagedContainer() {
        return ContainerBuilder.newBuilder(PagedContainer.class);
    }

    static @NotNull SimpleElementBuilder element() {
        return new SimpleElementBuilder();
    }

    static @NotNull <T> SwitchableElementBuilder<T> switchableElement(Class<T> optionsType) {
        return new SwitchableElementBuilder<>();
    }

    static @NotNull AnimatedElementBuilder animatedElement() {
        return new AnimatedElementBuilder();
    }

}
