import java.util.ArrayList;
import java.util.List;

class Project {
    String name;
    List<Task> tasks = new ArrayList<>();

    Project(String name) { this.name = name; }

    @Override
    public String toString() { return name; }
}