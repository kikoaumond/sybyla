package sybyla.wikipedia;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;

import org.apache.commons.lang3.StringEscapeUtils;
import org.junit.Test;

public class WikipediaRegexTest {
	
	@Test
	public void testMarkup() {
		String text = "[[Intelligence agency|Intelligence agencies and organizations]] of [[Israel]]";
		Set<String> markups = WikipediaRegex.findInternalLinks(text);
		assertTrue(markups.contains("Intelligence agency"));
		assertTrue(markups.contains("Intelligence agencies and organizations"));
		assertTrue(markups.contains("Israel"));
	}

	@Test
	public void testCategoryMarkup() {
		
		String text = "[[Category:1847 births]]\n"+
					  "[[Category:1930 deaths]]\n"+
					  "[[Category:Neurologists]]\n"+
					  "[[Category:People from Uppsala]]\n"+
					  "[[Category:Uppsala University faculty]]\n"+
					  "[[Category:Uppsala University alumni]]\n"+
					  "[[Category:Swedish physicians]]\n";
		
		Set<String> categories = WikipediaRegex.findCategoriesInEnglish(text);
		
		assertTrue(categories.contains("1847 births"));
		assertTrue(categories.contains("1930 deaths"));
		assertTrue(categories.contains("Neurologists"));
		assertTrue(categories.contains("Uppsala University faculty"));
		assertTrue(categories.contains("People from Uppsala"));
		assertTrue(categories.contains("Uppsala University alumni"));
		assertTrue(categories.contains("Swedish physicians"));


	}
	
	@Test
	public void testCategoryMarkupPortuguese(){
		
		String text="[[Categoria:Astronomia| ]]\n"+
					"[[Categoria:Geomática]]";
		
		Set<String> categories = WikipediaRegex.findCategoriesInPortuguese(text);
		assertTrue(categories.size()==2);
		assertTrue(categories.contains("Astronomia"));
		assertTrue(categories.contains("Geomática"));
	}
	
	@Test
	public void testInternalLinkMarkup() {
		
		String text = "[[File:Salomon Eberhard Henschen (ca. 1901).jpg|right|thumb|Salomon Eberhard Henschen (ca. 1901)]]\n"+
"'''Salomon Eberhard Henschen''' (28 February 1847, [[Uppsala]] - 16 December 1930) was a Swedish [[neurologist]].\n"+
"Beginning in 1862, he studied medicine at [[Uppsala]], later conducting [[botanical]] research in [[Brazil]] from 1867 to 1869. After his return to Sweden, he continued his medical studies at Uppsala, then furthering his education in [[Stockholm]] (1874) and [[Leipzig]] (1877).";
		
		Set<String> categories = WikipediaRegex.findInternalLinks(text);
		
		assertFalse(categories.contains("File:Salomon Eberhard Henschen (ca. 1901).jpg|right|thumb|Salomon Eberhard Henschen (ca. 1901)"));
		
		assertTrue(categories.contains("Uppsala"));
		assertTrue(categories.contains("neurologist"));
		assertTrue(categories.contains("botanical"));
		assertTrue(categories.contains("Brazil"));
		assertTrue(categories.contains("Stockholm"));
		assertTrue(categories.contains("Leipzig"));
	}
	
