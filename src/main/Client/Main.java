package Client;

public class Main {

    public static void main(String[] args) {
        WindowManager windowManager = new WindowManager("Screen Share", 1280, 720);
        windowManager.initConnectionWindow();
        new ConnectionManager();
    }

}
