import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.security.Key;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.DefaultTableModel;

//The beginning of the 'GameFrame' class where the grid start screen and grid is displayed to the player
public class GameFrame {
    //Setting up new variables/constants/objects for the game to use
    int CurrentCard = 2; //Numerical representation of what screen the player is currentl looking at
    CardLayout layout = new CardLayout(); //New layout that will be used on the JFrame
    JPanel panel = new JPanel(layout); //Initializing new JFrame using the layout
    JFrame frame = new JFrame(); //Pause JFrame
    String [][]grid; //Initializing the two dimensional string array
    int [][]path; //Initializing the shortest paht array to be used when the A-Star alrogithm returns the shortest path
    private String frameTitle; //Initializing the name of the JFrame
    int frameWidth = 1000; //Initializing the width of the JFrame as a constant
    int frameHeight = 800; //Initializing the height of the JFrame as a constant

    public int rowNum, columnNum; //Initializing variables for the number of rows and columns in the grid
    int playerColumn, playerRow; //Initializing variables for the current player position
    int originalPlayerRow, originalPlayerColumn; //Initilaixing variables for the orignal starting position

    boolean playerMove = true; //If the player is allowed to move or not (if set to false the player can not move)

    int mode; //What type of game the user is playing - 'easy'/'medium'/'hard'
    int numOfMoves; //Counter to how many times the player has made a valid move
    int numOfHints; //Counter to how many times the player has used a hint
    int score; //Initializing a variable to be used as the score once the player has completed the game
    long timeTaken; //Initializing a variable to be used as the total time taken for the user to complete the level
    long timeStart, timeEnd; //Initializing variables to be used as timestamps as to when the game started/ended
    long pauseTime = 0; //Initializing a variable to be used as the amount of time the player has 'paused' for
    long returnCounter; //Initializing a variable to be used as a time counter to how long until the display changes
    boolean giveUp; //If the user has pressed the 'giveup' button while playing the game
    boolean paused = false; //If the game has been 'paused' or not
    String announcementString; //Initializing a string variable for the 'announcement' string for the end of the game
    boolean updateGlobalStats = false; //If the top 10 leaderboard needs to be updated or not

    int userID; //Primary Key : ID - reference number of the users details in the 'users' table of the database

    int rectangleWidth = 30; //Constant value of how wide the squares on the grid should be
    int rectangleHeight = 30; //Constant value of how high the squares on the grid should be
    int gapInbetweenEachCell = 1; //How many pixels wide the gap inbetween each cell should be
    int startX = 0; //Initializing the starting position of the grid (used to make it center)
    int startY = 0; //Initializing the starting position of the grid (used to make it center)

    String lastCell = ""; //Used to store the contents of the last cell in the grid before the user moved into it

    //Private Class created so 'new KeyboardFocusManager' can be called
    //Used to listen to all keyboard inputs all accross the JFrame, regardless of screen or if a field is highlited
    private class InputEventManager implements KeyEventDispatcher {
        public boolean dispatchKeyEvent(KeyEvent key) {
            //Only looking at when a user presses a key and not releases or types
            if (key.getID() == KeyEvent.KEY_PRESSED) {
                if (key.getKeyCode() == KeyEvent.VK_P) { //Used for debugging to easily quit the program
                    System.out.println("SYSTEM EXIT");
                    System.exit(0);
                }
                if (key.getKeyCode() == KeyEvent.VK_ESCAPE) { //If the user presses the 'esc' key
                    if (CurrentCard == 1 && playerMove == true) { //If they are on the game frame playing a game
                        playerMove = false; //Stop the player from moving around the grid
                        paused = true; //Start the pause function
                        JFrame pause = new JFrame("Game Paused"); //Create a new 'Pause' JFrame
                        pause.add(new pauseButton(System.currentTimeMillis(), pause)); //Add 'pauseButton' object class
                        pause.setAlwaysOnTop(true); //Always have the 'pause' JFrame ontop of all window
                        pause.setVisible(true); //Set the JFrame to be visible
                        pause.pack(); //Lock all JFrame objects
                        pause.setLocationRelativeTo(null); //Set the JFrame to open in the center of the screen
                    }
                }
                if (key.getKeyCode() == KeyEvent.VK_W || key.getKeyCode() == KeyEvent.VK_A || key.getKeyCode() ==
                        KeyEvent.VK_S || key.getKeyCode() == KeyEvent.VK_D) { //If user pressed 'w'/'a'/'s'/d'
                    if (CurrentCard == 1 && playerMove == true) { //If user is playing the game
                        doPlayerMove(key); //Calling procedure to attempt the player move
                    }
                }
            }
            //IF input 'Released' or 'Typed' events needed, use 'KeyEvent.KEY_RELEASED' and 'KeyEvent.KEY_TYPED'
            return false; //Boolean return value required by method
        }
    }

