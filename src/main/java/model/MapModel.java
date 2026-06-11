package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Central model of the application map.
 * It stores medical sites, user points, drone bases, drones,
 * and spatial structures: Voronoi diagram and Delaunay triangulation.
 */
public class MapModel implements Serializable {
    private static final long serialVersionUID = 1L;

    private final List<MedicalSite> medicalSites;
    private final List<UserPoint> userPoints;
    private final List<DroneBase> droneBases;
    private final List<Drone> drones;

    private final VoronoiDiagram voronoiDiagram;
    private final DelaunayTriangulation delaunayTriangulation;

    public MapModel() {
        this.medicalSites = new ArrayList<>();
        this.userPoints = new ArrayList<>();
        this.droneBases = new ArrayList<>();
        this.drones = new ArrayList<>();
        this.voronoiDiagram = new VoronoiDiagram();
        this.delaunayTriangulation = new DelaunayTriangulation();
    }

    public void addMedicalSite(MedicalSite site) {
        medicalSites.add(Objects.requireNonNull(site, "site cannot be null"));
        updateDiagrams();
        updateUserPointAssignments();
    }

    public void removeMedicalSite(MedicalSite site) {
        medicalSites.remove(site);
        updateDiagrams();
        updateUserPointAssignments();
    }

    public void moveMedicalSite(MedicalSite site, Position newPosition) {
        Objects.requireNonNull(site, "site cannot be null");
        site.updatePosition(newPosition);
        updateDiagrams();
        updateUserPointAssignments();
    }

    public void addUserPoint(UserPoint userPoint) {
        Objects.requireNonNull(userPoint, "userPoint cannot be null");
        MedicalSite nearest = voronoiDiagram.getNearestSite(userPoint.getPosition());
        userPoint.assignNearestSite(nearest);
        userPoints.add(userPoint);
        updateVoronoiStatistics();
    }

    public void removeUserPoint(UserPoint userPoint) {
        userPoints.remove(userPoint);
        updateVoronoiStatistics();
    }

    public void moveUserPoint(UserPoint userPoint, Position newPosition) {
        Objects.requireNonNull(userPoint, "userPoint cannot be null");
        userPoint.updatePosition(newPosition);
        MedicalSite nearest = voronoiDiagram.getNearestSite(newPosition);
        userPoint.assignNearestSite(nearest);
        updateVoronoiStatistics();
    }

    public void addRandomUserPoints(int count, double minX, double minY, double maxX, double maxY) {
        if (count <= 0) {
            return;
        }

        Random random = new Random();

        for (int i = 0; i < count; i++) {
            double x = minX + random.nextDouble() * (maxX - minX);
            double y = minY + random.nextDouble() * (maxY - minY);

            UserPoint point = new UserPoint(
                    "UP-" + System.currentTimeMillis() + "-" + i,
                    new Position(x, y)
            );

            addUserPoint(point);
        }
    }

    public void addDroneBase(DroneBase base) {
        droneBases.add(Objects.requireNonNull(base, "base cannot be null"));
    }

    public void removeDroneBase(DroneBase base) {
        droneBases.remove(base);
    }

    public void addDrone(Drone drone) {
        drones.add(Objects.requireNonNull(drone, "drone cannot be null"));
    }

    public void removeDrone(Drone drone) {
        drones.remove(drone);
    }

    /**
     * Recomputes Voronoi and Delaunay structures after map modifications.
     */
    public void updateDiagrams() {
        voronoiDiagram.compute(medicalSites);
        delaunayTriangulation.compute(medicalSites);
        updateVoronoiStatistics();
    }

    /**
     * Reassigns each user point to the nearest medical site.
     */
    public void updateUserPointAssignments() {
        for (UserPoint userPoint : userPoints) {
            MedicalSite nearest = voronoiDiagram.getNearestSite(userPoint.getPosition());
            userPoint.assignNearestSite(nearest);
        }

        updateVoronoiStatistics();
    }

    /**
     * Updates statistics in Voronoi cells using the user points.
     */
    private void updateVoronoiStatistics() {
        for (VoronoiCell cell : voronoiDiagram.getCells()) {
            cell.resetUserPoints();
        }

        for (UserPoint userPoint : userPoints) {
            MedicalSite nearest = userPoint.getNearestSite();

            if (nearest == null) {
                nearest = voronoiDiagram.getNearestSite(userPoint.getPosition());
                userPoint.assignNearestSite(nearest);
            }

            VoronoiCell cell = voronoiDiagram.getCellBySite(nearest);

            if (cell != null) {
                cell.addUserPoint(userPoint);
            }
        }

        for (VoronoiCell cell : voronoiDiagram.getCells()) {
            cell.computeDensity();
        }
    }

    public MedicalSite findMedicalSiteById(String id) {
        for (MedicalSite site : medicalSites) {
            if (site.getId().equals(id)) {
                return site;
            }
        }

        return null;
    }

    public UserPoint findUserPointById(String id) {
        for (UserPoint point : userPoints) {
            if (point.getId().equals(id)) {
                return point;
            }
        }

        return null;
    }

    public DroneBase findDroneBaseById(String id) {
        for (DroneBase base : droneBases) {
            if (base.getId().equals(id)) {
                return base;
            }
        }

        return null;
    }

    public List<Hospital> getHospitals() {
        List<Hospital> hospitals = new ArrayList<>();

        for (MedicalSite site : medicalSites) {
            if (site instanceof Hospital) {
                hospitals.add((Hospital) site);
            }
        }

        return hospitals;
    }

    public List<CollectionCenter> getCollectionCenters() {
        List<CollectionCenter> centers = new ArrayList<>();

        for (MedicalSite site : medicalSites) {
            if (site instanceof CollectionCenter) {
                centers.add((CollectionCenter) site);
            }
        }

        return centers;
    }

    public List<MedicalSite> getMedicalSites() {
        return new ArrayList<>(medicalSites);
    }

    public List<UserPoint> getUserPoints() {
        return new ArrayList<>(userPoints);
    }

    public List<DroneBase> getDroneBases() {
        return new ArrayList<>(droneBases);
    }

    public List<Drone> getDrones() {
        return new ArrayList<>(drones);
    }

    public VoronoiDiagram getVoronoiDiagram() {
        return voronoiDiagram;
    }

    public DelaunayTriangulation getDelaunayTriangulation() {
        return delaunayTriangulation;
    }
}