package sybyla.sentiment;

public class Result{
	
	private double positive=0;
	private double negative=0;
	private double neutral=0;

	private double result=0;
	private double certainty=0;
	
	public Result(double result, double certainty){
		this.result=result;
		this.certainty=certainty;
	}
	
	public Result(double positive, double negative, double neutral){
		
		this.positive = positive;
		this.negative = negative;
		this.neutral =  neutral;
		
		double r =  (positive>negative)?positive:negative;
		r = (r>neutral)?r:neutral;
		
		if (positive ==  negative){
			result = 0;
			certainty = 0.5d;
		} else {
			if (r ==  positive){
					
				result = 1;
				certainty =  1/(1 + Math.exp(negative-positive) + Math.exp(neutral - positive));
				
			} else if (r ==  negative){
				
				result = -1;
				certainty =  1/(1 + Math.exp(positive-negative) + Math.exp(neutral - negative));

			} else if (r == neutral){
				result = 0;
				certainty =  1/(1 + Math.exp(positive-neutral) + Math.exp(negative - neutral));

			}
		}
	}

	public double getPositive() {
		return positive;
	}

	public double getNegative() {
		return negative;
	}
	
	public double getNeutral() {
		return neutral;
	}

	public double getResult() {
		return result;
	}

	public double getCertainty() {
		return certainty;
	}
}


