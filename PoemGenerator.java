import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.util.ArrayList;

public class PoemGenerator {
	/**
	 * This class generates a poem, returns a score based on a grammar check,
	 * adjusts the weights of the features, and creates a new (hopefully better)
	 * poem for a set number of iterations
	 */
	
	// the graph of words
	private WordGraph dictionary;
	
	// the graph of parts of speech
	private POSGraph posGraph;
	
	// number of words to look at when choosing the next word
	private int NUM_FEATURES = 5;
	
	//weights should always add up to 1
	private ArrayList<Double> weights = new ArrayList<Double>(NUM_FEATURES);
	
	// how quickly to adjust the weights
	private double stepSize = .3;
	
	public final File WORD_GRAPH_FILE = new File("C:\\Users\\Bri\\Documents\\new_dictionary.txt");
	public final File POS_GRAPH_FILE = new File("C:\\Users\\Bri\\Documents\\pos_graph.txt");
	
	String poem = "";
	
	public PoemGenerator(){
		
		//initialize each weight to be 1/number of weights
		for(int i = 0; i < NUM_FEATURES; i ++){
			weights.add(1/(double)NUM_FEATURES);
		}
		
	}
	
	public class RotatingArray{
		/**
		 * This data structure contains an array of x number of previous words in the poem,
		 * where x is the number of features we are looking at. When a new word is added to the poem,
		 * it adds this new word to the array and if the array is at capacity, it deletes the oldest 
		 * word kept in the array.
		 */
		private String[] words = new String[NUM_FEATURES];
		
		/* the index of the "oldest" word*/
		private int startIndex;
		
		/*Number of words currently in this object*/
		private int size;
		
		public RotatingArray(){
			startIndex = 0;
			size = 0;
		}
		
		/* add a new word to the array - gets rid of an old word if necessary to fit*/
		public void add(String word){
			if(size < words.length){
				words[size] = word;
				size ++;
			}
			else{
				/* replace the oldest word with this word*/
				words[startIndex] = word;
				startIndex = (startIndex + 1)%size;
				
			}
		}
		
		/* get a word given an index, where 0 is the oldest word*/
		public String get(int index){
			return words[(startIndex + index)%size];
		}
		
		/* returns # of word currently in the array */
		public int getSize(){
			return size;
		}
		
	}

	public static void main(String[] args) throws ClassNotFoundException, IOException {
		
		PoemGenerator nn = new PoemGenerator();
		
		// load in the wordGraph POSGraph
		nn.readDictionary();
		nn.readPOSGraph();
		
		//the score given to the previous poem
		double previousGrade = -1;
		
		// the index of the weight we are changing
		int toChange = 0;
		
		//number of poems to generate
		int timesToRun = 50;
		
		for(int i = 0; i < timesToRun; i ++){
			RotatingArray featureArray = nn.new RotatingArray();
			
			// Generate the first word in this poem from the list of words that an ED has 
			// started with
			String firstWord = nn.getSuggestedWord("start node",1,"ANY");
			nn.poem = firstWord;
			featureArray.add(firstWord);
			
			// writes a new poem
			// 62 is the average number of words in an ED poem
			nn.writeNextWord(featureArray,62);
			
			System.out.println(nn.poem);
			//returns a grade for the poem based on grammatical correctness
			double grade = Grammar.evaluate(nn.poem);
			System.out.println(grade);
			
			if(previousGrade < 0 || previousGrade < grade){
				// if the grade of this poem is greater than the grade of the previous poem
				// we should keep adjusting this weight
				nn.adjust(toChange);
			}
			else{
				// change the weight we are adjusting
				// just goes in a circle right now
				toChange = (toChange + 1)%nn.NUM_FEATURES;
				nn.adjust(toChange);
			}
			previousGrade = grade;
		}
		for(int i = 0; i < nn.weights.size(); i ++){
			System.out.println("weight " + i);
			System.out.print(" " + nn.weights.get(i));
		}
	}
	
