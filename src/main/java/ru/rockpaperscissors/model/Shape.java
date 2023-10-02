package ru.rockpaperscissors.model;

import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public enum Shape {
    ROCK,
    PAPER,
    SCISSORS;

    public static Optional<Shape> from(String string) {
        for (Shape shape : values()) {
            if (shape.name().equalsIgnoreCase(string)) {
                return Optional.of(shape);
            }
        }
        return Optional.empty();
    }

    @Override
    public String toString() {
        return name().toLowerCase();
    }

    public String getOutcomeMessage(Shape opponentShape) {
        Collection<Shape> shapes = List.of(this, opponentShape);
        String outcome = getShortOutcomeMessage(opponentShape);

        if (shapes.containsAll(List.of(ROCK, PAPER))) {
            return String.format("Paper covers rock! %s", outcome);
        }
        if (shapes.containsAll(List.of(ROCK, SCISSORS))) {
            return String.format("Rock smashes scissors! %s", outcome);
        }
        if (shapes.containsAll(List.of(PAPER, SCISSORS))) {
            return String.format("Scissors cuts paper! %s", outcome);
        }
        return String.format("Both players selected %s. %s", this, outcome);
    }

    public String getShortOutcomeMessage(Shape opponentShape) {
        switch (this) {
            case ROCK:
                if (opponentShape == PAPER) {
                    return "You lose.";
                }
                if (opponentShape == SCISSORS) {
                    return "You win!";
                }
                break;
            case PAPER:
                if (opponentShape == ROCK) {
                    return "You win!";
                }
                if (opponentShape == SCISSORS) {
                    return "You lose.";
                }
                break;
            case SCISSORS:
                if (opponentShape == ROCK) {
                    return "You lose.";
                }
                if (opponentShape == PAPER) {
                    return "You win!";
                }
                break;
        }
        return "It's a tie!";
    }
}
