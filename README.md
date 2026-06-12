# MEDADRONE — Livraison d’organes par drones

## 1. Présentation du projet

MEDADRONE est une application Java / JavaFX qui simule un système de livraison d’organes par drones entre des centres de prélèvement et des hôpitaux.

L’objectif est de proposer une solution permettant de :

- gérer une carte 2D ;
- ajouter, supprimer et déplacer des sites médicaux ;
- organiser l’espace avec Voronoï et Delaunay ;
- sélectionner automatiquement un drone disponible ;
- créer et suivre une mission de livraison ;
- importer et exporter une carte complète.

Dans notre projet, les centres de prélèvement représentent les points de référence, et les hôpitaux représentent les points demandeurs.

Un hôpital peut lancer une demande de livraison. Le système doit ensuite choisir un centre de prélèvement contenant l’organe demandé, sélectionner un drone disponible, puis suivre la mission jusqu’à la livraison de l’organe à l’hôpital receveur.

---

## 2. Contexte

Dans le domaine médical, le temps est un facteur critique lors du transport d’organes.

Un organe doit être transporté rapidement, de manière fiable et sécurisée, afin de préserver sa qualité et d’augmenter les chances de réussite de la transplantation.

L’utilisation de drones permet de réduire certains délais de transport, notamment en évitant les problèmes de circulation routière.

---

## 3. Problématique

La problématique du projet est la suivante :

**Comment organiser et optimiser la livraison d’un organe entre un centre de prélèvement et un hôpital receveur à l’aide de drones ?**

Le système doit prendre en compte :

- la position des centres de prélèvement ;
- la position des hôpitaux ;
- la position des bases de drones ;
- la disponibilité des drones ;
- l’autonomie des drones ;
- le calcul d’un trajet ;
- le suivi de la mission ;
- l’import et l’export des données de la carte.

---

## 4. Principe général

Le fonctionnement général de l’application est le suivant :

1. Un hôpital crée une demande de livraison.
2. Le système identifie le centre de prélèvement concerné.
3. Le système cherche un drone disponible.
4. Le drone part de sa position actuelle ou de sa base.
5. Le drone rejoint le centre de prélèvement.
6. Le drone récupère l’organe.
7. Le drone livre l’organe à l’hôpital demandeur.
8. La mission est suivie puis terminée.

Le trajet principal d’une mission est donc :

```text
Centre de prélèvement → Hôpital receveur
```

Dans l’interface graphique, on peut également visualiser le déplacement du drone depuis sa position ou sa base jusqu’au centre de prélèvement, puis vers l’hôpital.

---

## 5. Adaptation du cahier des charges à notre projet

Le cahier des charges parle de deux notions importantes :

```text
Points de référence
Points utilisateurs
```

Dans notre projet, nous avons adapté ces notions au domaine médical :

```text
Point de référence  = Centre de prélèvement
Point utilisateur   = Hôpital
```

Cela signifie que :

- les centres de prélèvement sont les points fixes à partir desquels les organes peuvent être envoyés ;
- les hôpitaux sont les points demandeurs qui reçoivent les organes ;
- chaque hôpital peut être associé au centre de prélèvement le plus proche grâce au diagramme de Voronoï.

---

## 6. Utilisation de Voronoï et Delaunay

Le projet utilise deux structures géométriques principales :

- le diagramme de Voronoï ;
- la triangulation de Delaunay.

Ces structures permettent d’organiser la carte et de représenter les relations de proximité entre les sites médicaux.

---

## 7. Diagramme de Voronoï

Dans notre application, le diagramme de Voronoï permet de découper l’espace en zones d’influence.

Les centres de prélèvement sont utilisés comme points de référence.

Chaque hôpital peut être associé au centre de prélèvement le plus proche.

Cela permet de répondre à la question suivante :

```text
Quel centre de prélèvement est le plus proche d’un hôpital donné ?
```

Dans notre implémentation, nous utilisons une version simplifiée mais fonctionnelle du diagramme de Voronoï.

---

## 8. Triangulation de Delaunay

La triangulation de Delaunay permet de relier les sites proches entre eux.

Dans notre projet, elle sert à visualiser les relations de proximité entre les sites médicaux sur la carte.

