package sybyla.nlp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import opennlp.tools.cmdline.parser.ParserTool;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.parser.AbstractBottomUpParser;
import opennlp.tools.parser.Parse;
import opennlp.tools.parser.Parser;
import opennlp.tools.parser.ParserFactory;
import opennlp.tools.parser.ParserModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;
import opennlp.tools.util.model.BaseModel;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

public class OpenNLPAnalyzer3 extends NLPAnalyzer{
	
    private static final Logger LOGGER = Logger.getLogger(OpenNLPAnalyzer3.class);
        
    public static final String OPEN_NLP_POS_MODEL_KEY = "sybyla.nlp.pos.model.file";
    public static final String DEFAULT_POS_MODEL = "/en-pos-maxent.bin";
    
    public static final String OPEN_NLP_SENTENCE_DETECTOR_MODEL_KEY = "sybyla.nlp.sentence.detector.model.file";
    public static final String DEFAULT_SENTENCE_DETECTOR_MODEL = "/en-sent.bin";
    
    public static final String OPEN_NLP_PARSER_MODEL_KEY = "sybyla.nlp.parser.model.file";
    public static final String DEFAULT_PARSER_MODEL = "/en-parser-chunking.bin";
    
    public static final String OPEN_NLP_PERSON_NAME_MODEL_KEY = "sybyla.nlp.person.name.model.file";
    public static final String DEFAULT_PERSON_NAME_MODEL = "/en-ner-person.bin";
    
    public static final String OPEN_NLP_ORGANIZATION_MODEL_KEY = "sybyla.nlp.organization.name.model.file";
    public static final String DEFAULT_ORGANIZATION_MODEL = "/en-ner-organization.bin";
    
    public static final String OPEN_NLP_LOCATION_MODEL_KEY ="sybyla.nlp.location.model.file";
    public static final String DEFAULT_LOCATION_MODEL = "/en-ner-location.bin";
    
    public static final String OPEN_NLP_TOKENIZER_MODEL_KEY ="sybyla.nlp.tokenizer.model.file";
    public static final String DEFAULT_TOKENIZER_MODEL = "/en-token.bin";
    
    public static final String CAPITALIZED_WORD_REGEX = "^[A-Z]+";
    public static final Pattern CAPITALIZED_WORD_PATTERN = Pattern.compile(CAPITALIZED_WORD_REGEX);
    public static final String WORD_REGEX = "[A-Za-z0-9]+[-]{0,1}";
    public static final Pattern UNMATCHED_INNER_QUOTE_PATTERN = Pattern.compile("[\\w|\\s]([“”\"]){1,1}[\\w|\\s]");
    
    


    public static final Pattern WORD_PATTERN = Pattern.compile(WORD_REGEX);
    
    public final static short NOUN_PHRASE = 0;
    public final static short PERSON = 1;
    public final static short LOCATION = 2;
    public final static short ORGANIZATION = 3;
    public final static short QUOTE = 4;
    public final static short NOUN = 5;
    
    private static final String[] NOUN_PHRASE_TAGS = {"NP"};
    private static final String PROPER_NOUN="NNP";
    private static final String PROPER_NOUN_PLURAL="NNPS";
    private static final String COMMON_NOUN="NN";
    private static final String COMMON_NOUN_PLURAL="NNS";
    private static final String FOREIGN_WORD="FW";    
    private static final String NOUN_PHRASE_TYPE="NP";


    private static final String SENTENCE_STOP="STOP";
    private static final String DOT=".";
    
    private static final String[] STARTS={"JJ","NN","NNS","NNP","NNPS","VBG", "FW"};
    
    private static final String[] ENDS={"NN","NNS","NNP","NNPS","VBG","FW"};
    
    private static final String[][] TRANSITIONS={{"NNP","NNP","NNPS","NN","NNS",".","CC","IN","DT"},
    											 {"NNPS","NNP","NNPS","NN","NNS",".","CC","IN","DT"},
    											 {"NN","NNP","NNPS","NN","NNS","CC","IN"},
    											 {"NNS","NNP","NNPS","NN","NNS","CC","IN"},
    											 {"DT","NNP", "FW"},
    											 {"FW","NN","NNS","NNP","NNPS"},
    											 {"CC","DT","NNP","NNPS"},
    											 {"IN","DT","NN","NNS","NNP","NNPS"},
    									  		 {".","NNP"},
    									  		 {"JJ","JJ","VBG","NN","FW"}};
    
    private static Set<String> starts =  new HashSet<String>();		
    private static Set<String> ends =  new HashSet<String>();								  		 

    private static Map<String,Set<String>> transitions= new HashMap<String, Set<String>>();
    
    static {
    	for(int i=0; i<TRANSITIONS.length;i++){
    		
    		Set<String> t = new HashSet<String>();
    		String[] rule = TRANSITIONS[i];
    		for(int j =1; j<rule.length;j++){
    			t.add(rule[j]);
    		}
    		transitions.put(rule[0], t);
    	}
    	
    	for(int i=0; i<STARTS.length;i++){
    		
    		starts.add(STARTS[i]);
    	}
    	
    	for(int i=0; i<ENDS.length;i++){
    		
    		ends.add(ENDS[i]);
    	}
    }
    
    private static final SentenceModel sentenceModel= (SentenceModel)setup(OPEN_NLP_SENTENCE_DETECTOR_MODEL_KEY,DEFAULT_SENTENCE_DETECTOR_MODEL);
    private  SentenceDetectorME _sentenceDetector = new SentenceDetectorME(sentenceModel);
    
    private static  final TokenizerModel tokenizerModel = (TokenizerModel)setup(OPEN_NLP_TOKENIZER_MODEL_KEY,DEFAULT_TOKENIZER_MODEL);
    private  TokenizerME _tokenizer = new TokenizerME(tokenizerModel);
    
    private static  final POSModel POSModel = (POSModel)setup(OPEN_NLP_POS_MODEL_KEY, DEFAULT_POS_MODEL);
    private  POSTaggerME _POSTagger = new POSTaggerME(POSModel);
    
    private static  ParserModel parserModel;
    private  Parser _parser;
    
    private static  final TokenNameFinderModel personNameModel = (TokenNameFinderModel)setup(OPEN_NLP_PERSON_NAME_MODEL_KEY, DEFAULT_PERSON_NAME_MODEL);
    private  NameFinderME _personNameFinder = new NameFinderME(personNameModel);
    
