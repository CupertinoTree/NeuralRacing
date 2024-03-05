package neuralPckg;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * Cette classe permet de représenter les voitures qui évoluent dans le jeu, considérées
 * comme des individus d'une population.
 * Elle permet à la voiture de se déplacer, de se repérer dans l'espace, et contient
 * toutes les données nécessaires pour être peinte sur l'interface graphique (position,
 * direction, et sens).
 * Elle prend en charge un <code>réseau de neurone</code>, afin de prendre les choix relatifs au style
 * de conduite à adopter. Enfin, c'est également cette classe qui permet de quantifier le
 * score obtenu par la voiture.
 * 
 * @author Ronan
 * 
 * @see NeuralModel.NeuralNetwork
 * @see BiologicalModel.Individual
 *
 */
public class Car {

	/**Le réseau de neurones de la voiture.*/
	NeuralModel.NeuralNetwork network;
	
	private double xLocation,
		   		   yLocation,
		   		   direction,
		   		   speed = 1;//,
		   					 //maxSpeed,
		   					 //minSpeed;
	
	boolean motorOn = true,
			evolve  = true;
	
	/**Le compte des lignes de récompenses croisées par la voiture dans le tour actuel.*/
	List<Line2D.Double> rewardLinesCrossed = new ArrayList<Line2D.Double>();
	
	int laps = 0,
		type = 0;
	
	/**L'opacité de la voiture, qui diminue progressivement après l'extinction du moteur.*/
	Float alphaComposite = 1f;
	
	/**
	 * Ce constructeur permet d'obtenir une voiture en lui donnant ses coordonnées
	 * et son orientation initiales, son <code>réseau de neurones</code>, ainsi qu'un
	 * booléen décrivant à quel onglet elle appartient. 
	 * 
	 * @param driver
	 * Le réseau de neurone du conducteur.
	 * 
	 * @param x
	 * La position de la voiture en abcisses.
	 * @param y
	 * La position de la voiture en ordonnées.
	 * @param speed
	 * La vitesse de la voiture au démarrage
	 * @param theta
	 * L'orientation de la voiture en radians.
	 * @param evolve
	 * Si ce booléen est vrai, la <code>CarsView</code> appartient à la <code>EvolutionView</code>.
	 * Sinon, elle appartient à la <code>ExperimentsView</code>.
	 */
	public Car(NeuralModel.NeuralNetwork driver, double x, double y, /*double maxSpeed, double minSpeed*/ double speed, double theta, boolean evolve) {
		this.network = driver;
		this.xLocation = x;
		this.yLocation = y;
		this.direction = theta;
		this.evolve    = evolve;
		//this.maxSpeed  = maxSpeed;
		//this.minSpeed  = minSpeed;
		
		this.speed     = speed;
	}
	
	/**
	 * Cette méthode permet à un conducteur humain de contrôler sa voiture.
	 * Le choix 0 revient à tourner à gauche, le choix 1 à droite.
	 * Le choix 2 permet d'accélérer, le 3 de freiner.
	 * Il est important de noter que cette méthode est bien distincte de la fonction
	 * <code>drive</code> : en effet, celle-ci permet seulement de modifier vitesse et
	 * direction, et non d'avancer.
	 * 
	 * @param choice
	 * Le choix de l'utilisateur.
	 * 
	 * @see #drive()
	 * @see Car
	 */
	public void humanDrive(int choice, double minSpeed, double maxSpeed) {
		
		if (choice == 0) {
			//L'angle diminue car dans le repère des JPanel le cercle trigonométrique est inversé.
			direction -= Math.PI/20;
			if (direction < 0) {
				direction = direction + 2 * Math.PI;
			}
		} else if (choice == 1) {
			//L'angle augmente car dans le repère des JPanel le cercle trigonométrique est inversé.
			direction += Math.PI/20;
			if (direction > 2*Math.PI) {
				direction = direction - 2 * Math.PI;
			}
		} else if (choice == 2) {
			if (speed < maxSpeed) {
				speed = Math.min(maxSpeed, speed + 0.2);
			}
		} else if (choice == 3) {
			if (speed > minSpeed) {
				speed = Math.max(minSpeed, speed - 0.2);
			}
		}
		
	}
	
