package code;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * This is the client code for the game of ConnectFour.
 * We followed the CFP (Connect Four protocol) that is more or less text-based.
 * The messages that are sent are as follows.:
 *
 *  C to S                      S to C
 *  ========================    =========================================
 *  MOVE(x) (x >= 0 & x < 48)   HELLO <String>  (String in {"RED", "BLUE"})
 *  QUIT                        MOVE_CORRECT
 *                              RIVAL_MOVED <n>
 *                              WIN
 *                              LOSS
 *                              TIE
 *                              MSG <text>
 *
 */

public class CFClient {

    private ImageIcon dot;
    private ImageIcon rivalDot;
    private JFrame gui = new JFrame("Connect Four");
    private JLabel msg = new JLabel("");

    private Socket s;
    private static int P = 8901;
    private BufferedReader ip;
    private PrintWriter op;
    
    
    private Block[] matrix = new Block[48];   
    private Block currentBlock;

    /**
     * To make the connection with the server, and construct the Graphical User Interface
     */
     
    public CFClient(String serverAddr) throws Exception {

       //Network setup
        s = new Socket("127.0.0.1", P);
        ip = new BufferedReader(new InputStreamReader(
            s.getInputStream()));
        op = new PrintWriter(s.getOutputStream(), true);

       //interface setup
        msg.setBackground(new java.awt.Color(204, 204, 204));
        gui.getContentPane().add(msg, "South");

        JPanel matrixPanel = new JPanel();
        matrixPanel.setBackground(new java.awt.Color(0, 0, 0));
        matrixPanel.setLayout(new GridLayout(6, 8, 2, 2));
        int l = matrix.length;
        for (int k = 0; k < l; k++) {
            final int i = k;
            matrix[k] = new Block();
            matrix[k].addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    currentBlock = matrix[i];
                    //System.out.println("MOVE " + j);
                    op.println("MOVE " + i);}});
            matrixPanel.add(matrix[k]);
        }
        gui.getContentPane().add(matrixPanel, "Center");
    }

  /**
     * In the client, there will be a thread which remains active and prepared for any realtime instructions by server.
     * An initial greeting of "HELLO" will be received, that is when the colour of the player is assigned.
     * We then enter a loop where messages "MOVE_CORRECT", "RIVAL_MOVED", "WIN",
     * "LOSS", "TIE", "MSG" are sent and handled appropriately as the game begins.
     * The messages "WIN", "LOSS" and "TIE" mark the potent end of the loop and the active game.
     * After that the player is asked if they want to continue playing.
     * Say , the player does not want to continue playing, then the loop finally ends and a "QUIT" message is sent to the server
     * to forfeit the connection. But if the player wants to continue playing then the game will restart;
     * with the same opponent if they answer the same or they will have to wait for a new opponent.
     * They maybe assigned a new color
     */
   
    public void play() throws Exception {
        String ins;
        try {
            ins =ip.readLine();
            if (ins.startsWith("HELLO")) {
                String colour = ins.substring(6);
                if(colour.equals("RED")){
                dot = new ImageIcon(getClass().getResource("/resources/red.png"));
                rivalDot = new ImageIcon(getClass().getResource("/resources/blue.png"));
                } else {
                    dot = new ImageIcon(getClass().getResource("/resources/blue.png"));
                    rivalDot = new ImageIcon(getClass().getResource("/resources/red.png"));
                }
               
                gui.setTitle("Connect Four - Player " + colour);
            }
            while (true) {
                ins =ip.readLine();
                if (begins(ins, "MOVE_CORRECT")) {
                    msg.setText("This move is valid, now wait for your chance.");
                    currentBlock=matrix[Integer.parseInt(ins.substring(12))];
                    currentBlock.setDot(dot);
                    currentBlock.repaint();
                } else if (begins(ins, "RIVAL_MOVED")) {
                    int pos = Integer.parseInt(ins.substring(12));
                    matrix[pos].setDot(rivalDot);
                    matrix[pos].repaint();
                    msg.setText("Your opponent has moved, it is your turn now");
                } else if (begins(ins, "WIN")) {
                    msg.setText("You have won! Congrats!");
                    break;
                } else if (begins(ins, "LOSS")) {
                    msg.setText("You have lost. Sorry :(");
                    break;
                } else if (begins(ins, "TIE")) {
                    msg.setText("You are tied with your opponent");
                    break;
                } else if (begins(ins, "MSG")) {
                    msg.setText(ins.substring(4));
                    if(ins.substring(4).equals("abort")){
                        msg.setText("Your opponent has disconnected :(");
                        break;
                    }
                }
                
            }
            op.println("QUIT");
        }
        finally {
            s.close();
        }
    }

    private boolean playOnceMore() {
        int ins = JOptionPane.showConfirmDialog(gui,
            "Do you want to have another round?",
            "This game is such FUN!!",
            JOptionPane.YES_NO_OPTION);
        gui.dispose();
        return ins == JOptionPane.YES_OPTION;
    }

    /**
     * Each block is a panel with background set to white.
     * Whenever a client makes a move the setIcon() function is called which makes the dot appear
     *
     */
    static class Block extends JPanel {
        JLabel b = new JLabel((Icon)null);

        public Block() {
            setBackground(new java.awt.Color(255, 255, 255));
            add(b);
        }

        public void setDot(Icon dot) {
            b.setIcon(dot);
        }
    }

    public boolean begins(String ins, String prefix) {
        return ins.startsWith(prefix);
    }

    /**
     * runs the app
     */
    public static void main(String[] args) throws Exception {
        while (true) {
            int l = args.length;
            String serverAddr = (l == 0) ? "127.0.0.1" : args[1];
         
            System.out.println(serverAddr);
            CFClient player = new CFClient(serverAddr);
            player.gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            player.gui.setSize(480, 320);
            player.gui.setVisible(true);
            player.gui.setResizable(false);
            player.play();
            if (!player.playOnceMore()) {
                break;
            }
        }
    }
}