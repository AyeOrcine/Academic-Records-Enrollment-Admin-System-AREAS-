import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        List<Course> courses = new ArrayList<>();
        courses.add(new Course("CS101", "Intro to Programming"));
        courses.add(new Course("CS102", "Data Structures"));

        List<Student> students = FileUtils.loadStudents(courses, "students.txt");
        List<Instructor> instructors = FileUtils.loadInstructors(courses, "instructors.txt");
        List<String> announcements = new ArrayList<>();
        announcements.add("Welcome to AREAS Portal!");

        PortalService portal = new PortalService(students, instructors);

        boolean running = true;
        while (running) {
            System.out.println("\n--- AREAS Portal ---");
            System.out.println("1. Admin Login");
            System.out.println("2. Instructor Login");
            System.out.println("3. Student Login");
            System.out.println("4. Register Student");
            System.out.println("5. Register Instructor");
            System.out.println("6. Search Student");
            System.out.println("7. View Announcements");
            System.out.println("8. Exit");
            System.out.print("Enter choice: ");
            int choice = Integer.parseInt(sc.nextLine());

            switch (choice) {
                case 1 -> { // Admin login
    System.out.print("Admin Username: "); String u = sc.nextLine();
    System.out.print("Admin Password: "); String p = sc.nextLine();
    if (u.equals("admin") && p.equals("admin")) {
        System.out.println("Admin logged in!");
        FileUtils.clearConsole();
        

        boolean adminMenu = true;
        while (adminMenu) {
            System.out.println("\n--- Admin Menu ---");
            System.out.println("1. Assign Courses to Instructor");
            System.out.println("2. View Students");
            System.out.println("3. View Instructors");
            System.out.println("4. Logout");
            System.out.print("Enter choice: ");
            int aChoice = Integer.parseInt(sc.nextLine());

            switch (aChoice) {
                case 1 -> {
                    
                    System.out.println("Select instructor:");
                    for (int i = 0; i < instructors.size(); i++)
                        System.out.println(i + ": " + instructors.get(i).getUsername());
                    int iIndex = Integer.parseInt(sc.nextLine());
                    Instructor inst = instructors.get(iIndex);

                    System.out.println("Select course to assign:");
                    for (int j = 0; j < courses.size(); j++)
                        System.out.println(j + ": " + courses.get(j).getCourseName());
                    int cIndex = Integer.parseInt(sc.nextLine());

                    inst.addCourse(courses.get(cIndex));
                    FileUtils.saveInstructors(instructors, "instructors.txt"); // dynamic saving
                    System.out.println(courses.get(cIndex).getCourseName() + " assigned to " + inst.getUsername());
                }
                case 2 -> {
                    System.out.println("Students:");
                    for (Student s : students) System.out.println("- " + s.getUsername());
                }
                case 3 -> {
                    System.out.println("Instructors:");
                    for (Instructor i : instructors) System.out.println("- " + i.getUsername());
                }
                case 4 -> adminMenu = false; 
            }
        }
    } else System.out.println("Invalid admin credentials.");
}

                case 2 -> { // Instructor login
                    FileUtils.clearConsole();
                    System.out.println("==== Instructors ====");
                    Instructor instructor = (Instructor) portal.login(sc, "instructor");
                    if (instructor == null) { System.out.println("Invalid credentials."); break; }
                    boolean instrMenu = true;
                    while (instrMenu) {
                        System.out.println("1.View Courses 2.Assign Grade 3.Logout");
                        int ic = Integer.parseInt(sc.nextLine());
                        switch (ic) {
                            case 1 -> instructor.displayInfo();
                            case 2 -> {
                                if (students.isEmpty()) { System.out.println("No students."); break; }
                                System.out.println("Select student:");
                                for (int i = 0; i < students.size(); i++)
                                    System.out.println(i + ":" + students.get(i).getUsername());
                                int sIndex = Integer.parseInt(sc.nextLine());

                                if (instructor.getTeachingCourses().isEmpty()) { System.out.println("No courses assigned."); break; }
                                System.out.println("Select course:");
                                List<Course> teachCourses = instructor.getTeachingCourses();
                                for (int i = 0; i < teachCourses.size(); i++)
                                    System.out.println(i + ":" + teachCourses.get(i).getCourseName());
                                int cIndex = Integer.parseInt(sc.nextLine());

                                System.out.print("Enter grade: "); String grade = sc.nextLine();
                                instructor.setStudentGrade(students.get(sIndex), teachCourses.get(cIndex), grade);
                                FileUtils.saveStudents(students, "students.txt"); // dynamic saving
                            }
                            case 3 -> instrMenu = false;
                        }
                    }
                }
                case 3 -> { // Student login
                    FileUtils.clearConsole();
                    System.out.println("==== Student ====");
                    Student student = (Student) portal.login(sc, "student");
                    if (student == null) { System.out.println("Invalid credentials."); break; }
                    boolean studentMenu = true;
                    while (studentMenu) {
                        System.out.println("1.View Grades 2.Enroll in Course 3.Logout");
                        int scChoice = Integer.parseInt(sc.nextLine());
                        switch (scChoice) {
                            case 1 -> student.displayGrades();
                            case 2 -> {
                                System.out.println("Available courses:");
                                for (int i = 0; i < courses.size(); i++)
                                    System.out.println(i + ":" + courses.get(i).getCourseName());
                                System.out.print("Choose course to enroll: ");
                                int cIndex = Integer.parseInt(sc.nextLine());
                                Course course = courses.get(cIndex);
                                student.enrollCourse(course);
                                FileUtils.saveStudents(students, "students.txt"); // dynamic saving
                                System.out.println(student.getUsername() + " has been enrolled in " + course.getCourseName());
                            }
                            case 3 -> studentMenu = false;
                        }
                    }
                }
                case 4 -> { // Register Student
                    FileUtils.clearConsole();
                    System.out.println("==== Student Registration ====");
                    System.out.print("Username: "); String uname = sc.nextLine();
                    System.out.print("Password: "); String pass = sc.nextLine();
                    Student newStudent = new Student(uname, pass);
                    students.add(newStudent);
                    FileUtils.saveStudents(students, "students.txt"); // dynamic saving
                    System.out.println("Student registered and saved!");
                }
                case 5 -> { // Register Instructor
                    FileUtils.clearConsole();
                    System.out.println("==== Instructors Registration ====");
                    System.out.print("Username: "); String uname = sc.nextLine();
                    System.out.print("Password: "); String pass = sc.nextLine();
                    Instructor newInstructor = new Instructor(uname, pass);
                    instructors.add(newInstructor);
                    FileUtils.saveInstructors(instructors, "instructors.txt"); // dynamic saving
                    System.out.println("Instructor registered and saved!");
                }
                case 6 -> { // Search Student
                    FileUtils.clearConsole();
                    System.out.print("Search student username: "); String search = sc.nextLine();
                    boolean found = false;
                    for (Student s : students) {
                        if (s.getUsername().equals(search)) {
                            s.displayGrades();
                            found = true;
                        }
                    }
                    if (!found) System.out.println("Student not found.");
                    
                }
                case 7 -> { // Announcements
                    FileUtils.clearConsole();
                    System.out.println("Announcements:");
                    for (String a : announcements) System.out.println("- " + a);
                }
                case 8 -> running = false;
                
            }
        }

        sc.close();
        System.out.println("Portal exited. All changes saved dynamically.");
    }
}
