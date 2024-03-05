package neuralPckg;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import neuralPckg.NeuralRacing.UserDefaults;

public class LanguageManager {
	
	/**Cette interface permet d'assurer que les éléments d'interfaces soient notifiés en cas de changement de langue.*/
	public static interface LanguageMutable { public void updateData();  }
	
	/**Cette liste contient les éléments de texte affichés dans l'interface du jeu.*/
	private static Map<String, String> textElements = new HashMap<String, String>();
	
	private static List<Runnable> primaryMutators = new ArrayList<Runnable>();
	
	/**Cette liste contient les éléments d'interface affichant du texte.*/
	private static List<LanguageMutable> UIElements = new ArrayList<LanguageMutable>();
	
	/**
	 * Cette méthode permet de changer le langage du jeu et de mettre à jour l'interface.
	 * 
	 * @param languageName
	 * Le nom du langage à utiliser.
	 */
	public static void loadLanguage(String languageName) {
		
		notifyPrimaryMutators();
		
		textElements.clear();
		
		EnhancedString line = new EnhancedString("");
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(NeuralRacing.reader.getResourceAsStream("/content/" + languageName + ".txt"), "UTF-8"));
			while (reader.ready()) { 
				line.setValue(reader.readLine());
				if (line.getValue().contains(":")) {
					textElements.put(line.substring(0, line.lastIndexOf(':')).getValue(), line.substring(line.lastIndexOf(':')+1, line.getValue().length()).getValue());
				}
			}
			reader.close();
			
			UserDefaults.language = languageName;
			UserDefaults.save();
			
		} catch (Exception exc) {
			JOptionPane.showMessageDialog(NeuralRacing.mainPanel, grabStringFromID("contentTextNotFound"), grabStringFromID("error"), JOptionPane.ERROR_MESSAGE);
		}
		
		notifyUserInterface();
	}
	
	public static void registerUIElement(LanguageMutable element) {
		UIElements.add(element);
	}
	
	public static void registerPrimaryMutator(Runnable mutator) {
		primaryMutators.add(mutator);
	}
	
	public static void removeUIElement(LanguageMutable element) {
		UIElements.remove(element);
	}
	
	private static void notifyPrimaryMutators() {
		for (Runnable mutator: primaryMutators) {
			mutator.run();
		}
	}
	
	private static void notifyUserInterface() {
		for (LanguageMutable element: UIElements) {
			element.updateData();
		}
	}
	
	public static String grabStringFromID(String identifier) {
		return textElements.get(identifier);
	}
	
}
