package sybyla.mapred;


import cascading.tuple.Fields;
import cascading.tuple.Tuple;
import cascading.tuple.TupleEntry;
import com.scaleunlimited.cascading.BaseDatum;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CategoryWebPageDatum extends BaseDatum
{

  /*  Category Web page Avro object
  private java.lang.CharSequence url;
   private java.lang.CharSequence topCategory;
   private java.lang.CharSequence bottomCategory;
   private java.lang.CharSequence portugueseCategory;
   private java.util.List<java.lang.CharSequence> categories;
   private int level;
   private java.lang.CharSequence content;
   private java.util.List<java.lang.CharSequence> features;
   */

    public static final String URL_FN = "url";
    public static final String TOP_CATEGORY_FN = "topCategory";
    public static final String BOTTOM_CATEGORY_FN = "bottomCategory";
    public static final String FULL_CATEGORY_FN = "fullCategory";
    public static final String PORTUGUESE_CATEGORY_FN = "portugueseCategory";
    public static final String CATEGORIES_FN =  "categories";
    public static final String NEGATIVE_CATEGORIES_FN =  "negativeCategories";
    public static final String LEVEL_FN = "level";
    public static final String CONTENT_FN = "content";
    public static final String FEATURES_FN = "features";

    public static final Fields FIELDS = new Fields(URL_FN, TOP_CATEGORY_FN, BOTTOM_CATEGORY_FN, FULL_CATEGORY_FN,
                                        PORTUGUESE_CATEGORY_FN, CATEGORIES_FN, LEVEL_FN, CONTENT_FN, FEATURES_FN);


    public CategoryWebPageDatum() {

        super(FIELDS);
    }

    public CategoryWebPageDatum(Tuple tuple) {

        super(FIELDS,tuple);
    }

    public CategoryWebPageDatum(TupleEntry tupleEntry) {

        super(tupleEntry);
        validateFields(tupleEntry,FIELDS);
    }

    public CategoryWebPageDatum(TupleEntry te, Fields fields) {

        super(fields);
        setTupleEntry(te);
    }

  public CategoryWebPageDatum(String url, String topCategory, String bottomCategory, String fullCategory, String portugueseCategory,
                              List<CharSequence> categories, List<CharSequence> negativeCategories, int level, String content, List<CharSequence> features) {

    super(FIELDS);
    setURL(url);
    setTopCategory(topCategory);
    setBottomCategory(bottomCategory);
    setFullCategory(fullCategory);
    setPortugueseCategory(portugueseCategory);
    setCategories(categories);
    setNegativeCategories(negativeCategories);
    setLevel(level);
    setContent(content);
    setFeatures(features);

  }

    public void setURL(String url)
    {
        _tupleEntry.setString(URL_FN, url);
    }

    public String getURL()
    {
        return _tupleEntry.getString(URL_FN);
    }

    public void setFeatures(List<CharSequence> features) {

        Tuple result = new Tuple();

        for(CharSequence feature: features){
           result.add(feature);
        }

        _tupleEntry.set(FEATURES_FN, result);
    }

    public List<CharSequence> getFeatures() {

        List<CharSequence> features = (List<CharSequence>)_tupleEntry.getObject(FEATURES_FN);

        return features;
    }

    public void setCategories(List<CharSequence> categories) {

        Tuple result = new Tuple();

        for(CharSequence category: categories){
            result.add(category);
        }

        _tupleEntry.set(CATEGORIES_FN, result);
    }

    public List<CharSequence> getCategories() {

        List<CharSequence> categories = (List<CharSequence>) _tupleEntry.getObject(CATEGORIES_FN);
        return categories;
    }


    public void setNegativeCategories(List<CharSequence> negativeCategories) {

        Tuple result = new Tuple();

        for(CharSequence category: negativeCategories){
            result.add(category);
        }

        _tupleEntry.set(NEGATIVE_CATEGORIES_FN, result);
    }

    public List<CharSequence> getNegativeCategories() {

        List<CharSequence> negativeCategories = (List<CharSequence>) _tupleEntry.getObject(NEGATIVE_CATEGORIES_FN);
        return negativeCategories;
    }


    public void setTopCategory(String topCategory)
    {
        _tupleEntry.setString(TOP_CATEGORY_FN, topCategory);
    }

    public String getTopCategory()
    {
        return _tupleEntry.getString(TOP_CATEGORY_FN);
    }

    public void setBottomCategory(String bottomCategory)
    {
        _tupleEntry.setString(BOTTOM_CATEGORY_FN, bottomCategory);
    }

    public String getBottomCategory()
    {
        return _tupleEntry.getString(BOTTOM_CATEGORY_FN);
    }

    public void setFullCategory(String fullCategory)
    {
        _tupleEntry.setString(FULL_CATEGORY_FN, fullCategory);
    }

    public String getFullCategory()
    {
        return _tupleEntry.getString(FULL_CATEGORY_FN);
    }

    public void setPortugueseCategory(String portugueseCategory)
    {
        _tupleEntry.setString(PORTUGUESE_CATEGORY_FN, portugueseCategory);
    }

    public String getPortugueseCategory()
    {
        return _tupleEntry.getString(PORTUGUESE_CATEGORY_FN);
    }

    public void setLevel(int level){
        _tupleEntry.setInteger(LEVEL_FN,level);
    }

    public int getLevel(){
        return _tupleEntry.getInteger(LEVEL_FN);
    }

    public void setContent(String content)
    {
        _tupleEntry.setString(CONTENT_FN, content);
    }

    public String getContent()
    {
        return _tupleEntry.getString(CONTENT_FN);
    }



}
