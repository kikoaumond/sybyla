package sybyla.classifier;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import sybyla.ml.BinaryWinnow;
import sybyla.ml.BinaryWinnow.ByteArray;
import sybyla.ml.ModelTerm;
import sybyla.nlp.OpenNLPAnalyzer3;
import sybyla.classifier.TermExtractor;

public class Classifier {
    private static final Logger LOGGER = Logger.getLogger(Classifier.class);
    
    // Keys passed via system properties
    public static final String CATEGORY_MODELS_PATH_KEY="category.models.path";
        
    // Required parameters.
    public static final String URL_PARAM = "html.url";
     
    // Optional parameters
    public static final String FORMAT_PARAM = "response.format";
    public static final String DEBUG_PARAM = "response.debug";
   
    public static final String MODEL_INDEX_MAX_TOP_TERMS="model.index.max.top.terms";
    public static final String MODEL_INDEX_MAX_MODELS_PER_TERM="model.index.max.models.per.term";
    public static final String MODEL_CASE_SENSITIVE="model.case.sensitive";
    
    public static final String MAX_MODELS_EVALUATED="max.models.evaluated";
    public static final String MAX_MODELS_RETURNED="max.models.returned";
    public static final String MAX_TOP_MODEL_TERMS="max.top.model.terms";
    public static final String USE_INDEX_PARAM="use.index";
    public static final String USE_TERM_COUNTS_PARAM="use.term.counts";
    public static final String USER_ID_PARAM="user.id";
    public static final String USE_ENTITIES_ONLY="entities.index.loookup";
	private static final String PARSED_CONTENT_PARAM = "parsed.content";

    // Default values for configurable parameters
    //number of top terms in a model to be included in the index
    private static final int DEFAULT_MODEL_INDEX_MAX_TOP_TERMS = 100;
    //maximum number of models to be evaluated 
    private static final int DEFAULT_INDEX_MAX_MODELS = 200;
    //maximum number of top ranked terms in weight in a model to use when building the index
    private static final int DEFAULT_TOP_MODEL_TERMS_RETURNED = 200;
    //maximum number of high scoring models to be returned per text cluster
    private static final int DEFAULT_TOP_MODELS=3;
    //use the index or do a full scan of all models
    private static final boolean DEFAULT_USE_INDEX=true;
    //use the term counts when computing the model scores
    private static final boolean DEFAULT_USE_TERM_COUNTS=true;
    //are the category models case-sensitive?
    private static final boolean DEFAULT_CASE_SENSITIVE=false;
    //use entities only for categorization
    private static final boolean DEFAULT_USE_ENTITIES_ONLY=false;
    //Location of category models
    private static final String DEFAULT_CATEGORY_MODELS_LOCATION="/mnt/data/current/category-models/";
   
    private TermExtractor termExtractor= new TermExtractor();

	private boolean useEntitiesOnly= DEFAULT_USE_ENTITIES_ONLY;

	private boolean useTermCounts = DEFAULT_USE_TERM_COUNTS;

	private boolean useIndex = DEFAULT_USE_INDEX;

	private int nTopTerms = DEFAULT_TOP_MODEL_TERMS_RETURNED;

	private int nTopModelsReturned = DEFAULT_TOP_MODELS;

	private int nTopModelsIndexLookup = DEFAULT_INDEX_MAX_MODELS;
	
	private static int indexModelsPerTerm = DEFAULT_MODEL_INDEX_MAX_TOP_TERMS;

	private String url;
	private String text;   
	private static String categoryModelsPath;
	static{
        categoryModelsPath = System.getProperty(CATEGORY_MODELS_PATH_KEY, DEFAULT_CATEGORY_MODELS_LOCATION);
	}
	
    private static  Set<BinaryWinnow> categoryModels;
    private static Map<String, Set<BinaryWinnow>> termModelIndex;
    private static boolean isCaseSensitive = DEFAULT_CASE_SENSITIVE;
    
