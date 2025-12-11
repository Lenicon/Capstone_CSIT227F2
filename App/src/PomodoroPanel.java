import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

class PomodoroPanel extends JPanel {

    private final BGM_Pomodoro player = new BGM_Pomodoro();
    private final String[] songs = {"Mondstadt", "Liyue", "Inazuma", "Sumeru", "Fontaine", "Custom..."};
    private final String[] songFiles = {
            "App/bgm_music/mondstadt.mp3",
            "App/bgm_music/liyue.mp3",
            "App/bgm_music/inazuma.mp3",
            "App/bgm_music/Sumeru.mp3",
            "App/bgm_music/fontaine.mp3",
            ""
    };
    private final JComboBox<String> songSelector = new JComboBox<>(songs);
    private String currentSongPath = songFiles[0];

    private final JTextField workField = new JTextField("25", 3);
    private final JTextField shortBreakField = new JTextField("5", 3);
    private final JTextField longBreakField = new JTextField("15", 3);
    private final JTextField cyclesBeforeLongBreakField = new JTextField("4", 2);

//    private final JTextField taskField = new JTextField(15);

    private final JButton startBtn = new JButton("Start");
    private final JButton resetBtn = new JButton("Reset");

//        JButton pauseBtn = new JButton("Pause");

    private final JLabel timerLabel = new JLabel("25:00", SwingConstants.CENTER);
    private final JLabel statusLabel = new JLabel("Idle", SwingConstants.CENTER);
    private final JLabel cycleCounterLabel = new JLabel("Cycle: 0/4", SwingConstants.CENTER);

//    private final JLabel currentTaskLabel = new JLabel("Current Task: No Active Task", SwingConstants.CENTER);

//    private final DefaultListModel<String> sessionLog = new DefaultListModel<>();

    private final javax.swing.Timer swingTimer;
    private int remainingSeconds = 0;
    private PomodoroState state = PomodoroState.IDLE;
    private int cyclesCompleted = 0;
    private PomodoroState previousStateBeforePause = null;

    //    private String currentTask = "No Active Task";
    private Date workStartTime;

    enum PomodoroState { IDLE, WORK, SHORT_BREAK, LONG_BREAK, PAUSED }

    PomodoroPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setBackground(Color.decode("#f5f5f5"));

        // Top Settings Panel
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        controls.setOpaque(false);

        controls.add(new JLabel("Work (min):"));
        workField.setFont(new Font("SansSerif", Font.PLAIN, 16));
        controls.add(workField);

        controls.add(new JLabel("Short (min):"));
        shortBreakField.setFont(new Font("SansSerif", Font.PLAIN, 16));
        controls.add(shortBreakField);

        controls.add(new JLabel("Long (min):"));
        longBreakField.setFont(new Font("SansSerif", Font.PLAIN, 16));
        controls.add(longBreakField);

        controls.add(new JLabel("Cycles:"));
        cyclesBeforeLongBreakField.setFont(new Font("SansSerif", Font.PLAIN, 16));
        controls.add(cyclesBeforeLongBreakField);

//        controls.add(new JLabel("Task:"));
//        taskField.setToolTipText("Enter your current task");
//        controls.add(taskField);
//        JButton setTaskBtn = new JButton("Set Task");
//        controls.add(setTaskBtn);

        add(controls, BorderLayout.NORTH);

        // Center Timer Panel
        JPanel center = new JPanel(new GridLayout(3, 1));
        center.setOpaque(false);

        timerLabel.setFont(new Font("Monospaced", Font.BOLD, 90));
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 32));
        cycleCounterLabel.setFont(new Font("SansSerif", Font.PLAIN, 20));

//        currentTaskLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
//        center.add(currentTaskLabel, BorderLayout.NORTH);

        center.add(statusLabel);
        center.add(timerLabel);
        center.add(cycleCounterLabel);

        add(center, BorderLayout.CENTER);

        // Right Panel (Log)
//        JPanel right = new JPanel(new BorderLayout(0, 5));
//        right.setPreferredSize(new Dimension(320, 0));
//        right.setBorder(BorderFactory.createTitledBorder("Session Log"));

//        JList<String> logList = new JList<>(sessionLog);
//        logList.setFont(new Font("Monospaced", Font.PLAIN, 13));
//        logList.setBackground(new Color(250, 250, 250));
//        JScrollPane logScroll = new JScrollPane(sessionLog);
//        logScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

//        right.add(logScroll, BorderLayout.CENTER);
//        add(right, BorderLayout.EAST);

        // Bottom Button Panel
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        bottomPanel.setOpaque(false);

        startBtn.setPreferredSize(new Dimension(100, 40));
        startBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        bottomPanel.add(startBtn);

//        controls.add(pauseBtn);

        resetBtn.setPreferredSize(new Dimension(100, 40));
        resetBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        bottomPanel.add(resetBtn);

        bottomPanel.add(new JLabel("Music:"));
        bottomPanel.add(songSelector);

        add(bottomPanel, BorderLayout.SOUTH);

        // Listeners
        startBtn.addActionListener(e -> {
            if (state == PomodoroState.IDLE){
                startPomodoro();
            } else{
                pausePomodoro();
            }
        });

