package me.zort.containr;

import com.google.common.collect.Maps;
import lombok.Getter;
import me.zort.containr.geometry.Tetragon;
import me.zort.containr.util.Pair;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Getter
public abstract class Container implements Iterable<Element> {

    private final Map<Integer, Container> containers;
    private final Map<Integer, Element> elements;

    private final Tetragon selection;
    @Getter
    private Container parent;

    public Container(int xSize, int ySize) {
        this(new Pair<>(0, 0), new Pair<>(xSize - 1, ySize - 1));
    }

    protected Container(Pair<Integer, Integer> corner1, Pair<Integer, Integer> corner2) {
        this.containers = Maps.newConcurrentMap();
        this.elements = Maps.newConcurrentMap();
        this.parent = null;
        this.selection = new Tetragon(corner1, corner2);
    }

    //public abstract Map<Integer, Element> content();
    public Map<Integer, Element> content(List<Class<? extends Element>> classes) {
        Map<Integer, Element> res = Maps.newHashMap();
        if(classes != null) {
            classes.forEach(clazz -> res.putAll(content(clazz)));
        } else {
            res.putAll(content((Class<Element>) null));
        }
        return res;
    }
    public abstract <T extends Element> Map<Integer, T> content(Class<T> clazz);
    public abstract boolean appendContainer(Container container);
    public void init() {}
    public void refresh(Player player) {}

    public void clear() {
        getContainers().clear();
        getElements().clear();
    }

    public boolean setContainer(@NotNull Container container, int positionRelativeIndex) {
        int[] positionRealCoords = convertElementRealPosToCoords(convertElementPosToRealPos(positionRelativeIndex));
        Tetragon selection = container.getSelection();
        selection.moveByLeftX(positionRealCoords[0]);
        selection.moveByTopY(positionRealCoords[1]);
        if(containers.values().stream().noneMatch(c -> c.getSelection().collidesWith(selection))) {
            container.setParent(this);
            container.init();
            containers.put(positionRelativeIndex, container);
            return true;
        }
        return false;
    }

    public boolean insertContainerInLine(Container container, int startPositionRelativeIndex, int endPositionRelativeIndex) {
        int i = startPositionRelativeIndex;
        while(i <= endPositionRelativeIndex) {
            if(setContainer(container, i)) {
                return true;
            }
            i++;
        }
        return false;
    }

    public void fillElement(Element element) {
        for(int i = 0; i < getSelection().size(); i++) {
            appendElement(element);
        }
    }

    public void fillElement(Element element, int fromIndexInclusive, int toIndexExclusive) {
        for(int i = fromIndexInclusive; i < toIndexExclusive; i++) {
            getElements().putIfAbsent(i, element);
        }
    }

    public boolean appendElements(Collection<Element> elements) {
        return appendElements(0, elements);
    }

    public boolean appendElements(int startIndex, Collection<Element> elements) {
        int successCount = 0;
        int currentSlot = startIndex;
        for(Element e : elements) {
            if(isFreeSlot(currentSlot)) {
                this.elements.put(currentSlot, e);
                successCount++;
            }
            currentSlot++;
        }
        return successCount == elements.size();
    }

    public boolean appendElement(Element element) {
        return appendElement(0, element);
    }

    public boolean appendElement(int startIndex, Element element) {
        int slot = startIndex;
        while(true) {
            if(isFreeSlot(slot)) {
                elements.put(slot, element);
                return true;
            }
            slot++;
        }
    }

    public void setElement(Element element, int relativeIndex) {
        getElements().put(relativeIndex, element);
    }

    public void moveAllByY(int yAddon, Class<?> filter) { // TODO: Test
        Function<Map.Entry<Integer, ?>, Boolean> func = entry -> filter == null || filter.isAssignableFrom(entry.getValue().getClass());
        Map<Integer, Container> containers = this.containers.entrySet().stream()
                .filter(func::apply)
                .collect(Collectors.toMap(entry -> entry.getKey() + (yAddon * selection.xSideSize()), Map.Entry::getValue));
        Map<Integer, Element> elements = this.elements.entrySet().stream()
                .filter(func::apply)
                .collect(Collectors.toMap(entry -> entry.getKey() + (yAddon * selection.xSideSize()), Map.Entry::getValue));
        if(filter == null) {
            this.containers.clear();
            this.elements.clear();
        } else {
            this.containers.entrySet().removeIf(func::apply);
            this.elements.entrySet().removeIf(func::apply);
        }
        this.containers.putAll(containers);
        this.elements.putAll(elements);
    }

