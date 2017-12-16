/**Stats.java
 * Perform analytics on poems. 
 * Desired analytics: 
 * >mean of line length
 * >
 */
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.io.File;


public class Stats {
	
	private static LinkedList<Double> poemLengths = new LinkedList<Double>() ; //length of each poem
	private static LinkedList<Double> numWords = new LinkedList<Double>() ; // number of words in each poem
	private static LinkedList<Double> lineLength = new LinkedList<Double>();
	
	public static void main(String[] args) throws IOException {
        String target_dir = "/Users/valeriemcculloch/Documents/poems";
        File dir = new File(target_dir);
        File[] files = dir.listFiles(); // get poems

        double len;
        double nWords;
        double tWords=0;
        double tLen=0;
        double minLen = 10000;
        
        for (File f : files) { // loop through each poem
            if(f.isFile()) {
                BufferedReader inputStream = null;

                try {
                    inputStream = new BufferedReader(
                                    new FileReader(f));
                    String line;
                    len = 0;
                    nWords = 0;
                    
                    
                    while ((line = inputStream.readLine()) != null) {
                    		line = line.replace("�", "--"); //fix character issue
                    		len+=1;
                    		nWords+=line.split(" ").length;
                    		tWords+=nWords;
                    		tLen+=1;
                    		
                    }
                    if(len<minLen && len>1) {
                    	minLen=len;
                    }
                    poemLengths.add(len);
                    numWords.add(nWords);
                    lineLength.add(nWords/len);
                }
                finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                }
            }
        } //end of for loop
        System.out.println(mean(poemLengths));
        System.out.println(mean(numWords));
        System.out.println(tWords/tLen);
        System.out.println(minLen);
        
    }
	
	public static double mean(LinkedList<Double> list) {
		double num = 0;
		for(Double i: list) {
			num+=i;
		}
		
		return num/list.size();
		
	}
}
