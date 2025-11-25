import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class Main extends JFrame{
    private JPanel mainPane;

    public Main(){

        setContentPane(panelMain);
        setTitle("Character Counter (Len.icon)");
        pack();
//        setSize(600, 400);
        setMinimumSize(getSize());
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    public static void main(String[] args) {
        SwingUtilities.invokeLater ( () -> new Main());
    }
}
