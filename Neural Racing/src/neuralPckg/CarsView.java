package neuralPckg;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

/**
 * Cette sous-classe de <code>UIPanel</code> comprend tout le matériel nécessaire pour gérer l'affichage
 * des voitures. Ce type de vue a pour but d'être visuellement transparent, afin de laisser voir la <code>RaceGrid</code>
 * se trouvant derrière. Seuls les véhicules sont ainsi visibles.
 * 
 * @author Ronan
 *
 * @see NeuralRacing.UIPanel
 * @see Car
 * @see RaceGrid
 */
public class CarsView extends NeuralRacing.UIPanel {

	private static final long serialVersionUID = 1624763485381842411L;
	
	static int halfCarWidth,
		       halfCarHeight;
	
	/**Le point de départ du circuit actuel.*/
	Point2D.Double origin;
	
	/**L'orientation de départ des voitures sur le circuit actuel.*/
	double originalDirection;
	
	/**L'ensemble des voitures.*/
	Car [] cars;
	
	/**Le nombre de voiture.*/
	int populationSize;
	
	double minimumSpeed,
		   maximumSpeed;
	
	/**
	 * Si ce booléen est vrai, la <code>CarsView</code> appartient à la <code>EvolutionView</code>.
	 * Sinon, elle appartient à la <code>ExperimentsView</code>.
	 */
	boolean evolve;
	
	/**
	 * Si une des voitures est contrôlée par un humain, cette variable contient
	 * l'index du véhicule dans la population. Sinon, elle vaut -1.
	 */
	int humanDriverIndex    = -1;
	
	/**
	 * Dans le cas de la <code>ExperimentsView</code>, il peut-être nécessaire d'afficher
	 * une fausse voiture au point de départ du circuit, quand des voitures ont été ajoutées
	 * mais que la partie n'a pas encore été lancée. Quand cette variable vaut -1, aucun
	 * véhicule n'est affiché. Sinon, cette variable vaut l'index de l'image à afficher.
	 */
	int displayFakeCarStyle = -1;
	
	/**
	 * Cette sous-classe de <code>UIPanel</code> comprend tout le matériel nécessaire pour gérer l'affichage
	 * des voitures.
	 * 
	 * @param size
	 * La taille de la vue.
	 * 
	 * @param raceGrid
	 * La grille du circuit sur lequel roulent les voitures.
	 * 
	 * @param populationCount
	 * Le nombre de voiture.
	 * 
	 * @param evolve
	 * Si ce booléen est vrai, la <code>CarsView</code> appartient à la <code>EvolutionView</code>.
	 * Sinon, elle appartient à la <code>ExperimentsView</code>.
	 * 
	 * @see NeuralRacing.UIPanel
	 * @see Car
	 */
	public CarsView(Dimension size, RaceGrid raceGrid, int populationCount, double maximumSpeed, double minimumSpeed, boolean evolve) {
		
		this.setSize(size);
		//La vue est transparente, pour laisser apparaître le circuit en arrière plan.
		this.setOpaque(false);
		this.originalDirection = raceGrid.originalDirection;
		this.origin = raceGrid.start;
		this.evolve = evolve;
		
		halfCarHeight = NeuralRacing.redCar.getHeight()/2;
		halfCarWidth  = NeuralRacing.redCar.getWidth()/2;
		
		populationSize = populationCount;
		cars           = new Car[populationCount];
		
		this.maximumSpeed = maximumSpeed;
		this.minimumSpeed = minimumSpeed;
		
		for (int index = 0; index < populationCount; index++) { cars[index] = new Car(null, origin.getX(), origin.getY(), minimumSpeed, originalDirection, evolve); }
		
	}
	
	/**
	 * Cette méthode permet de créer une groupe de voitures d'une
	 * taille donnée, en leur assignant leurs vitesses maximale et minimale.
	 * 
	 * 
	 * @param size
	 * La taille du groupe de voitures.
	 * @param min
	 * La vitesse minimale des voitures.
	 * @param max
	 * La vitesse maximale des voitures.
	 */
	public void createCarsGroup(int size) {
		
		populationSize = size;
		
		//Les "vraies" voitures étant apparues, plus besoin d'en afficher une fausse.
		displayFakeCarStyle = -1;
		
		cars = new Car[size];
		
		for (int i = 0; i < size; i++) {
			cars[i] = new Car(null, origin.getX(), origin.getY(), minimumSpeed, originalDirection, evolve);
			//cars[i].speed    = mean;
			//cars[i].minSpeed = min;
			//cars[i].maxSpeed = max;
		}
	}
	
