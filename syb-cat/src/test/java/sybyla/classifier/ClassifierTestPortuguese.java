package sybyla.classifier;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import sybyla.classifier.CategoryMap.CategoryMapEntry;
import sybyla.io.FastFileReader;

public class ClassifierTestPortuguese {
	
	@BeforeClass
	public static void setup() throws IOException{
		Classifier.init();
	}
	
	private String loadFile(String file) throws IOException{
		
		FastFileReader fileReader =  new FastFileReader(file);
		String line;
		StringBuilder sb = new StringBuilder();
		while((line=fileReader.readLine()) != null){
				sb.append(line+"\n");
		}
		return sb.toString();
		
	}

	@Test
	public void test1() throws Exception {
		String text =  loadFile("src/test/resources/painelFolha.txt");
		Set<String> expectedCategories = new HashSet<String>();

		expectedCategories.add("Law, Government & Politics");


		Classifier classifier = new Classifier();
		List<Category> categories = classifier.classify(text);
		for(int i=0;i<categories.size();i++){
			Category scoredCategory =  categories.get(i);
			String category = scoredCategory.getName();
			System.out.println(category+" => score: "+scoredCategory.getScore());
			Set<CategoryMapEntry>  entries= CategoryMap.getCategoryEntry(category);
			for (CategoryMapEntry entry: entries){
				System.out.println(entry.toJSON());
				String cat = entry.getDisplayCategory();
				String iab =  entry.getIab();
				expectedCategories.remove(cat);
				expectedCategories.remove(iab);
			}
		}
		System.out.println("#######################################");
		assertTrue(expectedCategories.size()==0);

	}

	@Test
	public void test2() throws Exception {
		String text =  loadFile("src/test/resources/casaJardim.txt");
		Set<String> expectedCategories = new HashSet<String>();
	
		expectedCategories.add("Casa e Jardim");
	
	
		Classifier classifier = new Classifier();
		List<Category> categories = classifier.classify(text);
		for(int i=0;i<categories.size();i++){
			Category scoredCategory =  categories.get(i);
			String category = scoredCategory.getName();
			System.out.println(category+" => score: "+scoredCategory.getScore());
			Set<CategoryMapEntry>  entries= CategoryMap.getCategoryEntry(category);
			for (CategoryMapEntry entry: entries){
				System.out.println(entry.toJSON());
				String cat = entry.getDisplayCategory();
				String iab =  entry.getIab();
				expectedCategories.remove(cat);
				expectedCategories.remove(iab);
			}
		}
		System.out.println("#######################################");
		assertTrue(expectedCategories.size()==0);
	
	}

	@Test
	public void test4() throws Exception {
		String text =  loadFile("src/test/resources/politica.txt");
		Set<String> expectedCategories = new HashSet<String>();
	
		expectedCategories.add("Política");
	
	
		Classifier classifier = new Classifier();
		List<Category> categories = classifier.classify(text);
		for(int i=0;i<categories.size();i++){
			Category scoredCategory =  categories.get(i);
			String category = scoredCategory.getName();
			System.out.println(category+" => score: "+scoredCategory.getScore());
			Set<CategoryMapEntry>  entries= CategoryMap.getCategoryEntry(category);
			for (CategoryMapEntry entry: entries){
				System.out.println(entry.toJSON());
				String cat = entry.getDisplayCategory();
				String iab =  entry.getIab();
				expectedCategories.remove(cat);
				expectedCategories.remove(iab);
			}
		}
		System.out.println("#######################################");
		assertTrue(expectedCategories.size()==0);
	
	}

	@Test
	public void test3() throws Exception {
		String text =  loadFile("src/test/resources/toyotaRAV4.txt");
		Set<String> expectedCategories = new HashSet<String>();
	
		expectedCategories.add("Automóveis");
	
	
		Classifier classifier = new Classifier();
		List<Category> categories = classifier.classify(text);
		for(int i=0;i<categories.size();i++){
			Category scoredCategory =  categories.get(i);
			String category = scoredCategory.getName();
			System.out.println(category+" => score: "+scoredCategory.getScore());
			Set<CategoryMapEntry>  entries= CategoryMap.getCategoryEntry(category);
			for (CategoryMapEntry entry: entries){
				System.out.println(entry.toJSON());
				String cat = entry.getDisplayCategory();
				String iab =  entry.getIab();
				expectedCategories.remove(cat);
				expectedCategories.remove(iab);
			}
		}
		System.out.println("#######################################");
		assertTrue(expectedCategories.size()==0);
	
	}
	
	
}
