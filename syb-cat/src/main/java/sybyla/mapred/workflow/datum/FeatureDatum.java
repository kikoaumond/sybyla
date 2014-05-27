package sybyla.mapred.workflow.datum;


import cascading.tuple.Fields;
import cascading.tuple.TupleEntry;
import com.scaleunlimited.cascading.BaseDatum;

public class FeatureDatum extends BaseDatum {

  public static final String LABEL_FN = "label";
  public static final String FEATURE_FN = "feature";


  public static final Fields FIELDS = new Fields(FEATURE_FN, LABEL_FN);

  public static final Class<?>[] TYPES = new Class[] {String.class, Integer.class};

  public FeatureDatum() {

    super(FIELDS);
  }

  public FeatureDatum(Fields fields) {

    super(fields);
  }

  public FeatureDatum(TupleEntry te) {

    super(FIELDS);
    setTupleEntry(te);
  }

  public FeatureDatum(TupleEntry te, Fields fields) {

    super(fields);
    setTupleEntry(te);
  }

  public FeatureDatum( String feature, Integer label) {

    super(FIELDS);
    setLabel(label);
    setFeature(feature);
  }

  public void setLabel(Integer label) {
    _tupleEntry.setInteger(LABEL_FN, label);
  }

  public Integer getLabel() {
    return _tupleEntry.getInteger(LABEL_FN);
  }

  public void setFeature(String feature) {
    _tupleEntry.setString(FEATURE_FN, feature);
  }

  public String getFeature() {
    return _tupleEntry.getString(FEATURE_FN);
  }

}
