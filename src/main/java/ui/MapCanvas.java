package ui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import model.CollectionCenter;
import model.Drone;
import model.DroneBase;
import model.Hospital;
import model.MapModel;
import model.MedicalSite;
import model.Mission;
import model.Position;
import model.Route;
import model.Triangle;
import model.UserPoint;
import model.VoronoiCell;

/**
 * Canvas used to draw the map.
 * It displays medical sites, drone bases, drones, user points,
 * Voronoi cells, Delaunay triangles and the current mission route.
 */
public class MapCanvas extends Canvas {

    private final MapModel mapModel;
    private Mission currentMission;

    public MapCanvas(MapModel mapModel) {
        super(900, 650);
        this.mapModel = mapModel;

        // Redraw automatically if the canvas size changes.
        widthProperty().addListener(event -> draw());
        heightProperty().addListener(event -> draw());
    }

    public void setCurrentMission(Mission mission) {
        this.currentMission = mission;
        draw();
    }

    public void draw() {
        GraphicsContext gc = getGraphicsContext2D();

        clear(gc);

        drawDelaunay(gc);
        drawVoronoi(gc);
        drawMissionRoute(gc);
        drawDroneBases(gc);
        drawMedicalSites(gc);
        drawUserPoints(gc);
        drawDrones(gc);
        drawLegend(gc);
    }

    private void clear(GraphicsContext gc) {
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, getWidth(), getHeight());

        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(1);

        // Simple grid
        for (int x = 0; x <= getWidth(); x += 50) {
            gc.strokeLine(x, 0, x, getHeight());
        }

