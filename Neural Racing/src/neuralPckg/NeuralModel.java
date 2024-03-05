package neuralPckg;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * Cette classe contient tout le matériel nécessaire pour prendre en charge les
 * mécanismes des réseaux de neurones à propagation avant.
 * 
 * @author Ronan
 * 
 * @see BrainStruct
 * @see SquashingFunction
 * @see Neuron
 * @see Layer
 * @see NeuralNetwork
 * 
 */
public class NeuralModel {
	
	/**
	 * Cette classe résume l'architecture du modèle de réseau utilisé.
	 * <p>
	 * <strong>Variables :</strong>
	 * <p>
	 * <code>numberOfHiddenLayers</code> : le nombre de couches cachées.
	 * <p>
	 * <code>numberOfNeuronsPerHiddenLayer</code> : un tableau à une dimension contenant le nombre de neurone dans chaque couche cachée.
	 * <p>
	 * <code>numberOfInputNeurons</code> : le nombre de neurones dans la couche d'entrée.
	 * <p>
	 * <code>numberOfOutputNeurons</code> : le nombre de neurones dans la couche de sortie.
	 * 
	 * @author Ronan
	 * 
	 * @see NeuralNetwork
	 * 
	 */
	static class BrainStruct {
		
		/**Le nombre de couches cachées.*/
	    static int numberOfHiddenLayers = 2;
	    
	    /**Un tableau à une dimension contenant le nombre de neurone dans chaque couche cachée.*/
	    static int [] numberOfNeuronsPerHiddenLayer = { 6, 5 };
	    
	    /**Le nombre de neurones dans la couche d'entrée.*/
	    static int numberOfInputNeurons = 6;
	    
	    /**Le nombre de neurones dans la couche de sortie.*/
	    static int numberOfOutputNeurons = 4;
	}
	
	/**
	 * Cette énumération représente le type de fonction d'activation qui régira les neurones d'une couche.
	 * 
	 * <p><strong>Valeurs :</strong></p>
	 * <p><code>.NONE</code> : aucune fonction d'activation.</p>
	 * <p><code>.TANH</code> : la fonction Tangente Hyperbolique. Le nombre initial est ainsi replacé dans l'intervalle ]-1;1[.</p>
	 *
	 *@author Ronan
	 *
	 *@see NeuralNetwork
	 *
	 */
	static enum SquashingFunction {
	    NONE, TANH;
		int toInt() { return this.equals(SquashingFunction.NONE) ? 0 : 1; }
	    public static SquashingFunction fromInteger(int value) { return value == 0 ? NONE : TANH; }
	}
	
	/**
	 * Un neurone possède un ensemble de connexions, allant vers chacun des neurones de la couche suivante,
	 * qui représentent les synapses des encéphales biologiques. La valeur d'une connexion est également
	 * appelée son <I>poids</I>. Le neurone possède aussi une valeur,
	 * qui est utile lorsque le réseau doit effectuer une prédiction.
	 * 
	 * @author Ronan
	 *
	 * @see NeuralNetwork
	 *
	 */
	static class Neuron {
		
		/**La valeur du neurone. Elle n'a d'importance qu'au cours d'une prédiction.*/
	    float value = 0f;
	    /**La liste des valeurs des connexions du neurone vers chacun de ceux de la couche suivante.*/
	    List<Float> connections = new ArrayList<Float>();
	    
	    /**
	     * Ce constructeur permet d'obtenir un neurone dont les connexions sont déterminées grâce
	     * à une représentation textuelle de la liste des valeurs des connexions du neurone.
	     * Les valeurs successives sont séparées par des tubes (le caractère "|").
	     * 
	     * @param stringRepresentation
	     * La représentation textuelle de la liste des valeurs des connexions du neurone.
	     * 
	     * @see Neuron
	     * 
	     */
	    public Neuron(EnhancedString stringRepresentation) throws IllegalArgumentException {
	    	//On crée une nouvelle chaîne de caractères, que l'on peut découper.
	        EnhancedString connectionsStr = stringRepresentation;
	        
	        //Tant qu'il reste des séparateurs, on découpe la nouvelle chaîne.
	        while (connectionsStr.firstIndexOf('|') != -1) {
	            connectionsStr = connectionsStr.substringFrom(connectionsStr.firstIndexOf('|') + 1);
	            //Puis, s'il y a encore un séparateur, on récupère le nombre qui le précède.
	            if (connectionsStr.firstIndexOf('|') != -1) {
	                this.connections.add(Float.valueOf(connectionsStr.substringTo(connectionsStr.firstIndexOf('|')).getValue()));
	            }
	        }
	    }
	    
