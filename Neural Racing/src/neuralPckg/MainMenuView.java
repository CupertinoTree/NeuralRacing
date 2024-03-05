package neuralPckg;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.SpringLayout;

import neuralPckg.NeuralRacing.UserDefaults;

/**
 * Cette sous-classe de <code>UIPanel</code> est le menu du jeu. Elle prend
 * en charge les animations de lancement, ainsi que la gestion de la musique.
 * Le menu permet également de passer d'un onglet à un autre.
 * 
 * @author Ronan
 *
 * @see NeuralRacing.UIPanel
 */
public class MainMenuView extends NeuralRacing.UIPanel {

	private static final long serialVersionUID = 78654239213495432L;
	
	/**Le bouton menant à la <code>EvolutionView</code>.*/
	NeuralRacing.UIButton evolutionButton;
	
	/**Le bouton menant à la <code>ExperimentsView</code>.*/
	NeuralRacing.UIButton experimentsButton;
	
	/**Le bouton menant à la <code>RaceEditorView</code>.*/
	NeuralRacing.UIButton raceEditorButton;
	
	/**Le bouton menant à la <code>OptionsView</code>.*/
	NeuralRacing.UIButton optionsButton;
	
	/**La musique de fond du jeu.*/
	Clip musicClip;
	
	double carPositionX,
		   titlePositionY,
		   firstDotsAlpha        = 0,
		   secondDotsAlpha       = 0,
		   visiblePercentage     = 0;
	
	boolean carUnveiled          = false,
			neuronDotsUnveiled   = false,
			neuronColumnUnveiled = false,
			titleUnveiled        = false;
	
	/**Le nombre de lignes dévoilées. Plus précisément, les lignes reliant deux cercles sont composées
	 * de quatre portions. A chaque nouvelle portion dévoilée pour toutes les lignes, cette variable
	 * est incrémentée de 1. Elle vaudra donc successivement 0, puis 1, puis 2, puis 3, puis 4.
	 */
	int numberOfLinesUnveiled    = 0;
	
	//Les positions de tous les cercles à tracer.
	int firstCircleX   = 271,
		firstCircleY   = 458,
		secondCircleX  = 308,
		secondCircleY  = 442,
		thirdCircleX   = 345,
		thirdCircleY   = 431,
		fourthCircleX  = 833,
		fourthCircleY  = 458,
		circleColumnX  = 685,
		columnFirstY   = 32,
		columnSecondY  = 67,
		columnThirdY   = 105,
		columnFourthY  = 210,
		//columnFirstY   = 45,
		//columnSecondY  = 80,
		//columnThirdY   = 117,
		//columnFourthY  = 220,
		circleDiameter = 22;
	
