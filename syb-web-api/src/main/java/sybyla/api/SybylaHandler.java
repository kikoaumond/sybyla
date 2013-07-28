package sybyla.api;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.mortbay.jetty.EofException;

import sybyla.controller.impl.CategoryController;
import sybyla.controller.impl.EYController;
import sybyla.controller.impl.GraphController;
import sybyla.controller.impl.TagController;
import sybyla.controller.impl.WhyController;
import sybyla.controller.impl.SentimentController;

import sybyla.error.AboveQuotaException;
import sybyla.error.ErrorHandler;
import sybyla.error.UnauthorizedAccessException;
import sybyla.http.HTTPUtils;
import sybyla.jaxb.ApiErrors;
import sybyla.jaxb.ApiResponse;
import sybyla.jaxb.ObjectFactory;
import sybyla.jaxb.ResponseWriter;


public class SybylaHandler extends AbstractHandler {
	
	static final Logger LOGGER = Logger.getLogger(SybylaHandler.class);
	private ObjectFactory objectFactory= new ObjectFactory();
	private ResponseWriter responseWriter; 
	private GraphController graphController;
	private CategoryController clusterController;
	private TagController tagController;
	private WhyController whyController;
	private SentimentController sentimentController;
	private EYController eyController;

	
	private static boolean graphEnabled=false;
	private static boolean clusterEnabled=false;
	private static boolean tagEnabled=false;
	private static boolean whyEnabled=false;
	private static boolean sentimentEnabled=false;
	private static boolean eyEnabled=false;


    
	public SybylaHandler() throws JAXBException{
		super();
		responseWriter = new ResponseWriter();
	}
	
	public String getResponseType(Map<String, String[]> params){
		
		String responseType =sybyla.http.Constants.JSON_MIME_TYPE;
		String requestURI = HTTPUtils.getParam(Constants.REQUEST_URI, params);
		if (requestURI != null){
			String r =  requestURI.toLowerCase();
			if (r.endsWith(Constants.XML_URI)||r.endsWith(Constants.XML_URI+"/")){
				responseType=sybyla.http.Constants.XML_MIME_TYPE;
				return responseType;
			}
		}
		
		String callback = HTTPUtils.getParam(Constants.CALLBACK_PARAM, params);
		if (callback!=null){
			responseType = sybyla.http.Constants.JSONP_MIME_TYPE;
		}

		return responseType;
	}
	
	public void setResponseType(HttpServletResponse response, String responseType){
		String responseTypeEncoding = responseType+";"+sybyla.http.Constants.CHARSET_UTF8;
		response.setContentType(responseTypeEncoding);
		response.setCharacterEncoding(Constants.UTF8);
	}
	
	public Controller getController(HttpServletRequest request) throws IllegalArgumentException{
		
		String uri = request.getRequestURI();
		if (uri.startsWith("/"+Constants.GRAPH_APP)){
			if (!graphEnabled){
				throw new IllegalArgumentException("Server is not handling requests for application "+Constants.GRAPH_APP);
			}
			if (graphController ==  null){
				graphController = new GraphController();
			}
			return graphController;
			
		}
		if (uri.startsWith("/"+Constants.CATEGORY_APP)){
			if (!clusterEnabled){
				throw new IllegalArgumentException("Server is not handling requests for application "+Constants.CATEGORY_APP);
			}
			if (clusterController ==  null){
				clusterController = new CategoryController();
			}
			return clusterController;
			
		}
		if (uri.startsWith("/"+Constants.TAG_APP)){
			if (!tagEnabled){
				throw new IllegalArgumentException("Server is not handling requests for application "+Constants.TAG_APP);
			}
			if (tagController ==  null){
				tagController = new TagController();
			}
			return tagController;
		}
		if (uri.startsWith("/"+Constants.WHY_APP)){
			if (!whyEnabled){
				throw new IllegalArgumentException("Server is not handling requests for application "+Constants.WHY_APP);
			}
			if (whyController ==  null){
				whyController = new WhyController();
			}
			return whyController;
		}
		if (uri.startsWith("/"+Constants.SENTIMENT_APP)){
			if (!sentimentEnabled){
				throw new IllegalArgumentException("Server is not handling requests for application "+Constants.SENTIMENT_APP);
			}
			if (sentimentController ==  null){
				sentimentController = new SentimentController();
			}
			return sentimentController;
		}
		if (uri.startsWith("/"+Constants.EY_APP)){
			if (!eyEnabled){
				throw new IllegalArgumentException("Server is not handling requests for application "+Constants.EY_APP);
			}
			if (eyController ==  null){
				eyController = new EYController();
			}
			return eyController;
		}
		
		throw new IllegalArgumentException("Unknown API: "+uri);
	}
	
