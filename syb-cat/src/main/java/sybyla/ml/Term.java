package sybyla.ml;

public class Term implements Comparable<Term>{
    
    public static final short ENTITY=1;
    public static final short NOUN=2;
    public static final short TITLE=3;
    private String _term;



    private double _pValue=-1.d;
    private short _type=-1;
    
    public Term(String term, double pValue, short type) {
        _term = term;
        _pValue = pValue;
        _type =  type;
    }

    public Term(String term, double pValue) {
        _term = term;
        _pValue = pValue;
    }

    public Term(String term) {
        _term = term;
    }
    
    public String get_term() {
        return _term;
    }

    public double get_pValue() {
        return _pValue;
    }

    public short get_type() {
        return _type;
    }

    public void set_pValue(double _pValue)
    {
        this._pValue = _pValue;
    }

    @Override
    public int compareTo(Term t) {
       if (this._pValue > t._pValue) {
            return 1;
       }
       if (this._pValue < t._pValue) {
           return -1;
       }
       
       return 0;
    }
    
}