import config.DBConfig;
import database.ConnectionPool;
import repositories.UserRepository;
import services.AuthService;
import services.SessionManager;
import models.AuthResult;

public class TestRegistration {
    public static void main(String[] args) throws Exception {
        DBConfig.validateRequiredVariables();
        ConnectionPool pool = ConnectionPool.getInstance();
        UserRepository repo = new UserRepository(pool);
        SessionManager sm = SessionManager.getInstance();
        AuthService auth = new AuthService(repo, sm);
        auth.initialize();
        
        long time = System.currentTimeMillis();
        String email = "test" + time + "@example.com";
        String password = "password123";
        String name = "Test User " + time;
        
        System.out.println("Registering: " + email);
        AuthResult r1 = auth.register(name, email, password);
        System.out.println("Result: " + r1.isSuccess() + " - " + r1.getMessage());
        
        System.out.println("Registering duplicate: " + email);
        AuthResult r2 = auth.register(name, email, password);
        System.out.println("Result: " + r2.isSuccess() + " - " + r2.getMessage());
        
        System.out.println("Logging in: " + email);
        AuthResult r3 = auth.login(email, password);
        System.out.println("Result: " + r3.isSuccess() + " - " + r3.getMessage());
        
        System.out.println("Logging in wrong password: " + email);
        AuthResult r4 = auth.login(email, "wrongpassword");
        System.out.println("Result: " + r4.isSuccess() + " - " + r4.getMessage());

        System.out.println("Logging in wrong email: wrong" + email);
        AuthResult r5 = auth.login("wrong" + email, password);
        System.out.println("Result: " + r5.isSuccess() + " - " + r5.getMessage());
        
        System.exit(0);
    }
}
