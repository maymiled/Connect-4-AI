import java.util.ArrayList;

import java.util.Iterator;

import java.util.List;

import java.util.Map.Entry;

import java.util.TreeMap;

import java.util.TreeSet;

import java.util.Comparator;

/**

* Class used to model the set of belief states already visited and to keep track of their values (in order to avoid visiting multiple times the same states)

*/

class ExploredSet{

TreeMap<BeliefState, Float> exploredSet;


/**

* construct an empty set

*/

public ExploredSet() {

this.exploredSet = new TreeMap<BeliefState, Float>();

}


/**

* Search if a given state belongs to the explored set and returns its values if that is the case

* @param state the state for which the search takes place

* @return the value of the state if it belongs to the set, and null otherwise

*/

public Float get(BeliefState state) {

Entry<BeliefState, Float> entry = this.exploredSet.ceilingEntry(state);

if(entry == null || state.compareTo(entry.getKey()) != 0) {

return null;

}

return entry.getValue() * state.probaSum() / entry.getKey().probaSum();

}


/**

* Put a belief state and its corresponding value into the set

* @param beliefState the belief state to be added

* @param value the

*/

public void put(BeliefState beliefState, float value) {

this.exploredSet.put(beliefState, value);

}

}



/**

* Class used to store all possible results of performing an action at a given belief state

*/

class Results implements Iterable<BeliefState>{

TreeMap<String, BeliefState> results;


public Results(){

this.results = new TreeMap<String, BeliefState>();

}


/**

* Return the belief state of the result that correspond to a given percept

* @param percept String that describe what is visible on the board for player 2

* @return belief state corresponding percept, or null if such a percept is not possible

*/

public BeliefState get(String percept) {

return this.results.get(percept);

}


public void put(String s, BeliefState state) {

this.results.put(s, state);

}


public Iterator<BeliefState> iterator(){

return this.results.values().iterator();

}

}



/**

* Class used to represent a belief state i.e., a set of possible states the agent may be in

*/