    private static final TokenNameFinderModel locationNameModel = (TokenNameFinderModel)setup(OPEN_NLP_LOCATION_MODEL_KEY, DEFAULT_LOCATION_MODEL);
    private  NameFinderME _locationNameFinder = new NameFinderME(locationNameModel);
    
    private static final  TokenNameFinderModel organizationNameModel = (TokenNameFinderModel)setup(OPEN_NLP_ORGANIZATION_MODEL_KEY, DEFAULT_ORGANIZATION_MODEL);
    private  NameFinderME _organizationNameFinder = new NameFinderME(organizationNameModel);
        
    private boolean _removeWikipediaCitationMarkup = true;
    private boolean _removeTrailingPunctuation = true;
    private boolean _doNotSplitPrepositionalPhrases = false;
    private boolean _removeUnmatchedQuotes = true;
    private boolean _removeApostrophes = true;
    private int _sizeLimit = 64;
    private boolean _available = true;
    private static final Pattern QUOTE_REGEX = Pattern.compile( "[\"“].*?[\"”]");
    public static final Pattern BAD_QUOTE_REGEX = Pattern.compile("([^\"\\s]){1,}([\"|\\(|\\)|\\[|||]|\\{|\\}]){1,1}([^\"\\s]){1,}");
    
    
    private boolean _logParses = false;
        
	/**
	 * loads models and initializes the appropriate tools, e.g. the Sentence Detector if the model is a sentence detector model, etc.
	 * @param model the model to be loaded
	 * @param modelKey the key for the model system property
	 * @param defaultModel the default file path for the model
	 */
	protected static BaseModel setup(String modelKey, String defaultModel) {
	    
	    String modelFileProperty = System.getProperty(modelKey);
	    String modelFile=defaultModel;
        if (modelFileProperty != null) {
            modelFile = modelFileProperty;
        }
        
        BaseModel model = null;
        
        LOGGER.info("Opening  model file " + modelFile);
        
        InputStream modelIn;
        modelIn = OpenNLPAnalyzer3.class.getResourceAsStream(modelFile);
        
        if (modelIn == null) throw new RuntimeException("Unable to load model "+ modelFile);
        
        try {
             if (modelKey.equals(OPEN_NLP_SENTENCE_DETECTOR_MODEL_KEY)) {
               model = new SentenceModel(modelIn);
         
            }
            else if (modelKey.equalsIgnoreCase(OPEN_NLP_TOKENIZER_MODEL_KEY)) {
              model = new TokenizerModel(modelIn);
              
           }
           else if (modelKey.equalsIgnoreCase(OPEN_NLP_PARSER_MODEL_KEY)) {
              model = new ParserModel(modelIn);
              
           }
           else if (modelKey.equals(OPEN_NLP_POS_MODEL_KEY)) {
              model = new POSModel(modelIn);
             
           }
           else if (   modelKey.equalsIgnoreCase(OPEN_NLP_PERSON_NAME_MODEL_KEY)
                    || modelKey.equalsIgnoreCase(OPEN_NLP_LOCATION_MODEL_KEY)
                    || modelKey.equalsIgnoreCase(OPEN_NLP_ORGANIZATION_MODEL_KEY)) {
                    
               model = new TokenNameFinderModel(modelIn);
                    
          }
       }
       catch (IOException e) {
          LOGGER.error("Error loading model file " + modelFile, e);
       }
       finally {
          if (modelIn != null) {
             try {
                modelIn.close();
             }
             catch (IOException e) {
                LOGGER.error("Error closing stream " + modelIn,e);
             }
          }
       }
       return model;
	}
	
	private Set<Sequence> findNounSequences(String[] sentence, String[] tags){
		
		int fullStop=0;
		//first, mark the end of the sentence
		if (tags.length>0 && tags[tags.length-1].equals(DOT)){
			tags[tags.length-1]=SENTENCE_STOP;
			fullStop=1;
		}
		
		Set<Sequence> sequences =  new HashSet<Sequence>();
		
		if(sentence.length - fullStop <= 3 ){
			return sequences;
		}

		int pos=0;
		EnglishSequence s = null;
		
		while (pos <= tags.length){
			
			if (pos == tags.length){
				if (s!=null && s.isValid()){
					sequences.add(s);
				}
				break;
			}
			
			if (s == null){
				if (isAllowedStart(tags[pos]) && isWord(sentence[pos])){
					s = new EnglishSequence(sentence[pos], tags[pos]);
				}
				pos++;
				continue;
			}
			
			if (isAllowedTransition(tags[pos-1],tags[pos]) && isWord(sentence[pos])){
				s = s.add(sentence[pos], tags[pos]);
			} else {
				
				if (s.isValid()){
					sequences.add(s);
				}
				
				Set<Sequence> exploded= new HashSet<Sequence>();
				
				Set<Sequence> e= s.explode();
				exploded.addAll(e);
				
				sequences.addAll(exploded);

				
				s =  null;
			}
			pos++;
		}
		
		return sequences;
	}
		
	private static boolean isAllowedStart(String pos){
		
		return starts.contains(pos);
	}
	
	private static boolean isAllowedEnd(String pos){
		
		return ends.contains(pos);
	}
	
	
	private static boolean isAllowedTransition(String pos1, String pos2){
		
		Set<String> t = transitions.get(pos1);
		if (t==null){
			return false;
		}
		if (t.contains(pos2)){
			return true;
		}
		return false;
	}
	
	private String streamToString(InputStream is) {
	    StringWriter writer = new StringWriter();
        try {
            IOUtils.copy(is,writer,"UTF-8");
        } catch (IOException e) {
            LOGGER.error("error reading input stream for sentence detection",e);
            return null;
        }
        String text = writer.toString();
        return text;
	}
	
	private String synonym(String s1, String s2){
		
		s1=" "+s1+" ";
		s2=" "+s2+" ";
		
		String synonym=s1.length()>s2.length()?" "+s1+" ":s2;
		String other=s1.length()>s2.length()?s2:s1;
		
		if (synonym.contains(other)){
			return synonym;
		}
		
		return null;
	}
	
	private String getSingular(String s1, String s2){
		
		String singular = s1;
		String plural = s2;
		
		if (s1.length() > s2.length()){
			String s =  singular;
			singular =plural;
			plural = s;
		}
		
		if (plural.equals(singular+"s")){
			return singular;
		}
		
		return null;
		
		
	}
	
