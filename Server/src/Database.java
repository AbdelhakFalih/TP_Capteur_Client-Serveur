import java.sql.*;

public class Database {

    private static Connection conn; // 🔹 On garde la connexion dans la classe

    public Database(String url, String username, String password) {
        try {
            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection(url, username, password);
            System.out.println("Connexion réussite a votre base de données!");
        } catch (Exception e) {
            System.out.println("❌ Connection Failed!");
            e.printStackTrace();
        }
    }

    // ✅ Retourne toutes les mesures
    public static ResultSet findAll() throws SQLException {
        Statement stmt = conn.createStatement();
        return stmt.executeQuery("SELECT * FROM Mesures LIMIT 10");
    }

    // ✅ Recherche par id_capteur - sécurisée (PreparedStatement)
    public static ResultSet findByCapteur(int id_capteur) throws SQLException {
        String sql = "SELECT * FROM Mesures WHERE id_capteur = ? LIMIT 10";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, id_capteur);
        return pstmt.executeQuery();
    }

    public static ResultSet findByGradient(String g , int l) throws SQLException {
        String sql = "SELECT " + g + " FROM Mesures LIMIT ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, l);
        return pstmt.executeQuery();
    }

    public static int Insert_mesure(int id_capteur, float T, float H, float P) throws SQLException {
        String sql = "INSERT INTO public.\"mesures\" (temperature, humidite, pression, id_capteur) VALUES (?, ?, ?, ?)";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setFloat(1, T);
        pstmt.setFloat(2, H);
        pstmt.setFloat(3, P);
        pstmt.setInt(4, id_capteur);
        int rows = pstmt.executeUpdate();
        return rows;
    }


    public static void Insert_Capteur(int id_capteur, String Login, String Password, String Role) throws SQLException {
        String sql = "INSERT INTO public.\"Users\" (id, username, password, role) VALUES (?, ?, ?, ?)";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, id_capteur);
        pstmt.setString(2, Login);
        pstmt.setString(3, Password);
        pstmt.setString(4, Role);
        pstmt.executeUpdate(); // ✅ exécuter
        pstmt.close();         // ✅ fermer
    }

    public static ResultSet Total_AVG() throws SQLException {
        String sql = "SELECT " +
                "AVG(temperature) AS avg_temp, " +
                "AVG(humidite) AS avg_humidite, " +
                "AVG(pression) AS avg_pression " +
                "FROM Mesures";
        Statement stmt = conn.createStatement();
        return stmt.executeQuery(sql);
    }


    public static ResultSet AVG_BY_SENSOR(int id_capteur) throws SQLException {
        String sql = "SELECT " +
                "AVG(temperature) AS avg_temp, " +
                "AVG(humidite) AS avg_humidite, " +
                "AVG(pression) AS avg_pression " +
                "FROM Mesures " +
                "WHERE id_capteur = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, id_capteur);
        return pstmt.executeQuery();
    }

    public static ResultSet AVG_BY_GRADEIENT(String m) throws SQLException {
        String sql = "SELECT AVG(" + m + ") AS avg_" + m + " FROM Mesures";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        ResultSet rs = pstmt.executeQuery();
        return rs;
    }




    // ✅ Fermer proprement
    public static void close() throws SQLException {
        if (conn != null) {
            conn.close();
            System.out.println("🔒 Connection closed.");
        }
    }
}
