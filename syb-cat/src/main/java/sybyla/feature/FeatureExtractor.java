package sybyla.feature; /**
 * Created with IntelliJ IDEA.
 * User: kiko
 * Date: 9/10/13
 * Time: 11:31 AM
 * To change this template use File | Settings | File Templates.
 */

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FeatureExtractor {

    private static  int neighborhood=5;

    public FeatureExtractor(){}

    public FeatureExtractor(int neighborhood){
        this.neighborhood =neighborhood;
    }

    public  List<String> extractFeatures(String sentence){

        String n =  Normalizer.normalize(sentence);

        List<String> features = new ArrayList<String>();

        if (sentence ==  null){
          return features;
        }

        String[] tokens = n.split("\\s");
        StringBuilder continuous = new StringBuilder();
        StringBuilder neighbors = new StringBuilder();
        for( int i=0; i< tokens.length; i++){

            continuous.delete(0, continuous.length());
            if (tokens[i].trim().equals("")){
                continue;
            }
            continuous.append(tokens[i]);
            features.add(continuous.toString());

            neighbors.delete(0, neighbors.length());
            neighbors.append(tokens[i]);
            int l = neighbors.length();

            for(int j = 1; (i+j < tokens.length && j <= neighborhood); j++){

                continuous.append(" ").append(tokens[i+j]);
                String c = continuous.toString();
                if (hasOneAlphaCharacter(c)){
                    features.add(c);
                }

                if (j > 1){

                    neighbors.append(" {");

                    for(int k=1;k<j;k++){
                        neighbors.append("_");
                    }

                    neighbors.append("} ").append(tokens[i+j]);
                    String ngb =  neighbors.toString();
                    if (hasOneAlphaCharacter(ngb)){
                        features.add(ngb);
                    }
                    neighbors.delete(l, neighbors.length());
                }
            }
        }
        return features;
    }

    public boolean hasOneAlphaCharacter(String feature){

        Matcher m = Pattern.compile("[a-zA-Z]").matcher(feature);

        if (m.find()){
            return true;
        }

        return false;
    }

    public  Set<String> extractUniqueFeatures(String sentence){

        List<String> features = extractFeatures(sentence);
        Set<String> uniqueFeatures =  new HashSet<String>();
        uniqueFeatures.addAll(features);
        return uniqueFeatures;

    }
}