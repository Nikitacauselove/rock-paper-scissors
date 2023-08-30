package ru.rockpaperscissors.service;

import lombok.extern.slf4j.Slf4j;
import ru.rockpaperscissors.model.Player;
import ru.rockpaperscissors.model.RockPaperScissorsGame;

import java.util.concurrent.*;

@Slf4j
public class MatchmakingService implements AutoCloseable {
    private final BlockingDeque<Player> waitingPlayers = new LinkedBlockingDeque<>();
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    @Override
    public void close() {
        executorService.shutdown();
    }

    private Player takePlayerFromQueue() throws InterruptedException {
        Player player = waitingPlayers.take();

        while (!player.isConnected()) {
            player.close();
            player = waitingPlayers.take();
        }
        return player;
    }

    private void createGame() throws InterruptedException {
        Player firstPlayer = takePlayerFromQueue();
        Player secondPlayer = takePlayerFromQueue();

        try {
            executorService.submit(new RockPaperScissorsGame(firstPlayer, secondPlayer)).get();
            firstPlayer.close();
            secondPlayer.close();
        } catch (CompletionException | ExecutionException | InterruptedException exception) {
            if (!firstPlayer.isClosed()) {
                addPlayer(firstPlayer, true);
            }
            if (!secondPlayer.isClosed()) {
                addPlayer(secondPlayer, true);
            }
        }
    }

    public void addPlayer(Player player, boolean addFirst) {
        if (addFirst) {
            waitingPlayers.addFirst(player);
            player.sendMessage("Your opponent has disconnected. Waiting for an opponent connection...", true);
            log.info("{} added to front of queue", player.getName());
        } else {
            waitingPlayers.add(player);
            player.sendMessage(String.format("Nice to meet you, %s. Waiting for an opponent connection...", player.getName()), true);
            log.info("{} added to queue", player.getName());
        }

        if (2 <= waitingPlayers.size()) {
            try {
                createGame();
            } catch (InterruptedException exception) {
                log.error(exception.getMessage(), exception);
            }
        }
    }
}