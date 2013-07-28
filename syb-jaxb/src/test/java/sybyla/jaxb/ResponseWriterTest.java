package sybyla.jaxb;

import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayOutputStream;

import org.junit.Test;

import sybyla.jaxb.ApiResponse;
import sybyla.jaxb.ObjectFactory;
import sybyla.jaxb.ResponseWriter;


public class ResponseWriterTest {
    
    @Test
    public void testNewFields() throws Exception {
        ObjectFactory factory = new ObjectFactory();
        ApiResponse response = factory.createApiResponse();
        
        
        ResponseWriter rw = new ResponseWriter();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        rw.serialize(baos, ResponseWriter.XML_MIME_TYPE, response);
        
        String xmlResponse = baos.toString("UTF-8");
        assertNotNull(xmlResponse);
        
        baos.reset();
        rw.serialize(baos,ResponseWriter.JSON_MIME_TYPE, response);
        System.out.println(baos.toString("UTF-8"));
    }
   
}
