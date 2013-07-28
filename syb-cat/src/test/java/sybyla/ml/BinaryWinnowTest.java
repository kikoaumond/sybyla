package sybyla.ml;

import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.Before;

import sybyla.ml.BinaryWinnow.ByteArray;
import static org.junit.Assert.*;

import sybyla.nlp.OpenNLPAnalyzer;

public class BinaryWinnowTest {
    
    private static Logger LOGGER = Logger.getLogger(BinaryWinnowTest.class);
    
    private OpenNLPAnalyzer _nlp = new OpenNLPAnalyzer();
    private BinaryWinnow _musicWinnow = new BinaryWinnow("Music");
    private BinaryWinnow _politicsWinnow = new BinaryWinnow("Politics");
    private Map<String, Set<String>> _fileTerms = new HashMap<String, Set<String>>();
    private Map<String, Set<String>> _testFileTerms = new HashMap<String, Set<String>>();
    private Map<String, String> _fileLabels = new HashMap<String, String>();
    
    @Before
    public void setup() {
        load("/Bill_Clinton.txt", "Politics");
        load("/Al_Gore.txt", "Politics");
        load("/Bill_OReilly.txt","Politics");
        load("/US_Senate.txt","Politics");
        load("/Beethoven.txt","Music");
        load("/Ramones.txt","Music");
        load("/Orbital.txt","Music");
        _musicWinnow.set_updateCounts(false);
        _politicsWinnow.set_updateCounts(false);
    }
    
   @Test
    public void testCounts() {
        assertTrue(_musicWinnow.get_nPositiveExamples()==3);
        assertTrue(_musicWinnow.get_nNegativeExamples() == 4);
        assertTrue(_politicsWinnow.get_nPositiveExamples() == 4);
        assertTrue(_politicsWinnow.get_nNegativeExamples() == 3);
        
        Map<ByteArray,Integer> positiveOccurrences = _musicWinnow.get_positiveOccurrences();
        for(ByteArray term:positiveOccurrences.keySet()) {
            Integer occurrences =  positiveOccurrences.get(term);
            assertTrue(occurrences >= 0 && occurrences <= 3);
        }
        
        Map<ByteArray,Integer> negativeOccurrences = _musicWinnow.get_negativeOccurrences();
        for(ByteArray term:negativeOccurrences.keySet()) {
            String s = BinaryWinnow.toString(term);
            System.out.println(s);
            Integer occurrences =  negativeOccurrences.get(term);
            assertTrue(occurrences >= 0 && occurrences <= 4);
        }
        
        positiveOccurrences = _politicsWinnow.get_positiveOccurrences();
        for(ByteArray term:positiveOccurrences.keySet()) {
            String s = BinaryWinnow.toString(term);
            System.out.println(s);
            Integer occurrences =  positiveOccurrences.get(term);
            assertTrue(occurrences >= 0 && occurrences <= 4);
        }
        
       negativeOccurrences = _politicsWinnow.get_negativeOccurrences();
        for(ByteArray term:negativeOccurrences.keySet()) {
            String s = BinaryWinnow.toString(term);
            System.out.println(s);
            Integer occurrences =  negativeOccurrences.get(term);
            assertTrue(occurrences >= 0 && occurrences <= 3);
        }
    }

    public double test(String file, BinaryWinnow winnow) {
        Set<String> allTerms = _testFileTerms.get(file);
        if (allTerms == null) {
            InputStream is = BinaryWinnowTest.class.getResourceAsStream(file);
            Map<Short,List<String>> nounMap = _nlp.findAll(is);
            allTerms = new HashSet<String>();
            for (List<String> terms : nounMap.values()) {
                allTerms.addAll(terms);
            }
            _testFileTerms.put(file, allTerms);
        }
            
        return winnow.predict(allTerms);
    }
    
    private void load(String file, String label) {
        InputStream is = BinaryWinnowTest.class.getResourceAsStream(file);
        Map<Short,List<String>> nounMap = _nlp.findAll(is);
        Set<String> allTerms = new HashSet<String>();
        for (List<String> terms : nounMap.values()) {
            allTerms.addAll(terms);
        }
        _musicWinnow.train(label, allTerms);
        _politicsWinnow.train(label, allTerms);
        _fileTerms.put(file, allTerms);
        _fileLabels.put(file, label);
    }
    
