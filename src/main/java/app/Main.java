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
import model.Triangle;
import model.UserPoint;
import model.VoronoiCell;
import model.enums.PriorityLevel;
import service.ImportExportService;
import service.OptimizationService;

import java.util.Arrays;
import java.util.Scanner;

/**
 * Complete command line version.
 * It is used to test the model independently from JavaFX.
 */
public class Main {

    private static MapModel map;
    private static OptimizationService optimizationService;
    private static ImportExportService importExportService;
    private static MedicalStaff doctor;
    private static Mission currentMission;

    public static void main(String[] args) {
        initializeDemoData();
        launchMenu();
    }

    private static void initializeDemoData() {
        map = new MapModel();
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

        map.addMedicalSite(hospitalA);
        map.addMedicalSite(hospitalB);
        map.addMedicalSite(centerA);
        map.addMedicalSite(centerB);

        map.addUserPoint(new UserPoint("UP1", new Position(120, 130)));
        map.addUserPoint(new UserPoint("UP2", new Position(210, 470)));
        map.addUserPoint(new UserPoint("UP3", new Position(520, 320)));

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

    private static void launchMenu() {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("\n=== ORGAN DRONE DELIVERY - COMPLETE CMD ===");
            System.out.println("1. Display medical sites");
            System.out.println("2. Add hospital");
            System.out.println("3. Add collection center");
            System.out.println("4. Remove medical site");
            System.out.println("5. Move medical site");
            System.out.println("6. Import medical sites from CSV");

            System.out.println("7. Display user points");
            System.out.println("8. Add user point");
            System.out.println("9. Add random user points");
            System.out.println("10. Remove user point");
            System.out.println("11. Move user point");

            System.out.println("12. Display drone bases");
            System.out.println("13. Add drone base");
            System.out.println("14. Add drone");
            System.out.println("15. Display drones");

            System.out.println("16. Display Voronoi cells");
            System.out.println("17. Inspect Voronoi cell");
            System.out.println("18. Find nearest medical site");

            System.out.println("19. Display Delaunay triangles");
            System.out.println("20. Inspect Delaunay triangle");

            System.out.println("21. Display global statistics");

            System.out.println("22. Create delivery request");
            System.out.println("23. Create mission automatically");
            System.out.println("24. Start current mission");
            System.out.println("25. Track current mission");
            System.out.println("26. Complete current mission");
            System.out.println("27. Cancel current mission");

            System.out.println("28. Export complete map");
            System.out.println("29. Import complete map");

            System.out.println("0. Exit");
            System.out.print("Choice: ");

            String choice = scanner.nextLine();

            try {
                switch (choice) {
                    case "1":
                        displayMedicalSites();
                        break;
                    case "2":
                        addHospital(scanner);
                        break;
                    case "3":
                        addCollectionCenter(scanner);
                        break;
                    case "4":
                        removeMedicalSite(scanner);
                        break;
                    case "5":
                        moveMedicalSite(scanner);
                        break;
                    case "6":
                        importMedicalSitesFromCsv(scanner);
                        break;
                    case "7":
                        displayUserPoints();
                        break;
                    case "8":
                        addUserPoint(scanner);
                        break;
                    case "9":
                        addRandomUserPoints(scanner);
                        break;
                    case "10":
                        removeUserPoint(scanner);
                        break;
                    case "11":
                        moveUserPoint(scanner);
                        break;
                    case "12":
                        displayDroneBases();
                        break;
                    case "13":
                        addDroneBase(scanner);
                        break;
                    case "14":
                        addDrone(scanner);
                        break;
                    case "15":
                        displayDrones();
                        break;
                    case "16":
                        displayVoronoi();
                        break;
                    case "17":
                        inspectVoronoiCell(scanner);
                        break;
                    case "18":
                        findNearestSite(scanner);
                        break;
                    case "19":
                        displayDelaunay();
                        break;
                    case "20":
                        inspectDelaunayTriangle(scanner);
                        break;
                    case "21":
                        displayGlobalStatistics();
                        break;
                    case "22":
                        createDeliveryRequest(scanner);
                        break;
                    case "23":
                        createAutomaticMission(scanner);
                        break;
                    case "24":
                        startCurrentMission();
                        break;
                    case "25":
                        trackCurrentMission();
                        break;
                    case "26":
                        completeCurrentMission();
                        break;
                    case "27":
                        cancelCurrentMission();
                        break;
                    case "28":
                        exportMap(scanner);
                        break;
                    case "29":
                        importMap(scanner);
                        break;
                    case "0":
                        running = false;
                        break;
                    default:
                        System.out.println("Invalid choice.");
                }
            } catch (Exception e) {
                System.out.println("Handled error: " + e.getMessage());
            }
        }

        scanner.close();
    }

