//Main class of the game - the point at which the program
//starts running.
public class Solver { //The name of this class will determine the name of the program when running.
    public static void main(String[] args) {
        databaseConnection.startup(); //Starts the 'startup' routine checking for a database exists.
        loginFrames.loginScreen(); //Starts the 'loginFrame' class to start the login GUI panel.
    }
}