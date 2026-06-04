package ui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import model.*;

public class MapCanvas extends Canvas {

    private MapModel mapData;

    public MapCanvas(MapModel mapData) {

        super(800, 700);

        this.mapData = mapData;

        draw();
    }

    private void draw() {

        GraphicsContext gc = getGraphicsContext2D();

        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, getWidth(), getHeight());

        drawHospitals(gc);
        drawDroneBases(gc);
    }

    private void drawDroneBases(GraphicsContext gc) {

        gc.setFill(Color.BLUE);

        for (DroneBase base : mapData.getDroneBases()) {

            double x = base.getPosition().getX();
            double y = base.getPosition().getY();

            gc.fillOval(x - 10, y - 10, 20, 20);

            gc.fillText(base.getName(), x + 15, y);
        }
    }

    private void drawHospitals(GraphicsContext gc) {

        gc.setFill(Color.GREEN);

        for (Hospital hospital : mapData.getHospitals()) {

            double x = hospital.getPosition().getX();
            double y = hospital.getPosition().getY();

            gc.fillRect(x - 10, y - 10, 20, 20);

            gc.fillText(hospital.getName(), x + 15, y);
        }
    }
}