package code;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * This is the server code for the game of ConnectFour.
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


public class CFServer {

    /**
     * Matches the opponent as they join, starts the running of game
     */
    public static void main(String[] args) throws Exception {
        int port = 8901;
        //String ip = "127.0.0.1" ;//"10.103.1.125";
        ServerSocket ss = new ServerSocket(port);

        System.out.println("Connect Four Server is Running");
       // System.out.println("Adress: "+ss.getInetAddress());
        //System.out.println("port: "+ss.getLocalSocketAddress());
        try {
            while (true) {
                GamePlay gameplay = new GamePlay();
                GamePlay.Player competitorA = gameplay.new Player(ss.accept(), "RED");
                GamePlay.Player competitorB = gameplay.new Player(ss.accept(), "BLUE");
                competitorA.setRival(competitorB);
                competitorB.setRival(competitorA);
                gameplay.currPlayer = competitorA;
                competitorA.start();
                competitorB.start();
            }
        } finally {
            ss.close();
        }
    }
}

/**
 * CF is for 2 players.
 */
class GamePlay {

    /**
     * Our matrix will have 48 blocks.
     * Every block is either marked or it is unmarked by respective players.  
     * Hence we define the matrix using an array of the class Player.
     * If the block is empty, it is unmarked and hence not referenced to any of the players.
     * If marked, it is referenced to the respective player.
     *
     */
    private Player[] matrix = {
        null, null, null, null, null, null,null, null,
        null, null, null, null, null, null,null, null,
        null, null, null, null, null, null,null, null,
        null, null, null, null, null, null,null, null,
        null, null, null, null, null, null,null, null,
        null, null, null, null, null, null,null, null};

    /**
     * current player.
     */
    Player currPlayer;
 
    /**
     * Returns value true, if the given player has won
     */
    public boolean hasWon() {
       
         // Checking horizontally
        for (int j = 0 ; j< 5 ; j++){//column
            for (int i = 0 ; i < 48 ; i+=8){//row
                /* if(matrix[i+j]!= null){
                     System.out.println("horizontal check");
                    System.out.println("i"+i);
                    System.out.println("j"+j);
                }*/
                 
                if (    matrix[i + j]!= null && matrix[i +j]== matrix[i +j+1] && matrix[i +j] == matrix[i+j+2] && matrix[i +j] ==  matrix[i+j+3]){
                return true;
                }
            }
        }
        // Checking vertically
        for (int i = 0 ; i< 24 ; i+=8){
            for (int j = 0 ; j < 8 ; j++){
               
               /* if(matrix[i+j]!= null){
                    System.out.println("vertical check");
                    System.out.println("i = "+i);
                    System.out.println("j = "+j);
                }*/
                 
                if ( matrix[i  + j]!= null && matrix[i +j]== matrix[i+8 +j] && matrix[i +j] == matrix[i+(16) +j] && matrix[i +j] ==  matrix[i+(24) +j]){
                     
                   //System.out.println("i= "+i);
                   //System.out.println("j= "+j);
                return true;
                }
            }
        }
        // Checking ascending diagonally
        for (int i = 24 ; i< 48 ; i+=8){
            for (int j = 0 ; j <5 ; j++){
                if (    matrix[i + j]!= null && matrix[i +j]== matrix[(i-8) +j+1] && matrix[i +j] == matrix[i-16 +j+2] && matrix[i +j] ==  matrix[(i-24) +j+3]){
                return true;
                }
            }
        }
        // Checking descending diagonally
        for (int i = 24 ; i< 48 ; i+=8){
            for (int j = 3 ; j < 8; j++){
                if (    matrix[i  + j]!= null && matrix[i +j]== matrix[(i-8) +j-1 ] && matrix[i +j] == matrix[(i-16) +j-2] && matrix[i +j] ==  matrix[(i-24) +j-3]){
                return true;
                }
            }
        }
       
        return false;
    }

