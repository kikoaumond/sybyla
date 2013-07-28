package sybyla.nlp;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.model.BaseModel;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

public class PortugueseOpenNLPAnalyzer extends NLPAnalyzer {
    private static final Logger LOGGER = Logger.getLogger(PortugueseOpenNLPAnalyzer.class);
    
    public static final String OPEN_NLP_POS_MODEL_KEY = "sybyla.nlp.pos.model.file";
    public static final String OPEN_NLP_SENTENCE_DETECTOR_MODEL_KEY = "sybyla.nlp.sentence.detector.model.file";
    public static final String OPEN_NLP_TOKENIZER_MODEL_KEY ="sybyla.nlp.tokenizer.model.file";

    
    public static final String POS_MODEL = "/pt-pos-maxent.bin";
    
    public static final String SENTENCE_DETECTOR_MODEL = "/pt-sent.bin";
      
    public static final String TOKENIZER_MODEL = "/pt-token.bin";
    
    private static final SentenceModel sentenceModel= (SentenceModel)init(OPEN_NLP_SENTENCE_DETECTOR_MODEL_KEY,SENTENCE_DETECTOR_MODEL);
    private  SentenceDetectorME sentenceDetector = new SentenceDetectorME(sentenceModel);
    
    private static  final TokenizerModel tokenizerModel = (TokenizerModel)init(OPEN_NLP_TOKENIZER_MODEL_KEY,TOKENIZER_MODEL);
    private  TokenizerME tokenizer = new TokenizerME(tokenizerModel);
    
    private static  final POSModel POSModel = (POSModel)init(OPEN_NLP_POS_MODEL_KEY, POS_MODEL);
    private  POSTaggerME POSTagger = new POSTaggerME(POSModel);
    
    public static final String NOUN_PHRASE_TYPE="frase";
    public static final Pattern BAD_QUOTE_REGEX = Pattern.compile("([^\"\\s]){1,}(\"){1,1}([^\"\\s]){1,}");

    public static final String WORD_REGEX = "[A-Za-z0-9]+[-]{0,1}";
    public static final Pattern WORD_PATTERN = Pattern.compile(WORD_REGEX);
    
    private static final String[] STARTS={"n","prop"};
    
    private static final String[] ENDS={"n","prop","adj"};
    
    private static final String[][] TRANSITIONS={{"n","n","prop","adj","conj-c","prp","v-pcp"},
    											 {"prop","prop","n","adj","conj-c","prp","v-pcp"},
    											 {"adj", "n","prop", "v-pcp"},
    											 {"prp","adj","n","prop"},
    											 {"conj-c","n","prop"},
    											 {"v-pcp","adj", "n","prop"}};
    
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
	
    /**
	 * loads models and initializes the appropriate tools, e.g. the Sentence Detector if the model 
	 * is a sentence detector model, etc.
	 * @param model the model to be loaded
	 * @param modelKey the key for the model system property
	 * @param defaultModel the default file path for the model
	 */

	/**
	 * loads models and initializes the appropriate tools, e.g. the Sentence Detector if the model is a sentence detector model, etc.
	 * @param model the model to be loaded
	 * @param modelKey the key for the model system property
	 * @param defaultModel the default file path for the model
	 */
	protected static BaseModel init(String modelKey, String defaultModel) {
	    
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
	       
	       else if (modelKey.equals(OPEN_NLP_POS_MODEL_KEY)) {
	          model = new POSModel(modelIn);
	         
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

	public String[] detectSentences(String text) {
		
	    String[] sentences;
	    sentences = sentenceDetector.sentDetect(text);
	    
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
	
	public String[] detectSentences(InputStream is) { 
	    String text = streamToString(is);
	    return detectSentences(text);
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

	public String[] tagPOS(String[] tokens){
	    String[] tags = POSTagger.tag(tokens);
	    return tags;
	}

	public String[] tokenize(String sentence) {
	
	    String [] tokens =  tokenizer.tokenize(sentence);
	        
	    return tokens;
	}

	public String[][] tokenize(String[] sentences) {
	     
	    String[][] tokens = new String[sentences.length][];
	    for(int i=0; i< sentences.length; i++) {
	        if (sentences[i]!=null) {
	            tokens[i] = tokenizer.tokenize(sentences[i]);
	            
	        }
	    }
	    return tokens;
	}

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
	
	
	private Set<Sequence> findNounSequences(String[] sentence, String[] tags){
		
		Set<Sequence> sequences =  new HashSet<Sequence>();
		
		if(sentence.length  <= 3 ){
			return sequences;
		}
	
		int pos=0;
		PortugueseSequence s = null;
		
		while (pos <= tags.length){
			
			if (pos == tags.length){
				if (s!=null && s.isValid()){
					sequences.add(s);
				}
				break;
			}
			
			if (s == null){
				if (isAllowedStart(tags[pos]) && isWord(sentence[pos])){
					s = new PortugueseSequence(sentence[pos], tags[pos]);
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

	public Map<String, Set<String>> buildDictionary(Set<Sequence> sequences){
		
		Map<String, Set<String>> dictionary =  new HashMap<String, Set<String>>();
		
		for(Sequence seq: sequences){
			PortugueseSequence sequence = (PortugueseSequence) seq;
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

	private static boolean isAllowedEnd(String pos){
		
		return ends.contains(pos);
	}

	private static boolean isAllowedStart(String pos){
		
		return starts.contains(pos);
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

	public static class PortugueseSequence extends Sequence {
		
		private List<String> tag= new ArrayList<String>();
		private List<String> terms = new ArrayList<String>();
		private String nounType=null;
		
		private PortugueseSequence(){}
		
		public PortugueseSequence(String term, String pos){
			terms.add(term);
			tag.add(pos);
		}
		    	
		public PortugueseSequence add(String term, String pos){
			
			PortugueseSequence newSequence = new PortugueseSequence();
			
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
			PortugueseSequence s=null;
			
			int i=0;
			while(i<tag.size()) {
				
				String tagPOS = tag.get(i);
				if (!isAllowedStart(tagPOS)){
					i++;
					continue;
				}
			
				if (tagPOS.equals("prop")) {
					
					s =  new PortugueseSequence(terms.get(i), tag.get(i));
					
					while(    i < tag.size()-1 
						  && (   tag.get(i+1).equals("prop"))){
						i++;
						s = s.add(terms.get(i), tag.get(i));
					}
					
					sequences.add(s);
					i++;
					continue;
				}
				
				s =  new PortugueseSequence(terms.get(i), tag.get(i));
				if (s.isValid()){
					sequences.add(s);
				}
				
				for(int j=i+1;j<tag.size(); j++){
					if (tag.get(j).equals("prop")){
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
					if (t.equals("n")){
						nounType = "n";
	    				continue;
					} else if ( t.equals("prop")){
						nounType = "prop";
	    				continue;
					}
				}else{
					if (   nounType.equals("prop") 
						&& (t.equals("prop"))){
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

			s = s.toLowerCase();
			String type = getNounType();
			if (type ==  null){
				return canonical;
			}
			
			if (   type.equals("n")  || type.equals("prop")){
				
				if(type.equals("prop")){
					String[] tokens = s.split("\\s");
					if (tokens.length>1){
						
						String last =  tokens[tokens.length-1];
						canonical.add(last);
						
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
			
			if (!(o instanceof PortugueseSequence)){
				return false;
			}
			
			PortugueseSequence s= (PortugueseSequence) o;
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