    public static void init() {
        
        indexModelsPerTerm = DEFAULT_MODEL_INDEX_MAX_TOP_TERMS;
        String modelsPerTermParam = System.getProperty(MODEL_INDEX_MAX_TOP_TERMS);
        if (modelsPerTermParam != null) {
            indexModelsPerTerm = Integer.parseInt(modelsPerTermParam);
        }
          
        String isCaseSensitiveKey =  System.getProperty(MODEL_CASE_SENSITIVE);
        if (isCaseSensitiveKey != null) {
            isCaseSensitive = Boolean.parseBoolean(isCaseSensitiveKey);
        }
        
        categoryModels = ModelLoader.loadCategoryModels(categoryModelsPath);

        indexModels(indexModelsPerTerm);
        
    }

    public List<Category> classify(String text)   {
                   
            List<Category> categories=getCategoryModelScores(text);
            return categories;
                           
    }
    
    	
	private static void indexModels(int nTopTerms) {
		
		termModelIndex = new HashMap<String, Set<BinaryWinnow>>();
	   
	   for (BinaryWinnow model: categoryModels) {
	       List<ModelTerm> topModelTerms = model.getTopTerms(nTopTerms);
	       for (ModelTerm modelTerm: topModelTerms) {
	          String term = modelTerm.get_term();
	          Set<BinaryWinnow> models = termModelIndex.get(term);
	          if (models == null) {
	              models = new HashSet<BinaryWinnow>();
	              termModelIndex.put(term, models);
	          }
	          models.add(model);
	       }
	   }
	   int nTerms = termModelIndex.keySet().size();
	   int nModels = 0;
	   for(String term: termModelIndex.keySet()) {
	       nModels += termModelIndex.get(term).size();
	   }
	   LOGGER.info(nTerms + " terms and " + nModels + " models loaded in index ");
   }
	
   private Set<BinaryWinnow> modelIndexLookup(int nModels, Set<String> terms) {
       
       Set<BinaryWinnow> topModels = new HashSet<BinaryWinnow>();
       Map<BinaryWinnow, Double> modelScores = new HashMap<BinaryWinnow, Double>();
             
       for(String term : terms) {
           if (term.length()<=1) continue;
           ByteArray termByteArray = BinaryWinnow.binarize(term);
           String t =  term;
           if(!BinaryWinnow.isCaseSensitve()) {
               t = term.toLowerCase();
           }
           Set<BinaryWinnow> models = termModelIndex.get(t);
           if (models !=null) {
               for(BinaryWinnow model: models)  {
                   double weight = model.getWeight(termByteArray);
                   if (weight > 0) {
                       if(modelScores.containsKey(model)) {
                           double s = modelScores.get(model);
                           modelScores.put(model, s+weight);
                       }
                       else {
                           modelScores.put(model, weight);
                       }
                   }
               }
           }
       }
       List<ModelIndexScore> modelIndexList = new ArrayList<ModelIndexScore>();
       for (BinaryWinnow model : modelScores.keySet()) {
           double score = modelScores.get(model);
           ModelIndexScore modelIndexScore = new ModelIndexScore(model,score);
           modelIndexList.add(modelIndexScore);
       }
       Collections.sort(modelIndexList, Collections.reverseOrder());
       
       int i=0;
       while(i<modelIndexList.size() && i<nModels) {
           topModels.add(modelIndexList.get(i).get_model());
           i++;
       }
       LOGGER.info("Total of "+ modelIndexList.size() + " considered in index");
       
       return topModels;
   }
   
