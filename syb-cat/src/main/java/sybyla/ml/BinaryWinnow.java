package sybyla.ml;

import java.io.UnsupportedEncodingException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import sybyla.classifier.Blacklist;
import sybyla.ml.Significance;
import sybyla.sort.Quick;

public class BinaryWinnow {
    
    private static Logger LOGGER = Logger.getLogger(BinaryWinnow.class);
	private static final Set<String> BLACKLIST = Blacklist.BLACKLIST;

    public String _label;
    //private Map<ByteArray,Double> _weights = new HashMap<ByteArray, Double>();
    private Map<ByteArray,WinnowTermEntry> _weights = new HashMap<ByteArray, WinnowTermEntry>();

   // private Map<ByteArray,Integer> _positiveOccurrences = new HashMap<ByteArray, Integer>();
    //private Map<ByteArray, Integer> _negativeOccurrences = new HashMap<ByteArray, Integer>();
    private Set<ByteArray> _restrictedTerms = new HashSet<ByteArray>();  //restricted terms are never pruned from the model
    private Set<Integer> _excludedTerms = new HashSet<Integer>();  //excluded terms are not added to the model
    private double _learningFactor=2;
    private double _threshold = 1.;
    private int _nHistogramBins = 100;
    private double _max;
    private double _min;
    private double[] _bins;
    private long[] _histogram;
    private int _nTerms;
    private boolean _restrictTerms = false;
    private int _nPositiveExamples=0;
    private int _nNegativeExamples=0;
    private boolean _updateCounts = true;
    private boolean _doNotTrain =  false;
    private long _nTermBytes=0;
    private long _nExcludedTermBytes=0;
    private static boolean isCaseSensitive=false;
     
    public BinaryWinnow(String label) {
        _label = label;
    }
    
    public BinaryWinnow(String label, Map<ByteArray,WinnowTermEntry> weights) {
        _label = label;
        _weights = weights;
    }
   
    public static ByteArray normalizeToByteArray(String s)  {
        String ns = Normalizer.normalize(s, Normalizer.Form.NFC);
        
        try {
            ByteArray b = new ByteArray(ns.getBytes("UTF-16BE"));
            //ByteArray b = new ByteArray(ns.getBytes("UTF-8"));

            return b;
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("Conversion from String to byte array error.  String: "+s,e);
        }
        return null;
    }
    
    
    
    public static String toString(ByteArray b)  {
        String s;
        try {
            s = new String(b._byteArray,"UTF-16BE");
            return s;
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("Conversion from byte array to String error.");
        }
        return null;
    }
    
    public double getWeight(String s) {
        ByteArray b = binarize(s);
        return getWeight(b);
    }
    
    public double getWeight(ByteArray b) {
        double weight = -1.d;
        WinnowTermEntry wte = _weights.get(b);
        if (wte == null) return weight;
        weight = wte.get_weight();
        return weight;
    }
    
    public static Set<ByteArray> binarize(Collection<String> terms) {
        Set<ByteArray> binaryTerms = new HashSet<ByteArray>();
        for (String term : terms) {
            ByteArray binaryTerm;
            if (isCaseSensitive) {
                binaryTerm = normalizeToByteArray(term);
            } else {
                binaryTerm = normalizeToByteArray(term.toLowerCase());
            }
            binaryTerms.add(binaryTerm);
        }
        return binaryTerms;
    } 
    
    public static ByteArray binarize(String term) {
        ByteArray binaryTerm;
        if (isCaseSensitive) {
            binaryTerm= normalizeToByteArray(term);
        } else {
            binaryTerm = normalizeToByteArray(term.toLowerCase());
        }
         
        return binaryTerm;
    }
    
    public static BinaryWinnow load(String line) throws WinnowParseException, UnsupportedEncodingException{
    	return load(line,true);
    }
    /**
     * parses a line of text into a Winnow object
     * @throws UnsupportedEncodingException 
     *
     */
    public static BinaryWinnow load(String line, boolean useBlacklist) throws WinnowParseException, UnsupportedEncodingException{
        String[] terms = line.split("\t");
        if (terms == null || terms.length ==0 ) {
            throw new WinnowParseException(" string is empty");
        }
        
        String category = terms[0];
        BinaryWinnow winnow = new BinaryWinnow(category);
        
        int nPositiveExamples = Integer.parseInt(terms[1]);
        winnow._nPositiveExamples = nPositiveExamples;
        
        int nNegativeExamples = Integer.parseInt(terms[2]);
        winnow._nNegativeExamples = nNegativeExamples;
                
        for (int i=0; i< (terms.length-3)/2; i++) {
            String term = terms[3 + 2*i];
            //do not load single character terms
            if (term.trim().length()<=1) continue;
            if (!isCaseSensitive) {
                term = term.toLowerCase();
            }
            if (useBlacklist && BLACKLIST.contains(term)){
            	continue;
            }
            ByteArray binaryTerm = normalizeToByteArray(term); 
            try{
            	Double weight = Double.parseDouble(terms[3 + 2*i + 1]);
            	winnow._weights.put(binaryTerm, new WinnowTermEntry(weight));
                winnow._nTermBytes +=binaryTerm._byteArray.length;
            } catch(NumberFormatException e){
            	LOGGER.error("Error reading element "+term+ " "+terms[3 + 2*i + 1]);
            }
            
        }
        return winnow;
    }
    