    private static void displayMedicalSites() {
        System.out.println("\nMedical sites:");

        for (MedicalSite site : map.getMedicalSites()) {
            System.out.println("- " + site.getId()
                    + " | " + site.getName()
                    + " | " + site.getClass().getSimpleName()
                    + " | position=(" + site.getPosition().getX()
                    + ", " + site.getPosition().getY() + ")");
        }
    }

    private static void addHospital(Scanner scanner) {
        System.out.print("Hospital id: ");
        String id = scanner.nextLine();

        System.out.print("Hospital name: ");
        String name = scanner.nextLine();

        double x = readDouble(scanner, "x: ");
        double y = readDouble(scanner, "y: ");

        map.addMedicalSite(new Hospital(id, name, new Position(x, y), true));
        System.out.println("Hospital added.");
    }

    private static void addCollectionCenter(Scanner scanner) {
        System.out.print("Collection center id: ");
        String id = scanner.nextLine();

        System.out.print("Collection center name: ");
        String name = scanner.nextLine();

        double x = readDouble(scanner, "x: ");
        double y = readDouble(scanner, "y: ");

        System.out.print("Organ types separated by comma: ");
        String organsInput = scanner.nextLine();

        map.addMedicalSite(new CollectionCenter(
                id,
                name,
                new Position(x, y),
                Arrays.asList(organsInput.split(","))
        ));

        System.out.println("Collection center added.");
    }

    private static void removeMedicalSite(Scanner scanner) {
        displayMedicalSites();

        System.out.print("Medical site id to remove: ");
        String id = scanner.nextLine();

        MedicalSite site = map.findMedicalSiteById(id);

        if (site == null) {
            System.out.println("Medical site not found.");
            return;
        }

        map.removeMedicalSite(site);
        System.out.println("Medical site removed.");
    }

    private static void moveMedicalSite(Scanner scanner) {
        displayMedicalSites();

        System.out.print("Medical site id to move: ");
        String id = scanner.nextLine();

        MedicalSite site = map.findMedicalSiteById(id);

        if (site == null) {
            System.out.println("Medical site not found.");
            return;
        }

        double x = readDouble(scanner, "New x: ");
        double y = readDouble(scanner, "New y: ");

        map.moveMedicalSite(site, new Position(x, y));
        System.out.println("Medical site moved.");
    }

    private static void importMedicalSitesFromCsv(Scanner scanner) {
        System.out.print("CSV file path: ");
        String path = scanner.nextLine();

        try {
            importExportService.importMedicalSitesFromCsv(map, path);
            System.out.println("Medical sites imported from CSV.");
        } catch (Exception e) {
            System.out.println("CSV import error: " + e.getMessage());
        }
    }

    private static void displayUserPoints() {
        System.out.println("\nUser points:");

        for (UserPoint point : map.getUserPoints()) {
            System.out.println("- " + point);
        }
    }

    private static void addUserPoint(Scanner scanner) {
        System.out.print("User point id: ");
        String id = scanner.nextLine();

        double x = readDouble(scanner, "x: ");
        double y = readDouble(scanner, "y: ");

        map.addUserPoint(new UserPoint(id, new Position(x, y)));
        System.out.println("User point added and linked to nearest medical site.");
    }

    private static void addRandomUserPoints(Scanner scanner) {
        int count = readInt(scanner, "Number of random user points: ");

        double minX = readDouble(scanner, "minX: ");
        double minY = readDouble(scanner, "minY: ");
        double maxX = readDouble(scanner, "maxX: ");
        double maxY = readDouble(scanner, "maxY: ");

        map.addRandomUserPoints(count, minX, minY, maxX, maxY);
        System.out.println("Random user points added.");
    }

