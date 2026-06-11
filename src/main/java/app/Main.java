package app;



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
import model.Route;
import model.Triangle;
import model.VoronoiCell;
import model.enums.PriorityLevel;
import service.ImportExportService;
import service.OptimizationService;

import java.util.Arrays;
import java.util.Scanner;

/**
 * Command line version used to test the model before JavaFX.
 */
public class Main {

    private static MapModel map;
    private static OptimizationService optimizationService;
    private static ImportExportService importExportService;

    private static Hospital hospitalA;
    private static Hospital hospitalB;
    private static CollectionCenter centerA;
    private static CollectionCenter centerB;
    private static MedicalStaff doctor;

    public static void main(String[] args) {
        initializeDemoData();

        if (args.length > 0 && args[0].equals("--menu")) {
            launchMenu();
        } else {
            launchDemoScenario();
        }
    }

    private static void initializeDemoData() {
        map = new MapModel();
        importExportService = new ImportExportService();

        hospitalA = new Hospital("H1", "Hospital Nord", new Position(100, 100), true);
        hospitalB = new Hospital("H2", "Hospital Sud", new Position(500, 300), true);

        centerA = new CollectionCenter(
                "C1",
                "Collection Center Est",
                new Position(200, 500),
                Arrays.asList("Heart", "Kidney", "Liver")
        );

        centerB = new CollectionCenter(
                "C2",
                "Collection Center Ouest",
                new Position(700, 150),
                Arrays.asList("Kidney", "Lung")
        );

        map.addMedicalSite(hospitalA);
        map.addMedicalSite(hospitalB);
        map.addMedicalSite(centerA);
        map.addMedicalSite(centerB);

        DroneBase base = new DroneBase("B1", "Main Drone Base", new Position(150, 150), 10);

        Drone drone1 = new Drone("D1", 1000, 90, 5, 60, new Position(120, 120));
        Drone drone2 = new Drone("D2", 700, 50, 5, 55, new Position(600, 200));
        Drone drone3 = new Drone("D3", 1200, 80, 5, 70, new Position(300, 450));

        base.addDrone(drone1);
        base.addDrone(drone2);
        base.addDrone(drone3);

        map.addDroneBase(base);
        map.addDrone(drone1);
        map.addDrone(drone2);
        map.addDrone(drone3);

        doctor = new MedicalStaff(
                "M1",
                "Yousef",
                "Boukah",
                "Doctor",
                "doctor@example.com",
                "0600000000",
                hospitalA
        );

        optimizationService = new OptimizationService(map.getDelaunayTriangulation());
    }

    private static void launchDemoScenario() {
        System.out.println("=== ORGAN DRONE DELIVERY - DEMO CMD ===");

        displayMedicalSites();
        displayVoronoi();
        displayDelaunay();

        DeliveryRequest request = doctor.createDeliveryRequest(
                centerA,
                hospitalA,
                "Kidney",
                PriorityLevel.CRITICAL
        );

        System.out.println("\nDelivery request created:");
        System.out.println("Organ: " + request.getOrganType());
        System.out.println("Priority: " + request.getPriorityLevel());

        Mission mission = optimizationService.createMission(request, map.getDrones());

        System.out.println("\nMission created:");
        Syst
        System.out.println("Drone: " + mission.getDrone().getId());
        System.out.println("Route distance: " + mission.getRoute().computeDistance());

        mission.start();
        mission.complete();
        hospitalA.receiveMission(mission);
        doctor.validateReception(mission);

        System.out.println("\nMission status: " + mission.getStatus());
        System.out.println("Reception confirmed: " + mission.isReceptionConfirmed());

        try {
            importExportService.exportMap(map, "map.bin");
            System.out.println("\nMap exported successfully to map.bin");

            MapModel importedMap = importExportService.importMap("map.bin");
            System.out.println("Map imported successfully.");
            System.out.println("Imported sites: " + importedMap.getMedicalSites().size());
        } catch (Exception e) {
            System.out.println("Import/export error: " + e.getMessage());
        }
    }

    private static void launchMenu() {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("\n=== MENU CMD ===");
            System.out.println("1. Display medical sites");
            System.out.println("2. Display Voronoi cells");
            System.out.println("3. Display Delaunay triangles");
            System.out.println("4. Find nearest medical site");
            System.out.println("5. Create demo mission");
            System.out.println("6. Export map");
            System.out.println("7. Import map");
            System.out.println("0. Exit");
            System.out.print("Choice: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    displayMedicalSites();
                    break;
                case "2":
                    displayVoronoi();
                    break;
                case "3":
                    displayDelaunay();
                    break;
                case "4":
                    findNearestSite(scanner);
                    break;
                case "5":
                    launchDemoScenario();
                    break;
                case "6":
                    exportMap();
                    break;
                case "7":
                    importMap();
                    break;
                case "0":
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        }

        scanner.close();
    }

    private static void displayMedicalSites() {
        System.out.println("\nMedical sites:");

        for (MedicalSite site : map.getMedicalSites()) {
            System.out.println("- " + site.getName()
                    + " at (" + site.getPosition().getX()
                    + ", " + site.getPosition().getY() + ")");
        }
    }

    private static void displayVoronoi() {
        System.out.println("\nVoronoi cells:");

        for (VoronoiCell cell : map.getVoronoiDiagram().getCells()) {
            System.out.println("- Owner: " + cell.getOwner().getName()
                    + ", points: " + cell.getPoints().size()
                    + ", surface: " + cell.getSurface()
                    + ", density: " + cell.getDensity());
        }
    }

    private static void displayDelaunay() {
        System.out.println("\nDelaunay triangles:");

        for (Triangle triangle : map.getDelaunayTriangulation().getTriangles()) {
            System.out.println("- " + triangle);
        }
    }

    private static void findNearestSite(Scanner scanner) {
        try {
            System.out.print("x = ");
            double x = Double.parseDouble(scanner.nextLine());

            System.out.print("y = ");
            double y = Double.parseDouble(scanner.nextLine());

            Position position = new Position(x, y);
            MedicalSite nearest = map.getVoronoiDiagram().getNearestSite(position);

            if (nearest == null) {
                System.out.println("No nearest site found.");
            } else {
                System.out.println("Nearest site: " + nearest.getName());
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid number.");
        }
    }

    private static void exportMap() {
        try {
            importExportService.exportMap(map, "map.bin");
            System.out.println("Map exported to map.bin");
        } catch (Exception e) {
            System.out.println("Export error: " + e.getMessage());
        }
    }

    private static void importMap() {
        try {
            map = importExportService.importMap("map.bin");
            optimizationService = new OptimizationService(map.getDelaunayTriangulation());
            System.out.println("Map imported from map.bin");
        } catch (Exception e) {
            System.out.println("Import error: " + e.getMessage());
        }
    }
}