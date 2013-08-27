package sybyla.ey;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Test;

import sybyla.ey.PartsModel.ScanResult;


public class PartsModelTest {

	public static final Logger LOGGER = Logger.getLogger(PartsModelTest.class);
	
	@Test
	public void testNormalize() {
		
		String text = "Por este instrumento particular, entre as Partes abaixo: "+
					  "AENEAN RHONCUS PURUS S.A., estabelecida na Município de Vazante, do Estado de Minas Gerais, na Rodovia LMG-KM 65,, inscrita no Cadastro Nacional de Pessoas Jurídicas do Ministério da Fazenda sob n° 42.412.651/0010-06, doravante denominada simplesmente CONTRATANTE; e "+
					  "ARCU SCELERISQUE LTDA., estabelecida no Município de Vazante, Estado de Minas Gerais, na Rua Dona Sebastiana, 195, inscrita no Cadastro Nacional de Pessoas Jurídicas do Ministério da Fazenda sob n° 09.467.474/0001-12, doravante denominada simplesmente CONTRATADA. "+
					  "Fica justo e avençado, por seus respectivos representantes legais abaixo, o presente Termo de Aditamento, e que se obrigam a cumprir integralmente, por si e seus sucessores, a saber:";

		String t =  PartsModel.normalize(text);
		String expected = "por este instrumento particular , entre as partes abaixo : "+
				  "aenean rhoncus purus s.a. , estabelecida na município de vazante , do estado de minas gerais , na rodovia lmg - km 65 , , inscrita no cadastro nacional de pessoas jurídicas do ministério da fazenda sob n° 42.412.651/0010 - 06 , doravante denominada simplesmente contratante ; e "+
				  "arcu scelerisque ltda. , estabelecida no município de vazante , estado de minas gerais , na rua dona sebastiana , 195 , inscrita no cadastro nacional de pessoas jurídicas do ministério da fazenda sob n° 09.467.474/0001 - 12 , doravante denominada simplesmente contratada. "+
				  "fica justo e avençado , por seus respectivos representantes legais abaixo , o presente termo de aditamento , e que se obrigam a cumprir integralmente , por si e seus sucessores , a saber :";
		assertEquals(expected, t);
		
		String t2 =  PartsModel.makeReference(text);
		String expected2 = "Por este instrumento particular , entre as Partes abaixo : "+
				  "AENEAN RHONCUS PURUS S.A. , estabelecida na Município de Vazante , do Estado de Minas Gerais , na Rodovia LMG - KM 65 , , inscrita no Cadastro Nacional de Pessoas Jurídicas do Ministério da Fazenda sob n° 42.412.651/0010 - 06 , doravante denominada simplesmente CONTRATANTE ; e "+
				  "ARCU SCELERISQUE LTDA. , estabelecida no Município de Vazante , Estado de Minas Gerais , na Rua Dona Sebastiana , 195 , inscrita no Cadastro Nacional de Pessoas Jurídicas do Ministério da Fazenda sob n° 09.467.474/0001 - 12 , doravante denominada simplesmente CONTRATADA. "+
				  "Fica justo e avençado , por seus respectivos representantes legais abaixo , o presente Termo de Aditamento , e que se obrigam a cumprir integralmente , por si e seus sucessores , a saber :";
		assertEquals(expected2, t2);

	}

	@Test
	public void test1() {
		String text;
		try {
			System.out.println("###########################################################################");
			System.out.println("\nCONTRATO:\n\n");
			text = loadFile("src/test/resources/_2__Aditivo_Contrato_3081_11___GPD.doc.txt");
			System.out.println(text);

			String value = CurrencyModel.getMaxValue(text);
			assertEquals("R$ 431.428,60", value);
			text =  PartsModel.normalize(text);
			String[] words =  text.split("\\s");
			ScanResult scanResult = PartsModel.scanPhrases(PartsModel.DELIM_PRE, words);
			assertNotNull(scanResult);
			assertTrue(scanResult.getMarkers().size()>0);
			
			text = loadFile("src/test/resources/_2__Aditivo_Contrato_3081_11___GPD.doc.txt");
			List<String> patterns =  PartsModel.evaluate(text);
			String t =  PartsModel.normalize(text);
			
			
			List<Tag> dates = DateModel.findDates(text );
			String contractDate = DateModel.findContractDate(dates, t);
			String expected = "26 de junho de 2012";
			assertEquals(expected, contractDate);
			String[] beginEndDates=DateModel.findBeginEndDates(dates, text);
			assertEquals("02/03/2011",beginEndDates[0]);
			assertEquals("02/03/2013",beginEndDates[1]);

			assertNotNull(patterns);
			assertTrue(patterns.size()>0);
			assertTrue(patterns.contains("AENEAN RHONCUS PURUS S.A."));
			assertTrue(patterns.contains("ARCU SCELERISQUE LTDA."));
			
			System.out.println("\nPARTE 1:"+patterns.get(0)+"\n");
			System.out.println("\nPARTE 2:"+patterns.get(1)+"\n");
			System.out.println("\nVALOR: "+value+"\n");
			System.out.println("\nDATA DO CONTRATO: "+contractDate+"\n");
			System.out.println("\nINÍCIO DA VIGÊNCIA: "+beginEndDates[0]+"\n");
			System.out.println("\nTÉRMINO DA VIGÊNCIA: "+beginEndDates[1]+"\n");

			System.out.println("###########################################################################");

			

		} catch (IOException e) {
			LOGGER.error("Error running test", e);
			fail();
		}
	}
	