    private static void removeUserPoint(Scanner scanner) {
        displayUserPoints();

        System.out.print("User point id to remove: ");
        String id = scanner.nextLine();

        UserPoint point = map.findUserPointById(id);

        if (point == null) {
            System.out.println("User point not found.");
            return;
        }

        map.removeUserPoint(point);
        System.out.println("User point removed.");
    }

    private static void moveUserPoint(Scanner scanner) {
        displayUserPoints();

        System.out.print("User point id to move: ");
        String id = scanner.nextLine();

        UserPoint point = map.findUserPointById(id);

        if (point == null) {
            System.out.println("User point not found.");
            return;
        }

        double x = readDouble(scanner, "New x: ");
        double y = readDouble(scanner, "New y: ");

        map.moveUserPoint(point, new Position(x, y));
        System.out.println("User point moved and relinked to nearest site.");
    }

    private static void displayDroneBases() {
        System.out.println("\nDrone bases:");

        for (DroneBase base : map.getDroneBases()) {
            System.out.println("- " + base.getId()
                    + " | " + base.getName()
                    + " | position=(" + base.getPosition().getX()
                    + ", " + base.getPosition().getY() + ")"
                    + " | capacity=" + base.getCapacity());
        }
    }

    private static void addDroneBase(Scanner scanner) {
        System.out.print("Drone base id: ");
        String id = scanner.nextLine();

        System.out.print("Drone base name: ");
        String name = scanner.nextLine();

        double x = readDouble(scanner, "x: ");
        double y = readDouble(scanner, "y: ");
        int capacity = readInt(scanner, "capacity: ");

        map.addDroneBase(new DroneBase(id, name, new Position(x, y), capacity));
        System.out.println("Drone base added.");
    }

    private static void addDrone(Scanner scanner) {
        displayDroneBases();

        System.out.print("Drone base id: ");
        String baseId = scanner.nextLine();

        DroneBase base = map.findDroneBaseById(baseId);

        if (base == null) {
            System.out.println("Drone base not found.");
            return;
        }

        System.out.print("Drone id: ");
        String id = scanner.nextLine();

        double autonomy = readDouble(scanner, "Autonomy: ");
        double battery = readDouble(scanner, "Battery level: ");
        double payload = readDouble(scanner, "Max payload: ");
        double speed = readDouble(scanner, "Speed: ");
        double x = readDouble(scanner, "x: ");
        double y = readDouble(scanner, "y: ");

        Drone drone = new Drone(id, autonomy, battery, payload, speed, new Position(x, y));

        base.addDrone(drone);
        map.addDrone(drone);

        System.out.println("Drone added.");
    }

    private static void displayDrones() {
        System.out.println("\nDrones:");

        for (Drone drone : map.getDrones()) {
            System.out.println("- " + drone.getId()
                    + " | status=" + drone.getStatus()
                    + " | battery=" + drone.getBatteryLevel()
                    + " | autonomy=" + drone.getAutonomy()
                    + " | position=(" + drone.getPosition().getX()
                    + ", " + drone.getPosition().getY() + ")");
        }
    }

    private static void displayVoronoi() {
        System.out.println("\nVoronoi cells:");

        for (VoronoiCell cell : map.getVoronoiDiagram().getCells()) {
            System.out.println("- Owner: " + cell.getOwner().getName()
                    + " | grid points=" + cell.getPoints().size()
                    + " | user points=" + cell.getNumberOfUserPoints()
                    + " | surface=" + cell.getSurface()
                    + " | density=" + cell.getDensity());
        }
    }

    private static void inspectVoronoiCell(Scanner scanner) {
        displayMedicalSites();

        System.out.print("Medical site id of the cell to inspect: ");
        String id = scanner.nextLine();

        MedicalSite site = map.findMedicalSiteById(id);

        if (site == null) {
            System.out.println("Medical site not found.");
            return;
        }

        VoronoiCell cell = map.getVoronoiDiagram().getCellBySite(site);

        if (cell == null) {
            System.out.println("Voronoi cell not found.");
            return;
        }

        System.out.println("\nVoronoi cell of " + site.getName());
        System.out.println("Surface: " + cell.getSurface());
        System.out.println("Density: " + cell.getDensity());
        System.out.println("User points: " + cell.getNumberOfUserPoints());
        System.out.println("Min distance: " + cell.getMinDistanceToUserPoints());
        System.out.println("Max distance: " + cell.getMaxDistanceToUserPoints());
        System.out.println("Average distance: " + cell.getAverageDistanceToUserPoints());
    }

