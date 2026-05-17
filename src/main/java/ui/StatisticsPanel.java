package ui;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import model.*;

public class StatisticsPanel extends VBox {

    public StatisticsPanel(MapData mapData) {
        setSpacing(12);
        setPrefWidth(300);
        setStyle("-fx-padding: 20; -fx-background-color: #eef3f8;");

        getChildren().add(new Label("STATISTIQUES"));

        getChildren().add(new Label("Bases de drones : " + mapData.getDroneBases().size()));
        getChildren().add(new Label("Demandes médicales : " + mapData.getMedicalRequests().size()));
        getChildren().add(new Label("Hôpitaux : " + mapData.getHospitals().size()));
        getChildren().add(new Label("Triangles Delaunay : " + mapData.getDelaunayTriangles().size()));

        getChildren().add(new Label(""));

        for (VoronoiCell cell : mapData.getVoronoiCells()) {
            getChildren().add(new Label(
                    cell.getSite().getName()
                            + " : "
                            + cell.getRequestCount()
                            + " demande(s)"
            ));
        }
    }
}
