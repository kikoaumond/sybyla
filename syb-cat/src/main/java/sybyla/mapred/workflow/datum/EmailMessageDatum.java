package sybyla.mapred.workflow.datum;


import cascading.tuple.Fields;
import cascading.tuple.TupleEntry;
import com.scaleunlimited.cascading.BaseDatum;

public class EmailMessageDatum extends BaseDatum {

  public static final String SUBJECT_FN = "subject";
  public static final String BODY_FN = "body";
  public static final String LABEL_FN = "label";

  public static final Fields FIELDS = new Fields(SUBJECT_FN, BODY_FN, LABEL_FN);

  public static final Class<?>[] TYPES = new Class[] {String.class, String.class, Integer.class};

  public EmailMessageDatum() {

    super(FIELDS);
  }

  public EmailMessageDatum(Fields fields) {

    super(fields);
  }

  public EmailMessageDatum(TupleEntry te) {

    super(FIELDS);
    setTupleEntry(te);
  }

  public EmailMessageDatum(TupleEntry te, Fields fields) {

    super(fields);
    setTupleEntry(te);
  }

  public EmailMessageDatum(String subject, String  body) {

    super(FIELDS);
    setSubject(subject);
    setBody(body);
  }

  public void setSubject(String subject) {
    _tupleEntry.setString(SUBJECT_FN, subject);
  }

  public String getSubject() {
    return _tupleEntry.getString(SUBJECT_FN);
  }

  public void setBody(String body) {
    _tupleEntry.setString(BODY_FN, body);
  }

  public String getBody() {
    return _tupleEntry.getString(BODY_FN);
  }

  public Integer getLabel(){
    return _tupleEntry.getInteger(LABEL_FN);
  }

  public void setLabel(Integer label){
    _tupleEntry.setInteger(LABEL_FN, label);
  }
}