	public String[] detectSentences(InputStream is) { 
	    String text = streamToString(is);
        return detectSentences(text);
	}
	
	public List<String>  findQuotes(String text) {
	    return findQuotes(text, true);
 	}
	
	public List<String>  findQuotes(String text, boolean dedupe) {
	    Matcher matcher = QUOTE_REGEX.matcher(text);
	    List<String> quotes = new ArrayList<String>();
	    while(matcher.find()) {
	        String quote = matcher.group();
	        quote = quote.substring(1, quote.length()-1);
	        //if (quote.length() <= _sizeLimit) {
	            quotes.add(matcher.group().replace("\"", "").trim());
	        //}
	    }
	    if (dedupe) {
	        return dedupe(quotes);
	    }
	    else {
	        return quotes;
	    }
	}
	
	public String[] detectSentences(String text) {
	   
	    String[] sentences;
	    sentences = _sentenceDetector.sentDetect(text);
	    
	    List<String> allSentences = new ArrayList<String>();
	    //OpenNLP does not handle newline characters, so we break sentences around those as well
	    for (String sentence: sentences) {
	        StringTokenizer tokenizer = new StringTokenizer(sentence,"\n");
	        while(tokenizer.hasMoreTokens()) {
	            String s = tokenizer.nextToken();
	            allSentences.add(s);
	        }
	    }
	    sentences = allSentences.toArray(new String[allSentences.size()]);
        return sentences;
	}
	
	public String[][] tokenize(String[] sentences) {
	     
	    String[][] tokens = new String[sentences.length][];
	    for(int i=0; i< sentences.length; i++) {
	        if (sentences[i]!=null) {
	            tokens[i] = _tokenizer.tokenize(sentences[i]);
	            
	        }
	    }
	    return tokens;
	}
	
	public String[] tokenize(String sentence) {
    
	    String [] tokens =  _tokenizer.tokenize(sentence);
	        
	    return tokens;
	}
	
	public void tagPOS(String[] tokens, String[] tags, double[] likelihoods) {
	    
	   tags = _POSTagger.tag(tokens);
	   likelihoods = _POSTagger.probs();
	}
	
	public String[] tagPOS(String[] tokens){
	    String[] tags = _POSTagger.tag(tokens);
	    return tags;
	}
	
	
	public String removeWikipediaCitationMarkup(String text)
	{
	    return text.replaceAll("\\[\\d+\\]", "");
	}
	
	public String removeTrailingPunctuation(String text) {
        if (_removeTrailingPunctuation) {
            return  text.replaceAll("[.,;:?!]*$", "");
        }
        return text;
	}
	
	public boolean isQuote(String text) {
	    return (   (text.startsWith("\"") && text.endsWith("\""))
	            || (text.startsWith("\'") && text.endsWith("\'")));
	}
	
	protected List<String> dedupe(List<String> terms) {
	    if ((terms==null) || (terms.size()==0)) return terms;
	    List<String> deduped = new ArrayList<String>(terms.size());
	    Collections.sort(terms);
	    deduped.add(terms.get(0));
	    for (String term: terms) {
	        if (!term.equals(deduped.get(deduped.size()-1))) {
	            deduped.add(term);
	        }
	    }
	    LOGGER.debug(terms.size() + " original terms, " + deduped.size() + " deduped terms");
	    return deduped;
	}
	
	public Set<String> getCapitalizedWordSequences(String text) {
	    Set<String> capitalizedWords = new HashSet<String>();
	    Matcher regexMatcher = CAPITALIZED_WORD_PATTERN.matcher(text);
	    while (regexMatcher.find()) {
	        String w = regexMatcher.group();
	        capitalizedWords.add(w);
	        // matched text: regexMatcher.group()
	        // match start: regexMatcher.start()
	        // match end: regexMatcher.end()
	    }
	    return capitalizedWords;

	}
	
	@Override
	public Set<Sequence>  findNounSequences(String text){
		
		String[] sentences = detectSentences(text);
		String[][] tokens = tokenize(sentences);
		
		Set<Sequence> nounSequences = new HashSet<Sequence>();
		
		for(int i=0; i<tokens.length; i++){
			
			String [] sentence = tokens[i];
			String[] tags = tagPOS(sentence);
			Set<Sequence>  sequences  = findNounSequences(sentence,tags);

			for(Sequence sequence: sequences){
					nounSequences.add(sequence);
			}
		}
		
		return nounSequences;
	}
	
	public Set<String>  findNounSequences(Set<EnglishSequence> sequences){
		
		Set<String> s = new HashSet<String>();
		for(EnglishSequence sequence: sequences){
			String sequenceString = sequence.toString();
			if (sequenceString != null){
				s.add(sequenceString);
			}
		}
		
		return s;
	}
	
	public Map<String,Set<String>> findNounSequences(String[][] tokens) {
	        
	        Map<String,Set<String>> nouns = new HashMap<String, Set<String>>();
	          
	        StringBuffer nounSequence = new StringBuffer();
	        Set<String> n = null;
	        String nounTag=null;
	        for (int i=0; i<tokens.length; i++) {
	            if (nounSequence.length() > 0) {
                    n.add(removeEnclosingCharacters(nounSequence.toString()));
                    nounSequence.delete(0, nounSequence.length());
                }
                
	            String[] tags;
	            
	            tags = _POSTagger.tag(tokens[i]);
	            
	            for (int j=0; j < tokens[i].length; j++) {
	                String tag = tags[j];
	                String word = tokens[i][j];
	                // String w = removeEnclosingCharacters(word);
	                String w = word;
	                
	                
	                boolean isNoun = isNoun(tag);
	                boolean isWord = isWord(w);
	                
	                
	                if (!isWord || !isSupportedCharacterSet(w) ) {
	                    
	                    if (nounSequence.length() > 0) {
	                        n.add(removeEnclosingCharacters(nounSequence.toString()));
	                        nounSequence.delete(0, nounSequence.length());
	                    }
	                    continue;
	                }
	                                
	                if (isNoun) {
	                    if (nounTag!=null && !nounTag.equals(tag)){
	                        
	                        if (nounSequence.length() > 0) {
	                            n =  nouns.get(nounTag);
	                            if (n == null) {
	                                n =  new HashSet<String>();
	                                nouns.put(tag, n);
	                            }
	                            n.add(removeEnclosingCharacters(nounSequence.toString()));
	                            nounSequence.delete(0, nounSequence.length());
	                        }
	                    }
	                    nounTag=tag;
	                    n =  nouns.get(nounTag);
	                    if (n == null) {
	                        n =  new HashSet<String>();
	                        nouns.put(tag, n);
	                    }
	                    
	                    if (nounSequence.length()>0) {
	                        nounSequence.append(" ");
	                    }
	                    nounSequence.append(w);
	                } else {
	                    if (nounSequence.length() > 0) {
	                        n.add(removeEnclosingCharacters(nounSequence.toString()));
	                        nounSequence.delete(0, nounSequence.length());
	                        nounTag=null;
	                        n=null;
	                    }
	                }
	                if(j==tokens[i].length-1 && nounSequence.length()>0) {
	                    n.add(removeEnclosingCharacters(nounSequence.toString()));
                        nounSequence.delete(0, nounSequence.length());
                        nounTag=null;
                        n=null;
	                }
	            }    
	        } 

	        return nouns;
	    }
	  