    public String getTopTermsAsString(Collection<String> termList, int n) throws UnsupportedEncodingException {
        StringBuffer topTerms = new StringBuffer();
        List<CategoryModelScore> termWeightList = new ArrayList<CategoryModelScore>();
        for (String term : termList) {
            ByteArray binaryTerm = normalizeToByteArray(term);

            if (_weights.containsKey(binaryTerm)) {
                double weight = _weights.get(binaryTerm).get_weight();
                CategoryModelScore  termWeight = new CategoryModelScore(term, weight);
                termWeightList.add(termWeight);
            }
        }
        Collections.sort(termWeightList, Collections.reverseOrder());
        
        int i=0;
        while (i<termWeightList.size()  && i < n) {
            CategoryModelScore termWeight = termWeightList.get(i);
            topTerms.append(termWeight.get_category()+": "+termWeight.get_score()+" | ");
            i++;
        }
        return topTerms.toString();
    }
    
    public String[] getTopTermList(Collection<String> termList, int n) throws UnsupportedEncodingException {
       
        List<CategoryModelScore> termWeightList = new ArrayList<CategoryModelScore>();
        for (String term : termList) {
            ByteArray binaryTerm = normalizeToByteArray(term);

            if (_weights.containsKey(binaryTerm)) {
                double weight = _weights.get(binaryTerm).get_weight();
                CategoryModelScore  termWeight = new CategoryModelScore(term, weight);
                termWeightList.add(termWeight);
            }
        }
        Collections.sort(termWeightList, Collections.reverseOrder());
        int size = termWeightList.size()<n?termWeightList.size():n;
        String[] topTerms = new String[size];
        
        int i=0;
        while (i<termWeightList.size()  && i < n) {
            CategoryModelScore termWeight = termWeightList.get(i);
            topTerms[i] =  termWeight.get_category();
            i++;
        }
        return topTerms;
    }
    
    public String[] getTopBinaryTermList(Collection<ByteArray> binaryTermList, int n) throws UnsupportedEncodingException {
        
        List<CategoryModelScore> termWeightList = new ArrayList<CategoryModelScore>();
        for (ByteArray binaryTerm : binaryTermList) {

            if (_weights.containsKey(binaryTerm)) {
                double weight = _weights.get(binaryTerm).get_weight();
                CategoryModelScore  termWeight = new CategoryModelScore(binaryTerm.get_term(), weight);
                termWeightList.add(termWeight);
            }
        }
        Collections.sort(termWeightList, Collections.reverseOrder());
        int size = termWeightList.size()<n?termWeightList.size():n;
        String[] topTerms = new String[size];
        
        int i=0;
        while (i<termWeightList.size()  && i < n) {
            CategoryModelScore termWeight = termWeightList.get(i);
            topTerms[i] =  termWeight.get_category();
            i++;
        }
        return topTerms;
    }
    
    public String[] getTopTerms(Collection<ByteArray> termList, int n) {
        
        List<CategoryModelScore> termWeightList = new ArrayList<CategoryModelScore>();
        for (ByteArray binaryTerm : termList) {
            if (_weights.containsKey(binaryTerm)) {
                double weight = _weights.get(binaryTerm).get_weight();
                CategoryModelScore  termWeight = new CategoryModelScore(binaryTerm.get_term(), weight);
                termWeightList.add(termWeight);
            }
        }
        Collections.sort(termWeightList, Collections.reverseOrder());
        int size = termWeightList.size()<n?termWeightList.size():n;
        String[] topTerms = new String[size];
        
        int i=0;
        while (i<termWeightList.size()  && i < n) {
            CategoryModelScore termWeight = termWeightList.get(i);
            topTerms[i] =  termWeight.get_category();
            i++;
        }
        return topTerms;
    }
    
    public String[] getTopTermListWithCounts(Map<String, Integer> termList, int n) throws UnsupportedEncodingException {
        
        List<CategoryModelScore> termWeightList = new ArrayList<CategoryModelScore>();
        for (String term : termList.keySet()) {
            ByteArray binaryTerm = normalizeToByteArray(term);

            if (_weights.containsKey(binaryTerm)) {
                double weight = _weights.get(binaryTerm).get_weight();
                int count = termList.get(term);
                double countWeight = weight*count;
                CategoryModelScore  termWeight = new CategoryModelScore(term, countWeight);
                termWeightList.add(termWeight);
            }
        }
        Collections.sort(termWeightList, Collections.reverseOrder());
        int size = termWeightList.size()<n?termWeightList.size():n;
        String[] topTerms = new String[size];
        
        int i=0;
        while (i<termWeightList.size()  && i < n) {
            CategoryModelScore termWeight = termWeightList.get(i);
            topTerms[i] =  termWeight.get_category();
            i++;
        }
        return topTerms;
    }
    
    public String[] getTopBinaryTermListWithCounts(Map<ByteArray, Integer> binaryTermCounts, int n) throws UnsupportedEncodingException {
        
        List<CategoryModelScore> termWeightList = new ArrayList<CategoryModelScore>();
        for (ByteArray binaryTerm : binaryTermCounts.keySet()) {

            if (_weights.containsKey(binaryTerm)) {
                double weight = _weights.get(binaryTerm).get_weight();
                int count = binaryTermCounts.get(binaryTerm);
                double countWeight = weight*count;
                CategoryModelScore  termWeight = new CategoryModelScore(binaryTerm.get_term(), countWeight);
                termWeightList.add(termWeight);
            }
        }
        Collections.sort(termWeightList, Collections.reverseOrder());
        int size = termWeightList.size()<n?termWeightList.size():n;
        String[] topTerms = new String[size];
        
        int i=0;
        while (i<termWeightList.size()  && i < n) {
            CategoryModelScore termWeight = termWeightList.get(i);
            topTerms[i] =  termWeight.get_category();
            i++;
        }
        return topTerms;
    }
    
