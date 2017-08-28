package View;

import Server.ProjectProperties;
import Sound.Sound;
import ViewModel.MyViewModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

public class MyViewController implements IView, Observer, Initializable {

    MyViewModel viewModel;
    boolean isUnderCalculations = false;

    @FXML
    private BorderPane mainBorderPane;
    @FXML
    MazeDisplayer mazeDisplayer;
    @FXML
    MenuItem getSolution;
    @FXML
    MenuItem newGame;
    @FXML
    MenuItem openGame;
    @FXML
    MenuItem saveGame;

    private CommandClass commandClass;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Sound.startPlayingMusic();
        mazeDisplayer.setMyViewController(this);
        mazeDisplayer.addEventFilter(MouseEvent.MOUSE_CLICKED, (e) -> mazeDisplayer.requestFocus());
        commandClass = new CommandClass();
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg == null)
            return;
        isUnderCalculations = false;
        enableAll();
        commandClass.execute((String) arg);
        handleReacheGoal();
    }

    public void handleReacheGoal() {
        if (isCurrentStateAtGoalState()) {
            raiseAlert("Game finished.", "Congratulations ! \n You have reached the goal !", "Cleaning game...");
            Sound.startWinningMusic();
            viewModel.deleteGame();
            mazeDisplayer.clearCanvas();
            enableAll();
        }
    }

    private boolean isCurrentStateAtGoalState() {
        return viewModel.getCharacterPositionRow() == viewModel.getGoalStateRow() &&
                viewModel.getCharacterPositionColumn() == viewModel.getGoalStateColumn();
    }

    private void raiseAlert(String title, String headerText, String info) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(info);
        alert.showAndWait();
    }

    public void displayMaze(int[][] maze) {
        mazeDisplayer.addEventFilter(MouseEvent.MOUSE_CLICKED, (e) -> mazeDisplayer.requestFocus());
        setMazeDisplayerData(maze);
        mazeDisplayer.setCharacterPosition(viewModel.getCharacterPositionRow(), viewModel.getCharacterPositionColumn());
        mazeDisplayer.redraw();
    }

    private void setMazeDisplayerData(int[][] maze) {
        mazeDisplayer.setMaze(maze);
        mazeDisplayer.setCharacterPosition(viewModel.getCharacterPositionRow(), viewModel.getCharacterPositionColumn());
        mazeDisplayer.setGoalPosition(viewModel.getGoalStateRow(), viewModel.getGoalStateColumn());
    }

    void setViewModel(MyViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @FXML
    public void showNewGameDialog() {

        Dialog<ButtonType> dialog = new Dialog<>();
        setDialog(dialog);
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("NewGameDialog.fxml"));
        loadFXML(dialog, fxmlLoader);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        Optional<ButtonType> result = dialog.showAndWait();
        handleOKClicked(result, fxmlLoader);
    }

    private void setDialog(Dialog<ButtonType> dialog) {
        dialog.initOwner(mainBorderPane.getScene().getWindow());
        dialog.setTitle("Create new game");
        dialog.setHeaderText("");
    }

    private void loadFXML(Dialog<ButtonType> dialog, FXMLLoader fxmlLoader) {
        try {
            dialog.getDialogPane().setContent(fxmlLoader.load());
        } catch (IOException e) {
        }
    }

    private void handleOKClicked(Optional<ButtonType> result, FXMLLoader fxmlLoader) {
        if (isUnderCalculations)
            return;
        if (result.isPresent() && result.get() == ButtonType.OK) {
            NewGameDialog controller = fxmlLoader.getController();
            int[/* row size , column size */] newGameSize = controller.processResults();
            try {
                viewModel.generateMaze(newGameSize[0], newGameSize[1]);
                disableAll();
            } catch (Exception e) {
                enableAll();
            }
        }
    }

    public void KeyPressed(KeyEvent keyEvent) {
        if (isUnderCalculations)
            return;
        viewModel.moveCharacter(keyEvent.getCode());
        keyEvent.consume();
    }

    public void saveGameAction() {
        if (isUnderCalculations)
            return;
        viewModel.saveGame();
    }


    public void openGameAction() {
        viewModel.openGame();
    }

    public void getSolutionAction() {
        try {
            disableAll();
            viewModel.solveGame();
        } catch (Exception e) {
            enableAll();
        }
    }

    public void propertiesAction() {
        raiseAlert("Properties", "", "Number of Runing threads : " + ProjectProperties.threadPoolSize + "\n" +
                "Game slgorithm name : " + ProjectProperties.searcher.getName() + "\n" +
                "Game creation algorithm : " + ProjectProperties.mazeGenerator.getName());
    }

    public void exitGameAction() {
        viewModel.exitGame();
    }

    public void gameInstructions() {
        raiseAlert("Game instructions : ",
                "",
                "The goal is to reach the goal point \n" +
                        "You may move left,right,forth or back but not through the walls \n" +
                        "The game may be created,loaded or saved in the fille button in the menu \n" +
                        "In order to see the solution for the maze click on the solution button on menu");
    }

    public void gameRulesAction() {
        raiseAlert("Game rules : ",
                "",
                "1.You are not allowed to go through walls. \n " +
                        "2.Your are allowed to go back,forth,left and right \n" +
                        "Good luck !");
    }

    public void solvingAlgorithmsAction() {
        raiseAlert("Solving algorithms : ",
                "",
                "In this game for the solution we used the BFS algorithm");
    }

    public void ownersAction() {
        raiseAlert("Game owners : ",
                "",
                "Anatoly Ivanov \n " +
                        "Alon Gigi");
    }

    void setResizeEvent(Scene scene) {
        scene.widthProperty().addListener((observableValue, oldSceneWidth, newSceneWidth) -> {
            mazeDisplayer.setWidth(newSceneWidth.doubleValue());
            mazeDisplayer.redraw();
        });
        scene.heightProperty().addListener((observableValue, oldSceneHeight, newSceneHeight) -> {
            mazeDisplayer.setHeight(newSceneHeight.doubleValue());
            mazeDisplayer.redraw();
        });
    }

    public void zoomMaze(ScrollEvent scrollEvent) {
        double zoomFactor = 0.001;
        double deltaY = scrollEvent.getDeltaY() * zoomFactor;
        Node p = mazeDisplayer;
        if (scrollEvent.isControlDown()) {
            p.setScaleX(p.getScaleX() + deltaY);
            p.setScaleY(p.getScaleY() + deltaY);
        }
    }

    public void disableAll() {
        isUnderCalculations = true;
        saveGame.setDisable(true);
        openGame.setDisable(true);
        newGame.setDisable(true);
        getSolution.setDisable(true);
    }

    public void enableAll() {
        isUnderCalculations = false;
        saveGame.setDisable(false);
        openGame.setDisable(false);
        newGame.setDisable(false);
        getSolution.setDisable(false);
    }


    public class CommandClass {
        Hashtable<String, Function> commandTable;

        CommandClass() {
            commandTable = new Hashtable<>();
            createGetSolution();
            createDisplayGame();
            createGenerateMaze();
            createSaveGame();
            createEnableAll();
        }

        public void addCommand(Function<int[][], Boolean> command, String commandName) {
            commandTable.put(commandName, command);
        }

        private void createGenerateMaze() {
            Function<int[][], Boolean> getSolutionFunc = (int[][] maze) -> {
                Sound.startPlayingMusic();
                displayMaze(viewModel.getMaze());
                return true;
            };
            addCommand(getSolutionFunc, "generateMaze");
        }

        private void createSaveGame() {
            Function<int[][], Boolean> getSolutionFunc = (int[][] maze) -> {
                enableAll();
                return true;
            };
            addCommand(getSolutionFunc, "SaveGame");
        }

        private void createEnableAll() {
            Function<int[][], Boolean> getSolutionFunc = (int[][] maze) -> {
                enableAll();
                return true;
            };
            addCommand(getSolutionFunc, "EnableAll");
        }

        private void createGetSolution() {
            Function<int[][], Boolean> getSolutionFunc = (int[][] maze) -> {
                mazeDisplayer.drawSolution(viewModel.getGameSolution());
                return true;
            };
            addCommand(getSolutionFunc, "getSolution");
        }

        private void createDisplayGame() {
            Function<int[][], Boolean> displayGame = (int[][] maze) -> {
                displayMaze(viewModel.getMaze());
                return true;
            };
            addCommand(displayGame, "displayGame");

        }

        private void execute(String mission) {
            commandTable.get(mission).apply(viewModel.getMaze());
        }

    }
}