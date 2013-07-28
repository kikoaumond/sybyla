package sybyla.sentiment;

import static org.junit.Assert.*;
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
	
	
	

	
	
}
