import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class Project {
    String name;
    List<Task> tasks = new ArrayList<>();
    int id;

    Project(String name, int id){
        this.name = name;
        this.id = id;
    }

    Project(String name) {
        this(name, new Random().nextInt());
    }

    public String fileName() {
        return name + "_" + id + ".dat";
    }

    @Override
    public String toString() { return name; }
}