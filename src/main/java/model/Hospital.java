package model;

public class Hospital {

    private String name;
    private Position position;

    public Hospital(String name, Position position) {
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