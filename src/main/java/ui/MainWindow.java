package ui;

import javafx.scene.layout.BorderPane;
import model.MapModel;

public class MainWindow extends BorderPane {

    public MainWindow(MapModel mapData) {

        MapCanvas canvas = new MapCanvas(mapData);

        setCenter(canvas);
    }
}