	/**
	 * Ce méthode prend en charge le déplacement de la voiture.
	 * Son fonctionement est le suivant :
	 * <p>
	 * <strong>-</strong> Si la voiture touche une barrière du circuit, on coupe son moteur : elle a perdu.
	 * <p>
	 * <strong>-</strong> Si la voiture touche la ligne d'arrivée dans le mauvais sens, elle a perdu.
	 * <p>
	 * <strong>-</strong>Si la voiture touche la ligne d'arrivée dans le bon sens, on met à jour son score.
	 * La variable <code>laps</code> est incrémentée : la voiture a achevé un tour complet de plus. On vide la liste des lignes de récompenses
	 * qu'elle a croisées.
	 * <p>
	 * <strong>-</strong> Si la voiture croise une ligne de récompense, on l'ajoute à la liste.
	 * <p>
	 * <strong>-</strong> Si la voiture possède un <code>Réseau de neurones</code> et qu'elle n'est donc pas conduite par un humain,
	 * on utilise sa méthode <code>predict</code> en lui donnant comme entrée ce que voient ses caméras, et sa vitesse. On obtient le choix
	 * du réseau, qu'on applique : on modifie l'orientation ou la vitesse de la voiture.
	 * <p>
	 * <strong>-</strong> Enfin, on fait avancer la voiture d'une distance égale à sa vitesse, dans la direction donnée par son angle.
	 * 
	 * @see Car
	 * @see NeuralModel.NeuralNetwork
	 */
	public void drive(RaceGrid raceGrid, double maxSpeed, double minSpeed) {
		
		if (!motorOn) { return; }
		
		//RaceGrid raceGrid = evolve ? NeuralRacing.evolutionView.raceView.raceGrid : NeuralRacing.experimentsView.raceView.raceGrid;
		
		//Le cercle trigonométrique étant inversé, on effectue nos calculs sur un angle ajusté.
		double adjustedDir = 2*Math.PI-this.direction;
		
		//Ce booléen permet de savoir si la voiture fait face à la ligne de départ.
		boolean isFacingStartLine = false;
		
		if (raceGrid.originalDirection == 0) {
			isFacingStartLine = adjustedDir > Math.PI/2 && adjustedDir < 3*Math.PI/2;
		} else if (raceGrid.originalDirection == Math.PI) {
			isFacingStartLine = adjustedDir < Math.PI/2 || adjustedDir > 3*Math.PI/2;
		} else {
			isFacingStartLine = Math.abs(adjustedDir-raceGrid.originalDirection) < Math.PI/2;
		}
		
		//On récupère les lignes qui constituent les bords de la voiture.
		Line2D.Double [] carLines = getLines();
		
		for (Line2D.Double carLine: carLines) {
			
			//Si une des lignes de la voiture croise une des lignes des parois
			//du terrain, la voiture est hors-jeu.
			for (Line2D.Double roadLine: raceGrid.hitbox) {
				if (getLineIntersection(carLine, roadLine) != null) {
					motorOn = false;
					speed = 0;
					return;
				}
			}
			
			if (isFacingStartLine && getLineIntersection(carLine, raceGrid.startLine) != null)  {
				//Si la voiture fait face à la ligne de départ et qu'elle la touche, elle devient hors-jeu.
				motorOn = false;
				speed = 0;
				return;
			} else if (getLineIntersection(carLine, raceGrid.startLine) != null && rewardLinesCrossed.size() == raceGrid.rewardLinesCount) {
				//Si la voiture croise la ligne de départ dans le bon sens et qu'elle a touché toutes les lignes de
				//récompense du circuit durant son tour, alors son nombre de tour est incrémenté de 1 et on réinitialise
				//la liste des lignes de récompenses croisées par la voiture.
				rewardLinesCrossed = new ArrayList<Line2D.Double>();
				laps++;
			}
			
			for (Line2D.Double rewardLine: raceGrid.rewardLines) {
				if (!rewardLinesCrossed.contains(rewardLine) && getLineIntersection(carLine, rewardLine) != null) {
					//Si la voiture n'a pas encore touché cette ligne pendant son tour et qu'elle la touche
					//actuellement, alors on l'ajoute à la liste des lignes rencontrées.
					rewardLinesCrossed.add(rewardLine);
				}
			}
		}
		
		//Si la voiture a un réseau de neurones, on lui demande son choix de conduite.
		int choice = -1;
		try {
			choice = network.predict(getBrainInputs(maxSpeed));
		} catch (NullPointerException exc) {}
		
		//On applique une logique analogue à celle utilisée dans la fonction hummanDrive.
		//Il est à noter que les changements de valeurs sont nettement plus petits : un réseau
		//fera des choix plus souvent et réagira nettement plus rapidement que l'humain, donc il
		//est normal de permettre plus de finesse à l'intelligence artificielle.
		if (choice == 0) {
			direction -= Math.PI/100;
			if (direction < 0) {
				direction = direction+2*Math.PI;
			}
		} else if (choice == 1) {
			direction += Math.PI/100;
			if (direction > 2*Math.PI) {
				direction = direction-2*Math.PI;
			}
		} else if (choice == 2) {
			if (speed < maxSpeed) {
				speed = Math.min(maxSpeed, speed + 0.05);
			}
		} else if (choice == 3) {
			if (speed > minSpeed) {
				speed = Math.max(minSpeed, speed - 0.05);
			}
		}
		
		//On applique le vecteur vitesse que porte la voiture sur elle-même.
		//L'axe des y étant inversé, on inverse le signe de la deuxième expression.
		this.xLocation += this.speed * Math.cos(adjustedDir);
		this.yLocation -= this.speed * Math.sin(adjustedDir);
	}
	
