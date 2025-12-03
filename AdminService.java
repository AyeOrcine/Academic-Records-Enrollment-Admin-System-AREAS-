public class AdminService {
    public void assignCourseToInstructor(Instructor instructor, Course course) {
        instructor.addCourse(course);
        System.out.println(course.getCourseName() + " has been assigned to " + instructor.getUsername()
);
    }
}
