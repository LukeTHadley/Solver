import java.io.BufferedReader;
import java.io.FileReader;

//The beginning of the RetrieveFile class
//This class allow the program to import a '.txt' file as a two dimensional array that can be used to in the game
public class RetrieveFile {
    //Initialising variables to be used in the class
    public String textFileLocation;
    int lineCount, largestNumberOfElements;
    public int playerCol, playerRow;

    //Object initialisation
    RetrieveFile(String filePath){ //When calling 'RetrieveFile' the object requires one argument, the file path of which to get the file from
        this.textFileLocation = filePath; //Setting the file location as the filepath given
        int[] values = getLinesNodesAmount(); //Calling the method to get the largest number of rows and columns in the grid
        this.lineCount = values[0]; //Setting the returned values to variables
        this.largestNumberOfElements = values[1];
    }

    //Method to read the grid in from file
    public String[][] getFile(){
        String [][]array = new String[lineCount][largestNumberOfElements]; //Initializing array to the correct size
        try{
            BufferedReader reader = new BufferedReader(new FileReader(textFileLocation)); //Open the text file
            String line; //Initializing 'line' as a string variable to be used for each line
            int counter = 0;
            while ((line = reader.readLine()) != null) { //Reads the next line in the grid while the text file still has more lines
                String []list = new String[largestNumberOfElements]; //Initializing a string array to put all elements of the read file into after being split
                int counter2 = 0;
                for (String token : line.split(",")) { //Splitting up the string bit by bit where there is a ','
                    list[counter2] = token; //Adding that split 'token' to the 'list' array
                    counter2 = counter2 + 1; }
                array[counter] = list; //Simply assigning all values of the line from the file to the new two dimensional array at the correct row number
                counter = counter + 1; } //Loops round until this has been done for all lines in the .txt file
            reader.close(); //Closing the file from the 'buffer' after all lines have been read
        } catch (Exception e){ //Exception handling for if there is an error while reading the .txt file
            System.out.println(e); }
        return array; //Returning the 'array' two dimensional string list which has be taken from the .txt file
    }

    /*
    Gets the number of lines in the .txt file.
    Then goes through each line in the .txt file getting the number of separate 'nodes' on the lines.
        - A node is a character split up by ','.
    This allows the 'getFile' method to know how many items there are going to be when writing the nodes
        to a 2 dimensional array.
    The number of lines in the .txt file are counted, and then the number of elements on each line
     */
    public int[] getLinesNodesAmount(){
        //Get number of lines
        int lineCount = 0;
        try{
            BufferedReader reader = new BufferedReader(new FileReader(textFileLocation)); //Open the text file
            while (reader.readLine() != null) { //Iterate through each line of the text file until there are no more
                lineCount++; //Add one to a counter every time there is a line
            }
            reader.close(); //Close the file
        } catch (Exception e){ //Exception handling for if there is an error when reading from the file
            System.out.println(e);
        }
        int []nodeCount = new int[lineCount];
        try{
            BufferedReader reader = new BufferedReader(new FileReader(textFileLocation)); //Open the text file
            String line;
            int counter = 0;
            while ((line = reader.readLine()) != null) { //Get number of nodes in each line and append the number to a list
                int elementCount = 0;
                for (String token : line.split(",")) { //Text file uses a ',' to spilt up each node, remove and split up the text at that point
                    elementCount++; //Add one to the element counter
                }
                nodeCount[counter] = elementCount;
                counter = counter + 1;
            }
        } catch (Exception e){
            System.out.println(e);
        }
        //Gets the largest number of elements on a row
        int largestElement = 0;
        for(int i = 0; i < lineCount; i++) {
            if(nodeCount[i] > largestElement){
                largestElement = nodeCount[i];
            }
        }
        int []values = {lineCount, largestElement};
        return values;
    }

    /*
    Check that the syntax of the .txt file that was converted into 'String[][]' is valid.
    Correct Syntax:
        - Starting point denoted by 'S' and only one is allowed, one must exist.
        - Ending point is denoted by 'F' and only one is allowed, one must exist.
        - Blocked cells are denoted by 'X' and empty/free cells are denoted by '0'.
        - All letters must be capitals.
        - A cell may only contain one of these four different cell denotations.
     If Syntax is correct, then the method will return 'true'; if not 'false'.
     */
    public boolean checkValidArray(String[][] array){
        //Setting all counters to 0
        int sCount = 0;
        int fCount = 0;
        int xCount = 0;
        int zeroCount = 0;
        int elseCount = 0;
        boolean returnValue = false;
        for(int c = 0; c < lineCount; c++) {
            for(int r = 0; r < largestNumberOfElements; r++) {
                String node = array[c][r];
                if("S".equals(node)){ //Finding the number of 'starting' points
                    playerRow = r;
                    playerCol = c;
                    sCount = sCount + 1;
                }
                else if("F".equals(node)){ //Finding the number of 'finishing' points
                    fCount = fCount + 1;
                }
                else if("X".equals(node)){ //Finding the number of 'walls'
                    xCount = xCount + 1;
                }
                else if("0".equals(node)){ //Finding the number of 'path' points
                    zeroCount = zeroCount + 1;
                }
                else{ //If any other objects apart frm the above are found in the grid add that to a counter
                    elseCount = elseCount + 1;
                }
            }
        }
        if(sCount == 1 && fCount == 1 && elseCount == 0) { //Check that there is only one starting point, one finishing point and no anomalies
            returnValue = true; //Return true if true
        }
        return returnValue; //Return the default 'false'
    }
}