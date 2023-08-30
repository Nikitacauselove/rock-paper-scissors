package ru.rockpaperscissors.model;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Shape {
    ROCK("rock"),
    PAPER("paper"),
    SCISSORS("scissors");

    private final String name;

    @Override
    public String toString() {
        return name;
    }

    public static boolean isValid(String string) {
        for (Shape shape : Shape.values()) {
            if (shape.name.equals(string)) {
                return true;
            }
        }
        return false;
    }
}
