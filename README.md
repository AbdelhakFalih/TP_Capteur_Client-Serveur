# 📡 Système de Gestion de Capteurs Environnementaux

## 📋 Description

Ce projet implémente un système client-serveur multi-threadé pour la gestion de capteurs environnementaux. Le système permet de collecter, stocker et consulter des mesures de température, d'humidité et de pression atmosphérique.

### Fonctionnalités principales

- **Mode Capteur** : Envoi automatique et périodique de mesures environnementales
- **Mode Administrateur** : Interface de consultation et d'analyse des données collectées
- **Architecture multi-threadée** : Gestion simultanée de plusieurs clients
- **Base de données PostgreSQL** : Stockage persistant des mesures

---

## 🏗️ Architecture

### Structure du projet

```
Tp2_multi_threading/
├── Server/                 # Application serveur
│   └── src/
│       ├── Main.java       # Point d'entrée du serveur
│       ├── Communication.java  # Thread de communication par client
│       ├── Database.java   # Gestion de la base de données
│       ├── Auth.java       # Authentification des clients
│       └── Mesures.java    # Traitement des mesures
│
├── Clients/                # Application client
│   └── src/
│       ├── Main.java       # Point d'entrée du client
│       ├── IDManager.java  # Gestion des IDs de capteurs
│       └── Mesures.java    # Classe de données pour les mesures
│
└── README.md              # Ce fichier
```

### Composants principaux

#### Serveur (`Server/`)

1. **Main.java** : Serveur principal qui écoute sur le port 1234
   - Crée un thread pour chaque client connecté
   - Gère la connexion à la base de données PostgreSQL

2. **Communication.java** : Thread de communication
   - Gère l'authentification (login/password)
   - Redirige vers le mode approprié (admin ou capteur)
   - Traite les requêtes de l'administrateur
   - Reçoit les mesures des capteurs

3. **Database.java** : Gestion de la base de données
   - Connexion PostgreSQL
   - Requêtes SQL (SELECT, INSERT, AVG)
   - Utilisation de PreparedStatement pour la sécurité

4. **Auth.java** : Authentification
   - Vérifie le mot de passe
   - Détermine le type d'utilisateur (admin si password = "admin", sinon capteur)
   - Enregistre l'utilisateur dans la base de données

5. **Mesures.java** : Traitement des mesures
   - Affichage de toutes les mesures
   - Filtrage par capteur ou par grandeur
   - Calcul de moyennes

#### Client (`Clients/`)

1. **Main.java** : Application client principale
   - Connexion au serveur
   - Authentification
   - Mode Capteur : génération et envoi de mesures
   - Mode Admin : interface de consultation

2. **IDManager.java** : Gestion des IDs
   - Génération d'IDs uniques pour les capteurs
   - Persistance dans un fichier texte

3. **Mesures.java** : Classe de données
   - Représente une mesure (température, humidite, pression)
   - Sérialisable pour l'envoi via ObjectOutputStream

---

## 🚀 Installation et Configuration

### Prérequis

- **Java JDK 8+**
- **PostgreSQL** (version 12 ou supérieure)
- **IDE** (IntelliJ IDEA, Eclipse, ou éditeur de texte)

### Configuration de la base de données

1. **Créer la base de données** :
```sql
CREATE DATABASE DB_mesure;
```

