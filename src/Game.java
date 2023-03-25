import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Class: Game
 * Author: Yannis Seimenis
 * Description: Game method is used to store game variables and control game logic.
 */

public class Game {
    /**
     * Global variables
     */
    private Block currentBlock;

    private int[] blockQueue;
    private final int[][] previousBlockPos;
    private int fallDelay = 1000;
    private int currentHoldBlock = -1;
    private int score = 0;
    private int level = 0;
    private int clearedLines = 0;
    private int requiredLineClears = 10;
    private int totalLines = 0;

    private boolean heldThisTurn = false;
    private boolean fastFall = false;
    private boolean running = false;

    private final ArrayList<int[]> setBlocks;

    /**
     * Game()
     * Game constructor, initialises game variables
     */
    public Game() {
        initBlockQueue();
        setBlocks = new ArrayList<>();
        previousBlockPos = new int[4][2];
    }

    /**
     * startGame()
     * Starts the game
     */
    public void startGame() {
        running = true;
        nextBlock();
    }

    /**
     * resetGame()
     * Resets all game variables for new game
     */
    public void resetGame() {
        fallDelay = 1000;
        currentHoldBlock = -1;
        score = 0;
        level = 0;
        clearedLines = 0;
        requiredLineClears = 10;
        totalLines = 0;
        heldThisTurn = false;
        initBlockQueue();
        setBlocks.clear();
        startGame();
    }

    /**
     * initBlockQueue()
     * Initialises the block queue
     */
    private void initBlockQueue() {
        blockQueue = new int[3];
        for (int i = 0 ; i < 3; i++) {
            blockQueue[i] = ThreadLocalRandom.current().nextInt(0, 7);
        }
    }

    /**
     * shuffleAndAddToQueue()
     * Shuffles the queue and adds a new random block
     */
    public void shuffleAndAddToQueue() {
        blockQueue[0] = blockQueue[1];
        blockQueue[1] = blockQueue[2];
        blockQueue[2] = ThreadLocalRandom.current().nextInt(0, 7);
    }

    /**
     * nextBlock()
     * Sets the current block to the next block in the queue and shuffles queue
     */
    public void nextBlock() {
        currentBlock = new Block(blockQueue[0]);
        shuffleAndAddToQueue();
    }

    /**
     * updatePreviousBlockPos()
     * Updates previous block position to the current blocks position
     */
    public void updatePreviousBlockPos() {
        for (int i = 0; i < 4; i++) {
            previousBlockPos[i] = Arrays.copyOf(currentBlock.getBlockLocation()[i], currentBlock.getBlockLocation()[i].length);
        }
    }

