package ui;


import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import model.CollectionCenter;
import model.DeliveryRequest;
import model.Hospital;
import model.MapModel;
import model.MedicalSite;
import model.MedicalStaff;
import model.Mission;
import model.Position;
import model.UserPoint;
import model.enums.PriorityLevel;
import service.ImportExportService;
import service.OptimizationService;

import java.util.Optional;

/**
 * Main JavaFX window.
 * This class contains the map canvas, the buttons and the log area.
 */
public class MainWindow extends BorderPane {

    private final MapModel mapModel;
    private final OptimizationService optimizationService;
    private final ImportExportService importExportService;
    private final MedicalStaff defaultDoctor;

    private final MapCanvas mapCanvas;
    private final TextArea logArea;

    private Mission currentMission;

    public MainWindow(
            MapModel mapModel,
            OptimizationService optimizationService,
            ImportExportService importExportService,
            MedicalStaff defaultDoctor
    ) {
        this.mapModel = mapModel;
        this.optimizationService = optimizationService;
        this.importExportService = importExportService;
        this.defaultDoctor = defaultDoctor;

        this.mapCanvas = new MapCanvas(mapModel);
        this.logArea = new TextArea();

        initializeLayout();
    }

    private void initializeLayout() {
        Label title = new Label("Organ Drone Delivery - JavaFX");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
        title.setPadding(new Insets(10));

        setTop(title);
        setCenter(mapCanvas);
        setLeft(createControlPanel());

        logArea.setEditable(false);
        logArea.setPrefHeight(130);
        logArea.setText("Application started.\n");

        setBottom(logArea);

        mapCanvas.draw();
    }

    private VBox createControlPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setPrefWidth(230);

        Label label = new Label("Actions");
        label.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Button addHospitalButton = new Button("Add Hospital");
        Button addCenterButton = new Button("Add Collection Center");
        Button addUserPointButton = new Button("Add User Point");
        Button addRandomPointsButton = new Button("Add Random User Points");

        Button displayStatsButton = new Button("Display Statistics");
        Button findNearestButton = new Button("Find Nearest Site");

        Button createMissionButton = new Button("Create Mission");
        Button startMissionButton = new Button("Start Mission");
        Button trackMissionButton = new Button("Track Mission");
        Button completeMissionButton = new Button("Complete Mission");
        Button cancelMissionButton = new Button("Cancel Mission");

        Button exportButton = new Button("Export Map");
        Button importButton = new Button("Import Map");

        addHospitalButton.setMaxWidth(Double.MAX_VALUE);
        addCenterButton.setMaxWidth(Double.MAX_VALUE);
        addUserPointButton.setMaxWidth(Double.MAX_VALUE);
        addRandomPointsButton.setMaxWidth(Double.MAX_VALUE);
        displayStatsButton.setMaxWidth(Double.MAX_VALUE);
        findNearestButton.setMaxWidth(Double.MAX_VALUE);
        createMissionButton.setMaxWidth(Double.MAX_VALUE);
        startMissionButton.setMaxWidth(Double.MAX_VALUE);
        trackMissionButton.setMaxWidth(Double.MAX_VALUE);
        completeMissionButton.setMaxWidth(Double.MAX_VALUE);
        cancelMissionButton.setMaxWidth(Double.MAX_VALUE);
        exportButton.setMaxWidth(Double.MAX_VALUE);
        importButton.setMaxWidth(Double.MAX_VALUE);

        addHospitalButton.setOnAction(event -> addHospital());
        addCenterButton.setOnAction(event -> addCollectionCenter());
        addUserPointButton.setOnAction(event -> addUserPoint());
        addRandomPointsButton.setOnAction(event -> addRandomUserPoints());

        displayStatsButton.setOnAction(event -> displayStatistics());
        findNearestButton.setOnAction(event -> findNearestSite());

        createMissionButton.setOnAction(event -> createMission());
        startMissionButton.setOnAction(event -> startMission());
        trackMissionButton.setOnAction(event -> trackMission());
        completeMissionButton.setOnAction(event -> completeMission());
        cancelMissionButton.setOnAction(event -> cancelMission());

        exportButton.setOnAction(event -> exportMap());
        importButton.setOnAction(event -> importMap());

        panel.getChildren().addAll(
                label,
                addHospitalButton,
                addCenterButton,
                addUserPointButton,
                addRandomPointsButton,
                displayStatsButton,
                findNearestButton,
                createMissionButton,
                startMissionButton,
                trackMissionButton,
                completeMissionButton,
                cancelMissionButton,
                exportButton,
                importButton
        );

