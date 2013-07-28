package sybyla.wikipedia;
 
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.apache.log4j.Logger;
import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;

public class StAXParser {
	
	private static final Logger LOGGER = Logger.getLogger(StAXParser.class);
	
	public static final String PREFIX="{http://www.mediawiki.org/xml/export-0.8/}";
	public static final String PAGE = PREFIX+"page";
	public static final String REDIRECT = PREFIX+"redirect";
	public static final String TITLE = PREFIX+"title";
	public static final String TITLE_ATTRIBUTE = "title";
	public static final String TEXT = PREFIX+"text";
	public static final String COMMENT = PREFIX+"comment";

	public static final String WIKIPEDIA = "Wikipedia";


	public void read(String file, PageProcessor processor) {
	  
		int nPages=0;
		try {
			// First create a new XMLInputFactory
			XMLInputFactory inputFactory = XMLInputFactory2.newInstance();
			// Setup a new eventReader
			InputStream in = new FileInputStream(file);
			LOGGER.info("Reading file "+file);
			
			XMLStreamReader2 reader = (XMLStreamReader2) inputFactory.createXMLStreamReader(in,"UTF-8");
			// Read the XML document
			Page page = null;

			int eventType = 0;
			String curElement = "";

			while (reader.hasNext()) {

				eventType = reader.next();
				switch (eventType) {
					
					case XMLEvent.START_ELEMENT:
						
						curElement = reader.getName().toString();
						
						if(PAGE.equals(curElement)){
							page = new Page();
							nPages++;
						}else if(REDIRECT.equals(curElement)){
							int c= reader.getAttributeCount();
							for (int i=0;i<c;i++){
								String n =  reader.getAttributeLocalName(i);
								String v = reader.getAttributeValue(i);
								if (page !=null && TITLE_ATTRIBUTE.equals(n)){
									page.addSynonym(v);
									break;
								}
							}							
						}
						
						break;
						
					case XMLEvent.CHARACTERS:
						if (page== null) break;
						
						if (TITLE.equals(curElement)){
							String content = reader.getText();
							if (!content.contains(":")){
								page.setTitle(content);
							} else {
								page = null;
								break;
							}
						} else if (TEXT.equals(curElement)){
							String content = reader.getText();
							page.setBody(content);
						} 
						
						break;
						
					case XMLEvent.END_ELEMENT:
						if (page ==null) break;
						curElement = reader.getName().toString();
						
						if(PAGE.equals(curElement)){
							processor.processPage(page);
							page = null;
						}
						curElement=null;
						break;
						
					case XMLEvent.END_DOCUMENT:
						LOGGER.info("Document parsing finishing. " +nPages+" read");
			}
				}
		} catch (FileNotFoundException e) {
			LOGGER.error("File not found "+ file,e);    
		} catch (XMLStreamException e) {
			LOGGER.error("Error parsing file "+ file,e);    
		}
	}

} 