	@Test
	public void test2() {
		String text;
		try {
			System.out.println("###########################################################################");
			System.out.println("\nCONTRATO:\n\n");
			text = loadFile("src/test/resources/_ad_03_3025_09_01.06.12_versao_final.doc.txt");
			System.out.println(text);
			String value = CurrencyModel.getMaxValue(text);
			assertEquals("R$ 1.250.317,45", value);
			List<String> patterns =  PartsModel.evaluate(text);
			
			String t = PartsModel.normalize(text);
			List<Tag> dates = DateModel.findDates(text);
			String contractDate = DateModel.findContractDate(dates, t);
			String expected = "18de maio de 2012";
			assertEquals(expected, contractDate);
			String[] beginEndDates=DateModel.findBeginEndDates(dates, text);
			

			
			System.out.println("\nPARTE 1:"+patterns.get(0)+"\n");
			System.out.println("\nPARTE 2:"+patterns.get(1)+"\n");
			System.out.println("\nVALOR: "+value+"\n");
			System.out.println("\nDATA DO CONTRATO: "+contractDate+"\n");
			System.out.println("\nINÍCIO DA VIGÊNCIA: "+beginEndDates[0]+"\n");
			System.out.println("\nTÉRMINO DA VIGÊNCIA: "+beginEndDates[1]+"\n");
			
			

			
			

			System.out.println("###########################################################################");
			assertEquals("01/04/2012",beginEndDates[0]);
			assertEquals("31/12/2012",beginEndDates[1]);
			assertNotNull(patterns);
			assertTrue(patterns.size()>0);
			assertTrue(patterns.contains("AENEAN RHONCUS PURUS S/A"));
			assertTrue(patterns.contains("ARCU SCELERISQUE LTDA"));
			

		} catch (IOException e) {
			LOGGER.error("Error running test", e);
			fail();
		}
	}
	
	
	@Test
	public void test3() {
		String text;
		try {
			System.out.println("###########################################################################");
			System.out.println("\nCONTRATO:\n\n");
			text = loadFile("src/test/resources/_assinado_2652_11 - ASM.txt");
			System.out.println(text);
			
			String value = CurrencyModel.getMaxValue(text);
			assertEquals("R$ 575.967,12", value);
			List<String> patterns =  PartsModel.evaluate(text);

			assertNotNull(patterns);
			assertTrue(patterns.size()>0);
			assertTrue(patterns.contains("AENEAN CIMENTOS SIA"));
			assertTrue(patterns.contains("ARCU SCELERISQUE LTDA"));
			
			List<Tag> dates = DateModel.findDates(text);
			String t =  PartsModel.normalize(text);
			String contractDate = DateModel.findContractDate(dates, t);
			String expected = "18/05/2011";
			assertEquals(expected, contractDate);
			String[] beginEndDates=DateModel.findBeginEndDates(dates, text);
			assertEquals("20/06/2011",beginEndDates[0]);
			assertEquals("20/06/2014",beginEndDates[1]);
			
			System.out.println("\nPARTE 1:"+patterns.get(0)+"\n");
			System.out.println("\nPARTE 2:"+patterns.get(1)+"\n");
			System.out.println("\nVALOR: "+value+"\n");
			System.out.println("\nDATA DO CONTRATO: "+contractDate+"\n");
			System.out.println("\nINÍCIO DA VIGÊNCIA: "+beginEndDates[0]+"\n");
			System.out.println("\nTÉRMINO DA VIGÊNCIA: "+beginEndDates[1]+"\n");

			System.out.println("###########################################################################");

		} catch (IOException e) {
			LOGGER.error("Error running test", e);
			fail();
		}
	}
	
	@Test
	public void test4() {
		String text;
		try {
			System.out.println("###########################################################################");
			System.out.println("\nCONTRATO:\n\n");
			text = loadFile("src/test/resources/_assinado_benedito fabio silverio - loc trabalho_2509201215220600.txt");
			System.out.println(text);
			
			List<String> currencies =  CurrencyModel.findCurrencies(text);
			String value = CurrencyModel.getMaxValue(text);
			assertEquals("", value);
			
			List<String> patterns =  PartsModel.evaluate(text);
			
			System.out.println("\nPARTE 1:"+patterns.get(0)+"\n");
			System.out.println("\nPARTE 2:"+patterns.get(1)+"\n");
			System.out.println("\nVALOR: "+value+"\n");
			
			List<Tag> dates = DateModel.findDates(text);
			String t =  PartsModel.normalize(text);

			String contractDate = DateModel.findContractDate(dates, t);
			String expected = "08 de novembro de 2011";
			assertEquals(expected, contractDate);
			String[] beginEndDates=DateModel.findBeginEndDates(dates, text);
			assertEquals("",beginEndDates[0]);
			assertEquals("",beginEndDates[1]);


			assertNotNull(patterns);
			assertTrue(patterns.size()>0);
			assertTrue(patterns.contains("PELLENTESQUE CONSEQUAT NISI"));
			assertTrue(patterns.contains("ARCU SCELERISQUE FÃBIO Sil.VÉR10"));
			System.out.println("\nDATA DO CONTRATO: "+contractDate+"\n");
			System.out.println("\nINÍCIO DA VIGÊNCIA: "+beginEndDates[0]+"\n");
			System.out.println("\nTÉRMINO DA VIGÊNCIA: "+beginEndDates[1]+"\n");
			
			
			System.out.println("###########################################################################");

		} catch (IOException e) {
			LOGGER.error("Error running test", e);
			fail();
		}
	}
	
	@Test
	public void test5() {
		String text;
		try {
			System.out.println("###########################################################################");
			System.out.println("\nCONTRATO:\n\n");
			text = loadFile("src/test/resources/_assinado_BROOKS EMPREENDIMENTOS LTDA EPP.txt");
			System.out.println(text);
			
			List<String> patterns =  PartsModel.evaluate(text);
			
			String t =  PartsModel.normalize(text);
			List<Tag> dates = DateModel.findDates(text);
			String contractDate = DateModel.findContractDate(dates,t);
			String expected = "09 de agosto de 2012";
			
			String[] beginEndDates=DateModel.findBeginEndDates(dates, text);
			

			
			System.out.println("\nPARTE 1:"+patterns.get(0)+"\n");
			System.out.println("\nPARTE 2:"+patterns.get(1)+"\n");

			
			
			System.out.println("\nDATA DO CONTRATO: "+contractDate+"\n");
			System.out.println("\nINÍCIO DA VIGÊNCIA: "+beginEndDates[0]+"\n");
			System.out.println("\nTÉRMINO DA VIGÊNCIA: "+beginEndDates[1]+"\n");
			
			System.out.println("###########################################################################");

			assertEquals("", contractDate);
			assertNotNull(patterns);
			assertTrue(patterns.size()>0);
			assertEquals("",beginEndDates[0]);
			assertEquals("",beginEndDates[1]);
			//assertTrue(patterns.contains("LOREM IPSUM"));
			//assertTrue(patterns.contains("ARCU SCELERISQUE Ltda"));
			

		} catch (IOException e) {
			LOGGER.error("Error running test", e);
			fail();
		}
	}
	
	@Test
	public void test6() {
		String text;
		try {
			System.out.println("###########################################################################");
			System.out.println("\nCONTRATO:\n\n");
			text = loadFile("src/test/resources/_assinado_Contrato 7207_12_Manserv.txt");
			System.out.println(text);
			
			String value = CurrencyModel.getMaxValue(text);
			assertEquals("R$ 1.523.806,75", value);
			
			List<String> patterns =  PartsModel.evaluate(text);
			
			String t =  PartsModel.normalize(text);
			List<Tag> dates = DateModel.findDates(text);
			String contractDate = DateModel.findContractDate(dates, t);
			String expected = "31 de julho de 2012";
			//assertEquals(expected, contractDate);
			String[] beginEndDates=DateModel.findBeginEndDates(dates, text);
			assertEquals("01/01/2012",beginEndDates[0]);
			assertEquals("",beginEndDates[1]);
			
			System.out.println("\nPARTE 1:"+patterns.get(0)+"\n");
			System.out.println("\nPARTE 2:"+patterns.get(1)+"\n");
			System.out.println("\nVALOR: "+value+"\n");
			System.out.println("\nDATA DO CONTRATO: "+contractDate+"\n");
			System.out.println("\nINÍCIO DA VIGÊNCIA: "+beginEndDates[0]+"\n");
			System.out.println("\nTÉRMINO DA VIGÊNCIA: "+beginEndDates[1]+"\n");
			


			assertNotNull(patterns);
			assertTrue(patterns.size()>0);
			assertTrue(patterns.contains("AENEAN METAIS S/A"));
			assertTrue(patterns.contains("ARCU SCELERISQUE S.A"));
			
			
			System.out.println("###########################################################################");

		} catch (IOException e) {
			LOGGER.error("Error running test", e);
			fail();
		}
	}
	
