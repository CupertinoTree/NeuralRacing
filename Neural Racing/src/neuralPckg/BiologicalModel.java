package neuralPckg;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

/**
 * 
 * Cette classe contient tout le matériel nécessaire pour prendre en charge les
 * mécanismes de l'Algorithme Génétique.
 * 
 * @author Ronan
 * 
 * @see Individual
 * @see Population
 * @see Algorithm
 * 
 */
public class BiologicalModel {
	
	/**
	 * Cette méthode permet de générer un nombre aléatoire dans un intervalle donnée.
	 * 
	 * @param min
	 * La borne inférieure.
	 * @param max
	 * La borne supérieure.
	 * @return
	 * Le nombre aléatoire généré.
	 */
	public static float getRandomNumberInRange(int min, int max) {

		if (min >= max) {
			throw new IllegalArgumentException();
		}
		
		return (new Random()).nextFloat()*(max - min) + min;
	}
	
	/**
	 * 
	 * Cette classe est une représentation des individus utilisés dans l'Algorithme Génétique.
	 * Un individu possède des gènes, sous forme d'un tableau unidimensionnel de nombres à virgule flottante.
	 * Chacun de ces gène correspond à un des poids du réseau de neurones associé à l'individu.
	 * On peut donc transformer un individu en un réseau de neurones, et inversement.
	 * 
	 * @author Ronan
	 * 
	 * @see NeuralModel.NeuralNetwork
	 *
	 */
	static class Individual {
	    
		/**Le tableau unidimensionnel stockant les gènes de l'individu.*/
	    List<Float> genes = new ArrayList<Float>();
	    
	    /**Le score de l'individu.*/
	    int fitness = -1;
	    /**Le type de l'individu. Il est utile pour choisir la couleur de la voiture que l'individu conduira.*/
	    int type;
	    
	    /**
		 * 
		 * Ce constructeur génère un individu avec des gènes aléatoires.
		 * 
		 * @param type
		 * Le type de l'individu. Il est utile pour choisir la couleur de la voiture que l'individu conduira.
		 * 
		 * @see NeuralModel.NeuralNetwork
		 * @see Individual
		 *
		 */
	    public Individual(int type) {
	    	
	    	this.type = type;
	    	
	    	//Pour chacune des connexions de chacun des neurones de chacunes des couches du réseau qu'on voudra obtenir,
	    	//on ajoute un gène aléatoire à l'individu.
	    	
	    	//On connecte ainsi la couche d'entrée à la première couche cachée ;
	    	//ou à la couche de sortie si on ne souhaite pas d'intermédiaire.
	        for (int i = 0; i < NeuralModel.BrainStruct.numberOfInputNeurons; i++) {
	            if (NeuralModel.BrainStruct.numberOfHiddenLayers != 0) {
	                for (int j = 0; j < NeuralModel.BrainStruct.numberOfNeuronsPerHiddenLayer[0]; j++) {
	                    genes.add(getRandomNumberInRange(-1, 1));
	                }
	            } else {
	                for (int j = 0; j < NeuralModel.BrainStruct.numberOfOutputNeurons; j++) {
	                	genes.add(getRandomNumberInRange(-1, 1));
	                }
	            }
	        }
	        
	        //Chaque couche cachée est connectée à la suivante.
	        if (NeuralModel.BrainStruct.numberOfHiddenLayers != 0) {
	            for (int i = 0; i < NeuralModel.BrainStruct.numberOfHiddenLayers - 1; i++) {
	                for (int b = 0; b < NeuralModel.BrainStruct.numberOfNeuronsPerHiddenLayer[i]; b++) {
	                    for (int c = 0; c < NeuralModel.BrainStruct.numberOfNeuronsPerHiddenLayer[i+1]; c++) {
	                    	genes.add(getRandomNumberInRange(-1, 1));
	                    }
	                }
	            }
	        
	            //La dernière couche cachée sera connectée à la couche de sortie.
	            for (int i = 0; i < NeuralModel.BrainStruct.numberOfNeuronsPerHiddenLayer[NeuralModel.BrainStruct.numberOfHiddenLayers-1]; i++) {
	                for (int a = 0; a < NeuralModel.BrainStruct.numberOfOutputNeurons; a++) {
	                	genes.add(getRandomNumberInRange(-1, 1));
	                }
	            }
	        }
	        
	    }
	    
