import javax.swing.*;
import java.awt.*;

class MainFrame extends JFrame {
    private final ProjectListPanel projectListPanel;
    private final ProjectTodoPanel projectTodoPanel;
    private final PomodoroPanel pomodoroPanel;
    private final GradesPanel gradesPanel;

    MainFrame() {
        super("College Productivity App");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(Frame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());

        // Left: Projects list (shared)
        projectListPanel = new ProjectListPanel(this);

        // Center: card panel switching between "Projects" view and others
        JTabbedPane tabs = new JTabbedPane();

        //Test Samples------------------------------
        Project sample = new Project("Test");
        sample.tasks.add(new Task("Buy groceries", 1, ProjectTodoPanel.DATE_FMT.parseQuiet("01/30/2026")));
        sample.tasks.add(new Task("Finish homework", 2, ProjectTodoPanel.DATE_FMT.parseQuiet("01/31/2026")));
        projectListPanel.addProject(sample);
//        //------------------------------------------

        projectTodoPanel = new ProjectTodoPanel();
        pomodoroPanel = new PomodoroPanel();
        gradesPanel = new GradesPanel();

        tabs.addTab("Projects", wrapPanels(projectListPanel, projectTodoPanel));
        tabs.addTab("Pomodoro", pomodoroPanel);
        tabs.addTab("Grades", gradesPanel);

        add(tabs, BorderLayout.CENTER);


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
