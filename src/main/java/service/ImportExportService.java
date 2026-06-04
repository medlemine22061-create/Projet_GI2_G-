package service;

import model.MapModel;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Service used to import and export a complete map in binary format.
 */
public class ImportExportService {

    /**
     * Exports a complete map model into a binary file.
     *
     * @param map map to export
     * @param filePath output file path
     * @throws IOException if the file cannot be written
     */
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

    /**
     * Imports a complete map model from a binary file.
     *
     * @param filePath input file path
     * @return imported map model
     * @throws IOException if the file cannot be read
     * @throws ClassNotFoundException if the class is not found during deserialization
     */
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
}