package sybyla.bayes;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
	private int vocabulary=0;
	
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
	
	private BayesModel positiveModel =  new BayesModel("positive",1);
	private BayesModel negativeModel = new BayesModel("negative",-1);
	private BayesModel neutralModel = new BayesModel("neutral",0);
	
	private static final String POSITIVE="Positiva";
	private static final String NEGATIVE="Negativa";
	private static final String NEUTRAL="Neutra";
	private static final String EXCLUDED="Excluído";
	private static final String MODEL_FILE="/sentimentModel.txt";

	public static BayesClassifier load() {
		BayesClassifier classifier=null;
		try {
			classifier = new BayesClassifier(Language.PORTUGUESE);
			InputStream in = BayesClassifier.class.getResourceAsStream(MODEL_FILE);
			classifier.loadModel(in);
		} catch (Exception e) {
			LOGGER.error("Error loading sentiment classifier ", e);
		}
		
		return classifier;
	}

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
	
	public BayesClassifier(Language language, String filename) throws Exception{

		if(language==Language.ENGLISH){
			
			nlp = new OpenNLPAnalyzer3();
		} 
		
		else if(language==Language.PORTUGUESE){
				
			nlp = new PortugueseOpenNLPAnalyzer();
		
		} else{
		
			throw new Exception("Unrecognized language");
		}
		
		loadModel(filename);
	
	}
	
	public void reset(){
		termEntropies = new HashMap<String, Double>(); 
		positiveTermEntropies = new HashMap<String, Double>(); 
		negativeTermEntropies = new HashMap<String, Double>(); 
		neutralTermEntropies = new HashMap<String, Double>(); 
		
		positiveModel =  new BayesModel("positive",1);
		negativeModel = new BayesModel("negative",-1);
		neutralModel = new BayesModel("neutral",0);
		
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
			
			double r =  (positive>negative)?positive:negative;
			r = (r>neutral)?r:neutral;
			
			if (positive ==  negative){
				result = 0;
				certainty = 0.5d;
			} else {
				if (r ==  positive){
						
					result = 1;
					certainty =  1/(1 + Math.exp(negative-positive) + Math.exp(neutral - positive));
					
				} else if (r ==  negative){
					
					result = -1;
					certainty =  1/(1 + Math.exp(positive-negative) + Math.exp(neutral - negative));

				} else if (r == neutral){
					result = 0;
					certainty =  1/(1 + Math.exp(positive-neutral) + Math.exp(negative - neutral));

				}
			}
		}

		public double getPositive() {
			return positive;
		}

		public double getNegative() {
			return negative;
		}

		public double getResult() {
			return result;
		}

		public double getCertainty() {
			return certainty;
		}
	}
	
	public void train(String file) throws Exception{
		List<TestElement> train = read(file);
		FeatureExtractor fe = new FeatureExtractor(4);
		for(TestElement te: train){
			String text = te.getText();
			double sentiment = te.getSentiment();
			String[] sentences = nlp.detectSentences(text);
			for(String sentence: sentences){
				List<String> features =  fe.extractFeatures(sentence);
				addToModel(features,sentiment);
			}
		}
		
		computeEntropies();
		buildModels();
	}
	
	public void train(File[] files) throws Exception{
		FeatureExtractor fe =  new FeatureExtractor(4);
		for(File file: files){
			String f =  file.getAbsolutePath();
			if (!f.endsWith(".txt")){
				continue;
			}
			List<TestElement> train = read(f);
			for(TestElement te: train){
				String text = te.getText();
				double sentiment = te.getSentiment();
				String[] sentences = nlp.detectSentences(text);
				for(String sentence: sentences){
					List<String> features =  fe.extractFeatures(sentence);
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
			neutralEntropy = pLogp(entr) + pLogp(epos + eneg);

			
			termEntropies.put(term, entropy);
			positiveTermEntropies.put(term, positiveEntropy);
			negativeTermEntropies.put(term, negativeEntropy);
			neutralTermEntropies.put(term, neutralEntropy);

			
		}
	}
	
	private void buildModels(){
		Set<String> allTerms  =  new HashSet<String>();
		allTerms.addAll(positive.keySet());
		allTerms.addAll(negative.keySet());
		allTerms.addAll(neutral.keySet());

		vocabulary = allTerms.size();
		
		buildModel(positiveModel,positive, positiveTermEntropies, vocabulary);
		buildModel(negativeModel,negative, negativeTermEntropies, vocabulary);
		buildModel(neutralModel,neutral, neutralTermEntropies, vocabulary);

		
	}
	
	private void buildModel(BayesModel model, Map<String,Integer> counts,Map<String,Double> entropies, int vocabulary){
		int n = 0;
		for(String term: counts.keySet()){
			Integer count = counts.get(term);
			if (count == null) count=0;
			n+=count;
		}
		
		for (String term: counts.keySet()){
			Integer count = counts.get(term);
			
			if (count == null) continue;
			
			double probability = ((double) count+1.d)/((double) n+vocabulary);
			double entropy = entropies.get(term);
			model.add(term, probability, entropy, count);
		}
		model.setVocabularySize(vocabulary);
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
	
	public void readModel(String filename){
		
	}
	
	public void writeModel(String filename, int length) throws IOException {
		
		List<String> terms  = new ArrayList<String>();
		
		terms.addAll(negativeModel.getTerms());
		terms.addAll(neutralModel.getTerms());
		terms.addAll(positiveModel.getTerms());
		
		Collections.sort(terms);
		
		
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(filename)),"UTF-8"));
		
		writer.write(terms.size()+"\n");
		
		LOGGER.info("writing model file "+filename);
		
		for(String term: terms){
			String[] tt  = term.split("\\s");
			if (tt.length <= length){
				negativeModel.write(term,writer);
				neutralModel.write(term,writer);
				positiveModel.write(term, writer);
			}
		}	
		writer.flush();
		writer.close();
	}
	public void saveModels(String filename) throws IOException{
		
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(filename)),"UTF-8"));
		LOGGER.info("writing model file "+filename);
		positiveModel.write(writer);
		
		negativeModel.write(writer);
		
		neutralModel.write(writer);
		writer.close();
	}
	
	
	public void loadModel(String filename) throws NumberFormatException, IOException{
		FileInputStream in  =  new FileInputStream(new File(filename));
		loadModel(in);
	}
	
	public void loadModel(InputStream in) throws NumberFormatException, IOException{
		
		String line =  null;
		try{
		BufferedReader reader = new BufferedReader(
									new InputStreamReader(in,"UTF-8"));

		line=reader.readLine();
		
		int vocabularySize =  Integer.parseInt(line);
		
		negativeModel.setVocabularySize(vocabularySize);
		neutralModel.setVocabularySize(vocabularySize);
		positiveModel.setVocabularySize(vocabularySize);
		
		while((line=reader.readLine())!=null){
			/*
			 * 		
		sb.append(sentiment).append("\t").
			append(term).append("\t").
			append(le.getProbability()).
			append("\t").
			append(le.getEntropy()).
			append(le.getOccurrences()).
			append("\n");
			
			 */
			String[] tokens = line.split("\t");
			int sentiment = Integer.parseInt(tokens[0]);
			String term = tokens[1];
			double probability = Double.parseDouble(tokens[2]);
			double entropy = Double.parseDouble(tokens[3]);
			int occurrences  = Integer.parseInt(tokens[4]);

			if (sentiment == -1){
				negativeModel.add(term, probability, entropy, occurrences);
			} else if (sentiment == 1){
				positiveModel.add(term, probability, entropy, occurrences);
			} else if (sentiment == 0){
				neutralModel.add(term, probability, entropy, occurrences);
			}
		}
		}catch(Exception e){
			LOGGER.error("Error reading line \n"+line,e);
			System.out.println(e);
		} finally {
			in.close();
		}
	}
	
	
	
	public void evaluateFile(String file) throws Exception{
		
		int[][] confusionMatrix =  new int[3][3];
		List<TestElement> elements  = read(file);
		for(TestElement element: elements){
			Result result = evaluate(element.getText());
			double s = result.getResult();
			double e = element.getSentiment();
			int i=1,j=1;
			if (s < 0){
				i=0;
			} else if (s > 0){
				i=2;
			}
			if (e < 0){
				j=0;
			} else if (e > 0){
				j=2;
			}
			confusionMatrix[i][j]++;
			if (i!=j){
				//LOGGER.info("Misclassification: expected "+e+" but was classified as "+s+"\n"+element.getText());
			}
		}
		int correct=0;
		int incorrect=0;
		
		StringBuilder sb =  new StringBuilder("\nCONFUSION MATRIX\n");
		for (int i=0;i<=2;i++){
			for (int j=0; j<=2;j++){
				sb.append(confusionMatrix[i][j]+ "\t");
				if (i==j){
					correct+=confusionMatrix[i][j];
				} else {
					incorrect+=confusionMatrix[i][j];
				}
			}
			sb.append("\n");
		}
		LOGGER.info("\n"+sb.toString()+"\n");
		LOGGER.info(correct+" correct classifications and "+incorrect+" incorrect classifications");
		
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
		FeatureExtractor fe =  new FeatureExtractor(1);
		if ((sentences==null) || (sentences.length==0)){
			return score;
		}
		int n=0;
		for(String s: sentences){
			List<String> features = fe.extractFeatures(s);
			double sc =model.evaluate(features);
			score += sc;
			n++;
				
		}

		if (n!=0){
			score =  score/(double) n;
		}
		return score;
	}
}
