import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

class User {
    protected String userID;
    protected String name;
    protected String email;
    protected String passwordHash;

    public User(String userID, String name, String email, String password) {
        this.userID = userID;
        this.name = name;
        this.email = email;
        this.passwordHash = hashPassword(password);
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    public void displayInfo() {
        System.out.println("ID: " + userID);
        System.out.println("Name: " + name);
        System.out.println("Email: " + email);
    }
}