        for (int y = 0; y <= getHeight(); y += 50) {
            gc.strokeLine(0, y, getWidth(), y);
        }
    }

    private void drawMedicalSites(GraphicsContext gc) {
        for (MedicalSite site : mapModel.getMedicalSites()) {
            Position position = site.getPosition();
            double x = position.getX();
            double y = position.getY();

            if (site instanceof Hospital) {
                gc.setFill(Color.GREEN);
                gc.fillOval(x - 7, y - 7, 14, 14);
                gc.setFill(Color.BLACK);
                gc.fillText("H: " + site.getName(), x + 10, y);
            } else if (site instanceof CollectionCenter) {
                gc.setFill(Color.ORANGE);
                gc.fillRect(x - 7, y - 7, 14, 14);
                gc.setFill(Color.BLACK);
                gc.fillText("C: " + site.getName(), x + 10, y);
            } else {
                gc.setFill(Color.GRAY);
                gc.fillOval(x - 6, y - 6, 12, 12);
                gc.setFill(Color.BLACK);
                gc.fillText(site.getName(), x + 10, y);
            }
        }
    }

    private void drawUserPoints(GraphicsContext gc) {
        gc.setFill(Color.PURPLE);

        for (UserPoint point : mapModel.getUserPoints()) {
            Position position = point.getPosition();
            double x = position.getX();
            double y = position.getY();

            gc.fillOval(x - 4, y - 4, 8, 8);

            if (point.getNearestSite() != null) {
                Position nearestPosition = point.getNearestSite().getPosition();

                gc.setStroke(Color.rgb(180, 120, 200, 0.4));
                gc.setLineWidth(1);
                gc.strokeLine(x, y, nearestPosition.getX(), nearestPosition.getY());

                gc.setFill(Color.PURPLE);
            }
        }
    }

    private void drawDroneBases(GraphicsContext gc) {
        for (DroneBase base : mapModel.getDroneBases()) {
            Position position = base.getPosition();
            double x = position.getX();
            double y = position.getY();

            gc.setFill(Color.DARKBLUE);
            gc.fillRect(x - 9, y - 9, 18, 18);

            gc.setFill(Color.BLACK);
            gc.fillText("Base: " + base.getName(), x + 12, y);
        }
    }

    private void drawDrones(GraphicsContext gc) {
        for (Drone drone : mapModel.getDrones()) {
            Position position = drone.getPosition();
            double x = position.getX();
            double y = position.getY();

            gc.setFill(Color.RED);

            double[] xPoints = {x, x - 7, x + 7};
            double[] yPoints = {y - 8, y + 8, y + 8};

            gc.fillPolygon(xPoints, yPoints, 3);

            gc.setFill(Color.BLACK);
            gc.fillText("D: " + drone.getId(), x + 10, y + 10);
        }
    }

    private void drawVoronoi(GraphicsContext gc) {
        /*
         * This is a simplified drawing.
         * Each Voronoi cell contains grid points assigned to the nearest site.
         * We draw these points with light colors.
         */
        for (VoronoiCell cell : mapModel.getVoronoiDiagram().getCells()) {
            gc.setFill(Color.rgb(120, 180, 255, 0.25));

            int counter = 0;

            for (Position point : cell.getPoints()) {
                /*
                 * To avoid drawing too many points and slowing the interface,
                 * we draw only one point out of two.
                 */
                if (counter % 2 == 0) {
                    gc.fillRect(point.getX(), point.getY(), 3, 3);
                }

                counter++;
            }
        }
    }

    private void drawDelaunay(GraphicsContext gc) {
        gc.setStroke(Color.rgb(0, 0, 0, 0.35));
        gc.setLineWidth(1.5);

        for (Triangle triangle : mapModel.getDelaunayTriangulation().getTriangles()) {
            Position a = triangle.getSiteA().getPosition();
            Position b = triangle.getSiteB().getPosition();
            Position c = triangle.getSiteC().getPosition();

            gc.strokeLine(a.getX(), a.getY(), b.getX(), b.getY());
            gc.strokeLine(b.getX(), b.getY(), c.getX(), c.getY());
            gc.strokeLine(c.getX(), c.getY(), a.getX(), a.getY());
        }
    }

    private void drawMissionRoute(GraphicsContext gc) {
        if (currentMission == null) {
            return;
        }

        Route route = currentMission.getRoute();

        if (route == null) {
            return;
        }

        gc.setStroke(Color.RED);
        gc.setLineWidth(3);

        Position current = route.getOrigin().getPosition();

        for (Position waypoint : route.getWaypoints()) {
            gc.strokeLine(
                    current.getX(),
                    current.getY(),
                    waypoint.getX(),
                    waypoint.getY()
            );

            gc.setFill(Color.RED);
            gc.fillOval(waypoint.getX() - 4, waypoint.getY() - 4, 8, 8);

            current = waypoint;
        }

        Position destination = route.getDestination().getPosition();

        gc.strokeLine(
                current.getX(),
                current.getY(),
                destination.getX(),
                destination.getY()
        );
    }

    private void drawLegend(GraphicsContext gc) {
        double x = 15;
        double y = getHeight() - 115;

        gc.setFill(Color.rgb(255, 255, 255, 0.85));
        gc.fillRect(x - 10, y - 20, 220, 105);

        gc.setStroke(Color.GRAY);
        gc.strokeRect(x - 10, y - 20, 220, 105);

        gc.setFill(Color.BLACK);
        gc.fillText("Legend:", x, y);

        gc.setFill(Color.GREEN);
        gc.fillOval(x, y + 15, 10, 10);
        gc.setFill(Color.BLACK);
        gc.fillText("Hospital", x + 20, y + 24);

        gc.setFill(Color.ORANGE);
        gc.fillRect(x, y + 35, 10, 10);
        gc.setFill(Color.BLACK);
        gc.fillText("Collection Center", x + 20, y + 44);

        gc.setFill(Color.PURPLE);
        gc.fillOval(x, y + 55, 8, 8);
        gc.setFill(Color.BLACK);
        gc.fillText("User Point", x + 20, y + 64);

        gc.setFill(Color.RED);
        double[] xPoints = {x + 5, x, x + 10};
        double[] yPoints = {y + 75, y + 87, y + 87};
        gc.fillPolygon(xPoints, yPoints, 3);
        gc.setFill(Color.BLACK);
        gc.fillText("Drone / Mission route", x + 20, y + 84);
    }
}