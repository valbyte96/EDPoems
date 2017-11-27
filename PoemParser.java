import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.patterns.surface.Token;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;

public class PoemParser {

	public static void main(String[] args) throws IOException {
		makeDictionary();
	}
	
	public static void makeDictionary() throws IOException{
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
		props.setProperty("ssplit.eolonly", "true");
		File dictFile = new File("C:\\Users\\Bri\\Documents\\new_dictionary.txt");
		File posFile = new File("C:\\Users\\Bri\\Documents\\pos_graph.txt");
		dictFile.createNewFile();
		posFile.createNewFile();
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		WordGraph dictionary = new WordGraph();
		POSGraph posGraph = new POSGraph();
		File[] fileList;
		File dir = new File("C:\\Users\\Bri\\Documents\\emily_d_poems\\");
		fileList = dir.listFiles();
		int numFiles = 0;
		for(File file : fileList){
			numFiles ++;
			if(numFiles > 200){
				break;
			}
			System.out.println(file.getName());
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = reader.readLine(); 
			String poem = "";
			int numWords = 0;
			while(line !=null){
				poem = poem + line + " \n";
				String[] terms = line.split("\\s+");
				numWords = numWords + terms.length + 1;
				line = reader.readLine();
			}
			Annotation document = new Annotation(poem);
			pipeline.annotate(document);
			List<CoreMap> sentences = document.get(SentencesAnnotation.class);
			ArrayList<String> previousWords = new ArrayList<String>(numWords);
			ArrayList<String> previousPOS = new ArrayList<String>(numWords);
			int numWordsInList = 0;
			// take line breaks into account
			for(CoreMap sentence: sentences){
				String NL = "new line";
				if(!dictionary.hasNode(NL)){
					dictionary.addNode(NL, NL);
				}
				if(!posGraph.hasNode(NL)){
					posGraph.addNode(NL);
				}
				int c = 1;
				for(String previousWord : previousWords){
					dictionary.increaseWeight(previousWord,NL,1 + (numWordsInList - c));
					c = c + 1;
				}
				previousWords.add(NL);
				c = 1;
				for(String lastPOS : previousPOS){
					posGraph.increaseWeight(lastPOS, NL, 1 + (numWordsInList - c));
					c = c + 1;
				}
				previousPOS.add(NL);
				numWordsInList ++;
				
				for(CoreLabel token: sentence.get(TokensAnnotation.class)){
					// traverse words in sentence
					String word = token.get(TextAnnotation.class);
					String pos = token.get(PartOfSpeechAnnotation.class);
					if(!dictionary.hasNode(word)){
						dictionary.addNode(word, pos);
					}
					int count = 1;
					for(String previousWord : previousWords){
						dictionary.increaseWeight(previousWord,word,1 + (numWordsInList - count));
						count = count + 1;
					}
					previousWords.add(word);
					if(!posGraph.hasNode(pos)){
						posGraph.addNode(pos);
					}
					int posCount = 1;
					for(String lastPOS : previousPOS){
						posGraph.increaseWeight(lastPOS, pos, 1 + (numWordsInList - posCount));
						posCount = posCount + 1;
					}
					previousPOS.add(pos);
					numWordsInList = numWordsInList + 1;
					
				}
			}
			reader.close();
		}
		dictionary.createSortedLists();
		FileOutputStream fos = new FileOutputStream(dictFile);
		ObjectOutputStream output = new ObjectOutputStream(fos);
		output.writeObject(dictionary);
		output.close();
		
		posGraph.createSortedLists();
		FileOutputStream fos2 = new FileOutputStream(posFile);
		ObjectOutputStream output2 = new ObjectOutputStream(fos2);
		output2.writeObject(posGraph);
		output2.close();
	}
}
