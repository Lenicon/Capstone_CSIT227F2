import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;
import java.text.DecimalFormat;

public class GWACalculator extends JPanel {

    private final DefaultListModel<Subject> subjectModel;
    private final JList<Subject> subjectList;
    private final JLabel resultLabel;

    // NEW: Instance of the file handler
    private final SubjectFileHandler fileHandler = new SubjectFileHandler();

    public GWACalculator() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // Title
        JLabel titleLabel = new JLabel("CIT GWA Calculator", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        add(titleLabel, BorderLayout.NORTH);

        // --- List Panel Setup ---
        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setBorder(BorderFactory.createTitledBorder("Subjects"));
        listPanel.setPreferredSize(new Dimension(300, 400));

        // Load existing data or start new
        DefaultListModel<Subject> initialModel;
        try {
            initialModel = fileHandler.loadSubjects();
        } catch (IOException | ClassNotFoundException e) {
            initialModel = new DefaultListModel<>();
            JOptionPane.showMessageDialog(this, "Could not load data. Starting with a blank list.", "Load Error", JOptionPane.ERROR_MESSAGE);
        }

        this.subjectModel = initialModel;
        subjectList = new JList<>(subjectModel);
        subjectList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listPanel.add(new JScrollPane(subjectList), BorderLayout.CENTER);

        // --- Button Panel Setup ---
        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 5, 1));
        buttonPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        JButton addButton = new JButton("Add");
        JButton editButton = new JButton("Edit");
        JButton removeButton = new JButton("Remove");
//        JButton saveButton = new JButton("Save Data");
//        JButton loadButton = new JButton("Load Data");

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(removeButton);
//        buttonPanel.add(saveButton);
//        buttonPanel.add(loadButton);
        listPanel.add(buttonPanel, BorderLayout.SOUTH);

        // --- Calculation Panel Setup (Right) ---
        JPanel calcPanel = new JPanel(new BorderLayout());
        calcPanel.setBorder(BorderFactory.createTitledBorder("Calculation"));

        resultLabel = new JLabel("Click Calculate GWA", SwingConstants.CENTER);
        resultLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        calcPanel.add(resultLabel, BorderLayout.CENTER);

        JButton calculateButton = new JButton("Calculate GWA");
        calculateButton.setBackground(new Color(70, 130, 180));
        calculateButton.setForeground(Color.WHITE);

        JPanel calcButtonPanel = new JPanel();
        calcButtonPanel.add(calculateButton);
        calcPanel.add(calcButtonPanel, BorderLayout.SOUTH);

        // --- Layout assembly ---
        JPanel contentPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1.0; gbc.weighty = 1.0;
        contentPanel.add(listPanel, gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        contentPanel.add(calcPanel, gbc);

        add(contentPanel, BorderLayout.CENTER);

        // --- Event handlers ---
        addButton.addActionListener(e -> addSubject());
        editButton.addActionListener(e -> editSelectedSubject());
        removeButton.addActionListener(e -> removeSelectedSubject());
        calculateButton.addActionListener(e -> calculateGWA());
//        saveButton.addActionListener(e -> saveSubjects());
//        loadButton.addActionListener(e -> loadSubjectsWithRefresh());

        // Calculate initial GWA if subjects were loaded successfully
        calculateGWA();
    }

    // --- File Handling Delegation ---
    private void saveSubjects() {
        try {
            fileHandler.saveSubjects(subjectModel);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error saving subjects: " + ex.getMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadSubjectsWithRefresh() {
        try {
            DefaultListModel<Subject> loadedModel = fileHandler.loadSubjects();
            // This is the clean way to replace the model's contents
            subjectModel.clear();
            for (int i = 0; i < loadedModel.size(); i++) {
                subjectModel.addElement(loadedModel.getElementAt(i));
            }
            calculateGWA();
        } catch (IOException | ClassNotFoundException ex) {
            JOptionPane.showMessageDialog(this, "Error loading subjects. Starting with a blank list.", "Load Error", JOptionPane.ERROR_MESSAGE);
            subjectModel.clear(); // Ensure list is clear on failure
            calculateGWA();
        }
    }


    // --- Core Logic Methods (Simplified/Cleaned) ---

    private void addSubject() {
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
                saveSubjects(); // Automatically save
                calculateGWA();

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

        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JTextField nameField = new JTextField(selected.getName());
        JTextField unitsField = new JTextField(String.valueOf(selected.getUnits()));
        JTextField gradeField = new JTextField(String.valueOf(selected.getGrade()));

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

                // Update the selected subject's properties
                selected.setName(name);
                selected.setUnits(units);
                selected.setGrade(grade);

                // Re-calculate and repaint the list item
                saveSubjects(); // Automatically save
                calculateGWA();
                subjectList.repaint();

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Please enter valid numbers for units and grade");
            }
        }
    }

    private void removeSelectedSubject() {
        Subject selected = subjectList.getSelectedValue();
        int selectedIndex = subjectList.getSelectedIndex();

        if (selected == null) {
            JOptionPane.showMessageDialog(null, "Select a subject first");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                null,
                "Remove subject '" + selected.getName() + "'?",
                "Confirm",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            subjectModel.remove(selectedIndex);
            saveSubjects(); // Automatically save
            calculateGWA(); // Recalculate after removal
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
                totalUnits += subject.getUnits();
                totalWeightedGrade += (subject.getGrade() * subject.getUnits());
            }

            double gwa = totalWeightedGrade / totalUnits;
            DecimalFormat df = new DecimalFormat("#.##");

            String formula = String.format(
                    "<html><center><h2>GWA = %s</h2>" +
                            "<p>∑(grade×units) = %s<br>" +
                            "∑units = %s</p></center></html>",
                    df.format(gwa), df.format(totalWeightedGrade), df.format(totalUnits)
            );

            resultLabel.setText(formula);

        } catch (Exception e) {
            resultLabel.setText("Error in calculation");
        }
    }
}