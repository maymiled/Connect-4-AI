# Connect-4-AI

# Projet IA - Connect 4 AI

Ce projet consiste à implémenter une intelligence artificielle capable de jouer à une variante du jeu **Puissance 4**, où le joueur IA ne peut pas voir tous les jetons adverses. Ce projet a été réalisé dans le cadre d'un cours d'intelligence artificielle.

## Fonctionnalités principales

1. **Heuristique pour évaluation des positions :**
   - Une matrice d'évaluation des cases est utilisée pour orienter les coups de l'IA en fonction des alignements possibles.
   - L'IA favorise les colonnes centrales, qui offrent plus de possibilités de victoires diagonales.

2. **Environnement partiellement observable :**
   - L'IA ne voit que les jetons visibles selon les règles spécifiques du jeu.
   - Utilisation d'états de croyance (*Belief States*) pour gérer les informations manquantes.

3. **Algorithmes d'optimisation :**
   - Recherche alpha-bêta pour explorer les coups optimaux.
   - Gestion de la profondeur de recherche en fonction de l'état du jeu.

4. **Interface utilisateur :**
   - Une interface graphique permet de jouer contre l'IA ou de simuler des parties automatiques.
   - Visualisation en temps réel des coups joués et des messages d'état (victoire, égalité, etc.).

## Organisation du projet

### Fichiers principaux

- `AI.java` : Contient l'implémentation de l'IA, y compris l'heuristique, la recherche alpha-bêta, et la fonction `findNextMove` pour déterminer le meilleur coup.
- `GameState.java` : Gère l'état actuel du plateau, y compris les vérifications de victoire et les mises à jour après chaque coup.
- `GameDisplay.java` : Permet de visualiser l'état du jeu et de gérer l'interface graphique.
- `ProbabilisticOpponentAI.java` : Implémente l'adversaire probabiliste, simulant des coups basés sur des heuristiques.
- `RandomSelector.java` : Utilitaire pour sélectionner aléatoirement parmi plusieurs options pondérées par des probabilités.
- `BoardDrawing.java` : Dessine le plateau et les pièces sur l'interface utilisateur.
- `Connect4UI.java` : Point d'entrée du programme, configure l'interface utilisateur et initialise les composants.


## Exécution du projet

### Prérequis

- **Java 8** ou version supérieure.
- Un IDE Java (comme IntelliJ IDEA ou Eclipse) ou un environnement en ligne de commande avec `javac` et `java`.

### Étapes pour exécuter

1. Compiler tous les fichiers Java :
   ```bash
   javac *.java
2. Lancer le programme :
   ```bash
   java Connect4UI
4. Suivre les instructions dans l'interface graphique.

## Stratégie de l'IA
**Heuristique des cases :**

Chaque case a un poids basé sur le nombre d'alignements possibles.
Les colonnes centrales sont privilégiées pour maximiser les chances de victoires diagonales.

**Recherche alpha-bêta :**

Exploration des coups possibles en profondeur pour anticiper les meilleurs choix.
Optimisation des performances grâce à un élagage efficace.
 
**Gestion des états de croyance :**

Les coups adverses possibles sont simulés pour réduire l'incertitude.

## Auteurs 
Projet réalisé par Mayy Miled, Rachelle Nasr, Paul Hoerter, Giuliano AlDarwish dans le cadre du cours d'intelligence artificielle à l'Université Paris Dauphine.

Note : Ce projet est une implémentation académique et peut être étendu pour inclure des fonctionnalités avancées, comme une analyse statistique détaillée des performances de l'IA.
