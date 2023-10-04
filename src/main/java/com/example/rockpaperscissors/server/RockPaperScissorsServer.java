package com.example.rockpaperscissors.server;

import com.example.rockpaperscissors.model.Player;
import com.example.rockpaperscissors.service.MatchmakingService;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class RockPaperScissorsServer {
    public static void run(int port) {
        try (MatchmakingService matchmakingService = new MatchmakingService(); ServerSocket serverSocket = new ServerSocket(port)) {
            log.info("Server started on port: {}", port);

            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                Player player = new Player(socket);

                CompletableFuture.runAsync(() -> {
                    try {
                        player.chooseName();
                        matchmakingService.addPlayer(player, false);
                    } catch (IOException exception) {
                        player.close();
                    }
                });
            }
        } catch (IOException exception) {
            log.error(exception.getMessage(), exception);
        }
    }
}