    /**
     * isTouchingBottomOrBlock()
     * Checks if the bottom of current block is touching the bottom or another block
     * @return - Returns true if block base is touching something
     */
    public boolean isTouchingBottomOrBlock() {
        for (int i = 0; i < 4; i++) {
            if (currentBlock.getBlockLocation()[i][1] == 19) {
                return true;
            } else {
                for (int[] setBlock : setBlocks) {
                    if (Arrays.equals(new int[]{currentBlock.getBlockLocation()[i][0], currentBlock.getBlockLocation()[i][1] + 1}, setBlock)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * moveBlockDown()
     * Moves the current block down by calling block method
     */
    public void moveBlockDown() {
        currentBlock.moveBlockDown();
    }

    /**
     * addToSetBlock()
     * Adds the current position of stationary block to the setBlock arrayList
     */
    public void addCurrentToSetBlock() {
        for (int i = 0; i < 4; i++) {
            setBlocks.add(new int[]{currentBlock.getBlockLocation()[i][0], currentBlock.getBlockLocation()[i][1]});
        }
    }

    /**
     * checkForFullRows()
     * Checks if there are any full rows to remove
     * @return - Returns array list of rows y coordinate to remove
     */
    public ArrayList<Integer> checkForFullRows() {
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
            //Update score (Scoring based on original BPS version of tetris)
            updateScoreAndLevel(rowsToRemove.size());
        }
        return rowsToRemove;
    }

    /**
     * updateScoreAndLevel(int rowToRemoveCount)
     * Updates score and level based on count of rows to remove
     * @param rowToRemoveCount - Int count of rows to remove
     */
    public void updateScoreAndLevel(int rowToRemoveCount) {
        switch (rowToRemoveCount) {
            case 1 -> score += 40;
            case 2 -> score += 100;
            case 3 -> score += 300;
            case 4 -> score += 1200;
        }
        totalLines += rowToRemoveCount;
        clearedLines += rowToRemoveCount;
        if (clearedLines >= requiredLineClears) {
            clearedLines = clearedLines - requiredLineClears;
            increaseLevel();
        }
    }

    /**
     * increaseLevel()
     * Increases the level and delay speed + required lines based on new level
     */
    public void increaseLevel() {
        level++;
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
     * checkIfGameOver()
     * Checks if blocks set on top border and ends game if true
     * @return - Returns true if game is over
     */
    public boolean checkIfGameOver() {
        for (int i = 0; i < 4; i++) {
            if (currentBlock.getBlockLocation()[i][1] == 0) {
                //Game over
                running = false;
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
    public void moveSide(int dir) {
        if (!isTouchingSideOrBlock(dir)) {
            //Copy array
            updatePreviousBlockPos();
            if (dir == 0) {
                currentBlock.moveBlockLeft();
            } else if (dir == 1) {
                currentBlock.moveBlockRight();
            }
        }
    }

    /**
     * isTouchingSideOrBlock(int dir)
     * Checks if the current blocks sides are touching borders or another block
     * @param dir - Direction to check specified from moveSide method
     * @return - Returns true if block is touching something
     */
    public boolean isTouchingSideOrBlock(int dir) {
        for (int i = 0; i < 4; i++) {
            if (dir == 0) {
                if (currentBlock.getBlockLocation()[i][0] == 0) {
                    return true;
                } else {
                    for (int[] setBlock : setBlocks) {
                        if (Arrays.equals(new int[]{currentBlock.getBlockLocation()[i][0] - 1, currentBlock.getBlockLocation()[i][1]}, setBlock)) {
                            return true;
                        }
                    }
                }
            } else if (dir == 1) {
                if (currentBlock.getBlockLocation()[i][0] == 9) {
                    return true;
                } else {
                    for (int[] setBlock : setBlocks) {
                        if (Arrays.equals(new int[]{currentBlock.getBlockLocation()[i][0] + 1, currentBlock.getBlockLocation()[i][1]}, setBlock)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * rotateBlock()
     * Sets the new rotation and position of a block, triggered by keyListener
     */
    public void rotateBlock() {
        int[][] rotateLoc = new int[4][2];
        int newBlockRotation = 0;
        switch (currentBlock.getBlockType()) {
            case 0 -> {
                if (currentBlock.getBlockRotation() == 0) {
                    rotateLoc[0][0] = currentBlock.getBlockLocation()[0][0] + 2;
                    rotateLoc[0][1] = currentBlock.getBlockLocation()[0][1] - 2;
                    rotateLoc[1][0] = currentBlock.getBlockLocation()[1][0] + 1;
                    rotateLoc[1][1] = currentBlock.getBlockLocation()[1][1] - 1;
                    rotateLoc[2][0] = currentBlock.getBlockLocation()[2][0];
                    rotateLoc[2][1] = currentBlock.getBlockLocation()[2][1];
                    rotateLoc[3][0] = currentBlock.getBlockLocation()[3][0] - 1;
                    rotateLoc[3][1] = currentBlock.getBlockLocation()[3][1] + 1;
                    newBlockRotation = 1;
                } else if (currentBlock.getBlockRotation() == 1) {
                    rotateLoc[0][0] = currentBlock.getBlockLocation()[0][0] - 2;
                    rotateLoc[0][1] = currentBlock.getBlockLocation()[0][1] + 2;
                    rotateLoc[1][0] = currentBlock.getBlockLocation()[1][0] - 1;
                    rotateLoc[1][1] = currentBlock.getBlockLocation()[1][1] + 1;
                    rotateLoc[2][0] = currentBlock.getBlockLocation()[2][0];
                    rotateLoc[2][1] = currentBlock.getBlockLocation()[2][1];
                    rotateLoc[3][0] = currentBlock.getBlockLocation()[3][0] + 1;
                    rotateLoc[3][1] = currentBlock.getBlockLocation()[3][1] - 1;
                }
            }
            case 1 -> {
                if (currentBlock.getBlockRotation() == 0) {
                    rotateLoc[0][0] = currentBlock.getBlockLocation()[0][0] + 2;
                    rotateLoc[0][1] = currentBlock.getBlockLocation()[0][1];
                    rotateLoc[1][0] = currentBlock.getBlockLocation()[1][0] + 1;
                    rotateLoc[1][1] = currentBlock.getBlockLocation()[1][1] - 1;
                    rotateLoc[2][0] = currentBlock.getBlockLocation()[2][0];
                    rotateLoc[2][1] = currentBlock.getBlockLocation()[2][1];
                    rotateLoc[3][0] = currentBlock.getBlockLocation()[3][0] - 1;
                    rotateLoc[3][1] = currentBlock.getBlockLocation()[3][1] + 1;
                    newBlockRotation = 1;
                } else if (currentBlock.getBlockRotation() == 1) {
                    rotateLoc[0][0] = currentBlock.getBlockLocation()[0][0];
                    rotateLoc[0][1] = currentBlock.getBlockLocation()[0][1] + 2;
                    rotateLoc[1][0] = currentBlock.getBlockLocation()[1][0] + 1;
                    rotateLoc[1][1] = currentBlock.getBlockLocation()[1][1] + 1;
                    rotateLoc[2][0] = currentBlock.getBlockLocation()[2][0];
                    rotateLoc[2][1] = currentBlock.getBlockLocation()[2][1];
                    rotateLoc[3][0] = currentBlock.getBlockLocation()[3][0] - 1;
                    rotateLoc[3][1] = currentBlock.getBlockLocation()[3][1] - 1;
                    newBlockRotation = 2;
                } else if (currentBlock.getBlockRotation() == 2) {
                    rotateLoc[0][0] = currentBlock.getBlockLocation()[0][0] - 2;
                    rotateLoc[0][1] = currentBlock.getBlockLocation()[0][1];
                    rotateLoc[1][0] = currentBlock.getBlockLocation()[1][0] - 1;
                    rotateLoc[1][1] = currentBlock.getBlockLocation()[1][1] + 1;
                    rotateLoc[2][0] = currentBlock.getBlockLocation()[2][0];
                    rotateLoc[2][1] = currentBlock.getBlockLocation()[2][1];
                    rotateLoc[3][0] = currentBlock.getBlockLocation()[3][0] + 1;
                    rotateLoc[3][1] = currentBlock.getBlockLocation()[3][1] - 1;
                    newBlockRotation = 3;
                } else if (currentBlock.getBlockRotation() == 3) {
                    rotateLoc[0][0] = currentBlock.getBlockLocation()[0][0];
                    rotateLoc[0][1] = currentBlock.getBlockLocation()[0][1] - 2;
                    rotateLoc[1][0] = currentBlock.getBlockLocation()[1][0] - 1;
                    rotateLoc[1][1] = currentBlock.getBlockLocation()[1][1] - 1;
                    rotateLoc[2][0] = currentBlock.getBlockLocation()[2][0];
                    rotateLoc[2][1] = currentBlock.getBlockLocation()[2][1];
                    rotateLoc[3][0] = currentBlock.getBlockLocation()[3][0] + 1;
                    rotateLoc[3][1] = currentBlock.getBlockLocation()[3][1] + 1;
                }
            }
            case 2 -> {
                if (currentBlock.getBlockRotation() == 0) {
                    rotateLoc[0][0] = currentBlock.getBlockLocation()[0][0];
                    rotateLoc[0][1] = currentBlock.getBlockLocation()[0][1] + 2;
                    rotateLoc[1][0] = currentBlock.getBlockLocation()[1][0] + 1;
                    rotateLoc[1][1] = currentBlock.getBlockLocation()[1][1] + 1;
                    rotateLoc[2][0] = currentBlock.getBlockLocation()[2][0];
                    rotateLoc[2][1] = currentBlock.getBlockLocation()[2][1];
                    rotateLoc[3][0] = currentBlock.getBlockLocation()[3][0] - 1;
                    rotateLoc[3][1] = currentBlock.getBlockLocation()[3][1] - 1;
                    newBlockRotation = 1;
                } else if (currentBlock.getBlockRotation() == 1) {
                    rotateLoc[0][0] = currentBlock.getBlockLocation()[0][0] - 2;
                    rotateLoc[0][1] = currentBlock.getBlockLocation()[0][1];
                    rotateLoc[1][0] = currentBlock.getBlockLocation()[1][0] - 1;
                    rotateLoc[1][1] = currentBlock.getBlockLocation()[1][1] - 1;
                    rotateLoc[2][0] = currentBlock.getBlockLocation()[2][0];
                    rotateLoc[2][1] = currentBlock.getBlockLocation()[2][1];
                    rotateLoc[3][0] = currentBlock.getBlockLocation()[3][0] + 1;
                    rotateLoc[3][1] = currentBlock.getBlockLocation()[3][1] + 1;
                    newBlockRotation = 2;
                } else if (currentBlock.getBlockRotation() == 2) {
                    rotateLoc[0][0] = currentBlock.getBlockLocation()[0][0];
                    rotateLoc[0][1] = currentBlock.getBlockLocation()[0][1] - 2;
                    rotateLoc[1][0] = currentBlock.getBlockLocation()[1][0] + 1;
                    rotateLoc[1][1] = currentBlock.getBlockLocation()[1][1] - 1;
                    rotateLoc[2][0] = currentBlock.getBlockLocation()[2][0];
                    rotateLoc[2][1] = currentBlock.getBlockLocation()[2][1];
                    rotateLoc[3][0] = currentBlock.getBlockLocation()[3][0] - 1;
                    rotateLoc[3][1] = currentBlock.getBlockLocation()[3][1] + 1;
                    newBlockRotation = 3;
                } else if (currentBlock.getBlockRotation() == 3) {
                    rotateLoc[0][0] = currentBlock.getBlockLocation()[0][0] + 2;
                    rotateLoc[0][1] = currentBlock.getBlockLocation()[0][1];
                    rotateLoc[1][0] = currentBlock.getBlockLocation()[1][0] - 1;
                    rotateLoc[1][1] = currentBlock.getBlockLocation()[1][1] + 1;
                    rotateLoc[2][0] = currentBlock.getBlockLocation()[2][0];
                    rotateLoc[2][1] = currentBlock.getBlockLocation()[2][1];
                    rotateLoc[3][0] = currentBlock.getBlockLocation()[3][0] + 1;
                    rotateLoc[3][1] = currentBlock.getBlockLocation()[3][1] - 1;
                }
            }
            case 4 -> {
                if (currentBlock.getBlockRotation() == 0) {
                    rotateLoc[0][0] = currentBlock.getBlockLocation()[0][0];
                    rotateLoc[0][1] = currentBlock.getBlockLocation()[0][1];
                    rotateLoc[1][0] = currentBlock.getBlockLocation()[1][0];
                    rotateLoc[1][1] = currentBlock.getBlockLocation()[1][1] + 1;
                    rotateLoc[2][0] = currentBlock.getBlockLocation()[2][0] + 2;
                    rotateLoc[2][1] = currentBlock.getBlockLocation()[2][1] + 1;
                    rotateLoc[3][0] = currentBlock.getBlockLocation()[3][0];
                    rotateLoc[3][1] = currentBlock.getBlockLocation()[3][1];
                    newBlockRotation = 1;
                } else if (currentBlock.getBlockRotation() == 1) {
                    rotateLoc[0][0] = currentBlock.getBlockLocation()[0][0];
                    rotateLoc[0][1] = currentBlock.getBlockLocation()[0][1];
                    rotateLoc[1][0] = currentBlock.getBlockLocation()[1][0];
                    rotateLoc[1][1] = currentBlock.getBlockLocation()[1][1] - 1;
                    rotateLoc[2][0] = currentBlock.getBlockLocation()[2][0] - 2;
                    rotateLoc[2][1] = currentBlock.getBlockLocation()[2][1] - 1;
                    rotateLoc[3][0] = currentBlock.getBlockLocation()[3][0];
                    rotateLoc[3][1] = currentBlock.getBlockLocation()[3][1];
                }
            }
            case 5 -> {
                if (currentBlock.getBlockRotation() == 0) {
                    rotateLoc[0][0] = currentBlock.getBlockLocation()[0][0];
                    rotateLoc[0][1] = currentBlock.getBlockLocation()[0][1];
                    rotateLoc[1][0] = currentBlock.getBlockLocation()[1][0] + 1;
                    rotateLoc[1][1] = currentBlock.getBlockLocation()[1][1] + 1;
                    rotateLoc[2][0] = currentBlock.getBlockLocation()[2][0];
                    rotateLoc[2][1] = currentBlock.getBlockLocation()[2][1];
                    rotateLoc[3][0] = currentBlock.getBlockLocation()[3][0];
                    rotateLoc[3][1] = currentBlock.getBlockLocation()[3][1];
                    newBlockRotation = 1;
                } else if (currentBlock.getBlockRotation() == 1) {
                    rotateLoc[0][0] = currentBlock.getBlockLocation()[0][0] - 1;
                    rotateLoc[0][1] = currentBlock.getBlockLocation()[0][1] + 1;
                    rotateLoc[1][0] = currentBlock.getBlockLocation()[1][0];
                    rotateLoc[1][1] = currentBlock.getBlockLocation()[1][1];
                    rotateLoc[2][0] = currentBlock.getBlockLocation()[2][0];
                    rotateLoc[2][1] = currentBlock.getBlockLocation()[2][1];
                    rotateLoc[3][0] = currentBlock.getBlockLocation()[3][0];
                    rotateLoc[3][1] = currentBlock.getBlockLocation()[3][1];
                    newBlockRotation = 2;
                } else if (currentBlock.getBlockRotation() == 2) {
                    rotateLoc[0][0] = currentBlock.getBlockLocation()[0][0];
                    rotateLoc[0][1] = currentBlock.getBlockLocation()[0][1];
                    rotateLoc[1][0] = currentBlock.getBlockLocation()[1][0];
                    rotateLoc[1][1] = currentBlock.getBlockLocation()[1][1];
                    rotateLoc[2][0] = currentBlock.getBlockLocation()[2][0];
                    rotateLoc[2][1] = currentBlock.getBlockLocation()[2][1];
                    rotateLoc[3][0] = currentBlock.getBlockLocation()[3][0] - 1;
                    rotateLoc[3][1] = currentBlock.getBlockLocation()[3][1] - 1;
                    newBlockRotation = 3;
                } else if (currentBlock.getBlockRotation() == 3) {
                    rotateLoc[0][0] = currentBlock.getBlockLocation()[0][0] + 1;
                    rotateLoc[0][1] = currentBlock.getBlockLocation()[0][1] - 1;
                    rotateLoc[1][0] = currentBlock.getBlockLocation()[1][0] - 1;
                    rotateLoc[1][1] = currentBlock.getBlockLocation()[1][1] - 1;
                    rotateLoc[2][0] = currentBlock.getBlockLocation()[2][0];
                    rotateLoc[2][1] = currentBlock.getBlockLocation()[2][1];
                    rotateLoc[3][0] = currentBlock.getBlockLocation()[3][0] + 1;
                    rotateLoc[3][1] = currentBlock.getBlockLocation()[3][1] + 1;
                }
            }
            case 6 -> {
                if (currentBlock.getBlockRotation() == 0) {
                    rotateLoc[0][0] = currentBlock.getBlockLocation()[0][0] + 1;
                    rotateLoc[0][1] = currentBlock.getBlockLocation()[0][1] + 2;
                    rotateLoc[1][0] = currentBlock.getBlockLocation()[1][0] + 1;
                    rotateLoc[1][1] = currentBlock.getBlockLocation()[1][1];
                    rotateLoc[2][0] = currentBlock.getBlockLocation()[2][0];
                    rotateLoc[2][1] = currentBlock.getBlockLocation()[2][1];
                    rotateLoc[3][0] = currentBlock.getBlockLocation()[3][0];
                    rotateLoc[3][1] = currentBlock.getBlockLocation()[3][1];
                    newBlockRotation = 1;
                } else if (currentBlock.getBlockRotation() == 1) {
                    rotateLoc[0][0] = currentBlock.getBlockLocation()[0][0] - 1;
                    rotateLoc[0][1] = currentBlock.getBlockLocation()[0][1] - 2;
                    rotateLoc[1][0] = currentBlock.getBlockLocation()[1][0] - 1;
                    rotateLoc[1][1] = currentBlock.getBlockLocation()[1][1];
                    rotateLoc[2][0] = currentBlock.getBlockLocation()[2][0];
                    rotateLoc[2][1] = currentBlock.getBlockLocation()[2][1];
                    rotateLoc[3][0] = currentBlock.getBlockLocation()[3][0];
                    rotateLoc[3][1] = currentBlock.getBlockLocation()[3][1];
                }
            }
        }
        boolean flag = false;
        //Check if new position is valid
        //Check if block is in space of other block
        for (int[] setBlock : setBlocks) {
            for (int j = 0; j < 4; j++) {
                if (Arrays.equals(new int[]{rotateLoc[j][0], rotateLoc[j][1]}, setBlock)) {
                    flag = true;
                    break;
                }
            }
        }
        //Check if block is outside of boundaries
        for (int j = 0; j < 4; j++) {
            if (rotateLoc[j][0] < 0 || rotateLoc[j][0] > 9) {
                flag = true;
            } else if (rotateLoc[j][1] < 0 || rotateLoc[j][1] > 19) {
                flag = true;
            }
        }
        //Check if block is square (o)
        if (currentBlock.getBlockType() == 3) {
            flag = true;
        }
        //If valid update current position to new rotation position
        if (!flag) {
            //Copy array
            updatePreviousBlockPos();
            currentBlock.setNewLocation(rotateLoc);
            currentBlock.setBlockRotation(newBlockRotation);
        }
    }

    //region Getters

    public boolean isHeldThisTurn() {
        return heldThisTurn;
    }

    public int[][] getCurrentBlockPos() {
        return currentBlock.getBlockLocation();
    }

    public Color getCurrentBlockColor() {
        return currentBlock.getBlockColor();
    }

    public int[][] getPreviousBlockPos() {
        return previousBlockPos;
    }

    public ArrayList<int[]> getSetBlocks() {
        return setBlocks;
    }

    public int getFallDelay() {
        return fallDelay;
    }

    public boolean isFastFall() {
        return fastFall;
    }

    public boolean isRunning() {
        return running;
    }

    public int[] getBlockQueue() {
        return blockQueue;
    }

    public int getScore() {
        return score;
    }

    public int getLevel() {
        return level;
    }

    public int getTotalLines() {
        return totalLines;
    }

    public int getCurrentHoldBlock() {
        return currentHoldBlock;
    }

    public int getCurrentBlockType() {
        return currentBlock.getBlockType();
    }

    //endregion

    //region Setters

    public void setHeldThisTurn(boolean heldThisTurn) {
        this.heldThisTurn = heldThisTurn;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public void newCurrentBlock(int blockType) {
        currentBlock = new Block(blockType);
    }

    public void setCurrentHoldBlock(int currentHoldBlock) {
        this.currentHoldBlock = currentHoldBlock;
    }

    public void setFastFall(boolean fastFall) {
        this.fastFall = fastFall;
    }

    public void addToSetBlocks(int[] blockToAdd) {
        setBlocks.add(blockToAdd);
    }

    public void removeFromSetBlocks(int index) {
        setBlocks.remove(index);
    }

    public void clearSetBlocks() {
        setBlocks.clear();
    }

    //endregion

}

