/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package socket;

import handler.GameHandler;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import utils.Utils;

/**
 *
 * @author Admin
 */
public class ClientHandler extends Thread {

    private String clientId;
    private String type;
    private final Socket clientSocket;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
        //System.out.println("A "+ type +" connected: " + id);
        System.out.println(clientSocket);
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public Socket getClientSocket() {
        return clientSocket;
    }
    
    public void sendText(String text) throws IOException {
        OutputStream out = clientSocket.getOutputStream();
        byte[] textBytes = text.getBytes();
        ArrayList<Byte> response = new ArrayList<>();
        response.add((byte) 129);
        
        if(textBytes.length <= 125) {
            response.add((byte) textBytes.length);
        } else if (textBytes.length >= 126 && textBytes.length <= 65535) {
            response.add((byte) 126);
            response.add((byte) ((textBytes.length >> 8) & 255));
            response.add((byte) (textBytes.length & 255));            
        } else {
            response.add((byte) 126);
            response.add((byte) ((textBytes.length >> 56) & 255));
            response.add((byte) ((textBytes.length >> 48) & 255));
            response.add((byte) ((textBytes.length >> 40) & 255));
            response.add((byte) ((textBytes.length >> 32) & 255));
            response.add((byte) ((textBytes.length >> 24) & 255));
            response.add((byte) ((textBytes.length >> 16) & 255));
            response.add((byte) ((textBytes.length >> 8) & 255));
            response.add((byte) (textBytes.length & 255));            
        }
        for (int i = 0; i < textBytes.length; i ++) {
            response.add(textBytes[i]);
        }
        byte[] responseBytes = new byte[response.size()];
        for (int i = 0; i < response.size(); i ++) {
            responseBytes[i] = response.get(i);
        }
        out.write(responseBytes);
    }

    @Override
    public void run() {
        try {
            InputStream in = clientSocket.getInputStream();
            OutputStream out = clientSocket.getOutputStream();
            byte[] buffer = new byte[1024];
            while (true) {
                in.read(buffer);
                String data = new String(buffer).trim();

                Matcher get = Pattern.compile("^GET").matcher(data);

                if (get.find()) {
                    //handshake
                    System.out.println(data);
                    Matcher match = Pattern.compile("Sec-WebSocket-Key: (.*)").matcher(data);
                    match.find();
                    byte[] response = ("HTTP/1.1 101 Switching Protocols\r\n"
                            + "Connection: Upgrade\r\n"
                            + "Upgrade: websocket\r\n"
                            + "Sec-WebSocket-Accept: "
                            + Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-1").digest((match.group(1) + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").getBytes("UTF-8")))
                            + "\r\n\r\n").getBytes("UTF-8");
                    out.write(response, 0, response.length);
                    
                    Matcher matchQuery = Pattern.compile("^GET\\s/\\?(.*)\\sHTTP/1.1").matcher(data);
                    matchQuery.find();
                    String queryString = matchQuery.group(1);
                    System.out.println(queryString);
                    Map<String, String> query = Utils.splitQuery(queryString);
                    this.type = query.get("type");
                    this.clientId = query.get("id");
                    
                    if (this.type.equals("game")) {
                        GameHandler.addRoom(this);
                    } else if (type.equals("console")) {
                        GameHandler.addConsoleToRoom(query.get("roomId"), this);
                    }                    
                } else {
                    // receive message
                    boolean fin = (buffer[0] & 0xFF & 0b10000000) != 0,
                            mask = (buffer[1] & 0xFF & 0b10000000) != 0; // must be true, "All messages from the client to the server have this bit set"

                    int opcode = buffer[0] & 0xFF & 0b00001111, // expecting 1 - text message
                            msglen = buffer[1] & 0xFF - 128, // & 0111 1111
                            offset = 2;

                    if (msglen == 126) {
                        msglen = Utils.convertByteArrayToInt(new byte[]{buffer[3], buffer[2]});
                    } else if (msglen == 127) {
                        msglen = Utils.convertByteArrayToInt(new byte[]{buffer[5], buffer[4], buffer[3], buffer[2], buffer[9], buffer[8], buffer[7], buffer[6]});
                        offset = 10;
                    }

                    if (msglen == 0) {
                        System.out.println("msglen == 0");
                    } else if (mask) {
                        byte[] decoded = new byte[msglen];
                        byte[] masks = new byte[]{buffer[offset], buffer[offset + 1], buffer[offset + 2], buffer[offset + 3]};
                        offset += 4;

                        for (int i = 0; i < msglen; i++) {
                            decoded[i] = (byte) (buffer[offset + i] ^ masks[i % 4]);
                        }
                        
                        String text = new String(decoded).trim();
                        System.out.println("text: " + text);
                        
                        if (type.equals("game")) {
                            GameHandler.sendMessageByGame(text, clientId);
                        } else if (type.equals("console")) {
                            GameHandler.sendMessageByConsole(text, clientId);
                        }
                    } else {
                        System.out.println("mask bit not set");
                    }
                }
            }
        } catch (IOException | NoSuchAlgorithmException ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
