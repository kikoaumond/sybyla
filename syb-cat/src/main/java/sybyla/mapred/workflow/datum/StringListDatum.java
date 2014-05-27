package sybyla.mapred.workflow.datum;

import cascading.tuple.Fields;
import cascading.tuple.Tuple;
import cascading.tuple.TupleEntry;
import com.scaleunlimited.cascading.BaseDatum;

import java.util.HashSet;
import java.util.Set;

/**
 * A StringSetDatum is  Datum containing a set of strings...
 *
 */
@SuppressWarnings("serial")
public class StringListDatum extends BaseDatum {

    public static final String STRING_SET_FN = fieldName(StringListDatum.class, "string_list");


    public static final Fields FIELDS = new Fields(STRING_SET_FN);

   // public static final Class[] TYPES = new Class[] {String.class, PageSource.class, Long.class, Long.class,
    //    Map.class, String.class, BytesWritable.class, String.class, String.class, Map.class, Long.class};

    public StringListDatum(TupleEntry te) {
        super(FIELDS);

        setTupleEntry(te);
    }

    public StringListDatum() {
        super(FIELDS);
        setStringSet(new HashSet<String>());
    }

    public StringListDatum(Set<String> stringSet) {
        super(FIELDS);

        setStringSet(stringSet);
    }

    public StringListDatum(Fields fields, Set<String> stringSet) {
        super(fields);
        
        setStringSet(stringSet);
    }

    
    public void setStringSet(Set<String> stringSet) {
        Tuple t = new Tuple();
        for (String string :stringSet) {
            t.add(string);
        }
        _tupleEntry.set(STRING_SET_FN, t);
    }

    public Set<String> getStringSet() {

        Set<String> stringSet = new HashSet<String>();
        Tuple t = (Tuple)_tupleEntry.getObject(STRING_SET_FN);

        int numEntries = t.size();
        for (int i = 0; i < numEntries; i++) {
            String s = t.getString(i);
            stringSet.add(s);
        }

        return stringSet;
    }
    
}
