package sybyla.jaxb;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import sybyla.jaxb.ApiResponse;

public class APIXMLParser {
    
    public static ApiResponse readResponse(String xml) throws JAXBException {

        // setup object mapper using the Response class
        JAXBContext context = JAXBContext.newInstance(ApiResponse.class);
        // parse the XML and return an instance of the AppConfig class
        InputStream is = new ByteArrayInputStream(xml.getBytes());
        ApiResponse response = (ApiResponse) context.createUnmarshaller().unmarshal(is);
        return response;    
    }
}
