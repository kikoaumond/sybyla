package sybyla.sentiment;

import sybyla.bayes.BayesClassifier;
import sybyla.db.DBEngine;
import sybyla.nlp.NLPAnalyzer;
import sybyla.nlp.OpenNLPAnalyzer3;
import sybyla.nlp.PortugueseOpenNLPAnalyzer;

public class Analyzer {
	
	private Model model;
	private NLPAnalyzer nlp;
	private static final Model PORTUGUESE_PRODUCT_MODEL=new Model(Model.PRODUCT_MODEL_PORTUGUESE);
	private static final BayesClassifier classifier = BayesClassifier.load();
	
	public  Analyzer(Language language, Type type) throws Exception{
		
		if(language==Language.ENGLISH){
			
			if (type==Type.PRODUCT){
				model = new Model(Model.PRODUCT_MODEL_ENGLISH);
				nlp = new OpenNLPAnalyzer3();
			}
			
			if (type==Type.FINANCIAL){
				model = new Model(Model.FINANCIAL_MODEL_ENGLISH);
				nlp = new OpenNLPAnalyzer3();
			}
			return;
		} 
		
		if(language==Language.PORTUGUESE){
				
			if (type==Type.PRODUCT){
				model = PORTUGUESE_PRODUCT_MODEL;
				nlp = new PortugueseOpenNLPAnalyzer();
			}
				
			if (type==Type.FINANCIAL){
				model = new Model(Model.FINANCIAL_MODEL_PORTUGUESE);
				nlp = new PortugueseOpenNLPAnalyzer();
			}
			return;
		} 
		
		throw new Exception("Unrecognized model or language");
	}
	
	public Result analyze(String text){
		
		if (text == null || text.trim().equals("")){
			return new Result(Model.NEUTRAL, 1.0);
		}

		double nPositive = 0;
		double nNegative = 0;
		
		String[] sentences = nlp.detectSentences(text);
		if (sentences == null || sentences.length==0 ){
			return new Result(Model.NEUTRAL, 1.0);
		}
		
		
		for(int i=0; i< sentences.length; i++){
			double score = model.evaluate(sentences[i]);
			if (score > 0){
				nPositive += score;
			} else if (score < 0){
				nNegative += score;
			}
		}
		
		double total = (double) sentences.length;//Math.abs(nNegative) + Math.abs(nPositive);
		
		if (total == 0) {
			
			return new Result(Model.NEUTRAL, 1.0);
		}
		
		if (nPositive == 0 && nNegative == 0){
			Result result  = classifier.evaluate(sentences);
			return result;
		}
		
		double score = (nPositive+nNegative)/total;
		if (score > 0 && Math.abs(score) > 1){
			score=1;
		}
		if (score < 0 && Math.abs(score) > 1){
			score=-1;
		} 
		return new Result(score,0.9);
		
	}
	
	public void insert(String customerKey, String text, int sentiment, String context){
		DBEngine.insertExample(customerKey, text, sentiment, context);
	}
}