	/**
	 * Cette méthode permet de réintialiser la transparence et la position du véhicule,
	 * ainsi que ses données relatives au score qu'elle a obtenu.
	 * 
	 * @param speed
	 * La vitesse de la voiture au démarrage
	 * 
	 * @param x
	 * La position de départ de la voiture en x
	 * 
	 * @param y
	 * La position de départ de la voiture en y
	 * 
	 * @param direction
	 * La direction de la voiture au départ
	 */
	public void carWash(double speed, double x, double y, double direction) {
		rewardLinesCrossed = new ArrayList<Line2D.Double>();
		laps = 0;
		alphaComposite = 1f;
		
		this.speed = speed;
		this.xLocation = x;
		this.yLocation = y;
		this.direction = direction;
	}
	
	/**
	 * Cette méthode permet d'obtenir le point d'intersection entre deux lignes.
	 * 
	 * @param lineOne
	 * La première ligne.
	 * @param lineTwo
	 * La deuxième ligne.
	 * @return
	 * Le point d'intersection.
	 * 
	 * @see Car
	 */
	private Point2D.Double getLineIntersection(Line2D.Double lineOne, Line2D.Double lineTwo) {
		
	    double  deltaX1 = lineOne.x2 - lineOne.x1,
	    		deltaY1 = lineOne.y2 - lineOne.y1,
	    		deltaX2 = lineTwo.x2 - lineTwo.x1,
	    		deltaY2 = lineTwo.y2 - lineTwo.y1,
	    		deltaX  = lineOne.x1 - lineTwo.x1,
	    		deltaY  = lineOne.y1 - lineTwo.y1,
	    		divider = -deltaX2 * deltaY1 + deltaX1 * deltaY2,
	    		s       = (-deltaY1 * deltaX + deltaX1 * deltaY) / divider,
	    		t       = ( deltaX2 * deltaY - deltaY2 * deltaX) / divider;
	    
	    if (s >= 0 && s <= 1 && t >= 0 && t <= 1) { return new Point2D.Double(lineOne.x1 + t * deltaX1, lineOne.y1 + t * deltaY1); }
	    
	    return null;
	}
	
