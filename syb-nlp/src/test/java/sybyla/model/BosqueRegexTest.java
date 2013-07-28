package sybyla.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class BosqueRegexTest {
	
	@Test
	public void test1(){
		String s = "==H:prop('PT' M S)	PT";
		String[] p = BosqueRegex.parse(s);
		assertEquals("PT",p[0]);
		assertEquals("prop",p[1]);
	}
	
	@Test
	public void test2(){
		String s = "	==P<:np";
		String[] p = BosqueRegex.parse(s);
		assertNull(p);
	}

}
