package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Represents a simplified Delaunay triangulation.
 *
 * This implementation uses a simple empty circumcircle test on every group of
 * three medical sites. It is not the most optimized algorithm, but it gives
 * a functional triangulation for a small ING1 project.
 */
public class DelaunayTriangulation implements Serializable {
    private static final long serialVersionUID = 1L;

    private final List<Triangle> triangles;

    public DelaunayTriangulation() {
        this.triangles = new ArrayList<>();
    }

    /**
     * Computes a simplified Delaunay triangulation.
     *
     * @param sites list of medical sites
     */
    public void compute(List<MedicalSite> sites) {
        triangles.clear();

        if (sites == null || sites.size() < 3) {
            return;
        }

        for (int i = 0; i < sites.size() - 2; i++) {
            for (int j = i + 1; j < sites.size() - 1; j++) {
                for (int k = j + 1; k < sites.size(); k++) {

                    Triangle triangle = new Triangle(
                            sites.get(i),
                            sites.get(j),
                            sites.get(k)
                    );

                    if (triangle.computeSurface() < 1e-9) {
                        continue;
                    }

                    boolean valid = true;

                    for (MedicalSite site : sites) {
                        if (triangle.containsInCircumcircle(site)) {
                            valid = false;
                            break;
                        }
                    }

                    if (valid) {
                        triangles.add(triangle);
                    }
                }
            }
        }

        if (triangles.isEmpty()) {
            computeFallbackNearestTriangles(sites);
        }
    }

    /**
     * Fallback version if the empty circumcircle test gives no triangle.
     * It creates triangles using nearest sites.
     */
    private void computeFallbackNearestTriangles(List<MedicalSite> sites) {
        List<MedicalSite> sortedSites = new ArrayList<>(sites);

        sortedSites.sort(Comparator.comparingDouble(site -> site.getPosition().getX()));

        for (int i = 0; i < sortedSites.size() - 2; i++) {
            Triangle triangle = new Triangle(
                    sortedSites.get(i),
                    sortedSites.get(i + 1),
                    sortedSites.get(i + 2)
            );

            if (triangle.computeSurface() > 1e-9) {
                triangles.add(triangle);
            }
        }
    }

    public void addTriangle(Triangle triangle) {
        if (triangle != null) {
            triangles.add(triangle);
        }
    }

    public void clear() {
        triangles.clear();
    }

    public List<Triangle> getTriangles() {
        return new ArrayList<>(triangles);
    }

    /**
     * Returns the triangles that contain a given medical site.
     */
    public List<Triangle> getTrianglesConnectedTo(MedicalSite site) {
        List<Triangle> result = new ArrayList<>();

        if (site == null) {
            return result;
        }

        for (Triangle triangle : triangles) {
            if (triangle.containsSite(site)) {
                result.add(triangle);
            }
        }

        return result;
    }
}