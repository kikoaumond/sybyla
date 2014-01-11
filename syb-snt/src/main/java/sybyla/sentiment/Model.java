package sybyla.sentiment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class Model {
	
	private static final Logger LOGGER =  Logger.getLogger(Model.class);
	
	private static final int COUNT=0;
	private static final int MAX=1;
	
	public static final double NEUTRAL=0;
	public static final double POSITIVE=1;
	public static final double NEGATIVE=-1;
	
	public static final	String SIG_POS="SIG_POS";
	public static final String SIG_NEG="SIG_NEG";
	public static final String SIG_NEUTRAL="SIG_NEUT";
	
	public static final String QUANT_POS="QUANT_POS";
	public static final String QUANT_NEG="QUANT_NEG";
	
	public static final String QUAL_POS="QUAL_POS";
	public static final String QUAL_NEG="QUAL_NEG";
	
	public static final String REV="REV";
	public static final String STR_NEG="STR_NEG";
	public static final String STR_POS="STR_POS";

	
	public static final String PHRASE_NEG="PHRASE_NEG";
	public static final String PHRASE_POS="PHRASE_POS";
	public static final String PHRASE_STR_NEG="PHRASE_STR_NEG";
	public static final String PHRASE_STR_POS="PHRASE_STR_POS";
	
	public static final String EXCLUDE="EXCLUDE";
	
	public static final Pattern MULTIPLE_SPACES_PATTERN =  Pattern.compile("([\\s]+)");
	public static final Pattern PUNCTUATION =  Pattern.compile("[,.;:!?-]");
	public static final Pattern PARENTHESES =  Pattern.compile("[()]");
    public static final Pattern QUOTES = Pattern.compile( "[\"“”'’‘]");

	public static final String PRODUCT_MODEL_PORTUGUESE="/product-portuguese-model.txt";
	public static final String FINANCIAL_MODEL_PORTUGUESE="/financial-portuguese-model.txt";
	public static final String FINANCIAL_MODEL_ENGLISH="/financial-english-model.txt";
	public static final String PRODUCT_MODEL_ENGLISH="product-english-model.txt";
	
	private static  Map<String, Set<String>> modelTerms =  new HashMap<String, Set<String>>();
	private static  Map<String, Set<String[]>> modelPhrases =  new HashMap<String, Set<String[]>>();

	public Model(String modelPath) {
		try {
			loadModel(modelPath);
		} catch (Exception e) {
			LOGGER.error("Error loading sentiment model "+modelPath, e);
		}
	}
	
	public static String normalize(String sentence){
		
		String s = PUNCTUATION.matcher(sentence).replaceAll(" ");
		s = QUOTES.matcher(s).replaceAll(" ");
		s = PARENTHESES.matcher(s).replaceAll(" ");
		s =  MULTIPLE_SPACES_PATTERN.matcher(s).replaceAll(" ");
		
		s =  s.toLowerCase().trim();
		
		return s;
	}
	
	private static boolean isAllowedLabel(String label){
		boolean b = QUAL_POS.equals(label) 
				    || QUAL_NEG.equals(label)
				    || QUANT_POS.equals(label) 
				    || QUANT_NEG.equals(label)
					|| SIG_POS.equals(label) 
					|| SIG_NEG.equals(label)
					|| SIG_NEUTRAL.equals(label)
					|| REV.equals(label)
					|| STR_NEG.equals(label)
					|| STR_POS.equals(label)
					|| PHRASE_NEG.equals(label)
					|| PHRASE_POS.equals(label)
					|| PHRASE_STR_NEG.equals(label)
					|| EXCLUDE.equals(label);
		return b;
	}
	
	private Map<String,Set<String>> loadModel(String modelPath) throws Exception{
		Map<String, Set<String>> model = modelTerms;
		InputStream is = Model.class.getResourceAsStream(modelPath);
		int n=0;
		LOGGER.info("Loading sentiment model file " + modelPath);
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String line = null;
		while((line=reader.readLine())!=null){
            if (line.trim().startsWith("#")){
            	continue;
            }
			String[] tokens = line.split("\\t");
			String label = tokens[0].trim();
                
			if (!isAllowedLabel(label)){
				throw new Exception("Unrecognized label in line "+line);
			}
                
			String term = tokens[1].toLowerCase().trim();
			
			if (label.equals(PHRASE_NEG) || label.equals(PHRASE_POS) || label.equals(PHRASE_STR_NEG)){
				String[] phrase = term.split("\\s");
				for(int i=0;i<phrase.length;i++){
					phrase[i] = " "+phrase[i].trim()+ " ";
				}
				Set<String[]> phr = modelPhrases.get(label);
				if (phr == null){
					phr = new HashSet<String[]>();
					modelPhrases.put(label, phr);
				}
				phr.add(phrase);
				n++;
				continue;
			}   
			
			if (term.startsWith("*")){
				term =  term.substring(1,term.length())+" ";
			} else if (term.endsWith("*")){
				term = " "+term.substring(0, term.length()-1).trim();
			} else {
				term = " "+term.trim()+" ";
			}
                
			Set<String> set = model.get(label);
                
			if (set == null){
				set =  new HashSet<String>();
				model.put(label,set);
			}
                
            set.add(term);
            n++;
                
		}
        reader.close();
       
		LOGGER.info("Loaded " + n + " sentiment markers from " + modelPath);
		return model;
	}
	
	public double evaluate(String sentence){
		
		String normalized = normalize(sentence);
		String[] tokens = tokenize(normalized);
		double[] scores = new double[tokens.length];
		
		for (int i=0; i< scores.length;i++){
			scores[i] = NEUTRAL;
		}
		
		Set<String> strongNegatives = modelTerms.get(STR_NEG);
		ScanResult sn = scan(tokens,strongNegatives);
		
		if(sn.size()>0){
			return 4*NEGATIVE;
		}
		
		ScanResult spn = scanPhrases(PHRASE_STR_NEG,tokens);	
		if (spn.size()>0){
			return 4*NEGATIVE;
		}
		
		ScanResult pn = scanPhrases(PHRASE_NEG,tokens);	
		if (pn.size()>0){
			return 1*NEGATIVE;
		}
		
		ScanResult pp = scanPhrases(PHRASE_POS,tokens);	
		if (pp.size()>0){
			return 1*POSITIVE;
		}
		
		Set<String> positiveSignifiers = modelTerms.get(SIG_POS);
		ScanResult posSig = scan(tokens, positiveSignifiers);
		
		Set<String> negativeSignifiers = modelTerms.get(SIG_NEG);
		ScanResult negSig = scan(tokens, negativeSignifiers);
		
		Set<String> positiveQuantifiers = modelTerms.get(QUANT_POS);
		ScanResult posQuant = scan(tokens, positiveQuantifiers);
		
		Set<String> negativeQuantifiers = modelTerms.get(QUANT_NEG);
		ScanResult negQuant = scan(tokens, negativeQuantifiers);
		
		Set<String>  positiveQualifiers = modelTerms.get(QUAL_POS);
		ScanResult posQual = scan(tokens, positiveQualifiers);
		
		Set<String> negativeQualifiers = modelTerms.get(QUAL_NEG);
		ScanResult negQual = scan(tokens, negativeQualifiers);
		
		Set<String> reversers = modelTerms.get(REV);
		ScanResult rev = scan (tokens, reversers);
		
		scores = evaluateQualifiers(POSITIVE, posQual, posQuant, negQuant, scores);
		scores = evaluateQualifiers(NEGATIVE, negQual, posQuant, negQuant, scores);
		scores = evaluateSignifiers(POSITIVE,  posSig, 
				 posQuant,  negQuant, 
				 posQual,  negQual, 
				 scores);
		scores = evaluateSignifiers(NEGATIVE,  negSig, 
				 posQuant,  negQuant, 
				 posQual,  negQual, 
				 scores);
		
		scores = evaluateReversers(rev, scores);

		double finalScore=score(scores);

		return finalScore;
	}
	
	private void updateScores(double sign, ScanResult r, double[] scores){
		List<Integer> positions = r.getPositions();
		for(int i=0; i<positions.size(); i++){
			int p = positions.get(i);
			scores[p]=sign;
		}
	}
	
	private double score(double[] scores){
		double s=0;
		double d=0;
		int n=0;
		for(int i=0; i<scores.length;i++){
			if (scores[i]!= NEUTRAL){
				s+=scores[i];
				n++;
				//s += scores[i]*i;
				//d += i;
			}
		}
		if (s==0){
			for(int j=scores.length-1; j>=0;j--){
				if(scores[j]!=NEUTRAL){
					return scores[j];
				}
			}
			return 0;
		}
		if(n==0){
			return 0;
		}
		return s/n;
	}
	
	private void computeReversers(ScanResult reversers, double[] scores){
		List<Integer> positions = reversers.getPositions();
		for(int i=0; i<scores.length; i++){
			if(scores[i]!=NEUTRAL){
				int p =  reversers.findClosestBefore(i, 3);
				if(p!=-1){
					scores[i]=-scores[i];
				}
			}
		}
	}
	
	private double[] evaluateQualifiers(double type, ScanResult qualifiers, ScanResult positiveQuantifiers, 
								    ScanResult negativeQuantifiers, double[] scores){
		
		List<Integer> qualifierPositions = qualifiers.getPositions();
		for(int i=0; i< qualifierPositions.size(); i++){
			int pos = qualifierPositions.get(i);
			scores[pos]=type;
		}
		for(int i=0;i<qualifierPositions.size();i++){
			int p = qualifierPositions.get(i);
			int pp = positiveQuantifiers.findClosest(p);
			int np = negativeQuantifiers.findClosest(p);
			int c =  closest(p,pp,np);
			if(c==+1){
				scores[p]=c*type;
			} else if (c==-1){
				scores[p]=c*type;
			}
		}
		return scores;
	}
	
	
	private double[] evaluateSignifiers(double type, ScanResult signifiers, double[] scores ){
		
		List<Integer> signifierPositions = signifiers.getPositions();
		double s = 0;
		for(int i=0; i< signifierPositions.size(); i++){
			int p = signifierPositions.get(i);
			for(int j = p-3; j<=p+3;j++){
				if (j<p||j>=scores.length){
					continue;
				}
				s+=scores[j]/((double)(Math.abs(p-j)));
			}
			s=s*type;
			if (s>0){
				scores[p]=POSITIVE;
			} else if(s<0){
				scores[p]=NEGATIVE;
			}
		}
		return scores;
	}
	
	private double[] evaluateReversers(ScanResult reversers, double[] scores ){
		
		List<Integer> reverserPositions = reversers.getPositions();
		double s = 0;
		for(int i=0; i< reverserPositions.size(); i++){
			int p = reverserPositions.get(i);
			for(int j = p; j<=p+4;j++){
				if (j<0||j>=scores.length){
					continue;
				}
				scores[j]=-scores[j];
			}
		}
		return scores;
	}
	
	private double evaluateWithQuantifiers(ScanResult positiveQuantifier,
										ScanResult negativeQuantifier,
										ScanResult positiveQualifier,
										ScanResult negativeQualifier,
										double[] scores){
		
		double score = NEUTRAL;
		List<Integer> positivePositions = positiveQualifier.getPositions();
		List<Integer> negativePositions = negativeQualifier.getPositions();
		

		for(Integer p:positivePositions) {
			int pp = positiveQuantifier.findClosest(p);
			int np = negativeQuantifier.findClosest(p);
			double a =  assign(POSITIVE, p,pp,np);
			
			score +=a;
		}
		
		for(Integer n: negativePositions){
			int pp = positiveQuantifier.findClosest(n);
			int np = negativeQuantifier.findClosest(n);
			double a =  assign(NEGATIVE,n,pp,np);
			score +=a;
		}
		
		return score;
		
	}
	
	
	private ScanResult scanPhrases(String type, String[] sentence){

		ScanResult sr = new ScanResult();
		Set<String[]> phrases = modelPhrases.get(type);
		for(int i=0;i<sentence.length;i++){
			for(String[] phrase: phrases){
				if (   phrase.length > sentence.length 
					|| i > sentence.length - phrase.length){
					continue;
				}
				int pos = findPhrase(sentence, i, phrase);
				if (pos!=-1){
					String term = toTerm(phrase);
					sr.add(term, pos);
				}
			}
		}
		
		return sr;
		
	}
	
	String toTerm(String[] phrase){
		
		if(phrase == null || phrase.length==0){
			return null;
		}
		StringBuilder sb = new StringBuilder();
		sb.append(phrase[0]);
		for(int i=1;i<phrase.length;i++){
			sb.append(" ").append(phrase[i]);
		}
		return sb.toString();
	}
	
	private int findPhrase(String[] sentence, int sentenceIndex, String[] phrase){
		
		if(    sentence  ==  null || phrase ==  null 
			|| sentenceIndex<0 || sentenceIndex>= sentence.length){
			return -1;
		}
		if (sentence.length<phrase.length){
			return -1;
		}
		if (sentenceIndex > sentence.length - phrase.length){
			return -1;
		}
		
		for(int j=0; j< phrase.length; j++){
			if (!phrase[j].equals(sentence[sentenceIndex+j])){
				return -1;
			}
		}
		
		return sentenceIndex;
	}
	
	private int closest(int reference, int pos, int neg){
		if (pos==-1 && neg ==-1){
			return 0;
		} 
		
		int distPos= -1;
		int distNeg	= -1;
		
		if (pos != -1){
			distPos =  Math.abs(pos - reference);
		}
		
		if (neg != -1){
			distNeg =  Math.abs(neg -  reference);
		}
		
		if(distPos==-1 && distNeg==-1){
			return 0;
		} else if (distPos > distNeg){
			return 1;
		} else if (distNeg > distPos){
			return -1;
		}
		return 0;
		
	}
	
	private double assign(double sign, int pos, int posQuant, int negQuant){
		
		int distPos = -1;
		int distNeg	= -1;
		
		if (posQuant != -1){
			distPos =  Math.abs(pos - posQuant);
		}
		
		if (negQuant != -1){
			distNeg =  Math.abs(pos -  negQuant);
		}
		
		if(distPos==-1 && distNeg==-1){
			return sign;
		} else if (distPos > distNeg){
			if (sign == POSITIVE) {
				return POSITIVE;
			}
			if (sign == NEGATIVE){
				return NEGATIVE;
			}
		} else if (distNeg > distPos){
			if (sign == POSITIVE){
				return NEGATIVE;
			}
			if (sign == NEGATIVE){
				return POSITIVE;
			}
		}
		return NEUTRAL;
	}
	
	private String[] tokenize(String sentence){
		
		String[] s =  sentence.split(" ");
		for(int i=0;i<s.length;i++){
			s[i]=" "+s[i].trim()+" ";
		}
		return s;
	}
	
	public ScanResult scan(String[] tokens, Set<String> model){
		
		ScanResult result = new ScanResult();
		
		for(int i=0; i<tokens.length; i++){
			for(String m:model){
				if (tokens[i].contains(m) ||
					tokens[i].startsWith(m) || 
					tokens[i].endsWith(m)){
					result.add(tokens[i], i);
				}
			}
		}
		return result;
	}
	
	private double[] evaluateSignifiers(double type, ScanResult signifiers, 
										ScanResult positiveQuantifiers, ScanResult negativeQuantifiers, 
										ScanResult positiveQualifiers, ScanResult negativeQualifiers, 
										double[] scores){
		
		List<Integer> qualifierPositions = signifiers.getPositions();
		for(int i=0; i< qualifierPositions.size(); i++){
			int pos = qualifierPositions.get(i);
			scores[pos]=type;
		}
		
		for(int i=0;i<qualifierPositions.size();i++){
			
			int p = qualifierPositions.get(i);
			int pQuant = positiveQuantifiers.findClosest(p);
			int nQuant = negativeQuantifiers.findClosest(p);
			int quant =  closest(p,pQuant,nQuant);
			int quantPosition=-1;
			
			if(quant>0){
				quantPosition = pQuant;
			} else if (quant<0){
				quantPosition = nQuant;
			}
			
			int pQual = positiveQualifiers.findClosest(p);
			int nQual = negativeQualifiers.findClosest(p);
			int qual =  closest(p,pQual,nQual);
			int qualPosition=-1;
			
			if (qual>0){
				qualPosition = pQual;
			} else if (qual < 0){
				qualPosition = nQual;
			}
			
			double distQuant = (double)(Math.abs(p-quantPosition));
			double distQual = (double)(Math.abs(p-qualPosition));
			double c = qual/distQual + quant/distQuant;
			c=c*type;
			
			if (c>0){
				scores[p]=1;
			} else if (c<0){
				scores[p]=-1;
			}
		}
		return scores;
	}

	private static class ScanResult{
		
		private List<String> markers=new ArrayList<String>();
		private List<Integer> positions = new ArrayList<Integer>();
		private int max=-1;
		private int defaultDistance=3;
		
		public void add(String marker, int position){
			if(marker == null || marker.trim().equals("")|| position < 0){
				return;
			}
			markers.add(marker);
			positions.add(position);
			if (position>max){
				max=position;
			}
		}
		
		public int findClosest(int position){
			
			return findClosest(position, defaultDistance);
		}
		
		public List<String> getMarkers(){
			return markers;
		}
		
		public List<Integer> getPositions(){
			return positions;
		}
		
		public int size(){
			return markers.size();
		}
		
		public int max(){
			return max;
		}
		
		public int findClosest(int position, int limit){
			int closest=-1;
			int minDistance = Integer.MAX_VALUE;
			for(int i=0; i<positions.size(); i++){
				int p = positions.get(i);
				int d = Math.abs(p-position);
				if (d < limit && d<minDistance){
					minDistance = d;
					closest=p;
				}
			}
			return closest;
		}
		
		public int findClosestBefore(int position, int limit){
			int closest=-1;
			int minDistance = Integer.MAX_VALUE;
			for(int i=0; i<positions.size(); i++){
				int p = positions.get(i);
				int d = position-p;
				if (d>0 && d < limit && d<minDistance){
					minDistance = d;
					closest=p;
				}
			}
			return closest;
		}
		
		public String toString(){
			StringBuilder sb = new StringBuilder();
			for(int i=0;i<markers.size();i++){
				sb.append(markers.get(i)).append(" : ").append(positions.get(i)).append("\n");
			}
			return sb.toString();
		}
	}
}
