package app;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.*;
import ui.MainWindow;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {

        MapData mapData = new MapData();

        DroneBase baseA =
                new DroneBase(
                        "Drone Base A",
                        new Position(150, 150)
                );

        Hospital hospitalA =
                new Hospital(
                        "Hospital A",
                        new Position(400, 300)
                );

        mapData.getDroneBases().add(baseA);
        mapData.getHospitals().add(hospitalA);

        MainWindow root = new MainWindow(mapData);

        Scene scene = new Scene(root, 900, 600);

        stage.setTitle("OrganDrone");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
