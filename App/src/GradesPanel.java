import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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