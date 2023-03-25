package me.zort.containr.builder;

import com.google.common.base.Preconditions;
import lombok.AllArgsConstructor;
import lombok.Getter;
import me.zort.containr.*;
import me.zort.containr.component.element.ItemElement;
import me.zort.containr.component.gui.AnimatedGUI;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkArgument;

public final class PatternGUIBuilder implements GUIBuilder<GUI> {

    private final String[] pattern;
    private final Map<Integer, Container> containers;
    private final Map<Integer, Element> elements;
    private final int rows;
    private String title;
    private Element filler;

    public PatternGUIBuilder(final String title, final String[] pattern) {
        checkArgument(pattern.length > 0 && pattern.length < 7, "Pattern height must be > 0 and < 7!");
        validatePatternWidth(pattern);
        this.title = title;
        this.rows = pattern.length;
        this.pattern = pattern;
        this.containers = new ConcurrentHashMap<>();
        this.elements = new ConcurrentHashMap<>();
        this.filler = null;
    }

    public final PatternGUIBuilder andTitle(String title) {
        this.title = title;
        return this;
    }

    public final <T extends Container> PatternGUIBuilder andMark(String symbol, Class<T> typeClass, Consumer<T> initFunction) {
        Objects.requireNonNull(initFunction);
        return andMark(symbol, typeClass, (xSize, ySize) -> ContainerBuilder.newBuilder(typeClass)
                .size(xSize, ySize)
                .init(initFunction)
                .build());
    }

    @SuppressWarnings("unused")
    public final <T extends Container> PatternGUIBuilder andMark(String symbol, Class<T> typeClass, ContainerFactoryHelper<T> containerFactory) {
        Objects.requireNonNull(symbol);
        Objects.requireNonNull(containerFactory);
        for(PatternContainerMatcher.SizeMatch match : new PatternContainerMatcher(pattern, symbol).match()) {
            T container = containerFactory.create(match.getSize()[0], match.getSize()[1]);
            putContainerMatch(match, container);
        }
        return this;
    }

    private void putContainerMatch(PatternContainerMatcher.SizeMatch match, Container container) {
        this.containers.put(match.getIndex(), container);
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
    public PatternGUIBuilder addQueue(String symbol, Element... elementsToAdd) {
        checkSymbol(symbol);
        IndexIterator iter = new IndexIterator(pattern, symbol, i -> !elements.containsKey(i));
        for(Element element : elementsToAdd) {
            if(!iter.hasNext()) break;
            elements.put(iter.next(), element);
        }
        return this;
    }

    public PatternGUIBuilder andMark(String symbol, Element element) {
        checkSymbol(symbol);
        int index = 0;
        for(String line : pattern) {
            for(char c : line.toCharArray()) {
                if(c == symbol.charAt(0)) {
                    this.elements.put(index, element);
                }
                index++;
            }
        }
        return this;
    }

    public PatternGUIBuilder andMark(String symbol, ItemStack item) {
        return andMark(symbol, ItemElement.on(item));
    }

    public PatternGUIBuilder andFill(Element element) {
        this.filler = element;
        return this;
    }

    public AnimatedGUI build(int period, TimeUnit unit) {
        return new AnimatedGUI(title, rows, period, unit) {
            @Override
            public void build(Player p) {
                doBuild(this);
            }
        };
    }

    public <T extends GUI> T build(PatternGUIFactory<T> factory) {
        return factory.create(title, rows, this::doBuild);
    }

    public GUI build() {
        return new GUI(title, rows) {
            @Override
            public void build(Player p) {
                doBuild(this);
            }
        };
    }

    private void doBuild(GUI gui) {
        Container container = gui.getContainer();
        container.clear();
        containers.forEach(container::setContainer);
        elements.forEach(container::setElement);
        if(filler != null) {
            container.fillElement(filler);
        }
    }

    public interface PatternGUIFactory<T extends GUI> {
        T create(String title, int rows, Consumer<GUI> doBuildFunction);
    }

    private static class IndexIterator implements Iterator<Integer> {

        private final List<Integer> availableIndexes;
        private int current;

        public IndexIterator(@NotNull final String[] pattern,
                             @NotNull final String symbol,
                             @NotNull final Predicate<Integer> pred) {
            this.availableIndexes = new ArrayList<>();
            this.current = -1;

            for(int i = 0; i < pattern.length; i++) {
                String line = pattern[i];
                for(char c : line.toCharArray()) {
                    if(c == symbol.charAt(0) && pred.test(i)) {
                        availableIndexes.add(i);
                    }
                }
            }
        }

        @Override
        public Integer next() {
            return availableIndexes.get(++current);
        }

        @Override
        public boolean hasNext() {
            return availableIndexes.size() > current + 1;
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

    public interface ContainerFactoryHelper<T extends Container> {

        /**
         * Constructs a container based on found x and y sizes.
         * These sizes are calculated from provided pattern in the
         * {@link PatternGUIBuilder}.
         *
         * @param xSize X size.
         * @param ySize Y size.
         * @return Constructed container.
         */
        T create(int xSize, int ySize);

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
