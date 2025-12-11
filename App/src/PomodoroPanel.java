import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

// PomodoroPanel must implement Serializable to save its state.
class PomodoroPanel extends JPanel implements Serializable {

    // --- SERIALIZATION & FILE HANDLING ---
    private static final long serialVersionUID = 1L;
    private static final String DATA_FILE_NAME = "pomodoro.dat";

    // Inner class to hold all mutable settings and state for serialization
    private static class Settings implements Serializable {
        private static final long serialVersionUID = 3L;
        String workMinutes = "25";
        String shortBreakMinutes = "5";
        String longBreakMinutes = "15";
        String cyclesBeforeLongBreak = "4";
        int songSelectorIndex = 0;
        String customSongPath = ""; // Path if "Custom..." is chosen

    }

//    // Timer state fields
//    int remainingSeconds = 0;
//    PomodoroState state = PomodoroState.IDLE;
//    int cyclesCompleted = 0;

    private final BGM_Pomodoro player = new BGM_Pomodoro();
    private final String[] songs = {"None", "Mondstadt", "Liyue", "Inazuma", "Sumeru", "Fontaine", "Custom..."};
    private final String[] songFiles = {
            "",
            "App/bgm_music/mondstadt.mp3",
            "App/bgm_music/liyue.mp3",
            "App/bgm_music/inazuma.mp3",
            "App/bgm_music/Sumeru.mp3",
            "App/bgm_music/fontaine.mp3",
            ""
    };
    private final JComboBox<String> songSelector = new JComboBox<>(songs);
    private String currentSongPath = songFiles[1];

    private final JTextField workField = new JTextField("25", 3);
    private final JTextField shortBreakField = new JTextField("5", 3);
    private final JTextField longBreakField = new JTextField("15", 3);
    private final JTextField cyclesBeforeLongBreakField = new JTextField("4", 2);

    private final JButton startBtn = new JButton("Start");
    private final JButton resetBtn = new JButton("Reset");

    private final JLabel timerLabel = new JLabel("25:00", SwingConstants.CENTER);
    private final JLabel statusLabel = new JLabel("Idle", SwingConstants.CENTER);
    private final JLabel cycleCounterLabel = new JLabel("Cycle: 0/4", SwingConstants.CENTER);

    private final javax.swing.Timer swingTimer;
    private int remainingSeconds = 0;
    private PomodoroState state = PomodoroState.IDLE;
    private int cyclesCompleted = 0;
    private PomodoroState previousStateBeforePause = null;
    private Date workStartTime;

    enum PomodoroState { IDLE, WORK, SHORT_BREAK, LONG_BREAK, PAUSED }

