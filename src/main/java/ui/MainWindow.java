package ui;

import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import model.*;
import service.MapUpdateService;

public class MainWindow {

    private MapData mapData;
    private MapCanvas mapCanvas;
    private StatisticsPanel statisticsPanel;

    public void show(Stage stage) {
        createDemoData();

        mapCanvas = new MapCanvas(mapData);
        statisticsPanel = new StatisticsPanel(mapData);

        BorderPane root = new BorderPane();
        root.setCenter(mapCanvas);
        root.setRight(statisticsPanel);

        Scene scene = new Scene(root, 1100, 700);

        stage.setTitle("OrganDrone - Livraison d'organes par drones");
        stage.setScene(scene);
        stage.show();
    }

    private void createDemoData() {
        mapData = new MapData();

        DroneBase droneA = new DroneBase(1, "Drone Base A", new Point2D(100, 120), 3, 100, 80);
        DroneBase droneB = new DroneBase(2, "Drone Base B", new Point2D(520, 160), 2, 90, 80);
        DroneBase droneC = new DroneBase(3, "Drone Base C", new Point2D(330, 470), 1, 85, 80);

        mapData.addDroneBase(droneA);
        mapData.addDroneBase(droneB);
        mapData.addDroneBase(droneC);

        Hospital receiverA = new Hospital(1, "Receiver Hospital A", new Point2D(170, 300), false, null);
        Hospital receiverB = new Hospital(2, "Receiver Hospital B", new Point2D(650, 420), false, null);

        Hospital donorHeart = new Hospital(3, "Donor Center HEART", new Point2D(320, 170), true, OrganType.HEART);
        Hospital donorKidney = new Hospital(4, "Donor Center KIDNEY", new Point2D(470, 370), true, OrganType.KIDNEY);
        Hospital donorLung = new Hospital(5, "Donor Center LUNG", new Point2D(250, 530), true, OrganType.LUNG);

        mapData.addHospital(receiverA);
        mapData.addHospital(receiverB);
        mapData.addHospital(donorHeart);
        mapData.addHospital(donorKidney);
        mapData.addHospital(donorLung);

        mapData.addMedicalRequest(new MedicalRequest(
                1,
                "Req HEART",
                OrganType.HEART,
                PriorityLevel.CRITICAL,
                receiverA
        ));

        mapData.addMedicalRequest(new MedicalRequest(
                2,
                "Req KIDNEY",
                OrganType.KIDNEY,
                PriorityLevel.HIGH,
                receiverB
        ));

        mapData.addMedicalRequest(new MedicalRequest(
                3,
                "Req LUNG",
                OrganType.LUNG,
                PriorityLevel.MEDIUM,
                receiverA
        ));

        new MapUpdateService().updateMap(mapData);
    }
}