	@Test
	public void test7() {
		String text;
		try {
			System.out.println("###########################################################################");
			System.out.println("\nCONTRATO:\n\n");
			text = loadFile("src/test/resources/_assinado_Document (4).txt");
			System.out.println(text);
			
			String value = CurrencyModel.getMaxValue(text);
			assertEquals("", value);
			
			List<String> patterns =  PartsModel.evaluate(text);
			
			List<Tag> dates = DateModel.findDates(text);
			String t =  PartsModel.normalize(text);

			String contractDate = DateModel.findContractDate(dates, t);
			String expected = "01 de Janeiro de 2009";
			//assertEquals(expected, contractDate);
			String[] beginEndDates=DateModel.findBeginEndDates(dates, text);
			//assertEquals("01/01/2012",beginEndDates[0]);
			//assertEquals("",beginEndDates[1]);
			
			System.out.println("\nPARTE 1:"+patterns.get(0)+"\n");
			System.out.println("\nPARTE 2:"+patterns.get(1)+"\n");
			System.out.println("\nVALOR: "+value+"\n");
			System.out.println("\nDATA DO CONTRATO: "+contractDate+"\n");
			System.out.println("\nINÍCIO DA VIGÊNCIA: "+beginEndDates[0]+"\n");
			System.out.println("\nTÉRMINO DA VIGÊNCIA: "+beginEndDates[1]+"\n");


			assertNotNull(patterns);
			assertTrue(patterns.size()>0);
			assertTrue(patterns.contains("AENEAN INDUSTRIAL S.A"));
			assertTrue(patterns.contains("ARCU SCELERISQUE S.A."));
			
			
			System.out.println("###########################################################################");

		} catch (IOException e) {
			LOGGER.error("Error running test", e);
			fail();
		}
	}
	
	@Test
	public void test8() {
		String text;
		try {
			System.out.println("###########################################################################");
			System.out.println("\nCONTRATO:\n\n");
			text = loadFile("src/test/resources/_assinado_Document.txt");
			System.out.println(text);
			
			String value = CurrencyModel.getMaxValue(text);
			assertEquals("", value);
			
			List<Tag> dates = DateModel.findDates(text);
			String t =  PartsModel.normalize(text);

			String contractDate = DateModel.findContractDate(dates, t);
			String expected = "24 de junho de 2009";
			assertEquals(expected, contractDate);
			String[] beginEndDates=DateModel.findBeginEndDates(dates, text);
			assertEquals("",beginEndDates[0]);
			assertEquals("",beginEndDates[1]);
			
			List<String> patterns =  PartsModel.evaluate(text);
			
			System.out.println("\nPARTE 1:"+patterns.get(0)+"\n");
			System.out.println("\nPARTE 2:"+patterns.get(1)+"\n");
			System.out.println("\nVALOR: "+value+"\n");
			System.out.println("\nDATA DO CONTRATO: "+contractDate+"\n");
			System.out.println("\nINÍCIO DA VIGÊNCIA: "+beginEndDates[0]+"\n");
			System.out.println("\nTÉRMINO DA VIGÊNCIA: "+beginEndDates[1]+"\n");



			assertNotNull(patterns);
			assertTrue(patterns.size()>0);
			assertTrue(patterns.contains("QUISQUE CURSUS SUL MATO - GROSSENSE LTDA"));
			assertTrue(patterns.contains("ARCU SCELERISQUE RODOVlÁRIOS LTDA"));
			
			
			System.out.println("###########################################################################");

		} catch (IOException e) {
			LOGGER.error("Error running test", e);
			fail();
		}
	}
	
	@Test
	public void test9() {
		String text;
		try {
			System.out.println("###########################################################################");
			System.out.println("\nCONTRATO:\n\n");
			text = loadFile("src/test/resources/_assinado_http   sistemas.votorantim.co...ADITIVO_CONTRATO_G.HADDAD.txt");
			System.out.println(text);
			
			String value = CurrencyModel.getMaxValue(text);
			assertEquals("R$197.401,25", value);
			
			List<String> patterns =  PartsModel.evaluate(text);
			
			List<Tag> dates = DateModel.findDates(text);
			String t =  PartsModel.normalize(text);

			String contractDate = DateModel.findContractDate(dates,t);
			String expected = "03 de janeiro de 2011";
			assertEquals(expected, contractDate);
			String[] beginEndDates=DateModel.findBeginEndDates(dates, text);
			assertEquals("01 de outubro de 2010",beginEndDates[0]);
			assertEquals("30/09/2011",beginEndDates[1]);
			
			System.out.println("\nPARTE 1:"+patterns.get(0)+"\n");
			System.out.println("\nPARTE 2:"+patterns.get(1)+"\n");
			System.out.println("\nVALOR: "+value+"\n");
			System.out.println("\nDATA DO CONTRATO: "+contractDate+"\n");
			System.out.println("\nINÍCIO DA VIGÊNCIA: "+beginEndDates[0]+"\n");
			System.out.println("\nTÉRMINO DA VIGÊNCIA: "+beginEndDates[1]+"\n");

			assertNotNull(patterns);
			assertTrue(patterns.size()>0);
			assertTrue(patterns.contains("G. ARCU SCELERISQUE IMOBILIÁRIOS LTDA"));
			assertTrue(patterns.contains("PELLENTESQUE CONSEQUAT NISI"));
			
			
			System.out.println("###########################################################################");

		} catch (IOException e) {
			LOGGER.error("Error running test", e);
			fail();
		}
	}
	
	@Test
	public void test10() {
		String text;
		try {
			System.out.println("###########################################################################");
			System.out.println("\nCONTRATO:\n\n");
			text = loadFile("src/test/resources/_assinado_Jesusmar J. da Silva - Cont. 796-06.txt");
			System.out.println(text);
			
			String value = CurrencyModel.getMaxValue(text);
			assertEquals("", value);
			
			List<String> patterns =  PartsModel.evaluate(text);
			
			List<Tag> dates = DateModel.findDates(text);
			String t =  PartsModel.normalize(text);

			String contractDate = DateModel.findContractDate(dates, text);
			String expected = "";
			//assertEquals(expected, contractDate);
			String[] beginEndDates=DateModel.findBeginEndDates(dates, text);
			assertEquals("18 de agosto de 2006",beginEndDates[0]);
			assertEquals("17 de agosto de 2.009",beginEndDates[1]);
			
			
			System.out.println("\nPARTE 1:"+patterns.get(0)+"\n");
			System.out.println("\nPARTE 2:"+patterns.get(1)+"\n");
			System.out.println("\nVALOR: "+value+"\n");
			System.out.println("\nDATA DO CONTRATO: "+contractDate+"\n");
			System.out.println("\nINÍCIO DA VIGÊNCIA: "+beginEndDates[0]+"\n");
			System.out.println("\nTÉRMINO DA VIGÊNCIA: "+beginEndDates[1]+"\n");



			assertNotNull(patterns);
			assertTrue(patterns.size()>0);
			assertTrue(patterns.contains("ARCU SCELERISQUE MAR JOAQUIM D SILVA"));
			assertTrue(patterns.contains("AENEAN METAIS NÍQUEL S.A."));
			
			
			System.out.println("###########################################################################");

		} catch (IOException e) {
			LOGGER.error("Error running test", e);
			fail();
		}
	}
	
