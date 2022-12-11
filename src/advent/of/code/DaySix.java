package advent.of.code;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class DaySix {
    public static class DistinctSequenceFinder {
        int n;

        public DistinctSequenceFinder(int n) {
            this.n = n;
        }

        public int[] getIndices(String signal) {
            return IntStream.range(0, signal.length() - n)
                    .filter(i -> {
                        String nextN = signal.substring(i, i + n);
                        return nextN.length() == nextN.chars().distinct().count();
                    })
                    .map(i -> i + n)
                    .toArray();
        }
    }

    public static void main(String[] args) {
        IntStream.range(1, 3).forEach(partNumber -> {
            try (Stream<String> stringStream = Files.lines(Paths.get(args[0]))) {
                DistinctSequenceFinder sequenceFinder = new DistinctSequenceFinder(partNumber == 1 ? 4 : 14);
                System.out.println("Part " + partNumber + ":");
                stringStream.forEach(line -> System.out.println(sequenceFinder.getIndices(line)[0]));
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        });
    }
}
