package me.zort.containr.util;

import me.zort.containr.Container;
import me.zort.containr.geometry.Region;

public final class Util {

    public static int[] relativeToRealCoords(int[] relative, Container container) {
        Region region = container.getSelection();
        int cornerX = region.xMin();
        int cornerY = region.yMin();
        return new int[] {relative[0] + cornerX, relative[1] + cornerY};
    }

    public static int[] pos(int index, int containerXSize) {
        int row = (int) Math.floor(((double) index) / ((double) containerXSize));
        int column = index % containerXSize;
        return new int[] {column, row};
    }

    public static int pos(int[] loc, int containerXSize) {
        if(loc.length < 2) throw new IndexOutOfBoundsException("Index does not have enough length. Expected 2, not " + loc.length + ".");
        return (loc[1] * containerXSize) + loc[0];
    }

    public static void runCatching(Runnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
