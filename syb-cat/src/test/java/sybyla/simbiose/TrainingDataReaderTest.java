package sybyla.simbiose;

import org.junit.Test;

import java.io.IOException;

/**
 * Copyright SocialInSoma, 2013
 * User: kiko
 * Date: 4/15/14
 * Time: 6:13 PM
 */
public class TrainingDataReaderTest
{

    @Test
    public void test() throws IOException
    {
        TrainingDataReader reader = new TrainingDataReader();
        reader.read();
    }
}
