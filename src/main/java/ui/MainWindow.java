package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.CollectionCenter;
import model.DeliveryRequest;
import model.Drone;
import model.DroneBase;
import model.Hospital;
import model.MapModel;
import model.MedicalSite;
import model.MedicalStaff;
import model.Mission;
import model.Position;
import model.UserPoint;
import model.enums.DroneStatus;
import model.enums.PriorityLevel;
import service.ImportExportService;
import service.OptimizationService;

import java.util.Optional;

/**
 * Main JavaFX window — MEDADRONE Organ Delivery System.
 *
 * Mission logic:
 *   The user selects the hospital that issues the request (requester + destination)
 *   and the collection center that holds the organ (origin).
 *   The system finds the nearest available drone (from its base),
 *   which flies:  base  →  collection center  →  hospital.
 */
public class MainWindow extends BorderPane {

    // ── Services ──────────────────────────────────────────────────────────────
    private final MapModel            mapModel;
    private final OptimizationService optimizationService;
    private final ImportExportService importExportService;
    private final MedicalStaff        defaultDoctor;

    // ── UI ────────────────────────────────────────────────────────────────────
    private final MapCanvas mapCanvas;
    private int userPointCounter = 0;
    private Mission currentMission;

    // ── Styles ────────────────────────────────────────────────────────────────
    private static final String ROOT_STYLE =
            "-fx-background-color: #161A22;";
    private static final String SIDEBAR_STYLE =
            "-fx-background-color: #0D1420;"
                    + " -fx-border-color: #1E2E48; -fx-border-width: 0 1 0 0;";


    // ── Constructor ───────────────────────────────────────────────────────────
    public MainWindow(MapModel mapModel, OptimizationService optimizationService,
                      ImportExportService importExportService, MedicalStaff defaultDoctor) {
        this.mapModel            = mapModel;
        this.optimizationService = optimizationService;
        this.importExportService = importExportService;
        this.defaultDoctor       = defaultDoctor;
        this.mapCanvas           = new MapCanvas(mapModel);
        build();
    }

    // ── Layout ────────────────────────────────────────────────────────────────

    private void build() {
        setStyle(ROOT_STYLE);
        setTop(buildHeader());
        setLeft(buildSidebar());
        setCenter(mapCanvas);

        // Pas de terminal en bas — les logs s'affichent sur le canvas

        mapCanvas.draw();
    }

    private HBox buildHeader() {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(10, 20, 10, 20));
        bar.setStyle("-fx-background-color: #070C14;"
                + " -fx-border-color: #1E2E48; -fx-border-width: 0 0 1 0;");

        Label cross = new Label("✚");
        cross.setStyle("-fx-text-fill: #1ED882; -fx-font-size: 18px;");

