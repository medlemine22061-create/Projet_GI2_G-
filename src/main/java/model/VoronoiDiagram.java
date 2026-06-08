package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a Voronoi diagram used to partition the map into zones.
 * Each cell is associated with one medical site.
 */
public class VoronoiDiagram implements Serializable {

    private static final long serialVersionUID = 1L;

    private final List<VoronoiCell> cells;

    /**
     * Creates an empty Voronoi diagram.
     */
    public VoronoiDiagram() {
        this.cells = new ArrayList<>();
    }

    /**
     * Computes the Voronoi diagram from a list of medical sites.
     * This is a simplified version: it creates one cell for each site.
     * A real Voronoi algorithm can replace this method later.
     *
     * @param sites list of medical sites
     */
    public void compute(List<MedicalSite> sites) {
        cells.clear();

        if (sites == null) {
            return;
        }

        for (MedicalSite site : sites) {
            cells.add(new VoronoiCell(site));
        }
    }

    /**
     * Finds the nearest medical site to a given position.
     *
     * @param position target position
     * @return nearest medical site, or null if the diagram is empty
     */
    public MedicalSite getNearestSite(Position position) {
        Objects.requireNonNull(position, "position cannot be null");

        MedicalSite nearestSite = null;
        double bestDistance = Double.MAX_VALUE;

        for (VoronoiCell cell : cells) {
            MedicalSite site = cell.getOwner();
            double distance = site.getPosition().distanceTo(position);

            if (distance < bestDistance) {
                bestDistance = distance;
                nearestSite = site;
            }
        }

        return nearestSite;
    }

    /**
     * Returns the Voronoi cell associated with a medical site.
     *
     * @param site medical site
     * @return associated cell, or null if no cell is found
     */
    public VoronoiCell getCellBySite(MedicalSite site) {
        if (site == null) {
            return null;
        }

        for (VoronoiCell cell : cells) {
            if (cell.getOwner().equals(site)) {
                return cell;
            }
        }

        return null;
    }

    /**
     * Returns all Voronoi cells.
     *
     * @return copy of the cells list
     */
    public List<VoronoiCell> getCells() {
        return new ArrayList<>(cells);
    }
}