	/* increase the weight at this index by the step size*/
	public void adjust(int toChange){
		double currentValue = weights.get(toChange);
		currentValue = currentValue + stepSize;
		weights.set(toChange, currentValue);
		normalize();
		
	}
	
	/* makes sure all weights add up to 1 */
	public void normalize(){
		double sum = 0;
		for(double weight : weights){
			sum = sum + weight;
		}
	}
	
	/**
	 * 
	 * @param word the current word we are looking at from the feature array
	 * @param index the # of places the new word is after this word
	 * @param targetPOS the type of speech the new word should be
	 * @return the suggested word
	 */
	public String getSuggestedWord(String word, int index, String targetPOS){
		ArrayList<WordGraph.WordNode.Tuple> potentialWords = dictionary.getNode(word).getSortedList(index);
		if(potentialWords == null){
			return null;
		}
		int totalNumWords = 0;
		for(WordGraph.WordNode.Tuple tuple : potentialWords){
			// add the count of each individual word to the total count
			totalNumWords = totalNumWords + tuple.getCount();
		}
		// allows randomness in a hopefully normal distribution
		double percentile = Math.random();
		int desiredIndex = (int) (percentile*totalNumWords);
		int count = 0;
		String backUpWord = null;
		for(int i = 0; i < potentialWords.size(); i ++){
			String potentialWord = potentialWords.get(i).getWord();
			String POS = dictionary.getNode(potentialWord).getPOS();
			count = count + potentialWords.get(i).getCount();
			if(count >= desiredIndex){
				// return the first word in the last with the target POS once the count is greater
				// than the desired index
				if(POS.equals(targetPOS) || targetPOS.equals("ANY")){
					return potentialWords.get(i).getWord();
				}
			}
			else{
				if(POS.equals(targetPOS)){
					// in case there are no words after the desiredIndex that have the correct POS,
					// we want a backup word
					backUpWord = potentialWords.get(i).getWord();
				}
			}
		}
		if(backUpWord != null){
			return backUpWord;
		}
		else{
			return potentialWords.get(0).getWord();
		}
	}
	
	/**
	 * @param POS - a string representing the current POS
	 * @param index - the # of spaces before the next word this POS occurs
	 * @return a string representing this word's suggestion for the next word's POS
	 */
	public String getSuggestedPOS(String POS, int index){
		// each previous POS in the features suggests a POS for the next word in the poem
		
		//ordered list of all of the parts of speech that ever come after this part of speech
		ArrayList<POSGraph.POSNode.Tuple> potentialPOS = posGraph.getNode(POS).getSortedList(index);
		if(potentialPOS == null){
			return null;
		}
		int numWords = potentialPOS.size();
		double percentile = Math.random();
		// we tally up the counts of every POS in the list, starting with the most popular POS
		// when our tally is above or equal to the desired index, we choose that POS
		int desiredIndex = (int) (percentile*numWords);
		int count = 0;
		for(int i = 0; i < numWords; i ++){
			count = count + potentialPOS.get(i).getCount();
			if(count >= desiredIndex){
				return potentialPOS.get(i).getPOS();
			}
		}
		return potentialPOS.get(0).getPOS();
	}
	