	@Test
	public void test11() {
		String text;
		try {
			System.out.println("###########################################################################");
			System.out.println("\nCONTRATO:\n\n");
			text = loadFile("src/test/resources/_assinado_Luiz Alberto alves - assinado.txt");
			System.out.println(text);
			/*
			String value = CurrencyModel.getMaxValue(text);
			assertEquals("", value);
			
			List<Tag> dates = DateModel.findDates(text);
			String contractDate = DateModel.findContractDate(dates);
			String expected = "";
			//assertEquals(expected, contractDate);
			String[] beginEndDates=DateModel.findBeginEndDates(dates, text);
			assertEquals("18 de agosto de 2006",beginEndDates[0]);
			assertEquals("17 de agosto de 2.009",beginEndDates[1]);
			
			System.out.println("\nDATA DO CONTRATO: "+contractDate+"\n");
			System.out.println("\nINÍCIO DA VIGÊNCIA: "+beginEndDates[0]+"\n");
			System.out.println("\nTÉRMINO DA VIGÊNCIA: "+beginEndDates[1]+"\n");
			
			
			List<String> patterns =  PartsModel.evaluate(text);
			

			assertNotNull(patterns);
			assertTrue(patterns.size()==0);*/
			
			
			
			System.out.println("###########################################################################");

		} catch (IOException e) {
			LOGGER.error("Error running test", e);
			fail();
		}
	}
	
	@Test
	public void test12() {
		String text;
		try {
			System.out.println("###########################################################################");
			System.out.println("\nCONTRATO:\n\n");
			text = loadFile("src/test/resources/_Assinado_msg_2391_msg_2355_Arrendamento_Faz_Taquarussu__MS.doc.txt");
			System.out.println(text);
			
			String value = CurrencyModel.getMaxValue(text);
			assertEquals("R$ 133.584,00", value);
			
			List<String> patterns =  PartsModel.evaluate(text);
			List<Tag> dates = DateModel.findDates(text);
			String t =  PartsModel.normalize(text);

			String contractDate = DateModel.findContractDate(dates, text);
			String expected = "12 de fevereiro de 2007";
			assertEquals(expected, contractDate);
			String[] beginEndDates=DateModel.findBeginEndDates(dates, text);
			assertEquals("",beginEndDates[0]);
			assertEquals("",beginEndDates[1]);
			
			System.out.println("\nPARTE 1:"+patterns.get(0)+"\n");
			System.out.println("\nPARTE 2:"+patterns.get(1)+"\n");
			System.out.println("\nVALOR: "+value+"\n");
			System.out.println("\nDATA DO CONTRATO: "+contractDate+"\n");
			System.out.println("\nINÍCIO DA VIGÊNCIA: "+beginEndDates[0]+"\n");
			System.out.println("\nTÉRMINO DA VIGÊNCIA: "+beginEndDates[1]+"\n");


			assertNotNull(patterns);
			assertTrue(patterns.size()>0);
			assertTrue(patterns.contains("VIVAMUS UT MAGNA."));
			assertTrue(patterns.contains("ARCU SCELERISQUE"));
			
			
			System.out.println("###########################################################################");

		} catch (IOException e) {
			LOGGER.error("Error running test", e);
			fail();
		}
	}
	
	@Test
	public void test13() {
		String text;
		try {
			System.out.println("###########################################################################");
			System.out.println("\nCONTRATO:\n\n");
			text = loadFile("src/test/resources/_assinado_Nitronel_Ltda_ME_aditivo_1.txt");
			System.out.println(text);
			
			String value = CurrencyModel.getMaxValue(text);
			assertEquals("", value);
			
			List<String> patterns =  PartsModel.evaluate(text);
			
			List<Tag> dates = DateModel.findDates(text);
			String t =  PartsModel.normalize(text);

			String contractDate = DateModel.findContractDate(dates,t);
			String expected = "";
			assertEquals(expected, contractDate);
			String[] beginEndDates=DateModel.findBeginEndDates(dates, text);
			assertEquals("27 de março de 2007",beginEndDates[0]);
			assertEquals("26 de março de 2008",beginEndDates[1]);
			
			System.out.println("\nPARTE 1:"+patterns.get(0)+"\n");
			System.out.println("\nPARTE 2:"+patterns.get(1)+"\n");
			System.out.println("\nVALOR: "+value+"\n");
			System.out.println("\nDATA DO CONTRATO: "+contractDate+"\n");
			System.out.println("\nINÍCIO DA VIGÊNCIA: "+beginEndDates[0]+"\n");
			System.out.println("\nTÉRMINO DA VIGÊNCIA: "+beginEndDates[1]+"\n");


			assertNotNull(patterns);
			assertTrue(patterns.size()>0);
			assertTrue(patterns.contains("AENEAN CIMENTOS LTDA"));
			assertTrue(patterns.contains("ARCU SCELERISQUE LTDA. ME"));
			
			
			System.out.println("###########################################################################");

		} catch (IOException e) {
			LOGGER.error("Error running test", e);
			fail();
		}
	}
	
	@Test
	public void test14() {
		String text;
		try {
			System.out.println("###########################################################################");
			System.out.println("\nCONTRATO:\n\n");
			text = loadFile("src/test/resources/_assinado_PEDROSO ASSINADO.txt");
			System.out.println(text);
			
			String value = CurrencyModel.getMaxValue(text);
			assertEquals("", value);
			
			List<String> patterns =  PartsModel.evaluate(text);
			
			assertNotNull(patterns);
			assertTrue(patterns.size()==0);
			
			
			
			System.out.println("###########################################################################");

		} catch (IOException e) {
			LOGGER.error("Error running test", e);
			fail();
		}
	}
	
