import java.io.*;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/* ----------------------------
   Base classes (User, Student, Instructor)
   ---------------------------- */

class User {
    // Protected fields to allow subclass access (but still encapsulated)
    protected String userID;
    protected String name;
    protected String email;
    protected String passwordHash; // store hashed password only

    // Main constructor used when creating new user with plaintext password
    public User(String userID, String name, String email, String passwordPlaintext) {
        this.userID = userID;
        this.name = name;
        this.email = email;
        this.passwordHash = hashPassword(passwordPlaintext); // hash on creation
    }

    // Alternate constructor used when loading from file (password already hashed)
    public User(String userID, String name, String email, String passwordHash, boolean alreadyHashed) {
        this.userID = userID;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    // Login compares hashed input to stored hash
    public boolean login(String passwordPlaintext) {
        return passwordHash.equals(hashPassword(passwordPlaintext));
    }

    // SHA-256 hash utility
    protected String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(password.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            // On failure, fall back to plaintext (not secure) — but log should note such failure.
            return password;
        }
    }

    // For saving to CSV: prefix "S" or "I" will be handled by caller
    public String toCSV() {
        // Format: userID,name,email,passwordHash
        return String.join(",", escapeCSV(userID), escapeCSV(name), escapeCSV(email), escapeCSV(passwordHash));
    }

    // Utility to escape commas/newlines in CSV fields (very simple)
    protected static String escapeCSV(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            s = s.replace("\"", "\"\"");
            return "\"" + s + "\"";
        }
        return s;
    }

    public void displayInfo() {
        System.out.println("User: " + name);
    }
}

/* Student class with enrollments list */
class Student extends User {
    // Student holds enrollments for quick access; enrollments are persisted to file too
    private List<Enrollment> enrollments = new ArrayList<>();

    // Create new student (plaintext password)
    public Student(String userID, String name, String email, String password) {
        super(userID, name, email, password);
    }

    // Load student with hashed password
    public Student(String userID, String name, String email, String passwordHash, boolean alreadyHashed) {
        super(userID, name, email, passwordHash, true);
    }

    public void addEnrollment(Enrollment e) {
        // Avoid duplicate enrollments for same course
        for (Enrollment ex : enrollments) {
            if (ex.getCourse().getCourseCode().equals(e.getCourse().getCourseCode())) {
                return; // already enrolled
            }
        }
        enrollments.add(e);
    }

    public List<Enrollment> getEnrollments() {
        return enrollments;
    }

    // View grades for each enrollment
    public void viewGrades() {
        if (enrollments.isEmpty()) {
            System.out.println("No enrolled courses yet.");
            return;
        }
        for (Enrollment e : enrollments) {
            System.out.println("Course: " + e.getCourse().getCourseTitle()
                               + " (" + e.getCourse().getCourseCode() + ")");
            e.displayGradesWithOverall();
            System.out.println("Attendance: " + e.getAttendanceCount() + "/" + e.getTotalSessions()
                               + " (" + String.format("%.1f", e.getAttendancePercentage()) + "%)");
            System.out.println("-------------------------------------");
        }
        // Show GPA
        System.out.println("GPA: " + String.format("%.2f", computeGPA()));
    }

    // Export student transcript/report as CSV for spreadsheet
    public void exportReportCSV() {
        String fileName = "student_report_" + userID + ".csv";
        try (PrintWriter pw = new PrintWriter(new FileWriter(fileName))) {
            pw.println("Student ID,Name,Email");
            pw.println(userID + "," + name + "," + email);
            pw.println();
            pw.println("CourseCode,CourseTitle,Assignment,Quiz,FinalExam,Overall,AttendanceCount,TotalSessions,Attendance%");

            for (Enrollment e : enrollments) {
                double overall = e.computeOverall();
                pw.printf("%s,%s,%.2f,%.2f,%.2f,%.2f,%d,%d,%.1f\n",
                          e.getCourse().getCourseCode(),
                          e.getCourse().getCourseTitle(),
                          e.getAssignmentGrade(),
                          e.getQuizGrade(),
                          e.getFinalExamGrade(),
                          overall,
                          e.getAttendanceCount(),
                          e.getTotalSessions(),
                          e.getAttendancePercentage());
            }
            pw.println();
            pw.println("GPA," + String.format("%.2f", computeGPA()));
            System.out.println("Report exported as " + fileName);
            AcademicRecordsEnrollmentAdminSystem.log("Exported report for student " + userID);
        } catch (IOException ex) {
            System.out.println("Error exporting report: " + ex.getMessage());
        }
    }

    // Simple GPA calculation: convert each overall grade to 4.0 scale, average
    public double computeGPA() {
        if (enrollments.isEmpty()) return 0.0;
        double totalPoints = 0.0;
        for (Enrollment e : enrollments) {
            double overall = e.computeOverall();
            totalPoints += gradeToPoint(overall);
        }
        return totalPoints / enrollments.size();
    }

