package sybyla.sentiment;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import sybyla.io.FastFileReader;

public class AnalyzerTest {
	@Test
	public void testNull() throws Exception{
		Analyzer analyzer = new Analyzer(Language.PORTUGUESE, Type.PRODUCT);
		double s =  analyzer.analyze(null);
		assertTrue(s==0);
	}
	
	@Test 
	public void test1() throws Exception{
		Analyzer analyzer = new Analyzer(Language.PORTUGUESE, Type.PRODUCT);
		String text = loadFile("src/test/resources/samsung.txt");
		double s =  analyzer.analyze(text);
		System.out.println("Score: "+s);
		assertTrue(s>0);
	}
	
	@Test
	public void testAnalyze() throws Exception{
		Analyzer analyzer = new Analyzer(Language.PORTUGUESE, Type.PRODUCT);
		String text = "Em termos de hardware os Galaxy S já vem batendo há algum tempo já, mas isso se reflete tbm em um produto com acabamento de menor qualidade, um SO ainda menos confiavel e preços ainda nas alturas!!! " +
						"Mas sem duvidas que sem uma renovação a Apple vai ficando pra trás em termos de criatividade!!! =/ ";
		double score = analyzer.analyze(text);
		System.out.println("Score: "+ score);
		assertTrue(score < 0);
	}
	
	@Test
	public void testAnalyze2() throws Exception{
		Analyzer analyzer = new Analyzer(Language.PORTUGUESE, Type.PRODUCT);
		
		String text = "‘Windows Phone 7 is currently supporting only for Samsung devices but we are planning to expand the service to the other devices soon.\n"+
		"You can download ChatON with your Samsung Devices at the Samsung Zone in the Market Place.\n"+
		"We hope you will enjoy ChatON’s cross platform experience with your Samsung Windows Phone.’\n"+

		"Boa, Samsung! (Só que não)\n"+

		"Seria legal uma observação no post, quanto a disponibilidade do app para Windows Phone.";
		
		double score = analyzer.analyze(text);
		System.out.println("Score: "+score);
		assertTrue(score < 0);
	}
	
	@Test
	public void testAnalyze3() throws Exception{
		Analyzer analyzer = new Analyzer(Language.PORTUGUESE, Type.PRODUCT);
		
		String text = "óoooooooodiooooooooooooo!!!! O PIOR É LIGAR PRA LÁ, PRA ASSISTENCIA E SABER Q: \"NÃO SR, PELO VISTO VAI SER CARO O REPARO. NÃO DEVE SER SÓ UM FUSIL NÃO. 1 ano e 3 meses por um produtor de 7.000 mil reais. A GENTE É MUITO ÓTÁRIO MSM.";
		
		double score = analyzer.analyze(text);
		System.out.println("Score: "+score);
		assertTrue(score < 0);
	}
	
	@Test
	public void testAnalyze4() throws Exception{
		Analyzer analyzer = new Analyzer(Language.PORTUGUESE, Type.PRODUCT);
		
		String text = "Com tantos problemas de atendimento ao clientes de grandes corporações tenho que admitir que uma delas me surpreende toda vez que ligo lá pela excelência em que faz o atendimento.\n"+
				"A Samsung atende como deveria todos atenderem, e não foi uma vez, digo porque já liguei lá algumas vezes e em todas fui muito bem atendido.\n"+
				"Então fica aqui o ELOGIO a essa empresa.";
		
		double score = analyzer.analyze(text);
		System.out.println("Score: "+score);
		assertTrue(score > 0);
	}
	
	@Test
	public void testAnalyze5() throws Exception{
		Analyzer analyzer = new Analyzer(Language.PORTUGUESE, Type.PRODUCT);
		
		String text = "apesar de gostarmos da Samsung, o produt é ruim.";
		
		double score = analyzer.analyze(text);
		System.out.println("Score: "+score);
		assertTrue(score < 0);
	}
	
	@Test
	public void testAnalyze6() throws Exception{
		Analyzer analyzer = new Analyzer(Language.PORTUGUESE, Type.PRODUCT);
		
		String text = "O produto funcionou mal";
		
		double score = analyzer.analyze(text);
		System.out.println("Score: "+score);
		assertTrue(score < 0);
	}
	
	
	private String loadFile(String file) throws IOException{
		
		FastFileReader fileReader =  new FastFileReader(file);
		String line;
		StringBuilder sb = new StringBuilder();
		while((line=fileReader.readLine()) != null){
				sb.append(line+"\n");
		}
		return sb.toString();
		
	}
}
