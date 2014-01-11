package sybyla.bayes;

import java.io.File;

import sybyla.sentiment.Language;

public class ModelBuilder {
	
	public static void main(String[] args){
		
		String inputFile = args[0];
		String outputFileName = args[1];
		File file = new File(inputFile);
		File[] files=new File[0];
		try {
			
			BayesClassifier classifier = new BayesClassifier(Language.PORTUGUESE);

			if (file.isDirectory()){
				files = file.listFiles();
				classifier.train(files);
				for (File f: files){
					classifier.evaluateFile(f.getAbsolutePath());
				}
				
			} else {
				classifier.train(inputFile);
				classifier.evaluateFile(inputFile);
			}
			classifier.writeModel(outputFileName,5);
			BayesClassifier newClassifier = new BayesClassifier(Language.PORTUGUESE);

			newClassifier.loadModel(outputFileName);//-0.6731d);
			for (File f: files){
				newClassifier.evaluateFile(f.getCanonicalPath());
			}
			
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
			System.exit(-1);
		}
	}
	

}
