import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.*;
import java.util.*;
import java.util.List;

/**
 * CollegeProductivityApp
 *
 * Single-file Swing app combining:
 * - Projects + To-do lists (with difficulty stars, standardized deadlines, sorting)
 * - Pomodoro timer (configurable, start/pause/reset, session log)
 * - Grade calculator (courses with weighted assignments)
 *
 * Compile & run:
 *   javac CollegeProductivityApp.java
 *   java CollegeProductivityApp
 *
 * Notes:
 * - Deadline format: MM/dd/yyyy
 * - Task difficulty: 0-3 stars
 */
public class CollegeProductivityApp {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame());
    }
}

/* -----------------------------
   Main application frame
   ----------------------------- */
class MainFrame extends JFrame {
    private final ProjectListPanel projectListPanel;
    private final ProjectTodoPanel projectTodoPanel;
    private final PomodoroPanel pomodoroPanel;
    private final GradesPanel gradesPanel;

    MainFrame() {
        super("College Productivity App");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLayout(new BorderLayout());

        // Left: Projects list (shared)
        projectListPanel = new ProjectListPanel(this);

        // Center: card panel switching between "Projects" view and others
        JTabbedPane tabs = new JTabbedPane();

        projectTodoPanel = new ProjectTodoPanel();
        pomodoroPanel = new PomodoroPanel();
        gradesPanel = new GradesPanel();

        tabs.addTab("Projects", wrapPanels(projectListPanel, projectTodoPanel));
        tabs.addTab("Pomodoro", pomodoroPanel);
        tabs.addTab("Grades", gradesPanel);

        add(tabs, BorderLayout.CENTER);

        // Create a sample project to start
        Project sample = new Project("Default Project");
        sample.tasks.add(new Task("Buy groceries", 1, ProjectTodoPanel.DATE_FMT.parseQuiet("01/30/2026")));
        sample.tasks.add(new Task("Finish homework", 2, ProjectTodoPanel.DATE_FMT.parseQuiet("01/31/2026")));
        projectListPanel.addProject(sample);
        projectListPanel.selectProject(sample);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    // Pass selected project to todo panel
    public void loadProject(Project p) {
        projectTodoPanel.loadProject(p);
    }

    // Utility: combine left and right panels for Projects tab
    private JPanel wrapPanels(JComponent left, JComponent right) {
        JPanel p = new JPanel(new BorderLayout());
        p.add(left, BorderLayout.WEST);
        p.add(right, BorderLayout.CENTER);
        return p;
    }
}

/* =====================================================
   Helpers: Date formatting wrapper for convenience
   ===================================================== */
class SafeDateFormat {
    private final SimpleDateFormat fmt;
    SafeDateFormat(String pattern) { fmt = new SimpleDateFormat(pattern); fmt.setLenient(false); }
    public Date parse(String s) throws ParseException { return fmt.parse(s); }
    public String format(Date d) { return fmt.format(d); }
    // convenience silent parse; returns today if fail
    public Date parseQuiet(String s) {
        try { return parse(s); } catch (Exception e) { return new Date(); }
    }
}

/* =====================================================
   Project & Task models
   ===================================================== */
class Project {
    String name;
    List<Task> tasks = new ArrayList<>();

    Project(String name) { this.name = name; }

    @Override
    public String toString() { return name; }
}

class Task {
    String name;
    int difficulty; // 0-3
    Date deadline;
    boolean completed = false;

    Task(String name, int difficulty, Date deadline) {
        this.name = name;
        this.difficulty = Math.max(0, Math.min(3, difficulty));
        this.deadline = deadline;
    }
}

/* =====================================================
   Left panel: Projects list
   ===================================================== */
class ProjectListPanel extends JPanel {
    private final DefaultListModel<Project> model = new DefaultListModel<>();
    private final JList<Project> list = new JList<>(model);
    private final MainFrame parent;

