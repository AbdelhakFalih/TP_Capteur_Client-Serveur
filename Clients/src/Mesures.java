import java.io.*;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class Mesures implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private float Temperature ;
    private float Humidite ;
    private float Pression ;
    private int id_capteur;
    public Mesures(int id_capteur , float t , float h , float p) {
        this.id_capteur = id_capteur;
        this.Temperature = t;
        this.Humidite = h;
        this.Pression = p;
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
