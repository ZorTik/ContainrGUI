package me.zort.containr.geometry;

import lombok.AllArgsConstructor;
import lombok.Data;
import me.zort.containr.internal.util.Pair;

import java.util.function.ToIntFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@AllArgsConstructor
@Data
public class Tetragon implements Cloneable {

    private Pair<Integer, Integer> corner1;
    private Pair<Integer, Integer> corner2;

    public boolean fitsInOtherWithOffset(int xOffset, int yOffset, int xLength, int yLength) {
        return xOffset + xSideSize() <= xLength && yOffset + ySideSize() <= yLength;
    }

    public boolean fitsInOtherWithOffsetX(int xOffset, int xLength) {
        return xOffset + xSideSize() <= xLength;
    }

    public boolean fitsInOtherWithOffsetY(int yOffset, int yLength) {
        return yOffset + ySideSize() <= yLength;
    }

    public boolean fitsInOtherByX(int xLength) {
        return xSideSize() <= xLength;
    }

    public boolean fitsInOtherByY(int yLength) {
        return ySideSize() <= yLength;
    }

    public void alignTopInRow(Tetragon tetragon) {
        moveByTopY(tetragon.yMax() + 1);
    }

    public void alignLeftInRow(Tetragon tetragon) {
        moveByLeftX(tetragon.xMax() + 1);
    }

    public void alignTopBy(Tetragon tetragon) {
        moveByTopY(tetragon.yMin());
    }

    public void alignLeftBy(Tetragon tetragon) {
        moveByLeftX(tetragon.xMin());
    }

    public void moveByTopY(int topY) {
        int difference = difference(yMin(), topY);
        final Pair<Integer, Integer> yMaxCorner = yMaxCorner() == yMinCorner() ? corner1 : yMaxCorner();
        final Pair<Integer, Integer> yMinCorner = yMaxCorner() == yMinCorner() ? corner2 : yMinCorner();
        yMaxCorner.setValue(yMaxCorner.getValue() + difference);
        yMinCorner.setValue(yMinCorner.getValue() + difference);
    }

    public void moveByLeftX(int leftX) {
        int difference = difference(xMin(), leftX);
        final Pair<Integer, Integer> xMaxCorner = xMaxCorner() == xMinCorner() ? corner1 : xMaxCorner();
        final Pair<Integer, Integer> xMinCorner = xMaxCorner() == xMinCorner() ? corner2 : xMinCorner();
        xMaxCorner.setKey(xMaxCorner.getKey() + difference);
        xMinCorner.setKey(xMinCorner.getKey() + difference);
    }

    public boolean collidesWith(Tetragon tetragon) {
        int xMin = xMin();
        int yMin = yMin();
        int xSide = xSideSize();
        int ySide = ySideSize();
        int x1 = tetragon.getCorner1().getKey();
        int x2 = tetragon.getCorner2().getKey();
        int y1 = tetragon.getCorner1().getValue();
        int y2 = tetragon.getCorner2().getValue();
        return
                ((x1 >= xMin && x1 < xMin + xSide) ||
                (x2 >= xMin && x2 < xMin + xSide)) &&
                        ((y1 >= yMin && y1 < yMin + ySide) ||
                        (y2 >= yMin && y2 < yMin + ySide));
    }

    public boolean contains(int[] coords) {
        int x = coords[0];
        int y = coords[1];
        return x >= xMin() && x <= xMax() && y >= yMin() && y <= yMax();
    }

    public boolean isInside(Tetragon tetragon) {
        int[] c = getExceedingCoords(tetragon);
        return c[0] == 0 && c[1] == 0 && c[2] == 0 && c[3] == 0;
    }

    public IntStream getAllIndexes(ToIntFunction<int[]> toIndexFunction) {
        IntStream[] streams = new IntStream[ySideSize()];
        final int yMin = yMin();
        final int xSideSize = xSideSize();
        final int xMin = xMin();
        for(int i = yMin; i <= yMax(); i++) {
            streams[i - yMin] = IntStream.rangeClosed(toIndexFunction.applyAsInt(new int[] {xMin, i}), toIndexFunction.applyAsInt(new int[] {xMin + xSideSize, i}));
        }
        return Stream.of(streams).flatMapToInt(i -> i);
    }

    public int[] getExceedingCoords(Tetragon tetragon) {
        int[] result = new int[4];
        int[] xExceeding = getExceedingXCoord(tetragon);
        result[0] = xExceeding[0];
        result[1] = xExceeding[1];
        int[] yExceeding = getExceedingXCoord(tetragon);
        result[2] = yExceeding[0];
        result[3] = yExceeding[1];
        return result;
    }

    public int[] getExceedingXCoord(Tetragon tetragon) {
        int[] result = new int[2];
        int xMinDifference = difference(tetragon.xMin(), xMin());
        if(xMinDifference > 0) xMinDifference = 0;
        int xMaxDifference = difference(tetragon.xMax(), xMax());
        if(xMaxDifference < 0) xMaxDifference = 0;
        result[0] = xMinDifference;
        result[1] = xMaxDifference;
        return result;
    }

    public int[] getExceedingYCoord(Tetragon tetragon) {
        int[] result = new int[2];
        int yMinDifference = difference(tetragon.yMin(), yMin());
        if(yMinDifference > 0) yMinDifference = 0;
        int yMaxDifference = difference(tetragon.yMax(), yMax());
        if(yMaxDifference < 0) yMaxDifference = 0;
        result[0] = yMinDifference;
        result[1] = yMaxDifference;
        return result;
    }

    public int perimeter() {
        return 2 * (xSideSize() + ySideSize());
    }

    public int content() {
        return xSideSize() * ySideSize();
    }

    public int size() {
        return content();
    }

    public int xSideSize() {
        return (Math.max(corner1.getKey(), corner2.getKey()) - Math.min(corner1.getKey(), corner2.getKey())) + 1;
    }

    public int ySideSize() {
        return (Math.max(corner1.getValue(), corner2.getValue()) - Math.min(corner1.getValue(), corner2.getValue())) + 1;
    }

    public Pair<Integer, Integer> xMaxCorner() {
        int xMax = xMax();
        return corner1.getKey() == xMax ? corner1 : corner2;
    }

    public Pair<Integer, Integer> xMinCorner() {
        int xMin = xMin();
        return corner1.getKey() == xMin ? corner1 : corner2;
    }

    public Pair<Integer, Integer> yMaxCorner() {
        int yMax = yMax();
        return corner1.getValue() == yMax ? corner1 : corner2;
    }

    public Pair<Integer, Integer> yMinCorner() {
        int yMin = yMin();
        return corner1.getValue() == yMin ? corner1 : corner2;
    }

    public int xMax() {
        return Math.max(corner1.getKey(), corner2.getKey());
    }

    public int xMin() {
        return Math.min(corner1.getKey(), corner2.getKey());
    }

    public int yMax() {
        return Math.max(corner1.getValue(), corner2.getValue());
    }

    public int yMin() {
        return Math.min(corner1.getValue(), corner2.getValue());
    }

    private int difference(int current, int target) {
        return Integer.compare(target, current) * (Math.max(target, current) - Math.min(target, current));
    }

    public Tetragon clone() {
        return clone(corner1, corner2);
    }

    public Tetragon clone(Pair<Integer, Integer> corner1, Pair<Integer, Integer> corner2) {
        return new Tetragon(corner1, corner2);
    }

}
