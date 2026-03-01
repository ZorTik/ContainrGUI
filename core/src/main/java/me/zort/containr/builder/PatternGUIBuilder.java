package me.zort.containr.builder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.zort.containr.*;
import me.zort.containr.component.element.ItemElement;
import me.zort.containr.component.gui.AnimatedGUI;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * A PatternGUIBuilder is utility that allows building complex GUIs
 * based on text pattern that could possibly be put into the configuration.
 *
 * @author ZorTik
 */
public final class PatternGUIBuilder implements GUIBuilder<GUI> {

    private final Map<Integer, ContainerFactory> containerFactories = new ConcurrentHashMap<>();
    private final Map<Integer, ElementFactory> elementFactories = new ConcurrentHashMap<>();
    private final String[] pattern;
    private final int rows;

    private String title;
    private ElementFactory fillerFactory = null;

    /**
     * @deprecated Use {@link GUIFactory} instead.
     */
    @Deprecated
    public interface PatternGUIFactory<T extends GUI> {

        T create(String title, int rows, Consumer<GUI> doBuildFunction);

    }

    /**
     * A factory that constructs a GUI based on provided title, rows and a build function.
     *
     * @param <T> Type of the GUI to be built.
     */
    public interface GUIFactory<T extends GUI> {

        /**
         * Constructs a GUI based on provided title, rows and a build function.
         *
         * @param title GUI's title.
         * @param rows GUI's rows.
         * @param doBuildFunction A function that accepts a GUI and a player and builds the GUI by setting
         *                        containers and elements to it.
         * @return Constructed GUI.
         */
        T create(String title, int rows, BiConsumer<GUI, Player> doBuildFunction);

    }

    public interface ContainerFactory {

        /**
         * Construct a container.
         *
         * @param gui The GUI the container is being built for.
         * @param player The player for whom the GUI is being built.
         * @return The container.
         */
        Container create(GUI gui, Player player);

        static ContainerFactory of(Container container) {
            return (gui, player) -> container;
        }

    }

    public interface AbstractContainerFactory {

        /**
         * Creates a container factory based on x and y sizes.
         *
         * @param xSize X size.
         * @param ySize Y size.
         * @return The created container factory.
         */
        ContainerFactory create(int xSize, int ySize);

        @Contract(pure = true)
        static @NotNull AbstractContainerFactory containerFactory(BiFunction<Integer, Integer, ContainerFactory> func) {
            return func::apply;
        }

        @Contract(pure = true)
        static @NotNull AbstractContainerFactory container(BiFunction<Integer, Integer, Container> func) {
            return (xSize, ySize) -> (gui, player) -> func.apply(xSize, ySize);
        }

    }

    public interface ElementFactory {

        /**
         * Construct an element.
         *
         * @param gui The GUI the element is being built for.
         * @param player The player for whom the GUI is being built.
         * @return The element.
         */
        Element create(GUI gui, Player player);

        static ElementFactory of(Element element) {
            return (gui, player) -> element;
        }

    }

    class SymbolMatchIterator implements Iterator<Integer> {

        private final List<Integer> availableIndexes;
        private int current;

        public SymbolMatchIterator(@NotNull final String symbol, @NotNull final Predicate<Integer> pred) {
            this.availableIndexes = new ArrayList<>();
            this.current = -1;

            populateIndexes(symbol, pred);
        }

        @Override
        public Integer next() {
            return availableIndexes.get(++current);
        }

        @Override
        public boolean hasNext() {
            return availableIndexes.size() > current + 1;
        }

        private void populateIndexes(String symbol, Predicate<Integer> pred) {
            int i = 0;
            for (String s : pattern) {
                for (char c : s.toCharArray()) {
                    if (c == symbol.charAt(0) && pred.test(i)) availableIndexes.add(i);
                    i++;
                }
            }
        }

    }

    public static final class PatternContainerMatcher {

        private final String[] pattern;
        private final char symbol;

        public PatternContainerMatcher(@NotNull final String[] pattern,
                                       @NotNull final String symbol) {
            checkSymbol(symbol);
            this.pattern = pattern;
            this.symbol = symbol.charAt(0);
        }