	    /**
		 * 
		 * Ce constructeur génère un individu sans aucun gène.
		 * 
		 * @see Individual
		 *
		 */
	    Individual() { this.type = 0; }
	    
	    /**
		 * 
		 * Cette méthode permet de reconstituer un réseau de neurones à partir des gènes de l'individu.
		 * 
		 * @return Le réseau de neurones.
		 * 
		 * @see NeuralModel.NeuralNetwork
		 * @see Individual
		 *
		 */
	    NeuralModel.NeuralNetwork toNeuralNet() {
	    	
	    	List<NeuralModel.Neuron> neurons = new ArrayList<NeuralModel.Neuron>();
	    	
	        int index = -1;
	        
	        //On crée les neurones de la couche d'entrée.
	        //On leur donne leur connections à la couche suivante,
	        //en se servant des gènes de l'individu.
	        for (int i = 0; i < NeuralModel.BrainStruct.numberOfInputNeurons; i++) {
	        	NeuralModel.Neuron neuron = new NeuralModel.Neuron(new ArrayList<Float>());
	        	//Si il y a des couches cachées, on le connecte à la première,
	        	//sinon, on le connecte à la couche de sortie.
	            if (NeuralModel.BrainStruct.numberOfHiddenLayers != 0) {
	                for (int b = 0; b < NeuralModel.BrainStruct.numberOfNeuronsPerHiddenLayer[0]; b++) {
	                    index += 1;
	                    neuron.connections.add(this.genes.get(index));
	                }
	            } else {
	                for (int b = 0; b < NeuralModel.BrainStruct.numberOfOutputNeurons; b++) {
	                    index += 1;
	                    neuron.connections.add(this.genes.get(index));
	                }
	            }
	            neurons.add(neuron);
	        }
	        
	        //On crée la couche d'entrée, avec les neurones précédents.
	        NeuralModel.Layer inputLayer = new NeuralModel.Layer(neurons, NeuralModel.SquashingFunction.NONE);
	        
	        //On applique la même logique pour reconstituer le réseau entier.
	        
	        List<NeuralModel.Layer> hiddenLayers = new ArrayList<NeuralModel.Layer>();
	        
	        for (int layerIndex = 0; layerIndex < NeuralModel.BrainStruct.numberOfHiddenLayers; layerIndex++) {
	            neurons = new ArrayList<NeuralModel.Neuron>();
	            for (int b = 1; b <= NeuralModel.BrainStruct.numberOfNeuronsPerHiddenLayer[layerIndex]; b++) {
	            	NeuralModel.Neuron neuron = new NeuralModel.Neuron(new ArrayList<Float>());;
	                if (layerIndex == NeuralModel.BrainStruct.numberOfNeuronsPerHiddenLayer.length - 1) {
	                    for (int c = 0; c < NeuralModel.BrainStruct.numberOfOutputNeurons; c++) {
	                        index += 1;
	                        neuron.connections.add(this.genes.get(index));
	                    }
	                } else {
	                    for (int c = 0; c < NeuralModel.BrainStruct.numberOfNeuronsPerHiddenLayer[layerIndex + 1]; c++) {
	                        index += 1;
	                        neuron.connections.add(this.genes.get(index));
	                    }
	                }
	                neurons.add(neuron);
	            }
	            hiddenLayers.add(new NeuralModel.Layer(neurons, NeuralModel.SquashingFunction.TANH));
	        }
	        
	        neurons = new ArrayList<NeuralModel.Neuron>();
	        
	        for (int i = 0; i < NeuralModel.BrainStruct.numberOfOutputNeurons; i++) { neurons.add(new NeuralModel.Neuron()); }
	        
	        NeuralModel.Layer outputLayer = new NeuralModel.Layer(neurons, NeuralModel.SquashingFunction.NONE);
	        
	        return new NeuralModel.NeuralNetwork(hiddenLayers, outputLayer, inputLayer);
	    }
	    
	}
	