    public List<ModelTerm> getTopTerms(int nTopTerms)  {
       
        List<ModelTerm> termList = new ArrayList<ModelTerm>();
        for(ByteArray binaryTerm: _weights.keySet()) {
            String term = BinaryWinnow.toString(binaryTerm);
            if (_weights.containsKey(binaryTerm)) {
                double weight = _weights.get(binaryTerm).get_weight();
                ModelTerm modelTerm = new ModelTerm(term, weight);
                termList.add(modelTerm);
            }
        }
        Collections.sort(termList, Collections.reverseOrder());
        
        List<ModelTerm>  topTermList = new ArrayList<ModelTerm>();
        int i=0;
        while (i<termList.size()  && i < nTopTerms) {
            topTermList.add(termList.get(i));
            i++;
        }
        
        return topTermList;
    }
    
    public double initialWeight(int nTerms) {
        if (nTerms == 0) return 1;
        double w = (1/((double)nTerms));
        return w;
    }
    
    public boolean containsLabel(List<String>trainingLabels) {
      Collections.sort(trainingLabels);
      int index = Collections.binarySearch(trainingLabels, _label);
      return (index >=0);
    }
    
    public void train(Set<String> trainingLabels, List<String> terms) {
        if (containsLabel(terms)) {
            train(_label,trainingLabels);
        }
        else {
            train("NOT-" + _label,trainingLabels);
        }
    }
    
    public void train(String trainingLabel, Set<String> terms)  {
        Set<Term> termSet = new HashSet<Term>();
        for (String term: terms) {
            Term t =  new Term(term);
            termSet.add(t);
        }
        train(termSet,trainingLabel);
    }

    public void train(String trainingLabel, Map<String, Double> terms)  {
        Set termSet = new HashSet<Term>();
        for (String term: terms.keySet()) {
            Double w = terms.get(term);
            if (w == null){
                w=1.0d;
            }
            if (w <= 0){
                continue;
            }
            Term t =  new Term(term,w);
            termSet.add(t);
        }
        train(termSet,trainingLabel);
    }

    public void train(String trainingLabel, Set<String> terms, int nTerms)  {
        
        if(_doNotTrain) return;
        Set<ByteArray> binaryTerms = binarize(terms);
        
        if (nTerms == 0) {
            nTerms = binaryTerms.size();
        }
        
        try{
            double sum = 0;
            for (ByteArray binaryTerm: binaryTerms) {
                if (_excludedTerms.contains(Arrays.hashCode(binaryTerm._byteArray))) {
                    continue;
                }
                if (!_weights.containsKey(binaryTerm) && !_restrictTerms) {
                    WinnowTermEntry newEntry =  new WinnowTermEntry(initialWeight(nTerms));
                    _weights.put(binaryTerm, newEntry);
                    _nTermBytes += binaryTerm._byteArray.length;
                }
                
                WinnowTermEntry w = _weights.get(binaryTerm);
                if (w == null) {
                   continue;
                }
                double weight = w.get_weight();
                
                sum += weight;
            }
        
            if ((sum >= _threshold) && (!trainingLabel.equals(_label))) {
                for (ByteArray binaryTerm: binaryTerms) {
                    WinnowTermEntry entry = _weights.get(binaryTerm);
                    if (entry!=null) {
                        double w = entry.get_weight();
                        double newWeight = w/_learningFactor;
                        entry.set_weight(newWeight);
                    }
                }
            }
        
            else if ((sum < _threshold) && (trainingLabel.equals(_label))) {
                for (ByteArray binaryTerm: binaryTerms) {
                    WinnowTermEntry entry = _weights.get(binaryTerm);
                    if (entry!=null) {
                        double w = entry.get_weight();
                        double newWeight = w*_learningFactor;
                        entry.set_weight(newWeight);
                    }
                }
            }
        
            if (_updateCounts) {
                updateTrainingExamples(trainingLabel);
                updateTermOccurrences(trainingLabel, binaryTerms);
            }
            _nTerms =  _weights.size();
        }
        catch (Exception e) {
            LOGGER.error("Error occurred in Winnow. model: " + _label + " size: " +_weights.size());
            LOGGER.error(e);
            e.printStackTrace();
        }
    }
    
    
    public void train(Set<Term> entries, String trainingLabel)  {
        
        if(_doNotTrain) return;
        
        int nTerms = entries.size();
        
        Set<ByteArray> binaryTerms = new HashSet<ByteArray>();
        double sumWeights = 0;

        for(Term entry:entries) {
            sumWeights += entry.get_pValue();
        }

        try{
            double sum = 0;
            for (Term entry: entries) {
                
                ByteArray binaryTerm = binarize(entry.get_term());
                binaryTerms.add(binaryTerm);
                
                if (_excludedTerms.contains(binaryTerm.hashCode())) {
                    continue;
                }
                
                if (!_weights.containsKey(binaryTerm) && !_restrictTerms) {

                    double initialWeight = entry.get_pValue()/sumWeights;
                    //double initialWeight = initialWeight(nTerms);

                    double pValue = entry.get_pValue();
                    short type = entry.get_type();

                    WinnowTermEntry newEntry =
                            new WinnowTermEntry(initialWeight, 0, 0, pValue, type);
                    _weights.put(binaryTerm, newEntry);
                    _nTermBytes += binaryTerm._byteArray.length;
                }
                
                WinnowTermEntry w = _weights.get(binaryTerm);
                if (w == null) {
                   continue;
                }
                double weight = w.get_weight();
                
                sum += weight;
            }
        
            if ((sum >= _threshold) && (!trainingLabel.equals(_label))) {
                for (ByteArray binaryTerm: binaryTerms) {
                    WinnowTermEntry entry = _weights.get(binaryTerm);
                    if (entry!=null) {
                        double w = entry.get_weight();
                        double newWeight = w/_learningFactor;
                        entry.set_weight(newWeight);
                    }
                }
            }
        
            else if ((sum < _threshold) && (trainingLabel.equals(_label))) {
                for (ByteArray binaryTerm: binaryTerms) {
                    WinnowTermEntry entry = _weights.get(binaryTerm);
                    if (entry!=null) {
                        double w = entry.get_weight();
                        double newWeight = w*_learningFactor;
                        entry.set_weight(newWeight);
                    }
                }
            }
        
            if (_updateCounts) {
                updateTrainingExamples(trainingLabel);
                updateTermOccurrences(trainingLabel, binaryTerms);
            }
            _nTerms =  _weights.size();
        }
        catch (Exception e) {
            LOGGER.error("Error occurred in Winnow. model: " + _label + " size: " +_weights.size());
            LOGGER.error(e);
            e.printStackTrace();
        }
    }
    
    
    public void updateTrainingExamples(String trainingLabel) {
        if (trainingLabel.equals(_label)){
            _nPositiveExamples++;
        } else {
            _nNegativeExamples++;
        }
    }
    
