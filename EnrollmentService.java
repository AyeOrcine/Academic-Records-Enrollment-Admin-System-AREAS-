public class EnrollmentService {
    public void enrollStudentInCourse(Student student, Course course) {
        student.enrollCourse(course);


        System.out.println(student.getUsername() + " has been enrolled in " + course.getCourseName());
    }
}
