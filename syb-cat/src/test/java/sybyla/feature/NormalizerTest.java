package sybyla.feature;

import org.junit.Test;

/**
 * Copyright SocialInSoma, 2013
 * User: kiko
 * Date: 5/24/14
 * Time: 8:55 PM
 */
public class NormalizerTest
{
   @Test
   public void testPunctuations() {
       String s = "- this is - - _ a test *-* with -*& characters beijã-flor";
       String ns = Normalizer.normalize(s);
   }
}