    // Convert numeric grade to grade points (example scale)
    private double gradeToPoint(double grade) {
        if (grade >= 93) return 4.0;
        if (grade >= 90) return 3.7;
        if (grade >= 87) return 3.3;
        if (grade >= 83) return 3.0;
        if (grade >= 80) return 2.7;
        if (grade >= 77) return 2.3;
        if (grade >= 73) return 2.0;
        if (grade >= 70) return 1.7;
        if (grade >= 67) return 1.3;
        if (grade >= 60) return 1.0;
        return 0.0;
    }

    @Override
    public void displayInfo() {
        System.out.println("Student ID: " + userID + " | Name: " + name + " | Email: " + email);
    }
}

/* Instructor class */
class Instructor extends User {
    private List<Course> coursesHandled = new ArrayList<>();

    // Create new instructor (plaintext)
    public Instructor(String userID, String name, String email, String password) {
        super(userID, name, email, password);
    }

    // Load with hashed password
    public Instructor(String userID, String name, String email, String passwordHash, boolean alreadyHashed) {
        super(userID, name, email, passwordHash, true);
    }

    public void addCourse(Course c) {
        // avoid duplicates
        for (Course ex : coursesHandled) {
            if (ex.getCourseCode().equals(c.getCourseCode())) return;
        }
        coursesHandled.add(c);
    }

    public List<Course> getCoursesHandled() {
        return coursesHandled;
    }

    // Assign grades to an existing enrollment
    public void assignGrades(Enrollment e, double assignment, double quiz, double finalExam) {
        e.setGrades(assignment, quiz, finalExam);
    }

    // Record attendance for an enrollment
    public void takeAttendance(Enrollment e, boolean present) {
        e.recordAttendance(present);
    }

    @Override
    public void displayInfo() {
        System.out.println("Instructor ID: " + userID + " | Name: " + name + " | Email: " + email);
    }
}

/* Course class stores its code, title, instructor and total sessions */
class Course {
    private String courseCode;
    private String courseTitle;
    private Instructor instructor;
    private int totalSessions = 0; // optional: number of class meetings

    public Course(String code, String title, Instructor instructor) {
        this.courseCode = code;
        this.courseTitle = title;
        this.instructor = instructor;
    }

    // Alternate constructor to load totalSessions from CSV
    public Course(String code, String title, Instructor instructor, int totalSessions) {
        this.courseCode = code;
        this.courseTitle = title;
        this.instructor = instructor;
        this.totalSessions = totalSessions;
    }

    public String getCourseCode() { return courseCode; }
    public String getCourseTitle() { return courseTitle; }
    public Instructor getInstructor() { return instructor; }

    public int getTotalSessions() { return totalSessions; }
    public void setTotalSessions(int s) { this.totalSessions = s; }

    public void displayCourseInfo() {
        System.out.println(courseCode + ": " + courseTitle + " | Instructor: " + instructor.name
                           + " | Sessions: " + totalSessions);
    }

    public void setInstructor(Instructor instructor) {
        this.instructor = instructor;
    }

    // For CSV saving: courseCode,courseTitle,instructorID,totalSessions
    public String toCSV() {
        String instrID = (instructor != null) ? instructor.userID : "None";
        return String.join(",", escapeCSV(courseCode), escapeCSV(courseTitle), escapeCSV(instrID), String.valueOf(totalSessions));
    }

    private static String escapeCSV(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            s = s.replace("\"", "\"\"");
            return "\"" + s + "\"";
        }
        return s;
    }
}

/* Enrollment holds student, course and grade & attendance data */
class Enrollment {
    private Student student;
    private Course course;
    private double assignmentGrade = 0.0;
    private double quizGrade = 0.0;
    private double finalExamGrade = 0.0;
    private int attendanceCount = 0; // number of times present
    // We store totalSessions to be able to compute attendance percentage reliably
    private int totalSessions = 0;

    public Enrollment(Student s, Course c) {
        this.student = s;
        this.course = c;
        this.totalSessions = c.getTotalSessions();
    }

    // When loading from CSV, allow passing all fields
    public Enrollment(Student s, Course c, double assignment, double quiz, double finalExam, int attendanceCount, int totalSessions) {
        this.student = s;
        this.course = c;
        this.assignmentGrade = assignment;
        this.quizGrade = quiz;
        this.finalExamGrade = finalExam;
        this.attendanceCount = attendanceCount;
        this.totalSessions = totalSessions;
    }

    // Set grades
    public void setGrades(double assignment, double quiz, double finalExam) {
        this.assignmentGrade = assignment;
        this.quizGrade = quiz;
        this.finalExamGrade = finalExam;
    }

    // Record presence for one session
    public void recordAttendance(boolean present) {
        if (present) attendanceCount++;
        // ensure totalSessions is at least attendanceCount
        if (attendanceCount > totalSessions) totalSessions = attendanceCount;
    }

    // Accessors
    public Course getCourse() { return course; }
    public Student getStudent() { return student; }
    public double getAssignmentGrade() { return assignmentGrade; }
    public double getQuizGrade() { return quizGrade; }
    public double getFinalExamGrade() { return finalExamGrade; }
    public int getAttendanceCount() { return attendanceCount; }
    public int getTotalSessions() { return totalSessions; }

