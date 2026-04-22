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

            int effectiveCost = Math.max(10, Math.min(12, BCRYPT_COST));
            String hashedPassword = BCrypt.hashpw(validatedPassword, BCrypt.gensalt(effectiveCost));
            User user = new User(UUID.randomUUID().toString(), sanitizedName, normalizedEmail, hashedPassword, true, Instant.now());
            userRepository.save(user);
            return new AuthResult(true, "Registration successful.");
        } catch (SQLException ex) {
            return new AuthResult(false, "Registration failed.");
        }
    }

    public AuthResult login(String email, String plainPassword) {
        String normalizedEmail = normalizeEmail(email);
        String password = sanitize(plainPassword);

        if (normalizedEmail.isEmpty() || password.isEmpty()) {
            return new AuthResult(false, "Email and password are required.");
        }
        if (!isValidEmail(normalizedEmail)) {
            return new AuthResult(false, "Invalid email or password");
        }

        try {
            Optional<User> userOptional = userRepository.findByEmail(normalizedEmail);
            if (userOptional.isEmpty()) {
                System.err.println("[ERROR] Authentication failed: user not found");
                return new AuthResult(false, "Invalid email or password");
            }

            User user = userOptional.get();
            if (!user.isActive()) {
                System.err.println("[ERROR] Authentication failed: account inactive");
                return new AuthResult(false, "Invalid email or password");
            }

            if (!BCrypt.checkpw(password, user.getPasswordHash())) {
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
