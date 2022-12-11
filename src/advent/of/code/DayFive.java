package advent.of.code;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DayFive {
    public static class Stacks {
        protected ArrayList<LinkedList<String>> stacks = new ArrayList<>();

        protected Pattern commandPattern = Pattern.compile("move (\\d+) from (\\d+) to (\\d+)");

        protected boolean canMoveMultipleCrates;

        public void setCanMoveMultipleCrates(boolean canMoveMultipleCrates) {
            this.canMoveMultipleCrates = canMoveMultipleCrates;
        }

        public Stacks execute(String command) {
            if (command.contains("[")) {
                handleAddition(command);
            } else if (command.contains("move")) {
                handleMove(command);
            }
            return this;
        }

        public ArrayList<LinkedList<String>> getStacks() {
            return stacks;
        }

        protected void handleAddition(String line) {
            IntStream.range(0, (line.length()) / 4 + 1)
                    .forEach(i -> {
                        if (stacks.size() < i + 1) {
                            stacks.add(new LinkedList());
                        }

                        String c = new String(new char[] {line.charAt(i * 4 + 1)});
                        if (c.matches("[A-Z]")) {
                            this.stacks.get(i).addFirst(c);
                        }
                    });
        }

        protected void handleMove(String command) {
            Matcher matcher = commandPattern.matcher(command);
            if (matcher.find()) {
                int quantity = Integer.valueOf(matcher.group(1));
                int from = Integer.valueOf(matcher.group(2));
                int to = Integer.valueOf(matcher.group(3));
                if (canMoveMultipleCrates) {
                    moveQuantityInBulkFromTo(quantity, from - 1, to - 1);
                } else {
                    moveQuantityFromTo(quantity, from - 1, to - 1);
                }
            }
        }

        protected void moveQuantityFromTo(int quantity, int from, int to) {
            IntStream.range(0, quantity)
                    .forEach(i -> moveFromTo(from, to));
        }

        protected void moveQuantityInBulkFromTo(int quantity, int from, int to) {
            LinkedList<String> toMove = new LinkedList<>();

            IntStream.range(0, quantity)
                    .forEach(i -> toMove.addLast(stacks.get(from).removeLast()));

            IntStream.range(0, quantity)
                    .forEach(i -> stacks.get(to).addLast(toMove.removeLast()));
        }

        protected void moveFromTo(int from, int to) {
            stacks.get(to).addLast(
                    stacks.get(from).removeLast()
            );
        }
    }

    public static void main(String[] args) {
        // Part 1
        try (Stream<String> stringStream = Files.lines(Paths.get(args[0]))) {
            Stacks commandStacks = new Stacks();
            commandStacks.setCanMoveMultipleCrates(false);
            stringStream.forEach(line -> commandStacks.execute(line));
            System.out.print("Part 1: ");
            commandStacks.getStacks().stream()
                    .forEach(stack -> System.out.print(stack.size() > 0 ? stack.getLast() : ""));
            System.out.println();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        // Part 2
        try (Stream<String> stringStream = Files.lines(Paths.get(args[0]))) {
            Stacks commandStacks = new Stacks();
            commandStacks.setCanMoveMultipleCrates(true);
            stringStream.forEach(line -> commandStacks.execute(line));
            System.out.print("Part 2: ");
            commandStacks.getStacks().stream()
                    .forEach(stack -> System.out.print(stack.size() > 0 ? stack.getLast() : ""));
            System.out.println();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
