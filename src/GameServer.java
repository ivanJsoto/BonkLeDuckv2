import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

public class GameServer {
    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(1127);
            System.out.println("Server started. Waiting for clients...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket);

                DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
                DataInputStream in = new DataInputStream(clientSocket.getInputStream());

                // Send duck coordinates to the client
                Thread clientHandler = new Thread(new ClientHandler(clientSocket, in, out));
                clientHandler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static int[] updateRandNum() {
        Random rand = new Random();
        int[] randArr = new int[2];
        randArr[0] = rand.nextInt(300);
        randArr[1] = rand.nextInt(300);
        return randArr;
    }

    static class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private final DataInputStream in;
        private final DataOutputStream out;

        ClientHandler(Socket socket, DataInputStream in, DataOutputStream out) {
            this.clientSocket = socket;
            this.in = in;
            this.out = out;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    // Read client messages
                    String clientMessage = in.readUTF();

                    // Check if the client is requesting new coordinates
                    if (clientMessage.equals("New Coordinates")) {
                        // Generate new coordinates for the duck
                        int[] newDuckCoordinates = updateRandNum();

                        // Send the new coordinates to the client
                        out.writeInt(newDuckCoordinates[0]);
                        out.writeInt(newDuckCoordinates[1]);

                    }

                    // Other game logic handling could go here based on client actions
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
