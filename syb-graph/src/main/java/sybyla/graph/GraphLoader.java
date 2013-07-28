package sybyla.graph;

public interface GraphLoader {
	
	public boolean insert(int c, String term, int t, String related, int r);

	public boolean insertBatch(int c, String term, int t, String related, int r);
}
