/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package socket;

import handler.GameHandler;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author Admin
 */
public class GameSocket extends Thread {

    public static void main(String[] args) {
        (new GameSocket()).run();
    }

    public GameSocket() {
        super("Server Thread");
    }

    @Override
    public void run() {
        try ( ServerSocket serverSocket = new ServerSocket(8080)) {
            Socket socket;
            try {
                while ((socket = serverSocket.accept()) != null) {
                    GameHandler.initialize();
                    System.out.println(socket);
                    (new ClientHandler(socket)).start();
                }
            } catch (IOException e) {
                e.printStackTrace();  // TODO: implement catch
            }
        } catch (IOException e) {
            e.printStackTrace();  // TODO: implement catch
            return;
        }
    }

}