    // Display grades and computed overall
    public void displayGradesWithOverall() {
        double overall = computeOverall();
        System.out.println("Assignments: " + assignmentGrade +
                " | Quizzes: " + quizGrade +
                " | Finals: " + finalExamGrade +
                " | Overall: " + String.format("%.2f", overall));
    }

    // Weighted average: assignment 40%, quiz 30%, final 30% (configurable if desired)
    public double computeOverall() {
        return (assignmentGrade * 0.4) + (quizGrade * 0.3) + (finalExamGrade * 0.3);
    }

    public double getAttendancePercentage() {
        if (totalSessions <= 0) return 0.0;
        return (attendanceCount * 100.0) / totalSessions;
    }

    // For saving to CSV: studentID,courseCode,assignment,quiz,final,attendanceCount,totalSessions
    public String toCSV() {
        return String.join(",",
                escapeCSV(student.userID),
                escapeCSV(course.getCourseCode()),
                String.valueOf(assignmentGrade),
                String.valueOf(quizGrade),
                String.valueOf(finalExamGrade),
                String.valueOf(attendanceCount),
                String.valueOf(totalSessions));
    }

    private static String escapeCSV(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            s = s.replace("\"", "\"\"");
            return "\"" + s + "\"";
        }
        return s;
    }
}

/* ----------------------------
   Main system class and data persistence
   ---------------------------- */

public class AcademicRecordsEnrollmentAdminSystem {
    static final String ADMIN_PASSWORD = "admin123";

    // In-memory collections
    static Map<String, Student> students = new HashMap<>(); // keyed by ID
    static Map<String, Instructor> instructors = new HashMap<>(); // keyed by ID
    static Map<String, Course> courses = new HashMap<>(); // keyed by course code
    static List<Enrollment> enrollments = new ArrayList<>(); // master enrollment list

    // Filenames
    static final String USERS_FILE = "users.csv";
    static final String COURSES_FILE = "courses.csv";
    static final String ENROLLMENTS_FILE = "enrollments.csv";
    static final String ANNOUNCE_FILE = "announcements.txt";
    static final String AUDIT_LOG = "audit.log";
    static final String INSTRUCTOR_FILE = "instructors.txt";
    static final String STUDENT_FILE = "students.txt";
    static final String OUTPUT_FILE = "grades.txt";

    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        // Load data at startup
        loadData();

        if (courses.isEmpty()) {
            courses.put("Litr 102", new Course("Litr 102", "ASEAN Literature", null, 0));
            courses.put("PATHFIT 3", new Course("PATHFIT 3", "Traditional and Recreational Games", null, 0));
            courses.put("CS 121", new Course("CS 121", "Advanced Computer Programming", null, 0));
            courses.put("Phy 101", new Course("Phy 101", "Calculus-Based Physics", null, 0));
            courses.put("CPE 405", new Course("CPE 405", "Discrete Mathematics", null, 0));
            courses.put("IT 212", new Course("IT 212", "Computer Networking 1", null, 0));
            courses.put("IT 211", new Course("IT 211", "Database Management System", null, 0));
            courses.put("CS 211", new Course("CS 211", "Object-Oriented Programming", null, 0));
            log("Initialized default courses.");
        }

