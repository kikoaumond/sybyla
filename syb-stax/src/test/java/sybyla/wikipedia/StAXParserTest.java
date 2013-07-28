package sybyla.wikipedia;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Set;

import org.junit.Test;

public class StAXParserTest {
	
	@Test
	public void testUTF8() throws IOException {
		StAXParser parser = new StAXParser();
		PageListProcessor processor = new PageListProcessor();
		parser.read("src/test/resources/utf8Test.xml",processor);
		List<Page> pages = processor.getPages();
		assertTrue(pages.size()==1);
		Page page = pages.get(0);
		String expectedTitle="Löts øf ÜTF-8 çharàctêrs";
		String title = page.getTitle();
		assertEquals(expectedTitle, title);
		
		
		

		
		StringBuilder sb = new StringBuilder();
		FileInputStream in = new FileInputStream("src/test/resources/wikipedia/SalomonHenschenBody.txt");
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String line;
		while((line=reader.readLine())!=null){
			sb.append(line).append("\n");
		}
		
		String expectedBody =  "Thïs ís sømë têxt wîth lóts of ÚTF-8 chärâcters";
		String body = page.getBody();
		assertEquals(expectedBody,body);
	}

	@Test
	public void test() throws IOException {
		StAXParser parser = new StAXParser();
		PageListProcessor processor = new PageListProcessor();
		parser.read("src/test/resources/wikipedia/SalomonHenschen.xml",processor);
		List<Page> pages = processor.getPages();
		assertTrue(pages.size()==1);
		Page page = pages.get(0);
		String expectedTitle="Salomon Eberhard Henschen";
		String title = page.getTitle();
		assertEquals(expectedTitle, title);
		assertNull(page.getSynonyms());
		Set<String> links = page.getLinks();
		
		assertFalse(links.contains("File:Salomon Eberhard Henschen (ca. 1901).jpg|right|thumb|Salomon Eberhard Henschen (ca. 1901)"));
		
		assertTrue(links.contains("Uppsala"));
		assertTrue(links.contains("neurologist"));
		assertTrue(links.contains("botanical"));
		assertTrue(links.contains("Brazil"));
		assertTrue(links.contains("Stockholm"));
		assertTrue(links.contains("Leipzig"));
		
		Set<String> categories =  page.getCategories();
		assertTrue(categories.contains("1847 births"));
		assertTrue(categories.contains("1930 deaths"));
		assertTrue(categories.contains("Neurologists"));
		assertTrue(categories.contains("Uppsala University faculty"));
		assertTrue(categories.contains("People from Uppsala"));
		assertTrue(categories.contains("Uppsala University alumni"));
		assertTrue(categories.contains("Swedish physicians"));
		
		StringBuilder sb = new StringBuilder();
		FileInputStream in = new FileInputStream("src/test/resources/wikipedia/SalomonHenschenBody.txt");
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String line;
		while((line=reader.readLine())!=null){
			sb.append(line).append("\n");
		}
		
		String expectedBody =  sb.toString().trim();
		System.out.println("Expected \n"+expectedBody);
		assertEquals(expectedBody,page.getBody());
	}

	@Test
	public void testPortuguese() throws IOException {
		StAXParser parser = new StAXParser();
		PageListProcessor processor = new PageListProcessor();
		parser.read("src/test/resources/wikipedia/Astronomia.xml",processor);
		List<Page> pages = processor.getPages();
		assertTrue(pages.size()==1);
		Page page = pages.get(0);
		String expectedTitle="Astronomia";
		String title = page.getTitle();
		assertEquals(expectedTitle, title);
		assertNull(page.getSynonyms());
		Set<String> links = page.getLinks();
		
		assertFalse(links.contains("File:Salomon Eberhard Henschen (ca. 1901).jpg|right|thumb|Salomon Eberhard Henschen (ca. 1901)"));
		
		assertTrue(links.contains("Ciências naturais"));
		assertTrue(links.contains("menir"));
		assertTrue(links.contains("Corpo celeste"));
		assertTrue(links.contains("Rio de Janeiro"));
		assertTrue(links.contains("estado do Rio de Janeiro"));
		
		
		Set<String> categories =  page.getCategories();
		assertTrue(categories.contains("Astronomia"));
		assertTrue(categories.contains("Geomática"));
		
		StringBuilder sb = new StringBuilder();
		FileInputStream in = new FileInputStream("src/test/resources/wikipedia/Astronomia.txt");
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String line;
		while((line=reader.readLine())!=null){
			sb.append(line).append("\n");
		}
		
		String expectedBody =  sb.toString().trim();
		System.out.println("Expected \n"+expectedBody);
		assertEquals(expectedBody,page.getBody());
	}
}