	/**
	 * Cette méthode permet d'obtenir les 4 points représentant les angles du véhicule.
	 * 
	 * @return
	 * Le tableau contenant les points.
	 * 
	 * @see Car
	 */
	public Point2D.Double [] getCorners() {
		Point2D.Double [] carCorners = new Point2D.Double [4];
		
		double sin           = Math.sin(direction),
		       cos           = Math.cos(direction),
		   	   topMiddleX    = xLocation + sin * CarsView.halfCarHeight,
		       topMiddleY    = yLocation - cos * CarsView.halfCarHeight,
	           bottomMiddleX = xLocation - sin * CarsView.halfCarHeight,
	           bottomMiddleY = yLocation + cos * CarsView.halfCarHeight;
			    
		Point2D.Double northWest = new Point2D.Double(topMiddleX    + (cos * CarsView.halfCarWidth), topMiddleY    + (sin * CarsView.halfCarWidth)),
					   northEast = new Point2D.Double(bottomMiddleX + (cos * CarsView.halfCarWidth), bottomMiddleY + (sin * CarsView.halfCarWidth)),
					   southWest = new Point2D.Double(topMiddleX    - (cos * CarsView.halfCarWidth), topMiddleY    - (sin * CarsView.halfCarWidth)),
	                   southEast = new Point2D.Double(bottomMiddleX - (cos * CarsView.halfCarWidth), bottomMiddleY - (sin * CarsView.halfCarWidth));
		    
		carCorners[0] = northWest;
		carCorners[1] = northEast;
		carCorners[2] = southEast;
		carCorners[3] = southWest;
		
		return carCorners;
	}
	
	/**
	 * Cette méthode permet d'obtenir les 4 lignes représentant le corps physique du véhicule.
	 * 
	 * @return
	 * Le tableau contenant les lignes.
	 * 
	 * @see Car
	 */
	public Line2D.Double [] getLines() {
		
		Line2D.Double [] carLines = new Line2D.Double [4];
		
		Point2D.Double [] carCorners = getCorners();
	    
		carLines [0] = new Line2D.Double(carCorners[1].x, carCorners[1].y, carCorners[0].x, carCorners[0].y);
		carLines [1] = new Line2D.Double(carCorners[1].x, carCorners[1].y, carCorners[2].x, carCorners[2].y);
		carLines [2] = new Line2D.Double(carCorners[3].x, carCorners[3].y, carCorners[0].x, carCorners[0].y);
		carLines [3] = new Line2D.Double(carCorners[3].x, carCorners[3].y, carCorners[2].x, carCorners[2].y);
		
		return carLines;
	}
	
