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
import model.UserPoint;
import java.util.Arrays;

/**
 * JavaFX entry point — MEDADRONE Organ Delivery System.
 *
 * Important: every drone starts AT the base position.
 * They are not shown individually on the map — only the base is shown.
 * When a mission starts, the selected drone departs visually from the base.
 */
public class MainApp extends Application {

    private MapModel            mapModel;
    private OptimizationService optimizationService;
    private ImportExportService importExportService;
    private MedicalStaff        defaultDoctor;

    @Override
    public void start(Stage primaryStage) {
        initData();

        MainWindow mainWindow = new MainWindow(
                mapModel, optimizationService, importExportService, defaultDoctor
        );

        Scene scene = new Scene(mainWindow, 1280, 820);

        primaryStage.setTitle("MEDADRONE — Organ Delivery System");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        // Force native window decoration (title bar + close/min/max buttons)
        primaryStage.initStyle(javafx.stage.StageStyle.DECORATED);
        primaryStage.show();
    }

    private void initData() {
        mapModel            = new MapModel();
        importExportService = new ImportExportService();

        Hospital h1 = new Hospital("H1", "Hospital Nord",  new Position(140, 130), true);
        Hospital h2 = new Hospital("H2", "Hospital Sud",   new Position(560, 360), true);

        CollectionCenter c1 = new CollectionCenter(
                "C1", "Collection Center Est",
                new Position(220, 510),
                Arrays.asList("Heart", "Kidney", "Liver")
        );
        CollectionCenter c2 = new CollectionCenter(
                "C2", "Collection Center Ouest",
                new Position(720, 160),
                Arrays.asList("Kidney", "Lung")
        );

        mapModel.addMedicalSite(h1);
        mapModel.addMedicalSite(h2);
        mapModel.addMedicalSite(c1);
        mapModel.addMedicalSite(c2);

        // One drone base — all drones start AT the base position
        Position basePos = new Position(400, 300);
        DroneBase base   = new DroneBase("B1", "Main Drone Base", basePos, 10);

        // All drones start at the base position (not scattered on the map)
        Drone d1 = new Drone("D1", 1000, 90, 5, 60, new Position(basePos.getX(), basePos.getY()));
        Drone d2 = new Drone("D2",  700, 50, 5, 55, new Position(basePos.getX(), basePos.getY()));
        Drone d3 = new Drone("D3", 1200, 80, 5, 70, new Position(basePos.getX(), basePos.getY()));

        base.addDrone(d1);
        base.addDrone(d2);
        base.addDrone(d3);

        mapModel.addDroneBase(base);
        mapModel.addDrone(d1);
        mapModel.addDrone(d2);
        mapModel.addDrone(d3);

        defaultDoctor = new MedicalStaff(
                "M1", "Yousef", "Boukah", "Chief Surgeon",
                "yousef.boukah@hospital-nord.fr", "0600000000", h1
        );

        optimizationService = new OptimizationService(mapModel.getDelaunayTriangulation(), mapModel);
    }

    public static void main(String[] args) {
        launch(args);
    }
}