/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package handler;

import com.google.gson.Gson;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import socket.ClientHandler;

/**
 *
 * @author Admin
 */
public class GameHandler {

    private static GameHandler instance;
    private static Map<String, Room> rooms = new HashMap<>();

    private GameHandler() {
    }

    public static void initialize() {
        if (instance == null) {
            instance = new GameHandler();
        }
    }

    public static void addRoom(ClientHandler client) {
        rooms.put(client.getClientId(), new Room(client));
    }

    public static void removeRoom(String id) throws IOException {
        Room room = rooms.get(id);
        GameMessage gameMessage = new GameMessage(GameMessageConstants.GAME_REMOVE, null);
        String message = new Gson().toJson(gameMessage);
        if (room.getConsoleOne() != null) {
            room.getConsoleOne().sendText(message);
        }
        if (room.getConsoleTwo() != null) {
            room.getConsoleTwo().sendText(message);
        }
        rooms.remove(id);
    }

    public static void addConsoleToRoom(String roomId, ClientHandler console) throws IOException {
        Room room = rooms.get(roomId);
        if (room == null) {
            Map<String, String> metaData = new LinkedHashMap<>();
            metaData.put("error", "ROOM_NOT_EXIST");
            GameMessage gameMessage = new GameMessage(GameMessageConstants.CONSOLE_REGISTED_TO_GAME_FAILED, metaData);
            String message = new Gson().toJson(gameMessage);
            console.sendText(message);
            return;
        }
        String registerMessage = room.registerConsole(console);
        if (!registerMessage.equals("")) {
            Map<String, String> metaData = new LinkedHashMap<>();
            metaData.put("error", registerMessage);
            GameMessage gameMessage = new GameMessage(GameMessageConstants.CONSOLE_REGISTED_TO_GAME_FAILED, metaData);
            String message = new Gson().toJson(gameMessage);
            console.sendText(message);
            return;
        }
        rooms.replace(roomId, room);
        Map<String, String> metaData = new LinkedHashMap<>();
        metaData.put("consoleId", console.getClientId());
        GameMessage gameMessage = new GameMessage(GameMessageConstants.CONSOLE_REGISTED_TO_GAME, metaData);
        String message = new Gson().toJson(gameMessage);
        room.getGame().sendText(message);
    }

    public static void removeConsoleFromRoom(String roomId, String consoleId) throws IOException {
        Room room = rooms.get(roomId);
        if (room == null) {
            return;
        }
        room.removeConsole(consoleId);
        rooms.replace(roomId, room);
        Map<String, String> metaData = new LinkedHashMap<>();
        metaData.put("consoleId", consoleId);
        GameMessage gameMessage = new GameMessage(GameMessageConstants.CONSOLE_REMOVE_FROM_GAME, metaData);
        String message = new Gson().toJson(gameMessage);
        room.getGame().sendText(message);
        if (room.getConsoleOne() != null) {
            room.getConsoleOne().sendText(message);
        }
        if (room.getConsoleTwo() != null) {
            room.getConsoleTwo().sendText(message);
        }
    }

    public static void sendMessageByGame(String message, String gameId) throws IOException {
        for (String key : rooms.keySet()) {
            Room room = rooms.get(key);
            if (room.getGame().getClientId().equals(gameId)) {
                System.out.println("Game: " + message);
                room.getConsoleOne().sendText(message);
                room.getConsoleTwo().sendText(message);
            }
        }
    }

    public static void sendMessageByConsole(String message, String consoleId) throws IOException {
        for (String key : rooms.keySet()) {
            Room room = rooms.get(key);
            if (room.getConsoleOne().getClientId().equals(consoleId)) {
                System.out.println("Player 1: " + message);
                room.getGame().sendText(message);
                room.getConsoleTwo().sendText(message);
            } else if (room.getConsoleTwo().getClientId().equals(consoleId)) {
                System.out.println("Player 2: " + message);
                room.getGame().sendText(message);
                room.getConsoleOne().sendText(message);
            }
        }
    }
}
