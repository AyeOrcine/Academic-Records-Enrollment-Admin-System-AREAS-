public class Course {
    private String courseCode;
    private String courseName;

    public Course(String code, String name) {
        this.courseCode = code;
        this.courseName = name;
    }

    public String getCourseCode() { return courseCode; }
    public String getCourseName() { return courseName; }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Course)) return false;
        return this.courseCode.equals(((Course)obj).courseCode);
    }

    @Override
    public int hashCode() { return courseCode.hashCode(); }

    @Override
    public String toString() { return courseCode + " - " + courseName; }
}
