package advent.of.code;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class DayFour {
    public static class Range {
        protected int start;
        protected int end;

        public Range(int start, int end) {
            this.start = start;
            this.end = end;
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }

        public boolean contains(Range range) {
            return this.getStart() >= range.getStart()
                    && this.getEnd() <= range.getEnd();
        }

        public boolean overlaps(Range range) {
            return this.getStart() >= range.getStart() && this.getStart() <= range.getEnd()
                    || this.getEnd() >= range.getStart() && this.getEnd() <= range.getEnd();
        }
    }

    public static class RangePair {
        Range rangeA;
        Range rangeB;

        public RangePair(Range rangeA, Range rangeB) {
            this.rangeA = rangeA;
            this.rangeB = rangeB;
        }

        public boolean hasFullOverlap() {
            return rangeA.contains(rangeB) || rangeB.contains(rangeA);
        }

        public boolean hasOverlap() {
            return rangeA.overlaps(rangeB) || rangeB.overlaps(rangeA);
        }
    }

    public static void main(String[] args) {
        try (Stream<String> stringStream = Files.lines(Paths.get(args[0]))) {
            int fullOverlapCount = (int) stringStream
                    .map(line -> line.split(","))
                    .map(ranges -> {
                        String[] rangeA = ranges[0].split("-");
                        String[] rangeB = ranges[1].split("-");
                        return new RangePair(
                                new Range(Integer.valueOf(rangeA[0]), Integer.valueOf(rangeA[1])),
                                new Range(Integer.valueOf(rangeB[0]), Integer.valueOf(rangeB[1]))
                        );
                    })
                    .filter(pair -> pair.hasFullOverlap())
                    .count();
            System.out.println("Part 1: " + fullOverlapCount);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        try (Stream<String> stringStream = Files.lines(Paths.get(args[0]))) {
            int partialOverlapCount = (int) stringStream
                    .map(line -> line.split(","))
                    .map(ranges -> {
                        String[] rangeA = ranges[0].split("-");
                        String[] rangeB = ranges[1].split("-");
                        return new RangePair(
                                new Range(Integer.valueOf(rangeA[0]), Integer.valueOf(rangeA[1])),
                                new Range(Integer.valueOf(rangeB[0]), Integer.valueOf(rangeB[1]))
                        );
                    })
                    .filter(pair -> pair.hasOverlap())
                    .count();
            System.out.println("Part 2: " + partialOverlapCount);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
