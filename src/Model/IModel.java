package Model;

import ViewModel.MyViewModel;
import javafx.scene.input.KeyCode;

/**
 * Created by anatolyi on 6/19/2017.
 */
public interface IModel {

    public void generateMaze(int rowSize, int columnSize);
    public int[][] getMazeInstance();
    public int[] getStartPoint();
    public int getCharacterPositionRow();
    public int getCharacterPositionColumn();
    public int getGoalPositionRow();
    public int getGoalPositionColumn();
    public void moveCharacter(String movement);
    public void saveGame(String path);
    public boolean isMazeExist();
    public void openMazeFile(String path);
    public void solveGame();
    public int[][] getGameSolution();
    public void exitGame();
    public void deleteMaze();


}
