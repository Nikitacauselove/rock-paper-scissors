package com.example.rockpaperscissors.model;

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

    private static final String CHOSE_FORMAT = "You chose %s, your opponent chose %s.";

    private Shape getPlayerShape(Player player) throws CompletionException {
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
        firstPlayer.sendMessage(String.format(CHOSE_FORMAT, firstPlayerShape, secondPlayerShape), true);
        secondPlayer.sendMessage(String.format(CHOSE_FORMAT, secondPlayerShape, firstPlayerShape), true);

        if (firstPlayerShape == secondPlayerShape) {
            isTie = true;
        } else {
            isOver = true;
        }

        firstPlayer.sendMessage(firstPlayerShape.getOutcomeMessage(secondPlayerShape), isTie & !isOver);
        secondPlayer.sendMessage(secondPlayerShape.getOutcomeMessage(firstPlayerShape), isTie & !isOver);
    }

    @Override
    public Void call() throws CompletionException, ExecutionException, InterruptedException {
        log.info("Game between {} and {} has started", firstPlayer.getName(), secondPlayer.getName());

        while (!isOver) {
            CompletableFuture<Shape> firstPlayerShape = CompletableFuture.supplyAsync(() -> getPlayerShape(firstPlayer));
            CompletableFuture<Shape> secondPlayerShape = CompletableFuture.supplyAsync(() -> getPlayerShape(secondPlayer));

            start(firstPlayerShape.get(), secondPlayerShape.get());
        }

        log.info("Game between {} and {} is over", firstPlayer.getName(), secondPlayer.getName());
        return null;
    }
}
