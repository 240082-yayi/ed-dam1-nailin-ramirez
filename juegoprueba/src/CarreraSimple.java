import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

public class CarreraSimple extends JPanel implements KeyListener {

    private static final int WIDTH = 420;
    private static final int HEIGHT = 640;
    private static final int ROAD_X = 60;
    private static final int ROAD_WIDTH = WIDTH - ROAD_X * 2;
    private static final int LANE_COUNT = 3;








    private static final int CAR_WIDTH = 44;
    private static final int CAR_HEIGHT = 80;
    private static final int PLAYER_SPEED = 7;

    private final Timer timer;
    private final Random random = new Random();
    private final ArrayList<Rectangle> obstacles = new ArrayList<>();

    private Rectangle player;
    private boolean leftPressed;
    private boolean rightPressed;
    private boolean gameOver;

    private int ticks;
    private int score;
    private int roadOffset;
    private int spawnTimer;

    public CarreraSimple() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(new Color(34, 90, 40));
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        addKeyListener(this);

        resetGame();

        timer = new Timer(16, e -> updateGame());
        timer.start();
    }

    private void resetGame() {
        player = new Rectangle(
                ROAD_X + ROAD_WIDTH / 2 - CAR_WIDTH / 2,
                HEIGHT - CAR_HEIGHT - 35,
                CAR_WIDTH,
                CAR_HEIGHT
        );

        obstacles.clear();
        leftPressed = false;
        rightPressed = false;
        gameOver = false;

        ticks = 0;
        score = 0;
        roadOffset = 0;
        spawnTimer = 0;
    }

    private void updateGame() {
        if (gameOver) {
            return;
        }

        ticks++;
        score = ticks / 6;

        int speed = currentSpeed();
        roadOffset = (roadOffset + speed) % 50;

        if (leftPressed && !rightPressed) {
            player.x -= PLAYER_SPEED;
        } else if (rightPressed && !leftPressed) {
            player.x += PLAYER_SPEED;
        }

        clampPlayer();

        spawnTimer++;
        int spawnDelay = Math.max(24, 62 - ticks / 90);
        if (spawnTimer >= spawnDelay) {
            spawnObstacle();
            spawnTimer = 0;
        }

        for (int i = obstacles.size() - 1; i >= 0; i--) {
            Rectangle obstacle = obstacles.get(i);
            obstacle.y += speed;

            if (obstacle.y > HEIGHT + 100) {
                obstacles.remove(i);
            } else if (player.intersects(obstacle)) {
                gameOver = true;
            }
        }

        repaint();
    }

    private int currentSpeed() {
        return Math.min(12, 3 + ticks / 300);
    }

    private void clampPlayer() {
        int minX = ROAD_X + 6;
        int maxX = ROAD_X + ROAD_WIDTH - CAR_WIDTH - 6;

        if (player.x < minX) {
            player.x = minX;
        }

        if (player.x > maxX) {
            player.x = maxX;
        }
    }

    private void spawnObstacle() {
        int laneWidth = ROAD_WIDTH / LANE_COUNT;
        int lane = random.nextInt(LANE_COUNT);
        int x = ROAD_X + lane * laneWidth + (laneWidth - CAR_WIDTH) / 2;

        obstacles.add(new Rectangle(x, -CAR_HEIGHT, CAR_WIDTH, CAR_HEIGHT));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Carretera
        g2.setColor(new Color(50, 50, 50));
        g2.fillRect(ROAD_X, 0, ROAD_WIDTH, HEIGHT);

        // Bordes de la carretera
        g2.setColor(new Color(255, 210, 0));
        g2.fillRect(ROAD_X - 6, 0, 6, HEIGHT);
        g2.fillRect(ROAD_X + ROAD_WIDTH, 0, 6, HEIGHT);

        // Líneas de los carriles
        g2.setColor(Color.WHITE);
        int laneWidth = ROAD_WIDTH / LANE_COUNT;

        for (int lane = 1; lane < LANE_COUNT; lane++) {
            int x = ROAD_X + lane * laneWidth - 3;

            for (int y = -50 + roadOffset; y < HEIGHT; y += 50) {
                g2.fillRect(x, y, 6, 30);
            }
        }

        // Coches enemigos
        for (Rectangle obstacle : obstacles) {
            drawCar(g2, obstacle.x, obstacle.y, new Color(220, 45, 45));
        }

        // Coche del jugador
        drawCar(g2, player.x, player.y, new Color(35, 145, 255));

        // HUD
        g2.setColor(Color.WHITE);
        g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, 18));
        g2.drawString("Puntuación: " + score, 10, 25);
        g2.drawString("Velocidad: " + currentSpeed(), 10, 50);

        if (gameOver) {
            drawGameOver(g2);
        }
    }

    private void drawCar(Graphics2D g2, int x, int y, Color bodyColor) {
        // Ruedas
        g2.setColor(Color.BLACK);
        g2.fillRect(x - 5, y + 10, 7, 18);
        g2.fillRect(x + CAR_WIDTH - 2, y + 10, 7, 18);
        g2.fillRect(x - 5, y + CAR_HEIGHT - 28, 7, 18);
        g2.fillRect(x + CAR_WIDTH - 2, y + CAR_HEIGHT - 28, 7, 18);

        // Carrocería
        g2.setColor(bodyColor);
        g2.fillRoundRect(x, y, CAR_WIDTH, CAR_HEIGHT, 12, 12);

        // Ventana delantera
        g2.setColor(new Color(220, 240, 255));
        g2.fillRoundRect(x + 7, y + 14, CAR_WIDTH - 14, 22, 8, 8);

        // Ventana trasera
        g2.setColor(new Color(0, 0, 0, 90));
        g2.fillRoundRect(x + 7, y + CAR_HEIGHT - 26, CAR_WIDTH - 14, 14, 8, 8);
    }

    private void drawGameOver(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 170));
        g2.fillRect(0, 0, WIDTH, HEIGHT);

        g2.setColor(Color.WHITE);

        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 42));
        String title = "GAME OVER";
        FontMetrics fm = g2.getFontMetrics();
        int x = (WIDTH - fm.stringWidth(title)) / 2;
        int y = HEIGHT / 2 - 40;
        g2.drawString(title, x, y);

        g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 24));
        fm = g2.getFontMetrics();
        String scoreText = "Puntuación: " + score;
        x = (WIDTH - fm.stringWidth(scoreText)) / 2;
        y = HEIGHT / 2 + 10;
        g2.drawString(scoreText, x, y);

        g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 18));
        fm = g2.getFontMetrics();
        String restartText = "Pulsa R para reiniciar";
        x = (WIDTH - fm.stringWidth(restartText)) / 2;
        y = HEIGHT / 2 + 50;
        g2.drawString(restartText, x, y);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();

        if (code == KeyEvent.VK_LEFT) {
            leftPressed = true;
        }

        if (code == KeyEvent.VK_RIGHT) {
            rightPressed = true;
        }

        if (code == KeyEvent.VK_R && gameOver) {
            resetGame();
            repaint();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();

        if (code == KeyEvent.VK_LEFT) {
            leftPressed = false;
        }

        if (code == KeyEvent.VK_RIGHT) {
            rightPressed = false;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // No se usa
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Carrera simple");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            CarreraSimple game = new CarreraSimple();
            frame.add(game);

            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setResizable(false);
            frame.setVisible(true);

            game.requestFocusInWindow();
        });
    }
}