	/**
	 * 
	 * Cette classe est une représentation des populations utilisées dans l'Algorithme Génétique.
	 * Une population possède une liste d'individus, dont elle peut retrouver celui qui a le meilleur score, et le score en question.
	 * Elle permet également de retrouver des statistiques concernant la répartition des scores parmi les individus.
	 * 
	 * @author Ronan
	 * 
	 * @see Individual
	 *
	 */
	static class Population {
	    
		/**Le tableau stockant les individus de la population.*/
	    Individual [] individuals;
	    int average = 0, type;
	    
	    Population() {}
	    
	    /**
	     * Ce constructeur permet de créer une population de taille et de type donnés.
	     * 
	     * @param size
	     * La taille de la population.
	     * @param type
	     * Le type de la population
	     * 
	     * @see Individual
	     * 
	     * */
	    Population(int size, int type) {
	    	this.type = type;
	    	individuals = new Individual[size];
	    	for (int i = 0; i < size; i++) { this.individuals[i] = new Individual(type); }
	    }
	    
	    /**
	     * 
	     * Cette méthode retourne l'individu possèdant le meilleur score dans la population.
	     * 
	     * @return
	     * L'individu possèdant le meilleur score.
	     * 
	     * @see Individual
	     */
	    Individual getFittest() {
	        Individual fittest = this.individuals[0];
	        
	        for (Individual individual: this.individuals) {
	            if (individual.fitness > fittest.fitness) {
	                fittest = individual;
	            }
	        }
	        
	        return fittest;
	    }
	    
	    /**
	     * 
	     * Cette méthode retourne le meilleur score dans la population.
	     * 
	     * @return
	     * Le meilleur score.
	     * 
	     * @see Individual
	     */
	    int getBestFitness() {
	        return this.getFittest().fitness;
	    }
	    
	    /**
	     * 
	     * Cette méthode retourne un dictionnaire comportant le pourcentage de la population en fonction du score,
	     * pour être affiché dans une <code>UIGraphView</code>.
	     * 
	     * @return
	     * Les données de répartition du score.
	     * 
	     * @see Individual
	     * @see NeuralRacing.UIGraphView
	     */
	    Map<Integer, Double> getHistogramData() {
	    	//On utilise une TreeMap pour conserver l'ordre des termes.
	    	Map<Integer, Double> data = new TreeMap<Integer, Double>();
	    	
	    	//On stocke d'abord l'effectif en fonction du score.
	    	for (Individual individual: this.individuals) {
	    		if (data.containsKey(individual.fitness)) {
	    			data.put(individual.fitness, data.get(individual.fitness)+1);
	    		} else {
	    			data.put(individual.fitness, 1d);
	    		}
	    	}
	    	
	    	//Puis on en fait un pourcentage.
	    	data.replaceAll((key, oldValue) -> 100d*oldValue/individuals.length);
	    	
	    	return data;
	    }
	    
	    /**La taille de la population*/
	    int getSize() { return this.individuals.length; }
	    
	}

	/**
	 * 
	 * Cette classe contient le matériel nécessaire pour prendre en charge les
	 * mécanismes biologiques de l'Algorithme Génétique.
	 * 
	 * @author Ronan
	 * 
	 * @see Individual
	 * @see Population
	 * 
	 */
	static class Algorithm {
	    
		/**
		 * Ce nombre représente la taille des échantillons dans lesquels on récupère
		 * les individus à reproduire ensemble, afin d'imiter un brassage social.
		 */
	    public static int tournamentSize = 50;
	    
	    /**
	     * Ce nombre représente le pourcentage de mutation à appliquer aux gènes
	     * d'un individu.
	     */
	    public static float mutationRate = 5f;
	    
