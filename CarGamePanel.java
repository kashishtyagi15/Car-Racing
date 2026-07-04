import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class CarGamePanel extends JPanel implements ActionListener {
    private Image roadImage, carImage, obstacleCarImage;
    
    private int carX, carY;
    private int roadY = 0; // For scrolling effect
    private final int carSpeed = 7;
    private static final int CAR_WIDTH = 80;
    private static final int CAR_HEIGHT = 90;
    
    private final int OBSTACLE_WIDTH = 80;
    private final int OBSTACLE_HEIGHT = 90;
    
    private Timer timer;
    private ArrayList<Rectangle> obstacles;
    private double obstacleSpeed = 5.0;
    private Random random;
    private boolean gameOver;
    private int score;

    private final Set<Integer> pressedKeys = new HashSet<>();

    public CarGamePanel() {
        // Load images
        roadImage = loadImage("/road.png", "road_0.png", 800, 600);
        carImage = loadImage("/car.png", "car.png", CAR_WIDTH, CAR_HEIGHT);
        obstacleCarImage = loadImage("/obstacle1.png", "obstacle.png", OBSTACLE_WIDTH, OBSTACLE_HEIGHT);
        
        obstacles = new ArrayList<>();
        random = new Random();
        
        timer = new Timer(16, this); // ~60 FPS
        
        setPreferredSize(new Dimension(800, 600));
        setFocusable(true);
        initKeyBindings();
        resetGame();
    }

    private void resetGame() {
        carX = (800 - CAR_WIDTH) / 2;
        carY = 600 - CAR_HEIGHT - 20;
        obstacles.clear();
        obstacleSpeed = 5.0;
        score = 0;
        gameOver = false;
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Draw Scrolling Road
        if (roadImage != null) {
            g2d.drawImage(roadImage, 0, roadY, getWidth(), getHeight(), this);
            g2d.drawImage(roadImage, 0, roadY - getHeight(), getWidth(), getHeight(), this);
        }

        // Draw Player
        if (carImage != null) {
            g2d.drawImage(carImage, carX, carY, CAR_WIDTH, CAR_HEIGHT, this);
        }

        // Draw Obstacles
        for (Rectangle obstacle : obstacles) {
            if (obstacleCarImage != null) {
                g2d.drawImage(obstacleCarImage, obstacle.x, obstacle.y, obstacle.width, obstacle.height, this);
            }
        }

        // UI Overlay
        drawUI(g2d);
    }

    private void drawUI(Graphics2D g2d) {
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.drawString("Score: " + score, 20, 30);

        if (gameOver) {
            g2d.setColor(new Color(0, 0, 0, 150));
            g2d.fillRect(0, 0, getWidth(), getHeight());
            
            g2d.setColor(Color.RED);
            g2d.setFont(new Font("Arial", Font.BOLD, 50));
            drawCenteredString(g2d, "GAME OVER", getHeight() / 2 - 20);
            
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.PLAIN, 20));
            drawCenteredString(g2d, "Final Score: " + score, getHeight() / 2 + 30);
            drawCenteredString(g2d, "Press 'R' to Restart", getHeight() / 2 + 70);
        }
    }

    private void drawCenteredString(Graphics g, String text, int y) {
        FontMetrics metrics = g.getFontMetrics(g.getFont());
        int x = (getWidth() - metrics.stringWidth(text)) / 2;
        g.drawString(text, x, y);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver) {
            updateMovement();
            updateRoad();
            updateObstacles();
            repaint();
        }
    }

    private void updateRoad() {
        roadY += (int)obstacleSpeed; // Road moves at same speed as cars
        if (roadY >= getHeight()) {
            roadY = 0;
        }
    }

    private void updateMovement() {
        if (pressedKeys.contains(KeyEvent.VK_LEFT) && carX > 0) carX -= carSpeed;
        if (pressedKeys.contains(KeyEvent.VK_RIGHT) && carX < getWidth() - CAR_WIDTH) carX += carSpeed;
        if (pressedKeys.contains(KeyEvent.VK_UP) && carY > 0) carY -= carSpeed;
        if (pressedKeys.contains(KeyEvent.VK_DOWN) && carY < getHeight() - CAR_HEIGHT) carY += carSpeed;
    }

    private void updateObstacles() {
        // Safer way to handle removal and scoring
        obstacles.removeIf(obs -> {
            obs.y += (int)obstacleSpeed;
            
            // Check Collision
            if (obs.intersects(new Rectangle(carX, carY, CAR_WIDTH, CAR_HEIGHT))) {
                gameOver = true;
                timer.stop();
            }
            
            if (obs.y > getHeight()) {
                score++;
                if (score % 5 == 0) obstacleSpeed += 0.5; // Difficulty curve
                return true; // Remove from list
            }
            return false;
        });

        // Spawning logic
        if (random.nextInt(40) == 0) {
            int obstacleX = random.nextInt(getWidth() - OBSTACLE_WIDTH);
            obstacles.add(new Rectangle(obstacleX, -OBSTACLE_HEIGHT, OBSTACLE_WIDTH, OBSTACLE_HEIGHT));
        }
    }

    private void initKeyBindings() {
        InputMap im = getInputMap(WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();

        int[] keys = {KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_R};
        String[] names = {"LEFT", "RIGHT", "UP", "DOWN", "RESTART"};

        for (int i = 0; i < keys.length; i++) {
            final int keyCode = keys[i];
            im.put(KeyStroke.getKeyStroke(keyCode, 0, false), names[i] + "_P");
            am.put(names[i] + "_P", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (keyCode == KeyEvent.VK_R && gameOver) resetGame();
                    pressedKeys.add(keyCode);
                }
            });

            im.put(KeyStroke.getKeyStroke(keyCode, 0, true), names[i] + "_R");
            am.put(names[i] + "_R", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    pressedKeys.remove(keyCode);
                }
            });
        }
    }

    private Image loadImage(String resourcePath, String fileName, int w, int h) {
        try {
            java.net.URL res = getClass().getResource(resourcePath);
            if (res != null) return ImageIO.read(res);
            File f = new File(fileName);
            if (f.exists()) return ImageIO.read(f);
        } catch (IOException ex) {
            System.err.println("Load error: " + fileName);
        }
        
        BufferedImage placeholder = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = placeholder.createGraphics();
        g2.setColor(Color.DARK_GRAY);
        g2.fillRect(0, 0, w, h);
        g2.setColor(Color.WHITE);
        g2.drawString(fileName, 5, h / 2);
        g2.dispose();
        return placeholder;
    }
}
