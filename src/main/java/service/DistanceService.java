package service;

import model.Point2D;

public class DistanceService {

    public double calculateDistance(Point2D a, Point2D b) {

        double dx = a.getX() - b.getX();
        double dy = a.getY() - b.getY();

        return Math.sqrt(dx * dx + dy * dy);
    }
}