	    /**
	     * Ce constructeur permet d'obtenir un neurone dont les connexions sont données.
	     * 
	     * @param connections
	     * La liste des valeurs des connexions du neurone.
	     * 
	     * @see Neuron
	     * 
	     */
	    public Neuron(List<Float> connections) {
	        this.connections = connections;
	    }
	    
	    /**
	     * Ce constructeur permet d'obtenir un neurone dont les connexions sont vides.
	     * 
	     * @see Neuron
	     * 
	     */
	    public Neuron() {}
	    
	    /**
	     * Cette méthode permet de mettre à jour la valeur du neurone, lors du processus
	     * de propagation vers l'avant. On effectue d'abord la somme pondérée
	     * des valeurs des neurones de la couche précédente, respectivement mutlipliées par
	     * le poids de la connexion qui les relie au présent neurone.
	     * La nouvelle valeur du neurone sera à cette somme pondérée,
	     * après passage par la fonction d'activation.
	     * 
	     * @param previousLayer
	     * La couche précédent celle à laquelle le neurone appartient.
	     * 
	     * @param indexInLayer
	     * L'index du neurone dans sa propre couche.
	     * 
	     * @param squashingType
	     * Le type de fonction d'activation.
	     * 
	     * @see Neuron
	     * @see Layer
	     * @see NeuralNetwork
	     * 
	     */
	    void updateValue(Layer previousLayer, int indexInLayer, SquashingFunction squashingType) {
	    	
	    	/**La nouvelle valeur du neurone.*/
	        float newValue = 0f;
	        
	        //On stocke successivement chacun des neurones de la couche précédente dans cette variable
	        //afin d'éviter des itérations successives dues à la méthode get(int index) de la liste
	        //de neurones.
	        Neuron previousNeuron;
	        
	        for (int i = 0; i < previousLayer.neurons.size(); i++) {
	        	previousNeuron = previousLayer.neurons.get(i);
	        	//On effectue la somme pondérée.
	            newValue += previousNeuron.connections.get(indexInLayer) * previousNeuron.value;
	        }
	        
	        //On passe la somme pondérée à travers la fonction d'activation.
	        this.value = (squashingType == SquashingFunction.TANH) ? (float) Math.tanh(newValue) : newValue;
	    }
	    
	    /**
	     * Cette méthode permet d'obtenir la représentation textuelle comprenant les valeurs utiles
	     * à la création d'un neurone identique à celui-ci. Les données sont précédées du préfixe "N["
	     * et se terminent par le suffixe "]", pour pouvoir séparer les différents neurones dans la représentation
	     * textuelle des couches. Les connexions du neurone sont séparés par des tubes (le caractère "|").
	     * 
	     * @return
	     * La représentation textuelle du neurone.
	     * 
	     * @see Neuron
	     * @see Layer
	     */
	    String toStringRpz() {
	        String stringConnections = "";
	        for (Float synapsis: this.connections) { stringConnections += "|" + synapsis; }
	        return "N[" + stringConnections + "|]";
	    }
	    
	}
	
	/**
	 * Une couche possède un ensemble de neurones, dont chacun est relié à la couche suivante.
	 * Un complexe composé de ces couches forme alors un réseau de neurones. Une couche a également un
	 * type de fonction d'activation, commun à l'ensemble de ses neurones.
	 * 
	 * @author Ronan
	 *
	 * @see Neuron
	 * @see NeuralNetwork
	 *
	 */
	static class Layer {
		
