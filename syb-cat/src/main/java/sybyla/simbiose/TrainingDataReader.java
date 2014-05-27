package sybyla.simbiose;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import sybyla.avro.CategoryWebPageToAvro;
import sybyla.feature.FeatureExtractor;
import sybyla.feature.Normalizer;
import sybyla.generated.avro.CategoryWebPage;
import sybyla.http.SimplePageParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Copyright SocialInSoma, 2013
 * User: kiko
 * Date: 4/15/14
 * Time: 5:46 PM
 */
public class TrainingDataReader
{
    private static Logger LOG = Logger.getLogger(TrainingDataReader.class);
    private static String trainingDataFile = "/simbiose/trainingData.txt";
    private static int FEATURE_EXTRACTOR_LENGTH= 5;
    private static String avroOutputDirectory = "/Users/kiko/sybyla/git/sybyla/syb-cat/src/test/resources/simbiose/avro/";
    private static String avroFileRoot = "simbioseCategoryPages";
    private static CategoryWebPageToAvro cwpa;

    private BufferedReader reader;
    private FeatureExtractor featureExtractor;
    private int nLines=0;
    private int nPages =0;
    private Map<String, Set<String>> topCategoriesMap = new HashMap<>();
    private Map<String, String> portugueseEnglishCategories = new HashMap<>();
    private Map<String, Integer> categoryCounts = new HashMap<>();
    private int nFeatures;
    private int nRecords = 10;

    public static final String FULL_CATEGORY="fullCategory";
    public static final String CATEGORY ="category";
    public static final String TOP_CATEGORY="topCategory";
    public static final String PORTUGUESE_CATEGORY="portugueseCategory";
    public static final String URL = "url";
    public static final String LEVEL = "level";

    public TrainingDataReader() throws UnsupportedEncodingException
    {
        InputStream is = TrainingDataReader.class.getResourceAsStream(trainingDataFile);
        reader = new BufferedReader(new InputStreamReader(is,"UTF-8"));
        featureExtractor = new FeatureExtractor(FEATURE_EXTRACTOR_LENGTH);
        cwpa = new CategoryWebPageToAvro(avroOutputDirectory, avroFileRoot);
    }

    public String normalize(String line)
    {
        String normalized = line.replaceAll("\"[^\\p{ASCII}]\", \"\"]", "");

        return normalized.trim().toLowerCase();
    }

    public void readCategories() throws IOException
    {
        String line = null;

        while((line = reader.readLine()) != null)
        {
            List<Map<String, String>> maps  = parseLine(line);

            if (maps == null){
                continue;
            }

            if (maps.size() == 0){
                continue;
            }

            String fullCategory = maps.get(0).get(FULL_CATEGORY);

            String topCategory = maps.get(0).get(TOP_CATEGORY);
            String category = maps.get(0).get(CATEGORY);
            String portugueseCategory = maps.get(0).get(PORTUGUESE_CATEGORY);

            if (topCategory.equals("")){

                LOG.error("no top category in line \n"+line);
            } else {

                Set<String> childrenCategories = topCategoriesMap.get(topCategory);

                if (childrenCategories == null) {
                    childrenCategories = new HashSet<>();
                    topCategoriesMap.put(topCategory, childrenCategories);
                }

                childrenCategories.add(fullCategory);
            }

            portugueseEnglishCategories.put(fullCategory, portugueseCategory);
            if (portugueseCategory.equals("")){
                LOG.error("No portuguese category in line \n"+line);
            }

            if (category.equals("")){
                LOG.error("No category in line\n"+line);
            }
        }

        reader.close();
        InputStream is = TrainingDataReader.class.getResourceAsStream(trainingDataFile);
        reader = new BufferedReader(new InputStreamReader(is,"UTF-8"));

        System.out.println("Categories Read");

        for (String topCategory: topCategoriesMap.keySet()) {

            Set<String> categories = topCategoriesMap.get(topCategory);
            for(String category: categories){
                String portugueseCategory = portugueseEnglishCategories.get(category);
                System.out.println("Top Category: "+topCategory+ "  Category: "+category+ "  Portuguese Category: "+portugueseCategory);
            }
        }
    }

    private List<Map<String,String>> parseLine(String line) throws IOException
    {
        if  (line == null || line.startsWith("#") || line.trim().equals("")) {
            return null;
        }

        String l =  normalize(line);

        String[] tokens = l.split("\\t");

        if (tokens.length < 5) {
            return null;
        }

        Pattern MULTIPLE_SPACES_PATTERN =  Pattern.compile("([\\s]+)");

        for(int i=0; i<tokens.length; i++){

            tokens[i] =  MULTIPLE_SPACES_PATTERN.matcher(tokens[i]).replaceAll(" ");

        }

        List<Map<String,String>> maps = new ArrayList<>();

        Map<String, String> map = new HashMap<>();

        String fullCategory = tokens[0].trim().toLowerCase();
        map.put(FULL_CATEGORY, fullCategory);

        String[] categories = fullCategory.split(">");

        String topCategory =  categories[0].trim().toLowerCase();
        map.put(TOP_CATEGORY, topCategory);

        String category = tokens[1].trim().toLowerCase();
        map.put(CATEGORY,category);

        String portugueseCategory =  tokens[2].trim().toLowerCase();
        map.put(PORTUGUESE_CATEGORY, portugueseCategory);

        String level = tokens[3];
        map.put(LEVEL, level);

        for (int i = 4; i < tokens.length; i++) {

            String url = tokens[i].trim();

            if (url ==  null || url.trim().equals("")){
                continue;
            }

            Map<String, String> m = new HashMap<>(map);
            m.put(URL, url);
            maps.add(m);
        }

        return maps;
    }



