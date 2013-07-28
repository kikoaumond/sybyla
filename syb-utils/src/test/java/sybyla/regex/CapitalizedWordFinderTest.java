package sybyla.regex;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Set;

import org.junit.Test;

import sybyla.io.FastFileReader;
 

public class CapitalizedWordFinderTest {
 
	@Test
    public void test1(){
		String text = "Bill  Clinton was the President of the United States";
		String[] words = text.split("\\s");
		CapitalizedWordFinder cap = new CapitalizedWordFinder();
		Set<String> terms = cap.find(words);
		assertTrue(terms.contains("Bill Clinton"));
		assertTrue(terms.contains("President of the United States"));
		assertTrue(terms.size()==2);
	}
 
	@Test
    public void test2(){
		String text = "The House of Representatives";
		String[] words = text.split("\\s");
		CapitalizedWordFinder cap = new CapitalizedWordFinder();
		Set<String> terms = cap.find(words);
		assertTrue(terms.contains("House of Representatives"));
		assertTrue(terms.size()==1);
	}
	
	@Test
    public void test3(){
		String text = "William Jeffersön   Clinton was the President of the Ünited States";
		CapitalizedWordFinder cap = new CapitalizedWordFinder();
		Set<String> terms = cap.find(text);
		assertTrue(terms.contains("William Jeffersön Clinton"));
		assertTrue(terms.contains("President"));
		assertTrue(terms.contains("Ünited States"));

		assertTrue(terms.size()==3);
	}
	
	@Test
    public void test4() throws IOException{
		FastFileReader reader = new FastFileReader("src/test/resources/regex/bessemer.txt");
		StringBuilder sb = new StringBuilder();
		String line =null;
		while ((line = reader.readLine())!=null){
			sb.append(line).append("\n");
		}
		String text = sb.toString();
		CapitalizedWordFinder cap = new CapitalizedWordFinder();
		Set<String> terms = cap.find(text);
		assertTrue(terms.contains("Bessemer Venture Partners"));
		
	}
	

 
	
 
	
}