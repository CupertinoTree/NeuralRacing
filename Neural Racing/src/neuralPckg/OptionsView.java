package neuralPckg;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.sound.sampled.Clip;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SpringLayout;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;

import neuralPckg.NeuralRacing.UserDefaults;

/**
 * Cette sous-classe de <code>UIPanel</code> est la vue d'options.
 * Elle permet de régler les valeurs des booléens de la classe <code>UserDefaults</code>
 * et de gérer les données stockées.
 * 
 * @author Ronan
 *
 * @see NeuralRacing.UIPanel
 */
public class OptionsView extends NeuralRacing.UIPanel implements ActionListener {
	
	private static final long serialVersionUID = 94566387438825632L;
	
	JPanel contentView = new JPanel(), headerView = new JPanel();
	
	SpringLayout headerViewLayout,
				 contentViewLayout;
	
	/**Le label permettant d'afficher la source de la musique.*/
	NeuralRacing.UILabel musicRef;
	/**Le label permettant d'afficher les crédits.*/
	NeuralRacing.UILabel names;
	/**Le label permettant d'afficher la source des icônes.*/
	NeuralRacing.UILabel iconsRef;
	
	/**Le bouton permettant d'ouvrir le guide.*/
	NeuralRacing.UIButton openGuide;
	/**Le bouton de retour au menu.*/
	NeuralRacing.UIButton backToMenu;
	/**Le bouton pour sélectionner la liste des circuits.*/
	NeuralRacing.UIButton raceList;
	/**Le bouton pour sélectionner la liste des individus.*/
	NeuralRacing.UIButton networkList;
	/**Le bouton permettant de renommer un circuit ou un individu.*/
	NeuralRacing.UIButton rename;
	/**Le bouton permettant de supprimer un circuit ou un individu.*/
	NeuralRacing.UIButton destroy;
	/**Le bouton permettant de changer de langue.*/
	NeuralRacing.UIButton language;
	
	
	/**Le Toggle permettant de contrôler la valeur du booléen fadingEnabled de la classe UserDefaults.*/
	NeuralRacing.UIParameterToggle fadingEnabled;
	/**Le Toggle permettant de contrôler la valeur du booléen shouldDrawLines de la classe UserDefaults.*/
	NeuralRacing.UIParameterToggle shouldDrawLines;
	/**Le Toggle permettant de contrôler la valeur du booléen jingleEnabled de la classe UserDefaults.*/
	NeuralRacing.UIParameterToggle launchSound;
	/**Le Toggle permettant de contrôler la valeur du booléen musicEnabled de la classe UserDefaults.*/
	NeuralRacing.UIParameterToggle music;
	
	/**Le titre de la vue.*/
	JLabel header = new JLabel();
	
	JScrollPane scrollView;
	JTable dataTable;
	
	/**Les noms des circuits ou des individus, selon ce qui est affiché.*/
	Object [] keySet;
	
	boolean leftSelected = true;
	