        return panel;
    }

    private void addHospital() {
        try {
            String id = askText("Hospital id");
            String name = askText("Hospital name");
            double x = askDouble("Hospital x");
            double y = askDouble("Hospital y");

            Hospital hospital = new Hospital(id, name, new Position(x, y), true);
            mapModel.addMedicalSite(hospital);

            refresh("Hospital added: " + name);
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private void addCollectionCenter() {
        try {
            String id = askText("Collection center id");
            String name = askText("Collection center name");
            double x = askDouble("Collection center x");
            double y = askDouble("Collection center y");

            String organs = askText("Organ types separated by comma, example: Kidney,Heart,Liver");

            CollectionCenter center = new CollectionCenter(
                    id,
                    name,
                    new Position(x, y),
                    java.util.Arrays.asList(organs.split(","))
            );

            mapModel.addMedicalSite(center);

            refresh("Collection center added: " + name);
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private void addUserPoint() {
        try {
            String id = askText("User point id");
            double x = askDouble("User point x");
            double y = askDouble("User point y");

            UserPoint userPoint = new UserPoint(id, new Position(x, y));
            mapModel.addUserPoint(userPoint);

            refresh("User point added: " + id);
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private void addRandomUserPoints() {
        try {
            int count = askInt("Number of random user points");

            mapModel.addRandomUserPoints(count, 0, 0, 800, 600);

            refresh(count + " random user points added.");
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private void displayStatistics() {
        StringBuilder builder = new StringBuilder();

        builder.append("\n=== Statistics ===\n");
        builder.append("Medical sites: ").append(mapModel.getMedicalSites().size()).append("\n");
        builder.append("Hospitals: ").append(mapModel.getHospitals().size()).append("\n");
        builder.append("Collection centers: ").append(mapModel.getCollectionCenters().size()).append("\n");
        builder.append("User points: ").append(mapModel.getUserPoints().size()).append("\n");
        builder.append("Drone bases: ").append(mapModel.getDroneBases().size()).append("\n");
        builder.append("Drones: ").append(mapModel.getDrones().size()).append("\n");
        builder.append("Voronoi cells: ").append(mapModel.getVoronoiDiagram().getCells().size()).append("\n");
        builder.append("Delaunay triangles: ").append(mapModel.getDelaunayTriangulation().getTriangles().size()).append("\n");

        log(builder.toString());
    }

    private void findNearestSite() {
        try {
            double x = askDouble("x");
            double y = askDouble("y");

            MedicalSite nearest = mapModel.getVoronoiDiagram().getNearestSite(new Position(x, y));

            if (nearest == null) {
                log("No nearest site found.");
            } else {
                log("Nearest site: " + nearest.getId() + " | " + nearest.getName());
            }
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private void createMission() {
        try {
            CollectionCenter origin = chooseFirstCollectionCenter();
            Hospital destination = chooseFirstHospital();

            if (origin == null || destination == null) {
                showError("You need at least one collection center and one hospital.");
                return;
            }

            String organType = askText("Organ type, example: Kidney");
            PriorityLevel priorityLevel = PriorityLevel.CRITICAL;

            DeliveryRequest request = defaultDoctor.createDeliveryRequest(
                    origin,
                    destination,
                    organType,
                    priorityLevel
            );

            currentMission = optimizationService.createMission(request, mapModel.getDrones());

            mapCanvas.setCurrentMission(currentMission);

            refresh("Mission created with drone: " + currentMission.getDrone().getId()
                    + "\nRoute distance: " + currentMission.getRoute().computeDistance());
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private void startMission() {
        if (currentMission == null) {
            showError("No current mission.");
            return;
        }

        currentMission.start();
        refresh("Mission started.");
    }

    private void trackMission() {
        if (currentMission == null) {
            showError("No current mission.");
            return;
        }

        log("\n=== Current Mission ==="
                + "\nId: " + currentMission.getId()
                + "\nStatus: " + currentMission.getStatus()
                + "\nDrone: " + currentMission.getDrone().getId()
                + "\nCurrent position: (" + currentMission.getCurrentPosition().getX()
                + ", " + currentMission.getCurrentPosition().getY() + ")"
                + "\nBattery: " + currentMission.getBatteryLevel()
                + "\nReception confirmed: " + currentMission.isReceptionConfirmed()
        );
    }

    private void completeMission() {
        if (currentMission == null) {
            showError("No current mission.");
            return;
        }

        currentMission.complete();
        currentMission.getRequest().getDestination().receiveMission(currentMission);
        defaultDoctor.validateReception(currentMission);

        refresh("Mission completed and reception confirmed.");
    }

    private void cancelMission() {
        if (currentMission == null) {
            showError("No current mission.");
            return;
        }

        currentMission.cancel();
        refresh("Mission cancelled.");
    }

    private void exportMap() {
        try {
            String path = askText("Export file path, example: map.bin");
            importExportService.exportMap(mapModel, path);

            log("Map exported to: " + path);
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private void importMap() {
        try {
            String path = askText("Import file path, example: map.bin");
            MapModel importedMap = importExportService.importMap(path);

            showInfo("Import done", "The map was imported successfully.\nRestart the JavaFX app to use the imported map in this simple version.");

            log("Map imported from: " + path);
            log("Imported medical sites: " + importedMap.getMedicalSites().size());
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private CollectionCenter chooseFirstCollectionCenter() {
        if (mapModel.getCollectionCenters().isEmpty()) {
            return null;
        }

        return mapModel.getCollectionCenters().get(0);
    }

    private Hospital chooseFirstHospital() {
        if (mapModel.getHospitals().isEmpty()) {
            return null;
        }

        return mapModel.getHospitals().get(0);
    }

    private String askText(String message) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Input");
        dialog.setHeaderText(message);
        dialog.setContentText(message + ":");

        Optional<String> result = dialog.showAndWait();

        if (result.isEmpty() || result.get().isBlank()) {
            throw new IllegalArgumentException("Input cannot be empty.");
        }

        return result.get();
    }

    private double askDouble(String message) {
        String input = askText(message);

        try {
            return Double.parseDouble(input);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number: " + input);
        }
    }

    private int askInt(String message) {
        String input = askText(message);

        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid integer: " + input);
        }
    }

    private void refresh(String message) {
        mapCanvas.draw();
        log(message);
    }

    private void log(String message) {
        logArea.appendText(message + "\n");
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("An error occurred");
        alert.setContentText(message);
        alert.showAndWait();

        log("Error: " + message);
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}