    public void compress() {
        containers.values().forEach(c -> {
            Tetragon inner = c.getSelection();
            int[] exceedingCoords = inner.getExceedingCoords(selection);
            if(inner.fitsInOtherByX(selection.xSideSize())) {
                inner.moveByLeftX(
                        exceedingCoords[0] != 0
                                ? inner.xMin() + exceedingCoords[0]
                                : inner.xMin() + exceedingCoords[1]
                );
            }
            if(inner.fitsInOtherByY(selection.ySideSize())) {
                inner.moveByTopY(
                        exceedingCoords[2] != 0
                                ? inner.yMin() + exceedingCoords[2]
                                : inner.yMin() + exceedingCoords[3]
                );
            }
        });
    }

    public Optional<Pair<Container, Element>> findElementById(String id) {
        AtomicReference<Optional<Pair<Container, Element>>> result = new AtomicReference<>(elements.values().stream()
                .filter(e -> e.getId().equals(id))
                .map(e -> new Pair<>(this, e))
                .findFirst());
        if(result.get().isPresent()) return result.get();
        containers.values().forEach(c -> {
            Optional<Pair<Container, Element>> innerElementOptional = c.findElementById(id);
            if(innerElementOptional.isPresent()) {
                result.set(innerElementOptional);
            }
        });
        return result.get();
    }

    public IntStream searchContainers(Class<? extends Container> containerClass) {
        return containers.entrySet().stream()
                .filter(entry -> entry.getValue().getClass().equals(containerClass))
                .mapToInt(Map.Entry::getKey);
    }

    public int searchContainer(Class<? extends Container> containerClass) {
        return searchContainers(containerClass).findFirst().orElse(-1);
    }

    public int[] getEmptyElementSlots() {
        return IntStream.range(0, getSelection().size())
                .filter(i -> !getElements().containsKey(i))
                .toArray();
    }

    protected IntStream getFreeSlots() {
        return IntStream.range(0, getSelection().size());
    }

    public boolean isFreeSlot(int slot) {
        return containers.values().stream().noneMatch(c -> c.getSelection().contains(convertElementRealPosToCoords(slot))) && !elements.containsKey(slot);
    }

    protected void setParent(Container parent) {
        if(parent != null && parent != this) {
            parent.getContainers().values().removeIf(c -> c == this);
        }
        this.parent = parent;
    }

    protected int[] convertElementRealPosToCoords(int pos) {
        //return Util.relativeToRealCoords(Util.pos(pos, selection.xSideSize()), this);
        return Util.pos(pos, 9);
    }

    protected int convertElementPosToRealPos(int pos) {
        return Util.pos(Util.relativeToRealCoords(Util.pos(pos, selection.xSideSize()), this), 9);
    }

    protected int maxLocalAttachedSlot() {
        return getElements().keySet().stream().max(Integer::compare).orElse(-1);
    }

    protected int maxAttachedSlot() {
        int slot = getElements().keySet().stream()
                .mapToInt(this::convertElementPosToRealPos)
                .max().orElse(-1);
        for(Container c : containers.values()) {
            int temp = c.maxAttachedSlot();
            if(temp > slot) slot = temp;
        }
        return slot;
    }

    public List<Container> innerContainers() {
        List<Container> containers = new ArrayList<>(this.containers.values());
        this.containers.values().forEach(c -> containers.addAll(c.innerContainers()));
        return containers;
    }

    public List<Element> innerElements() {
        List<Element> elements = new ArrayList<>(this.elements.values());
        containers.values().forEach(c -> elements.addAll(c.innerElements()));
        return elements;
    }

    @NotNull
    @Override
    public Iterator<Element> iterator() {
        return elements.values().iterator();
    }

}
