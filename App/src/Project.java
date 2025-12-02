import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.time.Instant;

class Project implements Serializable {
    private static final long serialVersionUID = 3L;

    List<Task> tasks = new ArrayList<>();
    private String name;
    private final UUID id;
    private final Instant creationDate;

    Project(String name){
        this.name = name;
        this.creationDate = Instant.now();
        this.id = UUID.randomUUID();
    }

    public String getFileName() {
        return this.id.toString() + ".dat";
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Instant getCreationDate() {
        return creationDate;
    }
}