/**
 * Autogenerated by Avro
 * 
 * DO NOT EDIT DIRECTLY
 */
package sybyla.generated.avro;  
@SuppressWarnings("all")
/** Avro representation of a web page to be used in category training */
@org.apache.avro.specific.AvroGenerated
public class CategoryWebPage extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"CategoryWebPage\",\"namespace\":\"sybyla.generated.avro\",\"doc\":\"Avro representation of a web page to be used in category training\",\"fields\":[{\"name\":\"url\",\"type\":\"string\"},{\"name\":\"topCategory\",\"type\":\"string\"},{\"name\":\"bottomCategory\",\"type\":\"string\"},{\"name\":\"fullCategory\",\"type\":\"string\"},{\"name\":\"portugueseCategory\",\"type\":\"string\"},{\"name\":\"categories\",\"type\":{\"type\":\"array\",\"items\":\"string\"}},{\"name\":\"negativeCategories\",\"type\":{\"type\":\"array\",\"items\":\"string\"}},{\"name\":\"level\",\"type\":\"int\"},{\"name\":\"content\",\"type\":\"string\"},{\"name\":\"features\",\"type\":{\"type\":\"array\",\"items\":\"string\"}}]}");
   private java.lang.CharSequence url;
   private java.lang.CharSequence topCategory;
   private java.lang.CharSequence bottomCategory;
   private java.lang.CharSequence fullCategory;
   private java.lang.CharSequence portugueseCategory;
   private java.util.List<java.lang.CharSequence> categories;
   private java.util.List<java.lang.CharSequence> negativeCategories;
   private int level;
   private java.lang.CharSequence content;
   private java.util.List<java.lang.CharSequence> features;

  /**
   * Default constructor.
   */
  public CategoryWebPage() {}

  /**
   * All-args constructor.
   */
  public CategoryWebPage(java.lang.CharSequence url, java.lang.CharSequence topCategory, java.lang.CharSequence bottomCategory, java.lang.CharSequence fullCategory, java.lang.CharSequence portugueseCategory, java.util.List<java.lang.CharSequence> categories, java.util.List<java.lang.CharSequence> negativeCategories, java.lang.Integer level, java.lang.CharSequence content, java.util.List<java.lang.CharSequence> features) {
    this.url = url;
    this.topCategory = topCategory;
    this.bottomCategory = bottomCategory;
    this.fullCategory = fullCategory;
    this.portugueseCategory = portugueseCategory;
    this.categories = categories;
    this.negativeCategories = negativeCategories;
    this.level = level;
    this.content = content;
    this.features = features;
  }

  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call. 
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return url;
    case 1: return topCategory;
    case 2: return bottomCategory;
    case 3: return fullCategory;
    case 4: return portugueseCategory;
    case 5: return categories;
    case 6: return negativeCategories;
    case 7: return level;
    case 8: return content;
    case 9: return features;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }
  // Used by DatumReader.  Applications should not call. 
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: url = (java.lang.CharSequence)value$; break;
    case 1: topCategory = (java.lang.CharSequence)value$; break;
    case 2: bottomCategory = (java.lang.CharSequence)value$; break;
    case 3: fullCategory = (java.lang.CharSequence)value$; break;
    case 4: portugueseCategory = (java.lang.CharSequence)value$; break;
    case 5: categories = (java.util.List<java.lang.CharSequence>)value$; break;
    case 6: negativeCategories = (java.util.List<java.lang.CharSequence>)value$; break;
    case 7: level = (java.lang.Integer)value$; break;
    case 8: content = (java.lang.CharSequence)value$; break;
    case 9: features = (java.util.List<java.lang.CharSequence>)value$; break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  /**
   * Gets the value of the 'url' field.
   */
  public java.lang.CharSequence getUrl() {
    return url;
  }

  /**
   * Sets the value of the 'url' field.
   * @param value the value to set.
   */
  public void setUrl(java.lang.CharSequence value) {
    this.url = value;
  }

  /**
   * Gets the value of the 'topCategory' field.
   */
  public java.lang.CharSequence getTopCategory() {
    return topCategory;
  }

  /**
   * Sets the value of the 'topCategory' field.
   * @param value the value to set.
   */
  public void setTopCategory(java.lang.CharSequence value) {
    this.topCategory = value;
  }

  /**
   * Gets the value of the 'bottomCategory' field.
   */
  public java.lang.CharSequence getBottomCategory() {
    return bottomCategory;
  }

  /**
   * Sets the value of the 'bottomCategory' field.
   * @param value the value to set.
   */
  public void setBottomCategory(java.lang.CharSequence value) {
    this.bottomCategory = value;
  }

  /**
   * Gets the value of the 'fullCategory' field.
   */
  public java.lang.CharSequence getFullCategory() {
    return fullCategory;
  }

  /**
   * Sets the value of the 'fullCategory' field.
   * @param value the value to set.
   */
  public void setFullCategory(java.lang.CharSequence value) {
    this.fullCategory = value;
  }

  /**
   * Gets the value of the 'portugueseCategory' field.
   */
  public java.lang.CharSequence getPortugueseCategory() {
    return portugueseCategory;
  }

  /**
   * Sets the value of the 'portugueseCategory' field.
   * @param value the value to set.
   */
  public void setPortugueseCategory(java.lang.CharSequence value) {
    this.portugueseCategory = value;
  }

  /**
   * Gets the value of the 'categories' field.
   */
  public java.util.List<java.lang.CharSequence> getCategories() {
    return categories;
  }

  /**
   * Sets the value of the 'categories' field.
   * @param value the value to set.
   */
  public void setCategories(java.util.List<java.lang.CharSequence> value) {
    this.categories = value;
  }

  /**
   * Gets the value of the 'negativeCategories' field.
   */
  public java.util.List<java.lang.CharSequence> getNegativeCategories() {
    return negativeCategories;
  }

  /**
   * Sets the value of the 'negativeCategories' field.
   * @param value the value to set.
   */
  public void setNegativeCategories(java.util.List<java.lang.CharSequence> value) {
    this.negativeCategories = value;
  }

  /**
   * Gets the value of the 'level' field.
   */
  public java.lang.Integer getLevel() {
    return level;
  }

  /**
   * Sets the value of the 'level' field.
   * @param value the value to set.
   */
  public void setLevel(java.lang.Integer value) {
    this.level = value;
  }

  /**
   * Gets the value of the 'content' field.
   */
  public java.lang.CharSequence getContent() {
    return content;
  }

  /**
   * Sets the value of the 'content' field.
   * @param value the value to set.
   */
  public void setContent(java.lang.CharSequence value) {
    this.content = value;
  }

  /**
   * Gets the value of the 'features' field.
   */
  public java.util.List<java.lang.CharSequence> getFeatures() {
    return features;
  }

  /**
   * Sets the value of the 'features' field.
   * @param value the value to set.
   */
  public void setFeatures(java.util.List<java.lang.CharSequence> value) {
    this.features = value;
  }

  /** Creates a new CategoryWebPage RecordBuilder */
  public static sybyla.generated.avro.CategoryWebPage.Builder newBuilder() {
    return new sybyla.generated.avro.CategoryWebPage.Builder();
  }
  
  /** Creates a new CategoryWebPage RecordBuilder by copying an existing Builder */
  public static sybyla.generated.avro.CategoryWebPage.Builder newBuilder(sybyla.generated.avro.CategoryWebPage.Builder other) {
    return new sybyla.generated.avro.CategoryWebPage.Builder(other);
  }
  
  /** Creates a new CategoryWebPage RecordBuilder by copying an existing CategoryWebPage instance */
  public static sybyla.generated.avro.CategoryWebPage.Builder newBuilder(sybyla.generated.avro.CategoryWebPage other) {
    return new sybyla.generated.avro.CategoryWebPage.Builder(other);
  }
  
  /**
   * RecordBuilder for CategoryWebPage instances.
   */
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<CategoryWebPage>
    implements org.apache.avro.data.RecordBuilder<CategoryWebPage> {

    private java.lang.CharSequence url;
    private java.lang.CharSequence topCategory;
    private java.lang.CharSequence bottomCategory;
    private java.lang.CharSequence fullCategory;
    private java.lang.CharSequence portugueseCategory;
    private java.util.List<java.lang.CharSequence> categories;
    private java.util.List<java.lang.CharSequence> negativeCategories;
    private int level;
    private java.lang.CharSequence content;
    private java.util.List<java.lang.CharSequence> features;

    /** Creates a new Builder */
    private Builder() {
      super(sybyla.generated.avro.CategoryWebPage.SCHEMA$);
    }
    
    /** Creates a Builder by copying an existing Builder */
    private Builder(sybyla.generated.avro.CategoryWebPage.Builder other) {
      super(other);
    }
    
    /** Creates a Builder by copying an existing CategoryWebPage instance */
    private Builder(sybyla.generated.avro.CategoryWebPage other) {
            super(sybyla.generated.avro.CategoryWebPage.SCHEMA$);
      if (isValidValue(fields()[0], other.url)) {
        this.url = data().deepCopy(fields()[0].schema(), other.url);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.topCategory)) {
        this.topCategory = data().deepCopy(fields()[1].schema(), other.topCategory);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.bottomCategory)) {
        this.bottomCategory = data().deepCopy(fields()[2].schema(), other.bottomCategory);
        fieldSetFlags()[2] = true;
      }
      if (isValidValue(fields()[3], other.fullCategory)) {
        this.fullCategory = data().deepCopy(fields()[3].schema(), other.fullCategory);
        fieldSetFlags()[3] = true;
      }
      if (isValidValue(fields()[4], other.portugueseCategory)) {
        this.portugueseCategory = data().deepCopy(fields()[4].schema(), other.portugueseCategory);
        fieldSetFlags()[4] = true;
      }
      if (isValidValue(fields()[5], other.categories)) {
        this.categories = data().deepCopy(fields()[5].schema(), other.categories);
        fieldSetFlags()[5] = true;
      }
      if (isValidValue(fields()[6], other.negativeCategories)) {
        this.negativeCategories = data().deepCopy(fields()[6].schema(), other.negativeCategories);
        fieldSetFlags()[6] = true;
      }
      if (isValidValue(fields()[7], other.level)) {
        this.level = data().deepCopy(fields()[7].schema(), other.level);
        fieldSetFlags()[7] = true;
      }
      if (isValidValue(fields()[8], other.content)) {
        this.content = data().deepCopy(fields()[8].schema(), other.content);
        fieldSetFlags()[8] = true;
      }
      if (isValidValue(fields()[9], other.features)) {
        this.features = data().deepCopy(fields()[9].schema(), other.features);
        fieldSetFlags()[9] = true;
      }
    }

    /** Gets the value of the 'url' field */
    public java.lang.CharSequence getUrl() {
      return url;
    }
    
    /** Sets the value of the 'url' field */
    public sybyla.generated.avro.CategoryWebPage.Builder setUrl(java.lang.CharSequence value) {
      validate(fields()[0], value);
      this.url = value;
      fieldSetFlags()[0] = true;
      return this; 
    }
    
    /** Checks whether the 'url' field has been set */
    public boolean hasUrl() {
      return fieldSetFlags()[0];
    }
    
    /** Clears the value of the 'url' field */
    public sybyla.generated.avro.CategoryWebPage.Builder clearUrl() {
      url = null;
      fieldSetFlags()[0] = false;
      return this;
    }

    /** Gets the value of the 'topCategory' field */
    public java.lang.CharSequence getTopCategory() {
      return topCategory;
    }
    
    /** Sets the value of the 'topCategory' field */
    public sybyla.generated.avro.CategoryWebPage.Builder setTopCategory(java.lang.CharSequence value) {
      validate(fields()[1], value);
      this.topCategory = value;
      fieldSetFlags()[1] = true;
      return this; 
    }
    
    /** Checks whether the 'topCategory' field has been set */
    public boolean hasTopCategory() {
      return fieldSetFlags()[1];
    }
    
    /** Clears the value of the 'topCategory' field */
    public sybyla.generated.avro.CategoryWebPage.Builder clearTopCategory() {
      topCategory = null;
      fieldSetFlags()[1] = false;
      return this;
    }

    /** Gets the value of the 'bottomCategory' field */
    public java.lang.CharSequence getBottomCategory() {
      return bottomCategory;
    }
    
    /** Sets the value of the 'bottomCategory' field */
    public sybyla.generated.avro.CategoryWebPage.Builder setBottomCategory(java.lang.CharSequence value) {
      validate(fields()[2], value);
      this.bottomCategory = value;
      fieldSetFlags()[2] = true;
      return this; 
    }
    
    /** Checks whether the 'bottomCategory' field has been set */
    public boolean hasBottomCategory() {
      return fieldSetFlags()[2];
    }
    
    /** Clears the value of the 'bottomCategory' field */
    public sybyla.generated.avro.CategoryWebPage.Builder clearBottomCategory() {
      bottomCategory = null;
      fieldSetFlags()[2] = false;
      return this;
    }

    /** Gets the value of the 'fullCategory' field */
    public java.lang.CharSequence getFullCategory() {
      return fullCategory;
    }
    
    /** Sets the value of the 'fullCategory' field */
    public sybyla.generated.avro.CategoryWebPage.Builder setFullCategory(java.lang.CharSequence value) {
      validate(fields()[3], value);
      this.fullCategory = value;
      fieldSetFlags()[3] = true;
      return this; 
    }
    
    /** Checks whether the 'fullCategory' field has been set */
    public boolean hasFullCategory() {
      return fieldSetFlags()[3];
    }
    
    /** Clears the value of the 'fullCategory' field */
    public sybyla.generated.avro.CategoryWebPage.Builder clearFullCategory() {
      fullCategory = null;
      fieldSetFlags()[3] = false;
      return this;
    }

    /** Gets the value of the 'portugueseCategory' field */
    public java.lang.CharSequence getPortugueseCategory() {
      return portugueseCategory;
    }
    
    /** Sets the value of the 'portugueseCategory' field */
    public sybyla.generated.avro.CategoryWebPage.Builder setPortugueseCategory(java.lang.CharSequence value) {
      validate(fields()[4], value);
      this.portugueseCategory = value;
      fieldSetFlags()[4] = true;
      return this; 
    }
    
    /** Checks whether the 'portugueseCategory' field has been set */
    public boolean hasPortugueseCategory() {
      return fieldSetFlags()[4];
    }
    
    /** Clears the value of the 'portugueseCategory' field */
    public sybyla.generated.avro.CategoryWebPage.Builder clearPortugueseCategory() {
      portugueseCategory = null;
      fieldSetFlags()[4] = false;
      return this;
    }

    /** Gets the value of the 'categories' field */
    public java.util.List<java.lang.CharSequence> getCategories() {
      return categories;
    }
    
    /** Sets the value of the 'categories' field */
    public sybyla.generated.avro.CategoryWebPage.Builder setCategories(java.util.List<java.lang.CharSequence> value) {
      validate(fields()[5], value);
      this.categories = value;
      fieldSetFlags()[5] = true;
      return this; 
    }
    
    /** Checks whether the 'categories' field has been set */
    public boolean hasCategories() {
      return fieldSetFlags()[5];
    }
    
    /** Clears the value of the 'categories' field */
    public sybyla.generated.avro.CategoryWebPage.Builder clearCategories() {
      categories = null;
      fieldSetFlags()[5] = false;
      return this;
    }

    /** Gets the value of the 'negativeCategories' field */
    public java.util.List<java.lang.CharSequence> getNegativeCategories() {
      return negativeCategories;
    }
    
    /** Sets the value of the 'negativeCategories' field */
    public sybyla.generated.avro.CategoryWebPage.Builder setNegativeCategories(java.util.List<java.lang.CharSequence> value) {
      validate(fields()[6], value);
      this.negativeCategories = value;
      fieldSetFlags()[6] = true;
      return this; 
    }
    
    /** Checks whether the 'negativeCategories' field has been set */
    public boolean hasNegativeCategories() {
      return fieldSetFlags()[6];
    }
    
    /** Clears the value of the 'negativeCategories' field */
    public sybyla.generated.avro.CategoryWebPage.Builder clearNegativeCategories() {
      negativeCategories = null;
      fieldSetFlags()[6] = false;
      return this;
    }

    /** Gets the value of the 'level' field */
    public java.lang.Integer getLevel() {
      return level;
    }
    
    /** Sets the value of the 'level' field */
    public sybyla.generated.avro.CategoryWebPage.Builder setLevel(int value) {
      validate(fields()[7], value);
      this.level = value;
      fieldSetFlags()[7] = true;
      return this; 
    }
    
    /** Checks whether the 'level' field has been set */
    public boolean hasLevel() {
      return fieldSetFlags()[7];
    }
    
    /** Clears the value of the 'level' field */
    public sybyla.generated.avro.CategoryWebPage.Builder clearLevel() {
      fieldSetFlags()[7] = false;
      return this;
    }

    /** Gets the value of the 'content' field */
    public java.lang.CharSequence getContent() {
      return content;
    }
    
    /** Sets the value of the 'content' field */
    public sybyla.generated.avro.CategoryWebPage.Builder setContent(java.lang.CharSequence value) {
      validate(fields()[8], value);
      this.content = value;
      fieldSetFlags()[8] = true;
      return this; 
    }
    
    /** Checks whether the 'content' field has been set */
    public boolean hasContent() {
      return fieldSetFlags()[8];
    }
    
    /** Clears the value of the 'content' field */
    public sybyla.generated.avro.CategoryWebPage.Builder clearContent() {
      content = null;
      fieldSetFlags()[8] = false;
      return this;
    }

    /** Gets the value of the 'features' field */
    public java.util.List<java.lang.CharSequence> getFeatures() {
      return features;
    }
    
    /** Sets the value of the 'features' field */
    public sybyla.generated.avro.CategoryWebPage.Builder setFeatures(java.util.List<java.lang.CharSequence> value) {
      validate(fields()[9], value);
      this.features = value;
      fieldSetFlags()[9] = true;
      return this; 
    }
    
    /** Checks whether the 'features' field has been set */
    public boolean hasFeatures() {
      return fieldSetFlags()[9];
    }
    
    /** Clears the value of the 'features' field */
    public sybyla.generated.avro.CategoryWebPage.Builder clearFeatures() {
      features = null;
      fieldSetFlags()[9] = false;
      return this;
    }

    @Override
    public CategoryWebPage build() {
      try {
        CategoryWebPage record = new CategoryWebPage();
        record.url = fieldSetFlags()[0] ? this.url : (java.lang.CharSequence) defaultValue(fields()[0]);
        record.topCategory = fieldSetFlags()[1] ? this.topCategory : (java.lang.CharSequence) defaultValue(fields()[1]);
        record.bottomCategory = fieldSetFlags()[2] ? this.bottomCategory : (java.lang.CharSequence) defaultValue(fields()[2]);
        record.fullCategory = fieldSetFlags()[3] ? this.fullCategory : (java.lang.CharSequence) defaultValue(fields()[3]);
        record.portugueseCategory = fieldSetFlags()[4] ? this.portugueseCategory : (java.lang.CharSequence) defaultValue(fields()[4]);
        record.categories = fieldSetFlags()[5] ? this.categories : (java.util.List<java.lang.CharSequence>) defaultValue(fields()[5]);
        record.negativeCategories = fieldSetFlags()[6] ? this.negativeCategories : (java.util.List<java.lang.CharSequence>) defaultValue(fields()[6]);
        record.level = fieldSetFlags()[7] ? this.level : (java.lang.Integer) defaultValue(fields()[7]);
        record.content = fieldSetFlags()[8] ? this.content : (java.lang.CharSequence) defaultValue(fields()[8]);
        record.features = fieldSetFlags()[9] ? this.features : (java.util.List<java.lang.CharSequence>) defaultValue(fields()[9]);
        return record;
      } catch (Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }
}
