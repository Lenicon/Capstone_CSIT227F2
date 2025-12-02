import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ProjectFileHandler {
    /**
     * Scans %APPDATA%/Group67/ for all .dat files and loads them.
     * @return List of Project objects sorted by Creation Date (Newest first).
     */
    public List<Project> loadAllProjects() {
        List<Project> projects = new ArrayList<>();
        File dir = AppConfig.getProjectDirectory();

        // 1. Filter for .dat files only
        File[] files = dir.listFiles((d, name) -> name.endsWith(".dat"));

        if (files != null) {
            for (File file : files) {
                try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
                    // 2. Deserialize the ENTIRE object (Name, ID, Date, Tasks)
                    Project p = (Project) in.readObject();
                    projects.add(p);
                } catch (Exception e) {
                    System.err.println("Failed to load file: " + file.getName());
                    e.printStackTrace(); // Skip corrupted files
                }
            }
        }

        // 3. Sort by creation date (Newest on top)
        projects.sort(Comparator.comparing(Project::getCreationDate).reversed());

        return projects;
    }

    /**
     * Saves a specific project to [UUID].dat
     */
    public void saveProject(Project p) {
        File dir = AppConfig.getProjectDirectory();
        // Use the Project's UUID for the filename
        File file = new File(dir, p.getFileName()); // e.g., "550e8400-e29b....dat"

        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(p);
//            System.out.println("Saved project: " + p.getName());
        } catch (IOException e) {
            System.err.println("Error saving project: " + p.getName());
            e.printStackTrace();
        }
    }

    /**
     * Deletes the specific [UUID].dat file
     */
    public void deleteProject(Project p) {
        File dir = AppConfig.getProjectDirectory();
        File file = new File(dir, p.getFileName());

        if (file.exists()) {
            if (file.delete()) {
//                System.out.println("Deleted file: " + file.getName());
            } else {
                System.err.println("Failed to delete file: " + file.getName());
            }
        }
    }
}