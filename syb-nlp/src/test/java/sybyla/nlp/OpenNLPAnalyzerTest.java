package sybyla.nlp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;

import static junit.framework.Assert.*;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import sybyla.nlp.Sequence;

public class OpenNLPAnalyzerTest {
    
    @Test
    public void testFindNamedEntities() {
        URL url = this.getClass().getResource("/Bill_Clinton.txt");
        InputStream is = OpenNLPAnalyzerTest.class.getResourceAsStream("/Bill_Clinton.txt");
        OpenNLPAnalyzer nlp = new OpenNLPAnalyzer();
        nlp.set_logParses(true);
        Map<Short,List<String>>terms = nlp.findAll(is);
        List<String> nounPhrases =  terms.get(OpenNLPAnalyzer.NOUN);
        for(String nounPhrase: nounPhrases){
            System.out.println(nounPhrase);
        }
        
    }
    
    @Test
    public void testPOS() {
        InputStream is = OpenNLPAnalyzerTest.class.getResourceAsStream("/bessemer.txt");
        OpenNLPAnalyzer2 nlp = new OpenNLPAnalyzer2();
        String[] sentences = nlp.detectSentences(is);
        	
        for(String sentence: sentences){
        	String[] tokens =  nlp.tokenize(sentence);
        	String[] tags =  nlp.tagPOS(tokens);
        	for (int i=0;i<tokens.length;i++){
        		System.out.print(tokens[i]+" ("+tags[i]+") ");
        	}
        	System.out.println();
        }

    }
    @Test
    public void testPOS2() {
        InputStream is = OpenNLPAnalyzerTest.class.getResourceAsStream("/pope.txt");
        OpenNLPAnalyzer2 nlp = new OpenNLPAnalyzer2();
        String[] sentences = nlp.detectSentences(is);
        	
        for(String sentence: sentences){
        	String[] tokens =  nlp.tokenize(sentence);
        	String[] tags =  nlp.tagPOS(tokens);
        	for (int i=0;i<tokens.length;i++){
        		System.out.print(tokens[i]+" ("+tags[i]+") ");
        	}
        	System.out.println();
        }
    }
    
    @Test
    public void testPOS5() {
        InputStream is = OpenNLPAnalyzerTest.class.getResourceAsStream("/osamaBinLaden.txt");
        OpenNLPAnalyzer2 nlp = new OpenNLPAnalyzer2();
        String[] sentences = nlp.detectSentences(is);
        	
        for(String sentence: sentences){
        	String[] tokens =  nlp.tokenize(sentence);
        	String[] tags =  nlp.tagPOS(tokens);
        	for (int i=0;i<tokens.length;i++){
        		System.out.print(tokens[i]+" ("+tags[i]+") ");
        	}
        	System.out.println();
        }
    }
    
    @Test
    public void testPOS3() {
        
        OpenNLPAnalyzer2 nlp = new OpenNLPAnalyzer2();	
    	String sentence =  "I like surfing";
    	String[] tokens =  nlp.tokenize(sentence);
        String[] tags =  nlp.tagPOS(tokens);
        for (int i=0;i<tokens.length;i++){
        	System.out.print(tokens[i]+" ("+tags[i]+") ");
        }
        System.out.println();
        
    }
    
    @Test
    public void testPOS4() {
        
        OpenNLPAnalyzer2 nlp = new OpenNLPAnalyzer2();	
    	String sentence =  "Jack the Ripper lived in London";
    	String[] tokens =  nlp.tokenize(sentence);
        String[] tags =  nlp.tagPOS(tokens);
        for (int i=0;i<tokens.length;i++){
        	System.out.print(tokens[i]+" ("+tags[i]+") ");
        }
        System.out.println();
        
    }
    
    @Test
    public void testAnalyze() {
        InputStream is = OpenNLPAnalyzerTest.class.getResourceAsStream("/bessemer.txt");
        OpenNLPAnalyzer2 nlp = new OpenNLPAnalyzer2();
        Map<Short,List<String>> all =nlp.findAll(is);

    }
    
    @Test
    public void testFindNamedEntities2() {
        InputStream is = OpenNLPAnalyzerTest.class.getResourceAsStream("/Bruce_Springsteen.txt");
        OpenNLPAnalyzer nlp = new OpenNLPAnalyzer();
        List<String> nouns = nlp.findNouns(is);
        for (String noun: nouns) {
            System.out.println(noun);
        }
        nlp.set_logParses(true);
        Map<Short,List<String>>terms = nlp.findAll(is);
        List<String> nounPhrases =  terms.get(OpenNLPAnalyzer.NOUN);
        for(String nounPhrase: nounPhrases){
            System.out.println(nounPhrase);
        }
        
    }
    