        public List<SizeMatch> match() {
            Findings findings = new Findings();

            int lineIndex = 0;
            for(String line : pattern) {
                char[] chars = line.toCharArray();

                int blobStart = -1;
                for(int i = 0; i < chars.length; i++) {
                    char c = chars[i];
                    if(c != symbol) continue;

                    if(i == 0 || c != chars[i - 1]) {
                        blobStart = i;
                    }
                    if(i == chars.length - 1 || c != chars[i + 1]) {
                        if(blobStart == -1) {
                            blobStart = i;
                        }

                        // Saving left-right bounds on every line temporarily.
                        findings.getBegins().add(new int[] {blobStart, lineIndex});
                        findings.getEnds().add(new int[] {i, lineIndex});
                    }
                }
                lineIndex++;
            }

            return findings.findMatches();
        }

        @Getter
        private static class Findings {
            private final List<int[]> begins = new ArrayList<>();
            private final List<int[]> ends = new ArrayList<>();

            public List<SizeMatch> findMatches() {
                List<SizeMatch> matches = new ArrayList<>();
                List<int[]> matchedEnds = new ArrayList<>();
                for(int[] begin : begins) {
                    int[] end = ends.stream()
                            .filter(e -> e[1] == begin[1] && !matchedEnds.contains(e))
                            .min(Comparator.comparingInt(e -> e[0])).get();
                    matchedEnds.add(end);
                    Optional<SizeMatch> matchOptional = matches.stream()
                            .filter(m -> m.matchesBeginEnd(begin, end))
                            .findFirst();
                    if(matchOptional.isPresent()) {
                        matchOptional.get().getSize()[1]++;
                    } else {
                        matches.add(new SizeMatch((begin[1] * 9) + begin[0], new int[] {(end[0] + 1) - begin[0], 1}));
                    }
                }

                return matches;
            }

        }

        @AllArgsConstructor
        @Getter
        public static class SizeMatch {
            private final int index;
            private final int[] size;

            public boolean matchesBeginEnd(int[] begin, int[] end) {
                int x = this.getIndex() % 9;
                return begin[0] == x && end[0] == x + (size[0] - 1);
            }
        }

    }

    @Deprecated
    public interface PatternContainerFactory<T extends Container> {

        /**
         * Constructs a container based on found x and y sizes and
         * an init function.
         * These sizes are calculated from provided pattern in the
         * {@link PatternGUIBuilder}.
         *
         * @param xSize X size.
         * @param ySize Y size.
         * @param initFunction An init function
         * @return Constructed container.
         */
        T create(int xSize, int ySize, Consumer<T> initFunction);

    }

    public PatternGUIBuilder(final @NotNull String title, final @NotNull String[] pattern) {
        checkArgument(
                pattern.length > 0 && pattern.length < 7, "Pattern height must be > 0 and < 7!");
        validatePatternWidth(pattern);
        this.title = title;
        this.rows = pattern.length;
        this.pattern = pattern;
    }

    /**
     * Builds a new PatternGUIBuilder from bukkit ConfigurationSection to speed-up
     * the development process.
     * <p>
     * Configuration section example:
     * <pre>
     *     # The GUI's title.
     *     title: "My GUI"
     *     # The GUI's pattern that represents a GUI layout.
     *     # Symbols in the pattern are linked to the item specification in the
     *     # 'items' section.
     *     pattern:
     *     - "#########"
     *     - "#X#X#X#X#"
     *     - "#########"
     *     # Item that fills empty slots in the GUI. Could be same structure
     *     # as the items sections.
     *     filler:
     *       type: "STAINED_GLASS_PANE"
     *       data: 7
     *     # Items that represent each mark (symbol) in the pattern above.
     *     # Marks written here could be duplicate in in the pattern, but not
     *     # in this items section.
     *     items:
     *       X:
     *         type: "DIAMOND"
     * </pre>
     *
     * @param section The configuration section to build the PatternGUIBuilder from.
     * @return The built PatternGUIBuilder.
     */
    public static @NotNull PatternGUIBuilder fromConfig(ConfigurationSection section) {
        PatternGUIBuilder builder = new PatternGUIBuilder(
                section.getString("title", ""),
                section.getStringList("pattern").toArray(new String[0])
        );
        if (section.contains("filler")) {
            builder.andFill(Component.element(section.getConfigurationSection("filler")).build());
        }
        if (section.contains("items")) {
            section.getConfigurationSection("items").getKeys(false).forEach(key -> {
                builder.andMark(key, Component.element(section.getConfigurationSection("items." + key)).build());
            });
        }
        return builder;
    }

