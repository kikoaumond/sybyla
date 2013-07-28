package sybyla.nlp;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public abstract class Sequence implements Comparable<Sequence>{
	
	private List<String> tag= new ArrayList<String>();
	private List<String> terms = new ArrayList<String>();
	private String nounType=null;
	
	protected Sequence(){}
	
	public Sequence(String term, String pos){
		terms.add(term);
		tag.add(pos);
	}
	    	
	public abstract Set<Sequence> explode();
	
	public abstract String getNounType();
	
	public abstract Set<String> canonicalize();
	
	public abstract boolean isValid();

	public abstract String toString();
	
	@Override
	public boolean equals(Object o){
		
		if (!(o instanceof Sequence)){
			return false;
		}
		
		Sequence s= (Sequence) o;
		if (this.terms.size() != s.terms.size()){
			return false;
		}
		
		for(int i=0; i<this.terms.size(); i++){
			
			String t1 = this.terms.get(i);
			String t2 = s.terms.get(i);
			
			if (!t1.equals(t2)) return false;

		}
		
		return true;
	}
	
	@Override
	public int hashCode(){
		String s =  toString();
		if (s==null){
			return "".hashCode();
		}
		return toString().hashCode();
	}
	
	@Override
	public int compareTo(Sequence s) {

		String s1=this.toString();
		String s2=s.toString();
		if (s1 ==  null){
			if (s2 == null){
				return 0;
			}
			return -1;
		}
		if (s2 == null){
			return 1;
		}
		
		int i =  s1.compareTo(s2);
		return i;
	}
}

