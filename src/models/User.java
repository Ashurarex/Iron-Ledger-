package models;

import java.time.Instant;

public class User {
    private final String id;
    private final String fullName;
    private final String email;
    private final String passwordHash;
    private final boolean active;
    private final Instant createdAt;

    public User(String id, String email, String passwordHash, Instant createdAt) {
        this(id, "", email, passwordHash, true, createdAt);
    }

    public User(String id, String fullName, String email, String passwordHash, boolean active, Instant createdAt) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.active = active;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public boolean isActive() {
        return active;
    }
}
