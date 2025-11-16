/**
 * SERVEUR - Thread de communication pour chaque client
 * 
 * Cette classe gère la communication avec un client connecté.
 * Chaque client est traité dans un thread séparé, permettant la gestion
 * simultanée de plusieurs clients (capteurs et administrateurs).
 * 
 * Flux de communication :
 * 1. Réception de l'ID du client
 * 2. Authentification (login/password)
 * 3. Redirection vers le mode approprié :
 *    - Mode ADMIN (rep == 1) : Menu interactif de consultation
 *    - Mode CAPTEUR (rep == 0) : Réception périodique de mesures
 * 
 * @author TP2 Multi-threading
 * @version 1.0
 */

import java.io.*;
import java.net.Socket;
import java.sql.SQLException;

public class Communication extends Thread {
    private Socket socket;
    private Database db;
    private Auth a = new Auth();

    private PrintWriter ps;
    private BufferedReader br;
    private int rep;

    public Communication(Socket socket, Database db) throws IOException {
        this.socket = socket;
        this.db = db;

        this.ps = new PrintWriter(socket.getOutputStream(), true);
        this.br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    @Override
    public void run() {

        try {
            // ======================== AUTHENTIFICATION ===========================
            String id_str = br.readLine();
            if (id_str == null) return;

            int id_capteur = Integer.parseInt(id_str);
            System.out.println("Client " + id_capteur + " connecté");

            ps.println("Entrer Votre Login :");
            String login = br.readLine();
            if (login == null) return;

            ps.println("Entrer Votre Password :");
            String password = br.readLine();
            if (password == null) return;

            rep = a.Authenticate(id_capteur, login, password, ps);

        } catch (Exception e) {
            System.err.println("Erreur connexion durant authentification : " + e.getMessage());
            return;
        }

        // ============================= ADMIN ===============================
        if (rep == 1) {
            try {
                System.out.println("Admin connecté !");

                ps.println("\n=== BONJOUR ADMIN ===" +
                        "\n=== MENU SUPERVISEUR ===" +
                        "\n1. Voir toutes les enregistrements :" +
                        "\n2. Voir les enregistrements d'un capteur :" +
                        "\n3. Voir les enregistrements d'une mesure :" +
                        "\n4. Voir les moyennes générale :" +
                        "\n5. Voir la moyenne d'un capteur :" +
                        "\n6. Voir la moyenne d'une mesure :" +
                        "\n7. Quitter");
                ps.flush();

                while (true) {
                    ps.println("Entrer votre choix :");  // ➤ Prompt
                    ps.flush();

                    String choixStr = br.readLine();
                    if (choixStr == null) {
                        System.out.println("Admin déconnecté.");
                        break;
                    }

                    int choix;
                    try {
                        choix = Integer.parseInt(choixStr.trim());
                    } catch (NumberFormatException e) {
                        ps.println("Choix invalide !");
                        ps.println("END");
                        ps.flush();
                        continue; // redemande le choix
                    }

                    System.out.println("Choix admin : " + choix);

                    switch (choix) {
                        case 1:
                            Mesures.Tous_mesure(ps);
                            break;

                        case 2:
                            ps.println("Entrer L'id du capteur ");
                            String id_str = br.readLine();
                            int id_capteur =  Integer.parseInt(id_str);
                            Mesures.Mesure_Par_Capteur(ps, id_capteur);
                            break;

                        case 3:
                            // Choix 3 : Voir les enregistrements d'une mesure
                            // Validation de la grandeur avec boucle de retry
                            String grandeur3 = null;
                            while (true) {
                                ps.println("Entrer la grandeur (temperature / humidite / pression) :");
                                ps.flush();
                                grandeur3 = br.readLine();
                                if (grandeur3 == null) break;

                                grandeur3 = grandeur3.trim().toLowerCase();

                                // Vérification de la validité de la grandeur
                                if (!grandeur3.equals("temperature") && !grandeur3.equals("humidite") && !grandeur3.equals("pression")) {
                                    // Grandeur invalide : on envoie une erreur et on redemande
                                    ps.println("ERREUR : Grandeur invalide !");
                                    ps.println("END");
                                    ps.flush();
                                    // Continue la boucle pour redemander la grandeur (sans revenir au menu)
                                } else {
                                    // Grandeur valide, on sort de la boucle
                                    break;
                                }
                            }
                            if (grandeur3 != null) {
                                // Appel à la méthode qui demande la limite et affiche les résultats
                                Mesures.Mesure_Par_Grandeur(br, ps, grandeur3);
                            }
                            break;
                        case 4:
                            Mesures.Moyenne_mesure(ps);
                            break;

                        case 5:
                            ps.println("Entrer L'id du capteur ");
                            String id_str2 = br.readLine();
                            int id_capteur2 =  Integer.parseInt(id_str2);
                            Mesures.Moyenne_Par_Capteur(ps,id_capteur2);
                            break;

                        case 6:
                            // Choix 6 : Voir la moyenne d'une mesure
                            // Validation de la grandeur avec boucle de retry
                            String g6 = null;
                            while (true) {
                                ps.println("Entrer la grandeur (temperature / humidite / pression) :");
                                ps.flush();
                                g6 = br.readLine();
                                if (g6 == null) break;

                                g6 = g6.trim().toLowerCase();

                                // Vérification de la validité de la grandeur
                                if (!g6.equals("temperature") && !g6.equals("humidite") && !g6.equals("pression")) {
                                    // Grandeur invalide : on envoie une erreur et on redemande
                                    ps.println("ERREUR : Grandeur invalide !");
                                    ps.println("END");
                                    ps.flush();
                                    // Continue la boucle pour redemander la grandeur (sans revenir au menu)
                                } else {
                                    // Grandeur valide, on sort de la boucle
                                    break;
                                }
                            }
                            if (g6 != null) {
                                // Calcul et affichage de la moyenne pour la grandeur spécifiée
                                Mesures.Moyenne_par_grandeur(ps, g6);
                            }
                            break;


                        case 7:
                            ps.println("Vous avez quitter");
                            ps.flush();
                            System.out.println("Admin a quitté.");
                            socket.close();
                            return;

                        default:
                            ps.println("Choix invalide !");
                            ps.println("END");
                            ps.flush();
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        // ============================= CAPTEUR ===============================
        else if (rep == 0) {
            System.out.println("Capteur connecté et envoi en cours...");

            try {
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

                while (true) {
                    Mesures m = (Mesures) ois.readObject();
                    System.out.println("Reçu du capteur : " + m);
                    m.Insertion_Mesures(
                            m.getId_capteur(),
                            m.getTemperature(),
                            m.getHumidite(),
                            m.getPression()
                    );
                }

            } catch (IOException e) {
                System.out.println("Capteur déconnecté.");
                return;  // ⬅ FIN DU THREAD
            } catch (Exception e) {
                System.err.println("Erreur capteur : " + e.getMessage());
                return;
            }
        }
    }
}