	/**
	 * Cette méthode permet d'assigner les réseaux de neurones de toute la population
	 * à leur voiture respective, en dehors du processus d'évolution.
	 * 
	 * @param population
	 * La population à utiliser.
	 */
	public void setExperimentalPopulation(BiologicalModel.Population population) {
		
		//Les "vraies" voitures étant apparues, plus besoin d'en afficher une fausse.
		displayFakeCarStyle = -1;
		
		for (int i = 0; i < populationSize; i ++) { cars[i] = new Car(population.individuals[i].toNeuralNet(), origin.getX(), origin.getY(), minimumSpeed, originalDirection, evolve); }
	}
	
	/**
	 * Cette méthode permet de replacer toutes les voitures
	 * à la position et à l'orientation d'origine, et de réinitialiser
	 * leur données relatives à la progression dans la course.
	 */
	public void cleanCarsGroup() {
		for (int i = 0; i < populationSize; i++) {
			//cars[i].xLocation = origin.getX();
			//cars[i].yLocation = origin.getY();
			//cars[i].direction = originalDirection;
			
			cars[i].carWash(minimumSpeed, origin.getX(), origin.getY(), originalDirection);
		}
	}
	
	/**
	 * Cette méthode permet d'assigner les nouveaux réseaux de neurones aux voitures
	 * à chaque nouvelle génération.
	 * 
	 *  @param population
	 *  La nouvelle population après reproduction
	 */
	public void preparePopulation(BiologicalModel.Population population) {
		for (int i = 0; i < population.getSize(); i++) {
			this.cars[i].network = population.individuals[i].toNeuralNet();
			this.cars[i].type    = population.individuals[i].type;
			this.cars[i].motorOn = true;
		}
	}
	
	@Override public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		
		if (displayFakeCarStyle != -1) {
			AffineTransform transform = AffineTransform.getTranslateInstance(origin.getX()-halfCarWidth, origin.getY()-halfCarHeight);
			//La rotation se fait autour du centre de la voiture.
			transform.rotate(originalDirection, halfCarWidth, halfCarHeight);
			
			g2d.drawImage(NeuralRacing.cars[displayFakeCarStyle], transform, null);
			return;
		}
		
		for (int i = 0; i < populationSize; i++) {
			
			Car currentCar = cars[i];
			if (currentCar == null) { continue; }
			drawCar(currentCar, g2d);
			
		}
		
	}
	
	/**
	 * Cette méthode permet de peindre une voiture sur la <code>CarsView</code>.
	 * 
	 * @param currentCar
	 * La voiture à peindre.
	 * @param g2d
	 * Le <code>Graphics2D</code> de la vue, sur lequel on peint la voiture.
	 * 
	 * @see Graphics2D
	 */
	private void drawCar(Car currentCar, Graphics2D g2d) {
		
		if (NeuralRacing.UserDefaults.fadingEnabled) { 
			if (!currentCar.motorOn && currentCar.alphaComposite == 0f) { return; }
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, currentCar.alphaComposite));
		}
		
		int type = currentCar.type;
		AffineTransform transform = AffineTransform.getTranslateInstance(currentCar.getXLocation()-halfCarWidth, currentCar.getYLocation()-halfCarHeight);
		transform.rotate(currentCar.getDirection(), halfCarWidth, halfCarHeight);
		
		g2d.drawImage(NeuralRacing.cars[type], transform, null);
		
		if (NeuralRacing.UserDefaults.shouldDrawLines) {
			
			g2d.setColor(Color.red);
			
			for (Line2D.Double line: currentCar.getLines()) {
				g2d.drawLine((int) line.x1, (int) line.y1, (int) line.x2, (int) line.y2); 
			}
			
			if (currentCar.motorOn) {
				for (Line2D.Double line: currentCar.getVisionLines()) {
					int color = (int) (255*(line.getP1().distance(line.getP2())/140));
					g2d.setColor(new Color(color, color, color));
					g2d.drawLine((int) line.x1, (int) line.y1, (int) line.x2, (int) line.y2);
				}
			}
		}
	}
	
	@Override public void keyActionDetected(String keyPressed) {
		
		if (humanDriverIndex == -1 || !NeuralRacing.experimentsView.isPlaying) { return; }
		
		switch (keyPressed) {
			case "Left" : cars[humanDriverIndex].humanDrive(0, minimumSpeed, maximumSpeed); break;
			case "Right": cars[humanDriverIndex].humanDrive(1, minimumSpeed, maximumSpeed); break;
			case "Up"   : cars[humanDriverIndex].humanDrive(2, minimumSpeed, maximumSpeed); break;
			case "Down" : cars[humanDriverIndex].humanDrive(3, minimumSpeed, maximumSpeed); break;
			default     :                                      							    break;
		}
	}

}
