package ViewModel;

import Model.IModel;
import Model.MyModel;
import View.Main;
import javafx.scene.control.Alert;
import javafx.scene.input.KeyCode;
import javafx.stage.FileChooser;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by anatolyi on 6/19/2017.
 */
public class MyViewModel extends Observable implements Observer {

    IModel model;
    private int[][] maze = null;
    private int currentRowIndex;
    private int currentColumnIndex;


    public MyViewModel(MyModel viewModel) {
        this.model = viewModel;
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o == model) {
            maze = model.getMazeInstance();
            setCurrentPosition();
            setGoalPosition();
            setChanged();
            notifyObservers((String) arg);
        }
    }

    private void setCurrentPosition() {
        currentRowIndex = model.getCharacterPositionRow();
        currentColumnIndex = model.getCharacterPositionColumn();
    }

    private void setGoalPosition() {
    }

    public void generateMaze(int rowSize, int columnSize) throws Exception {
        if (isLegalSize(rowSize, columnSize))
            model.generateMaze(rowSize, columnSize);
        else
            throw new Exception();
    }

    private boolean isLegalSize(int rowSize, int columnSize) {
        return rowSize >= 10 || columnSize >= 10;
    }


    public int[][] getMaze() {
        return model.getMazeInstance();
    }

    public int getCharacterPositionRow() {
        return model.getCharacterPositionRow();
    }

    public int getCharacterPositionColumn() {
        return model.getCharacterPositionColumn();
    }

    public int getGoalStateRow() {
        return model.getGoalPositionRow();
    }

    public int getGoalStateColumn() {
        return model.getGoalPositionColumn();
    }


    private void raiseAlert(String title, String headerText, String info) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(info);
        alert.showAndWait();
    }

    public void moveCharacter(KeyCode movement) {
        if (isLegalMove(movement))
            model.moveCharacter(getMovement(movement));
        else
            notifyObservers("EnableAll");
    }

    private boolean isLegalMove(KeyCode movement) {
        switch (getMovement(movement)) {
            case "UP":
                return isLegalMove(currentRowIndex - 1, currentColumnIndex);
            case "DOWN":
                return isLegalMove(currentRowIndex + 1, currentColumnIndex);
            case "RIGHT":
                return isLegalMove(currentRowIndex, currentColumnIndex + 1);
            case "LEFT":
                return isLegalMove(currentRowIndex, currentColumnIndex - 1);
            default:
                notifyObservers("EnableAll");
                return false;
        }
    }

    private String getMovement(KeyCode movement) {
        if (movement == KeyCode.UP || movement == KeyCode.NUMPAD8 || movement == KeyCode.W)
            return "UP";
        else if (movement == KeyCode.DOWN || movement == KeyCode.NUMPAD2 || movement == KeyCode.S)
            return "DOWN";
        else if (movement == KeyCode.LEFT || movement == KeyCode.NUMPAD4 || movement == KeyCode.A)
            return "LEFT";
        else if (movement == KeyCode.RIGHT || movement == KeyCode.NUMPAD6 || movement == KeyCode.D)
            return "RIGHT";
        return "STAY";
    }

    private boolean isLegalMove(int row, int column) {
        return isInBorders(row, column) && isACorridor(row, column);
    }

    private boolean isACorridor(int row, int column) {
        return maze[row][column] == 0;
    }

    private boolean isInBorders(int row, int column) {
        return row >= 0 && row < maze.length && column >= 0 && column < maze[0].length;
    }

    public void saveGame() {
        if (!model.isMazeExist()) {
            raiseAlert("Warning !",
                    "",
                    "Maze is not exist,game couldn't be saved.");
            return;
        }
        FileChooser fc = new FileChooser();
        setFileChooser(fc, "Save maze", "resources/MazeGames");
        fc.getExtensionFilters().addAll(new FileChooser.ExtensionFilter(".Maze", "*.Maze"));
        File phil = fc.showSaveDialog(Main.primaryStage);
        if (phil != null)
            model.saveGame(phil.getAbsolutePath());
    }

    public boolean isMazeExist() {
        return model.isMazeExist();
    }

    public void openGame() {
        FileChooser fc = new FileChooser();
        setFileChooser(fc, "Open maze", "resources/MazeGames");
        fc.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("*.Maze", "*.Maze"));
        try {
            File selectedFile = fc.showOpenDialog(null);
            if (selectedFile != null)
                model.openMazeFile(selectedFile.getAbsolutePath());
            else
                notifyObservers("EnableAll");
        } catch (Exception e) {
            notifyObservers("EnableAll");
            raiseAlert("Warning",
                    "",
                    "Only maze file can be opened.");
        }
    }

    private void setFileChooser(FileChooser fc, String title, String initialDirectory) {
        fc.setTitle(title);
        fc.setInitialDirectory(new File(initialDirectory));
    }

    public void solveGame() throws Exception {
        if (!model.isMazeExist()) {
            notifyObservers("EnableAll");
            raiseAlert("Warning",
                    "",
                    "Solution can't be created without a game.");
            throw new Exception();
        }
        else
            model.solveGame();
    }

    public int[][] getGameSolution() {
        return model.getGameSolution();
    }

    public void exitGame() {
        model.exitGame();
    }

    public void deleteGame() {
        model.deleteMaze();
    }

}
