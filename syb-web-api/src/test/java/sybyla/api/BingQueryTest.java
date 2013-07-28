package sybyla.api;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.eclipse.jetty.util.ajax.JSON;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class BingQueryTest {
	
	
	
	
	public void run(String urlStr, String callback) {
	      String accountKey="8uFglROxxOLfMvypepsQNRF3+zpvORgyKu1ttf/UTDw=";
	      byte[] accountKeyBytes = Base64.encodeBase64((accountKey + ":" + accountKey).getBytes());
	      String accountKeyEnc = new String(accountKeyBytes);

		  HttpURLConnection connection = null;
	     // OutputStreamWriter wr = null;
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
	          connection.setReadTimeout(300000);
	          connection.setConnectTimeout(300000);
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
	          System.out.println(sb.toString());
	          
	          assertTrue(response != null && !response.equals(""));
	          String json= response;
	          if (callback!=null){
		          assertTrue(response.startsWith(callback+"({"));
		          int begin =  response.indexOf("{");
		          int end = response.indexOf(");");
		          json = response.substring(begin,end);
	          }
	          
	          Object o = JSON.parse(json);
	          assertTrue(o!=null);
	                    
	      } catch (MalformedURLException e) {
	          e.printStackTrace();
	          fail();
	      } catch (ProtocolException e) {
	          e.printStackTrace();
	          fail();
	      } catch (IOException e) {
	          e.printStackTrace();
	          fail();
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

	@Test
	public void testBingSearch() {
	    
		String urlStr="https://api.datamarket.azure.com/Bing/Search/v1/Web?$format=json&Query=%27%22Mike%20Watt%22%20%26%20%22Dos%20(band)%22%27";
		String callback=null;
		run(urlStr, callback);
		/*
		 * {"d":
		 * 	{"results":
		 * 		[{"__metadata":{"uri":"https://api.datamarket.azure.com/Data.ashx/Bing/Search/v1/Web?Query=\u0027\"Mike Watt\" & \"Dos (band)\"\u0027&$skip=0&$top=1","type":"WebResult"},
		 * 		  "ID":"a3d52dfa-19bd-40e7-a6b8-8cfc17180377",
		 * 		  "Title":"Dos (band) - Wikipedia, the free encyclopedia",
		 * 		  "Description":"Dos (from the Spanish for \"two\") is an American punk group composed of Mike Watt and Kira Roessler, who both sing and play bass guitar. Critic Greg Prato describes ...",
		 * 		  "DisplayUrl":"en.wikipedia.org/wiki/Dos_(band)","Url":"http://en.wikipedia.org/wiki/Dos_(band)"},{"__metadata":{"uri":"https://api.datamarket.azure.com/Data.ashx/Bing/Search/v1/Web?Query=\u0027\"Mike Watt\" & \"Dos (band)\"\u0027&$skip=1&$top=1","type":"WebResult"},"ID":"5c2c2571-ddd1-416d-b023-2288bac3e69b","Title":"dos (the band)","Description":"mike watt (born in 1957, portsmouth, virigina, usa) started playing music with d. boon at age 13, copying songs off of ccr, the who, ...","DisplayUrl":"www.hootpage.com/hoot_dos.html","Url":"http://www.hootpage.com/hoot_dos.html"},
		 * 		 {"__metadata":{"uri":"https://api.datamarket.azure.com/Data.ashx/Bing/Search/v1/Web?Query=\u0027\"Mike Watt\" & \"Dos (band)\"\u0027&$skip=2&$top=1","type":"WebResult"},
		 * 		  "ID":"b5c224c9-78c8-4c45-befe-04a5edeb1f5c","Title":"Dos | Free Music, Tour Dates, Photos, Videos","Description":"mike watt: bass. Top Friends (15) Dos has 4240 friends. View: All; Online; New; Many Birthdays; dreams are free, mother ...","DisplayUrl":"www.myspace.com/dosasintwo","Url":"http://www.myspace.com/dosasintwo"},
		 *       {"__metadata":{"uri":"https://api.datamarket.azure.com/Data.ashx/Bing/Search/v1/Web?Query=\u0027\"Mike Watt\" & \"Dos (band)\"\u0027&$skip=3&$top=1","type":"WebResult"},"ID":"42420ed8-04fa-4e4d-87c8-fd3a3a434173","Title":"Mike Watt band Dos releasing \u0027Dos y Dos\u0027 (video premiere)","Description":"Mike Watt is currently \"trying his hardest\" on bass for Iggy & The Stooges on the European Festival circuit, and will return in time to play a Dos album release party ...","DisplayUrl":"www.brooklynvegan.com/archives/2011/07/mike_watt_band.html","Url":"http://www.brooklynvegan.com/archives/2011/07/mike_watt_band.html"},
		 *       {"__metadata":{"uri":"https://api.datamarket.azure.com/Data.ashx/Bing/Search/v1/Web?Query=\u0027\"Mike Watt\" & \"Dos (band)\"\u0027&$skip=4&$top=1","type":"WebResult"},"ID":"db7a29c5-5f82-4b50-b503-bb8481a10318","Title":"Mike Watt","Description":"...more on Wikipedia about \"Dos (band)\" fIREHOSE was an indie/ punk band consisting of Mike Watt (bass, vocals), Ed Crawford (guitar, vocals), and George Hurley (drums).","DisplayUrl":"www.shortopedia.com/M/I/Mike_Watt","Url":"http://www.shortopedia.com/M/I/Mike_Watt"},
		 *       {"__metadata":{"uri":"https://api.datamarket.azure.com/Data.ashx/Bing/Search/v1/Web?Query=\u0027\"Mike Watt\" & \"Dos (band)\"\u0027&$skip=5&$top=1","type":"WebResult"},"ID":"95fc0527-cd94-4732-92e6-bd919da30559","Title":"Dos (band) - Wikipedia + social media, news, videos and search","Description":"Dos (from the Spanish for \"two\") is an American punk group composed of Mike Watt and Kira Roessler, who both sing and play bass guitar. Critic Greg Prato [1 ...","DisplayUrl":"www.sidepad.com/Dos_(band)","Url":"http://www.sidepad.com/Dos_(band)"},
		 *       {"__metadata":{"uri":"https://api.datamarket.azure.com/Data.ashx/Bing/Search/v1/Web?Query=\u0027\"Mike Watt\" & \"Dos (band)\"\u0027&$skip=6&$top=1","type":"WebResult"},"ID":"8cce6b17-b4a6-47d8-a5b8-9a7087e3b5dc","Title":"Learn and talk about Mike Watt","Description":"Learn and talk about Mike Watt, and check out Mike Watt on ... Mike Watt albums. The Minutemen songs. Clenchedwrench. Dos (band) Firehose (band)","DisplayUrl":"www.digplanet.com/wiki/Category:Mike_Watt","Url":"http://www.digplanet.com/wiki/Category:Mike_Watt"},
		 *       {"__metadata":{"uri":"https://api.datamarket.azure.com/Data.ashx/Bing/Search/v1/Web?Query=\u0027\"Mike Watt\" & \"Dos (band)\"\u0027&$skip=7&$top=1","type":"WebResult"},"ID":"14a7c167-49e6-4494-8ecb-78b84210d94c","Title":"Mike Watt - Music Is Life @ Artistopia.com","Description":"Artistopia Music is the best source for information on Mike Watt biography, including access to music videos, music ... Minutemen , dos (band)|dos , and ...","DisplayUrl":"www.artistopia.com/mike-watt","Url":"http://www.artistopia.com/mike-watt"},
		 *       {"__metadata":{"uri":"https://api.datamarket.azure.com/Data.ashx/Bing/Search/v1/Web?Query=\u0027\"Mike Watt\" & \"Dos (band)\"\u0027&$skip=8&$top=1","type":"WebResult"},"ID":"d40eeafc-a173-4a8e-b35b-d65694a5a9c2","Title":"Dos (band) - Mashpedia","Description":"Dos (from the Spanish for \"two\") is an American punk group composed of Mike Watt and Kira Roessler, who both sing and play bass guitar. Critic Greg Prato [1 ...","DisplayUrl":"www.mashpedia.com/Dos_(band)","Url":"http://www.mashpedia.com/Dos_(band)"},
		 *       {"__metadata":{"uri":"https://api.datamarket.azure.com/Data.ashx/Bing/Search/v1/Web?Query=\u0027\"Mike Watt\" & \"Dos (band)\"\u0027&$skip=9&$top=1","type":"WebResult"},"ID":"84145244-a103-46ae-82d2-69c607c12a9c","Title":"mike watt | Online friends on Myspace","Description":"mike watt Radio ... Dos. Band; Follow; IM; Send Message","DisplayUrl":"fr.myspace.com/wattfrompedromusic/friends/online","Url":"http://fr.myspace.com/wattfrompedromusic/friends/online"},
		 *       {"__metadata":{"uri":"https://api.datamarket.azure.com/Data.ashx/Bing/Search/v1/Web?Query=\u0027\"Mike Watt\" & \"Dos (band)\"\u0027&$skip=10&$top=1","type":"WebResult"},"ID":"0ab10bd1-91fd-4015-9460-9bdd6e3b4cf8","Title":"Dos - \"Forever\" - YouTube","Description":"http://www.myspace.com/dosasintwo http://www.discogs.com/artist/DOS+(4) http://en.wikipedia.org/wiki/Dos_(band)","DisplayUrl":"www.youtube.com/watch?v=LDZ20IZwUh8","Url":"http://www.youtube.com/watch?v=LDZ20IZwUh8"},
		 *       {"__metadata":{"uri":"https://api.datamarket.azure.com/Data.ashx/Bing/Search/v1/Web?Query=\u0027\"Mike Watt\" & \"Dos (band)\"\u0027&$skip=11&$top=1","type":"WebResult"},"ID":"9d12d7c0-c6ea-414e-bd66-00c934770f5f","Title":"Category:Mike Watt - Wikipedia, the free encyclopedia","Description":"Pages in category \"Mike Watt\" The following 16 pages are in this category, out of 16 total. ... Dos (band) F. Firehose (band) H. Hellride; J. The Jom and Terry Show; M.","DisplayUrl":"en.wikipedia.org/wiki/Category:Mike_Watt","Url":"http://en.wikipedia.org/wiki/Category:Mike_Watt"},
		 *       {"__metadata":{"uri":"https://api.datamarket.azure.com/Data.ashx/Bing/Search/v1/Web?Query=\u0027\"Mike Watt\" & \"Dos (band)\"\u0027&$skip=12&$top=1","type":"WebResult"},"ID":"cc91c1da-8ff8-4b70-ba83-2eeecc0f606b","Title":"Mike Watt Topics at DuckDuckGo","Description":"Clenchedwrench (stylized as clenchedwrench) is an independent record label founded by Mike Watt, ... Dos (band) Dos (from the Spanish for \"two\") ...","DisplayUrl":"duckduckgo.com/c/Mike_Watt","Url":"http://duckduckgo.com/c/Mike_Watt"},
		 *       {"__metadata":{"uri":"https://api.datamarket.azure.com/Data.ashx/Bing/Search/v1/Web?Query=\u0027\"Mike Watt\" & \"Dos (band)\"\u0027&$skip=13&$top=1","type":"WebResult"},"ID":"863c0621-facd-4d30-a2e6-43ffa0be0dee","Title":"Fatty and Friends – Free listening, concerts, stats, & pictures ...","Description":"... after hearing Mike Watt’s Dos band. In the spring of 2008, they recorded and released their first EP called \"Hey Mike!! DEMO\" on their own.","DisplayUrl":"www.last.fm/music/Fatty+and+Friends","Url":"http://www.last.fm/music/Fatty+and+Friends"},
		 *       {"__metadata":{"uri":"https://api.datamarket.azure.com/Data.ashx/Bing/Search/v1/Web?Query=\u0027\"Mike Watt\" & \"Dos (band)\"\u0027&$skip=14&$top=1","type":"WebResult"},"ID":"7025765c-516e-4e1d-946b-4cd04d77a23c","Title":"Dos Band - Pipl Directory","Description":"People named Dos Band. Find the person you\u0027re looking for and related people. Search by Name, Email, ... [ Mike Watt | Artistopia.com - www.artistopia.com] DOS.","DisplayUrl":"https://pipl.com/directory/name/Band/Dos","Url":"https://pipl.com/directory/name/Band/Dos/"},
		 *       {"__metadata":{"uri":"https://api.datamarket.azure.com/Data.ashx/Bing/Search/v1/Web?Query=\u0027\"Mike Watt\" & \"Dos (band)\"\u0027&$skip=15&$top=1","type":"WebResult"},"ID":"b7337a5b-10c2-4c09-9754-f304a5d64619","Title":"Dos - discography, line-up, biography, interviews, photos","Description":"Current line-up: Others bands/comments: Mike Watt : Bass : Kira Roessler : Vocals, Bass : Black Flag","DisplayUrl":"www.spirit-of-rock.com/groupe-groupe-Dos-l-en.html","Url":"http://www.spirit-of-rock.com/groupe-groupe-Dos-l-en.html"},
		 *       {"__metadata":{"uri":"https://api.datamarket.azure.com/Data.ashx/Bing/Search/v1/Web?Query=\u0027\"Mike Watt\" & \"Dos (band)\"\u0027&$skip=16&$top=1","type":"WebResult"},"ID":"030064aa-dbd2-4253-bbf9-bfa492579856","Title":"dos : Free Music : Free Audio : Download & Streaming : Internet ...","Description":"Rights On March 26, 2003, Mike Watt gave the go ahead to Scott Cronin to include his solo material and bands fIREHOSE, Minutemen and dos in the Archive project.","DisplayUrl":"archive.org/details/dos","Url":"http://archive.org/details/dos"},
		 *       {"__metadata":{"uri":"https://api.datamarket.azure.com/Data.ashx/Bing/Search/v1/Web?Query=\u0027\"Mike Watt\" & \"Dos (band)\"\u0027&$skip=17&$top=1","type":"WebResult"},"ID":"51b8821c-d09d-4b39-a9a7-7a8b462855de","Title":"Dos (band)とは - goo Wikipedia (ウィキペディア)","Description":"Dos (from the Spanish for \"two\") is an American punk group composed of Mike Watt and Kira Roessler, who both sing and play bass guitar. Critic Greg Prato [1 ...","DisplayUrl":"wpedia.goo.ne.jp/enwiki/Dos_(band)","Url":"http://wpedia.goo.ne.jp/enwiki/Dos_(band)"},
		 *       {"__metadata":{"uri":"https://api.datamarket.azure.com/Data.ashx/Bing/Search/v1/Web?Query=\u0027\"Mike Watt\" & \"Dos (band)\"\u0027&$skip=18&$top=1","type":"WebResult"},"ID":"2c39ea46-ca69-4d69-9172-c2422bd9486b","Title":"Dos - \"Taking Away the Fire\" - YouTube","Description":"http://en.wikipedia.org/wiki/Dos_(band) Category: Music. Tags: dos; mike watt; kira roessler; License: ... 4:39 Watch Later Error Mike Watt ...","DisplayUrl":"www.youtube.com/watch?v=lH4tcbqsjXI","Url":"http://www.youtube.com/watch?v=lH4tcbqsjXI"},
		 *       {"__metadata":{"uri":"https://api.datamarket.azure.com/Data.ashx/Bing/Search/v1/Web?Query=\u0027\"Mike Watt\" & \"Dos (band)\"\u0027&$skip=19&$top=1","type":"WebResult"},"ID":"6a20559d-9593-444c-adb4-87bb25522de2","Title":"Dos - MusicBrainz","Description":"US indie rock duo Mike Watt & Kira Roessler, Type: Group, Country: United States. ... Dos (band) View all relationships; Last updated on 2011-12-05 09:35 UTC. Dos ...","DisplayUrl":"musicbrainz.org/artist/8105","Url":"http://musicbrainz.org/artist/8105"},
		 *       {"__metadata":{"uri":"https://api.datamarket.azure.com/Data.ashx/Bing/Search/v1/Web?Query=\u0027\"Mike Watt\" & \"Dos (band)\"\u0027&$skip=20&$top=1","type":"WebResult"},"ID":"bfe342bf-47a0-4cf1-941d-24366ebf8fe2","Title":"MINUTEFLAG tribute page | Online friends on Myspace","Description":"MINUTEFLAG tribute page Radio ... Dos. Band; Follow; IM; Send Message ... mike watt","DisplayUrl":"fr.myspace.com/minuteflag/friends/online","Url":"http://fr.myspace.com/minuteflag/friends/online#!"},
		 *       {"__metadata":{"uri":"https://api.datamarket.azure.com/Data.ashx/Bing/Search/v1/Web?Query=\u0027\"Mike Watt\" & \"Dos (band)\"\u0027&$skip=21&$top=1","type":"WebResult"},"ID":"4331aa81-e41d-4a43-a85b-bfcf9190f60e","Title":"Topic: Dos (band) - Factbites: Where results make sense","Description":"Dos is a curious yet interesting double bass guitar side project for Mike Watt (of the Minutemen and fIREHOSE) ... The PR DOS band gap is associated with its dominant minimum.","DisplayUrl":"www.factbites.com/topics/Dos-(band)","Url":"http://www.factbites.com/topics/Dos-%28band%29"},
		 *       {"__metadata":{"uri":"https://api.datamarket.azure.com/Data.ashx/Bing/Search/v1/Web?Query=\u0027\"Mike Watt\" & \"Dos (band)\"\u0027&$skip=22&$top=1","type":"WebResult"},"ID":"70062557-c958-45f6-9203-4f1d364b4fa7","Title":"Learn and talk about New Alliance Records artists, Artists by ...","Description":"Bob Mould. Ciccone Youth. Descendents. Dos (band) Hüsker Dü. Kira Roessler. Mike Watt. Minutemen (band) Secret Hate","DisplayUrl":"www.digplanet.com/wiki/Category:New_Alliance_Records_artists","Url":"http://www.digplanet.com/wiki/Category:New_Alliance_Records_artists"},
		 *       {"__metadata":{"uri":"https://api.datamarket.azure.com/Data.ashx/Bing/Search/v1/Web?Query=\u0027\"Mike Watt\" & \"Dos (band)\"\u0027&$skip=23&$top=1","type":"WebResult"},"ID":"66d27611-20c8-47a6-b94d-0ecf38606895","Title":"Dos (band)","Description":"This page contains a list of user images about Dos (band) which are relevant to the point and besides images, you can also use the tabs in the bottom to browse Dos ...","DisplayUrl":"solution-nine.com/Dos_(band)","Url":"http://solution-nine.com/Dos_(band)"},
		 *       {"__metadata":{"uri":"https://api.datamarket.azure.com/Data.ashx/Bing/Search/v1/Web?Query=\u0027\"Mike Watt\" & \"Dos (band)\"\u0027&$skip=24&$top=1","type":"WebResult"},"ID":"7b814d0f-c39f-4b33-98b0-a11cb26e3bf9","Title":"New Alliance Records artists: Belongs To | Times of India","Description":"Dos (band) Hüsker Dü. Minutemen (band) Bob Mould . Kira Roessler. Secret Hate. Mike Watt. Stories from the Network. LATEST NEWS; MOST READ; MOST COMMENTED; MOST","DisplayUrl":"timesofindia.indiatimes.com/topiclist/New-Alliance-Records-artists","Url":"http://timesofindia.indiatimes.com/topiclist/New-Alliance-Records-artists"},
		 *       {"__metadata":{"uri":"https://api.datamarket.azure.com/Data.ashx/Bing/Search/v1/Web?Query=\u0027\"Mike Watt\" & \"Dos (band)\"\u0027&$skip=25&$top=1","type":"WebResult"},"ID":"ba0b4089-5659-4656-9887-5475a49fd3c6","Title":"DOS (disambiguation)","Description":", a video game for the Nintendo DS. Dos may refer to: Dos (band), an American band that consists of the bass rock duo Mike Watt and Kira Roessler.","DisplayUrl":"english.turkcebilgi.com/DOS+(disambiguation)","Url":"http://english.turkcebilgi.com/DOS+(disambiguation)"},
		 *       {"__metadata":{"uri":"https://api.datamarket.azure.com/Data.ashx/Bing/Search/v1/Web?Query=\u0027\"Mike Watt\" & \"Dos (band)\"\u0027&$skip=26&$top=1","type":"WebResult"},"ID":"0110fcf7-58cb-4cf4-9e1e-a53f63bb3809","Title":"Kira Roessler - Wikipedia","Description":"Dos: Band precedenti: Black Flag DC3 Twisted Roots Sexsick: Album pubblicati: 9: Studio: 8: Live: 1: ... Dopo lo scioglimento dei Black Flag formò i Dos con Mike Watt ...","DisplayUrl":"it.wikipedia.org/wiki/Kira_Roessler","Url":"http://it.wikipedia.org/wiki/Kira_Roessler"},
		 *       {"__metadata":{"uri":"https://api.datamarket.azure.com/Data.ashx/Bing/Search/v1/Web?Query=\u0027\"Mike Watt\" & \"Dos (band)\"\u0027&$skip=27&$top=1","type":"WebResult"},"ID":"dd7b9441-d4fe-4daa-a07c-b0d81f3b2313","Title":"Chi e\u0027 Black Kira - waatp.it","Description":"Chi e\u0027 Black Kira - waatp.it.Guarda anche Black Kira: foto, profili reti sociali, videoclip, weblink, ai blogs, alle notizie, libri, profili microblogs","DisplayUrl":"waatp.it/people/black-kira","Url":"http://waatp.it/people/black-kira/"},
		 *       {"__metadata":{"uri":"https://api.datamarket.azure.com/Data.ashx/Bing/Search/v1/Web?Query=\u0027\"Mike Watt\" & \"Dos (band)\"\u0027&$skip=28&$top=1","type":"WebResult"},"ID":"78528e33-7016-4e12-be55-85aaa1f03cd7","Title":"mike watt + the secondmen - \"el mar cura todo in europe too\" tour ...","Description":"mike watt + the secondmen \"el mar cura todo in europe too\" tour 2005 diary week 5. paul roessler - organ, singing raul morales - drums ... we still have our dos band.","DisplayUrl":"www.hootpage.com/hoot_elmarcuraeurodiary5.html","Url":"http://www.hootpage.com/hoot_elmarcuraeurodiary5.html"},
		 *       {"__metadata":{"uri":"https://api.datamarket.azure.com/Data.ashx/Bing/Search/v1/Web?Query=\u0027\"Mike Watt\" & \"Dos (band)\"\u0027&$skip=29&$top=1","type":"WebResult"},"ID":"f2083a35-6538-4202-96bb-6afcd76ebb08","Title":"Latest News and Information on Hoot (EP)","Description":"... 3000 album) S.M. Entertainment discography Dos (band) Minutemen (band) List of UK Singles Chart Christmas number twos Megan and Liz Girls\u0027 Generation discography Mike Watt The Cu ...","DisplayUrl":"www.background-checks.sure-review.com/press/Hoot-(EP).html","Url":"http://www.background-checks.sure-review.com/press/Hoot-(EP).html"},
		 *       {"__metadata":{"uri":"https://api.datamarket.azure.com/Data.ashx/Bing/Search/v1/Web?Query=\u0027\"Mike Watt\" & \"Dos (band)\"\u0027&$skip=30&$top=1","type":"WebResult"},"ID":"80d0d1f0-5750-4d2d-98f8-996ca34f7540","Title":"THE CREW on Myspace","Description":"... Generation X (band) Generiks George Hurley and Mike Watt ... Disconvenience Disrupters Divide & Conquer The Divine Horsemen Dos (band) Dow Jones and ...","DisplayUrl":"www.myspace.com/thecrewofevil","Url":"http://www.myspace.com/thecrewofevil#!"},
		 *       {"__metadata":{"uri":"https://api.datamarket.azure.com/Data.ashx/Bing/Search/v1/Web?Query=\u0027\"Mike Watt\" & \"Dos (band)\"\u0027&$skip=31&$top=1","type":"WebResult"},"ID":"82ee25dc-5fd0-4c23-9290-8e9b452216c1","Title":"Firehose (band) in Encyclopedia","Description":"Since disbanding, Mike Watt has released four solo albums and been involved in numerous musical projects including the longstanding bass duo, Dos (with ex-Black Flag ...","DisplayUrl":"www.tutorgigpedia.com/ed/Firehose_(band)","Url":"http://www.tutorgigpedia.com/ed/Firehose_(band)"},
		 *       {"__metadata":{"uri":"https://api.datamarket.azure.com/Data.ashx/Bing/Search/v1/Web?Query=\u0027\"Mike Watt\" & \"Dos (band)\"\u0027&$skip=32&$top=1","type":"WebResult"},"ID":"50f5a0d5-256d-48d7-927f-74656eb7e6a8","Title":"DoS Meaning and Example Sentence: Meaning, definition, sample ...","Description":"Dos (band): Dos (from the Spanish for \"two\") is an American punk group composed of Mike Watt and Kira Roessler , ...","DisplayUrl":"www.dictionary30.com/meaning/DoS","Url":"http://www.dictionary30.com/meaning/DoS"},
		 *       {"__metadata":{"uri":"https://api.datamarket.azure.com/Data.ashx/Bing/Search/v1/Web?Query=\u0027\"Mike Watt\" & \"Dos (band)\"\u0027&$skip=33&$top=1","type":"WebResult"},"ID":"47d97270-3508-4217-a7fb-59d0766804d5","Title":"Dictionary - Definition of DOS","Description":"Earth\u0027s largest dictionary with more than 1226 modern languages and Eve!","DisplayUrl":"www.websters-online-dictionary.org/definitions/DOS","Url":"http://www.websters-online-dictionary.org/definitions/DOS"},
		 *       {"__metadata":{"uri":"https://api.datamarket.azure.com/Data.ashx/Bing/Search/v1/Web?Query=\u0027\"Mike Watt\" & \"Dos (band)\"\u0027&$skip=34&$top=1","type":"WebResult"},"ID":"4ed1da53-9f90-46c6-9b87-8470741e2845","Title":"Kill Rock Stars","Description":"Number of entries: 115 ... Dos (band) ... Mike Watt","DisplayUrl":"www.at1ce.org/themenreihe.p?c=Kill%20Rock%20Stars","Url":"http://www.at1ce.org/themenreihe.p?c=Kill%20Rock%20Stars"},
		 *       {"__metadata":{"uri":"https://api.datamarket.azure.com/Data.ashx/Bing/Search/v1/Web?Query=\u0027\"Mike Watt\" & \"Dos (band)\"\u0027&$skip=35&$top=1","type":"WebResult"},"ID":"09acd601-327e-4c0f-bdd3-28947cfbf807","Title":"huggies - PriceCheck Shopping South Africa","Description":"... Mike Watt, Sleater-Kinney, Huggy Bear (Band), Free Kitten, Deerhoof, Heavens to Betsy, Unwound, DOS (Band), Comet Gain, Marnie Stern ...","DisplayUrl":"www.pricecheck.co.za/search/?search=huggies","Url":"http://www.pricecheck.co.za/search/?search=huggies"},
		 *       {"__metadata":{"uri":"https://api.datamarket.azure.com/Data.ashx/Bing/Search/v1/Web?Query=\u0027\"Mike Watt\" & \"Dos (band)\"\u0027&$skip=36&$top=1","type":"WebResult"},"ID":"0a961896-2da0-42b6-a890-de52d6d6550c","Title":"MetaGlossary.com: Dos","Description":"Dos is an American Indie rock/Punk rock group comprised of Mike Watt and Kira Roessler, who both sing and play bass guitar. http://en.wikipedia.org/wiki/Dos_(band)","DisplayUrl":"metaglossary.com/meanings/532863","Url":"http://metaglossary.com/meanings/532863/"},
		 *       {"__metadata":{"uri":"https://api.datamarket.azure.com/Data.ashx/Bing/Search/v1/Web?Query=\u0027\"Mike Watt\" & \"Dos (band)\"\u0027&$skip=37&$top=1","type":"WebResult"},"ID":"25ceea71-54f9-4d5e-a41b-096ae7fa3329","Title":"Encyclopedia entries starting with DOS - Who or What is ...","Description":"Dos (band) Dos is a two-bass indie/punk band featuring the former husband-and-wife team of Mike Watt (Minutemen, fIREHOSE, The Stooges, Banyan) ...","DisplayUrl":"encycl.opentopia.com/D/DO/DOS","Url":"http://encycl.opentopia.com/D/DO/DOS"},
		 *       {"__metadata":{"uri":"https://api.datamarket.azure.com/Data.ashx/Bing/Search/v1/Web?Query=\u0027\"Mike Watt\" & \"Dos (band)\"\u0027&$skip=38&$top=1","type":"WebResult"},"ID":"354b4889-7170-45d4-88ee-b1ae70bb93c2","Title":"Gabbing With Gus | Music, News, Interviews","Description":"... the two bass duo with Mike Watt as well as loaning her four string tallents to ... Bassist, Bassists, Black Flag, Dos band, entertainment, Gabbing With ...","DisplayUrl":"gabbingwithgus.wordpress.com","Url":"http://gabbingwithgus.wordpress.com/"},
		 *       {"__metadata":{"uri":"https://api.datamarket.azure.com/Data.ashx/Bing/Search/v1/Web?Query=\u0027\"Mike Watt\" & \"Dos (band)\"\u0027&$skip=39&$top=1","type":"WebResult"},"ID":"217628f9-2cf6-4238-8dcd-c22715ec93a1","Title":"Musical groups founded by married couples","Description":"4 Dos (band) 5 New Victory Band. 6 Resurrection Band. 7 Low (band) 8 Wings (band) 9 The A-Bones. 10 X (American band) 11 Starland Vocal Band. 12 The Sundays. 13 Dean and Britta.","DisplayUrl":"www.at1ce.org/themenreihe.p?c=Musical%20groups%20founded%20by%20...","Url":"http://www.at1ce.org/themenreihe.p?c=Musical%20groups%20founded%20by%20married%20couples"},
		 *       {"__metadata":{"uri":"https://api.datamarket.azure.com/Data.ashx/Bing/Search/v1/Web?Query=\u0027\"Mike Watt\" & \"Dos (band)\"\u0027&$skip=40&$top=1","type":"WebResult"},"ID":"afd107ca-1e20-404d-a24c-06562cf4d2db","Title":"Black Flag - Music Is Life @ Artistopia.com","Description":"Kira Roessler continues to record and perform with the band DOS, a duet with then-husband and Minutemen bassist Mike Watt. In September 2003, ...","DisplayUrl":"www.artistopia.com/black-flag","Url":"http://www.artistopia.com/black-flag"},
		 *       {"__metadata":{"uri":"https://api.datamarket.azure.com/Data.ashx/Bing/Search/v1/Web?Query=\u0027\"Mike Watt\" & \"Dos (band)\"\u0027&$skip=41&$top=1","type":"WebResult"},"ID":"a71230ce-5967-4cdc-aee3-82c22522dc18","Title":"Loot.co.za: Sitemap","Description":"9781840003628 1840003626 Floral Living 9781576471197 1576471195 A Charles Ives Omnibus, James A. Burk 9780394251349 0394251342 Remains of the Day, Kazuo Ishiguro","DisplayUrl":"www2.loot.co.za/index/html/index2452.html","Url":"http://www2.loot.co.za/index/html/index2452.html"},
		 *       {"__metadata":{"uri":"https://api.datamarket.azure.com/Data.ashx/Bing/Search/v1/Web?Query=\u0027\"Mike Watt\" & \"Dos (band)\"\u0027&$skip=42&$top=1","type":"WebResult"},"ID":"36f94f83-275d-411b-b83a-16bed6380ab9","Title":"Loot.co.za: Sitemap","Description":"9781408672754 1408672758 St. Teresa\u0027s Book-Mark - A Meditative Commentary, De San Joseph Lucas 9781436883504 1436883504 Janus, Lake Sonnets, Etc. and Other Poems ...","DisplayUrl":"www2.loot.co.za/index/html/index98.html","Url":"http://www2.loot.co.za/index/html/index98.html"},
		 *       {"__metadata":{"uri":"https://api.datamarket.azure.com/Data.ashx/Bing/Search/v1/Web?Query=\u0027\"Mike Watt\" & \"Dos (band)\"\u0027&$skip=43&$top=1","type":"WebResult"},"ID":"f8132fa6-d3db-403e-a8fd-8d6e7e0da456","Title":"www.todddunkel.com","Description":"ISBN: 1244559822; TITLE: Sst Records Artists, including: Kira Roessler, Mike Watt ... Hüsker Dü, Ciccone Youth, Dos (band), Minutemen (band), Meat ... Bad ...","DisplayUrl":"www.todddunkel.com/isbn/isbnindex0822.html","Url":"http://www.todddunkel.com/isbn/isbnindex0822.html"},
		 *       {"__metadata":{"uri":"https://api.datamarket.azure.com/Data.ashx/Bing/Search/v1/Web?Query=\u0027\"Mike Watt\" & \"Dos (band)\"\u0027&$skip=44&$top=1","type":"WebResult"},"ID":"c6146d86-24eb-4520-931a-8464b5d51d1c","Title":"Dos: Information from Answers.com","Description":"Dos Genres: Rock Biography Dos is a curious yet interesting double bass guitar side project for Mike Watt (of the Minutemen and fIREHOSE ) and his former","DisplayUrl":"www.answers.com/topic/dos-band","Url":"http://www.answers.com/topic/dos-band"},
		 *       {"__metadata":{"uri":"https://api.datamarket.azure.com/Data.ashx/Bing/Search/v1/Web?Query=\u0027\"Mike Watt\" & \"Dos (band)\"\u0027&$skip=45&$top=1","type":"WebResult"},"ID":"528a82d4-4f17-46c6-abde-740d81741a26","Title":"wn.com","Description":"","DisplayUrl":"wn.com/pt_Mike_Watt","Url":"http://wn.com/pt_Mike_Watt"},
		 *       {"__metadata":{"uri":"https://api.datamarket.azure.com/Data.ashx/Bing/Search/v1/Web?Query=\u0027\"Mike Watt\" & \"Dos (band)\"\u0027&$skip=46&$top=1","type":"WebResult"},"ID":"94cb8c2b-2fa0-48a6-a541-7f884aa542d9","Title":"Mike Watt - World News","Description":"Mike Watt Interview and Bass Lesson. PlayThisRiff.com, Mike Watt with Dave Grohl, Pat Smear, Eddie Vedder and the Missingmen in Seattle, mike watt - big train, Mike ...","DisplayUrl":"wn.com/Mike_Watt","Url":"http://wn.com/Mike_Watt"},
		 *       {"__metadata":{"uri":"https://api.datamarket.azure.com/Data.ashx/Bing/Search/v1/Web?Query=\u0027\"Mike Watt\" & \"Dos (band)\"\u0027&$skip=47&$top=1","type":"WebResult"},"ID":"912a474f-0014-4a07-947b-51666ba28610","Title":"Mike Kinney - Mitra Celebrities :: Celebrity Resources On The Net","Description":"List of web resources, latest news, images, videos, blog postings, and realtime conversation about mike kinney. We also provide some recomendation so you can surf the ...","DisplayUrl":"celebrities.mitrasites.com/mike-kinney.html","Url":"http://celebrities.mitrasites.com/mike-kinney.html"},
		 *       {"__metadata":{"uri":"https://api.datamarket.azure.com/Data.ashx/Bing/Search/v1/Web?Query=\u0027\"Mike Watt\" & \"Dos (band)\"\u0027&$skip=48&$top=1","type":"WebResult"},"ID":"e90d330b-028c-41a5-91dc-432c9132d6ea","Title":"Uno Con Dos: Information from Answers.com","Description":"Fans of Mike Watt\u0027s bassy banterings will love Uno Con Dos. ~ Greg Prato, Rovi ... Dos (band) Kira Roessler. Mike Watt. Answers Properties. Answers;","DisplayUrl":"www.answers.com/topic/uno-con-dos-1991-album-by-dos","Url":"http://www.answers.com/topic/uno-con-dos-1991-album-by-dos"},
		 *       {"__metadata":{"uri":"https://api.datamarket.azure.com/Data.ashx/Bing/Search/v1/Web?Query=\u0027\"Mike Watt\" & \"Dos (band)\"\u0027&$skip=49&$top=1","type":"WebResult"},"ID":"4fe1a848-82d2-448b-a5b8-5adbf5774ed2","Title":"Mike Watt shows different frame of mind in \u0027On and Off Bass ...","Description":"Punk mainstay Mike Watt was out late playing a show at the local ballet school the night before, but early on a Saturday, the bassist best known for his ...","DisplayUrl":"article.wn.com/view/2012/05/05/Mike_Watt_shows_different_frame_of...","Url":"http://article.wn.com/view/2012/05/05/Mike_Watt_shows_different_frame_of_mind_in_On_and_Off_Bass/"}],"__next":"https://api.datamarket.azure.com/Data.ashx/Bing/Search/v1/Web?Query=\u0027%22Mike%20Watt%22%20&%20%22Dos%20(band)%22\u0027&$skip=50"}}

		 */
	}
	
	
	
	

}
