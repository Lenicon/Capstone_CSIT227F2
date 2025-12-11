import java.io.Serializable;
import java.text.DecimalFormat;

public class Subject implements Serializable {

    private static final long serialVersionUID = 2L;

    // Using a static final DecimalFormat for efficiency in toString
    private static final DecimalFormat df = new DecimalFormat("#.##");

    String name;
    double units;
    double grade;

    public Subject(String name, double units, double grade) {
        this.name = name;
        this.units = units;
        this.grade = grade;
    }

    // Getters and Setters (Good practice for encapsulated data)
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getUnits() { return units; }
    public void setUnits(double units) { this.units = units; }
    public double getGrade() { return grade; }
    public void setGrade(double grade) { this.grade = grade; }

    @Override
    public String toString() {
        return name + " (" + df.format(units) + " units, " + df.format(grade) + ")";
    }
}