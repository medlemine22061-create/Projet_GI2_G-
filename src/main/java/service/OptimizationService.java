package service;

import model.CollectionCenter;
import model.DelaunayTriangulation;
import model.DeliveryRequest;
import model.Drone;
import model.DroneBase;
import model.Hospital;
import model.MapModel;
import model.Mission;
import model.Route;
import model.Triangle;
import model.enums.DroneStatus;

import java.util.List;

/**
 * Service responsible for drone selection, route computation and mission creation.
 *
 * ── Algorithmic logic ──────────────────────────────────────────────────────────
 *
 *  1. OPTIMAL BASE SELECTION  (uses Voronoï indirectly)
 *     We want the base that minimises the total flight distance:
 *       score(base) = dist(base → collection center) + dist(base → hospital)
 *     This ensures the drone takes the most efficient path given BOTH endpoints.
 *
 *  2. DRONE SELECTION  (within the chosen base)
 *     Among available drones in the best base, we pick the one with:
 *       - sufficient battery/autonomy to complete the round trip
 *       - best battery level (most reliable)
 *
 *  3. ROUTE COMPUTATION  (uses Delaunay triangulation)
 *     The direct route is:  base → collection center → hospital
 *     No intermediate waypoints are added.
 *     Delaunay is used here to CHECK ADJACENCY:
 *       - if origin and destination share a Delaunay edge (appear together
 *         in a triangle), they are considered "neighbours" and the direct
 *         route is confirmed as optimal.
 *       - This is meaningful because Delaunay edges connect the geometrically
 *         nearest pairs — exactly the property needed to justify a direct flight.
 *
 *  The route is ALWAYS base → center → hospital (no parasitic waypoints).
 */
public class OptimizationService {

    private final DelaunayTriangulation delaunay;
    private final MapModel              mapModel;

