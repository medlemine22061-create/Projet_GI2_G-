package service;

import model.CollectionCenter;
import model.DelaunayTriangulation;
import model.DeliveryRequest;
import model.Drone;
import model.Hospital;
import model.MedicalSite;
import model.Mission;
import model.Route;
import model.Triangle;
import model.enums.DroneStatus;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Service responsible for route computation, drone selection and mission creation.
 */
public class OptimizationService {

    private final DelaunayTriangulation delaunayTriangulation;

    public OptimizationService(DelaunayTriangulation delaunayTriangulation) {
        this.delaunayTriangulation = delaunayTriangulation;
    }

    /**
     * Finds the nearest available drone from the collection center.
     */
    public Drone findNearestAvailableDrone(List<Drone> drones, CollectionCenter origin) {
        if (drones == null || origin == null) {
            return null;
        }

        Drone bestDrone = null;
        double bestDistance = Double.MAX_VALUE;

        for (Drone drone : drones) {
            if (!drone.isAvailable()) {
                continue;
            }

            double distance = drone.getPosition().distanceTo(origin.getPosition());

            if (distance < bestDistance) {
                bestDistance = distance;
                bestDrone = drone;
            }
        }

        return bestDrone;
    }

    /**
     * Computes a route between a collection center and a hospital.
     *
     * The method uses Delaunay triangles in a simplified way:
     * if a triangle connects the origin and the destination, the third point
     * can be used as an intermediate waypoint.
     */
    public Route computeOptimalRoute(CollectionCenter origin, Hospital destination) {
        if (origin == null || destination == null) {
            throw new IllegalArgumentException("origin and destination cannot be null");
        }

        Route route = new Route(origin, destination);

        if (delaunayTriangulation == null) {
            return route;
        }

        List<Triangle> triangles = delaunayTriangulation.getTriangles();

        for (Triangle triangle : triangles) {
            boolean containsOrigin = triangle.containsSite(origin);
            boolean containsDestination = triangle.containsSite(destination);

            if (containsOrigin && containsDestination) {
                MedicalSite waypointSite = findThirdSite(triangle, origin, destination);

                if (waypointSite != null) {
                    route.addWaypoint(waypointSite.getPosition());
                }

                return route;
            }
        }

        return route;
    }

    private MedicalSite findThirdSite(Triangle triangle, MedicalSite first, MedicalSite second) {
        for (MedicalSite site : triangle.getSites()) {
            if (!site.equals(first) && !site.equals(second)) {
                return site;
            }
        }

        return null;
    }

    /**
     * Selects the best drone for a given route.
     *
     * Score = distance from drone to origin + route distance.
     */
    public Drone selectBestDrone(List<Drone> drones, Route route) {
        if (drones == null || route == null) {
            return null;
        }

        List<Drone> candidates = new ArrayList<>();

        for (Drone drone : drones) {
            if (drone.isAvailable() && drone.canDoMission(route)) {
                candidates.add(drone);
            }
        }

        return candidates.stream()
                .min(Comparator.comparingDouble(drone ->
                        drone.getPosition().distanceTo(route.getOrigin().getPosition())
                                + route.computeDistance()
                ))
                .orElse(null);
    }

    /**
     * Creates a mission from a delivery request.
     */
    public Mission createMission(DeliveryRequest request, List<Drone> drones) {
        if (request == null) {
            throw new IllegalArgumentException("request cannot be null");
        }

        request.validate();

        Route route = computeOptimalRoute(
                request.getOrigin(),
                request.getDestination()
        );

        Drone drone = selectBestDrone(drones, route);

        if (drone == null) {
            throw new IllegalStateException("No available drone can complete this mission");
        }

        drone.setStatus(DroneStatus.IN_MISSION);

        Mission mission = new Mission(request, drone, route);
        mission.addHistoryEvent("Mission created with drone " + drone.getId());

        return mission;
    }
}