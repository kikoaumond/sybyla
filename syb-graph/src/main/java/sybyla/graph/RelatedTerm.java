package sybyla.graph;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class RelatedTerm implements Serializable, Comparable<RelatedTerm>{

	private static final long serialVersionUID = 1L;
	private int _nTerm=0;
	private int _nRelatedTerm=0;
	private int _nIntersection=0;
	private String _relatedTerm;
	
	public RelatedTerm (String relatedTerm, int nTerm, int nRelatedTerm, int nIntersection){
		
		_relatedTerm = relatedTerm;
		_nTerm = nTerm;
		_nRelatedTerm = nRelatedTerm;
		_nIntersection = nIntersection;
	}
	
	public static RelatedTerm toRelatedTerm(byte[] bytes) throws IOException, ClassNotFoundException {
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		ObjectInput in = new ObjectInputStream(bis);
		Object o = in.readObject();
		RelatedTerm rt = (RelatedTerm) o;
		return rt;
	}
	
	public byte[] toBytes() throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out =  new ObjectOutputStream(bos);
		out.writeObject(this);
		byte[] bytes  =  bos.toByteArray();
		return bytes;
	}
	
	 @Override
     public int compareTo(RelatedTerm rt) {
		 //int c = this._nIntersection - rt._nIntersection;
		 double d = ((double)this._nIntersection)/((double)this._nRelatedTerm);
		 double d2 = ((double)rt._nIntersection)/((double)rt._nRelatedTerm);
		 if (d>d2) {
			 return 1;
		 } else if (d<d2){
			 return -1;
		 } else {
			 return 0;
		 }
     }
	
	public int get_nTerm() {
		return _nTerm;
	}
	public int get_nRelatedTerm() {
		return _nRelatedTerm;
	}
	public int get_n12() {
		return _nIntersection;
	}
	public String get_relatedTerm() {
		return _relatedTerm;
	}
}
