import java.util.*;

//The beginning of the A-Star Search Algorithm method that can be called as an object
public class SearchAlgorithm {

    //Initialising variables for the object to run
    public boolean pathPossible; //Is updated to 'true' if a path is possible
    private String [][]grid; //Is the 2Dimensional grid to be searched through
    private int rowCount, columnCount; //These values are the grid dimensions to be used when searching though
    private int startRow, startColumn; //These integer values are the position of the start cell in the grid
    private int finishRow, finishColumn; //These integers are the position of the end cell in the grid
    private int [][]walls; //This is a 2Dimensional array of cells that the player can't go through - ROW-COLUMN

    private final int gCost = 10; //This is the cost of moving to another node

    private static Cell [][] cellGrid = new Cell[5][5]; //Initializing a list of 'Cells' for the 'Cells' objects to use
    private static boolean closedCells[][]; //A list where all nodes in the grid that do not aid in furthering the shortest path go
    private static PriorityQueue<Cell> openCells; //Initialises a 'queue' for all open cells so that items can be 'popped' off the queue when searching through

    //Object initialisation
    SearchAlgorithm(String[][] gridToSearch){ //When calling a new 'SearchAlgorithm' it requires one argument - the grid to be traversed
        //Setup values for the object to use. Resets them if object has been used already.
        this.pathPossible = false; //Resetting the a variable called 'pathPossible' to false, so that it is reset from the last time the algorithm is used
        this.grid = gridToSearch; //Initializing the 'grid' two dimensional array for the rest of the function to use as the 'gridToSearch' argument
        updateGridInformation(); //Calling the 'updateGridInformation' procedure
        search(); //Starting the A-Star algorithm on searching through the grid for the shortest possible path
    }

    //Setting up a class for each 'Cell' object to be updated with when being search through the grid
    static class Cell { //Each cell will have
        int hCost = 0; //A heuristic cost
        int fCost = 0; //Overall path cost ('h' heuristic cost + 'g' move cost)
        int row, column; //The position in the two dimensional array where the node is located
        Cell parent; //The node that comes before the current node in the path
        Cell(int row, int column) {
            this.row = row;
            this.column = column;
        }
    }

    //A function to return the heuristic 'h' cost of a cell
    private int calculateHeuristicCost(int currentRow, int currentColumn){
        //Returning the 'Manhattan' heuristic
        //h = absolute ( current_cell.x - finishing_cell.x ) + absolute ( current_cell.y - finishing_cell.y )
        return (Math.abs(currentRow - finishRow) + Math.abs(currentColumn - finishColumn));
    }

    //Set the 'f' cost of a the current cell
    private void setCellFCost(Cell current, Cell nextMove){
        if(nextMove == null || closedCells[nextMove.row][nextMove.column]){ //If the cell is a wall ('null') or closed, don't update the cost
            return;
        }
        int f = nextMove.hCost+(current.fCost+gCost); //update the finishing path cost
        boolean inOpen = openCells.contains(nextMove);
        if(!inOpen || f < nextMove.fCost){
            nextMove.fCost = f; //update the next moves f cost
            nextMove.parent = current; //set the next moves parent node as the current node
            if(!inOpen){
                openCells.add(nextMove); //Add the next node to the openCells queue to be searched through next
            }
        }
    }

    private void updateGridInformation(){
        this.rowCount = grid.length; //Set the number of rows to search through
        this.columnCount = grid[0].length; //Set the number of nodes on each line
        int wallCounter = 0; //Initialize a counter for the number of walls in the grid
        for(int rowCounter = 0; rowCounter < rowCount; rowCounter++){
            for(int columnCounter = 0; columnCounter < columnCount; columnCounter++){
                //For each cell of the grid
                if("S".equals(grid[rowCounter][columnCounter])){ //Check for the starting point
                    startRow = rowCounter;
                    startColumn = columnCounter;
                }
                if("F".equals(grid[rowCounter][columnCounter])){ //Check for the finishing point
                    finishRow = rowCounter;
                    finishColumn = columnCounter;
                }
                if("X".equals(grid[rowCounter][columnCounter])){ //Set the number of walls
                    wallCounter++;
                }
            }
        }
        this.walls = new int[wallCounter][2]; //Setting a list for correct number of walls in the grid
        int counter = 0;
        for(int rowCounter = 0; rowCounter < rowCount; rowCounter++) {
            for (int columnCounter = 0; columnCounter < columnCount; columnCounter++) {
                if("X".equals(grid[rowCounter][columnCounter])){
                    this.walls[counter][0] = rowCounter;
                    this.walls[counter][1] = columnCounter;
                    counter++; //Setting the row and column values to a list called 'walls'
                }
            }
        }
    }

