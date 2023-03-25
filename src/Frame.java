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
 * Class: Frame
 * Author: Yannis Seimenis
 * Description: Frame class controls all visual aspects of the game.
 * TODO - BUGS:
 * - contentPane width/height different on laptop and desktop! (Make dynamic?)
 * - Queue shows first block when game started
 */

public class Frame {
    /**
     * Global variables
     */
    private JFrame jFrame;

    private JPanel startPanel;
    private JPanel gamePanel;
    private JPanel gridPanel;
    private JPanel queuePanel;
    private JPanel gameOverPanel;
    private JPanel pauseCoverPanel;
    private JPanel[][] gameGrid;

    private JLabel titleLabel;
    private JLabel backgroundLabel;
    private JLabel scoreLabel;
    private JLabel levelLabel;
    private JLabel linesLabel;
    private JLabel holdImgLabel;
    private JLabel gameOverScoreLabel;
    private JLabel[] queuePicLabels;

    private Font pixelFont;

    private ImageIcon iBlockIcon;
    private ImageIcon jBlockIcon;
    private ImageIcon lBlockIcon;
    private ImageIcon oBlockIcon;
    private ImageIcon sBlockIcon;
    private ImageIcon tBlockIcon;
    private ImageIcon zBlockIcon;

    private Thread titleLabelThread;
    private Thread blockGravityThread;

    private int currentBackground = 0;

    private boolean animateTitle = true;

    private final Game game;

    /**
     * Frame(Game newGame)
     * Frame constructor, initialises all java swing components and new game object
     * @param newGame - Game object which controls variables inside the frame
     */
    public Frame(Game newGame) {
        game = newGame;

        initFrame();
        initFont();
        initStartPanel();
        initGameOverMessage();
        initGamePanel();
        initPausePanel();
        initGridPanel();
        initGameGrid();
        initScorePanel();
        initLevelPanel();
        initLinesPanel();
        initImageIcons();
        initQueuePanel();
        initHoldPanel();

        initFrameBackground();

        jFrame.setVisible(true);
    }

    //region Initialisation Methods

    /**
     * initFrame()
     * Initialises JFrame component
     */
    private void initFrame() {
        jFrame = new JFrame("Tetris");
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.setLayout(null);
        jFrame.setResizable(false);
        jFrame.setSize(418, 561);
        jFrame.addKeyListener(keyListener);
    }

    /**
     * initFont()
     * Initialises new pixel style font
     */
    private void initFont() {
        try {
            File fontFile = new File("resources/pixeboy-font.ttf");
            Font newFont = Font.createFont(Font.TRUETYPE_FONT, fontFile);
            pixelFont = newFont.deriveFont(20f);
        } catch (IOException | FontFormatException e) {
            e.printStackTrace();
        }
    }

    /**
     * initFrameBackground()
     * Initialises label containing background image
     */
    private void initFrameBackground() {
        try {
            backgroundLabel = new JLabel(new ImageIcon(ImageIO.read(new File("resources/background.png"))));
            backgroundLabel.setBounds(0,0, 402, 522);
            jFrame.add(backgroundLabel);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * initStartPanel()
     * Initialises start menu components
     */
    private void initStartPanel() {
        startPanel = new JPanel();
        startPanel.setBounds(10, 110, 382, 200);
        startPanel.setOpaque(false);
        startPanel.setLayout(new BoxLayout(startPanel, BoxLayout.Y_AXIS));

        titleLabel = new JLabel("Tetris");
        titleLabel.setFont(pixelFont);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setFont(pixelFont.deriveFont(70f));
        startPanel.add(titleLabel);
        animateTitleLabel();

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

        jFrame.add(startPanel);

        startGameLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                animateTitle = false;
                titleLabelThread.interrupt();
                startPanel.setVisible(false);
                gamePanel.setVisible(true);
                game.startGame();
                updateQueue();
                updateCurrentBlock(true);
                gravity();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                startGameLabel.setForeground(Color.WHITE);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                startGameLabel.setForeground(new Color(51, 51, 51));
            }
        });

        switchThemeLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                changeBackgroundImage();
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
    }

    /**
     * initGamePanel()
     * Initialises game panel
     */
    private void initGamePanel() {
        gamePanel = new JPanel();
        gamePanel.setBounds(0, 0, 402, 522);
        gamePanel.setOpaque(false);
        gamePanel.setVisible(false);
        gamePanel.setLayout(null);
        jFrame.add(gamePanel);
    }

    /**
     * initGridPanel()
     * Initialises grid panel which holds game grid
     */
    private void initGridPanel() {
        gridPanel = new JPanel();
        gridPanel.setBounds(10, 10, 252, 502);
        gridPanel.setBorder(new LineBorder(Color.BLACK, 2));
        gridPanel.setOpaque(false);
        gridPanel.setLayout(null);
        gamePanel.add(gridPanel);
    }

    /**
     * initGameGrid()
     * Initialises 2D array of JPanels aka gameGrid and adds to gamePanel
     */
    private void initGameGrid() {
        gameGrid = new JPanel[10][20];
        int y = 1;
        for (int i = 0; i < 20; i++) {
            int x = 1;
            for (int j = 0; j < 10; j++) {
                gameGrid[j][i] = new JPanel();
                gameGrid[j][i].setBounds(x, y, 25, 25);
                gameGrid[j][i].setBackground(Color.WHITE);
                gameGrid[j][i].setOpaque(false);
                gridPanel.add(gameGrid[j][i]);
                x += 25;
            }
            y += 25;
        }
    }

    /**
     * initScorePanel()
     * Initialises score panel and components
     */
    private void initScorePanel() {
        JPanel scorePanel = new JPanel();
        scorePanel.setBounds(272, 10, 120, 30);
        scorePanel.setOpaque(false);
        scorePanel.setBorder(new LineBorder(Color.BLACK, 2));
        scoreLabel = new JLabel("Score: 0");
        scoreLabel.setFont(pixelFont);
        scorePanel.add(scoreLabel);
        gamePanel.add(scorePanel);
    }

    /**
     * initLevelPanel()
     * Initialises level panel and components
     */
    private void initLevelPanel() {
        JPanel levelPanel = new JPanel();
        levelPanel.setBounds(272, 50, 120, 30);
        levelPanel.setOpaque(false);
        levelPanel.setBorder(new LineBorder(Color.BLACK, 2));
        levelLabel = new JLabel("Level: 0");
        levelLabel.setFont(pixelFont);
        levelPanel.add(levelLabel);
        gamePanel.add(levelPanel);
    }

    /**
     * initLinesPanel()
     * Initialises lines panel and components
     */
    private void initLinesPanel() {
        JPanel linesPanel = new JPanel();
        linesPanel.setBounds(272, 90, 120, 30);
        linesPanel.setOpaque(false);
        linesPanel.setBorder(new LineBorder(Color.BLACK, 2));
        linesLabel = new JLabel("Lines: 0");
        linesLabel.setFont(pixelFont);
        linesPanel.add(linesLabel);
        gamePanel.add(linesPanel);
    }

    /**
     * initImageIcons()
     * Initialises all image icons from file
     */
    private void initImageIcons() {
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
     * initQueuePanel()
     * Initialises queue panel and components
     */
    private void initQueuePanel() {
        queuePanel = new JPanel();
        queuePanel.setBounds(272, 130, 120, 190);
        queuePanel.setBorder(new LineBorder(Color.BLACK, 2));
        queuePanel.setLayout(null);
        queuePanel.setOpaque(false);
        gamePanel.add(queuePanel);
        queuePicLabels  = new JLabel[3];
        queuePicLabels[0] = new JLabel();
        queuePicLabels[1] = new JLabel();
        queuePicLabels[2] = new JLabel();
        updateQueue();
    }

    /**
     * initHoldPanel()
     * Initialises hold panel and components
     */
    private void initHoldPanel() {
        JLabel holdLabel = new JLabel("Hold Block (H)");
        holdLabel.setFont(pixelFont);
        JPanel holdPanel = new JPanel();
        holdPanel.setBounds(272, 330, 120, 90);
        holdPanel.setBackground(null);
        holdPanel.setOpaque(false);
        holdPanel.setBorder(new LineBorder(Color.BLACK, 2));
        holdPanel.add(holdLabel);
        holdImgLabel = new JLabel();
        holdPanel.add(holdImgLabel);
        gamePanel.add(holdPanel);
    }

    /**
     * initPausePanel()
     * Initialises pause panel and components
     */
    private void initPausePanel() {
        pauseCoverPanel = new JPanel();
        pauseCoverPanel.setBounds(10, 10, 252, 502);
        pauseCoverPanel.setBorder(new LineBorder(Color.BLACK, 2));
        pauseCoverPanel.setBackground(new Color(51, 51, 51, 150));
        pauseCoverPanel.setVisible(false);
        gamePanel.add(pauseCoverPanel);

        JLabel pauseCoverLabel = new JLabel("Paused");
        pauseCoverLabel.setFont(pixelFont.deriveFont(70f));
        pauseCoverLabel.setForeground(Color.WHITE);
        pauseCoverPanel.add(pauseCoverLabel);

        JPanel pausePanel = new JPanel();
        pausePanel.setBounds(272, 430, 120, 30);
        pausePanel.setOpaque(false);
        pausePanel.setBorder(new LineBorder(Color.BLACK, 2));

        JLabel pauseLabel = new JLabel("Pause (P)");
        pauseLabel.setFont(pixelFont.deriveFont(26f));
        pausePanel.add(pauseLabel);

        gamePanel.add(pausePanel);

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

    /**
     * initGameOverMessage()
     * Initialises game over message panel and its components
     */
    private void initGameOverMessage() {
        gameOverPanel = new JPanel();
        gameOverPanel.setBounds(10, 110, 382, 200);
        gameOverPanel.setOpaque(false);
        gameOverPanel.setLayout(new BoxLayout(gameOverPanel, BoxLayout.Y_AXIS));

        JLabel gameOverLabel = new JLabel("Game Over");
        gameOverLabel.setFont(pixelFont.deriveFont(70f));
        gameOverLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        gameOverPanel.add(gameOverLabel);

        gameOverScoreLabel = new JLabel("Score: 0");
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
                resetGame();
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
        jFrame.add(gameOverPanel);
    }

    //endregion

    //region Update Methods

    /**
     * updateQueue()
     * Updates the images and location in the queue panel
     */
    private void updateQueue() {
        Thread queueThread = new Thread(() -> {
            for (int i = 0; i < 3; i++) {
                switch (game.getBlockQueue()[i]) {
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
     * changeBackgroundImage()
     * Updates the current background image
     */
    private void changeBackgroundImage() {
        try {
            if (currentBackground == 0) {
                backgroundLabel.setIcon(new ImageIcon(ImageIO.read(new File("resources/background-2.png"))));
                currentBackground = 1;
            } else if (currentBackground == 1) {
                backgroundLabel.setIcon(new ImageIcon(ImageIO.read(new File("resources/background.png"))));
                currentBackground = 0;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * updateHoldImage()
     * Updates the image in hold panel
     */
    private void updateHoldImage() {
        if (game.getCurrentHoldBlock() != -1) {
            ImageIcon icon = switch (game.getCurrentHoldBlock()) {
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
     * updateScoreLabel()
     * Updates the text inside the score panel
     */
    private void updateScoreLabel() {
        scoreLabel.setText("Score: " + game.getScore());
    }

    /**
     * updateLevelLabel()
     * Updates the text inside the level label
     */
    private void updateLevelLabel() {
        levelLabel.setText("Level: " + game.getLevel());
    }

    /**
     * updateLinesLabel()
     * Updates the text inside the
     */
    private void updateLinesLabel() {
        linesLabel.setText("Lines: " + game.getTotalLines());
    }

    /**
     * updateCurrentBlock(boolean isNewBlock)
     * Updates the new position of current block and grid and removes previous block location
     * if not new block
     * @param isNewBlock - Boolean representing if block to update is new or not
     */
    private void updateCurrentBlock(boolean isNewBlock) {
        if (!isNewBlock) {
            for (int i = 0; i < 4; i++) {
                gameGrid[game.getPreviousBlockPos()[i][0]][game.getPreviousBlockPos()[i][1]].setBackground(Color.WHITE);
                gameGrid[game.getPreviousBlockPos()[i][0]][game.getPreviousBlockPos()[i][1]].setBorder(null);
                gameGrid[game.getPreviousBlockPos()[i][0]][game.getPreviousBlockPos()[i][1]].setOpaque(false);
            }
        }
        for (int i = 0; i < 4; i++) {
            gameGrid[game.getCurrentBlockPos()[i][0]][game.getCurrentBlockPos()[i][1]].setOpaque(true);
            gameGrid[game.getCurrentBlockPos()[i][0]][game.getCurrentBlockPos()[i][1]].setBackground(game.getCurrentBlockColor());
            gameGrid[game.getCurrentBlockPos()[i][0]][game.getCurrentBlockPos()[i][1]].setBorder(new LineBorder(Color.BLACK));
        }
    }

    //endregion

    /**
     * keyListener
     * Key listener which is added to main frame to get user input
     * from keyboard and send to appropriate methods
     */
    private final KeyListener keyListener = new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == 37 || e.getKeyCode() == 65) {
                game.moveSide(0);
                updateCurrentBlock(false);
            } else if (e.getKeyCode() == 39 || e.getKeyCode() == 68) {
                game.moveSide(1);
                updateCurrentBlock(false);
            } else if (e.getKeyCode() == 40 || e.getKeyCode() == 83) {
                game.setFastFall(true);
            } else if (e.getKeyCode() == 38 || e.getKeyCode() == 87) {
                game.rotateBlock();
                updateCurrentBlock(false);
            } else if (e.getKeyCode() == 72) {
                holdBlock();
            } else if (e.getKeyCode() == 80) {
                pauseGame();
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            if (e.getKeyCode() == 40 || e.getKeyCode() == 83) {
                game.setFastFall(false);
            }
        }
    };

    /**
     * animateTitleLabel()
     * Creates an animation to move tetris title label up and down indefinitely until
     * game starts
     */
    private void animateTitleLabel() {
        titleLabelThread = new Thread(() -> {
            int count = 0;
            while (animateTitle) {
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

    /**
     * gravity()
     * Gravity method controls the falling and updating of blocks in the game grid
     */
    private void gravity() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {}
        blockGravityThread = new Thread(() -> {
            while (game.isRunning()) {
                if (!game.isTouchingBottomOrBlock()) {
                    //Copy array
                    game.updatePreviousBlockPos();
                    game.moveBlockDown();
                    updateCurrentBlock(false);
                    try {
                        for (int i = 0; i < game.getFallDelay()/50 && !game.isFastFall(); i++) {
                            Thread.sleep(50);
                        }
                        if (game.isFastFall()) {
                            Thread.sleep(50);
                        }
                    } catch (InterruptedException ignored) {
                    }
                } else {
                    game.addCurrentToSetBlock();
                    removeRows(game.checkForFullRows());
                    updateScoreLabel();
                    updateLevelLabel();
                    updateLinesLabel();
                    if (!game.checkIfGameOver()) {
                        game.nextBlock();
                        game.setHeldThisTurn(false);
                        updateQueue();
                        updateCurrentBlock(true);
                    } else {
                        blockGravityThread.interrupt();
                        gameOverAnimation();
                        showGameOverMessage();
                    }
                }
            }
        });
        blockGravityThread.start();
    }

    /**
     * showGameOverMessage()
     * Shows the game over message when game is finished, hides main game panel
     */
    private void showGameOverMessage() {
        gamePanel.setVisible(false);
        gameOverScoreLabel.setText("Score: " + game.getScore());
        gameOverPanel.setVisible(true);
    }

    /**
     * gameOverAnimation()
     * Displays animation when game is over
     */
    public void gameOverAnimation() {
        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 10; j++) {
                if (gameGrid[j][i].getBackground() != Color.WHITE) {
                    int green = ThreadLocalRandom.current().nextInt(150, 251);
                    int blue = ThreadLocalRandom.current().nextInt(85, 186);
                    gameGrid[j][i].setBackground(new Color(250, green, blue));
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
     * removeRows(ArrayList<Integer> rowsToRemove)
     * Plays clear line animation, removes row and shuffles blocks above down
     * @param rowsToRemove - ArrayList of rows (y coordinate) to remove
     */
    private void removeRows(ArrayList<Integer> rowsToRemove) {
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
                for (int k = 0; k < game.getSetBlocks().size(); k++) {
                    if (Arrays.equals(new int[]{j, i}, game.getSetBlocks().get(k))) {
                        game.removeFromSetBlocks(k);
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
            game.clearSetBlocks();
            //Add new setBlocks
            for (int j = 0; j < 20; j++) {
                for (int k = 0; k < 10; k++) {
                    if (gameGrid[k][j].getBackground() != Color.WHITE) {
                        game.addToSetBlocks(new int[]{k, j});
                    }
                }
            }
        }
    }

    /**
     * holdBlock()
     * Holds the current block and recalls block that is currently on hold
     */
    private void holdBlock() {
        if (!game.isHeldThisTurn()) {
            if (game.getCurrentHoldBlock() != -1) {
                for (int i = 0; i < 4; i++) {
                    gameGrid[game.getCurrentBlockPos()[i][0]][game.getCurrentBlockPos()[i][1]].setBackground(Color.WHITE);
                    gameGrid[game.getCurrentBlockPos()[i][0]][game.getCurrentBlockPos()[i][1]].setBorder(null);
                    gameGrid[game.getCurrentBlockPos()[i][0]][game.getCurrentBlockPos()[i][1]].setOpaque(false);
                }
                int temp = game.getCurrentBlockType();
                game.newCurrentBlock(game.getCurrentHoldBlock());
                game.setCurrentHoldBlock(temp);
                updateHoldImage();
                updateCurrentBlock(true);
            } else {
                game.setCurrentHoldBlock(game.getCurrentBlockType());
                updateHoldImage();
                for (int i = 0; i < 4; i++) {
                    gameGrid[game.getCurrentBlockPos()[i][0]][game.getCurrentBlockPos()[i][1]].setBackground(Color.WHITE);
                    gameGrid[game.getCurrentBlockPos()[i][0]][game.getCurrentBlockPos()[i][1]].setBorder(null);
                    gameGrid[game.getCurrentBlockPos()[i][0]][game.getCurrentBlockPos()[i][1]].setOpaque(false);
                }
                game.nextBlock();
                updateCurrentBlock(true);
                game.shuffleAndAddToQueue();
                updateQueue();
            }
            game.setHeldThisTurn(true);
        }
    }

    /**
     * pauseGame()
     * Pauses or resumes the game when key pressed or button clicked
     */
    private void pauseGame() {
        if (game.isRunning()) {
            game.setRunning(false);
            blockGravityThread.interrupt();
            pauseCoverPanel.setVisible(true);
        } else {
            game.setRunning(true);
            gravity();
            pauseCoverPanel.setVisible(false);
        }
    }

    /**
     * resetGame()
     * Resets the game
     */
    private void resetGame() {
        game.resetGame();
        gameOverPanel.setVisible(false);
        gamePanel.setVisible(true);
        updateHoldImage();
        updateQueue();
        updateScoreLabel();
        updateLinesLabel();
        updateLevelLabel();
        gravity();
    }

}