    /**
     * Sets a title to the GUI.
     *
     * @param title The title to set.
     * @return The current builder.
     */
    public @NotNull PatternGUIBuilder andTitle(final @NotNull String title) {
        this.title = title;
        return this;
    }

    /**
     * @deprecated Use {@link #andMark(String, AbstractContainerFactory)} instead.
     */
    @Deprecated
    public <T extends Container> @NotNull PatternGUIBuilder andMark(
            String symbol,
            Class<T> typeClass,
            Consumer<T> initFunction
    ) {
        return andMark(symbol, typeClass, (xSize, ySize, initFunc) -> ContainerBuilder.newBuilder(typeClass)
                .size(xSize, ySize)
                .init(Objects.requireNonNull(initFunc))
                .build(), initFunction);
    }

    /**
     * @deprecated Use {@link #andMark(String, AbstractContainerFactory)} instead.
     */
    @Deprecated
    public <T extends Container> @NotNull PatternGUIBuilder andMark(
            String symbol,
            Class<T> typeClass,
            PatternContainerFactory<T> containerFactory,
            Consumer<T> initFunction
    ) {
        return andMark(symbol, (AbstractContainerFactory) (xSize, ySize) ->
                (ContainerFactory) (gui, player) -> containerFactory.create(xSize, ySize, initFunction));
    }

    /**
     * Marks a symbol in the pattern as a container with a factory that creates
     * a container based on found x and y sizes.
     * <p></p>
     * Example:
     * <pre>
     *     andMark("X", AbstractContainerFactory.containerFactory((xSize, ySize) -> {
     *             return (gui, player) -> {
     *                 return Component.staticContainer()
     *                         .size(xSize, ySize)
     *                         .init(c -> {
     *                             // init logic
     *                         })
     *                         .build();
     *             };
     *         }));
     *     andMark("X", AbstractContainerFactory.container((xSize, ySize) -> {
     *             return Component.staticContainer()
     *                     .size(xSize, ySize)
     *                     .init(c -> {
     *                         // init logic
     *                     })
     *                     .build();
     *         }));
     * </pre>
     *
     * @param symbol Symbol to be marked as a container.
     * @param abstractContainerFactory Factory that creates a container factory based on found x and y sizes.
     * @return This builder.
     */
    public @NotNull PatternGUIBuilder andMark(String symbol, AbstractContainerFactory abstractContainerFactory) {
        Objects.requireNonNull(symbol);
        Objects.requireNonNull(abstractContainerFactory);

        for(PatternContainerMatcher.SizeMatch match : new PatternContainerMatcher(pattern, symbol).match()) {
            ContainerFactory containerFactory = abstractContainerFactory.create(
                    match.getSize()[0], match.getSize()[1]);

            this.containerFactories.put(match.getIndex(), containerFactory);
        }
        return this;
    }

    public PatternGUIBuilder andMark(final @NotNull String symbol, final @NotNull ItemStack item) {
        return andMark(symbol, ItemElement.on(item));
    }

    public PatternGUIBuilder andMark(final @NotNull String symbol, final @NotNull Element element) {
        return andMark(symbol, ElementFactory.of(element));
    }

    public PatternGUIBuilder andMark(final @NotNull String symbol, final @NotNull ElementFactory elementFactory) {
        checkSymbol(symbol);
        new SymbolMatchIterator(symbol, i -> true).forEachRemaining(i -> elementFactories.put(i, elementFactory));
        return this;
    }

