package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a drone base that contains drones
 */

public class DroneBase implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String id;
    private String name;
    private Position position;
    private int capacity;
    private final List<Drone> drones;

    /**
     * Creates a drone base.
     *
     * @param id unique identifier
     * @param name base name
     * @param position base position
     * @param capacity maximum number of drones
     */
    public DroneBase(String id, String name, Position position, int capacity) {
        this.id = MedicalSite.requireText(id, "id");
        this.name = MedicalSite.requireText(name, "name");
        this.position = Objects.requireNonNull(position, "position cannot be null");

        if (capacity < 0) {
            throw new IllegalArgumentException("capacity cannot be negative");
        }

        this.capacity = capacity;
        this.drones = new ArrayList<>();
    }

    /**
     * Adds a drone to this base.
     *
     * @param drone drone to add
     * @throws IllegalStateException if the base is full
     */
    public void addDrone(Drone drone) {
        Objects.requireNonNull(drone, "drone cannot be null");

        if (drones.size() >= capacity) {
            throw new IllegalStateException("Drone base is full");
        }

        drones.add(drone);
    }

    /**
     * Removes a drone from this base.
     *
     * @param drone drone to remove
     */
    public void removeDrone(Drone drone) {
        drones.remove(drone);
    }

    /**
     * Returns all drones in this base that are available for a mission.
     *
     * @return list of available drones
     */
    public List<Drone> getAvailableDrones() {
        List<Drone> result = new ArrayList<>();

        for (Drone drone : drones) {
            if (drone.isAvailable()) {
                result.add(drone);
            }
        }

        return result;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Position getPosition() {
        return position;
    }

    public int getCapacity() {
        return capacity;
    }

    /**
     * Returns all drones registered in this base.
     *
     * @return list of all drones
     */
    public List<Drone> getDrones() {
        return new ArrayList<>(drones);
    }

    /**
     * Updates the current position of the drone.
     *
     * @param position new position
     */
    public void updatePosition(Position position) {
        this.position = Objects.requireNonNull(position, "position cannot be null");
    }
}