        mainMenu();
        // Save on exit
        saveAll();
    }

    // Primary console menu
    public static void mainMenu() {
        while (true) {
            System.out.println("\n==== STUDENT COURSE MANAGEMENT SYSTEM ====");
            System.out.println("1. Admin");
            System.out.println("2. Instructor Login");
            System.out.println("3. Student Login");
            System.out.println("4. Register Student");
            System.out.println("5. Register Instructor");
            System.out.println("6. Search (student/course/instructor)");
            System.out.println("7. View Announcements");
            System.out.println("8. Exit");
            System.out.print("Enter choice: ");
            String line = sc.nextLine().trim();
            int ch = safeParseInt(line, -1);

            switch (ch) {
                case 1 -> adminLogin();
                case 2 -> instructorLogin();
                case 3 -> studentLogin();
                case 4 -> registerStudent();
                case 5 -> registerInstructor();
                case 6 -> searchMenu();
                case 7 -> viewAnnouncements();
                case 8 -> { saveAll(); System.out.println("Goodbye!"); System.exit(0); }
                default -> System.out.println("Invalid choice. Try again.");
            }
        }
    }

    /* ----------------------------
       Authentication & registration
       ---------------------------- */
    public static void adminLogin() {
        System.out.print("Enter Admin Password: ");
        String pass = sc.nextLine();

        if (pass.equals(ADMIN_PASSWORD)) {
            System.out.println("Access granted!");
            adminMenu();
        } else {
            System.out.println("Wrong password.");
        }
    }

    public static void instructorLogin() {
        System.out.print("Enter Instructor ID: ");
        String id = sc.nextLine().trim();
        System.out.print("Enter Password: ");
        String pw = sc.nextLine();
        Instructor i = instructors.get(id);
        if (i != null && i.login(pw)) {
            log("Instructor " + id + " logged in.");
            instructorMenu(i);
            return;
        }
        System.out.println("Login failed!");
    }

    public static void studentLogin() {
        System.out.print("Enter Student ID: ");
        String id = sc.nextLine().trim();
        System.out.print("Enter Password: ");
        String pw = sc.nextLine();
        Student s = students.get(id);
        if (s != null && s.login(pw)) {
            log("Student " + id + " logged in.");
            studentMenu(s);
            return;
        }
        System.out.println("Login failed!");
    }

    // Register student with validation and duplicate checking
    public static void registerStudent() {
        System.out.print("Student ID (5 digits): "); String id = sc.nextLine().trim();
        while (!id.matches("\\d{5}")) {
            System.out.print("Invalid ID. Enter exactly 5 digits: ");
            id = sc.nextLine().trim();
        }
        if (id.isEmpty()) { System.out.println("ID cannot be empty."); return; }
        if (students.containsKey(id) || instructors.containsKey(id)) {
            System.out.println("ID already exists.");
            return;
        }
        System.out.print("Name: "); String name = sc.nextLine().trim();
        while (!name.matches("[a-zA-Z ]+")) {
            System.out.print("Invalid name. Letters and spaces only: ");
            name = sc.nextLine().trim();
        }
        if (name.isEmpty()) { System.out.println("Name cannot be empty."); return; }
        System.out.print("Email: "); String email = sc.nextLine().trim();
        if (!isValidEmail(email)) { System.out.println("Invalid email format."); return; }
        System.out.print("Password: "); String pw = sc.nextLine();
        if (pw.length() < 4) { System.out.println("Password must be at least 4 characters."); return; }

        Student s = new Student(id, name, email, pw);
        students.put(id, s);
        saveAll();
        log("Registered new student: " + id);
        System.out.println("Student registered!");
    }

    // Register instructor with duplicate checking
    public static void registerInstructor() {
        System.out.print("Instructor ID (5 digits): "); String id = sc.nextLine().trim();
        while (!id.matches("\\d{5}")) {
            System.out.print("Invalid ID. Enter exactly 5 digits: ");
            id = sc.nextLine().trim();
        }
        if (id.isEmpty()) { System.out.println("ID cannot be empty."); return; }
        if (instructors.containsKey(id) || students.containsKey(id)) {
            System.out.println("ID already exists.");
            return;
        }
        System.out.print("Name: "); String name = sc.nextLine().trim();
        while (!name.matches("[a-zA-Z ]+")) {
            System.out.print("Invalid name. Letters and spaces only: ");
            name = sc.nextLine().trim();
        }
        if (name.isEmpty()) { System.out.println("Name cannot be empty."); return; }
        System.out.print("Email: "); String email = sc.nextLine().trim();
        if (!isValidEmail(email)) { System.out.println("Invalid email format."); return; }
        System.out.print("Password: "); String pw = sc.nextLine();
        if (pw.length() < 4) { System.out.println("Password must be at least 4 characters."); return; }

        Instructor i = new Instructor(id, name, email, pw);
        instructors.put(id, i);
        saveAll();
        log("Registered new instructor: " + id);
        System.out.println("Instructor registered!");
    }

    /* ----------------------------
       Admin menu + features ()
       ---------------------------- */
    public static void adminMenu() {
        loadUsers();


        while (true) {
            System.out.println("\n=== ADMIN MENU ===");
            System.out.println("1. View All Instructors");
            System.out.println("2. Search Student");
            System.out.println("3. Assign Instructor to Course");
            System.out.println("4. View Student Grades & Assigned Instructor");
            System.out.println("5. Back to Main Menu");
            System.out.print("Enter choice: ");

            String ch = sc.nextLine();

            switch (ch) {
                case "1": adminViewInstructors(); break;
                case "2": adminSearchStudent(); break;
                case "3": adminAssignInstructorToCourse(); break;
                case "4": adminViewStudentGrades(); break;
                case "5": return;
                default: System.out.println("Invalid!");
            }
        }
    }

    public static void adminViewInstructors() {
        if (instructors.isEmpty()) {
            System.out.println("No instructors yet.");
            return;
        }

        System.out.println("\n=== INSTRUCTOR LIST ===");
        for (Instructor i : instructors.values()) {
            i.displayInfo();
        }
    }

    public static void adminSearchStudent() {
        System.out.print("Enter student ID to search: ");
        String id = sc.nextLine().trim();

        Student s = students.get(id);
        if (s != null) {
            System.out.println("Student found:");
            s.displayInfo();
        } else {
            System.out.println("Student not found.");
        }
    }

    static final String COURSE_ASSIGN_FILE = "course_assignments.txt";

    public static void adminAssignInstructorToCourse() {
        System.out.println("\n=== AVAILABLE COURSES ===");

        if (courses.isEmpty()) {
            System.out.println("No courses available.");
            return;
        }

        // Show list of Courses
        for (Course c : courses.values()) {
            String instr = (c.getInstructor() != null) ? c.getInstructor().name : "No instructor yet";
            System.out.println(c.getCourseCode() + " - " + c.getCourseTitle() + " | Instructor: " + instr);
        }

        // Ask instructor
        System.out.print("\nEnter Instructor ID: ");
        String instructorID = sc.nextLine().trim();

        Instructor i = instructors.get(instructorID);
        if (i == null) {
            System.out.println("Instructor not found! Assign failed.");
            return;
        }

        // Ask course code
        System.out.print("Enter Course Code to assign: ");
        String codeInput = sc.nextLine().trim();

        // Normalize input: remove ALL spaces, match case-insensitive
        String normalizedInput = codeInput.replaceAll("\\s+", "").toUpperCase();

        Course c = null;

        // Search for matching course
        for (String key : courses.keySet()) {
            String normalizedKey = key.replaceAll("\\s+", "").toUpperCase();
            if (normalizedKey.equals(normalizedInput)) {
                c = courses.get(key);
                break;
            }
        }

        if (c == null) {
            System.out.println("Invalid course code! Assign failed.");
            return;
        }

        // Assign instructor
        c.setInstructor(i);
        saveAll();
        log("Instructor " + instructorID + " assigned to " + c.getCourseCode());

        System.out.println("Successfully assigned instructor to " + c.getCourseCode() + "!");
    }

    static final String STUDENT_INSTRUCTOR_FILE = "student_instructor.txt";

    public static void adminViewStudentGrades() {
    File gradeFile = new File(OUTPUT_FILE);
    File assignFile = new File(STUDENT_INSTRUCTOR_FILE);

    if (!gradeFile.exists()) {
        System.out.println("No grades recorded.");
        return;
    }

    System.out.println("\n=== STUDENT GRADES + INSTRUCTOR ===");
        try (Scanner g = new Scanner(gradeFile)) {
            while (g.hasNextLine()) {
                String line = g.nextLine();
                String[] parts = line.split(",");
                String sid = parts[0];

                String instructor = "Not assigned";

                if (assignFile.exists()) {
                    try (Scanner a = new Scanner(assignFile)) {
                        while (a.hasNextLine()) {
                            String al = a.nextLine();
                            String[] ap = al.split(",");
                            if (ap[0].equals(sid)) instructor = ap[1];
                        }
                    }
                }
                System.out.println(line + " | Instructor: " + instructor);
            }
        } catch (Exception e) {
            System.out.println("Error.");
        }
    }

    /* ----------------------------
       Instructor menu + features (Add Course, Assign Grades, Take Attendance, Announcements)
       ---------------------------- */

    public static void instructorMenu(Instructor i) {
        while (true) {
            System.out.println("\nInstructor Menu - " + i.name);
            System.out.println("1. Add Course");
            System.out.println("2. Assign Grades");
            System.out.println("3. Take Attendance");
            System.out.println("4. View Courses");
            System.out.println("5. Announcements (Post)");
            System.out.println("6. Logout");
            System.out.print("Enter choice: ");
            int ch = safeParseInt(sc.nextLine(), -1);
            switch (ch) {
                case 1 -> addCourse(i);
                case 2 -> assignGrades(i);
                case 3 -> takeAttendance(i);
                case 4 -> {
                    // show instructor's courses
                    if (i.getCoursesHandled().isEmpty()) System.out.println("No courses yet.");
                    i.getCoursesHandled().forEach(Course::displayCourseInfo);
                }
                case 5 -> postAnnouncement(i);
                case 6 -> { log("Instructor " + i.userID + " logged out."); return; }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    // Add course and link to instructor
    public static void addCourse(Instructor i) {
        System.out.print("Course Code: "); String code = sc.nextLine().trim();
        if (code.isEmpty()) { System.out.println("Code cannot be empty."); return; }
        if (courses.containsKey(code)) { System.out.println("Course already exists."); return; }
        System.out.print("Course Title: "); String title = sc.nextLine().trim();
        System.out.print("Total number of sessions (enter 0 if unknown): ");
        int sessions = safeParseInt(sc.nextLine(), 0);
        Course c = new Course(code, title, i, sessions);
        courses.put(code, c);
        i.addCourse(c);
        log("Course added " + code + " by instructor " + i.userID);
        System.out.println("Course added!");
    }

    // Assign grades — must find existing enrollment (or create by enrolling student properly)
    public static void assignGrades(Instructor i) {
        System.out.print("Enter Student ID: "); String sid = sc.nextLine().trim();
        Student s = students.get(sid);
        if (s == null) { System.out.println("Student not found."); return; }

        System.out.print("Course Code: "); String code = sc.nextLine().trim();
        Course c = courses.get(code);
        if (c == null) { System.out.println("Course not found."); return; }

        // verify instructor handles the course
        if (!c.getInstructor().userID.equals(i.userID)) {
            System.out.println("You do not handle that course.");
            return;
        }

        // Get or create an enrollment tying the student to course
        Enrollment e = getOrCreateEnrollment(s, c);

        System.out.print("Assignment grade (0-100): "); double a = safeParseDouble(sc.nextLine(), -1);
        System.out.print("Quiz grade (0-100): "); double q = safeParseDouble(sc.nextLine(), -1);
        System.out.print("Final Exam grade (0-100): "); double f = safeParseDouble(sc.nextLine(), -1);
        if (a < 0 || q < 0 || f < 0) { System.out.println("Invalid grade input."); return; }

        i.assignGrades(e, a, q, f);
        log("Instructor " + i.userID + " assigned grades for student " + s.userID + " in " + c.getCourseCode());
        System.out.println("Grades assigned!");
    }

    // Take attendance for a session — increments total sessions if necessary
    public static void takeAttendance(Instructor i) {
        System.out.print("Enter Student ID: "); String sid = sc.nextLine().trim();
        Student s = students.get(sid);
        if (s == null) { System.out.println("Student not found."); return; }

        System.out.print("Course Code: "); String code = sc.nextLine().trim();
        Course c = courses.get(code);
        if (c == null) { System.out.println("Course not found."); return; }

        // verify instructor handles the course
        if (!c.getInstructor().userID.equals(i.userID)) {
            System.out.println("You do not handle that course.");
            return;
        }

        // Get or create enrollment
        Enrollment e = getOrCreateEnrollment(s, c);

        System.out.print("Present? (y/n): "); String ans = sc.nextLine().trim();
        boolean present = ans.equalsIgnoreCase("y");
        e.recordAttendance(present);
        // ensure course totalSessions recorded is consistent
        int newTotal = Math.max(c.getTotalSessions(), e.getTotalSessions());
        c.setTotalSessions(newTotal);
        log("Instructor " + i.userID + " recorded attendance for student " + s.userID + " in " + c.getCourseCode() + " present=" + present);
        System.out.println("Attendance recorded!");
    }

    // Instructor posts an announcement which is appended to announcements file
    public static void postAnnouncement(Instructor i) {
        System.out.println("Enter announcement message (single line): ");
        String msg = sc.nextLine().trim();
        if (msg.isEmpty()) { System.out.println("Empty announcement not posted."); return; }
        String entry = formattedNow() + " | " + i.name + " (" + i.userID + "): " + msg;
        try (PrintWriter pw = new PrintWriter(new FileWriter(ANNOUNCE_FILE, true))) {
            pw.println(entry);
            log("Announcement posted by " + i.userID);
            System.out.println("Announcement posted.");
        } catch (IOException e) {
            System.out.println("Error writing announcement: " + e.getMessage());
        }
    }

    /* ----------------------------
       Student menu + features (View grades, Export report)
       ---------------------------- */

    public static void studentMenu(Student s) {
        while (true) {
            System.out.println("\nStudent Menu - " + s.name);
            System.out.println("1. View Grades");
            System.out.println("2. Export Report (CSV)");
            System.out.println("3. Enroll in Course");
            System.out.println("4. View Enrolled Courses");
            System.out.println("5. Logout");
            System.out.print("Enter choice: ");
            int ch = safeParseInt(sc.nextLine(), -1);
            switch (ch) {
                case 1 -> s.viewGrades();
                case 2 -> s.exportReportCSV();
                case 3 -> enrollInCourse(s);
                case 4 -> {
                    if (s.getEnrollments().isEmpty()) System.out.println("No courses enrolled.");
                    else {
                        for (Enrollment e : s.getEnrollments()) {
                            System.out.println(e.getCourse().getCourseCode() + " - " + e.getCourse().getCourseTitle()
                                               + " | Instructor: " + e.getCourse().getInstructor().name
                                               + " | Sessions: " + e.getCourse().getTotalSessions());
                        }
                    }
                }
                case 5 -> { log("Student " + s.userID + " logged out."); return; }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    // Student initiates enrollment (should be validated)
    public static void enrollInCourse(Student s) {
        System.out.println("\n=== AVAILABLE COURSES ===");
            if (courses.isEmpty()) {
                System.out.println("No courses available.");
                return;
            }

            for (Course c : courses.values()) {
                String instr = (c.getInstructor() != null) ? c.getInstructor().name : "No instructor yet";
                System.out.println(c.getCourseCode() + " - " + c.getCourseTitle() + " | Instructor: " + instr);
            }

            System.out.print("\nEnter Course Code to enroll: ");
            String code = sc.nextLine().trim();

            // **Validation: course must exist exactly**
            Course c = courses.get(code);
            if (c == null) {
                System.out.println("Invalid course code. Enrollment failed.");
                return;
            }

            // Check duplicate enrollment
            for (Enrollment e : s.getEnrollments()) {
                if (e.getCourse().getCourseCode().equals(code)) {
                    System.out.println("Already enrolled in that course.");
                    return;
                }
            }

            Enrollment e = new Enrollment(s, c, 0, 0, 0, 0, c.getTotalSessions());
            s.addEnrollment(e);
            enrollments.add(e);
            
            saveAll(); // auto save after enrollment
            log("Student " + s.userID + " enrolled in " + code);

            System.out.println("Successfully enrolled in " + code + "!");
    }

    /* ----------------------------
       Enrollment utilities
       ---------------------------- */

    // Get existing enrollment or create and persist new one
    public static Enrollment getOrCreateEnrollment(Student s, Course c) {
        // First search in student's enrollments
        for (Enrollment e : s.getEnrollments()) {
            if (e.getCourse().getCourseCode().equals(c.getCourseCode())) {
                return e;
            }
        }
        // If not found, search master list (in case student was loaded after enrollment)
        for (Enrollment e : enrollments) {
            if (e.getStudent().userID.equals(s.userID) && e.getCourse().getCourseCode().equals(c.getCourseCode())) {
                // attach to student if not attached
                s.addEnrollment(e);
                return e;
            }
        }
        // Create new enrollment
        Enrollment e = new Enrollment(s, c, 0, 0, 0, 0, c.getTotalSessions());
        s.addEnrollment(e);
        enrollments.add(e);
        return e;
    }

    /* ----------------------------
       Persistence: Save & Load CSV files
       ---------------------------- */

    // Save all data atomically (best-effort). Files: users.csv, courses.csv, enrollments.csv
    public static void saveAll() {
        saveUsers();
        saveCourses();
        saveEnrollments();
        // announcements are appended when posted; audit log always appended
    }

    public static void saveUsers() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(USERS_FILE))) {
            // Each line: type(S/I),userID,name,email,passwordHash
            for (Student s : students.values()) {
                pw.println(String.join(",", "S", escapeCSV(s.userID), escapeCSV(s.name), escapeCSV(s.email), escapeCSV(s.passwordHash)));
            }
            for (Instructor i : instructors.values()) {
                pw.println(String.join(",", "I", escapeCSV(i.userID), escapeCSV(i.name), escapeCSV(i.email), escapeCSV(i.passwordHash)));
            }
            log("Saved users to " + USERS_FILE);
        } catch (IOException e) {
            System.out.println("Error saving users: " + e.getMessage());
        }
    }

    public static void saveCourses() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(COURSES_FILE))) {
            // Each line: courseCode,courseTitle,instructorID,totalSessions
            for (Course c : courses.values()) {
                pw.println(c.toCSV());
            }
            log("Saved courses to " + COURSES_FILE);
        } catch (IOException e) {
            System.out.println("Error saving courses: " + e.getMessage());
        }
    }

    public static void saveEnrollments() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(ENROLLMENTS_FILE))) {
            // Each line: studentID,courseCode,assignment,quiz,final,attendanceCount,totalSessions
            for (Enrollment e : enrollments) {
                pw.println(e.toCSV());
            }
            log("Saved enrollments to " + ENROLLMENTS_FILE);
        } catch (IOException e) {
            System.out.println("Error saving enrollments: " + e.getMessage());
        }
    }

    // Loading on startup: users, courses, enrollments
    public static void loadData() {
        loadUsers();
        loadCourses();
        loadEnrollments();
        System.out.println("Data loaded. Students: " + students.size() + ", Instructors: " + instructors.size() + ", Courses: " + courses.size() + ", Enrollments: " + enrollments.size());
    }

    // Load users.csv format: type,userID,name,email,passwordHash
    public static void loadUsers() {
        File f = new File(USERS_FILE);
        if (!f.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                // naive CSV split: this assumes no commas inside fields — for robust parse use a CSV library
                String[] p = splitCSV(line);
                if (p.length < 5) continue;
                String type = p[0];
                String id = p[1];
                String name = p[2];
                String email = p[3];
                String passwordHash = p[4];
                if (type.equals("S")) {
                    Student s = new Student(id, name, email, passwordHash, true);
                    students.put(id, s);
                } else if (type.equals("I")) {
                    Instructor i = new Instructor(id, name, email, passwordHash, true);
                    instructors.put(id, i);
                }
            }
            log("Loaded users from " + USERS_FILE);
        } catch (Exception e) {
            System.out.println("Error loading users: " + e.getMessage());
        }
    }

    // Load courses.csv: courseCode,courseTitle,instructorID,totalSessions
    public static void loadCourses() {
        File f = new File(COURSES_FILE);
        if (!f.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = splitCSV(line);
                if (p.length < 4) continue;
                String code = p[0];
                String title = p[1];
                String instrID = p[2];
                int totalSessions = safeParseInt(p[3], 0);
                Instructor instr = instructors.get(instrID);
                Course c = new Course(code, title, instr, totalSessions);
                courses.put(code, c);
            }
            log("Loaded courses from " + COURSES_FILE);
        } catch (Exception e) {
            System.out.println("Error loading courses: " + e.getMessage());
        }
    }

    // Load enrollments.csv: studentID,courseCode,assignment,quiz,final,attendanceCount,totalSessions
    public static void loadEnrollments() {
        File f = new File(ENROLLMENTS_FILE);
        if (!f.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = splitCSV(line);
                if (p.length < 7) continue;
                String sid = p[0];
                String code = p[1];
                double a = safeParseDouble(p[2], 0);
                double q = safeParseDouble(p[3], 0);
                double fin = safeParseDouble(p[4], 0);
                int attend = safeParseInt(p[5], 0);
                int total = safeParseInt(p[6], 0);

                Student s = students.get(sid);
                Course c = courses.get(code);
                if (s == null || c == null) continue; // skip if missing data
                Enrollment e = new Enrollment(s, c, a, q, fin, attend, total);
                enrollments.add(e);
                s.addEnrollment(e);
            }
            log("Loaded enrollments from " + ENROLLMENTS_FILE);
        } catch (Exception e) {
            System.out.println("Error loading enrollments: " + e.getMessage());
        }
    }

    /* ----------------------------
       Search and utility features
       ---------------------------- */

    public static void searchMenu() {
        System.out.println("Search:");
        System.out.println("1. Student by ID or name");
        System.out.println("2. Course by code or title");
        System.out.println("3. Instructor by ID or name");
        System.out.print("Choice: ");
        int ch = safeParseInt(sc.nextLine(), -1);
        switch (ch) {
            case 1 -> searchStudent();
            case 2 -> searchCourse();
            case 3 -> searchInstructor();
            default -> System.out.println("Invalid choice.");
        }
    }

    public static void searchStudent() {
        System.out.print("Enter ID or name keyword: ");
        String q = sc.nextLine().trim().toLowerCase();
        boolean found = false;
        for (Student s : students.values()) {
            if (s.userID.toLowerCase().contains(q) || s.name.toLowerCase().contains(q)) {
                s.displayInfo();
                found = true;
            }
        }
        if (!found) System.out.println("No students found.");
    }

    public static void searchCourse() {
        System.out.print("Enter course code or title keyword: ");
        String q = sc.nextLine().trim().toLowerCase();

        boolean found = false;
        for (Course c : courses.values()) {
            if (c.getCourseCode().toLowerCase().contains(q) || c.getCourseTitle().toLowerCase().contains(q)) {
                System.out.println("\nCourse found:");
                System.out.println(c.getCourseCode() + " - " + c.getCourseTitle());
                String instr = (c.getInstructor() != null) ? c.getInstructor().name : "No instructor yet";
                System.out.println("Instructor: " + instr);
                found = true;
            }
        }

        if (!found) System.out.println("No courses found.");
    }

    public static void searchInstructor() {
        System.out.print("Enter ID or name keyword: ");
        String q = sc.nextLine().trim().toLowerCase();
        boolean found = false;
        for (Instructor i : instructors.values()) {
            if (i.userID.toLowerCase().contains(q) || i.name.toLowerCase().contains(q)) {
                i.displayInfo();
                found = true;
            }
        }
        if (!found) System.out.println("No instructors found.");
    }

    // View announcements
    public static void viewAnnouncements() {
        File f = new File(ANNOUNCE_FILE);
        if (!f.exists()) { System.out.println("No announcements yet."); return; }
        System.out.println("\n--- Announcements ---");
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) System.out.println(line);
        } catch (IOException e) {
            System.out.println("Error reading announcements: " + e.getMessage());
        }
    }

    /* ----------------------------
       Simple utility functions
       ---------------------------- */

    // Very small CSV splitter that handles quoted fields (not fully robust but sufficient here)
    private static String[] splitCSV(String line) {
        List<String> parts = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                inQuotes = !inQuotes;
            } else if (ch == ',' && !inQuotes) {
                parts.add(cur.toString().trim());
                cur.setLength(0);
            } else {
                cur.append(ch);
            }
        }
        parts.add(cur.toString().trim());
        return parts.toArray(new String[0]);
    }

    // Simple CSV escaping utility used above
    private static String escapeCSV(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            s = s.replace("\"", "\"\"");
            return "\"" + s + "\"";
        }
        return s;
    }

    // Safe integer parse with default
    private static int safeParseInt(String s, int def) {
        try { return Integer.parseInt(s.trim()); }
        catch (Exception e) { return def; }
    }

    // Safe double parse with default
    private static double safeParseDouble(String s, double def) {
        try { return Double.parseDouble(s.trim()); }
        catch (Exception e) { return def; }
    }

    // Very small email validation
    private static boolean isValidEmail(String email) {
        return email.contains("@") && email.contains(".") && email.indexOf('@') < email.lastIndexOf('.');
    }

    // Logging helper: append actions to audit log with timestamp
    public static void log(String msg) {
        String entry = formattedNow() + " | " + msg;
        try (PrintWriter pw = new PrintWriter(new FileWriter(AUDIT_LOG, true))) {
            pw.println(entry);
        } catch (IOException e) {
            // ignore log failures
        }
    }

    // Format current time nicely
    private static String formattedNow() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}