package neuralPckg;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SpringLayout;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicScrollBarUI;

/**
 * Cette sous-classe de <code>UIPanel</code> est la vue d'évolution.
 * Grâce à de nombreux paramètres, elle permet à l'utilisateur de gérer au mieux
 * le processus d'évolution d'une population de voiture, tout en voyant leur conduite en direct.
 * Une partie dédiée aux statistiques permet également de suivre le progrès de manière plus précise.
 * 
 * @author Ronan
 *
 * @see NeuralRacing.UIPanel
 */
public class EvolutionView extends NeuralRacing.UIPanel implements ActionListener {
	
	private static final long serialVersionUID = 6033934130506965058L;
	
	static int defaultMinSpeed = 10, defaultMaxSpeed = 30, ultimateMinSpeed = 1, ultimateMaxSpeed = 100;
	
	JPanel contentView = new JPanel(), headerView = new JPanel(), detailView = new JPanel();
	
	JScrollPane scrollView;
	
	/**La vue du circuit.*/
	RaceView raceView;
	
	/**La vue des voitures.*/
	CarsView carsView;
	
	/**La grille de jeu sélectionnée.*/
	RaceGrid raceGrid;
	
	SpringLayout headerViewLayout,
				 detailViewLayout;
	
	JLabel header         = new JLabel(),
		   bestIndivTitle = new JLabel();
	
	NeuralRacing.UIButton openGuide,
						  backToMenu,
						  openRace,
						  startOver,
						  play,
						  fastForward,
						  fastBackward,
						  showFitnessData,
						  showSpreadingData,
						  saveBest,
						  insertIndiv;
	
	NeuralRacing.UILabel  generationLabel,
						  timeLabel,
						  paramsLabel,
						  statisticsLabel,
						  histogramLabel,
						  beforeStartLabel,
						  anytimeLabel;
	
	NeuralRacing.UIView   generationPlaceholder,
						  timePlaceholder,
						  statisticsPlaceholder,
						  selectionPlaceholder,
						  paramsPlaceholder;
	
	/**Le nom du circuit sélectionné.*/
	String selectedRace = "";
	
	NeuralRacing.UISelector    systemSelector;
	NeuralRacing.UIProgressBar hourglass;
	NeuralRacing.UIGraphView   graphView,
							   histogramLikeView;
	NeuralRacing.UINeuralView  neuralView;
	
	NeuralRacing.UIParameterToggle keepBest;
	NeuralRacing.UIParameterSlider populationCount,
								   minSpeed,
								   maxSpeed,
								   mutationRate,
								   tournamentSize,
								   defaultTime;
	
	int populationSize   = 100,
		generation       = 0,
		timeLeft         = 40,
		viewingFrequency = 10,
		defaultTimeLeft  = 40,
		newTimeLeft      = 40,
		realTime         = 0,
		bestFitness      = -1,
		average          = -1;
	
	//double minimumSpeed  = 1,
	//	   maximumSpeed  = 3;
	
	boolean populationAlive = false,
			clean           = true,
			isPlaying       = false,
			leftSelected    = true;
	
	/**Les vitesses de rafraîchissement disponibles.*/
	double [] speeds = { 0.125, 0.25, 0.5, 1, 2, 5, 10 };
	
	//La vitesse par défaut est de 1.
	int speedIndex = 3;
	
	/**Cette variable permet d'éviter que deux tâches du <code>Timer</code> soient lancées en même temps.*/
	Boolean coalesces;
	
	Timer gameClock;
	
	BiologicalModel.Population population;
	
	/**La liste des individus à insérer.*/
	List<BiologicalModel.Individual> insertion = new ArrayList<BiologicalModel.Individual>();
	
