import java.util.Random; //Allows the use of the inbuilt Java 'random' methods to return random integers


//Start of the class 'randomGrid' - this object can generate a random grid at multiple difficultly factors
public class randomGrid {
    //Setting up variables for the object to use
    String type;
    int gridColumnCount, gridRowCount;
    int minPathLength; //The minimum number of moves needed from the 'starting' to 'finishing' point
    float randPercentage; //The percent of which the new blank grid should be covered with 'walls'
    public int startColumn, startRow;

    //Initialization of the object
    randomGrid(String mode, int rowCount, int columnCount) { //Object requires three arguments
        this.type = mode; //The type of grid to be generated - 'easy'/'medium'/'hard'
        //By allowing and using these values the method can easily be 'scaled' to generate a grid of a multitude of sizes
        this.gridColumnCount = columnCount; //Number of columns the grid needs to have
        this.gridRowCount = rowCount; //Number of rows the grid needs to have
        if (type.toLowerCase() == "easy") { //Set values for an 'easy' random grid
            randPercentage = 30;
            this.minPathLength = 15;
        } else if (type.toLowerCase() == "medium") { //Set values for a 'medium' difficulty random grid
            randPercentage = 35;
            this.minPathLength = 25;
        } else if (type.toLowerCase() == "hard") { //Set values for a 'hard difficulty random grid
            randPercentage = 40;
            this.minPathLength = 50;
        } //Values get larger as the difficulty increases
    }

    //Function to return a new random integer
    public static int randomInt(int max){
        Random r = new Random(); //Using the Java 'random' method to call a new random integer
        return r.nextInt(max); //Using 'max' as the largest number that should be generated - used as a cap so that no number over that variable is generated
    }

    //Function to randomly generate a grid
    /*
    Generating a valid grid to the correct syntax for the program to be able to display and go through.
    Correct Syntax:
        - Starting point denoted by 'S' and only one is allowed, one must exist.
        - Ending point is denoted by 'F' and only one is allowed, one must exist.
        - Blocked/'walled' cells are denoted by 'X' and empty/free 'path' cells are denoted by '0'.
        - All letters must be capitals.
        A cell may only contain one of these four different cell denotations.
        Randomly generated 'X' cells can only overwrite '0' cells so that a consistent number of 'walls' are present in the grid
        'S' and 'F' cells must also only be generated on '0' cells
    */
    public String[][] generateGrid() { //Returns a two dimensional string array to be used to display to the player
        //Setting up values before generating grid
        long starTime = System.currentTimeMillis(); //Setting a variable as the current time in milliseconds as the starting time to be used later
        System.out.println("Generating new '" + type + "' grid");
        String [][]grid = new String[gridColumnCount][gridRowCount]; //Initializing a blank grid for the correct row and column length given at initialization
        int totalCellsNum = gridRowCount * gridColumnCount; //Calculate the total number of nodes in the grid
        int walledCellsNeeded = Math.round(totalCellsNum * (randPercentage/100)); //Calculating the number of walls needed from the percentage
        int testCaseNum = 1; //Setting a variable as the first test case number
        while(true){ //Starting generation of grid - infinite while loop will only be broken out of when a fully valid grid is generated.
            //Set the two dimensional string array as '0' - path cells (setting the grid to hold all default cells)
            for (int c = 0; c < gridColumnCount; c++) { //For each column in the new grid
                for (int r = 0; r < gridRowCount; r++) { //For each row in the new grid
                        grid[c][r] = "0"; //Set the node as '0'
                }
            }
            //Setting random cell walls in the new grid
            int counter = 0; //Counter to count the number of walls once they have been successfully added
            while(counter <= walledCellsNeeded){ //While the counter is less than or equil to the number of walls needed for the level's percentage
                int randomRow = randomInt(gridRowCount); //Generate a random number for the 'row' value - with a maximum value of the number of rows in the grid
                int randomColumn = randomInt(gridColumnCount); //Generate a random number for the 'column' value - with a maximum value of the number of columns in the grid
                if(grid[randomColumn][randomRow] == "0"){ //If the cell just randomly found is a '0' / wall cell
                    grid[randomColumn][randomRow] = "X"; //Set the cell as a 'X' / wall
                    counter++; //Add one to the wall counter
                }
            } //While loop will break once there are enough valid walls that have been generated
            //Adding random starting and finishing points
            String []values = {"S", "F"}; //The type of cells that need to be added
            for(int i = 0; i < values.length; i++){ //For each value in the 'values' grid - Assigning positions of 'S' and then 'F'
                while(true){ //Infinate while loop only broken when a the node being assigned has been assigned correctly
                    int row = randomInt(gridRowCount); //Generate a random number for the 'row' value - with a maxiumum value of the number of rows in the grid
                    int column = randomInt(gridColumnCount); //Generate a random number for the 'column' value - with a maximum value of the number of columns in the grid
                    if(grid[column][row] == "0"){ //If the cell just randomly found is a '0' / wall cell
                        grid[column][row] = values[i]; //Set the cell as the current type that is being assigned - 'S' then 'F'
                        break; //Break out of the while loop so that the next position type can be assigned a cell in the grid
                    }
                }
            } //For loop will only end when a value for the 'S' and 'F' point have been successfully found
            //All values for the random grid have been initialized at this point
            //Starting validation of the grid
            Object search = new SearchAlgorithm(grid); //Setting up a new A-Star algorithm object passing the new grid through it for it to search
            boolean gridTraversable = ((SearchAlgorithm) search).pathPossible; //Getting the value from the A-Star algorithm if the grid is traversable or not
            int pathLength = ((SearchAlgorithm) search).getPathLength(); //Getting the int value of the length of the path from the A-Star algorithm
            if(gridTraversable && pathLength > minPathLength){ //If the grid is traversable and the path length is greater than the minimum path length assigned
                //A randomly generated grid has been successfully generated
                long endTime = System.currentTimeMillis(); //Setting another values as the current time now as the grid has been generated
                for(int c = 0; c < gridColumnCount; c++) { //Finding the start node
                    for(int r = 0; r < gridRowCount; r++) {
                        String node = grid[c][r];
                        if("S".equals(node)){
                            startColumn = c;
                            startRow = r;
                        }
                    }
                }
                long miliseconds = endTime - starTime; //Calculating the total time it took in milliseconds to generate the grid
                System.out.println("'" + type + "' grid found in : " + miliseconds + " Miliseconds - Total number of generated random grids: " + testCaseNum);
                break; //Breaking out of the while loop finally when a random grid has been successfully generated
            }else{ //A randomly generated grid has not been successfully generated
                testCaseNum++; //Adding one to the 'testCaseNum' counter - counts how many random grids are generated before finding a successful one
            }
        }
        return grid; //Returning the fully generated random grid
    }
}