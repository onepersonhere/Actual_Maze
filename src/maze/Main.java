package maze;

import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        // Many thanks to Mariano for the idea of using the debugMode flag
        final boolean debugMode = false;
        Maze maze = null;
        String fileName;
        Scanner scanner = new Scanner(System.in);

        while (true) {
            mainMenu(maze != null);
            int action = scanner.nextInt();
            switch (action) {
                case 1:
                    System.out.println("Enter the size of a new maze");
                    int mazeSize = scanner.nextInt();
                    maze = new Maze(mazeSize, debugMode);
                    maze.generate();
                    maze.calculateMaze();
                    maze.displayMaze();
                    break;
                case 2:
                    scanner.nextLine();
                    System.out.println("Enter the file name:");
                    fileName = scanner.nextLine();
                    try {
                        maze = (Maze) Maze.loadMaze(fileName);
                        System.out.println("Load was successful!");
                    } catch (Exception e) {
                        System.out.println("Cannot load the maze. It has an invalid format");
                        //e.printStackTrace();
                    }
                    break;
                case 3:
                    if (maze == null) {
                        System.out.println("Incorrect option. Please try again");
                    } else {
                        scanner.nextLine();
                        System.out.println("Enter the file name:");
                        fileName = scanner.nextLine();
                        try {
                            Maze.saveMaze(maze, fileName);
                            System.out.println("Save was successful!");
                        } catch (Exception e) {
                            System.out.println("ERROR! Save NOT successful. " + e);
                        }
                    }
                    break;
                case 4:
                    assert maze != null;
                    maze.displayMaze();
                    break;
                case 5:
                    assert maze != null;
                    maze.walkMaze();
                    maze.calculateMaze();
                    maze.displayMaze();
                    break;
                case 0:
                    System.out.println("Bye !");
                    System.exit(0);
                default:
                    System.out.println("Incorrect option. Please try again");
                    break;
            }
        }
    }

    public static void mainMenu(boolean hasMaze) {
        System.out.println("=== Menu ===\n" +
                "1. Generate a new maze\n" +
                "2. Load a maze");
        if (hasMaze) {
            System.out.println("3. Save the maze\n" +
                    "4. Display the maze\n" +
                    "5. Find the escape.");
        }
        System.out.println("0. Exit");
    }
}

class Maze implements Serializable {
    private final int mazeSize;
    private final boolean debugMode;
    int heightEdges;
    private int[][] adjacencyMatrix;
    private int[][] minSpanTree;
    private int[][] maze; // the array of the maze to print
    Set<Integer> resultSet; // The path through the maze will be on these nodes
    boolean passed = false; // Initially, the maze was not passed
    final String WALL = "\u2588\u2588";
    final String EMPTY = "  ";
    final String PASS = "//";

    public Maze(int mazeSize, boolean debugMode) {
        this.mazeSize = mazeSize;
        this.debugMode = debugMode;
    }

