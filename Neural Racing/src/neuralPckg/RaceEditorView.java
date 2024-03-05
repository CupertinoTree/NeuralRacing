package neuralPckg;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SpringLayout;
import javax.swing.filechooser.FileNameExtensionFilter;

import neuralPckg.RaceGrid.TileType;

/**
 * Cette sous-classe de <code>UIPanel</code> est l'éditeur de circuit.
 * Elle permet d'éditer un circuit existant, qu'il soit importé ou en local,
 * et de sauvegarder/exporter des circuits. Deux modes d'édition sont pris en charge :
 * <p>
 * - la Peinture par Points, dans lequel on clique ou on glisse le curseur pour changer instantanément
 * les cases sous-jacentes en la case choisie.
 * <p>
 * - le mode de sélection, qui permet d'effectuer des élections de zones pour les remplir, les copier, les
 * coller, ou les couper.
 * 
 * @author Ronan
 *
 * @see NeuralRacing.UIPanel
 */
public class RaceEditorView extends NeuralRacing.UIPanel implements ActionListener {

	private static final long serialVersionUID = 4343567654398798L;
	
	JPanel contentView,
		   dragView   = new DragToSelectView(),
		   headerView = new JPanel(),
		   detailView = new JPanel();
	
	SpringLayout headerViewLayout, detailViewLayout;
	
	JLabel header = new JLabel("Éditeur de circuit");
	
	NeuralRacing.UIButton openGuide,
						  backToMenu,
						  manageFile,
						  rotateLeft,
						  rotateRight,
						  fill,
						  copy,
						  paste,
						  cut;
	
	NeuralRacing.UIView   tileTypePlaceholder,
		    			  orientationPlaceholder,
		    			  toolsPlaceholder;
	
	NeuralRacing.UILabel  tileTypeTitle,
						  orientationTitle,
						  toolsTitle;
	
	NeuralRacing.UISelector pencilSelector;
	
	JButton tileChooser;
	
	Font menuFont = NeuralRacing.getSystemFont(Font.BOLD, 14);
	
	AffineTransformOp leftRotation  = new AffineTransformOp(AffineTransform.getRotateInstance(-Math.PI/2, 80/2, 80/2), AffineTransformOp.TYPE_BILINEAR),
					  rightRotation = new AffineTransformOp(AffineTransform.getRotateInstance( Math.PI/2, 80/2, 80/2), AffineTransformOp.TYPE_BILINEAR);
	
	boolean leftCursorSelected = true,
			editsSaved              = true;
	
	public int firstSelectedRow    = -1,
			   firstSelectedColumn = -1,
			   lastSelectedRow     = -1,
			   lastSelectedColumn  = -1;
	
	/**Le type d'orientation de la case sélectionnée.*/
	int orientation = 0;
	
	/**Le type de la case sélectionnée.*/
	TileType tileType = TileType.STRAIGHT_ROAD;
	
	/**La grille de jeu en cours d'édition.*/
	RaceGrid raceGrid;
	
	/**Le presse-papier utile au découpages, collages, et copies.*/
	RaceGrid clipBoard;
	
	/**La dernière grille non modifiée.
	 * <p>
	 * Représente une grille vide par défaut. Si vous ouvrez un fichier, elle représentera ce fichier.
	 * <p>
	 * Ainsi, on peut comparer cette grille et <code>RaceGrid</code> pour savoir si l'utilisateur
	 * a fait des modifications non sauvegardées.
	 * */
	RaceGrid referenceGrid;
	
	JPopupMenu manageMenu, tileChooserMenu;
	
	JMenuItem editFile     = new JMenuItem("Modifier un circuit",     NeuralRacing.editFile),
			  importFile   = new JMenuItem("Importer un circuit",     NeuralRacing.importFile),
			  saveToLib    = new JMenuItem("Sauvegarder le circuit",  NeuralRacing.saveToLib),
			  exportFile   = new JMenuItem("Exporter le circuit",     NeuralRacing.exportFile),
			  straightTile = new JMenuItem("Ligne droite",            NeuralRacing.straightRoadSelectorIcon),
			  curvedTile   = new JMenuItem("Virage",                  NeuralRacing.curvedRoadSelectorIcon),
			  grassTile    = new JMenuItem("Herbe",                   NeuralRacing.grassSelectorIcon),
			  startTile    = new JMenuItem("Ligne de départ",         NeuralRacing.startLineSelectorIcon);
	
	JFileChooser loadFileChooser,
				 saveFileChooser;
	
	/**Le nom du circuit en cours de modification.*/
	String selectedRace = "";
	
