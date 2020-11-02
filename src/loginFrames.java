import java.awt.*;
import javax.swing.*;
import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

//The beginning of the 'loginFrames' class where the GUIs for both the login screen and the registration screen
public class loginFrames extends JPanel{
    public static int userID;
    static String loginText;

    //Check that the login credentials that the user inputted in the frame are the same as in the database
    public static boolean attemptSignin(JFrame frame, JLabel loginLable, String username, String password){
        String loginMessage = "";
        try{
            //Check database for the username and password entered
            String quearyStatment = "SELECT users.username, users.password, users.id\n" +
                    "FROM users\n" +
                    "WHERE (((users.username)='" + username + "') AND ((users.password)='" + password + "'));\n";
            List strNeeded = new ArrayList(); //Setting a list of the data that needs to be returned
            strNeeded.add("Username");
            strNeeded.add("Password");
            strNeeded.add("id");
            //Querying the database and setting the return values to variables
            List databaseQueary = new ArrayList(databaseConnection.quearyTable(quearyStatment, strNeeded));
            String databaseUsername = String.valueOf(databaseQueary.get(0));
            String databasePassword = String.valueOf(databaseQueary.get(1));
            String databaseID = String.valueOf(databaseQueary.get(2));
            updateID(databaseID);
            //If the username and password that the user entered is the same as in the database
            if(databaseUsername.equals(username) && databasePassword.equals(password)){
                loginMessage = "login successful"; //Login is successful
                loginLable.setText(loginMessage);
                return true; //Return true
            }
        }
        catch (Exception e){ //Exception handling for if the database returns a value that errors
            loginMessage = "Username or Password incorrect"; //Setting the text of a label to show to the user
            loginLable.setText(loginMessage);
        }
        frame.add(loginLable);
        return false; //Return false as the credentials inputted do not match the database
    }

    //Update the user id value
    public static void updateID(String idFromDB){
        userID = Integer.valueOf(idFromDB);
    }

    //Check new registration values are valid
    public static boolean checkRegistration(JFrame frame, JTextField usrNameInput, JLabel passwordWarningLable,
                                            JLabel usernameWarningLable, JPasswordField usrPasswordInput,
                                            JPasswordField usrPasswordInput2){
        boolean passwordSuccess = false;
        boolean usernameSuccess = false;
        //Check that the username does not exist in the database already
        try{
            //Check database for the username entered
            String username = usrNameInput.getText();
            String quearyStatment = "SELECT Users.Username\n" +
                    "FROM Users\n" +
                    "WHERE (((Users.Username)='" + username + "'));\n";
            List strNeeded = new ArrayList();
            strNeeded.add("Username");
            //Querying the database and setting the return values to variables
            List databaseQueary = new ArrayList(databaseConnection.quearyTable(quearyStatment, strNeeded));
            String databaseUsername = String.valueOf(databaseQueary.get(0));
            if(databaseUsername.equals(username)){ //If username already exists in the database
                //Update a label for the user to see whats wrong with their credentials
                usernameWarningLable.setText("Username already exists");
                frame.add(usernameWarningLable);
                usernameSuccess = false; //Set a value of false as the username is invalid
            }
        }
        catch (Exception quearyException){ //Exception handling if the database returns a value that errors
            //Database errors as the SQL statement is attempting to look for a record that does not exist
            //The record should not exist as a username that does not exist is needed
            usernameSuccess = true; //Set a value of true as the username does not exist
            usernameWarningLable.setText("");
            frame.add(usernameWarningLable);
        }
        //Check that both password fields are the same
        if(String.valueOf(usrPasswordInput.getPassword()).equals(String.valueOf(usrPasswordInput2.getPassword()))){
            //If both values are the same, passwords were inputted correctly
            passwordWarningLable.setText("");
            frame.add(passwordWarningLable);
            passwordSuccess = true;
        }
        else{
            //When passwords for both fields were not inputted as the same
            passwordWarningLable.setText("Passwords do not match");
            frame.add(passwordWarningLable);
            passwordSuccess = false;
        }
        //Returning values
        if(passwordSuccess == true && usernameSuccess == true){
            return true; //Return true if the username does not exist in the database and the passwords are valid
        }
        else{
            return false; //Return false if not true
        }
    }

    //Inserting new user into the database
    public static boolean registerNewUser(JTextField username, JPasswordField password){
        String usernameStr = username.getText(); //Getting the new username
        String passwordStr = String.valueOf(password.getPassword()); //Getting the password
        //Setting the SQL statement for inserting the data into the database
        String sqlStatement = "INSERT INTO users (Username, Password)\n" +
                "VALUES ('" + usernameStr + "', '" + passwordStr + "');";
        if(databaseConnection.executeStatement(sqlStatement) == true){ //Running statement
            System.out.println("Added new user!");
            return true; //If databaseConnection returns true - successful inserted into the database, return true
        }
        return false; //Return false if there was a problem inserting into the database (shouldn't happen)
    }