    private List<String> getNegativeCategories(String topCategory, int n)
    {
        int p = 0;
        List<String> result = new ArrayList<>();
        int count = 0;

        while (count < n ){

            for(String tc: topCategoriesMap.keySet()){

                if (tc.equals(topCategory)){

                    continue;
                }

                Set<String> categories = topCategoriesMap.get(tc);
                int index = 0;

                for ( String category: categories){

                    if (index == p){
                        result.add(category);
                        count++;
                        break;
                    }
                    index++;
                }
                if (count ==  n){
                    break;
                }
            }
            p++;
        }


        return result;
    }

    public void read() throws IOException
    {
        readCategories();
        String line =  null;

        while((line = reader.readLine())!=null){

            nLines++;

            List<Map<String, String>> maps  = parseLine(line);

            if (maps == null){
                continue;
            }

            if (maps.size() == 0){
                continue;
            }

            String fullCategory = maps.get(0).get(FULL_CATEGORY);

            String topCategory = maps.get(0).get(TOP_CATEGORY);
            String category = maps.get(0).get(CATEGORY);
            String portugueseCategory = maps.get(0).get(PORTUGUESE_CATEGORY);

            String[] categories = fullCategory.split(">");

            List<CharSequence> categoryList = new ArrayList<>();

            for(int i=0;i<categories.length;i++) {

                String c =  categories[i].trim().toLowerCase();

                if (!c.equals("")) {

                    categoryList.add(c);
                }
            }

            String lv = maps.get(0).get(LEVEL);

            int level=-1;

            try{

                level = Integer.parseInt(lv);

            } catch(NumberFormatException e){

                LOG.error("error parsing level for line "+line);
            }

            List<String> pages = new ArrayList<>();

            for (Map<String,String> map: maps) {

                String url = map.get(URL);

                if (!url.equals("")) {

                    pages.add(url);
                    String content = getContent(url);

                    if (content == null) {
                        continue;
                    }

                    SimplePageParser parser = new SimplePageParser();
                    String text =  parser.pageText(content);
                    Normalizer.normalize(text);
                    List<String> features = featureExtractor.extractFeatures(text);
                    List<CharSequence> f = new ArrayList<>();


                    for (String feature: features){
                        f.add(feature);
                    }

                    nFeatures += f.size();

                    if (features ==  null || features.size() == 0){
                        continue;
                    }

                    List<String> nc = getNegativeCategories(topCategory, level+1);
                    Set<CharSequence>  ncs = new HashSet<>();

                    for(String negativeCategory: nc){

                        ncs.add(negativeCategory);
                    }

                    List<CharSequence> negativeCategories = new ArrayList<>(ncs);

                    CategoryWebPage categoryWebPage =  new CategoryWebPage();
                    categoryWebPage.setUrl(url);
                    categoryWebPage.setCategories(categoryList);
                    categoryWebPage.setTopCategory(topCategory);
                    categoryWebPage.setBottomCategory(category);
                    categoryWebPage.setContent(text);
                    categoryWebPage.setFeatures(f);
                    categoryWebPage.setPortugueseCategory(portugueseCategory);
                    categoryWebPage.setLevel(level);
                    categoryWebPage.setNegativeCategories(negativeCategories);
                    categoryWebPage.setFullCategory(fullCategory);

                    try {
                        cwpa.write(categoryWebPage);
                        Integer count = categoryCounts.get(category);
                        if (count == null){
                            count = new Integer(0);
                        }
                        count =  count.intValue()+1;
                        categoryCounts.put(category,count);

                    } catch (Throwable t) {
                        LOG.error("Error writing avro file", t);
                    }
                }
            }

            nPages++;
            if (nRecords > 0 && nPages == nRecords){
                break;
            }

        }

        System.out.println(nPages +" categories with training data read");
        System.out.println(nFeatures+ " Features read");
        List<String> categories = new ArrayList<>(categoryCounts.keySet());
        Collections.sort(categories);
        for(String category: categories){
            Integer count = categoryCounts.get(category);
            System.out.println("Category "+category+" has "+count+" documents read as examples");
        }
    }

    private String getContent(String url)
    {
        try{
            Connection connection = Jsoup.connect(url).userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:10.0) Gecko/20100101 Firefox/10.0").referrer("http://www.google.com");
            Document document = connection.get();
            String text = document.toString();
            return text;
        } catch(Throwable t){
            LOG.error("Error getting content for URL " + url);
            return null;
        }
    }


}
