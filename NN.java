import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.util.ArrayList;

public class NN {
	
	// the graph of words
	private WordGraph dictionary;
	
	// the graph of parts of speech
	private POSGraph posGraph;
	
	// number of words to look at when choosing the next word
	private int NUM_FEATURES = 1;
	
	//weights should always add up to 1
	private ArrayList<Double> weights = new ArrayList<Double>(NUM_FEATURES);
	
	// how quickly to adjust the weights
	private double stepSize = .3;
	
	String poem = "";
	
	public NN(){
		
		//initialize weights to be equal 1/number of weights
		for(int i = 0; i < NUM_FEATURES; i ++){
			weights.add(1/(double)NUM_FEATURES);
		}
		
	}
	
	public class RotatingArray{
		// a data structure to keep the list of previous words
		private String[] words = new String[NUM_FEATURES];
		
		/* the index of the "oldest" word*/
		private int startIndex;
		
		/*Number of words currently in this object*/
		private int size;
		
		public RotatingArray(){
			startIndex = 0;
			size = 0;
		}
		
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
		
		public String get(int index){
			return words[(startIndex + index)%size];
		}
		
		public int getSize(){
			//return # of strings currently in the data structure
			return size;
		}
		
	}

	public static void main(String[] args) throws ClassNotFoundException, IOException {
		
		NN nn = new NN();
		nn.readDictionary();
		nn.readPOSGraph();
		
		//the score given to the previous poem
		int lastGrade = -1;
		
		// what weight we are changing
		int toChange = 0;
		
		//number of poems to generate
		int timesToRun = 70;
		
		for(int i = 0; i < timesToRun; i ++){
			RotatingArray a = nn.new RotatingArray();
			
			// right now the first word in the poem is always the - we should randomize this
			a.add("the");
			nn.poem = "the";
			nn.run(a,40);
			System.out.println(nn.poem);
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			System.out.print("Grade the poem from 0-5");
			
			String response = reader.readLine();
			System.out.println("Your grade was " + response);
			int grade = Integer.parseInt(response);
			System.out.println(toChange);
			if(lastGrade < grade){
				//good, keep doing what we are doing
				nn.adjust(toChange);
			}
			else{
				//change the weight we are adjusting
				//probably should just go in a circle for now
				toChange = (toChange + 1)%nn.NUM_FEATURES;
				nn.adjust(toChange);
			}
			lastGrade = grade;
		}
	}
	
	public void adjust(int toChange){
		double currentValue = weights.get(toChange);
		currentValue = currentValue + stepSize;
		weights.set(toChange, currentValue);
		normalize();
		
	}
	
	public void normalize(){
		double sum = 0;
		for(double weight : weights){
			sum = sum + weight;
		}
		for(int i = 0; i < weights.size(); i ++){
//			weights.set(i, weights.get(i)/sum);
//			System.out.print("index");
//			System.out.println(i);
//			System.out.print("weight");
//			System.out.println(weights.get(i));
		}
	}
	
	public String getSuggestedWord(String word, int index, String targetPOS){
		ArrayList<WordGraph.WordNode.Tuple> potentialWords = dictionary.getNode(word).getSortedList(index);
		if(potentialWords == null){
			return null;
		}
		int numWords = potentialWords.size();
		double percentile = Math.random();
		int desiredIndex = (int) (percentile*numWords);
		int count = 0;
		for(int i = 0; i < numWords; i ++){
			String potentialWord = potentialWords.get(i).getWord();
			String POS = dictionary.getNode(potentialWord).getPOS();
			count = count + potentialWords.get(i).getCount();
			if(count >= desiredIndex && POS.equals(targetPOS)){
//				System.out.println(potentialWords.get(i).getWord());
//				System.out.println(count);
				return potentialWords.get(i).getWord();
			}
		}
		return potentialWords.get(0).getWord();
	}
	
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
	
	public String getPOS(RotatingArray input){
		String[] suggestions = new String[input.getSize()];
		int currentIndex = 0;
		for(int i = 0; i < input.getSize(); i ++){
			// each word in the features array "suggests" the word that most often comes i spaces after it
			WordGraph.WordNode node1 = dictionary.getNode(input.get(i));
			String pos = dictionary.getNode(input.get(i)).getPOS();
			POSGraph.POSNode node = posGraph.getNode(pos);
			String suggestedPOS = null;
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
	
	public void run(RotatingArray input, int countDown){
		
		String POS = getPOS(input);
		
		//add randomness
		String[] suggestedWords = new String[input.getSize()];
		int currentIndex = 0;
		for(int i = 0; i < input.getSize(); i ++){
			String suggestedWord = getSuggestedWord(input.get(i),input.getSize() - i,POS);
			if(suggestedWord != null){
				boolean match = false;
				for(int j = 0; j < suggestedWords.length; j ++){
					if(suggestedWords[j] == suggestedWord){
						match = true;
					}
				}
				if(!match){
					suggestedWords[currentIndex] = suggestedWord;
					currentIndex ++;
				}
			}
		}
		double[] preference = new double[suggestedWords.length];
		
		int index = 0;
		for(String word : suggestedWords){
			preference[index] = 1;
			for(int i = 0; i < input.getSize(); i ++){
				int count = dictionary.getNode(input.get(i)).getWeight(word, input.getSize() - i);
				preference[index] = preference[index]*count*weights.get(i);
			}
			index ++;
		}
		
		//for each word in here - find how often it is used after all other words in the array
		//not just the one that has it as its most common descendent
		String winner = suggestedWords[0];
		double winningTally = preference[0];
		for(int i = 1; i < suggestedWords.length; i ++){
//			System.out.println(suggestedWords[i]);
//			System.out.println(preference[i]);
			if(preference[i] > winningTally){
				winner = suggestedWords[i];
				winningTally = preference[i];
			}
		}
		//System.out.println(winner);
//		int i = (int) Math.random()*suggestedWords.length;
//		winner = suggestedWords[i];
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
			run(input,countDown);
		}
	}
	
	public WordGraph readDictionary() throws IOException, ClassNotFoundException{
		File dict = new File("C:\\Users\\Bri\\Documents\\new_dictionary.txt");
		FileInputStream fis = new FileInputStream(dict);
		ObjectInputStream input = new ObjectInputStream(fis);
		this.dictionary = (WordGraph) input.readObject();
		fis.close();
		input.close();
		return dictionary;
	}
	
	public POSGraph readPOSGraph() throws IOException, ClassNotFoundException{
		File dict = new File("C:\\Users\\Bri\\Documents\\pos_graph.txt");
		FileInputStream fis = new FileInputStream(dict);
		ObjectInputStream input = new ObjectInputStream(fis);
		this.posGraph = (POSGraph) input.readObject();
		fis.close();
		input.close();
		return posGraph;
	}

}
