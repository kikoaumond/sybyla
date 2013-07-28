package sybyla.tag;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import sybyla.io.FastFileReader;
import sybyla.nlp.Language;
import sybyla.tag.Tagger.Tag;

public class TaggerTest {

	@Test
	public void testGetTags() throws IOException {
		FastFileReader reader = new FastFileReader("src/test/resources/pope.txt");
		StringBuilder sb = new StringBuilder();
		String line = null;
		while((line=reader.readLine())!=null){
			sb.append(line).append("\n");
		}
		Tagger tagger = new Tagger();
		String text = sb.toString();
		List<Tag> tags = tagger.getTags(text);
		for(Tag tag: tags){
			System.out.println(tag.getTerm()+" ->"+tag.getRelevance());
		}
	}
	
	@Test
	public void testGetTagsByType2() throws IOException {
		FastFileReader reader = new FastFileReader("src/test/resources/osamaBinLaden.txt");
		StringBuilder sb = new StringBuilder();
		String line = null;
		while((line=reader.readLine())!=null){
			sb.append(line).append("\n");
		}
		Tagger tagger = new Tagger();
		String text = sb.toString();
		Map<String,List<Tag>> tags = tagger.getTagsByType(text);
		for (String type: tags.keySet()){
			System.out.println();
			System.out.println("##################"+type+"######################");
			System.out.println();

			List<Tag> l = tags.get(type);
			for(Tag tag: l){
				System.out.println(tag.getTerm()+" ->"+tag.getRelevance());
			}
		}
	}	
	
	@Test
	public void testGetTagsByTypePortuguese2() throws IOException {
		FastFileReader reader = new FastFileReader("src/test/resources/portuguese/folha2.txt");
		StringBuilder sb = new StringBuilder();
		String line = null;
		while((line=reader.readLine())!=null){
			sb.append(line).append("\n");
		}
		Tagger tagger = new Tagger();
		String text = sb.toString();
		Map<String,List<Tag>> tags = tagger.getTagsByType(text, Language.PORTUGUESE);
		
		Set<String> expectedTags = new HashSet<String>();
		expectedTags.add("Palácio");
		for (String type: tags.keySet()){
			System.out.println();
			System.out.println("##################"+type+"######################");
			System.out.println();

			List<Tag> l = tags.get(type);
			for(Tag tag: l){
				System.out.println(tag.getTerm()+" ->"+tag.getRelevance());
				expectedTags.remove(tag.getTerm());
			}
		}	
		assertTrue(expectedTags.size()==0);
	}
	
	@Test
	public void testGetTagsByType3() throws IOException {
		FastFileReader reader = new FastFileReader("src/test/resources/benghazi.txt");
		StringBuilder sb = new StringBuilder();
		String line = null;
		while((line=reader.readLine())!=null){
			sb.append(line).append("\n");
		}
		Tagger tagger = new Tagger();
		String text = sb.toString();
		
		Map<String, Set<String>> entityMap = tagger.getEntities(text);
		for(String type: entityMap.keySet()){
			Set<String> entities = entityMap.get(type);
			System.out.println(type+"-------------------");
			for (String entity: entities){
				System.out.println(entity);
			}
		}
		Map<String,List<Tag>> tags = tagger.getTagsByType(text, Language.ENGLISH);
		
		Set<String> expectedTags = new HashSet<String>();
		expectedTags.add("Benghazi");
		for (String type: tags.keySet()){
			System.out.println();
			System.out.println("##################"+type+"######################");
			System.out.println();

			List<Tag> l = tags.get(type);
			for(Tag tag: l){
				System.out.println(tag.getTerm()+" ->"+tag.getRelevance());
				expectedTags.remove(tag.getTerm());
			}
		}	
		assertTrue(expectedTags.size()==0);
	}

	@Test
	public void testGetTagsByType4() throws IOException {
		FastFileReader reader = new FastFileReader("src/test/resources/moneyLaundering.txt");
		StringBuilder sb = new StringBuilder();
		String line = null;
		while((line=reader.readLine())!=null){
			sb.append(line).append("\n");
		}
		Tagger tagger = new Tagger();
		String text = sb.toString();
		
		Map<String, Set<String>> entityMap = tagger.getEntities(text);
		for(String type: entityMap.keySet()){
			Set<String> entities = entityMap.get(type);
			System.out.println(type+"-------------------");
			for (String entity: entities){
				System.out.println(entity);
			}
		}
		Map<String,List<Tag>> tags = tagger.getTagsByType(text, Language.ENGLISH);
		
		Set<String> expectedTags = new HashSet<String>();
		expectedTags.add("money laundering");
		for (String type: tags.keySet()){
			System.out.println();
			System.out.println("##################"+type+"######################");
			System.out.println();

			List<Tag> l = tags.get(type);
			for(Tag tag: l){
				System.out.println(tag.getTerm()+" ->"+tag.getRelevance());
				expectedTags.remove(tag.getTerm());
			}
		}	
		assertTrue(expectedTags.size()==0);
	}
	
