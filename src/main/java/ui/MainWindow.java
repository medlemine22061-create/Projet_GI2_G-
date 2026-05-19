package ui;

import javafx.scene.layout.BorderPane;
import model.MapData;

public class MainWindow extends BorderPane {

    public MainWindow(MapData mapData) {

        MapCanvas canvas = new MapCanvas(mapData);

        setCenter(canvas);
    }
}
