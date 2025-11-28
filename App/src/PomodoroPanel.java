import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Date;

/* =====================================================
   Pomodoro Panel
   - Configurable durations
   - Start / Pause / Reset
   - Shows remaining time, session count, log of completed sessions
   ===================================================== */
class PomodoroPanel extends JPanel {
    private final JTextField workField = new JTextField("25", 3);
    private final JTextField shortBreakField = new JTextField("5", 3);
    private final JTextField longBreakField = new JTextField("15", 3);
    private final JButton startBtn = new JButton("Start");
    private final JButton pauseBtn = new JButton("Pause");
    private final JButton resetBtn = new JButton("Reset");
    private final JLabel timerLabel = new JLabel("25:00", SwingConstants.CENTER);
    private final JLabel statusLabel = new JLabel("Idle", SwingConstants.CENTER);
    private final DefaultListModel<String> sessionLog = new DefaultListModel<>();
    private final JList<String> logList = new JList<>(sessionLog);

    // state
    private javax.swing.Timer swingTimer;
    private int remainingSeconds = 0;
    private PomodoroState state = PomodoroState.IDLE;
    private int cyclesCompleted = 0; // count work sessions completed

    enum PomodoroState { IDLE, WORK, SHORT_BREAK, LONG_BREAK, PAUSED }

    PomodoroPanel() {
        setLayout(new BorderLayout(8,8));
        setBorder(new EmptyBorder(12,12,12,12));

        // Top: controls
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        controls.add(new JLabel("Work (min):"));
        controls.add(workField);
        controls.add(new JLabel("Short break (min):"));
        controls.add(shortBreakField);
        controls.add(new JLabel("Long break (min):"));
        controls.add(longBreakField);

        controls.add(startBtn);
        controls.add(pauseBtn);
        controls.add(resetBtn);

        add(controls, BorderLayout.NORTH);

        // Center: timer and status
        timerLabel.setFont(new Font("Monospaced", Font.BOLD, 48));
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        JPanel center = new JPanel(new BorderLayout());
        center.add(timerLabel, BorderLayout.CENTER);
        center.add(statusLabel, BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);

        // Right: session log
        JPanel right = new JPanel(new BorderLayout());
        right.setPreferredSize(new Dimension(300,0));
        right.add(new JLabel("Session Log"), BorderLayout.NORTH);
        right.add(new JScrollPane(logList), BorderLayout.CENTER);
        add(right, BorderLayout.EAST);

        // Button actions
        startBtn.addActionListener(e -> startPomodoro());
        pauseBtn.addActionListener(e -> pausePomodoro());
        resetBtn.addActionListener(e -> resetPomodoro());

        // timer
        swingTimer = new javax.swing.Timer(1000, e -> tick());
        updateTimerDisplay();
    }

    private void startPomodoro() {
        if (state == PomodoroState.PAUSED) {
            // resume
            swingTimer.start();
            state = previousStateBeforePause != null ? previousStateBeforePause : PomodoroState.WORK;
            statusLabel.setText("Resumed: " + state);
            return;
        }

        if (state == PomodoroState.IDLE) {
            // start work session
            int workMin = parseIntOrDefault(workField.getText().trim(), 25);
            remainingSeconds = workMin * 60;
            state = PomodoroState.WORK;
            statusLabel.setText("Work");
            swingTimer.start();
        }
    }

    private PomodoroState previousStateBeforePause = null;
    private void pausePomodoro() {
        if (state == PomodoroState.IDLE) return;
        if (state == PomodoroState.PAUSED) {
            // resume
            swingTimer.start();
            state = previousStateBeforePause != null ? previousStateBeforePause : PomodoroState.WORK;
            statusLabel.setText("Resumed: " + state);
            previousStateBeforePause = null;
        } else {
            // pause
            previousStateBeforePause = state;
            state = PomodoroState.PAUSED;
            swingTimer.stop();
            statusLabel.setText("Paused");
        }
    }

    private void resetPomodoro() {
        swingTimer.stop();
        state = PomodoroState.IDLE;
        remainingSeconds = 0;
        cyclesCompleted = 0;
        statusLabel.setText("Idle");
        timerLabel.setText(formatSec( parseIntOrDefault(workField.getText().trim(),25) * 60 ));
        sessionLog.clear();
    }

    private void tick() {
        if (remainingSeconds > 0) {
            remainingSeconds--;
            updateTimerDisplay();
            if (remainingSeconds == 0) {
                swingTimer.stop();
                handlePeriodEnd();
            }
        } else {
            // safety: start next automatically
            swingTimer.stop();
            handlePeriodEnd();
        }
    }

    private void handlePeriodEnd() {
        switch (state) {
            case WORK -> {
                cyclesCompleted++;
                sessionLog.addElement("Work done: " + new Date());
                // determine if long break after 4 cycles
                if (cyclesCompleted % 4 == 0) {
                    int longMin = parseIntOrDefault(longBreakField.getText().trim(), 15);
                    remainingSeconds = longMin * 60;
                    state = PomodoroState.LONG_BREAK;
                    statusLabel.setText("Long Break");
                } else {
                    int shortMin = parseIntOrDefault(shortBreakField.getText().trim(), 5);
                    remainingSeconds = shortMin * 60;
                    state = PomodoroState.SHORT_BREAK;
                    statusLabel.setText("Short Break");
                }
                swingTimer.start();
            }
            case SHORT_BREAK, LONG_BREAK -> {
                sessionLog.addElement("Break ended: " + new Date());
                // start next work automatically
                int workMin = parseIntOrDefault(workField.getText().trim(), 25);
                remainingSeconds = workMin * 60;
                state = PomodoroState.WORK;
                statusLabel.setText("Work");
                swingTimer.start();
            }
            default -> {
                state = PomodoroState.IDLE;
                statusLabel.setText("Idle");
            }
        }
        updateTimerDisplay();
    }

    private void updateTimerDisplay() {
        timerLabel.setText(formatSec(remainingSeconds == 0 ? parseIntOrDefault(workField.getText().trim(),25) * 60 : remainingSeconds));
    }

    private static int parseIntOrDefault(String s, int def) {
        try { return Integer.parseInt(s); } catch (Exception e) { return def; }
    }

    private static String formatSec(int sec) {
        if (sec < 0) sec = 0;
        int m = sec / 60;
        int s = sec % 60;
        return String.format("%02d:%02d", m, s);
    }
}