   private Set<BinaryWinnow> modelIndexLookup(int nModels, Map<String, Integer> termCounts) {
       
       Set<BinaryWinnow> topModels = new HashSet<BinaryWinnow>();
       Map<BinaryWinnow, Double> modelScores = new HashMap<BinaryWinnow, Double>();
       Map<String, Set<String>> modelTerms =  new HashMap<String, Set<String>>();
             
       for(String term : termCounts.keySet()) {
           ByteArray termByteArray = BinaryWinnow.binarize(term);
           String t =  term;
           if(!BinaryWinnow.isCaseSensitve()) {
               t = term.toLowerCase();
           }
           Set<BinaryWinnow> models = termModelIndex.get(t);
           if (models !=null) {
               for(BinaryWinnow model: models)  {
                   double weight = model.getWeight(termByteArray);
                   int count = termCounts.get(term);
                   int n = models.size();
                   double specificity = 1/((double)(n));
                   if (weight > 0) {
                       if(modelScores.containsKey(model)) {
                           double score = modelScores.get(model);
                           modelScores.put(model, score + specificity);
                       }
                       else {
                           modelScores.put(model, specificity);
                       }
                   }
               }
           }
       }
       List<ModelIndexScore> modelIndexList = new ArrayList<ModelIndexScore>();
       for (BinaryWinnow model : modelScores.keySet()) {
           double score = modelScores.get(model);
           ModelIndexScore modelIndexScore = new ModelIndexScore(model,score);
           modelIndexList.add(modelIndexScore);
       }
       Collections.sort(modelIndexList, Collections.reverseOrder());
       
       int i=0;
       while(i<modelIndexList.size() && i<nModels) {
           topModels.add(modelIndexList.get(i).get_model());
           i++;
       }
       LOGGER.info("Total of "+ modelIndexList.size() + " considered in index");
       
       return topModels;
   }
   
   
   /**
    * returns the nTopScores category models with the highest scores 
    * @param doc
    * @param nTopScores
    * @return
 * @throws Exception 
    */
   private List<Category> getCategoryModelScores(String text) {
       
       
       if (text ==  null) return new ArrayList<Category>();
       
       List<Category> categoryResultList = new ArrayList<Category>();
       Set<String> terms = null;

       Map<String, Integer> termCounts=null;
       Set<String> entities =  null;
       Map<String, Integer> entitiesIndexTermCount = null;
           
       OpenNLPAnalyzer3 openNLPAnalyzer = new OpenNLPAnalyzer3();
           
       String[] sentences = openNLPAnalyzer.detectSentences(text);
       String[][] tokens = openNLPAnalyzer.tokenize(sentences);
               
           
        if (useTermCounts) {
               
        	termCounts = termExtractor.extractWithCounts(tokens, 5, BinaryWinnow.isCaseSensitve());
               
        } else {
               
        	terms = termExtractor.extract(tokens, 5, BinaryWinnow.isCaseSensitve());
           
        }
           
                      
        Set<BinaryWinnow> models = categoryModels;
        if (useIndex) {
               
        	if (useTermCounts) {
        		
        		Map<String,Integer> indexLookup = termCounts;
        		
        		models = modelIndexLookup(nTopModelsIndexLookup,indexLookup);
        		
        	} else {
        		if (useEntitiesOnly) {
        			terms = entities;
        		}
        		models = modelIndexLookup(nTopModelsIndexLookup, terms);
        	}
        }
           
        List<Category> scores = new ArrayList<Category>();
           
           Map<ByteArray, Integer> binaryTermCounts =  null;
           Set<ByteArray> binaryTerms = null;
           
           if (useTermCounts) {
              binaryTermCounts =  new HashMap<ByteArray,Integer>();
               for(String term: termCounts.keySet()) {
                   ByteArray binaryTerm = BinaryWinnow.binarize(term);
                   binaryTerm.set_term(term);
                   Integer count = termCounts.get(term);
                   binaryTermCounts.put(binaryTerm,count);
               }
           } else {
               binaryTerms = new HashSet<ByteArray>();
               for(String term: terms) {
                   ByteArray binaryTerm = BinaryWinnow.binarize(term);
                   binaryTerm.set_term(term);
                   binaryTerms.add(binaryTerm);
               }
           }
          
           for(BinaryWinnow winnow : models) {
               String category = winnow.get_label();
               double score = 0;
               
               if (useTermCounts) {   
                   score = winnow.predictBinaryWithCounts(binaryTermCounts);
                } else {
                   score = winnow.predictBinary(binaryTerms);
                }  
               
               String[] topTerms=null;
               try {
                   if (score >= winnow.get_threshold()) {
                       if (nTopTerms > 0) {
                           if (useTermCounts) {
                               topTerms = winnow.getTopBinaryTermListWithCounts(binaryTermCounts, nTopTerms);
                           } else {
                               topTerms = winnow.getTopBinaryTermList(binaryTerms, nTopTerms);
                           }  
                       }
                       Category scoredCategory = new Category(category,score);                      
                       //scoredCategory.getTermList().addAll(Arrays.asList(topTerms));
                       scores.add(scoredCategory);
                   }
                } catch (UnsupportedEncodingException e) {
                    LOGGER.error(e);
                }
           }
           
           if (scores.size()==0){
        	   Set<Category> entityCategories =  findCategoriesFromNamedEntities(openNLPAnalyzer, tokens);
        	   scores.addAll(entityCategories);
           }
           //compares scores in reverse order
           orderScoresInReverse(scores);
           
           int i = scores.size()-1;
           while( i > 0 && scores.size() > nTopModelsReturned) {
               scores.remove(i);
               i--;
           }
                 
           categoryResultList.addAll(scores);
        
       return categoryResultList;
   }
   
   
   private Set<Category> findCategoriesFromNamedEntities(OpenNLPAnalyzer3 nlp, 
		   												 String[][] tokens) {
	   Map<String, Category> map = new HashMap<String, Category>();
	   Set<Category> categories =  new HashSet<Category>();
	   Map<Short, List<String>> entities = nlp.findNamedEntities(tokens);
	   for(Short entityType: entities.keySet()){
		   List<String> e =  entities.get(entityType);
		   Set<Category> entityCategories = CategoryEntityMap.getCategories(e);
		   for (Category ec: entityCategories){
			   String categoryName =  ec.getName();
			   Category c =  map.get(categoryName);
			   if (c==null){
				   map.put(categoryName, ec);
			   } else {
				   c.setScore(c.getScore()+1);
			   }
		   }
	   }
	   categories.addAll(map.values());
	   return categories;
   }
   	