Elle permet aussi d’avoir une structure géométrique cohérente entre les points.

Dans notre implémentation, nous utilisons une version simplifiée mais fonctionnelle de la triangulation de Delaunay.

---

## 9. Fonctionnalités principales

L’application propose deux modes d’utilisation :

- une version en ligne de commande ;
- une interface graphique JavaFX.

---

## 10. Fonctionnalités de gestion de carte

L’application permet de :

- ajouter un hôpital ;
- ajouter un centre de prélèvement ;
- supprimer un site médical ;
- déplacer un site médical ;
- importer des sites médicaux depuis un fichier CSV ;
- exporter une carte complète en fichier binaire ;
- importer une carte complète depuis un fichier binaire.

---

## 11. Fonctionnalités Voronoï / Delaunay

L’application permet de :

- calculer les zones de Voronoï ;
- afficher les cellules de Voronoï ;
- trouver le site le plus proche d’une position ;
- calculer la triangulation de Delaunay ;
- afficher les triangles de Delaunay ;
- inspecter les informations d’une zone ;
- inspecter les informations d’un triangle ;
- afficher des statistiques.

---

## 12. Fonctionnalités drones et missions

L’application permet de :

- ajouter une base de drones ;
- ajouter un drone ;
- afficher les drones disponibles ;
- créer une demande de livraison ;
- créer automatiquement une mission ;
- sélectionner un drone disponible ;
- vérifier l’autonomie du drone ;
- démarrer une mission ;
- suivre une mission ;
- terminer une mission ;
- annuler une mission.

---

## 13. Technologies utilisées

Le projet utilise les technologies suivantes :

- Java ;
- JavaFX ;
- programmation orientée objet ;
- interface graphique avec Canvas JavaFX ;
- import CSV ;
- export binaire ;
- sérialisation binaire ;
- diagramme de Voronoï ;
- triangulation de Delaunay.

---

## 14. Organisation du projet

```text
src/main/java/
│
├── app/
│   └── Main.java
│
├── model/
│   ├── Position.java
│   ├── MedicalSite.java
│   ├── Hospital.java
│   ├── CollectionCenter.java
│   ├── MedicalStaff.java
│   ├── Drone.java
│   ├── DroneBase.java
│   ├── Route.java
│   ├── Mission.java
│   ├── DeliveryRequest.java
│   ├── MapModel.java
│   ├── VoronoiDiagram.java
│   ├── VoronoiCell.java
│   ├── DelaunayTriangulation.java
│   └── Triangle.java
│
├── model/enums/
│   ├── PriorityLevel.java
│   ├── RequestStatus.java
│   ├── DroneStatus.java
│   └── MissionStatus.java
│
├── service/
│   ├── OptimizationService.java
│   └── ImportExportService.java
│
└── ui/
    ├── MainApp.java
    ├── MainWindow.java
    └── MapCanvas.java
```

---

## 15. Description des packages

### Package `model`

Le package `model` contient les classes principales du projet.

Exemples :

- `Position` : représente une position sur la carte avec des coordonnées x et y.
- `MedicalSite` : classe abstraite représentant un site médical.
- `Hospital` : représente un hôpital receveur.
- `CollectionCenter` : représente un centre de prélèvement.
- `MedicalStaff` : représente un membre du personnel médical.
- `Drone` : représente un drone.
- `DroneBase` : représente une base contenant des drones.
- `DeliveryRequest` : représente une demande de livraison.
- `Mission` : représente une mission de livraison.
- `Route` : représente le trajet d’une mission.
- `MapModel` : représente la carte complète.
- `VoronoiDiagram` : calcule les zones de Voronoï.
- `VoronoiCell` : représente une cellule de Voronoï.
- `DelaunayTriangulation` : calcule la triangulation de Delaunay.
- `Triangle` : représente un triangle de Delaunay.

---

### Package `model.enums`

Le package `model.enums` contient les énumérations utilisées dans le projet.

Exemples :

- `PriorityLevel` : niveau de priorité d’une demande.
- `RequestStatus` : état d’une demande de livraison.
- `DroneStatus` : état d’un drone.
- `MissionStatus` : état d’une mission.

