package ru.rockpaperscissors.model;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.regex.Pattern;

@Getter
@Slf4j
public class Player implements AutoCloseable {
    private final Socket socket;
    private final BufferedReader bufferedReader;
    private final PrintWriter printWriter;
    private boolean closed = false;
    private String name;

    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-z ,.'-]+$", Pattern.CASE_INSENSITIVE);

    public Player(Socket socket) throws IOException {
        this.socket = socket;
        this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.printWriter = new PrintWriter(socket.getOutputStream(), true);
    }

    @Override
    public void close() {
        printWriter.close();
        try {
            bufferedReader.close();
        } catch (IOException exception) {
            log.error(exception.getMessage(), exception);
        }
        try {
            socket.close();
        } catch (IOException exception) {
            log.error(exception.getMessage(), exception);
        }
        closed = true;
        if (name != null) {
            log.info("{} has disconnected", name);
        } else {
            log.info("Player disconnected before entering name");
        }
    }

    public String getMessage() throws IOException {
        String message = bufferedReader.readLine();

        if (message == null) {
            throw new IOException();
        }
        return message;
    }

    public void setName() throws IOException {
        sendMessage("Enter your name:", true);
        String name = getMessage();

        while (!NAME_PATTERN.matcher(name).matches()) {
            sendMessage("Invalid name. Please, try again:", true);
            name = getMessage();
        }
        this.name = name;
    }

    public void sendMessage(String message, boolean clear) {
        if (clear) {
            printWriter.print("\u001B[2J");
        }
        printWriter.println(message);
    }

    public boolean isConnected() {
        try {
            try {
                socket.setSoTimeout(1);
                getMessage();
            } catch (SocketTimeoutException exception) {
                return true;
            } catch (IOException exception) {
                return false;
            } finally {
                socket.setSoTimeout(0);
            }
        } catch (SocketException exception) {
            return false;
        }
        return true;
    }
}
