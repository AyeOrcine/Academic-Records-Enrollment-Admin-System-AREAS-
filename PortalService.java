import java.util.List;
import java.util.Scanner;

public class PortalService {
    private List<Student> students;
    private List<Instructor> instructors;

    public PortalService(List<Student> students, List<Instructor> instructors) {
        this.students = students;
        this.instructors = instructors;
    }

    public User login(Scanner sc, String type) {
        System.out.print("Username: ");
        String username = sc.nextLine();
        System.out.print("Password: ");
        String password = sc.nextLine();

        switch (type.toLowerCase()) {
            case "student" -> {
                for (Student s : students)
                    if (s.getUsername().equals(username) && s.checkPassword(password)) return s;
            }
            case "instructor" -> {
                for (Instructor i : instructors)
                    if (i.getUsername().equals(username) && i.checkPassword(password)) return i;
            }
        }
        return null; 
    }
}