    @Test
    public void testParser1() {
        String text="Jenn and I saw And You Will Know Us by the Trail of Dead last night at the Warfield";
        OpenNLPAnalyzer nlp = new OpenNLPAnalyzer();
        nlp.findNounPhrases(text);
        nlp.findNamedEntities(text);
    }
    
    @Test
    public void testParser3() {
        String text="Hamilton and I watched the Try Again video last night";
        OpenNLPAnalyzer nlp = new OpenNLPAnalyzer();
        nlp.findNounPhrases(text);
        nlp.findNamedEntities(text);
    }

    
   @Test
    public void testParser2() {
        String text="Bill Clinton was the last president of the Cold War";
        OpenNLPAnalyzer nlp = new OpenNLPAnalyzer();
        nlp.findNounPhrases(text);
    }
   
   
   @Test
   public void testNounSequences() throws IOException {
       BufferedReader reader = new BufferedReader(new FileReader("src/test/resources/pope.txt"));
       StringBuilder sb = new StringBuilder();
       String line;
       while((line=reader.readLine())!=null){
    	   sb.append(line).append("\n");
       }
       String text = sb.toString();
       OpenNLPAnalyzer3 nlp = new OpenNLPAnalyzer3();
       Set<Sequence> nounSequences = nlp.findNounSequences(text);
       List<Sequence> l = new ArrayList<Sequence>(nounSequences);
       Collections.sort(l);
       System.out.println("-------SEQUENCES-------------");
       for (Sequence nounSequence: l){
    	   System.out.println(nounSequence.toString());
       }
       System.out.println("---------DICTIONARY---------");
       Map<String, Set<String>> dictionary = nlp.buildDictionary(nounSequences);
       List<String> ll = new ArrayList<String>(dictionary.keySet());
       Collections.sort(ll);
       for(String k:ll){
    	   System.out.print(k);
    	   Set<String> c = dictionary.get(k);
    	   System.out.print(" => ");
    	   for(String s: c){
    		   System.out.print("\t |"+s);
    	   }
    	   System.out.println();
       }
 }
    
    /**
     * Main entry point. The command-line arguments are concatenated together
     * (separated by spaces) and used as the word form to look up.
     */
    /*
    public static void main(String[] args) {
        String posModelFile =  DEFAULT_POS_MODEL;
        String fileLocation = System.getProperty(OPEN_NLP_POS_MODEL_KEY);
        
        if(fileLocation != null && !fileLocation.trim().equals("")) posModelFile = fileLocation;
        InputStream modelIn =  null;
        POSTaggerME tagger = null;
        
        try {
              modelIn = new FileInputStream(posModelFile);
              POSModel model = new POSModel(modelIn);
              tagger = new POSTaggerME(model);
            }
            catch (IOException e) {
                LOGGER.error("Could not load POS model file " + posModelFile, e);
                System.exit(1);
            }
            
            try {
              int nTerms=0;
              BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(args[0]))));
              String outputFile = args[0] + ".nouns";
              String notNounsFile = args[0] + ".notNouns";
              BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(outputFile))));
              BufferedWriter notNounWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(notNounsFile))));

        
              String term = reader.readLine();
        
              while (term != null) {
                  nTerms++;
                  String[] tokens = term.split("\\s+");
                  String[] tags = tagger.tag(tokens);
                  double[] likelihodds = tagger.probs();
                  boolean isNoun =  false;
                  for (String tag: tags) {
                      if (tag.startsWith("N")) {
                          isNoun = true;
                          break;
                      }
                  }
                  if (isNoun) {
                      nouns++;
                      writer.write(term + "\n");
                      writer.flush();
                  }
                  else {
                      nonNouns++;
                      notNounWriter.write(term + "\n");
                      notNounWriter.flush();
                  }
                  if (nTerms%10000==0) {
                      LOGGER.info(nTerms + " analyzed : " + nouns + " Nouns identified | Non-nouns: " + nonNouns );
                  }
                  term = reader.readLine();
              }
              LOGGER.info(nTerms + " analyzed : " + nouns + " Nouns identified | Non-nouns: " + nonNouns );
              reader.close();
              writer.close();
              modelIn.close();
        } catch (FileNotFoundException e) {
              LOGGER.error("Error reading file " + args[0],e);
        } catch (IOException e) {
             LOGGER.error("Error reading file " + args[0],e);
        }
    }       
    */
}
