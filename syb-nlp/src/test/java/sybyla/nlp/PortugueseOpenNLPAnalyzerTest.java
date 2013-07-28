package sybyla.nlp;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import sybyla.nlp.PortugueseOpenNLPAnalyzer.PortugueseSequence;

public class PortugueseOpenNLPAnalyzerTest {

    @Test
    public void testPOS() {
        InputStream is = OpenNLPAnalyzerTest.class.getResourceAsStream("/folha.txt");
        PortugueseOpenNLPAnalyzer nlp = new PortugueseOpenNLPAnalyzer();
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
    public void testNounSequences() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("src/test/resources/folha.txt"));
        StringBuilder sb = new StringBuilder();
        String line;
        while((line=reader.readLine())!=null){
     	   sb.append(line).append("\n");
        }
        String text = sb.toString();
        PortugueseOpenNLPAnalyzer nlp = new PortugueseOpenNLPAnalyzer();
        Set<Sequence> nounSequences = nlp.findNounSequences(text);
        List<Sequence> l = new ArrayList<Sequence>(nounSequences);
        Collections.sort(l);
        System.out.println("-------SEQUENCES-------------");
        Set<String> expectedNouns = new HashSet<String>();
        expectedNouns.add("prefeito");
        expectedNouns.add("prefeito Fernando Haddad"); 
        expectedNouns.add("Fernando Haddad"); 
        expectedNouns.add("Coordenação de Subprefeituras"); 
        expectedNouns.add("cabos eleitorais"); 
        expectedNouns.add("Alain Fresnot"); 
        expectedNouns.add("Ferreira Gullar"); 
        expectedNouns.add("JOELMIR TAVARES"); 
        expectedNouns.add("LÍGIA MESQUITA"); 





        for (Sequence nounSequence: l){
     	   System.out.println(nounSequence.toString());
     	   expectedNouns.remove(nounSequence.toString());
        }
        assertTrue(expectedNouns.size()==0);
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
    
}
