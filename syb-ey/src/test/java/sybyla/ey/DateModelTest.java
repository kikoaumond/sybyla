package sybyla.ey;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.regex.Matcher;

import org.junit.Test;

public class DateModelTest {

	@Test
	public void testDatePattern() {
		Matcher m = DateModel.DATE_PATTERN.matcher("02/03/2011");
		assertTrue(m.find());
		String s = m.group();
		System.out.println(s);
		
		m = DateModel.DATE_PATTERN.matcher("2/3/2011");
		assertTrue(m.find());
		s = m.group();
		System.out.println(s);
		
		m = DateModel.DATE_PATTERN.matcher("2/3/11");
		assertTrue(m.find());
		s = m.group();
		System.out.println(s);
		
		m = DateModel.DATE_PATTERN.matcher("2/3/98");
		assertTrue(m.find());
		s = m.group();
		System.out.println(s);
		
		m = DateModel.DATE_PATTERN.matcher("2/3/1998");
		assertTrue(m.find());
		s = m.group();
		System.out.println(s);
		
		m = DateModel.DATE_PATTERN.matcher("31/05/1998");
		assertTrue(m.find());
		s = m.group();
		System.out.println(s);
	}

	
	@Test
	public void testGetDate() {
		Matcher m = DateModel.DATE_PATTERN.matcher("02/03/2011");
		assertTrue(m.find());
		String s = m.group();
		System.out.println(s);
		Date d =  DateModel.getDate(s);
		assertNotNull( d);

		
		m = DateModel.DATE_PATTERN.matcher("2/3/2011");
		assertTrue(m.find());
		s = m.group();
		System.out.println(s);
		d =  DateModel.getDate(s);
		assertNotNull( d);

		
		m = DateModel.DATE_PATTERN.matcher("2/3/11");
		assertTrue(m.find());
		s = m.group();
		System.out.println(s);
		d =  DateModel.getDate(s);
		assertNotNull( d);

		
		m = DateModel.DATE_PATTERN.matcher("2/3/98");
		assertTrue(m.find());
		s = m.group();
		System.out.println(s);
		d =  DateModel.getDate(s);
		assertNotNull( d);

		
		m = DateModel.DATE_PATTERN.matcher("2/3/1998");
		assertTrue(m.find());
		s = m.group();
		System.out.println(s);
		d =  DateModel.getDate(s);
		assertNotNull( d);

		
		m = DateModel.DATE_PATTERN.matcher("31/05/1998");
		assertTrue(m.find());
		s = m.group();
		System.out.println(s);
		d =  DateModel.getDate(s);
		assertNotNull( d);

	}
	
	@Test
	public void testDateFormat2(){
		String t = "São Paulo, 26 de Junho de 2012";
		t=PartsModel.normalize(t);
		
		Matcher m = DateModel.DATE_PATTERN_2.matcher(t);
		assertTrue(m.find());
		String s = m.group();
		System.out.println(s);
	}
}
