package advent.of.code;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.OptionalInt;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class DayEight {
    public static class Forrest {
        protected ArrayList<Integer> treeMap = new ArrayList<>();
        int widthOfForest = 0;

        public Forrest addRow(String row) {
            if (widthOfForest != 0 && row.length() != widthOfForest) {
                throw new RuntimeException("Faulty input, each line must be of the same length");
            }
            widthOfForest = row.length();
            row.chars().forEach(treeMap::add);
            return this;
        }

        public int getTreeHeightAt(int x, int y) {
            final int idx = y*widthOfForest + x;
            return treeMap.get(idx);
        }

        public int getWidth() {
            return widthOfForest;
        }

        public int getHeight() {
            return treeMap.size() / widthOfForest;
        }
    }

    public static class ForrestMap<T> {
        protected Forrest forrest;

        protected ArrayList<T> map;

        public ForrestMap(Forrest forrest, T initValue) {
            this.forrest = forrest;
            final int width = forrest.getHeight();
            final int height = forrest.getHeight();
            map = new ArrayList<>();
            IntStream.range(0, width*height).forEach(i -> map.add(initValue));
        }

        public int getIndexOfCoordinate(int x, int y) {
            return y*forrest.getWidth() + x;
        }

        public int[] getCoordinateOfIndex(int idx) {
            final int x = idx % forrest.getWidth();
            final int y = (idx - x) / forrest.getWidth();
            return new int[] {x, y};
        }

        protected ForrestMap<T> setAt(int x, int y, T value) {
            final int idx = getIndexOfCoordinate(x, y);
            map.set(idx, value);
            return this;
        }
    }

    public static class VisibilityMap extends ForrestMap<Boolean> {
        protected int currentMaxima = Integer.MIN_VALUE;

        public VisibilityMap(Forrest forrest) {
            super(forrest, false);
            final int width = forrest.getHeight();
            final int height = forrest.getHeight();
            IntStream.range(0, height)
                    .forEach(y -> {
                        fillRow(y, true);
                        fillRow(y, false);
                    });
            IntStream.range(0, width)
                    .forEach(x -> {
                        fillCol(x, true);
                        fillCol(x, false);
                    });
        }

        protected void fillRow(int y, boolean forward) {
            final int width = forrest.getWidth();
            currentMaxima = Integer.MIN_VALUE;
            IntStream.range(0, forrest.getWidth())
                    .map(x -> forward ? x : width - 1 - x)
                    .forEach(x -> {
                        final int currentHeight = forrest.getTreeHeightAt(y, x);
                        if (currentHeight > currentMaxima) {
                            setAt(x, y, true);
                            currentMaxima = currentHeight;
                        }
                    });
        }

        protected void fillCol(int x, boolean forward) {
            final int height = forrest.getHeight();
            currentMaxima = Integer.MIN_VALUE;
            IntStream.range(0, forrest.getWidth())
                    .map(y -> forward ? y : height - 1 - y)
                    .forEach(y -> {
                        final int currentHeight = forrest.getTreeHeightAt(y, x);
                        if (currentHeight > currentMaxima) {
                            setAt(x, y, true);
                            currentMaxima = currentHeight;
                        }
                    });
        }

        @Override
        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            IntStream.range(0, map.size())
                    .forEach(idx -> {
                        if (idx % (forrest.getWidth()) == 0) {
                            stringBuilder.append("\n");
                        }
                        stringBuilder.append(map.get(idx) ? '1' : '0');
                    });
            return stringBuilder.toString();
        }

        public int countVisibleTrees() {
            return IntStream.range(0, map.size())
                    .mapToObj(i -> map.get(i))
                    .mapToInt(v -> v ? 1 : 0)
                    .sum();
        }
    }

    public static class ScenicScoreMap extends ForrestMap<Integer> {
        public ScenicScoreMap(Forrest forrest) {
            super(forrest, 0);
            IntStream.range(0, map.size())
                    .forEach(idx -> {
                        final int[] xy = getCoordinateOfIndex(idx);
                        final int scenicScore = calculateScenicScoreAt(xy[0], xy[1]);
                        setAt(xy[0], xy[1], scenicScore);
                    });
        }

        protected int calculateScenicScoreAt(int x, int y) {
            final int currentHeight = forrest.getTreeHeightAt(x, y);

            OptionalInt down = IntStream.range(y + 1, forrest.getHeight())
                    .filter(yp -> forrest.getTreeHeightAt(x, yp) >= currentHeight)
                    .findFirst();

            OptionalInt right = IntStream.range(x + 1, forrest.getWidth())
                    .filter(xp -> forrest.getTreeHeightAt(xp, y) >= currentHeight)
                    .findFirst();

            OptionalInt up = IntStream.range(1, y)
                    .map(yp -> y - yp)
                    .filter(yp -> forrest.getTreeHeightAt(x, yp) >= currentHeight)
                    .findFirst();

            OptionalInt left = IntStream.range(1, x)
                    .map(xp -> x - xp)
                    .filter(xp -> forrest.getTreeHeightAt(xp, y) >= currentHeight)
                    .findFirst();

            final int downLength = down.isEmpty() ? forrest.getHeight() - y - 1: down.getAsInt() - y;
            final int rightLength = right.isEmpty() ? forrest.getWidth() - x - 1 : right.getAsInt() - x;
            final int upLength = up.isEmpty() ? y : y - up.getAsInt();
            final int leftLength = left.isEmpty() ? x : x - left.getAsInt();

            return downLength * rightLength * upLength * leftLength;
        }

        @Override
        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            IntStream.range(0, map.size()).forEach(idx -> {
                if ((idx % forrest.getWidth()) == 0) {
                    stringBuilder.append("\n");
                }
                stringBuilder.append(String.format("%06d ", map.get(idx)));
            });
            return stringBuilder.toString();
        }

        public int getMaxScenicScore() {
            return map.stream().mapToInt(i -> (int)i).max().getAsInt();
        }
    }

    public static void main(String[] args) {
        try (Stream<String> stringStream = Files.lines(Paths.get(args[0]))) {
            Forrest forrest = new Forrest();
            stringStream.forEach(forrest::addRow);

            // Part 1
            VisibilityMap visibilityMap = new VisibilityMap(forrest);
            // Uncomment below to output the visibility map for sanity check
            // System.out.println(visibilityMap);
            System.out.println("Part 1: " + visibilityMap.countVisibleTrees());

            // Part 2
            ScenicScoreMap scoreMap = new ScenicScoreMap(forrest);
            // Uncomment below output the score map for sanity check
            // System.out.println(scoreMap);
            System.out.println("Part 2: " + scoreMap.getMaxScenicScore());

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
