package sybyla.sentiment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FeatureExtractor {
	
	private static  int neighborhood=1;
	
	public FeatureExtractor(int neighborhood){
		this.neighborhood =neighborhood;
	}
	
	public  List<String> extractFeatures(String sentence){
		String n =  Normalizer.normalize(sentence);
		List<String> features = new ArrayList<String>();
		String[] tokens = n.split("\\s");
		StringBuilder continuous = new StringBuilder();
		StringBuilder neighbors = new StringBuilder();
		for( int i=0; i< tokens.length; i++){
			
			continuous.delete(0, continuous.length());
			continuous.append(tokens[i]);
			features.add(continuous.toString());
			
			neighbors.delete(0, neighbors.length());
			neighbors.append(tokens[i]);
			int l = neighbors.length();
			
			for(int j = 1; (i+j < tokens.length && j <= neighborhood); j++){
			
				continuous.append(" ").append(tokens[i+j]);
				features.add(continuous.toString());
				
				if (j > 1){
				
					neighbors.append(" {");
					
					for(int k=1;k<j;k++){
						neighbors.append("_");
					}
					
					neighbors.append("} ").append(tokens[i+j]);
					features.add(neighbors.toString());
					neighbors.delete(l, neighbors.length());
				}
			}
		}
		return features;
	}
	
	public  Set<String> extractUniqueFeatures(String sentence){
		
		List<String> features = extractFeatures(sentence);
		Set<String> uniqueFeatures =  new HashSet<String>();
		uniqueFeatures.addAll(features);
		return uniqueFeatures;
		
	}
}