    /**
     * Returns true value if no null blocks remain.
     */
    public boolean matrixExhausted() {
        for (int k = 0; k < matrix.length; k++) {
            if (matrix[k] == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * This function is evoked every time a client makes or tries to
     * make a move by the respective thread.
     * Function checks whether the move made is valid
     * This is how it works:
     *   1)the client which requests the move is the current player.
     *   2)the block that they are trying to fill must be empty  
     *
     * If the move is checked to be valid:
     *   1)The state of the game will be updated
     *     that is, the block will be set according to the player and
     *     the other client will be assigned current player.
     *   2)The other client will be notified of the move made by the
     *     previous current client and will be updated accordingly.
     *
     */
   
    public synchronized int validMove(int position, Player player) {
        int minposition = (position % 8)+8*5;
        //System.out.println("pos = " + position);
        //System.out.println("min = " + minposition);
        //int k = position;
        for(int k = minposition ; k >= position ; k-= 8){
            if (player == currPlayer && matrix[k] == null) {
                //System.out.println("inside if.");
                matrix[k] = currPlayer;
                currPlayer = currPlayer.rival;
                currPlayer.rivalMoved(k);
                return k;
            }
        }
        return -1;
    }

    /**
     * Player class is the class that extends the Thread class for 
     * the implementation of this multi-threaded app.
     * Every client or competitor runs as an object of this class on a separate thread.
     * The competitors are both assigned a separate colour, i.e., red or blue. 
     * As a means of connection with the competitors that are the clients,
     * there is a socket having both i/p and o/p channels.
     * As the communication is only via messages, simply a writer and a reader is used.
     * 
     */
    class Player extends Thread {
       
        BufferedReader ip;
        Socket s;
        PrintWriter op;
        Player rival;
        String colour;

        /**
         * 
         * Builds handler-thread, does the initialisation of i/p and o/p channels.
         * Sends greetings in accordance to the colour of the contender.
         */
        public Player(Socket s, String colour) {
            this.colour = colour;
            this.s = s;
            
            try {
                op = new PrintWriter(s.getOutputStream(), true);
                ip = new BufferedReader(
                    new InputStreamReader(s.getInputStream()));
                op.println("HELLO " + colour);
                op.println("MSG Waiting for opponent to connect");
            } catch (IOException e) {
                System.out.println("Player died: " + e);
            }
        }

        /**
         * Is notified of the contender
         */
        public void setRival(Player rival) {
            this.rival = rival;
        }

        /**
         * Manages "RIVAL_MOVED" text
         */
        public void rivalMoved(int position) {
            op.println("RIVAL_MOVED " + position);
            op.println(
                hasWon() ? "LOSS" : matrixExhausted() ? "TIE" : "");
        }

        /**
         * Run function of the main thread
         */
        public void run() {
            try {
                // Will only begin to execute succeeding the players' connection
                op.println("MSG All players have connected.");

                // Notify the contender of their turn to make a move
                if (colour.equals("RED")) {
                    op.println("MSG Your move.");
                }

                // Receive instructions continuously about moves made by the players and execute them.
                while (true) {
                    String ins = ip.readLine();
                    if (ins.startsWith("MOVE")) {
                        int position = Integer.parseInt(ins.substring(5));
                        int validposition = validMove(position, this);
                        if (validposition!= -1) {
                            op.println("MOVE_CORRECT"+validposition);
                            op.println(hasWon() ? "WIN"
                                         : matrixExhausted() ? "TIE"
                                         : "");
                        } else {
                            op.println("MSG ?");
                        }
                    } else if (ins.startsWith("QUIT")) {
                        return;
                    }
                }
            } catch (IOException e) {
                    System.out.println("Player died: " + e);
                    rival.op.println("MSG abort");
                    try {s.close();
                } catch (IOException ex) {
                    Logger.getLogger(GamePlay.class.getName()).log(Level.SEVERE, null, ex);
                }
            } finally {
                try {s.close();} catch (IOException e) {}
            }
        }
    }
}
