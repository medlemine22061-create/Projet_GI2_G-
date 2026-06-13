# MEDADRONE — Livraison d'organes par drones

CY TECH — ING1-GI — 2025-2026

Équipe : Ahmed Jeddou Mohamed Lemine · Abdy Yousef · Boukah Boukah

Dépôt  : https://github.com/medlemine22061-create/Projet_GI2_G-

---

## Description

Application JavaFX simulant un système de livraison d'organes par drones entre
des centres de prélèvement et des hôpitaux. Le système utilise les diagrammes
de Voronoï et la triangulation de Delaunay pour optimiser l'affectation des
bases de drones et les trajets de livraison.

Principes clés :
- Chaque zone d'hôpital est calculée par partitionnement de Voronoï
- La base optimale minimise dist(base->centre) + dist(base->hôpital)
- La triangulation de Delaunay vérifie l'adjacence géométrique entre sites
- Les médecins (MedicalStaff) jouent le rôle de points utilisateurs Voronoï

---

## Prérequis

- Java 17 ou supérieur
- Maven 3.8 ou supérieur
- Git

---

## Installation et lancement

### Étape 1 — Cloner le dépôt

    git clone https://github.com/medlemine22061-create/Projet_GI2_G-
    cd Projet_GI2_G-

### Étape 2 — Lancer l'interface JavaFX

    mvn javafx:run

### Étape 3 — Lancer la version ligne de commande (optionnel)

    mvn package -q
    java -jar target/ProjetJavaFX-1.0-SNAPSHOT-jar-with-dependencies.jar

### Étape 4 — Générer la JavaDoc

    mvn javadoc:javadoc
    mkdir -p docs
    cp -r target/site/apidocs/* docs/

---

## Structure du projet

    src/main/java/
    ├── model/
    │   ├── MedicalSite.java              Classe abstraite de base (hôpitaux + centres)
    │   ├── Hospital.java                 Hôpital receveur (émet les demandes)
    │   ├── CollectionCenter.java         Centre de prélèvement (origine de la livraison)
    │   ├── MedicalStaff.java             Médecin = point utilisateur Voronoï
    │   ├── Drone.java                    Drone médical
    │   ├── DroneBase.java                Base de stockage des drones
    │   ├── Mission.java                  Cycle de vie d'une mission de livraison
    │   ├── DeliveryRequest.java          Demande de livraison d'organe
    │   ├── Route.java                    Trajet (centre -> hôpital)
    │   ├── Position.java                 Coordonnées 2D
    │   ├── UserPoint.java                Point utilisateur Voronoï (= médecin)
    │   ├── MapModel.java                 Modèle central de la carte
    │   ├── VoronoiDiagram.java           Calcul du diagramme de Voronoï
    │   ├── VoronoiCell.java              Cellule Voronoï avec statistiques
    │   ├── DelaunayTriangulation.java    Triangulation de Delaunay
    │   └── Triangle.java                 Triangle avec méthodes du cercle circonscrit
    ├── service/
    │   ├── OptimizationService.java      Algorithme de sélection base + drone
    │   └── ImportExportService.java      Import CSV et export/import binaire
    ├── ui/
    │   ├── MainApp.java                  Point d'entrée JavaFX
    │   ├── MainWindow.java               Panneau latéral et gestion des actions
    │   └── MapCanvas.java                Canvas interactif de la carte
    └── app/
        └── Main.java                     Point d'entrée ligne de commande

---

## Format du fichier CSV

Créez un fichier (ex: sites.csv) et chargez-le via le bouton Import CSV :

    H,H3,Hopital Est,300,200
    H,H4,Hopital Ouest,650,450
    C,C3,Centre Nord,150,100,Rein|Coeur|Foie
    C,C4,Centre Sud,700,550,Poumon|Rein

    H -> Hôpital         : H, ID, Nom, X, Y
    C -> Centre prélèv.  : C, ID, Nom, X, Y, Organe1|Organe2|...

---

## Fonctionnalités de l'interface

    SITES MÉDICAUX
      Ajouter hôpital             Ajouter un hôpital aux coordonnées données
      Ajouter hôpitaux aléatoires Générer N hôpitaux à des positions aléatoires
      Ajouter centre prélèvement  Ajouter un centre de prélèvement
      Supprimer site              Supprimer un site par son ID
      Déplacer site               Déplacer par ID ou glisser-déposer sur la carte

    MÉDECINS (Points utilisateurs)
      Ajouter médecin             Enregistrer un médecin lié à un hôpital
      Supprimer médecin           Supprimer un médecin par son ID
      Lister médecins             Afficher tous les médecins par hôpital

    BASES DE DRONES
      Ajouter base                Créer une nouvelle base de drones
      Ajouter drone à base        Ajouter un drone à une base existante
      Déplacer base               Repositionner une base par ID

    DONNÉES CARTE
      Importer CSV                Importer hôpitaux et centres depuis un fichier CSV
      Exporter carte              Sauvegarder la carte en fichier binaire (~/ par défaut)
      Importer carte              Charger une carte sauvegardée

    DIAGNOSTICS
      Statistiques                Afficher stats des zones, distances, médecins
      Trouver site le plus proche Trouver le centre et la base optimale pour une position

    CONTRÔLE MISSION
      Créer mission               Saisir uniquement l'ID hôpital — le système choisit auto
      Lancer et animer            Démarrer le vol animé du drone avec chronomètre
      Suivre mission              Afficher position et batterie en direct
      Annuler mission             Annuler la mission et effacer le trajet

---

## Interactions sur la carte

    Clic sur hôpital ou centre    -> Panneau info (ID, nom, stats Voronoï)
    Clic sur base de drones       -> Panneau base (drones, niveaux de batterie)
    Clic sur centroïde triangle   -> Panneau triangle (distances, surface, médecins)
    Clic sur médecin (losange)    -> Surligne les zones Voronoï voisines
    Glisser un élément            -> Le déplacer sur la carte
    Molette souris                -> Zoom (les zones Voronoï deviennent plus visibles)
    Clic molette + glisser        -> Déplacer la vue (pan)

---

## Points utilisateurs — Choix de conception

Le cahier des charges demande des points utilisateurs génériques assignables
au site Voronoï le plus proche. Dans notre projet, seuls les médecins peuvent
émettre des demandes de livraison. Chaque médecin enregistré (MedicalStaff)
est donc automatiquement créé comme UserPoint à la position de son hôpital :

  - Il est lié au centre de prélèvement le plus proche via Voronoï
  - Il est compté dans les statistiques de zone (densité, distances)
  - Il apparaît comme un losange violet sur la carte
  - Un clic dessus surligne les zones voisines

---

## Commandes pour le jour de la présentation

    # 1. Ouvrir un terminal (pas d'IDE autorisé)
    git clone https://github.com/medlemine22061-create/Projet_GI2_G-
    cd Projet_GI2_G-

    # 2. Compiler
    mvn compile

    # 3. Lancer l'interface JavaFX
    mvn javafx:run

    # 4. Lancer le CLI si besoin
    mvn package -q
    java -jar target/ProjetJavaFX-1.0-SNAPSHOT-jar-with-dependencies.jar

---

## Convention de commits Git

    feat:     nouvelle fonctionnalité
    fix:      correction de bug
    docs:     mise à jour de la documentation
    refactor: restructuration du code sans changement fonctionnel

Exemples :
    feat: ajouter animation drone avec chronomètre
    fix: corriger constructeur OptimizationService (paramètre MapModel)
    docs: ajouter JavaDoc aux classes Mission et Drone

---

CY TECH — ING1-GI — 2025-2026
