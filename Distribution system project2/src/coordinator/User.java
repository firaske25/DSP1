package coordinator;

import java.io.Serializable;

public class User implements Serializable {

    private int userId;
    private String username;
    private String password;
    private String role;       // "Manager" أو "Employee"
    private String department; // مثل "development", "QA", "design"

    public User(int id,String username, String password, String role, String department) {
        this.userId = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.department = department;
    }

    // Getters
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    public String getDepartment() {
        return department;
    }

    public int getUserId(){
        return userId;
    }

    // Setters (إذا أردت تعديل بعض الحقول مستقبلاً)
    public void setPassword(String password) {
        this.password = password;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

}
