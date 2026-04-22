package ras.model;

public class User {
    private int id;
    private String username;
    private String passwordHash;
    private String role; // MANAGER, CLERK, STOREKEEPER
    private boolean locked;
    private int failedAttempts;

    public User(int id, String username, String passwordHash, String role, boolean locked, int failedAttempts) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.locked = locked;
        this.failedAttempts = failedAttempts;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getRole() { return role; }
    public boolean isLocked() { return locked; }
    public int getFailedAttempts() { return failedAttempts; }

    public void setId(int id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public void setRole(String role) { this.role = role; }
    public void setLocked(boolean locked) { this.locked = locked; }
    public void setFailedAttempts(int failedAttempts) { this.failedAttempts = failedAttempts; }

    @Override
    public String toString() { return username + " (" + role + ")"; }
}
