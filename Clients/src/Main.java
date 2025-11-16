/**
 * CLIENT - Application de gestion de capteurs environnementaux
 * 
 * Ce client permet de se connecter au serveur et d'agir soit comme :
 * - Un CAPTEUR : envoie automatiquement des mesures (température, humidité, pression)
 * - Un ADMIN : consulte les mesures stockées dans la base de données
 * 
 * Architecture :
 * - Communication via Socket TCP sur le port 1234
 * - Authentification simple (login/password)
 * - Mode capteur : envoi périodique de mesures via ObjectOutputStream
 * - Mode admin : interaction textuelle via PrintWriter/BufferedReader
 * 
 * @author TP2 Multi-threading
 * @version 1.0
 */

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Main {
    /**
     * Variable statique pour stocker une ligne lue en avance
     * Utilisée pour gérer le cas où le prompt "limite" arrive avant
     * que le client ne soit prêt à le traiter (choix 3)
     */
    private static String ligneEnAttente = null;
    
    /**
     * Génère un nombre aléatoire dans une plage donnée
     * Utilisé par le mode capteur pour simuler des mesures
     * 
     * @param min Valeur minimale
     * @param max Valeur maximale
     * @return Nombre aléatoire entre min et max
     */
    public static float randomInRange(float min, float max) {
        return min + (float) Math.random() * (max - min);
    }

    /**
     * Point d'entrée principal du client
     * 
     * Flux d'exécution :
     * 1. Connexion au serveur (localhost:1234)
     * 2. Génération et envoi d'un ID unique
     * 3. Authentification (login/password)
     * 4. Redirection vers le mode approprié (capteur ou admin)
     */
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        ObjectOutputStream oos = null;
        PrintWriter ps = null;
        BufferedReader br = null;

        try {
            Socket client_socket = new Socket("localhost", 1234);

            ps = new PrintWriter(client_socket.getOutputStream(), true);
            br = new BufferedReader(new InputStreamReader(client_socket.getInputStream()));

            // Gestion de l'ID
            int id = IDManager.loadID() + 1;
            IDManager.saveID(id);

            // Envoi de l'ID au serveur
            ps.println(id);
            ps.flush();

            // --- Authentification ---
            System.out.print(br.readLine() + " ");
            ps.println(sc.nextLine());
            ps.flush();

            System.out.print(br.readLine() + " ");
            ps.println(sc.nextLine());
            ps.flush();

            // Réception du message + status
            String message = br.readLine();
            System.out.println(message != null ? message : "");

            String statusLine = br.readLine();
            if (statusLine == null) {
                return;
            }
            int status = Integer.parseInt(statusLine.trim());

            // --- CAPTEUR ---
            if (status == 0) {
                CapteurMod(id, client_socket, oos);
            }
            // --- ADMIN ---
            else if (status == 1) {
                AdminMode(client_socket, sc, br, ps);
            }

        } catch (Exception e) {
            System.err.println("Erreur : " + e.getMessage());
        } finally {
            try {
                if (oos != null) oos.close();
                if (ps != null) ps.close();
                if (br != null) br.close();
                sc.close();
            } catch (Exception ignored) {}
        }
    }

    /**
     * Mode Administrateur - Interface de consultation des mesures
     * 
     * Permet à l'administrateur de :
     * 1. Voir toutes les enregistrements
     * 2. Voir les enregistrements d'un capteur spécifique
     * 3. Voir les enregistrements d'une mesure (température/humidité/pression)
     * 4. Voir les moyennes générales
     * 5. Voir la moyenne d'un capteur
     * 6. Voir la moyenne d'une mesure
     * 7. Quitter
     * 
     * @param client_socket Socket de communication avec le serveur
     * @param sc Scanner pour la saisie utilisateur
     * @param br BufferedReader pour lire les réponses du serveur
     * @param ps PrintWriter pour envoyer les commandes au serveur
     * @throws IOException En cas d'erreur de communication
     */
    public static void AdminMode(Socket client_socket, Scanner sc, BufferedReader br, PrintWriter ps)
            throws IOException {

        while (true) {
            // 1. Attendre et afficher le prompt "Entrer votre choix :"
            String line;
            boolean promptTrouve = false;
            while ((line = br.readLine()) != null) {
                System.out.println(line);

                if (line.contains("Vous avez quitter") || line.contains("quitter")) {
                    return;
                }
                if (line.contains("Entrer votre choix :")) {
                    promptTrouve = true;
                    break;
                }
            }
            if (!promptTrouve) {
                break;
            }

            // 2. Saisie du choix
            System.out.print("Choix > ");
            String choix = sc.nextLine().trim();
            ps.println(choix);
            ps.flush();

            // Quitter
            if ("7".equals(choix)) {
                return;
            }

            // 3. Traitement spécifique selon le choix
            switch (choix) {
                case "1":
                case "4":
                    lireJusquaEND(br);
                    break;

                case "2":
                case "5":
                    demanderEtEnvoyer(br, ps, sc, "id du capteur");
                    lireJusquaEND(br);
                    break;
                case "3":
                    // Pour le choix 3, on gère grandeur et limite dans un seul appel
                    // car la limite peut être traitée directement après la grandeur valide
                    if (demanderEtEnvoyer(br, ps, sc, "grandeur")) {
                        // Si la grandeur était valide, on a peut-être déjà traité la limite
                        // Sinon, on doit la traiter maintenant
                        demanderEtEnvoyer(br, ps, sc, "limite");
                    }
                    lireJusquaEND(br);
                    break;
                case "6":
                    demanderEtEnvoyer(br, ps, sc, "grandeur");
                    lireJusquaEND(br);
                    break;
                default:
                    lireJusquaEND(br);
                    break;
            }

            // Séparateur visuel entre les opérations
            System.out.println("\n" + "─".repeat(70) + "\n");
        }
    }

    /**
     * Méthode utilitaire : lit et affiche les résultats du serveur jusqu'à "END"
     * Améliore l'affichage avec des bordures et un formatage clair
     */
    private static void lireJusquaEND(BufferedReader br) throws IOException {
        String line;
        boolean dansTableau = false;
        
        System.out.println("\n" + "═".repeat(70));
        System.out.println("📊 RÉSULTATS");
        System.out.println("═".repeat(70));
        
        while ((line = br.readLine()) != null) {
            if ("END".equals(line)) {
                break;
            }
            
            // Détecter les en-têtes de tableau
            if (line.contains("||") || line.contains("---") || line.contains("CAPTEUR") || 
                line.contains("Temperature") || line.contains("AVG_") || line.contains("Valeur")) {
                dansTableau = true;
                if (line.contains("---")) {
                    System.out.println("─".repeat(70));
                } else {
                    System.out.println("│ " + line);
                }
            } else if (line.contains("Mesures") || line.contains("Moyenne") || 
                      line.contains("Dernières") || line.contains("globale")) {
                // Titres de sections
                System.out.println("\n" + "─".repeat(70));
                System.out.println("  " + line);
                System.out.println("─".repeat(70));
            } else if (line.trim().isEmpty()) {
                // Ignorer les lignes vides
                continue;
            } else if (dansTableau && line.contains("||")) {
                // Lignes de données dans un tableau
                System.out.println("│ " + line);
            } else {
                // Autres lignes (valeurs simples, messages)
                System.out.println("  " + line);
            }
        }
        
        System.out.println("═".repeat(70) + "\n");
    }

    /**
     * Méthode utilitaire : attend un prompt précis du serveur et envoie la réponse de l'utilisateur
     * 
     * Fonctionnalités spéciales :
     * - Gère les erreurs de grandeur : si l'utilisateur entre une grandeur invalide,
     *   le serveur envoie "ERREUR" et redemande automatiquement la grandeur
     * - Gère les lignes en avance : pour le choix 3, si le prompt "limite" arrive
     *   avant que le client ne soit prêt, il est stocké dans ligneEnAttente
     * 
     * @param br BufferedReader pour lire les messages du serveur
     * @param ps PrintWriter pour envoyer les réponses
     * @param sc Scanner pour la saisie utilisateur
     * @param motCle Mot-clé à rechercher dans les prompts (ex: "grandeur", "limite", "id du capteur")
     * @return true si la saisie a été effectuée avec succès, false sinon
     * @throws IOException En cas d'erreur de communication
     */
    private static boolean demanderEtEnvoyer(BufferedReader br, PrintWriter ps, Scanner sc, String motCle)
            throws IOException {

        boolean attenteConfirmationGrandeur = false;

        while (true) {
            String line;
            
            // Si on a une ligne en attente, l'utiliser
            if (ligneEnAttente != null) {
                line = ligneEnAttente;
                ligneEnAttente = null;
            } else {
                line = br.readLine();
                if (line == null) return false;
            }

            System.out.println(line);

            // Si le serveur envoie ERREUR, on lit jusqu'à END puis on attend le nouveau prompt
            if (line.toLowerCase().contains("erreur")) {
                // Lire jusqu'à END pour nettoyer le buffer
                String endLine;
                while ((endLine = br.readLine()) != null) {
                    System.out.println(endLine);
                    if ("END".equalsIgnoreCase(endLine.trim())) {
                        break;
                    }
                }
                // Réinitialiser le flag car on va redemander la grandeur
                attenteConfirmationGrandeur = false;
                ligneEnAttente = null;
                // Après END, le serveur va redemander la grandeur
                // On continue la boucle pour lire le nouveau prompt dans la prochaine itération
                continue;
            }

            // Si END arrive (sans erreur précédente) → ignorer et continuer
            if ("END".equalsIgnoreCase(line.trim())) {
                continue;
            }

            // Si on attend la confirmation de la grandeur et qu'on reçoit autre chose qu'une erreur,
            // c'est que la grandeur était valide
            if (attenteConfirmationGrandeur) {
                // Pour le choix 3 : on reçoit le prompt "limite"
                // Pour le choix 6 : on reçoit directement les résultats (moyenne, etc.)
                if (line.toLowerCase().contains("limite")) {
                    // La grandeur était valide, on a reçu le prompt pour la limite
                    // Si on cherche "limite", on la traite maintenant
                    if (motCle.equals("limite")) {
                        System.out.print("> ");
                        String reponse = sc.nextLine().trim();
                        ps.println(reponse);
                        ps.flush();
                        return true;
                    } else {
                        // Sinon, on stocke cette ligne pour le prochain appel
                        ligneEnAttente = line;
                        return true; // La grandeur était valide
                    }
                } else if (line.toLowerCase().contains("moyenne") ||
                           line.toLowerCase().contains("dernières") ||
                           line.toLowerCase().contains("valeur") ||
                           line.toLowerCase().contains("globale")) {
                    // Pour le choix 6, on reçoit directement les résultats
                    return true;
                }
            }

            // Si on trouve le prompt avec le mot-clé (grandeur, limite, id du capteur)
            if (line.toLowerCase().contains(motCle.toLowerCase())) {
                System.out.print("> ");
                String reponse = sc.nextLine().trim();
                ps.println(reponse);
                ps.flush();
                
                // Pour la grandeur, on doit attendre la confirmation (pas d'erreur)
                // Si c'est invalide, le serveur enverra ERREUR et on recommencera
                // Si c'est valide, le serveur passera à la suite (limite pour choix 3, ou résultats pour choix 6)
                if (motCle.equals("grandeur")) {
                    // Marquer qu'on attend la confirmation
                    attenteConfirmationGrandeur = true;
                    // Continuer la boucle pour vérifier si on reçoit une erreur ou le prochain prompt
                    continue;
                } else {
                    // Pour les autres prompts (limite, id), on retourne directement
                    return true;
                }
            }

            // Si le serveur renvoie le menu → on quitte le prompt
            if (line.contains("Entrer votre choix")) {
                return false;
            }
        }
    }


    /**
     * Mode Capteur - Envoi périodique de mesures environnementales
     * 
     * Le capteur génère et envoie automatiquement des mesures toutes les 3 secondes :
     * - Température : entre 20°C et 25°C
     * - Humidité : entre 30% et 60%
     * - Pression : entre 980 hPa et 1040 hPa
     * 
     * Les mesures sont sérialisées et envoyées via ObjectOutputStream
     * Le serveur les reçoit et les stocke dans la base de données
     * 
     * @param id Identifiant unique du capteur
     * @param client_socket Socket de communication avec le serveur
     * @param oos ObjectOutputStream pour envoyer les objets Mesures
     * @throws IOException En cas d'erreur de communication
     * @throws InterruptedException Si le thread est interrompu pendant le sleep
     */
    public static void CapteurMod(int id, Socket client_socket, ObjectOutputStream oos) throws IOException, InterruptedException {
        oos = new ObjectOutputStream(client_socket.getOutputStream());

        while (true) {
            // Génération de mesures aléatoires dans des plages réalistes
            float t = Main.randomInRange(20f, 25f);  // Température en °C
            float h = Main.randomInRange(30f, 60f);  // Humidité en %
            float p = Main.randomInRange(980f, 1040f); // Pression en hPa

            // Création de l'objet Mesures
            Mesures m = new Mesures(id, t, h, p);

            // Envoi de l'objet au serveur
            oos.writeObject(m);
            oos.flush();
            oos.reset(); // Réinitialise le cache pour éviter les références partagées

            // Attente de 3 secondes avant la prochaine mesure
            Thread.sleep(3000);
        }
    }
}