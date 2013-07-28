package sybyla.graph;

public class RankedNode implements Comparable<RankedNode>{
	
	float rank;
	private String term;
	private float relevance;
	private long nodeId;
	
	public RankedNode( String term, long nodeId){
		this.term = term;
		this.nodeId = nodeId;
		this.rank = Float.MAX_VALUE;
	}
	
	
	public RankedNode( String term, long nodeId, float rank ){
		this.term = term;
		this.nodeId =  nodeId;
		this.rank = rank;
	}
	
	@Override
	public int hashCode(){
		return term.hashCode();
	}
	
	@Override
	public int compareTo( RankedNode o ) {
		
		if (o == null){
			throw new NullPointerException();
		}
		
		if (this.rank > o.rank){
			return 1;
		} else if (this.rank < o.rank){
			return -1;
		} else {
			return this.term.compareTo(o.term);
		}
	}
	
	@Override
	public boolean equals( Object o ){
		if (o ==  null) {
			return false;
		}
		
		if (! (o instanceof RankedNode)) {
			return false;
		}
		
		RankedNode rn = (RankedNode) o;
		return this.term.equalsIgnoreCase(rn.term) ;
	}


	public float getRank() {
		return rank;
	}


	public String getTerm() {
		return term;
	}


	public void setRank(float rank) {
		this.rank = rank;
	}


	public void setTerm(String term) {
		this.term = term;
	}


	public long getNodeId() {
		return nodeId;
	}


	public float getRelevance() {
		return relevance;
	}


	public void setRelevance(float relevance) {
		this.relevance = relevance;
	}
	
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append(term).append(" => ").append(rank);
		return sb.toString();
	}
}
