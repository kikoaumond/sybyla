package sybyla.sentiment;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.junit.Test;

public class AnalyzerTest {
	@Test
	public void testNull() throws Exception{
		Analyzer analyzer = new Analyzer(Language.PORTUGUESE, Type.PRODUCT);
		Result r =  analyzer.analyze(null);
		double s = r.getResult(); 
		assertTrue(s==0);
	}
	
	@Test 
	public void test1() throws Exception{
		Analyzer analyzer = new Analyzer(Language.PORTUGUESE, Type.PRODUCT);
		String text = loadFile("src/test/resources/samsung.txt");
		Result r =  analyzer.analyze(text);
		double s = r.getResult(); 
		System.out.println("Score: "+s);
		assertTrue(s>0);
	}
	
	@Test
	public void testAnalyze() throws Exception{
		Analyzer analyzer = new Analyzer(Language.PORTUGUESE, Type.PRODUCT);
		String text = "Em termos de hardware os Galaxy S já vem batendo há algum tempo já, mas isso se reflete tbm em um produto com acabamento de menor qualidade, um SO ainda menos confiavel e preços ainda nas alturas!!! " +
						"Mas sem duvidas que sem uma renovação a Apple vai ficando pra trás em termos de criatividade!!! =/ ";
		Result r =  analyzer.analyze(text);
		double score = r.getResult(); 
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
		
		Result r =  analyzer.analyze(text);
		double score = r.getResult(); 
		System.out.println("Score: "+score);
		assertTrue(score < 0);
	}
	
	@Test
	public void testAnalyze3() throws Exception{
		Analyzer analyzer = new Analyzer(Language.PORTUGUESE, Type.PRODUCT);
		
		String text = "óoooooooodiooooooooooooo!!!! O PIOR É LIGAR PRA LÁ, PRA ASSISTENCIA E SABER Q: \"NÃO SR, PELO VISTO VAI SER CARO O REPARO. NÃO DEVE SER SÓ UM FUSIL NÃO. 1 ano e 3 meses por um produtor de 7.000 mil reais. A GENTE É MUITO ÓTÁRIO MSM.";
		
		Result r =  analyzer.analyze(text);
		double score = r.getResult(); 
		System.out.println("Score: "+score);
		assertTrue(score < 0);
	}
	
	@Test
	public void testAnalyze4() throws Exception{
		Analyzer analyzer = new Analyzer(Language.PORTUGUESE, Type.PRODUCT);
		
		String text = "Com tantos problemas de atendimento ao clientes de grandes corporações tenho que admitir que uma delas me surpreende toda vez que ligo lá pela excelência em que faz o atendimento.\n"+
				"A Samsung atende como deveria todos atenderem, e não foi uma vez, digo porque já liguei lá algumas vezes e em todas fui muito bem atendido.\n"+
				"Então fica aqui o ELOGIO a essa empresa.";
		
		Result r =  analyzer.analyze(text);
		double score = r.getResult(); 
		System.out.println("Score: "+score);
		assertTrue(score > 0);
	}
	
	@Test
	public void testAnalyze5() throws Exception{
		Analyzer analyzer = new Analyzer(Language.PORTUGUESE, Type.PRODUCT);
		
		String text = "apesar de gostarmos da Samsung, o produt é ruim.";
		
		Result r =  analyzer.analyze(text);
		double score = r.getResult(); 
		System.out.println("Score: "+score);
		assertTrue(score < 0);
	}
	
	
	@Test
	public void testAnalyze6() throws Exception{
		Analyzer analyzer = new Analyzer(Language.PORTUGUESE, Type.PRODUCT);
		
		String text = "O produto funcionou mal";
		
		Result r =  analyzer.analyze(text);
		double score = r.getResult(); 
		System.out.println("Score: "+score);
		assertTrue(score < 0);
	}
	
	@Test
	public void testAnalyze7() throws Exception{
		Analyzer analyzer = new Analyzer(Language.PORTUGUESE, Type.PRODUCT);
		
		String text = "	Mas, vc não acha que esse \"tratamento\" deveria ser muito mais rápido? Desde de maio estou com problemas e não recebo respostas... Estou decepcionada, sou cliente da tecla há quase 10 anos, e nunca tive problemas...Agora a Mandic está deixando a desejar, os telefone de contato da cobrança não atendem, no chat, não atendem, faço registro não respondem... ";
		
		Result r =  analyzer.analyze(text);
		double score = r.getResult(); 
		System.out.println("Score: "+score);
		assertTrue(score < 0);
	}
	
	@Test
	public void testAnalyze8() throws Exception{
		Analyzer analyzer = new Analyzer(Language.PORTUGUESE, Type.PRODUCT);
		
		String text = "O produto não funcionou.";
		
		Result r =  analyzer.analyze(text);
		double score = r.getResult(); 
		System.out.println("Score: "+score);
		assertTrue(score < 0);
	}
	
