/*
 * Advanced Programming – CA1
 * Student Name: Leandro Crisol
 * Student ID: 23156503
 *
 * Class: EventServerMain
 * 
 * Entry point for the server-side application. Creates a shared EventStore,
 * accepts client TCP connections, and generates a dedicated ClientHandler
 * thread for each connection.
 */
package eventboard;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class EventServerMain {

    public static final int PORT = 5550;

    public static void main(String[] args) {
        EventStore store = new EventStore();

        System.out.println("Event Server running on port " + PORT);

        try {
            ServerSocket serverSocket = new ServerSocket(PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(clientSocket, store);
                Thread t = new Thread(handler);
                t.start();
            }

        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