	@Test
	public void testInternaAltlLinkMarkup() {
		
		String text="Henschen is known for his investigations of [[aphasia]], as well as " +
				"his systematic studies involving the " +
				"[[Visual system|visual components/pathways of the brain]]. His " +
				"''Klinische und anatomische Beiträge zur Pathologie des Gehirns'' " +
				"(Clinical and anatomical contributions to the pathology of the brain) " +
				"was published over 25 editions from 1890 to 1930." +
				"&lt;ref&gt;[http://www.worldcat.org/identities/lccn-nr96-21021 WorldCat Identities] " +
				"(publications)" +
				"&lt;/ref&gt; In 1919 he described [[dyscalculia]], and later introduced " +
				"the term &quot;[[acalculia]]&quot; to define the impairment of " +
				"mathematical abilities in individuals with [[brain damage]] (1925)." +
				"&lt;ref&gt;[http://dyscalculiaforum.com/forum/viewthread.php?thread_id=357 " +
				"The Dyscalculia Forum] News about Dyscalculia..." +
				"&lt;/ref&gt;&lt;ref&gt;[http://www.medlink.com/medlinkcontent.asp Neurology " +
				"MedLink] Acalculia, Clinical Summary&lt;/ref&gt;";
				
		Set<String> altLinks = WikipediaRegex.findInternalAltLinks(text);
		assertTrue(altLinks.contains("visual components/pathways of the brain"));

		Set<String> links = WikipediaRegex.findInternalLinks(text);
		assertFalse(links.contains("Visual system|visual components/pathways of the brain"));
		assertTrue(links.contains("Visual system"));
		assertTrue(links.contains("visual components/pathways of the brain"));
		

	}

	
	@Test
	public void testCleanInternalLinks() {
		
		String text = "[[File:Salomon Eberhard Henschen (ca. 1901).jpg|right|thumb|Salomon Eberhard Henschen (ca. 1901)]]\n"+
"'''Salomon Eberhard Henschen''' (28 February 1847, [[Uppsala]] - 16 December 1930) was a Swedish [[neurologist]].\n"+
"Beginning in 1862, he studied medicine at [[Uppsala]], later conducting [[botanical]] research in [[Brazil]] from 1867 to 1869. After his return to Sweden, he continued his medical studies at Uppsala, then furthering his education in [[Stockholm]] (1874) and [[Leipzig]] (1877).";
		
		String t = WikipediaRegex.cleanInternalLinks(text);
		
		String expected = "[[File:Salomon Eberhard Henschen (ca. 1901).jpg|right|thumb|Salomon Eberhard Henschen (ca. 1901)]]\n"+
"'''Salomon Eberhard Henschen''' (28 February 1847, Uppsala - 16 December 1930) was a Swedish neurologist.\n"+
"Beginning in 1862, he studied medicine at Uppsala, later conducting botanical research in Brazil from 1867 to 1869. After his return to Sweden, he continued his medical studies at Uppsala, then furthering his education in Stockholm (1874) and Leipzig (1877).";
		
		assertEquals(expected, t);
			
	}
	
	@Test
	public void testCleanAndDeleteMarkup() {
		
		String text = "[[File:Salomon Eberhard Henschen (ca. 1901).jpg|right|thumb|Salomon Eberhard Henschen (ca. 1901)]]\n"+
"'''Salomon Eberhard Henschen''' (28 February 1847, [[Uppsala]] - 16 December 1930) was a Swedish [[neurologist]].\n"+
"Beginning in 1862, he studied medicine at [[Uppsala]], later conducting [[botanical]] research in [[Brazil]] from 1867 to 1869. After his return to Sweden, he continued his medical studies at Uppsala, then furthering his education in [[Stockholm]] (1874) and [[Leipzig]] (1877).";
		
		String t = WikipediaRegex.cleanInternalLinks(text);
		t = WikipediaRegex.deleteMarkup(t);
		t = WikipediaRegex.deleteMarkup(t);

		
		String expected = "\n"+
"'''Salomon Eberhard Henschen''' (28 February 1847, Uppsala - 16 December 1930) was a Swedish neurologist.\n"+
"Beginning in 1862, he studied medicine at Uppsala, later conducting botanical research in Brazil from 1867 to 1869. After his return to Sweden, he continued his medical studies at Uppsala, then furthering his education in Stockholm (1874) and Leipzig (1877).";
		
		assertEquals(expected, t);
			
	}
	
	@Test
	public void testCleanFormatting() {
		
		String text = "[[File:Salomon Eberhard Henschen (ca. 1901).jpg|right|thumb|Salomon Eberhard Henschen (ca. 1901)]]\n"+
"'''Salomon Eberhard Henschen''' (28 February 1847, [[Uppsala]] - 16 December 1930) was a Swedish [[neurologist]].\n"+
"Beginning in 1862, he studied medicine at [[Uppsala]], later conducting [[botanical]] research in [[Brazil]] from 1867 to 1869. After his return to Sweden, he continued his medical studies at Uppsala, then furthering his education in [[Stockholm]] (1874) and [[Leipzig]] (1877).";
		
		String t = WikipediaRegex.cleanFormatting(text);
		
		String expected =  "[[File:Salomon Eberhard Henschen (ca. 1901).jpg|right|thumb|Salomon Eberhard Henschen (ca. 1901)]]\n"+
				"Salomon Eberhard Henschen (28 February 1847, [[Uppsala]] - 16 December 1930) was a Swedish [[neurologist]].\n"+
				"Beginning in 1862, he studied medicine at [[Uppsala]], later conducting [[botanical]] research in [[Brazil]] from 1867 to 1869. After his return to Sweden, he continued his medical studies at Uppsala, then furthering his education in [[Stockholm]] (1874) and [[Leipzig]] (1877).";
						
		assertEquals(expected, t);
			
	}
	