2. **Créer les tables nécessaires** :
```sql
-- Table pour les capteurs/utilisateurs
CREATE TABLE capteurs (
    id_capteur INTEGER PRIMARY KEY,
    login VARCHAR(50),
    password VARCHAR(50),
    type VARCHAR(20)
);

-- Table pour les mesures
CREATE TABLE mesures (
    id SERIAL PRIMARY KEY,
    id_capteur INTEGER,
    temperature FLOAT,
    humidite FLOAT,
    pression FLOAT,
    date_mesure TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

3. **Configurer les identifiants** dans `Server/src/Main.java` :
```java
Database db = new Database("jdbc:postgresql://localhost:5432/DB_mesure", "postgres", "root");
```
   - Modifier `"postgres"` et `"root"` selon vos identifiants PostgreSQL

### Compilation

#### Serveur
```bash
cd Server/src
javac -cp ".:postgresql-42.x.x.jar" *.java
```

#### Client
```bash
cd Clients/src
javac *.java
```

**Note** : Assurez-vous d'avoir le driver PostgreSQL (`postgresql-42.x.x.jar`) dans le classpath du serveur.

---

## 💻 Utilisation

### Démarrage du serveur

```bash
cd Server/src
java -cp ".:postgresql-42.x.x.jar" Main
```

Le serveur affiche :
```
Connexion réussite a votre base de données!
Serveur en écoute...
```

### Démarrage du client

#### Mode Capteur

1. Lancer le client :
```bash
cd Clients/src
java Main
```

2. Entrer un login (n'importe quel nom)
3. Entrer un mot de passe (n'importe quoi sauf "admin")

Le capteur commence automatiquement à envoyer des mesures toutes les 3 secondes :
- Température : 20-25°C
- Humidité : 30-60%
- Pression : 980-1040 hPa

#### Mode Administrateur

1. Lancer le client :
```bash
cd Clients/src
java Main
```

2. Entrer un login (n'importe quel nom)
3. Entrer le mot de passe : **`admin`**

Le menu administrateur s'affiche :

```
=== BONJOUR ADMIN ===
=== MENU SUPERVISEUR ===
1. Voir toutes les enregistrements
2. Voir les enregistrements d'un capteur
3. Voir les enregistrements d'une mesure
4. Voir les moyennes générale
5. Voir la moyenne d'un capteur
6. Voir la moyenne d'une mesure
7. Quitter
```

---

## 📊 Fonctionnalités Administrateur

### 1. Voir toutes les enregistrements
Affiche les 10 dernières mesures de tous les capteurs.

### 2. Voir les enregistrements d'un capteur
Demande l'ID du capteur et affiche ses 10 dernières mesures.

### 3. Voir les enregistrements d'une mesure
- Demande la grandeur : `temperature`, `humidite`, ou `pression`
- **Validation** : Si la grandeur est invalide, le serveur redemande uniquement la grandeur (sans revenir au menu)
- Demande le nombre de mesures à afficher (limite)
- Affiche les N dernières mesures de la grandeur spécifiée

### 4. Voir les moyennes générales
Affiche la moyenne de toutes les mesures (température, humidité, pression).

### 5. Voir la moyenne d'un capteur
Demande l'ID du capteur et affiche les moyennes de ses mesures.

### 6. Voir la moyenne d'une mesure
- Demande la grandeur : `temperature`, `humidite`, ou `pression`
- **Validation** : Si la grandeur est invalide, le serveur redemande uniquement la grandeur (sans revenir au menu)
- Affiche la moyenne globale de la grandeur spécifiée

### 7. Quitter
Ferme la connexion et retourne au système.

---

## 🔄 Flux de Communication

### Authentification

```
Client → Serveur : ID du capteur
Serveur → Client : "Entrer Votre Login :"
Client → Serveur : login
Serveur → Client : "Entrer Votre Password :"
Client → Serveur : password
Serveur → Client : Message de confirmation + Status (0=capteur, 1=admin)
```

### Mode Capteur

```
Client → Serveur : Objet Mesures (sérialisé)
[Attente 3 secondes]
Client → Serveur : Objet Mesures (sérialisé)
...
```

### Mode Admin

```
Serveur → Client : Menu
Serveur → Client : "Entrer votre choix :"
Client → Serveur : choix (1-7)
[Selon le choix, échange de données]
Serveur → Client : Résultats + "END"
```

### Gestion des erreurs (Choix 3 et 6)

```
Serveur → Client : "Entrer la grandeur (temperature / humidite / pression) :"
Client → Serveur : "valeur_invalide"
Serveur → Client : "ERREUR : Grandeur invalide !"
Serveur → Client : "END"
Serveur → Client : "Entrer la grandeur (temperature / humidite / pression) :"  [Redemande]
Client → Serveur : "temperature"  [Valeur valide]
Serveur → Client : [Suite du traitement]
```

---

## 🛠️ Technologies Utilisées

- **Java** : Langage de programmation
- **Socket TCP** : Communication réseau
- **PostgreSQL** : Base de données relationnelle
- **Multi-threading** : Gestion simultanée de clients
- **Sérialisation Java** : Envoi d'objets via le réseau

---

## 📝 Notes Techniques

### Sécurité

- Utilisation de `PreparedStatement` pour éviter les injections SQL
- Validation des entrées utilisateur (grandeurs, IDs)
- Gestion des erreurs de connexion

### Performance

- Architecture multi-threadée pour gérer plusieurs clients simultanément
- Chaque client est traité dans un thread séparé
- Pas de blocage entre les clients

### Gestion des erreurs

- Validation des grandeurs avec retry automatique (choix 3 et 6)
- Gestion des déconnexions
- Messages d'erreur clairs pour l'utilisateur

---

## 🐛 Dépannage

### Le serveur ne démarre pas

- Vérifiez que PostgreSQL est démarré
- Vérifiez les identifiants de connexion dans `Main.java`
- Vérifiez que le port 1234 n'est pas déjà utilisé

### Erreur de connexion à la base de données

- Vérifiez que la base de données `DB_mesure` existe
- Vérifiez que les tables sont créées
- Vérifiez les identifiants PostgreSQL

### Le client ne se connecte pas

- Vérifiez que le serveur est démarré
- Vérifiez que le port 1234 est accessible
- Vérifiez l'adresse du serveur (localhost par défaut)

---

## 📚 Structure des Données

### Table `mesures`

| Colonne      | Type    | Description                    |
|--------------|---------|--------------------------------|
| id           | SERIAL  | Identifiant unique             |
| id_capteur   | INTEGER | ID du capteur                  |
| temperature  | FLOAT   | Température en °C              |
| humidite     | FLOAT   | Humidité en %                  |
| pression     | FLOAT   | Pression en hPa                |
| date_mesure  | TIMESTAMP | Date et heure de la mesure   |

### Table `capteurs`

| Colonne    | Type    | Description                    |
|------------|---------|--------------------------------|
| id_capteur | INTEGER | Identifiant unique             |
| login      | VARCHAR | Nom d'utilisateur              |
| password   | VARCHAR | Mot de passe                   |
| type       | VARCHAR | Type (admin ou user)           |

---

## 👥 Auteurs

TP2 Multi-threading - Module Client/Serveur

---

## 📄 Licence

Ce projet est un travail académique.

---

## 🔮 Améliorations Possibles

- Interface graphique (JavaFX, Swing)
- Chiffrement des communications (SSL/TLS)
- Authentification plus sécurisée (hashage des mots de passe)
- Export des données (CSV, JSON)
- Graphiques et visualisations
- Notifications en temps réel
- API REST pour l'accès aux données

---

## 📞 Support

Pour toute question ou problème, consultez les commentaires dans le code source ou contactez votre enseignant.

---

**Version** : 1.0  
**Dernière mise à jour** : 2024

