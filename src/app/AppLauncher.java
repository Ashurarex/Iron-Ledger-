package app;

import config.DBConfig;
import database.ConnectionPool;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.SwingUtilities;
import repositories.UserRepository;
import services.AuthService;
import services.SessionManager;
import ui.screens.DashboardScreen;
import ui.screens.LoginScreen;
import ui.screens.RegisterScreen;

public class AppLauncher {
    private ConnectionPool connectionPool;
    private AuthService authService;

    public AppLauncher() {
    }

    public void launch() {
        DBConfig.validateRequiredVariables();
        testDatabaseConnection();
        initializeAuthentication();
        showMainWindow();
    }

    private void initializeAuthentication() {
        try {
            UserRepository userRepository = new UserRepository(getConnectionPool());
            authService = new AuthService(userRepository, SessionManager.getInstance());
            authService.initialize();
            System.out.println("[INFO] Authentication module initialized");
        } catch (SQLException ex) {
            System.err.println("[ERROR] Failed to initialize authentication module: " + ex.getMessage());
            throw new IllegalStateException("Authentication initialization failed.", ex);
        }
    }

    private void testDatabaseConnection() {
        ConnectionPool pool = getConnectionPool();
        if (pool == null) {
            return;
        }

        Connection connection = null;
        try {
            connection = pool.acquireConnection();
            try (
                PreparedStatement statement = connection.prepareStatement("SELECT 1");
                ResultSet resultSet = statement.executeQuery()
            ) {
                if (resultSet.next()) {
                    System.out.println("[INFO] Database connected successfully");
                } else {
                    System.err.println("[ERROR] Database connection test query did not return expected result.");
                }
            }
        } catch (SQLException ex) {
            System.err.println("[ERROR] Database connection failed: " + ex.getMessage());
        } catch (IllegalStateException ex) {
            System.err.println("[ERROR] Database configuration error: " + ex.getMessage());
            throw ex;
        } finally {
            pool.releaseConnection(connection);
        }
    }

    private ConnectionPool getConnectionPool() {
        if (connectionPool != null) {
            return connectionPool;
        }

        try {
            connectionPool = ConnectionPool.getInstance();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                connectionPool.shutdown();
                System.out.println("[INFO] Connection pool shutdown complete");
            }));
            return connectionPool;
        } catch (IllegalStateException ex) {
            System.err.println("[ERROR] Database configuration error: " + ex.getMessage());
            throw ex;
        }
    }

    private void showMainWindow() {
        SwingUtilities.invokeLater(() -> {
            showLoginScreen();
        });
    }

    private void showLoginScreen() {
        LoginScreen loginScreen = new LoginScreen(
            authService,
            this::showDashboard,
            this::showRegisterScreen
        );
        loginScreen.setVisible(true);
    }

    private void showRegisterScreen() {
        RegisterScreen registerScreen = new RegisterScreen(
            authService,
            this::showLoginScreen,
            this::showLoginScreen
        );
        registerScreen.setVisible(true);
    }

    private void showDashboard() {
        DashboardScreen dashboardScreen = new DashboardScreen();
        dashboardScreen.setVisible(true);
    }
}
