package me.zort.containr.util;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class CyclicArrayList<T> extends LinkedList<T> {

    @Setter
    @Getter
    private int currentPos;

    public CyclicArrayList() {
        super();
        reset();
    }

    public CyclicArrayList(List<T> initialList) {
        super(initialList);
        reset();
    }

    public void reset() {
        this.currentPos = 0;
    }

    public boolean isFirst() {
        return currentPos == 0;
    }

    public boolean isLast() {
        return currentPos >= size() - 1;
    }

    public synchronized Optional<T> lookupPrevious() {
        return getCyclic(currentPos <= 0 ? size() - 1 : currentPos - 1);
    }

    public synchronized Optional<T> lookupNext() {
        return getCyclic(currentPos >= size() - 1 ? 0 : currentPos + 1);
    }

    public synchronized Optional<T> getCurrent() {
        return getCyclic(currentPos);
    }

    public synchronized Optional<T> getNext() {
        if(isEmpty()) return Optional.empty();
        if(currentPos >= size() - 1) currentPos = -1;
        currentPos++;
        return getCurrent();
    }

    private synchronized Optional<T> getCyclic(int pos) {
        if(isEmpty()) return Optional.empty();
        return pos < size()
                ? Optional.of(get(pos))
                : Optional.of(get(size() - 1));
    }

}
