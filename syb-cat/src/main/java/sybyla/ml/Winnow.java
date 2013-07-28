package sybyla.ml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import sybyla.hash.StringHash;
import sybyla.ml.Significance;

public class Winnow {
    
    private static Logger LOGGER = Logger.getLogger(Winnow.class);
    
    public String _label;
    private Map<String,Double> _weights = new HashMap<String, Double>();
    private Map<String,Long> _positiveOccurrences = new HashMap<String, Long>();
    private Map<String, Long> _negativeOccurrences = new HashMap<String, Long>();
    private Set<String> _restrictedTerms = new HashSet<String>();  //restricted terms are never pruned from the model
    private Set<Long> _excludedTerms = new HashSet<Long>();  //excluded terms are not added to the model
    private double _learningFactor=2;
    private double _threshold = 1;
    private int _nHistogramBins = 100;
    private double _max;
    private double _min;
    private double[] _bins;
    private long[] _histogram;
    private long _nTerms;
    private boolean _restrictTerms = false;
    private int _nPositiveExamples=0;
    private int _nNegativeExamples=0;
    private boolean _updateCounts = true;
    private boolean _doNotTrain =  false;
     
    public Winnow(String label) {
        _label = label;
    }
    
    public Winnow(String label, Map<String,Double> weights) {
        _label = label;
        _weights = weights;
    }
   
    /**
     * parses a line of text into a Winnow object
     *
     */
    public static Winnow load(String line) throws WinnowParseException{
        if (line == null || line.length()==0) {
            return null;
        }
        String[] terms = line.split("\t");
        if (terms == null || terms.length ==0 ) {
            LOGGER.error("Error loding winnow: string is empty");
        }
        
        String category = terms[0];
        Winnow winnow = new Winnow(category);
        
        int nPositiveExamples = Integer.parseInt(terms[1]);
        winnow._nPositiveExamples = nPositiveExamples;
        
        int nNegativeExamples = Integer.parseInt(terms[2]);
        winnow._nNegativeExamples = nNegativeExamples;
                
        for (int i=0; i< (terms.length-3)/2; i++) {
            String term = terms[3 + 2*i];
            Double weight = Double.parseDouble(terms[3 + 2*i + 1]);
            winnow._weights.put(term, weight);
        }
        
        return winnow;
    }
    
