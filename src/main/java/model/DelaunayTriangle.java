package model;

public class DelaunayTriangle {

    private DroneBase a;

    private DroneBase b;

    private DroneBase c;

    private Point2D circumcenter;

    private double area;

    public DelaunayTriangle(
            DroneBase a,
            DroneBase b,
            DroneBase c
    ) {

        this.a = a;
        this.b = b;
        this.c = c;

        this.circumcenter = computeCircumcenter();

        this.area = computeArea();
    }

    public DroneBase getA() {
        return a;
    }

    public DroneBase getB() {
        return b;
    }

    public DroneBase getC() {
        return c;
    }

    public Point2D getCircumcenter() {
        return circumcenter;
    }

    public double getArea() {
        return area;
    }

    private double computeArea() {

        double x1 = a.getPosition().getX();
        double y1 = a.getPosition().getY();

        double x2 = b.getPosition().getX();
        double y2 = b.getPosition().getY();

        double x3 = c.getPosition().getX();
        double y3 = c.getPosition().getY();

        return Math.abs(
                (x1 * (y2 - y3)
                        + x2 * (y3 - y1)
                        + x3 * (y1 - y2)) / 2.0
        );
    }

    private Point2D computeCircumcenter() {

        double centerX =
                (
                        a.getPosition().getX()
                                + b.getPosition().getX()
                                + c.getPosition().getX()
                ) / 3.0;

        double centerY =
                (
                        a.getPosition().getY()
                                + b.getPosition().getY()
                                + c.getPosition().getY()
                ) / 3.0;

        return new Point2D(centerX, centerY);
    }

    @Override
    public String toString() {

        return "DelaunayTriangle{" +
                "a=" + a.getName() +
                ", b=" + b.getName() +
                ", c=" + c.getName() +
                ", area=" + area +
                '}';
    }
}