        Label title = new Label("  MEDADRONE  —  Organ Delivery System");
        title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;"
                + " -fx-text-fill: #D0E8FF; -fx-font-family: 'Monospace';");

        bar.getChildren().addAll(cross, title);
        return bar;
    }

    private ScrollPane buildSidebar() {
        VBox p = new VBox(6);
        p.setPadding(new Insets(12, 10, 12, 10));
        p.setPrefWidth(215);
        p.setStyle(SIDEBAR_STYLE);

        // Medical Sites
        p.getChildren().add(section("MEDICAL SITES"));
        p.getChildren().add(btn("＋  Add Hospital",          "#1ED882", e -> addHospital()));
        p.getChildren().add(btn("＋  Add Collection Center", "#FFA030", e -> addCenter()));
        p.getChildren().add(btn("✕  Remove Site",            "#FF5050", e -> removeSite()));
        p.getChildren().add(btn("↔  Move Site",              "#50C0FF", e -> moveSite()));
        p.getChildren().add(sep());

        p.getChildren().add(section("DRONE BASES"));
        p.getChildren().add(btn("＋  Add Drone Base",        "#8C64FF", e -> addDroneBase()));
        p.getChildren().add(sep());

        // Map Data
        p.getChildren().add(section("MAP DATA"));
        p.getChildren().add(btn("⬆  Import CSV",             "#50C0FF", e -> importCsv()));
        p.getChildren().add(btn("💾  Export Map",             "#8C64FF", e -> exportMap()));
        p.getChildren().add(btn("📂  Import Map",             "#8C64FF", e -> importMap()));
        p.getChildren().add(sep());

        // Diagnostics
        p.getChildren().add(section("USER POINTS"));
        p.getChildren().add(btn("+   Add User Point",         "#C864FF", e -> addUserPoint()));
        p.getChildren().add(btn("+   Add Random User Points", "#C864FF", e -> addRandomUserPoints()));
        p.getChildren().add(btn("x   Remove User Point",      "#FF5050", e -> removeUserPoint()));
        p.getChildren().add(sep());

        p.getChildren().add(section("DIAGNOSTICS"));
        p.getChildren().add(btn("    Statistics",             "#50C0FF", e -> stats()));
        p.getChildren().add(btn("    Find Nearest Site",      "#50C0FF", e -> findNearest()));
        p.getChildren().add(sep());

        // Mission Control
        p.getChildren().add(section("MISSION CONTROL"));
        p.getChildren().add(btn("🚀  Create Mission",         "#1ED882", e -> createMission()));
        p.getChildren().add(btn("▶   Launch & Animate",       "#FFA030", e -> launchMission()));
        p.getChildren().add(btn("📡  Track Mission",          "#50C0FF", e -> trackMission()));
        p.getChildren().add(btn("↔  Move Drone Base",         "#8C64FF", e -> moveDroneBase()));
        p.getChildren().add(btn("✕   Cancel Mission",         "#FF5050", e -> cancelMission()));

        ScrollPane sp = new ScrollPane(p);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background-color: transparent; -fx-background: transparent;"
                + " -fx-border-color: transparent;");
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        return sp;
    }

    // ── Button / label factories ──────────────────────────────────────────────

    private Button btn(String text, String hex,
                       javafx.event.EventHandler<javafx.event.ActionEvent> h) {
        Button b = new Button(text);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setAlignment(Pos.CENTER_LEFT);
        b.setPadding(new Insets(7, 10, 7, 10));
        String base = "-fx-background-color:#0E1828; -fx-text-fill:" + hex
                + "; -fx-font-family:'Monospace'; -fx-font-size:11px;"
                + " -fx-background-radius:5; -fx-cursor:hand;";
        String hover = "-fx-background-color:#192E50; -fx-text-fill:" + hex
                + "; -fx-font-family:'Monospace'; -fx-font-size:11px;"
                + " -fx-background-radius:5; -fx-cursor:hand;";
        b.setStyle(base);
        b.setOnMouseEntered(e -> b.setStyle(hover));
        b.setOnMouseExited(e  -> b.setStyle(base));
        b.setOnAction(h);
        return b;
    }

    private Label section(String t) {
        Label l = new Label(t);
        l.setStyle("-fx-font-size:10px; -fx-font-weight:bold;"
                + " -fx-text-fill:#3A5A80; -fx-font-family:'Monospace';");
        l.setPadding(new Insets(8, 0, 2, 0));
        return l;
    }

    private Separator sep() {
        Separator s = new Separator();
        s.setStyle("-fx-background-color:#1E2E48;");
        return s;
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    private void addHospital() {
        try {
            String id   = ask("Hospital ID  (e.g. H3)");
            String name = ask("Hospital name");
            double x    = askD("X coordinate  (0–850)");
            double y    = askD("Y coordinate  (0–620)");
            mapModel.addMedicalSite(new model.Hospital(id, name, new Position(x, y), true));
            refresh("✚  Hospital added: " + name);
        } catch (Exception e) { err(e.getMessage()); }
    }

    private void addCenter() {
        try {
            String id     = ask("Collection center ID  (e.g. C3)");
            String name   = ask("Collection center name");
            double x      = askD("X coordinate  (0–850)");
            double y      = askD("Y coordinate  (0–620)");
            String organs = ask("Organ types separated by comma  (e.g. Kidney,Heart,Liver)");
            mapModel.addMedicalSite(new model.CollectionCenter(
                    id, name, new Position(x, y),
                    java.util.Arrays.asList(organs.split(","))
            ));
            refresh("✚  Collection center added: " + name);
        } catch (Exception e) { err(e.getMessage()); }
    }

    private void removeSite() {
        try {
            String id = ask("ID of the site to remove");
            MedicalSite site = mapModel.findMedicalSiteById(id);
            if (site == null) { err("No site found: " + id); return; }
            mapModel.removeMedicalSite(site);
            refresh("✕  Site removed: " + site.getName());
        } catch (Exception e) { err(e.getMessage()); }
    }

    private void moveSite() {
        try {
            String id = ask("ID of the site to move");
            MedicalSite site = mapModel.findMedicalSiteById(id);
            if (site == null) { err("No site found: " + id); return; }
            double x = askD("New X coordinate");
            double y = askD("New Y coordinate");
            mapModel.moveMedicalSite(site, new Position(x, y));
            refresh("↔  Site moved: " + site.getName()
                    + " → (" + (int)x + ", " + (int)y + ")");
        } catch (Exception e) { err(e.getMessage()); }
    }

    /**
     * Adds a new drone base with one or more drones inside.
     *
     * The user provides:
     *  - Base ID, name, X/Y position, capacity
     *  - For each drone: ID, autonomy, battery, payload, speed
     *    (drone position is set to the base position automatically)
     */
    private void addDroneBase() {
        try {
            // ── Base info ──────────────────────────────────────────────────────
            String baseId   = ask("Base ID  (e.g. B2)");
            String baseName = ask("Base name  (e.g. South Drone Base)");
            double bx       = askD("Base X coordinate  (0–850)");
            double by       = askD("Base Y coordinate  (0–620)");
            int    capacity = (int) askD("Base capacity  (max number of drones, e.g. 5)");

            Position basePos = new Position(bx, by);
            DroneBase base   = new DroneBase(baseId, baseName, basePos, capacity);

            // ── Drones ─────────────────────────────────────────────────────────
            int droneCount = (int) askD("Number of drones to add to this base  (e.g. 2)");

            if (droneCount < 1 || droneCount > capacity) {
                err("Drone count must be between 1 and " + capacity);
                return;
            }

            StringBuilder summary = new StringBuilder();

            for (int i = 0; i < droneCount; i++) {
                log("── Drone " + (i+1) + " of " + droneCount + " ───────────────");

                String droneId  = ask("Drone ID  (e.g. D4)");
                double autonomy = askD("Autonomy  km  (e.g. 1000)");
                double battery  = askD("Battery level  %  (0–100, e.g. 90)");
                double payload  = askD("Max payload  kg  (e.g. 5)");
                double speed    = askD("Speed  km/h  (e.g. 60)");

                // Drone starts at the base position
                Drone drone = new Drone(droneId, autonomy, battery, payload, speed,
                        new Position(basePos.getX(), basePos.getY()));

                base.addDrone(drone);
                mapModel.addDrone(drone);

                summary.append("    ").append(droneId)
                        .append("  autonomy=").append((int)autonomy)
                        .append("  bat=").append((int)battery).append("%")
                        .append("  speed=").append((int)speed).append("\n");
            }

            mapModel.addDroneBase(base);
            refresh("+ Drone base added: " + baseName + "\n"
                    + "    Position: (" + (int)bx + ", " + (int)by + ")\n"
                    + "    Drones:\n" + summary);

        } catch (Exception e) { err(e.getMessage()); }
    }

    private void moveDroneBase() {
        try {
            StringBuilder list = new StringBuilder("Available bases:\n");
            for (model.DroneBase b : mapModel.getDroneBases())
                list.append("  ").append(b.getId()).append("  ->  ").append(b.getName()).append("\n");
            mapCanvas.showStats(list.toString());

            String id = ask("Base ID to move");
            model.DroneBase base = null;
            for (model.DroneBase b : mapModel.getDroneBases())
                if (b.getId().equalsIgnoreCase(id)) { base = b; break; }
            if (base == null) { err("Base not found: " + id); return; }

            double x = askD("New X coordinate");
            double y = askD("New Y coordinate");
            base.getPosition().setX(x);
            base.getPosition().setY(y);
            mapCanvas.closeStats();
            mapCanvas.draw();
        } catch (Exception e) { err(e.getMessage()); }
    }

    private void importCsv() {
        try {
            String path = ask("CSV file path  (e.g. sites.csv)");
            importExportService.importMedicalSitesFromCsv(mapModel, path);
            refresh("⬆  CSV imported from: " + path);
        } catch (Exception e) { err(e.getMessage()); }
    }

    private void exportMap() {
        try {
            String path = ask("Export file path  (e.g. map.bin)");
            importExportService.exportMap(mapModel, path);
            log("💾  Map exported to: " + path);
        } catch (Exception e) { err(e.getMessage()); }
    }

    private void importMap() {
        try {
            String path = ask("Import file path  (e.g. map.bin)");
            importExportService.importMap(path);
            log("📂  Map imported from: " + path);
            info("Import successful", "Restart the app to load the imported map.");
        } catch (Exception e) { err(e.getMessage()); }
    }

    private void addUserPoint() {
        try {
            double x = askD("X coordinate  (0-850)");
            double y = askD("Y coordinate  (0-620)");
            String id = "U" + (++userPointCounter);
            UserPoint up = new UserPoint(id, new Position(x, y));
            mapModel.addUserPoint(up);
            refresh("+ User point added: " + id
                    + " -> nearest: " + (up.getNearestSite() != null
                    ? up.getNearestSite().getName() : "none"));
        } catch (Exception e) { err(e.getMessage()); }
    }

    private void addRandomUserPoints() {
        try {
            int count = (int) askD("Number of random user points to add (e.g. 10)");
            int before = mapModel.getUserPoints().size();
            mapModel.addRandomUserPoints(count, 20, 20, 900, 640);
            int added = mapModel.getUserPoints().size() - before;
            // Update counter
            userPointCounter = mapModel.getUserPoints().size();
            refresh("+ " + added + " random user points added.");
        } catch (Exception e) { err(e.getMessage()); }
    }

    private void removeUserPoint() {
        try {
            if (mapModel.getUserPoints().isEmpty()) {
                err("No user points on the map."); return;
            }
            StringBuilder list = new StringBuilder("User points:\n");
            for (UserPoint up : mapModel.getUserPoints())
                list.append("  ").append(up.getId()).append("  (")
                        .append((int)up.getPosition().getX()).append(",")
                        .append((int)up.getPosition().getY()).append(")\n");
            mapCanvas.showStats(list.toString());
            String id = ask("User point ID to remove");
            UserPoint found = null;
            for (UserPoint up : mapModel.getUserPoints())
                if (up.getId().equalsIgnoreCase(id)) { found = up; break; }
            if (found == null) { err("User point not found: " + id); return; }
            mapModel.removeUserPoint(found);
            mapCanvas.closeStats();
            refresh("x User point removed: " + id);
        } catch (Exception e) { err(e.getMessage()); }
    }

    private void stats() {
        StringBuilder sb = new StringBuilder();
        sb.append("── STATISTICS ─────────────────────────\n");
        sb.append("  Hospitals       : ").append(mapModel.getHospitals().size()).append("\n");
        sb.append("  Collection ctrs : ").append(mapModel.getCollectionCenters().size()).append("\n");
        sb.append("  Drone bases     : ").append(mapModel.getDroneBases().size()).append("\n");
        sb.append("  Drones          : ").append(mapModel.getDrones().size()).append("\n");
        sb.append("  Voronoi cells   : ").append(mapModel.getVoronoiDiagram().getCells().size()).append("\n");
        sb.append("  Delaunay tri.   : ").append(mapModel.getDelaunayTriangulation().getTriangles().size()).append("\n");
        sb.append("\n── VORONOI ZONES ──────────────────────\n");
        for (var cell : mapModel.getVoronoiDiagram().getCells()) {
            sb.append(String.format("  %-20s  surface:%5.0f  density:%.4f\n",
                    cell.getOwner().getName(), cell.getSurface(), cell.getDensity()));
        }
        // Afficher sur le canvas, pas dans un terminal
        mapCanvas.showStats(sb.toString());
    }

    private void findNearest() {
        try {
            double x = askD("X coordinate");
            double y = askD("Y coordinate");
            MedicalSite s = mapModel.getVoronoiDiagram().getNearestSite(new Position(x, y));
            log(s == null ? "  No site found."
                    : "🔍  Nearest site: [" + s.getId() + "]  " + s.getName());
        } catch (Exception e) { err(e.getMessage()); }
    }

    /**
     * Creates a mission.
     *
     * The user enters:
     *   1. The hospital ID that issues the request (= requester & destination)
     *   2. The collection center ID (= organ origin)
     *   3. The organ type
     *
     * The system automatically selects the nearest available drone from its base.
     */
    private void createMission() {
        try {
            // List available hospitals
            StringBuilder hList = new StringBuilder("\n── Available hospitals ────────\n");
            for (Hospital h : mapModel.getHospitals())
                hList.append("  ").append(h.getId()).append("  ->  ").append(h.getName()).append("\n");

            // List available collection centers
            StringBuilder cList = new StringBuilder("── Available collection centers ──\n");
            for (CollectionCenter c : mapModel.getCollectionCenters())
                cList.append("  ").append(c.getId()).append("  ->  ").append(c.getName()).append("\n");

            log(hList.toString());
            log(cList.toString());

            String hospId   = ask("Hospital ID issuing the request (requester + destination)");
            String centerId = ask("Collection center ID (organ origin)");

            Hospital         hospital = findHospital(hospId);
            CollectionCenter center   = findCenter(centerId);

            if (hospital == null) { err("Hospital not found: " + hospId);           return; }
            if (center   == null) { err("Collection center not found: " + centerId); return; }

            String organ = ask("Organ type  (e.g. Kidney, Heart, Liver)");

            // Find optimal base BEFORE creating the request (for display)
            model.DroneBase optimalBase = optimizationService.findOptimalBase(center, hospital);
            if (optimalBase == null) { err("No drone base available."); return; }

            double scoreBase =
                    optimalBase.getPosition().distanceTo(center.getPosition())
                            + optimalBase.getPosition().distanceTo(hospital.getPosition());

            boolean adjacent = optimizationService.areDelaunayNeighbours(center, hospital);

            DeliveryRequest request = defaultDoctor.createDeliveryRequest(
                    center, hospital, organ, PriorityLevel.CRITICAL
            );

            currentMission = optimizationService.createMission(request, mapModel.getDrones());
            mapCanvas.setCurrentMission(currentMission);

            refresh(String.format(
                    "\n=== MISSION CREATED ===================================\n"
                            + "  Organ      : %s\n"
                            + "  From       : [%s] %s\n"
                            + "  To         : [%s] %s\n"
                            + "\n--- Optimal base selection (Voronoi zone coverage) ---\n"
                            + "  Base       : %s\n"
                            + "  Score      : %.1f  (dist_to_center + dist_to_hospital)\n"
                            + "\n--- Delaunay adjacency check -------------------------\n"
                            + "  Center & hospital are Delaunay neighbours: %s\n"
                            + "  -> %s\n"
                            + "\n--- Selected drone -----------------------------------\n"
                            + "  Drone      : %s  (battery: %d%%)\n"
                            + "  Route      : [%s] -> [%s] -> [%s]\n"
                            + "  Distance   : %.1f units\n"
                            + "=======================================================\n"
                            + "  Click 'Launch & Animate' to watch the drone fly.",
                    organ,
                    center.getId(),   center.getName(),
                    hospital.getId(), hospital.getName(),
                    optimalBase.getName(), scoreBase,
                    adjacent ? "YES" : "NO",
                    adjacent
                            ? "Direct flight confirmed as geometrically optimal by Delaunay."
                            : "No direct Delaunay edge — route goes through nearest Voronoi path.",
                    currentMission.getDrone().getId(),
                    (int) currentMission.getDrone().getBatteryLevel(),
                    optimalBase.getName(),
                    center.getName(),
                    hospital.getName(),
                    currentMission.getRoute().computeDistance()
            ));
        } catch (Exception e) { err(e.getMessage()); }
    }

    private void launchMission() {
        if (currentMission == null) { err("Create a mission first."); return; }
        currentMission.start();
        mapCanvas.startDroneAnimation();
        log("▶   Mission launched — drone is flying…  (watch the map)");
    }

    private void trackMission() {
        if (currentMission == null) { err("No current mission."); return; }
        log(String.format(
                "\n── MISSION TRACKING ─────────────────\n"
                        + "  ID       : %s\n"
                        + "  Status   : %s\n"
                        + "  Drone    : %s\n"
                        + "  Battery  : %.0f%%\n"
                        + "  Position : (%.0f, %.0f)\n"
                        + "  Reception: %s",
                currentMission.getId(),
                currentMission.getStatus(),
                currentMission.getDrone().getId(),
                currentMission.getBatteryLevel(),
                currentMission.getCurrentPosition().getX(),
                currentMission.getCurrentPosition().getY(),
                currentMission.isReceptionConfirmed() ? "Confirmed ✔" : "Pending"
        ));
    }

    // completeMission() removed from UI — drone completes automatically after animation

    private void cancelMission() {
        if (currentMission == null) { err("No current mission."); return; }
        currentMission.cancel();
        // Clear route + drone from canvas
        mapCanvas.cancelMission();
        log("✕   Mission cancelled — route and drone cleared.");
        currentMission = null;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Hospital findHospital(String id) {
        for (Hospital h : mapModel.getHospitals())
            if (h.getId().equalsIgnoreCase(id)) return h;
        return null;
    }

    private CollectionCenter findCenter(String id) {
        for (CollectionCenter c : mapModel.getCollectionCenters())
            if (c.getId().equalsIgnoreCase(id)) return c;
        return null;
    }

    private String ask(String prompt) {
        TextInputDialog d = new TextInputDialog();
        d.setTitle("Input required");
        d.setHeaderText(null);
        d.setContentText(prompt + ":");
        Optional<String> r = d.showAndWait();
        if (r.isEmpty() || r.get().isBlank())
            throw new IllegalArgumentException("Input cannot be empty.");
        return r.get().trim();
    }

    private double askD(String prompt) {
        try { return Double.parseDouble(ask(prompt)); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("Invalid number."); }
    }

    private void refresh(String msg) { mapCanvas.draw(); mapCanvas.showStats(msg); }
    private void log(String msg)     { mapCanvas.showStats(msg); }

    private void err(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Error"); a.setHeaderText(null); a.setContentText(msg);
        a.showAndWait();
        log("⚠   " + msg);
    }

    private void info(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg);
        a.showAndWait();
    }
}