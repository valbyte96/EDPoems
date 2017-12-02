/** Grammar.java
 * @author Ted McCulloch
 * @version 11/12/2017
 * This class contains the methods necessary to evaluating the success of the 
 * poems created via ML. It takes in the poem as an array of strings (subject to change).
 * It evaluates each line and returns a fitness so that the ML algorithms can adjust 
 * their parameters accordingly. 
 */
import java.io.*;
import java.util.*;

import edu.stanford.nlp.coref.CorefCoreAnnotations;

import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.io.*;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.util.*;
public class Grammar {
	/**global fields for properties*/
	public static Properties props = new Properties();
	public static StanfordCoreNLP pipeline;
	public static final double avgLength = 5; //Change; average length of ED Poem
	
	
	/**	
	 * @param poems: ML created poems as an array of Strings
	 * @return fitness: double value between 0 and 1
	 * [0] = absolutely unsuccessful; (0, 1) = partially successful; [1] = absolution successful
	 */
	public static double evaluate(String[] poems) {
		// init properties and pipeline
		props.setProperty("annotators", "tokenize, ssplit, pos");	
		pipeline = new StanfordCoreNLP(props);
		int num = 0; // init avg counter
		int denom = poems.length;
		
		
		
		// evaluate fitness
		double fit = 0;
		for(String line: poems) {
			fit += lineEval(line);
			num+=line.split(" ").length;
		}
		//return dupLines(poems,1); //for testing 
		return (fit/denom + lengthFit(num/denom)+dupLines(poems,1))/3; //return the average of all fitnesses 
	}
	
	/**
	 * @param line: line of poem as a String
	 * @return returns either 0 or 1;
	 * returns 0 if unsuccessful and 1 if successful
	 * Strategy is to look into the space where things fail, as
	 * that is hypothetically smaller than the space where things are legal.
	 * --> @TODO: proof of this concept? 
	 * REFERENCE:
	 * >POS meanings: https://cs.nyu.edu/grishman/jet/guide/PennPOS.html
	 */
	public static int lineEval(String line) {
		//if(repeat(line)) { //two or more parts of speech in a row
			//return 0;
		//}
		if(!validEnd(line)) { //ends with legal word
			return 0;
		}
		
		return 1;	
	}
	
	/**
	 * @param text: String to annotate 
	 * Get parts of speech for each word */
	private static CoreMap POS (String text) {
        // create an empty Annotation just with the given text
        Annotation document = new Annotation(text);
        // run all Annotators on this text
        pipeline.annotate(document);      
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);      
		return sentences.get(0);
	}
	
//******************************TRAINING METHODS**************************************
	/**
	 * @param poems, threshold: array of poems, threshold for how many repeats is too many
	 * catches repeated lines and penalizes score after a specified threshold.
	 * Uses HashMap so as not to double count lines.
	 */
	public static double dupLines(String[] poems, int threshold) {
		HashSet<String> R = new HashSet<String>();
		int repeats;
		int penalty = 0;
		for(int i=0; i<poems.length; i++) {
			repeats = 0;
			for(int j=0; j<poems.length; j++) {
				if(i!=j && poems[i].equals(poems[j]) && !R.contains(poems[i]) ) {
					
					repeats+=1;
				}
			}
			if (repeats>threshold) {
				R.add(poems[i]);
				penalty+=1;
			}
		}
		return (poems.length-penalty)/poems.length;
	}
	/**
	 * @TODO: create a fitness for the MODE rather than the mean
	 */
	
	
	/** 
	 * @param avg
	 * @return fitness: double between 0 and 1
	 * this evaluates the average length
	 */
	public static double lengthFit(double avg) {
		if(avg<avgLength) { 
			return avg/avgLength;
		}
		else {
			return avgLength/avg;
		}
	}
	
	/**
	 * @param line
	 * @return false if invalid; true if valid
	 * Detects if ending of line is valid
	 * Invalid endings
	 * >CC
	 * >Article --> abbreviation
	 * >anything else?
	 */
	public static boolean validEnd(String line) {
		String[] words = line.split(" ");
		String lastPOS = POS(words[words.length-1]).get(TokensAnnotation.class)
				.get(0).get(PartOfSpeechAnnotation.class);
		if(lastPOS.equals("MD")) {
			return false;
		}	
		return true;
	}
	
	/**
	 * @param line: String value & line of poetry
	 * @return true if repeated; false if not repeated
	 * Catches if there are two of the same type of words in a row.
	 * @TODO: modify for specific cases where repetition of type is legal
	 */
	public static boolean repeat(String line) {
		CoreMap sentence = POS(line); // annotate line
		String last = "";
		for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
			String current = token.get(PartOfSpeechAnnotation.class);
			if(current.equals(last)) {
				return true;
			}
			last = current;
		}
		return false;
	}
}
