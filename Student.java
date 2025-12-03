import java.util.*;
import java.util.List;
import java.util.ArrayList;


class Student extends User {
    private Map<String, Double> grades = new HashMap<>();
    private List<String> enrolledCourses = new ArrayList<>();
    private String assignedInstructor = "None";

    public Student(String userID, String name, String email, String password) {
        super(userID, name, email, password);
    }

    public void enrollCourse(String courseCode) {
        enrolledCourses.add(courseCode);
    }

    public void assignInstructor(String instructorName) {
        this.assignedInstructor = instructorName;
    }

    public void setGrade(String courseCode, double grade) {
        grades.put(courseCode, grade);
    }

    public void displayStudentRecord() {
        displayInfo();
        System.out.println("Assigned Instructor: " + assignedInstructor);
        System.out.println("Courses:");
        for (String c : enrolledCourses) {
            System.out.println("- " + c);
        }
        System.out.println("Grades:");
        for (Map.Entry<String, Double> e : grades.entrySet()) {
            System.out.println(e.getKey() + ": " + e.getValue());
        }
    }
}
