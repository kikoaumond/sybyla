package sybyla.jaxb;

import java.util.List;

import sybyla.jaxb.TreeResult;

public class Utils {
    
    public static boolean equals(TreeResult tr1, TreeResult tr2){
        
        if (tr1.getId() != tr2.getId()) return false;
        
        if (!tr1.getName().equals(tr2.getName())) return false;
        
        if (tr1.getData().getRelevance()!=tr2.getData().getRelevance()) return false;
        
        List<TreeResult> ch1 = tr1.getChildren();
        List<TreeResult> ch2 = tr2.getChildren();
        if (!((ch1==null && ch2==null) || (ch1!=null && ch2!=null))) return false;
        
        if (ch1!=null && ch2!=null){
            
            if (ch1.size() != ch2.size()) return false;
            
            for(int i=0; i<ch1.size();i++){
                 TreeResult ctr1 = ch1.get(i);
                 TreeResult ctr2 = ch2.get(i); 
                 if (!equals(ctr1,ctr2)) return false;
                
            }
        }
        
        return true;
    }
}