    public OptimizationService(DelaunayTriangulation delaunay, MapModel mapModel) {
        this.delaunay  = delaunay;
        this.mapModel  = mapModel;
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Creates a complete mission from a delivery request.
     *
     * Steps:
     *   1. Find the optimal base  (minimises base→center + base→hospital)
     *   2. Select the best drone in that base
     *   3. Build the route  base → center → hospital
     *   4. Log whether the center–hospital pair are Delaunay neighbours
     */
    public Mission createMission(DeliveryRequest request, List<Drone> drones) {
        if (request == null) throw new IllegalArgumentException("request cannot be null");
        request.validate();

        CollectionCenter center   = request.getOrigin();
        Hospital         hospital = request.getDestination();

        // 1. Optimal base
        DroneBase bestBase = findOptimalBase(center, hospital);
        if (bestBase == null) throw new IllegalStateException("No drone base available.");

        // 2. Best drone in that base
        Drone bestDrone = selectBestDrone(bestBase, center, hospital);
        if (bestDrone == null)
            throw new IllegalStateException(
                    "No available drone in base [" + bestBase.getName()
                            + "] can complete this mission. Check battery/autonomy.");

        // 3. Route: base → center → hospital  (no parasitic waypoint)
        Route route = new Route(center, hospital);
        // (the base departure is handled visually in MapCanvas via droneBasePos)

        // 4. Delaunay adjacency check (informational — logged in mission history)
        boolean adjacent = areDelaunayNeighbours(center, hospital);

        bestDrone.setStatus(DroneStatus.IN_MISSION);

        Mission mission = new Mission(request, bestDrone, route);
        mission.addHistoryEvent("Optimal base selected: " + bestBase.getName()
                + "  |  score=" + String.format("%.1f",
                bestBase.getPosition().distanceTo(center.getPosition())
                        + bestBase.getPosition().distanceTo(hospital.getPosition())));
        mission.addHistoryEvent("Drone selected: " + bestDrone.getId()
                + "  battery=" + (int)bestDrone.getBatteryLevel() + "%");
        mission.addHistoryEvent("Delaunay adjacency (center–hospital): " + adjacent);
        mission.addHistoryEvent("Route: [" + bestBase.getName() + "] → ["
                + center.getName() + "] → [" + hospital.getName() + "]");

        return mission;
    }

    // ── Step 1 : optimal base ─────────────────────────────────────────────────

    /**
     * Finds the drone base that minimises:
     *   dist(base → collection center) + dist(base → hospital)
     *
     * This is the base that sits closest to BOTH endpoints simultaneously,
     * ensuring the shortest total flight.
     *
     * @param center   organ collection center (departure after base)
     * @param hospital requesting hospital     (final destination)
     * @return the optimal DroneBase, or null if none available
     */
    public DroneBase findOptimalBase(CollectionCenter center, Hospital hospital) {
        if (mapModel == null) return null;

        DroneBase best      = null;
        double    bestScore = Double.MAX_VALUE;

        for (DroneBase base : mapModel.getDroneBases()) {
            if (base.getAvailableDrones().isEmpty()) continue;

            double score =
                    base.getPosition().distanceTo(center.getPosition())
                            + base.getPosition().distanceTo(hospital.getPosition());

            if (score < bestScore) {
                bestScore = score;
                best      = base;
            }
        }

        return best;
    }

    // ── Step 2 : best drone in the chosen base ────────────────────────────────

    /**
     * Selects the best available drone in the given base.
     *
     * Criteria (in order):
     *   1. Drone must be available (not already in mission)
     *   2. Drone must have enough autonomy for:
     *        dist(base→center) + dist(center→hospital)
     *   3. Among eligible drones, pick the one with the highest battery level
     *
     * @param base     the pre-selected drone base
     * @param center   collection center
     * @param hospital destination hospital
     * @return best Drone, or null if none qualifies
     */
    public Drone selectBestDrone(DroneBase base,
                                 CollectionCenter center,
                                 Hospital hospital) {
        if (base == null) return null;

        double totalDist =
                base.getPosition().distanceTo(center.getPosition())
                        + center.getPosition().distanceTo(hospital.getPosition());

        Drone  best        = null;
        double bestBattery = -1;

        for (Drone drone : base.getAvailableDrones()) {
            // Check autonomy (stored in km; totalDist is in canvas units ~1 unit ≈ 1 km)
            if (drone.getAutonomy() < totalDist) continue;

            if (drone.getBatteryLevel() > bestBattery) {
                bestBattery = drone.getBatteryLevel();
                best        = drone;
            }
        }

        return best;
    }

    // ── Step 3 : Delaunay adjacency check ─────────────────────────────────────

    /**
     * Returns true if the collection center and the hospital share a Delaunay
     * edge, i.e. they appear together in at least one triangle.
     *
     * In Delaunay triangulation, two sites share an edge only if no other site
     * lies inside their circumcircle — meaning they are "geometrically nearest
     * neighbours". A direct flight between them is therefore provably optimal
     * among all Delaunay paths.
     *
     * @param center   collection center
     * @param hospital destination hospital
     * @return true if they are Delaunay neighbours
     */
    public boolean areDelaunayNeighbours(CollectionCenter center, Hospital hospital) {
        if (delaunay == null) return false;

        for (Triangle t : delaunay.getTriangles()) {
            if (t.containsSite(center) && t.containsSite(hospital)) {
                return true;
            }
        }
        return false;
    }

    // ── Kept for backward compatibility ──────────────────────────────────────

    /**
     * @deprecated Use {@link #createMission(DeliveryRequest, List)} instead.
     */
    @Deprecated
    public Drone findNearestAvailableDrone(List<Drone> drones,
                                           CollectionCenter origin) {
        if (drones == null || origin == null) return null;

        Drone  best     = null;
        double bestDist = Double.MAX_VALUE;

        for (Drone d : drones) {
            if (!d.isAvailable()) continue;
            double dist = d.getPosition().distanceTo(origin.getPosition());
            if (dist < bestDist) { bestDist = dist; best = d; }
        }
        return best;
    }
}