	@Test
	public void test15() {
		String text;
		try {
			System.out.println("###########################################################################");
			System.out.println("\nCONTRATO:\n\n");
			text = loadFile("src/test/resources/_assinado_Pelotas.txt");
			System.out.println(text);
			
			String value = CurrencyModel.getMaxValue(text);
			assertEquals("R$ 878,85", value);
			
			List<String> patterns =  PartsModel.evaluate(text);

			List<Tag> dates = DateModel.findDates(text);
			String t =  PartsModel.normalize(text);

			String contractDate = DateModel.findContractDate(dates,t);
			String expected = "02 de janeiro de 2006";
			assertEquals(expected, contractDate);
			String[] beginEndDates=DateModel.findBeginEndDates(dates, text);
			assertEquals("",beginEndDates[0]);
			assertEquals("",beginEndDates[1]);
			
			System.out.println("\nPARTE 1:"+patterns.get(0)+"\n");
			System.out.println("\nPARTE 2:"+patterns.get(1)+"\n");
			System.out.println("\nVALOR: "+value+"\n");
			System.out.println("\nDATA DO CONTRATO: "+contractDate+"\n");
			System.out.println("\nINÍCIO DA VIGÊNCIA: "+beginEndDates[0]+"\n");
			System.out.println("\nTÉRMINO DA VIGÊNCIA: "+beginEndDates[1]+"\n");
			


			assertNotNull(patterns);
			assertTrue(patterns.size()>0);
			assertTrue(patterns.contains("AENEAN CELULOSE E PAPEL S/A"));
			assertTrue(patterns.contains("ARCU SCELERISQUE"));
			
			
			System.out.println("###########################################################################");

		} catch (IOException e) {
			LOGGER.error("Error running test", e);
			fail();
		}
	}
	
	@Test
	public void test16() {
		String text;
		try {
			System.out.println("###########################################################################");
			System.out.println("\nCONTRATO:\n\n");
			text = loadFile("src/test/resources/_assinado_Ronaldo Barreto 1078.txt");
			System.out.println(text);
			
			String value = CurrencyModel.getMaxValue(text);
			assertEquals("R$ 717.934,22", value);
			
			List<String> patterns =  PartsModel.evaluate(text);
			List<Tag> dates = DateModel.findDates(text);
			String t =  PartsModel.normalize(text);

			String contractDate = DateModel.findContractDate(dates,t);
			String expected = "21 de fevereiro de 2011";
			assertEquals(expected, contractDate);
			String[] beginEndDates=DateModel.findBeginEndDates(dates, text);
			assertEquals("",beginEndDates[0]);
			assertEquals("",beginEndDates[1]);
			
			System.out.println("\nPARTE 1:"+patterns.get(0)+"\n");
			System.out.println("\nPARTE 2:"+patterns.get(1)+"\n");
			System.out.println("\nVALOR: "+value+"\n");
			System.out.println("\nDATA DO CONTRATO: "+contractDate+"\n");
			System.out.println("\nINÍCIO DA VIGÊNCIA: "+beginEndDates[0]+"\n");
			System.out.println("\nTÉRMINO DA VIGÊNCIA: "+beginEndDates[1]+"\n");
			


			assertNotNull(patterns);
			assertTrue(patterns.size()>0);
			assertTrue(patterns.contains("QUISQUE CURSUS S.A."));
			assertTrue(patterns.contains("ARCU SCELERISQUE"));
			
			
			System.out.println("###########################################################################");

		} catch (IOException e) {
			LOGGER.error("Error running test", e);
			fail();
		}
	}
	
	@Test
	public void test17() {
		String text;
		try {
			System.out.println("###########################################################################");
			System.out.println("\nCONTRATO:\n\n");
			text = loadFile("src/test/resources/_assinado_Termo de Ades∆o ao Contrato VM 9020 (Formalizado).txt");
			System.out.println(text);
			
			String value = CurrencyModel.getMaxValue(text);
			assertEquals("R$ 97.605,65", value);
			
			List<String> patterns =  PartsModel.evaluate(text);
			
			List<Tag> dates = DateModel.findDates(text);
			String t =  PartsModel.normalize(text);

			String contractDate = DateModel.findContractDate(dates,t);
			String expected = "01 de novembro de 2011";
			assertEquals(expected, contractDate);
			String[] beginEndDates=DateModel.findBeginEndDates(dates, text);
			assertEquals("",beginEndDates[0]);
			assertEquals("",beginEndDates[1]);
			
			System.out.println("\nPARTE 1:"+patterns.get(0)+"\n");
			System.out.println("\nPARTE 2:"+patterns.get(1)+"\n");
			System.out.println("\nVALOR: "+value+"\n");
			System.out.println("\nDATA DO CONTRATO: "+contractDate+"\n");
			System.out.println("\nINÍCIO DA VIGÊNCIA: "+beginEndDates[0]+"\n");
			System.out.println("\nTÉRMINO DA VIGÊNCIA: "+beginEndDates[1]+"\n");
			
			assertNotNull(patterns);
			assertTrue(patterns.size()>0);
			assertTrue(patterns.contains("ARCU SCELERISQUE LTDA."));
			assertTrue(patterns.contains("AENEAN METAIS PARTICIPAÇÕES LTDA."));
			
			
			System.out.println("###########################################################################");

		} catch (IOException e) {
			LOGGER.error("Error running test", e);
			fail();
		}
	}
	
	@Test
	public void test18() {
		String text;
		try {
			System.out.println("###########################################################################");
			System.out.println("\nCONTRATO:\n\n");
			text = loadFile("src/test/resources/_assinado_Victor  Hugo.txt");
			System.out.println(text);
			
			String value = CurrencyModel.getMaxValue(text);
			assertEquals("", value);
			
			List<String> patterns =  PartsModel.evaluate(text);
			
			List<Tag> dates = DateModel.findDates(text);
			String t =  PartsModel.normalize(text);

			String contractDate = DateModel.findContractDate(dates,t);
			String expected = "20 de abril de 2012";
			assertEquals(expected, contractDate);
			String[] beginEndDates=DateModel.findBeginEndDates(dates, text);
			assertEquals("",beginEndDates[0]);
			assertEquals("",beginEndDates[1]);
			
			System.out.println("\nPARTE 1:"+patterns.get(0)+"\n");
			System.out.println("\nPARTE 2:"+patterns.get(1)+"\n");
			System.out.println("\nVALOR: "+value+"\n");
			System.out.println("\nDATA DO CONTRATO: "+contractDate+"\n");
			System.out.println("\nINÍCIO DA VIGÊNCIA: "+beginEndDates[0]+"\n");
			System.out.println("\nTÉRMINO DA VIGÊNCIA: "+beginEndDates[1]+"\n");

			assertNotNull(patterns);
			assertTrue(patterns.size()>0);
			assertTrue(patterns.contains("- QUISQUE CURSUS CELULOSE SIA"));
			assertTrue(patterns.contains("- ARCU SCELERISQUE"));
			
			
			System.out.println("###########################################################################");

		} catch (IOException e) {
			LOGGER.error("Error running test", e);
			fail();
		}
	}
	
