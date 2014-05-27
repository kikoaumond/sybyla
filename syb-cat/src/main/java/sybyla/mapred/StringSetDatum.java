package sybyla.mapred;

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
public class StringSetDatum extends BaseDatum {

    public static final String STRING_SET_FN = fieldName(StringSetDatum.class, "stringSet");
    

    public static final Fields FIELDS = new Fields(STRING_SET_FN);

    public StringSetDatum(TupleEntry te) {
        super(FIELDS);
        
        setTupleEntry(te);
    }
    
    public StringSetDatum() {
        super(FIELDS);
        setStringSet(new HashSet<String>());
    }
    
    public StringSetDatum(Set<String> stringSet) {
        super(FIELDS);
        
        setStringSet(stringSet);
    }
    
    public StringSetDatum(Fields fields, Set<String> stringSet) {
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
        Tuple t = (Tuple)_tupleEntry.get(STRING_SET_FN);

        int numEntries = t.size();
        for (int i = 0; i < numEntries; i++) {
            String s = t.getString(i);
            stringSet.add(s);
        }

        return stringSet;
    }
    
}
