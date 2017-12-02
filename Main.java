/**
 * This class is used for testing the Grammar.java class
 * TEST TYPES: 
 * >TYPE 1: types designed to succeed - i.e.: actual ED poems
 * >TYPE 2: types designed to fail - obvious failures and edge cases 
 */
import java.io.*;
import java.util.*;

import edu.stanford.nlp.coref.CorefCoreAnnotations;

import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.io.*;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.util.*;


public class Main {
	public static void main(String[] args) {
		//TYPE 1 TESTS
		
		String[] test1 = {"this works","this works", "this works", "this works"};
		//double r1 = Grammar.evaluate(test1);
		//System.out.println(r1);
		
		//TYPE 2 TESTS
		String[] test2 = {"what do","all ends"};
		//double r2 = Grammar.evaluate(test2);
		//System.out.println(r2);
		
		String[] poem1 = {"A narrow fellow in the grass",
				"Occasionally rides;",
		"You may have met himódid you not",
		"His notice instant is,",
		"The grass divides as with a comb,",
		"A spotted shaft is seen,",
		"And then it closes at your feet,",
		"And opens further on.",

		"He likes a boggy acre",
		"A floor too cool for corn,",
		"Yet when a boy and barefoot,",
		"I more than once at noon",
		"Have passed, I thought, a whip lash,",
		"Unbraiding in the sun,",
		"When stooping to secure it,",
		"It wrinkled and was gone.",

		"Several of nature's people",
		"I know, and they know me;",
		"I feel for them a transport",
		"Of cordiality.",
		"Yet never met this fellow,",
		"Attended or alone,",
		"Without a tighter breathing,",
		"And zero at the bone."};
		double r3 = Grammar.evaluate(poem1);
		System.out.println(r3);
		
		
		
	}

}
