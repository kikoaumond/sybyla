package sybyla.mapred;

import cascading.tuple.Fields;
import cascading.tuple.Tuple;
import cascading.tuple.TupleEntry;
import com.scaleunlimited.cascading.BaseDatum;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A DocDatum represents a page that we've parsed/analyzed, which is suitable for
 * generating phrase maps, indexing, etc.
 *
 */
@SuppressWarnings("serial")
public class WinnowDatum extends BaseDatum {

    public static final String CATEGORY_FN = fieldName(WinnowDatum.class, "category");
    public static final String TERM_WEIGHTS_FN = fieldName(WinnowDatum.class, "term_weight");
    public static final String  N_POSITIVE_EXAMPLES_FN = fieldName(WinnowDatum.class,"n_positive_examples");
    public static final String  N_NEGATIVE_EXAMPLES_FN = fieldName(WinnowDatum.class,"n_negative_examples");
    

    public static final Fields FIELDS = new Fields(CATEGORY_FN, N_POSITIVE_EXAMPLES_FN, 
                                                   N_NEGATIVE_EXAMPLES_FN, TERM_WEIGHTS_FN);

    
    public WinnowDatum(TupleEntry te) {
        super(FIELDS);
        
        setTupleEntry(te);
    }
    
    public WinnowDatum() {
        super(FIELDS);
        setTermWeights(new HashMap<String,Double>());
    }
    
    public WinnowDatum(String category, Map<String, Double> termWeights,
                       Integer nPositiveExamples, Integer nNegativeExamples) {
        super(FIELDS);
        
        setCategory(category);
        setTermWeights(termWeights);
        setNegativeExamples(nNegativeExamples);
        setPositiveExamples(nPositiveExamples);
    }


    public WinnowDatum(String category,Tuple termWeightsTuple,
                       Integer nPositiveExamples, Integer nNegativeExamples) {
        super(FIELDS);

        setCategory(category);
        setTermWeights(termWeightsTuple);
        setNegativeExamples(nNegativeExamples);
        setPositiveExamples(nPositiveExamples);
    }


    public void setCategory(String category) {
        _tupleEntry.set(CATEGORY_FN,category);
    }
    
    public String getCategory() {
        return _tupleEntry.getString(CATEGORY_FN);
    }
    
    public void setNegativeExamples(int negativeExamples) {
        _tupleEntry.set(N_NEGATIVE_EXAMPLES_FN,negativeExamples);
    }
    
    public Integer getNegativeExamples() {
        return _tupleEntry.getInteger(N_NEGATIVE_EXAMPLES_FN);
    }
    
    public void setPositiveExamples(int positiveExamples) {
        _tupleEntry.set(N_POSITIVE_EXAMPLES_FN,positiveExamples);
    }
    
    public Integer getPositiveExamples() {
        return _tupleEntry.getInteger(N_POSITIVE_EXAMPLES_FN);
    }
    
    public void setTermWeights(Map<String, Double> termWeights) {

        Tuple t =getWeightTermsTuple(termWeights);
        _tupleEntry.set(TERM_WEIGHTS_FN, t);
    }

    public void setTermWeights(Tuple termWeightsTuple){

        _tupleEntry.set(TERM_WEIGHTS_FN,termWeightsTuple);
    }

    public Tuple getWeightTermsTuple(Map<String, Double> termWeights) {

        List<TermWeight>  termWeightList= new ArrayList<>();
        Tuple tuple = new Tuple();


        for (String term: termWeights.keySet()) {

            Double weight = termWeights.get(term);
            TermWeight tw =  new TermWeight(term,weight);
            termWeightList.add(tw);
        }

        Collections.sort(termWeightList, Collections.reverseOrder());

        for (int i = 0; i<termWeights.size(); i++){

            TermWeight tw =  termWeightList.get(i);
            String term = tw.term;
            double weight = tw.weight;
            tuple.addString(term);
            tuple.addDouble(weight);
        }

        return tuple;
    }

    public Map<String, Double> getWeightTerms() {

        Map<String, Double> weightTerms = new HashMap<String, Double>();


        Tuple t = (Tuple)_tupleEntry.get(TERM_WEIGHTS_FN);

        int numEntries = t.size() / 2;
        for (int i = 0; i < numEntries; i++) {
            String term = t.getString(i * 2);
            Double weight = t.getDouble((i * 2) + 1);
            weightTerms.put(term, weight);
        }

        return weightTerms;
    }


    private class TermWeight implements Comparable<TermWeight>{

        private String term;
        private double weight;

        public TermWeight(String term, double weight){

            this.term = term;
            this.weight =  weight;
        }

        @Override
        public int compareTo(TermWeight o)
        {
              if (this.weight > o.weight){
                  return 1;
              } else if (this.weight < o.weight){
                return -1;
              }  else {
                  return this.term.compareTo(o.term);
              }

        }
    }
    
}
