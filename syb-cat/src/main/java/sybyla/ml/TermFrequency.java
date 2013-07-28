package sybyla.ml;

public class TermFrequency implements Comparable<TermFrequency>{

    private String _term;
    private int _np=0;
    private int _nn=0;
    
    public TermFrequency(String term, int np, int nn) {
        _term= term;
        _np = np;
        _nn = nn;
    }
    
    public String get_term() {
        return _term;
    }
    
    public int get_np() {
        return _np;
    }
    
    public int get_nn() {
        return _nn;
    }
    
    @Override
    public int compareTo(TermFrequency o) {
    	
    	TermFrequency tf = (TermFrequency) o;
            
    	if (this._nn -  this._np ==  tf._nn - tf._np) {
    		return this._nn -  tf._nn;
    	} else {
    		return this._nn - this._np - tf._nn + tf._np;
        }
    }

}
