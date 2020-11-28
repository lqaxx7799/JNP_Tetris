/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package handler;

import socket.ClientHandler;

/**
 *
 * @author Admin
 */
public class Room {

    private ClientHandler game;
    private ClientHandler consoleOne;
    private ClientHandler consoleTwo;

    public Room() {
        this.game = null;
        this.consoleOne = null;
        this.consoleTwo = null;
    }

    public Room(ClientHandler game) {
        this.game = game;
        this.consoleOne = null;
        this.consoleTwo = null;
    }

    public ClientHandler getGame() {
        return game;
    }

    public void setGame(ClientHandler game) {
        this.game = game;
    }

    public ClientHandler getConsoleOne() {
        return consoleOne;
    }

    public void setConsoleOne(ClientHandler consoleOne) {
        this.consoleOne = consoleOne;
    }

    public ClientHandler getConsoleTwo() {
        return consoleTwo;
    }

    public void setConsoleTwo(ClientHandler consoleTwo) {
        this.consoleTwo = consoleTwo;
    }

    public String registerConsole(ClientHandler console) {
        if (this.consoleOne == null) {
            this.consoleOne = console;
            return "";
        } else if (this.consoleOne != null && this.consoleTwo == null) {
            this.consoleTwo = console;
            return "";
        }
        return "ROOM_FULL";
    }

    public void removeConsole(String consoleId) {
        if (this.consoleOne.getClientId().equals(consoleId)) {
            this.consoleOne = null;
        } else if (this.consoleTwo.getClientId().equals(consoleId)) {
            this.consoleTwo = null;
        }
    }
}
