package neuralPckg;

import java.awt.BasicStroke;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SpringLayout;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicSliderUI;
import javax.swing.table.AbstractTableModel;

import neuralPckg.NeuralModel.BrainStruct;

/**
 * La classe principale de Neural Racing. C'est le point d'entrée du programme.
 * En tant que tel, elle n'est pas destinée à être instanciée.
 * Elle permet également de stocker des ressources et des méthodes
 * dont l'utilisation est commune aux différentes autres classes.
 * 
 * @author Ronan
 *
 * @see UIPanel
 * @see UIView
 * @see UIProgressBar
 * @see UILabel
 * @see UIButton
 * @see UISelector
 * @see UIGraphView
 * @see GraphPanel
 * @see UIParameterToggle
 * @see UIParameterSlider
 * @see UINeuralView
 * @see KeyListeningAction
 * @see UserDefaults
 */
public class NeuralRacing {
	
	/**
	 * C'est la représentation du circuit par défaut de Neural Racing.
	 * Il ne peut être supprimé ou renommé.
	 * 
	 * @see RaceGrid
	 */
	static String defaultRace = "start(3|6|4);straight(1|0|1-6);straight(1|2|1-4);straight(1|4|1);straight(1|6|3);straight(1|9|1-6);straight(0|1-8|7);straight(0|3-5|5);straight(0|5|2);straight(0|5-8|0);straight(0|1|0);curved(0|0|0);curved(2|9|7);curved(3|0|7);curved(3|2|5);curved(0|4|0);curved(1|2|0);curved(1|9|0);curved(3|4|2);curved(1|6|2);curved(2|6|5);";
	
	
	/**
	 * C'est la représentation de l'individu par défaut de Neural Racing.
	 * Il ne peut être supprimé ou renommé.
	 * 
	 * @see NeuralModel
	 */
	static String defaultIndiv = "L(0|N[|-0.48849392|0.17950952|-0.5351249|0.8312553|-0.962762|-0.73464084|]N[|0.75679255|0.4043212|-0.22971869|0.39925265|-0.6974627|-0.20055437|]N[|0.6806061|-0.8725375|0.71394646|0.7438654|-0.24576116|-0.6260086|]N[|0.7387905|-0.7720592|-0.9513762|-0.60743296|0.6568867|-0.44552875|]N[|-0.1435585|-0.07861233|0.29355597|0.5873692|0.68514574|0.5802671|]N[|0.15966392|-0.3319955|0.7228521|0.5189712|0.5819684|-0.9950974|]|)#L(1|N[|-0.65089893|0.46413493|-0.26776278|-0.13760638|-0.93147004|]N[|-0.8179518|0.95945585|0.59778893|-0.60079193|-0.58863866|]N[|-0.2820716|0.60114765|-0.12743223|-0.89435184|-0.9203104|]N[|-0.5275233|0.27334392|-0.59140897|-0.58511496|0.8507596|]N[|0.8604065|-0.7974943|0.33259308|0.106408|0.9085008|]N[|-0.6093904|-0.34082305|-0.8842217|0.5569091|0.70907664|]|)L(1|N[|0.78000426|-0.05088985|-0.23014963|0.48797095|]N[|-0.5473126|0.50450814|-0.6091912|-0.2008729|]N[|-0.45123637|-0.41189897|-0.4062444|0.3612517|]N[|0.4352405|0.6898508|0.6813599|-0.83361876|]N[|0.34780312|0.4710058|0.70276725|0.38542426|]|)#L(0|N[|]N[|]N[|]N[|]|)";
	
	static Class<?> reader = (new NeuralRacing()).getClass();
	
	/**Image commune aux classes du programme.*/
	static Icon straightRoadSelectorIcon  = new ImageIcon(reader.getResource("/assets/customButtons/straightRoadSelector.png")),
			    curvedRoadSelectorIcon    = new ImageIcon(reader.getResource("/assets/customButtons/curvedRoadSelector.png")),
			    grassSelectorIcon         = new ImageIcon(reader.getResource("/assets/customButtons/grassSelector.png")),
			    startLineSelectorIcon     = new ImageIcon(reader.getResource("/assets/customButtons/startLineSelector.png")),
			    editFile                  = new ImageIcon(reader.getResource("/assets/customButtons/editFile.png")),
				importFile			      = new ImageIcon(reader.getResource("/assets/customButtons/importFile.png")),
			    saveToLib                 = new ImageIcon(reader.getResource("/assets/customButtons/saveToLib.png")),
			    exportFile		   	      = new ImageIcon(reader.getResource("/assets/customButtons/exportFile.png"));
	
	/**Couleur commune aux classes du programme.*/
	static Color darkGrayColor   		  = new Color(85, 85, 85),
				 darkerGrayColor 		  = new Color(43, 43, 43),
				 lightGrayColor           = new Color(240, 240, 240),
				 lighterGrayColor         = new Color(250, 250, 250),
				 menuGreen   			  = new Color(20, 139, 30),
				 darkGreenBackground 	  = new Color(0, 108, 58),
				 widespreadGreenishGreen  = new Color(10, 175, 99),
				 selectedGreenTint 		  = new Color(0, 160, 13),
				 negativeOrange           = new Color(255, 120, 0);
	
	/**Image commune aux classes du programme.*/
	static BufferedImage startLine,
						 grass,
						 straightRoad,
						 curvedRoad,
						 greenCar,
				  		 redCar,
				  		 blueCar,
				  		 orangeCar,
				  		 greyCar,
				  		 purpleCar,
				  		 neonCar,
				  		 blackCar,
				  		 yellowCar,
				  		 pinkCar,
				  		 rotateLeft,
				  		 rotateRight,
				  		 play,
				  		 pause,
				  		 fastForward,
				  		 fastBackward,
				  		 pencilSelectorLeftIcon,
				  		 pencilSelectorRightIcon,
				  		 systemSelectorLeftIcon,
				  		 systemSelectorRightIcon,
				  		 mainSelectorLeftIcon,
				  		 mainSelectorRightIcon,
				  		 insertIcon,
						 openRaceIcon,
					     manageFileIcon,
					     brainIcon,
					     superCarImage,
					     titleImage,
					     languageIcon,
					     guideIcon;
	
	/**Ce tableau contient toutes les images de voiture.*/
	static BufferedImage [] cars     = new BufferedImage [10];
	
	/**Ce tableau contient le nom de toutes les images de voiture.*/
	static String        [] carsName = {};
	
	/**Le <code> CardLayout </code> principal, contenant tous les onglets.*/
	static CardLayout cardStack = new CardLayout();
	
	static JFrame window;
	
	static JPanel mainPanel;
	static MainMenuView menuView;
	static EvolutionView evolutionView;
	static ExperimentsView experimentsView;
	static RaceEditorView raceEditorView;
	static OptionsView optionsView;
	
	//Ces dictionnaires stockent les circuits et réseaux, et leur nom.
	static Map<String, String> raceList    = new HashMap<String, String>(),
							   networkList = new HashMap<String, String>();
	
	/**Le tableau contenant les différentes langues supportées par le programme.*/
	static String [] languageArray = { "Français", "English" }; 
	
	private static Boolean avenirNextExists;
	
	public static UIUserGuideFrame guide;
	
