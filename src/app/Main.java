package app;

public class Main {
    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        AppLauncher launcher = new AppLauncher();
        launcher.launch();
    }
}
