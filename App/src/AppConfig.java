import java.io.File;

public class AppConfig {
    // The name of the folder inside AppData
    public static final String APP_FOLDER_NAME = "Group67";

    public static File getProjectDirectory() {
        // 1. Get the system's APPDATA environment variable
        String appData = System.getenv("APPDATA");

        // Fallback for Mac/Linux (uses user home directory)
        if (appData == null) {
            appData = System.getProperty("user.home");
        }

        // 2. Construct the full path: C:\Users\Name\AppData\Roaming\Group67
        File dir = new File(appData, APP_FOLDER_NAME);

        // 3. Create the directory if it doesn't exist yet
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (created) {
                System.out.println("Created directory: " + dir.getAbsolutePath());
            }
        }

        return dir;
    }
}