package app;


import model.*;
import model.enums.PriorityLevel;
import service.OptimizationService;

import java.io.IOException;
import java.util.List;

/**
 * Simple command-line entry point used to test the model without JavaFX.
 */
public class Main {

    /**
     * Runs a minimal scenario to test the model.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        try {
            MapModel map = new MapModel();

            Hospital hospital = new Hospital(
                    "H1",
                    "Hospital A",
                    new Position(10, 10),
                    true
            );

            CollectionCenter center = new CollectionCenter(
                    "C1",
                    "Collection Center A",
                    new Position(0, 0),
                    List.of("heart", "kidney")
            );

            DroneBase base = new DroneBase(
                    "B1",
                    "Drone Base A",
                    new Position(1, 1),
                    5
            );

            Drone drone = new Drone(
                    "D1",
                    new Position(1, 1),
                    100.0,
                    5.0
            );

            base.addDrone(drone);

            map.addMedicalSite(hospital);
            map.addMedicalSite(center);
            map.addDroneBase(base);
            map.addDrone(drone);

            MedicalStaff doctor = new MedicalStaff(
                    "S1",
                    "Boukah",
                    "Boukah",
                    "Doctor",
                    "doctor@example.com",
                    "",
                    hospital
            );

            DeliveryRequest request = doctor.createDeliveryRequest(
                    center,
                    hospital,
                    "heart",
                    PriorityLevel.CRITICAL
            );

            OptimizationService optimizationService = new OptimizationService();

            Mission mission = optimizationService.createMission(
                    request,
                    map.getDrones()
            );

            mission.start();

            mission.updateTracking(
                    new Position(5, 5),
                    85.0,
                    4.0,
                    0.1
            );

            doctor.validateReception(mission);

            System.out.println("===== Mission result =====");
            System.out.println("Mission status: " + mission.getStatus());
            System.out.println("Priority: " + mission.getPriorityLevel());
            System.out.println("Reception confirmed: " + mission.isReceptionConfirmed());

            System.out.println();
            System.out.println("===== Mission history =====");
            for (String event : mission.getHistory()) {
                System.out.println(event);
            }

            System.out.println();
            System.out.println("===== Voronoi / Delaunay test =====");

            VoronoiDiagram voronoiDiagram = map.getVoronoiDiagram();
            System.out.println("Number of Voronoi cells: " + voronoiDiagram.getCells().size());

            for (VoronoiCell cell : voronoiDiagram.getCells()) {
                System.out.println("Cell owner: " + cell.getOwner().getName());
            }

            DelaunayTriangulation delaunayTriangulation = map.getDelaunayTriangulation();
            System.out.println("Number of Delaunay triangles: " + delaunayTriangulation.getTriangles().size());

            for (Triangle triangle : delaunayTriangulation.getTriangles()) {
                System.out.println("Triangle surface: " + triangle.computeSurface());
            }

            testExportImport(map);

        } catch (RuntimeException exception) {
            System.err.println("Application error: " + exception.getMessage());
        }
    }

    /**
     * Tests binary export and import of the map.
     *
     * @param map map to export and import
     */
    private static void testExportImport(MapModel map) {
        ImportExportService importExportService = new ImportExportService();
        String filePath = "map-save.bin";

        try {
            importExportService.exportMap(map, filePath);
            MapModel importedMap = importExportService.importMap(filePath);

            System.out.println();
            System.out.println("===== Import / Export test =====");
            System.out.println("Imported medical sites: " + importedMap.getMedicalSites().size());
            System.out.println("Imported drones: " + importedMap.getDrones().size());

        } catch (IOException | ClassNotFoundException exception) {
            System.err.println("Import/export error: " + exception.getMessage());
        }
    }
}