	@Test
	public void test19() {
		String text;
		try {
			System.out.println("###########################################################################");
			System.out.println("\nCONTRATO:\n\n");
			text = loadFile("src/test/resources/_ASTROGILDO_COELHO_DA_CUNHA.doc.txt");
			System.out.println(text);
			
			String value = CurrencyModel.getMaxValue(text);
			assertEquals("R$ 22,20", value);
			
			List<String> patterns =  PartsModel.evaluate(text);
			
			List<Tag> dates = DateModel.findDates(text);
			String t =  PartsModel.normalize(text);

			String contractDate = DateModel.findContractDate(dates,t);
			String expected = "03 de setembro de 2008";
			assertEquals(expected, contractDate);
			String[] beginEndDates=DateModel.findBeginEndDates(dates, text);
			assertEquals("",beginEndDates[0]);
			assertEquals("",beginEndDates[1]);
			
			System.out.println("\nPARTE 1:"+patterns.get(0)+"\n");
			System.out.println("\nPARTE 2:"+patterns.get(1)+"\n");
			System.out.println("\nVALOR: "+value+"\n");
			System.out.println("\nDATA DO CONTRATO: "+contractDate+"\n");
			System.out.println("\nINÍCIO DA VIGÊNCIA: "+beginEndDates[0]+"\n");
			System.out.println("\nTÉRMINO DA VIGÊNCIA: "+beginEndDates[1]+"\n");

			assertNotNull(patterns);
			assertTrue(patterns.size()>0);
			assertTrue(patterns.contains("AENEAN TORTOR EST S/A"));
			assertTrue(patterns.contains("ARCU SCELERISQUE"));
			
			
			System.out.println("###########################################################################");

		} catch (IOException e) {
			LOGGER.error("Error running test", e);
			fail();
		}
	}
	
	@Test
	public void test20() {
		String text;
		try {
			System.out.println("###########################################################################");
			System.out.println("\nCONTRATO:\n\n");
			text = loadFile("src/test/resources/_Contrato_810_2011___Laura_Cardillo___PMA.doc.txt");
			System.out.println(text);
			
			String value = CurrencyModel.getMaxValue(text);
			assertEquals("", value);
			
			
			List<String> patterns =  PartsModel.evaluate(text);
			
			List<Tag> dates = DateModel.findDates(text);
			String t =  PartsModel.normalize(text);

			String contractDate = DateModel.findContractDate(dates,t);
			String expected = "31 de março de 2007";
			assertEquals(expected, contractDate);
			String[] beginEndDates=DateModel.findBeginEndDates(dates, text);
			assertEquals("01 de junho de 2006",beginEndDates[0]);
			assertEquals("31 de maio de 2008",beginEndDates[1]);
			
			System.out.println("\nPARTE 1:"+patterns.get(0)+"\n");
			System.out.println("\nPARTE 2:"+patterns.get(1)+"\n");
			System.out.println("\nVALOR: "+value+"\n");
			System.out.println("\nDATA DO CONTRATO: "+contractDate+"\n");
			System.out.println("\nINÍCIO DA VIGÊNCIA: "+beginEndDates[0]+"\n");
			System.out.println("\nTÉRMINO DA VIGÊNCIA: "+beginEndDates[1]+"\n");


			assertNotNull(patterns);
			assertTrue(patterns.size()>0);
			assertTrue(patterns.contains("AENEAN RHONCUS PURUS S/A"));
			assertTrue(patterns.contains("ARCU SCELERISQUE LTDA."));
			
			
			System.out.println("###########################################################################");

		} catch (IOException e) {
			LOGGER.error("Error running test", e);
			fail();
		}
	}
	

	@Test
	public void test21() {
		String text;
		try {
			System.out.println("###########################################################################");
			System.out.println("\nCONTRATO:\n\n");
			text = loadFile("src/test/resources/_Contrato_UNISERV.doc.txt");
			System.out.println(text);
			
			String value = CurrencyModel.getMaxValue(text);
			assertEquals("", value);
			
			List<String> patterns =  PartsModel.evaluate(text);
			
			List<Tag> dates = DateModel.findDates(text);
			String t =  PartsModel.normalize(text);

			String contractDate = DateModel.findContractDate(dates,t);
			String expected = "02 de janeiro de 2013";
			assertEquals(expected, contractDate);
			String[] beginEndDates=DateModel.findBeginEndDates(dates, text);
			assertEquals("",beginEndDates[0]);
			assertEquals("",beginEndDates[1]);
			
			System.out.println("\nPARTE 1:"+patterns.get(0)+"\n");
			System.out.println("\nPARTE 2:"+patterns.get(1)+"\n");
			System.out.println("\nVALOR: "+value+"\n");
			System.out.println("\nDATA DO CONTRATO: "+contractDate+"\n");
			System.out.println("\nINÍCIO DA VIGÊNCIA: "+beginEndDates[0]+"\n");
			System.out.println("\nTÉRMINO DA VIGÊNCIA: "+beginEndDates[1]+"\n");


			assertNotNull(patterns);
			assertTrue(patterns.size()>0);
			assertTrue(patterns.contains("AENEAN FELIS DAPIBUS S/A"));
			assertTrue(patterns.contains("ARCU SCELERISQUE LTDA"));
			
			
			System.out.println("###########################################################################");

		} catch (IOException e) {
			LOGGER.error("Error running test", e);
			fail();
		}
	}
	
	@Test
	public void test22() {
		String text;
		try {
			System.out.println("###########################################################################");
			System.out.println("\nCONTRATO:\n\n");
			text = loadFile("src/test/resources/_Fazenda_Pau_Grande.doc.txt");
			System.out.println(text);
			
			String value = CurrencyModel.getMaxValue(text);
			assertEquals("", value);
			
			List<String> patterns =  PartsModel.evaluate(text);
			
			List<Tag> dates = DateModel.findDates(text);
			String t =  PartsModel.normalize(text);

			String contractDate = DateModel.findContractDate(dates,t);
			String expected = "27 de maio de 2011";
			assertEquals(expected, contractDate);
			String[] beginEndDates=DateModel.findBeginEndDates(dates, text);
			assertEquals("",beginEndDates[0]);
			assertEquals("",beginEndDates[1]);
			
			System.out.println("\nPARTE 1:"+patterns.get(0)+"\n");
			System.out.println("\nPARTE 2:"+patterns.get(1)+"\n");
			System.out.println("\nVALOR: "+value+"\n");
			System.out.println("\nDATA DO CONTRATO: "+contractDate+"\n");
			System.out.println("\nINÍCIO DA VIGÊNCIA: "+beginEndDates[0]+"\n");
			System.out.println("\nTÉRMINO DA VIGÊNCIA: "+beginEndDates[1]+"\n");


			assertNotNull(patterns);
			assertTrue(patterns.size()>0);
			assertTrue(patterns.contains("QUISQUE CURSUS S/A"));
			assertTrue(patterns.contains("ARCU SCELERISQUE"));
			
			
			System.out.println("###########################################################################");

		} catch (IOException e) {
			LOGGER.error("Error running test", e);
			fail();
		}
	}
	