    public void updateTermOccurrences(String trainingLabel, Set<ByteArray> terms) {
            
        for (ByteArray binaryTerm: terms) {
            WinnowTermEntry entry = _weights.get(binaryTerm);
            if(entry == null) {
                entry = new WinnowTermEntry(0.d,0,0);
                _weights.put(binaryTerm,entry);
            }
            if (trainingLabel.equals(_label)) {
                entry.incrementPositiveOccurrences();
            } else {
                entry.incrementNegativeOccurrences();
            }
        }
    }
    
    public boolean isCategory(List<String> terms) {
        double sum = 0;
        Set<ByteArray> binaryTerms = binarize(terms);
        for (ByteArray binaryTerm: binaryTerms) {
            if (_weights.containsKey(binaryTerm)) {
                sum += _weights.get(binaryTerms).get_weight();
            }
            if (sum >= _threshold) return true;
        }
        
        return false;
    }
    
    public double predict(Collection<String> terms) {
        double sum = 0;
        Set<ByteArray> binaryTerms = binarize(terms);
        for (ByteArray binaryTerm: binaryTerms) {
           if(_weights.containsKey(binaryTerm)) {
               sum += _weights.get(binaryTerm).get_weight();
           }
        }
        return sum;
    }
    
    public double predictBinary(Set<ByteArray> binaryTerms) {
        double sum = 0;
        for (ByteArray binaryTerm: binaryTerms) {
           if(_weights.containsKey(binaryTerm)) {
               sum += _weights.get(binaryTerm).get_weight();
           }
        }
        return sum;
    }
    
    public double predictBinaryWithCounts(Map<ByteArray, Integer> termCounts) {
    	StringBuilder sb = new StringBuilder();
        double sum = 0;
        sb.append("MODEL: "+_label+"\n");
        for (ByteArray binaryTerm: termCounts.keySet()) {
           if(_weights.containsKey(binaryTerm)) {
               int count=termCounts.get(binaryTerm);
               double weight = _weights.get(binaryTerm).get_weight();
               double total = weight*count;
               sb.append("TERM: "+binaryTerm._term +" WEIGHT: "+weight+" COUNT: "+ count+" TOTAL: "+total+"\n");
               
               sum += total;
               
           }
        }
        sb.append("SCORE: "+ sum+"\n");
        if (sum > _threshold){
        	LOGGER.debug(sb.toString());
        }
        return sum;
    }
    
    public double predictWithCounts(Map<String, Integer> termCounts) {
        double sum = 0;

        for (String term: termCounts.keySet()) {
            ByteArray binaryTerm = binarize(term);
           if(_weights.containsKey(binaryTerm)) {
               int count=termCounts.get(term); 
               sum += _weights.get(binaryTerm).get_weight()*count;
               
           }
        }
        return sum;
    }
    
    
    public String histogram()
    {
        boolean first = true;
        _min=0;
        _max=0;
        for (ByteArray binaryTerm : _weights.keySet()) {
            double w = logScale(_weights.get(binaryTerm).get_weight());
            if (first) {
                _min = w;
                _max = w;
                first = false;
            }else {
                if (_min > w ) {
                    _min = w;
                }
                if (_max < w ) {
                    _max = w;
                }
            }
        }
        
        _histogram = new long[_nHistogramBins];
        _bins = new double[_nHistogramBins];
        double interval = (_max - _min)/_nHistogramBins;
        _nTerms = 0;
         
        for (ByteArray binaryTerm : _weights.keySet()) {
            double w = logScale(_weights.get(binaryTerm).get_weight());
            int index = (int) Math.floor((w - _min)/interval);
            if (index == _nHistogramBins) {
                index-=1;
            }
            _histogram[index]++;
            _nTerms++;
        }
        
        StringBuffer sb = new StringBuffer("Histogram: " + _nTerms + " terms \n");
        double w0 = _min;
        double w1;
        long s = 0;
        for (int i=1; i<=_nHistogramBins;i++) {
            w1 = w0 + interval;
            _bins[i-1] = w1;
            s += _histogram[i-1];
            
            sb.append(w0 + " - " + w1 + " : " +_histogram[i-1] + " ("+ Math.floor(100*(double)s/(double)_nTerms) + " %) \n");
            w0=w1;
        }
        return sb.toString();
    }
    
