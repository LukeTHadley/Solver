import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.File;
import java.lang.String;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/*
CLASS databaseConnection

Implements 'SQLite' database connection through external library jar.
Jar file needs to be installed in order to run
When opening a database connection the string "jdbc:sqlite:" needs to be added to the file path
    string so that the plugin knows to use the library jar.
*/
public class databaseConnection {

    public static String filePath = "SOLVER.db"; //File location/name of the database file
    public static String dbLocation = "jdbc:sqlite:" + filePath; //Setting driver string
    //Setting the the SQL statements to be executed if a new database file needs to be generated
    public static String []createTableStmts =
            //Creating 'users' table
            {"CREATE TABLE \"users\" (\n" +
                    "\t\"id\"\tINTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE,\n" +
                    "\t\"username\"\tTEXT NOT NULL,\n" +
                    "\t\"password\"\tTEXT NOT NULL\n" +
                    ")",
            "CREATE TABLE \"levels\" (\n" +
                    "\t\"level_number\"\tINTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE,\n" +
                    "\t\"level_type\"\tTEXT NOT NULL\n" +
                    ")",
            "CREATE TABLE \"scores\" (\n" +
                    "\t\"id\"\tINTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,\n" +
                    "\t\"user_id\"\tINTEGER NOT NULL,\n" +
                    "\t\"levels_number\"\tINTEGER NOT NULL,\n" +
                    "\t\"score\"\tINTEGER NOT NULL,\n" +
                    "\t\"time_taken\"\tINTEGER NOT NULL,\n" +
                    "\t\"num_of_moves\"\tINTEGER NOT NULL,\n" +
                    "\t\"hints_used\"\tINTEGER NOT NULL,\n" +
                    "\t\"date_played\"\tTEXT NOT NULL,\n" +
                    "\tFOREIGN KEY(\"levels_number\") REFERENCES \"levels\"(\"level_number\"),\n" +
                    "\tFOREIGN KEY(\"user_id\") REFERENCES \"users\"(\"id\")\n" +
                    ")",
            //Statements to add all level types to the 'levels' table
            "INSERT INTO levels (level_type)\n" +
                    "VALUES ('Easy');", //Easy mode
            "INSERT INTO levels (level_type)\n" +
                    "VALUES ('Medium');", //Medium mode
            "INSERT INTO levels (level_type)\n" +
                    "VALUES ('Hard');", //Hard mode
            "INSERT INTO levels (level_type)\n" +
                    "VALUES ('Custom File');"}; //Custom mode

    //Function to open and start a connection with the database
    private static Connection connect(){
        Connection conn = null;
        try { //Attempt
            conn = DriverManager.getConnection(dbLocation); //Database connecting through SQLite library
        } catch (SQLException e) { //Catch error if there was a problem connecting to the database
            System.out.println("An Error occurred while connecting to the database file\n\t" + e.getMessage());
        }
        return conn; } //Return the database connection so that the statements can be executed

    //Function to execute SQL statements. Path of file and the SQL statement as strings are passed as arguments
    public static boolean executeStatement(String sql){
        System.out.println("Starting execution of SQL statement");
        System.out.println("----\n" +sql + "\n----");
        try (Connection conn = connect(); //Create new connection
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql); //Execute SQL statement passed through the SQLite library
            System.out.println("Execution of SQL statement successful");
            return true; //Statement successfully executed
        } catch (SQLException e) { //Catch error if there was a problem when executing SQL statement
            System.out.println("An Error occurred while executing the SQL statement\n\t" + e.getMessage());
            return false; //Statement not executed successfully
        }
    }

    //Function to create a new database file using SQLite driver with error catching to output all progress
    private static void createNewDatabase() {
        try (Connection conn = connect()) { //New connection (if a file does not exist) will create a new .db file
            if (conn != null) { System.out.println("A new database file has been created!\nFilepath: " + filePath); }
        } catch (SQLException e) { //As long as Library/Driver/Filepath correct - should not error
            System.out.println("An error occurred while creating a database file!\n" + e.getMessage());
        }
    }

    //Function to check if a file exists in the current directory
    private static boolean directoryCheck(String fileName){ //Requires the fileName as an argument
        System.out.println("Starting directory check for '" + fileName + "'");
        File directory = new File(fileName); //Using 'File' function - setting up object
        boolean exists = directory.exists(); //Does the file exist in the directory
        if (exists) { System.out.println("Database Found!\nFile '" + fileName + " Exists"); }
        else { System.out.println("File '" + fileName + "' doesn't exist!"); }
        return exists; //Returns boolean value of 'exists'
    }

    //Function to query with the database
    public static List quearyTable(String sql, List strNeeded){
        List returnValues = new ArrayList();
        try (Connection conn = connect(); //New database connection using SQLite library
             Statement stmt  = conn.createStatement(); //Execute the query statement
             ResultSet rs    = stmt.executeQuery(sql)){ //Return values that the statement gets
            for (int i = 0; i < (strNeeded.size()); i++) { //For the number of elements needed
                //Append them to an array to be returned
                returnValues.add((rs.getString(String.valueOf(strNeeded.get(i)))));
            }
        } catch (SQLException e) { //Catch error if there was a problem while querying the database
            System.out.println("An error occurred while executing queary statement!\n" + e.getMessage());
        }
        return returnValues; //Return the values gathered from the execution statement
    }

    //Function to get the top 10 high scores in the database
    public static List getHighScores(){
        List returnValues = new ArrayList(); //Setup a new array to add the query values into
        //Setting up string with the SQL statement to select the top 10 scores
        String sql = "SELECT users.username, scores.score\n" +
                "FROM scores\n" +
                "LEFT JOIN\n" +
                "users\n" +
                "on scores.user_id = users.id\n" +
                "ORDER BY score\n" +
                "LIMIT 10;";
        try (Connection conn = connect(); //create a new connection using SQLite library
             Statement stmt  = conn.createStatement(); //Execute the SQL statement
             ResultSet rs    = stmt.executeQuery(sql)){ //Return the results the SQL statement gets
            //Loop through the results set
            while (rs.next()) {
                returnValues.add(rs.getString("username")); //Adding the username of the current set to the return list
                returnValues.add(rs.getString("score")); //Adding the score of the current set to the return list
            }
        } catch (SQLException e) { //Cath error if there was a problem while querying the database
            System.out.println(e.getMessage());
        }
        //Adding blank values to the return array so that when displaying them as a table there is no EOL error
        while(returnValues.size() < 20){
            returnValues.add(" ");
        }
        return returnValues; //Returning results
    }

    //Procedure to check if the database exists in the same directory of the game or not
    public static void startup() {
        //If the file does not exist in the directory then a new .db file is created with the tables
        if (!directoryCheck(filePath)) {
            System.out.println("Starting creation of database file...");
            createNewDatabase(); //Create new database
            System.out.println("Adding Tables...");
            //Run through all statements in 'createTableStmts' so all are run in order one by one
            for (int counter = 0; counter < createTableStmts.length; counter++) {
                executeStatement(createTableStmts[counter]); //Execute current statement
            }
        }
    }
}