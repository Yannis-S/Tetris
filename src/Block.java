import java.awt.*;

/**
 * Class: Block
 * Author: Yannis Seimenis
 * Description: Java class that represents a tetromino block.
 */
public class Block {
    /**
     * Global variables
     */
    private final int blockType;
    private int blockRotation;
    private int[][] blockLocation;
    private Color blockColor;

    /**
     * Block(int newBlockType)
     * Block class constructor, initialises new block type, location, color and rotation
     * @param newBlockType - New block type represented as int
     */
    public Block(int newBlockType) {
        blockType = newBlockType;
        blockRotation = 0;
        setNewBlockLocationAndColor(newBlockType);
    }

    /**
     * setNewBlockLocationAndColor(int newBlockType)
     * Sets a new blocks starting location and color
     * @param newBlockType - New block type represented as an int
     */
    private void setNewBlockLocationAndColor(int newBlockType) {
        blockLocation = new int[4][2];
        switch (newBlockType) {
            case 0 -> {
                blockLocation[0] = new int[]{3, 0};
                blockLocation[1] = new int[]{4, 0};
                blockLocation[2] = new int[]{5, 0};
                blockLocation[3] = new int[]{6, 0};
                blockColor = new Color(247, 202, 208);
            }
            case 1 -> {
                blockLocation[0] = new int[]{3, 0};
                blockLocation[1] = new int[]{3, 1};
                blockLocation[2] = new int[]{4, 1};
                blockLocation[3] = new int[]{5, 1};
                blockColor = new Color(249, 190, 199);
            }
            case 2 -> {
                blockLocation[0] = new int[]{6, 0};
                blockLocation[1] = new int[]{4, 1};
                blockLocation[2] = new int[]{5, 1};
                blockLocation[3] = new int[]{6, 1};
                blockColor = new Color(251, 177, 189);
            }
            case 3 -> {
                blockLocation[0] = new int[]{4, 0};
                blockLocation[1] = new int[]{5, 0};
                blockLocation[2] = new int[]{4, 1};
                blockLocation[3] = new int[]{5, 1};
                blockColor = new Color(255, 153, 172);
            }
            case 4 -> {
                blockLocation[0] = new int[]{5, 0};
                blockLocation[1] = new int[]{6, 0};
                blockLocation[2] = new int[]{4, 1};
                blockLocation[3] = new int[]{5, 1};
                blockColor = new Color(255, 133, 161);
            }
            case 5 -> {
                blockLocation[0] = new int[]{4, 0};
                blockLocation[1] = new int[]{3, 1};
                blockLocation[2] = new int[]{4, 1};
                blockLocation[3] = new int[]{5, 1};
                blockColor = new Color(255, 112, 150);
            }
            case 6 -> {
                blockLocation[0] = new int[]{4, 0};
                blockLocation[1] = new int[]{5, 0};
                blockLocation[2] = new int[]{5, 1};
                blockLocation[3] = new int[]{6, 1};
                blockColor = new Color(255, 92, 138);
            }
        }
    }

    //region Move Block Methods

    /**
     * moveBlockDown()
     * Moves the block location down
     */
    public void moveBlockDown() {
        for (int i = 0; i < 4; i++) {
            blockLocation[i][1]++;
        }
    }

    /**
     * moveBlockLeft()
     * Moves the block location left
     */
    public void moveBlockLeft() {
        for (int i = 0; i < 4; i++) {
            blockLocation[i][0]--;
        }
    }

    /**
     * moveBlockRight()
     * Moves the blocks location right
     */
    public void moveBlockRight() {
        for (int i = 0; i < 4; i++) {
            blockLocation[i][0]++;
        }
    }

    //endregion

    //region Setters

    /**
     * setNewLocation(int[][] newLocation)
     * Sets a new location of the block (used to set new position after rotation)
     * @param newLocation - New location as 2D array of int
     */
    public void setNewLocation(int[][] newLocation) {
        for (int i = 0; i < 4; i++) {
            blockLocation[i][0] = newLocation[i][0];
            blockLocation[i][1] = newLocation[i][1];
        }
    }

    /**
     * setBlockRotation(int blockRotation)
     * Sets the blocks rotation as an int
     * @param blockRotation - New rotation represented as an int
     */
    public void setBlockRotation(int blockRotation) {
        this.blockRotation = blockRotation;
    }

    //endregion

    //region Getters

    /**
     * getBlockType()
     * @return - Returns the blocks type as int
     */
    public int getBlockType() {
        return blockType;
    }

    /**
     * getBlockRotation()
     * @return - Returns the blocks rotation as int
     */
    public int getBlockRotation() {
        return blockRotation;
    }

    /**
     * getBlockLocation()
     * @return - Returns the blocks location as 2D array of ints
     */
    public int[][] getBlockLocation() {
        return blockLocation;
    }

    /**
     * getBlockColor()
     * @return - Returns the blocks color as Color object
     */
    public Color getBlockColor() {
        return blockColor;
    }

    //endregion
}