package sybyla.bayes;

import java.io.File;

import sybyla.sentiment.Language;

public class ModelBuilder {
	
	public static void main(String[] args){
		
		String inputFile = args[0];
		String outputFileName = args[1];
		File file = new File(inputFile);
		try {
			
			BayesClassifier classifier = new BayesClassifier(Language.PORTUGUESE);

			if (file.isDirectory()){
				String[] files = file.list();
				classifier.train(files);
			} else {
				classifier.train(inputFile);
			}
			classifier.saveModels(outputFileName);
			
		} catch (Exception e) {
			System.out.println(e);
			System.exit(-1);
		}
	}
	

}
