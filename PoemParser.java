import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.util.CoreMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.*;

public class PoemParser {
	/**
	 * This class iterates through each file (each file should be a poem) in the given directory
	 * and creates a WordGraph and POSGraph from them and writes each to a file.
	 */
	public final File WORD_GRAPH_FILE = new File("C:\\Users\\Bri\\Documents\\new_dictionary.txt");
	public final File POS_GRAPH_FILE = new File("C:\\Users\\Bri\\Documents\\pos_graph.txt");
	public final File POEM_DIRECTORY = new File("C:\\Users\\Bri\\Documents\\emily_d_poems\\");
	
	public static void main(String[] args) throws IOException {
		PoemParser parser = new PoemParser();
		parser.makeWordGraphAndPOSGraph();
	}
	
	public void makeWordGraphAndPOSGraph() throws IOException{
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
		
		// split each document at the new line symbol - each line is a sentence
		props.setProperty("ssplit.eolonly", "true");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		
		WordGraph dictionary = new WordGraph();
		POSGraph posGraph = new POSGraph();
		File[] fileList;
		fileList = POEM_DIRECTORY.listFiles();
		
		for(File file : fileList){
			System.out.println(file.getName());
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = reader.readLine();
			String poem = "";
			int numWords = 0;
			
			while(line !=null){
				poem = poem + line + " \n";
				String[] words = line.split("\\s+");
				// each new line counts as a word; hence the + 1
				numWords = numWords + words.length + 1;
				line = reader.readLine();
			}
			
			Annotation document = new Annotation(poem);
			pipeline.annotate(document);
			List<CoreMap> sentences = document.get(SentencesAnnotation.class);
			ArrayList<String> previousWords = new ArrayList<String>(numWords);
			ArrayList<String> previousPOS = new ArrayList<String>(numWords);
			int indexCurrentWord = 0;
			
			
			// every new line is a sentence
			
			for(CoreMap sentence: sentences){

				// a new line is its own node in both graphs
				String NL = "new line";
				if(!dictionary.hasNode(NL)){
					dictionary.addNode(NL, NL);
				}
				if(!posGraph.hasNode(NL)){
					posGraph.addNode(NL);
				}

				for(int i = 0; i < indexCurrentWord; i ++){
					// for every previous word in the poem, add an edge to this word (the new line)
					
					String previousWord = previousWords.get(i);
					dictionary.increaseWeight(previousWord, NL, indexCurrentWord - i);
					String lastPOS = previousPOS.get(i);
					posGraph.increaseWeight(lastPOS, NL, indexCurrentWord - i);
				}
				previousWords.add(NL);
				previousPOS.add(NL);
				indexCurrentWord ++;
				
				for(CoreLabel token: sentence.get(TokensAnnotation.class)){
					// traverse each word in the sentence
					String word = token.get(TextAnnotation.class);
					String pos = token.get(PartOfSpeechAnnotation.class);
					
					if(!dictionary.hasNode(word)){
						// add word to the dictionary
						dictionary.addNode(word, pos);
					}
					if(!posGraph.hasNode(pos)){
						// add pos of this word to the posgraph
						posGraph.addNode(pos);
					}
					
					for(int i = 0; i < indexCurrentWord; i ++){
						// for every previous word in the poem, add an edge to this word
						String previousWord = previousWords.get(i);
						dictionary.increaseWeight(previousWord, word, indexCurrentWord - i);
						String lastPOS = previousPOS.get(i);
						posGraph.increaseWeight(lastPOS, pos, indexCurrentWord - i);
					}
					
					previousWords.add(word);
					previousPOS.add(pos);
					indexCurrentWord = indexCurrentWord + 1;
					
				}
			}
			
			if(previousWords.size() > 0){
				// A start node to help us keep track of what words a poem tends to start with
				if(!dictionary.hasNode("start node")){
					dictionary.addNode("start node", "start");
				}
				dictionary.increaseWeight("start node", previousWords.get(1), 1);
				reader.close();
			}
		}
		
		// Write the word graph to a file
		WORD_GRAPH_FILE.createNewFile();
		dictionary.createSortedLists();
		FileOutputStream fos = new FileOutputStream(WORD_GRAPH_FILE);
		ObjectOutputStream output = new ObjectOutputStream(fos);
		output.writeObject(dictionary);
		output.close();
		
		// Write the part of speech graph to a file		
		POS_GRAPH_FILE.createNewFile();
		posGraph.createSortedLists();
		FileOutputStream fos2 = new FileOutputStream(POS_GRAPH_FILE);
		ObjectOutputStream output2 = new ObjectOutputStream(fos2);
		output2.writeObject(posGraph);
		output2.close();
	}
}