    private void iterate() {
        for (String file :_fileTerms.keySet()) {
            Set<String> terms = _fileTerms.get(file);
            String label = _fileLabels.get(file);
            _musicWinnow.train(label, terms);
            _politicsWinnow.train(label, terms);
        }
    }
    
    private void computeScores(String file, String exepectedLabel) {
        Set<String> terms = _fileTerms.get(file);
        double musicScore = _musicWinnow.predict(terms);
        double politicsScore = _politicsWinnow.predict(terms);
        LOGGER.debug(file + " Music: " + musicScore + " Politics: " + politicsScore);
    }
    
    @Test
    public void testWinnow() {
        for(int i =0; i<5; i++) {
            int iter = i+1;
            LOGGER.debug("ITERATION "+iter);
            iterate();

            computeScores("/Bill_Clinton.txt", "Politics");
            computeScores("/Al_Gore.txt", "Politics");
            computeScores("/Bill_OReilly.txt","Politics");
            computeScores("/Beethoven.txt","Music");
            computeScores("/Ramones.txt","Music");
            computeScores("/Orbital.txt","Music");
            
            String file;
            Double p,m;
            
            LOGGER.debug("TESTING");
            
            file = "/Newt_Gingrich.txt";
            p = test(file, _politicsWinnow);
            m = test(file, _musicWinnow);
            
            LOGGER.debug(file + " Music: " + m + " Politics: " + p);
            
            file = "/US_House_of_Representatives.txt";
            p = test(file, _politicsWinnow);
            m = test(file, _musicWinnow);
            
            LOGGER.debug(file + " Music: " + m + " Politics: " + p);
            
            file = "/Bruce_Springsteen.txt";
            p = test(file, _politicsWinnow);
            m = test(file, _musicWinnow);
            
            LOGGER.debug(file + " Music: " + m + " Politics: " + p);
            
            file = "/Miles_Davis.txt";
            p = test(file, _politicsWinnow);
            m = test(file, _musicWinnow);
            
            LOGGER.debug(file + " Music: " + m + " Politics: " + p);

        }
       //LOGGER.debug(_musicWinnow.histogram());
       //LOGGER.debug(_politicsWinnow.histogram());
       
       _musicWinnow.pruneBySignificance(0.05);
       _politicsWinnow.pruneBySignificance(0.05);
       
       _musicWinnow.set_restrict(true);
       _politicsWinnow.set_restrict(true);
       
       for(int i =0; i<5; i++) {
           iterate();

           LOGGER.debug("ITERATION "+i);

           computeScores("/Bill_Clinton.txt", "Politics");
           computeScores("/Al_Gore.txt", "Politics");
           computeScores("/Bill_OReilly.txt","Politics");
           computeScores("/Beethoven.txt","Music");
           computeScores("/Ramones.txt","Music");
           computeScores("/Orbital.txt","Music");
           
           String file;
           Double p,m;
           
           LOGGER.debug("TESTING");
           
           file = "/Newt_Gingrich.txt";
           p = test(file, _politicsWinnow);
           m = test(file, _musicWinnow);
           
           LOGGER.debug(file + " Music: " + m + " Politics: " + p);
           
           file = "/US_House_of_Representatives.txt";
           p = test(file, _politicsWinnow);
           m = test(file, _musicWinnow);
           
           LOGGER.debug(file + " Music: " + m + " Politics: " + p);
           
           file = "/Bruce_Springsteen.txt";
           p = test(file, _politicsWinnow);
           m = test(file, _musicWinnow);
           
           LOGGER.debug(file + " Music: " + m + " Politics: " + p);
           
           file = "/Miles_Davis.txt";
           p = test(file, _politicsWinnow);
           m = test(file, _musicWinnow);
           
           LOGGER.debug(file + " Music: " + m + " Politics: " + p);
           
       }
       
       //LOGGER.debug(_musicWinnow.histogram());
       //LOGGER.debug(_politicsWinnow.histogram());
    }
}

