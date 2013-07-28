package sybyla.classifier;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import sybyla.nlp.OpenNLPAnalyzer3;

public class TermExtractorTest {

	@Test
	public void test() throws IOException {
		
		BufferedReader reader = new BufferedReader(new FileReader("src/test/resources/mossad.txt"));
        StringBuilder sb = new StringBuilder();
        String line;
        while((line=reader.readLine())!=null){
     	   sb.append(line).append("\n");
        }
        String text = sb.toString();
	
		OpenNLPAnalyzer3 openNLPAnalyzer = new OpenNLPAnalyzer3();
        
	    String[] sentences = openNLPAnalyzer.detectSentences(text);
	    String[][] tokens = openNLPAnalyzer.tokenize(sentences);
	    TermExtractor termExtractor = new TermExtractor();         
	           
	    boolean caseSensitive =  false;        
	    Map<String,Integer> termCounts = termExtractor.extractWithCounts(tokens, 5, caseSensitive);
	    List<String> s =  new ArrayList<String>(termCounts.keySet());
	    Collections.sort(s);
	    for (String st: s){
	    	Integer c = termCounts.get(st);
	    	System.out.println(st+" => "+c);
	    }
	    
	}

	@Test
	public void test2() throws IOException {
		
		BufferedReader reader = new BufferedReader(new FileReader("src/test/resources/abbottabad.txt"));
        StringBuilder sb = new StringBuilder();
        String line;
        while((line=reader.readLine())!=null){
     	   sb.append(line).append("\n");
        }
        String text = sb.toString();
	
		OpenNLPAnalyzer3 openNLPAnalyzer = new OpenNLPAnalyzer3();
        
	    String[] sentences = openNLPAnalyzer.detectSentences(text);
	    String[][] tokens = openNLPAnalyzer.tokenize(sentences);
	    TermExtractor termExtractor = new TermExtractor();         
	           
	    boolean caseSensitive =  false;        
	    Map<String,Integer> termCounts = termExtractor.extractWithCounts(tokens, 5, caseSensitive);
	    List<String> s =  new ArrayList<String>(termCounts.keySet());
	    Collections.sort(s);
	    for (String st: s){
	    	Integer c = termCounts.get(st);
	    	System.out.println(st+" => "+c);
	    }
	}
	
}
