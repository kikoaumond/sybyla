package sybyla.ey;

public class Tag implements Comparable<Tag>{
	
	private int begin;
	private int end;
	private String tag;

	public Tag(String tag, int begin, int end) {
		this.tag=tag;
		this.begin=begin;
		this.end=end;
	}

	@Override
	public int compareTo(Tag o) {
		int diff = this.begin -  o.end;
		if (diff == 0){
			diff =  this.end - o.end;
		}
		return diff;
	}

	public int getBegin() {
		return begin;
	}

	public int getEnd() {
		return end;
	}

	public String getTag() {
		return tag;
	}
}