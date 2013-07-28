package sybyla.ml;

public class CategoryModelScore implements Comparable<CategoryModelScore>{

    private String _category;
    private double _score;
    private String[] _terms;
    
    public CategoryModelScore(String category, double score) {
        this(category, score, null);
    }
    
     public CategoryModelScore(String category, double score, String[] terms) {
        _category= category;
        _score = score;
        _terms = terms;
    }
    
    public String get_category() {
        return _category;
    }
    
    public double get_score() {
        return _score;
    }
    
    public String[] get_terms() {
        return _terms;
    }
    
    @Override
    public int compareTo(CategoryModelScore o) {
        CategoryModelScore categoryScore = (CategoryModelScore) o;
        if (this._score >  categoryScore._score) {
            return 1;
        }
        if (this._score <  categoryScore._score) {
            return -1;
        }
            
        return 0;
    }

}
