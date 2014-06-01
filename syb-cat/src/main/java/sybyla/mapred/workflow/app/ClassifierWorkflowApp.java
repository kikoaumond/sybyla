package sybyla.mapred.workflow.app;

import cascading.flow.Flow;
import cascading.property.AppProps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sybyla.mapred.workflow.ClassifierFlow;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Copyright SocialInSoma, 2013
 * User: kiko
 * Date: 1/7/14
 * Time: 2:20 PM
 */
public class ClassifierWorkflowApp
{

    private static final Logger LOG = LoggerFactory.getLogger(ClassifierWorkflowApp.class);

     public static void main(String[] args){

         if (args.length!=2){
             usage();
             System.exit(0);
         }

         String inputFilesDir =  args[0];
         String  workingDir = args[1];

         Map<String,String> options = new HashMap<String,String>();

         options.put(ClassifierFlow.WORKING_DIR_OPTION, workingDir);
         options.put(ClassifierFlow.INPUT_PATH_OPTION, inputFilesDir);
         options.put(ClassifierFlow.N_ITERATIONS,"10");
         options.put(ClassifierFlow.CUMMULATIVE_WEIGHT_PERCENTAGE,"0.7");
         options.put(ClassifierFlow.RELEVANCE_FACTOR,"5");

         Properties properties = new Properties();
         properties.put("io.sort.mb", "1000");
         properties.put("io.sort.record.percent","0.20");
         properties.put("mapred.map.child.java.opts","-Xmx1024m");
         properties.put("mapred.reduce.child.java.opts","-Xmx2048m");
         properties.put( "mapred.child.java.opts", "-Xms2048m -Xmx2048m XX:+UseConcMarkSweepGC");
         AppProps.setApplicationJarClass(properties,sybyla.mapred.workflow.app.ClassifierWorkflowApp.class);


         try{
             // Create & run the workflow
             Flow flow = ClassifierFlow.createFlow(options, properties);
             flow.writeDOT( "dot/classifierWorkflow.dot" );
             flow.complete();
         }  catch(Throwable t){
             LOG.error("Error running workflow ", t);
             System.out.println(t.getMessage());
             System.exit(-1);
         }
     }

    public static void usage(){
        System.out.println("workflow.app.ClassifierWorkflowApp <working dir> <input files dir>");
    }
}
