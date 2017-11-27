import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;

public class POSGraph implements Serializable{

	private static final long serialVersionUID = 1L;
	private HashMap<String,POSNode> partsOfSpeech = new HashMap<String,POSNode>();
	
	public POSNode getNode(String key){
		return partsOfSpeech.get(key);
	}
	
	public boolean hasNode(String key){
		return partsOfSpeech.containsKey(key);
	}
	
	public void createSortedLists(){
		Set<String> keys = partsOfSpeech.keySet();
		for(String key : keys){
			partsOfSpeech.get(key).createSortedLists();
		}
	}
	
	public void addNode(String partOfSpeech){
		POSNode node = new POSNode(partOfSpeech);
		partsOfSpeech.put(partOfSpeech, node);
	}
	
	public void increaseWeight(String previousPOS, String POS, int position){
		POSNode firstNode = partsOfSpeech.get(previousPOS);
		firstNode.increaseWeight(POS,position);
	}
	
	public class POSNode implements Serializable{
		
		private static final long serialVersionUID = 1L;
		private String partOfSpeech;
		private HashMap<Integer,HashMap<String,Integer>> weights = new HashMap<Integer,HashMap<String,Integer>>();
		private HashMap<Integer, ArrayList<Tuple>> sortedWeights = new HashMap<Integer, ArrayList<Tuple>>();
		
		public POSNode(String partOfSpeech){
			this.partOfSpeech = partOfSpeech;
		}
		
		public class Tuple implements Serializable{
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
			private String POS;
			private int count;
			
			public Tuple(String POS, int count){
				this.POS = POS;
				this.count = count;
			}
			
			public int getCount(){
				return count;
			}
			
			public String getPOS(){
				return POS;
			}
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
		
		public String getPOS(){
			return partOfSpeech;
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
			//System.out.println("here");
			if(weights.containsKey(placement)){
				if(weights.get(placement).containsKey(word)){
					return weights.get(placement).get(word);
				}
			}
			return 0;
		}
	}

}
