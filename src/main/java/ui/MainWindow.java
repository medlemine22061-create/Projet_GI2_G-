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
    private Mission currentMission;

    // ── Styles ────────────────────────────────────────────────────────────────
    private static final String ROOT_STYLE =
            "-fx-background-color: #2C2C2A;";
    private static final String SIDEBAR_STYLE =
            "-fx-background-color: #1E1E1C;"
                    + " -fx-border-color: #3A3A38; -fx-border-width: 0 1 0 0;";


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
        bar.setStyle("-fx-background-color: #161614;"
                + " -fx-border-color: #3A3A38; -fx-border-width: 0 0 1 0;");

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
        p.getChildren().add(btn("＋  Add Drone to Base",     "#8C64FF", e -> addDroneToBase()));
        p.getChildren().add(sep());

        // Map Data
        p.getChildren().add(section("MAP DATA"));
        p.getChildren().add(btn("⬆  Import CSV",             "#50C0FF", e -> importCsv()));
        p.getChildren().add(btn("💾  Export Map",             "#8C64FF", e -> exportMap()));
        p.getChildren().add(btn("📂  Import Map",             "#8C64FF", e -> importMap()));
        p.getChildren().add(sep());

        // Diagnostics
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
        String base = "-fx-background-color:#252523; -fx-text-fill:" + hex
                + "; -fx-font-family:'Monospace'; -fx-font-size:11px;"
                + " -fx-background-radius:5; -fx-cursor:hand;";
        String hover = "-fx-background-color:#3A3A38; -fx-text-fill:" + hex
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
            String id      = ask("Hospital ID  (e.g. H3)");
            String name    = ask("Hospital name");
            double x       = askD("X coordinate  (0-850)");
            double y       = askD("Y coordinate  (0-620)");
            int numDoctors = (int) askD("Number of doctors responsible for deliveries");

            model.Hospital newHospital = new model.Hospital(id, name, new Position(x, y), true);
            mapModel.addMedicalSite(newHospital);

            // Register hospital as UserPoint (numDoctors = number of user points)
            // Each doctor is a potential requester -> add N user points for this hospital
            for (int i = 0; i < numDoctors; i++) {
                UserPoint up = new UserPoint("U-" + id + "-D" + (i+1), new Position(x, y));
                mapModel.addUserPoint(up);
            }
            String nearest = mapModel.getVoronoiDiagram().getNearestSite(new Position(x, y)) != null
                    ? mapModel.getVoronoiDiagram().getNearestSite(new Position(x, y)).getName()
                    : "none";
            refresh("+ Hospital added: " + name
                    + "\n  Doctors registered: " + numDoctors
                    + "\n  Nearest collection center: " + nearest);
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
     * Adds a single drone to an existing drone base.
     * Shows an error if no base exists on the map.
     */
    private void addDroneToBase() {
        // Check if any base exists
        if (mapModel.getDroneBases().isEmpty()) {
            err("No drone base available on the map.\n"
                    + "Please add a drone base first using 'Add Drone Base'.");
            return;
        }

        try {
            // List available bases
            StringBuilder list = new StringBuilder("Available bases:\n");
            for (model.DroneBase b : mapModel.getDroneBases()) {
                int available = b.getAvailableDrones().size();
                int total     = b.getDrones().size();
                int capacity  = b.getCapacity();
                list.append("  ").append(b.getId())
                        .append("  ->  ").append(b.getName())
                        .append("  [").append(total).append("/").append(capacity).append(" drones]");
                if (total >= capacity)
                    list.append("  FULL");
                list.append("\n");
            }
            mapCanvas.showStats(list.toString());

            String baseId = ask("Base ID to add drone to");
            model.DroneBase base = null;
            for (model.DroneBase b : mapModel.getDroneBases())
                if (b.getId().equalsIgnoreCase(baseId)) { base = b; break; }

            if (base == null) {
                mapCanvas.closeStats();
                err("No base found with ID: " + baseId);
                return;
            }

            if (base.getDrones().size() >= base.getCapacity()) {
                mapCanvas.closeStats();
                err("Base [" + base.getName() + "] is full.\n"
                        + "Capacity: " + base.getCapacity() + " drones.\n"
                        + "Please increase the capacity or choose another base.");
                return;
            }

            // Drone info
            String droneId  = ask("Drone ID  (e.g. D4)");
            double autonomy = askD("Autonomy  km  (e.g. 1000)");
            double battery  = askD("Battery level  %  (0-100)");
            double payload  = askD("Max payload  kg  (e.g. 5)");
            double speed    = askD("Speed  km/h  (e.g. 60)");

            model.Drone drone = new model.Drone(droneId, autonomy, battery, payload, speed,
                    new Position(base.getPosition().getX(), base.getPosition().getY()));

            base.addDrone(drone);
            mapModel.addDrone(drone);

            mapCanvas.closeStats();
            refresh("+ Drone added: " + droneId
                    + "\n  Base    : " + base.getName()
                    + "\n  Battery : " + (int)battery + "%"
                    + "\n  Autonomy: " + (int)autonomy + " km"
                    + "\n  Drones in base: " + base.getDrones().size()
                    + "/" + base.getCapacity());

        } catch (Exception e) { mapCanvas.closeStats(); err(e.getMessage()); }
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
            // Default: current working directory
            String defaultPath = System.getProperty("user.home")
                    + java.io.File.separator + "medadrone_map.bin";

            javafx.scene.control.TextInputDialog d = new javafx.scene.control.TextInputDialog(defaultPath);
            d.setTitle("Export map");
            d.setHeaderText("Export path");
            d.setContentText("Save to:");
            java.util.Optional<String> result = d.showAndWait();
            if (result.isEmpty() || result.get().isBlank()) return;

            String path = result.get().trim();
            java.io.File file = new java.io.File(path);

            // Create parent directories if needed
            if (file.getParentFile() != null && !file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }

            importExportService.exportMap(mapModel, path);
            mapCanvas.showStats("Map exported successfully\n  Path: " + path);
        } catch (Exception e) {
            err("Export failed: " + e.getMessage()
                    + "\nTip: use a simple path like: "
                    + System.getProperty("user.home") + "/medadrone_map.bin");
        }
    }

    private void importMap() {
        try {
            String defaultPath = System.getProperty("user.home")
                    + java.io.File.separator + "medadrone_map.bin";

            javafx.scene.control.TextInputDialog d = new javafx.scene.control.TextInputDialog(defaultPath);
            d.setTitle("Import map");
            d.setHeaderText("Import path");
            d.setContentText("Load from:");
            java.util.Optional<String> result = d.showAndWait();
            if (result.isEmpty() || result.get().isBlank()) return;

            String path = result.get().trim();
            java.io.File file = new java.io.File(path);
            if (!file.exists()) {
                err("File not found: " + path
                        + "\nMake sure you exported the map first."); return;
            }

            importExportService.importMap(path);
            mapCanvas.showStats("Map imported successfully\n  Path: " + path
                    + "\n\nNote: hospitals and collection centers\n"
                    + "are fixed data in our system.\n"
                    + "Import loads drone bases and drones.");
        } catch (Exception e) {
            err("Import failed: " + e.getMessage());
        }
    }

    private void stats() {
        StringBuilder sb = new StringBuilder();
        sb.append("── GENERAL ────────────────────────────\n");
        sb.append("  Hospitals       : ").append(mapModel.getHospitals().size()).append("\n");
        sb.append("  Collection ctrs : ").append(mapModel.getCollectionCenters().size()).append("\n");
        sb.append("  Drone bases     : ").append(mapModel.getDroneBases().size()).append("\n");
        sb.append("  Drones          : ").append(mapModel.getDrones().size()).append("\n");
        sb.append("  Voronoi cells   : ").append(mapModel.getVoronoiDiagram().getCells().size()).append("\n");
        sb.append("  Delaunay tri.   : ").append(mapModel.getDelaunayTriangulation().getTriangles().size()).append("\n");
        sb.append("  User points     : ").append(mapModel.getUserPoints().size()).append("\n");

        // ── Distances centre-hopital ──────────────────────────────────────────
        sb.append("\n── CENTER-HOSPITAL DISTANCES ──────────\n");
        double minDist = Double.MAX_VALUE, maxDist = 0, sumDist = 0;
        int pairCount = 0;
        for (Hospital h : mapModel.getHospitals()) {
            for (CollectionCenter c : mapModel.getCollectionCenters()) {
                double d = h.getPosition().distanceTo(c.getPosition());
                if (d < minDist) minDist = d;
                if (d > maxDist) maxDist = d;
                sumDist += d;
                pairCount++;
            }
        }
        if (pairCount > 0) {
            sb.append(String.format("  Min distance    : %.1f units\n", minDist));
            sb.append(String.format("  Max distance    : %.1f units\n", maxDist));
            sb.append(String.format("  Avg distance    : %.1f units\n", sumDist / pairCount));
            sb.append("  Pairs analyzed  : ").append(pairCount).append("\n");
        } else {
            sb.append("  No hospital-center pairs found.\n");
        }

        // ── Voronoi zones ─────────────────────────────────────────────────────
        sb.append("\n── VORONOI ZONES ──────────────────────\n");
        for (var cell : mapModel.getVoronoiDiagram().getCells()) {
            sb.append(String.format("  %-18s  surface:%5.0f  userpts:%d\n",
                    cell.getOwner().getName(), cell.getSurface(), cell.getNumberOfUserPoints()));
        }

        // ── Doctors par hopital ───────────────────────────────────────────────
        sb.append("\n── DOCTORS PER HOSPITAL ───────────────\n");
        for (Hospital h : mapModel.getHospitals()) {
            long doctorCount = mapModel.getUserPoints().stream()
                    .filter(up -> up.getId().startsWith("U-" + h.getId() + "-D"))
                    .count();
            sb.append(String.format("  %-18s  doctors: %d\n",
                    h.getName(), doctorCount));
        }

        mapCanvas.showStats(sb.toString());
    }

    private void findNearest() {
        try {
            double x = askD("X coordinate  (0-850)");
            double y = askD("Y coordinate  (0-620)");
            Position pos = new Position(x, y);

            StringBuilder sb = new StringBuilder("── FIND NEAREST ───────────────────────\n");

            // Nearest collection center (via Voronoi)
            CollectionCenter nearestCenter = null;
            double minCenterDist = Double.MAX_VALUE;
            for (CollectionCenter c : mapModel.getCollectionCenters()) {
                double d = pos.distanceTo(c.getPosition());
                if (d < minCenterDist) { minCenterDist = d; nearestCenter = c; }
            }
            if (nearestCenter != null) {
                sb.append(String.format("  Nearest center  : [%s] %s  (%.1f units)\n",
                        nearestCenter.getId(), nearestCenter.getName(), minCenterDist));
            } else {
                sb.append("  No collection center found.\n");
            }

            // Optimal drone base (minimises dist_to_center + dist_to_pos)
            model.DroneBase optimalBase = null;
            double minScore = Double.MAX_VALUE;
            for (model.DroneBase b : mapModel.getDroneBases()) {
                if (b.getAvailableDrones().isEmpty()) continue;
                double score = b.getPosition().distanceTo(pos)
                        + (nearestCenter != null
                        ? b.getPosition().distanceTo(nearestCenter.getPosition()) : 0);
                if (score < minScore) { minScore = score; optimalBase = b; }
            }
            if (optimalBase != null) {
                sb.append(String.format("  Optimal base    : [%s] %s  (score=%.1f)\n",
                        optimalBase.getId(), optimalBase.getName(), minScore));
                sb.append("    Available drones: " + optimalBase.getAvailableDrones().size() + "\n");
            } else {
                sb.append("  No drone base available.\n");
            }

            mapCanvas.showStats(sb.toString());
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
    /**
     * Creates a mission.
     * The user provides ONLY the hospital ID.
     * The system automatically:
     *   1. Finds the nearest collection center (via Voronoi)
     *   2. Finds the optimal drone base (minimises dist_base->center + dist_base->hospital)
     *   3. Selects the best available drone in that base
     */
    private void createMission() {
        try {
            if (mapModel.getHospitals().isEmpty()) {
                err("No hospital on the map. Please add a hospital first."); return;
            }
            if (mapModel.getCollectionCenters().isEmpty()) {
                err("No collection center on the map."); return;
            }
            if (mapModel.getDroneBases().isEmpty()) {
                err("No drone base on the map. Please add a drone base first."); return;
            }

            // Show available hospitals on canvas
            StringBuilder hList = new StringBuilder("── Available hospitals ─────────\n");
            for (Hospital h : mapModel.getHospitals())
                hList.append("  [").append(h.getId()).append("]  ").append(h.getName()).append("\n");
            mapCanvas.showStats(hList.toString());

            String hospId = ask("Hospital ID issuing the request");
            Hospital hospital = findHospital(hospId);
            if (hospital == null) {
                mapCanvas.closeStats();
                err("Hospital not found: " + hospId); return;
            }

            // Auto-find nearest collection center via Voronoi
            CollectionCenter center = null;
            double minDist = Double.MAX_VALUE;
            for (CollectionCenter c : mapModel.getCollectionCenters()) {
                double d = hospital.getPosition().distanceTo(c.getPosition());
                if (d < minDist) { minDist = d; center = c; }
            }
            if (center == null) {
                mapCanvas.closeStats();
                err("No collection center found."); return;
            }

            String organ = ask("Organ type  (e.g. Kidney, Heart, Liver)");

            // Auto-find optimal base
            model.DroneBase optimalBase = optimizationService.findOptimalBase(center, hospital);
            if (optimalBase == null) {
                mapCanvas.closeStats();
                err("No drone base available or all drones are in mission."); return;
            }

            double scoreBase =
                    optimalBase.getPosition().distanceTo(center.getPosition())
                            + optimalBase.getPosition().distanceTo(hospital.getPosition());

            boolean adjacent = optimizationService.areDelaunayNeighbours(center, hospital);

            DeliveryRequest request = defaultDoctor.createDeliveryRequest(
                    center, hospital, organ, PriorityLevel.CRITICAL
            );

            currentMission = optimizationService.createMission(request, mapModel.getDrones());
            mapCanvas.setCurrentMission(currentMission);

            mapCanvas.showStats(String.format(
                    "=== MISSION CREATED ===========================\n"
                            + "  Hospital   : [%s] %s\n"
                            + "  Center     : [%s] %s  (auto-selected, %.1f units)\n"
                            + "  Organ      : %s\n"
                            + "\n  Optimal base  : %s  (score=%.1f)\n"
                            + "  Drone         : %s  (battery: %d%%)\n"
                            + "  Delaunay adj  : %s\n"
                            + "  Route dist    : %.1f units\n"
                            + "==============================================\n"
                            + "  Click Launch & Animate to start the mission.",
                    hospital.getId(), hospital.getName(),
                    center.getId(),   center.getName(), minDist,
                    organ,
                    optimalBase.getName(), scoreBase,
                    currentMission.getDrone().getId(),
                    (int) currentMission.getDrone().getBatteryLevel(),
                    adjacent ? "YES (direct route optimal)" : "NO",
                    currentMission.getRoute().computeDistance()
            ));
        } catch (Exception e) { mapCanvas.closeStats(); err(e.getMessage()); }
    }

    private void launchMission() {
        if (currentMission == null) { err("Create a mission first."); return; }
        currentMission.start();
        mapCanvas.startDroneAnimation();
        log("▶   Mission launched — drone is flying…  (watch the map)");
    }

    private void trackMission() {
        if (currentMission == null) { err("No current mission."); return; }

        Position pos = currentMission.getCurrentPosition();
        double   bat = currentMission.getBatteryLevel();

        // Simulate battery consumption based on distance travelled
        double totalDist = currentMission.getRoute().computeDistance();
        double drone_speed = currentMission.getDrone().getAutonomy();
        double batteryConsumed = totalDist > 0 && drone_speed > 0
                ? (totalDist / drone_speed) * 100.0 : 0;
        double currentBattery = Math.max(0, bat - batteryConsumed);

        // Battery bar visual (10 blocks)
        int filled = (int)(currentBattery / 10);
        StringBuilder bar = new StringBuilder("  [");
        for (int i = 0; i < 10; i++) bar.append(i < filled ? "█" : "░");
        bar.append("]  ").append(String.format("%.0f%%", currentBattery));

        String batteryStatus = currentBattery > 60 ? "GOOD"
                : currentBattery > 30 ? "LOW"
                : "CRITICAL";

        mapCanvas.showStats(String.format(
                "── MISSION TRACKING ──────────────────\n"
                        + "  Mission ID  : %s\n"
                        + "  Status      : %s\n"
                        + "  Drone       : %s\n"
                        + "\n── LIVE POSITION ──────────────────────\n"
                        + "  X = %.1f   Y = %.1f\n"
                        + "\n── BATTERY ────────────────────────────\n"
                        + "%s\n"
                        + "  Status      : %s\n"
                        + "\n── MISSION INFO ───────────────────────\n"
                        + "  Route dist  : %.1f units\n"
                        + "  Reception   : %s",
                currentMission.getId(),
                currentMission.getStatus(),
                currentMission.getDrone().getId(),
                pos != null ? pos.getX() : 0,
                pos != null ? pos.getY() : 0,
                bar.toString(),
                batteryStatus,
                totalDist,
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

    private void refresh(String msg) { mapCanvas.draw(); }
    private void log(String msg)     { /* messages go to canvas panels only when relevant */ }

    private void err(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Error"); a.setHeaderText(null); a.setContentText(msg);
        a.showAndWait();
        // Do NOT call showStats on error — stats panel is for statistics only
    }

    private void info(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg);
        a.showAndWait();
    }
}