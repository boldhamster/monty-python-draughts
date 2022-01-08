// 0904889y Emmet Young

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client extends JFrame implements ActionListener {

    int clientNumber; // 1 = Arthur, 2 = Ni
    boolean concedeDefeat; // Used when players are stuck or just want to quit
    int deleteCounter; // Tracks the current position in the deleteRowCol array

    View view;
    Model model;

    // Containers for converting the String updates from the Receive button into usable variables
    Icon currentIcon;
    int leaveRow;
    int leaveCol;
    int moveRow;
    int moveCol;
    int deleteSize;
    int[] deleteRowCol;

    // GameWorker connects the client and socket, and initialises their input and output streams
    private class GameWorker extends SwingWorker<Void,Void> {
        private Socket socket = null;
        private ObjectInputStream inputStream = null;
        private Client parent;

        public GameWorker(Socket s, Client parent) {
            this.socket = s;
            this.parent = parent;
            try {
                inputStream = new ObjectInputStream(this.socket.getInputStream());
            }catch(IOException e) {
                e.printStackTrace();
            }
        }

        // This method checks for new messages from the inputStream, and updates the client
        public Void doInBackground() {
            System.out.println("Game Worker Started");
            GameUpdate gUp = null;
            try {
                // Cast the Game Update class to the readObject() method
                while((gUp = (GameUpdate)inputStream.readObject())!= null) {
                    System.out.println(gUp);
                    // Update the messageIn of this client with the String in gUp
                    parent.update(gUp);
                }
            }catch(ClassNotFoundException | IOException e) {
                e.printStackTrace();
            } finally {
                return null;
            }
        }
    }

    private Socket server = null;
    private ObjectOutputStream outputStream;

    // Client constructor creates a new instance of Client and loads the game window
    public Client() throws IOException {

        // Ask players what clientNumber they will use
        boolean go = false;
        String strNumber = "";
        while (!go) {
            strNumber = JOptionPane.showInputDialog(this, "Enter your player number: 1 or 2");
            if (strNumber.contains("1") || strNumber.contains("2")) {
                go = true;
            }
        }

        // Update strNumber in case the user has typed "01" or "1kl" etc
        // Prevents problems with parseInt
        if (strNumber.contains("1")) {
            strNumber = "1";
        } else if (strNumber.contains("2")) {
            strNumber = "2";
        }
        this.clientNumber = Integer.parseInt(strNumber);

        // Create a new instance of the view and the model for each client
        view = new View();
        model = new Model();

        // add an ActionListener to each button on the board
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                view.getSquares()[i][j].addActionListener(this);
            }
        }
        view.getSubmit().addActionListener(this); // To Submit a GameUpdate
        view.getReceive().addActionListener(this); // To Receive a GameUpdate
        view.getNewGame().addActionListener(this); // To start a NewGame
        view.getImage().addActionListener(this); // To quit or concede

        // Set the start of game defaults for relevant game variables
        model.setArthurCount(12);
        model.setNiCount(12);
        // Each button will identify what client you are
        view.getSubmit().setText("P"+clientNumber + " Submit");
        view.getReceive().setText("P"+clientNumber + " Receive");
        if (clientNumber == 1) {
            view.getPlayerText().setText("It's Your Turn"); // if you are Arthur, it's your turn
            model.setCanMove(true); // canMove is used to control when the game responds to clicks on the game board
            view.getSubmit().setEnabled(false); // Submit is only enabled after a turn is complete and CanMove is false
            view.getReceive().setEnabled(true); // Receive is always enabled, but its code is wrapped in a CanReceive boolean
        } else if (clientNumber == 2) {
            view.getPlayerText().setText("It's Their Turn"); // if you are Ni, it's their turn first
            model.setCanMove(false);
            view.getSubmit().setEnabled(false);
            view.getReceive().setEnabled(true);
        }
        model.setGameOver(false);
        view.getNewGame().setEnabled(false);
        deleteCounter = 0;
        // Represent the player scores inside the NewGame button while its not enabled
        view.getNewGame().setText("A: "+ model.getArthurCount() + " vs. " + "N: " + model.getNiCount());
        concedeDefeat = false;

        // Try to connect to the running server
        connect();
        try {
            outputStream = new ObjectOutputStream(server.getOutputStream());
        }catch(IOException e) {
            e.printStackTrace();
        }

        // Create a GameWorker instance with each new Client
        GameWorker gameWorker = new GameWorker(server,this);
        gameWorker.execute();
        System.out.println("Ready To Play");

        // Explain game quit options to user
        JFrame g = new JFrame();
        JOptionPane.showMessageDialog(g,"To QUIT or CONCEDE, click the Monty Python logo, and type YES");

        // Remind the player what side they're on
        String name = "";
        if (clientNumber == 1) {
            name = "King Arthur";
        } else if (clientNumber == 2) {
            name = "Ni Ni Ni Ni!";
        }
        JFrame f = new JFrame();
        JOptionPane.showMessageDialog(f, "You are " + name);
    }

    // This method connects to server using localhost (127.0.0.1) and the free port 8765
    private void connect() {
        try {
            server = new Socket("127.0.0.1",8765);
            System.out.println("Connected");
        }catch(IOException e) {
            e.printStackTrace();
        }
    }

    // This method uses the String message in the GameUpdate object to update each client with the latest game move
    // The boolean model.CanReceive is also set to true, so that the opponent's Receive button becomes active
    public void update(GameUpdate gUp) {
        model.setMessageIn(gUp.toString());
        model.setCanReceive(true);
    }

    // For easier reading, the main components of the actionPerformed method are highlighted with Todo:
    // This method contains the game's controller methods, as well as connecting the Submit button to the outputStream
    public void actionPerformed(ActionEvent e) {

        // Todo: Submit Button
        if(e.getSource() == view.getSubmit()) {
            StringBuilder submitMessage = new StringBuilder();

            // First check if the client has just conceded defeat
            if (concedeDefeat) {
                submitMessage.append("CONCEDE");

                // Otherwise, pull together the current game state information and append it to submitMessage
            } else {
                String icon = "";
                int leaveRow;
                int leaveCol;
                int moveRow;
                int moveCol;

                // "MOVE" is added to the beginning so that the final message is easier for users to read
                submitMessage.append("MOVE");

                // Game piece to move
                if (model.getCurrentIcon() == view.getArthur()) {
                    icon = "1";
                } else if (model.getCurrentIcon() == view.getRobin()) {
                    icon = "2";
                } else if (model.getCurrentIcon() == view.getNi()) {
                    icon = "3";
                } else if (model.getCurrentIcon() == view.getNiKing()) {
                    icon = "4";
                }
                submitMessage.append(icon);

                // All stored in model when player made their last move
                leaveRow = model.getLeaveRow();
                leaveCol = model.getLeaveCol();
                moveRow = model.getMoveRow();
                moveCol = model.getMoveCol();
                submitMessage.append(leaveRow);
                submitMessage.append(leaveCol);
                submitMessage.append(moveRow);
                submitMessage.append(moveCol);

                // If the player jumped over an opponent, the deleteOpponent method stored it in deleteRowCol
                if (model.getOpponentDeleted()) {
                    submitMessage.append("DELETE");
                    for (int del : model.getDeleteRowCol()) {
                        submitMessage.append(del);
                    }
                }
                model.resetDeleteRowCol(); // Reset the deleted opponents array
            }

            // Now submit the compiled message to the server
            String messageText = submitMessage.toString();
            try {
                outputStream.writeObject(new GameUpdate(messageText));
            }catch(IOException ex) {
                ex.printStackTrace();
            }
            // Disable Submit until the next time the player has finished moving
            view.getSubmit().setEnabled(false);
            view.getReceive().setEnabled(true);

            // if you've just conceded defeat, update the New Game text and break out of this actionPerformed method
            if (concedeDefeat) {
                view.getNewGame().setText("New Game");
                return;
            }

            // Tell the user their turn is over and stop them from moving game pieces
            view.getPlayerText().setText("It's Their Turn");
            model.setCanMove(false);

            // Check if the latest move means it's now Game Over
            // If yes, declare a winner then update the relevant buttons and booleans to Game Over status
            if (model.getArthurCount() < 1) {
                JFrame f = new JFrame();
                JOptionPane.showMessageDialog(f,"GAME OVER! The Knights Who Say Ni WIN!");
                model.setGameOver(true);
                view.getNewGame().setEnabled(true);
                view.getReceive().setEnabled(true);
                view.getSubmit().setEnabled(false);
                model.setCanMove(false);
                view.getNewGame().setText("New Game");
            } else if (model.getNiCount() < 1) {
                JFrame f = new JFrame();
                JOptionPane.showMessageDialog(f,"GAME OVER! King Arthur & Sir Robin WIN!");
                model.setGameOver(true);
                view.getNewGame().setEnabled(true);
                view.getReceive().setEnabled(true);
                view.getSubmit().setEnabled(false);
                model.setCanMove(false);
                view.getNewGame().setText("New Game");
            }
            deleteCounter = 0; // re-set for next turn

            // Because of the Server/Client design used here, the Server sends every message input back out to both Clients
            // However, the submitting Client should not have an active Receive button until their opponent has sent a new message
            // So, set the model.canReceive variable to false to override that part of the command for this Client only
            model.setCanReceive(false);
            // When it's Game Over, the NewGame button will display "New Game" instead of the player totals
            if (!model.getGameOver()) {
                view.getNewGame().setText("A: "+ model.getArthurCount() + " vs. " + "N: " + model.getNiCount());
            }
            return; // break out of actionPerformed
        }

        // Todo: Receive Button
        if (e.getSource() == view.getReceive()) {

            // PlayerCanReceive is used to check if the player has received a new message (triggered by the update method)
            // The message in model.getMessageIn() is also set by the update method
            if (model.PlayerCanReceive()) {
                String message = model.getMessageIn();
                System.out.println(message);

                // If the opponent has conceded, check which client is receiving
                // Then update the display and model to the Game Over defaults
                if (message.contains("CONCEDE")) {
                    if (clientNumber == 1) {
                        JFrame g = new JFrame();
                        JOptionPane.showMessageDialog(g,"GAME OVER! King Arthur & Sir Robin WIN!");
                        model.setGameOver(true);
                        view.getNewGame().setEnabled(true);
                        view.getNewGame().setText("New Game");
                        view.getReceive().setEnabled(true);
                        view.getSubmit().setEnabled(false);
                        model.setCanMove(false);
                    } else if (clientNumber == 2) {
                        JFrame h = new JFrame();
                        JOptionPane.showMessageDialog(h,"GAME OVER! The Knights Who Say Ni WIN!");
                        model.setGameOver(true);
                        view.getNewGame().setEnabled(true);
                        view.getNewGame().setText("New Game");
                        view.getReceive().setEnabled(true);
                        view.getSubmit().setEnabled(false);
                        model.setCanMove(false);
                    }
                    return;
                }

                // If not Game Over, continue unpacking the String message into usable variables
                // use charAt() to extract the char and getNumericValue to convert it to an integer
                int iconNum = Character.getNumericValue(message.charAt(4));
                // Set the currentIcon (1 = Arthur, 2 = Robin, 3 = Ni, 4 = King Ni)
                if (iconNum == 1) {
                    currentIcon = view.getArthur();
                } else if (iconNum == 2) {
                    currentIcon = view.getRobin();
                } else if (iconNum == 3) {
                    currentIcon = view.getNi();
                } else if (iconNum == 4) {
                    currentIcon = view.getNiKing();
                }

                // Get info for moving game piece
                leaveRow = Character.getNumericValue(message.charAt(5));
                leaveCol =  Character.getNumericValue(message.charAt(6));
                moveRow =  Character.getNumericValue(message.charAt(7));
                moveCol =  Character.getNumericValue(message.charAt(8));

                // Wipe the old location and set the icon on the new location
                view.getSquares()[leaveRow][leaveCol].setIcon(null);
                view.getSquares()[moveRow][moveCol].setIcon(currentIcon);


                // Check if game pieces have been jumped over in the move and need to be deleted
                if (message.contains("DELETE")) {
                    deleteSize = 12; // Can store 6 jumps per player move
                    deleteRowCol = new int[deleteSize];
                    int counter = deleteSize;
                    int position = 15; // Start at 16th position in the message, after (MOVE12345DELETE...)
                    int i = 0; // Starting position in deleteRowCol array

                    // Scan through the array, loading all deletion co-ordinates
                    while (counter != 0) {
                        deleteRowCol[i] = Character.getNumericValue(message.charAt(position));
                        i++; // Increment array position
                        position++; // Increment message position
                        counter--; // Decrement counter (size of array length)
                    }

                    // Delete jumped pieces from the board and count the number of deletions (pairs++)
                    // If there's something to delete, count it and then setIcon to null
                    // This way, the code won't count the default "0,0" pairs in the array
                    int j = 0;
                    int pairs = 0;
                    while (j < 11) {
                        if (view.getSquares()[deleteRowCol[j]][deleteRowCol[j+1]].getIcon() != null) {
                            pairs++; // Count the pairs
                        }
                        view.getSquares()[deleteRowCol[j]][deleteRowCol[j+1]].setIcon(null);
                        j = (j + 2); // skip past current j and j+1
                    }

                    // For every deleted piece, decrement that user's total
                    // If you're Receiving deleted game pieces then they are yours
                    // So, check which client you are and decrement that total
                    while (pairs > 0) {
                        if (clientNumber == 1) {
                            model.decrementArthurCount();
                        } else if (clientNumber == 2) {
                            model.decrementNiCount();
                        }
                        pairs--;
                    }
                }

                model.resetDeleteRowCol(); // Reset array
                kingCheck(moveRow, moveCol); // Check if any pieces need to be updated to a king in last move
                view.getReceive().setEnabled(true);
                view.getPlayerText().setText("It's Your Turn");
                model.setCanMove(true);

                // Same Game Over check as in Submit button
                if (model.getArthurCount() < 1) {
                    JFrame f = new JFrame();
                    f.setBackground(Color.GRAY);
                    JOptionPane.showMessageDialog(f,"GAME OVER! The Knights Who Say Ni WIN!");
                    model.setGameOver(true);
                    view.getNewGame().setEnabled(true);
                    view.getNewGame().setText("New Game");
                    view.getReceive().setEnabled(true);
                    view.getSubmit().setEnabled(false);
                    model.setCanMove(false);
                } else if (model.getNiCount() < 1) {
                    JFrame f = new JFrame();
                    f.setBackground(Color.GRAY);
                    JOptionPane.showMessageDialog(f,"GAME OVER! King Arthur & Sir Robin WIN!");
                    model.setGameOver(true);
                    view.getNewGame().setEnabled(true);
                    view.getNewGame().setText("New Game");
                    view.getReceive().setEnabled(true);
                    view.getSubmit().setEnabled(false);
                    model.setCanMove(false);
                }

            }
            // Update current player piece totals, reset CanReceive so it can check for the next message input
            if (!model.getGameOver()) {
                view.getNewGame().setText("A: "+ model.getArthurCount() + " vs. " + "N: " + model.getNiCount());
            }
            model.setCanReceive(false);

            return; // end actionPerformed here

        }

        // Todo: NewGame Button
        if (e.getSource() == view.getNewGame()) {

            // Clear the highlights and icons from the board, add starting pieces, reset game defaults
            resetSquares();
            for (int k = 0; k < 8; k++) {
                for (int l = 0; l < 8; l++) {
                    view.getSquares()[k][l].setIcon(null);
                }
            }
            view.addGamePieces();
            model.setArthurCount(12);
            model.setNiCount(12);
            if (clientNumber == 1) {
                view.getPlayerText().setText("It's Your Turn"); // if you are Arthur it's your turn
                model.setCanMove(true);
                view.getSubmit().setEnabled(false);
                view.getReceive().setEnabled(true);
            } else if (clientNumber == 2) {
                view.getPlayerText().setText("It's Their Turn"); // if you are Ni it's their turn first
                model.setCanMove(false);
                view.getSubmit().setEnabled(false);
                view.getReceive().setEnabled(true);
            }
            model.setGameOver(false);
            view.getNewGame().setEnabled(false);
            deleteCounter = 0;
            view.getNewGame().setText("A: "+ model.getArthurCount() + " vs. " + "N: " + model.getNiCount());
            concedeDefeat = false;
        }

        // Todo: Player Selects Game Board Button
        // The game board section of the actionPerformed method reads the current game state
        // It then uses that information to decide what the player is trying to do with their click

        if (model.PlayerCanMove() && !model.getGameOver() && ((e.getSource() != view.getSubmit()) ||
                (e.getSource() != view.getReceive()) || (e.getSource() != view.getImage()))) {
            // If PlayerCanMove and it's not Game Over, and the player's clicked the game board

            JButton currButton;
            Icon currentIcon = view.getSquares()[model.getIconRow()][model.getIconCol()].getIcon(); // Initialise object
            Color highlight = view.getColour3();

            // Loop through the board co-ordinates, 0-0 to 7-7 and find the game square (JButton) that was clicked
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    currButton =  view.getSquares()[i][j];

                    if (e.getSource() == currButton) {

                        if (currButton.getIcon() != null) { // Button has a player piece on it

                            // Check if clientNumber corresponds to game piece selected
                            if (((currButton.getIcon() == view.getArthur() ||
                                    currButton.getIcon() == view.getRobin()) && clientNumber == 1) ||
                                    ((currButton.getIcon() == view.getNi() ||
                                            currButton.getIcon() == view.getNiKing()) && clientNumber == 2)) {

                                // Player has re-clicked same piece while still jumping
                                // Reset the squares and redraw the jump highlights
                                if (model.getStillJumping() && currButton ==
                                        view.getSquares()[model.getIconRow()][model.getIconCol()]) {
                                    resetSquares();
                                    newJumpHighlights(currentIcon, i, j, highlight);

                                    // Player has clicked a different button while in the middle of a jump
                                } else if (model.getStillJumping() && currButton !=
                                        view.getSquares()[model.getIconRow()][model.getIconCol()]) {
                                    break;

                                    // Player has clicked a button and they're not in the middle of a multi-jump
                                    // Update current icon position and Icon object
                                } else if (!model.getStillJumping()) {
                                    model.setIconRow(i);
                                    model.setIconCol(j);

                                    // Only if the player's move is not over, update the leave row/col
                                    if (model.PlayerCanMove()) {
                                        model.setLeaveRow(i);
                                        model.setLeaveCol(j);
                                    }
                                    currentIcon = view.getSquares()[model.getIconRow()][model.getIconCol()].getIcon();
                                    resetSquares(); // Clear all highlights, ready for new ones
                                    newJumpHighlights(currentIcon, i, j, highlight); // Set new jump highlights

                                    // If there are no jumps highlighted, offer move suggestions
                                    // Players aren't supposed to move 1 space if a jump is available
                                    if (!checkSquares()) {
                                        moveHighlights(currentIcon, i, j, highlight);
                                    }
                                }
                            }
                        } else { // The button does not contain a player piece

                            if (currButton.getBackground() == highlight) { // If board button is already highlighted

                                // Update latest player move details
                                moveGamePiece(currentIcon, i, j); // Button is highlighted so move is allowed
                                model.setCurrentIcon(currentIcon);
                                model.setMoveRow(i);
                                model.setMoveCol(j);
                                deleteOpponent(i, j); // Check if the piece just jumped, delete opponent if so
                                kingCheck(i, j); // Check if a new king is being made
                                resetSquares();
                                model.setIconRow(i);
                                model.setIconCol(j);

                                // If player just made a jump, check if they can do more in this move (a multi-jump)
                                if (model.getOpponentDeleted()) {
                                    newJumpHighlights(currentIcon, i, j, highlight);
                                }

                                // Check if any squares are now highlighted
                                // If there are no more jumps to make, update model and enable the submit button
                                if (!checkSquares()) {
                                    model.setStillJumping(false);
                                    model.setCanMove(false);
                                    view.getSubmit().setEnabled(true);
                                } else {
                                    model.setStillJumping(true);
                                }

                                // If board square clicked is not highlighted and player is still jumping, do nothing
                            } else if (currButton.getBackground() != highlight && model.getStillJumping()) {
                                break;
                            } else { // If no highlights, do nothing (it's not a valid move)
                                resetSquares();
                            }
                        }
                        return;
                    }
                }
            }
        }

        // Todo: Logo Button
        // An escape route for players who are stuck (no moves available) or who just want to quit the game now
        if (e.getSource() == view.getImage()) {
            String concedeText = "";
            boolean ask = true;

            while (ask) {
                concedeText = JOptionPane.showInputDialog(this, "Do you concede? Enter YES or NO below:");
                if (concedeText.contains("YES")) {
                    ask = false;
                    this.concedeDefeat = true;
                } else if (concedeText.contains("NO")) {
                    ask = false;
                }
            }

            // If conceded, cut to end of game and ask winner to click Submit button to update their opponent
            if (concedeDefeat) {

                if (clientNumber == 1) {
                    JFrame g = new JFrame();
                    JOptionPane.showMessageDialog(g,"GAME OVER! The Knights Who Say Ni WIN!");
                    model.setGameOver(true);
                    view.getNewGame().setEnabled(true);
                    view.getNewGame().setText("New Game");
                    view.getReceive().setEnabled(true);
                    JOptionPane.showMessageDialog(g,"Click Submit To Admit Your Defeat");
                    view.getSubmit().setEnabled(true);
                    model.setCanMove(false);
                } else if (clientNumber == 2) {
                    JFrame h = new JFrame();
                    JOptionPane.showMessageDialog(h,"GAME OVER! King Arthur & Sir Robin WIN!");
                    model.setGameOver(true);
                    view.getNewGame().setEnabled(true);
                    view.getNewGame().setText("New Game");
                    view.getReceive().setEnabled(true);
                    JOptionPane.showMessageDialog(h,"Click Submit To Admit Your Defeat");
                    view.getSubmit().setEnabled(true);
                    model.setCanMove(false);
                }
            }
        }
    }

    // Reset the board colours to default (clear highlights)
    public void resetSquares() {
        for (int k = 0; k < 8; k++) {
            for (int l = 0; l < 8; l++) {
                // Even numbered squares
                view.getSquares()[k][l].setBackground(view.getColour2());

                // Odd numbered squares
                if ((k + l) % 2 != 0) {
                    view.getSquares()[k][l].setBackground(view.getColour1());
                }
            }
        }
    }

    // Checks the game board for highlighted squares
    public boolean checkSquares() {
        for (int k = 0; k < 8; k++) {
            for (int l = 0; l < 8; l++) {
                if (view.getSquares()[k][l].getBackground() == view.getColour3()) {
                    return true;
                }
            }
        }
        return false;
    }

    // This method checks if a board button has an icon on it, and if not it highlights that square
    public void highlightMove(int row, int col, Color highlight) {
        if (view.getSquares()[row][col].getIcon() == null) {
            view.getSquares()[row][col].setBackground(highlight);
        }
    }

    // Moves an icon by using setIcon on the new button location and setting old location icon to null
    public void moveGamePiece(Icon mover, int row, int col) {
        view.getSquares()[row][col].setIcon(mover);
        view.getSquares()[model.getIconRow()][model.getIconCol()].setIcon(null);
    }

    // This method uses the highlightMove method to check if locations on the board can be moved to
    // The colours mentioned come from the players' move and jump templates in the report
    public void moveHighlights(Icon mover, int row, int col, Color highlight) {
        if (mover == view.getArthur()) {
            if (row > 0 && col > 0 && col < 7) { //GREEN
                highlightMove(row-1,col-1, highlight); // Up left
                highlightMove(row-1,col+1, highlight); // Up right
            } else if (row > 0 && col == 0) { //ORANGE L
                highlightMove(row-1,col+1, highlight); // Up right
            } else if (row > 0 && col == 7) { //ORANGE R
                highlightMove(row-1,col-1, highlight); // Up left
            }

        } else if (mover == view.getNi()) {
            if (row < 7 && col > 0 && col < 7) { //GREEN
                highlightMove(row+1,col-1, highlight); // Down left
                highlightMove(row+1, col+1, highlight); // Down right
            } else if (row < 7 && col == 0) { //ORANGE L
                highlightMove(row+1,col+1, highlight); // Down right
            } else if (row < 7 && col == 7) { //ORANGE R
                highlightMove(row+1,col-1, highlight); // Down left
            }

        } else if (mover == view.getRobin() || mover == view.getNiKing()) {
            if (row < 7 && row > 0 && col > 0 && col < 7) { //GREEN
                highlightMove(row-1, col-1, highlight); // Up left
                highlightMove(row-1, col+1, highlight); // Up right
                highlightMove(row+1, col-1, highlight); // Down left
                highlightMove(row+1, col+1, highlight); // Down right
            } else if (row == 0 && col > 0 && col < 7) { //ORANGE T
                highlightMove(row+1,col-1, highlight); // Down left
                highlightMove(row+1, col+1, highlight); // Down right
            } else if (row == 7 && col > 0 && col < 7) { //ORANGE B
                highlightMove(row-1, col-1, highlight); // Up left
                highlightMove(row-1, col+1, highlight); // Up right
            } else if (row > 0 && row < 7 && col == 0) { //ORANGE L
                highlightMove(row-1, col+1, highlight); // Up right
                highlightMove(row+1, col+1, highlight); // Down right
            } else if (row > 0 && row < 7 && col == 7) { //ORANGE R
                highlightMove(row-1, col-1, highlight); // Up left
                highlightMove(row+1, col-1, highlight); // Down left
            } else if (row == 0 && col == 0) { //PURPLE TL
                highlightMove(row+1, col+1, highlight); // Down right
            } else if (row == 0 && col == 7) { //PURPLE TR
                highlightMove(row+1, col-1, highlight); // Down left
            } else if (row == 7 && col == 0) { //PURPLE BL
                highlightMove(row-1, col+1, highlight); // Up right
            } else if (row == 7 && col == 7) { //PURPLE BR
                highlightMove(row-1, col-1, highlight); // Up left
            }
        }
    }

    // This method checks a given board button to see if a jump is possible
    // It checks one button to see if it contains an opponent, and then checks the button beyond that to see if it's empty
    public void checker(Icon currentIcon, int i, int j, int k, int l, Color highlight) {
        JButton checkThis = view.getSquares()[i][j];
        JButton highlightThat = view.getSquares()[k][l];

        if (currentIcon == view.getArthur() || currentIcon == view.getRobin()) {
            if (checkThis.getIcon() == view.getNi() || checkThis.getIcon() == view.getNiKing()) {
                if (highlightThat.getIcon() == null) { // if there is an empty space to jump to
                    highlightThat.setBackground(highlight); // highlight that space
                }
            }

        } else if (currentIcon == view.getNi() || currentIcon == view.getNiKing()) {
            if (checkThis.getIcon() == view.getArthur() || checkThis.getIcon() == view.getRobin()) {
                if (highlightThat.getIcon() == null) { // if there is an empty space to jump to
                    highlightThat.setBackground(highlight); // highlight that space
                }
            }
        }
    }

    // This method combines the checker method above with the current game piece's location to check for possible jumps
    // As with the moveHighlights method, it uses if statements to find the piece's location & jumper to know who's jumping
    // This way, only legal jumps will be suggested
    public void newJumpHighlights(Icon jumper, int row, int col, Color highlight) {

        // Arthur pieces can jump upward
        if (jumper == view.getArthur()) {
            if ((row > 1) && (col > 1 && col < 6)) { //GREEN
                checker(jumper, row-1, col+1, row-2, col+2, highlight);
                checker(jumper, row-1, col-1, row-2, col-2, highlight);
            } else if ((row > 1) && (col < 2)) { //YELLOW
                checker(jumper, row-1, col+1, row-2, col+2, highlight);
            } else if ((row > 1) && (col == 6 || col == 7)) { //YELLOW
                checker(jumper, row-1, col-1, row-2, col-2, highlight);
            }

            // Ni pieces can jump downward
        } else if (jumper == view.getNi()) {
            if ((row < 6) && (col > 1 && col < 6)) { //GREEN
                checker(jumper, row+1, col+1, row+2, col+2, highlight);
                checker(jumper, row+1, col-1, row+2, col-2, highlight);
            } else if ((row < 6) && (col < 2)) { //YELLOW
                checker(jumper, row+1, col+1, row+2, col+2, highlight);
            } else if ((row < 6) && (col == 6 || col == 7)) { //YELLOW
                checker(jumper, row+1, col-1, row+2, col-2, highlight);
            }

            // King pieces can jump up or down
        } else if (jumper == view.getRobin() || jumper == view.getNiKing()) {
            if ((row > 1 && row < 6) && (col > 1 && col < 6)) { //GREEN
                checker(jumper, row-1, col-1, row-2, col-2, highlight); // up l
                checker(jumper, row-1, col+1, row-2, col+2, highlight); // up r
                checker(jumper, row+1, col-1, row+2, col-2, highlight); // down l
                checker(jumper, row+1, col+1, row+2, col+2, highlight); // down r
            } else if ((row == 0 || row == 1) && (col == 0 || col == 1)) { //PURPLE
                checker(jumper, row+1, col+1, row+2, col+2, highlight); // down r
            } else if ((row == 0 || row == 1) && (col == 6 || col == 7)) { //PURPLE
                checker(jumper, row+1, col-1, row+2, col-2, highlight); // down l
            } else if ((row == 6 || row == 7) && (col == 0 || col == 1)) { //PURPLE
                checker(jumper, row-1, col+1, row-2, col+2, highlight); // up r
            } else if ((row == 6 || row == 7) && (col == 6 || col == 7)) { //PURPLE
                checker(jumper, row-1, col-1, row-2, col-2, highlight); // up l
            } else if ((row > 1 && row < 6) && (col == 0 || col == 1)) { // ORANGE
                checker(jumper, row+1, col+1, row+2, col+2, highlight); // down r
                checker(jumper, row-1, col+1, row-2, col+2, highlight); // up r
            } else if ((row > 1 && row < 6) && (col == 6 || col == 7)) { // ORANGE
                checker(jumper, row+1, col-1, row+2, col-2, highlight); // down l
                checker(jumper, row-1, col-1, row-2, col-2, highlight); // up l
            } else if ((row == 0 || row == 1) && (col > 1 && col < 6)) { // ORANGE
                checker(jumper, row+1, col-1, row+2, col-2, highlight); // down l
                checker(jumper, row+1, col+1, row+2, col+2, highlight); // down r
            } else if ((row == 6 || row == 7) && (col > 1 && col < 6)) { // ORANGE
                checker(jumper, row-1, col-1, row-2, col-2, highlight); // up l
                checker(jumper, row-1, col+1, row-2, col+2, highlight); // up r
            }
        }
    }

    // This method takes in the destination of a player move and the relative location of the jumped piece
    // Whichever game piece is on the jumped board square is decremented
    public void decrementPlayerCount(int destRow, int destCol, int i, int j) {

        if ((view.getSquares()[destRow + i][destCol + j].getIcon() == view.getNiKing()) ||
                view.getSquares()[destRow + i][destCol + j].getIcon() == view.getNi()) {
            model.decrementNiCount();

        } else if ((view.getSquares()[destRow + i][destCol + j].getIcon() == view.getRobin()) ||
                view.getSquares()[destRow + i][destCol + j].getIcon() == view.getArthur()) {
            model.decrementArthurCount();
        }
    }

    // This method compares the landing location of a game piece to its last location on the board
    // Depending on the difference between landing loc and last loc, the method decides if a jump has been made
    // If it detects a jump, the method deletes (sets null) the piece in-between the jumper's landing and last location
    public void deleteOpponent(int destRow, int destCol) {
        if (destRow - model.getIconRow() == -2 && destCol - model.getIconCol() == 2) { // If this is a right jump up
            decrementPlayerCount(destRow, destCol, +1, -1); // deduct 1 from the relevant player's total
            view.getSquares()[destRow + 1][destCol - 1].setIcon(null); // Erase the opponent's piece
            model.setOpponentDeleted(true); // Used for tracking game state
            // Add the delete positions to the delete array for Submitting moves
            model.getDeleteRowCol()[deleteCounter] = destRow + 1;
            deleteCounter++;
            model.getDeleteRowCol()[deleteCounter] = destCol - 1;
            deleteCounter++;

        } else if (destRow - model.getIconRow() == -2 && destCol - model.getIconCol() == -2) { // Left jump up
            decrementPlayerCount(destRow, destCol, +1, +1);
            view.getSquares()[destRow + 1][destCol + 1].setIcon(null);
            model.setOpponentDeleted(true);
            model.getDeleteRowCol()[deleteCounter] = destRow + 1;
            deleteCounter++;
            model.getDeleteRowCol()[deleteCounter] = destCol + 1;
            deleteCounter++;

        } else if (destRow - model.getIconRow() == 2 && destCol - model.getIconCol() == 2) { // Right jump down
            decrementPlayerCount(destRow, destCol, -1, -1);
            view.getSquares()[destRow - 1][destCol - 1].setIcon(null);
            model.setOpponentDeleted(true);
            model.getDeleteRowCol()[deleteCounter] = destRow - 1;
            deleteCounter++;
            model.getDeleteRowCol()[deleteCounter] = destCol - 1;
            deleteCounter++;

        } else if (destRow - model.getIconRow() == 2 && destCol - model.getIconCol() == -2) { // Left jump down
            decrementPlayerCount(destRow, destCol, -1, +1);
            view.getSquares()[destRow - 1][destCol + 1].setIcon(null);
            model.setOpponentDeleted(true);
            model.getDeleteRowCol()[deleteCounter] = destRow - 1;
            deleteCounter++;
            model.getDeleteRowCol()[deleteCounter] = destCol + 1;
            deleteCounter++;
        } else {
            // Otherwise, reset OpponentDeleted, the deleteRowCol array, and the deleteCounter for the next turn
            model.setOpponentDeleted(false);
            model.resetDeleteRowCol();
            deleteCounter = 0;
        }
    }

    // This method checks if a game piece has reached the other end of the board, and turns it into a King if it has
    public void kingCheck(int row, int col) {
        if (row == 0) { // if on top row
            if (view.getSquares()[row][col].getIcon() == view.getArthur()) { // Check if Arthur
                view.getSquares()[row][col].setIcon(view.getRobin()); // Change to King (Robin)
                JFrame f = new JFrame();
                JOptionPane.showMessageDialog(f,"Brave, brave, Sir Robin!");
            }
        } else if (row == 7) { // if on bottom row
            if (view.getSquares()[row][col].getIcon() == view.getNi()) { // Check if Ni
                view.getSquares()[row][col].setIcon(view.getNiKing()); // Change to Ni King
                JFrame f = new JFrame();
                JOptionPane.showMessageDialog(f,"Ni! Ni! Ni! Ni! Ni! Ni!");
            }
        }
    }

    public static void main(String[] args) throws IOException {
        new Client();
    }
}