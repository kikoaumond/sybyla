package sybyla.bayes;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class BayesModel {
	
	String name;
	Map<String, LikelihoodEntropy> model = new HashMap<String,LikelihoodEntropy>();
	
	private static class LikelihoodEntropy implements Comparable<LikelihoodEntropy> {
		
		String term;
		double logLikelihod;
		double entropy;
		
		public LikelihoodEntropy(String term, double probability, double entropy){
			this.term =  term;
			this.logLikelihod =  Math.log(probability);
			this.entropy=entropy;
		}

		@Override
		public int compareTo(LikelihoodEntropy le) {
			if (this.entropy < le.entropy){
				return -1;
			} else if (this.entropy > le.entropy){
				return 1;
			} else {
				if (this.logLikelihod > le.logLikelihod){
					
				}
				return 0;
			}
		}

		public String getTerm() {
			return term;
		}

		public double getLogLikelihod() {
			return logLikelihod;
		}

		public double getEntropy() {
			return entropy;
		}
	}
	
	public BayesModel(String name){
		this.name = name;
	}
	
	public void add(String term, double probability, double entropy){
		LikelihoodEntropy me = new LikelihoodEntropy(term, probability, entropy);
		model.put(term,me);
	}
	
	public double evaluate(Collection<String> features){
		double result = 0;
		for(String feature: features){
			LikelihoodEntropy le = model.get(feature);
			if (le == null) continue;
			result +=le.getLogLikelihod();
		}
		return result;
	}
	
	
}
