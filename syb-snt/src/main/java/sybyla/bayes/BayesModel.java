package sybyla.bayes;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BayesModel {
	
	String name;
	Map<String, LikelihoodEntropy> model = new HashMap<String,LikelihoodEntropy>();
	private int vocabularySize=0;
	private int size=0;
	private int sentiment;
	
	
	public BayesModel(String name, int sentiment){
		this.name = name;
		this.sentiment =  sentiment;
	}
	
	public void add(String term, double probability, double entropy, int occurrences){
		LikelihoodEntropy me = new LikelihoodEntropy(term, probability, entropy, occurrences);
		model.put(term,me);
		size++;
	}
	
	public double evaluate(Collection<String> features){
		double result = 0;
		for(String feature: features){
			LikelihoodEntropy le = model.get(feature);
			if (le != null) {
				result +=le.getLogLikelihod();
			} else {
				result += Math.log(1.d/((double) (size + vocabularySize) ));
			}
		}
		return result;
	}
	
	public void read(BufferedReader reader) throws Exception{
	
		String line =null;
		while((line=reader.readLine())!=null){
			
			String[] tokens = line.split("\t");
			int sentiment = Integer.parseInt(tokens[0]);
			String term = tokens[1];
			double probability = Double.parseDouble(tokens[2]);
			double entropy = Double.parseDouble(tokens[3]);
			int occurrences  = Integer.parseInt(tokens[4]);

			LikelihoodEntropy le = new LikelihoodEntropy(term, probability, entropy, occurrences);
			model.put(term, le);
			
		}
	}
	
	public void write (String term, BufferedWriter writer) throws IOException{
		
		StringBuilder sb = new StringBuilder();
		
		LikelihoodEntropy le = model.get(term);
		if (le == null) return;
		
		sb.append(sentiment).append("\t").
			append(term).append("\t").
			append(le.getProbability()).append("\t").
			append(le.getEntropy()).append("\t").
			append(le.getOccurrences()).append("\n");
		
		writer.write(sb.toString());
	}
	
	public Set<String> getTerms(){
		return model.keySet();
	}
	
	public void write(BufferedWriter writer) throws IOException{
		
		List<String> ordered  = new ArrayList<String>();
		ordered.addAll(model.keySet());
		Collections.sort(ordered);
		for(String term: ordered){
			write(term,writer);
			writer.write("\n");
		}
	}
	
	public LikelihoodEntropy get(String term){
		return model.get(term);
	}
	
	public int getSize() {
		return size;
	}
	public int getVocabularySize() {
		return vocabularySize;
	}

	public void setVocabularySize(int vocabularySize) {
		this.vocabularySize = vocabularySize;
	}
}
