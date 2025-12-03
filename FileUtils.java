import java.io.*;
import java.util.*;

public class FileUtils {

    public static void clearConsole() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    // -------- STUDENT PERSISTENCE --------
    public static void saveStudents(List<Student> students, String filename) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            for (Student s : students) {
                StringBuilder sb = new StringBuilder();
                sb.append(s.getUsername()).append(",").append("123"); // default password
                for (Course c : s.getGrades().keySet())
                    sb.append(",").append(c.getCourseCode()).append(":").append(s.getGrades().get(c));
                pw.println(sb.toString());
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    public static List<Student> loadStudents(List<Course> courses, String filename) {
        List<Student> students = new ArrayList<>();
        File file = new File(filename);
        if (!file.exists()) return students;

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                Student s = new Student(parts[0], parts[1]);
                for (int i = 2; i < parts.length; i++) {
                    String[] cg = parts[i].split(":");
                    String code = cg[0];
                    String grade = cg[1];
                    for (Course c : courses) {
                        if (c.getCourseCode().equals(code)) {
                            s.enrollCourse(c);
                            s.setGrade(c, grade);
                        }
                    }
                }
                students.add(s);
            }
        } catch (IOException e) { e.printStackTrace(); }
        return students;
    }

    // -------- INSTRUCTOR PERSISTENCE --------
    public static void saveInstructors(List<Instructor> instructors, String filename) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            for (Instructor i : instructors) {
                StringBuilder sb = new StringBuilder();
                sb.append(i.getUsername()).append(",").append("123");
                for (Course c : i.getTeachingCourses())
                    sb.append(",").append(c.getCourseCode());
                pw.println(sb.toString());
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    public static List<Instructor> loadInstructors(List<Course> courses, String filename) {
        List<Instructor> instructors = new ArrayList<>();
        File file = new File(filename);
        if (!file.exists()) return instructors;

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                Instructor i = new Instructor(parts[0], parts[1]);
                for (int j = 2; j < parts.length; j++) {
                    String code = parts[j];
                    for (Course c : courses) {
                        if (c.getCourseCode().equals(code)) i.addCourse(c);
                    }
                }
                instructors.add(i);
            }
        } catch (IOException e) { e.printStackTrace(); }
        return instructors;
    }
}
