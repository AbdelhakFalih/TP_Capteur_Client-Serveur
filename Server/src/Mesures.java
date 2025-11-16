import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Mesures implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private float Temperature ;
    private float Humidite ;
    private float Pression ;
    private int id_capteur;
    public Mesures(int id_capteur , float t , float h , float p) throws SQLException {
        this.Temperature = t;
        this.Humidite = h;
        this.Pression = p;
        System.out.println("Mesure Enregistrer dans la base de donnee par succes !!");
    }
    public void Insertion_Mesures(int id_capteur , float t , float h , float p) throws SQLException {
        int r = Database.Insert_mesure(id_capteur,t,h,p);
        if (r > 0) {
            System.out.println(r + " ligne(s) mise(s) à jour !");
        } else {
            System.out.println("Aucune ligne modifiée.");
        }
    }
    public static String formatRow(Object... cols) {
        return String.format("%-12s || %-15s || %-12s || %-15s", cols);
    }
    public static void Tous_mesure(PrintWriter ps) throws SQLException {
        ResultSet data = Database.findAll();

        ps.println("ID CAPTEUR   || Temperature(°C) || Humidite(%)  || Pression(hPa)");
        ps.println("---------------------------------------------------------------------");

        while (data.next()) {
            int id = data.getInt("id_capteur");
            float T = data.getFloat("temperature");
            float H = data.getFloat("humidite");
            float P = data.getFloat("pression");

            ps.println(formatRow(id, String.format("%.2f", T), String.format("%.2f", H), String.format("%.2f", P)));
        }

        ps.println("END");
        ps.flush();
    }
    public static void Mesure_Par_Capteur(PrintWriter ps, int id_capteur) throws SQLException {
        ResultSet data = Database.findByCapteur(id_capteur);

        ps.println("Mesures du capteur N° " + id_capteur);
        ps.println("Temperature(°C) || Humidite(%) || Pression(hPa)");
        ps.println("--------------------------------------------------");

        while (data.next()) {
            float T = data.getFloat("temperature");
            float H = data.getFloat("humidite");
            float P = data.getFloat("pression");

            ps.println(String.format("%-15s || %-12s || %-12s",
                    String.format("%.2f", T),
                    String.format("%.2f", H),
                    String.format("%.2f", P)
            ));
        }

        ps.println("END");
        ps.flush();
    }

    // Choix 3 – plus de vérification, on fait confiance au serveur
    public static void Mesure_Par_Grandeur(BufferedReader br, PrintWriter ps, String grandeur) throws SQLException, IOException {

        // Demande limite
        ps.println("Entrer la limite d'affichage (ex: 10) :");
        ps.flush();
        String limitStr = br.readLine();
        int limit = 10;
        if (limitStr != null && !limitStr.trim().isEmpty()) {
            try { limit = Integer.parseInt(limitStr.trim()); } catch (Exception e) { limit = 10; }
            if (limit <= 0) limit = 10;
        }

        ResultSet rs = Database.findByGradient(grandeur, limit);

        ps.println("Dernières " + limit + " mesures de " + grandeur + " :");
        ps.println("Valeur");
        ps.println("-------------------");

        int count = 0;
        while (rs.next() && count < limit) {
            ps.println(String.format("%.2f", rs.getFloat(grandeur)));
            count++;
        }
        if (count == 0) ps.println("Aucune donnée.");

        ps.println("END");
        ps.flush();
    }

    // Choix 6 – ultra simple
    public static void Moyenne_par_grandeur(PrintWriter ps, String grandeur) throws SQLException {
        ResultSet rs = Database.AVG_BY_GRADEIENT(grandeur);
        String nom = grandeur.substring(0, 1).toUpperCase() + grandeur.substring(1);

        ps.println("Moyenne globale de la " + nom + " :");

        if (rs.next()) {
            ps.println(String.format("%.2f", rs.getFloat("avg_" + grandeur)));
        } else {
            ps.println("Aucune donnée disponible.");
        }

        ps.println("END");
        ps.flush();
    }




    public static void Moyenne_mesure(PrintWriter ps) throws SQLException {
        ResultSet data = Database.Total_AVG();

        ps.println("Moyenne de toutes les mesures :");
        ps.println("AVG_Temperature || AVG_Humidite || AVG_Pression");
        ps.println("-------------------------------------------------");

        while (data.next()) {
            float T = data.getFloat("avg_temp");
            float H = data.getFloat("avg_humidite");
            float P = data.getFloat("avg_pression");

            ps.println(String.format("%-15s || %-12s || %-12s",
                    String.format("%.2f", T),
                    String.format("%.2f", H),
                    String.format("%.2f", P)
            ));
        }

        ps.println("END");
        ps.flush();
    }
    public static void Moyenne_Par_Capteur(PrintWriter ps, int id_capteur) throws SQLException, IOException {
        ResultSet data = Database.AVG_BY_SENSOR(id_capteur);
        ps.println("Moyenne des mesures du capteur N° " + id_capteur);
        ps.println("AVG_Temperature || AVG_Humidite || AVG_Pression");
        ps.println("-------------------------------------------------");

        while (data.next()) {
            float T = data.getFloat("avg_temp");
            float H = data.getFloat("avg_humidite");
            float P = data.getFloat("avg_pression");

            ps.println(String.format("%-15s || %-12s || %-12s",
                    String.format("%.2f", T),
                    String.format("%.2f", H),
                    String.format("%.2f", P)
            ));
        }

        ps.println("END");
        ps.flush();
    }



    public float getTemperature() {
        return Temperature;
    }

    public float getHumidite() {
        return Humidite;
    }

    public float getPression() {
        return Pression;
    }

    public int getId_capteur() {
        return id_capteur;
    }

    @Override
    public String toString() {
        return "Mesures{" +
                "Capteur=" + id_capteur +
                ", Temperature=" + Temperature +
                ", Humidite=" + Humidite +
                ", Pression=" + Pression +
                '}';
    }
}
