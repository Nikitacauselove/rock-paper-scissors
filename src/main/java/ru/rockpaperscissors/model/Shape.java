package ru.rockpaperscissors.model;

import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public enum Shape {
    ROCK,
    PAPER,
    SCISSORS;

    @Override
    public String toString() {
        return name().toLowerCase();
    }

    public static Optional<Shape> from(String string) {
        for (Shape shape : values()) {
            if (shape.name().equalsIgnoreCase(string)) {
                return Optional.of(shape);
            }
        }
        return Optional.empty();
    }
}