    ProjectListPanel(MainFrame parent) {
        this.parent = parent;
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(260, 600));
        setBorder(BorderFactory.createTitledBorder("Projects"));

        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> lst, Object value, int idx, boolean sel, boolean focus) {
                super.getListCellRendererComponent(lst, value, idx, sel, focus);
                if (value instanceof Project p) setText(p.name);
                return this;
            }
        });

        list.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Project p = list.getSelectedValue();
                if (p != null) parent.loadProject(p);
            }
        });

        JPanel bottom = new JPanel(new GridLayout(3, 1, 6, 6));
        bottom.setBorder(new EmptyBorder(6,6,6,6));
        JButton add = new JButton("Add Project");
        JButton rename = new JButton("Rename Project");
        JButton remove = new JButton("Remove Project");

        add.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(this, "Project name:");
            if (name != null && !name.trim().isEmpty()) {
                Project p = new Project(name.trim());
                model.addElement(p);
                list.setSelectedValue(p, true);
            }
        });

        rename.addActionListener(e -> {
            Project p = list.getSelectedValue();
            if (p == null) { JOptionPane.showMessageDialog(this, "Select a project first."); return; }
            String name = JOptionPane.showInputDialog(this, "New name:", p.name);
            if (name != null && !name.trim().isEmpty()) { p.name = name.trim(); list.repaint(); }
        });

        remove.addActionListener(e -> {
            Project p = list.getSelectedValue();
            if (p == null) { JOptionPane.showMessageDialog(this, "Select a project first."); return; }
            int ok = JOptionPane.showConfirmDialog(this, "Remove project \"" + p.name + "\"?","Confirm",JOptionPane.YES_NO_OPTION);
            if (ok == JOptionPane.YES_OPTION) { model.removeElement(p); }
        });

        bottom.add(add); bottom.add(rename); bottom.add(remove);

        add(new JScrollPane(list), BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    public void addProject(Project p) { model.addElement(p); }
    public void selectProject(Project p) { list.setSelectedValue(p, true); }
}

/* =====================================================
   RIGHT: Project Todo Panel (fixed header, fixed rows, scroll)
   ===================================================== */
class ProjectTodoPanel extends JPanel {
    static final SafeDateFormat DATE_FMT = new SafeDateFormat("MM/dd/yyyy");

    private Project currentProject = null;

    // UI pieces
    private final JProgressBar progressBar = new JProgressBar();
    private final JComboBox<String> sortMode = new JComboBox<>(new String[]{"Sort: Name", "Sort: Deadline", "Sort: Difficulty"});
    private final JButton addTaskButton = new JButton("Add Task");
    private final JPanel taskListPanel = new JPanel(); // contains rows, scrollable

