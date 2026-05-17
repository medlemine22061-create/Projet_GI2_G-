package ui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import model.*;

public class MapCanvas extends Canvas {

    private MapData mapData;

    public MapCanvas(MapData mapData) {
        super(850, 700);
        this.mapData = mapData;
        draw();
    }

    private void draw() {
        GraphicsContext gc = getGraphicsContext2D();

        drawBackground(gc);
        drawFakeRoads(gc);
        drawVoronoiZones(gc);
        drawDelaunay(gc);
        drawAssignments(gc);
        drawHospitals(gc);
        drawDroneBases(gc);
        // drawRequests(gc);
        drawLegend(gc);
    }

    private void drawBackground(GraphicsContext gc) {
        gc.setFill(Color.web("#eef4f8"));
        gc.fillRect(0, 0, getWidth(), getHeight());

        gc.setStroke(Color.web("#d8e2ea"));
        gc.setLineWidth(1);

        for (int x = 0; x < getWidth(); x += 50) {
            gc.strokeLine(x, 0, x, getHeight());
        }

        for (int y = 0; y < getHeight(); y += 50) {
            gc.strokeLine(0, y, getWidth(), y);
        }

        gc.setFill(Color.web("#263238"));
        gc.setFont(Font.font("Arial", 20));
        gc.fillText("Carte opérationnelle - Livraison d'organes par drones", 25, 35);
    }

    private void drawFakeRoads(GraphicsContext gc) {
        gc.setStroke(Color.web("#c8d1d9"));
        gc.setLineWidth(8);

        gc.strokeLine(40, 120, 760, 180);
        gc.strokeLine(100, 520, 760, 420);
        gc.strokeLine(180, 80, 300, 620);
        gc.strokeLine(650, 70, 480, 610);

        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2);

