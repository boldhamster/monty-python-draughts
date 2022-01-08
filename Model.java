
import javax.swing.*;

public class Model {

    private int currRow; // the current selected game piece's row position
    private int currCol; // the current selected game piece's column position
    private boolean gameOver;
    private boolean opponentDeleted;

    private boolean stillJumping; // Used when players do a multi-jump
    private boolean canMove; // Controls whether board & pieces react to clicks
    private int arthurCount; // Arthur game piece total
    private int niCount; // Ni game piece total
    private String messageIn; // Stores new messages sent over the server
    private boolean canReceive; // Controls whether Receive button is active or not

    // Used for storing game state info to be submitted over server
    private Icon currentIcon;
    private int leaveRow;
    private int leaveCol;
    private int moveRow;
    private int moveCol;
    private int[] deleteRowCol = new int [12]; // Set to 12 so it can store up to 6 jumps per turn

    public Model() {
    }

    // Reset all array numbers to 0 (0-0 on the board is not an active square in the game)
    public void resetDeleteRowCol() {
        for (int i = 0; i < deleteRowCol.length; i++) {
            deleteRowCol[i] = 0;
        }
    }

    // Getters and Setters

    public boolean PlayerCanMove() {
        return canMove;
    }

    public void setCanMove(boolean canMove) {
        this.canMove = canMove;
    }

    public boolean PlayerCanReceive() {
        return canReceive;
    }

    public void setCanReceive(boolean canReceive) {
        this.canReceive = canReceive;
    }

    public String getMessageIn() {
        return messageIn;
    }

    public void setMessageIn(String messageIn) {
        this.messageIn = messageIn;
    }

    public Icon getCurrentIcon() {
        return currentIcon;
    }

    public void setCurrentIcon(Icon currentIcon) {
        this.currentIcon = currentIcon;
    }

    public int getLeaveRow() {
        return leaveRow;
    }

    public void setLeaveRow(int leaveRow) {
        this.leaveRow = leaveRow;
    }

    public int getLeaveCol() {
        return leaveCol;
    }

    public void setLeaveCol(int leaveCol) {
        this.leaveCol = leaveCol;
    }

    public int getMoveRow() {
        return moveRow;
    }

    public void setMoveRow(int moveRow) {
        this.moveRow = moveRow;
    }

    public int getMoveCol() {
        return moveCol;
    }

    public void setMoveCol(int moveCol) {
        this.moveCol = moveCol;
    }

    public int[] getDeleteRowCol() {
        return deleteRowCol;
    }

    public void setNiCount(int niCount) {
        this.niCount = niCount;
    }

    public void setArthurCount(int arthurCount) {
        this.arthurCount = arthurCount;
    }

    public int getNiCount() {
        return niCount;
    }

    public void decrementNiCount() {
        this.niCount--;
    }

    public int getArthurCount() {
        return arthurCount;
    }

    public void decrementArthurCount() {
        this.arthurCount--;
    }

    public boolean getOpponentDeleted() {
        return opponentDeleted;
    }

    public void setOpponentDeleted(boolean opponentDeleted) {
        this.opponentDeleted = opponentDeleted;
    }

    public void setStillJumping(boolean stillJumping) {
        this.stillJumping = stillJumping;
    }

    public boolean getStillJumping() {
        return stillJumping;
    }

    public void setIconRow(int row) {
        currRow = row;
    }

    public void setIconCol(int col) {
        currCol = col;
    }

    public int getIconRow() {
        return currRow;
    }

    public int getIconCol() {
        return currCol;
    }

    public boolean getGameOver() {
        return gameOver;
    }

    public void setGameOver(boolean input) {
        gameOver = input;
    }


}