	public static void main(String[] args) {
		
		//Afin de parfaire la compatibilité entre les différentes plateformes,
		//on utilise les attributs visuels et fonctionnels de l'environnement commun.
		try {
			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {}
		
		//On récupère les paramètres choisis par l'utilisateur.
		//Cela s'avèrera notamment utile dès lors qu'il faudra choisir de lancer ou non la musique, et le jingle.
		UserDefaults.read();
		
		LanguageManager.registerPrimaryMutator(new Runnable() {
			@Override public void run() {
				NeuralRacing.raceList.remove(LanguageManager.grabStringFromID("defaultRace"));
			}
		});
		
		//On permet au tableau des noms de voiture de récupérer les noms
		//correspondant à la langue choisie, et au circuit par défaut de changer de nom.
		LanguageManager.registerUIElement(new LanguageManager.LanguageMutable() {
			@Override public void updateData() {
				String [] newCarsName = { LanguageManager.grabStringFromID("greenColor"), LanguageManager.grabStringFromID("redColor"), LanguageManager.grabStringFromID("cyanColor"), LanguageManager.grabStringFromID("orangeColor"), LanguageManager.grabStringFromID("greyColor"), LanguageManager.grabStringFromID("purpleColor"), LanguageManager.grabStringFromID("neonBlueColor"), LanguageManager.grabStringFromID("blackColor"), LanguageManager.grabStringFromID("yellowColor"), LanguageManager.grabStringFromID("pinkColor") };
				NeuralRacing.carsName = newCarsName;
				NeuralRacing.raceList.put(LanguageManager.grabStringFromID("defaultRace"), NeuralRacing.defaultRace);
				NeuralRacing.networkList.put(LanguageManager.grabStringFromID("defaultIndiv"), NeuralRacing.defaultIndiv);
				
				if (!NeuralRacing.raceList.containsKey(NeuralRacing.evolutionView.selectedRace))   { NeuralRacing.evolutionView.selectedRace   = LanguageManager.grabStringFromID("defaultRace"); }
				if (!NeuralRacing.raceList.containsKey(NeuralRacing.experimentsView.selectedRace)) { NeuralRacing.experimentsView.selectedRace = LanguageManager.grabStringFromID("defaultRace"); }
				if (!NeuralRacing.raceList.containsKey(NeuralRacing.raceEditorView.selectedRace))  { NeuralRacing.raceEditorView.selectedRace  = LanguageManager.grabStringFromID("defaultRace"); }
			}
		});
		
		window = new JFrame("Neural Racing");
		window.setSize(901, 661 + window.getInsets().top);
		window.setResizable(false);
		window.setLocationRelativeTo(null);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		try {
			greenCar  			    = ImageIO.read(reader.getResource("/assets/greenCar.png"));
			redCar  			    = ImageIO.read(reader.getResource("/assets/redCar.png"));
			blueCar  			    = ImageIO.read(reader.getResource("/assets/blueCar.png"));
			orangeCar  			    = ImageIO.read(reader.getResource("/assets/orangeCar.png"));
			greyCar  			    = ImageIO.read(reader.getResource("/assets/greyCar.png"));
			purpleCar  			    = ImageIO.read(reader.getResource("/assets/purpleCar.png"));
			neonCar  			    = ImageIO.read(reader.getResource("/assets/neonCar.png"));
			blackCar  			    = ImageIO.read(reader.getResource("/assets/blackCar.png"));
			yellowCar  			    = ImageIO.read(reader.getResource("/assets/yellowCar.png"));
			pinkCar  			    = ImageIO.read(reader.getResource("/assets/pinkCar.png"));
			play  			        = ImageIO.read(reader.getResource("/assets/customButtons/play.png"));
			pause    			    = ImageIO.read(reader.getResource("/assets/customButtons/pause.png"));
			fastForward      	    = ImageIO.read(reader.getResource("/assets/customButtons/forth.png"));
		    fastBackward  		    = ImageIO.read(reader.getResource("/assets/customButtons/back.png"));
		    rotateLeft      	    = ImageIO.read(reader.getResource("/assets/customButtons/rotateLeft.png"));
		    rotateRight  		    = ImageIO.read(reader.getResource("/assets/customButtons/rotateRight.png"));
			pencilSelectorLeftIcon  = ImageIO.read(reader.getResource("/assets/customButtons/pencilSelectorLeft.png"));
			pencilSelectorRightIcon = ImageIO.read(reader.getResource("/assets/customButtons/pencilSelectorRight.png"));
			systemSelectorLeftIcon  = ImageIO.read(reader.getResource("/assets/customButtons/hyperparams.png"));
			systemSelectorRightIcon = ImageIO.read(reader.getResource("/assets/customButtons/histograms.png"));
			mainSelectorLeftIcon    = ImageIO.read(reader.getResource("/assets/customButtons/steer.png"));
			mainSelectorRightIcon   = ImageIO.read(reader.getResource("/assets/customButtons/lab.png"));
			insertIcon  	        = ImageIO.read(reader.getResource("/assets/customButtons/insert.png"));
			openRaceIcon  			= ImageIO.read(reader.getResource("/assets/customButtons/openRace.png"));
			manageFileIcon  	    = ImageIO.read(reader.getResource("/assets/customButtons/manageFile.png"));
			brainIcon  			    = ImageIO.read(reader.getResource("/assets/customButtons/brain.png"));
			superCarImage  		    = ImageIO.read(reader.getResource("/assets/menuCar.png"));
			titleImage  		    = ImageIO.read(reader.getResource("/assets/menuTitle.png"));
			startLine  		        = ImageIO.read(reader.getResource("/assets/startLine.png"));
			grass  		            = ImageIO.read(reader.getResource("/assets/grass.png"));
			straightRoad  		    = ImageIO.read(reader.getResource("/assets/straightRoad.png"));
			curvedRoad  		    = ImageIO.read(reader.getResource("/assets/curvedRoad.png"));
			languageIcon            = ImageIO.read(reader.getResource("/assets/customButtons/language.png"));
			guideIcon               = ImageIO.read(reader.getResource("/assets/customButtons/guide.png"));
			
		} catch (Exception e) {
			JOptionPane.showMessageDialog(window, LanguageManager.grabStringFromID("noImages"), LanguageManager.grabStringFromID("error"), JOptionPane.ERROR_MESSAGE);
			System.exit(0);
			return;
		}
		
		cars[0] = greenCar;
		cars[1] = redCar;
		cars[2] = blueCar;
		cars[3] = orangeCar;
		cars[4] = greyCar;
		cars[5] = purpleCar;
		cars[6] = neonCar;
		cars[7] = blackCar;
		cars[8] = yellowCar;
		cars[9] = pinkCar;
		
		guide = new UIUserGuideFrame();
		
		mainPanel = new JPanel();
		mainPanel.setSize(window.getSize());
		mainPanel.setLayout(cardStack);
		mainPanel.setBackground(Color.black);
		
		//On met en place le KeyBinding pour les flèches du clavier, afin de permettre la conduite.
		//On donne les identifiants dans la <code>InputMap</code>, et les actions correspondantes dans la <code>ActionMap</code>.
		mainPanel.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0),  "Left");
		mainPanel.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "Right");
		mainPanel.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0),    "Up");
		mainPanel.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0),  "Down");
		
		mainPanel.getActionMap().put("Left",  new KeyListeningAction("Left"));
		mainPanel.getActionMap().put("Right", new KeyListeningAction("Right"));
		mainPanel.getActionMap().put("Up",    new KeyListeningAction("Up"));
		mainPanel.getActionMap().put("Down",  new KeyListeningAction("Down"));
		
		/*
		 * On lit le contenu du fichier contenant les représentation
		 * de tous les circuits créés par l'utilisateur.
		 * On stock ces représentations, et le nom associé à chacune, dans la liste prévue à cet effet.
		 */
		EnhancedString line = new EnhancedString("");
		try {
			
			Scanner reader = new Scanner(new File("data/defaultList.gameGrid"));
			while (reader.hasNextLine()) { 
				line.setValue(reader.nextLine());
				if (line.getValue().contains(":")) {
					raceList.put(line.substring(0, line.lastIndexOf(':')).getValue(), line.substring(line.lastIndexOf(':')+1, line.getValue().length()).getValue());
				}
			}
			reader.close();
			
		} catch (Exception exc) {
			try {
				
				//Si le dossier data n'existe pas, on l'ajoute.
				File dataDir = new File("data");
				if (!dataDir.exists()) { dataDir.mkdir(); }
				
				//On écrit le fichier contenant la liste des circuits.
				BufferedWriter writer = new BufferedWriter(new FileWriter(new File("data/defaultList.gameGrid")));
				writer.write("");
				writer.close();
				
            } catch (Exception ex) { exc.printStackTrace(); }
		}
		
		raceList.put(LanguageManager.grabStringFromID("defaultRace"), defaultRace);
		//networkList.put(LanguageManager.grabStringFromID("defaultIndiv"), NeuralRacing.defaultIndiv);
		
		
		/*
		 * On lit le contenu du fichier contenant les représentation
		 * de tous les individus générés par l'utilisateur.
		 * On stock ces représentations, et le nom associé à chacune, dans la liste prévue à cet effet.
		 */
		try {
			
			Scanner reader = new Scanner((new File("data/defaultList.neuralNetwork")));
			while (reader.hasNextLine()) { 
				line.setValue(reader.nextLine());
				if (line.getValue().contains(":")) {
					networkList.put(line.substring(0, line.lastIndexOf(':')).getValue(), line.substring(line.lastIndexOf(':')+1, line.getValue().length()).getValue());
				}
			}
			reader.close();
			
		} catch (Exception exc) {
			try {
				
				//Si le dossier data n'existe pas, on l'ajoute.
				File dataDir = new File("data");
				if (!dataDir.exists()) { dataDir.mkdir(); }
				
				//On écrit le fichier contenant la liste des circuits.
				BufferedWriter writer = new BufferedWriter(new FileWriter(new File("data/defaultList.neuralNetwork")));
				writer.write("");
				writer.close();
				
            } catch (Exception ex) {}
		}
		
		NeuralRacing.loadFont();
		
		menuView        = new MainMenuView(window.getSize());
		evolutionView   = new EvolutionView(window.getSize());
		experimentsView = new ExperimentsView(window.getSize());
		raceEditorView  = new RaceEditorView(window.getSize());
		optionsView     = new OptionsView(window.getSize());
		
		mainPanel.add(menuView);
		cardStack.addLayoutComponent(menuView, "menuPanel");
		mainPanel.add(evolutionView);
		cardStack.addLayoutComponent(evolutionView, "evolutionPanel");
		mainPanel.add(experimentsView);
		cardStack.addLayoutComponent(experimentsView, "experimentsPanel");
		mainPanel.add(raceEditorView);
		cardStack.addLayoutComponent(raceEditorView, "raceEditorPanel");
		mainPanel.add(optionsView);
		cardStack.addLayoutComponent(optionsView, "optionsPanel");
		
		//On lit les textes correspondant à la langue choisie.
		LanguageManager.loadLanguage(UserDefaults.language);
		
		window.setBackground(Color.black);
		window.setContentPane(mainPanel);
		window.setVisible(true);
		window.setSize(901, 661 + window.getInsets().top);
		try {
			Thread.sleep(1000L);
		} catch (InterruptedException exc) {}
		
		//On lance l'animation d'apparition du menu.
		menuView.animate();
	}
	
	/**
	 * Cette méthode permet d'afficher l'onglet voulu, en utilisant le <code>CardLayout</code>
	 * prinicipal. Si nécessaire, l'onglet ouvert sera rafraîchi.
	 * 
	 * @param identifier
	 * L'identifiant de l'onglet, tel qu'utilisé pour l'ajouter au <code>CardLayout</code>.
	 * 
	 * @see CardLayout
	 */
	public static void showView(String identifier) {
		
		//Si l'utilisateur revient dans le menu des options, on met à jour ses tables de données.
		if (identifier.equals("optionsPanel")) {
			optionsView.keySet = optionsView.leftSelected ? raceList.keySet().toArray() : networkList.keySet().toArray();
			((AbstractTableModel) optionsView.dataTable.getModel()).fireTableDataChanged();
		}
		
		cardStack.show(mainPanel, identifier);
	}
	
	/**Cette méthode permet de sauvegarder la liste des circuits dans le fichier dédié.*/
	public static void saveRaces() {
		String rpz = "";
		
		//On concatène toutes les représentations.
		for (Map.Entry<String, String> entry : raceList.entrySet()) {
			//On ne sauve pas le circuit par défaut pour ne pas le dupliquer.
			if (entry.getKey().equals(LanguageManager.grabStringFromID("defaultRace"))) { continue; }
			//On sépare la clé et la représentation par le caractère ":"
		    rpz += entry.getKey() + ":" + entry.getValue() + "\n";
		}
		try {
			BufferedWriter writer = new BufferedWriter( new FileWriter("data/defaultList.gameGrid"));
			writer.write(rpz);
			writer.close();
        } catch (IOException exc) {
        	JOptionPane.showMessageDialog(mainPanel, LanguageManager.grabStringFromID("raceSaveError"), LanguageManager.grabStringFromID("error"), JOptionPane.ERROR_MESSAGE);
        }
	}
	
	/**Cette méthode permet de sauvegarder la liste des individus dans le fichier dédié.*/
	public static void saveNetworks() {
		String rpz = "";

		//On concatène toutes les représentations.
		for (Map.Entry<String, String> entry : networkList.entrySet()) {
			//On ne sauve pas le circuit par défaut pour ne pas le dupliquer.
			if (entry.getKey().equals(LanguageManager.grabStringFromID("defaultIndiv"))) { continue; }
			//On sépare la clé et la représentation par le caractère ":"
		    rpz += entry.getKey() + ":" + entry.getValue() + "\n";
		}
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter("data/defaultList.neuralNetwork"));
			writer.write(rpz);
			writer.close();
        } catch (IOException exc) {
        	JOptionPane.showMessageDialog(mainPanel, LanguageManager.grabStringFromID("indivSaveError"), LanguageManager.grabStringFromID("error"), JOptionPane.ERROR_MESSAGE);
        }
	}
	
	/**
	 * Cette fonction récupère la police Avenir Next si elle n'existe pas déjà dans le système.
	 */
	private static void loadFont() {
		
		GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		
		//if ((new ArrayList<String>(Arrays.asList(graphicsEnvironment.getAvailableFontFamilyNames()))).contains("Avenir Next")) { return; }
		
	    try {
	        File fontFile = new File("fonts/AvenirNext-Bold.ttf");
	        System.out.println(fontFile.exists());
	        Font font = Font.createFont(Font.TRUETYPE_FONT, fontFile);
	        //graphicsEnvironment.registerFont(font);
	    } catch (Exception exception) {
	        JOptionPane.showMessageDialog(null, exception.getMessage());
	    }
	}
	
	/**
	 * Cette méthode permet d'obtenir une police commune dans tout le logiciel.
	 * 
	 * @param type
	 * Le type de la police.
	 * @param size
	 * La taille de la police
	 * 
	 * @return
	 * La police obtenue.
	 * 
	 * @see Font
	 */
	public static Font getSystemFont(int type, int size) {
		return new Font("Avenir Next", type, size);
	}
	
	/**
	 * Cette méthode permet d'obtenir un objet <code>BufferedImage</code> à partir d'un objet <code>Icon</code>,
	 * ce qui s'avère particulièrement utile pour appliquer des <code>AffineTransform</code> à des icônes.
	 * 
	 * @param image
	 * L'icône à convertir.
	 * @return
	 * L'image obtenue.
	 * 
	 * @see BufferedImage
	 * @see Icon
	 * @see AffineTransform
	 */
	public static BufferedImage getImageFromIcon(Icon image) {
		//On récupère la BufferedImage sur laquelle on va peindre l'icône.
		BufferedImage contentImage = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(image.getIconWidth(), image.getIconHeight(), Transparency.TRANSLUCENT);
		//On peint l'icône sur son objet Graphics.
		Graphics g = contentImage.createGraphics();
		image.paintIcon(null, g, 0, 0);
		//Relâche le graphics pour libérer les ressources qu'il occupe.
		g.dispose();
		return contentImage;
	}
	
	/**
	 * 
	 * Cette sous-classe de <code>JPanel</code> permet simplement la réception
	 * des évènements envoyés par la <code>ActionMap</code> du panneau principal.
	 * 
	 * @author Ronan
	 * @see JPanel
	 * @see NeuralRacing
	 */
	public static class UIPanel extends JPanel { private static final long serialVersionUID = 75645321L; public void keyActionDetected(String keyPressed) {} }
	
	/**
	 * Cette sous-classe de <code>JPanel</code> permet d'avoir des coins arrondis.
	 * 
	 * @author Ronan
	 * @see JPanel
	 * @see NeuralRacing
	 *
	 */
	static class UIView extends UIPanel {

		private static final long serialVersionUID = 564323557643312L;
		
		/**Le rayon de l'arc de cercle dessiné aux coins de la vue.*/
		int cornerRadius = 20;
		/**La couleur de fond par défaut de la vue.*/
		Color regularColor;
		/**
		 * Cette sous-classe de <code>JPanel</code> permet d'avoir des coins arrondis.
		 * 
		 * @param dimension
		 * La taille de la vue.
		 * @param componentColor
		 * La couleur de fond par défaut de la vue.
		 * @param cornerRadius
		 * Le rayon de l'arc de cercle dessiné aux coins de la vue.
		 */
		public UIView(Dimension dimension, Color componentColor, int cornerRadius) {
			this.setSize(dimension);
			this.setPreferredSize(dimension);
			this.setOpaque(false);
			this.setBackground(componentColor);
			this.cornerRadius     = cornerRadius;
			this.regularColor     = componentColor;
		}
		
		@Override public void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2d = (Graphics2D) g;
			
			//On active l'anti-alliasing pour éviter que les coins manquent de détail.
			RenderingHints qualityHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
			qualityHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY );
			g2d.setRenderingHints(qualityHints);
			
			g2d.setColor(getBackground());
			g2d.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
		}
	}
	
	/**
	 * 
	 * Cette sous-classe de <code>UIPanel</code> permet d'avoir une barre de progression.
	 * Elle possède des coins arrondis, et est séparée en deux parties, délimitées visuellement
	 * par deux couleurs distinctes : le progrès actuel, et la couleur de fond.
	 * 
	 * @author Ronan
	 * @see UIPanel
	 * @see NeuralRacing
	 */
	static class UIProgressBar extends UIView {
		
		private static final long serialVersionUID = 1460481243390254118L;
		
		/**Le pourcentage actuel de progrès.*/
		int progress = 0;
		
		/**La couleur du progrès actuel.*/
		Color progressColor;
		
		/**
		 * Cette sous-classe de <code>UIPanel</code> permet d'avoir une barre de progression.
		 * Elle possède des coins arrondis, et est séparée en deux parties, délimitées visuellement
		 * par deux couleurs distinctes : le progrès actuel, et la couleur de fond.
		 * 
		 * @param dimension
		 * La taille du composant.
		 * @param componentColor
		 * La couleur de fond.
		 * @param progressColor
		 * La couleur du progrès actuel.
		 * @param cornerRadius
		 * Le rayon de l'arc de cercle dessiné aux coins de la vue.
		 * @param initialProgress
		 * Le progrès initial.
		 * 
		 * @see UIPanel
		 * 
		 */
		public UIProgressBar(Dimension dimension, Color componentColor, Color progressColor, int cornerRadius, int initialProgress) {
			super(dimension, componentColor, cornerRadius);
			this.progress      = initialProgress;
			this.progressColor = progressColor;
		}

		@Override public void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.setColor(progressColor);
			g.fillRoundRect(0, 0, this.getWidth()*progress/100, this.getHeight(), this.cornerRadius, this.cornerRadius);
		}
		
		/**
		 * Cette méthode permet de changer la valeur du progrès.
		 * 
		 * @param timeLeft
		 * La nouvelle valeur du progrès.
		 */
		public void updateValue(int timeLeft) {
			this.progress = timeLeft;
			this.repaint();
		}
		
	}
	
	/**
	 * Cette sous-classe de <code>UIPanel</code> permet d'afficher du texte, et éventuellement une icône.
	 * 
	 * @author Ronan
	 * @see UIPanel
	 * @see NeuralRacing
	 */
	static class UILabel extends UIView implements LanguageManager.LanguageMutable {

		private static final long serialVersionUID = 7654356534245L;
		
		/**Le texte affiché.*/
		String title;
		/**L'icône affichée.*/
		BufferedImage icon;
		
		/**Ce <code>Runnable</code> permet d'adapter le texte affiché au langage choisi.*/
		private Runnable mutator;
		
		/**
		 * 
		 * Cette sous-classe de <code>UIPanel</code> permet d'afficher du texte, et éventuellement une icône.
		 * 
		 * @param dimension
		 * La taille du composant.
		 * @param title
		 * Le texte à afficher.
		 * @param componentColor
		 * La couleur de fond.
		 * @param cornerRadius
		 * Le rayon de l'arc de cercle dessiné aux coins de la vue.
		 * @param mutator
		 * Ce <code>Runnable</code> permet d'adapter le texte affiché au langage choisi.
		 * 
		 * @see UIPanel
		 * 
		 */
		public UILabel(Dimension dimension, Color componentColor, int cornerRadius, Runnable mutator) {
			super(dimension, componentColor, cornerRadius);
			this.mutator = mutator;
			
			if (this.mutator != null) { LanguageManager.registerUIElement(this); }
		}
		
		@Override public void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2d = (Graphics2D) g;
			
			g2d.setColor(getForeground());
			g2d.setFont(getFont());
			
			//Si on ne doit afficher qu'une icône, il suffit de la centrer, sans autre soucis.
			if (title == null && icon != null) {
				g2d.drawImage(icon, (getWidth()-icon.getWidth())/2, (getHeight()-icon.getHeight())/2, icon.getWidth(), icon.getHeight(), null);
				return;
			}
			
			//On récupère la dimension nécessaire à l'affichage du texte.
            Rectangle2D textBounds = getFont().getStringBounds(title, g2d.getFontRenderContext());
            
            //Si on affiche aussi une image, on décalera le texte vers la droite d'une distance égale à
            //la largeur de l'image.
            //Le padding permet de donner un écart entre l'image et le texte.
            int xOffset = icon == null ? 0 : icon.getWidth(),
            	padding = xOffset == 0 ? 0 : 10;
            
            int x = (int) ((getWidth() - textBounds.getWidth() + xOffset + padding)  / 2d - textBounds.getX()),
                y = (int) ((getHeight() - textBounds.getHeight()) / 2.0 - textBounds.getY());
            g2d.drawString(title, x, y);
            
            if (icon != null) {
            	int width  = icon.getWidth(),
            		height = icon.getHeight();
            	g2d.drawImage(icon, x - padding/2 - xOffset/2 - width/2 - 10, y - 2 - height/2 + (int) textBounds.getCenterY(), width, height, null);
            }
		}
		
		/**
		 * Cette méthode permet de donner une icône au label.
		 * @param icon L'icône à afficher.
		 */
		public void setIcon(BufferedImage icon) {
			this.icon = icon;
		}
		
		@Override public void updateData() {
			mutator.run();
			if (title == null) { title = ""; }
			repaint();
		}
		
	}
	
	/**
	 * Cette sous-classe de <code>UILabel</code> permet de gérer les interactions avec un utilisateur,
	 * à travers les interfaces <code>MouseListener</code> et <code>ActionListener</code>.
	 * 
	 * @author Ronan
	 * @see UILabel
	 * @see NeuralRacing
	 * 
	 */
	static class UIButton extends UILabel implements MouseListener {

		private static final long serialVersionUID = 3274432154687543L;
		
		/**Les <code>ActionListeners</code> du bouton.*/
		private List<ActionListener> actionListeners = new ArrayList<ActionListener>();
		
		private boolean mouseEntered = false,
						mousePressed = false,
						isEnabled    = true;
		
		private Color highlightedColor, pressedColor;
		
		/**Si ce booléen est vrai, le bouton prend en charge le surlignage lorsque la souris passe au-dessus.*/
		boolean highlight = true;
		
		/**
		 * Cette sous-classe de <code>UILabel</code> permet de gérer les interactions avec un utilisateur,
		 * à travers les interfaces <code>MouseListener</code> et <code>ActionListener</code>.
		 * 
		 * @param dimension
		 * La taille du composant.
		 * @param componentColor
		 * La couleur de fond.
		 * @param cornerRadius
		 * Le rayon de l'arc de cercle dessiné aux coins de la vue.
		 * @param mutator
		 * Ce <code>Runnable</code> permet d'adapter le texte affiché au langage choisi.
		 * 
		 * @see UILabel
		 */
		public UIButton(Dimension dimension, Color componentColor, int cornerRadius, Runnable mutator) {
			super(dimension, componentColor, cornerRadius, mutator);
			this.addMouseListener(this);
			this.highlightedColor = componentColor.brighter();
			this.pressedColor     = componentColor.darker();
		}
		
		/**
		 * Cette méthode permet d'ajouter un <code>ActionListener</code> au bouton.
		 * 
		 * @param listener
		 * Le <code>ActionListener</code> en question.
		 */
		public void addActionListener(ActionListener listener) {
			actionListeners.add(listener);
		}
		
		/**
		 * Cette méthode permet de changer la couleur de fond du bouton.
		 * 
		 * @param componentColor
		 * La couleur à appliquer.
		 */
		public void switchBackground(Color componentColor) { 
			super.setBackground(componentColor);
			this.regularColor     = componentColor;
			this.highlightedColor = componentColor.brighter();
			this.pressedColor     = componentColor.darker();
		}
		
		/**
		 * Cette méthode permet de simuler un clic sur le bouton.
		 */
		public void doClick() {
			if (!isEnabled) { return; }
			for (ActionListener iterator: actionListeners) {
				iterator.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "PRESSED"));
			}
		}
		
		/**
		 * Cette méthode permet de définir le fait que le bouton soit utilisable ou non par l'utilisateur.
		 */
		@Override public void setEnabled(boolean enabled) {
			this.isEnabled = enabled;
			if (mousePressed)      { this.setBackground(pressedColor); }
			else if (mouseEntered) { this.setBackground(highlightedColor); }
		}
		
		@Override public void mouseReleased(MouseEvent e) {
			mousePressed = false;
			if (mouseEntered) { doClick(); }
			if (highlight)    { this.setBackground(mouseEntered && isEnabled ? highlightedColor : regularColor); };
		}
		
		@Override public void mousePressed(MouseEvent e) {
			mousePressed = true;
			if (!isEnabled || !highlight) { return; };
			this.setBackground(pressedColor);
		}
		
		@Override public void mouseEntered(MouseEvent e) {
			mouseEntered = true;
			if (!isEnabled || !highlight) { return; };
			this.setBackground(mousePressed ? pressedColor : highlightedColor);
		}
		
		@Override public void mouseExited(MouseEvent e) {
			mouseEntered = false;
			if (!isEnabled || !highlight) { return; };
			this.setBackground(regularColor);
		}
		
		@Override public void mouseClicked(MouseEvent e) {}
	}
	
	/**
	 * Cette sous-classe de <code>UIView</code> permet à l'utilisateur de choisir
	 * entre deux options, représentées par des images.
	 * 
	 * @author Ronan
	 * @see UIView
	 * @see NeuralRacing
	 * 
	 */
	static class UISelector extends UIView implements MouseListener {
		
		private static final long serialVersionUID = -4327505001028315781L;
		
		/**Le <code>ActionListener</code> du sélecteur.*/
		ActionListener choiceListener;
		
		/**Une des images représentant les choix de l'utilisateur.*/
		BufferedImage leftChoice,
					  rightChoice;
		
		/**L'index de la sélection.*/
		int selected = 0;
		
		/**La couleur de la partie non sélectionnée.*/
		Color passiveColor;
		
		boolean mouseEntered = false;
		
		/**
		 * Cette sous-classe de <code>UIView</code> permet à l'utilisateur de choisir
		 * entre deux options, représentées par des images.
		 * 
		 * @param size
		 * La taille de la vue.
		 * @param background
		 * La couleur de la partie sélectionnée de la vue.
		 * @param passiveColor
		 * La couleur de la partie non sélectionnée de la vue.
		 * @param cornerRadius
		 * Le rayon de l'arc de cercle dessiné aux coins de la vue.
		 * @param listener
		 * Le <code>ActionListener</code> qui sera notifié lors d'un clic sur le sélecteur.
		 * @param leftChoice
		 * L'icône du choix de gauche.
		 * @param rightChoice
		 * L'icône du choix de droite.
		 * 
		 * @see UIView
		 * @see ActionListener
		 */
		public UISelector(Dimension size, Color background, Color passiveColor, int cornerRadius, ActionListener listener, BufferedImage leftChoice, BufferedImage rightChoice) {
			super(size, background, cornerRadius);
			this.choiceListener = listener;
			this.leftChoice     = leftChoice;
			this.rightChoice    = rightChoice;
			this.passiveColor   = passiveColor;
			this.addMouseListener(this);
		}
		
		@Override public void paintComponent(Graphics g) {
			super.paintComponent(g);
			
			//On peint la partie non selectionnée.
			g.setColor(selected == 0 ? passiveColor : regularColor);
			g.fillRoundRect(this.getWidth()/2, 0, this.getWidth()/2, this.getHeight(), this.cornerRadius, this.cornerRadius);
			g.fillRect(this.getWidth()/2, 0, cornerRadius, this.getHeight());
			
			//On peint les deux images.
			g.drawImage(leftChoice, (getWidth()/2-leftChoice.getWidth())/2, (getHeight()-leftChoice.getHeight())/2, leftChoice.getWidth(), leftChoice.getHeight(), null);
			g.drawImage(rightChoice, getWidth()/2 + (getWidth()/2-rightChoice.getWidth())/2, (getHeight()-rightChoice.getHeight())/2, rightChoice.getWidth(), rightChoice.getHeight(), null);
		}
		
		@Override public void mouseReleased(MouseEvent e) {
			
			if (!mouseEntered) { return; }
			
			if (e.getX() < getWidth()/2) {
				
				if (selected == 0) { return; }
				selected = 0;
				setBackground(regularColor);
				
			} else {
				
				if (selected == 1) { return; }
				selected = 1;
				setBackground(passiveColor);
				
			}
			
			if (choiceListener != null) { choiceListener.actionPerformed(new ActionEvent(this, selected, null)); }
			
			repaint();
		}
		
		@Override public void mouseEntered(MouseEvent e) { mouseEntered = true; }
		@Override public void mouseExited(MouseEvent e)  { mouseEntered = false; }
		
		@Override public void mouseClicked(MouseEvent e) {}
		@Override public void mousePressed(MouseEvent e) {}
	}
	
	/**
	 * Cette sous-classe de <code>UIView</code> permet d'afficher une vue contenant
	 * un <code>GraphPanel</code> dont les données sont contrôlables, doté d'un titre pour chaque axe.
	 * Les valeurs maximum sur chacun des axes sont également affichées, ainsi que l'origine du repère.
	 * Un bouton permet à l'utilisateur de faire apparaître une fenêtre présentant l'ensemble des données
	 * que le <code>GraphPanel</code> affiche.
	 * 
	 * @author Ronan
	 * @see UIView
	 * @see GraphPanel
	 * @see NeuralRacing
	 */
	static class UIGraphView extends UIView implements LanguageManager.LanguageMutable {
		
		private static final long serialVersionUID = -45347477049866707L;
		
		/**La vue permettant d'afficher un graphique.*/
		GraphPanel graph;
		
		/**Le layout permettant d'agencer les composants de la <code>UIGraphView</code>.*/
		SpringLayout layout = new SpringLayout();
		
		/**Le label montrant l'origine du repère.*/
		JLabel origin = new JLabel("0");
		/**Le label montrant la valeur maximum en abcisses.*/
		JLabel maxX   = new JLabel("-");
		/**Le label montrant la valeur maximum en ordonnées.*/
		JLabel maxY   = new JLabel("-");
		/**Le titre de l'axe des abcisses.*/
		JLabel xAxis  = new JLabel();
		
		/**Le titre de l'axe des ordonnées.*/
		String yTitle = "";
		/**Le panneau permettant d'afficher le titre de l'axe des ordonnées, écrit à la verticale.*/
		JPanel yAxis;
		
		/**Ce <code>Runnable</code> permet d'adapter le texte affiché au langage choisi.*/
		Runnable mutator;
		
		/**
		 * Cette sous-classe de <code>UIView</code> permet d'afficher une vue contenant
		 * un <code>GraphPanel</code> dont les données sont contrôlables, doté d'un titre pour chaque axe.
	 	 * Les valeurs maximum sur chacun des axes sont également affichées, ainsi que l'origine du repère.
	 	 * Un bouton permet à l'utilisateur de faire apparaître une fenêtre présentant l'ensemble des données
	 	 * que le <code>GraphPanel</code> affiche.
		 * 
		 * @param size
		 * La taille de la vue.
		 * @param backgroundColor
		 * La couleur de fond de la vue.
		 * @param lineColor
		 * La couleur de trait du graphique.
		 * @param cornerRadius
		 * Le rayon de l'arc de cercle dessiné aux coins de la vue.
		 * @param yTitle
		 * Le titre sur l'axe des ordonnées.
		 * @param xTitle
		 * Le titre sur l'axe des abcisses.
		 * @param ratio
		 * Le ratio à utiliser entre la taille du graphique et la taille de la vue.
		 * 
		 * @see UIView
		 * @see GraphPanel
		 */
		public UIGraphView(Dimension size, Color backgroundColor, Color lineColor, int cornerRadius, double ratio, Runnable mutator) {
			
			super(size, backgroundColor, cornerRadius);
			this.mutator = mutator;
			this.graph = new GraphPanel(new Dimension((int) (ratio*this.getWidth()), (int) (ratio*this.getHeight())));
			this.graph.setBackground(backgroundColor);
			this.graph.lineColor = lineColor;
			setForeground(Color.white);
			this.setLayout(layout);
			
			//On instancie le panneau sur lequel on écrira le titre
			//de l'axe des ordonnées, dans le sens vertical (de bas en haut).
			yAxis = new JPanel() {
				
				private static final long serialVersionUID = 87654321345677801L;

				@Override public void paintComponent(Graphics g) {
					super.paintComponent(g);
					Graphics2D g2d = (Graphics2D) g;
					Font rotatedFont = g2d.getFont().deriveFont(AffineTransform.getRotateInstance(-Math.PI/2));
					g2d.setFont(rotatedFont);
					Rectangle2D textBounds = g2d.getFont().getStringBounds(yTitle, g2d.getFontRenderContext());
					
					//On calcule la position du texte sur la vue.
		            int x = (int) ((getWidth()  + textBounds.getHeight()) / 2d - textBounds.getX()),
		                y = (int) ((getHeight() + textBounds.getWidth())  / 2d + textBounds.getY()/8d);
		            g2d.drawString(yTitle, x, y);
				}
			};
			
			yAxis.setPreferredSize(new Dimension(20, 50));
			yAxis.setBackground(backgroundColor);
			
			this.add(graph);
			layout.putConstraint(SpringLayout.VERTICAL_CENTER, graph, 0, SpringLayout.VERTICAL_CENTER, this);
			layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, graph, 0, SpringLayout.HORIZONTAL_CENTER, this);
			
			this.add(origin);
			origin.setForeground(getForeground());
			layout.putConstraint(SpringLayout.WEST, origin, 5, SpringLayout.WEST, this);
			layout.putConstraint(SpringLayout.SOUTH, origin, -5, SpringLayout.SOUTH, this);
			
			this.add(xAxis);
			xAxis.setForeground(getForeground());
			layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, xAxis, 0, SpringLayout.HORIZONTAL_CENTER, this);
			layout.putConstraint(SpringLayout.SOUTH, xAxis, -5, SpringLayout.SOUTH, this);
			
			this.add(yAxis);
			yAxis.setForeground(getForeground());
			layout.putConstraint(SpringLayout.WEST, yAxis, 0, SpringLayout.WEST, this);
			layout.putConstraint(SpringLayout.VERTICAL_CENTER, yAxis, 0, SpringLayout.VERTICAL_CENTER, this);
			
			this.add(maxX);
			maxX.setForeground(getForeground());
			layout.putConstraint(SpringLayout.EAST, maxX, -5, SpringLayout.EAST, this);
			layout.putConstraint(SpringLayout.SOUTH, maxX, -5, SpringLayout.SOUTH, this);
			
			this.add(maxY);
			maxY.setForeground(getForeground());
			layout.putConstraint(SpringLayout.WEST, maxY, 5, SpringLayout.WEST, this);
			layout.putConstraint(SpringLayout.NORTH, maxY, 5, SpringLayout.NORTH, this);
			
			LanguageManager.registerUIElement(this);
		}
		
		/**
		 * Cette méthode permet de régler la police de tous
		 * les <code>JLabels</code> du graphique.
		 * 
		 * @param font
		 * La police en question.
		 * 
		 * @see JLabel
		 */
		public void updateFont(Font font) {
			this.origin.setFont(font);
			this.xAxis.setFont(font);
			this.yAxis.setFont(font);
			this.maxX.setFont(font);
			this.maxY.setFont(font);
		}
		
		/**
		 * Cette méthode permet de donner l'ensemble des points à
		 * afficher dans le graphique.
		 * 
		 * @param data
		 * L'ensemble des points.
		 */
		public void setPoints(Map<Integer, Double> data) {
			this.graph.setIgnoreRepaint(true);
			
			graph.data.clear();
			graph.data.putAll(data);
			graph.getMax();
			
			this.graph.setIgnoreRepaint(false);
			
			this.maxX.setText("" + this.graph.maxX);
			this.maxY.setText("" + this.graph.maxY);
			repaint();
		}
		
		/**
		 * Cette méthode permet d'ajouter un point au graphique.
		 * 
		 * @param x
		 * L'abcisse du nouveau point.
		 * @param y
		 * L'ordonnée du nouveau point.
		 */
		public void addPoint(int x, double y) {
			this.graph.setIgnoreRepaint(true);
			
			this.graph.data.put(x, y);
			this.graph.getMax();
			
			this.graph.setIgnoreRepaint(false);
			
			this.maxX.setText("" + this.graph.maxX);
			this.maxY.setText("" + this.graph.maxY);
			repaint();
		}
		
		/**
		 * Cette méthode permet d'afficher une fenêtre afin de
		 * présenter à l'utilisateur l'ensemble des données du graphique.
		 */
		public void showDataTable() {
			
			JFrame frame = new JFrame(LanguageManager.grabStringFromID("data"));
			frame.setSize(400, 500);
			frame.setResizable(false);
			frame.setLocationRelativeTo(null);
			
			Object [] keySet   = graph.data.keySet().toArray(),
					  valueSet = graph.data.values().toArray();
			
			JTable table = new JTable(new AbstractTableModel() {
				private static final long serialVersionUID = 165432124242323L;
				public int getColumnCount() { return 2; }
				public int getRowCount() { return graph.data.size() + 1;}
		        public Object getValueAt(int row, int col) { return row == 0 ? (col == 0 ? xAxis.getText() : yTitle) : (col == 0 ? keySet[row-1] : valueSet[row-1]); }
			});
			
			table.setFont(getSystemFont(Font.BOLD, 16));
			table.setForeground(Color.WHITE);
			table.setGridColor(Color.lightGray);
			table.setBackground(NeuralRacing.darkGrayColor);
			table.setRowHeight(30);
			table.setTableHeader(null);
			
			JScrollPane scrollView = new JScrollPane(table);
			scrollView.setBackground(NeuralRacing.darkGrayColor);
			scrollView.getViewport().setBackground(NeuralRacing.darkGrayColor);
			scrollView.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			scrollView.getVerticalScrollBar().setUnitIncrement(8);
			
			frame.setContentPane(scrollView);
			frame.setVisible(true);
			
		}
		
		/**
		 * Cette méthode permet de nettoyer le graphique.
		 */
		public void clear() {
			this.graph.setIgnoreRepaint(true);
			
			this.graph.data.clear();
			
			this.graph.setIgnoreRepaint(false);
			
			this.maxX.setText("-");
			this.maxY.setText("-");
			repaint();
		}

		@Override public void updateData() {
			mutator.run();
			repaint();
		}
		
	}
	
	/**
	 * Cette sous-classe de <code>JPanel</code> permet d'afficher un graphique
	 * dont les données sont contrôlables. Visuellement, il consiste en un ensemble
	 * de segments de droite reliant un à un tous les points contenus dans les données.
	 * 
	 * @author Ronan
	 * @see JPanel
	 * @see NeuralRacing
	 */
	static class GraphPanel extends JPanel {
		
		private static final long serialVersionUID = 98765436543234221L;
		
		/**L'ensemble des données du graphique, sous forme de paires <strong>(x; y)</strong>. L'utilisation
		 * d'une <code>TreeMap</code> permet de garder tous les points dans l'ordre croissant des <strong>x</strong>.
		 * @see TreeMap
		 */
		Map<Integer, Double> data = new TreeMap<Integer, Double>();
		
		/**La couleur des lignes du graphique.*/
		Color lineColor;
		
		/**La valeur maximale de l'axe concerné.*/
		int maxX, maxY;
		
		/**Si ce booléen est vrai, alors les données en <strong>y</strong> sont affichées sous forme de pourcentage.*/
		boolean scaleAsPercentage = false;
		
		/**
		 * Cette sous-classe de <code>JPanel</code> permet d'afficher un graphique
		 * dont les données sont contrôlables. Visuellement, il consiste en un ensemble
		 * de segments de droite reliant un à un tous les points contenus dans les données.
		 * 
		 * @param dimension
		 * La taille de la vue.
		 * 
		 * @see JPanel
		 */
		public GraphPanel(Dimension dimension) {
			this.setSize(dimension);
			this.setPreferredSize(dimension);
		}
		
		
		/**
		 * Cette méthode permet aux propriétés {@link #maxX} et {@link #maxY} de se
		 * voir attribuer leur valeur, en itérant dans les données du graphique.
		 */
		private void getMax() {
			
			int max = 0;
			for (int number: data.keySet()) {
				if (number > max) { max = number; }
			}
			maxX = max;
			
			if (scaleAsPercentage) {
				maxY = 100;
				return;
			}
			
			max = 0;
			for (double number: data.values()) {
				if (number > max) { max = (int) number; }
			}
			maxY = max;
		}
		
		@Override public void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2d = (Graphics2D) g;
			
			//On met en place l'antia-alliasing, pour éviter que les lignes ne soient trop pixelisées.
			RenderingHints qualityHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			qualityHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g2d.setRenderingHints(qualityHints);
			
			//On a besoin d'au-moins deux points pour tracer nos droites.
			if (data.size() < 2) {
				g2d.setColor(lineColor);
				g2d.setFont(getSystemFont(Font.BOLD, 12));
	            Rectangle2D textBounds = getFont().getStringBounds(LanguageManager.grabStringFromID("waiting"), g2d.getFontRenderContext());
	            
	            int x = (int) ((getWidth() - textBounds.getWidth())  / 2d - textBounds.getX()),
	                y = (int) ((getHeight() - textBounds.getHeight()) / 2.0 - textBounds.getY());
	            g2d.drawString(LanguageManager.grabStringFromID("waiting"), x, y);
				return;
			}
			
			double xUnit = this.getPreferredSize().getWidth()/maxX,
				   yUnit = this.getPreferredSize().getHeight()/maxY;
			
			Object [] keySet   = data.keySet().toArray(),
					  valueSet = data.values().toArray();
			
			g2d.setColor(lineColor);
			g2d.setStroke(new BasicStroke(2));
			for (int index = 0; index < keySet.length-1; index++) {
				int x1 = (int) (xUnit * (int) keySet[index]),
					y1 = (int) (this.getPreferredSize().getHeight()-(yUnit * (Double) valueSet[index])),
					x2 = (int) (xUnit * (int) keySet[index+1]),
					y2 = (int) (this.getPreferredSize().getHeight()-(yUnit * (Double) valueSet[index+1]));
				g2d.drawLine(x1, y1, x2, y2);
			}
		}
		
	}
	
	/**
	 * Cette sous-classe de <code>UIView</code> consiste en une vue contenant
	 * un titre et un <code>UIButton</code>, agencés verticalement. Le bouton contrôle
	 * un booléen, et change de titre (soit "Oui", soit "Non") et de couleur, en fonction de
	 * sa valeur actuelle, à chaque appui de l'utilisateur.
	 * Un <code>ChangeListener</code> permet au programme d'être notifié à chaque appui sur le bouton.
	 * 
	 * @author Ronan
	 * 
	 * @see UIView
	 * @see UIButton
	 * @see ChangeListener
	 * @see NeuralRacing
	 */
	static class UIParameterToggle extends UIView implements LanguageManager.LanguageMutable {
		
		private static final long serialVersionUID = 94767336745429L;
		
		/**Le layout permettant d'agencer les composants du <code>UIParameterToggle</code>.*/
		SpringLayout layout = new SpringLayout();
		
		/**Le bouton qui agit comme un interrupteur.*/
		UIButton toggle;
		
		/**Le titre de la vue.*/
		JLabel   label;
		
		boolean defaultValue,
				currentValue;
		
		/**Ce <code>Runnable</code> permet d'adapter le texte affiché au langage choisi.*/
		private Runnable mutator;
		
		/**Ce <code>Listener</code> permet au programme d'être notifié à chaque appui sur le bouton.*/
		ChangeListener listener;
		
		/**
		 * Cette sous-classe de <code>UIView</code> consiste en une vue contenant
		 * un titre et un <code>UIButton</code>, agencés verticalement. Le bouton contrôle
		 * un booléen, et change de titre (soit "Oui", soit "Non") et de couleur, en fonction de
	 	 * sa valeur actuelle, à chaque appui de l'utilisateur.
	 	 * Un <code>ChangeListener</code> permet au programme d'être notifié à chaque appui sur le bouton.
		 * 
		 * @param dimension
		 * La taille de la vue.
		 * @param componentColor
		 * La couleur de fond de la vue.
		 * @param cornerRadius
		 * Le rayon de l'arc de cercle dessiné aux coins de la vue.
		 * @param title
		 * Le titre à afficher.
		 * @param trueColor
		 * La couleur de fond du bouton dans son état "Vrai".
		 * @param falseColor
		 * La couleur de fond du bouton dans son état "Faux".
		 * @param value
		 * La valeur par défaut du bouton.
		 * 
		 * @see UIView
		 * @see UIButton
	 	 * @see ChangeListener
		 */
		public UIParameterToggle(Dimension dimension, Color componentColor, int cornerRadius, Color trueColor, Color falseColor, boolean value, Runnable mutator) {
			
			super(dimension, componentColor, cornerRadius);
			this.defaultValue = value;
			this.currentValue = value;
			this.setLayout(layout);
			this.mutator = mutator;
			
			label = new JLabel();
			label.setHorizontalAlignment(JLabel.CENTER);
			add(label);
			layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, label, 0, SpringLayout.HORIZONTAL_CENTER, this);
			layout.putConstraint(SpringLayout.NORTH, label, (int) (dimension.getHeight()/4), SpringLayout.NORTH, this);
			
			toggle = new UIButton(new Dimension((int) dimension.getWidth()-20, (int) dimension.getHeight()/4), defaultValue ? trueColor : falseColor, 10, () -> toggle.title = this.currentValue ? LanguageManager.grabStringFromID("yes") : LanguageManager.grabStringFromID("no"));
			LanguageManager.registerUIElement(getThis());
			
			toggle.addActionListener(new ActionListener() {
				@Override public void actionPerformed(ActionEvent evt) {
					currentValue = !currentValue;
					toggle.switchBackground(currentValue ? trueColor : falseColor);
					toggle.updateData();
					
					if (listener != null) {
						listener.stateChanged(new ChangeEvent(getThis()));
					}
					
					repaint();
				}
			});
			
			this.add(toggle);
			layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, toggle, 0, SpringLayout.HORIZONTAL_CENTER, this);
			layout.putConstraint(SpringLayout.SOUTH, toggle, (int) (-dimension.getHeight()/6), SpringLayout.SOUTH, this);
			
		}
		
		/**Ce méthode permet de réinitialiser le contenu du <code>UIParameterToggle</code>.*/
		public void clear() { if (currentValue != defaultValue) { toggle.doClick(); } }
		
		/**
		 * Cette méthode permet d'obtenir le <code>UIParameterToggle</code>.
		 * 
		 * @return
		 * Le <code>UIParameterToggle</code>.
		 **/
		private UIParameterToggle getThis() { return this; }

		@Override public void updateData() {
			mutator.run();
			toggle.updateData();
			repaint();
		}
	}
	
	/**
	 * Cette sous-classe de <code>UIView</code> consiste en une vue contenant
	 * un titre et un <code>JSlider</code>, agencés verticalement. Le slider permet
	 * à l'utilisateur de choisir une valeur numérique dans l'intervalle spécifié.
	 * A chaque interaction avec le slider, un <code>ChangeListener</code> est notifié,
	 * et le titre est modifié : il affiche un texte, suivi de la valeur actuelle, puis d'une
	 * éventuelle unité. Texte et unité sont spécifiés dans le constructeur.
	 * 
	 * @author Ronan
	 * 
	 * @see UIView
	 * @see JSlider
	 * @see ChangeListener
	 * @see NeuralRacing
	 */
	static class UIParameterSlider extends UIView implements LanguageManager.LanguageMutable {
		
		private static final long serialVersionUID = 76531311345L;
		
		/**Le layout permettant d'agencer les composants du <code>UIParameterSlider</code>.*/
		SpringLayout layout = new SpringLayout();
		
		/**Le slider permettant à l'utilisateur de choisir une valeur.*/
		JSlider slider = new JSlider();
		
		/**Le label qui permet d'afficher titre, valeur, et unité.*/
		JLabel label;
		
		/**Valeur utilisée pour l'affichage textuel.*/
		String title = "",
			   unit;
		
		/**La valeur par défaut du slider.*/
		int defaultValue;
		
		/**Ce <code>Runnable</code> permet d'adapter le texte affiché au langage choisi.*/
		Runnable mutator;
		
		/**
		 * Cette sous-classe de <code>UIView</code> consiste en une vue contenant
		 * un titre et un <code>JSlider</code>, agencés verticalement. Le slider permet
		 * à l'utilisateur de choisir une valeur numérique dans l'intervalle spécifié.
		 * A chaque interaction avec le slider, un <code>ChangeListener</code> est notifié,
		 * et le titre est modifié : il affiche un texte, suivi de la valeur actuelle, puis d'une
		 * éventuelle unité.
		 * 
		 * @param dimension
		 * La taille de la vue.
		 * @param componentColor
		 * La couleur de fond de la vue.
		 * @param trackColor
		 * La couleur de fond du slider.
		 * @param thumbColor
		 * La couleur de la poignée du slider.
		 * @param cornerRadius
		 * Le rayon de l'arc de cercle dessiné aux coins de la vue.
		 * @param title
		 * Le titre à afficher.
		 * @param unit
		 * L'unité à afficher.
		 * @param min
		 * La valeur minimum du slider.
		 * @param max
		 * La valeur maximum du slider.
		 * @param defaultValue
		 * La valeur par défaut du slider.
		 * 
		 * @see UIView
		 * @see JSlider
		 * @see ChangeListener
		 */
		public UIParameterSlider(Dimension dimension, Color componentColor, Color trackColor, Color thumbColor, int cornerRadius, String unit, int min, int max, int defaultValue, Runnable mutator) {
			super(dimension, componentColor, cornerRadius);
			
			this.setLayout(layout);
			
			this.mutator      = mutator;
			this.unit         = unit;
			this.defaultValue = defaultValue;
			
			label = new JLabel(title + defaultValue + unit);
			label.setHorizontalAlignment(JLabel.CENTER);
			add(label);
			layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, label, 0, SpringLayout.HORIZONTAL_CENTER, this);
			layout.putConstraint(SpringLayout.NORTH, label, (int) (dimension.getHeight()/4), SpringLayout.NORTH, this);
			
			slider.setValue(defaultValue);
			slider.setMinimum(min);
			slider.setMaximum(max);
			slider.setPreferredSize(new Dimension((int) dimension.getWidth() - 20, 10));
			slider.setOpaque(false);
			
			slider.setUI(new BasicSliderUI(slider) {
				
				@Override public void paint(Graphics g, JComponent c) {
			        Graphics2D g2d = (Graphics2D) g;
			        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			        super.paint(g, c);
			    }

			    @Override protected Dimension getThumbSize() {
			        return new Dimension(10, 10);
			    }

			    @Override public void paintTrack(Graphics g) {
			    	g.setColor(trackColor);
			        g.fillRoundRect(trackRect.x, trackRect.y, trackRect.width, trackRect.height, trackRect.height, trackRect.height);
			    }

			    @Override public void paintThumb(Graphics g) {
			    	g.setColor(thumbColor);
			        g.fillOval(thumbRect.x, thumbRect.y, thumbRect.width, thumbRect.height);
			    }
			    
			    @Override public void paintFocus(Graphics g) { return; }
			});
			
			this.add(slider);
			layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, slider, 0, SpringLayout.HORIZONTAL_CENTER, this);
			layout.putConstraint(SpringLayout.SOUTH, slider, (int) (-dimension.getHeight()/4), SpringLayout.SOUTH, this);
			
			slider.addChangeListener(new ChangeListener() {
				@Override public void stateChanged(ChangeEvent e) { label.setText(title + slider.getValue() + unit); }
			});
			
			LanguageManager.registerUIElement(this);
			
		}
		
		/**Ce méthode permet de réinitialiser le contenu du <code>UIParameterSlider</code>.*/
		public void clear() {
			label.setText(title + defaultValue + unit);
			slider.setValue(defaultValue);
			repaint();
		}

		@Override public void updateData() {
			mutator.run();
			label.setText(title + slider.getValue() + unit);
			repaint();
		}
	}
	
	/**
	 * Cette sous-classe de <code>UIView</code> permet d'afficher la représentation graphique
	 * d'un réseau de neurone à propagation avant. Chacun des neurones est représenté par un disque.
	 * Chacune des couches est représentée par une colonne de neurones espacés de façon égale.
	 * Les couches s'étalent une par une, à l'horizontale, espacées de façon égale. Les connexions
	 * sont représentées par des lignes, dont la couleur dépend du signe, et dont l'épaisseur est proportionelle
	 * à la valeur.
	 * 
	 * @author Ronan
	 * 
	 * @see UIView
	 * @see NeuralModel
	 * @see NeuralRacing
	 */
	static class UINeuralView extends UIView {
		
		private static final long serialVersionUID = -823471392199153945L;
		
		/**
		 * Si ce booléen est vrai, les connexions dont la valeur ne dépasse pas 0,5 sont considérées comme inexistantes.
		 * Dans Neural Racing, l'option est toujours activée, afin de rendre l'ensemble plus lisible.
		 */
		boolean shouldHideWeakConnections = true;
		
		/**Le réseau de neurones de la voiture.*/
		private NeuralModel.NeuralNetwork neuralNetwork;
		
		Color positive,
			  negative,
			  node;
		
		/**
		 * Cette sous-classe de <code>UIView</code> permet d'afficher la représentation graphique
		 * d'un réseau de neurone à propagation avant. Chacun des neurones est représenté par un disque.
		 * Chacune des couches est représentée par une colonne de neurones espacés de façon égale.
		 * Les couches s'étalent une par une, à l'horizontale, espacées de façon égale. Les connexions
		 * sont représentées par des lignes, dont la couleur dépend du signe, et dont l'épaisseur est proportionelle
		 * à la valeur.
		 * 
		 * @param size
		 * La taille de la vue.
		 * @param backgroundColor
		 * La couleur de fond de la vue.
		 * @param cornerRadius
		 * Le rayon de l'arc de cercle dessiné aux coins de la vue.
		 * @param positiveColor
		 * La couleur des connexions positives.
		 * @param negativeColor
		 * La couleur des connexions négatives.
		 * @param nodeColor
		 * La couleur des neurones.
		 * 
		 * @see UIView
		 * @see NeuralModel
		 */
		public UINeuralView(Dimension size, Color backgroundColor, int cornerRadius, Color positiveColor, Color negativeColor, Color nodeColor) {
			super(size, backgroundColor, cornerRadius);
			this.positive = positiveColor;
			this.negative = negativeColor;
			this.node     = nodeColor;
		}
		
		/**
		 * Cette méthode permet d'obtenir le réseau de neurones de la vue.
		 * 
		 * @return
		 * Le réseau de neurones de la vue.
		 */
		public NeuralModel.NeuralNetwork getNetwork() {
			return this.neuralNetwork;
		}
		
		/**
		 * Cette méthode permet de choisir le réseau de neurones de la vue.
		 * 
		 * @param network
		 * Le réseau de neurones à afficher.
		 */
		public void setNetwork(NeuralModel.NeuralNetwork network) {
			this.neuralNetwork = network;
			repaint();
		}
		
		public void paintComponent(Graphics g) {
			
			super.paintComponent(g);
			Graphics2D g2d = (Graphics2D) g;
			
			//Si on a pas de réseau, on affiche le messae "Attente" au centre.
			if (neuralNetwork == null) {
				g2d.setColor(positive);
				g2d.setFont(getSystemFont(Font.BOLD, 12));
	            Rectangle2D textBounds = getFont().getStringBounds(LanguageManager.grabStringFromID("waiting"), g2d.getFontRenderContext());
	            
	            int x = (int) ((getWidth() - textBounds.getWidth())  / 2d - textBounds.getX()),
	                y = (int) ((getHeight() - textBounds.getHeight()) / 2.0 - textBounds.getY());
	            g2d.drawString(LanguageManager.grabStringFromID("waiting"), x, y);
				return;
			}
			
			//On récupère l'espacement entre chacune des couches.
		    double spaceBetweenLayers = (double) this.getWidth()/ (double) (neuralNetwork.hiddenLayers.size() + 2);
		    
		    //L'espacement de la couche d'entrée avec la gauche de la vue, et de la couche de sortie avec la droite.
	        double halfCutWidth = spaceBetweenLayers/2;
	        
	        for (int layerIndex = 0; layerIndex < neuralNetwork.hiddenLayers.size() + 2; layerIndex++) {
			    
	        	int neuronsCount = layerIndex == 0 ? BrainStruct.numberOfInputNeurons : layerIndex == neuralNetwork.hiddenLayers.size() + 1 ? BrainStruct.numberOfOutputNeurons : BrainStruct.numberOfNeuronsPerHiddenLayer[layerIndex-1];
			    
	        	double spaceBetweenNeurons = this.getHeight()/(double) neuronsCount;
			    double halfCutHeight = spaceBetweenNeurons/2;
			    
			    int nextLayerCount = layerIndex < neuralNetwork.hiddenLayers.size() + 1 ? (layerIndex == neuralNetwork.hiddenLayers.size() ? BrainStruct.numberOfOutputNeurons : BrainStruct.numberOfNeuronsPerHiddenLayer[layerIndex]) : 0;
			    
			    double spaceBetweenNeuronsInNextLayer = this.getHeight()/(double) nextLayerCount;
			    double nextHalfCutHeight = spaceBetweenNeuronsInNextLayer/2;
			            
			    for (int neuronIndex = 0; neuronIndex < neuronsCount; neuronIndex++) {
			    	
			    	double neuronX = halfCutWidth  + (double) layerIndex  * spaceBetweenLayers;
			        double neuronY = halfCutHeight + (double) neuronIndex * spaceBetweenNeurons;
			        
			        //Les neurones de la couche de sortie n'ont pas de connexion.
			        if (layerIndex < neuralNetwork.hiddenLayers.size() + 1) {
			        	
			        	NeuralModel.Neuron currentNeuron = layerIndex == 0 ? neuralNetwork.inputLayer.neurons.get(neuronIndex) : neuralNetwork.hiddenLayers.get(layerIndex - 1).neurons.get(neuronIndex);
			        	
			        	for (int nextNeuronIndex = 0; nextNeuronIndex < nextLayerCount; nextNeuronIndex++) {
			        		
			        		double weight = currentNeuron.connections.get(nextNeuronIndex);
			        		
			        		weight = shouldHideWeakConnections ? (Math.abs(weight) < 0.5 ? 0 : weight) : weight;
			        		
			                if (weight < 0) {
	                            g2d.setColor(negative);
	                        } else {
	                        	g2d.setColor(positive);
	                        }
			                
			                int lineWidth = (int) Math.abs(weight * 5);
			                g2d.setStroke(new BasicStroke(lineWidth));
			                
			                Point2D.Double startPoint = new Point2D.Double(neuronX, neuronY),
			                			   endPoint   = new Point2D.Double(neuronX + spaceBetweenLayers, nextHalfCutHeight + (double) nextNeuronIndex * spaceBetweenNeuronsInNextLayer);
			                
			                //On dessine la connexion.
			                g2d.draw(new Line2D.Double(startPoint, endPoint));
			            }
			       }
			       
			        //On dessine le neurone.
			       g2d.setColor(node);
			       g2d.fillOval((int) neuronX-5, (int) neuronY-5, 10, 10);
			    }      			   
	        }
		}
	}
	
	/**
	 * Cette sous-classe de <code>AbstractAction</code> permet de prendre
	 * en charge le Key Binding, en faisant passer les informations des touches
	 * entrées par l'utilisateur à la vue d'expérimentations.
	 * 
	 * @author Ronan
	 *
	 * @see AbstractAction
	 * @see NeuralRacing
	 */
	private static class KeyListeningAction extends AbstractAction {
		
		private static final long serialVersionUID = -5590532635828016872L;
		
		public KeyListeningAction(String actionCommand) {
	         this.putValue(ACTION_COMMAND_KEY, actionCommand);
	    }
		
	    @Override public void actionPerformed(ActionEvent actionEvt) {
	    	experimentsView.keyActionDetected(actionEvt.getActionCommand());
	    }
		
	}
	
	/**
	 * Cette classe prend en charge la lecture et la sauvegarde de quatre
	 * booléens utiles à l'ensemble du programme :
	 * <p>
	 * <strong>- Fading</strong> : le fait que les voitures disparaissent progressivement
	 * après leur élimination, au lieu de rester sur place.
	 * <p>
	 * <strong>- Hitbox</strong> le fait d'afficher les corps physiques en jeu.
	 * <p>
	 * <strong>- Jingle</strong> le fait de jouer le jingle de démarrage (énonciation du titre du jeu).
	 * <p>
	 * <strong>- Musique</strong> le fait de jouer la musique du jeu.
	 * <p>
	 * 
	 * Elle contient également un <code>String</code> :
	 * <p>
	 * <strong>- Language</strong> : la langue utilisée par le logiciel.
	 * <p>
	 * 
	 * Ces options sont contrôlables par l'utilisateur, depuis la <code>OptionsView</code>.
	 * 
	 * @author Ronan
	 * 
	 * @see OptionsView
	 * @see NeuralRacing
	 *
	 */
	public static class UserDefaults {
		
		/**La langue du logiciel.*/
		public static String language = "Français";
		
		/**Le fait que les voitures disparaissent progressivement après leur élimination, au lieu de rester sur place.*/
		public static boolean fadingEnabled   = true;
		
		/**Le fait d'afficher les corps physiques en jeu.*/
		public static boolean shouldDrawLines = false;
		
		/**Le fait de jouer le jingle de démarrage (énonciation du titre du jeu).*/
		public static boolean jingleEnabled   = true;
		
		/**Le fait de jouer la musique du jeu.*/
		public static boolean musicEnabled    = true;
		
		/**
		 * Cette méthode permet de lire les options de l'utilisateur.
		 * @see UserDefaults
		 */
		public static void read() {
			
			try {
				Scanner reader = new Scanner(new File("data/userDefaults"));
				
				language        = reader.nextLine();
				
				fadingEnabled   = reader.nextBoolean();
				shouldDrawLines = reader.nextBoolean();
				jingleEnabled   = reader.nextBoolean();
				musicEnabled    = reader.nextBoolean();
				
				reader.close();
			} catch (Exception exc) { save(); }
			
			for (String idiom: languageArray) { if (idiom.equals(language)) { return; } }
			language = "Français";
			save();
			
		}
		
		/**
		 * Cette méthode permet de sauvegarder les options choisies par l'utilisateur.
		 * @see UserDefaults
		 */
		public static void save() {
			
			File dataDir = new File("data");
			if (!dataDir.exists()) { dataDir.mkdir(); }
			
			try {
				BufferedWriter writer = new BufferedWriter(new FileWriter("data/userDefaults"));
				writer.write(language + "\n" + fadingEnabled + "\n" + shouldDrawLines + "\n" + jingleEnabled + "\n" + musicEnabled);
				writer.close();
	        } catch (IOException exc) {}
		}
		
	}
	
}