    PomodoroPanel() {
        // --- 1. Load settings immediately on startup ---
        loadSettings();

        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setBackground(Color.decode("#f5f5f5"));

        // Top Settings Panel
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        controls.setOpaque(false);

        // --- Fields and Listeners for Auto-Save ---
        FocusAdapter autoSaveAdapter = new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                saveSettings(); // Save settings when a text field loses focus
            }
        };

        controls.add(new JLabel("Work (min):"));
        workField.setFont(new Font("SansSerif", Font.PLAIN, 16));
        workField.addFocusListener(autoSaveAdapter); // Auto-save
        controls.add(workField);

        controls.add(new JLabel("Short (min):"));
        shortBreakField.setFont(new Font("SansSerif", Font.PLAIN, 16));
        shortBreakField.addFocusListener(autoSaveAdapter); // Auto-save
        controls.add(shortBreakField);

        controls.add(new JLabel("Long (min):"));
        longBreakField.setFont(new Font("SansSerif", Font.PLAIN, 16));
        longBreakField.addFocusListener(autoSaveAdapter); // Auto-save
        controls.add(longBreakField);

        controls.add(new JLabel("Cycles:"));
        cyclesBeforeLongBreakField.setFont(new Font("SansSerif", Font.PLAIN, 16));
        cyclesBeforeLongBreakField.addFocusListener(autoSaveAdapter); // Auto-save
        controls.add(cyclesBeforeLongBreakField);

        add(controls, BorderLayout.NORTH);

        // Center Timer Panel
        JPanel center = new JPanel(new GridLayout(3, 1));
        center.setOpaque(false);

        timerLabel.setFont(new Font("Monospaced", Font.BOLD, 90));
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 32));
        cycleCounterLabel.setFont(new Font("SansSerif", Font.PLAIN, 20));

        center.add(statusLabel);
        center.add(timerLabel);
        center.add(cycleCounterLabel);

        add(center, BorderLayout.CENTER);

        // Bottom Button Panel
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        bottomPanel.setOpaque(false);

        startBtn.setPreferredSize(new Dimension(100, 40));
        startBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        bottomPanel.add(startBtn);

        resetBtn.setPreferredSize(new Dimension(100, 40));
        resetBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        bottomPanel.add(resetBtn);

        bottomPanel.add(new JLabel("Music:"));
        bottomPanel.add(songSelector);

        add(bottomPanel, BorderLayout.SOUTH);

        // Listeners
        startBtn.addActionListener(e -> {
            if (state == PomodoroState.IDLE || state == PomodoroState.PAUSED) {
                startPomodoro();
            } else{
                pausePomodoro();
            }
        });

        resetBtn.addActionListener(e -> resetPomodoro());

        songSelector.addActionListener(e -> {
            updateSongPath(songSelector.getSelectedIndex());
            // --- AUTO SAVE AFTER SONG CHANGE ---
            saveSettings();

            if (state == PomodoroState.WORK && !startBtn.getText().equals("Resume")) {
                player.stop();
                player.play(currentSongPath, true);
            }
        });

        swingTimer = new javax.swing.Timer(1000, e -> tick());
        updateTimerDisplay();
        updateStatusDisplay(); // Initialize status and cycle labels correctly after loading
    }

    // --- NEW: Use AppConfig for File Path ---
    private File getStorageFile() {
        // Get the base directory from the centralized AppConfig class
        File appDir = AppConfig.getProjectDirectory();
        // Return the full path: %APPDATA%/Group67/pomodoro.dat
        return new File(appDir, DATA_FILE_NAME);
    }

    // --- NEW: Load Settings Method ---
    private void loadSettings() {
        File dataFile = getStorageFile();
        if (!dataFile.exists()) {
            // If no file exists, use defaults (which are set in the JTextFields by default)
//            System.out.println("No saved settings found. Using defaults.");
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(dataFile))) {
            Settings loadedSettings = (Settings) ois.readObject();

            // Load Timer Settings
            workField.setText(loadedSettings.workMinutes);
            shortBreakField.setText(loadedSettings.shortBreakMinutes);
            longBreakField.setText(loadedSettings.longBreakMinutes);
            cyclesBeforeLongBreakField.setText(loadedSettings.cyclesBeforeLongBreak);

            // Load Song Settings
            songSelector.setSelectedIndex(loadedSettings.songSelectorIndex);
            updateSongPath(loadedSettings.songSelectorIndex, loadedSettings.customSongPath);

            // Load Timer State
//            remainingSeconds = loadedSettings.remainingSeconds;
//            cyclesCompleted = loadedSettings.cyclesCompleted;
//            state = loadedSettings.state;

            // If the timer was running when saved, restart it immediately (optional)
            if (state == PomodoroState.WORK || state == PomodoroState.SHORT_BREAK || state == PomodoroState.LONG_BREAK) {
                // If loaded in a running state, set to PAUSED to allow the user to click Resume
                state = PomodoroState.PAUSED;
                startBtn.setText("Resume");
            } else {
                state = PomodoroState.IDLE;
                startBtn.setText("Start");
                remainingSeconds = parseIntOrDefault(workField.getText().trim(), 25) * 60; // Set initial time
            }

//            System.out.println("Settings loaded successfully.");

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading settings: " + e.getMessage());
            // Optionally delete corrupt file
            dataFile.delete();
        }
    }

    // --- NEW: Save Settings Method ---
    public void saveSettings() {
        Settings settingsToSave = new Settings();

        // Save Timer Settings
        settingsToSave.workMinutes = workField.getText().trim();
        settingsToSave.shortBreakMinutes = shortBreakField.getText().trim();
        settingsToSave.longBreakMinutes = longBreakField.getText().trim();
        settingsToSave.cyclesBeforeLongBreak = cyclesBeforeLongBreakField.getText().trim();

        // Save Song Settings
        settingsToSave.songSelectorIndex = songSelector.getSelectedIndex();
        settingsToSave.customSongPath = (settingsToSave.songSelectorIndex == songs.length - 1)
                ? currentSongPath
                : "";

        // Save Timer State
        // Only save state if not IDLE (or if PAUSED, keep remaining time)
//        if (state != PomodoroState.IDLE) {
//            settingsToSave.remainingSeconds = remainingSeconds;
//            settingsToSave.cyclesCompleted = cyclesCompleted;
//            settingsToSave.state = state;
//        } else {
//            // If Idle, reset state variables to default for next load
//            settingsToSave.remainingSeconds = 0;
//            settingsToSave.cyclesCompleted = 0;
//            settingsToSave.state = PomodoroState.IDLE;
//        }


        File dataFile = getStorageFile();
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(dataFile))) {
            oos.writeObject(settingsToSave);
            // System.out.println("Settings saved."); // Silent save
        } catch (IOException e) {
            System.err.println("Error saving settings: " + e.getMessage());
        }
    }

    // Unified method to update song path and handle custom selection
    private void updateSongPath(int index) {
        updateSongPath(index, null);
    }

    private void updateSongPath(int index, String loadedCustomPath) {
        if (index == songs.length - 1) { // "Custom..." selected
            String path = (loadedCustomPath != null && !loadedCustomPath.isEmpty())
                    ? loadedCustomPath
                    : getCustomSongPathFromUser();

            if (path == null || path.isEmpty()) {
                // User cancelled or selected invalid, revert to default
                songSelector.setSelectedIndex(0);
                currentSongPath = songFiles[0];
            } else {
                currentSongPath = path;
            }
        } else {
            currentSongPath = songFiles[index];
        }
    }

    private String getCustomSongPathFromUser() {
        JFileChooser fileChooser = new JFileChooser();
        // Set a filter for common audio files if needed
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile().getAbsolutePath();
        }
        return null;
    }


    private void startPomodoro() {
        if (state == PomodoroState.PAUSED) {
            swingTimer.start();
            state = previousStateBeforePause != null ? previousStateBeforePause : PomodoroState.WORK;

            if (state == PomodoroState.WORK) {
                player.play(currentSongPath, true);
            }
            updateStatusDisplay();
            startBtn.setText("Pause");
            return;
        }

        if (state == PomodoroState.IDLE) {
            int workMin = parseIntOrDefault(workField.getText().trim(), 25);
            remainingSeconds = workMin * 60;
            state = PomodoroState.WORK;
            updateStatusDisplay();

            updateCycleLabel();
            swingTimer.start();
            workStartTime = new Date();
            player.play(currentSongPath, true);
            startBtn.setText("Pause");

            // --- AUTO SAVE AFTER START ---
            saveSettings();
        }
    }

    private void pausePomodoro() {
        if (state == PomodoroState.IDLE) return;

        // Toggle pause state
        if (state != PomodoroState.PAUSED) {
            startBtn.setText("Resume");
            previousStateBeforePause = state;
            state = PomodoroState.PAUSED;
            swingTimer.stop();
            player.stop();

            updateStatusDisplay();

            // --- AUTO SAVE AFTER PAUSE ---
            saveSettings();
        } else {
            // This case is handled by startPomodoro()
        }
        updateCycleLabel();
    }

    private void resetPomodoro() {
        swingTimer.stop();
        player.stop();
        state = PomodoroState.IDLE;
        remainingSeconds = 0;
        cyclesCompleted = 0;
        updateStatusDisplay();
        updateCycleLabel();
        timerLabel.setText(formatSec(parseIntOrDefault(workField.getText().trim(), 25) * 60));
        startBtn.setText("Start");

        // --- AUTO SAVE AFTER RESET ---
        saveSettings();
    }

    private void tick() {
        if (remainingSeconds > 0) {
            remainingSeconds--;
            updateTimerDisplay();
            // Note: Saving every second is too much. Save only on state change.
        } else {
            swingTimer.stop();
            handlePeriodEnd();
            // --- AUTO SAVE AFTER PERIOD END ---
            saveSettings();
        }
    }

    private void handlePeriodEnd() {
        player.stop();

        if (state == PomodoroState.WORK) {
            cyclesCompleted++;
            int target = parseIntOrDefault(cyclesBeforeLongBreakField.getText().trim(), 4);

            if (cyclesCompleted % target == 0) {
                int longMin = parseIntOrDefault(longBreakField.getText().trim(), 15);
                remainingSeconds = longMin * 60;
                state = PomodoroState.LONG_BREAK;
                updateStatusDisplay();
            } else {
                int shortMin = parseIntOrDefault(shortBreakField.getText().trim(), 5);
                remainingSeconds = shortMin * 60;
                state = PomodoroState.SHORT_BREAK;
                updateStatusDisplay();
            }
            swingTimer.start();
            updateCycleLabel();
        }
        else if (state == PomodoroState.SHORT_BREAK || state == PomodoroState.LONG_BREAK) {
            int workMin = parseIntOrDefault(workField.getText().trim(), 25);
            remainingSeconds = workMin * 60;
            state = PomodoroState.WORK;
            updateStatusDisplay();

            swingTimer.start();
            workStartTime = new Date();
            updateCycleLabel();
            player.play(currentSongPath, true);
        }
        else {
            state = PomodoroState.IDLE;
            updateStatusDisplay();
            updateCycleLabel();
        }
        updateTimerDisplay();
        updateStatusDisplay();
    }

    private void updateStatusDisplay() {
        // Adjust status label color based on state
        switch (state) {
            case WORK:
                statusLabel.setForeground(new Color(34, 139, 34)); // Green
                statusLabel.setText("WORK");
                break;
            case SHORT_BREAK:
                statusLabel.setForeground(Color.BLUE);
                statusLabel.setText("SHORT BREAK");
                break;
            case LONG_BREAK:
                statusLabel.setForeground(Color.BLUE);
                statusLabel.setText("LONG BREAK");
                break;
            case PAUSED:
                statusLabel.setForeground(Color.RED);
                statusLabel.setText("PAUSED");
                break;
            case IDLE:
                statusLabel.setText("IDLE");
            default:
                statusLabel.setForeground(Color.BLACK);
                remainingSeconds = parseIntOrDefault(workField.getText().trim(), 25) * 60;
                break;
        }

    }

    private void updateTimerDisplay() {
        timerLabel.setText(formatSec(remainingSeconds));
    }

    private void updateCycleLabel() {
        int target = parseIntOrDefault(cyclesBeforeLongBreakField.getText().trim(), 4);
        cycleCounterLabel.setText("Cycle: " + cyclesCompleted + "/" + target);
    }

    private String getTimeStamp() {
        return new SimpleDateFormat("HH:mm:ss").format(new Date());
    }

    private static int parseIntOrDefault(String s, int def) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return def; }
    }

    private static String formatSec(int sec) {
        if (sec < 0) sec = 0;
        int m = sec / 60;
        int s = sec % 60;
        return String.format("%02d:%02d", m, s);
    }
}