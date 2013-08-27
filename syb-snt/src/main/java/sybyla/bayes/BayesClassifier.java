package sybyla.bayes;

import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import sybyla.nlp.NLPAnalyzer;
import sybyla.nlp.OpenNLPAnalyzer3;
import sybyla.nlp.PortugueseOpenNLPAnalyzer;
import sybyla.sentiment.FeatureExtractor;
import sybyla.sentiment.Language;

public class BayesClassifier {
	
	private int n=0;
	
	private Map<String, Integer> positive= new HashMap<String,Integer>();
	private int nPositiveTerms=0;
	
	private Map<String, Integer> negative= new HashMap<String, Integer>();
	private int nNegativeTerms=0;
	
	private Map<String, Integer> neutral= new HashMap<String, Integer>();
	private int nNeutralTerms=0;
	
	private NLPAnalyzer nlp;
	
	private boolean equalPriors=true;
	private boolean uniqueFeatures=false;
	
	private Map<String, Double> termEntropies = new HashMap<String, Double>(); 
	private Map<String, Double> positiveTermEntropies = new HashMap<String, Double>(); 
	private Map<String, Double> negativeTermEntropies = new HashMap<String, Double>(); 
	private Map<String, Double> neutralTermEntropies = new HashMap<String, Double>(); 
	
	private BayesModel positiveModel =  new BayesModel("positive");
	private BayesModel negativeModel = new BayesModel("negative");
	private BayesModel neutralModel = new BayesModel("neutral");
	
	
	public BayesClassifier(Language language) throws Exception{

		if(language==Language.ENGLISH){
			
			nlp = new OpenNLPAnalyzer3();
		} 
		
		else if(language==Language.PORTUGUESE){
				
			nlp = new PortugueseOpenNLPAnalyzer();
		
		} else{
		
			throw new Exception("Unrecognized language");
		}
	
	}
	
	public void train(String file) throws IOException{
		List<TestElement> train = read(file);
		for(TestElement te: train){
			String text = te.getText();
			double sentiment = te.getSentiment();
			String[] sentences = nlp.detectSentences(text);
			for(String sentence: sentences){
				List<String> features =  FeatureExtractor.extractFeatures(sentence);
				addToModel(features,sentiment);
			}
		}
		
		computeEntropies();
	}
	
	private void computeEntropies(){
		
		Set<String> allTerms = new HashSet<String>();
		allTerms.addAll(positive.keySet());
		allTerms.addAll(negative.keySet());
		allTerms.addAll(neutral.keySet());
		
		for(String term: allTerms){
			
			Integer pos = positive.get(term);
			if (pos == null) pos = 0;
			
			Integer neg = negative.get(term);
			if (neg == null) neg = 0;
			
			Integer ntr = neutral.get(term);
			if (ntr == null) ntr = 0;
			
			int sum = pos + neg + ntr;
			
			double entropy = 0;
			double positiveEntropy = 0;
			double negativeEntropy = 0;
			double neutralEntropy = 0;

			double epos = 0;
			double eneg = 0;
			double entr = 0;
			
			if (sum!=0){
				epos = (double) pos/(double) sum;
				eneg = (double) neg/(double) sum;
				entr = (double) ntr/(double) sum;
			}
					

			entropy += pLogp(epos) + pLogp(eneg) + pLogp(entr);
			positiveEntropy = pLogp(epos) + pLogp(eneg + entr);
			negativeEntropy = pLogp(eneg) + pLogp(epos + entr);
			neutralEntropy =  pLogp(entr) + pLogp(epos + eneg);
			
			termEntropies.put(term, entropy);
			positiveTermEntropies.put(term, positiveEntropy);
			negativeTermEntropies.put(term, negativeEntropy);
			neutralTermEntropies.put(term, neutralEntropy);
			
		}
	}
	
	private double pLogp(double p){
		if (p == 0) return 0;
		return p*Math.log(p);
	}
	
	private void addToModel(Collection<String> features, double sentiment){
		
		Map<String, Integer> map;
		
		if( sentiment >0) {
			map = positive;
		} else if (sentiment <0) {
			map = negative;
		} else {
			map =  neutral;
		}
		
		for (String feature: features){
			Integer count = map.get(feature);
			if (count ==  null){
				count=0;
			}
			map.put(feature, count++);
		}
		
		if( sentiment >0) {
			nPositiveTerms = positive.keySet().size();
		} else if (sentiment <0) {
			nNegativeTerms = negative.keySet().size();
		} else {
			nNeutralTerms =  neutral.keySet().size();
		}
	}
	
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
}