//        pauseBtn.addActionListener(e -> {
//
//        });

        resetBtn.addActionListener(e -> resetPomodoro());

        songSelector.addActionListener(e -> {
            int index = songSelector.getSelectedIndex();
            if (index == songs.length - 1) {
                JFileChooser fileChooser = new JFileChooser();
                int result = fileChooser.showOpenDialog(this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    currentSongPath = fileChooser.getSelectedFile().getAbsolutePath();
                } else {
                    songSelector.setSelectedIndex(0);
                    currentSongPath = songFiles[0];
                }
            } else {
                currentSongPath = songFiles[index];
            }
            if (state == PomodoroState.WORK && !startBtn.getText().equals("Resume")) {
                player.stop();
                player.play(currentSongPath, true);
            }
        });

//        setTaskBtn.addActionListener(e -> {
//            String taskText = taskField.getText().trim();
//            if (!taskText.isEmpty()) {
//                currentTask = taskText;
//                currentTaskLabel.setText("Current Task: " + currentTask);
//                sessionLog.addElement("[" + getTimeStamp() + "] Task set: " + currentTask);
//                taskField.setText("");
//            }
//        });

        swingTimer = new javax.swing.Timer(1000, e -> tick());
        updateTimerDisplay();
    }

    private void startPomodoro() {
        if (state == PomodoroState.PAUSED) {
            swingTimer.start();
//            state = previousStateBeforePause != null ? previousStateBeforePause : PomodoroState.WORK;
            state = previousStateBeforePause != null ? previousStateBeforePause : PomodoroState.WORK;

            if (state == PomodoroState.WORK) {
                statusLabel.setText("WORK");
                player.play(currentSongPath, true);
            }
            startBtn.setText("Pause");
            return;
        }

        if (state == PomodoroState.IDLE) {
            int workMin = parseIntOrDefault(workField.getText().trim(), 25);
            remainingSeconds = workMin * 60;
            state = PomodoroState.WORK;
            statusLabel.setText("WORK");
            statusLabel.setForeground(new Color(34, 139, 34));

            updateCycleLabel();
            swingTimer.start();
            workStartTime = new Date();
            player.play(currentSongPath, true);
            startBtn.setText("Pause");
        }
    }

    private void pausePomodoro() {
        if (state == PomodoroState.IDLE) return;

        if (state == PomodoroState.PAUSED) {
            startBtn.setText("Pause");
            swingTimer.start();
            state = previousStateBeforePause != null ? previousStateBeforePause : PomodoroState.WORK;
            if (state == PomodoroState.WORK) {
                statusLabel.setText("WORK");
            } else if (state == PomodoroState.SHORT_BREAK){

                statusLabel.setText("Short Break");
            } else {

                statusLabel.setText("Long Break");
            }
            previousStateBeforePause = null;
        } else {
            startBtn.setText("Resume");
            previousStateBeforePause = state;
            state = PomodoroState.PAUSED;
            swingTimer.stop();
            player.stop();
            statusLabel.setText("Paused");
        }
        updateCycleLabel();
    }

    private void resetPomodoro() {
        swingTimer.stop();
        player.stop();
        state = PomodoroState.IDLE;
        remainingSeconds = 0;
        cyclesCompleted = 0;
        statusLabel.setText("Idle");
        statusLabel.setForeground(Color.BLACK);
        updateCycleLabel();
        timerLabel.setText(formatSec(parseIntOrDefault(workField.getText().trim(), 25) * 60));
        startBtn.setText("Start");

//        sessionLog.clear();
//        currentTask = "No Active Task";
//        currentTaskLabel.setText("Current Task: No Active Task");
//        taskField.setText("");
    }

    private void tick() {
        if (remainingSeconds > 0) {
            remainingSeconds--;
            updateTimerDisplay();
        } else {
            swingTimer.stop();
            handlePeriodEnd();
        }
    }

    private void handlePeriodEnd() {
        player.stop();

        if (state == PomodoroState.WORK) {
            cyclesCompleted++;
            int target = parseIntOrDefault(cyclesBeforeLongBreakField.getText().trim(), 4);

//                String logEntry = durationStr + " - " + currentTask;
//                sessionLog.addElement(logEntry);
//                sessionLog.addElement("  Completed cycle " + cyclesCompleted + " of " + target);

            if (cyclesCompleted % target == 0) {
                int longMin = parseIntOrDefault(longBreakField.getText().trim(), 15);
                remainingSeconds = longMin * 60;
                state = PomodoroState.LONG_BREAK;
                statusLabel.setText("Long Break");
                statusLabel.setForeground(Color.BLUE);
            } else {
                int shortMin = parseIntOrDefault(shortBreakField.getText().trim(), 5);
                remainingSeconds = shortMin * 60;
                state = PomodoroState.SHORT_BREAK;
                statusLabel.setText("Short Break");
                statusLabel.setForeground(Color.BLUE);
            }
            swingTimer.start();
            updateCycleLabel();
        }
        else if (state == PomodoroState.SHORT_BREAK || state == PomodoroState.LONG_BREAK) {
            int workMin = parseIntOrDefault(workField.getText().trim(), 25);
            remainingSeconds = workMin * 60;
            state = PomodoroState.WORK;
            statusLabel.setText("WORK");
            statusLabel.setForeground(new Color(34, 139, 34));

            swingTimer.start();
            workStartTime = new Date();
            updateCycleLabel();
            player.play(currentSongPath, true);
        }
        else {
            state = PomodoroState.IDLE;
            statusLabel.setText("Idle");
            updateCycleLabel();
        }
        updateTimerDisplay();
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