    //Object initialization for the 'GameFrame'
    public GameFrame(int id){ //Only requires one argument - ID
        this.userID = id; //Setting the argument integer as a global object integer
        //Getting the 'Username' from the database using the 'userID' as the primary key of the 'users' table
        String quearyStatment = "SELECT users.username, users.id\n" +
                "FROM users\n" +
                "WHERE (((users.id)='" + userID + "'));\n"; //SQL statement to be used
        java.util.List strNeeded = new ArrayList();
        strNeeded.add("Username"); //Creating a new list and adding what the required results are
        List databaseQueary = new ArrayList(databaseConnection.quearyTable(quearyStatment, strNeeded)); //Queary
        String databaseUsername = String.valueOf(databaseQueary.get(0)); //Setting the return values to variables
        this.frameTitle = "Solver - Playing as " + databaseUsername; //Setting the text of the JFrame title
        //Adding classes to panel so they can be added to the JPanel CardLayout
        panel.add(new StartScreen(frameWidth, frameHeight), "start"); //Can be called by using 'start'
        panel.add(new GridDisplay(frameWidth, frameHeight), "game"); //Can be called by using 'game'
        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        manager.addKeyEventDispatcher(new InputEventManager()); //Adding the keyboard listener to the JFrame
        frame.add(panel); //Adding the panel content to the JFrame
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //Setting the game to quit if the user closes the window
        frame.setTitle(frameTitle); //Setting the title of the JFrame to the 'frameTitle' string
        frame.setSize(frameWidth, frameHeight); //Setting the size of the JFrame
        frame.setResizable(false); //Setting the JFrame so the user can't resize it
        frame.setLocationRelativeTo(null); //Setting the JFrame to open in the middle of the uses screen
        frame.pack(); //Lock all JFrame objects
        frame.setVisible(true); //Set the JFrame to be visible
    }

    public void displayGridAsText(){
        System.out.println("Grid: ");
        for(int c = 0; c < columnNum; c++) {
            for(int r = 0; r < rowNum; r++) {
                System.out.print(grid[c][r]); }
            System.out.println(); }
    }

    //Procedure to generate the score value at the end of the game using data gathered
    private void generateScore(){
        setTimeTaken(); //Update the time that the player took to finish the level
        //Score <-- (timeTaken/2) + (numberOfMovesPlayerMade-(A-StarShortestPathLength-2)) + (numberOfHintsUsed*10)
        score = (Long.valueOf(timeTaken).intValue()/2) + numOfMoves-(path.length-2) + (numOfHints * 10);
    }

    //Procedure to update the global variable of the time the player took to play the game
    //1000 milliseconds = 1 second - seconds = milliseconds/1000
    private void setTimeTaken(){
        timeTaken = ((Long.valueOf(((timeEnd - timeStart) / 1000)).intValue()) - pauseTime);
    }

    //Update the database with the data gathred from the users level just played
    private void updateDatabase(){
        //Setting up the SQL 'INSERT' statement to be used
        String sql = "INSERT INTO scores (user_id, levels_number, score, time_taken, num_of_moves, " +
                "hints_used, date_played)\n" +
                "VALUES (" + userID + ", " + mode + ", " + score + ", " + timeTaken + ", " + numOfMoves
                + ", " + numOfHints + ", date('now'))";
        //Running the SQL statement
        databaseConnection.executeStatement(sql);
    }

    //Switch what screen the user is seeing in the 'layout manager'
    //1 = game screen
    //2 = start screen
    public String changeCard(){
        //Switching values round
        if (CurrentCard == 1) {
            CurrentCard = 2;
            System.out.println("Going to START");
            return "start";        } //Returning what screen to show next
        if (CurrentCard == 2) {
            CurrentCard = 1;
            System.out.println("Returning to GRID");
            return "game";        } //Returning what screen to show next
        return "NULL"; //This return statement is never used
    }

    //Update the players current position in the grid
    public void updateCurrentPlayerPosition(){
        for(int c = 0; c < columnNum; c++) {
            for(int r = 0; r < rowNum; r++) { //For every node in the grid
                String node = grid[c][r]; //Check that node
                if("S".equals(node)){ //Is equil to a 'S' - player cell
                    this.playerColumn = c; //Update the objects 'playerColumn' value
                    this.playerRow = r; //Update the objects 'playerRow' value
                }
            }
        }
    }

    //Get the shortest possible path
    public void importShortestPathToGrid(){
        String cellType; //Setting up a string variable to be used
        updateCurrentPlayerPosition(); //Updating the current players position in the object
        for(int counter = 1; counter < path.length; counter++){ //Loop for every move in the shortest path
            if (counter == 0){ //If the path is the starting point in the path
                cellType = "S";
            }
            else if (counter == path.length){ //If the path is the ending point in the path
                cellType = "F";
            }
            else{
                cellType = "A"; //Any other path
            }
            grid[path[counter][1]][path[counter][0]] = cellType; //Update the grid with these cell types
        }
        grid[0][0] = "0";
        grid[originalPlayerColumn][originalPlayerRow] = "S"; //Update the grid with the original starting point
        grid[playerColumn][playerRow] = "S"; //Update the grid with the players current position in the grid
    }

    //Procedure to move the player in the grid
    public void doPlayerMove(KeyEvent key){
        updateCurrentPlayerPosition(); //Update the players current position
        String direction = getPlayerMoveDirection(key); //Getting the movement from the key input
        if(checkPlayerMoveValid(direction) == false){ //If the players move isn't valid
            System.out.println("Move " + direction + " is invalid!");
            return; //Return - don't run the rest of the procedure
        }
        if(direction == "UP"){ //If the movement is 'Up' from the current position - update the grid
            grid[playerColumn][playerRow] = "0";
            lastCell = grid[playerColumn-1][playerRow];
            grid[playerColumn-1][playerRow] = "S";
        }
        if(direction == "DOWN"){ //If the movement is 'Down' from the current position - update the grid
            grid[playerColumn][playerRow] = "0";
            lastCell = grid[playerColumn+1][playerRow];
            grid[playerColumn+1][playerRow] = "S";
        }
        if(direction == "LEFT"){ //If the movement is 'Left' from the current position - update the grid
            grid[playerColumn][playerRow] = "0";
            lastCell = grid[playerColumn][playerRow-1];
            grid[playerColumn][playerRow-1] = "S";
        }
        if(direction == "RIGHT"){ //If the movement is 'Right' from the current position - update the grid
            grid[playerColumn][playerRow] = "0";
            lastCell = grid[playerColumn][playerRow+1];
            grid[playerColumn][playerRow+1] = "S";
        }
        panel.repaint(); //Update and refresh the screen
        panel.revalidate(); //Update and refresh the screen
        this.numOfMoves = this.numOfMoves + 1; //Add one to the players movement counter
        if(checkIfWon(lastCell) == true){ //If the player has won the game
            System.out.println("You Won");
            returnCounter = System.currentTimeMillis(); //Start the return counter time
            timeEnd = System.currentTimeMillis(); //Set the finishing time
            generateScore(); //Generate the score
            //Seting the 'anncounementString' with values from the users game as a conclution
            announcementString = "You Won in '" + numOfMoves + "' moves with a score of '" + score
                    + "'<br> Shortest Path Length - " + (path.length-2);
            updateDatabase(); //Update the database with all the values from this game
        }
    }

