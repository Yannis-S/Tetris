import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ThreadLocalRandom;

/**
 * TetrisClone
 * Author: Yannis Seimenis
 * Date: 20/03/23
 * Description: A remake/remix of the classic tetris game built using Java and JavaSwing
 * TODO:
 * - Create Hard Drop Function
 * - Add music and sfx
 * - Create theme change function
 * - Refactor into multiple classes
 * - Create how to play page
 * TODO - KNOWN BUGS:
 * - Random tiles appearing in air sometimes cause game to finish!
 * - Clipping of images in next block queue
 */

public class TetrisClone {

    public static JFrame frame;
    public static JPanel gamePanel;
    public static JPanel[][] gameGrid;
    public static JPanel queuePanel;
    public static JLabel[] queuePicLabels;
    public static JLabel scoreLabel;
    public static JLabel backgroundLabel;
    public static JLabel holdImgLabel;
    public static JPanel scorePanel;
    public static JPanel holdPanel;
    public static JPanel gameOverPanel;
    public static JLabel gameOverScoreLabel;
    public static JLabel titleLabel;
    public static JPanel startPanel;
    public static JPanel levelPanel;
    public static JLabel levelLabel;
    public static JPanel linesPanel;
    public static JLabel linesLabel;
    public static JPanel pausePanel;
    public static JLabel pauseLabel;
    public static JPanel pauseCoverPanel;

    public static int[] blockQueue;
    public static int[][] currentBlock;
    public static int[][] previousBlockPos;
    public static int currentBlockType;
    public static int currentBlockRotation;
    public static int fallDelay = 1000;
    public static int currentHoldBlock = -1;
    public static int score = 0;
    public static int level = 0;
    public static int clearedLines = 0;
    public static int requiredLineClears = 10;
    public static int totalLines = 0;

    public static ImageIcon iBlockIcon;
    public static ImageIcon jBlockIcon;
    public static ImageIcon lBlockIcon;
    public static ImageIcon oBlockIcon;
    public static ImageIcon sBlockIcon;
    public static ImageIcon tBlockIcon;
    public static ImageIcon zBlockIcon;

    public static boolean fastFall = false;
    public static boolean running = false;
    public static boolean paused = false;

    public static Color currentBlockColor;

    public static ArrayList<int[]> setBlocks;

    public static Thread blockGravityThread;
    public static Thread titleLabelThread;

    public static Font pixelFont;

