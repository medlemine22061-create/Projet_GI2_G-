package model;

import java.util.ArrayList;
import java.util.List;

public class MapData {

    private List<DroneBase> droneBases = new ArrayList<>();
    private List<MedicalRequest> medicalRequests = new ArrayList<>();
    private List<Hospital> hospitals = new ArrayList<>();
    private List<VoronoiCell> voronoiCells = new ArrayList<>();
    private List<DelaunayTriangle> delaunayTriangles = new ArrayList<>();

    public List<DroneBase> getDroneBases() {
        return droneBases;
    }

    public List<MedicalRequest> getMedicalRequests() {
        return medicalRequests;
    }

    public List<Hospital> getHospitals() {
        return hospitals;
    }

    public List<VoronoiCell> getVoronoiCells() {
        return voronoiCells;
    }

    public void setVoronoiCells(List<VoronoiCell> voronoiCells) {
        this.voronoiCells = voronoiCells;
    }

    public List<DelaunayTriangle> getDelaunayTriangles() {
        return delaunayTriangles;
    }

    public void setDelaunayTriangles(List<DelaunayTriangle> delaunayTriangles) {
        this.delaunayTriangles = delaunayTriangles;
    }
}
