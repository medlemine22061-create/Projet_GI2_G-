package model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a triangle in the Delaunay triangulation.
 */
public class Triangle implements Serializable {

    private static final long serialVersionUID = 1L;

    private final MedicalSite siteA;
    private final MedicalSite siteB;
    private final MedicalSite siteC;

    /**
     * Creates a triangle from three medical sites.
     *
     * @param siteA first medical site
     * @param siteB second medical site
     * @param siteC third medical site
     */
    public Triangle(MedicalSite siteA, MedicalSite siteB, MedicalSite siteC) {
        this.siteA = Objects.requireNonNull(siteA, "siteA cannot be null");
        this.siteB = Objects.requireNonNull(siteB, "siteB cannot be null");
        this.siteC = Objects.requireNonNull(siteC, "siteC cannot be null");
    }

    /**
     * Computes the surface of the triangle.
     *
     * @return triangle surface
     */
    public double computeSurface() {
        Point2D a = siteA.getPosition();
        Point2D b = siteB.getPosition();
        Point2D c = siteC.getPosition();

        return Math.abs(
                a.getX() * (b.getY() - c.getY())
                        + b.getX() * (c.getY() - a.getY())
                        + c.getX() * (a.getY() - b.getY())
        ) / 2.0;
    }

    /**
     * Computes an approximate circumcenter.
     * For the first version, we use the average of the three positions.
     *
     * @return approximate circumcenter
     */
    public Point2D getCircumcenter() {
        double x = (
                siteA.getPosition().getX()
                        + siteB.getPosition().getX()
                        + siteC.getPosition().getX()
        ) / 3.0;

        double y = (
                siteA.getPosition().getY()
                        + siteB.getPosition().getY()
                        + siteC.getPosition().getY()
        ) / 3.0;

        return new Point2D(x, y);
    }

    /**
     * Computes the distance between site A and site B.
     *
     * @return distance AB
     */
    public double getDistanceAB() {
        return siteA.getPosition().distanceTo(siteB.getPosition());
    }

    /**
     * Computes the distance between site B and site C.
     *
     * @return distance BC
     */
    public double getDistanceBC() {
        return siteB.getPosition().distanceTo(siteC.getPosition());
    }

    /**
     * Computes the distance between site C and site A.
     *
     * @return distance CA
     */
    public double getDistanceCA() {
        return siteC.getPosition().distanceTo(siteA.getPosition());
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
}