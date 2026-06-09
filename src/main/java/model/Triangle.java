package model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Represents a triangle between three medical sites.
 */
public class Triangle implements Serializable {
    private static final long serialVersionUID = 1L;

    private final MedicalSite siteA;
    private final MedicalSite siteB;
    private final MedicalSite siteC;

    public Triangle(MedicalSite siteA, MedicalSite siteB, MedicalSite siteC) {
        this.siteA = Objects.requireNonNull(siteA, "siteA cannot be null");
        this.siteB = Objects.requireNonNull(siteB, "siteB cannot be null");
        this.siteC = Objects.requireNonNull(siteC, "siteC cannot be null");
    }

    public double computeSurface() {
        Position a = siteA.getPosition();
        Position b = siteB.getPosition();
        Position c = siteC.getPosition();

        return Math.abs(
                a.getX() * (b.getY() - c.getY())
                        + b.getX() * (c.getY() - a.getY())
                        + c.getX() * (a.getY() - b.getY())
        ) / 2.0;
    }

    /**
     * Computes the circumcenter of the triangle.
     * The circumcenter is used in the Delaunay condition.
     */
    public Position getCircumcenter() {
        Position a = siteA.getPosition();
        Position b = siteB.getPosition();
        Position c = siteC.getPosition();

        double ax = a.getX();
        double ay = a.getY();
        double bx = b.getX();
        double by = b.getY();
        double cx = c.getX();
        double cy = c.getY();

        double d = 2 * (ax * (by - cy) + bx * (cy - ay) + cx * (ay - by));

        if (Math.abs(d) < 1e-9) {
            return new Position(
                    (ax + bx + cx) / 3.0,
                    (ay + by + cy) / 3.0
            );
        }

        double ux =
                ((ax * ax + ay * ay) * (by - cy)
                        + (bx * bx + by * by) * (cy - ay)
                        + (cx * cx + cy * cy) * (ay - by)) / d;

        double uy =
                ((ax * ax + ay * ay) * (cx - bx)
                        + (bx * bx + by * by) * (ax - cx)
                        + (cx * cx + cy * cy) * (bx - ax)) / d;

        return new Position(ux, uy);
    }

    public double getCircumradius() {
        Position center = getCircumcenter();
        return center.distanceTo(siteA.getPosition());
    }

    /**
     * Checks if a medical site is inside the circumcircle of this triangle.
     * In a Delaunay triangulation, no other point should be inside the circumcircle.
     */
    public boolean containsInCircumcircle(MedicalSite site) {
        if (site == null || containsSite(site)) {
            return false;
        }

        Position center = getCircumcenter();
        double radius = getCircumradius();
        double distance = center.distanceTo(site.getPosition());

        return distance < radius - 1e-9;
    }

    public boolean containsSite(MedicalSite site) {
        return siteA.equals(site) || siteB.equals(site) || siteC.equals(site);
    }

    public double getDistanceAB() {
        return siteA.getPosition().distanceTo(siteB.getPosition());
    }

    public double getDistanceBC() {
        return siteB.getPosition().distanceTo(siteC.getPosition());
    }

    public double getDistanceCA() {
        return siteC.getPosition().distanceTo(siteA.getPosition());
    }

    public List<MedicalSite> getSites() {
        return Arrays.asList(siteA, siteB, siteC);
    }

    public MedicalSite getSiteA() {
        return siteA;
    }

    public MedicalSite getSiteB() {
        return siteB;
    }

    public MedicalSite getSiteC() {
        return siteC;
    }

    @Override
    public String toString() {
        return "Triangle{"
                + siteA.getName()
                + ", "
                + siteB.getName()
                + ", "
                + siteC.getName()
                + "}";
    }
}