    //Function to return the direction of movement as a string from the key input
    public String getPlayerMoveDirection(KeyEvent key){
        String direction;
        if(key.getKeyCode() == KeyEvent.VK_W){ direction = "UP"; } //'W' = 'UP'
        else if(key.getKeyCode() == KeyEvent.VK_A){ direction = "LEFT"; } //'A' = 'LEFT'
        else if(key.getKeyCode() == KeyEvent.VK_S){ direction = "DOWN"; } //'D' = 'DOWN'
        else{ direction = "RIGHT"; } //'S' = 'RIGHT'
        return direction; //Return the new string
    }

    //Function to check if a players move is valid
    public boolean checkPlayerMoveValid(String direction){ //One argument - direction of movement
        boolean valid = true; //Movement is always valid until set to false
        //check not edge so player does not go off grid and will not hit a wall
        if(direction == "UP"){
            if(playerColumn == 0 || "X".equals(grid[playerColumn-1][playerRow])){
                valid = false;
            }
        }
        if(direction == "DOWN"){
            if(playerColumn == columnNum-1 || "X".equals(grid[playerColumn+1][playerRow])){
                valid = false;
            }
        }
        if(direction == "LEFT"){
            if(playerRow == 0 || "X".equals(grid[playerColumn][playerRow-1])){
                valid = false;
            }
        }
        if(direction == "RIGHT"){
            if(playerRow == rowNum-1 || "X".equals(grid[playerColumn][playerRow+1])){
                valid = false;
            }
        }
        return valid; //Return the boolean variable - only set to false if the next position is a wall / 'X'
    }

    //Function to check if the player has entered the finishing cell - 'F'
    public boolean checkIfWon(String lastCell){
        if("F".equals(lastCell)){ //If the cell they are on is equil to 'F'
            return true; //Return true
        }else{ //If not
            return false; //Return false
        }
    }

    //Function to return in text value what type of grid the user has chosen to play
    public String getModeText(){
        String type;
        if(mode == 1){
            type = "easy";
        }
        else if (mode == 2){
            type = "medium";
        }
        else if (mode == 3){
            type = "hard";
        }else{
            type = "custom";
        }
        return type; //Return the string
    }

    //Updating all object variables that need to be to start a new game
    private void resetValuesForNewGame(String[][] grid){
        Object search = new SearchAlgorithm(grid); //Running the A-Star algorithm on the grid to be used
        pauseTime = 0; //Resetting the pause time
        path = ((SearchAlgorithm) search).getPath(); //Getting the shortest path from the A-Star algorithm
        columnNum = grid.length; //Setting the number of columns in the grid
        rowNum = grid[0].length; //Setting the number of rows in the grid
        giveUp = false; //Resetting the 'giveUp' variable
        numOfMoves = 0; //Resetting the number of moves to 0
        numOfHints = 0; //Resettign the number of hints used to 0
        returnCounter = 0; //Restting the return counter to 0
        score = 0; //Resetting the score to 0
        playerMove = true; //Allowing the player to move
        timeStart = System.currentTimeMillis(); //Setting the start time
        layout.show(panel, changeCard()); //Change the screen
        CurrentCard = 1;
    }

    //Starting the game - function used when the 'GameFrame' is called
    public static void startGame(int user_row){
        GameFrame gframe = new GameFrame(user_row);
    }

    //StartScreen JPanel - 'start'
    //Used in a 'class' format so that the entire class and its content can be called when setting up the JFrame
    class StartScreen extends JPanel {
        public String selectedFilePath; //Initializing a string for the filepath of a custom map
        int frameX, frameY; //Initializing variables for the JPanel size
        JButton useCustomGridButton = new JButton("use custom grid"); //Setting up JButton
        JLabel statsBoard = new JLabel(); //Setting up JLabel
        String statsString; //Initializing string for the statistics 'board'
        DefaultTableModel model = new DefaultTableModel(); //Setting up JTable
        JTable table = new JTable(model); //Setting up JTable

