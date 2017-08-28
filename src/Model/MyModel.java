package Model;

import Client.Client;

import IO.MyCompressorOutputStream;
import IO.MyDecompressorInputStream;
import Server.Server;
import algorithms.mazeGenerators.Maze;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import Server.*;
import algorithms.mazeGenerators.Position;
import algorithms.search.AState;
import algorithms.search.Solution;
import javafx.application.Platform;

/**
 * Created by anatolyi on 6/19/2017.
 */
public class MyModel extends Observable implements IModel {

    private Maze maze = null;
    private int[][] mazeSolution;
    private ExecutorService executor;
    private Server mazeGeneratingServer;
    private Server solveSearchProblemServer;

    public MyModel() {
        executor = Executors.newFixedThreadPool(5);
        try {
            mazeGeneratingServer = new Server(5400, 1000, new ServerStrategyGenerateMaze());
            mazeGeneratingServer.start();
            solveSearchProblemServer = new Server(5401, 1000, new ServerStrategySolveSearchProblem());
            solveSearchProblemServer.start();
        } catch (Exception ignored) {
        }
    }

    public void generateMaze(int rowSize, int columnSize) {

        executor.execute(new Thread(() -> {
            try {
                Client client = new Client(InetAddress.getLocalHost(), 5400, (inFromServer, outToServer) -> {
                    try {
                        ObjectOutputStream toServer = new ObjectOutputStream(outToServer);
                        ObjectInputStream fromServer = new ObjectInputStream(inFromServer);
                        toServer.flush();
                        int[] mazeDimensions = new int[]{rowSize, columnSize};
                        toServer.writeObject(mazeDimensions);
                        toServer.flush();
                        byte[] compressedMaze = (byte[]) fromServer.readObject();
                        InputStream is = new MyDecompressorInputStream(new ByteArrayInputStream(compressedMaze));
                        byte[] decompressedMaze = new byte[1000000];
                        is.read(decompressedMaze);
                        maze = new Maze(decompressedMaze);
                    } catch (Exception var10) {
                    }

                });
                client.communicateWithServer();
                setChanged();
                notifyObservers("generateMaze");
            } catch (UnknownHostException var1) {
            }
        }));
    }

    public int[][] getMazeInstance() {
        int[][] newMaze = new int[maze.getRowSize()][maze.getColumnSize()];
        for (int row = 0; row < maze.getRowSize(); row++) {
            for (int column = 0; column < maze.getColumnSize(); column++) {
                setMazeValue(newMaze, row, column);
            }
        }
        return newMaze;
    }

    private void setMazeValue(int[][] newMaze, int row, int column) {
        newMaze[row][column] = maze.getValue(row, column);
    }

    public int[] getStartPoint() {
        return new int[]{getRowSize(), getColumnSize()};
    }

    private int getRowSize() {
        return maze.getStartPosition().getRowIndex();
    }

    private int getColumnSize() {
        return maze.getStartPosition().getColumnIndex();
    }

    public int getCharacterPositionRow() {
        return getPositionRow(maze.getStartPosition());
    }

    private int getPositionRow(Position position) {
        return position.getRowIndex();
    }

    public int getCharacterPositionColumn() {
        return maze.getStartPosition().getColumnIndex();
    }

    public int getGoalPositionRow() {
        return maze.getGoalPosition().getRowIndex();
    }

    public int getGoalPositionColumn() {
        return maze.getGoalPosition().getColumnIndex();
    }

    public void moveCharacter(String movement) {
        switch (movement) {
            case "UP":
                maze.setStartPosition(maze.getStartPosition().getRowIndex() - 1, maze.getStartPosition().getColumnIndex());
                break;
            case "DOWN":
                maze.setStartPosition(maze.getStartPosition().getRowIndex() + 1, maze.getStartPosition().getColumnIndex());
                break;
            case "RIGHT":
                maze.setStartPosition(maze.getStartPosition().getRowIndex(), maze.getStartPosition().getColumnIndex() + 1);
                break;
            case "LEFT":
                maze.setStartPosition(maze.getStartPosition().getRowIndex(), maze.getStartPosition().getColumnIndex() - 1);
                break;
        }
        setChanged();
        notifyObservers("displayGame");
    }

    public void saveGame(String path) {
        MyCompressorOutputStream myCompressorOutputStream;
        notifyObservers("SaveGame");
        try {
            myCompressorOutputStream = new MyCompressorOutputStream(new FileOutputStream(path));
            myCompressorOutputStream.write(maze.toByteArray());
            myCompressorOutputStream.close();
        } catch (IOException e) {
            notifyObservers("EnableAll");
        }
    }

    public boolean isMazeExist() {
        return maze != null;
    }

    public void openMazeFile(String path) {
        try {
            MyDecompressorInputStream in = new MyDecompressorInputStream(new FileInputStream(path));
            byte[] savedMazeBytes = new byte[1000000];
            in.read(savedMazeBytes);
            in.close();
            maze = new Maze(savedMazeBytes);
            setChanged();
            notifyObservers("generateMaze");
        } catch (IOException e) {
            notifyObservers("EnableAll");
        }
    }

    public void solveGame() {
        executor.execute(new Thread(() -> {
            try {
                Client client = new Client(InetAddress.getLocalHost(), 5401, (inFromServer, outToServer) -> {
                    try {
                        ObjectOutputStream toServer = new ObjectOutputStream(outToServer);
                        ObjectInputStream fromServer = new ObjectInputStream(inFromServer);
                        toServer.flush();
                        toServer.writeObject(maze);
                        toServer.flush();
                        Solution mazeSolutionFromServer = (Solution) fromServer.readObject();
                        ArrayList<AState> mazeSolutionSteps = mazeSolutionFromServer.getSolutionPath();
                        mazeSolution = new int[mazeSolutionFromServer.getSize()][2];
                        for (int i = 0; i < mazeSolutionSteps.size(); ++i) {
                            mazeSolution[i][0] = mazeSolutionSteps.get(i).getRow();
                            mazeSolution[i][1] = mazeSolutionSteps.get(i).getColumn();
                        }
                        setChanged();
                        notifyObservers("getSolution");
                    } catch (Exception var10) {
                    }
                });
                client.communicateWithServer();
            } catch (UnknownHostException var1) {
                notifyObservers("EnableAll");
            }
        }));
    }

    public int[][] getGameSolution() {
        return mazeSolution;
    }

    public void exitGame() {
        executor.shutdown();
        mazeGeneratingServer.stop();
        solveSearchProblemServer.stop();
        Platform.exit();
        System.exit(0);
    }

    public void deleteMaze()
    {
        maze = null;
    }
}
