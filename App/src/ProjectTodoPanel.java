import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Comparator;
import java.util.Date;
// TODO: ADD DEPENDENCY, WE MIGHT NEED TO LOOK INTO MAVEN
import com.toedter.calendar.JDateChooser;

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
        JPanel row = new JPanel(new GridLayout(1,4,4,4));
        row.setBorder(new EmptyBorder(6,6,6,6));
        row.setPreferredSize(new Dimension(800, 30));
        row.add(wrapLabel("Task"));
        row.add(wrapLabel(""));
        row.add(wrapLabel("Difficulty"));
        row.add(wrapLabel("                            Deadline"));
        row.add(wrapLabel(""));
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
        JPanel row = new JPanel(new GridLayout(1,6,4,4));
        row.setBorder(new EmptyBorder(6,6,6,6));

        JLabel name = new JLabel(t.getName());
        name.setToolTipText(t.getName());
        name.setVerticalAlignment(SwingConstants.TOP);

        JLabel diff = new JLabel(t.stars());
        diff.setHorizontalAlignment(SwingConstants.CENTER);
        diff.setVerticalAlignment(1);

        JLabel dl = new JLabel(t.getDeadlineString());
        dl.setHorizontalAlignment(SwingConstants.CENTER);
        dl.setVerticalAlignment(SwingConstants.TOP);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 2));
        JButton finish = new JButton("Finish");
        JButton edit = new JButton("Edit");
        JButton remove = new JButton("Remove");

        finish.addActionListener(e -> {
            t.setCompleted(true);
            refreshTasks();
        });

        edit.addActionListener(e -> openEditTaskDialog(t));

        remove.addActionListener(e -> {
            int ok = JOptionPane.showConfirmDialog(this, "Delete task \""+t.getName()+"\"?","Confirm",JOptionPane.YES_NO_OPTION);
            if (ok == JOptionPane.YES_OPTION) {
                currentProject.tasks.remove(t);
                refreshTasks();
            }
        });

        actions.add(finish);
        actions.add(edit);
        actions.add(remove);
        actions.setAlignmentY((float)0.0);
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
        wrapper.setPreferredSize(new Dimension(800, 68));
        wrapper.setMinimumSize(new Dimension(800, 68));
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));
        return wrapper;
    }

    /* ---------- Dialogs ---------- */
    private void openAddTaskDialog() {
        if (currentProject == null) { JOptionPane.showMessageDialog(this, "Select a project first."); return; }

        JTextField nameField = new JTextField(18);
        JDateChooser deadlineChooser = new JDateChooser();
        deadlineChooser.setDateFormatString("MM/dd/yyyy");

        JCheckBox noDeadlineCheck = new JCheckBox("No deadline");
        noDeadlineCheck.addActionListener(e -> deadlineChooser.setEnabled(!noDeadlineCheck.isSelected()));

        JComboBox<Integer> difficultyBox = new JComboBox<>(new Integer[]{0,1,2,3});

        JPanel panel = new JPanel(new GridLayout(3,2,8,8));
        panel.add(new JLabel("Task name:")); panel.add(nameField);

        panel.add(new JLabel("Deadline:"));
        JPanel deadlinePanel = new JPanel(new BorderLayout());
        deadlinePanel.add(deadlineChooser, BorderLayout.CENTER);
        deadlinePanel.add(noDeadlineCheck, BorderLayout.EAST);
        panel.add(deadlinePanel);

        panel.add(new JLabel("Difficulty (0-3):")); panel.add(difficultyBox);

        int res = JOptionPane.showConfirmDialog(this, panel, "Add Task", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;

        String name = nameField.getText().trim();
        Date deadline = noDeadlineCheck.isSelected() ? null : deadlineChooser.getDate();
        int diff = (Integer) difficultyBox.getSelectedItem();

        if (name.isEmpty()) { JOptionPane.showMessageDialog(this, "Task name cannot be empty."); return; }

        currentProject.tasks.add(new Task(name, diff, deadline));
        refreshTasks();
    }

    private void openEditTaskDialog(Task t) {
        JTextField nameField = new JTextField(t.getName(),18);
        JDateChooser deadlineChooser = new JDateChooser();
        deadlineChooser.setDate(t.getDeadline());
        deadlineChooser.setDateFormatString("MM/dd/yyyy");

        JCheckBox noDeadlineCheck = new JCheckBox("No deadline");
        noDeadlineCheck.setSelected(t.getDeadline() == null);
        deadlineChooser.setEnabled(!noDeadlineCheck.isSelected());
        noDeadlineCheck.addActionListener(e -> deadlineChooser.setEnabled(!noDeadlineCheck.isSelected()));

        JComboBox<Integer> difficultyBox = new JComboBox<>(new Integer[]{0,1,2,3});
        difficultyBox.setSelectedItem(t.getDifficulty());

        JPanel panel = new JPanel(new GridLayout(3,2,8,8));
        panel.add(new JLabel("Task name:")); panel.add(nameField);

        panel.add(new JLabel("Deadline:"));
        JPanel deadlinePanel = new JPanel(new BorderLayout());
        deadlinePanel.add(deadlineChooser, BorderLayout.CENTER);
        deadlinePanel.add(noDeadlineCheck, BorderLayout.EAST);
        panel.add(deadlinePanel);

        panel.add(new JLabel("Difficulty (0-3):")); panel.add(difficultyBox);

        int res = JOptionPane.showConfirmDialog(this, panel, "Edit Task", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;

        String name = nameField.getText().trim();
        Date deadline = noDeadlineCheck.isSelected() ? null : deadlineChooser.getDate();
        int diff = (Integer) difficultyBox.getSelectedItem();

        if (name.isEmpty()) { JOptionPane.showMessageDialog(this, "Task name cannot be empty."); return; }

        t.setName(name);
        t.setDifficulty(diff);
        t.setDeadline(deadline);
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
            case 1 -> currentProject.tasks.sort(Comparator.comparing(task -> task.getDeadline(), Comparator.nullsLast(Comparator.naturalOrder())));
            case 2 -> currentProject.tasks.sort(Comparator.comparingInt(task -> task.getDifficulty()));
            default -> currentProject.tasks.sort(Comparator.comparing(task -> task.getName().toLowerCase()));
        }

        // add rows
        int completedTasks = 0;
        for (Task t : currentProject.tasks) {
            JPanel row = createTaskRow(t);
            taskListPanel.add(wrapFixedHeight(row));
            if (t.isCompleted()) completedTasks++;
        }

        int totalTasks = currentProject.tasks.size();
        int pct = totalTasks == 0 ? 0 : (int)((completedTasks / (double) totalTasks) * 100);
        progressBar.setValue(pct);
        progressBar.setString(pct + "% completed");

        revalidate(); repaint();
    }

}