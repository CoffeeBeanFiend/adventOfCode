package advent.of.code;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class DayTen {
    public static class CPU {
        protected int register = 1;

        protected ArrayList<Integer> registerHistory = new ArrayList<>();

        CPU() {
            registerHistory.add(register);
        }

        public CPU executeNoop() {
            nextCycle();
            return this;
        }

        public CPU executeAddX(int x) {
            nextCycle();
            nextCycle();
            register += x;
            return this;
        }

        public ArrayList<Integer> getSignalStrength() {
            ArrayList<Integer> signalStrength = new ArrayList<>();
            IntStream.range(0, registerHistory.size())
                    .forEach(i -> {
                        if ((i - 20) % 40 == 0) {
                            int cycle = i;
                            int registerValue = registerHistory.get(cycle);
                            signalStrength.add(cycle * registerValue);
                        }
                    });

            return signalStrength;
        };

        public CPU outputScreenPixels() {
            ArrayList<String> pixels = new ArrayList<>();
            IntStream.range(1, registerHistory.size())
                    .forEach(i -> {
                        int cycle = i;
                        int registerValue = registerHistory.get(cycle);

                        int rowPos = (i - 1) % 40;

                        if (rowPos == 0) {
                            System.out.print("\n");
                        }

                        if (registerValue >= rowPos - 1 && registerValue <= rowPos + 1) {
                            System.out.print("##");
                        } else {
                            System.out.print("..");
                        }

                    });
            return this;
        }

        protected CPU nextCycle() {
            registerHistory.add(register);
            return this;
        }
    }

    public static Pattern addxPattern = Pattern.compile("(addx).(\\d+|-\\d+)");

    public static void main(String[] args) {
        String inputFilePath = args[0];
        try (Stream<String> stringStream = Files.lines(Paths.get(inputFilePath))) {
            CPU cpu = new CPU();
            stringStream.forEachOrdered(line -> {
                Matcher addXMatcher = addxPattern.matcher(line);

                if (addXMatcher.find()) {
                    int x = Integer.valueOf(addXMatcher.group(2));
                    cpu.executeAddX(x);
                } else if (line.equals("noop")) {
                    cpu.executeNoop();
                } else {
                    throw new RuntimeException("Invalid line in input: " + line);
                }
            });
            cpu.getSignalStrength();
            System.out.println("Part 1:" + cpu.getSignalStrength().stream().mapToInt(i -> i).sum());
            System.out.println("Part 2:");
            System.out.println();
            cpu.outputScreenPixels();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