	public Set<String> consolidate(Map<Short, List<String>> entityMap) {
	    
	    Set<String> entities = new HashSet<String>();
	    Map<String, String> dictionary = new HashMap<String,String>();
	    for(Short entityType: entityMap.keySet()) {
	        List<String> entityList = entityMap.get(entityType);
	        for(String entity: entityList){
	            String[] tokens = entity.split("\\s");
	            for (String token: tokens){
	                dictionary.put(token, entity);
	            }
	        }
	    }
	    
	    for(Short entityType: entityMap.keySet()) {
            List<String> entityList = entityMap.get(entityType);
            for(String entity: entityList){
                String e = dictionary.get(entity);
                if(e == null) {
                    entities.add(entity);
                } else {
                    entities.add(e);
                }
            }
        }
	    return entities;
	}
	
	   public List<String> consolidate(List<String>nouns) {
	        
	        List<String> entities = new ArrayList<String>();
	        Map<String, List<String>> dictionary = new HashMap<String,List<String>>();
	        for(String noun: nouns){
	            String[] tokens = noun.split("\\s");
	            for (String token: tokens){
	                List<String> n = dictionary.get(token);
	                if (n==null) {
	                    n=new ArrayList<String>();
	                    dictionary.put(token, n);
	                }
	                n.add(noun);
	            }
	        }
	        
	        
	        for(String noun: nouns){
	            List<String> e = dictionary.get(noun);
	            if(e == null ) {
	                entities.add(noun);
	            } else {
	                entities.addAll(e);
	            }
	        }
	       
	        return entities;
	    }
	
	/**
	 * dedupes a slave list of terms by looking its terms up in a master list;
	 * if the master list contains the term, it is removed from the slave list.
	 * The master list may be sorted if necessary, but no elements are removed from it
	 * @param master - the master reference list
	 * @param slave - the slave list to be deduped
	 * @param sorted - specifies if the lists are already sorted; if they are not, lists
	 * @return the deduped list
	 */
	protected List<String> dedupe(List<String> master, List<String> slave, boolean sorted) {
	    if (master == null || master.size() == 0) return new ArrayList<String>(slave);
	    
	    if (!sorted) {
	        Collections.sort(master);
	        Collections.sort(slave);
	    }
	    
	    Iterator<String> j = master.iterator();
	    Iterator<String> i =slave.iterator() ;
	    
	    String m = j.next();
	    
	    Set<String> deduped = new HashSet<String>();
	    deduped.addAll(slave);
	    Iterator<String> k =  deduped.iterator();
	    
	    while (i.hasNext() && k.hasNext()) {
	        
	        String s = i.next();
	        k.next();
	        while (s.compareTo(m)>0 && j.hasNext()) {
	            m = j.next();
	        }
	        if(s.compareTo(m)==0) k.remove();
	    }
	    LOGGER.debug(slave.size() + " elements in original, " + deduped.size() + " elements after deduping against entities");
	    List<String> d =  new ArrayList<String>();
	    d.addAll(deduped);
	    return d;
	}

	public List<String> findNames(String[] tokens, short nameType) {
	    NameFinderME finder=null;
	    if (nameType ==  PERSON) {
	       /* if (_personNameFinder == null) {
	            synchronized(personNameModel) {
	                if (personNameModel == null) {
	                    personNameModel = (TokenNameFinderModel)setup(OPEN_NLP_PERSON_NAME_MODEL_KEY, DEFAULT_PERSON_NAME_MODEL);
	                }
	            }
	            _personNameFinder = new NameFinderME(personNameModel);
	        }*/
	        finder = _personNameFinder;
	    }
	    else if (nameType == LOCATION) {
	        /*if (_locationNameFinder == null) {
	            synchronized(locationNameModel) {
	                if(locationNameModel == null) {
	                    locationNameModel = (TokenNameFinderModel)setup(OPEN_NLP_LOCATION_MODEL_KEY, DEFAULT_LOCATION_MODEL);
	                }
	            }
	            _locationNameFinder = new NameFinderME(locationNameModel);
	        }*/
	        finder = _locationNameFinder;
	    }
	    else if (nameType == ORGANIZATION) {
	        /*if (_organizationNameFinder == null) {
	            synchronized(organizationNameModel) {
	                if (organizationNameModel == null) {
	                    organizationNameModel = (TokenNameFinderModel)setup(OPEN_NLP_ORGANIZATION_MODEL_KEY, DEFAULT_ORGANIZATION_MODEL);
	                }
	            }
	            _organizationNameFinder = new NameFinderME(organizationNameModel);
	        }*/
	        finder = _organizationNameFinder;
	    } else return null;
	    
	    Span[] nameIndexes;
	    
	    long t1= System.currentTimeMillis();
	    nameIndexes = finder.find(tokens);
	    double[] probs = finder.probs(nameIndexes);
	    
	    long t2 = System.currentTimeMillis();
	    LOGGER.debug(t2-t1+" ms calling OpenNLP to find named entities");
	    
	    List<String> names = new ArrayList<String>(nameIndexes.length);
	    t1 = System.currentTimeMillis();
	    StringBuffer sb = new StringBuffer();
	    
	    for (Span span: nameIndexes) {
	       sb.delete(0, sb.length());

	        int start = span.getStart();
	        int end = span.getEnd();
	        for(int i=start; i<end; i++) {
	            sb.append(tokens[i].trim());
	            if (i<end-1 && !tokens[i].startsWith("\'")) {
	                sb.append(" ");
	            }
	        }
	        String w = removeEnclosingCharacters(sb.toString());
	        if (isWord(w)) {
	           // w = Normalizer.normalize(w, Normalizer.Form.NFC);
	            names.add(w);
	        }
	    }
	    finder.clearAdaptiveData();
	    t2=System.currentTimeMillis();
	    LOGGER.debug(t2-t1+" spent processing OpenNLP output");
	    
	    return names;
	}
	
