class Course {
    private String courseCode;
    private String courseName;
    private int units;

    public Course(String code, String name, int units) {
        this.courseCode = code;
        this.courseName = name;
        this.units = units;
    }

    public String getCourseCode() { return courseCode; }
    public String getCourseName() { return courseName; }
    public int getUnits() { return units; }

    public void displayCourseDetails() {
        System.out.println(courseCode + " - " + courseName + " (" + units + " units)");
    }
}