   /**
    * returns the nTopScores category models with the highest scores 
    * @param doc
    * @param nTopScores
    * @return
 * @throws Exception 
    */
   private List<Category> getCategoryModelScoresOriginal(String content, int nTopScores, 
                                                          int nModels, int nTopTerms, 
                                                          boolean useIndex, boolean useTermCounts, 
                                                          boolean useEntitiesOnly) throws Exception {
       
       if (content ==  null) return new ArrayList<Category>();
       
       
       List<Category> categoryResultList = new ArrayList<Category>();


           
           Map<Short, List<String>> termMap=null;
           Map<String, Integer> termCounts=null;
           Set<String> entities =  null;
           Map<String, Integer> entitiesIndexTermCount = null;
           OpenNLPAnalyzer3 openNLPAnalyzer = new OpenNLPAnalyzer3();
           if (openNLPAnalyzer == null) {
               throw new Exception("Could not obtain OpenNLPAnalyzer from pool");
           }
           
           
           termMap = openNLPAnalyzer.findAll(content);
                                     
           if (useEntitiesOnly) {
               entities = new HashSet<String>();
               List<String> people = termMap.get(OpenNLPAnalyzer3.PERSON);
               entities.addAll(people);
               List<String> locations = termMap.get(OpenNLPAnalyzer3.LOCATION);
               entities.addAll(locations);
               List<String> organizations = termMap.get(OpenNLPAnalyzer3.ORGANIZATION);
               entities.addAll(organizations);
           }
           
           if (useTermCounts) {
               termCounts =  getCounts(termMap, content);
               if (useEntitiesOnly) {
                   entitiesIndexTermCount = new HashMap<String, Integer>();
                   for(String entity: entities) {
                       Integer count = termCounts.get(entity);
                       entitiesIndexTermCount.put(entity, count);
                   }
               }
           } 
           
           Set<String> all = new HashSet<String>();
           if (useTermCounts) {
               all.addAll(termCounts.keySet());
           }else{
               for(List<String> termList : termMap.values()) {
                   all.addAll(termList);
               }
           }
           
           Set<BinaryWinnow> models = categoryModels;
           if (useIndex) {
               
               if (useTermCounts) {
                   Map<String,Integer> indexLookup = termCounts;
                   if (useEntitiesOnly) {
                       indexLookup = entitiesIndexTermCount;
                   }
                   models = modelIndexLookup(nModels,indexLookup);
               } else {
                   Set<String> terms = all;
                   if (useEntitiesOnly) {
                       terms = entities;
                   }
                   models = modelIndexLookup(nModels, terms);
               }
           }
           
           List<Category> scores = new ArrayList<Category>();
           
           Map<ByteArray, Integer> binaryTermCounts =  null;
           Set<ByteArray> binaryTerms = null;
           
           if (useTermCounts) {
              binaryTermCounts =  new HashMap<ByteArray,Integer>();
               for(String term: termCounts.keySet()) {
                   ByteArray binaryTerm = BinaryWinnow.binarize(term);
                   binaryTerm.set_term(term);
                   Integer count = termCounts.get(term);
                   binaryTermCounts.put(binaryTerm,count);
               }
           } else {
               binaryTerms = new HashSet<ByteArray>();
               for(String term: all) {
                   ByteArray binaryTerm = BinaryWinnow.binarize(term);
                   binaryTerm.set_term(term);
                   binaryTerms.add(binaryTerm);
               }
           }
          
           for(BinaryWinnow winnow : models) {
               String category = winnow.get_label();
               double score = 0;
               
               if (useTermCounts) {   
                   score = winnow.predictBinaryWithCounts(binaryTermCounts);
                } else {
                   score = winnow.predictBinary(binaryTerms);
                }  
               
               String[] topTerms=null;
               try {
                   if (score >= winnow.get_threshold()) {
                       if (nTopTerms > 0) {
                           if (useTermCounts) {
                               topTerms = winnow.getTopBinaryTermListWithCounts(binaryTermCounts, nTopTerms);
                           } else {
                               topTerms = winnow.getTopBinaryTermList(binaryTerms, nTopTerms);
                           }  
                       }
                       
                       Category scoredCategory = new Category(category, score);
                       
                       scores.add(scoredCategory);
                   }
                } catch (UnsupportedEncodingException e) {
                    LOGGER.error(e);
                }
           }
           
           //compares scores in reverse order
           orderScoresInReverse(scores);
           
           int i = scores.size()-1;
           while( i > 0 && scores.size() > nTopScores) {
               scores.remove(i);
               i--;
           }
           categoryResultList.addAll(scores);
           

       
       
       return categoryResultList;
   }
   