	@Test
	public void testDeleteMetadata() {
		
		String text = "Some text here {{Persondata &lt;!-- Metadata: see [[Wikipedia:Persondata]]. --&gt;\n"+
				"| NAME              = Henschen, Salomon Eberhard\n"+ 
				"| ALTERNATIVE NAMES =\n"+ 
				"| SHORT DESCRIPTION = Swedish neurologist\n"+
				"| DATE OF BIRTH     = 28 February 1847\n"+ 
				"| PLACE OF BIRTH    = Uppsala\n"+
				"| DATE OF DEATH     = 16 December 1930\n"+
				"| PLACE OF DEATH    =\n"+ 
				"}}";
		
		String t = WikipediaRegex.deleteMetadata(text);
		
		String expected =  "Some text here ";
		assertEquals(expected, t);
			
	}
	
	@Test
	public void testDeleteMetadata2() {
		
		String text = "	A few examples of this process:"+

	"{| class=&quot;wikitable&quot; border=&quot;1&quot;"+
	"|-"+
	"| |'''Physical process'''"+
	"| |'''Experimental tool'''"+
	"| |'''Theoretical model'''"+
	"| |'''Explains/predicts'''"+
	"|-"+
	"| |[[Gravitation]]"+
	"| |[[Radio telescope]]s"+
	"| |[[Nordtvedt effect|Self-gravitating system]]"+
	"| |Emergence of a [[star system]]"+
	"|-"+
	"| |[[Nuclear fusion]]"+
	"| |[[Spectroscopy]]"+
	"| |[[Stellar evolution]]"+
	"| |How the stars shine and how [[nucleosynthesis|metals formed]]"+
	"|-"+
	"| |[[The Big Bang]]"+
	"| |[[Hubble Space Telescope]], [[COBE]]"+
	"| |[[Expanding universe]]"+
	"| |[[Age of the Universe]]"+
	"|-"+
	"| |[[Quantum fluctuation]]s"+
	"| |"+
	"| |[[Cosmic inflation]]"+
	"| |[[Flatness problem]]"+
	"|-"+
	"| |[[Gravitational collapse]]"+
	"| |[[X-ray astronomy]]"+
	"| |[[General relativity]]"+
	"| |[[Black hole]]s at the center of [[Andromeda galaxy]]"+
	"|-"+
	"| |[[CNO cycle]] in [[star]]s"+
	"| |"+
	"| |"+
	"| |"+
	"|-"+
	"|}"+

	"[[Dark matter]] and [[dark energy]] are the current leading topics in astronomy";
		
		String t = WikipediaRegex.deleteMetadata(text);
		
		String expected =  "	A few examples of this process:[[Dark matter]] and [[dark energy]] are the current leading topics in astronomy";
		assertEquals(expected, t);
			
	}
	

	
	@Test
	public void testCleanSections() {
		
		String text = "In 1897 he became a member of the [[Swedish Academy of Sciences]]. " +
				"In 1923/24, he was one of a small group of neurologists who attended to [[Lenin]]," +
				"following the Soviet leader's third and final stroke. With his son, Folke Henschen " +
				"(1881-1977), he collaborated on an [[autopsy]] of Lenin's brain.&lt;ref&gt;" +
				"[http://plants.jstor.org/person/bm000003574 JSTOR Plant Science] Henschen, " +
				"Salomon Eberhard (1847-1930)&lt;/ref&gt;\n"+
				"== References ==";
		
		
		Matcher m = WikipediaRegex.WIKIPEDIA_HEADING_PATTERN.matcher(text);
		Set<String> headings = new HashSet<String>();
		if(m.find()){
			String h = m.group(m.groupCount());
			headings.add(h);	
		}
		
		String t = WikipediaRegex.cleanSections(text);
		
		String expected =  "In 1897 he became a member of the [[Swedish Academy of Sciences]]. " +
				"In 1923/24, he was one of a small group of neurologists who attended to [[Lenin]]," +
				"following the Soviet leader's third and final stroke. With his son, Folke Henschen " +
				"(1881-1977), he collaborated on an [[autopsy]] of Lenin's brain.&lt;ref&gt;" +
				"[http://plants.jstor.org/person/bm000003574 JSTOR Plant Science] Henschen, " +
				"Salomon Eberhard (1847-1930)&lt;/ref&gt;\n" +
				" References ";
		assertEquals(expected, t);
			
	}
	
