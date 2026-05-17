package model;

import java.util.ArrayList;
import java.util.List;

public class MapData {

    private List<DroneBase> droneBases = new ArrayList<>();
    private List<MedicalRequest> medicalRequests = new ArrayList<>();
    private List<Hospital> hospitals = new ArrayList<>();
    private List<VoronoiCell> voronoiCells = new ArrayList<>();
    private List<DelaunayTriangle> delaunayTriangles = new ArrayList<>();
    private List<DeliveryMission> deliveryMissions = new ArrayList<>();

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

    public List<DeliveryMission> getDeliveryMissions() {
        return deliveryMissions;
    }

    public void addDroneBase(DroneBase droneBase) {
        droneBases.add(droneBase);
    }

    public void addMedicalRequest(MedicalRequest medicalRequest) {
        medicalRequests.add(medicalRequest);
    }

    public void addHospital(Hospital hospital) {
        hospitals.add(hospital);
    }

    public void addDeliveryMission(DeliveryMission deliveryMission) {
        deliveryMissions.add(deliveryMission);
    }

    public void clearAll() {
        droneBases.clear();
        medicalRequests.clear();
        hospitals.clear();
        voronoiCells.clear();
        delaunayTriangles.clear();
        deliveryMissions.clear();
    }
}