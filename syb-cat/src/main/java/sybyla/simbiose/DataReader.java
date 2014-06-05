package sybyla.simbiose;

import org.apache.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import sybyla.avro.CategoryWebPageToAvro;
import sybyla.feature.FeatureExtractor;
import sybyla.feature.Normalizer;
import sybyla.generated.avro.CategoryWebPage;
import sybyla.http.SimplePageParser;
import sybyla.mapred.workflow.ClassifierFlow;

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
public class DataReader
{
    private static Logger LOG = Logger.getLogger(DataReader.class);
    private static String trainingDataFile = "/simbiose/trainingData.txt";
    private static int FEATURE_EXTRACTOR_LENGTH= 2;
    private static String avroOutputDirectory = "/Users/kiko/sybyla/simbiose/input/prod/";
    private static String avroFileRoot = "simbioseCategoryPages";
    private static CategoryWebPageToAvro cwpa;

    private BufferedReader reader;
    private FeatureExtractor featureExtractor;
    private int nLines=0;
    private int nPages =0;
    private Map<String, Set<String>> topCategoriesMap = new HashMap<>();
    private Map<String, Set<String>> urlCategoriesMap = new HashMap<>();
    private List<String> allCategories = new ArrayList<>();
    private int categoriesCursor = 0;

    private Map<String, String> portugueseEnglishCategories = new HashMap<>();
    private Map<String, Integer> categoryCounts = new HashMap<>();
    private int nFeatures;
    private int nRecords = -1;

    public static final String FULL_CATEGORY="fullCategory";
    public static final String CATEGORY ="category";
    public static final String TOP_CATEGORY="topCategory";
    public static final String PORTUGUESE_CATEGORY="portugueseCategory";
    public static final String URL = "url";
    public static final String LEVEL = "level";

    public DataReader() throws UnsupportedEncodingException
    {
        InputStream is = DataReader.class.getResourceAsStream(trainingDataFile);
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
        int nLines = 0;

        Set<String> allCategoriesSet = new HashSet<>();

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

            String category = maps.get(0).get(CATEGORY);
            String portugueseCategory = maps.get(0).get(PORTUGUESE_CATEGORY);
            portugueseEnglishCategories.put(fullCategory, portugueseCategory);
            if (portugueseCategory.equals("")){
                LOG.error("No portuguese category in line \n"+line);
            }

            if (category.equals("")){
                LOG.error("No category in line\n"+line);
            }

            Set<String> parentCategories = ClassifierFlow.getAllParentCategories(fullCategory);

            allCategoriesSet.add(fullCategory);
            allCategoriesSet.addAll(parentCategories);

            for (Map<String,String> map: maps){

                String url = map.get(URL);
                Set<String> allURLCategories = urlCategoriesMap.get(url);

                if (allURLCategories ==  null){

                    allURLCategories =  new HashSet<>();
                    urlCategoriesMap.put(url,allURLCategories);
                }
                allURLCategories.add(fullCategory);
                allURLCategories.addAll(parentCategories);

            }

            nLines++;

            if (nRecords > 0 && nLines == nRecords){
                break;
            }
        }

        reader.close();
        InputStream is = DataReader.class.getResourceAsStream(trainingDataFile);
        reader = new BufferedReader(new InputStreamReader(is,"UTF-8"));

        allCategories.addAll(allCategoriesSet);
        Collections.sort(allCategories);

        System.out.println("Categories Read");

