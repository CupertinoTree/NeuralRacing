package neuralPckg;

import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * Cette classe contient tout le matériel nécessaire pour prendre en charge la
 * représentation et la gestion des circuits du jeu. Les grilles de jeu peuvent
 * être représentées textuellement, et instanciées à partir du texte.
 * 
 * @author Ronan
 * 
 * @see TileType
 * @see Box
 * 
 */
public class RaceGrid {
	
	/**
	 * Cette énumération représente les types de cases de la grille.
	 * 
	 * <p><strong>Valeurs :</strong></p>
	 * <p><code>.START</code> : une ligne de départ.</p>
	 * <p><code>.GRASS</code> : de l'herbe.</p>
	 * <p><code>.STRAIGHT_ROAD</code> : une ligne droite.</p>
	 * <p><code>.CURVED_ROAD</code> : un virage.</p>
	 *
	 * @author Ronan
	 *
	 * @see RaceGrid
	 *
	 */
	static enum TileType { START, GRASS, STRAIGHT_ROAD, CURVED_ROAD; }
	
	/**
	 * Cette classe représente une case dans la grille de jeu. Chaque case possède
	 * un type, définie par l'énumération <code>TileType</code> ci-dessus. Une case a aussi
	 * une orientation (l'angle de rotation de l'image à afficher), et un type d'orientation
	 * (compris entre 0 et 3, c'est une représentation plus maniable de l'orientation).
	 * 
	 * @author Ronan
	 *
	 * @see RaceGrid
	 * @see TileType
	 * 
	 */
	static class Box {
		
		/**Le type de la case.*/
		TileType tileType;
		
		/**
		 * Le type d'orientation de la case, compris entre 0 et 3. C'est le nombre de rotation
		 * d'une valeur de 90° à effectuer pour retrouver l'orientation de la case
		 */
		int orientationType;
		
		/**L'orientation de la case. Elle représente l'angle à appliquer à l'image avant affichage.*/
		double orientation;
		
		/**
		 * Ce constructeur permet d'obtenir une case de type et d'orientation connue.
		 * 
		 * @param type
		 * Le type de la case.
		 * @param theta
		 * L'orientation de la case.
		 */
		public Box(TileType type, double theta) { this.tileType = type; this.orientation = theta; }
	}
	
	/**La liste des lignes de barrière du circuit : c'est en quelque sorte son corps physique.*/
	List<Line2D.Double> hitbox = new ArrayList<Line2D.Double>();
	
	/**La liste des lignes de récompense du circuit.*/
	List<Line2D.Double> rewardLines = new ArrayList<Line2D.Double>();
	
	/**Le point de départ du circuit. Il se trouve au centre de la case de départ.*/
	Point2D.Double start;
	
	/**La ligne de départ. Elle agit comme une barrière dans un sens, et est franchissable dans l'autre sens.*/
	Line2D.Double startLine;
	
	/**La direction de départ des voitures. Elle dépend de l'orientation de la ligne de départ.*/
	double originalDirection;
	
	int rewardLinesCount,
		rowCount    = 8,
		columnCount = 10;
	
	/**La grille du circuit.*/
	Box[][] grid = new Box[columnCount][rowCount];

	/**
	 * Ce constructeur permet d'obtenir une grille de jeu à partir
	 * de sa représentation textuelle, et de taille fixée à 8*10. 
	 * 
	 * @param strRepresentation
	 * La représentation textuelle du circuit.
	 * 
	 * @param ignoreStartLack
	 * Si ce booléen est vrai, le constructeur n'envoie pas d'erreur en cas d'absence de ligne de départ.
	 * 
	 * @throws IllegalArgumentException
	 * Cette erreur est renvoyée si la représentation textuelle est incorrecte.
	 * 
	 * @see RaceGrid
	 * 
	 */
	RaceGrid(EnhancedString strRepresentation, boolean ignoreStartLack) throws IllegalArgumentException { this(strRepresentation, ignoreStartLack, 10, 8); }
	
