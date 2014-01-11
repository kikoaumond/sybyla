package sybyla.sentiment;

import org.apache.log4j.Logger;

import sybyla.bayes.BayesClassifier;
import sybyla.db.DBEngine;
import sybyla.nlp.NLPAnalyzer;
import sybyla.nlp.OpenNLPAnalyzer3;
import sybyla.nlp.PortugueseOpenNLPAnalyzer;

public class Analyzer {
	private static final Logger LOGGER = Logger.getLogger(Analyzer.class);
	private Model model;
	private NLPAnalyzer nlp;
	private static final Model PORTUGUESE_PRODUCT_MODEL=new Model(Model.PRODUCT_MODEL_PORTUGUESE);
	private static final BayesClassifier classifier = BayesClassifier.load();
	static{
		try {
			DBEngine.init();
		} catch (Exception e) {
			LOGGER.error("Error initializing DBEngine",e);
		}
	}
	
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
		double score = (nPositive+nNegative)/total;
		if (score > 0 && Math.abs(score) > 1){
			score=1;
		}
		if (score < 0 && Math.abs(score) > 1){
			score=-1;
		} 
		
		Result result  = classifier.evaluate(sentences);
		double r = result.getResult();
		if (r<0){
			r=-1;
		} else if (r>0){
			r=1;
		} else {
			r=0;
		}
		if (r==score){
			return new Result(r, 1.);
		} else {
			double certainty =  result.getCertainty();
			if (score == 0){
				if (certainty > 0.8){
					return result;
				} 
			} else {
				if (certainty > 0.99){
					return result;
				}
			}
			return new Result(score, 0.9);
			//} else {
			//	return result;
			//}
		}
	}
	
	public void insert(String customerKey, String text, int sentiment, String context){
		DBEngine.insertExample(customerKey, text, sentiment, context);
	}
}
