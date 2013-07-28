package sybyla.classifier;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import sybyla.nlp.OpenNLPAnalyzer3;

public class CategoryEntityMapTest {
	private static OpenNLPAnalyzer3 openNLPAnalyzer;
	@BeforeClass
	public static void setup(){
		 openNLPAnalyzer = new OpenNLPAnalyzer3();
	}
	@Test
	public void test() throws IOException {
		
		BufferedReader reader = new BufferedReader(new FileReader("src/test/resources/abbottabad.txt"));
        StringBuilder sb = new StringBuilder();
        String line;
        while((line=reader.readLine())!=null){
     	   sb.append(line).append("\n");
        }
        String text = sb.toString();
	
        
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
	    
	   Set<Category> categories =  CategoryEntityMap.getCategories(termCounts);
	   for(Category category: categories){
		   System.out.println(category.getName()+"\t"+category.getScore());
	   }
	}

	@Test
	public void test2() throws IOException {
		
		BufferedReader reader = new BufferedReader(new FileReader("src/test/resources/mossad.txt"));
        StringBuilder sb = new StringBuilder();
        String line;
        while((line=reader.readLine())!=null){
     	   sb.append(line).append("\n");
        }
        String text = sb.toString();
	        
	    String[] sentences = openNLPAnalyzer.detectSentences(text);
	    String[][] tokens = openNLPAnalyzer.tokenize(sentences);
	    Map<Short, List<String>> entities = openNLPAnalyzer.findNamedEntities(tokens);
	    Set<String> ents =  new HashSet<String>();
	    for(List<String> e: entities.values()){
	    	ents.addAll(e);
	    }
	    
	    
		Set<Category> categories =  CategoryEntityMap.getCategories(ents);
		for(Category category: categories){
			System.out.println(category.getName()+"\t"+category.getScore());
		}
	}
	
	
	
}
