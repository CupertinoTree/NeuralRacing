package neuralPckg;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SpringLayout;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicScrollBarUI;

/**
 * Cette sous-classe de <code>UIPanel</code> est la vue d'expérimentations.
 * L'utilisateur peut ajouter jusqu'à dix voitures, dont il choisit la couleur et le conducteur (humain ou artificiel).
 * Le laboratoire permet quant à lui de reproduire des individus ensemble afin d'en sauvegarder la progéniture, et de muter
 * un individu pour enregistrer le résultat dans la liste d'individus.
 * 
 * @author Ronan
 *
 * @see NeuralRacing.UIPanel
 */
public class ExperimentsView extends NeuralRacing.UIPanel implements ActionListener {
	
	private static final long serialVersionUID = 94532387434525632L;
		
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
	
	JLabel header = new JLabel();
	
	NeuralRacing.UIView   timePlaceholder,
						  paramsPlaceholder,
						  addDriverPlaceholder,
						  labPlaceholder,
						  reproductionPlaceholder,
						  mutationPlaceholder;
	
	NeuralRacing.UILabel  timeLabel,
						  labTitle;
	
	NeuralRacing.UIButton openGuide,
						  backToMenu,
						  openRace,
						  startOver,
						  play,
						  fastBackward,
						  fastForward,
						  addDriver,
						  chooseFirstIndiv,
						  chooseScdIndiv,
						  chooseTrdIndiv,
						  reproduce,
						  mutate;
	
	NeuralRacing.UIParameterSlider mutationRate,
								   minSpeed,
								   maxSpeed;
	
	NeuralRacing.UISelector mainSelector;
	
	JLabel reproductionLabel = new JLabel(),
		   selection1        = new JLabel(),
		   selection2        = new JLabel(),
		   selection3        = new JLabel(),
		   mutationLabel     = new JLabel();
	
	String selectedRace = "",
		   selectedIndiv1,
		   selectedIndiv2,
		   selectedIndiv3;
	
	boolean clean        = true,
			leftSelected = true,
			humanAdded   = false,
			isPlaying    = false;
	
	double viewingFrequency = 10;
	
	double [] speeds = { 0.125, 0.25, 0.5, 1, 2, 5, 10 };
	int speedIndex = 3;
	
	Timer mainTimer;
	
	/**Cette variable permet d'éviter que deux tâches du <code>Timer</code> soient lancées en même temps.*/
	Boolean coalesces;
	
	List<DriverView> driverViews     = new ArrayList<DriverView>();
	Set<Integer>     availableStyles = new TreeSet<Integer>();
	List<String>     networkKeys     = new ArrayList<String>();
	
	/**Si un conducteur humain est ajouté et que l'utilisateur change de langue, cette vue permet de traduire le nom du conducteur humain.*/
	DriverView transitView;
	
