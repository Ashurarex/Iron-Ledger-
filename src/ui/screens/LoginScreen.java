package ui.screens;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import models.AuthResult;
import services.AuthService;
import ui.components.MaterialTextField;
import ui.theme.ThemeManager;

public class LoginScreen extends JFrame {
    private final AuthService authService;
    private final Runnable onLoginSuccess;
    private final Runnable onOpenRegister;

    private final MaterialTextField emailField;
    private final MaterialTextField passwordField;

    public LoginScreen(AuthService authService, Runnable onLoginSuccess, Runnable onOpenRegister) {
        super("Iron Ledger - Login");
        this.authService = authService;
        this.onLoginSuccess = onLoginSuccess;
        this.onOpenRegister = onOpenRegister;

        emailField = new MaterialTextField("Email");
        passwordField = new MaterialTextField("Password", true);

        initialize();
    }

    private void initialize() {
        ThemeManager tm = ThemeManager.getInstance();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 320);
        setResizable(false);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        getContentPane().setBackground(tm.getBackground());

        JPanel formPanel = new JPanel(new GridLayout(2, 1, 8, 8));
        formPanel.setOpaque(false);
        formPanel.add(emailField);
        formPanel.add(passwordField);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setOpaque(false);
        JButton registerButton = new JButton("Register");
        JButton loginButton = new JButton("Login");

        registerButton.addActionListener(event -> {
            dispose();
            onOpenRegister.run();
        });

        loginButton.addActionListener(event -> handleLogin());

        actionPanel.add(registerButton);
        actionPanel.add(loginButton);

        add(formPanel, BorderLayout.CENTER);
        add(actionPanel, BorderLayout.SOUTH);
    }

    private void handleLogin() {
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());

        AuthResult result = authService.login(email, password);
        if (result.isSuccess()) {
            JOptionPane.showMessageDialog(this, result.getMessage(), "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            onLoginSuccess.run();
            return;
        }

        JOptionPane.showMessageDialog(this, result.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}
