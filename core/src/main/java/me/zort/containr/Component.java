package me.zort.containr;

import me.zort.containr.builder.*;
import me.zort.containr.internal.util.ItemBuilder;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;

/**
 * Represents any component usable in the Containr library.
 * Also, this is the place where component api methods are present.
 *
 * @author ZorTik
 */
public interface Component {

    class ElementDeserializer {

        private final List<BiConsumer<ItemBuilder, ConfigurationSection>> after;

        private ElementDeserializer() {
            after = new CopyOnWriteArrayList<>();
        }

        public @NotNull ElementDeserializer namePostprocessor(BiConsumer<String, ItemStack> mod) {
            return addPostprocessor((b, s) -> b.withBuildModifier(item -> mod.accept(b.getName(), item)));
        }

        public @NotNull ElementDeserializer lorePostprocessor(BiConsumer<List<String>, ItemStack> mod) {
            return addPostprocessor((b, s) -> b.withBuildModifier(item -> mod.accept(b.getLore(), item)));
        }

        public @NotNull ElementDeserializer addPostprocessor(BiConsumer<ItemBuilder, ConfigurationSection> mod) {
            after.add(mod);
            return this;
        }

        public @NotNull SimpleElementBuilder element() {
            return Component.element();
        }

        public @NotNull SimpleElementBuilder element(ConfigurationSection section) {
            return Component.element(section);
        }

        public @NotNull SimpleElementBuilder element(
                ConfigurationSection section,
                BiConsumer<ItemBuilder, ConfigurationSection> modifier
        ) {
            return Component.element(section, (b, s) -> {
                modifier.accept(b, s);
                after.forEach(consumer -> consumer.accept(b, s));
            });
        }

    }

    /**
     * Creates a new GUI builder.
     *
     * @return The GUI builder
     */
    static @NotNull SimpleGUIBuilder gui() {
        return new SimpleGUIBuilder();
    }

    /**
     * Creates a new pattern GUI builder with provided title and pattern.
     *
     * @return The GUI builder
     */
    static @NotNull PatternGUIBuilder gui(@NotNull String title, @NotNull String[] pattern) {
        return new PatternGUIBuilder(title, pattern);
    }

    /**
     * Creates a new container builder for static container.
     *
     * @return The container builder
     */
    static @NotNull ContainerBuilder<StaticContainer> staticContainer() {
        return ContainerBuilder.newBuilder(StaticContainer.class);
    }

    /**
     * Creates a new container builder for paged container.
     *
     * @return The container builder
     */
    static @NotNull ContainerBuilder<PagedContainer> pagedContainer() {
        return ContainerBuilder.newBuilder(PagedContainer.class);
    }

    /**
     * Creates a new simple element builder.
     * This is a simple way to create an element without inheriting
     * the element class.
     *
     * @return The element builder
     */
    static @NotNull SimpleElementBuilder element() {
        return new SimpleElementBuilder();
    }

    /**
     * Creates a new simple element builder with provided static item.
     *
     * @param item The item to set
     * @return The element builder
     */
    static @NotNull SimpleElementBuilder element(ItemStack item) {
        return new SimpleElementBuilder().item(item);
    }

    static @NotNull SimpleElementBuilder element(ConfigurationSection section) {
        return SimpleElementBuilder.fromConfig(section);
    }

    static @NotNull SimpleElementBuilder element(
            ConfigurationSection section,
            BiConsumer<ItemBuilder, ConfigurationSection> modifier
    ) {
        return SimpleElementBuilder.fromConfig(section, modifier);
    }

    static @NotNull ElementDeserializer elementDeserializer() {
        return new ElementDeserializer();
    }

    /**
     * Creates a new switchable element builder.
     *
     * @param optionsType The type class of the options
     * @return The switchable element builder
     * @param <T> The type of the options
     */
    static @NotNull <T> SwitchableElementBuilder<T> switchableElement(Class<T> optionsType) {
        return new SwitchableElementBuilder<>();
    }

    /**
     * Creates a builder to create a new animated element.
     *
     * @return The animated element builder
     */
    static @NotNull AnimatedElementBuilder animatedElement() {
        return new AnimatedElementBuilder();
    }

}
