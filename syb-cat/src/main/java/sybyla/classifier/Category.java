package sybyla.classifier;

public class Category implements Comparable<Category>{
	
	private String name;
	private double score;
	private int hashcode;
	
	public Category(String category, double score){
		this.name =  category;
		this.score =  score;
		this.hashcode =  category.hashCode();
	}
	
	@Override
	public boolean equals(Object o){
		
		if (o == null) return false;
		
		if (!(o instanceof Category)) return false;
		
		Category c= (Category)o;
		if (this.name == null) {
			 if (c.name == null) return true;
			 return false;
		} else {
			if (this.name.equals(c.name)) return true;
		}
		return false;
		
	}
	@Override
	public int compareTo(Category sc) {
		
		if (this.score > sc.score) return 1;
		
		if (this.score < sc.score) return -1;
		
		return 0;
	}

	public String getName() {
		return name;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}
	
	@Override
	public int hashCode(){
		return hashcode;
	}

}
