package sybyla.error;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.mortbay.jetty.EofException;

import sybyla.jaxb.ApiResponse;
import sybyla.jaxb.Error;

import sybyla.api.Constants;
import sybyla.jaxb.ObjectFactory;
import sybyla.http.HTTPUtils;
import sybyla.jaxb.ApiErrors;
import sybyla.jaxb.ResponseWriter;

public class ErrorHandler {
	private static final Logger LOGGER = Logger.getLogger(ErrorHandler.class);
	private static final ResponseWriter RESPONSE_WRITER = getResponseWriter(); 

	private static ResponseWriter getResponseWriter() {
		try {
			return new ResponseWriter();
		} catch (JAXBException e) {
			LOGGER.error("Error creating ResponseWriter",e);
			return null;
		}
	}
	
	public static void handleException(HttpServletRequest request, 
								HttpServletResponse response, 
								String responseType,
								int status, 
								ApiErrors error, 
								Throwable t) {
		try {
			response.setStatus(status);
			String responseTypeEncoding = responseType+";"+sybyla.http.Constants.CHARSET_UTF8;
			response.setContentType(responseTypeEncoding);
			response.setCharacterEncoding(Constants.UTF8);
			if (	responseType.equals(sybyla.http.Constants.JSON_MIME_TYPE)
				||  responseType.equals(sybyla.http.Constants.XML_MIME_TYPE)) {
				
				ApiResponse apiErrorResponse = makeApiErrorResponse( request,error, t);
				RESPONSE_WRITER.serialize(response.getOutputStream(), responseType, apiErrorResponse);
			
			} else if (responseType.equals(sybyla.http.Constants.JSONP_MIME_TYPE)){
				
				JSONObject apiErrorJSON = makeApiErrorJSON(request, error,t);
				String callback = request.getParameter(Constants.CALLBACK_PARAM);
				String output = HTTPUtils.wrapCallback(callback, apiErrorJSON);
	    		response.getWriter().println(output);
			}
			response.flushBuffer();
		} catch (EofException e) {
			LOGGER.info("Connection closed by client, error response lost");
		} catch (Throwable t2) {
			LOGGER.error("Error generating error response to request", t2);
			LOGGER.error("Request: "+request.getRequestURL()+request.getQueryString());
		}
	}

	private static JSONObject makeApiErrorJSON(HttpServletRequest request,
			 							ApiErrors error, 
			 							Throwable t) throws JSONException{
	
		JSONObject response =  new JSONObject();
		JSONObject err = new JSONObject();
		err.put("request", HTTPUtils.getFullUrl(request));
		err.put("errorType", error.toString());
		
		if (error == ApiErrors.InternalError) {
			err.put("errorMessage","Internal Server Error; our team has been alerted and is acting on it.");
		} else{
	
			String errorMsg = t.getMessage();
			if (errorMsg == null) {
				errorMsg = t.getClass().getSimpleName();
			}
			err.put("errorMessage", errorMsg);
		}
		response.put("error",err);
		JSONObject r = new JSONObject();
		r.put("Response", response);
		return r;
	}

	private static ApiResponse makeApiErrorResponse(HttpServletRequest request,
									 ApiErrors error, 
									 Throwable t){
		ObjectFactory factory= new ObjectFactory();
		ApiResponse apiResponse = factory.createApiResponse();
		Error apiError = factory.createError();
		apiResponse.setError(apiError);
		apiResponse.setRequestURL(HTTPUtils.getFullUrl(request));
		
		apiError.setErrorType(error.toString());
		
		if (error == ApiErrors.InternalError) {
			apiError.setErrorMessage("Internal Server Error; our team has been alerted and is acting on it.");
		} else{
		
			String errorMsg = t.getMessage();
			if (errorMsg == null) {
				errorMsg = t.getClass().getSimpleName();
			}
			apiError.setErrorMessage(errorMsg);
		}
		return apiResponse;
	}

}
