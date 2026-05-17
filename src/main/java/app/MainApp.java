package app;

import javafx.application.Application;
import javafx.stage.Stage;
import ui.MainWindow;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        new MainWindow().show(stage);
    }

    public static void main(String[] args) {
        launch();
    }
}