class BeliefState implements Comparable<BeliefState>, Iterable<GameState>{

private byte[] isVisible;


private TreeSet<GameState> beliefState;


private int played;


public BeliefState() {

this.beliefState = new TreeSet<GameState>();

this.isVisible = new byte[6];

for(int i = 0; i < 6; i++) {

this.isVisible[i] = Byte.MIN_VALUE;

}

this.played = 0;

}


public BeliefState(byte[] isVisible, int played) {

this();

for(int i = 0; i < 6; i++) {

this.isVisible[i] = isVisible[i];

}

this.played = played;

}


public void setStates(BeliefState beliefState) {

this.beliefState = beliefState.beliefState;

for(int i = 0; i < 6; i++) {

this.isVisible[i] = beliefState.isVisible[i];

}

this.played = beliefState.played;

}


public boolean contains(GameState state) {

return this.beliefState.contains(state);

}



/**

* returns the number of states in the belief state

* @return number of state

*/

public int size() {

return this.beliefState.size();

}


public void add(GameState state) {

if(!this.beliefState.contains(state)) {

this.beliefState.add(state);

}

else {

GameState copy = this.beliefState.floor(state);

copy.addProba(state.proba());

}

}


/**

* Compute the possible results from a given believe state, after the opponent perform an action. This function souhd be used only when this is the turn of the opponent.

* @return an objet of class result containing all possible result of an action performed by the opponent if this is the turn of the opponent, and null otherwise.

*/

public Results predict(){

if(this.turn()) {

Results tmstates = new Results();

for(GameState state: this.beliefState) {

RandomSelector rs = new RandomSelector();

ArrayList<Integer> listColumn = new ArrayList<Integer>();

ArrayList<Integer> listGameOver = new ArrayList<Integer>();

int minGameOver = Integer.MAX_VALUE;

for(int column = 0; column < 7; column++) {

if(!state.isFull(column)) {

GameState copy = state.copy();

copy.putPiece(column);

if(copy.isGameOver()) {

listColumn.clear();

listColumn.add(column);

rs = new RandomSelector();

rs.add(1);

break;

}

int nbrGameOver = 0;

for(int i = 0; i < 7; i++) {

if(!copy.isFull(i)) {

GameState copycopy = copy.copy();

copycopy.putPiece(i);

if(copycopy.isGameOver()) {

nbrGameOver++;

}

}

}

if(nbrGameOver == 0) {

rs.add(ProbabilisticOpponentAI.heuristicValue(state, column));

listColumn.add(column);

}

else {

if(minGameOver > nbrGameOver) {

minGameOver = nbrGameOver;

listGameOver.clear();

listGameOver.add(column);

}

else {

if(minGameOver == nbrGameOver) {

listGameOver.add(column);

}

}

}

}

}

int index = 0;

if(listColumn.isEmpty()) {

for(int column: listGameOver) {

listColumn.add(column);

rs.add(1);

}

}

for(int column: listColumn) {

GameState copy = state.copy();

if(!copy.isFull(column)) {

byte[] tab = new byte[6];

for(int i = 0; i < 6; i++) {

tab[i] = this.isVisible[i];

}

copy.putPiece(column);

if(copy.isGameOver()) {

for(int i = 0; i < 6; i++) {

for(int j = 0; j < 7; j++) {

BeliefState.setVisible(i, j, true, tab);

}

}

}

else {

boolean isVisible = copy.isGameOver() || copy.isFull(column);

BeliefState.setVisible(5, column, isVisible, tab);

for(int row = 4; row > -1; row--) {

isVisible = isVisible || copy.content(row, column) == 2;

BeliefState.setVisible(row, column, isVisible, tab);

}

}

String s = "";

char c = 0;

for(int i = 0; i < 6; i++) {

int val = tab[i] + 128;

s += ((char)(val % 128));

c += (val / 128) << i;

}

s += c;

copy.multProba(rs.probability(index++));

BeliefState bs = tmstates.get(s);

if(bs!= null) {

bs.add(copy);

}

else {

bs = new BeliefState(tab, this.played + 1);

bs.add(copy);

tmstates.put(s, bs);

}

}

}

}

return tmstates;

}

else {

return null;

}

}


/**

* Perform the action corresponding for the player to play a given column, and return the result of this action for each state of the belief state as a Results

* @param column index of the column played

* @return object of type Results representing all states resulting from playing the column if this is the turn of the player, and null otherwise

*/

public Results putPiecePlayer(int column){

if(!this.turn()) {

Results tmstates = new Results();

for(GameState state: this.beliefState) {

GameState copy = state.copy();

byte[] tab = new byte[6];

for(int i = 0; i < 6; i++) {

tab[i] = this.isVisible[i];

}

copy.putPiece(column);

if(copy.isGameOver()) {

for(int i = 0; i < 6; i++) {

for(int j = 0; j < 7; j++) {

BeliefState.setVisible(i, j, true, tab);

}

}

}

else {

boolean isVisible = copy.isFull(column);

BeliefState.setVisible(5, column, isVisible, tab);

for(int row = 4; row > -1; row--) {

isVisible = isVisible || copy.content(row, column) == 2;

BeliefState.setVisible(row, column, isVisible, tab);

}

}

String s = "";

char c = 0;

for(int i = 0; i < 6; i++) {

int val = tab[i] + 128;

s += ((char)(val % 128));

c += (val / 128) << i;

}

s += c;

BeliefState bs = tmstates.get(s);

if(bs!= null) {

bs.add(copy);

}

else {

bs = new BeliefState(tab, this.played + 1);

bs.add(copy);

tmstates.put(s, bs);

}

}

return tmstates;

}

else {

return null;

}


}


public static BeliefState filter(Results beliefStates, GameState state) {

byte tab[] = new byte[6];

for(int i = 0; i < 6; i++) {

tab[i] = Byte.MIN_VALUE;

}

for(int column = 0; column < 7; column++) {

boolean isVisible = state.isGameOver() || state.isFull(column);

BeliefState.setVisible(5, column, isVisible, tab);

for(int row = 4; row > -1; row--) {

isVisible = isVisible || (state.content(row, column) == 2);

BeliefState.setVisible(row, column, isVisible, tab);

}

}

String s = "";

char c = 0;

for(int i = 0; i < 6; i++) {

int val = tab[i] + 128;

s += ((char)(val % 128));

c += (val / 128) << i;

}

s += c;

BeliefState beliefState = beliefStates.get(s);

RandomSelector rs = new RandomSelector();

for(GameState st: beliefState.beliefState) {

rs.add(st.proba());

}

int i = 0;

for(GameState st: beliefState.beliefState) {

st.setProba(rs.probability(i++));

}

return beliefState;

}


/**

* Make a copy of the belief state containing the same states

* @return copy of the belief state

*/

public BeliefState copy() {

BeliefState bs = new BeliefState();

for(GameState state: this.beliefState) {

bs.add(state.copy());

}

for(int i = 0; i < 6; i++) {

bs.isVisible[i] = this.isVisible[i];

}

bs.played = this.played;

return bs;

}


public Iterator<GameState> iterator(){

return this.beliefState.iterator();

}


/**

* Return the list of the column where a piece can be played (columns which are not full)

* @return

*/

public ArrayList<Integer> getMoves(){

if(!this.isGameOver()) {

ArrayList<Integer> moves = new ArrayList<Integer>();

GameState state = this.beliefState.first();

for(int i = 0; i < 7; i++) {

if(!state.isFull(i))

moves.add(i);

}

return moves;

}

else {

return new ArrayList<Integer>();

}

}


/**

* Provide information about the next player to play

* @return true if the next to play is the opponent, and false otherwise

*/

public boolean turn() {

return this.beliefState.first().turn();

}


public boolean isVisible(int row, int column) {

int pos = row * 7 + column;

int index = pos / 8;

pos = pos % 8;

return ((this.isVisible[index] + 128) >> pos) % 2 == 1;

}


public void setVisible(int row, int column, boolean val) {

int pos = row * 7 + column;

int index = pos / 8;

pos = pos % 8;

int delta = ((val? 1: 0) - (this.isVisible(row, column)? 1: 0)) << pos;

this.isVisible[index] = (byte) (this.isVisible[index] + delta);

}


public static void setVisible(int row, int column, boolean val, byte[] tab) {

int pos = row * 7 + column;

int index = pos / 8;

pos = pos % 8;

int posValue = tab[index] + 128;

int delta = ((val? 1: 0) - ((posValue >> pos) % 2)) << pos;

tab[index] = (byte) (posValue + delta - 128);

}


/**

* Check if the game is over in all state of the belief state. Note that when the game is over, the board is revealed and the environment becomes observable.

* @return true if the game is over, and false otherwise

*/

public boolean isGameOver() {

for(GameState state: this.beliefState) {

if(!state.isGameOver()) {

return false;

}

}

return true;

}


/**

* Check if all the games in the belief state are full

* @return

*/

public boolean isFull() {

return this.beliefState.first().isFull();

}




public void restart() {

this.beliefState = new TreeSet<GameState>();

this.isVisible = new byte[6];

for(int i = 0; i < 6; i++) {

this.isVisible[i] = Byte.MIN_VALUE;

}

this.played = 0;

}


public String toString() {

String s = "BeliefState: size = " + this.beliefState.size() + " played = " + this.played + "\n";

for(int row = 5; row > -1; row--) {

for(int column = 0; column < 7; column++) {

s += this.isVisible(row, column)? "1": "0";

}

s += "\n";

}

for(GameState state:this.beliefState) {

s += state.toString() + "\n";

}

return s;

}


public int compareTo(BeliefState bs) {

if(this.played != bs.played)

return this.played > bs.played? 1: -1;

for(int i = 0; i < 6; i++) {

if(this.isVisible[i] != bs.isVisible[i])

return this.isVisible[i] > bs.isVisible[i]? 1: -1;

}

if(this.beliefState.size() != bs.beliefState.size()) {

return this.beliefState.size() > bs.beliefState.size()? 1: -1;

}

Iterator<GameState> iter = bs.beliefState.iterator();

for(GameState next: this.beliefState) {

GameState otherNext = iter.next();

int comp = next.compareTo(otherNext);

if(comp != 0)

return comp;

}

iter = bs.beliefState.iterator();

float sum1 = this.probaSum(), sum2 = bs.probaSum();

for(GameState next: this.beliefState) {

GameState otherNext = iter.next();

if(Math.abs(next.proba() * sum1 - otherNext.proba() * sum2) > 0.001) {

return next.proba() > otherNext.proba()? 1: -1;

}

}

return 0;

}


public float probaSum() {

float sum = 0;

for(GameState state: this.beliefState) {

sum += state.proba();

}

return sum;

}

}



