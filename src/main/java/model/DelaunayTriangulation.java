package model;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Delaunay triangulation.
 * This first version uses a simplified construction.
 */
public class DelaunayTriangulation implements Serializable {

    private static final long serialVersionUID = 1L;

    private final List<Triangle> triangles;

    /**
     * Creates an empty Delaunay triangulation.
     */
    public DelaunayTriangulation() {
        this.triangles = new ArrayList<>();
    }

    /**
     * Computes a simplified triangulation from medical sites.
     * It creates triangles from consecutive sites.
     * A real Delaunay algorithm can replace this method later.
     *
     * @param sites list of medical sites
     */
    public void compute(List<MedicalSite> sites) {
        triangles.clear();

        if (sites == null || sites.size() < 3) {
            return;
        }

        for (int i = 0; i <= sites.size() - 3; i++) {
            Triangle triangle = new Triangle(
                    sites.get(i),
                    sites.get(i + 1),
                    sites.get(i + 2)
            );

            triangles.add(triangle);
        }
    }

    /**
     * Adds a triangle manually to the triangulation.
     *
     * @param triangle triangle to add
     */
    public void addTriangle(Triangle triangle) {
        if (triangle != null) {
            triangles.add(triangle);
        }
    }

    /**
     * Clears all triangles.
     */
    public void clear() {
        triangles.clear();
    }

    /**
     * Returns all triangles.
     *
     * @return copy of the triangles list
     */
    public List<Triangle> getTriangles() {
        return new ArrayList<>(triangles);
    }
}
