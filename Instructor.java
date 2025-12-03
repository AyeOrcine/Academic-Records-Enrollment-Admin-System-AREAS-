import java.util.List;
import java.util.ArrayList;


class Instructor extends User {
    private List<String> assignedCourses = new ArrayList<>();

    public Instructor(String userID, String name, String email, String password) {
        super(userID, name, email, password);
    }

    public void assignCourse(String courseCode) {
        assignedCourses.add(courseCode);
    }

    public void displayInstructorInfo() {
        displayInfo();
        System.out.println("Assigned Courses:");
        for (String c : assignedCourses) {
            System.out.println("- " + c);
        }
    }
}
