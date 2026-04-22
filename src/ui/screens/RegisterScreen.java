package ui.screens;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import models.AuthResult;
import services.AuthService;

public class RegisterScreen extends JFrame {
    private final AuthService authService;
    private final Runnable onRegisterSuccess;
    private final Runnable onBackToLogin;

    private final JTextField nameField;
    private final JTextField emailField;
    private final JPasswordField passwordField;

    public RegisterScreen(AuthService authService, Runnable onRegisterSuccess, Runnable onBackToLogin) {
        super("Iron Ledger - Register");
        this.authService = authService;
        this.onRegisterSuccess = onRegisterSuccess;
        this.onBackToLogin = onBackToLogin;

        nameField = new JTextField();
        emailField = new JTextField();
        passwordField = new JPasswordField();

        initialize();
    }

    private void initialize() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(500, 320);
        setResizable(false);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(6, 1, 8, 8));
        formPanel.add(new JLabel("Name"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Email"));
        formPanel.add(emailField);
        formPanel.add(new JLabel("Password"));
        formPanel.add(passwordField);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton backButton = new JButton("Back to Login");
        JButton registerButton = new JButton("Register");

        backButton.addActionListener(event -> {
            dispose();
            onBackToLogin.run();
        });

        registerButton.addActionListener(event -> handleRegister());

        actionPanel.add(backButton);
        actionPanel.add(registerButton);

        add(formPanel, BorderLayout.CENTER);
        add(actionPanel, BorderLayout.SOUTH);
    }

    private void handleRegister() {
        String name = nameField.getText();
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());

        try {
            AuthResult result = authService.register(name, email, password);
            if (result.isSuccess()) {
                JOptionPane.showMessageDialog(this, "Registration successful", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
                onRegisterSuccess.run();
            } else {
                JOptionPane.showMessageDialog(this, result.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            System.out.println("[ERROR] Unexpected error during registration: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "An unexpected error occurred: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