	/**
	 * Cette sous-classe de <code>UIPanel</code> est l'éditeur de circuit.
	 * Elle permet d'éditer un circuit existant, qu'il soit importé ou en local,
	 * et de sauvegarder/exporter des circuits. Deux modes d'édition sont pris en charge :
	 * <p>
	 * - la Peinture par Points, dans lequel on clique ou on glisse le curseur pour changer instantanément
	 * les cases sous-jacentes en la case choisie.
	 * <p>
	 * - le mode de sélection, qui permet d'effectuer des élections de zones pour les remplir, les copier, les
	 * coller, ou les couper.
	 * 
	 * @param size
	 * La taille de la vue.
	 *
	 * @see NeuralRacing.UIPanel
	 */
	public RaceEditorView(Dimension size) {
		
		this.setPreferredSize(size);
		this.setLayout(new BorderLayout());
		contentView = new RaceView(new Dimension((int) (size.getWidth()-200), (int) (size.getHeight()-200)), new RaceGrid(new EnhancedString(""), true), false);
		raceGrid = ((RaceView) contentView).raceGrid;
		headerView.setPreferredSize(new Dimension((int) (size.getWidth()-200), 100));
		detailView.setPreferredSize(new Dimension(200, (int) (size.getHeight()-200)));
		contentView.setPreferredSize(new Dimension((int) (size.getWidth()-200), (int) (size.getHeight()-200)));
		
		referenceGrid = new RaceGrid(new EnhancedString(""), true);
		
		headerView.setBackground(NeuralRacing.darkerGrayColor);
		detailView.setBackground(new Color(0, 108, 58));
		contentView.setBackground(Color.white);
		
		headerViewLayout = new SpringLayout();
		headerView.setLayout(headerViewLayout);
		
		LanguageManager.registerUIElement(new LanguageManager.LanguageMutable() {
			@Override public void updateData() {
				header.setText(LanguageManager.grabStringFromID("raceEditorTitleExtended"));
				
				    editFile.setText(LanguageManager.grabStringFromID("editRace"));
				  importFile.setText(LanguageManager.grabStringFromID("importRace"));
				   saveToLib.setText(LanguageManager.grabStringFromID("saveRace"));
				  exportFile.setText(LanguageManager.grabStringFromID("exportRace"));
				straightTile.setText(LanguageManager.grabStringFromID("straightTile"));
				  curvedTile.setText(LanguageManager.grabStringFromID("curvedTile"));
				   grassTile.setText(LanguageManager.grabStringFromID("grassTile"));
				   startTile.setText(LanguageManager.grabStringFromID("startTile"));
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
				NeuralRacing.guide.openPage("raceEditorView");
			}
		});
		
		backToMenu = new NeuralRacing.UIButton(new Dimension(100, 55), NeuralRacing.darkGrayColor, 20, () -> backToMenu.title = LanguageManager.grabStringFromID("menu"));
		backToMenu.setFont(NeuralRacing.getSystemFont(Font.BOLD, 25));
		backToMenu.setForeground(Color.white);
		backToMenu.addActionListener(this);
		headerView.add(backToMenu);
		headerViewLayout.putConstraint(SpringLayout.VERTICAL_CENTER, backToMenu, 0, SpringLayout.VERTICAL_CENTER, headerView);
		headerViewLayout.putConstraint(SpringLayout.WEST, backToMenu, 15, SpringLayout.EAST, openGuide);
		
		manageFile = new NeuralRacing.UIButton(new Dimension(139, 55), NeuralRacing.darkGrayColor, 20, () -> manageFile.title = LanguageManager.grabStringFromID("manage"));
		manageFile.setIcon(NeuralRacing.manageFileIcon);
		manageFile.addActionListener(this);
		manageFile.setForeground(Color.white);
		manageFile.setFont(NeuralRacing.getSystemFont(Font.BOLD, 22));
		headerView.add(manageFile);
		headerViewLayout.putConstraint(SpringLayout.VERTICAL_CENTER, manageFile, 0, SpringLayout.VERTICAL_CENTER, headerView);
		headerViewLayout.putConstraint(SpringLayout.EAST, manageFile, -35, SpringLayout.EAST, this);
		
		manageMenu = new JPopupMenu();
		manageMenu.setBackground(NeuralRacing.darkerGrayColor);
		editFile.setFont(menuFont);
		editFile.setBackground(NeuralRacing.darkerGrayColor);
		editFile.setForeground(Color.WHITE);
		importFile.setFont(menuFont);
		importFile.setBackground(NeuralRacing.darkerGrayColor);
		importFile.setForeground(Color.WHITE);
		saveToLib.setFont(menuFont);
		saveToLib.setBackground(NeuralRacing.darkerGrayColor);
		saveToLib.setForeground(Color.WHITE);
		exportFile.setFont(menuFont);
		exportFile.setBackground(NeuralRacing.darkerGrayColor);
		exportFile.setForeground(Color.WHITE);
		manageMenu.add(editFile);
		manageMenu.add(importFile);
		manageMenu.add(saveToLib);
		manageMenu.add(exportFile);
		
		loadFileChooser = new JFileChooser();
		loadFileChooser.setAcceptAllFileFilterUsed(false);
		loadFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		loadFileChooser.setFileFilter(new FileNameExtensionFilter(LanguageManager.grabStringFromID("neuralRacingRace"), "gameGrid"));
		
		saveFileChooser = new JFileChooser();
		saveFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		
		ActionListener loadAndSaveListener = new ActionListener() {

			@Override public void actionPerformed(ActionEvent e) {
				if (e.getSource() == importFile) {
					
					if (!clear()) { return; }
					
					if (loadFileChooser.showOpenDialog(contentView) == JFileChooser.APPROVE_OPTION) {
						
						try {
							BufferedReader reader = new BufferedReader(new FileReader(loadFileChooser.getSelectedFile()));
							
							EnhancedString str = new EnhancedString(reader.readLine());
							//On charge la nouvelle grille dans raceGrid
							raceGrid.synchronizeWithGrid(new RaceGrid(str, false));
							//On charge la nouvelle grille en temps que nouvelle référence
							referenceGrid.synchronizeWithGrid(new RaceGrid(str, false));
							//contentView.repaint();
							reader.close();
						} catch (IOException exc) {
			            	JOptionPane.showMessageDialog(contentView, LanguageManager.grabStringFromID("openFileFail"), LanguageManager.grabStringFromID("error"), JOptionPane.ERROR_MESSAGE);
			            	return;
						} catch (IllegalArgumentException exc) {
			            	JOptionPane.showMessageDialog(contentView, exc.getMessage(), LanguageManager.grabStringFromID("error"), JOptionPane.ERROR_MESSAGE);
			            	return;
			            }
						
						NeuralRacing.raceEditorView.repaint();
					}
					
				} else if (e.getSource() == editFile) {
					
					if (!clear()) { return; }
					
					String name = (String) JOptionPane.showInputDialog(contentView, LanguageManager.grabStringFromID("chooseRace"), LanguageManager.grabStringFromID("message"), JOptionPane.PLAIN_MESSAGE, null, NeuralRacing.raceList.keySet().toArray(), selectedRace);
					if (name == null) { return; } else { selectedRace = name; }
					try {
						EnhancedString str = new EnhancedString(NeuralRacing.raceList.get(name));
						//On charge la nouvelle grille dans raceGrid
						raceGrid.synchronizeWithGrid(new RaceGrid(str, false));
						//On charge la nouvelle grille en temps que nouvelle référence
						referenceGrid.synchronizeWithGrid(new RaceGrid(str, false));
					} catch (IllegalArgumentException exc) {
						JOptionPane.showMessageDialog(contentView, exc.getMessage(), LanguageManager.grabStringFromID("error"), JOptionPane.ERROR_MESSAGE);
						return;
					}
					
					NeuralRacing.raceEditorView.repaint();
					
				} else if (e.getSource() == saveToLib) {
					
					String name = (String) JOptionPane.showInputDialog(NeuralRacing.raceEditorView, LanguageManager.grabStringFromID("giveAName") + " " + LanguageManager.grabStringFromID("new") + " " + LanguageManager.grabStringFromID("race") + " !", LanguageManager.grabStringFromID("message"), JOptionPane.PLAIN_MESSAGE, null, null, null);
					
					if (name == null) { return; }
					
					if (NeuralRacing.raceList.containsKey(name)) {
						if (JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog(contentView, LanguageManager.grabStringFromID("an") + " " + LanguageManager.grabStringFromID("race") + " " + LanguageManager.grabStringFromID("alreadyExists") + "\n" + LanguageManager.grabStringFromID("ifCancel"), LanguageManager.grabStringFromID("warning"), JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE)) { return; }
					}
					
					try {
						NeuralRacing.raceList.put(name, raceGrid.toStringRpz());
					} catch (IllegalArgumentException exc) {
						JOptionPane.showMessageDialog(contentView, exc.getMessage(), LanguageManager.grabStringFromID("error"), JOptionPane.ERROR_MESSAGE);
						return;
					}
					
					NeuralRacing.saveRaces();
					editsSaved = true;
					
					
				} else if (e.getSource() == exportFile) {
					
					if (saveFileChooser.showSaveDialog(contentView) == JFileChooser.APPROVE_OPTION) {
						
						String name = saveFileChooser.getSelectedFile().getAbsolutePath();
						
						if (name.lastIndexOf('.') == -1) {
							name += ".gameGrid";
						} else if (!(name.contains(".gameGrid"))) {
							name = name.substring(0, name.lastIndexOf('.') + 1) + "gameGrid";
						}
						
						if (name.lastIndexOf(".gameGrid") + 1 == saveFileChooser.getSelectedFile().getAbsolutePath().length()) {
							name = name.substring(0, name.lastIndexOf(".gameGrid")) + "unknown.gameGrid";
						}
						
						if (new File(name).exists()) { if (JOptionPane.showConfirmDialog(contentView, LanguageManager.grabStringFromID("an") + " " + LanguageManager.grabStringFromID("race") + " " + LanguageManager.grabStringFromID("alreadyExists")) != JOptionPane.OK_OPTION) { return; } }
						
						String rpz = "";
						try {
							rpz = raceGrid.toStringRpz();
						} catch (IllegalArgumentException exc) {
			            	JOptionPane.showMessageDialog(contentView, LanguageManager.grabStringFromID("saveFileFail") + " " + exc.getMessage(), LanguageManager.grabStringFromID("error"), JOptionPane.ERROR_MESSAGE);
			            	return;
						}
						
						try {
							BufferedWriter writer = new BufferedWriter( new FileWriter(name));
							writer.write(rpz);
							writer.close();
							JOptionPane.showMessageDialog(contentView, LanguageManager.grabStringFromID("saveFileSuccess"), LanguageManager.grabStringFromID("info"), JOptionPane.INFORMATION_MESSAGE);
			            } catch (IOException exc) {
			            	JOptionPane.showMessageDialog(contentView, LanguageManager.grabStringFromID("saveFileFail"), LanguageManager.grabStringFromID("error"), JOptionPane.ERROR_MESSAGE);
			            	return;
			            }
						
						editsSaved = true;
					}
				}
			}
		};
		
		editFile.addActionListener(loadAndSaveListener);
		importFile.addActionListener(loadAndSaveListener);
		saveToLib.addActionListener(loadAndSaveListener);
		exportFile.addActionListener(loadAndSaveListener);
		
		header.setFont(NeuralRacing.getSystemFont(Font.BOLD, 32));
		header.setForeground(Color.white);
		headerView.add(header);
		headerViewLayout.putConstraint(SpringLayout.VERTICAL_CENTER, header, 0, SpringLayout.VERTICAL_CENTER, headerView);
		headerViewLayout.putConstraint(SpringLayout.WEST, header, 16, SpringLayout.EAST, backToMenu);
		
		detailViewLayout = new SpringLayout();
		detailView.setLayout(detailViewLayout);
		
		ActionListener listener = new ActionListener() {
			@Override public void actionPerformed(ActionEvent evt) {
				if (evt.getID() == 0 && !leftCursorSelected) {
					//Si on choisi le mode de peinture par point, la séléction disparaît.
					((DragToSelectView) dragView).mouseClicked(null);
					leftCursorSelected = true;
					shouldShowTools(false);
				} else if (evt.getID() == 1 && leftCursorSelected) {
					leftCursorSelected = false;
					shouldShowTools(true);
				}
			}
		};
		
		pencilSelector = new NeuralRacing.UISelector(new Dimension(170, 60), NeuralRacing.selectedGreenTint, NeuralRacing.widespreadGreenishGreen, 25, listener, NeuralRacing.pencilSelectorLeftIcon, NeuralRacing.pencilSelectorRightIcon);
		detailView.add(pencilSelector);
		detailViewLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, pencilSelector, 0, SpringLayout.HORIZONTAL_CENTER, detailView);
		detailViewLayout.putConstraint(SpringLayout.NORTH, pencilSelector, 16, SpringLayout.NORTH, detailView);
		