	public void handle(String target,
            		   Request baseRequest,
            		   HttpServletRequest request,
            		   HttpServletResponse response) 
    throws IOException, ServletException
    {
		
		if (baseRequest.isHandled()){
	            return;
		}
		
		String responseType =  sybyla.http.Constants.JSON_MIME_TYPE;
        try {
        	
        	long startTime = System.currentTimeMillis();
        	long requestTime = 0;
        	Map<String, String[]> params = HTTPUtils.getParamsAndHeaders(request);
        	Monitor.monitor(params);
        	//TODO: implement authorization
        	/*
        	if (!Authorizer.authorize(params)){
        		Monitor.monitorDenied(params);
        		throw new UnauthorizedAccessException("Unauthorized access.  Request denied");
        	}*/
        	
        	Controller controller = getController(request);
        	controller.checkParams(params);

        	responseType = getResponseType(params);
        	setResponseType(response, responseType);
        	String[] rt = {responseType};
        	params.put(Constants.RESPONSE_TYPE, rt);
        	
    		ApiResponse apiResponse = objectFactory.createApiResponse();
    		controller.process(params, apiResponse);
    		apiResponse.setRequestURL(request.getRequestURL().toString());
    	
    		long endTime =  System.currentTimeMillis();
    		requestTime = endTime - startTime;
    		
    		apiResponse.setRequestTime(requestTime+" ms");
        	
        	if (   responseType.equals(sybyla.http.Constants.JSON_MIME_TYPE)
        		|| responseType.equals(sybyla.http.Constants.XML_MIME_TYPE)){
        		
        		response.setStatus(HttpServletResponse.SC_OK);
                responseWriter.serialize(response.getOutputStream(), responseType, apiResponse);
        	
        	} else if (responseType.equals(sybyla.http.Constants.JSONP_MIME_TYPE)){

        		        		
        		String callback =  HTTPUtils.getParam(Constants.CALLBACK_PARAM, params);
        		String output = responseWriter.toJSON(apiResponse);
        		output = HTTPUtils.wrapCallback(callback, output);
        		
        		byte[] utf8 = output.getBytes(Charset.forName("UTF-8"));
        		response.setStatus(HttpServletResponse.SC_OK);
        		ServletOutputStream os  = response.getOutputStream();
        		os.write(utf8);
        		LOGGER.debug(output);
        	}
        	baseRequest.setHandled(true);
        	response.flushBuffer();
        	String appName =  controller.getAppName();
        	Monitor.addRequestTime(appName, requestTime);
        	
        } catch (IllegalArgumentException e) {
            ErrorHandler.handleException(request, response, responseType, HttpServletResponse.SC_BAD_REQUEST, ApiErrors.IllegalArgumentError, e);
        } catch(UnauthorizedAccessException e) {
        	ErrorHandler.handleException(request, response, responseType, HttpServletResponse.SC_UNAUTHORIZED, ApiErrors.UnauthorizedAccessError, e);
        } catch(AboveQuotaException e) {
        	ErrorHandler.handleException(request, response, responseType, HttpServletResponse.SC_UNAUTHORIZED, ApiErrors.AboveQuotaError, e);
        }catch (EofException e) {
            LOGGER.info("Connection closed by client, response lost");
        } catch (Throwable t) {
            LOGGER.error("Internal Server error handling request", t);
			LOGGER.error("Request: "+request.getRequestURL()+request.getQueryString());
            ErrorHandler.handleException(request, response, responseType, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ApiErrors.InternalError, t);
        } finally{
			baseRequest.setHandled(true);
			response.flushBuffer();
		}
    }

	public static boolean isGraphEnabled() {
		return graphEnabled;
	}

	public static void setGraphEnabled(boolean graphEnabled) {
		SybylaHandler.graphEnabled = graphEnabled;
	}

	public static boolean isClusterEnabled() {
		return clusterEnabled;
	}

	public static void setClusterEnabled(boolean clusterEnabled) {
		SybylaHandler.clusterEnabled = clusterEnabled;
	}

	public static boolean isTagEnabled() {
		return tagEnabled;
	}

	public static void setTagEnabled(boolean tagEnabled) {
		SybylaHandler.tagEnabled = tagEnabled;
	}

	public static boolean isWhyEnabled() {
		return whyEnabled;
	}

	public static void setWhyEnabled(boolean whyEnabled) {
		SybylaHandler.whyEnabled = whyEnabled;
	}
	
	public static boolean isSentimentEnabled() {
		return sentimentEnabled;
	}

	public static void setSentimentEnabled(boolean sentimentEnabled) {
		SybylaHandler.sentimentEnabled = sentimentEnabled;
	}

	public static boolean isEYtEnabled() {
		return eyEnabled;
	}

	public static void setEYEnabled(boolean eyEnabled) {
		SybylaHandler.eyEnabled = eyEnabled;
	}
}
