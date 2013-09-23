package sybyla.bayes;


class LikelihoodEntropy implements Comparable<LikelihoodEntropy> {
	
	String term;
	double logLikelihod;
	double entropy;
	double probability;
	int occurrences;
	
	public LikelihoodEntropy(String term, double probability, double entropy, int occurrences){
		this.term =  term;
		this.probability =  probability;
		this.logLikelihod =  Math.log(probability);
		this.entropy=entropy;
		this.occurrences = occurrences;
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
	
	public double getProbability(){
		return probability;
	}

	public int getOccurrences() {
		return occurrences;
	}
}