	@Test
	public void testDeleteReferences() {
		
		String text = "In 1897 he became a member of the [[Swedish Academy of Sciences]]. " +
				"In 1923/24, he was one of a small group of neurologists who attended to [[Lenin]]," +
				"following the Soviet leader's third and final stroke. With his son, Folke Henschen " +
				"(1881-1977), he collaborated on an [[autopsy]] of Lenin's brain.&lt;ref&gt;" +
				"[http://plants.jstor.org/person/bm000003574 JSTOR Plant Science] Henschen, " +
				"Salomon Eberhard (1847-1930)&lt;/ref&gt;"+
				"== References ==";
		text = StringEscapeUtils.unescapeHtml4(text);
		String t = WikipediaRegex.deleteReferences(text);
		
		String expected =  "In 1897 he became a member of the [[Swedish Academy of Sciences]]. " +
				"In 1923/24, he was one of a small group of neurologists who attended to [[Lenin]]," +
				"following the Soviet leader's third and final stroke. With his son, Folke Henschen " +
				"(1881-1977), he collaborated on an [[autopsy]] of Lenin's brain." +
				"== References ==";
		assertEquals(expected, t);
			
	}
	
	@Test
	public void testDeleteReferences2() {
		String text="Henschen is known for his investigations of [[aphasia]], " +
				"as well as his systematic studies involving the " +
				"[[Visual system|visual components/pathways of the brain]]. " +
				"His ''Klinische und anatomische Beiträge zur Pathologie des Gehirns'' " +
				"(Clinical and anatomical contributions to the pathology of the brain) " +
				"was published over 25 editions from 1890 to 1930." +
				"< ref name=\"blah\" / >"+
				"&lt;ref&gt;[http://www.worldcat.org/identities/lccn-nr96-21021 WorldCat Identities] (publications)" +
				"&lt;/ref&gt; In 1919 he described [[dyscalculia]], and later introduced " +
				"the term &quot;[[acalculia]]&quot; to define the impairment of " +
				"mathematical abilities in individuals with [[brain damage]] (1925)." +
				"&lt;ref&gt;[http://dyscalculiaforum.com/forum/viewthread.php?thread_id=357 " +
				"The Dyscalculia Forum] News about Dyscalculia..." +
				"&lt;/ref&gt;&lt;ref&gt;[http://www.medlink.com/medlinkcontent.asp Neurology MedLink] Acalculia, Clinical Summary&lt;/ref&gt;";
				
		text = StringEscapeUtils.unescapeHtml4(text);
		
		Set<String> references = WikipediaRegex.findReferences(text);
		
		assertTrue(references.contains("<ref>[http://www.worldcat.org/identities/lccn-nr96-21021 WorldCat Identities] (publications)</ref>"));
		assertTrue(references.contains("<ref>[http://dyscalculiaforum.com/forum/viewthread.php?thread_id=357 The Dyscalculia Forum] News about Dyscalculia...</ref>"));
		assertTrue(references.contains("<ref>[http://www.medlink.com/medlinkcontent.asp Neurology MedLink] Acalculia, Clinical Summary</ref>"));
		
		String t = WikipediaRegex.deleteReferences(text);
		 t = WikipediaRegex.deleteHTMLMarkup(t);

		
		
		String expected =  "Henschen is known for his investigations of [[aphasia]], " +
				"as well as his systematic studies involving the " +
				"[[Visual system|visual components/pathways of the brain]]. " +
				"His ''Klinische und anatomische Beiträge zur Pathologie des Gehirns'' " +
				"(Clinical and anatomical contributions to the pathology of the brain) " +
				"was published over 25 editions from 1890 to 1930." +
				" In 1919 he described [[dyscalculia]], and later introduced " +
				"the term \"[[acalculia]]\" to define the impairment of " +
				"mathematical abilities in individuals with [[brain damage]] (1925).";
				
		assertEquals(expected, t);
			
	}

	
	@Test
	public void testDeleteAnnotations() {
		
		String text="Some text here [[Category:1847 births]]\n"+
			    "[[Category:1930 deaths]]\n"+
			    "[[Category:Neurologists]]\n"+
			    "[[Category:People from Uppsala]]\n"+
			    "[[Category:Uppsala University faculty]]\n"+
			    "[[Category:Uppsala University alumni]]\n"+
			    "[[Category:Swedish physicians]]\n"+

				"[[de:Salomon Eberhard Henschen]]\n"+
				"[[sv:Salomon Eberhard Henschen]]";
		
		String t = WikipediaRegex.deleteAnnotations(text);
		
		String expected =  "Some text here";
		assertEquals(expected, t.trim());
			
	}
	