	@Test
	public void testGetTagsByTypePortuguese() throws IOException {
		FastFileReader reader = new FastFileReader("src/test/resources/portuguese/folha.txt");
		StringBuilder sb = new StringBuilder();
		String line = null;
		while((line=reader.readLine())!=null){
			sb.append(line).append("\n");
		}
		Tagger tagger = new Tagger();
		String text = sb.toString();
		
		Map<String,List<Tag>> tags = tagger.getTagsByType(text, Language.PORTUGUESE);
		
		Set<String> expectedTags = new HashSet<String>();
		expectedTags.add("São Paulo");
		expectedTags.add("Educação");
		expectedTags.add("Maximilien Averláiz");
		
		for (String type: tags.keySet()){
			System.out.println();
			System.out.println("##################"+type+"######################");
			System.out.println();

			List<Tag> l = tags.get(type);
			for(Tag tag: l){
				System.out.println(tag.getTerm()+" ->"+tag.getRelevance());
				expectedTags.remove(tag.getTerm());
			}
			
		}
		assertTrue(expectedTags.size()==0);
		
		
	}
	
	@Test 
	public void testCountOccurrences(){
		String text = "Peter met Peters at San Peter's Square as the ceremony petered out";
		String term =  "Peter";
		Pattern pattern = Pattern.compile("(?i)\\b("+term+"([’s|\\'s|s]){0,1})\\b");
		Matcher matcher = pattern.matcher(text);
	    int n = 0;
	    while (matcher.find()){
	    	 int start = matcher.start();
	    	 int end = matcher.end();
	    	 String t = text.substring(start, end);
	    	 System.out.println(t);
	    	  n++;
	    }
	     
	    assertEquals(3,n);
	    System.out.println();
		term =  "peter";
		pattern = Pattern.compile("(?i)\\b("+term+"([’s|\\'s|s]){0,1})\\b");
		matcher = pattern.matcher(text);
	    n = 0;
	    while (matcher.find()){
	    	 int start = matcher.start();
	    	 int end = matcher.end();
	    	 String t = text.substring(start, end);
	    	 System.out.println(t);
	    	  n++;
	    }
	     
	    assertEquals(3,n);

	    System.out.println();
		term =  "peters";
		pattern = Pattern.compile("(?i)\\b("+term+"([’s|\\'s|s]){0,1})\\b");
		matcher = pattern.matcher(text);
	    n = 0;
	    while (matcher.find()){
	    	 int start = matcher.start();
	    	 int end = matcher.end();
	    	 String t = text.substring(start, end);
	    	 System.out.println(t);
	    	  n++;
	    }
	     
	    assertEquals(1,n);
	    
	    System.out.println();
		term =  "peter's";
		pattern = Pattern.compile("(?i)\\b("+term+"([’s|\\'s|s]){0,1})\\b");
		matcher = pattern.matcher(text);
	    n = 0;
	    while (matcher.find()){
	    	 int start = matcher.start();
	    	 int end = matcher.end();
	    	 String t = text.substring(start, end);
	    	 System.out.println(t);
	    	  n++;
	    }
	     
	    assertEquals(1,n);
	    
	    System.out.println();
		text = "Peter met Peters at San Peter’s Square as the ceremony petered out";

		term =  "peter’s";
		pattern = Pattern.compile("(?i)\\b("+term+"([’s|\\'s|s]){0,1})\\b");
		matcher = pattern.matcher(text);
	    n = 0;
	    while (matcher.find()){
	    	 int start = matcher.start();
	    	 int end = matcher.end();
	    	 String t = text.substring(start, end);
	    	 System.out.println(t);
	    	  n++;
	    }
	     
	    assertEquals(1,n);
	}
	
	@Test
	public void testExclusionPatterns(){
		
		String term="&#010;";
		assertTrue(term.matches("([^a-zA-Z]){1,}"));
		Pattern p = Pattern.compile("[^a-zA-Z0-9-\\'’]");
		assertTrue(p.matcher(term).find());
		
		term= "abc123456";
		Pattern p2 = Pattern.compile("([\\d]{5,})");
		assertTrue(p2.matcher(term).find());
		
		term="0abc";
		Pattern p3 = Pattern.compile( "^[^a-zA-Z]{1,}");
		assertTrue(p3.matcher(term).find());
		assertTrue(term.matches("^[^a-zA-Z].{0,}"));
		
	}
	
	@Test
	public void testCountOccurrences2(){
	
		String text = "But Benedict was seen as a weak manager, and his papacy was troubled by " +
				"debilitating scandals, most recently “Vatileaks,” in which his butler was " +
				"convicted by a Vatican court in October of aggravated theft after he admitted " +
				"stealing confidential documents, many of which wound up in a tell-all book that " +
				"showed behind-the-scenes Vatican intrigue.";
		
		String term="Vatileaks";
		
		Pattern pattern = Pattern.compile("(?i)\\b("+term+"([’s|\\'s|s]){0,1})\\b");

	    Matcher matcher = pattern.matcher(text);
	    int n = 0;
	    while (matcher.find()){
	    	n++;
	    }
	    assertEquals(1,n);

	}

	@Test
	public void testGetTagsByType() throws IOException {
		FastFileReader reader = new FastFileReader("src/test/resources/pope.txt");
		StringBuilder sb = new StringBuilder();
		String line = null;
		while((line=reader.readLine())!=null){
			sb.append(line).append("\n");
		}
		Tagger tagger = new Tagger();
		String text = sb.toString();
		Map<String,List<Tag>> tags = tagger.getTagsByType(text);
		for (String type: tags.keySet()){
			System.out.println();
			System.out.println("##################"+type+"######################");
			System.out.println();
	
			List<Tag> l = tags.get(type);
			for(Tag tag: l){
				System.out.println(tag.getTerm()+" ->"+tag.getRelevance());
			}
		}
		
	}
	
}
