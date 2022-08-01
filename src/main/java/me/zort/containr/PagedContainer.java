package me.zort.containr;

import com.google.common.collect.Maps;
import me.zort.containr.geometry.Tetragon;
import me.zort.containr.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Function;

public class PagedContainer extends Container {

    private int currentPageIndex = 0;

    public PagedContainer(int xSize, int ySize) {
        super(xSize, ySize);
    }

    protected PagedContainer(Pair<Integer, Integer> corner1, Pair<Integer, Integer> corner2) {
        super(corner1, corner2);
    }

    public void switchPage(int pageIndex) {
        this.currentPageIndex = pageIndex;
    }

    public boolean nextPage() {
        if(isLastPage()) return false;
        switchPage(getCurrentPageIndex() + 1);
        onPageSwitch(getCurrentPage() - 1, getCurrentPage());
        return true;
    }

    public boolean previousPage() {
        if(isFirstPage()) return false;
        switchPage(getCurrentPageIndex() - 1);
        onPageSwitch(getCurrentPage() + 1, getCurrentPage());
        return true;
    }

    public void onPageSwitch(int before, int after) {}

    public boolean isFirstPage() {
        return currentPageIndex <= 0;
    }

    public boolean isLastPage() {
        return currentPageIndex >= getMaxPageIndex();
    }

    public int getTotalPages() {
        return getMaxPageIndex() + 1;
    }

    public int getMaxPageIndex() {
        /*int maxAttachedSlot = maxAttachedSlot();
        int pageIndex = 0;
        while(convertElementPosToRealPos(pageIndex * getSelection().size()) < maxAttachedSlot) {
            pageIndex++;
        }*/
        return Math.max(
                getPageIndexByRelativePos(getContainers().keySet().stream().mapToInt(i -> i).max().orElse(0)),
                getPageIndexByRelativePos(getElements().keySet().stream().mapToInt(i -> i).max().orElse(0))
        );
    }

    public int getCurrentPageIndex() {
        return currentPageIndex;
    }

    public int getCurrentPage() {
        return getCurrentPageIndex() + 1;
    }

    @Override
    public boolean appendContainer(Container container) {
        if(!container.getSelection().fitsInOtherByX(getSelection().xSideSize())) {
            return false;
        }
        int positionRelativeIndex = 0;
        while(!setContainer(container, positionRelativeIndex)) {
            positionRelativeIndex++;
        }
        return true;
    }

    public int appendContainerResultingIndex(Container container) {
        if(!container.getSelection().fitsInOtherByX(getSelection().xSideSize())) {
            return -1;
        }
        int positionRelativeIndex = 0;
        while(!setContainer(container, positionRelativeIndex)) {
            positionRelativeIndex++;
        }
        return positionRelativeIndex;
    }

    @Override
    public boolean setContainer(@NotNull Container container, int positionRelativeIndex) {
        Function<Integer, Boolean> fitsFunc = slot -> {
            int[] coords = Util.pos(slot, getSelection().xSideSize());
            return container.getSelection().fitsInOtherWithOffsetX(coords[0], getSelection().xSideSize());
        };
        if(!fitsFunc.apply(positionRelativeIndex)) {
            return false;
        }
        int[] positionRealCoords = convertElementRealPosToCoords(convertElementPosToRealPos(positionRelativeIndex % getSelection().size()));
        int pageIndex = convertElementRealPosToCoords(convertElementPosToRealPos(positionRelativeIndex))[1] / getSelection().ySideSize();
        Tetragon selection = container.getSelection();
        selection.moveByLeftX(positionRealCoords[0]);
        selection.moveByTopY(positionRealCoords[1]);
        if(getContainers().entrySet().stream()
                .filter(e -> isOnPage(e.getKey(), pageIndex))
                .noneMatch(e -> e.getValue().getSelection().collidesWith(selection))) {
            if(!getContainers().containsKey(positionRelativeIndex) && !getElements().containsKey(positionRelativeIndex)) {
                container.setParent(this);
                container.init();
                getContainers().put(positionRelativeIndex, container);
                return true;
            } else return false;
        }
        return false;
    }

    @Override
    public <T extends Element> Map<Integer, T> content(@Nullable Class<T> clazz) {
        Map<Integer, T> result = Maps.newHashMap();
        getContainers().entrySet().stream()
                .filter(e -> isOnThisPage(e.getKey()))
                .filter(e -> {
                    int relativeSlot = e.getKey();
                    Container c = e.getValue();
                    int[] relativeCoords = Util.pos(relativeSlot, getSelection().xSideSize());
                    return c.getSelection().fitsInOtherWithOffsetX(relativeCoords[0], getSelection().xSideSize());
                })
                .forEach(e -> {
                    Container c = e.getValue();
                    result.putAll(c.content(clazz));
                });
        getElements().forEach((pos, e) -> {
            if((clazz == null || clazz.isAssignableFrom(e.getClass())) && pos >= getSelection().size() * currentPageIndex && pos < getSelection().size() * (currentPageIndex + 1)) {
                int realPos = convertElementPosToRealPos(pos - (getSelection().size() * currentPageIndex));
                result.put(realPos, (T) e);
            }
        });
        return result;
    }

    protected boolean isOnThisPage(int relativePos) {
        return isOnPage(relativePos, currentPageIndex);
    }

    private boolean isOnPage(int relativePos, int pageIndex) {
        return relativePos >= getSelection().size() * pageIndex && relativePos < getSelection().size() * (pageIndex + 1);
    }

    private int getPageIndexByRelativePos(int relativePos) {
        int pageIndex = 0;
        while((pageIndex + 1) * getSelection().size() <= relativePos) {
            pageIndex++;
        }
        return pageIndex;
    }

}
