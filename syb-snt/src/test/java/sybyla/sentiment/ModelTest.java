package sybyla.sentiment;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Set;

import org.junit.Test;


public class ModelTest {
	
	@Test
	public void testLoad() throws Exception {
		Model model = new Model(Model.PRODUCT_MODEL_PORTUGUESE);
		assertNotNull(model);
	}
	
	@Test
	public void testNormalize() throws Exception {
		String text  = " This is a string   of  text with  multiple spaces .\n\n";
		String t = Model.normalize(text);
		assertEquals("this is a string of text with multiple spaces",t);
	}
	
	@Test
	public void testEvaluate() throws Exception{
		Model model = new Model(Model.PRODUCT_MODEL_PORTUGUESE);
		String sentence = "ChatON é um messenger para celulares rico em recursos";
		double score = model.evaluate(sentence);
		System.out.println("Score: "+score);
		assertTrue(score > 0);
	}
	
	@Test
	public void testEvaluate2() throws Exception{
		Model model = new Model(Model.PRODUCT_MODEL_PORTUGUESE);
		String sentence = "O produto não funcionou";
		double score = model.evaluate(sentence);
		System.out.println("Score: "+score);
		assertTrue(score < 0);
	}
	
	@Test
	public void testEvaluate3() throws Exception{
		Model model = new Model(Model.PRODUCT_MODEL_PORTUGUESE);
		String sentence = "	O ChatON não se limita apenas ao envio de mensagens de texto";
		double score = model.evaluate(sentence);
		System.out.println("Score: "+score);
		assertTrue(score > 0);
	}
	
	@Test
	public void testEvaluate4() throws Exception{
		Model model = new Model(Model.PRODUCT_MODEL_PORTUGUESE);
		String sentence = "O produto funcionou mal";
		double score = model.evaluate(sentence);
		System.out.println("Score: "+score);
		assertTrue(score < 0);
	}
	
	
	@Test
	public void testFeatures(){
		String s =  "O que é isso ?? ?! Nem dá para acreditar, não é? Mas fazer o que: Nem adianta reclamar.  a Globo manda neste país...";
		List<String> features = FeatureExtractor.extractFeatures(s);
		System.out.println(features.size()+" Features");
		for(String feature: features){
			
			System.out.println(feature);
		}
		assertTrue(features.contains("o que é isso ?!"));
		assertTrue(features.contains("globo manda neste país ..."));
		assertTrue(features.contains("nem {_} para"));
		assertTrue(features.contains("nem dá para acreditar"));
		assertTrue(features.contains("fazer o que"));
		assertTrue(features.contains("adianta reclamar"));
		assertTrue(features.contains("manda"));
		assertTrue(features.contains("é ? mas fazer o que"));
		assertTrue(features.contains("é {____} que"));
	}

	
	
}
