package View;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

/**
 * Created by anatolyi on 6/19/2017.
 */
public class NewGameDialog {

    @FXML
    private TextField rowSize;

    @FXML
    private TextField columnSize;

    public int[] processResults() {
        String mazeRowSize = rowSize.getText().trim();
        String mazeColumnSize = columnSize.getText().trim();

        if (illegalInput())
            return new int[]{-1, -1};

        return new int[/* row size , column size */]{Integer.parseInt(mazeRowSize), Integer.parseInt(mazeColumnSize)};
    }

    private boolean illegalInput() {
        boolean illegalInput = isEmptyString() || !isANumber() || isSizeTooSmall();
        if (illegalInput) {
            if (isEmptyString())
                raiseAlert("Warning !",
                        "",
                        "Nothing is entered \n" +
                                "Game couldn't be loaded.");
            else if (!isANumber())
                raiseAlert("Warning !",
                        "",
                        "Number must be entered,nothing else \n" +
                                "Game couldn't be loaded.");
            else if (isSizeTooSmall())
                raiseAlert("Warning !",
                        "",
                        "Choosen size is too small. \n" +
                                "In order to have pleasent game enter size bigger than 10 \n" +
                                "Game couldn't be loaded.");
        }
        return illegalInput;
    }

    private boolean isEmptyString() {
        return getRow().equals("") || getColumn().equals("");
    }

    private boolean isANumber() {
        try {
            Integer.parseInt(getRow());
            Integer.parseInt(getColumn());
        } catch(NumberFormatException | NullPointerException e) {
            return false;
        }
        return true;
    }

    private boolean isSizeTooSmall() {
        return Integer.parseInt(getRow()) < 10 || Integer.parseInt(getColumn()) < 10;
    }

    private String getRow() {
        return rowSize.getText().trim();
    }

    private String getColumn() {
        return columnSize.getText().trim();
    }

    private void raiseAlert(String title, String headerText, String info) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(info);
        alert.showAndWait();
    }

}