	@Test
	public void test23() {
		String text;
		try {
			System.out.println("###########################################################################");
			System.out.println("\nCONTRATO:\n\n");
			text = loadFile("src/test/resources/AD02.txt");
			System.out.println(text);
			
			List<String> patterns =  PartsModel.evaluate(text);
			
			String value = CurrencyModel.getMaxValue(text);
			
			
			List<Tag> dates = DateModel.findDates(text);
			String t =  PartsModel.normalize(text);

			String contractDate = DateModel.findContractDate(dates,t);
			String expected = "";
			assertEquals(expected, contractDate);
			String[] beginEndDates=DateModel.findBeginEndDates(dates, text);
			assertEquals("",beginEndDates[0]);
			assertEquals("",beginEndDates[1]);
			
			System.out.println("\nPARTE 1:"+patterns.get(0)+"\n");
			System.out.println("\nPARTE 2:"+patterns.get(1)+"\n");
			System.out.println("\nVALOR: "+value+"\n");
			System.out.println("\nDATA DO CONTRATO: "+contractDate+"\n");
			System.out.println("\nINÍCIO DA VIGÊNCIA: "+beginEndDates[0]+"\n");
			System.out.println("\nTÉRMINO DA VIGÊNCIA: "+beginEndDates[1]+"\n");
			
			System.out.println("###########################################################################");


			assertEquals("$224", value);
			assertNotNull(patterns);
			assertTrue(patterns.size()>0);
			assertTrue(patterns.contains("PELLENTESQUE CONSEQUAT NISI"));
			//assertTrue(patterns.contains("ARCU SCELERISQUE"));
			
			

		} catch (IOException e) {
			LOGGER.error("Error running test", e);
			fail();
		}
	}
	
	
	@Test
	public void test24() {
		String text;
		try {
			System.out.println("###########################################################################");
			System.out.println("\nCONTRATO:\n\n");
			text = loadFile("src/test/resources/CGD_HSBC_VPAR.txt");
			System.out.println(text);
			
			String value = CurrencyModel.getMaxValue(text);
			assertEquals("$75", value);
			
			List<String> patterns =  PartsModel.evaluate(text);
			
			List<Tag> dates = DateModel.findDates(text);
			String t =  PartsModel.normalize(text);

			String contractDate = DateModel.findContractDate(dates,t);
			String expected = "";
			assertEquals(expected, contractDate);
			String[] beginEndDates=DateModel.findBeginEndDates(dates, text);
			assertEquals("",beginEndDates[0]);
			assertEquals("",beginEndDates[1]);
			
			System.out.println("\nPARTE 1:"+patterns.get(0)+"\n");
			System.out.println("\nPARTE 2:"+patterns.get(1)+"\n");
			System.out.println("\nVALOR: "+value+"\n");
			System.out.println("\nDATA DO CONTRATO: "+contractDate+"\n");
			System.out.println("\nINÍCIO DA VIGÊNCIA: "+beginEndDates[0]+"\n");
			System.out.println("\nTÉRMINO DA VIGÊNCIA: "+beginEndDates[1]+"\n");
			
			System.out.println("###########################################################################");

			assertNotNull(patterns);
			assertTrue(patterns.size()>0);
			//assertTrue(patterns.contains("YOTORANTlhl PARTICIPAC(IES BA."));
			//assertTrue(patterns.contains("HSIK ilank Brasii 5.A"));
			
			
			

		} catch (IOException e) {
			LOGGER.error("Error running test", e);
			fail();
		}
	}
	
	@Test
	public void test25() {
		String text;
		try {
			System.out.println("###########################################################################");
			System.out.println("\nCONTRATO:\n\n");
			text = loadFile("src/test/resources/CUSD_RC__DCPC_ES_0028_2013_Reserva_de_Ca.txt");
			System.out.println(text);
			
			String value = CurrencyModel.getMaxValue(text);
			
			List<String> patterns =  PartsModel.evaluate(text);
			List<Tag> dates = DateModel.findDates(text);
			String t =  PartsModel.normalize(text);

			String contractDate = DateModel.findContractDate(dates,t);
			String expected = "16 de janeiro de 2013";
			assertEquals(expected, contractDate);
			String[] beginEndDates=DateModel.findBeginEndDates(dates, text);
			
			
			System.out.println("\nPARTE 1:"+patterns.get(0)+"\n");
			System.out.println("\nPARTE 2:"+patterns.get(1)+"\n");
			System.out.println("\nVALOR: "+value+"\n");
			System.out.println("\nDATA DO CONTRATO: "+contractDate+"\n");
			System.out.println("\nINÍCIO DA VIGÊNCIA: "+beginEndDates[0]+"\n");
			System.out.println("\nTÉRMINO DA VIGÊNCIA: "+beginEndDates[1]+"\n");
			
			assertEquals("", value);
			assertEquals("01/01/2013",beginEndDates[0]);
			assertEquals("31/12/2013",beginEndDates[1]);

			assertNotNull(patterns);
			assertTrue(patterns.size()>0);
			assertTrue(patterns.contains("ARCU SCELERISQUE S.A. - ESCELSA"));
			assertTrue(patterns.contains("QUISQUE CURSUS S/A"));
			
			
			System.out.println("###########################################################################");

		} catch (IOException e) {
			LOGGER.error("Error running test", e);
			fail();
		}
	}
	
	
	@Test
	public void test26() {
		String text;
		try {
			System.out.println("###########################################################################");
			System.out.println("\nCONTRATO:\n\n");
			text = loadFile("src/test/resources/mm_1564_SJ_Paulo_Ferreira_-_PENHOR_AGR÷COLA.txt");
			System.out.println(text);
			
			String value = CurrencyModel.getMaxValue(text);
			
			List<String> patterns =  PartsModel.evaluate(text);
			
			List<Tag> dates = DateModel.findDates(text);
			String t =  PartsModel.normalize(text);

			String contractDate = DateModel.findContractDate(dates,t);
			String expected = "19 de março de 2007";
			assertEquals(expected, contractDate);
			String[] beginEndDates=DateModel.findBeginEndDates(dates, text);
			
			
			System.out.println("\nPARTE 1:"+patterns.get(0)+"\n");
			System.out.println("\nPARTE 2:"+patterns.get(1)+"\n");
			System.out.println("\nVALOR: "+value+"\n");
			System.out.println("\nDATA DO CONTRATO: "+contractDate+"\n");
			System.out.println("\nINÍCIO DA VIGÊNCIA: "+beginEndDates[0]+"\n");
			System.out.println("\nTÉRMINO DA VIGÊNCIA: "+beginEndDates[1]+"\n");
			
			assertEquals("", value);
			assertEquals("",beginEndDates[0]);
			assertEquals("",beginEndDates[1]);

			assertNotNull(patterns);
			assertTrue(patterns.size()>0);
			assertTrue(patterns.contains("PAULO JOSÉ PEREIRA FERREIRA"));
			assertTrue(patterns.contains("AENEAN TORTOR EST S/A"));
			
			
			System.out.println("###########################################################################");

		} catch (IOException e) {
			LOGGER.error("Error running test", e);
			fail();
		}
	}
	