		/**La liste de neurones de la couche.*/
	    List<Neuron> neurons = new ArrayList<Neuron>();
	    
	    /**Le type de fonction d'activation de la couche.*/
	    SquashingFunction squashingType;
	    
	    /**
	     * Ce constructeur permet d'obtenir une couche dont les neurones sont déterminés grâce
	     * à une représentation textuelle de la liste de neurones.
	     * Les représentations successives des neurones sont délimitables grâce à leur préfixe
	     * et à leur suffixe.
	     * 
	     * @param stringRepresentation
	     * La représentation textuelle de la liste des représentations des neurones.
	     * 
	     * @see Neuron
	     * @see Layer
	     * 
	     */
	    public Layer(EnhancedString stringRepresentation) throws IllegalArgumentException {
	    	//On restore le premier caractère au cas où il aurait été retiré.
	        EnhancedString normalizedStr = (stringRepresentation.firstChar() == 'L') ? stringRepresentation : new EnhancedString("L" + stringRepresentation.getValue());
	        
	        //On lit le type de fonction d'activation de la couche.
	        this.squashingType = SquashingFunction.fromInteger(Integer.parseInt(normalizedStr.substring('(', '|').getValue()));
	        
	        //Cette variable pourra être découpée au fur et à mesure de la lecture.
	        EnhancedString neuronsStr = normalizedStr;
	        
	      //Tant qu'on trouve le préfixe "N", on découpe la nouvelle chaîne.
	        while (neuronsStr.firstIndexOf('N') != -1) {
	            neuronsStr = neuronsStr.substringFrom(neuronsStr.firstIndexOf('N') + 1);
	            
	            //Si on trouve également le suffixe "]", on récupère la représentation
	            //du neurone, et on en fait un objet Neuron avant de l'ajouter à notre liste.
	            if (neuronsStr.firstIndexOf(']') != -1) {
	                this.neurons.add(new Neuron(neuronsStr.substringTo(neuronsStr.firstIndexOf(']'))));
	            }
	        }
	    }
	    
	    /**
	     * Ce constructeur permet d'obtenir une couche dont les neurones et le type
	     * de fonction d'activation sont donnés.
	     * 
	     * @param neurons
	     * La liste des neurones de la couche.
	     * 
	     * @param squashingType
	     * Le type de fonction d'activation de la couche.
	     * 
	     * @see Layer
	     * @see Neuron
	     * 
	     */
	    public Layer(List<Neuron> neurons, SquashingFunction squashingType) {
	        this.neurons = neurons;
	        this.squashingType = squashingType;
	    }
	    
	    /**
	     * Cette méthode permet d'obtenir la représentation textuelle comprenant les données utiles
	     * à la création d'une couche identique à celle-ci. La représentation est précédée du préfixe "L(|"
	     * et se termine par le suffixe "|)", pour pouvoir séparer les différentes couches dans la représentation
	     * textuelle des réseaux.
	     * 
	     * @return
	     * La représentation textuelle de la couche.
	     * 
	     * @see Neuron
	     * @see Layer
	     * @see NeuralNetwork
	     */
	    String toStringRpz() {
	        String neuronStr = "|";
	        for (Neuron neuron: this.neurons) { neuronStr += neuron.toStringRpz(); }
	        return "L(" + this.squashingType.toInt() + neuronStr + "|)";
	    }
	    
	}
	
	/**
	 * Un réseau de neurones à propagation avant possède un ensemble de couches, reliées entre elles.
	 * La première, identifiée comme la <I>couche d'entrée</I>, reçoit les valeurs à faire propager dans le réseau.
	 * Ce sont les entrées : dans <strong>Neural Racing</strong>, elles correspondent à 5 valeurs de distance et
	 * une relative à la vitesse.
	 * <p>
	 * La dernière est nommée <I>couche de sortie</I>. Après propagation dans le réseau, l'activation de ses neurones
	 * exprime la prédiction obtenue : cette dernière correspond au neurone le plus activé.
	 * <p>
	 * Les couches intermédiaires se nomment <I>couches cachées</I>, car il est difficile de suivre
	 * ce qu'il s'y produit. Elles font transiter l'information de la couche d'entrée à la couche de sortie.
	 * 
	 * @author Ronan
	 *
	 * @see Neuron
	 * @see Layer
	 *
	 */
	static class NeuralNetwork {
		
