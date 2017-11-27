import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

public class WordGraph implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private HashMap<String,WordNode> words = new HashMap<String,WordNode>();
	
	public WordGraph(){
		
	}
	
	public WordNode getNode(String key){
		return words.get(key);
	}
	
	public void createSortedLists(){
		Set<String> keys = words.keySet();
		for(String key : keys){
			words.get(key).createSortedLists();
		}
	}
	
	public boolean hasNode(String key){
		return words.containsKey(key);
	}
	
	public void addNode(String key, String partOfSpeech){
		WordNode node = new WordNode(key, partOfSpeech);
		words.put(key,node);
	}
	
	public void increaseWeight(String previousWord, String word, int i) {
		WordNode firstNode = words.get(previousWord);
		firstNode.increaseWeight(word,i);
		
	}
	
//	public void increaseWeight(String firstWord, String secondWord){
//		// order matters
//		WordNode firstNode = words.get(firstWord);
//		firstNode.increaseWeight(secondWord);
//	}

	public class WordNode implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private String word;
		private String partOfSpeech;
		private HashMap<Integer,HashMap<String,Integer>> weights = new HashMap<Integer,HashMap<String,Integer>>();
		private HashMap<Integer, ArrayList<Tuple>> sortedWeights = new HashMap<Integer, ArrayList<Tuple>>();
		
		public WordNode(String word, String partOfSpeech){
			this.word = word;
			this.partOfSpeech = partOfSpeech;
		}
		
		public void createSortedLists(){
			Set<Integer> keys = weights.keySet();
			for(int key : keys){
				HashMap<String,Integer> w = weights.get(key);
				Set<Entry<String,Integer>> entrySet = w.entrySet();
				ArrayList<Entry<String,Integer>> unsortedList = new ArrayList<Entry<String,Integer>>(entrySet.size());
				for(Entry e : entrySet){
					unsortedList.add(e);
				}
				ArrayList<Entry<String,Integer>> sortedList = QuickSorter.quicksort(unsortedList,0,unsortedList.size() - 1);
				ArrayList<Tuple> sortedTuples = new ArrayList<Tuple>(sortedList.size());
				for(Entry<String,Integer> e : sortedList){
					sortedTuples.add(new Tuple(e.getKey(),e.getValue()));
				}
				sortedWeights.put(key, sortedTuples);
			}
		}
		
		public class Tuple implements Serializable{
			private String word;
			private int count;
			
			public Tuple(String word, int count){
				this.word = word;
				this.count = count;
			}
			
			public int getCount(){
				return count;
			}
			
			public String getWord(){
				return word;
			}
		}
		
		public String getWord(){
			return word;
		}
		
		public void increaseWeight(String toIncrease, int placeBeforeWord){
			if(weights.containsKey(placeBeforeWord)){
				int previousCount = 0;
				if(weights.get(placeBeforeWord).containsKey(toIncrease)){
					previousCount = weights.get(placeBeforeWord).get(toIncrease);
				}
				int count = previousCount + 1;
				weights.get(placeBeforeWord).put(toIncrease, count);
			}
			else{
				HashMap<String, Integer> newMap = new HashMap<String,Integer>();
				newMap.put(toIncrease, 1);
				weights.put(placeBeforeWord,newMap);
			}
		}
		
		public ArrayList<Tuple> getSortedList(int placement){
			return sortedWeights.get(placement);
		}
		
		public int getWeight(String word, int placement){
			if(weights.containsKey(placement)){
				if(weights.get(placement).containsKey(word)){
					return weights.get(placement).get(word);
				}
			}
			return 0;
		}
		
		public String getPOS(){
			return this.partOfSpeech;
		}
	}
	
}
