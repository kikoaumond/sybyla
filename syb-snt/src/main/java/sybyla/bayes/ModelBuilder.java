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
				File[] files = file.listFiles();
				classifier.train(files);
				for (File f: files){
					classifier.evaluateFile(f.getAbsolutePath());
				}
				
			} else {
				classifier.train(inputFile);
				classifier.evaluateFile(inputFile);
			}
			classifier.writeModel(outputFileName,3);
			BayesClassifier newClassifier = new BayesClassifier(Language.PORTUGUESE);

			newClassifier.loadModel(outputFileName);
			newClassifier.evaluateFile(inputFile);
			
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
			System.exit(-1);
		}
	}
	

}
