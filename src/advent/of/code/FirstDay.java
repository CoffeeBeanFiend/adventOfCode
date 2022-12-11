package advent.of.code;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.OptionalInt;
import java.util.Scanner;
import java.util.stream.IntStream;

public class FirstDay {
    static class IntegerGroupAccumulator implements Iterator<Integer>, Iterable<Integer> {
        protected Scanner sc;
        protected Integer sum;
        protected boolean hasNext;
        public IntegerGroupAccumulator(Scanner sc) {
            this.sc = sc;
            this.sum = 0;
        }

        @Override
        public boolean hasNext() {
            sum = 0;
            hasNext = false;
            while (sc.hasNextLine()) {
                hasNext = true;
                String line = sc.nextLine();
                if (line.matches("\\d+")) {
                    sum += Integer.valueOf(line);
                } else {
                    break;
                }
            }
            return hasNext;
        }

        @Override
        public Integer next() {
            return sum;
        }

        @Override
        public Iterator iterator() {
            return this;
        }
    }

    public static class TopFinder {
        protected Integer[] topValues;

        protected int n;

        public TopFinder(IntegerGroupAccumulator groupAccumulator, int n) {
            this.n = n;
            topValues = new Integer[n];

            for (Integer groupSumValue: groupAccumulator) {
                OptionalInt topPosition = findPositionInTop(groupSumValue);

                if (topPosition.isPresent()) {
                    insertToTopN(topPosition.getAsInt(), groupSumValue);
                }
            }
        }

        private OptionalInt findPositionInTop(int value) {
            return IntStream.range(0, n).filter((i) -> topValues[i] == null || value > topValues[i]).findFirst();
        }

        private void insertToTopN(int topPositionIndex, int value) {
            // Shift existing values following topPositionIndex before inserting new value at topPositionIndex
            IntStream.range(topPositionIndex + 1, n)
                    .map(i -> n + topPositionIndex - i) // Iterate from n to topPositionIndex + 1
                    .forEach((i) -> {
                        topValues[i] = topValues[i - 1];
                    });

            // Insert new sum at topPositionIndex
            topValues[topPositionIndex] = value;
        }

        public int getMaxValue(int n) {
            return topValues[n];
        }

        public int sumOfTopValues() {
            return Arrays.stream(topValues).reduce(
                    0,
                    (a, b) -> a + b
            );
        }

        @Override
        public String toString() {
            return Arrays.stream(topValues).map(String::valueOf).reduce(
                    "",
                    (a, b) -> a + (a.equals("") ? "" : " + ") + b
            ) + " = " + sumOfTopValues();
        }
    }

    public static void main(String[] args) {
        try {
            TopFinder topOneFinder = new TopFinder(
                    new IntegerGroupAccumulator(new Scanner(new File(args[0]))),
                    1
            );
            TopFinder topThreeFinder = new TopFinder(
                    new IntegerGroupAccumulator(new Scanner(new File(args[0]))),
                    3
            );
            System.out.println("Part 1: " + topOneFinder.getMaxValue(0));
            System.out.println("Part 2: " + topThreeFinder);
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }
}
