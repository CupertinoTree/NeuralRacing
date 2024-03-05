package neuralPckg;

/**
 * Cette classe peut-être considérée comme une extension des possibilités
 * offertes par le type <code>String</code>.
 * 
 * @author Ronan
 * 
 * @see String
 *
 */
public class EnhancedString {

	/**La valeur de la chaîne de caractère.*/
	private String value;
	
	/**
	 * Ce constructeur permet d'obtenir un <code>EnhancedString</code> de valeur connue.
	 * 
	 * @param value
	 * La valeur à utiliser.
	 * 
	 * @see EnhancedString
	 */
	public EnhancedString(String value) { this.value = value; }
	
	/**
	 * Cette méthode permet d'obtenir la valeur du <code>EnhancedString</code>.
	 * 
	 * @return
	 * La valeur du <code>EnhancedString</code>.
	 * 
	 * @see EnhancedString
	 */
	String getValue() { return this.value; }
	
	/**
	 * Cette méthode permet de donner une valeur au <code>EnhancedString</code>.
	 * 
	 * @param value
	 * La valeur à adopter.
	 * 
	 * @see EnhancedString
	 */
	void setValue(String value) { this.value = value; }
    
	/**
	 * Cette méthode permet d'obtenir le premier caractère du <code>EnhancedString</code>.
	 * 
	 * @return
	 * Le premier caractère du <code>EnhancedString</code>.
	 * 
	 * @see EnhancedString
	 */
    char firstChar() { return value.charAt(0); }
    
    /**
     * Cette méthode permet d'obtenir une partie de la valeur du <code>EnhancedString</code>.
     * Le découpage se fait à partir d'un certain index, jusqu'à la fin.
     * 
     * @param begin
     * L'index de début.
     * 
     * @return
     * Le <code>EnhancedString</code> obtenu.
     * 
     * @see EnhancedString
     */
    EnhancedString substringFrom(int begin) { return new EnhancedString(this.value.substring(begin, this.value.length())); }
    
    /**
     * Cette méthode permet d'obtenir une partie de la valeur du <code>EnhancedString</code>.
     * Le découpage se fait à partir du début, jusqu'à un certain index.
     * 
     * @param end
     * L'index de fin.
     * 
     * @return
     * Le <code>EnhancedString</code> obtenu.
     * 
     * @see EnhancedString
     */
    EnhancedString substringTo(int end) { return new EnhancedString(this.value.substring(0, end)); }
    
    /**
     * Cette méthode permet d'obtenir une partie de la valeur du <code>EnhancedString</code>.
     * Le découpage se fait à partir d'un certain index, jusqu'à un autre index.
     * 
     * @param begin
     * L'index de début.
     * @param end
     * L'index de fin.
     * 
     * @return
     * Le <code>EnhancedString</code> obtenu.
     * 
     * @see EnhancedString
     */
    EnhancedString substring(int begin, int end) { return new EnhancedString(this.value.substring(begin, end)); }
   
    /**
     * Cette méthode permet d'obtenir une partie de la valeur du <code>EnhancedString</code>.
     * Le découpage se fait à partir d'un caractère, jusqu'à un autre caractère.
     * 
     * @param beginning
     * Le caractère de début.
     * @param endChar
     * Le caractère de fin.
     * 
     * @return
     * Le <code>EnhancedString</code> obtenu.
     * 
     * @see EnhancedString
     */
    EnhancedString substring(char beginning, char endChar) {
        int beginningIndex       = this.firstIndexOf(beginning);
        EnhancedString scdString = this.substringFrom(beginningIndex);
        int end                  = scdString.firstIndexOf(endChar);
        return new EnhancedString(this.value.substring(beginningIndex + 1, end+1));
    }
    
	/**
	 * Cette méthode permet d'obtenir l'index de la première apparition d'un caractère dans le <code>EnhancedString</code>.
	 * 
	 * @param element
	 * Le caractère considéré.
	 * 
	 * @return
	 * L'index de la première apparition de ce caractère.
	 * 
	 * @see EnhancedString
	 */
    int firstIndexOf(char element) {
    	
    	if (this.value == null) { return -1; }
    	
        int index = -1;
        for (char iterator: this.value.toCharArray()) {
            index++;
            if (iterator == element) {
                return index;
            }
        }
        return -1;
    }
    
    /**
     * Cette méthode permet d'obtenir l'index de la dernière apparition d'un caractère dans le <code>EnhancedString</code>.
     * 
     * @param element
     * Le caractère considéré.
     * 
     * @return
     * L'index de la dernière apparition de ce caractère.
     * 
     * @see EnhancedString
     */
    int lastIndexOf(char element) {
        int index = -1;
        int lastIndex = -1;
        for (char iterator: this.value.toCharArray()) {
            index++;
            if (iterator == element) {
                lastIndex = index;
            }
        }
        return lastIndex;
    }
	
}