    public void pruneByDifferencialCoverage(double threshold) {
        
        Map<ByteArray,WinnowTermEntry> _newWeights = new HashMap<ByteArray, WinnowTermEntry>();
        
        for (ByteArray binaryTerm : _weights.keySet()) {
            //do not prune restricted terms
            if(_restrictedTerms.contains(binaryTerm)) {
                _newWeights.put(binaryTerm, _weights.get(binaryTerm));
            }
            Integer p = _weights.get(binaryTerm).get_nPositiveOccurrences();
            if (p == null) {
                p = 0;
            }
            Integer n = _weights.get(binaryTerm).get_nNegativeOccurrences();
            if (n == null) {
                n = 0;
            }
            double pCoverage = 0;
            if (_nPositiveExamples!=0) {
                pCoverage = p/(double)_nPositiveExamples;
            }
            
            double nCoverage = 0;
            if (_nNegativeExamples!=0) {
                nCoverage = n/(double)_nNegativeExamples;
            }
            
            double diff = pCoverage - nCoverage;
            if (diff >= threshold) {
                _newWeights.put(binaryTerm, _weights.get(binaryTerm));
            }
        }
        long n = _weights.size();
        _weights = _newWeights;
        _nTerms = _weights.size();
        LOGGER.debug("Model pruned from " + n + " terms to " + _weights.size() + " terms with differencial coverage of "+ threshold);
    }
    
    public void pruneBySignificance(double threshold) {
        
        Map<ByteArray,WinnowTermEntry> newWeights = new HashMap<ByteArray, WinnowTermEntry>();
        
        for (ByteArray binaryTerm : _weights.keySet()) {
            //do not prune restricted terms
            if (_restrictedTerms.contains(binaryTerm)) {
                newWeights.put(binaryTerm, _weights.get(binaryTerm));
            }
            Integer p = _weights.get(binaryTerm).get_nPositiveOccurrences();
            if (p == null) {
                p = 0;
            }
            Integer n = _weights.get(binaryTerm).get_nNegativeOccurrences();
            if (n == null) {
                n = 0;
            }
            
            double pValue = 1.d;
            if (n < p) {
                pValue = Significance.pValue(_nPositiveExamples, p.intValue(), _nNegativeExamples, n.intValue());
            }
            
            if (pValue <= threshold && p>n) {
                newWeights.put(binaryTerm, _weights.get(binaryTerm));
            }
        }
        long n = _weights.size();
        _weights.clear(); //help free memory
        _weights = newWeights;
        _nTerms = _weights.size();
        LOGGER.debug("Model pruned from " + n + " terms to " + _weights.size() + " terms with differencial coverage of "+ threshold);
    }


    
    public int excludeMostNegativeTerms(double topPercentile) {
        
        BinaryTermFrequency[] termFrequencies = new BinaryTermFrequency[_weights.size() - _restrictedTerms.size()];
        //final ByteArray dummyByteArray = new ByteArray(new byte[0]);
        //final BinaryTermFrequency filler = new BinaryTermFrequency(dummyByteArray,0,0);
        int start = _excludedTerms.size();
        int i=0;        
        
        for(ByteArray term : _weights.keySet()) {
            
            if (_restrictedTerms.contains(term)) {
                continue;
            }
            
            Integer nPos = _weights.get(term).get_nPositiveOccurrences();
            Integer nNeg = _weights.get(term).get_nNegativeOccurrences();
            int n =0;
            int p =0;
            
            if (nPos != null) {
                p = nPos.intValue();
            }
            
            if (nNeg != null) {
                n = nNeg.intValue();
            }
            
            BinaryTermFrequency t =  new BinaryTermFrequency(term,p,n);
            
            termFrequencies[i] = t;
            i++;
        }
        
        // fill the remaining empty spots of the array;  we do this so we don't have
        // to trim the array, which would require the creation of a second array,
        // which would increase memory usage
        //for(int j=i;i<termFrequencies.length;j++) {
        //    termFrequencies[j] = filler;
        //}
        
        //Arrays.sort(termFrequencies, Collections.reverseOrder());
        Quick.sort(termFrequencies);

        int n = (int)Math.round((double) termFrequencies.length*topPercentile);
        int counter = termFrequencies.length-1;
        int index = 0;
        while (counter <= n && index >= 0) {
            ByteArray term = termFrequencies[i].get_term();

            _weights.remove(term);
            _excludedTerms.add(term.hashCode());
            counter++;
            index--;
        }
        _nTerms = _weights.size();
        int end = _excludedTerms.size();
        return end - start;
        
    }
    /**
     * Keeps nTermsToKeep terms with the lowest pValues in the Winnow model, removing the rest.
     * @param nTermsToKeep
     */
    public int pruneByPValue(int nTermsToKeep) {
        if (nTermsToKeep <=0 ) {
            return 0;
        }
        
        if (nTermsToKeep >= _weights.size() -  _restrictedTerms.size()) {
            return 0;
        }
        int start = _excludedTerms.size();
        BinaryTermPValue[] pValues = new BinaryTermPValue[_weights.size() - _restrictedTerms.size()];
        
        int i=0;        
        
        for(ByteArray term : _weights.keySet()) {
            
            if (_restrictedTerms.contains(term)) {
                continue;
            }
            
            Integer nPos = _weights.get(term).get_nPositiveOccurrences();
            Integer nNeg = _weights.get(term).get_nNegativeOccurrences();
            
            int n =0;
            int p =0;
            
            if (nPos != null) {
                p = nPos.intValue();
            }
            
            if (nNeg != null) {
                n = nNeg.intValue();
            }
            
            BinaryTermPValue t =  new BinaryTermPValue(term, _nPositiveExamples, p, _nNegativeExamples, n);
            
            pValues[i] = t;
            i++;
        }
        
        // fill the remaining empty spots of the array;  we do this so we don't have
        // to trim the array, which would require the creation of a second array,
        // which would increase memory usage
        
        Quick.sort(pValues);

        int index = pValues.length-1;
        while (nTermsToKeep > _weights.size() - _restrictedTerms.size() && index >= 0) {
            ByteArray term = pValues[index].get_term();

            _weights.remove(term);
            _excludedTerms.add(term.hashCode());
            index--;
        }
        
        _nTerms = _weights.size(); 
        int end = _excludedTerms.size();
        return end - start;
    }