---

### Package `service`

Le package `service` contient les classes qui réalisent des traitements.

Exemples :

- `OptimizationService` : choisit un drone disponible et crée une mission.
- `ImportExportService` : importe et exporte la carte.

---

### Package `ui`

Le package `ui` contient l’interface graphique JavaFX.

Exemples :

- `MainApp` : lance l’application JavaFX.
- `MainWindow` : organise la fenêtre principale.
- `MapCanvas` : affiche la carte, les sites, les drones, Voronoï, Delaunay et les missions.

---

### Package `app`

Le package `app` contient la version en ligne de commande.

Exemple :

- `Main` : lance le menu CMD pour tester le modèle sans JavaFX.

---

## 16. Prérequis

Pour compiler et exécuter le projet, il faut installer :

- JDK 21 ou une version plus récente ;
- JavaFX SDK.

Dans notre environnement, JavaFX est par exemple installé ici :

```text
C:\Users\DFC\Desktop\javafx-sdk-26
```

Le dossier important est le dossier `lib` :

```text
C:\Users\DFC\Desktop\javafx-sdk-26\lib
```

Il doit contenir des fichiers comme :

```text
javafx.controls.jar
javafx.graphics.jar
javafx.base.jar
```

---

## 17. Compilation et exécution de la version CMD

La version CMD permet de tester le modèle sans lancer l’interface JavaFX.

Depuis la racine du projet :

```powershell
PS C:\program java\Projet_GI2_G->
```

Compiler sans le package `ui` :

```powershell
$files = Get-ChildItem -Recurse src\main\java\*.java | Where-Object { $_.FullName -notlike "*\ui\*" }
javac -d out $files.FullName
```

Lancer la version CMD :

```powershell
java -cp out app.Main
```

---

## 18. Compilation et exécution de JavaFX

Avant de compiler JavaFX, il faut indiquer le chemin du dossier `lib` de JavaFX.

Exemple :

```powershell
$javafx = "C:\Users\DFC\Desktop\javafx-sdk-26\lib"
```

Vérifier que le chemin existe :

```powershell
Test-Path $javafx
Get-ChildItem $javafx
```

La commande `Test-Path` doit afficher :

```text
True
```

Compiler tout le projet avec JavaFX :

```powershell
$files = Get-ChildItem -Recurse src\main\java\*.java
javac --module-path "$javafx" --add-modules javafx.controls,javafx.graphics -d out $files.FullName
```

Lancer l’interface JavaFX :

```powershell
java --module-path "$javafx" --add-modules javafx.controls,javafx.graphics -cp out ui.MainApp
```

---

## 19. Format du fichier CSV

L’application permet d’importer des sites médicaux depuis un fichier CSV.

Format pour un hôpital :

```text
H,H1,Hospital Nord,100,100
```

Format pour un centre de prélèvement :

```text
C,C1,Collection Center Est,200,500,Kidney|Heart|Liver
```

Exemple complet :

```text
H,H1,Hospital Nord,100,100
H,H2,Hospital Sud,500,300
C,C1,Collection Center Est,200,500,Kidney|Heart|Liver
C,C2,Collection Center Ouest,700,150,Kidney|Lung
```

Signification des colonnes :

```text
Type,Identifiant,Nom,X,Y,Types d’organes
```

- `H` signifie hôpital.
- `C` signifie centre de prélèvement.
- `X` et `Y` sont les coordonnées sur la carte.
- Les organes sont séparés avec `|`.

---

## 20. Import/export binaire

L’export binaire permet de sauvegarder l’état complet de la carte.

Les données sauvegardées peuvent contenir :

- les hôpitaux ;
- les centres de prélèvement ;
- les bases de drones ;
- les drones ;
- les positions ;
- les structures Voronoï ;
- les structures Delaunay.

Exemple de fichier exporté :

```text
map.bin
```

Dans le CMD ou dans JavaFX, on peut ensuite réimporter ce fichier pour récupérer la carte.

---

## 21. Démonstration conseillée

Pour présenter le projet, il est conseillé de montrer :