    private static void findNearestSite(Scanner scanner) {
        double x = readDouble(scanner, "x: ");
        double y = readDouble(scanner, "y: ");

        MedicalSite nearest = map.getVoronoiDiagram().getNearestSite(new Position(x, y));

        if (nearest == null) {
            System.out.println("No nearest site found.");
        } else {
            System.out.println("Nearest site: " + nearest.getId() + " | " + nearest.getName());
        }
    }

    private static void displayDelaunay() {
        System.out.println("\nDelaunay triangles:");

        int index = 0;

        for (Triangle triangle : map.getDelaunayTriangulation().getTriangles()) {
            System.out.println(index + ". " + triangle);
            index++;
        }
    }

    private static void inspectDelaunayTriangle(Scanner scanner) {
        displayDelaunay();

        int index = readInt(scanner, "Triangle index: ");

        if (index < 0 || index >= map.getDelaunayTriangulation().getTriangles().size()) {
            System.out.println("Invalid triangle index.");
            return;
        }

        Triangle triangle = map.getDelaunayTriangulation().getTriangles().get(index);

        System.out.println("\nTriangle information:");
        System.out.println(triangle);
        System.out.println("Surface: " + triangle.computeSurface());
        System.out.println("Distance AB: " + triangle.getDistanceAB());
        System.out.println("Distance BC: " + triangle.getDistanceBC());
        System.out.println("Distance CA: " + triangle.getDistanceCA());
        System.out.println("Circumcenter: (" + triangle.getCircumcenter().getX()
                + ", " + triangle.getCircumcenter().getY() + ")");

        for (MedicalSite site : triangle.getSites()) {
            VoronoiCell cell = map.getVoronoiDiagram().getCellBySite(site);
            int count = cell == null ? 0 : cell.getNumberOfUserPoints();
            System.out.println("User points near " + site.getName() + ": " + count);
        }
    }

    private static void displayGlobalStatistics() {
        System.out.println("\nGlobal statistics:");
        System.out.println("Medical sites: " + map.getMedicalSites().size());
        System.out.println("Hospitals: " + map.getHospitals().size());
        System.out.println("Collection centers: " + map.getCollectionCenters().size());
        System.out.println("User points: " + map.getUserPoints().size());
        System.out.println("Drone bases: " + map.getDroneBases().size());
        System.out.println("Drones: " + map.getDrones().size());
        System.out.println("Voronoi cells: " + map.getVoronoiDiagram().getCells().size());
        System.out.println("Delaunay triangles: " + map.getDelaunayTriangulation().getTriangles().size());

        double totalSurface = 0.0;

        for (VoronoiCell cell : map.getVoronoiDiagram().getCells()) {
            totalSurface += cell.getSurface();
        }

        System.out.println("Total Voronoi approximated surface: " + totalSurface);
    }

    private static void createDeliveryRequest(Scanner scanner) {
        CollectionCenter origin = chooseCollectionCenter(scanner);
        Hospital destination = chooseHospital(scanner);

        if (origin == null || destination == null) {
            return;
        }

        System.out.print("Organ type: ");
        String organType = scanner.nextLine();

        PriorityLevel priority = choosePriority(scanner);

        if (priority == null) {
            return;
        }

        DeliveryRequest request = doctor.createDeliveryRequest(origin, destination, organType, priority);

        System.out.println("Delivery request created:");
        System.out.println("Origin: " + request.getOrigin().getName());
        System.out.println("Destination: " + request.getDestination().getName());
        System.out.println("Organ: " + request.getOrganType());
        System.out.println("Priority: " + request.getPriorityLevel());
    }