	@Test
	public void testAnalyze9() throws Exception{
		Analyzer analyzer = new Analyzer(Language.PORTUGUESE, Type.PRODUCT);
		
		String text = "	Mas, vc não acha que esse \"tratamento\" deveria ser muito mais rápido? Desde de maio estou com problemas e não recebo respostas... Estou decepcionada, sou cliente da tecla há quase 10 anos, e nunca tive problemas...Agora a Mandic está deixando a desejar, os telefone de contato da cobrança não atendem, no chat, não atendem, faço registro não respondem... ";
		
		Result r =  analyzer.analyze(text);
		double score = r.getResult(); 
		System.out.println("Score: "+score);
		assertTrue(score < 0);
	}
	private String loadFile(String file) throws IOException{
		
		BufferedReader fileReader =  new BufferedReader(new InputStreamReader(new FileInputStream(new File(file))));
		String line;
		StringBuilder sb = new StringBuilder();
		while((line=fileReader.readLine()) != null){
				sb.append(line+"\n");
		}
		return sb.toString();
		
	}

	@Test
	public void testAnalyze10() throws Exception{
		Analyzer analyzer = new Analyzer(Language.PORTUGUESE, Type.PRODUCT);
		
		String text = "To precisando de uns dias na praia. Nao agüento mais esse tempo maluco de Sp.";
		
		Result r =  analyzer.analyze(text);
		double score = r.getResult(); 
		System.out.println("Score: "+score);
		assertTrue(score < 0);
	}
	
	@Test
	public void testAnalyze11() throws Exception{
		Analyzer analyzer = new Analyzer(Language.PORTUGUESE, Type.PRODUCT);
		
		String text = "Sabia que seu Facebook tem pastas e mensagens escondidas? Veja aqui como acessar e ver se tem algo lá que preste ";
		
		Result r =  analyzer.analyze(text);
		double score = r.getResult(); 
		System.out.println("Score: "+score);
		assertTrue(score == -1);
	}
	
	@Test
	public void testAnalyze12() throws Exception{
		Analyzer analyzer = new Analyzer(Language.PORTUGUESE, Type.PRODUCT);
		
		String text = "Demetrius Brasil ‏@demetriusbrasil 15 abr @MandicSA sempre disponível #só_QUE_não ";
		
		Result r =  analyzer.analyze(text);
		double score = r.getResult(); 
		System.out.println("Score: "+score);
		assertTrue(score < 0);
	}
	
	@Test
	public void testAnalyze13() throws Exception{
		Analyzer analyzer = new Analyzer(Language.PORTUGUESE, Type.PRODUCT);
		
		String text = "	Gente, não sei pq tanta celeuma em relação ao Jeff Bezos comprar o The Washington Post... " +
				"Todo mundo sabe que ele é...o DEMÔNIO! " +
				"Claramente comprou o \"Post\" somente para destruir toda a categoria dos jornais, mesma coisa que fez com as livrarias! " +
				"E gastar US$ 250 milhões do próprio bolso para ajudar a destruir o jornalismo vai até que sair barato, certo? " +
				"Dinheiro de pinga...Tb não entendi a repercussão disso hoje na Folha, no Estadão etc... " +
				"É claro que as famílias Frias, Mesquita, Civita, Marinho, JAMAIS usaram suas propriedades para obter benefícios políticos. " +
				"NUNCA, BLASFÊMIA!!! Nem a família Graham, coitada da Katherine! Uma tiazinha sem malícia! " +
				"Imaginar que essas famílias utilizam ou utilizavam os títulos para obter vantagens comerciais e políticas é um absurdo!!! " +
				"Enfim, vamos erguer um protesto contra o Jeff Tinhoso, pq ele se tornou literalmente o emissário das más notícias!";

		
		Result r =  analyzer.analyze(text);
		double score = r.getResult(); 
		System.out.println("Score: "+score);
		assertTrue(score < 0);
	}
	
	@Test
	public void testAnalyze14() throws Exception{
		Analyzer analyzer = new Analyzer(Language.PORTUGUESE, Type.PRODUCT);
		
		String text = "Muito bom! Nossa meta é dobrar o número de associados até o final do ano.";

		Result r =  analyzer.analyze(text);
		double score = r.getResult(); 
		System.out.println("Score: "+score);
		assertTrue(score > 0);
	}
	
	@Test
	public void testAnalyze15() throws Exception{
		Analyzer analyzer = new Analyzer(Language.PORTUGUESE, Type.PRODUCT);
		
		String text = "Excelente vídeo sobre finanças pessoais, com o querido Ricardo Amorim";

		Result r =  analyzer.analyze(text);
		double score = r.getResult(); 
		System.out.println("Score: "+score);
		assertTrue(score > 0);
	}
	
	@Test
	public void testAnalyze16() throws Exception{
		Analyzer analyzer = new Analyzer(Language.PORTUGUESE, Type.PRODUCT);
		
		String text = "Sandro J. S. Souza ‏@xkurts 1 ago Ate agora, decepcao total esse email marketing da XXXX";

		Result r =  analyzer.analyze(text);
		double score = r.getResult(); 
		System.out.println("Score: "+score);
		assertTrue(score < 0);
	}
	