1. le lancement de l’application JavaFX ;
2. l’affichage des hôpitaux et centres de prélèvement ;
3. l’affichage de Voronoï ;
4. l’affichage de Delaunay ;
5. l’ajout d’un site médical ;
6. le déplacement d’un site médical ;
7. la recherche du site le plus proche ;
8. la création d’une demande de livraison ;
9. la création d’une mission ;
10. le suivi de la mission ;
11. l’export de la carte ;
12. l’import de la carte.

Si certaines fonctionnalités ne sont pas visibles dans JavaFX, il est possible de les montrer avec la version CMD.

---

## 22. Tests recommandés

Avant la présentation, il faut tester :

```text
[ ] Compilation du CMD
[ ] Lancement du CMD
[ ] Compilation de JavaFX
[ ] Lancement de JavaFX
[ ] Ajout d’un hôpital
[ ] Ajout d’un centre de prélèvement
[ ] Déplacement d’un site
[ ] Suppression d’un site
[ ] Import CSV
[ ] Affichage Voronoï
[ ] Affichage Delaunay
[ ] Recherche du site le plus proche
[ ] Ajout d’une base de drones
[ ] Ajout d’un drone
[ ] Création d’une demande de livraison
[ ] Création d’une mission automatique
[ ] Démarrage d’une mission
[ ] Suivi d’une mission
[ ] Fin d’une mission
[ ] Annulation d’une mission
[ ] Export binaire
[ ] Import binaire
```

Il faut aussi tester les erreurs :

```text
[ ] Mauvais identifiant
[ ] Mauvais nombre
[ ] Fichier CSV incorrect
[ ] Fichier binaire inexistant
[ ] Mission impossible si aucun drone disponible
```

---

## 23. Lancement depuis IntelliJ IDEA

Il est aussi possible de lancer le projet depuis IntelliJ IDEA.

### Pour lancer la version CMD

Exécuter la classe :

```text
app.Main
```

### Pour lancer JavaFX

Exécuter la classe :

```text
ui.MainApp
```

Il faut vérifier que JavaFX est bien configuré dans les options de lancement avec :

```text
--module-path "C:\Users\DFC\Desktop\javafx-sdk-26\lib" --add-modules javafx.controls,javafx.graphics
```

---

## 24. Équipe

Projet réalisé dans le cadre du module PGL ING1 à CY Tech.

Membres du groupe :

```text
Yousef
Med Lemin
Boukah
```

---

## 25. Avancement

- [x] Définition du sujet
- [x] Analyse des besoins
- [x] Modélisation des classes principales
- [x] Version CMD
- [x] Import/export binaire
- [x] Import CSV
- [x] Implémentation simplifiée de Voronoï
- [x] Implémentation simplifiée de Delaunay
- [x] Création des missions
- [x] Sélection automatique d’un drone
- [x] Interface JavaFX
- [ ] Amélioration finale de l’affichage JavaFX
- [ ] Tests finaux avant présentation

---

## 26. Remarques importantes

La version actuelle utilise des versions simplifiées de Voronoï et de Delaunay.

L’objectif n’est pas d’avoir une bibliothèque géométrique professionnelle, mais d’avoir une implémentation fonctionnelle qui permet de :

- organiser la carte ;
- associer les hôpitaux aux centres proches ;
- afficher les zones ;
- afficher les triangles ;
- tester les missions de livraison.

La version CMD est utile pour tester le modèle indépendamment de JavaFX.

L’interface JavaFX permet de visualiser la carte et de manipuler les principales fonctionnalités de manière graphique.

---

## 27. Commandes rapides

### Compiler et lancer le CMD

```powershell
$files = Get-ChildItem -Recurse src\main\java\*.java | Where-Object { $_.FullName -notlike "*\ui\*" }
javac -d out $files.FullName
java -cp out app.Main
```

### Compiler et lancer JavaFX

```powershell
$javafx = "C:\Users\DFC\Desktop\javafx-sdk-26\lib"
$files = Get-ChildItem -Recurse src\main\java\*.java
javac --module-path "$javafx" --add-modules javafx.controls,javafx.graphics -d out $files.FullName
java --module-path "$javafx" --add-modules javafx.controls,javafx.graphics -cp out ui.MainApp
```