	public Map<Short,List<String>> findNamedEntities(InputStream in) { 

	    StringWriter writer = new StringWriter();
	    try {
	       IOUtils.copy(in,writer,"UTF-8");
	    } catch (IOException e) {
	       LOGGER.error("error reading input stream for sentence detection",e);
	       return null;
	    }
	    String text = writer.toString();
	    String[] sentences =  detectSentences(text);
	    String[][]tokens = tokenize(sentences);
	    return findNamedEntities(tokens);
	        
	}
	
	public List<String> findNouns(String text) {
	    String[] sentences = detectSentences(text);
	    String[][] tokens = tokenize(sentences);
	    return findNouns(tokens, true);
	}
	
	public List<String> findNouns(String[][] tokens, boolean dedupe) {
	    
	    List<String> nouns = new ArrayList<String>();
	    
	    boolean keepGoing = false;	
	    StringBuffer nounSequence = new StringBuffer();
	    for (int i=0; i<tokens.length; i++) {
	        String[] tags;
	        
	        tags = _POSTagger.tag(tokens[i]);
	        
	        for (int j=0; j < tokens[i].length; j++) {
	            String tag = tags[j];
	            String word = tokens[i][j];
	            // String w = removeEnclosingCharacters(word);
	            String w = word;
	            
	            boolean isNoun = isNoun(tag);
                boolean isWord = isWord(w);
                boolean isPossessive = isPossessive(tag);
	            
                if (!isWord || !isSupportedCharacterSet(w)) {
	                keepGoing = false;
	                if (nounSequence.length() > 0) {
	                    nouns.add(removeEnclosingCharacters(nounSequence.toString()));
	                    nounSequence.delete(0, nounSequence.length());
	                }
	                continue;
	            }
	            
	           // boolean isAdjectiveFollowedByNoun=false;
	            if (isAdjective(tag)) {
	                if(j+1<tags.length) {
	                    String nextTag=tags[j+1];
	                    if (isNoun(nextTag)) {
	                        //isAdjectiveFollowedByNoun=true;
	                        if (nounSequence.length() > 0) {
	                            nouns.add(removeEnclosingCharacters(nounSequence.toString()));
	                            nounSequence.delete(0, nounSequence.length());
	                        }
	                        nounSequence.append(w);
	                        keepGoing=true;
	                        continue;
	                    }
	                }
	            }
	            
	           
	            if (isNoun || isPossessive) {//(isNoun || isAdjectiveFollowedByNoun)) {
	                //String w = removeEnclosingCharacters(word);
	                if (!isPossessive) {
	                    nouns.add(w);
	                }
	                
	                if (!keepGoing) {
	                    keepGoing = true;
	                    nounSequence.append(w);
	                }
	                else {
	                    
	                    //if it's a "'s", do not append a space
	                    if (isPossessive) {
	                        nounSequence.append(w);
	                    } else {
	                        nouns.add(w);
	                        nounSequence.append(" "+w);
	                    }
	                    //w = removeEnclosingCharacters(nounSequence.toString());
	                    nouns.add(removeEnclosingCharacters(nounSequence.toString()));
	                    continue;
	                }
	            } else if (keepGoing && isConnective(tags,j)){
	                //String w = removeEnclosingCharacters(word);
	           
	                    nounSequence.append(" " + w);
	                    continue;
	                
	            } else {
	                keepGoing = false;
	                //String w = removeEnclosingCharacters(nounSequence.toString());
	                if (nounSequence.length() > 0) {
	                    nouns.add(removeEnclosingCharacters(nounSequence.toString()));
	                    nounSequence.delete(0, nounSequence.length());
	                }
	            }
	            //Let's not use gerund verbs for now.  We can uncomment this later
	            /*if (isGerundVerb(tag)) {
	                //String w = removeEnclosingCharacters(word);
	               
	                nouns.add(removeEnclosingCharacters(removeEnclosingCharacters(w)));
	                
	                if (j-1>=0) {
	                    if (isAdjective(tags[j-1])){
	                        w = removeEnclosingCharacters(_tokens[i][j-1]+" "+w);
	                        
	                        nouns.add(removeEnclosingCharacters(w));
	                    }
	                }
	                
	            }*/
	        }
	    }
	    if (dedupe) {
	        return dedupe(nouns);
	    } else {
	        return nouns;
	    }
	    
	}
	
	private boolean isGerundVerb(String tag) {
	    return tag.equals("VBG");
	}
	
	private boolean isConnective (String[] tags, int i) {
	    if (i < tags.length) {
	        if (isPreposition(tags[i])) {
	            if (i + 1 < tags.length) {
	                if (isNoun(tags[i+1]) || isArticle(tags[i+1])) {
	                    return true;
	                } 
	            }
	        } else {
	            if (i-1>=0) {
	               if (isPreposition(tags[i-1]) && isArticle(tags[i])) {
	                   return true;
	               }
	            }
	        }
	    }
	    return false;  
	}
	
	public List<String> findNounPhrase(InputStream is) {
	    String text = streamToString(is);
	    return findNounPhrases(text);
	}
	
	public List<String> findNounPhrases(String text) {
	    
	    List<String> nounPhrases = new ArrayList<String>();
	    
	    if (parserModel ==  null || _parser == null) {
	        parserModel = (ParserModel)setup(OPEN_NLP_PARSER_MODEL_KEY,DEFAULT_PARSER_MODEL);
	        
	        _parser = ParserFactory.create(parserModel);
	    }
	    String[] sentences = detectSentences(text);
	    for (String sentence: sentences) {
	        Parse[] parses = ParserTool.parseLine(sentence, _parser, 1);
	        for(Parse parse: parses) {
	           List<String> parseNounPhrases = filterNounPhrases(parse);
	           nounPhrases.addAll(parseNounPhrases);
	        }
	    }
	    nounPhrases =  dedupe(nounPhrases);
	    return nounPhrases;
	}
	