	@Test
	public void testDeleteComments() {
	
	String text="* Cosmologia: Estuda a origem e a evolução do universo."+
				"<!--"+
				"Cosmology (from the Greek κόσμος \"world, universe\" and λόγος \"word, study\") could be considered the study of the universe as a whole."+

				"Observations of the large-scale structure of the universe, a branch known as physical cosmology, have provided a deep understanding of the formation and evolution of the cosmos. Fundamental to modern cosmology is the well-accepted theory of the big bang, wherein our universe began at a single point in time, and thereafter expanded over the course of 13.7 Gyr to its present condition.<ref name=Dodelson2003/> The concept of the big bang can be traced back to the discovery of the microwave background radiation in 1965.<ref name=Dodelson2003></ref>"+

				"In the course of this expansion, the universe underwent several evolutionary stages. In the very early moments, it is theorized that the universe experienced a very rapid cosmic inflation, which homogenized the starting conditions. Thereafter, nucleosynthesis produced the elemental abundance of the early universe.<ref name=Dodelson2003/> (See also nucleocosmochronology.)"+

				"When the first atoms formed, space became transparent to radiation, releasing the energy viewed today as the microwave background radiation. The expanding universe then underwent a Dark Age due to the lack of stellar energy sources.<ref name=\"cosmology 101\"></ref>"+

				"A hierarchical structure of matter began to form from minute variations in the mass density. Matter accumulated in the densest regions, forming clouds of gas and the earliest stars. These massive stars triggered the reionization process and are believed to have created many of the heavy elements in the early universe which tend to decay back to the lighter elements extending the cycle.<ref>Dodelson, 2003, pp. 216–261</ref>"+

				"Gravitational aggregations clustered into filaments, leaving voids in the gaps. Gradually, organizations of gas and dust merged to form the first primitive galaxies. Over time, these pulled in more matter, and were often organized into groups and clusters of galaxies, then into larger-scale superclusters.<ref></ref>"+

				"Fundamental to the structure of the universe is the existence of dark matter and dark energy. These are now thought to be the dominant components, forming 96% of the mass of the universe. For this reason, much effort is expended in trying to understand the physics of these components.<ref></ref>"+
				"-->"+

 				"Astronomia teórica"+ 
 				"Tópicos estudados pelos astrônomos teóricos são: dinâmica e evolução estelar; formação e evolução de galáxias; estrutura em grande escala da matéria no Universo; origem dos raios cósmicos; relatividade geral e cosmologia física, incluindo Cosmologia das cordas e física de astropartículas."+
 				"<!--"+
 				"Theoretical astronomers use a wide variety of tools which include analytical models (for example, polytropes to approximate the behaviors of a star) and computational numerical simulations. Each has some advantages. Analytical models of a process are generally better for giving insight into the heart of what is going on. Numerical models can reveal the existence of phenomena and effects that would otherwise not be seen.<ref></ref><ref></ref>"+

				"Theorists in astronomy endeavor to create theoretical models and figure out the observational consequences of those models. This helps observers look for data that can refute a model or help in choosing between several alternate or conflicting models."+

				"Theorists also try to generate or modify models to take into account new data. In the case of an inconsistency, the general tendency is to try to make minimal modifications to the model to fit the data. In some cases, a large amount of inconsistent data over time may lead to total abandonment of a model."+

				"Topics studied by theoretical astronomers include: stellar dynamics and evolution; galaxy formation; large-scale structure of matter in the Universe; origin of cosmic rays; general relativity and physical cosmology, including string cosmology and astroparticle physics. Astrophysical relativity serves as a tool to gauge the properties of large scale structures for which gravitation plays a significant role in physical phenomena investigated and as the basis for black hole (astro)physics and the study of gravitational waves."+

				"Some widely accepted and studied theories and models in astronomy, now included in the Lambda-CDM model are the Big Bang, Cosmic inflation, dark matter, and fundamental theories of physics."+

				"A few examples of this process:"+



				"Dark matter and dark energy are the current leading topics in astronomy,<ref></ref> as their discovery and controversy originated during the study of the galaxies."+
				"-->";
		String t =  WikipediaRegex.deleteHTMLComments(text);
		String expected = "* Cosmologia: Estuda a origem e a evolução do universo."+

 				"Astronomia teórica"+ 
 				"Tópicos estudados pelos astrônomos teóricos são: dinâmica e evolução estelar; formação e evolução de galáxias; estrutura em grande escala da matéria no Universo; origem dos raios cósmicos; relatividade geral e cosmologia física, incluindo Cosmologia das cordas e física de astropartículas.";
 				
		assertEquals(expected, t);
	
	}


}
