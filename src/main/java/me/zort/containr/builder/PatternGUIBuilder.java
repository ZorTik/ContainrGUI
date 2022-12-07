package me.zort.containr.builder;

import com.google.common.base.Preconditions;
import lombok.AllArgsConstructor;
import lombok.Getter;
import me.zort.containr.*;
import me.zort.containr.component.element.ItemElement;
import me.zort.containr.component.gui.AnimatedGUI;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class PatternGUIBuilder implements GUIBuilder<GUI> {

    private final int rows;

    private final String[] pattern;
    private final Map<Integer, Container> containers;
    private final Map<Integer, Element> elements;

    private String title;
    private Element filler;

    public PatternGUIBuilder(String title, String[] pattern) {
        Preconditions.checkArgument(pattern.length > 0 && pattern.length < 7, "Pattern height must be > 0 and < 7!");
        validatePatternWidth(pattern);
        this.title = title;
        this.rows = pattern.length;
        this.pattern = pattern;
        this.containers = new ConcurrentHashMap<>();
        this.elements = new ConcurrentHashMap<>();
        this.filler = null;
    }

    public PatternGUIBuilder andTitle(String title) {
        this.title = title;
        return this;
    }

    public <T extends Container> PatternGUIBuilder andMark(String symbol, Class<T> typeClass, Consumer<T> initFunction) {
        /*for(LocalContainerMatcher.SizeMatch match : new LocalContainerMatcher(pattern, symbol).match()) {
            T container = ContainerBuilder.newBuilder(typeClass)
                    .size(match.getSize()[0], match.getSize()[1])
                    .init(initFunction)
                    .build();
            putContainerMatch(match, container);
        }
        return this;*/

        return andMark(symbol, typeClass, (xSize, ySize) -> ContainerBuilder.newBuilder(typeClass)
                .size(xSize, ySize)
                .init(initFunction)
                .build());
    }

    public <T extends Container> PatternGUIBuilder andMark(String symbol, Class<T> typeClass, ContainerFactoryHelper<T> containerFactory) {
        for(LocalContainerMatcher.SizeMatch match : new LocalContainerMatcher(pattern, symbol).match()) {
            T container = containerFactory.create(match.getSize()[0], match.getSize()[1]);
            putContainerMatch(match, container);
        }
        return this;
    }

    private void putContainerMatch(LocalContainerMatcher.SizeMatch match, Container container) {
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

        public IndexIterator(String[] pattern, String symbol, Predicate<Integer> pred) {
            this.availableIndexes = new ArrayList<>();
            this.current = -1;
            int index = 0;
            for(String line : pattern) {
                for(char c : line.toCharArray()) {
                    if(c == symbol.charAt(0) && pred.test(index)) {
                        availableIndexes.add(index);
                    }
                    index++;
                }
            }
        }

        @Override
        public Integer next() {
            current++;
            return availableIndexes.get(current);
        }

        @Override
        public boolean hasNext() {
            return availableIndexes.size() > current + 1;
        }

    }

    private static class LocalContainerMatcher {

        private final String[] pattern;
        private final String symbol;

        public LocalContainerMatcher(String[] pattern, String symbol) {
            checkSymbol(symbol);
            this.pattern = pattern;
            this.symbol = symbol;
        }

        public List<SizeMatch> match() {
            Findings findings = new Findings();

            int lineIndex = 0;
            for(String line : pattern) {
                char[] chars = line.toCharArray();

                int blobStart = -1;
                for(int i = 0; i < chars.length; i++) {
                    char c = chars[i];
                    if(c != symbol.charAt(0)) continue;

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

                for(int[] begin : begins) {
                    int[] end = ends.stream()
                            .filter(e -> e[1] == begin[1])
                            .min(Comparator.comparingInt(e -> e[0])).get();
                    Optional<SizeMatch> matchOptional = matches.stream()
                            .filter(m -> m.matchesBeginEnd(begin, end))
                            .findFirst();
                    if(matchOptional.isPresent()) {
                        matchOptional.get().getSize()[1]++;
                    } else {
                        matches.add(new SizeMatch(new int[] {end[0] + 1, end[1]}, (begin[1] * 9) + begin[0]));
                    }
                }

                return matches;
            }

        }

        @AllArgsConstructor
        @Getter
        private static class SizeMatch {
            private final int[] size;
            private final int index;

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
        Preconditions.checkArgument(symbol.length() == 1, "The symbol must be a single character!");
    }

    private static void validatePatternWidth(String[] pattern) {
        for(String line : pattern) {
            if(line.length() != 9) {
                throw new IllegalArgumentException("Pattern width must be 9!");
            }
        }
    }

}