   protected static void orderScoresInReverse(List<Category> scores) {

         Collections.sort(scores,Collections.reverseOrder());
   }
	
   
   public  Map<String,Integer> getCounts(Map<Short,List<String>> termMap, String text) {       

       Map<String, Integer> termCounts = new HashMap<String, Integer>();
       Set<String> terms = new HashSet<String>();
       
       for (Short entityType: termMap.keySet()) {
           List<String> entityList = termMap.get(entityType);
           terms.addAll(entityList);
       }
       
       String trimmedSpaceText = text.trim().replaceAll(" +", " ");

       for(String term: terms){
           int count = countOccurrences(term, trimmedSpaceText);
           termCounts.put(term, count);
       }
       
        return termCounts;
       
   }
   
   public  Map<String,Integer> getCounts(Set<String> terms, String text) {       
       String trimmedSpaceText = text.replaceAll(" +", " ");
       Map<String, Integer> termCounts = new HashMap<String, Integer>();
       
       for(String term: terms){
           int count = countOccurrences(term, trimmedSpaceText);
           termCounts.put(term, count);
       }
       
        return termCounts;
       
   }
   
   public int countOccurrences( String term, String text){
       if ((term == null) || term.trim().equals("") || term.length()==0) return 0;
       int nOccurrences = 0;
       int fromIndex = 0;
       int idx = 0;
       do {
           idx = text.indexOf(term, fromIndex);
           if(idx >=0) {
               //String t = text.substring(idx,idx+term.length());
               nOccurrences++;
               fromIndex += idx + term.length();
           }
       } while(idx>=0 && fromIndex < text.length());
       return nOccurrences;
   }
   