	/* Decides what part of speech the next word in the poem should be*/
	public String getNextPOS(RotatingArray input){
		String[] suggestions = new String[input.getSize()];
		int currentIndex = 0;
		for(int i = 0; i < input.getSize(); i ++){
			// the part of speech of each word in the features array "suggests" 
			// the POS that most often comes x spaces after it
			String pos = dictionary.getNode(input.get(i)).getPOS();
			POSGraph.POSNode node = posGraph.getNode(pos);
			String suggestedPOS = null;
			// the list of all POS that ever come x spaces after this POS
			ArrayList<POSGraph.POSNode.Tuple> potentialPOS = node.getSortedList(input.getSize() - i);
			if(potentialPOS != null){
				suggestedPOS = getSuggestedPOS(pos,input.getSize() - i);
				if(suggestedPOS != null){
					boolean match = false;
					for(int j = 0; j < suggestions.length; j ++){
						if(suggestions[j] == suggestedPOS){
							match = true;
						}
					}
					if(!match){
						// if the suggested POS doesn't match any already in the array, add it
						suggestions[currentIndex] = suggestedPOS;
						currentIndex ++;
					}
				}
			}
		}
		
		double[] votes = new double[suggestions.length];
		int index = 0;
		for(String POS : suggestions){
			// for every word in the features list - how often does a suggested word come i times after it?
			votes[index] = 1;
			for(int i = 0; i < suggestions.length; i ++){
				POSGraph.POSNode node = posGraph.getNode(dictionary.getNode(input.get(i)).getPOS());
				// multiplication means that if a POS never appears in the nth place after word n, its score
				// returns to 0 - attempt to enforce grammar
				double count = node.getWeight(POS, input.getSize() - i)*weights.get(i);
				votes[index] = votes[index]*count;
			}
			index ++;
		}
		
		// the most popular part of speech wins
		double winner = 0;
		String POS = suggestions[0];
		for(int i = 0; i < suggestions.length; i ++){
			if(votes[i] > winner){
				winner = votes[i];
				POS = suggestions[i];
			}
		}
		return POS;
	}
	
	/* recursive function - generate the next word in the poem
	 * input - the last x words in the poem, where x is the number of features
	 * countDown - the number of words left to write in the poem */
	public void writeNextWord(RotatingArray input, int countDown){
		
		// decide what the next part of speech should be
		String POS = getNextPOS(input);
		
		// Each word in the input area will suggest a next word
		String[] suggestedWords = new String[input.getSize()];
		int currentIndex = 0;
		
		// iterate through each previous word
		for(int i = 0; i < input.getSize(); i ++){
			String suggestedWord = getSuggestedWord(input.get(i),input.getSize() - i,POS);
			if(suggestedWord != null){
				boolean match = false;
				for(int j = 0; j < suggestedWords.length; j ++){
					// if the suggested word already exists in the array, don't
					// put it in again
					if(suggestedWords[j] == suggestedWord){
						match = true;
					}
				}
				if(!match){
					// if the suggested word doesn't already exist in the array,
					// put it in
					suggestedWords[currentIndex] = suggestedWord;
					currentIndex ++;
				}
			}
			else{
				System.out.println("shoot");
			}
		}
		
		// the strength of the suggested words
		double[] preference = new double[suggestedWords.length];
		
		int index = 0;
		for(String word : suggestedWords){
			// for each word in the input array, find how often each suggested word follows it by x places
			for(int i = 0; i < input.getSize(); i ++){
				int count = dictionary.getNode(input.get(i)).getWeight(word, input.getSize() - index);
				preference[i] = preference[i] + count*weights.get(index);
			}
			index ++;
		}

		String winner = suggestedWords[0];
		double winningTally = preference[0];
		for(int i = 1; i < currentIndex; i ++){
			// find the word with the strongest weight
			if(preference[i] > winningTally){
				winner = suggestedWords[i];
				winningTally = preference[i];
			}
		}
		input.add(winner);
		if(winner.equals("new line")){
			poem = poem + "\n";
		}
		else{
			poem = poem + " " + winner;
		}
		countDown = countDown - 1;
		if(countDown == 0){
			return;
		}
		else{
			writeNextWord(input,countDown);
		}
	}
	
	/* Read in the word graph */
	public WordGraph readDictionary() throws IOException, ClassNotFoundException{
		FileInputStream fis = new FileInputStream(WORD_GRAPH_FILE);
		ObjectInputStream input = new ObjectInputStream(fis);
		this.dictionary = (WordGraph) input.readObject();
		fis.close();
		input.close();
		return dictionary;
	}
	
	/* Read in the part of speech graph */
	public POSGraph readPOSGraph() throws IOException, ClassNotFoundException{
		FileInputStream fis = new FileInputStream(POS_GRAPH_FILE);
		ObjectInputStream input = new ObjectInputStream(fis);
		this.posGraph = (POSGraph) input.readObject();
		fis.close();
		input.close();
		return posGraph;
	}

}