    public String getTopTerms(Collection<String> termList, int n) {
        StringBuffer topTerms = new StringBuffer();
        List<CategoryModelScore> termWeightList = new ArrayList<CategoryModelScore>();
        for (String term : termList) {
            if (_weights.containsKey(term)) {
                double weight = _weights.get(term);
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
    
    public double initialWeight(Set<String> terms) {
        if (terms ==  null || terms.size() == 0) return 1;
        double nTerms = (double)terms.size();
        return 1/nTerms;
    }
    
    public boolean containsLabel(List<String>trainingLabels) {
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
     
    public void train(String trainingLabel, Set<String> terms) {
        if(_doNotTrain) return;
        try{
            double sum = 0;
            for (String term: terms) {
                if (_excludedTerms.contains(StringHash.getLongHash(term))) {
                    continue;
                }
                if (!_weights.containsKey(term) && !_restrictTerms) {
                    _weights.put(term, initialWeight(terms));
                }
                Double w = _weights.get(term);
                if (w == null) {
                   continue;
                }
                double weight = 0;
                if (w != null) {
                    weight = w.doubleValue();
                }
                sum += weight;
            }
        
            if ((sum >= _threshold) && (!trainingLabel.equals(_label))) {
                for (String term: terms) {
                    Double w = _weights.get(term);
                    if (w!=null) {
                        double newWeight = w.doubleValue()/_learningFactor;
                        _weights.put(term, newWeight);
                    }
                }
            }
        
            else if ((sum < _threshold) && (trainingLabel.equals(_label))) {
                for (String term: terms) {
                    Double w = _weights.get(term);
                    if (w!=null) {
                        double newWeight = w.doubleValue()*_learningFactor;
                        _weights.put(term, newWeight);
                    }
                }
            }
        
            if (_updateCounts) {
                updateTrainingExamples(trainingLabel);
                updateTermOccurrences(trainingLabel, terms);
            }
            _nTerms =  _weights.size();
        }
        catch (Exception e) {
            LOGGER.error("Error occurred in Winnow model " + _label + " size: " +_weights.size());
        }
    }
    
    
    public void updateTrainingExamples(String trainingLabel) {
        if (trainingLabel.equals(_label)){
            _nPositiveExamples++;
        } else {
            _nNegativeExamples++;
        }
    }
    
    public void updateTermOccurrences(String trainingLabel, Set<String> terms) {
        
        Map<String,Long> termCountMap;
        
        if (trainingLabel.equals(_label)) {
            termCountMap = _positiveOccurrences;
        } else {
            termCountMap = _negativeOccurrences;
        }
            
        for (String term: terms) {
            Long o = termCountMap.get(term);
            if (o == null) {
                o = new Long(0);
            }
            o++;
            termCountMap.put(term, o);
        }
    }
    
    public boolean isCategory(List<String> terms) {
        double sum = 0;
        for (String term: terms) {
            if (_weights.containsKey(terms)) {
                sum += _weights.get(term);
            }
            if (sum >= _threshold) return true;
        }
        
        return false;
    }
    
    public double predict(Collection<String> terms) {
        double sum = 0;
        for (String term: terms) {
           if(_weights.containsKey(term)) {
               sum += _weights.get(term);
           }
        }
        return sum;
    }
    
    
    public String histogram()
    {
        boolean first = true;
        _min=0;
        _max=0;
        for (String term : _weights.keySet()) {
            double w = logScale(_weights.get(term));
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
         
        for (String term: _weights.keySet()) {
            double w = logScale(_weights.get(term));
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
        
        Map<String,Double> _newWeights = new HashMap<String, Double>();
        
        for (String term : _weights.keySet()) {
            //do not prune restricted terms
            if(_restrictedTerms.contains(term)) {
                _newWeights.put(term, _weights.get(term));
            }
            Long p = _positiveOccurrences.get(term);
            if (p == null) {
                p = 0L;
            }
            Long n = _negativeOccurrences.get(term);
            if (n == null) {
                n = 0L;
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
            if (diff < threshold) {
                _positiveOccurrences.remove(term);
                _negativeOccurrences.remove(term);
            }
            else {
                _newWeights.put(term, _weights.get(term));
            }
        }
        long n = _weights.size();
        _weights = _newWeights;
        _nTerms = _weights.size();
        LOGGER.debug("Model pruned from " + n + " terms to " + _weights.size() + " terms with differencial coverage of "+ threshold);
    }
    
    public void pruneBySignificance(double threshold) {
        
        Map<String,Double> newWeights = new HashMap<String, Double>();
        
        for (String term : _weights.keySet()) {
            //do not prune restricted terms
            if (_restrictedTerms.contains(term)) {
                newWeights.put(term, _weights.get(term));
            }
            Long p = _positiveOccurrences.get(term);
            if (p == null) {
                p = 0L;
            }
            Long n = _negativeOccurrences.get(term);
            if (n == null) {
                n = 0L;
            }
            
            double pValue = 1.d;
            if (n < p) {
                pValue = Significance.pValue(_nPositiveExamples, p.intValue(), _nNegativeExamples, n.intValue());
            }
            
            if (pValue > threshold || n>=p) {
                _positiveOccurrences.remove(term);
                _negativeOccurrences.remove(term);
            }
            else if (pValue < threshold){
                newWeights.put(term, _weights.get(term));
            }
        }
        long n = _weights.size();
        _weights = newWeights;
        _nTerms = _weights.size();
        LOGGER.debug("Model pruned from " + n + " terms to " + _weights.size() + " terms with differencial coverage of "+ threshold);
    }
    
    public int excludeMostNegativeTerms(double topPercentile) {
        
        TermFrequency[] termFrequencies = new TermFrequency[_weights.keySet().size()];
        int start =  _excludedTerms.size();
        int i=0;
        for(String term : _weights.keySet()) {
            
            if (_restrictedTerms.contains(term)) {
                continue;
            }
            
            Long nPos = _positiveOccurrences.get(term);
            Long nNeg = _negativeOccurrences.get(term);
            int n =0;
            int p =0;
            
            if (nPos != null) {
                p = nPos.intValue();
            }
            
            if (nNeg != null) {
                n = nNeg.intValue();
            }
            TermFrequency t =  new TermFrequency(term,p,n);
            termFrequencies[i] = t;
            i++;
        }
        
        Arrays.sort(termFrequencies, Collections.reverseOrder());

        int n = (int)Math.round((double) termFrequencies.length*topPercentile);
        for (i=0; i<=n && i <termFrequencies.length ; i++) {
            String term = termFrequencies[i].get_term();
            //if (tf.get_nn() > tf.get_np()) {
            _weights.remove(term);
            _positiveOccurrences.remove(term);
            _negativeOccurrences.remove(term);
            _excludedTerms.add(StringHash.getLongHash(term));
            //}
        }
        _nTerms = _weights.size();
        int end = _excludedTerms.size();
        return end - start;
        
    }
    
    public void pruneByHistogram (double percentile) {
        if (_histogram == null) {
            histogram();
        }
        Map<String, Double> newWeights = new HashMap<String, Double>();
        long old = _weights.keySet().size();
        
        for (String term: _weights.keySet()) {
            //do not prune restricted terms
            if(_restrictedTerms.contains(term)) {
                newWeights.put(term, _weights.get(term));;
            }
            double w = logScale(_weights.get(term));
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

    public Map<String, Double> get_weights() {
        return _weights;
    }

    public void set_weights(Map<String, Double> _weights) {
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

    protected Map<String, Long> get_negativeOccurrences() {
        return _negativeOccurrences;
    }

    protected Map<String, Long> get_positiveOccurrences() {
        return _positiveOccurrences;
    }
    
    public void restrict(String term) {
        _restrictedTerms.add(term);
    }    
}