        //StartScreen object initialization
        public StartScreen(int X, int Y) { //Requires two arguments - the size of the JFrame
            this.frameX = X;
            this.frameY = Y;
            setLayout(null); //Setting the JFrame to allow for .setBounds

            JLabel welcomeMessage = new JLabel("Solver", SwingConstants.CENTER); //Setting up JLabel
            welcomeMessage.setBounds(0,0,1000,345); //Position/Size of label
            welcomeMessage.setFont(new Font("Arial", Font.PLAIN, 80)); //Font formatting of text
            add(welcomeMessage); //Adding JLabel to JPanel content

            //FUNCTIONS SECTION
            //Quit Button
            JButton quitButton = new JButton("Quit");
            quitButton.setBounds(895,735,100, 60); //Position/Size of button
            //Listener for if the button is pressed
            quitButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    System.exit(0);
                } //Quit the game
            });
            add(quitButton); //Adding JButton to JPanel content
            //Logout Button
            JButton logoutButton = new JButton("Logout");
            logoutButton.setBounds(795,735,100, 60); //Position/Size of button
            //Listener for if the button is pressed
            logoutButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    System.out.println("Logging out, returning to login screen.");
                    loginFrames.loginScreen(); //Show the logout screen
                    frame.dispose(); //Close the 'GameFrame;
                }
            });
            add(logoutButton); //Adding JButton to JPanel content
            //Help Button
            JButton helpButton = new JButton("Help");
            helpButton.setBounds(695,735,100, 60); //Position/Size of button
            //Listener for if the button is pressed
            helpButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    System.out.println("HELP");
                    playerMove = false; //Stop allowing the player to move
                    JFrame help = new JFrame("Help"); //Setting up new JFrame
                    help.add(new helpFrame(help)); //Adding 'helpFrame' JPanel content to the new JFrame
                    help.setAlwaysOnTop(true); //Setting the 'help' JFrame to always be ontop
                    help.setVisible(true); //Setting the 'help' JFrame to be visible
                    help.pack(); //Packing all of the JPanel content
                    help.setLocationRelativeTo(null); //Set the location to open in the center of the screen
                }
            });
            add(helpButton); //Adding JButton to JPanel content

            //Label for 'gamemode selection'
            JLabel selectLabel = new JLabel("Select a gamemode");
            selectLabel.setBounds( 735, 320,200, 30); //Position/Size of label
            add(selectLabel); //Adding JLabel to JPanel content

            //GAME SELECTION SECTION
            //Easy Button
            JButton easySelectionButton = new JButton("Easy");
            easySelectionButton.setBounds( 695,345,200, 30); //Position/Size of label
            //Listener for if the button is pressed
            easySelectionButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    //Starting object to create a new 20x20 'easy' grid
                    Object rGrid = new randomGrid("easy", 20, 20);
                    grid = ((randomGrid) rGrid).generateGrid(); //Generate the grid
                    originalPlayerColumn = ((randomGrid) rGrid).startColumn; //Update the starting point
                    originalPlayerRow = ((randomGrid) rGrid).startRow; //Update the starting point
                    resetValuesForNewGame(grid); //Reset values and switch to 'game' frame
                    mode = 1; //Update the mode to 1 - 'easy'
                }
            });
            add(easySelectionButton); //Adding JButton to JPanel content
            //Medium Button
            JButton mediumSelectionButton = new JButton("Medium");
            mediumSelectionButton.setBounds(695,370,200,30); //Position/Size of label
            //Listener for if the button is pressed
            mediumSelectionButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    //Starting object to create a new 20x20 'medium grid
                    Object rGrid = new randomGrid("medium", 20, 20);
                    grid = ((randomGrid) rGrid).generateGrid(); //Generate a random grid
                    originalPlayerColumn = ((randomGrid) rGrid).startColumn; //Update the starting point
                    originalPlayerRow = ((randomGrid) rGrid).startRow; //Update the starting point
                    resetValuesForNewGame(grid); //Reset values and switch to 'game' frame
                    mode = 2; //Update the mode to 2 - 'medium'
                }
            });
            add(mediumSelectionButton); //Adding JButton to JPanel content
            //Hard Button
            JButton hardSelectionButton = new JButton("Hard");
            hardSelectionButton.setBounds(695,395,200,30); //Position/Size of label
            //Listener for if the button is pressed
            hardSelectionButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    //Starting object to create a new 20x20 'hard' grid
                    Object rGrid = new randomGrid("hard", 20, 20);
                    grid = ((randomGrid) rGrid).generateGrid(); //Generate a random grid
                    originalPlayerColumn = ((randomGrid) rGrid).startColumn; //Update the starting point
                    originalPlayerRow = ((randomGrid) rGrid).startRow; //Update the starting point
                    resetValuesForNewGame(grid); //Reset values and switch to 'game' frame
                    mode = 3; //Update the mode to 3 - 'hard'
                }
            });
            add(hardSelectionButton); //Adding JButton to JPanel content
            //File Path Display Text Field
            JTextField filePath = new JTextField(selectedFilePath);
            filePath.setBounds(695,420,200,30); //Position/Size of label
            filePath.setEditable(false); //Setting the text field to not be editable
            add(filePath); //Adding JTextField to JPanel content

            //Label for 'filepath'
            JLabel importLabel = new JLabel("Filepath");
            importLabel.setBounds(645,420,200,30); //Position/Size of label
            add(importLabel); //Adding JLable to JPanel content

            //Browse Button
            JButton browseButton = new JButton("Browse");
            browseButton.setBounds(895,420,100,30); //Position/Size of label
            //Listener for if the button is pressed
            browseButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    //Starting creation of a new JFrame to allow the user to browse their files for a grid .txt file
                    JFrame browseFrame = new JFrame();
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
                    int result = fileChooser.showOpenDialog(browseFrame);  //Show file chooser in JFrame
                    if (result == JFileChooser.APPROVE_OPTION) { //If user has pressed 'ok'
                        File selectedFile = fileChooser.getSelectedFile(); //Getting file path
                        selectedFilePath = selectedFile.getAbsolutePath(); //Setting string value of file path
                        System.out.println("Selected file: " + selectedFilePath);
                    }
                    filePath.setText(selectedFilePath); //Setting the JTextField text as the file path
                    try{ //Attempt
                        int lineCount, largestNumberOfElements;
                        Object rf = new RetrieveFile(selectedFilePath); //Setting up 'RetrieveFile' object
                        String [][]selectedGrid = ((RetrieveFile) rf).getFile(); //Getting the grid
                        boolean valid = ((RetrieveFile) rf).checkValidArray(selectedGrid); //Getting if the grid is valid
                        int []vals = ((RetrieveFile) rf).getLinesNodesAmount(); //Getting the number of rows/columns
                        lineCount = vals[0]; //Setting number of rows to a variable
                        largestNumberOfElements = vals[1]; //Setting number of nodes on a line to a variable
                        Object search = new SearchAlgorithm(selectedGrid); //Setting up a A-Star object
                        boolean gridTraversable = ((SearchAlgorithm) search).pathPossible; //Getting if the grid is traversable
                        if(valid == true && gridTraversable == true){ //If the grid is valid and traversable
                            //Set the values to use the custom grid for the game
                            System.out.println("Grid from '" + selectedFilePath + " is valid");
                            System.out.println("GRID STATS\n\tGrid is " + lineCount + " x " + largestNumberOfElements + "\n");
                            grid = selectedGrid;
                            displayGridAsText();
                            originalPlayerRow = ((RetrieveFile) rf).playerRow;
                            originalPlayerColumn = ((RetrieveFile) rf).playerCol;
                            useCustomGridButton.setEnabled(true); //Enable the 'useCustomeGrid' button
                        }else{
                            System.out.println("Grid from text file invalid");
                        }
                    }catch(Exception except){ //Exception handling if an error occurs reading the file
                        System.out.println(except);
                    }
                }
            });
            add(browseButton); //Adding JButton to JPanel content
            //Play Custom Grid Button
            //This button can only be used if the file path selected has a valid grid to be used in the game
            useCustomGridButton.setBounds(695,445,200,30); //Position/Size of button
            useCustomGridButton.setEnabled(false); //Setting the ability to click on the button as 'false'
            //Listener for if the button is pressed
            useCustomGridButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    useCustomGridButton.setEnabled(false); //Set as false
                    filePath.setText(""); //Reset the text of the JTextField
                    resetValuesForNewGame(grid); //Run the new grid
                    mode = 4; //Set the mode as 'custom'
                }
            });
            add(useCustomGridButton); //Adding JButton to JPanel content

            ///Latest Stats Section
            statsBoard.setBounds(400,frameY/2-80,200,150); //Position/Size of JLabel
            add(statsBoard); //Adding JLabel to JPanel content

            JLabel scoresLable = new JLabel("High Scores Ranking", SwingConstants.CENTER);
            scoresLable.setBounds(30, frameY/2-108, 250, 30); //Position/Size of Label
            add(scoresLable); //Adding JLabel to JPanel content

            //Setting up JLabel
            model.addColumn("ID");
            model.addColumn("Username");
            model.addColumn("Score");
            List databaseQueary1 = new ArrayList(databaseConnection.getHighScores()); //Getting top 10 highest scores from database
            //Adding all results to the JTabel
            model.addRow(new Object[] { "<html><b>Ranking</b><</html>", "<html><b>Username</b><</html>", "<html><b>Score</b><</html>"});
            model.addRow(new Object[] { "1", String.valueOf(databaseQueary1.get(0)), String.valueOf(databaseQueary1.get(1))});
            model.addRow(new Object[] { "2", String.valueOf(databaseQueary1.get(2)), String.valueOf(databaseQueary1.get(3)) });
            model.addRow(new Object[] { "3", String.valueOf(databaseQueary1.get(4)), String.valueOf(databaseQueary1.get(5))});
            model.addRow(new Object[] { "4", String.valueOf(databaseQueary1.get(6)), String.valueOf(databaseQueary1.get(7)) });
            model.addRow(new Object[] { "5", String.valueOf(databaseQueary1.get(8)), String.valueOf(databaseQueary1.get(9))});
            model.addRow(new Object[] { "6", String.valueOf(databaseQueary1.get(10)), String.valueOf(databaseQueary1.get(11)) });
            model.addRow(new Object[] { "7", String.valueOf(databaseQueary1.get(12)), String.valueOf(databaseQueary1.get(13))});
            model.addRow(new Object[] { "8", String.valueOf(databaseQueary1.get(14)), String.valueOf(databaseQueary1.get(15)) });
            model.addRow(new Object[] { "9", String.valueOf(databaseQueary1.get(16)), String.valueOf(databaseQueary1.get(17))});
            model.addRow(new Object[]  { "10", String.valueOf(databaseQueary1.get(18)), String.valueOf(databaseQueary1.get(19)) });
            table.setBounds(30,frameY/2-80,250, 175); //Position/Size of JTabel
            add(table); //Adding JTabel to JPanel content
        }

        public void paintComponent(Graphics g){
            //Update top 10 leaderboard
            if(updateGlobalStats){ //If the program should update the top 10 player statistics
                for(int counter = 0; counter < 10; counter++){
                    model.removeRow(1);
                }
                List databaseQueary1 = new ArrayList(databaseConnection.getHighScores()); //Get top 10 high scores from database
                //Adding all results to JTabel
                model.addRow(new Object[] { "1", String.valueOf(databaseQueary1.get(0)), String.valueOf(databaseQueary1.get(1))});
                model.addRow(new Object[] { "2", String.valueOf(databaseQueary1.get(2)), String.valueOf(databaseQueary1.get(3)) });
                model.addRow(new Object[] { "3", String.valueOf(databaseQueary1.get(4)), String.valueOf(databaseQueary1.get(5))});
                model.addRow(new Object[] { "4", String.valueOf(databaseQueary1.get(6)), String.valueOf(databaseQueary1.get(7)) });
                model.addRow(new Object[] { "5", String.valueOf(databaseQueary1.get(8)), String.valueOf(databaseQueary1.get(9))});
                model.addRow(new Object[] { "6", String.valueOf(databaseQueary1.get(10)), String.valueOf(databaseQueary1.get(11)) });
                model.addRow(new Object[] { "7", String.valueOf(databaseQueary1.get(12)), String.valueOf(databaseQueary1.get(13))});
                model.addRow(new Object[] { "8", String.valueOf(databaseQueary1.get(14)), String.valueOf(databaseQueary1.get(15)) });
                model.addRow(new Object[] { "9", String.valueOf(databaseQueary1.get(16)), String.valueOf(databaseQueary1.get(17))});
                model.addRow(new Object[]  { "10", String.valueOf(databaseQueary1.get(18)), String.valueOf(databaseQueary1.get(19)) });
                updateGlobalStats = false; //Setting the updateGlobalStats to false again - complete
            }
            //Update latest stats board for the statistics of the latest played game
            //SQL Queary statement to get the username
            String sql = "SELECT max(id) FROM scores WHERE user_id = '" + userID + "';";
            java.util.List strNeeded = new ArrayList();
            strNeeded.add("max(id)");
            List databaseQueary = new ArrayList(databaseConnection.quearyTable(sql, strNeeded)); //Queary database
            String id = String.valueOf(databaseQueary.get(0)); //Setting return values to variables
            try{
                if(id != null) {
                    //Setting SQL queary statement
                    String sqlQueary = "SELECT levels.level_type, scores.score, scores.time_taken, scores.num_of_moves, scores.hints_used, scores.date_played\n" +
                            "FROM levels\n" +
                            "LEFT JOIN\n" +
                            "scores\n" +
                            "on levels.level_number = scores.levels_number\n" +
                            "WHERE scores.id = '" + id + "';";
                    List strNeeded2 = new ArrayList(); //Setting values needed back from the database
                    strNeeded2.add("date_played");
                    strNeeded2.add("level_type");
                    strNeeded2.add("score");
                    strNeeded2.add("time_taken");
                    strNeeded2.add("num_of_moves");
                    strNeeded2.add("hints_used");
                    List databaseQueary2 = new ArrayList(databaseConnection.quearyTable(sqlQueary, strNeeded2)); //Starting queary
                    //Setting JLabel text
                    statsString = "<html><center><b>Statistics from latest game</b><br>Last Played Date: " + String.valueOf(databaseQueary2.get(0)) +
                            "<br>Mode: " + String.valueOf(databaseQueary2.get(1)) +
                            "<br>Score: " + String.valueOf(databaseQueary2.get(2)) +
                            "<br>Time: " + String.valueOf(databaseQueary2.get(3)) +
                            "<br>Moves: " + String.valueOf(databaseQueary2.get(4)) +
                            "<br>Hints Used: " + String.valueOf(databaseQueary2.get(5)) +
                            "</center></html>";
                }
            }catch (Exception e){ //If there is an error or database couldn't find any stats
                    //Set no statistics message
                    statsString = "<html><center><b>No statistics saved yet</center></html>";
            }
            //Update JLabel
            statsBoard.setText(statsString);
            repaint();
        }

        public Dimension getPreferredSize() {
            return new Dimension(frameX, frameY);
        } //Setting dimentions of JPanel
    }

    //GridDisplay JPanel - 'game'
    //Used in a 'class' format so that the entire class and its content can be called when setting up the JFrame
    class GridDisplay extends JPanel {
        int frameX, frameY; //JPanel Size
        JLabel statistics = new JLabel();
        JLabel announcement = new JLabel();
        String usernameStr; //Initializing username String

        //Object initialization
        GridDisplay(int frameX, int frameY){ //Requires two arguments - JPanel dimensions
            setLayout(null); //Setting the JFrame to allow for .setBounds
            this.frameX = frameX;
            this.frameY = frameY;

            //Getting the username from the database by using the 'userID'
            String quearyStatment = "SELECT users.username, users.id\n" + //Setting SQL statement
                    "FROM users\n" +
                    "WHERE (((users.id)='" + userID + "'));\n";
            java.util.List strNeeded = new ArrayList();
            strNeeded.add("Username"); //Setting required return values
            List databaseQueary = new ArrayList(databaseConnection.quearyTable(quearyStatment, strNeeded)); //Queary
            String databaseUsername = String.valueOf(databaseQueary.get(0)); //Setting return values to variables
            usernameStr = "Playing as: " + databaseUsername; //Setting JFrame title string

            statistics.setBounds(10, 0, 300, 100); //Position/Size of label
            add(statistics); //Adding JLabel to JPanel content
            announcement.setBounds((1000/2)-(300/2),745,300,50); //Position/Size of label
            add(announcement); //Adding JLabel to JPanel content

            JButton hintButton = new JButton("Hint");
            hintButton.setBounds(895,555,100, 60); //Position/Size of button
            //Listener for if the button is pressed
            hintButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    updateCurrentPlayerPosition(); //Update current player position
                    Object search = new SearchAlgorithm(grid); //Setup new A-Star search object
                    int pathLength = ((SearchAlgorithm) search).getPathLength(); //Get the shortest path length
                    if ((pathLength-2) > 15){ //If shorteset path is greater than 15 moves
                        int[][] path = ((SearchAlgorithm) search).getPath(); //Get the shortest path
                        grid[playerColumn][playerRow] = "0"; //Set the current player position to '0'
                        int newColumn = path[path.length-6][0];
                        int newRow = path[path.length-6][1];
                        grid[newRow][newColumn] = "S"; //Setting player position 5 moves from the current one
                        frame.repaint(); //Repaint the grid
                        updateCurrentPlayerPosition(); //Update the current player position
                        numOfMoves = numOfMoves + 10; //Add 10 to the number of moves counter
                        numOfHints++; //Add one to the number of hints used counter
                    }
                }
            });
            add(hintButton); //Adding JButton to JPanel content
            //SEtting up label to show controls
            JLabel label = new JLabel("<html>Controls<br>W - forward<br>A - left<br>S - down<br> D - " +
                    "right<br>ESC - pause</html>"); //Using HTML formatting to format text in JLabel
            label.setBounds(10,245, 1000, 1000); //Position/Size of label
            add(label); //Adding JLabel to JPanel content

            //Setting up 'help' button
            JButton helpButton = new JButton("Help");
            helpButton.setBounds(895,675,100, 60); //Position/Size of button
            //Listener for if the button is pressed
            helpButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    playerMove = false; //Stop the player from moving
                    JFrame help = new JFrame("Help"); //Setting up new JFrame
                    help.add(new helpFrame(help)); //Adding new 'helpFrame' JPanel content class
                    help.setAlwaysOnTop(true); //Set the window to always be on top of any other windows
                    help.setVisible(true); //Set the window to be visible
                    help.pack(); //Packing all JPanel content
                    help.setLocationRelativeTo(null); //Setting the window to start in the middle of the screen
                }
            });
            add(helpButton); //Adding JButton  to JPanel content

            //Setting up 'give up' button
            JButton giveUpButton = new JButton("Give Up");
            giveUpButton.setBounds(895,615,100, 60); //Position/Size of button
            //Listener for if the button is pressed
            giveUpButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    giveUp = true; //Player has given up
                    returnCounter = System.currentTimeMillis(); //Start return counter
                    announcementString = "Given Up"; //Setting anouncement string to show the player has given up
                }
            });
            add(giveUpButton); // Adding JButton to JPanel content

            //Setting up a 'pause' button
            JButton pauseButton = new JButton("Pause");
            pauseButton.setBounds(895,735,100, 60); //Position/Size of button
            //Listener for if the button is pressed
            pauseButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    playerMove = false; //Stop the player from moving
                    paused = true; //Set the game as paused
                    JFrame pause = new JFrame("Game Paused"); //Setup new window to show the game is paused
                    pause.add(new pauseButton(System.currentTimeMillis(), pause)); //Adding pauseButton JPanel content
                    pause.setAlwaysOnTop(true); //Setting the window to always be on top
                    pause.setVisible(true); //Setting the window to be visible
                    pause.pack(); //Packing all objects to the JFrame
                    pause.setLocationRelativeTo(null); //Opening the window in the middle of the screen
                }
            });
            add(pauseButton); //Adding the JButton to JPanel content

            JButton returnButton = new JButton("Back");
            returnButton.setBounds(895,495,100, 60); //Position/Size of button
            //Listener for if the button is pressed
            returnButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    layout.show(panel, changeCard()); //Change visible JPanel / go back to 'start'
                }
            });
            add(returnButton); //Adding the JButton to JPanel content
        }

        //Paint Component of the grid - How the grid and variables that need to be updated are updated
        public void paintComponent(Graphics g){
            int currentX = startX; //Starting position of the grid in the JPanel
            int currentY = startY; //Starting position of the grid in the JPanel
            //Setting up string values to be displayed to the user in the 'statistics' section
            String moveStr = "Moves: " + numOfMoves; //String of the moves counter
            String timeStr = "Time passed: " + ((Long.valueOf(((System.currentTimeMillis() - timeStart) /
                    1000)).intValue()) - pauseTime); //String of the time the user has taken so far
            String modeStr = "Mode: " + getModeText(); //String of the mode of the game
            String hintStr = "Hints: " + numOfHints; //String of the number of hints the user has used

            setGridCenter(); //Set the grid to start in the center of the JFrame
            for(int c = 0; c < columnNum; c++){
                for(int r = 0; r < rowNum; r++){ //For each cell in the 2 dimensional array
                    String node = grid[c][r]; //Set the node to a string to easily be compared in 'if' statements
                    if("S".equals(node)){ //If player/starting node
                        g.setColor(Color.green); //Set the square colour as green
                    }
                    else if("F".equals(node)){ //If finishing/end/ node
                        g.setColor(Color.red); //Set the square colour as red
                    }
                    else if("X".equals(node)){ //If a wall / 'X' node
                        g.setColor(Color.gray); //Set the colour to grey
                    }
                    else if("A".equals(node)){ //If a A-Star shortest path position
                        g.setColor(Color.yellow); //Set the colour to square
                    }
                    else{ //If a normal '0' node
                        g.setColor(Color.lightGray); //Set the colour to light grey
                    }
                    //Paint the actual square on the JPanel to the correct position
                    g.fillRect(currentX,currentY,rectangleWidth, rectangleHeight);
                    //Update the position of the next cell column position relative to the JPanel
                    currentX = currentX + rectangleWidth + gapInbetweenEachCell;
                }
                //Update the position of the next cell row position relative to the JPanel
                currentY = currentY + rectangleHeight + gapInbetweenEachCell;
                currentX = startX; //Reset the starting point of the column
            }

            if(returnCounter > 0 || giveUp == true){ //If the game has ended or the player has 'given up'
                updateGlobalStats = true; //The 'start' screen will need  to update the top 10 scores
                playerMove = false; //Stop the player from moving
                importShortestPathToGrid(); //Display the shortest path to the grid
                long now = System.currentTimeMillis(); //Set a the current time to a variable
                int counter = Long.valueOf((now - returnCounter) / 1000).intValue(); //Calculate the return counter
                announcement.setText("<html><center>" + announcementString + "<br>Returning to grid in " +
                        (5-counter) + "</center></html>"); //Set the announcement text
                if (counter > 5){ //If the 5 second counter has ended
                    layout.show(panel, changeCard()); //Switch back to the 'start' JPanel
                }
            }else{ //If the game has not ended
                statistics.setText("<html>" + usernameStr + "<br>" + moveStr + "<br>" + timeStr + "<br>" +
                        modeStr + "<br>" + hintStr + "</html>"); //String of the 'statistics' JLabel text
                announcement.setText(""); //Making sure the announcement label doesn't display anything
            }
            if(!paused){ //If the game isn't paused
                panel.repaint(); //Update the grid again and start from the beginning of the 'paint component'
                panel.revalidate();
            }
        }

        public Dimension getPreferredSize() { return new Dimension(frameX, frameY); } //Setting JPanel dimensions

        //Function to update the size of the grid to make sure paint component draws the grid in the center of JPanel
        public void setGridCenter(){
            int []sizes = getGridSize(); //Returns 2 values in int array {frameWidth, frameHeight}
            Insets insets = getInsetSizes(); //Get the sizes of the JFrame borders
            startX = (frameWidth-sizes[0]-insets.left-insets.right)/2; //Setting the width
            startY = (frameHeight-sizes[1]-insets.top-insets.bottom)/2; //Setting the height
        }

        //Returns 2 values in int array {frameWidth, frameHeight}
        private int[] getGridSize(){
            Insets insets = getInsetSizes();
            int frameWidth = (rectangleWidth * rowNum) + ((rowNum-1)*gapInbetweenEachCell) +
                    insets.left + insets.right;
            int frameHeight = (rectangleHeight * columnNum) + ((columnNum-1)*gapInbetweenEachCell) +
                    insets.top + insets.bottom;
            int []values = {frameWidth, frameHeight};
            return values;
        }

        //Function to return the border sizes of the JPanel
        private Insets getInsetSizes(){ return frame.getInsets(); }
    }

    //JPanel object class to display a 'paused' JFrame and count the time the game has been paused
    class pauseButton extends JPanel{
        long startTime;

        //pauseButton JPanel initializer
        pauseButton(long time, JFrame f){ //Requires two arguments, time the 'pause' button was pressed and JFrame
            this.startTime = time; //Setting the pause start time as a object variable
            JButton exit = new JButton("resume"); //Creating new JButton - Will be center as no there is no layout
            //Listener for if the button is pressed
            exit.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    paused = false; //Game is no longer paused
                    long endTime = System.currentTimeMillis(); //Set the stop time
                    //Calculating the total time that the game has been paused for
                    //pausedTime <-- pausedTime + ((endTime - startTime) / 1000)
                    pauseTime = pauseTime + Long.valueOf((endTime - startTime) / 1000).intValue();
                    playerMove = true; //Allow the player to move
                    f.dispose(); //Close the 'paused' window
                    System.out.println(pauseTime);
                    panel.repaint(); //Update and refresh the grid
                    panel.revalidate();
                }
            });
            add(exit); //Adding the exit button to the JPanel content
        }

        public Dimension getPreferredSize() {
            return new Dimension(200,38);
        } //JPanel default size
    }

    //JPanel object class to display a 'help' JFrame
    class helpFrame extends JPanel{

        //Initialization of JPanel objects
        helpFrame(Frame f){ //Requires one argument - JFrame of 'helpFrame'
            setLayout(null); //Setting the objects to be allowed to use .setBounds
            JLabel text = new JLabel();
            //Using HTML text formatting to set the text of the JLabel to be displayed
            text.setText("<html><center>\n" +
                    "<b>Help Information</b><br>\n" +
                    "<b>Selecting a game</b><br>\n" +
                    "You are able to select from four different game types; easy, medium, hard, and custom.<br>\n" +
                    "In order to select custom you must provide the directory of a valid maze in a '.txt' " +
                    "file.<br>\n" +
                    "A maze is valid if, there is one a 'S' (start point), there is one 'F' (finishing point), " +
                    "'W' for walls and '0' for a blank cell.<br>\n" +
                    "Each cell must be split up with a ',' and there must the same number of elements on every " +
                    "row (line of the text file).<br>\n" +
                    "<br>\n" +
                    "<b>Controls</b><br>\n" +
                    "When playing the game, you can:<br>\n" +
                    "<b>W</b> (move forwards)<br>\n" +
                    "<b>A</b> (move left)<br>\n" +
                    "<b>S</b> (move backwards)<br>\n" +
                    "<b>D</b> (move down)<br>\n" +
                    "A 'hint' will move you 5 moves closer to the finishing position, but will also add 10 to " +
                    "your move counter.<br>\n" +
                    "You can 'giveup' at any point in the game, and the shortest path will be shown to you.<br>\n" +
                    "Pause the game by pressing 'ESC', or the 'pause' button.<br>\n" +
                    "</center></html>");
            text.setBounds(10,10,380,350); //Position/Size of label
            add(text); //Adding JLabel to JPanel content

            JButton close = new JButton("close"); //Setting up new JButton
            //Listener for if the button is pressed
            close.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    playerMove = true; //Allow the player to move
                    f.dispose(); //Close the 'help' JFrame
                }
            });
            close.setBounds((400/2)-30, 380, 60,30); //Position/Size of button
            add(close); //Adding JButton to JPanel content
        }

        public Dimension getPreferredSize() { return new Dimension(400, 420);} //Set default JPanel Size
    }
}