	/**
	 * Cette sous-classe de <code>UIPanel</code> est la vue d'options.
	 * Elle permet de régler les valeurs des booléens de la classe <code>UserDefaults</code>
	 * et de gérer les données stockées.
	 * 
	 * @param size
	 * La taille de la vue.
	 *
	 * @see NeuralRacing.UIPanel
	 */
	public OptionsView(Dimension size) {
		
		this.setPreferredSize(size);
		this.setLayout(new BorderLayout());
		headerView.setPreferredSize(new Dimension((int) (size.getWidth()-200), 100));
		contentView.setPreferredSize(new Dimension((int) size.getWidth(), (int) (size.getHeight()-200)));
		
		headerView.setBackground(NeuralRacing.darkerGrayColor);
		contentView.setBackground(NeuralRacing.darkGrayColor);
		
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
				NeuralRacing.guide.openPage("optionsView");
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
		header.setForeground(Color.WHITE);
		headerView.add(header);
		headerViewLayout.putConstraint(SpringLayout.VERTICAL_CENTER, header, 0, SpringLayout.VERTICAL_CENTER, headerView);
		headerViewLayout.putConstraint(SpringLayout.WEST, header, 16, SpringLayout.EAST, backToMenu);
		
		LanguageManager.registerUIElement(new LanguageManager.LanguageMutable() {
			@Override public void updateData() {
				header.setText(LanguageManager.grabStringFromID("optionsTitle"));
			}
		});
		
		language = new NeuralRacing.UIButton(new Dimension(139, 55), NeuralRacing.darkGrayColor, 20, () -> language.title = LanguageManager.grabStringFromID("language"));
		language.setIcon(NeuralRacing.languageIcon);
		language.addActionListener(this);
		language.setForeground(Color.white);
		language.setFont(NeuralRacing.getSystemFont(Font.BOLD, 22));
		headerView.add(language);
		headerViewLayout.putConstraint(SpringLayout.VERTICAL_CENTER, language, 0, SpringLayout.VERTICAL_CENTER, headerView);
		headerViewLayout.putConstraint(SpringLayout.EAST, language, -35, SpringLayout.EAST, this);
		
		language.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				String newLanguage = (String) JOptionPane.showInputDialog(contentView, LanguageManager.grabStringFromID("chooseLanguage"), LanguageManager.grabStringFromID("message"), JOptionPane.PLAIN_MESSAGE, null, NeuralRacing.languageArray, UserDefaults.language);
				
				if (newLanguage == null) { return; }
				
				LanguageManager.loadLanguage(newLanguage);
			}
		});
		
		contentViewLayout = new SpringLayout();
		contentView.setLayout(contentViewLayout);
		
		//Ce listener permet de mettre à jour les valeurs de la classe UserDefaults
		//en fonction des choix de l'utilisateur.
		ChangeListener optionsListener = new ChangeListener() {

			@Override public void stateChanged(ChangeEvent evt) {
				
				if (evt.getSource().equals(fadingEnabled)) {
					
					NeuralRacing.UserDefaults.fadingEnabled = fadingEnabled.currentValue;
					
				} else if (evt.getSource().equals(shouldDrawLines)) {
					
					NeuralRacing.UserDefaults.shouldDrawLines = shouldDrawLines.currentValue;
					
				} else if (evt.getSource().equals(launchSound)) {
					
					NeuralRacing.UserDefaults.jingleEnabled = launchSound.currentValue;
					
				} else if (evt.getSource().equals(music)) {
					
					NeuralRacing.UserDefaults.musicEnabled = music.currentValue;
					
					if (music.currentValue) {
						//Si music devient vrai, on relance la musique.
						NeuralRacing.menuView.musicClip.loop(Clip.LOOP_CONTINUOUSLY);
					} else {
						//Sinon, on la met en pause.
						NeuralRacing.menuView.musicClip.stop();
					}
					
				}
				
				UserDefaults.save();
				
			}
			
		};
		
		fadingEnabled = new NeuralRacing.UIParameterToggle(new Dimension(150, 100), NeuralRacing.darkGreenBackground, 20, NeuralRacing.widespreadGreenishGreen, NeuralRacing.selectedGreenTint, NeuralRacing.UserDefaults.fadingEnabled, () -> fadingEnabled.label.setText(LanguageManager.grabStringFromID("fading") + " :"));
		fadingEnabled.label.setForeground(Color.WHITE);
		fadingEnabled.label.setFont(NeuralRacing.getSystemFont(Font.BOLD, 16));
		fadingEnabled.toggle.setForeground(Color.WHITE);
		fadingEnabled.toggle.setFont(NeuralRacing.getSystemFont(Font.BOLD, 14));
		contentView.add(fadingEnabled);
		contentViewLayout.putConstraint(SpringLayout.WEST, fadingEnabled, 30, SpringLayout.WEST, contentView);
		contentViewLayout.putConstraint(SpringLayout.NORTH, fadingEnabled, 30, SpringLayout.NORTH, contentView);
		fadingEnabled.listener = optionsListener;
		
		shouldDrawLines = new NeuralRacing.UIParameterToggle(new Dimension(150, 100), NeuralRacing.darkGreenBackground, 20, NeuralRacing.widespreadGreenishGreen, NeuralRacing.selectedGreenTint, NeuralRacing.UserDefaults.shouldDrawLines, () -> shouldDrawLines.label.setText(LanguageManager.grabStringFromID("hitbox") + " :"));
		shouldDrawLines.label.setForeground(Color.WHITE);
		shouldDrawLines.label.setFont(NeuralRacing.getSystemFont(Font.BOLD, 16));
		shouldDrawLines.toggle.setForeground(Color.WHITE);
		shouldDrawLines.toggle.setFont(NeuralRacing.getSystemFont(Font.BOLD, 14));
		contentView.add(shouldDrawLines);
		contentViewLayout.putConstraint(SpringLayout.WEST, shouldDrawLines, 15, SpringLayout.EAST, fadingEnabled);
		contentViewLayout.putConstraint(SpringLayout.NORTH, shouldDrawLines, 30, SpringLayout.NORTH, contentView);
		shouldDrawLines.listener = optionsListener;
		
		launchSound = new NeuralRacing.UIParameterToggle(new Dimension(150, 100), NeuralRacing.darkGreenBackground, 20, NeuralRacing.widespreadGreenishGreen, NeuralRacing.selectedGreenTint, NeuralRacing.UserDefaults.jingleEnabled, () -> launchSound.label.setText(LanguageManager.grabStringFromID("jingle") + " :"));
		launchSound.label.setForeground(Color.WHITE);
		launchSound.label.setFont(NeuralRacing.getSystemFont(Font.BOLD, 16));
		launchSound.toggle.setForeground(Color.WHITE);
		launchSound.toggle.setFont(NeuralRacing.getSystemFont(Font.BOLD, 14));
		contentView.add(launchSound);
		contentViewLayout.putConstraint(SpringLayout.WEST, launchSound, 30, SpringLayout.WEST, contentView);
		contentViewLayout.putConstraint(SpringLayout.NORTH, launchSound, 15, SpringLayout.SOUTH, fadingEnabled);
		launchSound.listener = optionsListener;
		
		music = new NeuralRacing.UIParameterToggle(new Dimension(150, 100), NeuralRacing.darkGreenBackground, 20, NeuralRacing.widespreadGreenishGreen, NeuralRacing.selectedGreenTint, NeuralRacing.UserDefaults.musicEnabled, () -> music.label.setText(LanguageManager.grabStringFromID("music") + " :"));
		music.label.setForeground(Color.WHITE);
		music.label.setFont(NeuralRacing.getSystemFont(Font.BOLD, 16));
		music.toggle.setForeground(Color.WHITE);
		music.toggle.setFont(NeuralRacing.getSystemFont(Font.BOLD, 14));
		contentView.add(music);
		contentViewLayout.putConstraint(SpringLayout.WEST, music, 15, SpringLayout.EAST, fadingEnabled);
		contentViewLayout.putConstraint(SpringLayout.NORTH, music, 15, SpringLayout.SOUTH, shouldDrawLines);
		music.listener = optionsListener;
		
		names = new NeuralRacing.UILabel(new Dimension(315, 40), NeuralRacing.darkGreenBackground, 20, () -> names.title = LanguageManager.grabStringFromID("author"));
		names.setFont(NeuralRacing.getSystemFont(Font.BOLD, 14));
		names.setForeground(Color.white);
		contentView.add(names);
		contentViewLayout.putConstraint(SpringLayout.WEST, names, 30, SpringLayout.WEST, this);
		contentViewLayout.putConstraint(SpringLayout.NORTH, names, 30, SpringLayout.SOUTH, music);
		
		musicRef = new NeuralRacing.UILabel(new Dimension(315, 40), NeuralRacing.darkGreenBackground, 20, () -> musicRef.title = LanguageManager.grabStringFromID("music") + " : https://bensound.com");
		musicRef.setFont(NeuralRacing.getSystemFont(Font.BOLD, 14));
		musicRef.setForeground(Color.white);
		contentView.add(musicRef);
		contentViewLayout.putConstraint(SpringLayout.WEST, musicRef, 30, SpringLayout.WEST, this);
		contentViewLayout.putConstraint(SpringLayout.NORTH, musicRef, 15, SpringLayout.SOUTH, names);
		
		iconsRef = new NeuralRacing.UILabel(new Dimension(315, 40), NeuralRacing.darkGreenBackground, 20, () -> iconsRef.title = LanguageManager.grabStringFromID("icons") + " : https://icons8.com");
		iconsRef.setFont(NeuralRacing.getSystemFont(Font.BOLD, 14));
		iconsRef.setForeground(Color.white);
		contentView.add(iconsRef);
		contentViewLayout.putConstraint(SpringLayout.WEST, iconsRef, 30, SpringLayout.WEST, this);
		contentViewLayout.putConstraint(SpringLayout.NORTH, iconsRef, 15, SpringLayout.SOUTH, musicRef);
		
		//Ce listener permet de mettre à jour les valeurs de la table
		//quand l'utilisateur change de menu en haut (circuits ou individus).
		ActionListener selectorListener = new ActionListener() {
			@Override public void actionPerformed(ActionEvent evt) {
				
				leftSelected = evt.getSource().equals(raceList);
				
				raceList.switchBackground(  !leftSelected ? NeuralRacing.selectedGreenTint : NeuralRacing.darkGreenBackground);
				networkList.switchBackground(leftSelected ? NeuralRacing.selectedGreenTint : NeuralRacing.darkGreenBackground);
				
				keySet = leftSelected ? NeuralRacing.raceList.keySet().toArray() : NeuralRacing.networkList.keySet().toArray();
				((AbstractTableModel) dataTable.getModel()).fireTableDataChanged();
				
			}
		};
		
		raceList = new NeuralRacing.UIButton(new Dimension(250, 50), NeuralRacing.darkGreenBackground, 0, () -> raceList.title = LanguageManager.grabStringFromID("races"));
		raceList.setForeground(Color.white);
		raceList.setFont(NeuralRacing.getSystemFont(Font.BOLD, 20));
		contentView.add(raceList);
		contentViewLayout.putConstraint(SpringLayout.NORTH, raceList, 30, SpringLayout.NORTH, contentView);
		contentViewLayout.putConstraint(SpringLayout.WEST, raceList, 30, SpringLayout.EAST, shouldDrawLines);
		raceList.addActionListener(selectorListener);
		
		networkList = new NeuralRacing.UIButton(new Dimension(250, 50), NeuralRacing.selectedGreenTint, 0, () -> networkList.title = LanguageManager.grabStringFromID("indivs"));
		networkList.setForeground(Color.white);
		networkList.setFont(NeuralRacing.getSystemFont(Font.BOLD, 20));
		contentView.add(networkList);
		contentViewLayout.putConstraint(SpringLayout.NORTH, networkList, 30, SpringLayout.NORTH, contentView);
		contentViewLayout.putConstraint(SpringLayout.WEST, networkList, 0, SpringLayout.EAST, raceList);
		networkList.addActionListener(selectorListener);
		
		keySet = NeuralRacing.raceList.keySet().toArray();
		
		dataTable = new JTable(new AbstractTableModel() {
			
			private static final long serialVersionUID = 165432124242323L;
			
			public int    getColumnCount() { return 1; }
			public int    getRowCount()    { return keySet.length; }
			//Les espaces permettent simplement de mieux centrer le texte.
	        public Object getValueAt(int row, int col) { return "  " + keySet[row]; }
	        
		});
		
		dataTable.setBackground(NeuralRacing.darkGreenBackground);
		dataTable.setForeground(Color.WHITE);
		dataTable.setFont(NeuralRacing.getSystemFont(Font.BOLD, 16));
		dataTable.setGridColor(NeuralRacing.darkGrayColor);
		dataTable.setTableHeader(null);
		dataTable.setRowHeight(30);
		
		LanguageManager.registerUIElement(new LanguageManager.LanguageMutable() {
			@Override public void updateData() {
				keySet = leftSelected ? NeuralRacing.raceList.keySet().toArray() : NeuralRacing.networkList.keySet().toArray();
				((AbstractTableModel) dataTable.getModel()).fireTableDataChanged();
			}
		});
		
		scrollView = new JScrollPane(dataTable);
		scrollView.setBorder(BorderFactory.createEmptyBorder());
		scrollView.setBackground(NeuralRacing.darkGrayColor);
		scrollView.getViewport().setBackground(NeuralRacing.darkGrayColor);
		scrollView.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollView.getVerticalScrollBar().setUnitIncrement(8);
		scrollView.setPreferredSize(new Dimension(500, 405));
		contentView.add(scrollView);
		contentViewLayout.putConstraint(SpringLayout.NORTH, scrollView, 0, SpringLayout.SOUTH, raceList);
		contentViewLayout.putConstraint(SpringLayout.WEST,  scrollView, 30, SpringLayout.EAST, shouldDrawLines);
		
		rename = new NeuralRacing.UIButton(new Dimension(250, 50), NeuralRacing.darkerGrayColor, 0, () -> rename.title = LanguageManager.grabStringFromID("rename"));
		rename.setForeground(Color.white);
		rename.setFont(NeuralRacing.getSystemFont(Font.BOLD, 18));
		contentView.add(rename);
		contentViewLayout.putConstraint(SpringLayout.NORTH, rename, 0, SpringLayout.SOUTH, scrollView);
		contentViewLayout.putConstraint(SpringLayout.WEST, rename, 30, SpringLayout.EAST, shouldDrawLines);
		
		rename.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent evt) {
				
				int index = dataTable.getSelectedRow();
				Map<String, String> list = (leftSelected ? NeuralRacing.raceList : NeuralRacing.networkList);
				
				String key   = (String) list.keySet().toArray()[index],
					   value = list.get(key);
				
				if (leftSelected && key.equals(LanguageManager.grabStringFromID("defaultRace"))) {
					JOptionPane.showConfirmDialog(contentView, LanguageManager.grabStringFromID("renameError"), LanguageManager.grabStringFromID("error"), JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				if (!leftSelected && key.equals(LanguageManager.grabStringFromID("defaultIndiv"))) {
					JOptionPane.showConfirmDialog(contentView, LanguageManager.grabStringFromID("renameError1"), LanguageManager.grabStringFromID("error"), JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				String name = (String) JOptionPane.showInputDialog(NeuralRacing.raceEditorView, LanguageManager.grabStringFromID("giveAName") + " " + (leftSelected ? LanguageManager.grabStringFromID("race") : LanguageManager.grabStringFromID("indiv")) + " !", LanguageManager.grabStringFromID("message"), JOptionPane.PLAIN_MESSAGE, null, null, null);
				if (name == null) { return; }
				
				if (list.containsKey(name)) {
					if (JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog(contentView, LanguageManager.grabStringFromID("an") + " " + (leftSelected ? LanguageManager.grabStringFromID("race") : LanguageManager.grabStringFromID("indiv")) + " " + LanguageManager.grabStringFromID("alreadyExists") + "\n" + LanguageManager.grabStringFromID("ifCancel"), LanguageManager.grabStringFromID("Warning"), JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE));
					{ return; }
				}
				
				list.remove(key);
				list.put(name, value);
				
				if (leftSelected) {
					NeuralRacing.saveRaces();
					
					if (NeuralRacing.evolutionView.selectedRace.equals(key))   { NeuralRacing.evolutionView.selectedRace   = name; }
					if (NeuralRacing.experimentsView.selectedRace.equals(key)) { NeuralRacing.experimentsView.selectedRace = name; }
					if (NeuralRacing.raceEditorView.selectedRace.equals(key))  { NeuralRacing.raceEditorView.selectedRace  = name; }
				} else {
					NeuralRacing.saveNetworks();
					
					if (NeuralRacing.experimentsView.networkKeys.contains(key)) {
						for (ExperimentsView.DriverView view: NeuralRacing.experimentsView.driverViews) {
							if (view.nameLabel.title.equals(key)) {
								NeuralRacing.experimentsView.removeDriver(view);
								break;
							}
						}
					}
				}
				
				keySet = list.keySet().toArray();
				((AbstractTableModel) dataTable.getModel()).fireTableDataChanged();
				
			}
		});
		
		destroy = new NeuralRacing.UIButton(new Dimension(250, 50), NeuralRacing.darkerGrayColor, 0, () -> destroy.title = LanguageManager.grabStringFromID("destroy"));
		destroy.setForeground(Color.white);
		destroy.setFont(NeuralRacing.getSystemFont(Font.BOLD, 18));
		contentView.add(destroy);
		contentViewLayout.putConstraint(SpringLayout.NORTH, destroy, 0, SpringLayout.SOUTH, scrollView);
		contentViewLayout.putConstraint(SpringLayout.WEST, destroy, 0, SpringLayout.EAST, rename);
		
		destroy.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent evt) {
				int index = dataTable.getSelectedRow();
				Map<String, String> list = (leftSelected ? NeuralRacing.raceList : NeuralRacing.networkList);
				
				if (leftSelected && ((String) list.keySet().toArray()[index]).equals(LanguageManager.grabStringFromID("defaultRace"))) {
					JOptionPane.showConfirmDialog(contentView, LanguageManager.grabStringFromID("destroyError"), LanguageManager.grabStringFromID("error"), JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				if (!leftSelected && ((String) list.keySet().toArray()[index]).equals(LanguageManager.grabStringFromID("defaultIndiv"))) {
					JOptionPane.showConfirmDialog(contentView, LanguageManager.grabStringFromID("destroyError1"), LanguageManager.grabStringFromID("error"), JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				if (JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog(contentView, LanguageManager.grabStringFromID("destroyConfirm") +  " " + (leftSelected ? LanguageManager.grabStringFromID("thisRace") : LanguageManager.grabStringFromID("thisIndiv")) + " ?", LanguageManager.grabStringFromID("warning"), JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE)) { return; }
				
				String key = (String) list.keySet().toArray()[index];
				
				list.remove(key);
				
				if (leftSelected) {
					NeuralRacing.saveRaces();
					
					if (NeuralRacing.evolutionView.selectedRace.equals(key))   { NeuralRacing.evolutionView.selectedRace   = LanguageManager.grabStringFromID("defaultRace"); NeuralRacing.evolutionView.raceGrid.synchronizeWithGrid(new RaceGrid(new EnhancedString(NeuralRacing.raceList.get(LanguageManager.grabStringFromID("defaultRace"))), true)); }
					if (NeuralRacing.experimentsView.selectedRace.equals(key)) { NeuralRacing.experimentsView.selectedRace = LanguageManager.grabStringFromID("defaultRace"); NeuralRacing.experimentsView.raceGrid.synchronizeWithGrid(new RaceGrid(new EnhancedString(NeuralRacing.raceList.get(LanguageManager.grabStringFromID("defaultRace"))), true)); }
					if (NeuralRacing.raceEditorView.selectedRace.equals(key))  { NeuralRacing.raceEditorView.selectedRace  = ""; NeuralRacing.raceEditorView.raceGrid.synchronizeWithGrid(new RaceGrid(new EnhancedString(NeuralRacing.raceList.get(LanguageManager.grabStringFromID(""))), true)); }
				} else {
					NeuralRacing.saveNetworks();
					
					if (NeuralRacing.experimentsView.networkKeys.contains(key)) {
						for (ExperimentsView.DriverView view: NeuralRacing.experimentsView.driverViews) {
							if (view.nameLabel.title.equals(key)) {
								NeuralRacing.experimentsView.removeDriver(view);
								break;
							}
						}
					}
				}
				
				keySet = list.keySet().toArray();
				((AbstractTableModel) dataTable.getModel()).fireTableDataChanged();
			}
		});
		
		this.add(headerView, BorderLayout.NORTH);
		this.add(contentView, BorderLayout.WEST);
	}
	
	@Override public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(backToMenu)) {
			NeuralRacing.showView("menuPanel");
		}
	}
	
}