    /**
     * Keeps nTermsToKeep terms with the lowest pValues in the Winnow model, removing the rest.
     * @param weightPercentage
     */
    public int pruneByCummulativeWeight(double weightPercentage) {

        if (weightPercentage >= 1 || weightPercentage<0) {
            return 0;
        }

        if (weightPercentage >= _weights.size() -  _restrictedTerms.size()) {
            return 0;
        }

        int start = _excludedTerms.size();
        BinaryTermWeight[] termWeights = new BinaryTermWeight[_weights.size() - _restrictedTerms.size()];
        double weightSum = 0;

        int i=0;

        for(ByteArray term : _weights.keySet()) {

            double weight = _weights.get(term).get_weight();
            weightSum += weight;


            if (_restrictedTerms.contains(term)) {
                continue;
            }

            BinaryTermWeight t =  new BinaryTermWeight(term, weight);
            termWeights[i] = t;
            i++;
        }

        // fill the remaining empty spots of the array;  we do this so we don't have
        // to trim the array, which would require the creation of a second array,
        // which would increase memory usage

        Quick.sort(termWeights);

        double cummulativeWeight = 0;
        double previousWeight = termWeights[termWeights.length-1].get_weight();

        int index = termWeights.length-1;

        while ( index >= 0 ) {

            double w = termWeights[index].get_weight();
            assert(w <= previousWeight);

            cummulativeWeight += w;

            double ratio =  cummulativeWeight/weightSum;

            if (ratio <= weightPercentage){
                index--;
                continue;

            }  else {

                ByteArray term = termWeights[index].get_term();
                _weights.remove(term);
                _excludedTerms.add(term.hashCode());
            }

            index--;
            previousWeight = w;
        }

        _nTerms = _weights.size();
        int end = _excludedTerms.size();
        return end - start;
    }
    
    public int pruneByPValue(int nounsToKeep, int entitiesToKeep) {
        if (nounsToKeep < 0 && entitiesToKeep < 0) {
            return 0;
        }
        
        int start = _excludedTerms.size();
        Map< WinnowTermEntry, ByteArray> nounMap = new HashMap<WinnowTermEntry,ByteArray>();
        Map< WinnowTermEntry, ByteArray> entityMap = new HashMap<WinnowTermEntry,ByteArray>();
        
        List<WinnowTermEntry> nouns = new ArrayList<WinnowTermEntry>();
        List<WinnowTermEntry> entities = new ArrayList<WinnowTermEntry>();
                
        for(ByteArray term : _weights.keySet()) {
            
            if (_restrictedTerms.contains(term)) {
                continue;
            }
            
            WinnowTermEntry termEntry= _weights.get(term);
            short type = termEntry.get_type();
            if (type == Term.ENTITY) {
                entityMap.put(termEntry, term);
                entities.add(termEntry);
            }
            if (type == Term.NOUN) {
                nounMap.put(termEntry, term);
                nouns.add(termEntry);
            }
        }
        
        Comparator<WinnowTermEntry> pValueComparator = new Comparator<WinnowTermEntry>() {
                                                            public int compare(WinnowTermEntry t1, WinnowTermEntry t2) {
                                                                if (t1._pValue > t2._pValue) {
                                                                    return 1;
                                                                }
                                                                if (t1._pValue < t2._pValue) {
                                                                    return -1;
                                                                }
                                                                return 0;
                                                            }
                                                        };
                                                        
         Collections.sort(nouns,pValueComparator);
         Collections.sort(entities,pValueComparator);
        
        if (entitiesToKeep >=0 && entitiesToKeep < entities.size()) {
            int index = entitiesToKeep;
            while(index < entities.size()) {
                WinnowTermEntry termEntry = entities.get(index);
                ByteArray term = entityMap.get(termEntry);
                _weights.remove(term);
                _excludedTerms.add(term.hashCode());
                index++;
            }
        }
        
        if (nounsToKeep >=0 && nounsToKeep < nouns.size()) {
            int index = nounsToKeep;
            while(index < nouns.size()) {
                WinnowTermEntry termEntry = nouns.get(index);
                ByteArray term = nounMap.get(termEntry);
                _weights.remove(term);
                _excludedTerms.add(term.hashCode());
                index++;
            }
        }
        
        _nTerms = _weights.size(); 
        int end = _excludedTerms.size();
        return end - start;
    }
    
