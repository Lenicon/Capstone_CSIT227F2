import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

//Left panel: Projects list
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

        add.addActionListener(_ -> {
            String name = JOptionPane.showInputDialog(null, "Project name:");
            if (name != null && !name.trim().isEmpty()) {
                Project p = new Project(name.trim());
                model.addElement(p);
                selectProject(p);
            }
        });

        rename.addActionListener(_ -> {
            Project p = list.getSelectedValue();
            if (p == null) { JOptionPane.showMessageDialog(null, "Select a project first."); return; }
            String name = JOptionPane.showInputDialog(null, "New name:", p.name);
            if (name != null && !name.trim().isEmpty()) { p.name = name.trim(); list.repaint(); }
        });

        remove.addActionListener(_ -> {
            Project p = list.getSelectedValue();
            if (p == null) { JOptionPane.showMessageDialog(null, "Select a project first."); return; }
            int ok = JOptionPane.showConfirmDialog(null, "Remove project \"" + p.name + "\"?","Confirm",JOptionPane.YES_NO_OPTION);
            if (ok == JOptionPane.YES_OPTION) { model.removeElement(p); }
        });

        bottom.add(add); bottom.add(rename); bottom.add(remove);

        add(new JScrollPane(list), BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    public void addProject(Project p) { model.addElement(p); }
    public void selectProject(Project p) { list.setSelectedValue(p, true); }
    public void selectFirstProject() {
        if (list.isSelectionEmpty() && list.getModel().getSize() > 0) {
            list.setSelectedIndex(0);
        }
    }
}