		tileTypePlaceholder = new NeuralRacing.UIView(new Dimension(165, 165), NeuralRacing.widespreadGreenishGreen, 20);
		detailView.add(tileTypePlaceholder);
		detailViewLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, tileTypePlaceholder, 0, SpringLayout.HORIZONTAL_CENTER, detailView);
		detailViewLayout.putConstraint(SpringLayout.NORTH, tileTypePlaceholder, 16, SpringLayout.SOUTH, pencilSelector);
		
		tileTypeTitle = new NeuralRacing.UILabel(new Dimension(140, 36), NeuralRacing.darkGrayColor, 15, () -> tileTypeTitle.title = LanguageManager.grabStringFromID("tileType") + " :");
		tileTypeTitle.setForeground(Color.white);
		tileTypeTitle.setFont(NeuralRacing.getSystemFont(Font.BOLD, 16));
		detailView.add(tileTypeTitle);
		detailViewLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, tileTypeTitle, 0, SpringLayout.HORIZONTAL_CENTER, detailView);
		detailViewLayout.putConstraint(SpringLayout.NORTH, tileTypeTitle, 14, SpringLayout.NORTH, tileTypePlaceholder);
		detailView.setComponentZOrder(tileTypeTitle, 0);
		
		orientationPlaceholder = new NeuralRacing.UIView(new Dimension(165, 115), NeuralRacing.widespreadGreenishGreen, 20);
		detailView.add(orientationPlaceholder);
		detailViewLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, orientationPlaceholder, 0, SpringLayout.HORIZONTAL_CENTER, detailView);
		detailViewLayout.putConstraint(SpringLayout.NORTH, orientationPlaceholder, 16, SpringLayout.SOUTH, tileTypePlaceholder);
		
		orientationTitle = new NeuralRacing.UILabel(new Dimension(140, 36), NeuralRacing.darkGrayColor, 15, () -> orientationTitle.title = LanguageManager.grabStringFromID("orientation") + " :");
		orientationTitle.setForeground(Color.white);
		orientationTitle.setFont(NeuralRacing.getSystemFont(Font.BOLD, 16));
		detailView.add(orientationTitle);
		detailViewLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, orientationTitle, 0, SpringLayout.HORIZONTAL_CENTER, detailView);
		detailViewLayout.putConstraint(SpringLayout.NORTH, orientationTitle, 18, SpringLayout.NORTH, orientationPlaceholder);
		detailView.setComponentZOrder(orientationTitle, 0);
		
		toolsPlaceholder = new NeuralRacing.UIView(new Dimension(165, 130), NeuralRacing.widespreadGreenishGreen, 20);
		detailView.add(toolsPlaceholder);
		detailViewLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, toolsPlaceholder, 0, SpringLayout.HORIZONTAL_CENTER, detailView);
		detailViewLayout.putConstraint(SpringLayout.NORTH, toolsPlaceholder, 16, SpringLayout.SOUTH, orientationPlaceholder);
		
		toolsTitle = new NeuralRacing.UILabel(new Dimension(140, 36), NeuralRacing.darkGrayColor, 15, () -> toolsTitle.title = LanguageManager.grabStringFromID("tools") + " :");
		toolsTitle.setForeground(Color.white);
		toolsTitle.setFont(NeuralRacing.getSystemFont(Font.BOLD, 16));
		detailView.add(toolsTitle);
		detailViewLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, toolsTitle, 0, SpringLayout.HORIZONTAL_CENTER, detailView);
		detailViewLayout.putConstraint(SpringLayout.NORTH, toolsTitle, 18, SpringLayout.NORTH, toolsPlaceholder);
		detailView.setComponentZOrder(toolsTitle, 0);
		
		tileChooser = new JButton();
		tileChooser.setBorderPainted(false);
		tileChooser.addActionListener(this);
		tileChooser.setContentAreaFilled(false);
		tileChooser.setFocusPainted(false);
		tileChooser.setIcon(NeuralRacing.straightRoadSelectorIcon);
		detailView.add(tileChooser);
		detailViewLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, tileChooser, 0, SpringLayout.HORIZONTAL_CENTER, tileTypePlaceholder);
		detailViewLayout.putConstraint(SpringLayout.NORTH, tileChooser, 50, SpringLayout.NORTH, tileTypePlaceholder);
		detailView.setComponentZOrder(tileChooser, 0);
		
		rotateLeft = new NeuralRacing.UIButton(new Dimension(33, 36), NeuralRacing.widespreadGreenishGreen, 0, null);
		rotateLeft.setIcon(NeuralRacing.rotateLeft);
		rotateLeft.highlight = false;
		rotateLeft.addActionListener(this);
		detailView.add(rotateLeft);
		detailViewLayout.putConstraint(SpringLayout.WEST, rotateLeft, 35, SpringLayout.WEST, orientationPlaceholder);
		detailViewLayout.putConstraint(SpringLayout.NORTH, rotateLeft, 60, SpringLayout.NORTH, orientationPlaceholder);
		detailView.setComponentZOrder(rotateLeft, 0);
		
		rotateRight = new NeuralRacing.UIButton(new Dimension(33, 36), NeuralRacing.widespreadGreenishGreen, 0, null);
		rotateRight.setIcon(NeuralRacing.rotateRight);
		rotateRight.highlight = false;
		rotateRight.addActionListener(this);
		detailView.add(rotateRight);
		detailViewLayout.putConstraint(SpringLayout.EAST, rotateRight, -35, SpringLayout.EAST, orientationPlaceholder);
		detailViewLayout.putConstraint(SpringLayout.NORTH, rotateRight, 60, SpringLayout.NORTH, orientationPlaceholder);
		detailView.setComponentZOrder(rotateRight, 0);
		
		fill = new NeuralRacing.UIButton(new Dimension(65, 24), NeuralRacing.darkGrayColor, 12, () -> fill.title = LanguageManager.grabStringFromID("fill"));
		fill.setFont(NeuralRacing.getSystemFont(Font.BOLD, 12));
		fill.setForeground(Color.white);
		fill.addActionListener(this);
		detailView.add(fill);
		detailViewLayout.putConstraint(SpringLayout.WEST, fill, 12, SpringLayout.WEST, toolsPlaceholder);
		detailViewLayout.putConstraint(SpringLayout.NORTH, fill, 60, SpringLayout.NORTH, toolsPlaceholder);
		detailView.setComponentZOrder(fill, 0);
		
		copy = new NeuralRacing.UIButton(new Dimension(65, 24), NeuralRacing.darkGrayColor, 12, () -> copy.title = LanguageManager.grabStringFromID("copy"));
		copy.addActionListener(this);
		copy.setFont(NeuralRacing.getSystemFont(Font.BOLD, 12));
		copy.setForeground(Color.white);
		detailView.add(copy);
		detailViewLayout.putConstraint(SpringLayout.EAST, copy, -12, SpringLayout.EAST, toolsPlaceholder);
		detailViewLayout.putConstraint(SpringLayout.NORTH, copy, 60, SpringLayout.NORTH, toolsPlaceholder);
		detailView.setComponentZOrder(copy, 0);
		
		paste = new NeuralRacing.UIButton(new Dimension(65, 24), NeuralRacing.darkGrayColor, 12, () -> paste.title = LanguageManager.grabStringFromID("paste"));
		paste.setFont(NeuralRacing.getSystemFont(Font.BOLD, 12));
		paste.setForeground(Color.white);
		paste.addActionListener(this);
		detailView.add(paste);
		detailViewLayout.putConstraint(SpringLayout.WEST, paste, 12, SpringLayout.WEST, toolsPlaceholder);
		detailViewLayout.putConstraint(SpringLayout.NORTH, paste, 5, SpringLayout.SOUTH, fill);
		detailView.setComponentZOrder(paste, 0);
		
		cut = new NeuralRacing.UIButton(new Dimension(65, 24), NeuralRacing.darkGrayColor, 12, () -> cut.title = LanguageManager.grabStringFromID("cut"));
		cut.addActionListener(this);
		cut.setFont(NeuralRacing.getSystemFont(Font.BOLD, 12));
		cut.setForeground(Color.white);
		detailView.add(cut);
		detailViewLayout.putConstraint(SpringLayout.EAST, cut, -12, SpringLayout.EAST, toolsPlaceholder);
		detailViewLayout.putConstraint(SpringLayout.NORTH, cut, 5, SpringLayout.SOUTH, copy);
		detailView.setComponentZOrder(cut, 0);
		
		shouldShowTools(false);
		
		tileChooserMenu = new JPopupMenu();
		tileChooserMenu.setBackground(NeuralRacing.darkerGrayColor);
		straightTile.setFont(menuFont);
		straightTile.setBackground(NeuralRacing.darkerGrayColor);
		straightTile.setForeground(Color.WHITE);
		curvedTile.setFont(menuFont);
		curvedTile.setBackground(NeuralRacing.darkerGrayColor);
		curvedTile.setForeground(Color.WHITE);
		startTile.setFont(menuFont);
		startTile.setBackground(NeuralRacing.darkerGrayColor);
		startTile.setForeground(Color.WHITE);
		grassTile.setFont(menuFont);
		grassTile.setBackground(NeuralRacing.darkerGrayColor);
		grassTile.setForeground(Color.WHITE);
		tileChooserMenu.add(straightTile);
		tileChooserMenu.add(curvedTile);
		tileChooserMenu.add(startTile);
		tileChooserMenu.add(grassTile);
		
		ActionListener tileSelectionAction = new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				//On réinitialise l'orientation.
				orientation = 0;
				tileType = e.getSource() == straightTile ? TileType.STRAIGHT_ROAD : e.getSource() == curvedTile ? TileType.CURVED_ROAD : e.getSource() == startTile ? TileType.START : TileType.GRASS;
				tileChooser.setIcon(tileType == TileType.STRAIGHT_ROAD ? NeuralRacing.straightRoadSelectorIcon : tileType == TileType.CURVED_ROAD ? NeuralRacing.curvedRoadSelectorIcon : tileType == TileType.START ? NeuralRacing.startLineSelectorIcon : NeuralRacing.grassSelectorIcon);
			}
		};
		
		straightTile.addActionListener(tileSelectionAction);
		curvedTile.addActionListener(tileSelectionAction);
		startTile.addActionListener(tileSelectionAction);
		grassTile.addActionListener(tileSelectionAction);
		
		contentView.setLayout(new BorderLayout());
		dragView.setPreferredSize(new Dimension((int) (size.getWidth()-200), (int) (size.getHeight()-200)));
		contentView.addMouseListener((DragToSelectView) dragView);
		contentView.addMouseMotionListener((DragToSelectView) dragView);
		contentView.add(dragView, BorderLayout.CENTER);
		
		this.add(headerView, BorderLayout.NORTH);
		this.add(detailView, BorderLayout.EAST);
		this.add(contentView, BorderLayout.WEST);
	}
	
	/**
	 * Cette méthode permet de régler en un seul coup la visibilité
	 * et la disponibilité des vues et des boutons exclusifs au mode de sélection.
	 * 
	 * @param bool
	 * Le booléen utilisé.
	 */
	private void shouldShowTools(boolean bool) {
		toolsPlaceholder.setVisible(bool);
		toolsTitle.setVisible(bool);
		fill.setVisible(bool);
		copy.setVisible(bool);
		paste.setVisible(bool);
		cut.setVisible(bool);
		fill.setEnabled(bool);
		copy.setEnabled(bool);
		paste.setEnabled(bool);
		cut.setEnabled(bool);
	}
	
	@Override public void actionPerformed(ActionEvent e) {
		
		if (e.getSource().equals(backToMenu)) {
			
			if (clear()) { NeuralRacing.showView("menuPanel"); }
			
		} else if (e.getSource().equals(manageFile)) {
			
			manageMenu.show(manageFile, -30, 70);
			
		} else if (e.getSource().equals(tileChooser)) {
			
			tileChooserMenu.show(tileChooser, -70, 100);
			
		} else if (e.getSource().equals(rotateLeft)) {
			
			//L'herbe n'a pas d'orientation.
			if (tileType == TileType.GRASS) { return; }
			
			//On retourne l'icône du bouton.
			tileChooser.setIcon(new ImageIcon(leftRotation.filter(NeuralRacing.getImageFromIcon(tileChooser.getIcon()), null)));
			
			orientation--;
			
			if (orientation < 0) { orientation = (tileType == TileType.STRAIGHT_ROAD ? 1 : 3); }
			
		} else if (e.getSource().equals(rotateRight)) {
			
			//L'herbe n'a pas d'orientation.
			if (tileType == TileType.GRASS) { return; }
			
			tileChooser.setIcon(new ImageIcon(rightRotation.filter(NeuralRacing.getImageFromIcon(tileChooser.getIcon()), null)));
			
			orientation++;
			
			if (orientation > (tileType == TileType.STRAIGHT_ROAD ? 1 : 3)) { orientation = 0; }
			
		} else if (e.getSource().equals(fill)) {
			
			//Si la sélection est nulle, on ignore l'action.
			if (firstSelectedRow != -1 && firstSelectedColumn != -1 && lastSelectedRow != -1 && lastSelectedColumn != -1) {
				
				//On place des cases, donc la grille n'est plus immaculée.
				editsSaved = false;
				
				for (int column = firstSelectedColumn; column < lastSelectedColumn; column++) {
					for (int row = firstSelectedRow; row < lastSelectedRow; row++) {
						RaceGrid.Box newBox = new RaceGrid.Box(tileType, orientation*Math.PI/2);
						newBox.orientationType = orientation;
						raceGrid.grid[column][row] = newBox;
					}
				}
				contentView.repaint();
			}
		} else if (e.getSource().equals(copy)) {
			
			//Si la sélection est nulle, on ignore l'action.
			if (firstSelectedRow != -1 && firstSelectedColumn != -1 && lastSelectedRow != -1 && lastSelectedColumn != -1 && raceGrid != null) {
				clipBoard = raceGrid.slice(firstSelectedColumn, firstSelectedRow, lastSelectedColumn-firstSelectedColumn, lastSelectedRow-firstSelectedRow);
			}
			
		} else if (e.getSource().equals(paste)) {
			
			//Si la sélection est nulle, on ignore l'action.
			if (firstSelectedRow != -1 && firstSelectedColumn != -1 && lastSelectedRow != -1 && lastSelectedColumn != -1 && raceGrid != null && clipBoard != null) {
				
				//On place des cases, donc la grille n'est plus immaculée.
				editsSaved = false;
				
				raceGrid.insertGrid(clipBoard, new Rectangle(firstSelectedColumn, firstSelectedRow, lastSelectedColumn-firstSelectedColumn, lastSelectedRow-firstSelectedRow));
				contentView.repaint();
			}
			
		} else if (e.getSource().equals(cut)) {
			
			//Si la sélection est nulle, on ignore l'action.
			if (firstSelectedRow != -1 && firstSelectedColumn != -1 && lastSelectedRow != -1 && lastSelectedColumn != -1 && raceGrid != null) {
				
				//On place des cases, donc la grille n'est plus immaculée.
				editsSaved = false;
				
				clipBoard = raceGrid.slice(firstSelectedColumn, firstSelectedRow, lastSelectedColumn-firstSelectedColumn, lastSelectedRow-firstSelectedRow);
				//On remplace la partie découpée par de l'herbe.
				raceGrid.insertGrid(new RaceGrid(new EnhancedString(""), true, lastSelectedColumn-firstSelectedColumn, lastSelectedRow-firstSelectedRow), new Rectangle(firstSelectedColumn, firstSelectedRow, lastSelectedColumn-firstSelectedColumn, lastSelectedRow-firstSelectedRow));
				contentView.repaint();
			}
			
		}
	}
	
	/**
	 * Cette méthode permet de nettoier le contenu de la <code>RaceEditorView</code>.
	 * 
	 * @return
	 * <strong>False</strong> si l'utilisateur a annulé le nettoyage, <strong>true</strong> sinon.
	 */
	public boolean clear() {
		
		if (editsSaved) { return true; }
		
		boolean modified = !(raceGrid.equals(referenceGrid) || raceGrid.equals(new RaceGrid(new EnhancedString(""), true)));
		
		if (!modified) {
			firstSelectedRow    = -1;
			firstSelectedColumn = -1;
			lastSelectedRow     = -1;
			lastSelectedColumn  = -1;
			return true;
		}
		
		if (JOptionPane.showConfirmDialog(this, LanguageManager.grabStringFromID("giveUpRace"), LanguageManager.grabStringFromID("warning"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE) != JOptionPane.OK_OPTION) { return false; }
		
		((DragToSelectView) dragView).clear();
		
		firstSelectedRow    = -1;
		firstSelectedColumn = -1;
		lastSelectedRow     = -1;
		lastSelectedColumn  = -1;
		
		//On nettoie la grille de jeu.
		try {
			raceGrid.synchronizeWithGrid(new RaceGrid(new EnhancedString(""), true));
			referenceGrid.synchronizeWithGrid(new RaceGrid(new EnhancedString(""), true));
		} catch (IllegalArgumentException exc) {}
		
		repaint();
		
		editsSaved = true;
		return true;
	}
	
	/**
	 * Cette sous-classe de <code>JPanel</code> permet de gérer les interactions avec l'utilisateur :
	 * la <code>RaceView</code> ne servant que d'afficheur, un panneau transparent disposé au-dessus permet
	 * à l'utilisateur de sélectionner des zones ou de cliquer en des endroits précis, pour modifier la grille de jeu
	 * sous-jacente. Cette vue affiche notamment une zone translucide mettant en évidence la sélection actuelle.
	 * 
	 * @author Ronan
	 * 
	 * @see JPanel
	 * @see RaceView
	 * @see RaceEditorView
	 *
	 */
	class DragToSelectView extends JPanel implements MouseListener, MouseMotionListener {

		private static final long serialVersionUID = 5050578549037655532L;
		
		/**Le point du début de la sélection.*/
		Point selectionBegin;
		
		/**Le point de fin de la sélection.*/
		Point selectionEnd;
		
		/**Le point au Nord-Ouest de la sélection.*/
		Point northWest;
		
		/**Le point au Sud-Est de la sélection.*/
		Point southEast;
		
		public DragToSelectView() { this.setOpaque(false); }
		
		/**
		 * Cette méthode réinitialise les données de la vue : la sélection est vidée.
		 */
		public void clear() {
			selectionBegin = null;
			selectionEnd   = null;
			northWest      = null;
			southEast      = null;
		}
		
		/**
		 * Cette méthode permet de corriger les coordonnées d'un point afin qu'il rentre dans la grille de jeu.
		 * 
		 * @param source
		 * Le point à corriger.
		 * 
		 * @return
		 * Le point corrigé.
		 * 
		 */
		public Point getConstrainedCoords(Point source) {
			//La vue mesure 700 par 560, mais on enlève 1 car le pixel suivant serait le début d'une colonne/ligne qui n'existe pas.
			return new Point(Math.max(0, Math.min(source.x, 699)), Math.max(0, Math.min(source.y, 559)));
		}
		
		@Override public void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (firstSelectedRow != -1 && firstSelectedColumn != -1 && lastSelectedRow != -1 && lastSelectedColumn != -1) {
				//Si on a une zone sélectionnée, on en trace le contour, puis la zone intérieure, avec de la transparence.
				g.setColor(Color.WHITE);
				g.drawLine(firstSelectedColumn*70, firstSelectedRow*70, lastSelectedColumn*70,  firstSelectedRow*70);
				g.drawLine(firstSelectedColumn*70, firstSelectedRow*70, firstSelectedColumn*70, lastSelectedRow*70);
				g.drawLine(lastSelectedColumn*70,  lastSelectedRow*70,  lastSelectedColumn*70,  firstSelectedRow*70);
				g.drawLine(lastSelectedColumn*70,  lastSelectedRow*70,  firstSelectedColumn*70, lastSelectedRow*70);
				g.setColor(new Color(255, 255, 255, 50));
				g.fillRect(70*Math.min(firstSelectedColumn, lastSelectedColumn), 70*Math.min(firstSelectedRow, lastSelectedRow), 70*Math.abs(lastSelectedColumn-firstSelectedColumn), 70*Math.abs(lastSelectedRow-firstSelectedRow));
			}
		}
		
		@Override public void mouseDragged(MouseEvent e) {
			
			if (leftCursorSelected) {
				
				//Mode de peinture par point.
				
				//On a posé (au moins) une case, donc le circuit n'est plus immaculé.
				editsSaved = false;
				
				Point drawn = getConstrainedCoords(e.getPoint());
				
				//Une case a une largeur de 70.
				int row     = drawn.y / 70,
					column  = drawn.x / 70;
				
				//On ajoute la case.
				raceGrid.grid[column][row] = new RaceGrid.Box(tileType, orientation * Math.PI/2);
				raceGrid.grid[column][row].orientationType = orientation;
				
				contentView.repaint();
				return;
				
			} else {
				
				//Mode de sélection.
				
				selectionEnd = getConstrainedCoords(e.getPoint());
				
				northWest = new Point(Math.min(selectionBegin.x, selectionEnd.x), Math.min(selectionBegin.y, selectionEnd.y));
				southEast = new Point(Math.max(selectionBegin.x, selectionEnd.x), Math.max(selectionBegin.y, selectionEnd.y));
				
				//Si le curseur se déplace mais que la sélection n'a pas encore changée, on évite des calculs inutiles.
				if (firstSelectedRow == northWest.y / 70 && firstSelectedColumn == northWest.x / 70 && lastSelectedRow == 1 + southEast.y / 70 && lastSelectedColumn == 1 + southEast.x / 70) { return; }
				
				firstSelectedRow    = northWest.y     / 70;
				lastSelectedRow     = 1 + southEast.y / 70;
				firstSelectedColumn = northWest.x     / 70;
				lastSelectedColumn  = 1 + southEast.x / 70;
				
				this.repaint();
			}
		}
		
		@Override public void mousePressed(MouseEvent e) {
			//En mode peinture par point on ignore les pressions.
			if (leftCursorSelected) { return; }
			
			//En mode de sélection en revanche, elles permettent de trouver le début de la sélection.
			selectionBegin = e.getPoint();
		}
		
		@Override public void mouseReleased(MouseEvent e) {
			//En mode peinture par point on ignore les fins de pression.
			if (leftCursorSelected) { return; }
			//Le processus de sélection en cours est achevé.
			selectionBegin = selectionEnd = null;
			this.repaint();
		}
		
		@Override public void mouseClicked(MouseEvent e) {
			
			if (!leftCursorSelected) {
				//En mode de sélection, un clic permet d'abandonenr la dernière sélection.
				firstSelectedRow = firstSelectedColumn = lastSelectedRow = lastSelectedColumn  = -1;
				this.repaint();
			} else {
				//Une case a été posée, le circuit n'est plus immaculé.
				editsSaved = false;
				
				Point drawn = getConstrainedCoords(e.getPoint());
				
				int row     = drawn.y / 70,
					column  = drawn.x / 70;
				
				//On ajoute la case.
				raceGrid.grid[column][row] = new RaceGrid.Box(tileType, orientation * Math.PI/2);
				raceGrid.grid[column][row].orientationType = orientation;
				
				contentView.repaint();
				return;
			}
		}
		
		@Override public void    mouseMoved(MouseEvent e) {}
		@Override public void  mouseEntered(MouseEvent e) {}
		@Override public void   mouseExited(MouseEvent e) {}
		
	}
	
}
