package model;

public class DroneBase {

    private String name;
    private Position position;

    public DroneBase(String name, Position position) {
        this.name = name;
        this.position = position;
    }

    public String getName() {
        return name;
    }

    public Position getPosition() {
        return position;
    }
}
