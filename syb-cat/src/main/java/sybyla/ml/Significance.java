package sybyla.ml;

import java.util.Arrays;

/**
 *  This class computes a null-hypothesis test for weaning out terms during category learning
 * 
 * 
 *  @author kiko
 *
 */
public class Significance {
    
    private static final double MAX_LOG10_VALUE = Math.log10(Double.MAX_VALUE);
    private static final double MIN_LOG10_VALUE = Math.log10(Double.MIN_VALUE);

    
    public static double pValue( int nPosDocs, int nPosOccs, 
                                 int nNegDocs, int nNegOccs) {
        
        int nOccs = nPosOccs + nNegOccs;
        
        int maxNp = (nPosDocs <= nOccs)?nPosDocs:nOccs;
        
        int minNp = (nOccs - nNegDocs > 0)?nOccs-nNegDocs:0;
        
        
        int maxNn = (nNegDocs <= nOccs)?nNegDocs:nOccs;
        
        int minNn = (nOccs - nPosDocs > 0)?nOccs-nPosDocs:0;
        
        int np = minNp;
        int nn = minNn;
        
        double t = 0;
        
        for (np=minNp; np<=maxNp;np++) {
            for (nn=minNn; nn<=maxNn;nn++) {
                if (nn+np==nOccs) {
                    double f = log10f( nPosDocs, nPosOccs, nNegDocs, nNegOccs, np, nn);
                    //test for overflow and underflow first
                    if (f > MAX_LOG10_VALUE) {
                        return 0;
                    } 
                    else if (f >= MIN_LOG10_VALUE) {
                        t = t+ Math.pow(10,f);
                    }
                }
            }
        }
        
        return 1/t;
    }
    

    /**
     * returns the pValue with a sign; positive if the number of positive occurrences
     * is larger than the negative occurrences, negative otherwise
     * @param nPosDocs
     * @param nPosOccs
     * @param nNegDocs
     * @param nNegOccs
     * @return
     */
    public static double pValueSigned( int nPosDocs, int nPosOccs, 
                                       int nNegDocs, int nNegOccs) {
        double m;
        if (nPosOccs>nNegDocs) {
            m=1.;
        } else {
            m=-1.;        
        }
        return m*pValue( nPosDocs,  nPosOccs, nNegDocs,   nNegOccs);
    }
    
    /**
     * computes [(nPosDocs - nPosOccs)! nPosOccs! (nNegDocs - nNegOccs)! nNegOccs!] /
     *                  [(nPosDocs - np)! np! (nNegDocs - nn)! nn!]
     *                  
     * @param nPosDocs
     * @param nPosOccs
     * @param nNegDocs
     * @param nNegOccs
     * @param np
     * @param nn
     * @return
     * @throws Exception 
     */
    protected static double log10f(int nPosDocs, int nPosOccs, 
                    int nNegDocs, int nNegOccs,
                    int np, int nn) {
        
        if (nPosOccs > nPosDocs) {
            throw new RuntimeException("Number of positive occurrences ("+ nPosOccs+") " +
                                "must not be higher than number of positive documents ("+nPosDocs+")");        
        }
        
        if (nNegOccs > nNegDocs) {
            throw new RuntimeException("Number of negative occurrences ("+ nNegOccs+") " +
                                "must not be higher than number of negative documents ("+nNegDocs+")");        
        }
        
        if (np > nPosDocs) {
            throw new RuntimeException("np ("+ np+") " +
                                "must not be higher than number of positive documents ("+nPosDocs+")");        
        }
        
        if (nn > nNegDocs) {
            throw new RuntimeException("nn ("+ nn+") " +
                                "must not be higher than number of negative documents ("+nNegDocs+")");        
        }
        
        int[] numerator = {(nPosDocs - nPosOccs), nPosOccs, (nNegDocs - nNegOccs),nNegOccs};
        int[] denominator = {(nPosDocs - np),np,(nNegDocs - nn), nn};
        
        //we sort both the numerator and the denominator factors so we can 
        // avoid the risk of overflow by matching the largest numerator factor with
        //the largest denominator factor
        Arrays.sort(numerator);
        Arrays.sort(denominator);
        
        double f = 0;
        
        for (int i=0; i< numerator.length; i++) {
            f = f + log10FactorialRate(numerator[i],denominator[i]);
        }
        
        return f;  
    }
    
    
    /**
     * computes the ratio a!/b!  in an efficient way so as to avoid numerical overflow
     * @param a
     * @param b
     * @return a!/b!
     */
    protected static double factorialRate(int a, int b) {
        
        double num = (double)a;
        double den = (double)b;
        
        if (a == 0) {
            num = 1;
        }
        
        if (b == 0) {
            den  = 1;
        }
        double p = 1.0d;
        
        
        while((num > 1) ||  (den > 1)){
            double x = num/den;
            p = p*x;
            
            if (num - 1 > 1) {
                num = num - 1;
            } else {
                num = 1; 
            }
            if (den - 1 > 1) {
                den = den - 1;  
            } else {
                den = 1;
            }
        }
        
        return p;
        
    }


    /**
     * computes the ratio log10(a!/b!)  in an efficient way so as to avoid numerical overflow
     * @param a
     * @param b
     * @return log10(a!/b!)
     */
    protected static double log10FactorialRate(int a, int b) {
    
        double num = (double)a;
        double den = (double)b;
        
        if (a == 0) {
            num = 1;
        }
        
        if (b == 0) {
            den = 1;
        }
        
        double p = 0.0d;
    
        while((num > 1) ||  (den > 1)){
            double x = Math.log10(num) - Math.log10(den);
            p = p + x;
         
            num = num -1;
            if (num <= 1) {
                num = 1;
            }
        
            den = den -1;
            if (den <= 1) {
                den = 1;
            }
        }
        return p;
    }

    public static double pValue(long featureCategoryCount, long featureTotalCount, long categorySize, long totalSize){

        double categoryRatio = (double)(categorySize)/(double)(totalSize);

        if (categoryRatio == 0 || categoryRatio == 1){
            return 0;
        }

        double logN = featureCategoryCount*Math.log10(categoryRatio);

        double logD = featureCategoryCount*Math.log10(categoryRatio) + (featureTotalCount - featureCategoryCount)*Math.log10(1-categoryRatio);

        return Math.pow(logN - logD,10);
    }
}