		/**La couche d'entrée du réseau.*/
	    Layer inputLayer;
	    
		/**La liste des couches cachées du réseau.*/
		List<Layer> hiddenLayers = new ArrayList<Layer>();
		
		/**La couche de sortie du réseau.*/
	    Layer outputLayer;
	    
	    /**
	     * Ce constructeur permet d'obtenir un réseau dont toutes les couches sont données.
	     * 
	     * @param hiddenLayers
	     * La liste des couches cachées du réseau.
	     * 
	     * @param outputLayer
	     * La couche de sortie du réseau.
	     * 
	     * @param inputLayer
	     * La couche d'entrée du réseau.
	     * 
	     * @see NeuralNetwork
	     * @see Layer
	     * 
	     */
	    NeuralNetwork(List<Layer> hiddenLayers, Layer outputLayer, Layer inputLayer) {
	        this.hiddenLayers = hiddenLayers;
	        this.outputLayer  = outputLayer;
	        this.inputLayer   = inputLayer;
	    }
	    
	    /**
	     * Ce constructeur permet d'obtenir un réseau dont les couches sont déterminées grâce
	     * à une représentation textuelle de la liste de couches.
	     * Les représentations successives des couches sont délimitables grâce à leur préfixe
	     * et à leur suffixe.
	     * 
	     * @param stringRepresentation
	     * La représentation textuelle de la liste des couches du réseau.
	     * 
	     * @see Layer
	     * @see NeuralNetwork
	     * 
	     */
	    NeuralNetwork(EnhancedString stringRepresentation) throws IllegalArgumentException {
	    	
	    	//La première représentation est celle de la couche d'entrée : on en fait un objet Layer.
	    	this.inputLayer = new Layer(stringRepresentation.substringTo(stringRepresentation.firstIndexOf('#') - 1));
	    	
	    	//Cette chaîne de caractères pourra être découpée au fur et à mesure de notre avancée dans la représentation.
	        EnhancedString hiddenLayersStr = stringRepresentation.substring(stringRepresentation.firstIndexOf('#'), stringRepresentation.lastIndexOf('#'));
	       
	        //Tant qu'on trouve le préfixe "L", on découpe la chaîne.
	        while (hiddenLayersStr.firstIndexOf('L') != -1) {
	            hiddenLayersStr = hiddenLayersStr.substringFrom(hiddenLayersStr.firstIndexOf('L') + 1);
	            //Si le suffixe ")" est présent, on récupère la représentation de la couche entre les deux
	            //puis on en fait un objet Layer qu'on ajoute à la liste de couches cachées.
	            if (hiddenLayersStr.firstIndexOf(')') != -1) {
	                this.hiddenLayers.add(new Layer(hiddenLayersStr.substringTo(hiddenLayersStr.firstIndexOf(')'))));
	            }
	        }
	        
	        //La dernière représentation est celle de la couche de sortie : on en fait un objet Layer.
	        this.outputLayer = new Layer(stringRepresentation.substringFrom(stringRepresentation.lastIndexOf('#') + 1));
	    }
	    
	    /**
	     * Cette méthode permet d'obtenir la représentation textuelle comprenant les données utiles
	     * à la création d'un réseau identique à celui-ci. Les couches cachées sont séparées des couches
	     * d'entrée et de sortie par le caractère "#".
	     * 
	     * @return
	     * La représentation textuelle du réseau.
	     * 
	     * @see Neuron
	     * @see Layer
	     * @see NeuralNetwork
	     */
	    String toStringRpz() {
	        String layerStr = "";
	        for (Layer layer: this.hiddenLayers) { layerStr += layer.toStringRpz(); }
	        return this.inputLayer.toStringRpz() + "#" + layerStr + "#" + this.outputLayer.toStringRpz();
	    }
	    
