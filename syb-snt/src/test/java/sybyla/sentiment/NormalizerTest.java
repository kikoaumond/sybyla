package sybyla.sentiment;

import static org.junit.Assert.*;

import org.junit.Test;

public class NormalizerTest {

	@Test
	public void testNormalizeEllipsis() {
		String s =  "This is a phrase.  This is an ellipsis...";
		String n =  Normalizer.normalizeEllipsis(s);
		String e = "This is a phrase.  This is an ellipsis ... ";
		assertEquals(e,n);
	}
	
	@Test
	public void testNormalizeEllipsis2() {
		String s =  "This is a phrase.  This is an ellipsis..";
		String n =  Normalizer.normalizeEllipsis(s);
		String e = "This is a phrase.  This is an ellipsis ... ";
		assertEquals(e,n);
	}
	
	@Test
	public void testNormalizeEllipsis3() {
		String s =  "This is a phrase.  This is an ellipsis....";
		String n =  Normalizer.normalizeEllipsis(s);
		String e = "This is a phrase.  This is an ellipsis ... ";
		assertEquals(e,n);
	}
	
	@Test
	public void testNormalizeEllipsis4() {
		String s =  "This is an ellipsis... This is another ellipsis....";
		String n =  Normalizer.normalizeEllipsis(s);
		String e =  "This is an ellipsis ...  This is another ellipsis ... ";
		assertEquals(e,n);
	}
	
	@Test
	public void testNormalizeEllipsisAndFullStop() {
		String s =  "This is a phrase.  This is an ellipsis....";
		String n =  Normalizer.normalizeEllipsis(s);
		String e = "This is a phrase.  This is an ellipsis ... ";
		assertEquals(e,n);
		n =  Normalizer.normalizeFullStop(n);
		e = "This is a phrase   This is an ellipsis ... ";
		assertEquals(e,n);

	}
	
	@Test
	public void testNormalizeInterrogations() {
		String s =  "O que é isso?";
		String n =  Normalizer.normalizeInterrogations(s);
		String e = "O que é isso ? ";
		assertEquals(e,n);
		
	}
	
	@Test
	public void testNormalizeInterrogations2() {
		String s =  "O que é isso??";
		String n =  Normalizer.normalizeInterrogations(s);
		String e = "O que é isso ?? ";
		assertEquals(e,n);
		
	}
	
	@Test
	public void testNormalizeInterrogations3() {
		String s =  "O que é isso???";
		String n =  Normalizer.normalizeInterrogations(s);
		String e = "O que é isso ?? ";
		assertEquals(e,n);
		
	}
	
	@Test
	public void testNormalizeExclamations() {
		String s =  "Adorei!!! Muito Legal !";
		String n =  Normalizer.normalizeExclamations(s);
		String e = "Adorei !!  Muito Legal  ! ";
		assertEquals(e,n);
		
	}
	
	@Test
	public void testNormalizeExclamationInterrogationCombos() {
		String s =  "O que é isso???!";
		s =  Normalizer.normalizeInterrogations(s);
		s = Normalizer.normalizeExclamations(s);
		String n =  Normalizer.normalizeExclamationInterrogationCombos(s);
		String e = "O que é isso  ?!  ";
		assertEquals(e,n);
		
	}
	
	@Test
	public void testNormalizeExclamationInterrogationCombos2() {
		String s =  "O que é isso ?? ?!";
		s =  Normalizer.normalizeInterrogations(s);
		s = Normalizer.normalizeExclamations(s);
		String n =  Normalizer.normalizeExclamationInterrogationCombos(s);
		String e = "O que é isso   ?!  ";
		assertEquals(e,n);
		
	}
	
	@Test
	public void testNormalize() {
		String s =  "O que é isso ?? ?! Nem dá para acreditar, não é? Mas fazer o que: Nem adianta reclamar.  a Globo manda neste país...";
		String n =  Normalizer.normalize(s);
		String e = "o que é isso ?! nem dá para acreditar não é ? mas fazer o que nem adianta reclamar a globo manda neste país ...";
		assertEquals(e,n);
		
	}

}