	/**
	 * Ce constructeur permet d'obtenir une grille de jeu à partir de sa
	 * représentation textuelle.
	 * 
	 * @param strRepresentation
	 * La représentation textuelle du circuit.
	 * 
	 * @param ignoreStartLack
	 * Si ce booléen est vrai, le constructeur n'envoie pas d'erreur en cas d'absence de ligne de départ.
	 * 
	 * @param width
	 * La largeur de la grille.
	 * 
	 * @param height
	 * La hauteur de la grille.
	 * 
	 * @throws IllegalArgumentException
	 * Cette erreur est renvoyée si la représentation textuelle est incorrecte.
	 * 
	 * @see RaceGrid
	 * 
	 */
	public RaceGrid(EnhancedString strRepresentation, boolean ignoreStartLack, int width, int height) throws IllegalArgumentException {
		
		this.rowCount    = height;
		this.columnCount = width;
		
		//Par défaut, une case est toujours représentée par de l'herbe. On rempli donc la grille avec de l'herbe.
		for (int row = 0; row < rowCount; row++) {
			for (int column = 0; column < columnCount; column++) {
				grid[column][row] = new Box(TileType.GRASS, 0);
			}
		}
		
		//Cette chaîne de caractères pourra être découpée au fur et à mesure de l'avancée dans la représentation.
		EnhancedString mutableStr = strRepresentation;
		
		int declarationNumber = 0;
		
		//Tant qu'on a des déclarations, on avance dans la chaîne.
		while (mutableStr.firstIndexOf(';') != -1) {
			
			declarationNumber++;
			
			//On récupère la déclaration à analyser.
			EnhancedString currentDeclaration = mutableStr.substringTo(mutableStr.firstIndexOf(';'));
			
			int firstBracketIndex = currentDeclaration.firstIndexOf('(');
			int lastBracketIndex  = currentDeclaration.lastIndexOf(')');
			
			if (firstBracketIndex == -1 || lastBracketIndex == -1 || firstBracketIndex == 0 || lastBracketIndex != currentDeclaration.getValue().length()-1) {
				throw new IllegalArgumentException(LanguageManager.grabStringFromID("wrongRpz") +  " :\n" + LanguageManager.grabStringFromID("bracketLack") + " (d." + declarationNumber + ")" + ".");
			}
			
			//On récupère le type de la case.
			Box currentBox = null;
			if (currentDeclaration.substringTo(firstBracketIndex).getValue().equals("straight")) {
				
				currentBox = new Box(TileType.STRAIGHT_ROAD, 0);
				
			} else if (currentDeclaration.substringTo(firstBracketIndex).getValue().equals("curved")) {
				
				currentBox = new Box(TileType.CURVED_ROAD, 0);
				
			} else if (currentDeclaration.substringTo(firstBracketIndex).getValue().equals("start")) {
				
				if (start != null) { throw new IllegalArgumentException(LanguageManager.grabStringFromID("wrongRpz") +  " :\n" + LanguageManager.grabStringFromID("twoStart") + " (d." + declarationNumber + ")" +  "."); }
				currentBox = new Box(TileType.START, 0);
				
			} else { throw new IllegalArgumentException(LanguageManager.grabStringFromID("wrongRpz") +  " :\n" + LanguageManager.grabStringFromID("unknownBox") + " (d." + declarationNumber + ")" +  "."); }
			
			//On récupère les arguments qui se trouvent entre les parenthèses.
			EnhancedString argumentsStr = currentDeclaration.substring(firstBracketIndex+1, lastBracketIndex);
			
			int firstSeparatorIndex = argumentsStr.firstIndexOf('|');
			int lastSeparatorIndex  = argumentsStr.lastIndexOf('|');
			
			if (firstSeparatorIndex == -1 || lastSeparatorIndex == -1 || firstSeparatorIndex == lastSeparatorIndex) {
				throw new IllegalArgumentException(LanguageManager.grabStringFromID("wrongRpz") +  " :\n" + LanguageManager.grabStringFromID("tubeLack") + " (d." + declarationNumber + ")" +  ".");
			}
			
			if (firstSeparatorIndex == 0) { throw new IllegalArgumentException(LanguageManager.grabStringFromID("wrongRpz") +  " :\n" + LanguageManager.grabStringFromID("angleInt") + " (d." + declarationNumber + ")" +  "."); }
			if (firstSeparatorIndex + 1 == lastSeparatorIndex) { throw new IllegalArgumentException(LanguageManager.grabStringFromID("wrongRpz") +  " :\n" + LanguageManager.grabStringFromID("rowIntFail") + " (d." + declarationNumber + ")" +  "."); }
			if (lastSeparatorIndex == argumentsStr.getValue().length()-1) { throw new IllegalArgumentException(LanguageManager.grabStringFromID("wrongRpz") +  " :\n" + LanguageManager.grabStringFromID("columnIntFail") + " (d." + declarationNumber + ")" +  "."); }
			
			//On récupère le type d'orientation.
			int orientationType = -1;
			try {
				orientationType = Integer.parseInt(argumentsStr.substringTo(firstSeparatorIndex).getValue());
			} catch (NumberFormatException exc) {
				throw new IllegalArgumentException(LanguageManager.grabStringFromID("wrongRpz") +  " :\n" + LanguageManager.grabStringFromID("angleInt") + " (d." + declarationNumber + ")" +  ".");
			}

			if (currentBox.tileType == TileType.STRAIGHT_ROAD) {
				//Une ligne droite n'admet que deux directions (verticale ou horizontale).
				if (orientationType < 0 || orientationType > 1) { throw new IllegalArgumentException(LanguageManager.grabStringFromID("wrongRpz") +  " :\n" + LanguageManager.grabStringFromID("angleItvS") + " (d." + declarationNumber + ")" +  "."); }
			} else if (currentBox.tileType == TileType.CURVED_ROAD || currentBox.tileType == TileType.START) {
				//Les virages et les lignes de départ n'admettent que quatre orientations.
				if (orientationType < 0 || orientationType > 3) { throw new IllegalArgumentException(LanguageManager.grabStringFromID("wrongRpz") +  " :\n" + LanguageManager.grabStringFromID("angleItvL") + " " + (currentBox.tileType == TileType.CURVED_ROAD ? LanguageManager.grabStringFromID("aTurn") : LanguageManager.grabStringFromID("aStartLine") + " (d." + declarationNumber + ")" +  ".")); }
			}
			
			//On assigne ainsi le type d'orientation et l'orientation effective à la case.
			currentBox.orientationType = orientationType;
			currentBox.orientation     = orientationType * (Math.PI/2);
			
			int rowBegin,  rowEnd,  columnBegin,  columnEnd;
			    rowBegin = rowEnd = columnBegin = columnEnd = -1;
			
			//On découpe la partie d'informations relative à la ligne, ou à l'ensemble de lignes, de la déclaration,
			//afin de stocker dans les 4 variables ci-dessus le nombre de cases identiques en type et en orientation à introduire, et leur position.
			EnhancedString rowSubstring = argumentsStr.substringFrom(lastSeparatorIndex+1);
			if (rowSubstring.firstIndexOf('-') != -1) {
				//Si on a un ensemble de lignes, on récupère l'index du trait d'union.
				int hyphen = rowSubstring.firstIndexOf('-');
				if (currentBox.tileType == TileType.CURVED_ROAD || currentBox.tileType == TileType.START) { throw new IllegalArgumentException(LanguageManager.grabStringFromID("wrongRpz") +  " :\n" + LanguageManager.grabStringFromID("nonStraightItv") + " (d." + declarationNumber + ")" +  "."); }
				
				try {
					rowBegin = Integer.parseInt(rowSubstring.substringTo(hyphen).getValue());
					rowEnd   = Integer.parseInt(rowSubstring.substringFrom(hyphen+1).getValue());
				} catch (NumberFormatException exc) {
					throw new IllegalArgumentException(LanguageManager.grabStringFromID("wrongRpz") +  " :\n" + LanguageManager.grabStringFromID("intItvBounds") + " (l." + declarationNumber + ")" +  ".");
				}
				
				if (rowBegin < 0 || rowBegin > rowCount-1 || rowEnd < 0 || rowEnd > rowCount-1) { throw new IllegalArgumentException(LanguageManager.grabStringFromID("wrongRpz") +  " :\n" + LanguageManager.grabStringFromID("rowItv") + " (d." + declarationNumber + ")" +  "."); }
			} else {
				//Si on a une case seule, on lit directement l'information.
				try {
					rowBegin = rowEnd = Integer.parseInt(rowSubstring.getValue());
				} catch (NumberFormatException exc) {
					throw new IllegalArgumentException(LanguageManager.grabStringFromID("wrongRpz") +  " :\n" + LanguageManager.grabStringFromID("rowIntFail") + " (d." + declarationNumber + ")" +  ".");
				}
				
				if (rowBegin < 0 || rowBegin > rowCount-1) { throw new IllegalArgumentException(LanguageManager.grabStringFromID("wrongRpz") +  " :\n" + LanguageManager.grabStringFromID("rowItv") + " (d." + declarationNumber + ")" +  "."); }
			}
			
			//On découpe la partie d'informations relative à la colonne, ou à l'ensemble de colonnes, de la déclaration.
			EnhancedString columnSubstring = argumentsStr.substring(firstSeparatorIndex+1, lastSeparatorIndex);
			if (columnSubstring.firstIndexOf('-') != -1) {
				//Si on a un ensemble de lignes, on récupère l'index du trait d'union.
				int hyphen = columnSubstring.firstIndexOf('-');
				if (currentBox.tileType == TileType.CURVED_ROAD || currentBox.tileType == TileType.START) { throw new IllegalArgumentException(LanguageManager.grabStringFromID("wrongRpz") +  " :\n" + LanguageManager.grabStringFromID("nonStraightItv") + " (d." + declarationNumber + ")" +  "."); }
				
				try {
					columnBegin = Integer.parseInt(columnSubstring.substringTo(hyphen).getValue());
					columnEnd   = Integer.parseInt(columnSubstring.substringFrom(hyphen+1).getValue());
				} catch (NumberFormatException exc) {
					throw new IllegalArgumentException(LanguageManager.grabStringFromID("wrongRpz") +  " :\n" + LanguageManager.grabStringFromID("intItvBounds")  + " (d." + declarationNumber + ")" + ".");
				}

				if (columnBegin < 0 || columnBegin > columnCount-1 || columnEnd < 0 || columnEnd > columnCount-1) { throw new IllegalArgumentException(LanguageManager.grabStringFromID("wrongRpz") +  " :\n" + LanguageManager.grabStringFromID("columnItv") + " (d." + declarationNumber + ")" +  "."); }
			} else {
				//Si on a une case seule, on lit directement l'information.
				try {
					columnBegin = Integer.parseInt(columnSubstring.getValue());
					columnEnd   = columnBegin;
				} catch (NumberFormatException exc) {
					throw new IllegalArgumentException(LanguageManager.grabStringFromID("wrongRpz") +  " :\n" + LanguageManager.grabStringFromID("columnIntFail") + " (d." + declarationNumber + ")" +  ".");
				}
				
				if (columnBegin < 0 || columnBegin > columnCount-1) { throw new IllegalArgumentException(LanguageManager.grabStringFromID("wrongRpz") +  " :\n" + LanguageManager.grabStringFromID("columnItv") + " (d." + declarationNumber + ")" +  "."); }
			}
			
			if (columnBegin  != columnEnd && rowBegin != rowEnd) { throw new IllegalArgumentException(LanguageManager.grabStringFromID("wrongRpz") +  " :\n" + LanguageManager.grabStringFromID("gridSupport") + " (d." + declarationNumber + ")" +  "."); }
			
			if ((columnBegin != columnEnd && currentBox.tileType == TileType.STRAIGHT_ROAD && currentBox.orientationType == 1) || (rowBegin != rowEnd && currentBox.tileType == TileType.STRAIGHT_ROAD && currentBox.orientationType == 0)) { throw new IllegalArgumentException(LanguageManager.grabStringFromID("wrongRpz") +  " :\n" + LanguageManager.grabStringFromID("normalItv") + " (d." + declarationNumber + ")" +  "."); }
			
			//Désormais, on ajoute les murs nécessaires au corps physique du terrain.
			if (currentBox.tileType == TileType.STRAIGHT_ROAD || currentBox.tileType == TileType.START) {
				//Dans le cas des lignes droites ou des lignes d'arrivées, on a deux murs,
				//qui se font face.
				Point2D.Double firstLineOne = new Point2D.Double(), firstLineTwo = new Point2D.Double(), secondLineOne = new Point2D.Double(), secondLineTwo = new Point2D.Double();
				
				if (currentBox.orientationType == 0 || currentBox.orientationType == 2) {
					
					//Dans le sens horizontal, les murs sont horizontaux.
					firstLineOne.x  = secondLineOne.x = columnBegin*70;
					firstLineTwo.x  = secondLineTwo.x = (columnEnd+1)*70;
					firstLineOne.y  = firstLineTwo.y  = rowBegin*70;
					secondLineOne.y = secondLineTwo.y = (rowEnd+1)*70;
					
				} else {
					
					//Dans le sens vertical, les murs sont verticaux.
					firstLineOne.x  = firstLineTwo.x  = columnBegin*70;
					secondLineOne.x = secondLineTwo.x = (columnEnd+1)*70;
					firstLineOne.y  = secondLineOne.y = rowBegin*70;
					firstLineTwo.y  = secondLineTwo.y = (rowEnd+1)*70;
					
				}
				
				//On ajoute les deux lignes au corps physique du terrain.
				hitbox.add(new Line2D.Double(firstLineOne, firstLineTwo));
				hitbox.add(new Line2D.Double(secondLineOne, secondLineTwo));
				
			} else if (currentBox.tileType == TileType.CURVED_ROAD) {
				//Dans le cas des virages, on a deux murs adjacents,
				//avec l'un perpendiculaire à l'autre.
				Point2D.Double corner = new Point2D.Double(), firstBranch = new Point2D.Double(), secondBranch = new Point2D.Double();
				
				if (orientationType == 0) {
					
					corner.x       = secondBranch.x = columnBegin*70;
					firstBranch.x  = corner.x + 70;
					corner.y       = firstBranch.y = rowBegin*70;
					secondBranch.y = corner.y + 70;
					//On profite du fait d'avoir un accès à la position du coin pour ajouter
					//la ligne de récompense correspondante.
					rewardLines.add(new Line2D.Double(corner, new Point2D.Double(corner.getX()+70, corner.getY()+70)));
					
				} else if (orientationType == 1) {
					
					corner.x       = firstBranch.x = (columnBegin+1)*70;
					secondBranch.x = corner.x - 70;
					corner.y       = secondBranch.y = rowBegin*70;
					firstBranch.y  = corner.y + 70;
					//On profite du fait d'avoir un accès à la position du coin pour ajouter
					//la ligne de récompense correspondante.
					rewardLines.add(new Line2D.Double(corner, new Point2D.Double(corner.getX()-70, corner.getY()+70)));
					
				} else if (orientationType == 2) {
					
					corner.x       = secondBranch.x = (columnBegin+1)*70;
					firstBranch.x  = corner.x - 70;
					corner.y       = firstBranch.y = (rowBegin+1)*70;
					secondBranch.y = corner.y - 70;
					//On profite du fait d'avoir un accès à la position du coin pour ajouter
					//la ligne de récompense correspondante.
					rewardLines.add(new Line2D.Double(corner, new Point2D.Double(corner.getX()-70, corner.getY()-70)));
					
				} else if (orientationType == 3) {
					
					corner.x       = firstBranch.x = columnBegin*70;
					secondBranch.x = corner.x + 70;
					corner.y       = secondBranch.y = (rowBegin+1)*70;
					firstBranch.y  = corner.y - 70;
					//On profite du fait d'avoir un accès à la position du coin pour ajouter
					//la ligne de récompense correspondante.
					rewardLines.add(new Line2D.Double(corner, new Point2D.Double(corner.getX()+70, corner.getY()-70)));
					
				}
				
				//On ajoute les deux lignes au corps physique du terrain.
				hitbox.add(new Line2D.Double(corner, firstBranch));
				hitbox.add(new Line2D.Double(corner, secondBranch));
			}

			for (int row = rowBegin; row <= rowEnd; row++) {
				for (int column = columnBegin; column <= columnEnd; column++) {
					//Pour chacune des positions décrites par l'ensemble
					//inscrit dans la déclaration actuelle, si on a affaire à une ligne droite,
					//on ajoute la ligne de récompense qui lui correspond à la liste des lignes de récompense.
					if (currentBox.tileType == TileType.STRAIGHT_ROAD) {
						if (orientationType == 0) {
							//Si la ligne est horizontale, la ligne de récompense est verticale
							rewardLines.add(new Line2D.Double(column*70+35, row*70, column*70+35, (row+1)*70));
						} else {
							//Si la ligne est verticale, la ligne de récompense est horizontale
							rewardLines.add(new Line2D.Double(column*70, row*70+35, (column+1)*70, row*70+35));
						}
					}
					
					//On ajoute la case à la grille.
					grid[column][row] = currentBox;
				}
			}
			
			//Si on a actuellement affaire à une ligne de départ,
			//on récupère les informations relatives au départ sur le circuit.
			if (currentBox.tileType == TileType.START) {
				//Le point de départ est au centre de la case de départ.
				start = new Point2D.Double((columnBegin*70)+35, (rowBegin*70)+35);
				
				//La direction originale est égale à l'orientation de la case.
				originalDirection = currentBox.orientation;
				
				switch (orientationType) {
					//On récupère la ligne de départ.
					case 0: startLine = new Line2D.Double(columnBegin*70,     rowBegin*70,     columnBegin*70,     (rowBegin+1)*70); break;
					case 1: startLine = new Line2D.Double(columnBegin*70,     rowBegin*70,     (columnBegin+1)*70, rowBegin*70);     break;
					case 2: startLine = new Line2D.Double((columnBegin+1)*70, rowBegin*70,     (columnBegin+1)*70, (rowBegin+1)*70); break;
					case 3: startLine = new Line2D.Double(columnBegin*70,     (rowBegin+1)*70, (columnBegin+1)*70, (rowBegin+1)*70); break;
					default: break;
				}
			}
			
			//On avance dans la représentation textuelle.
			mutableStr = mutableStr.substringFrom(mutableStr.firstIndexOf(';')+1);
		}
		
		if (start == null && !ignoreStartLack) {
			throw new IllegalArgumentException(LanguageManager.grabStringFromID("wrongRpz") +  " :\n" + LanguageManager.grabStringFromID("startLack") + " (d." + declarationNumber + ")" +  ".");
		}
		
		rewardLinesCount = this.rewardLines.size();
		
	}
	
