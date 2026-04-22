package config;

import java.util.ArrayList;
import java.util.List;

public final class DBConfig {
    private static final String PRIMARY_DB_URL_KEY = "IRON_LEDGER_DB_URL";
    private static final String PRIMARY_DB_USER_KEY = "IRON_LEDGER_DB_USER";
    private static final String PRIMARY_DB_PASSWORD_KEY = "IRON_LEDGER_DB_PASSWORD";

    private static final String FALLBACK_DB_URL_KEY = "SUPABASE_DB_URL";
    private static final String FALLBACK_DB_USER_KEY = "SUPABASE_DB_USER";
    private static final String FALLBACK_DB_PASSWORD_KEY = "SUPABASE_DB_PASSWORD";

    private DBConfig() {
    }

    public static String getDbUrl() {
        return getRequiredEnv(PRIMARY_DB_URL_KEY, FALLBACK_DB_URL_KEY);
    }

    public static String getUsername() {
        return getRequiredEnv(PRIMARY_DB_USER_KEY, FALLBACK_DB_USER_KEY);
    }

    public static String getPassword() {
        return getRequiredEnv(PRIMARY_DB_PASSWORD_KEY, FALLBACK_DB_PASSWORD_KEY);
    }

    public static void validateRequiredVariables() {
        List<String> missing = new ArrayList<>();

        if (!isPresent(PRIMARY_DB_URL_KEY) && !isPresent(FALLBACK_DB_URL_KEY)) {
            missing.add(PRIMARY_DB_URL_KEY + " (or " + FALLBACK_DB_URL_KEY + ")");
        }
        if (!isPresent(PRIMARY_DB_USER_KEY) && !isPresent(FALLBACK_DB_USER_KEY)) {
            missing.add(PRIMARY_DB_USER_KEY + " (or " + FALLBACK_DB_USER_KEY + ")");
        }
        if (!isPresent(PRIMARY_DB_PASSWORD_KEY) && !isPresent(FALLBACK_DB_PASSWORD_KEY)) {
            missing.add(PRIMARY_DB_PASSWORD_KEY + " (or " + FALLBACK_DB_PASSWORD_KEY + ")");
        }

        if (!missing.isEmpty()) {
            throw new IllegalStateException("Missing required database environment variables: " + String.join(", ", missing));
        }
    }

    public static int getPoolSize() {
        String value = System.getenv("IRON_LEDGER_DB_POOL_SIZE");
        if (value == null || value.isBlank()) {
            return 5;
        }

        try {
            int parsed = Integer.parseInt(value.trim());
            return Math.max(parsed, 1);
        } catch (NumberFormatException ex) {
            return 5;
        }
    }

    private static String getRequiredEnv(String primaryKey, String fallbackKey) {
        String value = System.getenv(primaryKey);
        if (value != null && !value.isBlank()) {
            return value.trim();
        }

        String fallbackValue = System.getenv(fallbackKey);
        if (fallbackValue != null && !fallbackValue.isBlank()) {
            return fallbackValue.trim();
        }

        throw new IllegalStateException(
            "Missing required database environment variable. Set " + primaryKey + " (or " + fallbackKey + ")."
        );
    }

    private static boolean isPresent(String key) {
        String value = System.getenv(key);
        return value != null && !value.isBlank();
    }
}