    public void generate() {
        heightEdges = (int) Math.ceil(((double) mazeSize - 2) / 2);
        int weightEdges = (int) Math.ceil(((double) mazeSize - 2) / 2);
        if (debugMode) {
            System.out.println("Матрица узлов " + heightEdges + ":" + weightEdges);
        }

        adjacencyMatrix = new int[heightEdges * weightEdges][heightEdges * weightEdges];
        // Fill 0
        for (int i = 0; i < adjacencyMatrix.length; i++) {
            for (int j = 0; j < adjacencyMatrix.length; j++) {
                adjacencyMatrix[i][j] = 0;
            }
        }

        // Calculating nodes
        Random random = new Random();
        int node = 0;
        int edge;
        for (int i = 0; i < adjacencyMatrix.length - 1; i++) {
            // The adjacent right node
            edge = random.nextInt(mazeSize * mazeSize) + 1;
            // Let's check if we are not going out of the array
            if (node + 1 < ((node / weightEdges) + 1) * weightEdges) {
                adjacencyMatrix[node][node + 1] = edge;
                adjacencyMatrix[node + 1][node] = edge;
            }
            // The adjacent lower node
            edge = random.nextInt(mazeSize * mazeSize) + 1;
            // Let's check if we are not going out of the array
            if (node + weightEdges < adjacencyMatrix.length) {
                adjacencyMatrix[node][node + weightEdges] = edge;
                adjacencyMatrix[node + weightEdges][node] = edge;
            }
            node++;
        }
        // Print matrix in debug mode
        if (debugMode) {
            for (int[] matrix : adjacencyMatrix) {
                for (int j = 0; j < adjacencyMatrix.length; j++) {
                    System.out.print(matrix[j] + ",");
                }
                System.out.println();
            }
        }

        // Minimum spanning tree Pims
        minSpanTree = new int[adjacencyMatrix.length][adjacencyMatrix.length];
        Set<Integer> addedNodes = new HashSet<>();
        addedNodes.add(0);
        int way = 0;
        int nextNode = 0;
        int currentNode = 0;

        while (addedNodes.size() < adjacencyMatrix.length) {
            int minValues = adjacencyMatrix.length * adjacencyMatrix.length;


            for (int eachNode : addedNodes) {
                for (int j = 0; j < adjacencyMatrix.length; j++) {
                    // Check each node in the set for the value that it is not zero and that it is not in the set
                    if (adjacencyMatrix[eachNode][j] < minValues && adjacencyMatrix[eachNode][j] > 0) {
                        if (!addedNodes.contains(j)) {
                            minValues = adjacencyMatrix[eachNode][j];
                            currentNode = eachNode;
                            nextNode = j;
                        }
                    }
                }
            }

            // Calculating minimal tree
            way += adjacencyMatrix[currentNode][nextNode];
            // Print nodes in debug mode
            if (debugMode) {
                System.out.println(currentNode + ":" + nextNode + " " + adjacencyMatrix[currentNode][nextNode]);
            }
            // Adding the found node to the set and to the minimal tree matrix
            addedNodes.add(nextNode);
            minSpanTree[currentNode][nextNode] = 1;
            minSpanTree[nextNode][currentNode] = 1;

        }

        // Print minimum tree in debug mode
        if (debugMode) {
            System.out.println("Minimum way:" + way);
            for (int[] ints : minSpanTree) {
                for (int j = 0; j < minSpanTree.length; j++) {
                    System.out.print(ints[j] + ",");
                }
                System.out.println();
            }
        }
    }

    // Prepare maze
    // Lets fill it walls (1 = wall)
    public void calculateMaze() {
        maze = new int[mazeSize][mazeSize];
        for (int i = 0; i < mazeSize; i++) {
            for (int j = 0; j < mazeSize; j++) {
                maze[i][j] = 1;
            }
        }
        // The entrance to the maze is always where be node #0
        maze[1][0] = 2;

        int currentEdge = 0;
        int mazeRow = 1;
        int mazeCol = 1;
        while (currentEdge < heightEdges * heightEdges) {
            if (currentEdge + 1 < (currentEdge / heightEdges + 1) * heightEdges && minSpanTree[currentEdge][currentEdge + 1] == 1) {
                if (passed && resultSet.contains(currentEdge)) {
                    maze[mazeRow][mazeCol] = 2;
                } else {
                    maze[mazeRow][mazeCol] = 0;
                }
                maze[mazeRow][mazeCol + 1] = 0;
                maze[mazeRow][mazeCol + 2] = 0;
            }
            if (currentEdge + heightEdges < heightEdges * heightEdges) {
                if (minSpanTree[currentEdge][currentEdge + heightEdges] == 1) {
                    if (passed && resultSet.contains(currentEdge)) {
                        maze[mazeRow][mazeCol] = 2;
                    } else {
                        maze[mazeRow][mazeCol] = 0;
                    }
                    maze[mazeRow + 1][mazeCol] = 0;
                    maze[mazeRow + 2][mazeCol] = 0;

                }
            }
            if (currentEdge + 1 <= (currentEdge / heightEdges + 1) * heightEdges - 1) {
                mazeCol += 2;
            } else {
                mazeRow += 2;
                mazeCol = 1;
            }

            currentEdge++;
            // if the node was the last then the exit next to it
            if (currentEdge == heightEdges * heightEdges) {
                maze[mazeRow - 2][mazeSize - 1] = 2;
            }
        }

        // Add way from edge to edge
        for (int i = 0; i < mazeSize; i++) {
            for (int j = 0; j < mazeSize; j++) {
                // ->
                if (maze[i][j] == 2 && j + 2 < mazeSize) {
                    if (maze[i][j + 1] == 0 && maze[i][j + 2] == 2) {
                        maze[i][j + 1] = 2;
                    }
                }
                // -->
                if (maze[i][j] == 2 && j + 3 < mazeSize) {
                    if (maze[i][j + 1] == 0 && maze[i][j + 2] == 0 && maze[i][j + 3] == 2) {
                        maze[i][j + 2] = 2;
                    }
                }
                // V
                if (maze[i][j] == 2 && i + 2 < mazeSize) {
                    if (maze[i + 1][j] == 0 && maze[i + 2][j] == 2) {
                        maze[i + 1][j] = 2;
                    }
                }
                // ^
                if (maze[i][j] == 2 && j - 2 > 0) {
                    if (maze[i][j - 1] == 0 && maze[i][j - 2] == 2) {
                        maze[i][j - 1] = 2;
                    }
                }
                // ^-->
                if (maze[i][j] == 2 && j + 2 < mazeSize && i - 2 > 0) {
                    if (maze[i][j + 1] == 0 && maze[i][j + 2] == 0 && maze[i - 1][j + 2] == 0 && maze[i - 2][j + 2] == 2) {
                        maze[i][j + 1] = 2;
                        maze[i][j + 2] = 2;
                        maze[i - 1][j + 2] = 2;
                    }
                }
                // ^-->
                if (maze[i][j] == 2 && j + 2 < mazeSize && i - 2 > 0) {
                    if (maze[i][j + 1] == 0 && maze[i][j + 2] == 0 && maze[i - 1][j + 2] == 0 && maze[i - 2][j + 2] == 2) {
                        maze[i][j + 1] = 2;
                        maze[i][j + 2] = 2;
                        maze[i - 1][j + 2] = 2;
                    }
                }
                // ^<-
                if (maze[i][j] == 2 && j - 1 > 0 && i - 2 > 0) {
                    if (maze[i][j - 1] == 0 && maze[i - 1][j - 1] == 0 && maze[i - 2][j - 1] == 2) {
                        maze[i][j - 1] = 2;
                        maze[i - 1][j - 1] = 2;
                    }
                }
            }
        }
    }