	/**
	 * Cette sous-classe de <code>UIPanel</code> est la vue d'expérimentations.
	 * L'utilisateur peut ajouter jusqu'à dix voitures, dont il choisit la couleur et le conducteur (humain ou artificiel).
	 * Le laboratoire permet quant à lui de reproduire des individus ensemble afin d'en sauvegarder la progéniture, et de muter
	 * un individu pour enregistrer le résultat dans la liste d'individus.
	 * 
	 * @param size
	 * La taille de la vue.
	 *
	 * @see NeuralRacing.UIPanel
	 */
	public ExperimentsView(Dimension size) {
		
		for (int index = 0; index < 10; index++) {
			availableStyles.add(index);
		}
		
		this.setPreferredSize(size);
		this.setLayout(new BorderLayout());
		headerView.setPreferredSize(new Dimension((int) (size.getWidth()-200), 100));
		detailView.setPreferredSize(new Dimension(200, 470));
		contentView.setPreferredSize(new Dimension((int) (size.getWidth()-200), (int) (size.getHeight()-200)));
		
		headerView.setBackground(NeuralRacing.darkerGrayColor);
		detailView.setBackground(NeuralRacing.darkGreenBackground);
		contentView.setBackground(Color.white);
		
		LanguageManager.registerPrimaryMutator(new Runnable() {
			@Override public void run() {
				if (humanAdded) {
					for (DriverView view: driverViews) {
						if (view.nameLabel.title.equals(LanguageManager.grabStringFromID("humanDriver"))) {
							transitView = view;
							break;
						}
					}
				}
			}
		});
		
		LanguageManager.registerUIElement(new LanguageManager.LanguageMutable() {
			@Override public void updateData() {
				header.setText(LanguageManager.grabStringFromID("experimentsTitle"));
				reproductionLabel.setText(LanguageManager.grabStringFromID("reproduction"));
				selection1.setText(selectedIndiv1 == null ? LanguageManager.grabStringFromID("noSelection") : selectedIndiv1);
				selection2.setText(selectedIndiv2 == null ? LanguageManager.grabStringFromID("noSelection") : selectedIndiv2);
				selection3.setText(selectedIndiv3 == null ? LanguageManager.grabStringFromID("noSelection") : selectedIndiv3);
				mutationLabel.setText(LanguageManager.grabStringFromID("mutationRate"));
				
				if (transitView != null) {
					transitView.nameLabel.title = LanguageManager.grabStringFromID("humanDriver");
					transitView.nameLabel.repaint();
					transitView = null;
				}
			}
		});
		
		headerViewLayout = new SpringLayout();
		headerView.setLayout(headerViewLayout);
		
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
				NeuralRacing.guide.openPage("experimentsView");
			}
		});
		
		backToMenu = new NeuralRacing.UIButton(new Dimension(100, 55), NeuralRacing.darkGrayColor, 20, () -> backToMenu.title = LanguageManager.grabStringFromID("menu"));
		backToMenu.setFont(NeuralRacing.getSystemFont(Font.BOLD, 25));
		backToMenu.setForeground(Color.white);
		backToMenu.addActionListener(this);
		headerView.add(backToMenu);
		headerViewLayout.putConstraint(SpringLayout.VERTICAL_CENTER, backToMenu, 0, SpringLayout.VERTICAL_CENTER, headerView);
		headerViewLayout.putConstraint(SpringLayout.WEST, backToMenu, 15, SpringLayout.EAST, openGuide);
		
		header.setFont(NeuralRacing.getSystemFont(Font.BOLD, 32));
		header.setForeground(Color.white);
		headerView.add(header);
		headerViewLayout.putConstraint(SpringLayout.VERTICAL_CENTER, header, 0, SpringLayout.VERTICAL_CENTER, headerView);
		headerViewLayout.putConstraint(SpringLayout.WEST, header, 16, SpringLayout.EAST, backToMenu);
		
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
				
				clear();
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
					
					NeuralRacing.experimentsView.repaint();
				}
		});
		
		detailViewLayout = new SpringLayout();
		detailView.setLayout(detailViewLayout);
		
		ActionListener selectorListener = new ActionListener() {
			@Override public void actionPerformed(ActionEvent evt) {
				
				leftSelected = evt.getID() == 0;
				
				updateDetailViewSize();
				
				timePlaceholder.setVisible(leftSelected);
				timeLabel.setVisible(leftSelected);
				play.setVisible(leftSelected);
				fastBackward.setVisible(leftSelected);
				fastForward.setVisible(leftSelected);
				startOver.setVisible(leftSelected);
				addDriverPlaceholder.setVisible(leftSelected && !availableStyles.isEmpty());
				addDriver.setVisible(leftSelected && !availableStyles.isEmpty());
				paramsPlaceholder.setVisible(leftSelected);
				maxSpeed.setVisible(leftSelected);
				minSpeed.setVisible(leftSelected);
				
				labPlaceholder.setVisible(!leftSelected);
				labTitle.setVisible(!leftSelected);
				reproductionPlaceholder.setVisible(!leftSelected);
				reproductionLabel.setVisible(!leftSelected);
				chooseFirstIndiv.setVisible(!leftSelected);
				selection1.setVisible(!leftSelected);
				chooseScdIndiv.setVisible(!leftSelected);
				selection2.setVisible(!leftSelected);
				reproduce.setVisible(!leftSelected);
				mutationPlaceholder.setVisible(!leftSelected);
				mutationLabel.setVisible(!leftSelected);
				chooseTrdIndiv.setVisible(!leftSelected);
				selection3.setVisible(!leftSelected);
				mutate.setVisible(!leftSelected);
				mutationRate.setVisible(!leftSelected);
				
				play.setEnabled(leftSelected);
				fastBackward.setEnabled(leftSelected);
				fastForward.setEnabled(leftSelected);
				startOver.setEnabled(leftSelected);
				addDriver.setEnabled(leftSelected && !isPlaying && clean);
				minSpeed.slider.setEnabled(leftSelected && !isPlaying);
				maxSpeed.slider.setEnabled(leftSelected && !isPlaying);
				
				chooseFirstIndiv.setEnabled(!leftSelected);
				chooseScdIndiv.setEnabled(!leftSelected);
				reproduce.setEnabled(!leftSelected);
				chooseTrdIndiv.setEnabled(!leftSelected);
				mutate.setEnabled(!leftSelected);
				mutationRate.slider.setEnabled(!leftSelected);
				
				for (DriverView currentView: driverViews) {
					currentView.setVisible(leftSelected);
					currentView.setEnabled(leftSelected && !isPlaying);
				}
			}
		};
		
		mainSelector = new NeuralRacing.UISelector(new Dimension(170, 60), NeuralRacing.selectedGreenTint, NeuralRacing.widespreadGreenishGreen, 25, selectorListener, NeuralRacing.mainSelectorLeftIcon, NeuralRacing.mainSelectorRightIcon);
		detailView.add(mainSelector);
		detailViewLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, mainSelector, 0, SpringLayout.HORIZONTAL_CENTER, detailView);
		detailViewLayout.putConstraint(SpringLayout.NORTH, mainSelector, 16, SpringLayout.NORTH, this);
		
		timePlaceholder = new NeuralRacing.UIView(new Dimension(170, 185), NeuralRacing.widespreadGreenishGreen, 20);
		detailView.add(timePlaceholder);
		detailViewLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, timePlaceholder, 0, SpringLayout.HORIZONTAL_CENTER, detailView);
		detailViewLayout.putConstraint(SpringLayout.NORTH, timePlaceholder, 10, SpringLayout.SOUTH, mainSelector);
		
		timeLabel = new NeuralRacing.UILabel(new Dimension(150, 36), NeuralRacing.darkGrayColor, 15, () -> timeLabel.title = LanguageManager.grabStringFromID("time") + " : x" + EvolutionView.round(speeds[speedIndex]));
		timeLabel.setForeground(Color.WHITE);
		timeLabel.setFont(NeuralRacing.getSystemFont(Font.BOLD, 16));
		detailView.add(timeLabel);
		detailViewLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, timeLabel, 0, SpringLayout.HORIZONTAL_CENTER, detailView);
		detailViewLayout.putConstraint(SpringLayout.NORTH, timeLabel, 10, SpringLayout.NORTH, timePlaceholder);
		detailView.setComponentZOrder(timeLabel, 0);
		
		play = new NeuralRacing.UIButton(new Dimension(30, 39), NeuralRacing.widespreadGreenishGreen, 0, null);
		play.setIcon(NeuralRacing.play);
		play.highlight = false;
		headerView.add(play);
		detailViewLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, play, 0, SpringLayout.HORIZONTAL_CENTER, detailView);
		detailViewLayout.putConstraint(SpringLayout.VERTICAL_CENTER, play, 0, SpringLayout.VERTICAL_CENTER, timePlaceholder);
		detailView.setComponentZOrder(play, 0);
		
		play.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				
				if (networkKeys.isEmpty()) { return; }
				
				//Le fait de manier des sliders, dans une JScrollPane, empêche
				//un humain de conduire, car les flèches du clavier permettent alors de se déplacer
				//dans la JScrollPane. Naturellement, si le panneau principal récupère l'attention, il pourra
				//à nouveau écouter les appuis de l'utilisateur.
				NeuralRacing.mainPanel.grabFocus();
				
				isPlaying = !isPlaying;
				
				if (isPlaying) {
					
					if (clean) {
						
						if (minSpeed.slider.getValue() >= maxSpeed.slider.getValue()) {
							JOptionPane.showMessageDialog(contentView, LanguageManager.grabStringFromID("speedOrder"), LanguageManager.grabStringFromID("error"), JOptionPane.ERROR_MESSAGE);
							return;
						}
						
						carsView.createCarsGroup(networkKeys.size());
						
						for (int i = 0; i < networkKeys.size(); i++) {
							boolean isHuman = networkKeys.get(i).equals(LanguageManager.grabStringFromID("humanDriver"));
							carsView.cars[i].network = isHuman ? null : driverViews.get(i).neuralView.getNetwork();
							carsView.cars[i].type    = driverViews.get(i).imageIndex;
							carsView.cars[i].motorOn = true;
							if (isHuman) { carsView.humanDriverIndex = i; }
						}
					}
					
					play.setIcon(NeuralRacing.pause);
					setCleanToFalse();
					restoreTimer();
					
				} else {
					play.setIcon(NeuralRacing.play);
					
					mainTimer.cancel();
					mainTimer.purge();
					
				}
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
				timeLabel.title = LanguageManager.grabStringFromID("time") + " : x" + EvolutionView.round(speeds[speedIndex]);
				repaint();
				if (!isPlaying) { return; }
				mainTimer.cancel();
				mainTimer.purge();
				restoreTimer();
				NeuralRacing.mainPanel.grabFocus();
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
		
		paramsPlaceholder = new NeuralRacing.UIView(new Dimension(170, 176), NeuralRacing.widespreadGreenishGreen, 20);
		detailView.add(paramsPlaceholder);
		detailViewLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, paramsPlaceholder, 0, SpringLayout.HORIZONTAL_CENTER, detailView);
		detailViewLayout.putConstraint(SpringLayout.NORTH, paramsPlaceholder, 10, SpringLayout.SOUTH, timePlaceholder);
		
		minSpeed = new NeuralRacing.UIParameterSlider(new Dimension(150, 70), NeuralRacing.darkGrayColor, NeuralRacing.widespreadGreenishGreen, Color.white, 20, "px/ms", 1, 100, EvolutionView.defaultMinSpeed, () -> minSpeed.title = LanguageManager.grabStringFromID("minSpeed") + " : ");
		minSpeed.label.setForeground(Color.WHITE);
		minSpeed.label.setFont(NeuralRacing.getSystemFont(Font.BOLD, 12));
		detailViewLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, minSpeed, 0, SpringLayout.HORIZONTAL_CENTER, detailView);
		detailViewLayout.putConstraint(SpringLayout.NORTH, minSpeed, 12, SpringLayout.NORTH, paramsPlaceholder);
		detailView.setComponentZOrder(minSpeed, 0);
		
		maxSpeed = new NeuralRacing.UIParameterSlider(new Dimension(150, 70), NeuralRacing.darkGrayColor, NeuralRacing.widespreadGreenishGreen, Color.white, 20, "px/ms", 1, 100, EvolutionView.defaultMaxSpeed, () -> maxSpeed.title = LanguageManager.grabStringFromID("maxSpeed") + " : ");
		maxSpeed.label.setForeground(Color.WHITE);
		maxSpeed.label.setFont(NeuralRacing.getSystemFont(Font.BOLD, 12));
		detailViewLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, maxSpeed, 0, SpringLayout.HORIZONTAL_CENTER, detailView);
		detailViewLayout.putConstraint(SpringLayout.NORTH, maxSpeed, 12, SpringLayout.SOUTH, minSpeed);
		detailView.setComponentZOrder(maxSpeed, 0);
		
		ChangeListener changeListener = new ChangeListener() {

			@Override public void stateChanged(ChangeEvent e) {
				if (e.getSource() == minSpeed.slider) {
					
					if (!clean) { return; }
					carsView.minimumSpeed = (double) minSpeed.slider.getValue()/10d;
					
				} else if (e.getSource() == maxSpeed.slider) {
					
					if (!clean) { return; }
					carsView.maximumSpeed = (double) maxSpeed.slider.getValue()/10d;
					
				}
			}
			
		};
		
		minSpeed.slider.addChangeListener(changeListener);
		maxSpeed.slider.addChangeListener(changeListener);
		
		addDriverPlaceholder = new NeuralRacing.UIView(new Dimension(170, 50), NeuralRacing.widespreadGreenishGreen, 20);
		detailView.add(addDriverPlaceholder);
		detailViewLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, addDriverPlaceholder, 0, SpringLayout.HORIZONTAL_CENTER, detailView);
		detailViewLayout.putConstraint(SpringLayout.NORTH, addDriverPlaceholder, 10, SpringLayout.SOUTH, paramsPlaceholder);
		
		addDriver = new NeuralRacing.UIButton(new Dimension(160, 40), NeuralRacing.darkGrayColor, 10, () -> addDriver.title = LanguageManager.grabStringFromID("addDriver"));
		addDriver.setForeground(Color.WHITE);
		addDriver.setFont(NeuralRacing.getSystemFont(Font.BOLD, 14));
		detailView.add(addDriver);
		detailViewLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, addDriver, 0, SpringLayout.HORIZONTAL_CENTER, addDriverPlaceholder);
		detailViewLayout.putConstraint(SpringLayout.VERTICAL_CENTER, addDriver, 0, SpringLayout.VERTICAL_CENTER, addDriverPlaceholder);
		detailView.setComponentZOrder(addDriver, 0);
		
		addDriver.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				
				clear();
				
				List<String> selection = new ArrayList<String>();
				
				if (!humanAdded) {
					selection.add(LanguageManager.grabStringFromID("humanDriver"));
				}
				
				selection.addAll(NeuralRacing.networkList.keySet());
				
				String name = (String) JOptionPane.showInputDialog(contentView, LanguageManager.grabStringFromID("brkp1") + "\n" + LanguageManager.grabStringFromID("brkp2") + "\n" + LanguageManager.grabStringFromID("brkp3") + "\n" + LanguageManager.grabStringFromID("brkp4"), LanguageManager.grabStringFromID("newCar"), JOptionPane.PLAIN_MESSAGE, null, selection.toArray(), null);
				
				if (name == null) { return; }
				if (name == LanguageManager.grabStringFromID("humanDriver")) { humanAdded = true; }
				
				addDriverView(name);
			}
		});
		
		labPlaceholder = new NeuralRacing.UIView(new Dimension(170, 530), NeuralRacing.widespreadGreenishGreen, 20);
		detailView.add(labPlaceholder);
		detailViewLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, labPlaceholder, 0, SpringLayout.HORIZONTAL_CENTER, detailView);
		detailViewLayout.putConstraint(SpringLayout.NORTH, labPlaceholder, 10, SpringLayout.SOUTH, mainSelector);
		
		labTitle = new NeuralRacing.UILabel(new Dimension(150, 40), NeuralRacing.darkGrayColor, 20, () -> labTitle.title = LanguageManager.grabStringFromID("lab"));
		labTitle.setFont(NeuralRacing.getSystemFont(Font.BOLD, 16));
		labTitle.setForeground(Color.white);
		detailView.add(labTitle);
		detailViewLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, labTitle, 0, SpringLayout.HORIZONTAL_CENTER, detailView);
		detailViewLayout.putConstraint(SpringLayout.NORTH, labTitle, 10, SpringLayout.NORTH, labPlaceholder);
		detailView.setComponentZOrder(labTitle, 0);
		
		reproductionPlaceholder = new NeuralRacing.UIView(new Dimension(150, 220), NeuralRacing.darkGrayColor, 20);
		detailView.add(reproductionPlaceholder);
		detailViewLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, reproductionPlaceholder, 0, SpringLayout.HORIZONTAL_CENTER, detailView);
		detailViewLayout.putConstraint(SpringLayout.NORTH, reproductionPlaceholder, 10, SpringLayout.SOUTH, labTitle);
		detailView.setComponentZOrder(reproductionPlaceholder, 0);
		
		reproductionLabel.setForeground(Color.WHITE);
		reproductionLabel.setFont(NeuralRacing.getSystemFont(Font.BOLD, 14));
		detailView.add(reproductionLabel);
		detailViewLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, reproductionLabel, 0, SpringLayout.HORIZONTAL_CENTER, detailView);
		detailViewLayout.putConstraint(SpringLayout.NORTH, reproductionLabel, 10, SpringLayout.NORTH, reproductionPlaceholder);
		detailView.setComponentZOrder(reproductionLabel, 0);
		
		chooseFirstIndiv = new NeuralRacing.UIButton(new Dimension(130, 30), NeuralRacing.selectedGreenTint, 10, () -> chooseFirstIndiv.title = LanguageManager.grabStringFromID("chooseIndivShort"));
		chooseFirstIndiv.setForeground(Color.WHITE);
		chooseFirstIndiv.setFont(NeuralRacing.getSystemFont(Font.BOLD, 12));
		detailView.add(chooseFirstIndiv);
		detailViewLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, chooseFirstIndiv, 0, SpringLayout.HORIZONTAL_CENTER, detailView);
		detailViewLayout.putConstraint(SpringLayout.NORTH, chooseFirstIndiv, 10, SpringLayout.SOUTH, reproductionLabel);
		detailView.setComponentZOrder(chooseFirstIndiv, 0);
		
		chooseFirstIndiv.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				
				if (NeuralRacing.networkList.isEmpty()) { return; }
				
				selectedIndiv1 = (String) JOptionPane.showInputDialog(contentView, LanguageManager.grabStringFromID("chooseIndiv"), LanguageManager.grabStringFromID("message"), JOptionPane.PLAIN_MESSAGE, null, NeuralRacing.networkList.keySet().toArray(), selectedIndiv1);
				selection1.setText(selectedIndiv1 == null ? LanguageManager.grabStringFromID("noSelection") : selectedIndiv1);
			}
		});
		
		selection1.setForeground(NeuralRacing.widespreadGreenishGreen);
		selection1.setFont(NeuralRacing.getSystemFont(Font.BOLD, 12));
		detailView.add(selection1);
		detailViewLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, selection1, 0, SpringLayout.HORIZONTAL_CENTER, detailView);
		detailViewLayout.putConstraint(SpringLayout.NORTH, selection1, 10, SpringLayout.SOUTH, chooseFirstIndiv);
		detailView.setComponentZOrder(selection1, 0);
		
		chooseScdIndiv = new NeuralRacing.UIButton(new Dimension(130, 30), NeuralRacing.selectedGreenTint, 10, () -> chooseScdIndiv.title = LanguageManager.grabStringFromID("chooseIndivShort"));
		chooseScdIndiv.setForeground(Color.WHITE);
		chooseScdIndiv.setFont(NeuralRacing.getSystemFont(Font.BOLD, 12));
		detailView.add(chooseScdIndiv);
		detailViewLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, chooseScdIndiv, 0, SpringLayout.HORIZONTAL_CENTER, detailView);
		detailViewLayout.putConstraint(SpringLayout.NORTH, chooseScdIndiv, 10, SpringLayout.SOUTH, selection1);
		detailView.setComponentZOrder(chooseScdIndiv, 0);
		
		chooseScdIndiv.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				
				if (NeuralRacing.networkList.isEmpty()) { return; }
				
				selectedIndiv2 = (String) JOptionPane.showInputDialog(contentView, LanguageManager.grabStringFromID("chooseIndiv"), LanguageManager.grabStringFromID("message"), JOptionPane.PLAIN_MESSAGE, null, NeuralRacing.networkList.keySet().toArray(), selectedIndiv2);
				selection2.setText(selectedIndiv2 == null ? LanguageManager.grabStringFromID("noSelection") : selectedIndiv2);
			}
		});
		
		selection2.setForeground(NeuralRacing.widespreadGreenishGreen);
		selection2.setFont(NeuralRacing.getSystemFont(Font.BOLD, 12));
		detailView.add(selection2);
		detailViewLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, selection2, 0, SpringLayout.HORIZONTAL_CENTER, detailView);
		detailViewLayout.putConstraint(SpringLayout.NORTH, selection2, 10, SpringLayout.SOUTH, chooseScdIndiv);
		detailView.setComponentZOrder(selection2, 0);
		
		reproduce = new NeuralRacing.UIButton(new Dimension(130, 30), NeuralRacing.selectedGreenTint, 10, () -> reproduce.title = LanguageManager.grabStringFromID("reproduce"));
		reproduce.setForeground(Color.WHITE);
		reproduce.setFont(NeuralRacing.getSystemFont(Font.BOLD, 12));
		detailView.add(reproduce);
		detailViewLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, reproduce, 0, SpringLayout.HORIZONTAL_CENTER, detailView);
		detailViewLayout.putConstraint(SpringLayout.NORTH, reproduce, 10, SpringLayout.SOUTH, selection2);
		detailView.setComponentZOrder(reproduce, 0);
		
		reproduce.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				
				if (selectedIndiv1 == null || selectedIndiv2 == null) {
					JOptionPane.showMessageDialog(contentView, LanguageManager.grabStringFromID("select2Indiv"), LanguageManager.grabStringFromID("error"), JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				if (!NeuralRacing.networkList.containsKey(selectedIndiv1)) {
					JOptionPane.showMessageDialog(contentView, LanguageManager.grabStringFromID("firstIndivLost"), LanguageManager.grabStringFromID("error"), JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				if (!NeuralRacing.networkList.containsKey(selectedIndiv2)) {
					JOptionPane.showMessageDialog(contentView, LanguageManager.grabStringFromID("scdIndivLost"), LanguageManager.grabStringFromID("error"), JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				BiologicalModel.Individual indiv = BiologicalModel.Algorithm.reproduce((new NeuralModel.NeuralNetwork(new EnhancedString(NeuralRacing.networkList.get(selectedIndiv1)))).toIndividual(), (new NeuralModel.NeuralNetwork(new EnhancedString(NeuralRacing.networkList.get(selectedIndiv2)))).toIndividual());
				String name = JOptionPane.showInputDialog(contentView, LanguageManager.grabStringFromID("successfulOp") + "\n" + LanguageManager.grabStringFromID("giveANameIndiv"), LanguageManager.grabStringFromID("save"), JOptionPane.PLAIN_MESSAGE);
				if (name == null) { return; }
				if (NeuralRacing.networkList.containsKey(name)) {
					if (JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog(contentView, LanguageManager.grabStringFromID("an") + " " + LanguageManager.grabStringFromID("indiv") + " " + LanguageManager.grabStringFromID("alreadyExists") + "\n" + LanguageManager.grabStringFromID("ifCancel"), LanguageManager.grabStringFromID("warning"), JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE));
					{ return; }
				}
				
				NeuralRacing.networkList.put(name, indiv.toNeuralNet().toStringRpz());
				NeuralRacing.saveNetworks();
			}
		});
		
		mutationPlaceholder = new NeuralRacing.UIView(new Dimension(150, 150), NeuralRacing.darkGrayColor, 20);
		detailView.add(mutationPlaceholder);
		detailViewLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, mutationPlaceholder, 0, SpringLayout.HORIZONTAL_CENTER, detailView);
		detailViewLayout.putConstraint(SpringLayout.NORTH, mutationPlaceholder, 10, SpringLayout.SOUTH, reproductionPlaceholder);
		detailView.setComponentZOrder(mutationPlaceholder, 0);
		
		mutationLabel.setForeground(Color.WHITE);
		mutationLabel.setFont(NeuralRacing.getSystemFont(Font.BOLD, 14));
		detailView.add(mutationLabel);
		detailViewLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, mutationLabel, 0, SpringLayout.HORIZONTAL_CENTER, detailView);
		detailViewLayout.putConstraint(SpringLayout.NORTH, mutationLabel, 10, SpringLayout.NORTH, mutationPlaceholder);
		detailView.setComponentZOrder(mutationLabel, 0);
		
		chooseTrdIndiv = new NeuralRacing.UIButton(new Dimension(130, 30), NeuralRacing.selectedGreenTint, 10, () -> chooseTrdIndiv.title = LanguageManager.grabStringFromID("chooseIndivShort"));
		chooseTrdIndiv.setForeground(Color.WHITE);
		chooseTrdIndiv.setFont(NeuralRacing.getSystemFont(Font.BOLD, 12));
		detailView.add(chooseTrdIndiv);
		detailViewLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, chooseTrdIndiv, 0, SpringLayout.HORIZONTAL_CENTER, detailView);
		detailViewLayout.putConstraint(SpringLayout.NORTH, chooseTrdIndiv, 10, SpringLayout.SOUTH, mutationLabel);
		detailView.setComponentZOrder(chooseTrdIndiv, 0);
		
		chooseTrdIndiv.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				
				if (NeuralRacing.networkList.isEmpty()) { return; }
				
				selectedIndiv3 = (String) JOptionPane.showInputDialog(contentView, LanguageManager.grabStringFromID("chooseIndiv"), LanguageManager.grabStringFromID("message"), JOptionPane.PLAIN_MESSAGE, null, NeuralRacing.networkList.keySet().toArray(), selectedIndiv3);
				selection3.setText(selectedIndiv3 == null ? LanguageManager.grabStringFromID("noSelection") : selectedIndiv3);
			}
		});
		
		selection3.setForeground(NeuralRacing.widespreadGreenishGreen);
		selection3.setFont(NeuralRacing.getSystemFont(Font.BOLD, 12));
		detailView.add(selection3);
		detailViewLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, selection3, 0, SpringLayout.HORIZONTAL_CENTER, detailView);
		detailViewLayout.putConstraint(SpringLayout.NORTH, selection3, 10, SpringLayout.SOUTH, chooseTrdIndiv);
		detailView.setComponentZOrder(selection3, 0);
		
		mutate = new NeuralRacing.UIButton(new Dimension(130, 30), NeuralRacing.selectedGreenTint, 10, () -> mutate.title = LanguageManager.grabStringFromID("mutate"));
		mutate.setForeground(Color.WHITE);
		mutate.setFont(NeuralRacing.getSystemFont(Font.BOLD, 12));
		detailView.add(mutate);
		detailViewLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, mutate, 0, SpringLayout.HORIZONTAL_CENTER, detailView);
		detailViewLayout.putConstraint(SpringLayout.NORTH, mutate, 10, SpringLayout.SOUTH, selection3);
		detailView.setComponentZOrder(mutate, 0);
		
		mutate.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				
				if (selectedIndiv3 == null) {
					JOptionPane.showMessageDialog(contentView, LanguageManager.grabStringFromID("select1Indiv"), LanguageManager.grabStringFromID("error"), JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				if (!NeuralRacing.networkList.containsKey(selectedIndiv3)) {
					JOptionPane.showMessageDialog(contentView, LanguageManager.grabStringFromID("indivLost"), LanguageManager.grabStringFromID("error"), JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				float previousMutationRate = BiologicalModel.Algorithm.mutationRate;
				BiologicalModel.Algorithm.mutationRate = (float) mutationRate.slider.getValue();
				
				BiologicalModel.Individual individual = BiologicalModel.Algorithm.mutate((new NeuralModel.NeuralNetwork(new EnhancedString(NeuralRacing.networkList.get(selectedIndiv3)))).toIndividual());
				
				String name = JOptionPane.showInputDialog(contentView, LanguageManager.grabStringFromID("successfulOp") + "\n" + LanguageManager.grabStringFromID("giveANameIndiv"), LanguageManager.grabStringFromID("save"), JOptionPane.PLAIN_MESSAGE);
				if (name == null) { return; }
				
				if (NeuralRacing.networkList.containsKey(name)) {
					if (JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog(contentView, LanguageManager.grabStringFromID("an") + " " + LanguageManager.grabStringFromID("indiv") + " " + LanguageManager.grabStringFromID("alreadyExists") + "\n" + LanguageManager.grabStringFromID("ifCancel"), LanguageManager.grabStringFromID("Warning"), JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE)) { return; }
				}
				
				NeuralRacing.networkList.put(name, individual.toNeuralNet().toStringRpz());
				NeuralRacing.saveNetworks();
				
				BiologicalModel.Algorithm.mutationRate = previousMutationRate;
			}
		});
		
		mutationRate = new NeuralRacing.UIParameterSlider(new Dimension(150, 70), NeuralRacing.darkGrayColor, NeuralRacing.widespreadGreenishGreen, Color.white, 20, "%", 0, 100, 5, () -> mutationRate.title = LanguageManager.grabStringFromID("mutationRate") + " : ");
		mutationRate.label.setForeground(Color.WHITE);
		mutationRate.label.setFont(NeuralRacing.getSystemFont(Font.BOLD, 12));
		detailView.add(mutationRate);
		detailViewLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, mutationRate, 0, SpringLayout.HORIZONTAL_CENTER, detailView);
		detailViewLayout.putConstraint(SpringLayout.NORTH, mutationRate, 10, SpringLayout.SOUTH, mutationPlaceholder);
		detailView.setComponentZOrder(mutationRate, 0);
		
		selectorListener.actionPerformed(new ActionEvent(mainSelector, 0, null));
		
		try { raceGrid = new RaceGrid(new EnhancedString(NeuralRacing.defaultRace), false); } catch (IllegalArgumentException exc) {}
		raceView = new RaceView(new Dimension((int) size.getWidth(), (int) size.getHeight()-50), raceGrid);
		carsView = new CarsView(new Dimension((int) size.getWidth(), (int) size.getHeight()-50), raceGrid, 0, (double) EvolutionView.defaultMaxSpeed/10, (double) EvolutionView.defaultMinSpeed/10, false);
		
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
		scrollView.getVerticalScrollBar().setUnitIncrement(8);
		scrollView.setPreferredSize(new Dimension(200, (int) (size.getHeight()-200)));
		
		this.add(headerView, BorderLayout.NORTH);
		this.add(scrollView, BorderLayout.EAST);
		this.add(contentView, BorderLayout.WEST);
	}
	
	@Override public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(backToMenu)) {
			clear();
			NeuralRacing.showView("menuPanel");
		}
	}
	
	@Override public void keyActionDetected(String keyPressed) {
		carsView.keyActionDetected(keyPressed);
	}
	
	/**
	 * Cette méthode permet de relancer le <code>Timer</code>
	 * du jeu, en prenant ainsi en compte les changements liés à une
	 * éventuelle nouvelle fréquence de rafraîchissement.
	 */
	public void restoreTimer() {
		mainTimer = new Timer();
		
		mainTimer.scheduleAtFixedRate(new TimerTask() {
			@Override public void run() {
				
				if (coalesces != null) { return; }
				
				coalesces = true;
				for (Car currentCar: carsView.cars) {
					
					if (currentCar.motorOn) {
						currentCar.drive(raceGrid, carsView.maximumSpeed, carsView.minimumSpeed);
					} else {
						currentCar.motorOn = true;
						currentCar.carWash(carsView.minimumSpeed, carsView.origin.getX(), carsView.origin.getY(), carsView.originalDirection);
					}
				}
				
				contentView.repaint();
				
				coalesces = null;
				
			}
		}, 0L, (long) viewingFrequency);
	}
	
	/**
	 * Cette méthode permet de changer la hauteur de la DetailView en fonction
	 * du nombre de composants qui lui ont été ajoutés.
	 */
	public void updateDetailViewSize() {
		if (!leftSelected) {
			detailView.setPreferredSize(new Dimension(200, 630));
		} else {
			//On en profite pour mettre à jour displayFakeCarStyle
			if (clean) { if (!driverViews.isEmpty()) { carsView.displayFakeCarStyle = driverViews.get(driverViews.size()-1).imageIndex; carsView.repaint(); } }
			detailView.setPreferredSize(new Dimension(200, Math.max(470, 470 + (availableStyles.isEmpty() ? 0 : 65) + driverViews.size() * 200)));
		}
	}
	
	/**
	 * Cette méthode permet d'ajouter une vue de conducteur.
	 * 
	 * @param driverName
	 * Le nom du conducteur à ajouter.
	 */
	public void addDriverView(String driverName) {
		
		DriverView view = new DriverView(driverName, availableStyles.toArray(new Integer [0])[0]);
		
		detailViewLayout.removeLayoutComponent(addDriverPlaceholder);
		detailView.add(view);
		detailViewLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, view, 0, SpringLayout.HORIZONTAL_CENTER, detailView);
		detailViewLayout.putConstraint(SpringLayout.NORTH, view, 10, SpringLayout.SOUTH, driverViews.isEmpty() ? paramsPlaceholder : driverViews.get(driverViews.size()-1));
		detailViewLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, addDriverPlaceholder, 0, SpringLayout.HORIZONTAL_CENTER, detailView);
		detailViewLayout.putConstraint(SpringLayout.NORTH, addDriverPlaceholder, 10, SpringLayout.SOUTH, view);
		
		driverViews.add(view);
		networkKeys.add(driverName);
		availableStyles.remove(view.imageIndex);
		
		if (availableStyles.isEmpty()) {
			addDriverPlaceholder.setVisible(false);
			addDriver.setVisible(false);
			addDriver.setEnabled(false);
		}
		
		updateDetailViewSize();
		scrollView.getViewport().revalidate();
	}
	
	/**
	 * Cette méthode permet de retirer une vue de conducteur.
	 * 
	 * @param view
	 * La vue à retirer.
	 */
	public void removeDriver(DriverView view) {
		
		if (networkKeys.size() == 1) {
			carsView.displayFakeCarStyle = -1;
			carsView.repaint();
		}
		
		int index = driverViews.indexOf(view);
		JPanel previousView = (index == 0) ? paramsPlaceholder : driverViews.get(index-1),
			   nextView = (index == driverViews.size()-1) ? addDriverPlaceholder : driverViews.get(index+1);
		
		detailViewLayout.removeLayoutComponent(view);
		detailView.remove(view);
		
		detailViewLayout.removeLayoutComponent(nextView);
		detailViewLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, nextView, 0, SpringLayout.HORIZONTAL_CENTER, detailView);
		detailViewLayout.putConstraint(SpringLayout.NORTH, nextView, 10, SpringLayout.SOUTH, previousView);
		
		driverViews.remove(index);
		networkKeys.remove(index);
		
		if (availableStyles.size() == 0) {
			addDriverPlaceholder.setVisible(true);
			addDriver.setVisible(true);
			addDriver.setEnabled(true);
		}
		
		if (view.nameLabel.title.equals(LanguageManager.grabStringFromID("humanDriver"))) {
			carsView.humanDriverIndex = -1;
			humanAdded = false;
		}
		
		view.releaseFromMemory();
		
		availableStyles.add(view.imageIndex);
		updateDetailViewSize();
		scrollView.getViewport().revalidate();
		
	}
	
	/**
	 * Cette méthode permet de changer le paramètre <code>clean</code>
	 * de la vue tout en exécutant les changements adéquats : les vues de conducteurs
	 * et les paramètres de vitesse sont alors désactivés.
	 */
	public void setCleanToFalse() {
		if (!clean) { return; }
		
		//for (DriverView view: driverViews) { view.setEnabled(false); }
		
		minSpeed.slider.setEnabled(false);
		maxSpeed.slider.setEnabled(false);
		
		//addDriver.setEnabled(false);
		
		clean = false;
	}
	
	/**Cette méthode permet de réinitialiser les données de la vue.*/
	public void clear() {
		
		if (clean) { return; }
		
		if (isPlaying) { play.doClick(); }
		
		coalesces = null;
		
		//for (DriverView view: driverViews) { view.setEnabled(true); }
		
		//addDriver.setEnabled(true);
		
		//for (Car currentCar: carsView.cars) { currentCar.carWash(); }
		
		carsView.cleanCarsGroup();
		carsView.populationSize = 0;
		carsView.displayFakeCarStyle = driverViews.get(driverViews.size()-1).imageIndex;
		carsView.repaint();
		
		minSpeed.slider.setEnabled(leftSelected);
		maxSpeed.slider.setEnabled(leftSelected);
		
		selectedIndiv1 = selectedIndiv2 = selectedIndiv3 = null;
		
		selection1.setText(LanguageManager.grabStringFromID("noSelection"));
		selection2.setText(LanguageManager.grabStringFromID("noSelection"));
		selection3.setText(LanguageManager.grabStringFromID("noSelection"));
		mutationRate.clear();
		
		repaint();
		clean = true;
	}
	
	/**
	 * Cette sous-classe de <code>JPanel</code> permet d'indiquer à l'utilisateur
	 * la nature de l'individu qu'il a ajouté. La vue affiche son nom et son réseau de
	 * neurones. Elle permet également à l'utilisateur de choisir la couleur du véhicule,
	 * ainsi que de le retirer.
	 * 
	 * @author Ronan
	 *
	 * @see JPanel
	 */
	public static class DriverView extends JPanel {
		
		private static final long serialVersionUID = 4743753872244176706L;
		
		SpringLayout layout = new SpringLayout();
		
		NeuralRacing.UINeuralView neuralView;
		
		NeuralRacing.UILabel      nameLabel,
								  driverIconView;
		
		NeuralRacing.UIView       backgroundView;
		
		NeuralRacing.UIButton     remove,
								  carImage;
		
		/**L'index du style sélectionné pour la voiture.*/
		int imageIndex;
		
		public DriverView(String driverName, int imageIdx) {
			super();
			this.setPreferredSize(new Dimension(170, 190));
			this.setBackground(NeuralRacing.darkGreenBackground);
			this.setLayout(layout);
			this.imageIndex = imageIdx;
			
			carImage = new NeuralRacing.UIButton(new Dimension(40, 80), NeuralRacing.selectedGreenTint, 10, null);
			carImage.setIcon(getCarImage(NeuralRacing.cars[imageIndex]));
			this.add(carImage);
			layout.putConstraint(SpringLayout.WEST, carImage, 0, SpringLayout.WEST, this);
			layout.putConstraint(SpringLayout.VERTICAL_CENTER, carImage, 0, SpringLayout.VERTICAL_CENTER, this);
			
			ActionListener carChoiceListener = new ActionListener() {
				@Override public void actionPerformed(ActionEvent evt) {
					
					NeuralRacing.experimentsView.clear();
					
					int indexOfChoice = getIndexOf(((JMenuItem) evt.getSource()).getText());
					
					carImage.setIcon(getCarImage(NeuralRacing.cars[indexOfChoice]));
					NeuralRacing.experimentsView.availableStyles.remove(indexOfChoice);
					NeuralRacing.experimentsView.availableStyles.add(imageIndex);
					
					//Si la voiture est la dernière dans la liste, on met à jour le displayFakeCarStyle
					if (NeuralRacing.experimentsView.driverViews.indexOf(getThis()) == NeuralRacing.experimentsView.driverViews.size()-1) {
						NeuralRacing.experimentsView.carsView.displayFakeCarStyle = indexOfChoice;
						NeuralRacing.experimentsView.carsView.repaint();
					}
					
					imageIndex = indexOfChoice;
					getThis().repaint();
				}
			};
			
			carImage.addActionListener(new ActionListener() {
				@Override public void actionPerformed(ActionEvent evt) {
					
					JPopupMenu carChooser = new JPopupMenu();
					carChooser.setBackground(NeuralRacing.darkGrayColor);
					carChooser.setFont(NeuralRacing.getSystemFont(Font.BOLD, 14));
					
					for (int style: NeuralRacing.experimentsView.availableStyles) {
						if (style == getThis().imageIndex) { continue; }
						JMenuItem item = new JMenuItem(NeuralRacing.carsName[style], new ImageIcon(getCarImage(NeuralRacing.cars[style])));
						item.setBackground(NeuralRacing.darkGrayColor);
						item.setForeground(Color.WHITE);
						item.addActionListener(carChoiceListener);
						carChooser.add(item);
					}
					
					carChooser.show(getThis(), 0, 0);
					
				}
			});
			
			backgroundView = new NeuralRacing.UIView(new Dimension(120, 190), NeuralRacing.darkGrayColor, 20);
			this.add(backgroundView);
			layout.putConstraint(SpringLayout.EAST, backgroundView, 0, SpringLayout.EAST, this);
			layout.putConstraint(SpringLayout.VERTICAL_CENTER, backgroundView, 0, SpringLayout.VERTICAL_CENTER, this);
			
			nameLabel = new NeuralRacing.UILabel(new Dimension(100, 30), NeuralRacing.darkGrayColor, 20, null);
			nameLabel.title = driverName;
			nameLabel.setFont(NeuralRacing.getSystemFont(Font.BOLD, 10));
			nameLabel.setForeground(Color.white);
			this.add(nameLabel);
			layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, nameLabel, 0, SpringLayout.HORIZONTAL_CENTER, backgroundView);
			layout.putConstraint(SpringLayout.NORTH, nameLabel, 5, SpringLayout.NORTH, backgroundView);
			this.setComponentZOrder(nameLabel, 0);
			
			if (!driverName.equals(LanguageManager.grabStringFromID("humanDriver"))) { 
				neuralView = new NeuralRacing.UINeuralView(new Dimension(100, 100), NeuralRacing.darkGrayColor, 0, NeuralRacing.selectedGreenTint, NeuralRacing.negativeOrange, Color.WHITE);
				neuralView.setNetwork(new NeuralModel.NeuralNetwork(new EnhancedString(NeuralRacing.networkList.get(driverName))));
				this.add(neuralView);
				layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, neuralView, 0, SpringLayout.HORIZONTAL_CENTER, backgroundView);
				layout.putConstraint(SpringLayout.NORTH, neuralView, 5, SpringLayout.SOUTH, nameLabel);
				this.setComponentZOrder(neuralView, 0);
			} else {
				//Dans le cas d'un conducteur humain, on remplace la NeuralView par une icône de cerveau.
				driverIconView = new NeuralRacing.UILabel(new Dimension(100, 100), NeuralRacing.darkGrayColor, 0, null);
				driverIconView.setIcon(NeuralRacing.brainIcon);
				this.add(driverIconView);
				layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, driverIconView, 0, SpringLayout.HORIZONTAL_CENTER, backgroundView);
				layout.putConstraint(SpringLayout.NORTH, driverIconView, 5, SpringLayout.SOUTH, nameLabel);
				this.setComponentZOrder(driverIconView, 0);
			}
				
			remove = new NeuralRacing.UIButton(new Dimension(100, 30), NeuralRacing.selectedGreenTint, 10, () -> remove.title = LanguageManager.grabStringFromID("remove"));
			remove.updateData();
			remove.setForeground(Color.WHITE);
			remove.setFont(NeuralRacing.getSystemFont(Font.BOLD, 12));
			this.add(remove);
			layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, remove, 0, SpringLayout.HORIZONTAL_CENTER, backgroundView);
			layout.putConstraint(SpringLayout.SOUTH, remove, -10, SpringLayout.SOUTH, backgroundView);
			this.setComponentZOrder(remove, 0);
			
			remove.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent evt) { NeuralRacing.experimentsView.clear(); NeuralRacing.experimentsView.removeDriver(getThis()); } });
			
		}
		
		/**
		 * Cette méthode retourne la DriverView elle-même.
		 * 
		 * @return
		 * La DriverView elle-même.
		 */
		private DriverView getThis() { return this; }
		
		/**
		 * Cette méthode permet d'obtenir l'index d'un nom
		 * dans la liste des noms des voitures..
		 * 
		 * @param choice
		 * Le nom de la voiture.
		 * 
		 * @return
		 * L'index en question.
		 */
		private int getIndexOf(String choice) {
			for (int index = 0; index < 10; index++) {
				if (NeuralRacing.carsName[index].equals(choice)) { return index; }
			}
			
			return -1;
		}
		
		/**
		 * Cette méthode permet d'activer ou de désactiver l'ensemble des composants de la <code>DriverView</code>
		 * en un seul appel.
		 */
		public void setEnabled(boolean flag) {
			carImage.setEnabled(flag);
			remove.setEnabled(flag);
		}
		
		/**
		 * Cette méthode permet de retourner l'image de la voiture pour l'afficher dans le
		 * sens vertical plutôt qu'horizontal.
		 * 
		 * @param img
		 * L'image de la voiture, à l'horizontale.
		 * 
		 * @return
		 * L'image retournée de 90°.
		 */
		public BufferedImage getCarImage(BufferedImage img) {
			return new AffineTransformOp(AffineTransform.getRotateInstance(-Math.PI/2, img.getWidth()/2, img.getWidth()/2), AffineTransformOp.TYPE_BILINEAR).filter(img, null);
		}
		
		public void releaseFromMemory() {
			LanguageManager.removeUIElement(remove);
		}
		
	}
	
}