public class AI{



public AI() {}



public static final float epsilon = 0.015f; //constante fixée à 1.5% trouvée à taton qui sert à developper ou évaluer un état ssi sa probabilité est supérieure à cette constante

//c'est avec ce pourcentage de 1.5% que nous avions les meilleurs résultats






/**

* Méthode qui calcule le nombre de cases vides dans un état

* @param state l'état de jeu à analyser.

* @return le nombre de cases vides.

*/

private static int casesvides(GameState state) {

int c = 0;

for (int row = 0; row < 6; row++) {

for (int col = 0; col < 7; col++) {

if (state.content(row, col) == 0) {

c++;

}

}

}

return c;

}




/**

* développe un nouvel état de croyance : un état de l'état de croyance est développé si sa proba est supérieure à epsilon = 1,5%

* @param game l'état de croyance actuel.

* @param move le coup à jouer.

* @return le nouvel état de croyance développé.

*/

private static BeliefState develop_BeliefState(BeliefState game, int move) {

BeliefState nextBeliefState = new BeliefState();

for (GameState state : game) {

if (state.proba() > epsilon) {

GameState copyState = state.copy();

if (!copyState.isFull(move)) {

copyState.putPiece(move);

nextBeliefState.add(copyState);

}

}

}

return nextBeliefState;

}






/**

* Méthode qui trouve le prochain coup optimal à jouer en fonction d'unétat de croyance

* @param game l'état de croyance du jeu

* @return l'indice de la colonne où jouer le prochain coup

*/

public static int findNextMove(BeliefState game) {

float ratiocasesremplies = 1 - ((float) casesvides(game.iterator().next())) / 42; // calcule le pourcentage de cases remplies

int maxDepth = (ratiocasesremplies < 0.1) ? 1:3; //s'il y a moins de 10% des cases remplies (donc en début de partie), profondeur de 1, sinon profondeur de 3

int bestMove = 3; //coup par défaut fixé à 3 (colonne centrale) car c'est la colonne sur laquelle on a le plus de chances de faire des puissance 4 en diagonale

float bestScore = Float.NEGATIVE_INFINITY;







ArrayList<Integer> validMoves = game.getMoves();// colonnes jouables

ArrayList<Integer> optimalOrder = new ArrayList<>();

optimalOrder.add(3);

optimalOrder.add(2);

optimalOrder.add(4);

optimalOrder.add(5);

optimalOrder.add(1);

optimalOrder.add(6);

optimalOrder.add(0);

validMoves.sort(Comparator.comparingInt(optimalOrder::indexOf)); //méthode de la classe Comparator qui nous permet de trier la listes des colonnes jouables selon nos préférences

//On suppose qu'il est d'abord préférable de jouer les colonnes centrales (qui maximisent le nb de puissance 4 en diagonale) puis celles sur les côtés ensuite








for (Integer move : validMoves) {

BeliefState nextBeliefState = develop_BeliefState(game, move);



float score = alpha_beta(nextBeliefState, 0, maxDepth, false,Float.NEGATIVE_INFINITY,Float.POSITIVE_INFINITY);



//actualisation du coup

if(score > bestScore) {

bestScore = score;

bestMove = move;

}

}



// s'assurer que le coup est jouable sinon selectionner le premier mouvement valide dans les coups triés optimalement

if (!validMoves.contains(bestMove)) {

//System.err.println("colonne remplie");

bestMove = validMoves.get(0);





}



return bestMove;

}









/**

* méthode récursive qui implémente l'algorithme alpha-bêta selon le pseudo-code du cours

* @param game l'état de croyance actuel

* @param depth la profondeur actuelle de l'exploration

* @param maxDepth la profondeur maximale de recherche

* @param player_max true si c'est le tour du joueur maximisant, false sinon

* @param alpha la valeur alpha utilisée pour l'élagage alpha-bêta

* @param beta la valeur beta utilisée pour l'élagage alpha-bêta

* @return l'évaluation de l'état de croyance

*/

private static float alpha_beta(BeliefState game, int depth, int maxDepth, boolean player_max, float alpha, float beta) {

if (depth == maxDepth || game.isGameOver() || game.getMoves().isEmpty()) {

return evaluateBeliefState(game);

}



if (player_max) {

float maxEval = Float.NEGATIVE_INFINITY;

for (Integer move : game.getMoves()) {

BeliefState nextBeliefState = develop_BeliefState(game, move);

float eval = alpha_beta(nextBeliefState, depth + 1, maxDepth, false, alpha, beta);

maxEval = Math.max(maxEval, eval);

if (maxEval >= beta) {

return maxEval;

}

alpha = Math.max(alpha, maxEval);

}

return maxEval;

} else {

float minEval = Float.POSITIVE_INFINITY;

for (Integer move : game.getMoves()) {

BeliefState nextBeliefState = develop_BeliefState(game, move);

float eval = alpha_beta(nextBeliefState, depth + 1, maxDepth, true, alpha, beta);

minEval = Math.min(minEval, eval);

if (minEval <= alpha) {

return minEval;

}

beta = Math.min(beta, minEval);

}

return minEval;

}

}







/**

*evaluation d'un état de croyance : on évalue tous les états dans l'état de croyance grâce à l'heuristique puis en les multipliant par leur proba et on les somme.

Ensuite, on évalue l'état de croyance en normalisant cette somme par la somme des probabilités de ses états

* @param beliefState l'état de croyance à évaluer

* @return l'évaluation heuristique de l'état de croyance

*/


private static float evaluateBeliefState(BeliefState beliefState) {

float evaluation = 0;



for (GameState state : beliefState) {

if (state.proba()>epsilon) {

float stateValue = evaluateGameState(state);

evaluation += stateValue * state.proba();

}

}



evaluation /= beliefState.probaSum();





return evaluation;

}





// matrice qui nous sert pour le calcul de l'heuristique : on attribue à chaque case une valeur qui est le nombre de puissance 4 qu'on peut réaliser dans toutes les directions avec un pion sur cette case

// tirée d'un poly de prépa de théorie des jeux : https://pc-etoile.schola.fr/wp-content/uploads/pdf-cours-info/04.jeux.pdf

private static final int[][] MATRICE_HEURISTIQUE = {

{3, 4, 5, 7, 5, 4, 3},

{4, 6, 8, 10, 8, 6, 4},

{5, 8, 11, 13, 11, 8, 5},

{5, 8, 11, 13, 11, 8, 5},

{4, 6, 8, 10, 8, 6, 4},

{3, 4, 5, 7, 5, 4, 3}

};





/**

* Évalue un état de jeu en attribuant des scores basés sur la matrice d'heuristique et les alignements potentiels

* @param state l'état de jeu à évaluer

* @return le score de l'heuristique de l'état de jeu

*/

private static float evaluateGameState(GameState state) {

if (state.isGameOver()) {

return state.turn() ? -100 : 100; // si fin de partie et c'est a nous de jouer <=> défaite alors on renvoie -100 sinon si victoire on renvoie 100

}



float score = 0;



// on parcourt toutes les cases du plateau

for (int row = 0; row < 6; row++) {

for (int col = 0; col < 7; col++) {

int pion = state.content(row, col);

if (pion == 2) { // nos pions

score += MATRICE_HEURISTIQUE[row][col]; // pour tous nos pions, on ajoute au score les valeurs de la matrice sur lesquels ils sont placés

score += scorePosition(row, col, state, pion); // on ajoute aussi au score la valeur de l'évaluation des alignements (cf fonction d'après)

} else if (pion == 1) { // pion de l'adversaire

score -= MATRICE_HEURISTIQUE[row][col]; //sinon, on soustrait au score les valeurs de la matrice sur lesquels sont placés les pions adverses

score -= scorePosition(row, col, state, pion); // on soustrait la valeur de l'évaluation des alignements adverses

}

}

}



return score;

}








/**

* renvoie un score qui est la somme des évaluations des alignements de pion(s) selon les 4 directions d'un pion (verticale,horizontale,diagonale droite, diagonale gauche)

* @param row la ligne de la case

* @param col la colonne de la case

* @param state l'état de jeu contenant la case

* @param player le joueur (1 ou 2) pour lequel l'évaluation est effectuée

* @return le score heuristique de la position

*/

private static int scorePosition(int row, int col, GameState state, int player) {

int score = 0;



score += evaluateDirection(row, col, 0, 1, state, player);

score += evaluateDirection(row, col, 1, 0, state, player);

score += evaluateDirection(row, col, 1, 1, state, player);

score += evaluateDirection(row, col, 1, -1, state, player);



return score;

}







/**

*attribue une valeur en fonction des alignements encore possible : si 3 pions et 1 case vide alignés : +20 , 2 pions 2 cases vides : +5 , 1 pion 3 cases vides : +1 .

Les valeurs attribuées ont été choisies toujours en tatonnant, de sorte qu'elles soient assez proches de celles attribuées par les valeurs de la matrice MATRICE_HEURISTIQUE

* @param row la ligne de départ

* @param col la colonne de départ

* @param direction_row la direction en ligne (1, 0, ou -1)

* @param direction_col la direction en colonne (1, 0, ou -1)

* @param state l'état de jeu

* @param player le joueur (1 ou 2) pour lequel l'évaluation est effectuée

* @return le score de l'alignement

*/


private static int evaluateDirection(int row, int col, int direction_row, int direction_col, GameState state, int player) {

int nb_pions = 0;

int empty = 0;

int score = 0;



// parcourt jusqu'à 4 cases dans la direction spécifiée

for (int i = 0; i < 4; i++) {

int row_explored = row + i * direction_row;

int col_explored = col + i * direction_col;



if (row_explored < 0 || row_explored >= 6 || col_explored < 0 || col_explored >= 7) {

break;

} //limites du plateau



int pion = state.content(row_explored, col_explored);

if (pion == player) {

nb_pions++;

} else if (pion == 0) {

empty++;

} else {

break;}

}



if (nb_pions == 3 && empty == 1) { //alignement de 3 pions

score += 20;

} else if (nb_pions == 2 && empty == 2) { // alignement de 2 pions

score += 5;

} else if (nb_pions == 1 && empty == 3) { //alignement d'un pion

score += 1;

}



return score;

}





}