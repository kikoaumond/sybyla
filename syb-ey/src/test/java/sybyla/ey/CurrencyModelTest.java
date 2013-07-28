package sybyla.ey;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

import org.junit.Test;

public class CurrencyModelTest {

	@Test
	public void testPattern() {
		
		String text = "- O valor de R$ 169.000,00(cento e sessenta e nove mil reais), correspondente a 1.300 horas (Sr. Gilmar Perdigão), permanece inalterado, porém, o valor por hora que era de R$ 130,00, passa a ser de R$ 145,00, devido ao reajuste de 10,35%, em virtude do aniversário do referido contrato.";
		
		Set<String> expected = new HashSet<String>();
		
		expected.add("R$ 169.000,00");
		expected.add("R$ 130,00,");
		expected.add("R$ 145,00,");
		
		Set<Double> expectedValues = new HashSet<Double>();
		
		expectedValues.add(169000.00);
		expectedValues.add(130.00);
		expectedValues.add(145.00);
		
		Matcher matcher  = CurrencyModel.CURRENCY_PATTERN.matcher(text);
		
		while (matcher.find()){
			String c =  matcher.group();
			assertTrue(expected.contains(c));
			double d = CurrencyModel.convert(c);
			assertTrue(expectedValues.contains(d));
			System.out.println(c+"=>"+d);
		}
		
	}
	
	
	
	@Test
	public void testConvert() {
		String s = "R$ 765.393.96";
		double d =  CurrencyModel.convert(s);
		System.out.println(d);
		assertTrue(d==765393.96);
	}
	
	@Test
	public void testFindCurrencies() {
		
		String text = "- O valor de R$ 169.000,00(cento e sessenta e nove mil reais), correspondente a 1.300 horas (Sr. Gilmar Perdigão), permanece inalterado, porém, o valor por hora que era de R$ 130,00, passa a ser de R$ 145,00, devido ao reajuste de 10,35%, em virtude do aniversário do referido contrato.";
		
		Set<String> expected = new HashSet<String>();
		
		expected.add("R$ 169.000,00");
		expected.add("R$ 130,00");
		expected.add("R$ 145,00");
		
		Set<Double> expectedValues = new HashSet<Double>();
		
		expectedValues.add(169000.00);
		expectedValues.add(130.00);
		expectedValues.add(145.00);
		
		List<String> currencies =  CurrencyModel.findCurrencies(text);

		for(String c: currencies){
			assertTrue(expected.contains(c));
			double d = CurrencyModel.convert(c);
			assertTrue(expectedValues.contains(d));
			System.out.println(c+"=>"+d);
		}
		
	}
	@Test
	public void testBadValue(){
		double d =CurrencyModel.convert("$45,,00");
		assertTrue(d==0);
		System.out.println(d);
	}

}
