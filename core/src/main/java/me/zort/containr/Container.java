package me.zort.containr;

import com.google.common.annotations.Beta;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.zort.containr.evt.EvtBus;
import me.zort.containr.geometry.Region;
import me.zort.containr.internal.util.Pair;
import me.zort.containr.util.Util;
import org.apache.commons.lang.RandomStringUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A container is a component that can contain other components.
 * It dynamically provides contents calculated to Minecraft GUI
 * coordinates.
 *
 * @author ZorTik
 */
@Getter
public abstract class Container implements ContainerComponent {

    private final Map<Integer, Container> containers;
    private final Map<Integer, Element> elements;
    private final List<ComponentSource> attachedSources;
    private final ComponentTunnel componentTunnel;
    private final EvtBus<LocalEventInterface> eventBus;
    private final Region selection;
    private Container parent;
    private UpdateContext lastUpdateContext = null;
    private boolean initialized = false;

    public Container(final int xSize, final int ySize) {
        this.containers = new ConcurrentHashMap<>();
        this.elements = new ConcurrentHashMap<>();
        this.attachedSources = new CopyOnWriteArrayList<>();
        this.componentTunnel = new LocalComponentTunnel(this);
        this.eventBus = new EvtBus<>();
        this.parent = null;
        this.selection = new Region(new Pair<>(0, 0), new Pair<>(xSize - 1, ySize - 1));
    }

    /**
     * Appends another container to this container.
     * Since every container type has different implementation of
     * setting containers, this is not implemented here.
     *
     * @param container The container to append
     * @return Whether the container was appended successfully
     */
    public abstract boolean appendContainer(Container container);

    /**
     * Initialization function called on first component mount.
     * Should contain content initialization code.
     */
    @ApiStatus.OverrideOnly public void init() {}

    /**
     * Called on every {@link GUI#update(Player, boolean, Class[])}.
     * This should contain logic that needs to be refreshed on every
     * menu update.
     *
     * @param player The player that is subject of the update
     */
    @ApiStatus.OverrideOnly public void refresh(Player player) {}

    protected void doInit() {
        if (!initialized) {
            init();

            initialized = true;
        }
    }

    @ApiStatus.OverrideOnly
    public <T extends Element> Map<Integer, T> content(Class<T> clazz) {
        Map<Integer, T> content = new HashMap<>();

        // Emit event locally. Subscribers can modify the content map.
        eventBus.emit(new Event.UpdateEvent(this, content));

        return content;
    }

    public final Map<Integer, Element> content(List<Class<? extends Element>> classes) {
        Map<Integer, Element> res = Maps.newHashMap();
        if(classes != null) {
            classes.forEach(clazz -> res.putAll(content(clazz)));
        } else {
            res.putAll(content((Class<Element>) null));
        }
        return res;
    }

    public void emitEventRecursively(LocalEventInterface event) {
        eventBus.emit(event);
        containers.values().forEach(c -> c.emitEventRecursively(event));
    }

    public void clear() {
        getContainers().clear();
        getElements().clear();
    }

    @Beta
    public void attachSource(ComponentSource source) {
        this.attachedSources.add(source);
    }

    protected void registerSources() {
        for (ComponentSource source : this.attachedSources) {
            source.enable(componentTunnel);
        }
    }

    protected void unregisterSources() {
        for (ComponentSource source : this.attachedSources) {
            source.disable(componentTunnel);
        }
    }

    public boolean setContainer(int positionRelativeIndex, @NotNull Container container) {
        return setContainer(container, positionRelativeIndex);
    }

    @Deprecated
    public boolean setContainer(@NotNull Container container, int positionRelativeIndex) {
        int[] positionRealCoords = convertElementRealPosToCoords(convertElementPosToRealPos(positionRelativeIndex));
        Region selection = container.getSelection();
        selection.moveByLeftX(positionRealCoords[0]);
        selection.moveByTopY(positionRealCoords[1]);
        if(containers.values().stream().noneMatch(c -> c.getSelection().collidesWith(selection))) {
            container.setParent(this);
            //container.init();
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

    public void setElement(int relativeIndex, Element element) {
        setElement(element, relativeIndex);
    }

    @Deprecated
    public void setElement(Element element, int relativeIndex) {
        getElements().put(relativeIndex, element);
    }

    public void moveAllByY(int yAddon, Class<?> filter) {
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
            Region inner = c.getSelection();
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

    public @NotNull Optional<Pair<Container, Element>> findElementById(String id) {
        if (id == null) return Optional.empty();

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

    public @NotNull IntStream searchContainers(Class<? extends Container> containerClass) {
        return containers.entrySet().stream()
                .filter(entry -> entry.getValue().getClass().equals(containerClass))
                .mapToInt(Map.Entry::getKey);
    }

    public int searchContainer(Class<? extends Container> containerClass) {
        return searchContainers(containerClass).findFirst().orElse(-1);
    }

    public @NotNull List<Container> getContainers(boolean deep) {
        List<Container> containers = new ArrayList<>(getContainers().values());
        if(deep)
            containers.addAll(containers
                    .stream()
                    .flatMap(c -> c.getContainers(true).stream())
                    .collect(Collectors.toList()));
        return containers;
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

    void setParent(Container parent) {
        if(parent != null && parent != this) {
            parent.getContainers().values().removeIf(c -> c == this);
        }
        this.parent = parent;
    }

    void setLastUpdateContext(UpdateContext context) {
        this.lastUpdateContext = context;
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

    /**
     * Returns context of last performed update by a GUI.
     * This invocation is unsafe if there are more than one GUIs using this
     * container instance.
     *
     * @return The context of last update
     */
    @Nullable
    public UpdateContext getUpdateContextUnsafe() {
        Container current = this;
        UpdateContext ctx;
        while ((ctx = current.lastUpdateContext) == null) {
            current = current.getParent();
            if(current == null) {
                break;
            }
        }
        return ctx;
    }

    @NotNull
    @Override
    public Iterator<Element> iterator() {
        return elements.values().iterator();
    }

    @RequiredArgsConstructor
    @Getter
    public static class LocalComponentTunnel implements ComponentTunnel {
        private final ContainerComponent component;
        private final String id = RandomStringUtils.randomAlphanumeric(8);

        @Override
        public void send(ContainerComponent container) {
            if(!(container instanceof Container)) // TODO: Accept every ContainerComponent.
                throw new IllegalArgumentException("Container must be instance of Container!");

            component.appendContainer((Container) container);
        }
        @Override
        public void send(Element element) {
            component.appendElement(element);
        }

        @Override
        public void clear() {
            component.clear();
        }
    }

    public interface LocalEventInterface {
        @Nullable Container getContainer();
    }

    public static final class Event {

        @SuppressWarnings("unchecked, rawtypes")
        @RequiredArgsConstructor
        public static class UpdateEvent implements LocalEventInterface {
            @Getter
            private final Container container;
            private final Map content;

            public void append(Element element) {
                int index = content.keySet()
                        .stream()
                        .mapToInt(o -> (int) o)
                        .max().orElse(-1) + 1;
                content.put(index, element);
            }
        }

        @RequiredArgsConstructor
        @Getter
        public static class OpenEvent implements LocalEventInterface {
            private final GUI gui;
            private final Player player;

            @Override
            public @Nullable Container getContainer() {
                return null;
            }
        }

    }

}