    public void pruneByHistogram (double percentile) {
        if (_histogram == null) {
            histogram();
        }
        Map<ByteArray, WinnowTermEntry> newWeights = new HashMap<ByteArray, WinnowTermEntry>();
        long old = _weights.keySet().size();
        
        for (ByteArray term: _weights.keySet()) {
            //do not prune restricted terms
            if(_restrictedTerms.contains(term)) {
                newWeights.put(term, _weights.get(term));
            }
            double w = logScale(_weights.get(term).get_weight());
            int i = 0;
            double sum = 0;
                
            while (i<_bins.length-1 && w>_bins[i]) {
                 i++;
                 sum += _histogram[i];
            }
            if(sum/_nTerms >= percentile) {
                 newWeights.put(term,_weights.get(term));
            }
        }
        _weights = newWeights;
        LOGGER.info("Winnow model pruned from " + old + " terms to " + _weights.size());
    }

    public double logScale(double x) {
        return Math.log10(x);
    }
    
    public String get_label() {
        return _label;
    }

    public void set_label(String _label) {
        this._label = _label;
    }

    @Override
    public String toString(){
    	return _label;
    }
    
    public Map<String, Double> get_weights() {
        Map<String,Double> weights = new HashMap<String,Double>();
        
        for(ByteArray binaryTerm :_weights.keySet()) {

            WinnowTermEntry entry = _weights.get(binaryTerm);
            Double weight = entry.get_weight();

            if (weight ==  null || weight == 0.d) {

                continue;
            }

            String term = toString(binaryTerm);
            weights.put(term, weight);
        }
        return weights;
    }

    public void set_weights(Map<ByteArray, WinnowTermEntry> _weights) {
        this._weights = _weights;
    }

    public double get_learningFactor() {
        return _learningFactor;
    }

    public void set_learningFactor(double _learningFactor) {
        this._learningFactor = _learningFactor;
    }

    public double get_threshold() {
        return _threshold;
    }

    public void set_threshold(double _threshold) {
        this._threshold = _threshold;
    }

    public boolean is_restrict() {
        return _restrictTerms;
    }

    public void set_restrict(boolean _restrict) {
        this._restrictTerms = _restrict;
    }

    public void set_updateCounts(boolean _updateCounts) {
        this._updateCounts = _updateCounts;
    }

    public boolean is_updateCounts() {
        return _updateCounts;
    }

    public int get_nPositiveExamples() {
        return _nPositiveExamples;
    }

    public int get_nNegativeExamples() {
        return _nNegativeExamples;
    } 
    
    public int get_nTerms() {
        return _nTerms;
    }

    public long get_nTermBytes() {
        return _nTermBytes +_weights.size()*(64 + 32 + 32) ;
    }

    public long get_nExcludedTermBytes() {
        return _nExcludedTermBytes*32;
    }

    public static boolean isCaseSensitve() {
        return isCaseSensitive;
    }

    public static void setCaseSensitve(boolean b) {
        isCaseSensitive = b;
    }
    

    public void dumpExcludedTerms() {
        _excludedTerms.clear();
    }
    
    public static class WinnowParseException extends Exception {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        public WinnowParseException(String s) {
            super(s);
        }
       }

    protected Map<ByteArray, Integer> get_negativeOccurrences() {
        Map<ByteArray, Integer> n =  new HashMap<ByteArray,Integer>();
        for (ByteArray b: _weights.keySet()) {
            WinnowTermEntry entry = _weights.get(b);
            n.put(b, entry.get_nNegativeOccurrences());
        }
        return n;
    }

    protected Map<ByteArray, Integer> get_positiveOccurrences() {
        Map<ByteArray, Integer> p =  new HashMap<ByteArray,Integer>();
        for (ByteArray b: _weights.keySet()) {
            WinnowTermEntry entry = _weights.get(b);
            p.put(b, entry.get_nPositiveOccurrences());
        }
        return p;
    }
    
    public void restrict(String term, int nTerms) {
        ByteArray binaryTerm= normalizeToByteArray(term);
        _restrictedTerms.add(binaryTerm);
        if (!_weights.containsKey(binaryTerm)) {
            double initialWeight = initialWeight(nTerms); 
            _weights.put(binaryTerm, new WinnowTermEntry(initialWeight));
            _nTermBytes += binaryTerm._byteArray.length; 
        }
    }
    
    public static class ByteArray implements Comparable<ByteArray> {
        private byte[] _byteArray;
        private String _term;
       
        public ByteArray(byte[] byteArray) {
            _byteArray = byteArray;
        }
        public ByteArray(byte[] byteArray, String term) {
            this(byteArray);
            _term =  term;
        }
        
        @Override
        public boolean equals(Object o) {
            if(!(o instanceof ByteArray)) return false;
            
            ByteArray b = (ByteArray) o;
            
            if (this._byteArray.length != b._byteArray.length) {
                return false;
            }
            
            for(int i=0; i<this._byteArray.length;i++ ) {
                if (this._byteArray[i] != b._byteArray[i]) {
                    return false;
                }
            }
            
            return true;
        }

        @Override
        public int compareTo(ByteArray ba) {
            for (int i = 0, j = 0; i < this._byteArray.length && j < ba._byteArray.length; i++, j++) {
                    int a = (this._byteArray[i] & 0xff);
                    int b = (ba._byteArray[j] & 0xff);
                    if (a != b) {
                        return a - b;
                    }
                }
                return this._byteArray.length - ba._byteArray.length;
        }
        
        @Override
        public int hashCode() {
            int hash = Arrays.hashCode(_byteArray);
            return hash;
        }
        
        public String get_term() {
            return _term;
        }
        
        public void set_term(String term) {
            _term = term;
        }
    }
    static class WinnowTermEntry {
        
        private int _nPositiveOccurrences = 0;
        private int _nNegativeOccurrences = 0;
        private double _weight = 0.d;
        private double _pValue = -1.d;
        private short _type=-1;
        
