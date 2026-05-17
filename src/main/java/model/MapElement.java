package model;

public abstract class MapElement {

    private int id;
    private String name;
    private Point2D position;

    public MapElement(int id, String name, Point2D position) {

        this.id = id;
        this.name = name;
        this.position = position;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Point2D getPosition() {
        return position;
    }

    public void setPosition(Point2D position) {
        this.position = position;
    }
}