	/**
	 * Cette méthode permet d'obtenir les lignes de vision de la voiture.
	 * Ces lignes permettent au réseau de neurones de percevoir son environnement.
	 * Elles sont au nombre de 5 : une en face, une de chaque côté, et une partant de chaque coin avant
	 * et allant dans le sens des diagonales du rectangle représentant le corps physique de la voiture.
	 * Par défaut, une ligne de vision a une longueur de deux cases, soit 140. Mais sa taille est tronquée
	 * si elle coupe une des lignes du circuit, à l'endroit de l'intersection.
	 * 
	 * @return
	 * Le tableau contenant les lignes de vision de la voiture.
	 * 
	 * @see Car
	 */
	public Line2D.Double [] getVisionLines() {
		
		//On récupère la grille du terrain.
		RaceGrid raceGrid = evolve ? NeuralRacing.evolutionView.raceView.raceGrid : NeuralRacing.experimentsView.raceView.raceGrid;
		
		double fourthPi = Math.PI/4, halfPi = Math.PI/2;
		
		Line2D.Double [] lines = new Line2D.Double [5];
		Point2D.Double [] carCorners = getCorners();
		
		//On récupère les points de départ des cinq lignes.
		Point2D.Double northWest   = carCorners[0],
					   northEast   = carCorners[1],
					   centerFront = new Point2D.Double((northWest.getX() + northEast.getX())/2,     (northWest.getY() + northEast.getY())/2),
				       centerRight = new Point2D.Double((northEast.getX() + carCorners[2].getX())/2, (northEast.getY() + carCorners[2].getY())/2),
				       centerLeft  = new Point2D.Double((northWest.getX() + carCorners[3].getX())/2, (northWest.getY() + carCorners[3].getY())/2);   
		
		//On créé les lignes de vision avec leur longueur par défaut.
		Line2D.Double rightSideLine   = new Line2D.Double(centerRight.getX(), centerRight.getY(), centerRight.getX() + 140*Math.cos(direction + halfPi),   centerRight.getY() + 140*Math.sin(direction + halfPi)),
					  rightCornerLine = new Line2D.Double(northEast.getX(),   northEast.getY(),   northEast.getX()   + 140*Math.cos(direction + fourthPi), northEast.getY()   + 140*Math.sin(direction + fourthPi)),
					  frontLine       = new Line2D.Double(centerFront.getX(), centerFront.getY(), centerFront.getX() + 140*Math.cos(direction),            centerFront.getY() + 140*Math.sin(direction)),
					  leftCornerLine  = new Line2D.Double(northWest.getX(),   northWest.getY(),   northWest.getX()   + 140*Math.cos(direction - fourthPi), northWest.getY()   + 140*Math.sin(direction - fourthPi)),
					  leftSideLine    = new Line2D.Double(centerLeft.getX(),  centerLeft.getY(),  centerLeft.getX()  + 140*Math.cos(direction - halfPi),   centerLeft.getY()  + 140*Math.sin(direction - halfPi));
		
		lines[0] = leftSideLine;
		lines[1] = leftCornerLine;
		lines[2] = frontLine;
		lines[3] = rightCornerLine;
		lines[4] = rightSideLine;
		
		for (int i = 0; i < lines.length; i++) {
			for (Line2D.Double roadLine: raceGrid.hitbox) {
				Point2D.Double intersection = getLineIntersection(lines[i], roadLine);
				//Si la ligne de vision coupe une des lignes du circuit,
				//on la tronque au niveau du point d'intersection.
				if (intersection != null) { lines[i] = new Line2D.Double(lines[i].getP1(), intersection); }
			}
		}
		
		return lines;
	}
	
	/**
	 * Cette méthode permet d'obtenir le tableau de données que le réseau de la voiture
	 * utilisera pour prendre ses décisions. Il contient les données des caméras, et la
	 * vitesse du véhicule. Toutes ces données sont exprimées par des réels entre 0 et 1.
	 * Les valeurs des caméras se lisent de la façon suivante : le 0 équivaut à l'absence d'obstacle,
	 * le 1 à une collision immédiate.
	 * 
	 * @param maxSpeed
	 * La vitesse maximale de la voiture
	 * 
	 * @return
	 * Le tableau contenant les données que le réseau de la voiture utilisera pour prendre ses décisions.
	 * 
	 * @see Car
	 * @see NeuralModel.NeuralNetwork
	 */
	public float [] getBrainInputs(double maxSpeed) {
		
		Line2D.Double [] lines = this.getVisionLines();
		float [] inputs = new float [lines.length+1];
		
		for (int i = 0; i < lines.length; i++) {
			inputs[i] = (float) (1 - lines[i].getP1().distance(lines[i].getP2())/140);
		}
		
		inputs[5] = (float) speed/((float) EvolutionView.ultimateMaxSpeed/10);
		
		return inputs;
		
	}
	
	/**La position de la voiture en X*/
	public double getXLocation() { return this.xLocation; }
	
	/**La position de la voiture en Y*/
	public double getYLocation() { return this.yLocation; }
	
	/**La direction de la voiture*/
	public double getDirection() { return this.direction; }
	
}