        public WinnowTermEntry(double weight, int positiveOccurrences, int negativeOccurrences) {
            _weight = weight;
            _nPositiveOccurrences = positiveOccurrences;
            _nNegativeOccurrences = negativeOccurrences; 
        }
        
        public WinnowTermEntry(double weight, int positiveOccurrences, int negativeOccurrences, double pValue, short type) {
            _weight = weight;
            _nPositiveOccurrences = positiveOccurrences;
            _nNegativeOccurrences = negativeOccurrences; 
            _pValue = pValue;
            _type = type;
        }
         
        public WinnowTermEntry(double weight) {
            _weight = weight;
        }
        
        public void incrementPositiveOccurrences() {
            _nPositiveOccurrences++;
        }
        
        public void incrementNegativeOccurrences() {
            _nNegativeOccurrences++;
        }
        
        public int get_nPositiveOccurrences() {
            return _nPositiveOccurrences;
        }
        
        public void set_nPositiveOccurrences(int _nPositiveOccurrences) {
            this._nPositiveOccurrences = _nPositiveOccurrences;
        }
        
        public int get_nNegativeOccurrences() {
            return _nNegativeOccurrences;
        }
        
        public void set_nNegativeOccurrences(int _nNegativeOccurrences) {
            this._nNegativeOccurrences = _nNegativeOccurrences;
        }
        
        public double get_weight() {
            return _weight;
        }
        
        public void set_weight(double _weight) {
            this._weight = _weight;
        }
        
        public double get_pValue() {
            return _pValue;
        }
        
        public void set_pValue(double pValue) {
             _pValue = pValue;
        }
        
        public short get_type() {
            return _type;
        }
        
        public void set_type(short type) {
             _type = type;
        }
    }

    public static  class BinaryTermFrequency implements Comparable<BinaryTermFrequency>{

        private ByteArray _term;
        private int _np=0;
        private int _nn=0;
        
        public BinaryTermFrequency(ByteArray term, int np, int nn) {
            _term= term;
            _np = np;
            _nn = nn;
        }
        
        public ByteArray get_term() {
            return _term;
        }
        
        public int get_np() {
            return _np;
        }
        
        public int get_nn() {
            return _nn;
        }
        
        @Override
        public int compareTo(BinaryTermFrequency tf) {
            if (tf == null) {
                return 1;
            }
            if (this._nn -  this._np ==  tf._nn - tf._np) {
                if (this._nn !=  tf._nn) {
                    return this._nn -  tf._nn;
                } else {
                    return tf._np - this._np;
                }
            } else {
                return this._nn - this._np - tf._nn + tf._np;
           } 
        }
        
        @Override
        public boolean equals(Object o) {
            if(! (o instanceof BinaryTermFrequency)) return false;
            
            BinaryTermFrequency tf = (BinaryTermFrequency) o;
            
            if (!this._term.equals(tf._term)) return false;
            if(this._nn != tf._nn) return false;
            if(this._np != tf._np) return false;
            
            return true;
        }
    }

    public static  class BinaryTermPValue implements Comparable<BinaryTermPValue>{
    
        private ByteArray _term;

        private double _pValue=-1.d;
        private int _nPosOccs = 0;
        private int _nNegOccs = 0;
        
        public BinaryTermPValue(ByteArray term, int nPosDocs, int nPosOccs, int nNegDocs, int nNegOccs) {
            _term= term;
           
            _pValue = Significance.pValue(nPosDocs, nPosOccs, nNegDocs, nNegOccs);
           
            if (nPosOccs < nNegOccs) {
                _pValue = -_pValue;
                _nPosOccs = nPosOccs;
                _nNegOccs = nNegOccs;
            }
        }
        
        public ByteArray get_term() {
            return _term;
        }
        
        public int get_nPosOccs() {
            return _nPosOccs;
        }
         
        public int get_nNegOccs() {
            return _nNegOccs;
        }
        
        @Override
        public int compareTo(BinaryTermPValue pv) {
            
            if (pv == null) {
                return 1;
            }
            
            if (this._pValue > pv._pValue) {
               return 1;
            } 
            
            if (this._pValue < pv._pValue) {
                return -1;
            } 
            
            if (this._pValue == pv._pValue) {
                if (this._nNegOccs > pv._nNegOccs) {
                    return 1;
                }
                if (this._nPosOccs < pv._nPosOccs) {
                    return 1;
                }
            }
            return 0; 
        }
        
        @Override
        public boolean equals(Object o) {
            if(! (o instanceof BinaryTermPValue)) return false;
            
            BinaryTermPValue pv = (BinaryTermPValue) o;
            
            if (!this._term.equals(pv._term)) return false;
            if(this._pValue != pv._pValue) return false;
            
            return true;
        }
    }

    public static  class BinaryTermWeight implements Comparable<BinaryTermWeight>{

        private ByteArray _term;

        private double _weight = 0d;


        public BinaryTermWeight(ByteArray term, double weight) {

            _term= term;
            _weight = weight;
        }

        public ByteArray get_term() {
            return _term;
        }

        public double get_weight() {
            return _weight;
        }



        @Override
        public int compareTo(BinaryTermWeight btw) {

            if (btw == null) {
                return 1;
            }

            if (this._weight > btw._weight) {
                return 1;
            }

            if (this._weight < btw._weight) {
                return -1;
            }

            return 0;
        }

        @Override
        public boolean equals(Object o) {
            if(! (o instanceof BinaryTermWeight)) return false;

            BinaryTermWeight btw = (BinaryTermWeight) o;

            if (!this._term.equals(btw._term)) return false;
            if(this._weight != btw._weight) return false;

            return true;
        }
    }
}