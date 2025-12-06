/*
 * Advanced Programming – CA1
 * Student Name: Leandro Crisol
 * Student ID: 23156503
 *
 * Class: ClientHandler
 * 
 * Handles communication with a single client socket. Reads commands,
 * performs validation, interacts with the EventStore, and returns server
 * responses. Supports add/remove/list operations and clean termination.
 */
package eventboard;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private Socket socket;
    private EventStore store;

    public ClientHandler(Socket socket, EventStore store) {
        this.socket = socket;
        this.store = store;
    }

    @Override
    public void run() {
        System.out.println("Client connected: " + socket.getRemoteSocketAddress());

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            String line;
            while ((line = in.readLine()) != null) {
                line = line.trim();

                // Termination protocol
                if (line.equalsIgnoreCase("STOP")) {
                    out.println("TERMINATE");
                    break;
                }

                try {
                    String reply = handleCommand(line); // Calls method below
                    out.println(reply);
                } catch (InvalidCommandException e) {
                    out.println("InvalidCommandException: " + e.getMessage());
                } catch (Exception e) {
                    out.println("ERROR: " + e.getMessage());
                }
            }

            in.close();
            out.close();
        } catch (IOException e) {
            System.out.println("I/O error with client: " + e.getMessage());
        } finally {
            try {
                socket.close();
                System.out.println("Client disconnected: " + socket.getRemoteSocketAddress());
            } catch (IOException e) {
                System.out.println("Error closing socket: " + e.getMessage());
            }
        }
    }

    private String handleCommand(String message) throws InvalidCommandException {
        // Expected: action; date; time; description
                
        if (message.endsWith(";")) {
            throw new InvalidCommandException("Extra ';' detected at the end. Commands must not end with a semicolon.");
        }
        
        // Divide command into 4 parts separating them by ;
        String[] parts = message.split(";");

        if (parts.length != 4) {
            throw new InvalidCommandException("message must contain 4 fields: action; date; time; description");
        }  

        String action = parts[0].trim().toLowerCase();
        String date = parts[1].trim();
        String time = parts[2].trim();
        String description = parts[3].trim();

        if (date.isEmpty()) {
            throw new InvalidCommandException("date cannot be empty");
        }

        if (action.equals("add")) {
            if (time.isEmpty() || description.isEmpty()) {
                throw new InvalidCommandException("time and description required for add");
            }
            Event e = new Event(date, time, description);
            return store.formatEvents(date, store.addEvent(e));

        } else if (action.equals("remove")) {
            if (time.isEmpty() || description.isEmpty()) {
                throw new InvalidCommandException("time and description required for remove");
            }
            return store.formatEvents(date, store.removeEvent(date, time, description));

        } else if (action.equals("list")) {
            // time and description can be "-" and we ignore them
            return store.formatEvents(date, store.listEvents(date));

        } else {
            throw new InvalidCommandException("action must be add/remove/list");
        }
    }
}
