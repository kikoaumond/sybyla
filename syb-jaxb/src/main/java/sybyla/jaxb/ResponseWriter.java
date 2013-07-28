package sybyla.jaxb;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.SerializationConfig.Feature;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;

public class ResponseWriter {
    private static final Logger LOGGER = Logger.getLogger(ResponseWriter.class);
    
    public static final String JSON_MIME_TYPE="application/json";
    public static final String XML_MIME_TYPE="text/xml";
    
    private ObjectMapper jsonMapper;   // JSON serializer
    private Marshaller xmlMarshaller;  // XML serializer
    
    public ResponseWriter() throws JAXBException {
        
        LOGGER.trace("Setting up XML response support");
        try {
            JAXBContext context = JAXBContext.newInstance("sybyla.jaxb");
            xmlMarshaller = context.createMarshaller();
            xmlMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            xmlMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.FALSE);
            xmlMarshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");

        } catch (JAXBException e) {
            throw new JAXBException("Unable to initialize XML response support", e);
        }
        
        jsonMapper = makeObjectMapper();
    } 
    
    public static ObjectMapper makeObjectMapper(){
    	
    	ObjectMapper mapper = new ObjectMapper();
    	
        AnnotationIntrospector introspector = new JaxbAnnotationIntrospector();
        SerializationConfig sc = mapper.getSerializationConfig().withAnnotationIntrospector(introspector);
        mapper.setSerializationConfig(sc);
        
        DeserializationConfig dc = mapper.getDeserializationConfig().withAnnotationIntrospector(introspector);
        mapper.setDeserializationConfig(dc);
        
        mapper.setAnnotationIntrospector(introspector);
        mapper.setSerializationInclusion(Inclusion.NON_NULL);
        mapper.configure(Feature.WRAP_ROOT_VALUE, true);
        mapper.configure(DeserializationConfig.Feature.UNWRAP_ROOT_VALUE, true);
        //mapper.setPropertyNamingStrategy(new CamelCaseNamingStrategy());         
        return mapper;
    }
    
    public void serialize(OutputStream os, String type, Object result) 
        throws JAXBException, JsonGenerationException, JsonMappingException, IOException {

        if (type.equals(XML_MIME_TYPE)) {
            xmlMarshaller.marshal(result, os);
        } else {
            jsonMapper.writeValue(os, result);
       }
    }
    
    public String toJSON(Object o) throws JsonGenerationException, JsonMappingException, IOException{
    	return jsonMapper.writeValueAsString(o);
    }
    
    public String toXML(Object o) throws JAXBException {
    	StringWriter writer = new StringWriter();
    	 xmlMarshaller.marshal(o, writer);
    	 return writer.toString();
    }
}
