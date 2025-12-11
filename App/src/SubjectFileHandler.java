import javax.swing.DefaultListModel;
import java.io.*;

public class SubjectFileHandler {

    private static final String SUBJECTS_FILE_NAME = "subjects.dat";

    /**
     * Determines the full path to the subjects.dat file inside the Group67 directory 
     * defined by AppConfig.
     * * @return The File object pointing to the storage location.
     */
    private File getStorageFile() {
        // Use AppConfig to get the base directory (%APPDATA%/Group67)
        File appDir = AppConfig.getProjectDirectory();

        // Construct the full path: .../Group67/subjects.dat
        return new File(appDir, SUBJECTS_FILE_NAME);
    }

    /**
     * Saves the entire DefaultListModel of subjects to disk via Serialization.
     * * @param model The DefaultListModel containing Subject objects to save.
     * @throws IOException If there is an error writing the file.
     */
    public void saveSubjects(DefaultListModel<Subject> model) throws IOException {
        File dataFile = getStorageFile();
        // Use try-with-resources for automatic closing of streams
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(dataFile))) {
            oos.writeObject(model);
        }
    }

    /**
     * Loads the DefaultListModel of subjects from disk via Deserialization.
     * * @return The loaded DefaultListModel, or a new empty model if the file 
     * does not exist or is empty.
     * @throws ClassNotFoundException If the serialized class structure has changed.
     * @throws IOException If there is an error reading the file (e.g., corrupted data).
     */
    @SuppressWarnings("unchecked")
    public DefaultListModel<Subject> loadSubjects() throws IOException, ClassNotFoundException {
        File dataFile = getStorageFile();

        // Check if the file exists or is empty before attempting to read
        if (!dataFile.exists() || dataFile.length() == 0) {
            return new DefaultListModel<>(); // Return empty model
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(dataFile))) {
            // Read the whole serialized DefaultListModel object
            return (DefaultListModel<Subject>) ois.readObject();
        } catch (InvalidClassException | StreamCorruptedException e) {
            // Handle cases where the class structure changed or the file is corrupted
            System.err.println("Warning: Corrupt or incompatible data file found. Creating new file.");
            dataFile.delete(); // Delete the corrupt file
            throw e; // Re-throw to signal a load failure to the caller (GWACalculator)
        }
    }
}