import java.util.Date;

class Task {
    String name;
    int difficulty; // 0-3
    Date deadline;
    boolean completed = false;

    Task(String name, int difficulty, Date deadline) {
        this.name = name;
        this.difficulty = Math.max(0, Math.min(3, difficulty));
        this.deadline = deadline;
    }
}