	private boolean isNounPhraseTag(String tag) {
	    int i = Arrays.binarySearch(NOUN_PHRASE_TAGS, tag);
	    return (i>=0) && (i<NOUN_PHRASE_TAGS.length) && tag.equals(NOUN_PHRASE_TAGS[i]);
	}
	
	public static boolean isWord(String word) {
		
		if (word.equals(".")){
			return true;
		}
		
		if(word.length()>1 ) {
			Matcher matcher = WORD_PATTERN.matcher(word);
			boolean find = matcher.find();
			return find;
		}
		
		return false;
	}
	
	private boolean isNoun(String tag) {
	  
	    return ( tag.startsWith("NN") || tag.equals("FW"));
	}
	
	
	
	private boolean isArticle(String tag) {
	    return tag.equals("DT");
	}
	
	private boolean isPossessive(String tag) {
        return tag.equals("POS");
    }
	
	private boolean isAdjective(String tag) {
	    return tag.equals("JJ");
	}
	
	private boolean isPreposition(String tag) {
	    return tag.equals("IN");
	}
	
	private Parse getFirstToken(Parse p) {
	    Parse[] children = p.getChildren();
	    //we stop when we reach the parent of a token
	    if (children != null && children.length > 0 && children[0].getType().equals(AbstractBottomUpParser.TOK_NODE))  return p;
	    return getFirstToken(children[0]);
	}
	
	private boolean isNounPhraseFollowedByPrepositionalPhrase(Parse p) {
	   if (p.getType().equals("NP")) {
	       Parse[] children =  p.getChildren();
	       
	       if (children == null || children.length != 2) return false;
	       
	       if (children[0].getType().equals("NP") && children[1].getType().equals("PP")) {
	           Parse[] grandchildren = children[1].getChildren();
	           
	           if (grandchildren ==  null || grandchildren.length != 2) return false;
	           if (grandchildren[0].getType().equals("IN") && grandchildren[1].getType().equals("NP")) {
	               return true;
	           }
	       }
	       return false;
	   }
	   return false;
	}
	

	public List<String> filterNounPhrases(Parse parse) {
	    List<String> nounPhrases =  new ArrayList<String>();
	    
	    if (parse.getType().equals(AbstractBottomUpParser.TOK_NODE)) return nounPhrases;
	    
	    Span span = parse.getSpan();
	    String type = parse.getType();
	    int start = span.getStart();
	    int end = span.getEnd();
	    boolean isNounPhrase = isNounPhraseTag(type);
	    //boolean splitPhrase = true;
	    if (isNounPhrase) {	        
	        //get rid of noun-phrases that are only a pronoun or determiner
	        Parse[] children = parse.getChildren();
	        if ((children.length == 1) && (children[0].getType().equals("PRP") || children[0].getType().equals("DT"))) {
	            return nounPhrases;
	        }
	        
	        Parse firstToken = getFirstToken(parse);
	        //get rid of leading articles and possessive pronouns in noun phrases
	        if ((firstToken.getType().equals("DT")) || (firstToken.getType().equals("PRP$"))) {
	           start = firstToken.getSpan().getEnd();  
	        }
	         
	        //if (_doNotSplitPrepositionalPhrases && isNounPhraseFollowedByPrepositionalPhrase(parse)) {
	        //    splitPhrase = false;
	        //}
	        
	        String text = parse.getText().substring(start, end).trim();  
	        text = removeUnmatchedQuotes(text);

	        text = removeTrailingPunctuation(text);
	        
	        text = removeApostrophes(text);
	        
	        text = removeEnclosingCharacters(text);
	        
	        text = text.trim();
	       
	        if (text.length()<=_sizeLimit) {	        
	            nounPhrases.add(text);
	            if (_logParses) {
	                StringBuffer sb = new StringBuffer();
	                parse.show(sb);
	                LOGGER.debug(sb);
	                LOGGER.debug(text);
	            }
	        }
	    }
	    
	    //if (splitPhrase) {
	        Parse[] children = parse.getChildren();
	        for (Parse child: children) {
	            if (!child.getType().equals(AbstractBottomUpParser.TOK_NODE)) {
	                List<String> childrenNounPhrases = filterNounPhrases(child);
	                nounPhrases.addAll(childrenNounPhrases);
	            }
	        }
	    //}
	    return nounPhrases;
	}
	

	
	public Map<Short,List<String>> findNamedEntities(String[][] tokens) {
	   
	   Map<Short,List<String>> names = new HashMap<Short,List<String>>();
	   
	   for(int i=0; i<tokens.length;i++) {
	       long t1 = System.currentTimeMillis();
	       List<String> people = findNames(tokens[i],PERSON);
	       long t2 = System.currentTimeMillis();
	       LOGGER.debug(t2-t1 +" ms  spent finding people");
	       
	       t1 = System.currentTimeMillis();
	       List<String> personNames = names.get(PERSON);
	       if (personNames == null) {
	           personNames = new ArrayList<String>();
	           names.put(PERSON, personNames);
	       }
	       
	       for(String s: people) {
	           s=s.trim();
	           if (!personNames.contains(s)) {
	               personNames.add(s);
	           }
	       }  
	       
	       t2 = System.currentTimeMillis();
           LOGGER.debug(t2-t1 +" ms  spent processing people's names");
	       
	       List<String> locations = findNames(tokens[i],LOCATION);
	       List<String> locationNames = names.get(LOCATION);
           if (locationNames == null) {
               locationNames = new ArrayList<String>();
               names.put(LOCATION, locationNames);
           }

           for(String s: locations) {
               s=s.trim();
               if (!locationNames.contains(s)) {
                   locationNames.add(s);
               }
           }	    
           
	       List<String> organizations = findNames(tokens[i], ORGANIZATION);
	       List<String> organizationNames = names.get(ORGANIZATION);
           if (organizationNames == null) {
               organizationNames = new ArrayList<String>();
               names.put(ORGANIZATION, organizationNames);
           }
           
           for(String s: organizations) {
               s=s.trim();
               if (!organizationNames.contains(s)) {
                   organizationNames.add(s);
               }
           }       
       }
	   return names;
	}
	
	public List<String> findNouns(InputStream is) {
	    String text = streamToString(is);
	    return findNouns(text);
	}
	
	public Map<Short, List<String>> findAll(InputStream is) {
	    String text = streamToString(is);
	    return findAll(text);
	}
	
