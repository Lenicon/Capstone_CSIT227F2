import javax.swing.*;

public class Main extends JFrame{
    private JPanel mainPane;
    private JLabel ProjectTracker;
    private JProgressBar progressBar1;
    private JButton button1;
    private JTree tree1;

    public Main() {

        setContentPane(mainPane);
        setTitle("Project Task Manager");
        pack();
//        setSize(600, 400);
        setMinimumSize(getSize());
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> new Main());

    }

}
