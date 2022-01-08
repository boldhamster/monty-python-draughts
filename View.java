// 0904889y Emmet Young

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.io.IOException;

public class View extends JFrame {

    private JButton image; // Game logo
    private JLabel playerText; // "It's your/their turn"
    private JButton submit;
    private JButton receive;
    private JButton newGame;
    private JButton[][] squares = new JButton[8][8]; // Game board
    private Color colour1 = Color.GRAY;
    private Color colour2 = Color.LIGHT_GRAY;
    private Color colour3 = Color.ORANGE;
    private Color border = Color.DARK_GRAY;
    private String playerString;

    // Images for game pieces and logo
    private Icon arthur = new ImageIcon(ImageIO.read(getClass().getResource("arthur.png")));
    private Icon robin = new ImageIcon(ImageIO.read(getClass().getResource("robin.png")));
    private Icon ni = new ImageIcon(ImageIO.read(getClass().getResource("ni.png")));
    private Icon niKing = new ImageIcon(ImageIO.read(getClass().getResource("kingni.png")));
    private Icon mpTitle = new ImageIcon(ImageIO.read(getClass().getResource("mpTitle.png")));


    public View() throws IOException {
        submit = new JButton();
        receive = new JButton();
        newGame = new JButton();
        image = new JButton();

        // JButton appearance
        receive.setFont(new Font("Arial", Font.BOLD, 11));
        submit.setFont(new Font("Arial", Font.BOLD, 11));
        newGame.setFont(new Font("Arial", Font.BOLD, 14));
        newGame.setForeground(Color.WHITE);
        newGame.setMinimumSize(new Dimension(200,25));
        newGame.setMaximumSize(new Dimension(200,25));
        newGame.setPreferredSize(new Dimension(200,25));
        newGame.setBackground(Color.WHITE);
        newGame.setOpaque(true);
        newGame.setBorderPainted(true);
        newGame.setContentAreaFilled(false);
        newGame.setFocusPainted(false);
        image.setBackground(Color.GRAY);
        image.setOpaque(true);
        image.setBorderPainted(false);
        image.setContentAreaFilled(false);
        image.setFocusPainted(false);

        // JFrame's panels
        JPanel mainPanel = new JPanel();
        JPanel borderTop = new JPanel();
        JPanel borderBottom = new JPanel();
        JPanel borderBottomT = new JPanel();
        JPanel borderBottomM = new JPanel();
        JPanel borderBottomB = new JPanel();
        JPanel borderRight = new JPanel();
        JPanel borderLeft = new JPanel();
        JPanel boardPanel = new JPanel();

        // Set JPanels' background colour
        borderTop.setBackground(border);
        borderBottom.setBackground(border);
        borderBottomT.setBackground(border);
        borderBottomM.setBackground(border);
        borderBottomB.setBackground(border);
        borderRight.setBackground(border);
        borderLeft.setBackground(border);
        boardPanel.setBackground(border);

        // Create border for boardPanel
        Border blackLine = BorderFactory.createLineBorder(Color.WHITE, 3);
        boardPanel.setBorder(blackLine);

        // Add the mainPanel to the main JFrame
        this.add(mainPanel);

        // Bottom panel text and button
        GridBagLayout gl = new GridBagLayout();
        BoxLayout outerBox = new BoxLayout(borderBottom, BoxLayout.Y_AXIS);
        BoxLayout topBox = new BoxLayout(borderTop, BoxLayout.Y_AXIS);
        borderTop.setLayout(topBox);
        borderBottom.setLayout(outerBox);
        borderBottom.add(borderBottomT);
        borderBottom.add(borderBottomM);
        borderBottom.add(borderBottomB);

        // Game title image and player turn text
        borderTop.add(Box.createRigidArea((new Dimension(0,10))));
        playerText = new JLabel(playerString);
        playerText.setForeground(Color.WHITE);
        image.setAlignmentX(Component.CENTER_ALIGNMENT);
        playerText.setAlignmentX(Component.CENTER_ALIGNMENT);
        image.setIcon(mpTitle);
        borderTop.add(image);
        borderTop.add(Box.createRigidArea((new Dimension(0,10))));
        borderTop.add(playerText);

        // Add buttons to bottom panels
        borderBottomT.add(submit);
        borderBottomT.add(receive);
        borderBottomM.add(newGame);

        // Pad the other outer panels
        borderTop.add(Box.createRigidArea((new Dimension(50,20))));
        borderLeft.add(Box.createRigidArea((new Dimension(50,0))));
        borderRight.add(Box.createRigidArea((new Dimension(50,0))));

        // Add all other JPanels to the mainPanel
        BorderLayout bl = new BorderLayout();
        mainPanel.setLayout(bl);
        mainPanel.add(borderTop, BorderLayout.NORTH);
        mainPanel.add(borderBottom, BorderLayout.SOUTH);
        mainPanel.add(borderRight, BorderLayout.EAST);
        mainPanel.add(borderLeft, BorderLayout.WEST);
        mainPanel.add(boardPanel, BorderLayout.CENTER); // game panel

        // Create the boardPanel grid
        boardPanel.setLayout(new GridLayout(8,8));

        // Add new JButtons to the grid
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j <8; j++) {
                squares[i][j] = new JButton();
                squares[i][j].setBackground(colour2);

                // Change odd numbered tiles to lighter grey
                if ((i + j) % 2 != 0) {
                    squares[i][j].setBackground(colour1);
                }
                boardPanel.add(squares[i][j]);
            }
        }

        // Add starting 2 sets of 12 game pieces to the buttons
        addGamePieces();

        // Window appearance & settings
        setTitle("Monty Python & The Quest For The Holy Grail: Draughts");
        setSize(600, 600);
        this.setResizable(false);
        setLocationRelativeTo(null);
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    // This method uses an odd and an even array to add the game pieces at specific intervals
    public void addGamePieces() {
        int[] odds = {1,3,5,7};
        int[] evens = {0,2,4,6};

        for (int j = 0; j < 4; j++) {
            squares[0][odds[j]].setIcon(ni);
            squares[1][evens[j]].setIcon(ni);
            squares[2][odds[j]].setIcon(ni);
            squares[5][evens[j]].setIcon(arthur);
            squares[6][odds[j]].setIcon(arthur);
            squares[7][evens[j]].setIcon(arthur);
        }
    }

    // Getters and Setters

    public JButton getImage() {
        return image;
    }

    public JButton getReceive() {
        return receive;
    }

    public JButton getNewGame() {
        return newGame;
    }

    public JButton getSubmit() {
        return submit;
    }

    public JLabel getPlayerText() {
        return playerText;
    }

    public JButton[][] getSquares() {
        return squares;
    }

    public Color getColour1() {
        return colour1;
    }

    public Color getColour2() {
        return colour2;
    }

    public Color getColour3() {
        return colour3;
    }

    public Icon getArthur() {
        return arthur;
    }

    public Icon getRobin() {
        return robin;
    }

    public Icon getNi() {
        return ni;
    }

    public Icon getNiKing() {
        return niKing;
    }

}
