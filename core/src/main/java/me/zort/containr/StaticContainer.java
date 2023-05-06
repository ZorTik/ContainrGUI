package me.zort.containr;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import me.zort.containr.internal.util.Pair;
import me.zort.containr.util.Util;

import java.util.*;

/**
 * A container that can't be paged.
 *
 * @author ZorTik
 */
public class StaticContainer extends Container {

    public StaticContainer(int xSize, int ySize) {
        super(xSize, ySize);
    }

    @Override
    public boolean appendContainer(Container container) {
        PrimitiveIterator.OfInt positionCandidates = getFreeSlots()
                .filter(slot -> {
                    int[] coords = Util.pos(slot, getSelection().xSideSize());
                    return container.getSelection().fitsInOtherWithOffset(coords[0], coords[1], getSelection().xSideSize(), getSelection().ySideSize());
                })
                .iterator();
        if(!positionCandidates.hasNext()) return false;
        while(positionCandidates.hasNext()) {
            int positionRelativeIndex = positionCandidates.nextInt();
            if(setContainer(container, positionRelativeIndex)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public final <T extends Element> Map<Integer, T> content(Class<T> clazz) {
        Map<Integer, T> result = Maps.newHashMap();
        getElements().forEach((pos, e) -> {
            if((clazz == null || clazz.isAssignableFrom(e.getClass())) && pos < getSelection().size()) {
                result.put(convertElementPosToRealPos(pos), (T) e);
            }
        });
        getContainers().values().forEach(c -> result.putAll(c.content(clazz)));
        return result;
    }

    public final void centerElements() {
        centerElements(getSelection().xSideSize());
    }

    public final void centerElements(int xSize) {
        centerElements(xSize, 1);
    }

    public final void centerElements(int xSize, int elementDist) {
        centerElements(xSize, elementDist, 1, true);
    }

    public final void centerElements(int xSize, int elementDist, int yElementDist, boolean keepXSize) {
        if(xSize < 1) throw new IllegalArgumentException("Cannot center elements with x śide size < 1");
        if(getElements().size() == 0) return;
        List<Element> elementsClone = Lists.newArrayList(getElements().values());
        List<List<Element>> basePartition = Lists.partition(elementsClone, xSize);
        final int finalElementDist = elementDist;
        int maxRowWidth = basePartition.stream()
                .mapToInt(List::size)
                .max().orElse(0);
        if(maxRowWidth * finalElementDist > xSize) {
            elementDist = xSize / maxRowWidth;
        }
        int baseRowsSize = basePartition.size();
        LinkedList<List<Element>> rows = Lists.newLinkedList();
        if(baseRowsSize > 1) {
            if(baseRowsSize > 2) {
                for(int i = 0; i < baseRowsSize - 2; i++) {
                    rows.add(basePartition.get(i));
                }
            }
            List<Element> temp = Lists.newArrayList();
            for(int i = baseRowsSize - 2; i < baseRowsSize; i++) {
                temp.addAll(basePartition.get(i));
            }
            List<List<Element>> overlayPartition = Lists.partition(temp,
                    temp.size() > 1 ? (temp.size() / 2) + (temp.size() % 2) : 1
            );
            rows.addFirst(overlayPartition.get(0));
            if(overlayPartition.size() > 1) {
                rows.addLast(overlayPartition.get(1));
            }
        } else {
            rows.add(basePartition.get(0));
        }
        if(!keepXSize && rows.size() > getSelection().xSideSize()) {
            centerElements(xSize + 1, elementDist, yElementDist, false);
            return;
        }
        getElements().clear();
        for(int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            //int rowLeftElementIndex = rowIndex * xSize;
            int rowLeftElementIndex = ((getSelection().xSideSize() / 2) - (xSize / 2)) + ((((getSelection().ySideSize() % 2 == 0 && rows.size() % 2 != 0 ? getSelection().ySideSize() / 2 - 1 : getSelection().ySideSize() / 2) - ((rows.size() / 2) * yElementDist)) + (rowIndex * yElementDist)) * getSelection().xSideSize());
            List<Element> row = rows.get(rowIndex);
            Iterator<Element> rowIt = row.iterator();
            int eIndex = 0;
            for(int rri = (xSize / 2) - (Math.min((row.size() * elementDist) - 1, xSize)) / 2; rri < xSize; rri += elementDist) {
                if(rowIt.hasNext()) {
                    Element next = rowIt.next();
                    getElements().put(rowLeftElementIndex + rri, next);
                    eIndex++;
                }
            }
        }
    }

}
