package sybyla.ml;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Copyright SocialInSoma, 2013
 * User: kiko
 * Date: 5/29/14
 * Time: 5:52 PM
 */
public class SignificanceTest
{
    @Test
    public void testLogPValue(){

        long featureCategoryCount = 9;
        long featureTotalCount = 9;
        long categorySize = 344138;
        long totalSize = 1186072;

        double pValue = Significance.pValue(featureCategoryCount, featureTotalCount,categorySize,totalSize);
        assertTrue(1.46E-05 - pValue < 0.01E-05);

        featureCategoryCount = 2;
        featureTotalCount = 2;
        categorySize = 344138;
        totalSize = 1186072;

        pValue = Significance.pValue(featureCategoryCount, featureTotalCount,categorySize,totalSize);
        assertTrue(-1 - pValue < 0.1d);

        featureCategoryCount = 2;
        featureTotalCount = 0;
        categorySize = 344138;
        totalSize = 1186072;

        pValue = Significance.pValue(featureCategoryCount, featureTotalCount,categorySize,totalSize);
        assertTrue(-0.7 - pValue < 0.1d);

        featureCategoryCount = 9;
        featureTotalCount = 18;
        categorySize = 344138;
        totalSize = 1186072;

        pValue = Significance.pValue(featureCategoryCount, featureTotalCount,categorySize,totalSize);
        assertTrue(-6.1 - pValue < 0.1d);

        featureCategoryCount = 9;
        featureTotalCount = 18;
        categorySize = 344138;
        totalSize = 344138;

        pValue = Significance.pValue(featureCategoryCount, featureTotalCount,categorySize,totalSize);
        assertTrue(0 == pValue);
    }
}