    /**
     * main(String[] args)
     * Main method to call all initialisation methods and start the game
     * @param args - Not used
     */
    public static void main(String[] args) {
        //Initialise all components
        initFrame();
        initNewFont();
        initStartMenu();
        initPauseButton();
        initGameOverMessage();
        initGamePanel();
        initGameGrid();
        initImageIcons();
        initQueuePanel();
        initScorePanel();
        initHoldPanel();
        initLevelPanel();
        initLinesPanel();

        initOther();
        initQueue();
        updateQueue();
        frame.setVisible(true);
        //Add background image
        try {
            backgroundLabel = new JLabel(new ImageIcon(ImageIO.read(new File("resources/background-2.png"))));
            backgroundLabel.setBounds(0,0, 402, 522);
            frame.add(backgroundLabel);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Start game
        //nextBlock();
        //running = true;
        //blockGravity();
    }

    /**
     * initFrame()
     * Initialises the main frame
     */
    public static void initFrame() {
        frame = new JFrame("TetrisClone");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(null);
        frame.setResizable(false);
        frame.setSize(418, 561);
        frame.addKeyListener(keyListener);
    }

    /**
     * initGameOverMessage()
     * Initialises game over message panel and its components
     */
    public static void initGameOverMessage() {
        gameOverPanel = new JPanel();
        gameOverPanel.setBounds(10, 110, 382, 200);
        gameOverPanel.setOpaque(false);
        gameOverPanel.setLayout(new BoxLayout(gameOverPanel, BoxLayout.Y_AXIS));

        JLabel gameOverLabel = new JLabel("Game Over");
        gameOverLabel.setFont(pixelFont.deriveFont(70f));
        gameOverLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        gameOverPanel.add(gameOverLabel);

        gameOverScoreLabel = new JLabel("Score: " + score);
        gameOverScoreLabel.setFont(pixelFont.deriveFont(40f));
        gameOverScoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        gameOverPanel.add(gameOverScoreLabel);

        JLabel playAgainLabel = new JLabel("Play Again?");
        playAgainLabel.setFont(pixelFont.deriveFont(40f));
        playAgainLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        gameOverPanel.add(playAgainLabel);

        playAgainLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                restartGame();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                playAgainLabel.setForeground(Color.WHITE);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                playAgainLabel.setForeground(new Color(51, 51, 51));
            }
        });
        gameOverPanel.setVisible(false);
        frame.add(gameOverPanel);
    }

    /**
     * initGamePanel()
     * Initialises the game panel which grid components will be added to
     */
    public static void initGamePanel() {
        gamePanel = new JPanel();
        gamePanel.setBounds(10, 10, 252, 502);
        gamePanel.setBorder(new LineBorder(Color.BLACK, 2));
        gamePanel.setOpaque(false);
        gamePanel.setLayout(null);
        gamePanel.setVisible(false);
        frame.add(gamePanel);
    }

    /**
     * initGameGrid()
     * Initialises the game grid (2D array) components
     */
    public static void initGameGrid() {
        gameGrid = new JPanel[10][20];
        int y = 1;
        for (int i = 0; i < 20; i++) {
            int x = 1;
            for (int j = 0; j < 10; j++) {
                gameGrid[j][i] = new JPanel();
                gameGrid[j][i].setBounds(x, y, 25, 25);
                gameGrid[j][i].setBackground(Color.WHITE);
                gameGrid[j][i].setOpaque(false);
                gamePanel.add(gameGrid[j][i]);
                x += 25;
            }
            y += 25;
        }
    }

    /**
     * initScorePanel()
     * Initialises score panel and its components
     */
    public static void initScorePanel() {
        scorePanel = new JPanel();
        scorePanel.setBounds(272, 10, 120, 30);
        scorePanel.setOpaque(false);
        scorePanel.setBorder(new LineBorder(Color.BLACK, 2));
        scoreLabel = new JLabel("Score: " + score);
        scoreLabel.setFont(pixelFont);
        scorePanel.add(scoreLabel);
        scorePanel.setVisible(false);
        frame.add(scorePanel);
    }

    public static void initLevelPanel() {
        levelPanel = new JPanel();
        levelPanel.setBounds(272, 50, 120, 30);
        levelPanel.setOpaque(false);
        levelPanel.setBorder(new LineBorder(Color.BLACK, 2));

        levelLabel = new JLabel("Level: 0");
        levelLabel.setFont(pixelFont);
        levelPanel.add(levelLabel);

        levelPanel.setVisible(false);
        frame.add(levelPanel);
    }

    public static void initLinesPanel() {
        linesPanel = new JPanel();
        linesPanel.setBounds(272, 90, 120, 30);
        linesPanel.setOpaque(false);
        linesPanel.setBorder(new LineBorder(Color.BLACK, 2));

        linesLabel = new JLabel("Lines: " + totalLines);
        linesLabel.setFont(pixelFont);
        linesPanel.add(linesLabel);

        linesPanel.setVisible(false);
        frame.add(linesPanel);
    }

    /**
     * initQueuePanel()
     * Initialises panel that holds queue components
     */
    public static void initQueuePanel() {
        queuePanel = new JPanel();
        queuePanel.setBounds(272, 130, 120, 190);
        queuePanel.setBorder(new LineBorder(Color.BLACK, 2));
        queuePanel.setLayout(null);
        queuePanel.setOpaque(false);
        queuePanel.setVisible(false);
        frame.add(queuePanel);
    }

    /**
     * initHoldPanel()
     * Initialises hold panel components
     */
    public static void initHoldPanel() {
        JLabel holdLabel = new JLabel("Hold Block (H)");
        holdLabel.setFont(pixelFont);
        //holdLabel.setBounds(270, 240, 120, 20);
        holdPanel = new JPanel();
        holdPanel.setBounds(272, 330, 120, 90);
        holdPanel.setBackground(null);
        holdPanel.setOpaque(false);
        holdPanel.setBorder(new LineBorder(Color.BLACK, 2));
        holdPanel.add(holdLabel);
        holdImgLabel = new JLabel();
        holdPanel.add(holdImgLabel);
        holdPanel.setVisible(false);
        frame.add(holdPanel);
    }

    /**
     * initQueue()
     * Initialises queue components and adds 3 random blocks to queue
     */
    public static void initQueue() {
        queuePicLabels  = new JLabel[3];
        queuePicLabels[0] = new JLabel();
        queuePicLabels[1] = new JLabel();
        queuePicLabels[2] = new JLabel();
        blockQueue = new int[3];
        for (int i = 0; i < 3; i++) {
            blockQueue[i] = ThreadLocalRandom.current().nextInt(0, 7);
        }
    }

    public static void initPauseButton() {
        pauseCoverPanel = new JPanel();
        pauseCoverPanel.setBounds(10, 10, 252, 502);
        pauseCoverPanel.setBorder(new LineBorder(Color.BLACK, 2));
        pauseCoverPanel.setBackground(new Color(51, 51, 51, 150));
        pauseCoverPanel.setVisible(false);
        frame.add(pauseCoverPanel);

        JLabel pauseCoverLabel = new JLabel("Paused");
        pauseCoverLabel.setFont(pixelFont.deriveFont(70f));
        pauseCoverLabel.setForeground(Color.WHITE);
        pauseCoverPanel.add(pauseCoverLabel);

        pausePanel = new JPanel();
        pausePanel.setBounds(272, 430, 120, 30);
        pausePanel.setOpaque(false);
        pausePanel.setBorder(new LineBorder(Color.BLACK, 2));

        pauseLabel = new JLabel("Pause (P)");
        pauseLabel.setFont(pixelFont.deriveFont(26f));
        pausePanel.add(pauseLabel);

        pausePanel.setVisible(false);
        frame.add(pausePanel);

        pausePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                pauseGame();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                pauseLabel.setForeground(Color.WHITE);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                pauseLabel.setForeground(new Color(51, 51, 51));
            }
        });


    }

    public static void pauseGame() {
        if (paused) {
            //Resume
            pauseCoverPanel.setVisible(false);
            paused = false;
            running = true;
            blockGravity();
            pauseLabel.setText("Pause (P)");
        } else {
            //Pause
            pauseCoverPanel.setVisible(true);
            paused = true;
            running = false;
            blockGravityThread.interrupt();
            pauseLabel.setText("Resume (P)");
        }
    }


    /**
     * initNewFont()
     * Initialises new pixel style font
     */
    public static void initNewFont() {
        try {
            File fontFile = new File("resources/pixeboy-font.ttf");
            Font newFont = Font.createFont(Font.TRUETYPE_FONT, fontFile);
            pixelFont = newFont.deriveFont(20f);
        } catch (IOException | FontFormatException e) {
            e.printStackTrace();
        }
    }

    public static void initImageIcons() {
        try {
            iBlockIcon = new ImageIcon(ImageIO.read(new File("resources/IBlock.png")));
            jBlockIcon = new ImageIcon(ImageIO.read(new File("resources/JBlock.png")));
            lBlockIcon = new ImageIcon(ImageIO.read(new File("resources/LBlock.png")));
            oBlockIcon = new ImageIcon(ImageIO.read(new File("resources/OBlock.png")));
            sBlockIcon = new ImageIcon(ImageIO.read(new File("resources/SBlock.png")));
            tBlockIcon = new ImageIcon(ImageIO.read(new File("resources/TBlock.png")));
            zBlockIcon = new ImageIcon(ImageIO.read(new File("resources/ZBlock.png")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * initOther()
     * Initialises other needed components
     */
    public static void initOther() {
        currentBlock = new int[4][2];
        previousBlockPos = new int[4][2];
        setBlocks = new ArrayList<>();
    }

    /**
     * keyListener
     * Listens for keystrokes then calls appropriate method
     */
    public static KeyListener keyListener = new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == 37 || e.getKeyCode() == 65) {
                moveSide(0);
            } else if (e.getKeyCode() == 39 || e.getKeyCode() == 68) {
                moveSide(1);
            } else if (e.getKeyCode() == 40 || e.getKeyCode() == 83) {
                fastFall = true;
            } else if (e.getKeyCode() == 38 || e.getKeyCode() == 87) {
                rotateBlock();
            } else if (e.getKeyCode() == 72) {
                holdBlock();
            } else if (e.getKeyCode() == 80) {
                pauseGame();
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            if (e.getKeyCode() == 40 || e.getKeyCode() == 83) {
                fastFall = false;
            }
        }
    };

    /**
     * updateQueue()
     * Updates next block queue images from files
     */
    public static void updateQueue() {
        Thread queueThread = new Thread(() -> {
            for (int i = 0; i < 3; i++) {
                switch (blockQueue[i]) {
                    case 0 -> {
                        queuePicLabels[i].setIcon(iBlockIcon);
                        queuePicLabels[i].setBounds(10, 22 + (i * 60), 100, 25);
                        queuePanel.add(queuePicLabels[i]);
                    }
                    case 1 -> {
                        queuePicLabels[i].setIcon(jBlockIcon);
                        queuePicLabels[i].setBounds(22, 10 + (i * 60), 75, 50);
                        queuePanel.add(queuePicLabels[i]);
                    }
                    case 2 -> {
                        queuePicLabels[i].setIcon(lBlockIcon);
                        queuePicLabels[i].setBounds(22, 10 + (i * 60), 75, 50);
                        queuePanel.add(queuePicLabels[i]);
                    }
                    case 3 -> {
                        queuePicLabels[i].setIcon(oBlockIcon);
                        queuePicLabels[i].setBounds(35, 10 + (i * 60), 50, 50);
                        queuePanel.add(queuePicLabels[i]);
                    }
                    case 4 -> {
                        queuePicLabels[i].setIcon(sBlockIcon);
                        queuePicLabels[i].setBounds(22, 10 + (i * 60), 75, 50);
                        queuePanel.add(queuePicLabels[i]);
                    }
                    case 5 -> {
                        queuePicLabels[i].setIcon(tBlockIcon);
                        queuePicLabels[i].setBounds(22, 10 + (i * 60), 75, 50);
                        queuePanel.add(queuePicLabels[i]);
                    }
                    case 6 -> {
                        queuePicLabels[i].setIcon(zBlockIcon);
                        queuePicLabels[i].setBounds(22, 10 + (i * 60), 75, 50);
                        queuePanel.add(queuePicLabels[i]);
                    }
                }
            }
        });
        queueThread.start();
    }

    /**
     * shuffleAndAddToQueue()
     * Shuffles the next block queue and adds a new random block
     */
    public static void shuffleAndAddToQueue() {
        blockQueue[0] = blockQueue[1];
        blockQueue[1] = blockQueue[2];
        blockQueue[2] = ThreadLocalRandom.current().nextInt(0, 7);
        updateQueue();
    }

    /**
     * newCurrentBlock()
     * Sets the next/new blocks position and color at the top of the game grid
     */
    public static void newCurrentBlock() {
        /* blockNo reference:
            0 - iBlock
            1 - jBlock
            2 - lBlock
            3 - oBlock
            4 - sBlock
            5 - tBlock
            6 - zBlock
         */
        switch (currentBlockType) {
            case 0 -> {
                currentBlock[0] = new int[]{3, 0};
                currentBlock[1] = new int[]{4, 0};
                currentBlock[2] = new int[]{5, 0};
                currentBlock[3] = new int[]{6, 0};
                currentBlockColor = new Color(247, 202, 208);
            }
            case 1 -> {
                currentBlock[0] = new int[]{3, 0};
                currentBlock[1] = new int[]{3, 1};
                currentBlock[2] = new int[]{4, 1};
                currentBlock[3] = new int[]{5, 1};
                currentBlockColor = new Color(249, 190, 199);
            }
            case 2 -> {
                currentBlock[0] = new int[]{6, 0};
                currentBlock[1] = new int[]{4, 1};
                currentBlock[2] = new int[]{5, 1};
                currentBlock[3] = new int[]{6, 1};
                currentBlockColor = new Color(251, 177, 189);
            }
            case 3 -> {
                currentBlock[0] = new int[]{4, 0};
                currentBlock[1] = new int[]{5, 0};
                currentBlock[2] = new int[]{4, 1};
                currentBlock[3] = new int[]{5, 1};
                currentBlockColor = new Color(255, 153, 172);
            }
            case 4 -> {
                currentBlock[0] = new int[]{5, 0};
                currentBlock[1] = new int[]{6, 0};
                currentBlock[2] = new int[]{4, 1};
                currentBlock[3] = new int[]{5, 1};
                currentBlockColor = new Color(255, 133, 161);
            }
            case 5 -> {
                currentBlock[0] = new int[]{4, 0};
                currentBlock[1] = new int[]{3, 1};
                currentBlock[2] = new int[]{4, 1};
                currentBlock[3] = new int[]{5, 1};
                currentBlockColor = new Color(255, 112, 150);
            }
            case 6 -> {
                currentBlock[0] = new int[]{4, 0};
                currentBlock[1] = new int[]{5, 0};
                currentBlock[2] = new int[]{5, 1};
                currentBlock[3] = new int[]{6, 1};
                currentBlockColor = new Color(255, 92, 138);
            }
        }
        currentBlockRotation = 0;
    }

    /**
     * updateCurrentBlock(boolean isNewBlock)
     * Updates grid based on blocks new position
     * @param isNewBlock - Specifies if the block is a new block at top of grid
     */
    public static void updateCurrentBlock(boolean isNewBlock) {
        if (!isNewBlock) {
            for (int i = 0; i < 4; i++) {
                gameGrid[previousBlockPos[i][0]][previousBlockPos[i][1]].setBackground(Color.WHITE);
                gameGrid[previousBlockPos[i][0]][previousBlockPos[i][1]].setBorder(null);
                gameGrid[previousBlockPos[i][0]][previousBlockPos[i][1]].setOpaque(false);
            }
        }
        for (int i = 0; i < 4; i++) {
            gameGrid[currentBlock[i][0]][currentBlock[i][1]].setOpaque(true);
            gameGrid[currentBlock[i][0]][currentBlock[i][1]].setBackground(currentBlockColor);
            gameGrid[currentBlock[i][0]][currentBlock[i][1]].setBorder(new LineBorder(Color.BLACK));
        }
    }

    /**
     * blockGravity()
     * Moves the current block down while game is running in a new thread
     */
    public static void blockGravity() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {}
        blockGravityThread = new Thread(() -> {
            while (running) {
                if (!isTouchingBottomOrBlock()) {
                    //Copy array
                    for (int i = 0; i < currentBlock.length; i++) {
                        previousBlockPos[i] = Arrays.copyOf(currentBlock[i], currentBlock[i].length);
                    }
                    for (int i = 0; i < 4; i++) {
                        currentBlock[i][1]++;
                    }
                    updateCurrentBlock(false);
                    try {
                        for (int i = 0; i < fallDelay/50 && !fastFall; i++) {
                            Thread.sleep(50);
                        }
                        if (fastFall) {
                            Thread.sleep(50);
                        }
                    } catch (InterruptedException ignored) {
                    }
                } else {
                    addToSetBlock();
                    checkForFullRows();
                    if (!checkIfGameOver()) {
                        nextBlock();
                    }
                }
            }
        });
        blockGravityThread.start();
    }

    /**
     * checkIfGameOver()
     * Checks if blocks set on top border and ends game if true
     * @return - Returns true if game is over
     */
    public static boolean checkIfGameOver() {
        for (int i = 0; i < 4; i++) {
            if (currentBlock[i][1] == 0) {
                //Game over
                running = false;
                blockGravityThread.interrupt();
                gameOverAnimation();
                showGameOverMessage();
                return true;
            }
        }
        return false;
    }

    /**
     * moveSide(int dir)
     * Moves the current block side to side when key is pressed
     * @param dir - Direction of movement specified from keyListener
     */
    public static void moveSide(int dir) {
        if (!isTouchingSideOrBlock(dir)) {
            //Copy array
            for (int i = 0; i < currentBlock.length; i++) {
                previousBlockPos[i] = Arrays.copyOf(currentBlock[i], currentBlock[i].length);
            }
            for (int i = 0; i < 4; i++) {
                if (dir == 0) {
                    currentBlock[i][0]--;
                } else if (dir == 1) {
                    currentBlock[i][0]++;
                }
            }
            updateCurrentBlock(false);
        }
    }

    /**
     * isTouchingSideOrBlock(int dir)
     * Checks if the current blocks sides are touching borders or another block
     * @param dir - Direction to check specified from moveSide method
     * @return - Returns true if block is touching something
     */
    public static boolean isTouchingSideOrBlock(int dir) {
        for (int i = 0; i < 4; i++) {
            if (dir == 0) {
                if (currentBlock[i][0] == 0) {
                    return true;
                } else {
                    for (int[] setBlock : setBlocks) {
                        if (Arrays.equals(new int[]{currentBlock[i][0] - 1, currentBlock[i][1]}, setBlock)) {
                            return true;
                        }
                    }
                }
            } else if (dir == 1) {
                if (currentBlock[i][0] == 9) {
                    return true;
                } else {
                    for (int[] setBlock : setBlocks) {
                        if (Arrays.equals(new int[]{currentBlock[i][0] + 1, currentBlock[i][1]}, setBlock)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * isTouchingBottomOrBlock()
     * Checks if the bottom of current block is touching the bottom or another block
     * @return - Returns true if block base is touching something
     */
    public static boolean isTouchingBottomOrBlock() {
        for (int i = 0; i < 4; i++) {
            if (currentBlock[i][1] == 19) {
                return true;
            } else {
                for (int[] setBlock : setBlocks) {
                    if (Arrays.equals(new int[]{currentBlock[i][0], currentBlock[i][1] + 1}, setBlock)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * nextBlock()
     * Sets the next current block from the queue
     */
    public static void nextBlock() {
        currentBlockType = blockQueue[0];
        shuffleAndAddToQueue();
        newCurrentBlock();
        updateCurrentBlock(true);
    }

    /**
     * addToSetBlock()
     * Adds the current position of stationary block to the setBlock arrayList
     */
    public static void addToSetBlock() {
        for (int i = 0; i < 4; i++) {
            setBlocks.add(new int[]{currentBlock[i][0], currentBlock[i][1]});
        }
    }

    /**
     * rotateBlock()
     * Sets the new rotation and position of a block, triggered by keyListener
     */
    public static void rotateBlock() {
        int[][] rotateLoc = new int[4][2];
        int newBlockRotation = 0;
        switch (currentBlockType) {
            case 0 -> {
                if (currentBlockRotation == 0) {
                    rotateLoc[0][0] = currentBlock[0][0] + 2;
                    rotateLoc[0][1] = currentBlock[0][1] - 2;
                    rotateLoc[1][0] = currentBlock[1][0] + 1;
                    rotateLoc[1][1] = currentBlock[1][1] - 1;
                    rotateLoc[2][0] = currentBlock[2][0];
                    rotateLoc[2][1] = currentBlock[2][1];
                    rotateLoc[3][0] = currentBlock[3][0] - 1;
                    rotateLoc[3][1] = currentBlock[3][1] + 1;
                    newBlockRotation = 1;
                } else if (currentBlockRotation == 1) {
                    rotateLoc[0][0] = currentBlock[0][0] - 2;
                    rotateLoc[0][1] = currentBlock[0][1] + 2;
                    rotateLoc[1][0] = currentBlock[1][0] - 1;
                    rotateLoc[1][1] = currentBlock[1][1] + 1;
                    rotateLoc[2][0] = currentBlock[2][0];
                    rotateLoc[2][1] = currentBlock[2][1];
                    rotateLoc[3][0] = currentBlock[3][0] + 1;
                    rotateLoc[3][1] = currentBlock[3][1] - 1;
                }
            }
            case 1 -> {
                if (currentBlockRotation == 0) {
                    rotateLoc[0][0] = currentBlock[0][0] + 2;
                    rotateLoc[0][1] = currentBlock[0][1];
                    rotateLoc[1][0] = currentBlock[1][0] + 1;
                    rotateLoc[1][1] = currentBlock[1][1] - 1;
                    rotateLoc[2][0] = currentBlock[2][0];
                    rotateLoc[2][1] = currentBlock[2][1];
                    rotateLoc[3][0] = currentBlock[3][0] - 1;
                    rotateLoc[3][1] = currentBlock[3][1] + 1;
                    newBlockRotation = 1;
                } else if (currentBlockRotation == 1) {
                    rotateLoc[0][0] = currentBlock[0][0];
                    rotateLoc[0][1] = currentBlock[0][1] + 2;
                    rotateLoc[1][0] = currentBlock[1][0] + 1;
                    rotateLoc[1][1] = currentBlock[1][1] + 1;
                    rotateLoc[2][0] = currentBlock[2][0];
                    rotateLoc[2][1] = currentBlock[2][1];
                    rotateLoc[3][0] = currentBlock[3][0] - 1;
                    rotateLoc[3][1] = currentBlock[3][1] - 1;
                    newBlockRotation = 2;
                } else if (currentBlockRotation == 2) {
                    rotateLoc[0][0] = currentBlock[0][0] - 2;
                    rotateLoc[0][1] = currentBlock[0][1];
                    rotateLoc[1][0] = currentBlock[1][0] - 1;
                    rotateLoc[1][1] = currentBlock[1][1] + 1;
                    rotateLoc[2][0] = currentBlock[2][0];
                    rotateLoc[2][1] = currentBlock[2][1];
                    rotateLoc[3][0] = currentBlock[3][0] + 1;
                    rotateLoc[3][1] = currentBlock[3][1] - 1;
                    newBlockRotation = 3;
                } else if (currentBlockRotation == 3) {
                    rotateLoc[0][0] = currentBlock[0][0];
                    rotateLoc[0][1] = currentBlock[0][1] - 2;
                    rotateLoc[1][0] = currentBlock[1][0] - 1;
                    rotateLoc[1][1] = currentBlock[1][1] - 1;
                    rotateLoc[2][0] = currentBlock[2][0];
                    rotateLoc[2][1] = currentBlock[2][1];
                    rotateLoc[3][0] = currentBlock[3][0] + 1;
                    rotateLoc[3][1] = currentBlock[3][1] + 1;
                }
            }
            case 2 -> {
                if (currentBlockRotation == 0) {
                    rotateLoc[0][0] = currentBlock[0][0];
                    rotateLoc[0][1] = currentBlock[0][1] + 2;
                    rotateLoc[1][0] = currentBlock[1][0] + 1;
                    rotateLoc[1][1] = currentBlock[1][1] + 1;
                    rotateLoc[2][0] = currentBlock[2][0];
                    rotateLoc[2][1] = currentBlock[2][1];
                    rotateLoc[3][0] = currentBlock[3][0] - 1;
                    rotateLoc[3][1] = currentBlock[3][1] - 1;
                    newBlockRotation = 1;
                } else if (currentBlockRotation == 1) {
                    rotateLoc[0][0] = currentBlock[0][0] - 2;
                    rotateLoc[0][1] = currentBlock[0][1];
                    rotateLoc[1][0] = currentBlock[1][0] - 1;
                    rotateLoc[1][1] = currentBlock[1][1] - 1;
                    rotateLoc[2][0] = currentBlock[2][0];
                    rotateLoc[2][1] = currentBlock[2][1];
                    rotateLoc[3][0] = currentBlock[3][0] + 1;
                    rotateLoc[3][1] = currentBlock[3][1] + 1;
                    newBlockRotation = 2;
                } else if (currentBlockRotation == 2) {
                    rotateLoc[0][0] = currentBlock[0][0];
                    rotateLoc[0][1] = currentBlock[0][1] - 2;
                    rotateLoc[1][0] = currentBlock[1][0] + 1;
                    rotateLoc[1][1] = currentBlock[1][1] - 1;
                    rotateLoc[2][0] = currentBlock[2][0];
                    rotateLoc[2][1] = currentBlock[2][1];
                    rotateLoc[3][0] = currentBlock[3][0] - 1;
                    rotateLoc[3][1] = currentBlock[3][1] + 1;
                    newBlockRotation = 3;
                } else if (currentBlockRotation == 3) {
                    rotateLoc[0][0] = currentBlock[0][0] + 2;
                    rotateLoc[0][1] = currentBlock[0][1];
                    rotateLoc[1][0] = currentBlock[1][0] - 1;
                    rotateLoc[1][1] = currentBlock[1][1] + 1;
                    rotateLoc[2][0] = currentBlock[2][0];
                    rotateLoc[2][1] = currentBlock[2][1];
                    rotateLoc[3][0] = currentBlock[3][0] + 1;
                    rotateLoc[3][1] = currentBlock[3][1] - 1;
                }
            }
            case 4 -> {
                if (currentBlockRotation == 0) {
                    rotateLoc[0][0] = currentBlock[0][0];
                    rotateLoc[0][1] = currentBlock[0][1];
                    rotateLoc[1][0] = currentBlock[1][0];
                    rotateLoc[1][1] = currentBlock[1][1] + 1;
                    rotateLoc[2][0] = currentBlock[2][0] + 2;
                    rotateLoc[2][1] = currentBlock[2][1] + 1;
                    rotateLoc[3][0] = currentBlock[3][0];
                    rotateLoc[3][1] = currentBlock[3][1];
                    newBlockRotation = 1;
                } else if (currentBlockRotation == 1) {
                    rotateLoc[0][0] = currentBlock[0][0];
                    rotateLoc[0][1] = currentBlock[0][1];
                    rotateLoc[1][0] = currentBlock[1][0];
                    rotateLoc[1][1] = currentBlock[1][1] - 1;
                    rotateLoc[2][0] = currentBlock[2][0] - 2;
                    rotateLoc[2][1] = currentBlock[2][1] - 1;
                    rotateLoc[3][0] = currentBlock[3][0];
                    rotateLoc[3][1] = currentBlock[3][1];
                }
            }
            case 5 -> {
                if (currentBlockRotation == 0) {
                    rotateLoc[0][0] = currentBlock[0][0];
                    rotateLoc[0][1] = currentBlock[0][1];
                    rotateLoc[1][0] = currentBlock[1][0] + 1;
                    rotateLoc[1][1] = currentBlock[1][1] + 1;
                    rotateLoc[2][0] = currentBlock[2][0];
                    rotateLoc[2][1] = currentBlock[2][1];
                    rotateLoc[3][0] = currentBlock[3][0];
                    rotateLoc[3][1] = currentBlock[3][1];
                    newBlockRotation = 1;
                } else if (currentBlockRotation == 1) {
                    rotateLoc[0][0] = currentBlock[0][0] - 1;
                    rotateLoc[0][1] = currentBlock[0][1] + 1;
                    rotateLoc[1][0] = currentBlock[1][0];
                    rotateLoc[1][1] = currentBlock[1][1];
                    rotateLoc[2][0] = currentBlock[2][0];
                    rotateLoc[2][1] = currentBlock[2][1];
                    rotateLoc[3][0] = currentBlock[3][0];
                    rotateLoc[3][1] = currentBlock[3][1];
                    newBlockRotation = 2;
                } else if (currentBlockRotation == 2) {
                    rotateLoc[0][0] = currentBlock[0][0];
                    rotateLoc[0][1] = currentBlock[0][1];
                    rotateLoc[1][0] = currentBlock[1][0];
                    rotateLoc[1][1] = currentBlock[1][1];
                    rotateLoc[2][0] = currentBlock[2][0];
                    rotateLoc[2][1] = currentBlock[2][1];
                    rotateLoc[3][0] = currentBlock[3][0] - 1;
                    rotateLoc[3][1] = currentBlock[3][1] - 1;
                    newBlockRotation = 3;
                } else if (currentBlockRotation == 3) {
                    rotateLoc[0][0] = currentBlock[0][0] + 1;
                    rotateLoc[0][1] = currentBlock[0][1] - 1;
                    rotateLoc[1][0] = currentBlock[1][0] - 1;
                    rotateLoc[1][1] = currentBlock[1][1] - 1;
                    rotateLoc[2][0] = currentBlock[2][0];
                    rotateLoc[2][1] = currentBlock[2][1];
                    rotateLoc[3][0] = currentBlock[3][0] + 1;
                    rotateLoc[3][1] = currentBlock[3][1] + 1;
                }
            }
            case 6 -> {
                if (currentBlockRotation == 0) {
                    rotateLoc[0][0] = currentBlock[0][0] + 1;
                    rotateLoc[0][1] = currentBlock[0][1] + 2;
                    rotateLoc[1][0] = currentBlock[1][0] + 1;
                    rotateLoc[1][1] = currentBlock[1][1];
                    rotateLoc[2][0] = currentBlock[2][0];
                    rotateLoc[2][1] = currentBlock[2][1];
                    rotateLoc[3][0] = currentBlock[3][0];
                    rotateLoc[3][1] = currentBlock[3][1];
                    newBlockRotation = 1;
                } else if (currentBlockRotation == 1) {
                    rotateLoc[0][0] = currentBlock[0][0] - 1;
                    rotateLoc[0][1] = currentBlock[0][1] - 2;
                    rotateLoc[1][0] = currentBlock[1][0] - 1;
                    rotateLoc[1][1] = currentBlock[1][1];
                    rotateLoc[2][0] = currentBlock[2][0];
                    rotateLoc[2][1] = currentBlock[2][1];
                    rotateLoc[3][0] = currentBlock[3][0];
                    rotateLoc[3][1] = currentBlock[3][1];
                }
            }
        }
        boolean flag = false;
        //Check if new position is valid
        for (int[] setBlock : setBlocks) {
            for (int j = 0; j < 4; j++) {
                if (Arrays.equals(new int[]{rotateLoc[j][0], rotateLoc[j][1]}, setBlock)) {
                    flag = true;
                } else if (rotateLoc[j][0] < 0 || rotateLoc[j][0] > 9) {
                    flag = true;
                } else if (rotateLoc[j][1] < 0 || rotateLoc[j][1] > 19) {
                    flag = true;
                }
            }
        }
        if (currentBlockType == 3) {
            flag = true;
        }
        //If valid update current position to new rotation position
        if (!flag) {
            //Copy array
            for (int i = 0; i < currentBlock.length; i++) {
                previousBlockPos[i] = Arrays.copyOf(currentBlock[i], currentBlock[i].length);
            }
            for (int i = 0; i < 4; i++) {
                currentBlock[i][0] = rotateLoc[i][0];
                currentBlock[i][1] = rotateLoc[i][1];
            }
            currentBlockRotation = newBlockRotation;
            updateCurrentBlock(false);
        }

    }

    /**
     * checkForFullRows()
     * Checks for full rows, updates score then sends to removal method
     */
    public static void checkForFullRows() {
        ArrayList<Integer> rowsToRemove = new ArrayList<>();
        for (int i = 19; i > -1; i--) {
            int count = 0;
            for (int j = 0; j < 10; j++) {
                for (int[] setBlock : setBlocks) {
                    if (Arrays.equals(new int[]{j, i}, setBlock)) {
                        count++;
                    }
                }
            }
            if (count == 10) {
                rowsToRemove.add(i);
            }
        }
        if (rowsToRemove.size() > 0) {
            removeRows(rowsToRemove);
            //Update score (Scoring based on original BPS version of tetris)
            updateScoreAndLevel(rowsToRemove.size());
        }

    }

    public static void updateScoreAndLevel(int rowToRemoveCount) {
        switch (rowToRemoveCount) {
            case 1 -> addToScore(40);
            case 2 -> addToScore(100);
            case 3 -> addToScore(300);
            case 4 -> addToScore(1200);
        }
        totalLines += rowToRemoveCount;
        updateTotalLines();
        clearedLines += rowToRemoveCount;
        if (clearedLines >= requiredLineClears) {
            clearedLines = clearedLines - requiredLineClears;
            increaseLevel();
        }
    }

    public static void updateTotalLines() {
        linesLabel.setText("Lines: " + totalLines);
    }

    public static void increaseLevel() {
        level++;
        levelLabel.setText("Level: " + level);
        if (level < 10) {
            fallDelay -= 100;
        } else if (level == 10) {
            fallDelay -= 50;
        } else {
            fallDelay -= 10;
        }
        requiredLineClears = level * 10;
    }

    /**
     * removeRows(ArrayList<Integer> rowsToRemove)
     * Removes rows from game grid and shuffles any rows above down
     * @param rowsToRemove- ArrayList of rows to remove
     */
    public static void removeRows(ArrayList<Integer> rowsToRemove) {
        //Clear line animation
        for (Integer i : rowsToRemove) {
            int green = 250;
            int blue = 185;
            for (int j = 0; j < 10; j++) {
                gameGrid[j][i].setBackground(new Color(250, green, blue));
                green -= 10;
                blue -= 10;
                try {
                    Thread.sleep(15);
                } catch (InterruptedException ignored) {}
            }
        }
        //Remove line
        for (Integer i: rowsToRemove) {
            for (int j = 0; j < 10; j++) {
                //Remove row blocks from setBlocks arraylist
                for (int k = 0; k < setBlocks.size(); k++) {
                    if (Arrays.equals(new int[]{j, i}, setBlocks.get(k))) {
                        setBlocks.remove(k);
                    }
                    //Remove from grid
                    gameGrid[j][i].setBackground(Color.WHITE);
                    gameGrid[j][i].setBorder(null);
                    gameGrid[j][i].setOpaque(false);
                }
            }
        }
        //Shuffle above removed line
        Collections.reverse(rowsToRemove);
        for (Integer i: rowsToRemove) {
            //Copy and remove grid colors above remove line
            Color[][] gridColors = new Color[10][i];
            for (int j = 0; j < i; j++) {
                for (int k = 0; k < 10; k++) {
                    //COPY
                    gridColors[k][j] = gameGrid[k][j].getBackground();
                    //REMOVE
                    gameGrid[k][j].setBackground(Color.WHITE);
                    gameGrid[k][j].setBorder(null);
                    gameGrid[k][j].setOpaque(false);
                }
            }
            //Repaint colors
            for (int j = 0; j < i; j++) {
                for (int k = 0; k < 10; k++) {
                    if (gridColors[k][j] != Color.WHITE) {
                        gameGrid[k][j + 1].setBackground(gridColors[k][j]);
                        gameGrid[k][j + 1].setBorder(new LineBorder(Color.BLACK));
                        gameGrid[k][j + 1].setOpaque(true);
                    }
                }
            }
            //Remove old setBlocks
            setBlocks.clear();
            //Add new setBlocks
            for (int j = 0; j < 20; j++) {
                for (int k = 0; k < 10; k++) {
                    if (gameGrid[k][j].getBackground() != Color.WHITE) {
                        setBlocks.add(new int[]{k, j});
                    }
                }
            }
        }
    }

    /**
     * holdBlock()
     * Holds a block when key is pressed and sets current hold block to active block
     */
    public static void holdBlock() {
        if (currentHoldBlock != -1) {
            for (int i = 0; i < 4; i++) {
                gameGrid[currentBlock[i][0]][currentBlock[i][1]].setBackground(Color.WHITE);
                gameGrid[currentBlock[i][0]][currentBlock[i][1]].setBorder(null);
                gameGrid[currentBlock[i][0]][currentBlock[i][1]].setOpaque(false);
            }
            int temp = currentBlockType;
            currentBlockType = currentHoldBlock;
            currentHoldBlock = temp;
            updateHoldImage();
            newCurrentBlock();
            updateCurrentBlock(true);
        } else {
            currentHoldBlock = currentBlockType;
            updateHoldImage();
            for (int i = 0; i < 4; i++) {
                gameGrid[currentBlock[i][0]][currentBlock[i][1]].setBackground(Color.WHITE);
                gameGrid[currentBlock[i][0]][currentBlock[i][1]].setBorder(null);
                gameGrid[currentBlock[i][0]][currentBlock[i][1]].setOpaque(false);
            }
            nextBlock();
        }
    }

    /**
     * updateHoldImage()
     * Updates the image in the hold frame
     */
    public static void updateHoldImage() {
        if (currentHoldBlock != -1) {
            ImageIcon icon = switch (currentHoldBlock) {
                case 0 -> iBlockIcon;
                case 1 -> jBlockIcon;
                case 2 -> lBlockIcon;
                case 3 -> oBlockIcon;
                case 4 -> sBlockIcon;
                case 5 -> tBlockIcon;
                case 6 -> zBlockIcon;
                default -> null;
            };
            holdImgLabel.setIcon(icon);
        } else {
            holdImgLabel.setIcon(null);
        }
    }

    /**
     * addToScore(int n)
     * Adds n points to current score
     * @param n - Number of points to add to score
     */
    public static void addToScore(int n) {
        score += n;
        scoreLabel.setText("Score: " + score);
    }

    /**
     * gameOverAnimation()
     * Displays animation when game is over
     */
    public static void gameOverAnimation() {
        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 10; j++) {
                if (gameGrid[j][i].getBackground() != Color.WHITE) {
                    int red = ThreadLocalRandom.current().nextInt(0, 256);
                    int green = ThreadLocalRandom.current().nextInt(0, 256);
                    int blue = ThreadLocalRandom.current().nextInt(0, 256);
                    gameGrid[j][i].setBackground(new Color(red, green, blue));
                    try {
                        Thread.sleep(25);
                    } catch (InterruptedException ignored) {}
                    gameGrid[j][i].setBackground(Color.WHITE);
                    gameGrid[j][i].setBorder(null);
                    gameGrid[j][i].setOpaque(false);
                }
            }
        }
    }

    /**
     * showGameOverMessage()
     * Shows game over message panel and hides all other panels
     */
    public static void showGameOverMessage() {
        gamePanel.setVisible(false);
        queuePanel.setVisible(false);
        scorePanel.setVisible(false);
        holdPanel.setVisible(false);
        levelPanel.setVisible(false);
        linesPanel.setVisible(false);
        pausePanel.setVisible(false);
        gameOverScoreLabel.setText("Score: " + score);
        gameOverPanel.setVisible(true);
    }

    /**
     * restartGame()
     * Resets and restarts the game
     */
    public static void restartGame() {
        gameOverPanel.setVisible(false);
        gamePanel.setVisible(true);
        queuePanel.setVisible(true);
        scorePanel.setVisible(true);
        holdPanel.setVisible(true);
        levelPanel.setVisible(true);
        linesPanel.setVisible(true);
        pausePanel.setVisible(true);
        setBlocks.clear();
        score = 0;
        level = -1;
        increaseLevel();
        requiredLineClears = 10;
        clearedLines = 0;
        addToScore(0);
        totalLines = 0;
        updateTotalLines();
        currentHoldBlock = -1;
        fallDelay = 1000;
        updateHoldImage();
        updateQueue();
        nextBlock();
        running = true;
        blockGravity();
    }

    public static void initStartMenu() {
        startPanel = new JPanel();
        startPanel.setBounds(10, 110, 382, 200);
        startPanel.setOpaque(false);
        startPanel.setLayout(new BoxLayout(startPanel, BoxLayout.Y_AXIS));

        titleLabel = new JLabel("Tetris");
        titleLabel.setFont(pixelFont);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setFont(pixelFont.deriveFont(70f));
        startPanel.add(titleLabel);

        JLabel startGameLabel = new JLabel("Start");
        startGameLabel.setFont(pixelFont);
        startGameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        startGameLabel.setFont(pixelFont.deriveFont(40f));
        startPanel.add(startGameLabel);

        JLabel switchThemeLabel = new JLabel("Switch Theme");
        switchThemeLabel.setFont(pixelFont);
        switchThemeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        switchThemeLabel.setFont(pixelFont.deriveFont(40f));
        startPanel.add(switchThemeLabel);

        startGameLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                startGame();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                startGameLabel.setForeground(Color.WHITE);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                startGameLabel.setForeground(new Color(51, 51,51));
            }
        });

        switchThemeLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                //switch theme
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                switchThemeLabel.setForeground(Color.WHITE);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                switchThemeLabel.setForeground(new Color(51, 51, 51));
            }
        });

        frame.add(startPanel);

        animateTitleLabel();

    }

    public static void animateTitleLabel() {
        titleLabelThread = new Thread(() -> {
            int count = 0;
            while (!running) {
                if (count < 5) {
                    titleLabel.setLocation(titleLabel.getX(), titleLabel.getY() + 1);
                } else if (count < 9) {
                    titleLabel.setLocation(titleLabel.getX(), titleLabel.getY() - 1);
                } else {
                    count = 0;
                }
            try {
               Thread.sleep(100);
            } catch (InterruptedException ignored) {}
            count++;
            }
        });
        titleLabelThread.start();
    }

    public static void startGame() {
        startPanel.setVisible(false);
        titleLabelThread.interrupt();
        gamePanel.setVisible(true);
        scorePanel.setVisible(true);
        queuePanel.setVisible(true);
        holdPanel.setVisible(true);
        levelPanel.setVisible(true);
        linesPanel.setVisible(true);
        pausePanel.setVisible(true);
        nextBlock();
        running = true;
        blockGravity();
    }


}
