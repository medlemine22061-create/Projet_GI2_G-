package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Central model of the application map.
 * It stores medical sites, drone bases, drones, and the spatial structures
 * used by the application: Voronoi diagram and Delaunay triangulation.
 */
public class MapModel implements Serializable {

    private static final long serialVersionUID = 1L;

    private final List<MedicalSite> medicalSites;
    private final List<DroneBase> droneBases;
    private final List<Drone> drones;
    private final VoronoiDiagram voronoiDiagram;
    private final DelaunayTriangulation delaunayTriangulation;

    /**
     * Creates an empty map model.
     */
    public MapModel() {
        this.medicalSites = new ArrayList<>();
        this.droneBases = new ArrayList<>();
        this.drones = new ArrayList<>();
        this.voronoiDiagram = new VoronoiDiagram();
        this.delaunayTriangulation = new DelaunayTriangulation();
    }

    /**
     * Adds a medical site to the map.
     * After adding the site, the Voronoi diagram and Delaunay triangulation
     * are updated automatically.
     *
     * @param site medical site to add
     */
    public void addMedicalSite(MedicalSite site) {
        medicalSites.add(Objects.requireNonNull(site, "site cannot be null"));
        updateDiagrams();
    }

    /**
     * Removes a medical site from the map.
     * After removing the site, the Voronoi diagram and Delaunay triangulation
     * are updated automatically.
     *
     * @param site medical site to remove
     */
    public void removeMedicalSite(MedicalSite site) {
        medicalSites.remove(site);
        updateDiagrams();
    }

    /**
     * Moves a medical site to a new position.
     * After moving the site, the Voronoi diagram and Delaunay triangulation
     * are updated automatically.
     *
     * @param site medical site to move
     * @param position new position
     */
    public void moveMedicalSite(MedicalSite site, Position position) {
        Objects.requireNonNull(site, "site cannot be null");
        site.updatePosition(position);
        updateDiagrams();
    }

    /**
     * Adds a drone base to the map.
     *
     * @param base drone base to add
     */
    public void addDroneBase(DroneBase base) {
        droneBases.add(Objects.requireNonNull(base, "base cannot be null"));
    }

    /**
     * Removes a drone base from the map.
     *
     * @param base drone base to remove
     */
    public void removeDroneBase(DroneBase base) {
        droneBases.remove(base);
    }

    /**
     * Adds a drone to the map.
     *
     * @param drone drone to add
     */
    public void addDrone(Drone drone) {
        drones.add(Objects.requireNonNull(drone, "drone cannot be null"));
    }

    /**
     * Removes a drone from the map.
     *
     * @param drone drone to remove
     */
    public void removeDrone(Drone drone) {
        drones.remove(drone);
    }

    /**
     * Updates the Voronoi diagram and the Delaunay triangulation.
     * This method must be called after adding, removing, or moving medical sites.
     */
    public void updateDiagrams() {
        voronoiDiagram.compute(medicalSites);
        delaunayTriangulation.compute(medicalSites);
    }

    /**
     * Returns the medical sites of the map.
     *
     * @return copy of the medical sites list
     */
    public List<MedicalSite> getMedicalSites() {
        return new ArrayList<>(medicalSites);
    }

    /**
     * Returns the drone bases of the map.
     *
     * @return copy of the drone bases list
     */
    public List<DroneBase> getDroneBases() {
        return new ArrayList<>(droneBases);
    }

    /**
     * Returns the drones of the map.
     *
     * @return copy of the drones list
     */
    public List<Drone> getDrones() {
        return new ArrayList<>(drones);
    }

    /**
     * Returns the Voronoi diagram of the map.
     *
     * @return Voronoi diagram
     */
    public VoronoiDiagram getVoronoiDiagram() {
        return voronoiDiagram;
    }

    /**
     * Returns the Delaunay triangulation of the map.
     *
     * @return Delaunay triangulation
     */
    public DelaunayTriangulation getDelaunayTriangulation() {
        return delaunayTriangulation;
    }
}