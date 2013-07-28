package sybyla.ml;

public class ModelTerm implements Comparable<ModelTerm>{
    
    private String _term;
    private double _weight;
    
    public ModelTerm(String term, double weight) {
        _term = term;
        _weight = weight;
    }
    public ModelTerm(String term) {
        _term = term;
    }
    
    public String get_term() {
        return _term;
    }

    public double get_weight() {
        return _weight;
    }

    @Override
    public int compareTo(ModelTerm t) {
       if (this._weight > t._weight) {
            return 1;
       }
       if (this._weight < t._weight) {
           return -1;
       }
       
       return 0;
    }
}