	/**
	 * Cette méthode permet d'obtenir la représentation textuelle de la grille.
	 * Les cases qui ne sont pas de l'herbe sont représentées une à une,
	 * séparées par un point-virgule, de la façon suivante :
	 * <p>
	 * <code>typeDeCase(typeD'Orientation|x|y)</code> avec <code>typeDeCase = { straight, curved, start }</code>
	 * <p>
	 * Exemple avec un virage gauche-bas à la colonne 4 et à la ligne 8 : curved(1|4|8)
	 * <p>
	 * Cette représentation comprend aussi les ensemble de lignes droites, en séparant les bornes avec un tiret.
	 * <p>
	 * Exemple avec une série horizontale, allant de la colonne 1 à la 7, et à la ligne 2 :
	 * straight(0|1-7|2)
	 * <p>
	 * 
	 * Cette méthode ne vérifie pas la conformité de la grille. Elle est donc privée.
	 * 
	 * @return
	 * La représentation textuelle.
	 * 
	 * @see RaceGrid
	 * 
	 */
	private String toStringRpzUnsafe() {
		
		String strRepresentation = "";
		Box currentBox;
		
		//Cette liste stocke des tableaux contenant chacun trois éléments,
		//utiles à l'expression des ensembles de lignes horizontales.
		//Le premier est la colonne de départ de l'ensemble.
		//Le deuxième, la colonne de fin de l'ensemble.
		//Le dernier est la ligne à laquelle se trouve l'ensemble.
		//Si les deux premiers éléments sont égaux, l'ensemble est en fait une case isolée.
		List<int []> horizontalLines = new ArrayList<int []>();
		
		//Cette liste stocke des tableaux contenant chacun trois éléments,
		//utiles à l'expression des ensembles de lignes verticales.
		//Le premier est la colonne à laquelle se trouve l'ensemble.
		//Le deuxième est la ligne de départ de l'ensemble.
		//Le dernier, la ligne de fin de l'ensemble.
		//Si les deux premiers éléments sont égaux, l'ensemble est en fait une case isolée.
		List<int []> verticalLines = new ArrayList<int []>();
		
		for (int column = 0; column < columnCount; column++) {
			for (int row = 0; row < rowCount; row++) {
				//On récupère successivement la valeur de chacune des cases du terrain.
				currentBox = grid[column][row];
				
				//Si la case est de l'herbe, on l'ignore.
				if (currentBox.tileType != TileType.GRASS) {
					if (currentBox.tileType != TileType.STRAIGHT_ROAD) {
						
						//Si la case est un virage ou une ligne de départ, on l'ajoute directement à la représentation.
						strRepresentation += (currentBox.tileType == TileType.START ? "start" : "curved") + "(" + currentBox.orientationType + "|" + column + "|" + row + ");";
						
					} else {
						
						//Ce booléen exprime le fait que l'on a trouvé un ensemble auquel
						//la présente case doit appartenir.
						boolean found = false;
						
						if (currentBox.orientationType == 0) {
							//Si la case est une ligne horizontale.
							
							for (int [] line: horizontalLines) {
								if (line[2] == row) {
									//Si la row de la ligne horizontale n'est pas la même que celle de la case, on ignore cette ligne.
									
									if (line[1] == column-1) {
										//Si la colonne de fin de la ligne horizontale est située juste à gauche de la case,
										//on agrandit la ligne horizontale.
										found = true;
										line[1] = column;
									}
								}
							}
							
							if (!found) {
								//Si la case n'appartient pas à un autre ensemble,
								//on ajoute un nouvel ensemble horizontal.
								int [] newLine = new int [3];
								newLine[0] = newLine[1] = column;
								newLine[2] = row;
								horizontalLines.add(newLine);
							}
							
						} else {
							//Si la ligne est verticale.
							
							for (int [] line: verticalLines) {
								if (line[0] == column) {
									//Si la colonne de la ligne verticale n'est pas la même que celle de la case, on ignore cette ligne.
									
									if (line[2] == row-1) {
										//Si la row de fin de la ligne verticale est située juste en-dessous de la case,
										//on agrandit la ligne verticale.
										found = true;
										line[2] = row;
									}
								}
							}
							
							if (!found) {
								//Si la case n'appartient pas à un autre ensemble,
								//on ajoute un nouvel ensemble vertical.
								int [] newLine = new int [3];
								newLine[0] = column;
								newLine[1] = newLine[2] = row;
								verticalLines.add(newLine);
							}
							
						}
						
					}
				}
			}
		}
		
		//On ajoute finalement toutes les lignes droites.
		
		for (int [] horizontalLine: horizontalLines) {
			strRepresentation += "straight(0|" + (horizontalLine[0] != horizontalLine[1] ? horizontalLine[0] + "-" + horizontalLine[1] : horizontalLine[0]) + "|" + horizontalLine[2] + ");";
		}
		for (int [] verticalLine: verticalLines) {
			strRepresentation += "straight(1|" + verticalLine[0] + "|" + (verticalLine[1] != verticalLine[2] ? verticalLine[1] + "-" + verticalLine[2] : verticalLine[1]) + ");";
		}
		
		return strRepresentation;
		
	}
	