	    /**
		 * 
		 * Cette méthode permet de reconstituer un individu à partir des connexions du réseau.
		 * 
		 * @return L'individu.
		 * 
		 * @see BiologicalModel.Individual
		 * @see NeuralNetwork
		 *
		 */
	    BiologicalModel.Individual toIndividual() {
	    	
	    	//On créé un individu sans gène.
	    	BiologicalModel.Individual individual = new BiologicalModel.Individual();
	    	
	    	//On ajoute le poids de chacune des connexions de chacun des neurones de la couche d'entrée aux gènes de l'individu.
	    	for (Neuron neuron: this.inputLayer.neurons) {
	            for (Float weight: neuron.connections) {
	                individual.genes.add(weight);
	            }
	        }
	        
	    	//On ajoute le poids de chacune des connexions de chacun des neurones de chacune des couches cachées aux gèhes de l'individu.
	        for (Layer layer: this.hiddenLayers) {
	            for (Neuron neuron: layer.neurons) {
	                for (Float weight: neuron.connections) {
	                	individual.genes.add(weight);
	                }
	            }
	        }
	    	
	        //Les neurones de la couche de sortie n'ont pas de connexions, donc l'individu est déjà porteur
	        //de toute l'information nécessaire.
	        
	    	return individual;
	    }
	    
	    /**
	     * Cette méthode permet d'obtenir le choix du réseau dans le contexte
	     * des valeurs données en paramètres, à l'aide d'une propagation vers l'avant.
	     * 
	     * @param inputLayerValues
	     * Les valeurs à utiliser en entrée.
	     * 
	     * @return
	     * L'index du neurone le plus activé dans la couche de sortie.
	     * C'est le choix du réseau.
	     */
	    int predict(float [] inputLayerValues) {
	    	
	        int layerIndex  = -1;
	        int neuronIndex = -1;
	        
	        //On donne les valeurs initiales aux neurones de la couche d'entrée.
	        for (int i = 0; i < inputLayerValues.length; i++) {
	            this.inputLayer.neurons.get(i).value = inputLayerValues[i];
	        }
	        
	        //On met à jour les valeurs des neurones de chacune des couches cachées, dans l'ordre, vers l'avant.
	        for (Layer layer: this.hiddenLayers) {
	        	
	            layerIndex += 1;
	            neuronIndex = -1;
	            
	            for (Neuron neuron: layer.neurons) {
	                neuronIndex += 1;
	                //Si la couche cachée est en première position, la couche précédente est la couche d'entrée.
	                neuron.updateValue((layerIndex == 0) ? inputLayer : this.hiddenLayers.get(layerIndex-1), neuronIndex, layer.squashingType);
	            }
	        }
	        
	        neuronIndex = -1;
	        float firedOutputNeuronValue = -10f;
	        int   firedOutputNeuronIndex = -1;
	        
	        //On met à jour la valeur des neurones de la couche de sortie, et on en profite pour rechercher
	        //l'index du neurone le plus activé.
	        for (Neuron neuron: this.outputLayer.neurons) {
	        	
	            neuronIndex += 1;
	            //Si on a des couches cachées, la couche précédent la couche de sortie est la dernière couche cachée.
	            //Sinon, la couche précédent la couche de sortie est tout simplement la couche d'entrée.
	            neuron.updateValue((BrainStruct.numberOfHiddenLayers != 0 ? this.hiddenLayers.get(this.hiddenLayers.size()-1) : this.inputLayer), neuronIndex, SquashingFunction.NONE);
	            
	            if (neuron.value > firedOutputNeuronValue) {
	                firedOutputNeuronValue = neuron.value;
	                firedOutputNeuronIndex = neuronIndex;
	            }
	        }
	        
	        //On retourne cet index.
	        return firedOutputNeuronIndex;
	    }
	    
	}
	
}
