package ru.rockpaperscissors.model;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

@RequiredArgsConstructor
@Slf4j
public class Game implements Callable<Void> {
    private final Player firstPlayer;
    private final Player secondPlayer;
    private boolean isOver = false;
    private boolean isTie = false;

    private void broadcastOutcome(String message, Player winner, Player looser) {
        winner.sendMessage(String.format("%s You win!", message), false);
        looser.sendMessage(String.format("%s You lose!", message), false);
    }

    private Shape getShape(Player player) throws CompletionException {
        try {
            player.sendMessage("Make your hand shape. Type rock, paper or scissors:", !isTie);
            Optional<Shape> playerShape = Shape.from(player.getMessage());

            while (playerShape.isEmpty()) {
                player.sendMessage("Invalid hand shape. Type rock, paper or scissors:", true);
                playerShape = Shape.from(player.getMessage());
            }
            return playerShape.get();
        } catch (IOException exception) {
            player.close();
            throw new CompletionException(exception);
        }
    }

    private void start(Shape firstPlayerShape, Shape secondPlayerShape) {
        firstPlayer.sendMessage(String.format("You chose %s, your opponent chose %s.", firstPlayerShape, secondPlayerShape), true);
        secondPlayer.sendMessage(String.format("You chose %s, your opponent chose %s.", secondPlayerShape, firstPlayerShape), true);

        if (firstPlayerShape == secondPlayerShape) {
            firstPlayer.sendMessage(String.format("Both players selected %s. It's a tie!", firstPlayerShape), false);
            secondPlayer.sendMessage(String.format("Both players selected %s. It's a tie!", secondPlayerShape), false);
            isTie = true;
        } else if (firstPlayerShape == Shape.ROCK) {
            if (secondPlayerShape == Shape.SCISSORS) {
                broadcastOutcome("Rock smashes scissors!", firstPlayer, secondPlayer);
            } else {
                broadcastOutcome("Paper covers rock!", secondPlayer, firstPlayer);
            }
            isOver = true;
        } else if (firstPlayerShape == Shape.PAPER) {
            if (secondPlayerShape == Shape.ROCK) {
                broadcastOutcome("Paper covers rock!", firstPlayer, secondPlayer);
            } else {
                broadcastOutcome("Scissors cuts paper!", secondPlayer, firstPlayer);
            }
            isOver = true;
        } else if (firstPlayerShape == Shape.SCISSORS) {
            if (secondPlayerShape == Shape.PAPER) {
                broadcastOutcome("Scissors cuts paper!", firstPlayer, secondPlayer);
            } else {
                broadcastOutcome("Rock smashes scissors!", secondPlayer, firstPlayer);
            }
            isOver = true;
        }
    }

    @Override
    public Void call() throws CompletionException, ExecutionException, InterruptedException {
        log.info("Game between {} and {} has started", firstPlayer.getName(), secondPlayer.getName());

        while (!isOver) {
            CompletableFuture<Shape> firstPlayerShape = CompletableFuture.supplyAsync(() -> getShape(firstPlayer));
            CompletableFuture<Shape> secondPlayerShape = CompletableFuture.supplyAsync(() -> getShape(secondPlayer));

            start(firstPlayerShape.get(), secondPlayerShape.get());
        }

        log.info("Game between {} and {} is over", firstPlayer.getName(), secondPlayer.getName());
        return null;
    }
}
