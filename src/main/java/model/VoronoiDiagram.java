package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a simplified Voronoi diagram.
 *
 * This class implements a simplified Voronoi algorithm using a grid.
 * Each tested position of the grid is assigned to the nearest medical site.
 */
public class VoronoiDiagram implements Serializable {
    private static final long serialVersionUID = 1L;

    private final List<VoronoiCell> cells;

    public VoronoiDiagram() {
        this.cells = new ArrayList<>();
    }

    /**
     * Computes a simplified Voronoi diagram.
     *
     * The method creates one cell for each medical site, then scans a grid
     * around all sites. Each grid point is assigned to the nearest site.
     *
     * @param sites list of medical sites
     */
    public void compute(List<MedicalSite> sites) {
        cells.clear();

        if (sites == null || sites.isEmpty()) {
            return;
        }

        for (MedicalSite site : sites) {
            cells.add(new VoronoiCell(site));
        }

        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;

        for (MedicalSite site : sites) {
            Position position = site.getPosition();

            minX = Math.min(minX, position.getX());
            minY = Math.min(minY, position.getY());
            maxX = Math.max(maxX, position.getX());
            maxY = Math.max(maxY, position.getY());
        }

        double margin = 50.0;

        minX -= margin;
        minY -= margin;
        maxX += margin;
        maxY += margin;

        double step = 20.0;

        for (double x = minX; x <= maxX; x += step) {
            for (double y = minY; y <= maxY; y += step) {
                Position position = new Position(x, y);
                MedicalSite nearestSite = getNearestSite(position);

                if (nearestSite != null) {
                    VoronoiCell cell = getCellBySite(nearestSite);

                    if (cell != null) {
                        cell.addPoint(position);
                    }
                }
            }
        }

        for (VoronoiCell cell : cells) {
            cell.computeSurface(step);
            cell.computeDensity();
        }
    }

    /**
     * Finds the nearest medical site from a given position.
     * This is the main idea of a Voronoi diagram.
     *
     * @param position tested position
     * @return nearest medical site
     */
    public MedicalSite getNearestSite(Position position) {
        if (position == null || cells.isEmpty()) {
            return null;
        }

        MedicalSite nearestSite = null;
        double bestDistance = Double.MAX_VALUE;

        for (VoronoiCell cell : cells) {
            MedicalSite site = cell.getOwner();
            double distance = position.distanceTo(site.getPosition());

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
     * @return Voronoi cell
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

    public List<VoronoiCell> getCells() {
        return new ArrayList<>(cells);
    }
}