    //Public function to get the number of moves of the shortest path in the grid
    public int getPathLength(){
        Cell current = cellGrid[finishRow][finishColumn];
        int length = 1;
        while (current.parent != null) { //Iterating through all cells from the end node, changing parent each time until the start point is reached
            current = current.parent;
            length++;
        }
        return length; //Returns the integer value
    }


    public int[][] getPath(){
        int [][]pathFromEnd = new int[getPathLength()][2]; //Initializing a new integer two dimensional array to the length of the shortest path
        Cell current = cellGrid[finishRow][finishColumn];
        int counter = 0;
        while (current.parent != null) { //While there are still elements in the list
            pathFromEnd[counter][1] = current.row; //Adding the row of the current cell in the paths order
            pathFromEnd[counter][0] = current.column; //Adding the column of the current cell in the paths order
            counter++;
            current = current.parent; //Moving the current node to that of the parent of the current node to be searched through next loop
        }
        return pathFromEnd; //Returns an integer two dimensional array
    }

    private void search() { //This is where the A-Star algorithm starts to search through the grid
        long start = System.currentTimeMillis();
        cellGrid = new Cell[rowCount][columnCount]; //Initialising the 'cellGrid' array to be one that holds 'Cell' objects
        closedCells = new boolean[rowCount][columnCount]; //Setting a 'closedCells' array to hold if a cell has been 'closed' or not
        openCells = new PriorityQueue<>((Object object1, Object object2) -> { //Java method for creating a new 'queue', adding in objects to start
            Cell cell1 = (Cell) object1;
            Cell cell2 = (Cell) object2;
            return cell1.fCost < cell2.fCost ? -1 :
                    cell1.fCost > cell2.fCost ? 1 : 0;
        });
        for (int rowCounter = 0; rowCounter < rowCount; rowCounter++) {
            for (int columnCounter = 0; columnCounter < columnCount; columnCounter++) {
                cellGrid[rowCounter][columnCounter] = new Cell(rowCounter, columnCounter);
                cellGrid[rowCounter][columnCounter].hCost = calculateHeuristicCost(rowCounter, columnCounter);
            }
        }
        cellGrid[startRow][startColumn].fCost = 0; //Sets the cost of the starting point as 0 so to start the search from there
        for (int i = 0; i < walls.length; ++i) {
            cellGrid[walls[i][0]][walls[i][1]] = null; //Setting the position of all walls to a null state so the algorithm doesn't try go through them
        }
        openCells.add(cellGrid[startRow][startColumn]); //Adding starting point to the priority queue to start search at that cell
        boolean valid = false; //Setting up an infinite loop which will only 'break' when a condition sets this variable as 'true'
        while (!valid) { //Starting while loop, while 'valid' is false
            while (true) { //Starting a while loop which will break after the cell has been looked at.
                Cell current = openCells.poll();
                if (current == null){ //If the next cell is 'null' / where there are no more elements left in the array (no path scenario)
                    valid = true;
                    break; //Break out of all loops
                }
                closedCells[current.row][current.column] = true;
                if (current.equals(cellGrid[finishRow][finishColumn])) { //If the current cells row and column equals the finishing cell
                    valid = true;
                    break; //Break out of both while loops / Path has been found
                }
                Cell potentialNextMove; //
                //Setting the 'F' cost for each of the next possible moves
                if (current.row - 1 >= 0) { //Possible move 'up' in the grid
                    potentialNextMove = cellGrid[current.row - 1][current.column];
                    setCellFCost(current, potentialNextMove);
                }
                if (current.column - 1 >= 0) { //Possible move 'left' in the grid
                    potentialNextMove = cellGrid[current.row][current.column - 1];
                    setCellFCost(current, potentialNextMove);
                }
                if (current.column + 1 < grid[0].length) { //Possible move 'right' in the grid
                    potentialNextMove = cellGrid[current.row][current.column + 1];
                    setCellFCost(current, potentialNextMove);
                }
                if (current.row + 1 < grid.length) { //Possible move 'down' in the grid
                    potentialNextMove = cellGrid[current.row + 1][current.column];
                    setCellFCost(current, potentialNextMove);
                }
            }
        }
        if (closedCells[finishRow][finishColumn]) { //If the value of the finishing cell in 'closedCells' is 'True'
            //System.out.println("Time to search: " + (System.currentTimeMillis()-start) + " (ms)");
            this.pathPossible = true; //Then pathPossible is set to true
        } //Else it will not do anything
    }
}
