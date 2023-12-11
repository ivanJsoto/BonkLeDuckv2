import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;

import static java.awt.Toolkit.getDefaultToolkit;

public class GameClient {
    private static Image grassImage;
    private static boolean isDuckButtonAdded = false;
    public static int Score = 0;
    public static JLabel ScoreLabel;

    public static JButton QButton;

    private static void createGameWindow(JFrame mainFrame) {
        mainFrame.setTitle("BONK LE DUCK");
        mainFrame.setSize(800, 800);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setResizable(false);
        mainFrame.setLocationRelativeTo(null); // Center the frame on the screen
    }

    private static void scoreBoard(JPanel mainPanel) {

        ImageIcon icon = new ImageIcon("IMAGES/ScoreBoard.png");
        Image image = icon.getImage();
        Image newImg = image.getScaledInstance(200, 150, java.awt.Image.SCALE_SMOOTH);
        icon = new ImageIcon(newImg);
        JLabel ScoreBoardLabel = new JLabel(icon);

        ScoreLabel = new JLabel(String.valueOf(Score));
        Font labelFont = ScoreLabel.getFont();
        ScoreLabel.setFont(new Font(labelFont.getName(), Font.PLAIN, 50));

        mainPanel.add(ScoreLabel);
        mainPanel.add(ScoreBoardLabel);
        ScoreBoardLabel.setBounds(0, 600, 200, 150);
        ScoreLabel.setBounds(85, 650, 100, 50);
    }
    private static void drawPond(Graphics g, JFrame mainFrame) {
        // Draw grass background
        g.drawImage(grassImage, 0, 0, mainFrame.getWidth(), mainFrame.getHeight(), mainFrame);

        // Draw water in the middle as a blue circle
        int circleDiameter = 600;
        int circleX = (mainFrame.getWidth() - circleDiameter) / 2;
        int circleY = (mainFrame.getHeight() - circleDiameter) / 2;

        g.setColor(new Color(10, 180, 255));
        g.fillOval(circleX, circleY, circleDiameter, circleDiameter);

    }
    public static void updateScore(JLabel ScoreLabel) {
        Score += 1;
        ScoreLabel.setText(String.valueOf(Score));

        if (Score == 30) {
            AgainButton((JPanel) ScoreLabel.getParent());
            QuitButton((JPanel) ScoreLabel.getParent());

        }
    }
    public static void QuitButton(JPanel mainPanel){
        JButton Qbutton = new JButton("Quit");
        Qbutton.addActionListener(e -> {
            Container parent = Qbutton.getParent();
            Score = 0;
            ScoreLabel.setText(String.valueOf(Score)); // Reset the score label
            parent.remove(Qbutton);
            parent.revalidate();
            parent.repaint();
            PlayBonk();
        });
        mainPanel.add(Qbutton);
        Qbutton.setBounds(620, 700, 100, 50);
    }
    private static void AgainButton(JPanel mainPanel) {
        JButton Pbutton = new JButton("Play Again");
        Pbutton.addActionListener(e -> {
            Container parent = Pbutton.getParent();
            Score = 0;
            ScoreLabel.setText(String.valueOf(Score)); // Reset the score label
            parent.remove(Pbutton);
            parent.revalidate();
            parent.repaint();
            PlayBonk();
        });
        mainPanel.add(Pbutton);
        Pbutton.setBounds(620, 630, 100, 50);
    }

    private static void ducky( JPanel mainPanel,Socket socket) throws IOException {
        if (!isDuckButtonAdded) {
            ImageIcon icon = new ImageIcon("IMAGES/DUCK.png");
            Image image = icon.getImage(); // transform it
            Image newImg = image.getScaledInstance(80, 80, java.awt.Image.SCALE_SMOOTH);
            icon = new ImageIcon(newImg);
            JButton button = new JButton(icon);

            button.setBackground(new Color(10, 180, 255));
            button.setBorderPainted(false);
            int[] testArr;
            testArr = newCoords(socket);

            mainPanel.add(button);

            button.setBounds(testArr[0] + 220, testArr[1] + 220, 80, 80);
            isDuckButtonAdded = true;

            button.addActionListener(e -> {
                Container parent = button.getParent();
                parent.remove(button);
                parent.revalidate();
                parent.repaint();
                PlayBonk();
                updateScore(ScoreLabel);
                isDuckButtonAdded = false;
            });

        }
    }


    private static int[] newCoords(Socket socket) throws IOException { //todo calls new coordinates from the server... will probably apply to both clients-
        int[] newDuckCoordinates = {0, 0};
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        out.writeUTF("New Coordinates");
        out.flush();

        DataInputStream in = new DataInputStream(socket.getInputStream());
        newDuckCoordinates[0] = in.readInt();
        newDuckCoordinates[1] = in.readInt();

        return newDuckCoordinates;
    }

    public static void PlayMusic() {
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File("Sounds/Music.wav").getAbsoluteFile());
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();
        } catch(Exception ex) {
            System.out.println("Error with playing sound.");
            ex.printStackTrace();
        }
    }
    public static void PlayBonk() {
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File("Sounds/bonk (2).wav").getAbsoluteFile());
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
        } catch(Exception ex) {
            System.out.println("Error with playing sound.");
            ex.printStackTrace();

        }
    }

    public static void main(String[] args) throws IOException {

        //todo menustuff.....
        JFrame menuFrame = new JFrame();
        JMenuBar menuBar = new JMenuBar();
        JMenu singlePlayer = new JMenu("Play SinglePlayer");
        JMenu multiPlayer = new JMenu("Play MultiPlayer");

        Socket socket = null;
        try {
            socket = new Socket("localhost", 1127);
            System.out.println("Connected to server.");

        } catch (IOException e) {
            e.printStackTrace();
        }

        assert socket != null;


        Image batImage;
        try {
            grassImage = ImageIO.read(new File("IMAGES/Grass_image.jpg")); // Adjust the path as necessary
            batImage = ImageIO.read(new File("IMAGES/icons8-baseball-bat-100.png"));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Cursor batCursor = getDefaultToolkit().createCustomCursor(batImage, new Point(0, 0), "customCursor");

        JFrame mainFrame = new JFrame();
        createGameWindow(mainFrame);


        Socket finalSocket = socket;
        JPanel mainPanel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawPond(g, mainFrame);
                try {
                    ducky(this, finalSocket);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        mainFrame.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mainFrame.setCursor(batCursor);
            }
            @Override
            public void mouseDragged(MouseEvent e) {
            }
        });
        PlayMusic();


        scoreBoard(mainPanel);
        mainFrame.add(mainPanel);
        mainFrame.setVisible(true);
    }
}
