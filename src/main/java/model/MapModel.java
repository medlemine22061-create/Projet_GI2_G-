package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Central model of the application map.
 * It stores medical sites, drone bases, drones, and spatial structures.
 */
public class MapModel implements Serializable {
    private static final long serialVersionUID = 1L;

    private final List<MedicalSite> medicalSites;
    private final List<DroneBase> droneBases;
    private final List<Drone> drones;

    private final VoronoiDiagram voronoiDiagram;
    private final DelaunayTriangulation delaunayTriangulation;

    public MapModel() {
        this.medicalSites = new ArrayList<>();
        this.droneBases = new ArrayList<>();
        this.drones = new ArrayList<>();
        this.voronoiDiagram = new VoronoiDiagram();
        this.delaunayTriangulation = new DelaunayTriangulation();
    }

    public void addMedicalSite(MedicalSite site) {
        medicalSites.add(Objects.requireNonNull(site, "site cannot be null"));
        updateDiagrams();
    }

    public void removeMedicalSite(MedicalSite site) {
        medicalSites.remove(site);
        updateDiagrams();
    }

    public void moveMedicalSite(MedicalSite site, Position newPosition) {
        Objects.requireNonNull(site, "site cannot be null");
        site.updatePosition(newPosition);
        updateDiagrams();
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
    }

    public List<MedicalSite> getMedicalSites() {
        return new ArrayList<>(medicalSites);
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