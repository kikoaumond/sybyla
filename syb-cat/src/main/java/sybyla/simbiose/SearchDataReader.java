package sybyla.simbiose;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import net.billylieurance.azuresearch.AbstractAzureSearchQuery;
import net.billylieurance.azuresearch.AzureSearchResultSet;
import net.billylieurance.azuresearch.AzureSearchWebQuery;
import net.billylieurance.azuresearch.AzureSearchWebResult;
import org.json.JSONObject;
import org.json.JSONException;
import org.apache.commons.codec.binary.Base64;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SearchDataReader {

    private static final Logger LOG = LoggerFactory.getLogger(SearchDataReader.class);
    private static int nTopSearchResults = 4;
    private static BufferedReader reader;
    private static BufferedWriter writer;

    public static void main(String[] args) {

        String fileIn = args[0];
        String fileOut = args[1];

        try {
            readURLS(fileIn, fileOut);
        } catch (Exception e) {
            LOG.error("Error reading file and getting URL's",e);
        }
    }

    public static void readURLS(String fileIn, String fileOut) throws Exception
    {

        InputStream is = new FileInputStream(new File(fileIn));
        reader = new BufferedReader(new InputStreamReader(is,"UTF-8"));

        OutputStream os = new FileOutputStream(new File(fileOut));
        writer = new BufferedWriter(new OutputStreamWriter(os,"UTF-8"));

        String line =  null;
        while ( (line =  reader.readLine()) != null) {

            if (line.startsWith("#")) continue ;

            String[] tokens = line.split("\t");

            if (tokens.length <= 6){
                continue;
            }
            StringBuilder sb = new StringBuilder();
            sb.append(tokens[2]);

            for (int i=3; i<6; i++) {

                sb.append("\t"+tokens[i].trim());
            }

            Set<String> urls = new HashSet<>();

            for (int i=6; i<tokens.length; i++) {

                String searchPhrase = tokens[i];
                String query  = searchPhrase.replaceAll("\"","");
                try {
                    Set<String> results = queryBing(query);
                    urls.addAll(results);
                }   catch (Throwable t){
                    LOG.error("Error querying Bing for phrase "+ searchPhrase,t);
                }

                //String url = buildURL(query);
                //JSONObject result =  run(url);
                //System.out.println(result);
                //Set<String> urls =  getUrls(searchPhrase);
                Thread.sleep(2000);
                //System.out.println("total: "+urls.size());

                for ( String url: urls ) {
                    sb.append("\t"+url);
               }
            }

            sb.append("\n");
            writer.write(sb.toString());
            sb.delete(0, sb.length());
        }

        reader.close();
        writer.close();
    }


    private static Set<String> queryBing(String searchPhrase) {

        Set<String>  r  = new HashSet<>();
        AzureSearchWebQuery aq = new AzureSearchWebQuery();


        aq.setAppid("8uFglROxxOLfMvypepsQNRF3+zpvORgyKu1ttf/UTDw=");


        aq.setFormat(AbstractAzureSearchQuery.AZURESEARCH_FORMAT.XML);

        aq.setMarket("pt-BR");

        // searchParam is the field containing the keyword to be searched.
        aq.setQuery(searchPhrase);

        aq.doQuery();

        AzureSearchResultSet<AzureSearchWebResult> azureSearchResultSet = aq
            .getQueryResult();

        int n=0;
        for (Iterator<AzureSearchWebResult> iterator = azureSearchResultSet
            .iterator(); iterator.hasNext();) {
            AzureSearchWebResult result = (AzureSearchWebResult) iterator
                .next();

            // Populate the data from result object in to your custom objects.

            //System.out.println(result.getTitle());
            String url = result.getUrl();
            r.add(url);

            System.out.println(result.getUrl());
            n++;
            if (n == nTopSearchResults){
                break;
            }
            //System.out.println(result.getDisplayUrl());
            //System.out.println(result.getDescription());
        }

        return r;
    }
    private static String buildURL(String term) throws UnsupportedEncodingException{
        String query = URLEncoder.encode(term,"UTF-8");
        String urlStr="https://api.datamarket.azure.com/Bing/SearchWeb/v1/Web?$format=json&Query="+query;
        return urlStr;
    }


    private static JSONObject run(String urlStr) throws JSONException {
        String accountKey="8uFglROxxOLfMvypepsQNRF3+zpvORgyKu1ttf/UTDw=";
        byte[] accountKeyBytes = Base64.encodeBase64((accountKey + ":" + accountKey).getBytes());
        String accountKeyEnc = new String(accountKeyBytes);

        HttpURLConnection connection = null;
        BufferedReader rd  = null;
        StringBuilder sb = null;
        String line = null;

        URL serverAddress = null;

        try {
            serverAddress = new URL(urlStr);
            //set up out communications stuff
            connection = null;

            //Set up the initial connection
            connection = (HttpURLConnection)serverAddress.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(false);
            connection.setRequestProperty("Authorization", "Basic " + accountKeyEnc);
            connection.connect();

            //get the output stream writer and write the output to the server
            //not needed in this example
            //wr = new OutputStreamWriter(connection.getOutputStream());
            //wr.write("");
            //wr.flush();

            //read the result from the server
            rd  = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            sb = new StringBuilder();

            while ((line = rd.readLine()) != null)
            {
                sb.append(line + '\n');
            }

            String response = sb.toString().trim();
            JSONObject json= new JSONObject(response);
            return json;

        } catch (MalformedURLException e) {
            LOG.error("error running Why query",e);
            return null;
        } catch (ProtocolException e) {
            LOG.error("error running Why query",e);
            return null;
        } catch (IOException e) {
            LOG.error("error running Why query",e);
            return null;
        }
        finally
        {
            //close the connection, set all objects to null
            connection.disconnect();
            rd = null;
            sb = null;
            //wr = null;
            connection = null;
        }
    }

    public static Set<String> getUrls(String searchPhrase) throws IOException {

        Set<String> urls = new HashSet<>();
        String address = "http://ajax.googleapis.com/ajax/services/search/web?v=1.0&q=";

        //String query  = searchPhrase.replaceAll("[\"]{2,}","\"");
        String query  = searchPhrase.replaceAll("\"","");


        String charset = "UTF-8";

        try{

            URL url = new URL(address + URLEncoder.encode(query, charset));
            URLConnection conn = url.openConnection();
            conn.setConnectTimeout(10000);
            Reader reader = new InputStreamReader(url.openStream(), charset);
            SearchResults results = new Gson().fromJson(reader, SearchResults.class);

            int total = results.getResponseData().getResults().size();

            // Show title and URL of each results
            for(int i=0; i<=total-1; i++){

                String result =  results.getResponseData().getResults().get(i).getUrl();
                System.out.println(result);
                urls.add(result);
            }

        } catch(Throwable t) {
            LOG.error("error running query: "+ query,t);
        }

        return urls;
    }
}


class SearchResults {

    private ResponseData responseData;
    public ResponseData getResponseData() { return responseData; }
    public void setResponseData(ResponseData responseData) { this.responseData = responseData; }
    public String toString() { return "ResponseData[" + responseData + "]"; }

    static class ResponseData {
        private List<Result> results;
        public List<Result> getResults() { return results; }
        public void setResults(List<Result> results) { this.results = results; }
        public String toString() { return "Results[" + results + "]"; }
    }

    static class Result {
        private String url;
        private String title;
        public String getUrl() { return url; }
        public String getTitle() { return title; }
        public void setUrl(String url) { this.url = url; }
        public void setTitle(String title) { this.title = title; }
        public String toString() { return "Result[url:" + url +",title:" + title + "]"; }
    }

}
