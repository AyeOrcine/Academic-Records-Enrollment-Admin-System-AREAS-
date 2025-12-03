import java.util.HashMap;
import java.util.Map;

public class Student extends User {
    private HashMap<Course, String> grades;

    public Student(String username, String password) {
        super(username, password);
        grades = new HashMap<>();
    }

    public void enrollCourse(Course course) {
        if (!grades.containsKey(course)) grades.put(course, "N/A");
    }

    public void setGrade(Course course, String grade) {
        if (grades.containsKey(course)) grades.put(course, grade);
    }

    public void displayGrades() {
        System.out.println("Grades for " + getUsername() + ":");
        if (grades.isEmpty()) System.out.println("No courses enrolled.");
        else for (Map.Entry<Course, String> entry : grades.entrySet())
            System.out.println(entry.getKey().getCourseName() + ": " + entry.getValue());
    }

    public Map<Course, String> getGrades() { return grades; }

    @Override
    public void displayInfo() { displayGrades(); }
}
