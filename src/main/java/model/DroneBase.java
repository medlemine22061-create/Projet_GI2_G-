package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a logistical base containing drones.
 * It is not a medical site.
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

    public void addDrone(Drone drone) {
        Objects.requireNonNull(drone, "drone cannot be null");

        if (drones.size() >= capacity) {
            throw new IllegalStateException("Drone base is full");
        }

        drones.add(drone);
    }

    public void removeDrone(Drone drone) {
        drones.remove(drone);
    }

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

    public List<Drone> getDrones() {
        return new ArrayList<>(drones);
    }

    public void updatePosition(Position position) {
        this.position = Objects.requireNonNull(position, "position cannot be null");
    }
}