   private void setParams(Map<String,String> params){
	   //maximum number of models to be returned by index lookup (per text cluster)
       nTopModelsIndexLookup = DEFAULT_INDEX_MAX_MODELS;
       String nTopModelsIndexLookupParam = params.get(MAX_MODELS_EVALUATED);
       
       if (nTopModelsIndexLookupParam !=null) {
           nTopModelsIndexLookup = Integer.parseInt(nTopModelsIndexLookupParam);
       }
       
       //number of high scoring models returned by request (per text cluster)
       nTopModelsReturned = DEFAULT_TOP_MODELS;
       String nTopModelsReturnedParam = params.get(MAX_MODELS_RETURNED);
       
       
       if (nTopModelsReturnedParam != null) {
           nTopModelsReturned = Integer.parseInt(nTopModelsReturnedParam.trim());
       }
       
       //number of high scoring terms in model to be returned (per model)
       nTopTerms = DEFAULT_TOP_MODEL_TERMS_RETURNED;
       String nTopTermsParam = params.get(MAX_TOP_MODEL_TERMS);
       
       
       if (nTopTermsParam != null) {
           nTopTerms = Integer.parseInt(nTopTermsParam.trim());
       }
       
       String url = params.get(URL_PARAM);
       
       LOGGER.debug("URL: "+url);
       
       useIndex = DEFAULT_USE_INDEX;
       String useIndexParam = params.get(USE_INDEX_PARAM);
       
       if (useIndexParam !=null) {
           useIndex = Boolean.parseBoolean(useIndexParam);
       }
       
       useTermCounts = DEFAULT_USE_TERM_COUNTS;
       String useTermCountsParam = params.get(USE_TERM_COUNTS_PARAM);

       
       if (useTermCountsParam != null) {
           useTermCounts = Boolean.parseBoolean(useTermCountsParam);
       }
       
       useEntitiesOnly = DEFAULT_USE_ENTITIES_ONLY;
       String useEntitiesOnlyParam = params.get(USE_ENTITIES_ONLY);
       
   
       if (useEntitiesOnlyParam != null) {
           useEntitiesOnly = Boolean.parseBoolean(useEntitiesOnlyParam);
       }
       
       text = params.get(PARSED_CONTENT_PARAM);
   }
   
   protected void classify(Map<String,String> params) throws Exception   {
         
	   setParams(params);      
  
                    
        if (text != null) {
           /* List<ScoredCategory> categories = getResult(url,content,
                                        nTopModelsReturned,nTopModelsIndexLookup,
                                        nTopTerms, useIndex, useTermCounts, useEntitiesOnly); //
       
        */
               List<Category> categories=getCategoryModelScores(text);

        }         
   }

   
   private class ModelIndexScore implements Comparable<ModelIndexScore>{
	    
	    private BinaryWinnow _model;
	    private double _indexScore;
	    private Set<String> terms;
	    
	    public ModelIndexScore(BinaryWinnow model, double score) {
	        _model = model;
	        _indexScore = score;
	    }
	    
	    @SuppressWarnings("unused")
        public ModelIndexScore(BinaryWinnow model) {
	        _model = model;
	    }
	    @Override
	    public String toString(){
	    	return _model.toString()+"->"+_indexScore;
	    }
	    @SuppressWarnings("unused")
        public double increment(double indexScoreIncrement) {
	        _indexScore += indexScoreIncrement;
	        return _indexScore;
	    }
	    
	    public BinaryWinnow get_model() {
	        return _model;
	    }

	    @SuppressWarnings("unused")
        public double get_indexScore() {
	        return _indexScore;
	    }

	    public int compareTo(ModelIndexScore t) {
	       if (this._indexScore > t._indexScore) {
	            return 1;
	       }
	       if (this._indexScore < t._indexScore) {
	           return -1;
	       }
	       
	       return 0;
	    }
	}
	
	public static class ModelScore {
	    
	    private double _relevance=0;
        private double _specificity=0;
        
	    public ModelScore(double relevance, double specificity) {
	        _relevance = relevance;
	        _specificity =  specificity;
	    }
	    
	    public void set_relevance(double _relevance) {
            this._relevance = _relevance;
        }

        public void set_specificity(double _specificity) {
            this._specificity = _specificity;
        }


	    
	    public double get_relevance() { return _relevance;}
	    
	    public double get_specificity() { return _specificity;}
	}
	
}
