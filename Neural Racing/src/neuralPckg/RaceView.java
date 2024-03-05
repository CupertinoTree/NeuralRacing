package neuralPckg;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;

/**
 * Cette sous-classe de <code>UIPanel</code> comprend tout le matériel nécessaire pour gérer l'affichage
 * des circuits. Elle s'appuie sur une <code>RaceGrid</code> contenant la représentation du circuit à aficher.
 * 
 * @author Ronan
 * 
 * @see NeuralRacing.UIPanel
 * @see RaceGrid
 *
 */
public class RaceView extends NeuralRacing.UIPanel {
	
	private static final long serialVersionUID = 481252052061132792L;
	
	static int rowLength    = 8,
			   columnLength = 10,
			   squareSize   = 70;
	
	/**
	 * Ce booléen permet de déterminer si le circuit représenté peut servir
	 * à des voitures (quand il vaut vrai), ou si la vue sert plutôt
	 * à la création de circuits (quand il vaut faux).
	 */
	boolean isUsable = true;
	
	/**La grille de jeu à afficher.*/
	RaceGrid raceGrid;
	
	/**
	 * Cette sous-classe de <code>UIPanel</code> comprend tout le matériel nécessaire pour gérer l'affichage
	 * des circuits. Elle s'appuie sur une <code>RaceGrid</code> contenant la représentation du circuit à aficher.
	 * 
	 * @param size
	 * La taille de la vue.
	 * 
	 * @param raceGrid
	 * La grille de jeu à afficher.
	 * 
	 * @see NeuralRacing.UIPanel
	 * @see RaceGrid
	 */
	public RaceView(Dimension size, RaceGrid raceGrid) {
		this(size, raceGrid, true);
	}
	
	/**
	 * Cette sous-classe de <code>UIPanel</code> comprend tout le matériel nécessaire pour gérer l'affichage
	 * des circuits. Elle s'appuie sur une <code>RaceGrid</code> contenant la représentation du circuit à aficher.
	 * 
	 * @param size
	 * La taille de la vue.
	 * 
	 * @param raceGrid
	 * La grille de jeu à afficher.
	 * 
	 * @param isUsable
	 * Ce booléen permet de déterminer si la grille représentée peut servir
	 * à des voitures, ou si la vue sert plutôt à la création de circuits.
	 * 
	 * @see NeuralRacing.UIPanel
	 * @see RaceGrid
	 */
	public RaceView(Dimension size, RaceGrid raceGrid, boolean isUsable) {
		this.setSize(size);
		this.isUsable = isUsable;
		this.raceGrid = raceGrid;
		if (raceGrid != null) { this.repaint(); }
	}
	
	public void paintComponent(Graphics g) {
		
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		
		for (int row = 0; row < rowLength; row++) {
			for (int column = 0; column < columnLength; column++) {
				
				RaceGrid.Box currentBox = raceGrid.grid[column][row];
				
				BufferedImage img = null;
				
				switch (currentBox.tileType) {
					case GRASS: 	    img = NeuralRacing.grass;        break;
					case STRAIGHT_ROAD: img = NeuralRacing.straightRoad; break;
					case CURVED_ROAD:   img = NeuralRacing.curvedRoad;   break;
					case START:         img = NeuralRacing.startLine;    break;
					default:                                             break;
				}
				
				double angle = currentBox.orientation;
				
				AffineTransform transform = AffineTransform.getTranslateInstance(column*squareSize, row*squareSize);
				transform.rotate(angle, img.getWidth()/2, img.getHeight()/2);
				
				g2d.drawImage(img, transform, null);
			}
		}
		
		if (NeuralRacing.UserDefaults.shouldDrawLines && isUsable) {
			
			g2d.setColor(Color.red);
			
			for (Line2D.Double line: raceGrid.hitbox) { g2d.drawLine((int) line.x1, (int) line.y1, (int) line.x2, (int) line.y2); }
			
			g2d.setColor(Color.yellow);
			
			g2d.drawLine((int) raceGrid.startLine.getX1(), (int) raceGrid.startLine.getY1(), (int) raceGrid.startLine.getX2(), (int) raceGrid.startLine.getY2());
			
			for (Line2D.Double line: raceGrid.rewardLines) { g2d.drawLine((int) line.x1, (int) line.y1, (int) line.x2, (int) line.y2); }
		}
	}
	
}
