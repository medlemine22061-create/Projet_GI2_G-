package service;

import model.CollectionCenter;
import model.Hospital;
import model.MapModel;
import model.Position;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;

/**
 * Service used to import and export a complete map in binary format,
 * and to import medical sites in bulk from a CSV file.
 */
public class ImportExportService {

    public void exportMap(MapModel map, String filePath) throws IOException {
        if (map == null) {
            throw new IllegalArgumentException("map cannot be null");
        }

        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("filePath cannot be null or blank");
        }

        try (ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(filePath))) {
            output.writeObject(map);
        }
    }

    public MapModel importMap(String filePath) throws IOException, ClassNotFoundException {
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("filePath cannot be null or blank");
        }

        try (ObjectInputStream input = new ObjectInputStream(new FileInputStream(filePath))) {
            Object object = input.readObject();

            if (!(object instanceof MapModel)) {
                throw new IOException("The file does not contain a MapModel");
            }

            return (MapModel) object;
        }
    }

    /**
     * Imports medical sites from a CSV file.
     *
     * Hospital line:
     * H,H1,Hospital Nord,100,100
     *
     * Collection center line:
     * C,C1,Collection Center Est,200,500,Kidney|Heart|Liver
     */
    public void importMedicalSitesFromCsv(MapModel map, String filePath) throws IOException {
        if (map == null) {
            throw new IllegalArgumentException("map cannot be null");
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }

                String[] parts = line.split(",");

                if (parts.length < 5) {
                    continue;
                }

                String type = parts[0].trim();
                String id = parts[1].trim();
                String name = parts[2].trim();
                double x = Double.parseDouble(parts[3].trim());
                double y = Double.parseDouble(parts[4].trim());

                if (type.equalsIgnoreCase("H")) {
                    map.addMedicalSite(new Hospital(id, name, new Position(x, y), true));
                } else if (type.equalsIgnoreCase("C")) {
                    if (parts.length >= 6) {
                        String[] organs = parts[5].trim().split("\\|");
                        map.addMedicalSite(new CollectionCenter(
                                id,
                                name,
                                new Position(x, y),
                                Arrays.asList(organs)
                        ));
                    } else {
                        map.addMedicalSite(new CollectionCenter(
                                id,
                                name,
                                new Position(x, y),
                                Arrays.asList("Kidney")
                        ));
                    }
                }
            }
        }
    }
}