class Enrollment {
    private Student student;
    private Course course;

    public Enrollment(Student student, Course course) {
        this.student = student;
        this.course = course;
    }

    public void displayEnrollment() {
        System.out.println("Student: " + student.name + " enrolled in " + course.getCourseTitle());
    }
}
