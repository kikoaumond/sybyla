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

public class ClassifierTest {
	
	@BeforeClass
	public static void setup() throws IOException{
		Classifier.init();
	}

    @Test
    public void testClassifyURL1() {
        String url="http://economia.uol.com.br/listas/saiba-como-comprar-um-imovel.htm/";

        Classifier classifier = new Classifier();
        List<Category> categories = classifier.classifyURL(url);
        for(int i=0;i<categories.size();i++){
            Category scoredCategory =  categories.get(i);
            String category = scoredCategory.getName();
            System.out.println(category+" => score: "+scoredCategory.getScore());

        }
        System.out.println("#######################################");
        //assertTrue(expectedCategories.size()==0);
    }
	@Test
	public void test() throws Exception {
		String text =  loadFile("src/test/resources/Bill_Clinton.txt");
		Set<String> expectedCategories = new HashSet<String>();
		expectedCategories.add("News");
		expectedCategories.add("Law, Government & Politics");
		Classifier classifier = new Classifier();
		List<Category> categories = classifier.classify(text);
		for(int i=0;i<categories.size();i++){
			Category scoredCategory =  categories.get(i);
			String category = scoredCategory.getName();
			System.out.println(category+" => score: "+scoredCategory.getScore());
			Set<CategoryMapEntry>  entries= CategoryMap.getCategoryEntry(category);
			
			for (CategoryMapEntry entry: entries){
				String cat = entry.getDisplayCategory();
				String iab =  entry.getIab();
				expectedCategories.remove(cat);
				expectedCategories.remove(iab);
				System.out.println(entry.toJSON());
			}
		}
		System.out.println("#######################################");
		assertTrue(expectedCategories.size()==0);
	}
	
	@Test
	public void test4() throws Exception {
		String text =  loadFile("src/test/resources/abbottabad.txt");
		Set<String> expectedCategories = new HashSet<String>();
		expectedCategories.add("News");
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
	public void test5() throws Exception {
		String text =  loadFile("src/test/resources/mccartney.txt");
		Set<String> expectedCategories = new HashSet<String>();
		expectedCategories.add("Music");
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
	public void test6() throws Exception {
		String text =  loadFile("src/test/resources/mossad.txt");
		Set<String> expectedCategories = new HashSet<String>();
		expectedCategories.add("News");
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
	}
	
	@Test
	public void test7() throws Exception {
		String text =  loadFile("src/test/resources/BostonMarathon.txt");
		Set<String> expectedCategories = new HashSet<String>();
		expectedCategories.add("Politics");
		expectedCategories.add("News");
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
		assertTrue(!expectedCategories.contains("Politics"));
		assertTrue(!expectedCategories.contains("News"));


	}
	
	@Test
	public void test8() throws Exception {
		String text =  loadFile("src/test/resources/BostonBombing.txt");
		Set<String> expectedCategories = new HashSet<String>();
		expectedCategories.add("Politics");
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
	public void test9() throws Exception {
		String text =  loadFile("src/test/resources/BostonBombing2.txt");
		Set<String> expectedCategories = new HashSet<String>();
		expectedCategories.add("Politics");
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
	public void test10() throws Exception {
		String text =  loadFile("src/test/resources/benghazi.txt");
		Set<String> expectedCategories = new HashSet<String>();
		expectedCategories.add("Politics");
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
		String text =  loadFile("src/test/resources/Miles_Davis.txt");
		Set<String> expectedCategories = new HashSet<String>();
		expectedCategories.add("Music");
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
		String text =  loadFile("src/test/resources/Newt_Gingrich.txt");
		Set<String> expectedCategories = new HashSet<String>();
		expectedCategories.add("News");
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
	public void test11() throws Exception {
		String text =  loadFile("src/test/resources/immigration.txt");
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
	public void test12() throws Exception {
		String text =  loadFile("src/test/resources/popeBrazilVisit.txt");
		Set<String> expectedCategories = new HashSet<String>();
		expectedCategories.add("Religion");
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
	public void test13() throws Exception {
		String text =  loadFile("src/test/resources/royalBaby.txt");
		Set<String> expectedCategories = new HashSet<String>();
		expectedCategories.add("Family & Parenting");
		expectedCategories.add("Arts & Entertainment");
		expectedCategories.add("Celebrities");
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
	public void test14() throws Exception {
		String text =  loadFile("src/test/resources/MohamedMorsi.txt");
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
	public void test15() throws Exception {
		String text =  loadFile("src/test/resources/AnthonyBourdain.txt");
		Set<String> expectedCategories = new HashSet<String>();

		expectedCategories.add("Food & Drink");


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
	public void test16() throws Exception {
		String text =  loadFile("src/test/resources/longText.txt");
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
	public void test17() throws Exception {
		String text =  loadFile("src/test/resources/financialFraud.txt");
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
	public void test18() throws Exception {
		String text =  loadFile("src/test/resources/AnthonyWiener.txt");
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
	public void test19() throws Exception {
		String text =  loadFile("src/test/resources/Facebook.txt");
		Set<String> expectedCategories = new HashSet<String>();
	
		expectedCategories.add("Business");
	
	
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
	public void test20() throws Exception {
		String text =  loadFile("src/test/resources/ArizonaFire.txt");
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
	public void test21() throws Exception {
		String text =  loadFile("src/test/resources/MohamedMorsi2.txt");
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
	public void test22() throws Exception {
		String text =  loadFile("src/test/resources/smoking.txt");
		Set<String> expectedCategories = new HashSet<String>();
	
		expectedCategories.add("Health & Fitness");
	
	
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