	@Test
	public void test27() {
		String text;
		try {
			System.out.println("###########################################################################");
			System.out.println("\nCONTRATO:\n\n");
			text = loadFile("src/test/resources/mm_1564_SJ_Paulo_Ferreira_-_PENHOR_AGR÷COLA.txt");
			System.out.println(text);
			
			String value = CurrencyModel.getMaxValue(text);
			
			List<String> patterns =  PartsModel.evaluate(text);
			
			List<Tag> dates = DateModel.findDates(text);
			String t =  PartsModel.normalize(text);

			String contractDate = DateModel.findContractDate(dates,t);
			String expected = "19 de março de 2007";
			assertEquals(expected, contractDate);
			String[] beginEndDates=DateModel.findBeginEndDates(dates, text);
			
			
			System.out.println("\nPARTE 1:"+patterns.get(0)+"\n");
			System.out.println("\nPARTE 2:"+patterns.get(1)+"\n");
			System.out.println("\nVALOR: "+value+"\n");
			System.out.println("\nDATA DO CONTRATO: "+contractDate+"\n");
			System.out.println("\nINÍCIO DA VIGÊNCIA: "+beginEndDates[0]+"\n");
			System.out.println("\nTÉRMINO DA VIGÊNCIA: "+beginEndDates[1]+"\n");
			
			assertEquals("", value);
			assertEquals("",beginEndDates[0]);
			assertEquals("",beginEndDates[1]);


			assertNotNull(patterns);
			assertTrue(patterns.size()>0);
			assertTrue(patterns.contains("PAULO JOSÉ PEREIRA FERREIRA"));
			assertTrue(patterns.contains("AENEAN TORTOR EST S/A"));
			
			
			System.out.println("###########################################################################");

		} catch (IOException e) {
			LOGGER.error("Error running test", e);
			fail();
		}
	}
	
	@Test
	public void test28() {
		String text;
		try {
			System.out.println("###########################################################################");
			System.out.println("\nCONTRATO:\n\n");
			text = loadFile("src/test/resources/Termo_de_Ades_o_PROGRAMMERS_VC.txt");
			System.out.println(text);
			
			String value = CurrencyModel.getMaxValue(text);
			
			List<String> patterns =  PartsModel.evaluate(text);
			
			List<Tag> dates = DateModel.findDates(text);
			String t =  PartsModel.normalize(text);

			String contractDate = DateModel.findContractDate(dates,t);
			String expected = "10 de abril de 2012";
			assertEquals(expected, contractDate);
			String[] beginEndDates=DateModel.findBeginEndDates(dates, text);
			
			
			System.out.println("\nPARTE 1:"+patterns.get(0)+"\n");
			System.out.println("\nPARTE 2:"+patterns.get(1)+"\n");
			System.out.println("\nVALOR: "+value+"\n");
			System.out.println("\nDATA DO CONTRATO: "+contractDate+"\n");
			System.out.println("\nINÍCIO DA VIGÊNCIA: "+beginEndDates[0]+"\n");
			System.out.println("\nTÉRMINO DA VIGÊNCIA: "+beginEndDates[1]+"\n");
			
			assertEquals("01/01/2012",beginEndDates[0]);
			assertEquals("31/12/2012",beginEndDates[1]);
			
			assertEquals("R$ 2.572,20", value);

			assertNotNull(patterns);
			assertTrue(patterns.size()>0);
			assertTrue(patterns.contains("ARCU SCELERISQUE."));
			assertTrue(patterns.contains("AENEAN FELIS DAPIBUS S.A."));
			
			
			System.out.println("###########################################################################");

		} catch (IOException e) {
			LOGGER.error("Error running test", e);
			fail();
		}
	}
	
	@Test
	public void testAllCapsWithUTF8(){
		String text="FÃBIO";
		assertTrue(text.matches(PartsModel.ALL_CAPS_REGEX));
		text="FABIO";
		assertTrue(text.matches(PartsModel.ALL_CAPS_REGEX));
	}
	
	@Test
	public void testCapitalizedWithUTF8(){
		String text="ÁLVARO";
		assertTrue(text.matches(PartsModel.CAPITALIZED_REGEX));
		text="Álvaro";
		assertTrue(text.matches(PartsModel.CAPITALIZED_REGEX));
		text="S.A.";
		assertTrue(text.matches(PartsModel.CAPITALIZED_REGEX));
	}
	
	
	private String loadFile(String file) throws IOException{
		
		InputStream is = new FileInputStream(file);
		
		StringBuilder sb = new StringBuilder();
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(is,"UTF-8"));
		
		String line = null;
		
		while((line=reader.readLine())!=null){
			sb.append(line+"\n");
		}
		
		return sb.toString();
	}

	@Test
	public void testAnalyzer() {
		String text;
		try {
			System.out.println("###########################################################################");
			System.out.println("\nCONTRATO:\n\n");
			text = loadFile("src/test/resources/Termo_de_Ades_o_PROGRAMMERS_VC.txt");
			System.out.println(text);
			ContractAnalyzer analyzer = new ContractAnalyzer();
			String results =  analyzer.analyze(text);
			assertNotNull(results);
			System.out.println(results);
	
		} catch (IOException e) {
			LOGGER.error("Error running test", e);
			fail();
		}
	}

	@Test
	public void test29() {
		String text;
		try {
			System.out.println("###########################################################################");
			System.out.println("\nCONTRATO:\n\n");
			text = loadFile("src/test/resources/CCER_34977953___SITREL.txt");
			System.out.println(text);
			
			String value = CurrencyModel.getMaxValue(text);
			
			List<String> patterns =  PartsModel.evaluate(text);
			
			List<Tag> dates = DateModel.findDates(text);
			String t =  PartsModel.normalize(text);
	
			String contractDate = DateModel.findContractDate(dates,t);
			String expected = "10 de abril de 2012";
			//assertEquals(expected, contractDate);
			String[] beginEndDates=DateModel.findBeginEndDates(dates, text);
			
			
			System.out.println("\nPARTE 1:"+patterns.get(0)+"\n");
			System.out.println("\nPARTE 2:"+patterns.get(1)+"\n");
			System.out.println("\nVALOR: "+value+"\n");
			System.out.println("\nDATA DO CONTRATO: "+contractDate+"\n");
			System.out.println("\nINÍCIO DA VIGÊNCIA: "+beginEndDates[0]+"\n");
			System.out.println("\nTÉRMINO DA VIGÊNCIA: "+beginEndDates[1]+"\n");
			
			assertEquals("01/01/2012",beginEndDates[0]);
			assertEquals("31/12/2012",beginEndDates[1]);
			
			assertEquals("R$ 2.572,20", value);
	
			assertNotNull(patterns);
			assertTrue(patterns.size()>0);
			assertTrue(patterns.contains("ARCU SCELERISQUE."));
			assertTrue(patterns.contains("AENEAN FELIS DAPIBUS S.A."));
			
			
			System.out.println("###########################################################################");
	
		} catch (IOException e) {
			LOGGER.error("Error running test", e);
			fail();
		}
	}

}
