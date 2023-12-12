import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class GameServer {
    private JFrame frame;
    private JPanel panel;
    private JLabel scoreLabel1, scoreLabel2;
    public static HashMap<String, Integer> playerScores = new HashMap<>();
    public static int winningScore = 29;

    public GameServer() {
        frame = new JFrame("Server Score Frame");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(new BorderLayout());

        panel = new JPanel(new GridLayout(2, 1));

        scoreLabel1 = new JLabel("Player 1: 0", SwingConstants.CENTER);
        scoreLabel2 = new JLabel("Player 2: 0", SwingConstants.CENTER);

        panel.add(scoreLabel1);
        panel.add(scoreLabel2);

        frame.add(panel, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        GameServer gameServer = new GameServer();

        try {
            ServerSocket serverSocket = new ServerSocket(1127);
            System.out.println("Server started. Waiting for clients...");

            int currentPlayer = 1; // Initialize player number

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket);

                String clientAddress = clientSocket.getInetAddress().getHostAddress();
                int playerNumber = getPlayerNumber(clientAddress);

                DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
                DataInputStream in = new DataInputStream(clientSocket.getInputStream());

                Thread clientHandler = new Thread(new ClientHandler(clientSocket, in, out, playerNumber, gameServer));
                clientHandler.start();

                currentPlayer = (currentPlayer % 2) + 1; // Switch player number for next connection
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private final DataInputStream in;
        private final DataOutputStream out;
        private final int playerNumber;
        private final GameServer gameServer;

        ClientHandler(Socket socket, DataInputStream in, DataOutputStream out, int playerNumber, GameServer gameServer) {
            this.clientSocket = socket;
            this.in = in;
            this.out = out;
            this.playerNumber = playerNumber;
            this.gameServer = gameServer;
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

                        gameServer.updateScores(playerNumber); // Update scores when new coordinates are requested
                        System.out.println("Player " + playerNumber + " Score: " + playerScores.getOrDefault(playerNumber + "", 0));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static int[] updateRandNum() {
        Random rand = new Random();
        int[] randArr = new int[2];
        randArr[0] = rand.nextInt(300);
        randArr[1] = rand.nextInt(300);
        return randArr;
    }

    // Method to update scores
    private void updateScores(int playerNumber) {
        int updatedScore = playerScores.getOrDefault(playerNumber + "", -1) + 1;
        playerScores.put(playerNumber + "", updatedScore);

        if (playerNumber == 1) {
            scoreLabel1.setText("Player 1: " + updatedScore);
        } else if (playerNumber == 2) {
            scoreLabel2.setText("Player 2: " + updatedScore);
        }

        if (updatedScore == winningScore) {
            // Reset scores when a player reaches the winning score
            playerScores.put("1", 0);
            playerScores.put("2", 0);
            scoreLabel1.setText("Player 1: " + playerScores.get("1"));
            scoreLabel2.setText("Player 2: " + playerScores.get("2"));
        }
    }

    private static int getPlayerNumber(String clientAddress) {
        if (!playerScores.containsKey("1")) {
            return 1; // Player 1 is available, assign to the first client
        } else if (!playerScores.containsKey(clientAddress)) {
            return 2; // Player 1 is taken, assign Player 2 to new client
        } else {
            return playerScores.get(clientAddress); // Return existing player number for known client
        }

    }
}