	public  Map<Short,List<String>> findAll(String text){
	    long t1 =System.currentTimeMillis();
	    String[] sentences = detectSentences(text);
	    long t2 =System.currentTimeMillis();
	    LOGGER.debug(t2-t1 + " ms spent on sentence detection");
	    
        t1 =System.currentTimeMillis();
	    String[][] tokens = tokenize(sentences);
        t2 =System.currentTimeMillis();
        LOGGER.debug(t2-t1 + " ms spent on tokenization");

        t1 =System.currentTimeMillis();
	    Map<Short,List<String>> entities = findNamedEntities(tokens);
	    t2 =System.currentTimeMillis();
	    LOGGER.debug(t2-t1 + " ms spent on finding named entities");
	    
        t1 =System.currentTimeMillis();
	    List<String> quotes = findQuotes(text);
        t2 =System.currentTimeMillis();
        LOGGER.debug(t2-t1 + " ms spent on finding quotes");

	    entities.put(QUOTE, quotes);
	   // List<String> nounPhrases = findNounPhrases(text);
        t1 =System.currentTimeMillis();
	    List<String> nouns = findNouns(tokens, false);
	    t2 =System.currentTimeMillis();
	    LOGGER.debug(t2-t1 + " ms spent on finding nouns");
        t1 =System.currentTimeMillis();
	    for (Short entityType: entities.keySet()) {
	        List<String> entityList = entities.get(entityType);
	        Collections.sort(entityList);
	        nouns = dedupe(entityList,nouns, true);
	    }
        t2 =System.currentTimeMillis();
        LOGGER.debug(t2-t1 + " ms spent on deduping");

	    entities.put(NOUN,nouns);
	    
	    return entities;
	}

	private String removeEnclosingCharacters(String text) {

		String[] characters = {"*","(",")","\"","[","]"};
	    for(String character: characters) {
	        if (text.startsWith(character) && !text.endsWith(character)) {
                text= removeEnclosingCharacters(text.substring(1));
            }
            if (!text.startsWith(character) && text.endsWith(character)) {
                text= removeEnclosingCharacters(text.substring(0,text.length()-1));
            }
            if (text.startsWith(character) && !text.endsWith(character)) {
                text= removeEnclosingCharacters(text.substring(1));
            }
            if (!text.startsWith(character) && text.endsWith(character)) {
                text= removeEnclosingCharacters(text.substring(0,text.length()-1));
            }
	    }
	    return text;
	}
	public static boolean hasInnerUnmatchedQuotes(String text){
		boolean found = UNMATCHED_INNER_QUOTE_PATTERN.matcher(text).find();
		return found;
		
	}
	public static String removeUnmatchedQuotes(String text){
		
		if (text.startsWith("\"") && !text.endsWith("\"")) {
			return text.substring(1);
		}
		if (!text.startsWith("\"") && text.endsWith("\"")) {
			return text.substring(0,text.length()-1);
	    }
		if (text.startsWith("“") && !text.endsWith("”")) {
			return text.substring(1);
		}
		if (!text.startsWith("“") && text.endsWith("”")) {
			return text.substring(0,text.length()-1);
	    }
		
	    return text;
	}
	
	public static String removeQuotes(String text){
		
		if (text.startsWith("\"")) {
			text = text.substring(1);
		}
		
		if (text.endsWith("\"")) {
			text =  text.substring(0,text.length()-1);
	    }
		
		if (text.startsWith("“")) {
			text = text.substring(1);
		}
		
		if (text.endsWith("”")) {
			text =  text.substring(0,text.length()-1);
	    }
		
	    return text;
	}
	
	public static String removeApostrophes(String text) {
	   
	   if (text.endsWith("’s") || text.endsWith("'s")) {
           return text.substring(0,text.length()-2);
	   } else if (text.endsWith("'") || (text.endsWith("’"))){
		   return text.substring(0,text.length()-1);
	   }
	   return text;
	}
	
	public static Set<String> addApostrophe(String text) {
		   Set<String> a= new HashSet<String>();
		   if (text.endsWith("’s") || text.endsWith("'s") || text.endsWith("'") || (text.endsWith("’"))){
			   return a;
		   }
		   
		   if (text.endsWith("s")){
			   a.add(text+"'");
			   a.add(text+"’");
		   }else{
			   a.add(text+"'s");
			   a.add(text+"’s");
		   }
		  return a;
		}
		
	
	public static String makeSingular(String text) {
		   
		   if (text.endsWith("s") && !(text.endsWith("’s") || text.endsWith("'s"))) {
	           return text.substring(0,text.length()-1);
		   }
		   return null;
		}
	
    public boolean isRemoveWikipediaCitationMarkup() {
        return _removeWikipediaCitationMarkup;
    }

    public void setRemoveWikipediaCitationMarkup(boolean removeWikipediaCitationMarkup) {
        this._removeWikipediaCitationMarkup = removeWikipediaCitationMarkup;
    }

    public boolean is_logParses() {
        return _logParses;
    }

    public void set_logParses(boolean _logParses) {
        this._logParses = _logParses;
    }	
    
    public String normalize(String s) {
        return Normalizer.normalize(s, Normalizer.Form.NFC);
    }
    //we support UTF-8.  If a string contains UTF-16 characters that are not contained
    //in UTF-8 we flag it as unsupported so it can be removed from the term set
    public boolean isSupportedCharacterSet(String s) {
        //String sn = Normalizer.normalize(s, Normalizer.Form.NFC);
        
        try {
            String s8 = new String(s.getBytes("UTF-8"),"UTF-8");
            String s16 = new String(s.getBytes("UTF-16"),"UTF-16");
            if (s8.equals(s16)) {
                return true;
            }
            else {
                return false;
            }
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("Could not decode string "+s,e);
            return false;
        }
    }
    
    public synchronized boolean isAvailable() {
        return _available;
    }
    
    public synchronized void setAvailable(boolean b) {
        _available=b;
    }
    
    @Override
    public Map<String, Set<String>> buildDictionary(Set<Sequence> sequences){
    	
    	Map<String, Set<String>> dictionary =  new HashMap<String, Set<String>>();
    	
    	for(Sequence seq: sequences){
    		EnglishSequence sequence =  (EnglishSequence) seq;
    		Set<String> canonicals = sequence.canonicalize();

    		for (String canonical: canonicals){
    			String s =  sequence.toString();
    			if (s==null){
    				continue;
    			}
    			s =s.toLowerCase();
    			Set<String> c =  dictionary.get(s);
    			if (c == null){
    				c =  new HashSet<String>();
    				dictionary.put(s,c);
    			}
    			c.add(canonical);
    		}
    	}
    	
    	return dictionary;
    }
    

    
    public static class EnglishSequence extends Sequence {
    	