	/**
	 * Cette méthode permet d'obtenir la représentation textuelle de la grille.
	 * Les cases qui ne sont pas de l'herbe sont représentées une à une,
	 * séparées par un point-virgule, de la façon suivante :
	 * <p>
	 * <code>typeDeCase(typeD'Orientation|x|y)</code> avec <code>typeDeCase = { straight, curved, start }</code>
	 * <p>
	 * Exemple avec un virage gauche-bas à la colonne 4 et à la ligne 8 : curved(1|4|8)
	 * <p>
	 * Cette représentation comprend aussi les ensemble de lignes droites, en séparant les bornes avec un tiret.
	 * <p>
	 * Exemple avec une série horizontale, allant de la colonne 1 à la 7, et à la ligne 2 :
	 * straight(0|1-7|2)
	 * <p>
	 * Une représentation doit contenir une seule ligne de départ. On ne peut pas représenter des ensembles de lignes
	 * qui soient en même temps vertical et horizontal.
	 * 
	 * @return
	 * La représentation textuelle.
	 * 
	 * @throws IllegalArgumentException
	 * Cette erreur est renvoyée lorsque la représentation textuelle est incorrecte.
	 * 
	 * @see RaceGrid
	 * 
	 */
	String toStringRpz() throws IllegalArgumentException {
		
		String strRepresentation = toStringRpzUnsafe();
		
		//On vérifie que la représentation soit valide, par exemple pour éviter l'absence d'une ligne de départ.
		try {
			new RaceGrid(new EnhancedString(strRepresentation), false);
		} catch (IllegalArgumentException exc) {
			throw exc;
		}
		
		return strRepresentation;
	}
	
