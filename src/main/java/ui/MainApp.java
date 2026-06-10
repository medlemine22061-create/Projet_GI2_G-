package ui;


import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.CollectionCenter;
import model.Drone;
import model.DroneBase;
import model.Hospital;
import model.MapModel;
import model.MedicalStaff;
import model.Position;
import service.ImportExportService;
import service.OptimizationService;

import java.util.Arrays;

/**
 * JavaFX entry point of the application.
 * This class starts the graphical interface.
 */
public class MainApp extends Application {

    private MapModel mapModel;
    private OptimizationService optimizationService;
    private ImportExportService importExportService;
    private MedicalStaff defaultDoctor;

    @Override
    public void start(Stage primaryStage) {
        initializeData();

        MainWindow mainWindow = new MainWindow(
                mapModel,
                optimizationService,
                importExportService,
                defaultDoctor
        );

        Scene scene = new Scene(mainWindow, 1200, 800);

        primaryStage.setTitle("Organ Drone Delivery - JavaFX");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Creates initial demo data for the JavaFX interface.
     * This is similar to the demo data used in the CMD version.
     */
    private void initializeData() {
        mapModel = new MapModel();
        importExportService = new ImportExportService();

        Hospital hospitalA = new Hospital("H1", "Hospital Nord", new Position(100, 100), true);
        Hospital hospitalB = new Hospital("H2", "Hospital Sud", new Position(500, 300), true);

        CollectionCenter centerA = new CollectionCenter(
                "C1",
                "Collection Center Est",
                new Position(200, 500),
                Arrays.asList("Heart", "Kidney", "Liver")
        );

        CollectionCenter centerB = new CollectionCenter(
                "C2",
                "Collection Center Ouest",
                new Position(700, 150),
                Arrays.asList("Kidney", "Lung")
        );

        mapModel.addMedicalSite(hospitalA);
        mapModel.addMedicalSite(hospitalB);
        mapModel.addMedicalSite(centerA);
        mapModel.addMedicalSite(centerB);

        DroneBase base = new DroneBase("B1", "Main Drone Base", new Position(150, 150), 10);

        Drone drone1 = new Drone("D1", 1000, 90, 5, 60, new Position(120, 120));
        Drone drone2 = new Drone("D2", 700, 50, 5, 55, new Position(600, 200));
        Drone drone3 = new Drone("D3", 1200, 80, 5, 70, new Position(300, 450));

        base.addDrone(drone1);
        base.addDrone(drone2);
        base.addDrone(drone3);

        mapModel.addDroneBase(base);
        mapModel.addDrone(drone1);
        mapModel.addDrone(drone2);
        mapModel.addDrone(drone3);

        defaultDoctor = new MedicalStaff(
                "M1",
                "Yousef",
                "Boukah",
                "Doctor",
                "doctor@example.com",
                "0600000000",
                hospitalA
        );

        optimizationService = new OptimizationService(mapModel.getDelaunayTriangulation());
    }

    public static void main(String[] args) {
        launch(args);
    }
}