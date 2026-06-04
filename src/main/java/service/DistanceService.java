package service;

import model.Position;

public class DistanceService {

    public double calculateDistance(Position a, Position b) {

        double dx = a.getX() - b.getX();
        double dy = a.getY() - b.getY();

        return Math.sqrt(dx * dx + dy * dy);
    }
}