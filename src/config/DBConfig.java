package config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public final class DBConfig {
    private static final String PRIMARY_DB_URL_KEY = "IRON_LEDGER_DB_URL";
    private static final String PRIMARY_DB_USER_KEY = "IRON_LEDGER_DB_USER";
    private static final String PRIMARY_DB_PASSWORD_KEY = "IRON_LEDGER_DB_PASSWORD";
    private static final String PRIMARY_DB_POOL_SIZE_KEY = "IRON_LEDGER_DB_POOL_SIZE";

    private static final String FALLBACK_DB_URL_KEY = "SUPABASE_DB_URL";
    private static final String FALLBACK_DB_USER_KEY = "SUPABASE_DB_USER";
    private static final String FALLBACK_DB_PASSWORD_KEY = "SUPABASE_DB_PASSWORD";

    private static final String CONFIG_FILE_NAME = "config.properties";
    private static final Properties PROPERTIES = loadProperties();

    private DBConfig() {
    }

    public static String getDbUrl() {
        return getRequiredValue("DB_URL", PRIMARY_DB_URL_KEY, FALLBACK_DB_URL_KEY);
    }

    public static String getUsername() {
        return getRequiredValue("DB_USER", PRIMARY_DB_USER_KEY, FALLBACK_DB_USER_KEY);
    }

    public static String getPassword() {
        return getRequiredValue("DB_PASSWORD", PRIMARY_DB_PASSWORD_KEY, FALLBACK_DB_PASSWORD_KEY);
    }

    public static void validateRequiredVariables() {
        List<String> missing = new ArrayList<>();

        if (!hasValue("DB_URL", PRIMARY_DB_URL_KEY, FALLBACK_DB_URL_KEY)) {
            missing.add("DB_URL in " + CONFIG_FILE_NAME + " or " + PRIMARY_DB_URL_KEY + " (or " + FALLBACK_DB_URL_KEY + ")");
        }
        if (!hasValue("DB_USER", PRIMARY_DB_USER_KEY, FALLBACK_DB_USER_KEY)) {
            missing.add("DB_USER in " + CONFIG_FILE_NAME + " or " + PRIMARY_DB_USER_KEY + " (or " + FALLBACK_DB_USER_KEY + ")");
        }
        if (!hasValue("DB_PASSWORD", PRIMARY_DB_PASSWORD_KEY, FALLBACK_DB_PASSWORD_KEY)) {
            missing.add("DB_PASSWORD in " + CONFIG_FILE_NAME + " or " + PRIMARY_DB_PASSWORD_KEY + " (or " + FALLBACK_DB_PASSWORD_KEY + ")");
        }

        if (!missing.isEmpty()) {
            throw new IllegalStateException("Missing required database configuration: " + String.join(", ", missing));
        }
    }

    public static int getPoolSize() {
        String value = firstPresent(
            System.getenv(PRIMARY_DB_POOL_SIZE_KEY),
            PROPERTIES.getProperty("DB_POOL_SIZE")
        );
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

    private static String getRequiredValue(String propertyKey, String primaryEnvKey, String fallbackEnvKey) {
        String value = firstPresent(
            System.getenv(primaryEnvKey),
            System.getenv(fallbackEnvKey),
            PROPERTIES.getProperty(propertyKey)
        );
        if (value != null) {
            return value;
        }

        throw new IllegalStateException("Missing required database configuration. Set " + propertyKey + " in "
            + CONFIG_FILE_NAME + " or set " + primaryEnvKey + " (or " + fallbackEnvKey + ").");
    }

    private static boolean hasValue(String propertyKey, String primaryEnvKey, String fallbackEnvKey) {
        return firstPresent(
            System.getenv(primaryEnvKey),
            System.getenv(fallbackEnvKey),
            PROPERTIES.getProperty(propertyKey)
        ) != null;
    }

    private static String firstPresent(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        loadFromPath(properties, Path.of(CONFIG_FILE_NAME));

        if (properties.isEmpty()) {
            try (InputStream stream = DBConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE_NAME)) {
                if (stream != null) {
                    properties.load(stream);
                }
            } catch (IOException ex) {
                System.err.println("[WARN] Failed to read bundled " + CONFIG_FILE_NAME + ": " + ex.getMessage());
            }
        }
        return properties;
    }

    private static void loadFromPath(Properties properties, Path path) {
        if (!Files.isRegularFile(path)) {
            return;
        }

        try (InputStream stream = Files.newInputStream(path)) {
            properties.load(stream);
        } catch (IOException ex) {
            System.err.println("[WARN] Failed to read " + path + ": " + ex.getMessage());
        }
    }
}