    public static void loginScreen(){
        int frameX = 1000;
        int frameY = 800;
        //Main Menu
        ///Setup of frame/buttons/fields/labels etc...
        JFrame frame = new JFrame(); //Setting up JFrame
        JButton loginButton = new JButton("Login");
        JButton quitButton = new JButton("Quit");
        JButton registerButton = new JButton("Register a new account");
        JTextField usrNameInput = new JTextField(40);
        JPasswordField passwordField = new JPasswordField();
        JLabel welcomeMessage = new JLabel("Solver", SwingConstants.CENTER);
        JLabel loginTitle = new JLabel("login", SwingConstants.CENTER);
        JLabel usernameTitle = new JLabel("Username:");
        JLabel passwordTitle = new JLabel("Password:");
        JLabel loginLabel = new JLabel(loginText, JLabel.CENTER);

        usrNameInput.setBounds((frameX/2)-(250/2),frameY/2,250,50); //Position/Size of text box
        usrNameInput.setFont(new Font("Arial", Font.PLAIN, 25)); //Font formatting of text box
        frame.add(usrNameInput); //Adding text box to the JFrame
        passwordField.setBounds((frameX/2)-(250/2),460,250,50); //Position/Size of text box
        frame.add(passwordField); //Adding field to the JFrame
        loginTitle.setBounds((frameX/2)-(250/2),frameY/2-60 ,250,50); //Position/Size of text box
        loginTitle.setFont(new Font("Arial", Font.PLAIN, 25)); //Font formatting of text
        frame.add(loginTitle); //Adding label to the JFrame
        loginLabel.setBounds((frameX/2)-(250/2),500,250,50); //Position/Size of text
        loginLabel.setFont(new Font("Arial", Font.PLAIN, 15)); //Font formatting of text
        frame.add(loginLabel); //Adding label to the JFrame
        loginButton.setBounds((frameX/2)-(250/2),540,250,30); //Position/Size of button
        frame.add(loginButton); //Adding button to the JFrame
        //Listener for if the button is pressed
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String username = usrNameInput.getText(); //Get the text the user inputted in the text field
                //Get the text the user inputted in the password field
                String password = String.valueOf(passwordField.getPassword());
                //Run the 'attemptSignin' function
                if(attemptSignin(frame, loginLabel, username, password) == true){ //If function returns true
                    //Sign in was successful
                    GameFrame.startGame(userID); //Start the actual game by calling 'GameFrame.startGame'
                    //Passing the id of the row the users credentials are at in the 'users' table in the database
                    System.out.println("Logging in as user with ID '" + userID +"'.");
                    frame.dispose(); } //Close the login window
            }
        });
        registerButton.setBounds((frameX/2)-(250/2),580,250,30); //Position/Size of button
        frame.add(registerButton); //Adding button to the JFrame
        //Listener for if the button is pressed
        registerButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                registerScreen(); //Start the 'register screen'
                frame.dispose(); //CLose the login window
                //tempRegisterFrame.main();
            }
        });
        welcomeMessage.setBounds(0,0,1000,frameY/2); //Position/Size of label
        welcomeMessage.setFont(new Font("Arial", Font.PLAIN, 80)); //Font formatting of text
        frame.add(welcomeMessage); //Adding label to JFrame
        usernameTitle.setFont(new Font("Arial", Font.PLAIN, 20)); //Font formatting of text
        usernameTitle.setBounds((frameX/2)-(450/2),frameY/2,250,50); //Position/Size of label
        frame.add(usernameTitle); //Adding label to JFrame
        passwordTitle.setFont(new Font("Arial", Font.PLAIN, 20)); //Font formatting of text
        passwordTitle.setBounds((frameX/2)-(450/2),460,250,50); //Position/Size of label
        frame.add(passwordTitle); //Adding label to JFrame
        quitButton.setBounds(895,712,100, 60); //Position/Size of label
        frame.add(quitButton); //Adding the button to the JFrame
        //Listener for if the button is pressed
        quitButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                System.exit(0);
            }  }); //Quit the program
        frame.setSize(frameX, frameY); //Setting the size of the window (JFrame)
        frame.setLocationRelativeTo(null); //Setting the window to be positioned in the middle of the screen
        //Setting the program to quit if the window is closed
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLayout(null); //Set no layout mode so that positions of objects in window / JFrame can be positioned
        frame.setVisible(true); //Setting the window / JFrame to be visible
        frame.setResizable(false); //Setting the window / JFrame so that it can't be resized
        frame.setTitle("Solver - Login"); //Setting the title of the window / JFrame
    }

    public static void registerScreen(){
        int frameX = 1000;
        int frameY = 800;
        //Main Menu
        ///Setup of frame/buttons/fields/labels etc...
        JFrame frame = new JFrame();
        JLabel welcomeMessage = new JLabel("Solver", SwingConstants.CENTER);
        JTextField usrNameInput = new JTextField();
        JPasswordField usrPasswordInput = new JPasswordField();
        JPasswordField usrPasswordInput2 = new JPasswordField();
        JButton registerButton = new JButton("Register Account");
        JButton loginButton = new JButton("Already have an account? Login");
        JLabel usernameTitle = new JLabel("Username:");
        JButton quitButton = new JButton("Quit");
        JLabel password2Title = new JLabel("Confirm Password:");
        JLabel password1Title = new JLabel("Password:");
        JLabel passwordWarningLable = new JLabel("");
        JLabel usernameWarningLable = new JLabel("");

        welcomeMessage.setBounds(0,0,1000,frameY/2); //Position/Size of label
        welcomeMessage.setFont(new Font("Arial", Font.PLAIN, 80)); //Font formatting of text
        frame.add(welcomeMessage); //Adding label to JFrame
        usrNameInput.setBounds((frameX/2)-(250/2),frameY/2,250,50);
        usrNameInput.setFont(new Font("Arial", Font.PLAIN, 20)); //Font formatting of text
        frame.add(usrNameInput); //Adding text field to JFrame
        usrPasswordInput.setBounds((frameX/2)-(250/2),460,250,50); //Position/Size of text field
        frame.add(usrPasswordInput); //Adding text field to JFrame
        usrPasswordInput2.setBounds((frameX/2)-(250/2),520,250,50); //Position/Size of text field
        frame.add(usrPasswordInput2); //Adding text field to JFrame
        usernameTitle.setFont(new Font("Arial", Font.PLAIN, 20)); //Font formatting of text
        usernameTitle.setBounds((frameX/2)-(450/2),frameY/2,250,50); //Position/Size of label
        frame.add(usernameTitle); //Adding label to JFrame
        password1Title.setFont(new Font("Arial", Font.PLAIN, 20)); //Font formatting of text
        password1Title.setBounds((frameX/2)-(450/2),460,250,50); //Position/Size of label
        frame.add(password1Title); //Adding label to JFrame
        password2Title.setFont(new Font("Arial", Font.PLAIN, 20)); //Font formatting of text
        password2Title.setBounds((frameX/2)-303,520,250,50); //Position/Size of label
        frame.add(password2Title); //Adding label to JFrame
        usernameWarningLable.setFont(new Font("Arial", Font.PLAIN, 20)); //Font formatting of text
        usernameWarningLable.setBounds((frameX/2)+(250/2),frameY/2,250,50); //Position/Size of label
        frame.add(usernameWarningLable); //Adding label to JFrame
        passwordWarningLable.setFont(new Font("Arial", Font.PLAIN, 20)); //Font formatting of text
        passwordWarningLable.setBounds((frameX/2)+(250/2),460,300,50); //Position/Size of label
        frame.add(passwordWarningLable); //Adding label to JFrame
        registerButton.setBounds((frameX/2)-(250/2),580,250,30); //Position/Size of button
        frame.add(registerButton); //Adding button to JFrame
        //Listener for if the button is pressed
        registerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //Run function to check if the users new credentials are valid
                boolean check = checkRegistration(frame, usrNameInput, passwordWarningLable, usernameWarningLable,
                        usrPasswordInput, usrPasswordInput2);
                if(check == true){ //If the users credentials were valid
                    boolean Success = registerNewUser(usrNameInput, usrPasswordInput);
                    //Attempt to register the new user
                    if(Success){ //If Successfull
                        loginText = "New User Created!"; //Display text that new user account was added
                        frame.dispose(); //Close this window / JFrame
                        loginScreen(); //Open the login screen
                    }
                }
            }
        });
        loginButton.setBounds((frameX/2)-(250/2),620,250,30); //Position/Size of button
        frame.add(loginButton); //Adding button to JFrame
        //Listener for if the button is pressed
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                frame.dispose(); //Close this window / JFrame
                loginScreen(); //Open the login screen
            }
        });
        quitButton.setBounds(895,712,100, 60); //Position/Size of button
        frame.add(quitButton); //Adding button to JFrame
        //Listener for if the button is pressed
        quitButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                System.exit(0);
            } //Quit the program
        });
        frame.setSize(frameX, frameY); //Setting the size of the window (JFrame)
        frame.setLocationRelativeTo(null); //Setting the window to be positioned in the middle of the screen
        //Setting the program to quit if the window is closed
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLayout(null); //Set no layout mode so that positions of objects in window / JFrame can be positioned
        frame.setVisible(true); //Setting the window / JFrame to be visible
        frame.setResizable(false); //Setting the window / JFrame so that it can't be resized
        frame.setTitle("Solver - Register"); //Setting the title of the window / JFrame
    }
}