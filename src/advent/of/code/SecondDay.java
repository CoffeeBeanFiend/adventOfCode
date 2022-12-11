package advent.of.code;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SecondDay {
    public static class Game {
        int opponentsMove;
        int yourMove;

        public Game(String opponentsMove, String yourMove) {
            if (opponentsMove.length() != 1) {
                throw new RuntimeException("opponentsMove should be a String of length 1");
            }

            if (yourMove.length() != 1) {
                throw new RuntimeException("yourMove should be a String of length 1");
            }

            int opponentsMoveAsInt = 1 + opponentsMove.charAt(0) - 'A';
            int yourMoveAsInt = 1 + yourMove.charAt(0) - 'X';

            if (opponentsMoveAsInt < 1 || opponentsMoveAsInt > 3) {
                throw new RuntimeException("opponentsMove should be a A, B or C, not " + opponentsMove);
            }

            if (yourMoveAsInt < 1 || yourMoveAsInt > 3) {
                throw new RuntimeException("yourMove should be a X, Y or Z, not " + yourMove);
            }

            this.opponentsMove = opponentsMoveAsInt;
            this.yourMove = yourMoveAsInt;
        }

        public boolean isDraw() {
            return yourMove == opponentsMove;
        }

        public boolean isYourWin() {
            return yourMove == (opponentsMove % 3) + 1;
        }

        public void setYourMoveAsExpected() {
            switch(yourMove) {
                case 1: // Move to lose
                    yourMove = ((opponentsMove + 1) % 3) + 1;
                    break;
                case 2: // Move to draw
                    yourMove = opponentsMove;
                    break;
                case 3: // Move to win
                    yourMove = (opponentsMove % 3) + 1;
                    break;
            }
        }

        public Integer getYourScore() {
            int shapeScore = yourMove;
            int outcomeScore = isYourWin() ? 6 : isDraw() ? 3 : 0;
            return shapeScore + outcomeScore;
        }
    }

    public static void main(String[] args) {
        try {
            int totalScorePart1 = Files.lines(Paths.get(args[0]))
                    .map(line -> line.split("\\s"))
                    .map(moves -> new Game(moves[0], moves[1]))
                    .map(Game::getYourScore)
                    .reduce(0, (a, b) -> a + b);

            System.out.println("Part 1: " + totalScorePart1);

            int totalScorePart2 = Files.lines(Paths.get(args[0]))
                    .map(line -> line.split("\\s"))
                    .map(moves -> new Game(moves[0], moves[1]))
                    .map(game -> {
                        game.setYourMoveAsExpected();
                        return game.getYourScore();
                    })
                    .reduce(0, (a, b) -> a + b);

            System.out.println("Part 2: " + totalScorePart2);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
