package neuralPckg;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

public class UIUserGuideFrame extends JFrame {
	
	private static final long serialVersionUID = 834466844131L;
	
	JPanel tableView    = new JPanel();
	SpringLayout layout = new SpringLayout();
	
	JScrollPane contentView;
	JEditorPane webViewer;
	
	JButton evolutionView    = new JButton(),
			experimentsView  = new JButton(),
		    raceEditorView   = new JButton(),
			optionsView      = new JButton(),
			carsAndTracks    = new JButton(),
			neuralNetworks   = new JButton(),
			geneticAlgorithm = new JButton(),
			selected         = null;
	
	JLabel usageLabel        = new JLabel(),
		   theoryLabel       = new JLabel();
	
	NeuralRacing.UILabel header;
	
	boolean isExpanded = true;
	
	String selectedPage = "evolutionView";
	
	public UIUserGuideFrame() {
		
		super("");
		setSize(700, 600);
		getContentPane().setLayout(layout);
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		
		LanguageManager.registerUIElement(new LanguageManager.LanguageMutable() {
			@Override public void updateData() {
				setTitle(LanguageManager.grabStringFromID("guide"));
				
				usageLabel.setText("   " + LanguageManager.grabStringFromID("usageSection"));
				
				evolutionView.setText("   " + LanguageManager.grabStringFromID("evolutionTitle"));
				experimentsView.setText("   " + LanguageManager.grabStringFromID("experimentsTitle"));
				raceEditorView.setText("   " + LanguageManager.grabStringFromID("raceEditorTitleExtended"));
				optionsView.setText("   " + LanguageManager.grabStringFromID("optionsTitle"));
				
				theoryLabel.setText("   " + LanguageManager.grabStringFromID("theorySection"));
				
				carsAndTracks.setText("   " + LanguageManager.grabStringFromID("carsAndTracksTitle"));
				neuralNetworks.setText("   " + LanguageManager.grabStringFromID("neuralNetworksTitle"));
				geneticAlgorithm.setText("   " + LanguageManager.grabStringFromID("geneticAlgorithmTitle"));
				
				loadTabPage(selectedPage);
			}
		});
		
		tableView.setBackground(NeuralRacing.lightGrayColor);
		tableView.setPreferredSize(new Dimension(200, 600));
		
		header = new NeuralRacing.UILabel(new Dimension(200, 60), NeuralRacing.lighterGrayColor, 0, () -> header.title = LanguageManager.grabStringFromID("guide"));
		header.setIcon(NeuralRacing.guideIcon);
		header.setForeground(Color.BLACK);
		header.setFont(NeuralRacing.getSystemFont(Font.BOLD, 25));
		tableView.add(header);
		
		ActionListener tabListener = new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				
				if (e.getSource().equals(evolutionView)) {
					
					loadTabPage("evolutionView");
					
				} else if (e.getSource().equals(experimentsView)) {
					
					loadTabPage("experimentsView");
					
				} else if (e.getSource().equals(raceEditorView)) {
					
					loadTabPage("raceEditorView");
					
				} else if (e.getSource().equals(optionsView)) {
					
					loadTabPage("optionsView");
					
				} else if (e.getSource().equals(carsAndTracks)) {
					
					loadTabPage("carsAndTracks");
					
				} else if (e.getSource().equals(neuralNetworks)) {
					
					loadTabPage("neuralNetworks");
					
				} else if (e.getSource().equals(geneticAlgorithm)) {
					
					loadTabPage("geneticAlgorithm");
					
				}
				
				loadTabPage(selectedPage);
			}
		};
		
		usageLabel.setForeground(NeuralRacing.darkGreenBackground);
		usageLabel.setBackground(NeuralRacing.lighterGrayColor);
		usageLabel.setHorizontalAlignment(JLabel.LEFT);
		usageLabel.setVerticalAlignment(JLabel.BOTTOM);
		usageLabel.setPreferredSize(new Dimension(200, 40));
		usageLabel.setFont(NeuralRacing.getSystemFont(Font.BOLD, 15));
		tableView.add(usageLabel);
		
		evolutionView.setForeground(Color.BLACK);
		evolutionView.setBackground(NeuralRacing.lighterGrayColor);
		evolutionView.setFocusPainted(false);
		evolutionView.setBorder(BorderFactory.createEmptyBorder());
		evolutionView.setHorizontalAlignment(JButton.LEFT);
		evolutionView.setPreferredSize(new Dimension(200, 50));
		evolutionView.setFont(NeuralRacing.getSystemFont(Font.BOLD, 15));
		evolutionView.addActionListener(tabListener);
		tableView.add(evolutionView);
		
		experimentsView.setForeground(Color.BLACK);
		experimentsView.setBackground(NeuralRacing.lighterGrayColor);
		experimentsView.setFocusPainted(false);
		experimentsView.setBorder(BorderFactory.createEmptyBorder());
		experimentsView.setHorizontalAlignment(JButton.LEFT);
		experimentsView.setPreferredSize(new Dimension(200, 50));
		experimentsView.setFont(NeuralRacing.getSystemFont(Font.BOLD, 15));
		experimentsView.addActionListener(tabListener);
		tableView.add(experimentsView);
		
		raceEditorView.setForeground(Color.BLACK);
		raceEditorView.setBackground(NeuralRacing.lighterGrayColor);
		raceEditorView.setFocusPainted(false);
		raceEditorView.setBorder(BorderFactory.createEmptyBorder());
		raceEditorView.setHorizontalAlignment(JButton.LEFT);
		raceEditorView.setPreferredSize(new Dimension(200, 50));
		raceEditorView.setFont(NeuralRacing.getSystemFont(Font.BOLD, 15));
		raceEditorView.addActionListener(tabListener);
		tableView.add(raceEditorView);
		
		optionsView.setForeground(Color.BLACK);
		optionsView.setBackground(NeuralRacing.lighterGrayColor);
		optionsView.setFocusPainted(false);
		optionsView.setBorder(BorderFactory.createEmptyBorder());
		optionsView.setHorizontalAlignment(JButton.LEFT);
		optionsView.setPreferredSize(new Dimension(200, 50));
		optionsView.setFont(NeuralRacing.getSystemFont(Font.BOLD, 15));
		optionsView.addActionListener(tabListener);
		tableView.add(optionsView);
		
		theoryLabel.setForeground(NeuralRacing.darkGreenBackground);
		theoryLabel.setBackground(NeuralRacing.lighterGrayColor);
		theoryLabel.setHorizontalAlignment(JLabel.LEFT);
		theoryLabel.setVerticalAlignment(JLabel.BOTTOM);
		theoryLabel.setPreferredSize(new Dimension(200, 40));
		theoryLabel.setFont(NeuralRacing.getSystemFont(Font.BOLD, 15));
		tableView.add(theoryLabel);
		
		carsAndTracks.setForeground(Color.BLACK);
		carsAndTracks.setBackground(NeuralRacing.lighterGrayColor);
		carsAndTracks.setFocusPainted(false);
		carsAndTracks.setBorder(BorderFactory.createEmptyBorder());
		carsAndTracks.setHorizontalAlignment(JButton.LEFT);
		carsAndTracks.setPreferredSize(new Dimension(200, 50));
		carsAndTracks.setFont(NeuralRacing.getSystemFont(Font.BOLD, 15));
		carsAndTracks.addActionListener(tabListener);
		tableView.add(carsAndTracks);
		
		neuralNetworks.setForeground(Color.BLACK);
		neuralNetworks.setBackground(NeuralRacing.lighterGrayColor);
		neuralNetworks.setFocusPainted(false);
		neuralNetworks.setBorder(BorderFactory.createEmptyBorder());
		neuralNetworks.setHorizontalAlignment(JButton.LEFT);
		neuralNetworks.setPreferredSize(new Dimension(200, 50));
		neuralNetworks.setFont(NeuralRacing.getSystemFont(Font.BOLD, 15));
		neuralNetworks.addActionListener(tabListener);
		tableView.add(neuralNetworks);
		
		geneticAlgorithm.setForeground(Color.BLACK);
		geneticAlgorithm.setBackground(NeuralRacing.lighterGrayColor);
		geneticAlgorithm.setFocusPainted(false);
		geneticAlgorithm.setBorder(BorderFactory.createEmptyBorder());
		geneticAlgorithm.setHorizontalAlignment(JButton.LEFT);
		geneticAlgorithm.setPreferredSize(new Dimension(200, 50));
		geneticAlgorithm.setFont(NeuralRacing.getSystemFont(Font.BOLD, 15));
		geneticAlgorithm.addActionListener(tabListener);
		tableView.add(geneticAlgorithm);
		
		webViewer = new JEditorPane();
		webViewer.setContentType("text/html;charset=UTF-8");
		webViewer.setEditable(false);
		webViewer.setHighlighter(null);
		webViewer.setPreferredSize(new Dimension(500, 600));
		
		HTMLEditorKit editorKit = new HTMLEditorKit();
		
		StyleSheet sheet = new StyleSheet();
		sheet.addRule("body { margin: 10px }");
		sheet.addRule("h1, h2, h3, h4 {font-family: arial; font-weight: bold; margin-bottom: 7px; color: #2F2E2E; }");
		sheet.addRule("h1 { font-size: 20px; }");
		sheet.addRule("h2 { font-size: 17px; margin-top: 7px; }");
		sheet.addRule("h3 { font-size: 16px; }");
		sheet.addRule("h4 { font-size: 15px; color: #666666; }");
		sheet.addRule("h5, p, ul  { font-family: arial; font-size: 12px; color: #4A4A4A; text-align: justify; margin-bottom: 10px; }");
		sheet.addRule(".green_paragraph { color: green; font-weight: bold; }");
		sheet.addRule(".red_paragraph { color: red; }");
		sheet.addRule(".sub_paragraph { margin-left: 10px; }");
		sheet.addRule("h5, strong { font-weight: bold;color: #3A3A3A; }");
		sheet.addRule("ul, { padding-left: 20px }");
		sheet.addRule("i { font-weight: italic; }");
		editorKit.setStyleSheet(sheet);
		
		webViewer.setEditorKit(editorKit);
		webViewer.setDocument(editorKit.createDefaultDocument());
		
		contentView = new JScrollPane(webViewer);
		contentView.setBackground(Color.WHITE);
		contentView.getViewport().setBackground(Color.WHITE);;
		contentView.setBorder(BorderFactory.createEmptyBorder());
		contentView.setPreferredSize(new Dimension(500, 600));
		contentView.getVerticalScrollBar().setUnitIncrement(8);
		
		webViewer.addMouseListener(new MouseAdapter() {
			
			@Override public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() != 2 ||  SwingUtilities.isRightMouseButton(e)) { return; }
				isExpanded = !isExpanded;
				refreshLayout();
			}
			
		});
		
		refreshLayout();
		
	}
	
	public void openPage(String pageIdentifier) {
		if (selectedPage != pageIdentifier) { loadTabPage(pageIdentifier); }
		if (!this.isVisible()) { this.setVisible(true); }
	}
	
	private void loadTabPage(String pageIdentifier) {
		
		String defaultPage = "";
		
		try {
			webViewer.setPage(NeuralRacing.reader.getResource("/guide/" + NeuralRacing.UserDefaults.language + "/" + pageIdentifier + ".html"));
			selectedPage = pageIdentifier;
		} catch (Exception exc) {
			JOptionPane.showMessageDialog(NeuralRacing.mainPanel, LanguageManager.grabStringFromID("contentTextNotFound"), LanguageManager.grabStringFromID("error"), JOptionPane.ERROR_MESSAGE);
		}
		
		webViewer.setText(defaultPage);
		webViewer.setCaretPosition(0);
		
		if (selected != null) {
			selected.setBackground(NeuralRacing.lighterGrayColor);
		}
		
		switch(pageIdentifier) {
			case "evolutionView": selected = evolutionView; break;
			case "experimentsView": selected = experimentsView; break;
			case "raceEditorView": selected = raceEditorView; break;
			case "optionsView": selected = optionsView; break;
			case "carsAndTracks": selected = carsAndTracks; break;
			case "neuralNetworks": selected = neuralNetworks; break;
			case "geneticAlgorithm": selected = geneticAlgorithm; break;
		}
		
		selected.setBackground(NeuralRacing.lighterGrayColor.darker());
		
	}
	
	public void refreshLayout() {
		
		getContentPane().remove(tableView);
		layout.removeLayoutComponent(tableView);
		getContentPane().remove(contentView);
		layout.removeLayoutComponent(contentView);
		
		if (isExpanded) {
			
			layout.putConstraint(SpringLayout.WEST, tableView, 0, SpringLayout.WEST, getContentPane());
			layout.putConstraint(SpringLayout.NORTH, tableView, 0, SpringLayout.NORTH, getContentPane());
			layout.putConstraint(SpringLayout.SOUTH, tableView, 0, SpringLayout.SOUTH, getContentPane());
			getContentPane().add(tableView);
			
			layout.putConstraint(SpringLayout.EAST, tableView, 200, SpringLayout.WEST, getContentPane());
			layout.putConstraint(SpringLayout.WEST, contentView, 0, SpringLayout.EAST, tableView);
			
			layout.putConstraint(SpringLayout.EAST, contentView, 0, SpringLayout.EAST, getContentPane());
			layout.putConstraint(SpringLayout.NORTH, contentView, 0, SpringLayout.NORTH, getContentPane());
			layout.putConstraint(SpringLayout.SOUTH, contentView, 0, SpringLayout.SOUTH, getContentPane());
			getContentPane().add(contentView);
			
		} else {
			
			layout.putConstraint(SpringLayout.EAST, contentView, 0, SpringLayout.EAST, getContentPane());
			layout.putConstraint(SpringLayout.WEST, contentView, 0, SpringLayout.WEST, getContentPane());
			layout.putConstraint(SpringLayout.NORTH, contentView, 0, SpringLayout.NORTH, getContentPane());
			layout.putConstraint(SpringLayout.SOUTH, contentView, 0, SpringLayout.SOUTH, getContentPane());
			getContentPane().add(contentView);
			
		}
		
		getContentPane().revalidate();
	}
	
}