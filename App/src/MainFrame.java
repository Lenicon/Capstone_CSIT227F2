import javax.swing.*;
import java.awt.*;

class MainFrame extends JFrame {
    private final ProjectListPanel projectListPanel;
    private final ProjectTodoPanel projectTodoPanel;
    private final PomodoroPanel pomodoroPanel;
    private final GWACalculator gwaCalculator;

    MainFrame() {
        super("College Productivity App");

        Image icon = new ImageIcon("App/assets/Logo.png").getImage();
        setIconImage(icon);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(Frame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1200, 360));
        setLayout(new BorderLayout());

        projectTodoPanel = new ProjectTodoPanel();
        // Left: Projects list (shared)
        projectListPanel = new ProjectListPanel(this);
//        projectListPanel.loadProjects();    // load projects
        gwaCalculator = new GWACalculator();
        // Center: card panel switching between "Projects" view and others
        JTabbedPane tabs = new JTabbedPane();

        //Test Samples------------------------------
//        Project sample = new Project("Test");
//        sample.tasks.add(new Task("Buy groceries", 1, ProjectTodoPanel.DATE_FMT.parseQuiet("01/30/2026")));
//        sample.tasks.add(new Task("AB", 2, ProjectTodoPanel.DATE_FMT.parseQuiet("02/12/2025")));
//        sample.tasks.add(new Task("AA", 3, ProjectTodoPanel.DATE_FMT.parseQuiet("02/12/2025")));
//        sample.tasks.add(new Task("Finish homework", 2, ProjectTodoPanel.DATE_FMT.parseQuiet("01/31/2026")));
//        projectListPanel.addProject(sample);

//        //------------------------------------------

        pomodoroPanel = new PomodoroPanel();
// add tabs
        tabs.addTab("Projects", wrapPanels(projectListPanel, projectTodoPanel));
        tabs.addTab("Pomodoro", pomodoroPanel);
        tabs.addTab("CIT GWA Calculator", gwaCalculator);

        add(tabs, BorderLayout.CENTER);

        // Automatically select first project if it exists
        projectListPanel.selectFirstProject();

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