	@Test
	public void testAnalyze17() throws Exception{
		Analyzer analyzer = new Analyzer(Language.PORTUGUESE, Type.PRODUCT);
		
		String text = "Gustavo Henrique ‏@gugahb 1 ago Trabalhar com as PIORES empresas não tem preço para todas as outras existe a @MandicSA com ela sempre tem como piorar.";

		Result r =  analyzer.analyze(text);
		double score = r.getResult(); 
		System.out.println("Score: "+score);
		assertTrue(score < 0);
	}
	
	@Test
	public void testAnalyze18() throws Exception{
		Analyzer analyzer = new Analyzer(Language.PORTUGUESE, Type.PRODUCT);
		
		String text = "	meu site http://www.amandacosta.com.br  fora do ar frequentemente e hoje direto, " +
				"solicitei  @MandicSA @MandicSuporte  desde ontem e até agora nada";

		Result r =  analyzer.analyze(text);
		double score = r.getResult(); 
		System.out.println("Score: "+score);
		assertTrue(score < 0);
	}
	
	@Test
	public void testAnalyze19() throws Exception{
		Analyzer analyzer = new Analyzer(Language.PORTUGUESE, Type.PRODUCT);
		
		String text = "Infolinux BR ‏@infolinux 8 maio NÃO RECOMENDO! RT @MandicSA: Vai contratar um serviço de cloud? " +
				"Alguns pontos devem ser levados em consideração: http://bit.ly/11Tv43o ";

		Result r =  analyzer.analyze(text);
		double score = r.getResult(); 
		System.out.println("Score: "+score);
		assertTrue(score < 0);
	}
	
	@Test
	public void testAnalyze20() throws Exception{
		Analyzer analyzer = new Analyzer(Language.PORTUGUESE, Type.PRODUCT);
		
		String text = "Adler Medrado ‏@adlermedrado 7 maio Depois que a @MandicSA absorveu a tecla, " +
				"ocorreram instabilidades e agora não encontro o suporte on line e nem emititr boletos pelo painel.";

		Result r =  analyzer.analyze(text);
		double score = r.getResult(); 
		System.out.println("Score: "+score);
		assertTrue(score < 0);
	}
	
	@Test
	public void testAnalyze21() throws Exception{
		Analyzer analyzer = new Analyzer(Language.PORTUGUESE, Type.PRODUCT);
		
		String text = "Adler Medrado ‏@adlermedrado 7 maio Depois que a @MandicSA absorveu a tecla, " +
				"ocorreram instabilidades e agora não encontro o suporte on line e nem emititr boletos pelo painel.";

		Result r =  analyzer.analyze(text);
		double score = r.getResult(); 
		System.out.println("Score: "+score);
		assertTrue(score < 0);
	}
	
	@Test
	public void testAnalyze22() throws Exception{
		Analyzer analyzer = new Analyzer(Language.PORTUGUESE, Type.PRODUCT);
		
		String text = "	Amo o atendimento dessa merda de FIAT";

		Result r =  analyzer.analyze(text);
		double score = r.getResult(); 
		System.out.println("Score: "+score);
		assertTrue(score < 0);
	}
	
	@Test
	public void testAnalyze23() throws Exception{
		Analyzer analyzer = new Analyzer(Language.PORTUGUESE, Type.PRODUCT);
		
		String text = "Que final de domingo é esse, amigos da rede globo?!";

		Result r =  analyzer.analyze(text);
		double score = r.getResult(); 
		System.out.println("Score: "+score);
		assertTrue(score < 0);
	}
	
	@Test
	public void testAnalyze24() throws Exception{
		Analyzer analyzer = new Analyzer(Language.PORTUGUESE, Type.PRODUCT);
		
		String text = 	"Testemunho do ex-bruxo da rede globo - completo:o pior que nenhum citado se manifesta, porque será?     via youtube.";

		Result r =  analyzer.analyze(text);
		double score = r.getResult(); 
		System.out.println("Score: "+score);
		assertTrue(score < 0);
	}
	
	@Test
	public void testAnalyze25() throws Exception{
		Analyzer analyzer = new Analyzer(Language.PORTUGUESE, Type.PRODUCT);
		
		String text = 	"	Reclame aqui > sky - atendente sky promete e empresa não cumpre acordo     #reclameaqui.";

		Result r =  analyzer.analyze(text);
		double score = r.getResult(); 
		System.out.println("Score: "+score);
		assertTrue(score < 0);
	}
	
	@Test
	public void testAnalyze26() throws Exception{
		Analyzer analyzer = new Analyzer(Language.PORTUGUESE, Type.PRODUCT);
		
		String text = 	"tem boiller? tem sky nos quartos? caitolm.";

		Result r =  analyzer.analyze(text);
		double score = r.getResult(); 
		System.out.println("Score: "+score);
		assertTrue(score == 0);
	}
	
	@Test
	public void testAnalyze27() throws Exception{
		Analyzer analyzer = new Analyzer(Language.PORTUGUESE, Type.PRODUCT);
		
		String text = 	"No telecine pipoca quando eu olhei mais cedo ia passar espantalho um filmaco de terror.";

		Result r =  analyzer.analyze(text);
		double score = r.getResult(); 
		System.out.println("Score: "+score);
		assertTrue(score > 0);
	}
}
