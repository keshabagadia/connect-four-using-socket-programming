#ConnectFourJava
This project provides an online multi-player game on single server using threads in java by using Socket and ServerSocket classes communicating by CFP (Connect Four protocol), with graphical interface on client side using Swing and the NetBeans IDE.

### What is Connect four ?
Connect-Four is a tic-tac-toe-like two-player game in which players alternately place pieces on a vertical board with columns and rows (8 x 6 for our project). 
Each player uses pieces of a particular color (red and blue for our project), and the object is to be the first to obtain four pieces in a horizontal, vertical, or diagonal line. 
Because the board is vertical, pieces inserted in a given column always drop to the lowest unoccupied row of that column. 
As soon as a column contains 6 pieces, it is full and no other piece can be placed in the column.

### Implementation
Client uses Graphical User Interface (GUI) programming with Swing and the NetBeans IDE. Uses CFP(connect four protocol) for client/server connection. Threaded to allow multiple clients to play concurrently on a single server.
To run the code into your computer install Netbeans IDE. The Java SE Development Kit (JDK) 8 is required to install NetBeans IDE. 
Import the given project to netbeans and then first complile and run the CFServer file. Then compile and run CFClient file twice for two players. For 4 players you have to run the CFClient file 4 times and so on.


### This project can be modified to support other games by building their own protocols like CFP (Connect Four Protocol).


