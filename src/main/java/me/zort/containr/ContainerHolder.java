package me.zort.containr;

import me.zort.containr.internal.util.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.IntStream;

public abstract class ContainerHolder implements ContainerComponent {

    public abstract Container getContainer();

    @Override
    public <T extends Element> Map<Integer, T> content(Class<T> clazz) {
        return getContainer().content(clazz);
    }

    @Override
    public boolean appendContainer(Container container) {
        return getContainer().appendContainer(container);
    }

    @Override
    public boolean setContainer(int positionRelativeIndex, @NotNull Container container) {
        return getContainer().setContainer(positionRelativeIndex, container);
    }

    @Override
    public boolean setContainer(@NotNull Container container, int positionRelativeIndex) {
        return getContainer().setContainer(container, positionRelativeIndex);
    }

    @Override
    public boolean insertContainerInLine(Container container, int startPositionRelativeIndex, int endPositionRelativeIndex) {
        return getContainer().insertContainerInLine(container, startPositionRelativeIndex, endPositionRelativeIndex);
    }

    @Override
    public void fillElement(Element element) {
        getContainer().fillElement(element);
    }

    @Override
    public void fillElement(Element element, int fromIndexInclusive, int toIndexExclusive) {
        getContainer().fillElement(element, fromIndexInclusive, toIndexExclusive);
    }

    @Override
    public boolean appendElements(Collection<Element> elements) {
        return getContainer().appendElements(elements);
    }

    @Override
    public boolean appendElements(int startIndex, Collection<Element> elements) {
        return getContainer().appendElements(startIndex, elements);
    }

    @Override
    public boolean appendElement(Element element) {
        return getContainer().appendElement(element);
    }

    @Override
    public boolean appendElement(int startIndex, Element element) {
        return getContainer().appendElement(startIndex, element);
    }

    @Override
    public void setElement(int relativeIndex, Element element) {
        getContainer().setElement(relativeIndex, element);
    }

    @Override
    public void setElement(Element element, int relativeIndex) {
        getContainer().setElement(element, relativeIndex);
    }

    @Override
    public void moveAllByY(int yAddon, Class<?> filter) {
        getContainer().moveAllByY(yAddon, filter);
    }

    @Override
    public void compress() {
        getContainer().compress();
    }

    @Override
    public Optional<Pair<Container, Element>> findElementById(String id) {
        return getContainer().findElementById(id);
    }

    @Override
    public IntStream searchContainers(Class<? extends Container> containerClass) {
        return getContainer().searchContainers(containerClass);
    }

    @Override
    public int searchContainer(Class<? extends Container> containerClass) {
        return getContainer().searchContainer(containerClass);
    }

    @Override
    public int[] getEmptyElementSlots() {
        return getContainer().getEmptyElementSlots();
    }

    @Override
    public boolean isFreeSlot(int slot) {
        return getContainer().isFreeSlot(slot);
    }

    @Override
    public List<Container> innerContainers() {
        return getContainer().innerContainers();
    }

    @Override
    public List<Element> innerElements() {
        return getContainer().innerElements();
    }

    @NotNull
    @Override
    public Iterator<Element> iterator() {
        return getContainer().iterator();
    }
}
