package View;

import Model.MyModel;
import ViewModel.MyViewModel;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import Server.*;
import javafx.stage.WindowEvent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static sun.net.www.protocol.http.AuthCacheValue.Type.Server;

public class Main extends Application {


    public static Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception{
        this.primaryStage = primaryStage;

        MyModel model=new MyModel();
        MyViewModel viewModel=new MyViewModel(model);
        model.addObserver(viewModel);

        FXMLLoader fxmlLoader = new FXMLLoader();
        Parent root = fxmlLoader.load(getClass().getResource("MyView.fxml").openStream());
        primaryStage.setTitle("Maze 2D Game");
        Scene scene = new Scene(root, 1100, 800);
        scene.getStylesheets().add(getClass().getResource("ViewStyle.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();

        MyViewController view = fxmlLoader.getController();
        view.setResizeEvent(scene);

        MyViewController mwc = fxmlLoader.getController();
        mwc.setViewModel(viewModel);
        viewModel.addObserver(mwc);

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {
                model.exitGame();
            }
        });

    }

    public static void main(String[] args) {
        launch(args);
    }

}