	/**
	 * Cette méthode permet d'obtenir une partie de la grille de jeu.
	 * 
	 * @param x
	 * La coordonnée x de l'origine de la sélection.
	 * 
	 * @param y
	 * La coordonnée y de l'origine de la sélection.
	 * 
	 * @param width
	 * La largeur de la sélection.
	 * 
	 * @param height
	 * La hauteur de la sélection.
	 * 
	 * @return
	 * La partie obtenue.
	 * 
	 * @see RaceGrid
	 * 
	 */
	RaceGrid slice(int x, int y, int width, int height) {
		
		//Si la sélection dépasse de la grille, on s'arrête ici.
		if (x < 0 || x > columnCount || y < 0 || y > columnCount || width <= 0 || width > columnCount || height <= 0 || height > rowCount) { return null; }
		
		RaceGrid sliced = new RaceGrid(new EnhancedString(""), true, width, height);
		
		for (int column = x; column < x + width; column++) {
			for (int row = y; row < y + height; row++) {
				sliced.grid[column-x][row-y] = this.grid[column][row];
			}
		}
		
		return sliced;
	}
	
	/**
	 * Cette méthode permet d'insérer le contenu d'une grille de jeu dans celle-ci.
	 * 
	 * @param grid
	 * La grille de jeu à insérer.
	 * 
	 * @param frame
	 * La taille et la position de l'insertion.
	 * 
	 * @see RaceGrid
	 * 
	 */
	void insertGrid(RaceGrid grid, Rectangle frame) {
		
		//Si l'insertion dépasse de la grille ou qu'elle revient à ne rien changer, on s'arrête ici.
		if (frame.x > columnCount-1 || frame.x < 0 || frame.y > rowCount-1 || frame.y < 0 || frame.width <= 0 || frame.width > columnCount || frame.height <= 0 || frame.height > rowCount || (grid.grid.equals(this.grid) && frame.width == columnCount && frame.height == rowCount && frame.x == 0 && frame.y == 0)) { return; }
		
		//On retaille la grille à insérer, car la taille d'insertion ne fait pas forcément la même taille qu'elle.
		RaceGrid reframed = grid.slice(0, 0, Math.min(frame.width, grid.columnCount), Math.min(frame.height, grid.rowCount));
		
		for (int column = frame.x; column < frame.x + Math.min(frame.width, grid.columnCount); column++) {
			for (int row = frame.y; row < frame.y + Math.min(frame.height, grid.rowCount); row++) {
				this.grid[column][row] = reframed.grid[column - frame.x][row - frame.y];
			}
		}
		
	}
	
	/**
	 * Cette méthode permet de copier une autre grille dans celle-ci.
	 * 
	 * @param grid
	 * La grille de jeu à copier.
	 * 
	 * @see RaceGrid
	 * 
	 */
	void synchronizeWithGrid(RaceGrid grid) {
		this.grid              = grid.grid;
		this.hitbox            = grid.hitbox;
		this.rewardLines       = grid.rewardLines;
		this.rewardLinesCount  = grid.rewardLinesCount;
		this.start             = grid.start;
		this.startLine         = grid.startLine;
		this.originalDirection = grid.originalDirection;
	}
	
	/**
	 * Cette méthode compare la grille présente avec une autre.
	 * 
	 * @param grid
	 * La grille de jeu à comparer.
	 * @return
	 */
	boolean equals(RaceGrid grid) { return (this.toStringRpzUnsafe().equals(grid.toStringRpzUnsafe())); }
	
}