    	private List<String> tag= new ArrayList<String>();
    	private List<String> terms = new ArrayList<String>();
    	private String nounType=null;
    	
    	private EnglishSequence(){}
    	
    	public EnglishSequence(String term, String pos){
    		terms.add(term);
    		tag.add(pos);
    	}
    	    	
    	public EnglishSequence add(String term, String pos){
    		
    		EnglishSequence newSequence = new EnglishSequence();
    		
    		newSequence.tag.addAll(this.tag);
    		newSequence.terms.addAll(this.terms);
    		
    		newSequence.terms.add(term);
    		newSequence.tag.add(pos);
    		
    		return newSequence;
    	}
    	
    	public Set<Sequence> explode(){
    		
    		Set<Sequence> sequences =  new HashSet<Sequence>();
    		if (terms.size()==1){
    			return sequences;
    		}
    		EnglishSequence s=null;
    		
    		int i=0;
    		while(i<tag.size()) {
    			
    			String tagPOS = tag.get(i);
    			if (!isAllowedStart(tagPOS)){
    				i++;
    				continue;
    			}
    		
    			if (tagPOS.equals("NNP")) {
    				
    				s =  new EnglishSequence(terms.get(i), tag.get(i));
    				
    				while(    i < tag.size()-1 
    					  && (   tag.get(i+1).equals("NNP") 
    						  || tag.get(i+1).equals("NP")
    						  || tag.get(i+1).equals("."))){
    					i++;
    					s = s.add(terms.get(i), tag.get(i));
    				}
    				
    				sequences.add(s);
    				i++;
    				continue;
    			}
    			
    			s =  new EnglishSequence(terms.get(i), tag.get(i));
    			if (s.isValid()){
    				sequences.add(s);
    			}
    			
    			for(int j=i+1;j<tag.size(); j++){
    				if (tag.get(j).equals("NNP")){
    					break;
    				}
    				s = s.add(terms.get(j), tag.get(j));
    				if (s.isValid()){
    					sequences.add(s);
    				}
    			}
    			i++;
    		}
    		
    		return sequences;
    	}
    	
    	public String getNounType(){
    		
    		for(int i = 0; i<tag.size(); i++){
    			String t = tag.get(i);
    			if (nounType == null){
    				if (t.equals("NN") || t.equals("NNS")){
    					nounType = "NN";
        				continue;
    				} else if ( t.equals("NNP")|| t.equals("NNPS")){
    					nounType = "NNP";
        				continue;
    				}
    			}else{
    				if (   nounType.equals("NNP") 
    					&& (t.equals("NNP") || t.equals("NN") || t.equals("NNPS")|| t.equals("."))){
    					continue;
    				} else {
    					return NOUN_PHRASE_TYPE;
    				}
    			}
    		}
    		if (nounType == null){
    			nounType=NOUN_PHRASE_TYPE;
    		}
    		return nounType;
    	}
    	
    	public Set<String> canonicalize() {
    		
    		Set<String> canonical = new HashSet<String>();
    		String s =  toString();
    		
    		if (s==null){
    			return canonical;
    		}

    		
    		String type = getNounType();
    		if (type ==  null){
    			return canonical;
    		}
    		
    		s = s.toLowerCase();
    		if (   type.equals("NN")  || type.equals("NNS")
    			|| type.equals("NNP") || type.equals("NNPS")){
    			
    			if(type.equals("NNP")||type.equals("NNPS")){
    				String[] tokens = s.split("\\s");
    				if (tokens.length>1){
    					
    					String last =  tokens[tokens.length-1];
    					canonical.add(last);
    					Set<String> apostrophized = addApostrophe(last);
    					
    					for(String a: apostrophized){
    						canonical.add(a);
    					}
    					StringBuilder sb =  new StringBuilder(tokens[0]);
    					canonical.add(sb.toString());
    					for(int i=1; i<tokens.length-1; i++){
    						sb.append(" ").append(tokens[i]);
    						canonical.add(sb.toString());
    					}
    				}
    			}
    		}
    		
    		return canonical;
    	}
    	
    	public boolean isValid(){
    		
    		String start = tag.get(0);
    		
    		if (!isAllowedStart(start)){
    			return false;
    		}
    		
    		String end =  tag.get(tag.size()-1);
    		if (!isAllowedEnd(end)){
    			return false;
    		}
    		
    		for(int i=1;i<tag.size();i++){
    			
    			String tag1 = tag.get(i-1);
    			String tag2 = tag.get(i);
    			
    			if (!isAllowedTransition(tag1, tag2)){
    				return false;
    			}
    		}
    		
    		return true;
    	}
    	
    	
    	public String toString(){
    		
    		if (!isValid()){
    			return null;
    		}
    		
    		StringBuilder sb = new StringBuilder();
    		String space="";
    		
    		for(int i=0; i<terms.size();i++){
    			String term =  terms.get(i);
    			if (term.equals(".")){
    				space="";
    			}
    			sb.append(space).append(term);
    			space=" ";
    		}
    		
    		String t =  sb.toString();
    		t = removeQuotes(t);
    		if (BAD_QUOTE_REGEX.matcher(t).matches()){
    			return null;
    		}
    		
    		return t;
    	}
    	@Override
    	public boolean equals(Object o){
    		
    		if (!(o instanceof EnglishSequence)){
    			return false;
    		}
    		
    		EnglishSequence s= (EnglishSequence) o;
    		if (this.terms.size() != s.terms.size()){
    			return false;
    		}
    		
    		for(int i=0; i<this.terms.size(); i++){
    			
    			String t1 = this.terms.get(i);
    			String t2 = s.terms.get(i);
    			
    			if (!t1.equals(t2)) return false;

    		}
    		
    		return true;
    	}
    	
    	@Override
    	public int hashCode(){
    		String s =  toString();
    		if (s==null){
    			return "".hashCode();
    		}
    		return toString().hashCode();
    	}
    	
		@Override
		public int compareTo(Sequence s) {

			String s1=this.toString();
			String s2=s.toString();
			if (s1 ==  null){
				if (s2 == null){
					return 0;
				}
				return -1;
			}
			if (s2 == null){
				return 1;
			}
			
			int i =  s1.compareTo(s2);
			return i;
		}
    }
}
	