    private static void createAutomaticMission(Scanner scanner) {
        CollectionCenter origin = chooseCollectionCenter(scanner);
        Hospital destination = chooseHospital(scanner);

        if (origin == null || destination == null) {
            return;
        }

        System.out.print("Organ type: ");
        String organType = scanner.nextLine();

        PriorityLevel priority = choosePriority(scanner);

        if (priority == null) {
            return;
        }

        DeliveryRequest request = doctor.createDeliveryRequest(origin, destination, organType, priority);
        currentMission = optimizationService.createMission(request, map.getDrones());

        System.out.println("Mission created.");
        System.out.println("Mission id: " + currentMission.getId());
        System.out.println("Drone: " + currentMission.getDrone().getId());
        System.out.println("Route distance: " + currentMission.getRoute().computeDistance());
        System.out.println("Estimated time: " + currentMission.getRoute().estimateTime(currentMission.getDrone()));
    }

    private static void startCurrentMission() {
        if (currentMission == null) {
            System.out.println("No current mission.");
            return;
        }

        currentMission.start();
        System.out.println("Mission started.");
    }

    private static void trackCurrentMission() {
        if (currentMission == null) {
            System.out.println("No current mission.");
            return;
        }

        System.out.println("Mission id: " + currentMission.getId());
        System.out.println("Status: " + currentMission.getStatus());
        System.out.println("Drone: " + currentMission.getDrone().getId());
        System.out.println("Current position: (" + currentMission.getCurrentPosition().getX()
                + ", " + currentMission.getCurrentPosition().getY() + ")");
        System.out.println("Battery: " + currentMission.getBatteryLevel());
        System.out.println("Reception confirmed: " + currentMission.isReceptionConfirmed());
    }

    private static void completeCurrentMission() {
        if (currentMission == null) {
            System.out.println("No current mission.");
            return;
        }

        currentMission.complete();
        currentMission.getRequest().getDestination().receiveMission(currentMission);
        doctor.validateReception(currentMission);

        System.out.println("Mission completed and reception confirmed.");
    }

    private static void cancelCurrentMission() {
        if (currentMission == null) {
            System.out.println("No current mission.");
            return;
        }

        currentMission.cancel();
        System.out.println("Mission cancelled.");
    }

    private static void exportMap(Scanner scanner) {
        System.out.print("Binary export file path, example map.bin: ");
        String path = scanner.nextLine();

        try {
            importExportService.exportMap(map, path);
            System.out.println("Map exported.");
        } catch (Exception e) {
            System.out.println("Export error: " + e.getMessage());
        }
    }

    private static void importMap(Scanner scanner) {
        System.out.print("Binary import file path, example map.bin: ");
        String path = scanner.nextLine();

        try {
            map = importExportService.importMap(path);
            optimizationService = new OptimizationService(map.getDelaunayTriangulation());
            System.out.println("Map imported.");
        } catch (Exception e) {
            System.out.println("Import error: " + e.getMessage());
        }
    }

    private static CollectionCenter chooseCollectionCenter(Scanner scanner) {
        System.out.println("\nCollection centers:");

        for (CollectionCenter center : map.getCollectionCenters()) {
            System.out.println("- " + center.getId() + " | " + center.getName());
        }

        System.out.print("Choose collection center id: ");
        String id = scanner.nextLine();

        MedicalSite site = map.findMedicalSiteById(id);

        if (site instanceof CollectionCenter) {
            return (CollectionCenter) site;
        }

        System.out.println("Collection center not found.");
        return null;
    }

    private static Hospital chooseHospital(Scanner scanner) {
        System.out.println("\nHospitals:");

        for (Hospital hospital : map.getHospitals()) {
            System.out.println("- " + hospital.getId() + " | " + hospital.getName());
        }

        System.out.print("Choose hospital id: ");
        String id = scanner.nextLine();

        MedicalSite site = map.findMedicalSiteById(id);

        if (site instanceof Hospital) {
            return (Hospital) site;
        }

        System.out.println("Hospital not found.");
        return null;
    }

    private static PriorityLevel choosePriority(Scanner scanner) {
        System.out.println("Priority: LOW, MEDIUM, HIGH, CRITICAL");
        System.out.print("Priority choice: ");
        String input = scanner.nextLine();

        try {
            return PriorityLevel.valueOf(input.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid priority.");
            return null;
        }
    }

    private static double readDouble(Scanner scanner, String message) {
        while (true) {
            try {
                System.out.print(message);
                return Double.parseDouble(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Try again.");
            }
        }
    }

    private static int readInt(Scanner scanner, String message) {
        while (true) {
            try {
                System.out.print(message);
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid integer. Try again.");
            }
        }
    }
}