        gc.strokeLine(40, 120, 760, 180);
        gc.strokeLine(100, 520, 760, 420);
        gc.strokeLine(180, 80, 300, 620);
        gc.strokeLine(650, 70, 480, 610);
    }

    private void drawVoronoiZones(GraphicsContext gc) {
        Color[] colors = {
                Color.rgb(66, 135, 245, 0.16),
                Color.rgb(46, 204, 113, 0.16),
                Color.rgb(241, 196, 15, 0.16),
                Color.rgb(155, 89, 182, 0.16),
                Color.rgb(231, 76, 60, 0.16)
        };

        int i = 0;

        for (VoronoiCell cell : mapData.getVoronoiCells()) {
            DroneBase base = cell.getSite();
            double x = base.getPosition().getX();
            double y = base.getPosition().getY();

            gc.setFill(colors[i % colors.length]);
            gc.fillOval(x - 170, y - 140, 340, 280);

            gc.setStroke(Color.rgb(255, 255, 255, 0.7));
            gc.setLineWidth(2);
            gc.strokeOval(x - 170, y - 140, 340, 280);

            i++;
        }
    }

    private void drawDroneBases(GraphicsContext gc) {
        for (DroneBase base : mapData.getDroneBases()) {
            double x = base.getPosition().getX();
            double y = base.getPosition().getY();

            gc.setFill(Color.web("#1e6bd6"));
            gc.fillOval(x - 15, y - 15, 30, 30);

            gc.setStroke(Color.WHITE);
            gc.setLineWidth(3);
            gc.strokeOval(x - 15, y - 15, 30, 30);

            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Arial", 14));
            gc.fillText("✚", x - 5, y + 5);

            gc.setFill(Color.web("#0d47a1"));
            gc.setFont(Font.font("Arial", 13));
            gc.fillText(base.getName(), x + 18, y - 12);

            gc.setFont(Font.font("Arial", 11));
            gc.fillText("Drones: " + base.getAvailableDrones(), x + 18, y + 5);
        }
    }

    private void drawRequests(GraphicsContext gc) {
        for (MedicalRequest request : mapData.getMedicalRequests()) {
            double x = request.getPosition().getX();
            double y = request.getPosition().getY();

            Color color = Color.web("#d62828");

            if (request.getPriority() == PriorityLevel.CRITICAL) {
                color = Color.web("#b00020");
            } else if (request.getPriority() == PriorityLevel.HIGH) {
                color = Color.web("#e85d04");
            } else if (request.getPriority() == PriorityLevel.MEDIUM) {
                color = Color.web("#f77f00");
            }

            gc.setFill(color);
            gc.fillOval(x - 11, y - 11, 22, 22);

            gc.setStroke(Color.WHITE);
            gc.setLineWidth(2);
            gc.strokeOval(x - 11, y - 11, 22, 22);

            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Arial", 15));
            gc.fillText("+", x - 4, y + 5);

            gc.setFill(Color.web("#8b0000"));
            gc.setFont(Font.font("Arial", 13));
            gc.fillText(request.getName(), x + 14, y - 8);

            gc.setFont(Font.font("Arial", 11));
            gc.fillText(request.getOrganType().toString(), x + 14, y + 8);
        }
    }

    private void drawHospitals(GraphicsContext gc) {

        for (Hospital hospital : mapData.getHospitals()) {

            double x = hospital.getPosition().getX();
            double y = hospital.getPosition().getY();

            gc.setFill(Color.web("#2a9d8f"));
            gc.fillRoundRect(x - 14, y - 14, 28, 28, 6, 6);

            gc.setStroke(Color.WHITE);
            gc.setLineWidth(2);
            gc.strokeRoundRect(x - 14, y - 14, 28, 28, 6, 6);

            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Arial", 16));
            gc.fillText("H", x - 6, y + 6);

            String label = hospital.hasAvailableOrgan()
                    ? hospital.getName() + " (" + hospital.getAvailableOrganType() + ")"
                    : hospital.getName() + " (receiver)";

            gc.setFill(Color.web("#00695c"));
            gc.setFont(Font.font("Arial", 13));
            gc.fillText(label, x + 18, y + 5);
        }
    }

    private void drawAssignments(GraphicsContext gc) {

        for (MedicalRequest request : mapData.getMedicalRequests()) {

            DroneBase base = request.getAssignedDroneBase();
            Hospital donor = request.getDonorCenter();
            Hospital receiver = request.getReceiverHospital();

            if (base != null && donor != null && receiver != null) {

                gc.setStroke(Color.web("#0077b6"));
                gc.setLineWidth(2.5);

                gc.strokeLine(
                        base.getPosition().getX(),
                        base.getPosition().getY(),
                        donor.getPosition().getX(),
                        donor.getPosition().getY()
                );

                gc.setStroke(Color.web("#2a9d8f"));
                gc.setLineWidth(2.5);

                gc.strokeLine(
                        donor.getPosition().getX(),
                        donor.getPosition().getY(),
                        receiver.getPosition().getX(),
                        receiver.getPosition().getY()
                );

                double midX = (
                        base.getPosition().getX()
                                + donor.getPosition().getX()
                                + receiver.getPosition().getX()
                ) / 3.0;

                double midY = (
                        base.getPosition().getY()
                                + donor.getPosition().getY()
                                + receiver.getPosition().getY()
                ) / 3.0;

                gc.setFill(Color.web("#003049"));
                gc.setFont(Font.font("Arial", 11));
                gc.fillText(
                        String.format("Total %.1f", request.getEstimatedDistance()),
                        midX,
                        midY
                );
            }
        }
    }

    private void drawDelaunay(GraphicsContext gc) {
        gc.setStroke(Color.web("#5f6c72"));
        gc.setLineWidth(1.2);
        gc.setLineDashes(8);

        for (DelaunayTriangle triangle : mapData.getDelaunayTriangles()) {
            Point2D a = triangle.getA().getPosition();
            Point2D b = triangle.getB().getPosition();
            Point2D c = triangle.getC().getPosition();

            gc.strokeLine(a.getX(), a.getY(), b.getX(), b.getY());
            gc.strokeLine(b.getX(), b.getY(), c.getX(), c.getY());
            gc.strokeLine(c.getX(), c.getY(), a.getX(), a.getY());
        }

        gc.setLineDashes(null);
    }

    private void drawLegend(GraphicsContext gc) {
        double x = 25;
        double y = 600;

        gc.setFill(Color.rgb(255, 255, 255, 0.85));
        gc.fillRoundRect(x, y, 230, 80, 12, 12);

        gc.setStroke(Color.web("#cfd8dc"));
        gc.strokeRoundRect(x, y, 230, 80, 12, 12);

        gc.setFill(Color.web("#263238"));
        gc.setFont(Font.font("Arial", 13));
        gc.fillText("Légende", x + 12, y + 20);

        gc.setFill(Color.web("#1e6bd6"));
        gc.fillOval(x + 15, y + 32, 14, 14);
        gc.setFill(Color.web("#263238"));
        gc.fillText("Base de drones", x + 40, y + 44);

        gc.setFill(Color.web("#d62828"));
        gc.fillOval(x + 15, y + 52, 14, 14);
        gc.setFill(Color.web("#263238"));
        gc.fillText("Demande médicale", x + 40, y + 64);
    }
}