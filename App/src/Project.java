import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

class Project implements Serializable {
    private static final long serialVersionUID = 2L;
    String name;
    List<Task> tasks = new ArrayList<>();
    UUID id;

    Project(String name, int id){
        this.name = name;
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