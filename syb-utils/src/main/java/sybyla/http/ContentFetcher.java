package sybyla.http;

import java.io.IOException;


public class ContentFetcher {
	private String url;
	Client httpClient;
	private String content;
	private String responseType;
	
	public ContentFetcher(String url){
		this.url = url;
	}
	
	public void fetch() throws IOException{
		
		if (httpClient  ==  null){
		
			httpClient = new Client(url);
			httpClient.run();
			responseType = httpClient.getResponseType();
			
			if (responseType.contains(sybyla.http.Constants.HTML_MIME_TYPE)){
				SimplePageParser parser =  new SimplePageParser();
				String htmlText =  httpClient.getResponseContent();
				content = parser.pageText(htmlText);
			}
		}
	}

	public String getContent() {
		return content;
	}
}