	    /**
	     * Ce booléen indique s'il est nécessaire d'intégrer le champion de la population
	     * précédente dans la génération suivante.
	     */
	    public static boolean keepBest   = true;
	    
	    /**
	     * 
	     * Cette méthode retourne une nouvelle <code>Population</code> basée sur la génération précédente,
	     * en se servant des principes de l'Algorithme Génétique.
	     * 
	     * @param population
	     * La population précédente.
	     * 
	     * @return
	     * La nouvelle population.
	     * 
	     * @see Population
	     */
	    public static Population evolvePopulation(Population population) {
	    	
	        Population newPopulation = new Population();
	        newPopulation.individuals = new Individual [population.individuals.length];
	        
	        //On stocke la valeur du booléen keepBest pour
	        //éviter que sa valeur ne change durant son utilisation.
	        boolean elitism = keepBest;
	        
	        if (elitism) { newPopulation.individuals[0] = population.getFittest(); }
	        
	        for (int i = elitism ? 1 : 0; i < population.individuals.length; i++) {
	            Individual indiv1 = tournamentSelection(population);
	            Individual indiv2 = tournamentSelection(population);
	            Individual indiv = reproduce(indiv1, indiv2);
	            indiv.type = population.type;
	            newPopulation.individuals[i] = mutate(indiv);
	        }
	        
	        newPopulation.type = population.type;
	        return newPopulation;
	    }
	    
	    /**
	     * 
	     * Cette méthode retourne le meilleur <code>Individu</code> d'une sous-population,
	     * qui est elle-même un extrait aléatoire de la population d'origine, pour imiter le brassage social.
	     * 
	     * @param population
	     * La population d'origine.
	     * 
	     * @return
	     * L'individu.
	     * 
	     * @see Population
	     * @see Individual
	     */
	    private static Individual tournamentSelection(Population population) {
	        
	    	//On stocke d'abord la taille de l'échantillon,
	    	//pour éviter que l'utilisateur ne la modifie alors qu'elle est utilisée.
	    	int tournamentCount = tournamentSize;
	    	
	        Population tournament = new Population(tournamentCount, population.type);
	        
	        for (int i = 0; i < tournamentCount; i++) {
	            double randomId = getRandomNumberInRange(0, population.individuals.length-1);
	            tournament.individuals[i] = population.individuals[(int) randomId];
	        }
	        
	        return tournament.getFittest();
	    }
	    
	    /**
	     * 
	     * Cette méthode retourne un individu dont les gènes sont un mélange aléatoire entre deux individus,
	     * pour imiter les différents brassages génétiques prenant place dans la reproduction sexuée.
	     * 
	     * @param individual1
	     * Le premier parent.
	     * 
	     * @param individual2
	     * Le deuxième parent.
	     * 
	     * @return
	     * Le nouvel individu.
	     * 
	     * @see Individual
	     */
	    public static Individual reproduce(Individual individual1, Individual individual2) {
	        
	        Individual newIndividual = new Individual();
	        
	        for (int geneIndex = 0; geneIndex < individual1.genes.size(); geneIndex++) {
	            if (Math.random() > 0.5) {
	                newIndividual.genes.add(individual1.genes.get(geneIndex));
	            } else {
	            	newIndividual.genes.add(individual2.genes.get(geneIndex));
	            }
	        }
	        
	        return newIndividual;
	    }
	    
	    /**
	     * 
	     * Cette méthode retourne un individu dont les gènes ont été mutés,
	     * en accord avec le taux de mutation choisi.
	     * 
	     * @param individual
	     * L'individu à muter.
	     * 
	     * @return
	     * Le nouvel individu.
	     * 
	     * @see Individual
	     */
	    public static Individual mutate(Individual individual) {
	        
	        Individual indiv = individual;
	        
			for (int i = 0; i < indiv.genes.size(); i++) {
				if (getRandomNumberInRange(0, 100) < mutationRate) {
	        		indiv.genes.set(i, getRandomNumberInRange(-1, 1));
	        	}
			}
	        
	        return indiv;
	    }
	    
	}
	
}
