import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.DecimalFormat;

public class GWACalculator extends JPanel {
    private final DefaultListModel<Subject> subjectModel;
    private final JList<Subject> subjectList;
    private final JLabel resultLabel;
    private final DecimalFormat df = new DecimalFormat("#.##");

    class Subject {
        String name;
        double units;
        double grade;

        Subject(String name, double units, double grade) {
            this.name = name;
            this.units = units;
            this.grade = grade;
        }

        @Override
        public String toString() {
            return name + " (" + df.format(units) + " units, " + df.format(grade) + ")";
        }
    }

    public GWACalculator() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // Title
        JLabel titleLabel = new JLabel("CIT GWA Calculator", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        add(titleLabel, BorderLayout.NORTH);

        // Main content panel
        JPanel contentPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Left panel - Subject list
        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setBorder(BorderFactory.createTitledBorder("Subjects"));
        listPanel.setPreferredSize(new Dimension(300, 400));

        subjectModel = new DefaultListModel<>();
        subjectList = new JList<>(subjectModel);
        subjectList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane listScroll = new JScrollPane(subjectList);
        listPanel.add(listScroll, BorderLayout.CENTER);

        // Subject control buttons
        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 5, 5));
        buttonPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        JButton addButton = new JButton("Add");
        JButton editButton = new JButton("Edit");
        JButton removeButton = new JButton("Remove");

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(removeButton);

        listPanel.add(buttonPanel, BorderLayout.SOUTH);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        contentPanel.add(listPanel, gbc);

        // Right panel - Calculation
        JPanel calcPanel = new JPanel(new BorderLayout());
        calcPanel.setBorder(BorderFactory.createTitledBorder("Calculation"));

        resultLabel = new JLabel("Add subjects to calculate", SwingConstants.CENTER);
        resultLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        calcPanel.add(resultLabel, BorderLayout.CENTER);

        JButton calculateButton = new JButton("Calculate GWA");
        calculateButton.setBackground(new Color(70, 130, 180));
        calculateButton.setForeground(Color.WHITE);

        JPanel calcButtonPanel = new JPanel();
        calcButtonPanel.add(calculateButton);
        calcPanel.add(calcButtonPanel, BorderLayout.SOUTH);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        contentPanel.add(calcPanel, gbc);

        add(contentPanel, BorderLayout.CENTER);

        // Event handlers
        addButton.addActionListener(e -> addSubject());
        editButton.addActionListener(e -> editSelectedSubject());
        removeButton.addActionListener(e -> removeSelectedSubject());
        calculateButton.addActionListener(e -> calculateGWA());
    }

    private void addSubject() {
        // Create input dialog
        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JTextField nameField = new JTextField();
        JTextField unitsField = new JTextField("3.0");
        JTextField gradeField = new JTextField("5.0");

        panel.add(new JLabel("Subject Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Units:"));
        panel.add(unitsField);
        panel.add(new JLabel("Grade (Highest = 5.0):"));
        panel.add(gradeField);

        int result = JOptionPane.showConfirmDialog(
                null, panel, "Add Subject",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            try {
                String name = nameField.getText().trim();
                if (name.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Please enter a subject name");
                    return;
                }

                double units = Double.parseDouble(unitsField.getText().trim());
                if (units <= 0) {
                    JOptionPane.showMessageDialog(null, "Units must be positive");
                    return;
                }

                double grade = Double.parseDouble(gradeField.getText().trim());
                if (grade < 1.0 || grade > 5.0) {
                    JOptionPane.showMessageDialog(null, "Grade must be between 1.0 and 5.0");
                    return;
                }

                Subject newSubject = new Subject(name, units, grade);
                subjectModel.addElement(newSubject);

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Please enter valid numbers for units and grade");
            }
        }
    }

    private void editSelectedSubject() {
        Subject selected = subjectList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(null, "Select a subject first");
            return;
        }

        // Edit dialog (same as add but with current values)
        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JTextField nameField = new JTextField(selected.name);
        JTextField unitsField = new JTextField(String.valueOf(selected.units));
        JTextField gradeField = new JTextField(String.valueOf(selected.grade));

        panel.add(new JLabel("Subject Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Units:"));
        panel.add(unitsField);
        panel.add(new JLabel("Grade (Highest = 5.0):"));
        panel.add(gradeField);

        int result = JOptionPane.showConfirmDialog(
                null, panel, "Edit Subject",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            try {
                String name = nameField.getText().trim();
                if (name.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Please enter a subject name");
                    return;
                }

                double units = Double.parseDouble(unitsField.getText().trim());
                if (units <= 0) {
                    JOptionPane.showMessageDialog(null, "Units must be positive");
                    return;
                }

                double grade = Double.parseDouble(gradeField.getText().trim());
                if (grade < 1.0 || grade > 5.0) {
                    JOptionPane.showMessageDialog(null, "Grade must be between 1.0 and 5.0");
                    return;
                }

                // Update the selected subject
                selected.name = name;
                selected.units = units;
                selected.grade = grade;
                subjectList.repaint();

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Please enter valid numbers for units and grade");
            }
        }
    }

    private void removeSelectedSubject() {
        Subject selected = subjectList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(null, "Select a subject first");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                null,
                "Remove subject '" + selected.name + "'?",
                "Confirm",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            subjectModel.removeElement(selected);
        }
    }


    private void calculateGWA() {
        if (subjectModel.isEmpty()) {
            resultLabel.setText("No subjects to calculate");
            return;
        }

        try {
            double totalUnits = 0;
            double totalWeightedGrade = 0;

            for (int i = 0; i < subjectModel.size(); i++) {
                Subject subject = subjectModel.getElementAt(i);
                totalUnits += subject.units;
                totalWeightedGrade += (subject.grade * subject.units);
            }

            double gwa = totalWeightedGrade / totalUnits;

            String formula = String.format(
                    "<html><center><h2>GWA = %.2f</h2>" +
                            "<p>∑(grade×units) = %.2f<br>" +
                            "∑units = %.2f</p></center></html>",
                    gwa, totalWeightedGrade, totalUnits
            );

            resultLabel.setText(formula);

        } catch (Exception e) {
            resultLabel.setText("Error in calculation");
        }
    }
}