    // Print maze
    public void displayMaze() {
        for (int i = 0; i < mazeSize; i++) {
            for (int j = 0; j < mazeSize; j++) {
                if (maze[i][j] == 1) {
                    System.out.print(WALL);
                } else if (maze[i][j] == 2 && passed) {
                    System.out.print(PASS);
                } else {
                    System.out.print(EMPTY);
                }
            }
            System.out.println();
        }
        passed = false;
    }

    // Saving the object using serialization
    public static void saveMaze(Object obj, String fileName) throws IOException {
        FileOutputStream fos = new FileOutputStream(fileName);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(obj);
        oos.close();
    }

    // Loading (deserializing) an object
    public static Object loadMaze(String fileName) throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(fileName);
        BufferedInputStream bis = new BufferedInputStream(fis);
        ObjectInputStream ois = new ObjectInputStream(bis);
        Object obj = ois.readObject();
        ois.close();
        return obj;
    }

    // Pass of the Maze
    public void walkMaze() {
        // Use https://www.cs.usfca.edu/~galles/visualization/Dijkstra.html
        // Undirected Graph & Adjacency Matrix Representation
        char[] known = new char[minSpanTree.length];
        int[] cost = new int[minSpanTree.length];
        int[] path = new int[minSpanTree.length];
        passed = true;
        // Prepare arrays
        Arrays.fill(cost, Integer.MAX_VALUE);
        Arrays.fill(path, Integer.MAX_VALUE);

        int index = 0;
        known[0] = 'T';
        cost[0] = 0;
        path[0] = -1;
        for (int j = 0; j < minSpanTree.length; j++) {
            for (int i = 1; i < minSpanTree.length; i++) {
                if (minSpanTree[index][i] > 0 && known[i] != 'T') {
                    if (cost[i] > cost[index] + minSpanTree[index][i]) {
                        cost[i] = cost[index] + minSpanTree[index][i];
                        path[i] = index;
                    }
                }
            }
            int min = Integer.MAX_VALUE;
            for (int i = 0; i < cost.length; i++) {
                if (known[i] != 'T' && cost[i] < min) {
                    min = cost[i];
                    index = i;
                }
            }
            known[index] = 'T';
        }
        if (debugMode) {
            System.out.println("Vertex  Known  Cost  Path");
            for (int i = 0; i < cost.length; i++) {
                System.out.println("  " + i + "      " + known[i] + "    " + cost[i] + "   " + path[i]);
            }
        }
        index = path.length - 1;
        resultSet = new HashSet<>();
        while (index != 0) {
            resultSet.add(path[index]);
            index = path[index];
        }
        if (debugMode) {
            System.out.println("Путь:" + Arrays.toString(resultSet.toArray()));
            System.out.println("К-во узлов:" + resultSet.size());
        }
    }
}
