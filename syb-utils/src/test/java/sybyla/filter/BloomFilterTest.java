package sybyla.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Random;

import org.junit.Before;
import org.junit.Test;

public class BloomFilterTest {
	private static BloomFilter<String> filter;
	private static Random random;
	
	@Before
	public void setUp(){
		filter = new BloomFilter<String>(10000000,1000000);
		random = new Random();

	}
	@Test
	public void test() {
		int falsePositives = 0;
		int n= 1000000;
		for (int i=1;i<=n;i++){
			String term = "term"+i;
			filter.add(term);
			int r =  random.nextInt();
			String query = "term"+r;
			boolean contained = filter.contains(query);
			if (r>0 && r<=i){
				assertTrue(contained);
			} else {
				if(contained){
					falsePositives++;
				}
			}
		}
		System.out.println("Bloom Filter false positives: "+falsePositives+" out of "+n);
	}

}
