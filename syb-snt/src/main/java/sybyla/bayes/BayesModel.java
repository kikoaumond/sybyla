package sybyla.bayes;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BayesModel {
	
	String name;
	Map<String, LikelihoodEntropy> model = new HashMap<String,LikelihoodEntropy>();
	
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
	
	public void read(String file) throws Exception{
	
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(file)),"UTF-8") );
		String line = reader.readLine();
		if (line ==  null) throw new Exception("Could not read bayes model at"+file);
		name =  line;
		while((line=reader.readLine())!=null){
			String[] tokens = line.split("\t");
			String term = tokens[0];
			double probability = Double.parseDouble(tokens[1]);
			double entropy = Double.parseDouble(tokens[2]);
			LikelihoodEntropy le = new LikelihoodEntropy(term, probability, entropy);
			model.put(term, le);
		}
		reader.close();
	}
	
	public void write(String file) throws IOException{
		
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(file)),"UTF-8") );
		writer.write(name+"\n");
		List<String> ordered  = new ArrayList<String>();
		ordered.addAll(model.keySet());
		Collections.sort(ordered);
		StringBuilder sb = new StringBuilder();
		for(String term: ordered){
			sb.delete(0, sb.length());
			LikelihoodEntropy le = model.get(term);
			sb.append(term).append("\t").append(le.getProbability()).append("\t").append(le.getEntropy()).append("\n");
			
			writer.write(sb.toString());
		}
		writer.close();
	}
}