    /**
     * Puts an element to the queue for another symbol of the same type.
     * This can be used to insert elements to ornaments without using
     * containers.
     *
     * @see #andQueue(String, Element...)
     * @deprecated Use {@link #andQueue(String, Element...)} instead.
     */
    @Deprecated
    public PatternGUIBuilder addQueue(final @NotNull String symbol, Element... elementsToAdd) {
        return andQueue(symbol, elementsToAdd);
    }

    /**
     * Puts an element to the queue for another symbol of the same type.
     * This can be used to insert elements to ornaments without using
     * containers.
     * <p>
     * Example:
     * #X#X#X#
     * -------
     * If symbol on index 0 (first X from top left) is taken, the element
     * is put to the next available index.
     * @param symbol Symbol to be matched.
     * @param elementsToAdd Elements to be put.
     * @return This builder.
     */
    public PatternGUIBuilder andQueue(final @NotNull String symbol, Element... elementsToAdd) {
        ElementFactory[] factories = new ElementFactory[elementsToAdd.length];
        for(int i = 0; i < elementsToAdd.length; i++) {
            factories[i] = ElementFactory.of(elementsToAdd[i]);
        }

        return andQueue(symbol, factories);
    }

    /**
     * @see #andQueue(String, Element...)
     */
    public PatternGUIBuilder andQueue(final @NotNull String symbol, ElementFactory... elementsToAdd) {
        checkSymbol(symbol);
        SymbolMatchIterator iter = new SymbolMatchIterator(symbol, i -> !elementFactories.containsKey(i));
        for(ElementFactory element : elementsToAdd) {
            if(!iter.hasNext()) break;
            elementFactories.put(iter.next(), element);
        }
        return this;
    }

    public PatternGUIBuilder andFill(final @Nullable Element element) {
        return andFill(element == null ? null : ElementFactory.of(element));
    }

    public PatternGUIBuilder andFill(final @Nullable ElementFactory elementFactory) {
        this.fillerFactory = elementFactory;
        return this;
    }

    /**
     * Builds an animated GUI with the specified update period.
     *
     * @param period The update period for the animated GUI.
     * @param unit The time unit for the update period.
     * @return The built animated GUI.
     */
    public @NotNull AnimatedGUI build(int period, TimeUnit unit) {
        return new SimpleGUIBuilder().title(title).rows(rows)
                .prepare(this::doBuild)
                .build(period, unit);
    }

    /**
     * Builds the GUI.
     *
     * @return The built GUI.
     */
    public @NotNull GUI build() {
        return new SimpleGUIBuilder().title(title).rows(rows)
                .prepare(this::doBuild)
                .build();
    }

    /**
     * @deprecated Use {@link #build(GUIFactory)} instead.
     */
    @Deprecated
    public <T extends GUI> @NotNull T build(PatternGUIFactory<T> factory) {
        return factory.create(title, rows, (gui) -> doBuild(gui, null));
    }

    /**
     * Builds the GUI using a custom GUI factory.
     *
     * @param factory The factory to build the GUI with.
     * @return The built GUI.
     * @param <T> Type of the GUI to be built.
     */
    public <T extends GUI> @NotNull T build(GUIFactory<T> factory) {
        return factory.create(title, rows, this::doBuild);
    }

    private void doBuild(GUI gui, Player player) {
        Container container = gui.getContainer();
        container.clear();

        containerFactories.forEach((slot, factory) -> {
            container.setContainer(slot, factory.create(gui, player));
        });
        elementFactories.forEach((slot, factory) -> {
            container.setElement(slot, factory.create(gui, player));
        });

        if (fillerFactory != null) {
            container.fillElement(fillerFactory.create(gui, player));
        }
    }

    private static void checkSymbol(String symbol) {
        checkArgument(symbol.length() == 1, "The symbol must be a single character!");
    }

    private static void validatePatternWidth(String[] pattern) {
        for(String line : pattern) {
            if(line.length() != 9) {
                throw new IllegalArgumentException("Pattern width must be 9!");
            }
        }
    }

}
