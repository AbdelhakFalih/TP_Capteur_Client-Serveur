import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class Auth {
    public int Authenticate(int id, String Login, String Password, PrintWriter ps) throws SQLException {
        int rep = 0;
        if (Password.equals("admin")) {
                Database.Insert_Capteur(id,Login,Password,"admin");
                rep = 1;
                ps.println("Votre Login est admin et enregister dans la bdd");
                ps.println("1");
        } else {
                Database.Insert_Capteur(id,Login,Password,"user");
                ps.println("Votre Login est user et enregister dans la bdd");
                ps.println("0");
        }
        return rep;
    }

}