	/**
	 * Cette sous-classe de <code>UIPanel</code> est la vue d'évolution.
	 * Grâce à de nombreux paramètres, elle permet à l'utilisateur de gérer au mieux
	 * le processus d'évolution d'une population de voiture, tout en voyant leur conduite en direct.
	 * Une partie dédiée aux statistiques permet également de suivre le progrès de manière plus précise.
	 * 
	 * @param size
	 * La taille de la vue.
	 *
	 * @see NeuralRacing.UIPanel
	 */
	public EvolutionView(Dimension size) {
		
		this.setPreferredSize(size);
		this.setLayout(new BorderLayout());
		headerView.setPreferredSize(new Dimension((int) (size.getWidth()-200), 100));
		detailView.setPreferredSize(new Dimension(200, 1140));
		contentView.setPreferredSize(new Dimension((int) (size.getWidth()-200), (int) (size.getHeight()-200)));
		
		headerView.setBackground(NeuralRacing.darkerGrayColor);
		detailView.setBackground(new Color(0, 108, 58));
		
		headerViewLayout = new SpringLayout();
		headerView.setLayout(headerViewLayout);
		
		LanguageManager.registerUIElement(new LanguageManager.LanguageMutable() {
			@Override public void updateData() {
				header.setText(LanguageManager.grabStringFromID("evolutionTitleExtended"));
				bestIndivTitle.setText(LanguageManager.grabStringFromID("champion"));
			}
		});
		
		openGuide = new NeuralRacing.UIButton(new Dimension(40, 55), NeuralRacing.darkGrayColor, 20, null);
		openGuide.setIcon(NeuralRacing.guideIcon);
		openGuide.setFont(NeuralRacing.getSystemFont(Font.BOLD, 24));
		openGuide.setForeground(Color.white);
		openGuide.addActionListener(this);
		headerView.add(openGuide);
		headerViewLayout.putConstraint(SpringLayout.VERTICAL_CENTER, openGuide, 0, SpringLayout.VERTICAL_CENTER, headerView);
		headerViewLayout.putConstraint(SpringLayout.WEST, openGuide, 25, SpringLayout.WEST, headerView);
		
		openGuide.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent evt) {
				NeuralRacing.guide.openPage("evolutionView");
			}
		});
		
		backToMenu = new NeuralRacing.UIButton(new Dimension(100, 55), NeuralRacing.darkGrayColor, 20, () -> backToMenu.title = LanguageManager.grabStringFromID("menu"));
		backToMenu.setFont(NeuralRacing.getSystemFont(Font.BOLD, 25));
		backToMenu.setForeground(Color.white);
		backToMenu.addActionListener(this);
		headerView.add(backToMenu);
		headerViewLayout.putConstraint(SpringLayout.VERTICAL_CENTER, backToMenu, 0, SpringLayout.VERTICAL_CENTER, headerView);
		headerViewLayout.putConstraint(SpringLayout.WEST, backToMenu, 15, SpringLayout.EAST, openGuide);
		
		openRace = new NeuralRacing.UIButton(new Dimension(139, 55), NeuralRacing.darkGrayColor, 20, () -> openRace.title = LanguageManager.grabStringFromID("races"));
		openRace.setIcon(NeuralRacing.openRaceIcon);
		openRace.addActionListener(this);
		openRace.setForeground(Color.white);
		openRace.setFont(NeuralRacing.getSystemFont(Font.BOLD, 22));
		headerView.add(openRace);
		headerViewLayout.putConstraint(SpringLayout.VERTICAL_CENTER, openRace, 0, SpringLayout.VERTICAL_CENTER, headerView);
		headerViewLayout.putConstraint(SpringLayout.EAST, openRace, -35, SpringLayout.EAST, this);
		
		openRace.addActionListener(new ActionListener() {

			@Override public void actionPerformed(ActionEvent e) {
					
				if (!clear()) { return; }
				String name = (String) JOptionPane.showInputDialog(contentView, LanguageManager.grabStringFromID("chooseRace"), LanguageManager.grabStringFromID("message"), JOptionPane.PLAIN_MESSAGE, null, NeuralRacing.raceList.keySet().toArray(), selectedRace);
				if (name == null) { return; } else { selectedRace = name; }
				
				try {
					raceGrid.synchronizeWithGrid(new RaceGrid(new EnhancedString(NeuralRacing.raceList.get(name)), false));
					
				} catch (IllegalArgumentException exc) {
					JOptionPane.showMessageDialog(contentView, exc.getMessage(), LanguageManager.grabStringFromID("error"), JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				carsView.originalDirection = raceGrid.originalDirection;
				carsView.origin            = raceGrid.start;
				carsView.cleanCarsGroup();
				NeuralRacing.evolutionView.repaint();
			}
		});
		
		header.setFont(NeuralRacing.getSystemFont(Font.BOLD, 32));
		header.setForeground(Color.white);
		headerView.add(header);
		headerViewLayout.putConstraint(SpringLayout.VERTICAL_CENTER, header, 0, SpringLayout.VERTICAL_CENTER, headerView);
		headerViewLayout.putConstraint(SpringLayout.WEST, header, 15, SpringLayout.EAST, backToMenu);
		
		detailViewLayout = new SpringLayout();
		detailView.setLayout(detailViewLayout);
		
		generationPlaceholder = new NeuralRacing.UIView(new Dimension(170, 50), NeuralRacing.widespreadGreenishGreen, 20);
		detailView.add(generationPlaceholder);
		detailViewLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, generationPlaceholder, 0, SpringLayout.HORIZONTAL_CENTER, detailView);
		detailViewLayout.putConstraint(SpringLayout.NORTH, generationPlaceholder, 16, SpringLayout.NORTH, detailView);
		
		generationLabel = new NeuralRacing.UILabel(new Dimension(150, 36), NeuralRacing.darkGrayColor, 15, () -> generationLabel.title = LanguageManager.grabStringFromID("generation") + " " + generation);
		generationLabel.setForeground(Color.WHITE);
		generationLabel.setFont(NeuralRacing.getSystemFont(Font.BOLD, 16));
		detailView.add(generationLabel);
		detailViewLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, generationLabel, 0, SpringLayout.HORIZONTAL_CENTER, generationPlaceholder);
		detailViewLayout.putConstraint(SpringLayout.VERTICAL_CENTER, generationLabel, 0, SpringLayout.VERTICAL_CENTER, generationPlaceholder);
		detailView.setComponentZOrder(generationLabel, 0);
		
		timePlaceholder = new NeuralRacing.UIView(new Dimension(170, 185), NeuralRacing.widespreadGreenishGreen, 20);
		detailView.add(timePlaceholder);
		detailViewLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, timePlaceholder, 0, SpringLayout.HORIZONTAL_CENTER, detailView);
		detailViewLayout.putConstraint(SpringLayout.NORTH, timePlaceholder, 10, SpringLayout.SOUTH, generationPlaceholder);
		
		timeLabel = new NeuralRacing.UILabel(new Dimension(150, 36), NeuralRacing.darkGrayColor, 15, () -> timeLabel.title = LanguageManager.grabStringFromID("time") + " : x" + round(speeds[speedIndex]));
		timeLabel.setForeground(Color.WHITE);
		timeLabel.setFont(NeuralRacing.getSystemFont(Font.BOLD, 16));
		detailView.add(timeLabel);
		detailViewLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, timeLabel, 0, SpringLayout.HORIZONTAL_CENTER, detailView);
		detailViewLayout.putConstraint(SpringLayout.NORTH, timeLabel, 10, SpringLayout.NORTH, timePlaceholder);
		detailView.setComponentZOrder(timeLabel, 0);
		
		hourglass = new NeuralRacing.UIProgressBar(new Dimension(150, 6), NeuralRacing.darkGreenBackground, NeuralRacing.selectedGreenTint, 6, 100);
		detailView.add(hourglass);
		detailViewLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, hourglass, 0, SpringLayout.HORIZONTAL_CENTER, detailView);
		detailViewLayout.putConstraint(SpringLayout.NORTH, hourglass, 15, SpringLayout.SOUTH, timeLabel);
		detailView.setComponentZOrder(hourglass, 0);
		
		play = new NeuralRacing.UIButton(new Dimension(30, 39), NeuralRacing.widespreadGreenishGreen, 0, null);
		play.setIcon(NeuralRacing.play);
		play.highlight = false;
		headerView.add(play);
		detailViewLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, play, 0, SpringLayout.HORIZONTAL_CENTER, detailView);
		detailViewLayout.putConstraint(SpringLayout.NORTH, play, 15, SpringLayout.SOUTH, hourglass);
		detailView.setComponentZOrder(play, 0);
		
		play.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent evt) {
				
				if (isPlaying) {
					play.setIcon(NeuralRacing.play);
					gameClock.cancel();
					gameClock.purge();
				} else {
					
					if (clean) {
						
						if (carsView.minimumSpeed >= carsView.maximumSpeed) {
							JOptionPane.showMessageDialog(contentView, LanguageManager.grabStringFromID("speedOrder"), LanguageManager.grabStringFromID("error"), JOptionPane.ERROR_MESSAGE);
							return;
						}
						
						if (insertion.size() > populationSize) {
							JOptionPane.showMessageDialog(contentView, LanguageManager.grabStringFromID("nbrOrder"), LanguageManager.grabStringFromID("error"), JOptionPane.ERROR_MESSAGE);
							return;
						}
						
						carsView.createCarsGroup(populationSize);
						population = new BiologicalModel.Population(populationSize, 0);
						
						if (insertion != null) {
							int index = -1;
							for (BiologicalModel.Individual individual: insertion) {
								index++;
								population.individuals[index] = individual;
							}
						}
						
						for (int i = 0; i < populationSize; i++) {
							carsView.cars[i].network = population.individuals[i].toNeuralNet();
							carsView.cars[i].motorOn = true;
						}
					}
					
					//Les voitures sont lancées : la vue n'est plus immaculée.
					setCleanToFalse();
					play.setIcon(NeuralRacing.pause);
					restoreTimer();
				}
				
				isPlaying = !isPlaying;
				play.repaint();
			}
		});
		
		fastBackward = new NeuralRacing.UIButton(new Dimension(30, 18), NeuralRacing.widespreadGreenishGreen, 0, null);
		fastBackward.setIcon(NeuralRacing.fastBackward);
		fastBackward.highlight = false;
		detailView.add(fastBackward);
		detailViewLayout.putConstraint(SpringLayout.VERTICAL_CENTER, fastBackward, 0, SpringLayout.VERTICAL_CENTER, play);
		detailViewLayout.putConstraint(SpringLayout.EAST, fastBackward, -20, SpringLayout.WEST, play);
		detailView.setComponentZOrder(fastBackward, 0);
		
		fastForward = new NeuralRacing.UIButton(new Dimension(30, 18), NeuralRacing.widespreadGreenishGreen, 0, null);
		fastForward.setIcon(NeuralRacing.fastForward);
		fastForward.highlight = false;
		detailView.add(fastForward);
		detailViewLayout.putConstraint(SpringLayout.VERTICAL_CENTER, fastForward, 0, SpringLayout.VERTICAL_CENTER, play);
		detailViewLayout.putConstraint(SpringLayout.WEST, fastForward, 20, SpringLayout.EAST, play);
		detailView.setComponentZOrder(fastForward, 0);
		
		ActionListener backAndForth = new ActionListener() {
			
			@Override public void actionPerformed(ActionEvent evt) {
				
				if (evt.getSource().equals(fastBackward)) {
					if (speedIndex == 0) { return; }
					speedIndex = speedIndex-1;
				} else {
					if (speedIndex == speeds.length-1) { return; }
					speedIndex = speedIndex+1;
				}
				
				viewingFrequency = (int) (10/speeds[speedIndex]);
				timeLabel.title = LanguageManager.grabStringFromID("time") + " : x" + round(speeds[speedIndex]);
				repaint();
				if (!isPlaying) { return; }
				gameClock.cancel();
				gameClock.purge();
				restoreTimer();
			}
		};
		
		fastBackward.addActionListener(backAndForth);
		fastForward.addActionListener(backAndForth);
		
		startOver = new NeuralRacing.UIButton(new Dimension(150, 36), NeuralRacing.darkGrayColor, 12, () -> startOver.title = LanguageManager.grabStringFromID("startOver"));
		startOver.setForeground(Color.WHITE);
		startOver.setFont(NeuralRacing.getSystemFont(Font.BOLD, 14));
		detailView.add(startOver);
		detailViewLayout.putConstraint(SpringLayout.SOUTH, startOver, -8, SpringLayout.SOUTH, timePlaceholder);
		detailViewLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, startOver, 0, SpringLayout.HORIZONTAL_CENTER, timePlaceholder);
		detailView.setComponentZOrder(startOver, 0);
		
		startOver.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { clear(); }});
		
		ActionListener selectorListener = new ActionListener() {
			@Override public void actionPerformed(ActionEvent evt) {
				
				leftSelected = evt.getID() == 0;
				
				paramsPlaceholder.setVisible(leftSelected);
				paramsLabel.setVisible(leftSelected);
				beforeStartLabel.setVisible(leftSelected);
				populationCount.setVisible(leftSelected);
				minSpeed.setVisible(leftSelected);
				maxSpeed.setVisible(leftSelected);
				insertIndiv.setVisible(leftSelected);
				anytimeLabel.setVisible(leftSelected);
				mutationRate.setVisible(leftSelected);
				tournamentSize.setVisible(leftSelected);
				defaultTime.setVisible(leftSelected);
				keepBest.setVisible(leftSelected);
				
				statisticsPlaceholder.setVisible(!leftSelected);
				statisticsLabel.setVisible(!leftSelected);
				graphView.setVisible(!leftSelected);
				showFitnessData.setVisible(!leftSelected);
				histogramLabel.setVisible(!leftSelected);
				histogramLikeView.setVisible(!leftSelected);
				showSpreadingData.setVisible(!leftSelected);
				selectionPlaceholder.setVisible(!leftSelected);
				bestIndivTitle.setVisible(!leftSelected);
				neuralView.setVisible(!leftSelected);
				saveBest.setVisible(!leftSelected);
				
				showSpreadingData.setEnabled(!leftSelected);
				showFitnessData.setEnabled(!leftSelected);
				saveBest.setEnabled(!leftSelected && neuralView.getNetwork() != null);
				
				populationCount.slider.setEnabled(leftSelected && clean);
				minSpeed.slider.setEnabled(leftSelected && clean);
				maxSpeed.slider.setEnabled(leftSelected && clean);
				insertIndiv.setEnabled(leftSelected && clean);
				mutationRate.slider.setEnabled(leftSelected);
				tournamentSize.slider.setEnabled(leftSelected);
				defaultTime.slider.setEnabled(leftSelected);
				keepBest.toggle.setEnabled(leftSelected);
			}
		};
		
		systemSelector = new NeuralRacing.UISelector(new Dimension(170, 60), NeuralRacing.selectedGreenTint, NeuralRacing.widespreadGreenishGreen, 25, selectorListener, NeuralRacing.systemSelectorLeftIcon, NeuralRacing.systemSelectorRightIcon);
		detailView.add(systemSelector);
		detailViewLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, systemSelector, 0, SpringLayout.HORIZONTAL_CENTER, detailView);
		detailViewLayout.putConstraint(SpringLayout.NORTH, systemSelector, 10, SpringLayout.SOUTH, timePlaceholder);
		
		paramsPlaceholder = new NeuralRacing.UIView(new Dimension(170, 790), NeuralRacing.widespreadGreenishGreen, 20);
		detailView.add(paramsPlaceholder);
		detailViewLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, paramsPlaceholder, 0, SpringLayout.HORIZONTAL_CENTER, detailView);
		detailViewLayout.putConstraint(SpringLayout.NORTH, paramsPlaceholder, 10, SpringLayout.SOUTH, systemSelector);
		
		paramsLabel = new NeuralRacing.UILabel(new Dimension(150, 40), NeuralRacing.darkGrayColor, 20, () -> paramsLabel.title = LanguageManager.grabStringFromID("params"));
		paramsLabel.setFont(NeuralRacing.getSystemFont(Font.BOLD, 16));
		paramsLabel.setForeground(Color.white);
		detailView.add(paramsLabel);
		detailViewLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, paramsLabel, 0, SpringLayout.HORIZONTAL_CENTER, detailView);
		detailViewLayout.putConstraint(SpringLayout.NORTH, paramsLabel, 10, SpringLayout.NORTH, paramsPlaceholder);
		detailView.setComponentZOrder(paramsLabel, 0);
		
		beforeStartLabel = new NeuralRacing.UILabel(new Dimension(150, 30), NeuralRacing.darkGrayColor, 20, () -> beforeStartLabel.title = LanguageManager.grabStringFromID("beforeStart"));
		beforeStartLabel.setFont(NeuralRacing.getSystemFont(Font.BOLD, 12));
		beforeStartLabel.setForeground(Color.white);
		detailView.add(beforeStartLabel);
		detailViewLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, beforeStartLabel, 0, SpringLayout.HORIZONTAL_CENTER, detailView);
		detailViewLayout.putConstraint(SpringLayout.NORTH, beforeStartLabel, 10, SpringLayout.SOUTH, paramsLabel);
		detailView.setComponentZOrder(beforeStartLabel, 0);
		
		populationCount = new NeuralRacing.UIParameterSlider(new Dimension(150, 70), NeuralRacing.darkGrayColor, NeuralRacing.widespreadGreenishGreen, Color.white, 20, "", 10, 1000, 100, () -> populationCount.title = LanguageManager.grabStringFromID("size") + " : ");
		populationCount.label.setForeground(Color.WHITE);
		populationCount.label.setFont(NeuralRacing.getSystemFont(Font.BOLD, 12));
		detailViewLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, populationCount, 0, SpringLayout.HORIZONTAL_CENTER, detailView);
		detailViewLayout.putConstraint(SpringLayout.NORTH, populationCount, 10, SpringLayout.SOUTH, beforeStartLabel);
		detailView.setComponentZOrder(populationCount, 0);
		
		minSpeed = new NeuralRacing.UIParameterSlider(new Dimension(150, 70), NeuralRacing.darkGrayColor, NeuralRacing.widespreadGreenishGreen, Color.white, 20, "px/ms", EvolutionView.ultimateMinSpeed, EvolutionView.ultimateMaxSpeed, EvolutionView.defaultMinSpeed, () -> minSpeed.title = LanguageManager.grabStringFromID("minSpeed") + " : ");
		minSpeed.label.setForeground(Color.WHITE);
		minSpeed.label.setFont(NeuralRacing.getSystemFont(Font.BOLD, 12));
		detailViewLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, minSpeed, 0, SpringLayout.HORIZONTAL_CENTER, detailView);
		detailViewLayout.putConstraint(SpringLayout.NORTH, minSpeed, 10, SpringLayout.SOUTH, populationCount);
		detailView.setComponentZOrder(minSpeed, 0);
		
		maxSpeed = new NeuralRacing.UIParameterSlider(new Dimension(150, 70), NeuralRacing.darkGrayColor, NeuralRacing.widespreadGreenishGreen, Color.white, 20, "px/ms", EvolutionView.ultimateMinSpeed, EvolutionView.ultimateMaxSpeed, EvolutionView.defaultMaxSpeed, () -> maxSpeed.title = LanguageManager.grabStringFromID("maxSpeed") + " : ");
		maxSpeed.label.setForeground(Color.WHITE);
		maxSpeed.label.setFont(NeuralRacing.getSystemFont(Font.BOLD, 12));
		detailViewLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, maxSpeed, 0, SpringLayout.HORIZONTAL_CENTER, detailView);
		detailViewLayout.putConstraint(SpringLayout.NORTH, maxSpeed, 10, SpringLayout.SOUTH, minSpeed);
		detailView.setComponentZOrder(maxSpeed, 0);
		
		insertIndiv = new NeuralRacing.UIButton(new Dimension(150, 50), NeuralRacing.darkGrayColor, 20, () -> insertIndiv.title = LanguageManager.grabStringFromID("insert"));
		insertIndiv.setIcon(NeuralRacing.insertIcon);
		insertIndiv.setForeground(Color.WHITE);
		insertIndiv.setFont(NeuralRacing.getSystemFont(Font.BOLD, 14));
		detailView.add(insertIndiv);
		detailViewLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, insertIndiv, 0, SpringLayout.HORIZONTAL_CENTER, detailView);
		detailViewLayout.putConstraint(SpringLayout.NORTH, insertIndiv, 10, SpringLayout.SOUTH, maxSpeed);
		detailView.setComponentZOrder(insertIndiv, 0);
		
		insertIndiv.addActionListener(new ActionListener() {

			@Override public void actionPerformed(ActionEvent e) {
				
				if (NeuralRacing.networkList.isEmpty()) { return; }
				
				String name = (String) JOptionPane.showInputDialog(contentView, LanguageManager.grabStringFromID("chooseIndiv"), LanguageManager.grabStringFromID("message"), JOptionPane.PLAIN_MESSAGE, null, NeuralRacing.networkList.keySet().toArray(), null);
				if (name == null) { return; }
				insertion.add(new NeuralModel.NeuralNetwork(new EnhancedString(NeuralRacing.networkList.get(name))).toIndividual());
			}
			
		});
		
		anytimeLabel = new NeuralRacing.UILabel(new Dimension(150, 30), NeuralRacing.darkGrayColor, 20, () -> anytimeLabel.title = LanguageManager.grabStringFromID("otherSettings"));
		anytimeLabel.setFont(NeuralRacing.getSystemFont(Font.BOLD, 12));
		anytimeLabel.setForeground(Color.white);
		detailView.add(anytimeLabel);
		detailViewLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, anytimeLabel, 0, SpringLayout.HORIZONTAL_CENTER, detailView);
		detailViewLayout.putConstraint(SpringLayout.NORTH, anytimeLabel, 10, SpringLayout.SOUTH, insertIndiv);
		detailView.setComponentZOrder(anytimeLabel, 0);
		
		mutationRate = new NeuralRacing.UIParameterSlider(new Dimension(150, 70), NeuralRacing.darkGrayColor, NeuralRacing.widespreadGreenishGreen, Color.white, 20, "%", 0, 100, 5, () -> mutationRate.title = LanguageManager.grabStringFromID("mutationRate") + " : ");
		mutationRate.label.setForeground(Color.WHITE);
		mutationRate.label.setFont(NeuralRacing.getSystemFont(Font.BOLD, 12));
		detailView.add(mutationRate);
		detailViewLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, mutationRate, 0, SpringLayout.HORIZONTAL_CENTER, detailView);
		detailViewLayout.putConstraint(SpringLayout.NORTH, mutationRate, 10, SpringLayout.SOUTH, anytimeLabel);
		detailView.setComponentZOrder(mutationRate, 0);
		
		tournamentSize = new NeuralRacing.UIParameterSlider(new Dimension(150, 70), NeuralRacing.darkGrayColor, NeuralRacing.widespreadGreenishGreen, Color.white, 20, "%", 1, 100, 50, () -> tournamentSize.title = LanguageManager.grabStringFromID("tournament") + " : ");
		tournamentSize.label.setForeground(Color.WHITE);
		tournamentSize.label.setFont(NeuralRacing.getSystemFont(Font.BOLD, 12));
		detailView.add(tournamentSize);
		detailViewLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, tournamentSize, 0, SpringLayout.HORIZONTAL_CENTER, detailView);
		detailViewLayout.putConstraint(SpringLayout.NORTH, tournamentSize, 10, SpringLayout.SOUTH, mutationRate);
		detailView.setComponentZOrder(tournamentSize, 0);
		
		defaultTime = new NeuralRacing.UIParameterSlider(new Dimension(150, 70), NeuralRacing.darkGrayColor, NeuralRacing.widespreadGreenishGreen, Color.white, 20, "s", 10, 300, 40, () -> defaultTime.title = LanguageManager.grabStringFromID("time") + " : ");
		defaultTime.label.setForeground(Color.WHITE);
		defaultTime.label.setFont(NeuralRacing.getSystemFont(Font.BOLD, 12));
		detailView.add(defaultTime);
		detailViewLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, defaultTime, 0, SpringLayout.HORIZONTAL_CENTER, detailView);
		detailViewLayout.putConstraint(SpringLayout.NORTH, defaultTime, 10, SpringLayout.SOUTH, tournamentSize);
		detailView.setComponentZOrder(defaultTime, 0);
		
		keepBest = new NeuralRacing.UIParameterToggle(new Dimension(150, 100), NeuralRacing.darkGrayColor, 20, NeuralRacing.selectedGreenTint, NeuralRacing.darkGreenBackground, true, () -> keepBest.label.setText(LanguageManager.grabStringFromID("includeChampion") + " :"));
		keepBest.label.setForeground(Color.WHITE);
		keepBest.label.setFont(NeuralRacing.getSystemFont(Font.BOLD, 12));
		keepBest.toggle.setForeground(Color.WHITE);
		keepBest.toggle.setFont(NeuralRacing.getSystemFont(Font.BOLD, 14));
		detailView.add(keepBest);
		detailViewLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, keepBest, 0, SpringLayout.HORIZONTAL_CENTER, detailView);
		detailViewLayout.putConstraint(SpringLayout.NORTH, keepBest, 10, SpringLayout.SOUTH, defaultTime);
		detailView.setComponentZOrder(keepBest, 0);
		
		ChangeListener changeListener = new ChangeListener() {

			@Override public void stateChanged(ChangeEvent e) {
				
				if (e.getSource() == populationCount.slider) {
					
					if (!clean) { return; }
					populationSize = populationCount.slider.getValue();
					
				} else if (e.getSource() == minSpeed.slider) {
					
					if (!clean) { return; }
					carsView.minimumSpeed = (double) minSpeed.slider.getValue()/10d;
					
				} else if (e.getSource() == maxSpeed.slider) {
					
					if (!clean) { return; }
					carsView.maximumSpeed = (double) maxSpeed.slider.getValue()/10d;
					
				} else if (e.getSource() == mutationRate.slider) {
					
					BiologicalModel.Algorithm.mutationRate = mutationRate.slider.getValue();
					
				} else if (e.getSource() == tournamentSize.slider) {
					
					BiologicalModel.Algorithm.tournamentSize = Math.max(1, (int) (populationSize*tournamentSize.slider.getValue()/100));
					
				} else if (e.getSource() == defaultTime.slider) {
					
					newTimeLeft = defaultTime.slider.getValue();
					
				} else if (e.getSource() == keepBest) {
					
					BiologicalModel.Algorithm.keepBest = keepBest.currentValue;
					
				}
				
			}
			
		};
		
		populationCount.slider.addChangeListener(changeListener);
		minSpeed.slider.addChangeListener(changeListener);
		maxSpeed.slider.addChangeListener(changeListener);
		mutationRate.slider.addChangeListener(changeListener);
		tournamentSize.slider.addChangeListener(changeListener);
		defaultTime.slider.addChangeListener(changeListener);
		keepBest.listener = changeListener;
		
		statisticsPlaceholder = new NeuralRacing.UIView(new Dimension(170, 790), NeuralRacing.widespreadGreenishGreen, 20);
		detailView.add(statisticsPlaceholder);
		detailViewLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, statisticsPlaceholder, 0, SpringLayout.HORIZONTAL_CENTER, detailView);
		detailViewLayout.putConstraint(SpringLayout.NORTH, statisticsPlaceholder, 10, SpringLayout.SOUTH, systemSelector);
		
		statisticsLabel = new NeuralRacing.UILabel(new Dimension(150, 40), NeuralRacing.darkGrayColor, 20, () -> statisticsLabel.title = LanguageManager.grabStringFromID("score") + " : " + (bestFitness == -1 ? "-" : "" + bestFitness));
		statisticsLabel.setFont(NeuralRacing.getSystemFont(Font.BOLD, 16));
		statisticsLabel.setForeground(Color.white);
		detailView.add(statisticsLabel);
		detailViewLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, statisticsLabel, 0, SpringLayout.HORIZONTAL_CENTER, detailView);
		detailViewLayout.putConstraint(SpringLayout.NORTH, statisticsLabel, 10, SpringLayout.NORTH, statisticsPlaceholder);
		detailView.setComponentZOrder(statisticsLabel, 0);
		
		graphView = new NeuralRacing.UIGraphView(new Dimension(150, 172), NeuralRacing.darkGrayColor, NeuralRacing.widespreadGreenishGreen, 20, 0.7,  () -> { graphView.yTitle = LanguageManager.grabStringFromID("score"); graphView.xAxis.setText(LanguageManager.grabStringFromID("generation")); });
		graphView.updateFont(NeuralRacing.getSystemFont(Font.BOLD, 12));
		detailView.add(graphView);
		detailViewLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, graphView, 0, SpringLayout.HORIZONTAL_CENTER, detailView);
		detailViewLayout.putConstraint(SpringLayout.NORTH, graphView, 10, SpringLayout.SOUTH, statisticsLabel);
		detailView.setComponentZOrder(graphView, 0);
		
		showFitnessData = new NeuralRacing.UIButton(new Dimension(150, 30), NeuralRacing.darkGrayColor, 10, () -> showFitnessData.title = LanguageManager.grabStringFromID("showData"));
		showFitnessData.setForeground(Color.WHITE);
		showFitnessData.setFont(NeuralRacing.getSystemFont(Font.BOLD, 12));
		detailView.add(showFitnessData);
		detailViewLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, showFitnessData, 0, SpringLayout.HORIZONTAL_CENTER, detailView);
		detailViewLayout.putConstraint(SpringLayout.NORTH, showFitnessData, 10, SpringLayout.SOUTH, graphView);
		detailView.setComponentZOrder(showFitnessData, 0);
		
		showFitnessData.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { if (isPlaying) { play.doClick(); } graphView.showDataTable(); } });
		
		histogramLabel = new NeuralRacing.UILabel(new Dimension(150, 40), NeuralRacing.darkGrayColor, 20, () -> histogramLabel.title = LanguageManager.grabStringFromID("average") + " : " + (average == -1 ? "-" : "" + average));
		histogramLabel.setFont(NeuralRacing.getSystemFont(Font.BOLD, 14));
		histogramLabel.setForeground(Color.white);
		detailView.add(histogramLabel);
		detailViewLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, histogramLabel, 0, SpringLayout.HORIZONTAL_CENTER, detailView);
		detailViewLayout.putConstraint(SpringLayout.NORTH, histogramLabel, 10, SpringLayout.SOUTH, showFitnessData);
		detailView.setComponentZOrder(histogramLabel, 0);
		
		histogramLikeView = new NeuralRacing.UIGraphView(new Dimension(150, 172), NeuralRacing.darkGrayColor, NeuralRacing.widespreadGreenishGreen, 20, 0.7, () -> { histogramLikeView.yTitle = LanguageManager.grabStringFromID("size") + " (%)"; histogramLikeView.xAxis.setText(LanguageManager.grabStringFromID("score")); });
		histogramLikeView.updateFont(NeuralRacing.getSystemFont(Font.BOLD, 12));
		histogramLikeView.yAxis.setPreferredSize(new Dimension(20, 70));
		histogramLikeView.graph.scaleAsPercentage = true;
		detailView.add(histogramLikeView);
		detailViewLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, histogramLikeView, 0, SpringLayout.HORIZONTAL_CENTER, detailView);
		detailViewLayout.putConstraint(SpringLayout.NORTH, histogramLikeView, 10, SpringLayout.SOUTH, histogramLabel);
		detailView.setComponentZOrder(histogramLikeView, 0);
		
		showSpreadingData = new NeuralRacing.UIButton(new Dimension(150, 30), NeuralRacing.darkGrayColor, 10, () -> showSpreadingData.title = LanguageManager.grabStringFromID("showData"));
		showSpreadingData.setForeground(Color.WHITE);
		showSpreadingData.setFont(NeuralRacing.getSystemFont(Font.BOLD, 12));
		detailView.add(showSpreadingData);
		detailViewLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, showSpreadingData, 0, SpringLayout.HORIZONTAL_CENTER, detailView);
		detailViewLayout.putConstraint(SpringLayout.NORTH, showSpreadingData, 10, SpringLayout.SOUTH, histogramLikeView);
		detailView.setComponentZOrder(showSpreadingData, 0);
		
		showSpreadingData.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { if (isPlaying) { play.doClick(); } histogramLikeView.showDataTable(); } });
		
		selectionPlaceholder = new NeuralRacing.UIView(new Dimension(150, 225), NeuralRacing.darkGrayColor, 20);
		detailView.add(selectionPlaceholder);
		detailViewLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, selectionPlaceholder, 0, SpringLayout.HORIZONTAL_CENTER, detailView);
		detailViewLayout.putConstraint(SpringLayout.NORTH, selectionPlaceholder, 10, SpringLayout.SOUTH, showSpreadingData);
		detailView.setComponentZOrder(selectionPlaceholder, 0);
		
		bestIndivTitle.setForeground(Color.WHITE);
		bestIndivTitle.setFont(NeuralRacing.getSystemFont(Font.BOLD, 14));
		detailView.add(bestIndivTitle);
		detailViewLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, bestIndivTitle, 0, SpringLayout.HORIZONTAL_CENTER, detailView);
		detailViewLayout.putConstraint(SpringLayout.NORTH, bestIndivTitle, 10, SpringLayout.NORTH, selectionPlaceholder);
		detailView.setComponentZOrder(bestIndivTitle, 0);
		
		neuralView = new NeuralRacing.UINeuralView(new Dimension(130, 130), NeuralRacing.darkGrayColor, 0, NeuralRacing.selectedGreenTint, NeuralRacing.negativeOrange, Color.WHITE);
		detailView.add(neuralView);
		detailViewLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, neuralView, 0, SpringLayout.HORIZONTAL_CENTER, detailView);
		detailViewLayout.putConstraint(SpringLayout.NORTH, neuralView, 10, SpringLayout.SOUTH, bestIndivTitle);
		detailView.setComponentZOrder(neuralView, 0);
		
		saveBest = new NeuralRacing.UIButton(new Dimension(125, 30), NeuralRacing.darkGreenBackground, 10, () -> saveBest.title = LanguageManager.grabStringFromID("save"));
		saveBest.setForeground(Color.white);
		saveBest.setFont(NeuralRacing.getSystemFont(Font.BOLD, 12));
		detailView.add(saveBest);
		detailViewLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, saveBest, 0, SpringLayout.HORIZONTAL_CENTER, detailView);
		detailViewLayout.putConstraint(SpringLayout.NORTH, saveBest, 10, SpringLayout.SOUTH, neuralView);
		detailView.setComponentZOrder(saveBest, 0);
		
		saveBest.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent evt) {
				
				if (isPlaying) { play.doClick(); }
				
				String name = JOptionPane.showInputDialog(contentView, LanguageManager.grabStringFromID("giveAName") + " " + LanguageManager.grabStringFromID("indiv") + " !", LanguageManager.grabStringFromID("message"), JOptionPane.PLAIN_MESSAGE);
				if (name == null) { return; }
				if (NeuralRacing.networkList.containsKey(name)) {
					if (JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog(contentView, LanguageManager.grabStringFromID("an") + " " + LanguageManager.grabStringFromID("indiv") + " " + LanguageManager.grabStringFromID("alreadyExists") + "\n" + LanguageManager.grabStringFromID("ifCancel"), LanguageManager.grabStringFromID("warning"), JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE)) { return; }
				}
				
				NeuralRacing.networkList.put(name, neuralView.getNetwork().toStringRpz());
				NeuralRacing.saveNetworks();
			}
		});
		
		selectorListener.actionPerformed(new ActionEvent(systemSelector, 0, null));
		
		try { raceGrid = new RaceGrid(new EnhancedString(NeuralRacing.defaultRace), false); } catch (IllegalArgumentException exc) {}
		raceView = new RaceView(new Dimension((int) size.getWidth(), (int) size.getHeight()-50), raceGrid);
		carsView = new CarsView(new Dimension((int) size.getWidth(), (int) size.getHeight()-50), raceGrid, populationSize, (double) EvolutionView.defaultMaxSpeed/10, (double) EvolutionView.defaultMinSpeed/10, true);
		
		contentView.setLayout(null);
		contentView.add(carsView);
		contentView.add(raceView);
		
		scrollView = new JScrollPane(detailView);
		scrollView.setBorder(null);
		scrollView.getVerticalScrollBar().setPreferredSize(new Dimension(5, scrollView.getHeight()));
		scrollView.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
			
		 	@Override protected JButton createDecreaseButton(int orientation) {
		 		JButton button = new JButton();
		 		button.setPreferredSize(new Dimension(0, 0));
		    	return button;
		    }
		 
		    @Override protected JButton createIncreaseButton(int orientation) {
		    	JButton button = new JButton();
		 		button.setPreferredSize(new Dimension(0, 0));
		    	return button;
		    }
		    
			@Override protected void configureScrollBarColors(){
	            this.thumbColor = NeuralRacing.widespreadGreenishGreen;
	            this.trackColor = NeuralRacing.darkGreenBackground;
	        }
		});
		scrollView.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollView.getVerticalScrollBar().setUnitIncrement(12);
		scrollView.setPreferredSize(new Dimension(200, (int) (size.getHeight()-200)));
		
		this.add(headerView, BorderLayout.NORTH);
		this.add(scrollView, BorderLayout.EAST);
		this.add(contentView, BorderLayout.WEST);
	}
	
	@Override public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(backToMenu)) {
			if (clear()) { NeuralRacing.showView("menuPanel"); }
		}
	}
	
	/**
	 * Cette méthode permet de relancer le <code>Timer</code>
	 * du jeu, en prenant ainsi en compte les changements liés à une
	 * éventuelle nouvelle fréquence de rafraîchissement.
	 */
	public void restoreTimer() {
		gameClock = new Timer();
		gameClock.scheduleAtFixedRate(new TimerTask() {
			@Override public void run() {
				
				if (coalesces != null) { return; }
				coalesces = true;
				
				realTime++;

				if (realTime == 100) {
					realTime = 0;
					timeLeft--;
					hourglass.updateValue(100*timeLeft/defaultTimeLeft);
				}
				
				populationAlive = false;
				for (Car currentCar: carsView.cars) {
					if (currentCar.motorOn) {
						populationAlive = true;
						currentCar.drive(raceGrid, carsView.maximumSpeed, carsView.minimumSpeed);
					} else if (NeuralRacing.UserDefaults.fadingEnabled) {
						currentCar.alphaComposite = Math.max(0f, currentCar.alphaComposite - 0.1f);
					}
				}
				
				//Si la population est morte.
				if (!populationAlive || timeLeft <= 0) {
					
					if (defaultTimeLeft != newTimeLeft) { defaultTimeLeft = newTimeLeft; }
					
					for (int i = 0; i < populationSize; i++) {
						population.individuals[i].fitness = ((carsView.cars[i].laps * raceGrid.rewardLinesCount) + carsView.cars[i].rewardLinesCrossed.size());
					}
					
					carsView.cleanCarsGroup();
					
					neuralView.setNetwork(population.getFittest().toNeuralNet());
					neuralView.repaint();
					saveBest.setEnabled(true);
					
					bestFitness = population.getBestFitness();
					graphView.addPoint(generation, bestFitness);
					statisticsLabel.title = LanguageManager.grabStringFromID("score") + " : " + bestFitness;
					
					Map<Integer, Double> histogramData = population.getHistogramData();
					histogramLikeView.setPoints(histogramData);
					int sum = 0;
					for (int key: histogramData.keySet()) {
						sum += key;
					}
					average = sum/histogramData.size();
					histogramLabel.title = LanguageManager.grabStringFromID("average") + " : " + average;
					
					generation++;
					generationLabel.title = LanguageManager.grabStringFromID("generation") + " " + generation;
					
					population = BiologicalModel.Algorithm.evolvePopulation(population);
					
					carsView.preparePopulation(population);
					
					/*double meanSpeed = (minimumSpeed + maximumSpeed)/2;
					for (int i = 0; i < populationSize; i++) {
						carsView.cars[i].network = population.individuals[i].toNeuralNet();
						carsView.cars[i].type    = population.individuals[i].type;
						carsView.cars[i].motorOn = true;
						carsView.cars[i].speed   = (meanSpeed);
					}*/
					
					timeLeft = defaultTimeLeft;
					hourglass.updateValue(100);
					
				}
				repaint();
				coalesces = null;
			}
		}, 0L, (long) viewingFrequency);
	}
	
	/**
	 * Cette méthode permet de changer le paramètre <code>clean</code>
	 * de la vue tout en exécutant les changements adéquats : les paramètres
	 * figurant dans la partie "avant le départ" sont désactivés.
	 */
	public void setCleanToFalse() {
		populationCount.slider.setEnabled(false);
		minSpeed.slider.setEnabled(false);
		maxSpeed.slider.setEnabled(false);
		insertIndiv.setEnabled(false);
		clean = false;
	}
	
	/**
	 * Cette méthode permet de réinitialiser les données de la vue.
	 * 
	 * @return
	 * Faux si l'utilisateur a annulé le processus, vrai sinon.
	 */
	public boolean clear() {
		if (clean) { return true; }
		if (JOptionPane.showConfirmDialog(this, LanguageManager.grabStringFromID("giveUp"), LanguageManager.grabStringFromID("warning"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE) != JOptionPane.OK_OPTION) { return false; }
		
		if (isPlaying) { play.doClick(); }
		
		coalesces = null;
		bestFitness = average = -1;
		
		if (leftSelected) {
			populationCount.slider.setEnabled(true);
			minSpeed.slider.setEnabled(true);
			maxSpeed.slider.setEnabled(true);
			insertIndiv.setEnabled(true);
		}
		
		insertion.clear();;
		saveBest.setEnabled(false);
		carsView.cleanCarsGroup();
		generation = 0;
		timeLeft = defaultTimeLeft;
		hourglass.updateValue(100);
		
		clean = true;
		
		keepBest.clear();
		populationCount.clear();
		minSpeed.clear();
		maxSpeed.clear();
		mutationRate.clear();
		tournamentSize.clear();
		defaultTime.clear();
		
		repaint();
		graphView.clear();
		histogramLikeView.clear();
		neuralView.setNetwork(null);
		generationLabel.title = LanguageManager.grabStringFromID("generation") + " 0";
		statisticsLabel.title = LanguageManager.grabStringFromID("score") + " : -";
		histogramLabel.title  = LanguageManager.grabStringFromID("average") + " : -";
		return true;
	}
	
	/**
	 * Cette méthode converti un <code>double</code> en <code>String</code> tout en évitant
	 * les virgules suivies de zéros.
	 * 
	 * @param number
	 * Le nombre à convertir.
	 * 
	 * @return
	 * Le <code>String</code> ainsi formé.
	 * 
	 */
	public static String round(double number) {
		return (Math.round(number) == number ? "" + (int) number : "" + number);
	}
	
}