	//Les positions de tous les segments à tracer.
	int firstLineFirstSegmentLength   = 174,
		secondLineFirstSegmentLength  = 120,
		thirdLineFirstSegmentLength   = 72,
		fourthLineFirstSegmentLength  = 62,
		firstLineSecondSegmentLength  = 500,
		secondLineSecondSegmentLength = 500,
		thirdLineSecondSegmentLength  = 500,
		fourthLineSecondSegmentLength = 107,
		firstLineThirdSegmentLength   = 163,
		secondLineThirdSegmentLength  = 239,
		thirdLineThirdSegmentLength   = 309,
		fourthLineThirdSegmentLength  = 170,
		//firstLineThirdSegmentLength   = 150,
		//secondLineThirdSegmentLength  = 224,
		//thirdLineThirdSegmentLength   = 296,
		//fourthLineThirdSegmentLength  = 158,
		firstLineFourthSegmentLength  = 72,
		secondLineFourthSegmentLength = 109,
		thirdLineFourthSegmentLength  = 146,
		fourthLineFourthSegmentLength = 31,
		lineWidth                     = 4;
	
	
	/**
	 * Cette sous-classe de <code>UIPanel</code> est le menu du jeu. Elle prend
	 * en charge les animations de lancement, ainsi que la gestion de la musique.
	 * Le menu permet également de passer d'un onglet à un autre.
	 * 
	 * @param size
	 * La taille de la vue.
	 * 
	 * @see NeuralRacing.UIPanel
	 */
	public MainMenuView(Dimension size) {
		this.setPreferredSize(size);
		this.setBackground(Color.black);
		
		SpringLayout layoutMgr = new SpringLayout();
		this.setLayout(layoutMgr);
		
		evolutionButton = new NeuralRacing.UIButton(new Dimension(157, 33), NeuralRacing.menuGreen, 30, () -> evolutionButton.title = LanguageManager.grabStringFromID("evolutionTitle"));
		evolutionButton.setEnabled(false);
		evolutionButton.setPreferredSize(new Dimension(157, 33));
		evolutionButton.setFont(NeuralRacing.getSystemFont(Font.BOLD, 20));
		evolutionButton.setForeground(Color.WHITE);
		evolutionButton.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { NeuralRacing.showView("evolutionPanel"); }});
		this.add(evolutionButton);
		layoutMgr.putConstraint(SpringLayout.WEST, this.evolutionButton, 516, SpringLayout.WEST, this);
		layoutMgr.putConstraint(SpringLayout.NORTH, this.evolutionButton, 33, SpringLayout.NORTH, this);
		
		experimentsButton = new NeuralRacing.UIButton(new Dimension(157, 33), NeuralRacing.menuGreen, 30, () -> experimentsButton.title = LanguageManager.grabStringFromID("experimentsTitle"));
		experimentsButton.setEnabled(false);
		experimentsButton.setPreferredSize(new Dimension(157, 33));
		experimentsButton.setFont(NeuralRacing.getSystemFont(Font.BOLD, 20));
		experimentsButton.setForeground(Color.WHITE);
		experimentsButton.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { NeuralRacing.showView("experimentsPanel"); }});
		this.add(experimentsButton);
		layoutMgr.putConstraint(SpringLayout.WEST, this.experimentsButton, 516, SpringLayout.WEST, this);
		layoutMgr.putConstraint(SpringLayout.NORTH, this.experimentsButton, 72, SpringLayout.NORTH, this);
		
		raceEditorButton = new NeuralRacing.UIButton(new Dimension(157, 33), NeuralRacing.menuGreen, 30, () -> raceEditorButton.title = LanguageManager.grabStringFromID("raceEditorTitle"));
		raceEditorButton.setEnabled(false);
		raceEditorButton.setPreferredSize(new Dimension(157, 33));
		raceEditorButton.setFont(NeuralRacing.getSystemFont(Font.BOLD, 20));
		raceEditorButton.setForeground(Color.WHITE);
		raceEditorButton.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { NeuralRacing.showView("raceEditorPanel"); }});
		this.add(raceEditorButton);
		layoutMgr.putConstraint(SpringLayout.WEST, this.raceEditorButton, 516, SpringLayout.WEST, this);
		layoutMgr.putConstraint(SpringLayout.NORTH, this.raceEditorButton, 112, SpringLayout.NORTH, this);
		
		optionsButton = new NeuralRacing.UIButton(new Dimension(157, 33), NeuralRacing.menuGreen, 30, () -> optionsButton.title = LanguageManager.grabStringFromID("optionsTitle"));
		optionsButton.setEnabled(false);
		optionsButton.setPreferredSize(new Dimension(157, 33));
		optionsButton.setFont(NeuralRacing.getSystemFont(Font.BOLD, 20));
		optionsButton.setForeground(Color.WHITE);
		optionsButton.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { NeuralRacing.showView("optionsPanel"); }});
		this.add(optionsButton);
		layoutMgr.putConstraint(SpringLayout.WEST, this.optionsButton, 516, SpringLayout.WEST, this);
		layoutMgr.putConstraint(SpringLayout.NORTH, this.optionsButton, 214, SpringLayout.NORTH, this);
		
	}
	
	public void paintComponent(Graphics g) {
		
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		
		RenderingHints qualityHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
		qualityHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY );
		g2d.setRenderingHints(qualityHints);
		
		if (carUnveiled) { g.drawImage(NeuralRacing.superCarImage, (int) carPositionX, 0, null); }
		
		if (neuronDotsUnveiled) {
			g.setColor(new Color(0.08f, 0.55f, 0.12f, (float) firstDotsAlpha));
			g.fillOval(firstCircleX, firstCircleY, circleDiameter, circleDiameter);
			g.drawOval(secondCircleX, secondCircleY, circleDiameter, circleDiameter);
			g.fillOval(thirdCircleX, thirdCircleY, circleDiameter, circleDiameter);
			g.drawOval(fourthCircleX, fourthCircleY, circleDiameter, circleDiameter);
		}
		
		if (numberOfLinesUnveiled >= 1) {
			double actualPercentage = numberOfLinesUnveiled == 1 ? visiblePercentage : 1;
			g.fillRect(firstCircleX  + circleDiameter/2 - lineWidth/2, firstCircleY  - (int) (actualPercentage * firstLineFirstSegmentLength),  lineWidth, (int) (actualPercentage * firstLineFirstSegmentLength));
			g.fillRect(secondCircleX + circleDiameter/2 - lineWidth/2, secondCircleY - (int) (actualPercentage * secondLineFirstSegmentLength), lineWidth, (int) (actualPercentage * secondLineFirstSegmentLength));
			g.fillRect(thirdCircleX  + circleDiameter/2 - lineWidth/2, thirdCircleY  - (int) (actualPercentage * thirdLineFirstSegmentLength),  lineWidth, (int) (actualPercentage * thirdLineFirstSegmentLength));
			g.fillRect(fourthCircleX + circleDiameter/2 - lineWidth/2, fourthCircleY - (int) (actualPercentage * fourthLineFirstSegmentLength), lineWidth, (int) (actualPercentage * fourthLineFirstSegmentLength));
		}
		
		if (numberOfLinesUnveiled >= 2) {
			double actualPercentage = numberOfLinesUnveiled == 2 ? visiblePercentage : 1;
			g.fillRect(firstCircleX  + circleDiameter/2 - lineWidth/2, firstCircleY  - firstLineFirstSegmentLength,  (int) (actualPercentage * firstLineSecondSegmentLength), lineWidth);
			g.fillRect(secondCircleX + circleDiameter/2 - lineWidth/2, secondCircleY - secondLineFirstSegmentLength, (int) (actualPercentage * secondLineSecondSegmentLength), lineWidth);
			g.fillRect(thirdCircleX  + circleDiameter/2 - lineWidth/2, thirdCircleY  - thirdLineFirstSegmentLength,  (int) (actualPercentage * secondLineSecondSegmentLength), lineWidth);
			g.fillRect(fourthCircleX + circleDiameter/2 - lineWidth/2 - (int) (actualPercentage * fourthLineSecondSegmentLength), fourthCircleY - fourthLineFirstSegmentLength, (int) (actualPercentage * fourthLineSecondSegmentLength), lineWidth);
		}
		
		if (numberOfLinesUnveiled >= 3) {
			double actualPercentage = numberOfLinesUnveiled == 3 ? visiblePercentage : 1;
			g.fillRect(firstCircleX  + circleDiameter/2 - lineWidth/2 + firstLineSecondSegmentLength,  lineWidth + firstCircleY  - firstLineFirstSegmentLength  - (int) (firstLineThirdSegmentLength  * actualPercentage), lineWidth, (int) (actualPercentage * firstLineThirdSegmentLength));
			g.fillRect(secondCircleX + circleDiameter/2 - lineWidth/2 + secondLineSecondSegmentLength, lineWidth + secondCircleY - secondLineFirstSegmentLength - (int) (secondLineThirdSegmentLength * actualPercentage), lineWidth, (int) (actualPercentage * secondLineThirdSegmentLength));
			g.fillRect(thirdCircleX  + circleDiameter/2 - lineWidth/2 + thirdLineSecondSegmentLength,  lineWidth + thirdCircleY  - thirdLineFirstSegmentLength  - (int) (thirdLineThirdSegmentLength  * actualPercentage), lineWidth, (int) (actualPercentage * thirdLineThirdSegmentLength));
			g.fillRect(fourthCircleX + circleDiameter/2 - lineWidth/2 - fourthLineSecondSegmentLength, lineWidth + fourthCircleY - fourthLineFirstSegmentLength - (int) (fourthLineThirdSegmentLength * actualPercentage), lineWidth, (int) (actualPercentage * fourthLineThirdSegmentLength));
		}
		
		if (numberOfLinesUnveiled == 4) {
			double actualPercentage = numberOfLinesUnveiled == 4 ? visiblePercentage : 1;
			g.fillRect(firstCircleX  + circleDiameter/2 - lineWidth/2 + firstLineSecondSegmentLength  - (int) (actualPercentage * firstLineFourthSegmentLength),  lineWidth + firstCircleY  - firstLineFirstSegmentLength  - firstLineThirdSegmentLength,  (int) (actualPercentage * firstLineFourthSegmentLength), lineWidth);
			g.fillRect(secondCircleX + circleDiameter/2 - lineWidth/2 + secondLineSecondSegmentLength - (int) (actualPercentage * secondLineFourthSegmentLength), lineWidth + secondCircleY - secondLineFirstSegmentLength - secondLineThirdSegmentLength, (int) (actualPercentage * secondLineFourthSegmentLength), lineWidth);
			g.fillRect(thirdCircleX  + circleDiameter/2 - lineWidth/2 + thirdLineSecondSegmentLength  - (int) (actualPercentage * thirdLineFourthSegmentLength),  lineWidth + thirdCircleY  - thirdLineFirstSegmentLength  - thirdLineThirdSegmentLength,  (int) (actualPercentage * thirdLineFourthSegmentLength), lineWidth);
			g.fillRect(fourthCircleX + circleDiameter/2 - lineWidth/2 - fourthLineSecondSegmentLength - (int) (actualPercentage * fourthLineFourthSegmentLength), lineWidth + fourthCircleY - fourthLineFirstSegmentLength - fourthLineThirdSegmentLength, (int) (actualPercentage * fourthLineFourthSegmentLength), lineWidth);
		}
		
		if (neuronColumnUnveiled) {
			g.setColor(new Color(0.08f, 0.55f, 0.12f, (float) secondDotsAlpha));
			g.drawOval(circleColumnX, columnFirstY  + circleDiameter/2, circleDiameter, circleDiameter);
			g.drawOval(circleColumnX, columnSecondY + circleDiameter/2, circleDiameter, circleDiameter);
			g.fillOval(circleColumnX, columnThirdY  + circleDiameter/2, circleDiameter, circleDiameter);
			g.fillOval(circleColumnX, columnFourthY + circleDiameter/2, circleDiameter, circleDiameter);
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) secondDotsAlpha));
		} else {
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0f));
		}
		
		if (titleUnveiled) { g.drawImage(NeuralRacing.titleImage, 0, (int) titlePositionY, null); }
	}
	
	/**
	 * Cette méthode permet de lancer l'animation d'apparition du menu.
	 */
	public void animate() {
		
		//La voiture entre en scène.
		int carWidth = NeuralRacing.superCarImage.getWidth();
		double carWidthSqrt = Math.sqrt(carWidth);
		
		carPositionX = carWidth;
		carUnveiled = true;
		
		while (carPositionX > 0) {
			//Cette formule permet à la voiture de ralentir progressivement
			//durant son apparition.
			carPositionX -= carWidthSqrt - Math.sqrt(carWidth-carPositionX);
			carPositionX = Math.max(0, Math.floor(carPositionX));
			this.repaint();
			waitFor(1L);
		}		
		waitFor(500L);
		
		//Les premiers ronds entrent en scène.
		firstDotsAlpha = 0;
		neuronDotsUnveiled = true;
		while ((int) firstDotsAlpha < 1) {
			firstDotsAlpha += 0.001;
			this.repaint();
			waitFor(1L);
		}
		
		try {
			//On charge la musique de fond.
			musicClip = AudioSystem.getClip();
			AudioInputStream stream = AudioSystem.getAudioInputStream(new BufferedInputStream(getClass().getResourceAsStream("/audio/music.wav")));
			musicClip.open(stream);
		} catch (LineUnavailableException | UnsupportedAudioFileException | IOException exc) {}
		
		if (UserDefaults.musicEnabled) {
			//Si l'option est activée, la musique est lancée.
			musicClip.loop(Clip.LOOP_CONTINUOUSLY);
		}
		
		waitFor(500L);
		
		//Les lignes entrent en scène.
		for (int lineIndex = 0; lineIndex < 4; lineIndex++) {
			numberOfLinesUnveiled++;
			visiblePercentage = 0;
			while ((int) visiblePercentage < 1) {
				visiblePercentage += 0.01;
				this.repaint();
				waitFor(10L);
			}
		}
		
		//Les boutons, et les derniers ronds, entrent en scène.
		secondDotsAlpha = 0;
		neuronColumnUnveiled = true;
		while ((int) secondDotsAlpha < 1) {
			secondDotsAlpha += 0.001;
			this.repaint();
			waitFor(1L);
		}
		
		//Si l'option est activée, on lance le jingle.
		if (UserDefaults.jingleEnabled) {
			try {
				Clip jingleClip = AudioSystem.getClip();
				AudioInputStream stream = AudioSystem.getAudioInputStream(new BufferedInputStream(getClass().getResourceAsStream("/audio/jingle.wav")));
				jingleClip.open(stream);
				jingleClip.loop(0);
			} catch (LineUnavailableException | UnsupportedAudioFileException | IOException exc) {}
		}
		
		//Le titre entre en scène.
		double titleImageHeight = NeuralRacing.titleImage.getHeight();
		titlePositionY = -titleImageHeight;
		titleUnveiled = true;
		while ((int) titlePositionY < 0) {
			titlePositionY = Math.min(0, 0.99*titlePositionY);
			this.repaint();
			waitFor(1L);
		}
		
		//Enfin, les boutons deviennent utilisables.
		evolutionButton.setEnabled(true);
		experimentsButton.setEnabled(true);
		raceEditorButton.setEnabled(true);
		optionsButton.setEnabled(true);
		
	}
	
	/**
	 * Cette méthode permet d'empêcher toute action du <code>Thread</code> principal
	 * pendant une période donnée en paramètre.
	 * 
	 * @param millis
	 * La période d'arrêt, en millisecondes.
	 */
	private void waitFor(long millis) { try { Thread.sleep(millis); } catch (InterruptedException exc) {} }
	
}
