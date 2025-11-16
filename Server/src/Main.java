import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(String args[]) {
        try{
            ServerSocket ss = new ServerSocket(1234);
            System.out.println("Serveur en écoute...");
            Database db = new Database("jdbc:postgresql://localhost:5432/DB_mesure", "postgres", "root");
            while(true){
                // mode ecout ( attente )
                Socket s = ss.accept();
                System.out.println("Nouvelle connexion acceptée.");
                Thread c = new Communication(s,db);
                c.start();
                System.out.println("Serveur en écoute...");
            }
        }catch(IOException e){
            System.err.println("Erreur : "+e.getMessage());
        }
    }
}