    ProjectTodoPanel() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(8,8,8,8));

        // Top: progress + controls + fixed header
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        progressBar.setStringPainted(true);
        progressBar.setValue(0);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controls.add(addTaskButton);
        controls.add(sortMode);

        topContainer.add(progressBar);
        topContainer.add(controls);
        topContainer.add(createHeaderRow()); // fixed header

        add(topContainer, BorderLayout.NORTH);

        // Task list in scroll pane
        taskListPanel.setLayout(new BoxLayout(taskListPanel, BoxLayout.Y_AXIS));
        JScrollPane scroll = new JScrollPane(taskListPanel);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(scroll, BorderLayout.CENTER);

        // handlers
        addTaskButton.addActionListener(e -> openAddTaskDialog());
        sortMode.addActionListener(e -> refreshTasks());
    }

    public void loadProject(Project p) {
        currentProject = p;
        refreshTasks();
    }

    /* ---------- UI builders ---------- */
    private JPanel createHeaderRow() {
        JPanel row = new JPanel(new GridLayout(1,4));
        row.setPreferredSize(new Dimension(800, 30));
        row.add(wrapLabel("Task"));
        row.add(wrapLabel("Difficulty"));
        row.add(wrapLabel("Deadline"));
        row.add(wrapLabel("Actions"));
        row.setBorder(BorderFactory.createMatteBorder(0,0,2,0,Color.DARK_GRAY));
        return row;
    }

    private JLabel wrapLabel(String text) {
        JLabel l = new JLabel(text);
        l.setBorder(new EmptyBorder(4,6,4,6));
        return l;
    }

    private JPanel createTaskRow(Task t) {
        JPanel row = new JPanel(new GridLayout(1,4,4,4));
        row.setBorder(new EmptyBorder(6,6,6,6));

        JLabel name = new JLabel(t.name);
        name.setToolTipText(t.name);

        JLabel diff = new JLabel(stars(t.difficulty));
        diff.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel dl = new JLabel(DATE_FMT.format(t.deadline));
        dl.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 2));
        JButton finish = new JButton("Finish");
        JButton edit = new JButton("Edit");
        JButton remove = new JButton("Remove");

        finish.addActionListener(e -> {
            t.completed = true;
            refreshTasks();
        });

        edit.addActionListener(e -> openEditTaskDialog(t));

        remove.addActionListener(e -> {
            int ok = JOptionPane.showConfirmDialog(this, "Delete task \""+t.name+"\"?","Confirm",JOptionPane.YES_NO_OPTION);
            if (ok == JOptionPane.YES_OPTION) {
                currentProject.tasks.remove(t);
                refreshTasks();
            }
        });

        actions.add(finish);
        actions.add(edit);
        actions.add(remove);

        row.add(name);
        row.add(diff);
        row.add(dl);
        row.add(actions);

        return row;
    }

    // Ensure each row has fixed height by wrapping with fixed-size container
    private JPanel wrapFixedHeight(JPanel content) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(content, BorderLayout.CENTER);
        wrapper.setPreferredSize(new Dimension(800, 48));
        wrapper.setMinimumSize(new Dimension(800, 48));
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        return wrapper;
    }

    /* ---------- Dialogs ---------- */
    private void openAddTaskDialog() {
        if (currentProject == null) { JOptionPane.showMessageDialog(this, "Select a project first."); return; }

        JTextField nameField = new JTextField(18);
        JTextField deadlineField = new JTextField(10);
        JComboBox<Integer> difficultyBox = new JComboBox<>(new Integer[]{0,1,2,3});

        JPanel panel = new JPanel(new GridLayout(3,2,8,8));
        panel.add(new JLabel("Task name:")); panel.add(nameField);
        panel.add(new JLabel("Deadline (MM/dd/yyyy):")); panel.add(deadlineField);
        panel.add(new JLabel("Difficulty (0-3):")); panel.add(difficultyBox);

        int res = JOptionPane.showConfirmDialog(this, panel, "Add Task", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;

        String name = nameField.getText().trim();
        String dt = deadlineField.getText().trim();
        int diff = (Integer) difficultyBox.getSelectedItem();

        if (name.isEmpty()) { JOptionPane.showMessageDialog(this, "Task name cannot be empty."); return; }
        Date deadline;
        try { deadline = DATE_FMT.parse(dt); }
        catch (Exception ex) { JOptionPane.showMessageDialog(this, "Invalid date. Use MM/dd/yyyy."); return; }

        currentProject.tasks.add(new Task(name, diff, deadline));
        refreshTasks();
    }

    private void openEditTaskDialog(Task t) {
        JTextField nameField = new JTextField(t.name,18);
        JTextField deadlineField = new JTextField(DATE_FMT.format(t.deadline),10);
        JComboBox<Integer> difficultyBox = new JComboBox<>(new Integer[]{0,1,2,3});
        difficultyBox.setSelectedItem(t.difficulty);

        JPanel panel = new JPanel(new GridLayout(3,2,8,8));
        panel.add(new JLabel("Task name:")); panel.add(nameField);
        panel.add(new JLabel("Deadline (MM/dd/yyyy):")); panel.add(deadlineField);
        panel.add(new JLabel("Difficulty (0-3):")); panel.add(difficultyBox);

        int res = JOptionPane.showConfirmDialog(this, panel, "Edit Task", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;

        String name = nameField.getText().trim();
        String dt = deadlineField.getText().trim();
        int diff = (Integer) difficultyBox.getSelectedItem();

        if (name.isEmpty()) { JOptionPane.showMessageDialog(this, "Task name cannot be empty."); return; }
        Date deadline;
        try { deadline = DATE_FMT.parse(dt); } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Invalid date."); return; }

        t.name = name; t.deadline = deadline; t.difficulty = diff;
        refreshTasks();
    }

    /* ---------- Refresh & Sorting ---------- */
    private void refreshTasks() {
        taskListPanel.removeAll();

        if (currentProject == null) {
            progressBar.setValue(0);
            progressBar.setString("No project selected");
            revalidate(); repaint();
            return;
        }

        // sorting
        switch (sortMode.getSelectedIndex()) {
            case 1 -> currentProject.tasks.sort(Comparator.comparing(task -> task.deadline));
            case 2 -> currentProject.tasks.sort(Comparator.comparingInt(task -> task.difficulty));
            default -> currentProject.tasks.sort(Comparator.comparing(task -> task.name.toLowerCase()));
        }

        // add rows
        int completed = 0;
        for (Task t : currentProject.tasks) {
            JPanel row = createTaskRow(t);
            taskListPanel.add(wrapFixedHeight(row));
            if (t.completed) completed++;
        }

        int total = currentProject.tasks.size();
        int pct = total == 0 ? 0 : (int)((completed / (double) total) * 100);
        progressBar.setValue(pct);
        progressBar.setString(pct + "% completed");

        revalidate(); repaint();
    }

    private static String stars(int n) {
        n = Math.max(0, Math.min(3, n));
        return "★".repeat(n) + "☆".repeat(3-n);
    }
}

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

/* =====================================================
   Grades Panel
   - courses stored in map: name -> Course
   - course has assignments (name, weight (0..1), grade (0..100))
   - compute weighted average and "needed to reach target"
   ===================================================== */
class GradesPanel extends JPanel {
    private final Map<String, Course> courses = new LinkedHashMap<>();
    private final DefaultListModel<String> courseListModel = new DefaultListModel<>();
    private final JList<String> courseList = new JList<>(courseListModel);

    // right side: show assignments and controls
    private final DefaultListModel<String> assignmentModel = new DefaultListModel<>();
    private final JList<String> assignmentList = new JList<>(assignmentModel);

    private final JLabel courseAvgLabel = new JLabel("Course average: N/A");
    private final JLabel gpaLabel = new JLabel("Simple GPA (A-F mapping): N/A");

    GradesPanel() {
        setLayout(new BorderLayout(8,8));
        setBorder(new EmptyBorder(8,8,8,8));

        // Left: courses
        JPanel left = new JPanel(new BorderLayout(6,6));
        left.setPreferredSize(new Dimension(260,0));
        left.add(new JLabel("Courses"), BorderLayout.NORTH);
        left.add(new JScrollPane(courseList), BorderLayout.CENTER);

        JPanel courseButtons = new JPanel(new GridLayout(3,1,6,6));
        JButton addCourse = new JButton("Add Course");
        JButton renameCourse = new JButton("Rename Course");
        JButton deleteCourse = new JButton("Delete Course");
        courseButtons.add(addCourse); courseButtons.add(renameCourse); courseButtons.add(deleteCourse);
        left.add(courseButtons, BorderLayout.SOUTH);

        add(left, BorderLayout.WEST);

        // Center: assignments and stats
        JPanel center = new JPanel(new BorderLayout(6,6));
        center.add(new JLabel("Assignments"), BorderLayout.NORTH);
        center.add(new JScrollPane(assignmentList), BorderLayout.CENTER);

        JPanel assignBtns = new JPanel(new GridLayout(1,3,6,6));
        JButton addAssign = new JButton("Add Assignment");
        JButton editAssign = new JButton("Edit Assignment");
        JButton removeAssign = new JButton("Remove Assignment");
        assignBtns.add(addAssign); assignBtns.add(editAssign); assignBtns.add(removeAssign);

        center.add(assignBtns, BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);

        // Right: stats and calculators
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.add(courseAvgLabel);
        right.add(Box.createVerticalStrut(6));
        right.add(gpaLabel);
        right.add(Box.createVerticalStrut(12));

        JButton computeProjected = new JButton("Projected Grade (with hypothetical)");
        right.add(computeProjected);

        add(right, BorderLayout.EAST);

        // Handlers
        addCourse.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(this, "Course name:");
            if (name != null && !name.trim().isEmpty()) {
                Course c = new Course(name.trim());
                courses.put(c.name, c);
                courseListModel.addElement(c.name);
                courseList.setSelectedValue(c.name, true);
            }
        });

        renameCourse.addActionListener(e -> {
            String sel = courseList.getSelectedValue();
            if (sel == null) { JOptionPane.showMessageDialog(this,"Select a course first."); return; }
            String n = JOptionPane.showInputDialog(this,"New name:",sel);
            if (n != null && !n.trim().isEmpty()) {
                Course c = courses.remove(sel);
                c.name = n.trim();
                courses.put(c.name, c);
                int idx = courseList.getSelectedIndex();
                courseListModel.set(idx, c.name);
            }
        });

        deleteCourse.addActionListener(e -> {
            String sel = courseList.getSelectedValue();
            if (sel == null) { JOptionPane.showMessageDialog(this,"Select a course first."); return; }
            int ok = JOptionPane.showConfirmDialog(this, "Delete course "+sel+"?","Confirm",JOptionPane.YES_NO_OPTION);
            if (ok == JOptionPane.YES_OPTION) {
                courses.remove(sel);
                courseListModel.removeElement(sel);
                assignmentModel.clear();
                courseAvgLabel.setText("Course average: N/A");
            }
        });

        courseList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) loadCourse(courseList.getSelectedValue());
        });

        addAssign.addActionListener(e -> addOrEditAssignment(null));
        editAssign.addActionListener(e -> {
            Course c = currentCourse();
            int idx = assignmentList.getSelectedIndex();
            if (c == null || idx < 0) { JOptionPane.showMessageDialog(this,"Select an assignment."); return; }
            addOrEditAssignment(c.assignments.get(idx));
        });

        removeAssign.addActionListener(e -> {
            Course c = currentCourse();
            int idx = assignmentList.getSelectedIndex();
            if (c == null || idx < 0) { JOptionPane.showMessageDialog(this,"Select an assignment."); return; }
            int ok = JOptionPane.showConfirmDialog(this,"Remove assignment?","Confirm",JOptionPane.YES_NO_OPTION);
            if (ok == JOptionPane.YES_OPTION) {
                c.assignments.remove(idx);
                loadCourse(c.name);
            }
        });

        computeProjected.addActionListener(e -> {
            Course c = currentCourse();
            if (c == null) { JOptionPane.showMessageDialog(this,"Select a course."); return; }
            String w = JOptionPane.showInputDialog(this,"Enter weight remaining (0.0-1.0) for future work (e.g. 0.3):","0.3");
            String target = JOptionPane.showInputDialog(this,"Target overall grade (0-100):","90");
            try {
                double remWeight = Double.parseDouble(w);
                double targetGrade = Double.parseDouble(target);
                double currentWeighted = c.currentWeightedScore();
                double currentWeight = c.currentWeight();
                double needed = (targetGrade - currentWeighted) / remWeight;
                String msg = String.format("Current weighted: %.2f (%.0f%%). To reach %.2f overall, you need %.2f average on remaining %.0f%%.",
                        currentWeighted, currentWeight*100, targetGrade, needed, remWeight*100);
                JOptionPane.showMessageDialog(this, msg);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid input.");
            }
        });

        // init sample
        Course sample = new Course("Computer Science 101");
        sample.assignments.add(new Assignment("Homework 1", 0.05, 88));
        sample.assignments.add(new Assignment("Midterm", 0.3, 82));
        courses.put(sample.name, sample);
        courseListModel.addElement(sample.name);
    }

    private Course currentCourse() {
        String sel = courseList.getSelectedValue();
        if (sel == null) return null;
        return courses.get(sel);
    }

    private void loadCourse(String name) {
        assignmentModel.clear();
        Course c = courses.get(name);
        if (c == null) { courseAvgLabel.setText("Course average: N/A"); return; }
        for (Assignment a : c.assignments) assignmentModel.addElement(a.toDisplayString());
        double avg = c.currentWeightedScore();
        courseAvgLabel.setText(String.format("Course weighted: %.2f (weight %.0f%%)", avg, c.currentWeight()*100));
        gpaLabel.setText("Simple grade: " + letterGrade(avg));
    }

    private String letterGrade(double pct) {
        if (pct >= 93) return "A";
        if (pct >= 90) return "A-";
        if (pct >= 87) return "B+";
        if (pct >= 83) return "B";
        if (pct >= 80) return "B-";
        if (pct >= 77) return "C+";
        if (pct >= 70) return "C";
        if (pct >= 60) return "D";
        return "F";
    }

    private void addOrEditAssignment(Assignment existing) {
        Course c = currentCourse();
        if (c == null) { JOptionPane.showMessageDialog(this, "Select a course first."); return; }

        JTextField name = new JTextField(existing == null ? "" : existing.name, 16);
        JTextField weight = new JTextField(existing == null ? "0.10" : String.valueOf(existing.weight), 6);
        JTextField grade = new JTextField(existing == null ? "0" : String.valueOf(existing.grade), 6);

        JPanel panel = new JPanel(new GridLayout(3,2,6,6));
        panel.add(new JLabel("Assignment name:")); panel.add(name);
        panel.add(new JLabel("Weight (0.0-1.0):")); panel.add(weight);
        panel.add(new JLabel("Grade (0-100):")); panel.add(grade);

        int res = JOptionPane.showConfirmDialog(this, panel, existing == null ? "Add Assignment" : "Edit Assignment",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;

        try {
            String nm = name.getText().trim();
            double w = Double.parseDouble(weight.getText().trim());
            double g = Double.parseDouble(grade.getText().trim());
            if (nm.isEmpty()) { JOptionPane.showMessageDialog(this, "Name required."); return; }
            if (existing == null) {
                c.assignments.add(new Assignment(nm, w, g));
            } else {
                existing.name = nm; existing.weight = w; existing.grade = g;
            }
            loadCourse(c.name);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid values. Weight should be decimal (0.0-1.0).");
        }
    }

    /* Course & Assignment structures */
    static class Course {
        String name;
        List<Assignment> assignments = new ArrayList<>();
        Course(String name) { this.name = name; }

        // current weighted score (sum grade * weight)
        double currentWeightedScore() {
            double s = 0;
            for (Assignment a : assignments) s += a.grade * a.weight;
            // total weight assumed as fraction; return as percentage score scaled by total weight
            return s / Math.max(1e-9, 1.0); // note: returning raw weighted sum over 0..100
        }

        double currentWeight() {
            double w = 0;
            for (Assignment a : assignments) w += a.weight;
            return Math.min(1.0, w);
        }
    }

    static class Assignment {
        String name;
        double weight; // 0..1
        double grade;  // 0..100
        Assignment(String name, double weight, double grade) { this.name = name; this.weight = weight; this.grade = grade; }
        String toDisplayString() { return String.format("%s — w: %.2f — g: %.2f", name, weight, grade); }
    }
}