        for (String category: allCategories){
            System.out.println(category);
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



    private Set<String> getNegativeCategories(String url, int n)
    {
       Set<String> negativeCategories = new HashSet<>();
       int count = 0;
       int previousCursorPosition =  categoriesCursor;

       Set<String> urlCategories = urlCategoriesMap.get(url);

       while (count < n){

           String c = allCategories.get(categoriesCursor);

           if (urlCategories.contains(c) == false){
                negativeCategories.add(c);
                count++;
           }

           categoriesCursor++;

           if (categoriesCursor == allCategories.size()){
               categoriesCursor = 0;
           }

           if (categoriesCursor == previousCursorPosition){
               break;
           }
       }

        return negativeCategories;

    }

    public void read() throws IOException
    {
        readCategories();
        String line =  null;
        nPages = 0;
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
                    String pageText = getText(content);
                    List<String> features = getFeatures(pageText);
                    List<CharSequence> f = new ArrayList<>();


                    for (String feature: features){
                        f.add(feature);
                    }

                    nFeatures += f.size();

                    if (features ==  null || features.size() == 0){
                        continue;
                    }

                    Set<String> nc = getNegativeCategories(url,level);
                    List<CharSequence>  negativeCategories = new ArrayList<>();

                    for(String negativeCategory: nc){

                        negativeCategories.add(negativeCategory);
                    }


                    CategoryWebPage categoryWebPage =  new CategoryWebPage();
                    categoryWebPage.setUrl(url);
                    categoryWebPage.setCategories(categoryList);
                    categoryWebPage.setTopCategory(topCategory);
                    categoryWebPage.setBottomCategory(category);
                    categoryWebPage.setContent(pageText);
                    categoryWebPage.setFeatures(f);
                    categoryWebPage.setPortugueseCategory(portugueseCategory);
                    categoryWebPage.setLevel(level);
                    categoryWebPage.setNegativeCategories(negativeCategories);
                    categoryWebPage.setFullCategory(fullCategory);

                    try {
                        cwpa.write(categoryWebPage);
                        Integer count = categoryCounts.get(fullCategory);
                        if (count == null){
                            count = new Integer(0);
                        }
                        count =  count.intValue()+1;
                        categoryCounts.put(fullCategory,count);

                    } catch (Throwable t) {
                        LOG.error("Error writing avro file", t);
                    }
                    /*
                    String parentCategory = getImmediateParentCategory(fullCategory);
                    if ( parentCategory !=  null){

                        String[] parentIndividualCategories = parentCategory.split(">");
                        List<CharSequence> parentCategoryList = new ArrayList<>();
                        String parentTopCategory =  categories[0].trim().toLowerCase();
                        assert(parentTopCategory.equals(topCategory));
                        String parentBottomCategory = parentIndividualCategories[parentIndividualCategories.length-1].trim().toLowerCase();
                        String parentCategoryPortuguese =  portugueseEnglishCategories.get(parentCategory);

                        if (parentCategoryPortuguese == null) {
                            parentCategoryPortuguese ="Não há nome em Português para esta categoria";
                        }

                        int parentCategoryLevel = parentIndividualCategories.length;

                        CategoryWebPage parentCategoryWebPage =  new CategoryWebPage();
                        parentCategoryWebPage.setUrl(url);
                        parentCategoryWebPage.setCategories(parentCategoryList);
                        parentCategoryWebPage.setTopCategory(parentTopCategory);
                        parentCategoryWebPage.setBottomCategory(parentBottomCategory);
                        parentCategoryWebPage.setContent(text);
                        parentCategoryWebPage.setFeatures(f);
                        parentCategoryWebPage.setPortugueseCategory(parentCategoryPortuguese);
                        parentCategoryWebPage.setLevel(parentCategoryLevel);
                        parentCategoryWebPage.setNegativeCategories(negativeCategories);
                        parentCategoryWebPage.setFullCategory(parentCategory);

                        try {
                            cwpa.write(parentCategoryWebPage);
                            Integer count = categoryCounts.get(parentCategory);
                            if (count == null){
                                count = new Integer(0);
                            }
                            count =  count.intValue()+1;
                            categoryCounts.put(parentCategory,count);

                        } catch (Throwable t) {
                            LOG.error("Error writing avro file", t);
                        }
                    } */
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

    public String getContent(String url)
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

    public String getText(String html) throws IOException
    {
        SimplePageParser parser = new SimplePageParser();
        String text =  parser.pageText(html);
        return text;
    }

    public List<String> getFeatures(String content) throws IOException
    {
        String text =  getText(content);
        Normalizer.normalize(text);
        List<String> features = featureExtractor.extractFeatures(text);
        return features;
    }


}
