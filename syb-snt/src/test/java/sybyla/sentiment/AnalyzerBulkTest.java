package sybyla.sentiment;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class AnalyzerBulkTest {
	
	private static class TestElement{
		
		private String text;
		private double sentiment;
		
		public TestElement(String text, double sentiment){
			this.text =  text;
			this.sentiment = sentiment;
		}

		public String getText() {
			return text;
		}

		public double getSentiment() {
			return sentiment;
		}
	}
	
	private List<TestElement> read(String file) throws IOException{
		
		List<TestElement> elements = new ArrayList<TestElement>();
		BufferedReader reader =  new BufferedReader(new InputStreamReader(new FileInputStream(new File(file)),"UTF-16"));
		String line;
		while((line = reader.readLine())!=null){
			String[] tokens = line.split("\t");
			int sentiment = 0;
			if ( line.trim().startsWith("#") || tokens.length<4){
				continue;
			}
			String s = tokens[1];
			if (s.equals("Excluído")){
				continue;
			}
			if (s.equals("Positiva")){
				sentiment = 1;
			}
			else if (s.equals("Negativa")){
				sentiment =-1;
			} else if (s.equals("Neutra")){
				sentiment =0;
			} else{
				fail("Unknown label in test file: "+s);
			}
			
			
			String text =  tokens[3];
			TestElement t =  new TestElement(text, sentiment);
			elements.add(t);
		}
		return elements;
	}
	
	
	@Test
	public void test1(){
		runTest("src/test/resources/globo_tweets.txt");
	}
	
	@Test
	public void test2(){
		runTest("src/test/resources/sky_tweets.txt");
	}
	
	public void runTest(String file) {
		try {
			List<TestElement> elements = read(file);
			assertTrue(elements.size()>0);
			Analyzer analyzer = new Analyzer(Language.PORTUGUESE, Type.PRODUCT);
			int n = 0;
			int correct = 0;
			StringBuilder sb = new StringBuilder("Erros: \n");
			for (TestElement e: elements){
				n++;
				double score = analyzer.analyze(e.getText());
				double expectedScore =  e.getSentiment();
				String exp=null;
				String s=null;;
				if (expectedScore < 0){
					exp = "Negativa";
				} else if (expectedScore > 0){
					exp = "Positiva";
				} else if (expectedScore == 0){
					exp = "Neutra";
				}
				if (score < 0){
					s = "Negativa";
				} else if (score > 0){
					s = "Positiva";
				} else if (score == 0){
					s = "Neutra";
				}
				
				System.out.println(s+ " "+ score+"\t=>\t"+e.getText());
				
				if(   (score < 0 && expectedScore < 0)
				   || (score > 0 && expectedScore > 0)
				   || (score == 0 && expectedScore == 0))	{
					correct++;
				} else {
					sb.append("Erro:  classificação esperada "+ exp + " mas classificado como "+ s+"\n");
					sb.append("Text: "+ e.getText()+"\n");
				}
			}
			System.out.println(correct+ " casos de "+ n+"  casos corretamente classificados");
			System.out.println(sb.toString());
		} catch (Exception e) {
			System.out.println(e);
			Assert.fail(e.getMessage());
		}
	}

}
