package advent.of.code;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DayNine {
    public static class RopeTailRecorder {
        protected Set<String> pointsOccupied = new HashSet<>();

        public RopeTailRecorder logTailState(Knot knot) {
            pointsOccupied.add(knot.getXOfTail() + "," + knot.getYOfTail());
            return this;
        }

        public int getPositionsCovered() {
            return pointsOccupied.size();
        }
    }

    public static class Knot {
        protected int headX = 0;
        protected int headY = 0;
        protected int tailX = 0;
        protected int tailY = 0;

        public void adjustTail() {
            int diffX = headX - tailX;
            int diffY = headY - tailY;

            if (diffY*diffY > 1 && diffX*diffX == 1) {
                tailX += diffX;
            }

            if (diffX*diffX > 1 && diffY*diffY == 1) {
                tailY += diffY;
            }

            if (diffX == 2) {
                tailX++;
            } else if (diffX == -2) {
                tailX--;
            } else if (diffX*diffX > 2) {
                throw new RuntimeException("Invalid Knot state: " + this);
            }

            if (diffY == 2) {
                tailY++;
            } else if (diffY == -2) {
                tailY--;
            } else if (diffY*diffY > 2){
                throw new RuntimeException("Invalid Knot state: " + this);
            }
        }

        public Knot() {

        }

        public Knot(int headX, int headY, int tailX, int tailY) {
            this.headX = headX;
            this.headY = headY;
            this.tailX = tailX;
            this.tailY = tailY;
        }

        public Knot moveUp() {
            headY--;
            adjustTail();
            return this;
        }

        public Knot moveDown() {
            headY++;
            adjustTail();
            return this;
        }

        public Knot moveLeft() {
            headX--;
            adjustTail();
            return this;
        }

        public Knot moveRight() {
            headX++;
            adjustTail();
            return this;
        }

        public int getXOfHead() {
            return headX;
        }

        public int getYOfHead() {
            return headY;
        }

        public int getXOfTail() {
            return tailX;
        }

        public int getYOfTail() {
            return tailY;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            int minX = Math.min(headX, tailX);
            int minY = Math.min(headY, tailY);
            int maxX = Math.max(headX, tailX);
            int maxY = Math.max(headY, tailY);
            int width = 2 + (maxX - minX);
            int height = 2 + (maxY - minY);

            IntStream.range(0, width*height).forEach(idx -> {
                int x = idx % width;
                int y = (idx - x) / width;

                if (x == 0) {
                    sb.append("\n");
                }

                if (headX == x + minX && headY == y + minY) {
                    sb.append("H");
                } else if (tailX == x + minX && tailY == y + minY) {
                    sb.append("T");
                } else {
                    sb.append(".");
                }
            });

            sb.append("\n");

            return sb.toString();
        }
    }

    public static class Rope {
        Integer[] knotsX;
        Integer[] knotsY;

        int length;
        public Rope(int length) {
            this.length = length;
            knotsX = Collections.nCopies(length, 0).toArray(new Integer[0]);
            knotsY = Collections.nCopies(length, 0).toArray(new Integer[0]);
        }

        public Rope moveUp() {
            knotsY[0]--;
            adjustRope();
            return this;
        }

        public Rope moveDown() {
            knotsY[0]++;
            adjustRope();
            return this;
        }

        public Rope moveLeft() {
            knotsX[0]--;
            adjustRope();
            return this;
        }

        public Rope moveRight() {
            knotsX[0]++;
            adjustRope();
            return this;
        }

        public Knot getLastKnot() {
            int lastIdx = length - 1;
            return new Knot(
                    knotsX[lastIdx - 1],
                    knotsY[lastIdx - 1],
                    knotsX[lastIdx],
                    knotsY[lastIdx]
            );
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();

            int minX = Arrays.stream(knotsX).mapToInt(i -> i).min().getAsInt();
            int minY = Arrays.stream(knotsY).mapToInt(i -> i).min().getAsInt();
            int maxX = Arrays.stream(knotsX).mapToInt(i -> i).max().getAsInt();
            int maxY = Arrays.stream(knotsY).mapToInt(i -> i).max().getAsInt();

            int width = (maxX - minX) < 10 ? 10 : (maxX - minX);
            int height = (maxY - minY) < 10 ? 10 : (maxY - minY);

            IntStream.range(0, width*height).forEach(idx -> {
                int x = idx % width;
                int y = (idx - x) / width;

                if (x == 0) {
                    sb.append("\n");
                }

                OptionalInt knotAtXY = IntStream.range(0, length)
                        .filter(i -> knotsX[i] == x + minX && knotsY[i] == y + minY)
                        .findFirst();

                if (knotAtXY.isPresent()) {
                    int knotN = knotAtXY.getAsInt();
                    sb.append(knotN == 0 ? "H" : knotN);
                } else {
                    sb.append(".");
                }
            });

            sb.append("\n");

            return sb.toString();
        }

        protected Rope adjustRope() {
            IntStream.range(1, length).forEach(i -> {
                Knot knot = new Knot(
                        knotsX[i - 1],
                        knotsY[i - 1],
                        knotsX[i],
                        knotsY[i]
                );

                knot.adjustTail();

                knotsX[i] = knot.getXOfTail();
                knotsY[i] = knot.getYOfTail();
            });
            return this;
        }
    }

    public static Pattern movePattern = Pattern.compile("(U|D|L|R).(\\d+)");

    public static void partOne(String inputFilePath) {
        RopeTailRecorder headTailRecorder = new RopeTailRecorder();
        Knot knot = new Knot();

        try (Stream<String> stringStream = Files.lines(Paths.get(inputFilePath))) {
            stringStream.forEach(line -> {
                System.out.println(line);
                Matcher matcher = movePattern.matcher(line);
                headTailRecorder.logTailState(knot);
                if (matcher.find()) {
                    String direction = matcher.group(1);
                    int unitsToMove = Integer.valueOf(matcher.group(2));
                    switch(direction) {
                        case "U":
                            IntStream.range(0, unitsToMove).forEach(i -> {
                                knot.moveUp();
                                headTailRecorder.logTailState(knot);
                            });
                            break;
                        case "D":
                            IntStream.range(0, unitsToMove).forEach(i -> {
                                knot.moveDown();
                                headTailRecorder.logTailState(knot);
                            });
                            break;
                        case "L":
                            IntStream.range(0, unitsToMove).forEach(i -> {
                                knot.moveLeft();
                                headTailRecorder.logTailState(knot);
                            });
                            break;
                        case "R":
                            IntStream.range(0, unitsToMove).forEach(i -> {
                                knot.moveRight();
                                headTailRecorder.logTailState(knot);
                            });
                            break;
                        default:
                            throw new RuntimeException("Unexpected input from file: " + line);
                    }
                }
                System.out.println(knot);
            });

            System.out.println("Part 1:" + headTailRecorder.getPositionsCovered());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void partTwo(String inputFilePath) {
        RopeTailRecorder headTailRecorder = new RopeTailRecorder();
        Rope rope = new Rope(10);

        try (Stream<String> stringStream = Files.lines(Paths.get(inputFilePath))) {
            stringStream.forEach(line -> {
                Matcher matcher = movePattern.matcher(line);
                System.out.println(line);
                headTailRecorder.logTailState(rope.getLastKnot());
                if (matcher.find()) {
                    String direction = matcher.group(1);
                    int unitsToMove = Integer.valueOf(matcher.group(2));
                    switch(direction) {
                        case "U":
                            IntStream.range(0, unitsToMove).forEach(i -> {
                                rope.moveUp();
                                headTailRecorder.logTailState(rope.getLastKnot());
                            });
                            break;
                        case "D":
                            IntStream.range(0, unitsToMove).forEach(i -> {
                                rope.moveDown();
                                headTailRecorder.logTailState(rope.getLastKnot());
                            });
                            break;
                        case "L":
                            IntStream.range(0, unitsToMove).forEach(i -> {
                                rope.moveLeft();
                                headTailRecorder.logTailState(rope.getLastKnot());
                            });
                            break;
                        case "R":
                            IntStream.range(0, unitsToMove).forEach(i -> {
                                rope.moveRight();
                                headTailRecorder.logTailState(rope.getLastKnot());
                            });
                            break;
                        default:
                            throw new RuntimeException("Unexpected input from file: " + line);
                    }
                }
                System.out.println(rope);
            });

            System.out.println("Part 2:" + headTailRecorder.getPositionsCovered());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args) {
        System.out.println("Running Part 1");
        partOne(args[0]);
        System.out.println("End Part 1");

        System.out.println("Running Part 2");
        partTwo(args[0]);
        System.out.println("End Part 1");
    }
}
