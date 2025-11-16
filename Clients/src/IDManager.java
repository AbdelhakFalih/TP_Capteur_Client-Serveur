import java.io.*;

public class IDManager {
    private static final String FILE_NAME = "client_id.txt";

    public static int loadID() {
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_NAME))) {
            return Integer.parseInt(br.readLine());
        } catch (Exception e) {
            return 0; // Si le fichier n'existe pas → ID commence à 0
        }
    }

    public static void saveID(int id) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_NAME))) {
            pw.println(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
