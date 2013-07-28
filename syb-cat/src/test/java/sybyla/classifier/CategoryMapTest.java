package sybyla.classifier;

import static org.junit.Assert.*;

import org.junit.Test;

public class CategoryMapTest {

	@Test
	public void testRemoveParentheses(){
		String t = "Tom Moore (actor)";
		String p = CategoryMap.removeParentheses(t);
		assertEquals("Tom Moore",p);
	}

}
