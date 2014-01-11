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
import sybyla.sentiment.Result;

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
	
	private static final String POSITIVA="Positiva";
	private static final String POSITIVO="Positivo";

	private static final String NEGATIVA="Negativa";
	private static final String NEGATIVO="Negativo";

	private static final String NEUTRA="Neutra";
	private static final String NEUTRO="Neutro";
	
	private static final String EXCLUDED="Excluído";
	private static final String LIXO="lixo";

	private static final String MODEL_FILE="/sentiment_model.txt";
	private static final double DEFAULT_ENTROPY_FILTER=-0.6731d;

	public static BayesClassifier load() {
		BayesClassifier classifier=null;
		try {
			classifier = new BayesClassifier(Language.PORTUGUESE);
			InputStream in = BayesClassifier.class.getResourceAsStream(MODEL_FILE);
			classifier.loadModel(in);//, DEFAULT_ENTROPY_FILTER);
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
	
	public void train(String file) throws Exception{
		List<TestElement> train = read(file);
		FeatureExtractor fe = new FeatureExtractor(5);
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
		FeatureExtractor fe =  new FeatureExtractor(5);
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
			if ( line.trim().startsWith("#") || tokens.length<2){
				continue;
			}
			String s = tokens[0].trim();
			
			if (s.equalsIgnoreCase(EXCLUDED) || s.equalsIgnoreCase(LIXO)){
				continue;
			}
			if (s.equalsIgnoreCase(POSITIVA) || s.equalsIgnoreCase(POSITIVO)){
				sentiment = 1;
			}
			else if (s.equalsIgnoreCase(NEGATIVA) || s.equalsIgnoreCase(NEGATIVO)){
				sentiment =-1;
			} else if (s.equalsIgnoreCase(NEUTRA) || s.equalsIgnoreCase(NEUTRO)){
				sentiment =0;
			} else{
				//throw new Exception("Unknown label in test file: "+s+ " line: "+line);
				continue;
			}
					
			String text =  tokens[1];
			TestElement t =  new TestElement(text, sentiment);
			elements.add(t);
		}
		LOGGER.info("Read "+elements.size()+ " elements from file "+ file);
		return elements;
	}
	
	public void readModel(String filename){
		
	}
	
	public void writeModel(String filename, int length) throws IOException {
		
		Set<String> tt =  new HashSet<String>();

		List<String> terms  = new ArrayList<String>();
		
		tt.addAll(negativeModel.getTerms());
		tt.addAll(neutralModel.getTerms());
		tt.addAll(positiveModel.getTerms());
		
		terms.addAll(tt);
		
		Collections.sort(terms);		
		
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(filename)),"UTF-8"));
		
		writer.write(terms.size()+"\n");
		
		LOGGER.info("writing model file "+filename);
		
		for(String term: terms){
			String[] tokens  = term.split("\\s");
			
			LikelihoodEntropy pos =  positiveModel.get(term);
			LikelihoodEntropy neg =  negativeModel.get(term);
			LikelihoodEntropy ntr =  neutralModel.get(term);
			
			int posc = pos==null?0:pos.getOccurrences();
			int negc = neg==null?0:neg.getOccurrences();
			int ntrc = ntr==null?0:ntr.getOccurrences();
			
			int total = posc+negc+ntrc;
			if (total == 0){
				continue;
			}
			
			double posf = (double) posc/((double) total);
			double negf = (double) negc/((double) total);
			double ntrf = (double) ntrc/((double) total);
			
			boolean writePos = true;
			boolean writeNeg =  true;
			boolean writeNtr =  true;
			
			double minNeg = 0.40d;
			double minNtr = 0.75d;
			double minPos = 0.40d;
			
			if (negf < minNeg || negc <= 0){
				writeNeg = false;
			}
			
			if (ntrf < minNtr || ntrc <= 1){
				writeNtr =  false;
			}
			
			if (posf < minPos || posc <= 0){
				writePos = false;
			}
			
			
			if (tokens.length <= length){
				if (writeNeg){
					negativeModel.write(term,writer);
				}
				
				if (writeNtr){
					neutralModel.write(term,writer);
				}
				
				if (writePos){
					positiveModel.write(term, writer);
				}
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
	
	public void loadModel(String filename, double entropyFilter) throws NumberFormatException, IOException{
		FileInputStream in  =  new FileInputStream(new File(filename));
		loadModel(in, entropyFilter);
	}
	
	public void loadModel(InputStream in) throws NumberFormatException, IOException{
		
		String line =  null;
		int neg=0; 
		double negP=0.d;
		
		int pos=0;
		double posP=0.d;
		
		int ntr=0;
		double ntrP=0;
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
				negP+=probability;
				neg++;
			} else if (sentiment == 1){
				positiveModel.add(term, probability, entropy, occurrences);
				posP+=probability;
				pos++;
			} else if (sentiment == 0){
				neutralModel.add(term, probability, entropy, occurrences);
				ntrP+=probability;
				ntr++;
			}
		}
		}catch(Exception e){
			LOGGER.error("Error reading line \n"+line,e);
			System.out.println(e);
		} finally {
			in.close();
		}
		LOGGER.info("\nNeg: "+neg+" negP: "+negP+"\nNtr: "+ntr+" ntrP: "+ntrP+"\nPos: "+pos+" posP: "+posP);
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
				j=0;
			} else if (s > 0){
				j=2;
			}
			if (e < 0){
				i=0;
			} else if (e > 0){
				i=2;
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
		
	public Result evaluate(String text){
		
		String[]  sentences = nlp.detectSentences(text);
		return evaluate(sentences);
	}
	
	public Result evaluate(String[] sentences){
		
		double positiveScore = evaluate(sentences, positiveModel);
		double negativeScore = evaluate(sentences, negativeModel);
		double neutralScore = evaluate(sentences, neutralModel);

		Result result  =  new Result(positiveScore, negativeScore, neutralScore);
		return result;
	}
	
	private double evaluate(String[] sentences, BayesModel model){
		double score = 0;
		FeatureExtractor fe =  new FeatureExtractor(5);
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

	public void loadModel(InputStream in, double entropyFilter) throws NumberFormatException, IOException{
		
		String line =  null;
		int elements = 0;
		int discarded = 0;
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
			String[] words =  term.split("\\s");
			
			double probability = Double.parseDouble(tokens[2]);
			double entropy = Double.parseDouble(tokens[3]);
			int occurrences  = Integer.parseInt(tokens[4]);
			
			if (occurrences <= 1 && words.length > 1){
				discarded++;
				continue;
			}
			
			if(entropy < entropyFilter){
				discarded++;
				continue;
			}
			if (sentiment == -1){
				negativeModel.add(term, probability, entropy, occurrences);
				elements++;
			} else if (sentiment == 1){
				positiveModel.add(term, probability, entropy, occurrences);
				elements++;
			} else if (sentiment == 0){
				neutralModel.add(term, probability, entropy, occurrences);
				elements++;
			}
			elements++;
		}
		}catch(Exception e){
			LOGGER.error("Error reading line \n"+line,e);
			System.out.println(e);
		} finally {
			in.close();
		}
		LOGGER.info("Loaded total of "+ elements + " markers from model file. "
				+ discarded + " markers with low significance were not loaded. ");
	}
}
