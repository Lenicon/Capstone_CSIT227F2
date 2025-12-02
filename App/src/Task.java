import javax.swing.*;
import java.util.Date;
import java.util.IllformedLocaleException;
import java.io.Serializable;

class Task implements Serializable { // <--- THIS IS CRITICAL
    private static final long serialVersionUID = 4L;

    static final SafeDateFormat DATE_FMT = new SafeDateFormat("MM/dd/yyyy");
    private String name;
    private int difficulty; // 0-3
    private Date deadline;
    private boolean completed = false;

    Task(String name, int difficulty, Date deadline) {
        setName(name);
        setDifficulty(difficulty);
        setDeadline(deadline);
    }

    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }

    public void setDifficulty(int difficulty){
        this.difficulty = Math.max(0, Math.min(3, difficulty));
    }

    public void setName(String name){
        try {
            if (name == null || name.isEmpty()){
                throw new IllegalArgumentException();
            }
            this.name = name;
        }
        catch (IllformedLocaleException e){
            JOptionPane.showMessageDialog(null, "Invalid date.");
        }
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public String getName() {
        return name;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public Date getDeadline() {
        return deadline;
    }

    public boolean isCompleted() {
        return completed;
    }

    public String stars(){
        return "★".repeat(difficulty) + "☆".repeat(3-difficulty);
    }

    public String getDeadlineString() {
        if (deadline == null) return "No deadline";
        return DATE_FMT.format(deadline);
    }

}