package advent.of.code;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ThirdDay {
    static public class Rucksack {
        protected String[] compartments = new String[] {"", ""};

        public Rucksack(String content) {
            int mid = content.length() / 2;
            compartments[0] = content.substring(0, mid);
            compartments[1] = content.substring(mid);
        }

        public static int getItemPriority(char item) {
            if (item >= 'a' && item <= 'z') {
                return 1 + item - 'a';
            } else if (item >= 'A' && item <= 'Z') {
                return 27 + item - 'A';
            } else {
                throw new RuntimeException("Item " + item + " is not in range a-z or A-Z");
            }
        }

        public Stream<Character> getDuplicateItemsInCompartments() {
            return compartments[0].chars()
                    .mapToObj(c -> (char) c)
                    .filter(item -> compartments[1].indexOf(item) != -1)
                    .distinct();
        }

        public Stream<Character> getUniqueItems() {
            return (compartments[0] + compartments[1]).chars()
                    .mapToObj(c -> (char) c)
                    .distinct();
        }

        @Override
        public String toString() {
            return compartments[0] + " | " + compartments[1];
        }
    }

    static public class RucksackGroups implements Iterator<List<Rucksack>>, Iterable<List<Rucksack>> {
        Iterator<Rucksack> iterator;
        int groupSize;

        public RucksackGroups(Iterator<Rucksack> iterator, int groupSize) {
            this.iterator = iterator;
            this.groupSize = groupSize;
        }

        public boolean hasNext() {
            return iterator.hasNext();
        }

        public List<Rucksack> next() {
            List<Rucksack> groupOfSize = new ArrayList<>(groupSize);
            for (int i = 0; i < groupSize && iterator.hasNext(); i++) {
                groupOfSize.add(iterator.next());
            }
            return groupOfSize;
        }

        public static Stream<Character> getItemsOccurringInAllGroupMembers(List<Rucksack> group) {
            HashMap<Character, Integer> intersection = new HashMap<>();

            for (Rucksack rucksack: group) {
                rucksack.getUniqueItems().forEach((item) -> {
                    if (intersection.containsKey(item)) {
                        intersection.put(item, intersection.get(item) + 1);
                    } else {
                        intersection.put(item, 1);
                    }
                });
            }

            return intersection.keySet().stream().filter(item -> intersection.get(item) == group.size());
        }

        @Override
        public Iterator<List<Rucksack>> iterator() {
            return this;
        }
    }

    public static void main(String[] args) {
        try {
            int partOneSum = Files.lines(Paths.get(args[0]))
                    .map(Rucksack::new)
                    .map(Rucksack::getDuplicateItemsInCompartments)
                    .map(dupes -> dupes.map(Rucksack::getItemPriority).reduce(0, (a, b) -> a + b))
                    .reduce(0, (a, b) -> a + b);

            System.out.println("Part 1: " + partOneSum);


            Iterator<Rucksack> rucksackIterator = Files.lines(Paths.get(args[0])).map(Rucksack::new).iterator();
            RucksackGroups groups = new RucksackGroups(rucksackIterator, 3);

            Stream<List<Rucksack>> groupsStream = StreamSupport.stream(
                    Spliterators.spliteratorUnknownSize(groups, Spliterator.ORDERED),
                    false
            );

            int partTwoSunV2 = groupsStream
                    .map(RucksackGroups::getItemsOccurringInAllGroupMembers)
                    .map(items -> items.map(Rucksack::getItemPriority).reduce(0, (a, b) -> a + b))
                    .reduce(0, (a, b) -> a + b);

            System.out.println("Part 2: " + partTwoSunV2);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
