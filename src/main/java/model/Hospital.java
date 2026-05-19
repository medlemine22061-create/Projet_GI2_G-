package model;

public class Hospital {

    private String name;
    private Point2D position;

    public Hospital(String name, Point2D position) {
        this.name = name;
        this.position = position;
    }

    public String getName() {
        return name;
    }

    public Point2D getPosition() {
        return position;
    }
}