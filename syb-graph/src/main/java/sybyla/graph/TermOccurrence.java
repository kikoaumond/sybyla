package sybyla.graph;

public  class TermOccurrence implements Comparable<TermOccurrence>{
    
    private String _term;
    private int _occurrences=0;
    private double _frequency=1;
        
    public TermOccurrence(String term, int occurrences, double frequency) {
        _term = term;
        _occurrences =  occurrences;
        _frequency =  Math.log(frequency);
    }

    @Override
    public int compareTo(TermOccurrence t) {
        
        double x =  ((double)_occurrences)/_frequency - ((double)t._occurrences)/t._frequency;
        if (x>=0) {
            return 1;
        } else {
            return -1;
        }
    }

	public String get_term() {
		return _term;
	} 
}
