package service;

import model.DeliveryRequest;
import model.Drone;
import model.Hospital;
import model.CollectionCenter;
import model.Mission;
import model.Position;
import model.Route;
import model.enums.DroneStatus;
import model.enums.RequestStatus;

import java.util.List;
import java.util.Objects;

/**
 * Service responsible for drone assignment and route computation.
 */
public class OptimizationService {

    /**
     * Finds the nearest available drone from the origin collection center.
     *
     * @param drones list of drones
     * @param origin collection center
     * @return nearest available drone, or null if none is available
     */
    public Drone findNearestAvailableDrone(List<Drone> drones, CollectionCenter origin) {
        Objects.requireNonNull(origin, "origin cannot be null");

        if (drones == null || drones.isEmpty()) {
            return null;
        }

        Drone bestDrone = null;
        double bestDistance = Double.MAX_VALUE;

        for (Drone drone : drones) {
            if (drone.isAvailable()) {
                double distance = drone.getPosition().distanceTo(origin.getPosition());

                if (distance < bestDistance) {
                    bestDistance = distance;
                    bestDrone = drone;
                }
            }
        }

        return bestDrone;
    }

    /**
     * Computes a simple route between an origin and a destination.
     *
     * @param origin collection center
     * @param destination destination hospital
     * @return computed route
     */
    public Route computeOptimalRoute(CollectionCenter origin, Hospital destination) {
        return new Route(origin, destination);
    }

    /**
     * Selects the best drone that can complete the route.
     *
     * @param drones list of drones
     * @param route route to complete
     * @return selected drone, or null
     */
    public Drone selectBestDrone(List<Drone> drones, Route route) {
        if (drones == null || route == null) {
            return null;
        }

        Drone bestDrone = null;
        double bestDistance = Double.MAX_VALUE;
        Position originPosition = route.getOrigin().getPosition();

        for (Drone drone : drones) {
            if (drone.isAvailable() && drone.canDoMission(route)) {
                double distance = drone.getPosition().distanceTo(originPosition);

                if (distance < bestDistance) {
                    bestDistance = distance;
                    bestDrone = drone;
                }
            }
        }

        return bestDrone;
    }

    /**
     * Creates a mission from a delivery request.
     *
     * @param request delivery request
     * @param drones available drones
     * @return created mission
     */
    public Mission createMission(DeliveryRequest request, List<Drone> drones) {
        Objects.requireNonNull(request, "request cannot be null");

        if (request.getStatus() == RequestStatus.PENDING && !request.validate()) {
            throw new IllegalStateException("Request cannot be validated");
        }

        Route route = computeOptimalRoute(request.getOrigin(), request.getDestination());
        Drone drone = selectBestDrone(drones, route);

        if (drone == null) {
            throw new IllegalStateException("No available drone can do this mission");
        }

        drone.setStatus(DroneStatus.IN_MISSION);
        request.updateStatus(RequestStatus.ASSIGNED);

        return new Mission("MIS-" + System.currentTimeMillis(), request, drone, route);
    }
}