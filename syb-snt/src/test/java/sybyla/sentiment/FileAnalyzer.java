package sybyla.sentiment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Copyright SocialInSoma, 2013
 * User: kiko
 * Date: 11/19/13
 * Time: 4:53 PM
 */
public class FileAnalyzer {

  public static void main(String[] args){

    String input =  args[0];
    String output =  args[1];
    BufferedReader reader=null;
    BufferedWriter writer=null;
    String line =  null;
    int n=0;

    try {
      
      Analyzer analyzer = new Analyzer(Language.PORTUGUESE, Type.PRODUCT);
    

      reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(input)),"UTF-16"));
      writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(output)),"UTF-16"));

      line =  reader.readLine();
      n++;
      writer.write(line+"\t"+"Sentimento\tCerteza");

      while ((line = reader.readLine())!=null){
        n++;
        String[] tokens = line.split("\\t");
        if (tokens.length<8){
          System.out.println("Linha com erro: "+line);
          continue;
        }
        String tweet =  tokens[7];
        Result r =  analyzer.analyze(tweet);
    	double score = r.getResult();
    	double certainty = 100*r.getCertainty();
    	DecimalFormat format = new DecimalFormat("##.#");
    	
        String sentiment ="NEUTRO";
        if (score > 0){
        	sentiment = "POSITIVO";
        } else if (score < 0){
        	sentiment = "NEGATIVO";
        }
        String c =  format.format(certainty);
        writer.write(line+"\t"+sentiment+"\t"+c+"%\n");
      }
    } catch (Exception e){
      System.out.println(e);
      System.out.println(line);
      System.out.println(n);
    }   finally{
      try{
        reader.close();
        writer.close();
      }   catch (Exception e){
        System.out.println(e);
      }

    }
  }
}
