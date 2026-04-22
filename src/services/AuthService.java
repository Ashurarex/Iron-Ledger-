package services;

import java.sql.SQLException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import models.AuthResult;
import models.User;
import repositories.UserRepository;
import utils.BCrypt;

public class AuthService {
    private static final int BCRYPT_COST = 10;

    private final UserRepository userRepository;
    private final SessionManager sessionManager;

    public AuthService(UserRepository userRepository, SessionManager sessionManager) {
        this.userRepository = userRepository;
        this.sessionManager = sessionManager;
    }

    public void initialize() throws SQLException {
        userRepository.ensureUsersTable();
    }

    public AuthResult register(String email, String plainPassword) {
        String inferredName = inferNameFromEmail(email);
        return register(inferredName, email, plainPassword);
    }

    public AuthResult register(String name, String email, String plainPassword) {
        String sanitizedName = sanitize(name);
        String normalizedEmail = normalizeEmail(email);
        String validatedPassword = sanitize(plainPassword);

        if (sanitizedName.isEmpty()) {
            return new AuthResult(false, "Name is required.");
        }
        if (normalizedEmail.isEmpty()) {
            return new AuthResult(false, "Email is required.");
        }
        if (!isValidEmail(normalizedEmail)) {
            return new AuthResult(false, "Invalid email format.");
        }
        if (validatedPassword.isEmpty()) {
            return new AuthResult(false, "Password is required.");
        }
        if (validatedPassword.length() < 6) {
            return new AuthResult(false, "Password must be at least 6 characters.");
        }

        try {
            if (userRepository.existsByEmail(normalizedEmail)) {
                return new AuthResult(false, "Email already registered.");
            }

            String hashedPassword = BCrypt.hashpw(validatedPassword, BCrypt.gensalt(10));
            System.out.println("[INFO] Registering user: " + normalizedEmail);
            
            boolean success = userRepository.createUser(sanitizedName, normalizedEmail, hashedPassword);
            if (success) {
                System.out.println("[INFO] User inserted successfully");
                return new AuthResult(true, "Registration successful.");
            } else {
                return new AuthResult(false, "Registration failed.");
            }
        } catch (SQLException ex) {
            return new AuthResult(false, "Registration failed.");
        }
    }

    public AuthResult login(String email, String plainPassword) {
        email = email.trim().toLowerCase();
        String password = plainPassword.trim();

        if (email.isEmpty() || password.isEmpty()) {
            return new AuthResult(false, "Email and password are required.");
        }
        if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            return new AuthResult(false, "Invalid email or password");
        }

        try {
            System.out.println("[DEBUG] Login email: " + email);
            User user = userRepository.findByEmail(email);
            System.out.println("[DEBUG] User found: " + (user != null));
            
            if (user == null) {
                System.err.println("[ERROR] Authentication failed: user not found");
                return new AuthResult(false, "Invalid email or password");
            }

            if (!user.isActive()) {
                System.err.println("[ERROR] Authentication failed: account inactive");
                return new AuthResult(false, "Invalid email or password");
            }

            System.out.println("[DEBUG] Stored hash: " + user.getPasswordHash());
            boolean passwordMatch = BCrypt.checkpw(password, user.getPasswordHash());
            System.out.println("[DEBUG] Password match: " + passwordMatch);

            if (!passwordMatch) {
                System.err.println("[ERROR] Authentication failed: incorrect password");
                return new AuthResult(false, "Invalid email or password");
            }

            sessionManager.startSession(user);
            return new AuthResult(true, "Login successful.");
        } catch (SQLException ex) {
            return new AuthResult(false, "Login failed.");
        }
    }

    private String normalizeEmail(String email) {
        return sanitize(email).toLowerCase();
    }

    private String sanitize(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    private String inferNameFromEmail(String email) {
        String normalizedEmail = normalizeEmail(email);
        int atIndex = normalizedEmail.indexOf('@');
        if (atIndex <= 0) {
            return "";
        }
        return normalizedEmail.substring(0, atIndex);
    }
}
