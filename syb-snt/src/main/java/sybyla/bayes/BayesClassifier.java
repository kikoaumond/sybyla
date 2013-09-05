package sybyla.bayes;


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

import org.apache.log4j.Logger;

import sybyla.nlp.NLPAnalyzer;
import sybyla.nlp.OpenNLPAnalyzer3;
import sybyla.nlp.PortugueseOpenNLPAnalyzer;
import sybyla.sentiment.FeatureExtractor;
import sybyla.sentiment.Language;

public class BayesClassifier {
	
	private static final Logger LOGGER = Logger.getLogger(BayesClassifier.class);
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
	
	private static final String POSITIVE="Positiva";
	private static final String NEGATIVE="Negativa";
	private static final String NEUTRAL="Neutra";
	private static final String EXCLUDED="Excluído";
	
	
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
	
	public static class Result{
		
		private double positive=0;
		private double negative=0;
		private double neutral=0;
		private double result=0;
		private double certainty=0;
		
		public Result(double positive, double negative, double neutral){
			
			this.positive = positive;
			this.negative = negative;
			this.neutral =  neutral;
			
			result = (positive > negative)?positive:negative;
			result = (result > neutral)?result:neutral;
			
			if (result == 0){
				certainty = 0;
			} else {
				double pp = 0;
				if (positive !=0){
					pp = Math.exp(pp);
				}
				double np = 0;
				if (negative !=0){
					np = Math.exp(pp);
				}
				double ntp = 0;
				if (neutral != 0){
					ntp =  Math.exp(np);
				}
				if (result ==  positive){
					result = 1;
					certainty =  pp/(np+ntp);
				} else if (result ==  negative){
					result = -1;
					certainty =  np/(pp+ntp);

				} else {
					result = 0;
					certainty =  ntp/(pp+np);
				}
				
			}
		}
	}
	
	public void train(String file) throws Exception{
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
		buildModels();
	}
	
	public void train(String[] files) throws Exception{
		
		for(String f: files){
			List<TestElement> train = read(f);
			for(TestElement te: train){
				String text = te.getText();
				double sentiment = te.getSentiment();
				String[] sentences = nlp.detectSentences(text);
				for(String sentence: sentences){
					List<String> features =  FeatureExtractor.extractFeatures(sentence);
					addToModel(features,sentiment);
				}
			}
		}
		
		computeEntropies();
		buildModels();
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
	
	private void buildModels(){

		buildModel(positiveModel,positive, positiveTermEntropies);
		buildModel(negativeModel,negative, negativeTermEntropies);
		buildModel(neutralModel,neutral, neutralTermEntropies);

	}
	
	private void buildModel(BayesModel model, Map<String,Integer> counts,Map<String,Double> entropies){
		int n = 0;
		for(String term: counts.keySet()){
			Integer count = counts.get(term);
			if (count == null) count=0;
			n+=count;
		}
		
		for (String term: counts.keySet()){
			Integer count = counts.get(term);
			
			if (count == null) continue;
			
			double probability = (double) count/(double) n;
			double entropy = entropies.get(term);
			model.add(term, probability, entropy);
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
			map.put(feature, ++count);
		}
		
		if( sentiment >0) {
			nPositiveTerms = positive.keySet().size();
		} else if (sentiment <0) {
			nNegativeTerms = negative.keySet().size();
		} else {
			nNeutralTerms =  neutral.keySet().size();
		}
	}
	
	public static class TestElement{
		
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
	
	List<TestElement> read(String file) throws Exception{
		
		List<TestElement> elements = new ArrayList<TestElement>();
		BufferedReader reader =  new BufferedReader(new InputStreamReader(new FileInputStream(new File(file)),"UTF-8"));
		LOGGER.info("Opening file "+ file + " for reading");
		String line;
		while((line = reader.readLine())!=null){
			
			if (line.trim().length()==0) continue;
			String[] tokens = line.split("\t");
			int sentiment = 0;
			if ( line.trim().startsWith("#") || tokens.length<4){
				continue;
			}
			String s = tokens[1];
			
			if (s.equals(EXCLUDED)){
				continue;
			}
			if (s.equals(POSITIVE)){
				sentiment = 1;
			}
			else if (s.equals(NEGATIVE)){
				sentiment =-1;
			} else if (s.trim().equals(NEUTRAL)){
				sentiment =0;
			} else{
				throw new Exception("Unknown label in test file: "+s+ " line: "+line);
			}
					
			String text =  tokens[3];
			TestElement t =  new TestElement(text, sentiment);
			elements.add(t);
		}
		LOGGER.info("Read "+elements.size()+ " elements from file "+ file);
		return elements;
	}
	
	public void saveModels(String filename) throws IOException{
		
		LOGGER.info("writing model file "+filename+"_positive");
		positiveModel.write(filename+"_positive");
		
		LOGGER.info("writing model file "+filename+"_negative");
		negativeModel.write(filename+"_negative");
		
		LOGGER.info("writing model file "+filename+"_neutral");
		neutralModel.write(filename+"_neutral");
	}
	
	public void readModels(String filename) throws Exception{
		
		positiveModel = new BayesModel("positive");
		positiveModel.read(filename+"_positive");
		
		negativeModel = new BayesModel("negative");
		negativeModel.read(filename+"_negative");
		
		neutralModel  = new BayesModel("neutral");
		neutralModel.read(filename+"_neutral");
	}
	
	private Result evaluate(String text){
		
		String[]  sentences = nlp.detectSentences(text);
		double positiveScore = evaluate(sentences, positiveModel);
		double negativeScore = evaluate(sentences, negativeModel);
		double neutralScore = evaluate(sentences, neutralModel);
		Result result  =  new Result(positiveScore, negativeScore, neutralScore);
		return result;
	}
	
	private double evaluate(String[] sentences, BayesModel model){
		double score = 0;
		int n=0;
		if ((sentences==null) || (sentences.length==0)){
			for(String s: sentences){
				List<String> features = FeatureExtractor.extractFeatures(s);
				double sc =model.evaluate(features);
				score += sc;
				n++;
				
			}
		}
		
		score =  score/(double) n;
		return score;
	}
}
