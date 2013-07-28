package sybyla.graph;

import java.io.UnsupportedEncodingException;

import org.apache.log4j.Logger;



public class CorrelatedEntity implements Comparable<CorrelatedEntity>{
    
    private static final Logger LOGGER =  Logger.getLogger(CorrelatedEntity.class);
    private static final String UTF8="UTF-8";

    //private String _entity;
    private byte[] _entityBytes;
    private double _correlation;
    
    public CorrelatedEntity(String entity, double correlation) {
       // _entity = entity;
        
        try {
			_entityBytes = entity.getBytes(UTF8);
		} catch (UnsupportedEncodingException e) {
			
		}
		
        _correlation = correlation;
    }
    
	public String get_entity(){
        try {
			return new String(_entityBytes, UTF8);
		} catch (UnsupportedEncodingException e) {
			LOGGER.error("Error encoding CorrelatedEntity string",e);
			return null;
		}
    }

	public double get_correlation() {
        return _correlation;
    }

    @Override
    public int compareTo(CorrelatedEntity ce) {
       if (this._correlation > ce._correlation) {
            return 1;
       }
       if (this._correlation < ce._correlation) {
           return -1;
       }
       
       return 0;
    }
    
    private boolean compareBytes(byte[] b1, byte[] b2){
    	if (b1.length != b2.length) {
    		return false;
    	}
    	
    	for(int i=0;i < b1.length; i++){
    		if (b1[i]!=b2[i]){
    			return false;
    		}
    	}
    	return true;
    }
    
    public boolean equals(Object o) {
        if (!(o instanceof CorrelatedEntity)) {
            return false;
        }
        
        CorrelatedEntity ce = (CorrelatedEntity) o;
        //return (this._correlation == ce._correlation && this._entity.equals(ce._entity));
        return (this._correlation == ce._correlation && compareBytes(this._entityBytes, ce._entityBytes));
        
    }
}

