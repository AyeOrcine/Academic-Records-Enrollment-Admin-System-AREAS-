import java.util.ArrayList;
import java.util.List;

public class Instructor extends User {
    private List<Course> teachingCourses;

    public Instructor(String username, String password) {
        super(username, password);
        teachingCourses = new ArrayList<>();
    }

    public void addCourse(Course course) {
        if (!teachingCourses.contains(course)) teachingCourses.add(course);
    }

    public List<Course> getTeachingCourses() { return teachingCourses; }

    public void setStudentGrade(Student student, Course course, String grade) {
        if (teachingCourses.contains(course)) {
            student.setGrade(course, grade);
            System.out.println("Grade set: " + student.getUsername() + " -> " + course.getCourseName() + " = " + grade);
        } else {
            System.out.println("You do not teach this course.");
        }
    }

    @Override
    public void displayInfo() {
        System.out.println("Instructor: " + getUsername());
        System.out.println("Teaching courses:");
        if (teachingCourses.isEmpty()) System.out.println("None");
        else for (Course c : teachingCourses) System.out.println("- " + c.getCourseName());
    }
}
