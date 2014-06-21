package sybyla.mapred.workflow;

import cascading.operation.Buffer;
import cascading.pipe.CoGroup;
import cascading.pipe.GroupBy;
import cascading.pipe.Every;
import cascading.pipe.assembly.Discard;
import cascading.pipe.assembly.SumBy;
import cascading.pipe.assembly.Unique;
import cascading.tuple.Tuple;
import cascading.tuple.collect.SpillableTupleList;
import cascading.tuple.hadoop.collect.HadoopSpillableTupleList;
import com.scaleunlimited.cascading.LoggingFlowProcess;
import com.scaleunlimited.cascading.LoggingFlowReporter;
import org.apache.hadoop.io.compress.GzipCodec;
import sybyla.generated.avro.CategoryWebPage;
import cascading.avro.AvroScheme;
import cascading.flow.Flow;
import cascading.flow.FlowConnector;
import cascading.flow.FlowProcess;
import cascading.flow.hadoop.HadoopFlowConnector;
import cascading.flow.hadoop.HadoopFlowProcess;
import cascading.operation.BaseOperation;
import cascading.operation.BufferCall;
import cascading.operation.Filter;
import cascading.operation.FilterCall;
import cascading.operation.Function;
import cascading.operation.FunctionCall;
import cascading.operation.OperationCall;
import cascading.pipe.Each;
import cascading.pipe.Pipe;
import cascading.scheme.Scheme;
import cascading.scheme.hadoop.TextDelimited;
import cascading.tap.SinkMode;
import cascading.tap.Tap;
import cascading.tap.hadoop.Hfs;
import cascading.tuple.Fields;
import cascading.tuple.TupleEntry;
import com.scaleunlimited.cascading.FsUtils;
import com.scaleunlimited.cascading.NullContext;
import com.scaleunlimited.cascading.hadoop.HadoopUtils;
import sybyla.mapred.CategoryWebPageDatum;
import sybyla.mapred.WinnowDatum;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.JobConf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sybyla.ml.BinaryWinnow;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright SocialInSOMA, 2013
 * User: kiko
 * Date: 10/22/13
 * Time: 1:57 PM
 */
public class ClassifierFlow {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassifierFlow.class);

    public static final String WORKING_DIR_OPTION="workingDir";
    public static final String INPUT_PATH_OPTION="inputPath";
    public static final String FEATURES_DIR_NAME="features";
    public static final String MODELS_DIR_NAME="models";
    public static final String N_ITERATIONS="nIterations";
    public static final String CUMMULATIVE_WEIGHT_PERCENTAGE ="weightPercentage";
    public static final String RELEVANCE_FACTOR="relevanceFactor";
    public static final String TOP_FEATURES_PER_DOCUMENT="topFeaturesPerDocument";

    public static final String URL_FN ="url";
    public static final String PORTUGUESE_CATEGORY_FN="portugueseCategory";
    public static final String CATEGORY_FN="category";
    public static final String FEATURE_FN="feature";
    public static final String FULL_CATEGORY_FN="fullCategory";
    public static final String NEGATIVE_CATEGORIES_FN="negativeCategories";
    public static final String DUMMY_FN ="dummy";
    public static final String IS_CATEGORY_MEMBER_FN="isCategoryMember";
    public static final String FEATURES_SIZE_FN="featuresSize";
    public static final String FEATURES_FN="features";
    public static final String N_POSITIVE_EXAMPLES_FN = "nPositiveExamples";
    public static final String N_NEGATIVE_EXAMPLES_FN = "nNegativeExamples";
    public static final String TERM_WEIGHTS_FN = "termWeights";
    public static final String DISTINCT_CATEGORY_DOCUMENT_COUNT_FN ="distinctDocumentCount";
    public static final String RELEVANCE_FN =  "relevance";

    public static final String RHS = "_rhs";

    public static final String FEATURE_CATEGORY_COUNT_FN = "featureCategoryCount";
    public static final String FEATURE_TOTAL_COUNT_FN = "featureTotalCount";
    public static final String CATEGORY_SIZE_FN = "categorySize";
    public static final String TOTAL_SIZE_FN = "totalSize";

    public static final int DUMMY_VALUE=1;
 /*
        emits
  */
  private static class ExtractFeatures extends BaseOperation<NullContext> implements Function<NullContext> {

    private transient LoggingFlowProcess flowProcess;
    public static final Fields FIELDS = new Fields(DUMMY_FN, URL_FN,FULL_CATEGORY_FN, FEATURE_FN, PORTUGUESE_CATEGORY_FN,
                                                        NEGATIVE_CATEGORIES_FN);
    private int featureLength=3;

    public ExtractFeatures(){
        super(FIELDS);
    }

    @Override
    public void prepare(FlowProcess flowProcess, OperationCall<NullContext> operationCall) {

      super.prepare(flowProcess, operationCall);

      this.flowProcess = new LoggingFlowProcess((HadoopFlowProcess) flowProcess);
      this.flowProcess.addReporter(new LoggingFlowReporter());
    }

    @Override
    public boolean isSafe(){
      return true;
    }

    @Override
    public void operate(FlowProcess flowProcess, FunctionCall<NullContext> functionCall) {

        TupleEntry t = functionCall.getArguments();
        CategoryWebPageDatum categoryWebPageDatum = new CategoryWebPageDatum(t);
        String fullCategory = categoryWebPageDatum.getFullCategory();

        String url = categoryWebPageDatum.getURL();

        List<CharSequence> featureList =  categoryWebPageDatum.getFeatures();
        String portugueseCategory =  categoryWebPageDatum.getPortugueseCategory();
        List<CharSequence> negativeCategories = categoryWebPageDatum.getNegativeCategories();

         for(CharSequence feature: featureList){

            StringBuilder sb = new StringBuilder(feature.length());
            sb.append(feature);
            String f =  sb.toString();

            if (!hasOneAlphaCharacter(f)){
                continue;
            }


            String[] tokens = f.split("\\s");
            int length = tokens.length;
            for (String token: tokens){
                int a= token.indexOf("{");
                int b = token.indexOf("}");

                if (a!=-1 && b!=-1 && b>a){
                   length += b-a-2;
                }
            }

            if (length > featureLength){
                continue;
            }

            Tuple out =  new Tuple();

            out.add(DUMMY_VALUE);
            out.add(url);
            out.add(fullCategory);
            out.add(f);
            out.add(portugueseCategory);
            Tuple nc = buildTuple(negativeCategories);
            out.add(nc);

            functionCall.getOutputCollector().add(out);

            flowProcess.increment(WorkflowCounters.NUMBER_OF_FEATURES,1);
         }

        flowProcess.increment(WorkflowCounters.NUMBER_OF_DOCUMENTS,1);

    }

     private boolean hasOneAlphaCharacter(String feature){

         Matcher m = Pattern.compile("[\\w]", Pattern.UNICODE_CHARACTER_CLASS).matcher(feature);

         if (m.find()){
             return true;
         }

         return false;
     }
    @Override
    public void cleanup(FlowProcess flowProcess, OperationCall<NullContext> operationCall) {

        super.cleanup(flowProcess, operationCall);
    }

  }

    private static class DistinctDocumentBuffer extends BaseOperation<NullContext> implements Buffer<NullContext>{

        private int spillThreshold = 1000000;
        private AtomicLong tuplesProcessed = new AtomicLong(0);
        private static final long LOGGING_EVERY = 10000l;

        private transient LoggingFlowProcess flowProcess;
        private SpillableTupleList tupleListIn;


        public static final Fields FIELDS = new Fields(DUMMY_FN,FULL_CATEGORY_FN, FEATURE_FN, DISTINCT_CATEGORY_DOCUMENT_COUNT_FN,
                                                            FEATURE_CATEGORY_COUNT_FN);

        public DistinctDocumentBuffer() {

            super(FIELDS);
        }

        public boolean isSafe(){
            return true;
        }

        @Override
        public void prepare(FlowProcess flowProcess, OperationCall<NullContext> operationCall) {

            super.prepare(flowProcess, operationCall);

            this.flowProcess = new LoggingFlowProcess((HadoopFlowProcess) flowProcess);
            this.flowProcess.addReporter(new LoggingFlowReporter());

        }

        @Override
        public void operate(FlowProcess flowProcess, BufferCall<NullContext> bufferCall) {


            Iterator<TupleEntry> iter = bufferCall.getArgumentsIterator();
            TupleEntry groupTupleEntry = bufferCall.getGroup();

            String groupFeature = groupTupleEntry.getString(FEATURE_FN);
            String groupCategory =  groupTupleEntry.getString(FULL_CATEGORY_FN);
            TupleEntry in = null;

            DateFormat df = new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z");
            long n = tuplesProcessed.incrementAndGet();
            if ( n % LOGGING_EVERY == 0){
                System.out.println(n+" DistinctDocumentBuffer Feature "+ n+": "+ groupFeature+" "+df.format(new Date()));
            }

            try{
                tupleListIn = new HadoopSpillableTupleList(spillThreshold, new GzipCodec(),HadoopUtils.getDefaultJobConf());
            } catch(Exception e){
                LOGGER.error("Error getting default job configuration",e);
            }

            Set<String> urls = new HashSet<>();
            long featureCategoryCount = 0;

            while (iter.hasNext()) {

                featureCategoryCount++;
                TupleEntry te = iter.next();
                if (in == null){
                    in = te;
                }
                Tuple t = te.getTuple();
                tupleListIn.add(t);

                String url = te.getString(URL_FN);
                urls.add(url);
            }

            int nDistinctPages = urls.size();

            if (nDistinctPages > 1){
                Iterator listIterator = tupleListIn.iterator();

            //while (listIterator.hasNext()){

                Tuple t =  (Tuple) listIterator.next();
                in.setTuple(t);
                Tuple out = new Tuple();


                out.addInteger(in.getInteger(DUMMY_FN));
                out.addString(in.getString(FULL_CATEGORY_FN));
                out.addString(in.getString(FEATURE_FN));
                out.addInteger(nDistinctPages);
                out.addLong(featureCategoryCount);

                bufferCall.getOutputCollector().add(out);
            //}
            }
        }

        @Override
        public void cleanup(FlowProcess flowProcess, OperationCall<NullContext> operationCall) {

            super.cleanup(flowProcess, operationCall);
            tupleListIn.clear();

        }
    }

    private static class ParentCategoryBuffer extends BaseOperation<NullContext> implements Buffer<NullContext>{

        private int spillThreshold = 1000000;
        private static final int LOGGING_EVERY = 5000;
        private static AtomicInteger invocations = new AtomicInteger(0);

        private static final int FEATURE_CATEGORY_COUNT=0;
        private static final int CATEGORY_SIZE=1;
        private transient LoggingFlowProcess flowProcess;
        private SpillableTupleList tupleListIn;


        public static final Fields FIELDS = new Fields(DUMMY_FN, URL_FN,FULL_CATEGORY_FN,FEATURE_FN, PORTUGUESE_CATEGORY_FN,
                                                        NEGATIVE_CATEGORIES_FN, DISTINCT_CATEGORY_DOCUMENT_COUNT_FN, FEATURE_CATEGORY_COUNT_FN, CATEGORY_SIZE_FN,
                                                            TOTAL_SIZE_FN, FEATURE_TOTAL_COUNT_FN) ;

        public ParentCategoryBuffer() {

            super(FIELDS);
        }

        public boolean isSafe(){
            return true;
        }

        @Override
        public void prepare(FlowProcess flowProcess, OperationCall<NullContext> operationCall) {

            super.prepare(flowProcess, operationCall);

            this.flowProcess = new LoggingFlowProcess((HadoopFlowProcess) flowProcess);
            this.flowProcess.addReporter(new LoggingFlowReporter());

        }

        @Override
        public void operate(FlowProcess flowProcess, BufferCall<NullContext> bufferCall) {


            Iterator<TupleEntry> iter = bufferCall.getArgumentsIterator();
            TupleEntry groupTupleEntry = bufferCall.getGroup();

            String groupFeature = groupTupleEntry.getString(FEATURE_FN);
            DateFormat df = new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z");
            int n = invocations.incrementAndGet();
            if ( n % LOGGING_EVERY == 0){
                System.out.println(n+" ParentCategoryBuffer Feature: "+ groupFeature+" "+df.format(new Date()));
            }

            try{
                tupleListIn = new HadoopSpillableTupleList(spillThreshold, new GzipCodec(),HadoopUtils.getDefaultJobConf());
            } catch(Exception e){
                LOGGER.error("Error getting default job configuration",e);
            }

            Set<String> categories = new HashSet<>();

            Map<String, long[]> categoryCounts=null;
            Map<String, long[]> parentCategoryCounts=null;
            Map<String, Set<String>> parentCategories=null;


            Fields fields = null;

            while (iter.hasNext()) {

                TupleEntry te = iter.next();
                Tuple t = te.getTuple();

                if (fields == null )  {
                    fields = te.getFields();
                }

                String category = te.getString(FULL_CATEGORY_FN);

                String feature = te.getString(FEATURE_FN);
                assert(feature.equals(groupFeature));

                long featureCategoryCount = te.getLong(FEATURE_CATEGORY_COUNT_FN);

                tupleListIn.add(t);
                bufferCall.getOutputCollector().add(t);

                if (categories.contains(category)){
                    continue;
                }

                if (categoryCounts ==  null){
                   categoryCounts =  new HashMap<>();
                   parentCategoryCounts = new HashMap<>();
                   parentCategories = new HashMap<>();
                }

                categories.add(category);

                Set<String> parentCategorySet =  getAllParentCategories(category);
                parentCategories.put(category, parentCategorySet);

                long categorySize = te.getLong(CATEGORY_SIZE_FN);
                long[] counts = new long[2];

                counts[FEATURE_CATEGORY_COUNT] = featureCategoryCount;
                counts[CATEGORY_SIZE] = categorySize;

                categoryCounts.put(category, counts);

                for(String parentCategory: parentCategorySet){

                    if (categories.contains(parentCategory)){
                        continue;
                    }

                    long[] parentCounts =  parentCategoryCounts.get(parentCategory);

                    if (parentCounts  ==  null){

                        parentCounts = new long[2];
                        parentCounts[FEATURE_CATEGORY_COUNT] = 0;
                        parentCounts[CATEGORY_SIZE] = 0;
                        parentCategoryCounts.put(parentCategory, parentCounts);
                    }

                    parentCounts[FEATURE_CATEGORY_COUNT] += featureCategoryCount;
                    parentCounts[CATEGORY_SIZE] += categorySize;

                }

            }

            Iterator listIterator = tupleListIn.iterator();
            TupleEntry out=  new TupleEntry(FIELDS);

            while (listIterator.hasNext()){

                Tuple t =  (Tuple) listIterator.next();
                out.setTuple(t);

                String category =  out.getString(FULL_CATEGORY_FN);

                Set<String> parentCategorySet = parentCategories.get(category);
                if (parentCategorySet ==  null){
                    continue;
                }

                for(String parentCategory: parentCategorySet){

                     if (categories.contains(parentCategory)){
                         continue;
                     }
                    long[] counts = parentCategoryCounts.get(parentCategory);
                    if (counts == null){
                        continue;
                    }

                    out.setTuple(new Tuple(t));
                    out.set(FULL_CATEGORY_FN, parentCategory);
                    out.set(FEATURE_CATEGORY_COUNT_FN,counts[FEATURE_CATEGORY_COUNT]);
                    out.set(CATEGORY_SIZE_FN, counts[CATEGORY_SIZE]);
                    bufferCall.getOutputCollector().add(out.getTuple());
                }
            }
        }

        @Override
        public void cleanup(FlowProcess flowProcess, OperationCall<NullContext> operationCall) {

            super.cleanup(flowProcess, operationCall);
            tupleListIn.clear();

        }
    }

    public static String getParentCategory(String fullCategory){

        String result = null;
        int pos = fullCategory.lastIndexOf(">");
        if (pos == -1) {
            return null;
        }

        result = fullCategory.substring(0,pos).trim().toLowerCase();

        return result;
    }

    public static Set<String> getAllParentCategories(String fullCategory){

        Set<String> result = new HashSet<>();
        String parentCategory = getParentCategory(fullCategory);

        while(parentCategory != null) {
            result.add(parentCategory);
            parentCategory = getParentCategory(parentCategory);
        }

        return result;
    }

    public static class RelevanceFunction extends BaseOperation<NullContext> implements Function<NullContext>
    {

        public static final Fields FIELDS = new Fields(DUMMY_FN, URL_FN,FULL_CATEGORY_FN,FEATURE_FN, PORTUGUESE_CATEGORY_FN,
            NEGATIVE_CATEGORIES_FN, DISTINCT_CATEGORY_DOCUMENT_COUNT_FN, FEATURE_CATEGORY_COUNT_FN, CATEGORY_SIZE_FN,
            TOTAL_SIZE_FN, FEATURE_TOTAL_COUNT_FN, RELEVANCE_FN);

        public RelevanceFunction(){

           super(FIELDS);
        }



        @Override
        public void operate(FlowProcess flowProcess, FunctionCall<NullContext> functionCall) {

            // get the arguments TupleEntry
            TupleEntry arguments = functionCall.getArguments();
            String feature = arguments.getString(FEATURE_FN);
            long featureCategoryCount = arguments.getLong(FEATURE_CATEGORY_COUNT_FN);
            long featureTotalCount =  arguments.getLong(FEATURE_TOTAL_COUNT_FN);
            long categorySize =  arguments.getLong(CATEGORY_SIZE_FN);
            long totalSize = arguments.getLong(TOTAL_SIZE_FN);
            int distinctCategoryDocumentCount = arguments.getInteger(DISTINCT_CATEGORY_DOCUMENT_COUNT_FN);


            double categoryRatio = ((double) featureCategoryCount + 1.d)/(double)categorySize;
            double totalRatio =  (double) featureTotalCount/(double)totalSize;
            double notCategoryRatio = (double)(featureTotalCount-featureCategoryCount + 1.d)/(double)(totalSize-categorySize);
            double ratio = 0;

            ratio = categoryRatio/notCategoryRatio;


            //double ratio = categoryRatio/totalRatio;
            double relevance = ((double)distinctCategoryDocumentCount) * Math.log(ratio);

            Tuple t = new Tuple(arguments.getTuple());
            t.addDouble(relevance);

            functionCall.getOutputCollector().add(t);
        }
    }

    public static class RelevanceFilter extends BaseOperation implements Filter
    {

        private double minRelevance = 0;

        public RelevanceFilter()
        {
            super();
        }

        public RelevanceFilter(double minRelevance)
        {
            super();
            this.minRelevance = minRelevance;
        }

        @Override
        public boolean isRemove( FlowProcess flowProcess, FilterCall call )
        {
            // get the arguments TupleEntry
            TupleEntry arguments = call.getArguments();
            double relevance = arguments.getDouble(RELEVANCE_FN);
            if (relevance >= minRelevance){
                return false;
            } else {
                return true;
            }

        }
    }


    private static class DocumentBuffer extends BaseOperation<NullContext> implements Buffer<NullContext>
    {
       private int nTopFeatures =-1;

       public DocumentBuffer(int nTopTerms){
           super(new Fields(URL_FN, FULL_CATEGORY_FN,IS_CATEGORY_MEMBER_FN,FEATURES_SIZE_FN,FEATURES_FN));

           this.nTopFeatures = nTopTerms;
       }

        @Override
        public void operate(FlowProcess flowProcess, BufferCall<NullContext> bufferCall) {

            Iterator<TupleEntry> iter = bufferCall.getArgumentsIterator();

            Map<String,Double> features = new HashMap<>();
            List<String> negativeCategories =  new ArrayList<>();
            String url =  null;
            String category = null;

            int nTuples=0;

            List<Double> relevanceList =  new ArrayList<>();

            while (iter.hasNext()) {

                TupleEntry te =  iter.next();

                url = te.getString(URL_FN);
                category = te.getString(FULL_CATEGORY_FN);
                String feature = te.getString(FEATURE_FN);
                double relevance = te.getDouble(RELEVANCE_FN);

                relevanceList.add(relevance);

                features.put(feature, relevance);
                Tuple negativeCategoriesTuple = (Tuple)te.getObject(NEGATIVE_CATEGORIES_FN);
                nTuples++;

                if (nTuples >1){
                    continue;
                }

                for(int i=0; i < negativeCategoriesTuple.size();i++){

                    String negativeCategory = negativeCategoriesTuple.getString(i);
                    negativeCategories.add(negativeCategory);
                }
            }

            double minRelevance = Double.MIN_VALUE;

            if (nTopFeatures > relevanceList.size()){
                nTopFeatures = relevanceList.size();
            }

            if (nTopFeatures > 0){
                Collections.sort(relevanceList, Collections.reverseOrder());

                minRelevance = relevanceList.get(nTopFeatures-1);
            }

            Tuple positiveFeaturesTuple = new Tuple();
            Tuple negativeFeaturesTuple =  new Tuple();

            for(String feature: features.keySet()){

                double relevance = features.get(feature);

                //put all features in negative tuple
                Tuple negativeFeatureRelevanceTuple = new Tuple();

                negativeFeatureRelevanceTuple.addString(feature);
                negativeFeatureRelevanceTuple.addDouble(relevance);

                negativeFeaturesTuple.add(negativeFeatureRelevanceTuple);

                //only put the highest ranked features in psotive tuple
                if (relevance >= minRelevance){

                    Tuple featureRelevanceTuple = new Tuple();

                    featureRelevanceTuple.addString(feature);
                    featureRelevanceTuple.addDouble(relevance);

                    positiveFeaturesTuple.add(featureRelevanceTuple);
                }

            }

            Tuple positiveTuple = new Tuple();
            positiveTuple.add( url);
            positiveTuple.add(category);
            positiveTuple.add(true);
            positiveTuple.add(positiveFeaturesTuple.size());
            positiveTuple.add(positiveFeaturesTuple);
            System.out.println("POSITIVE: "+category+" FEATURES: "+positiveFeaturesTuple.size());

            bufferCall.getOutputCollector().add(positiveTuple);

            for(String negativeCategory: negativeCategories){

                Tuple negativeTuple = new Tuple();
                negativeTuple.add(url);
                negativeTuple.add(negativeCategory);
                negativeTuple.add(false);
                negativeTuple.add(negativeFeaturesTuple.size());
                negativeTuple.add(negativeFeaturesTuple);
                System.out.println("NEGATIVE: "+negativeCategory+" FEATURES: "+negativeFeaturesTuple.size());


                bufferCall.getOutputCollector().add(negativeTuple);
            }
        }
    }

    /**
     * receives  [url][category][is_category_member][n_terms][features]
     *
     * emits     [category][WinnowDatum.CATEGORY_FN][WinnowDatum.TERM_WEIGHTS_FN]
     *
     * @author kiko
     *
     */

    public static class WinnowBuffer
        extends BaseOperation<NullContext> implements Buffer<NullContext>{

        private transient SpillableTupleList tupleList;
        private int spillThreshold = 2000;

        private int nIterations = 1;
        private transient LoggingFlowProcess flowProcess;

        private double threshold = 0.01;

        private int capacity = 2500000;
        private double percentagePruning = 0.1;
        private double weightPercentage = 0;

        public WinnowBuffer(int nIterations, double weightPercentage){
            super(new Fields(CATEGORY_FN, N_POSITIVE_EXAMPLES_FN,N_NEGATIVE_EXAMPLES_FN, TERM_WEIGHTS_FN));
            this.nIterations = nIterations;
            this.weightPercentage = weightPercentage;
        }

        public WinnowBuffer(int nIterations, int nTermsToKeep){
            super(WinnowDatum.FIELDS);
            this.nIterations = nIterations;
        }

        @Override
        public void prepare(FlowProcess flowProcess, OperationCall<NullContext> operationCall) {
            super.prepare(flowProcess, operationCall);
            this.flowProcess = new LoggingFlowProcess((HadoopFlowProcess) flowProcess);
            this.flowProcess.addReporter(new LoggingFlowReporter());
        }

        @Override
        public void operate(FlowProcess flowProceess, BufferCall<NullContext> bufferCall) {

            TupleEntry group  = bufferCall.getGroup();
            String modelCategory = group.getString(FULL_CATEGORY_FN);
            BinaryWinnow winnow = new BinaryWinnow(modelCategory);

            try{
                tupleList = new HadoopSpillableTupleList(spillThreshold, new GzipCodec(),HadoopUtils.getDefaultJobConf());
            } catch(Exception e){
                LOGGER.error("Error getting default job configuration",e);
                System.out.println("Error getting default job configuration"+e.getMessage()+" Category: "+modelCategory);
                return;

            }

            Iterator<TupleEntry> iterator = bufferCall.getArgumentsIterator();

            while ( iterator.hasNext()) {
                TupleEntry arguments = iterator.next();
                tupleList.add(arguments.getTuple());
                flowProcess.increment(WorkflowCounters.NUMBER_OF_TRAINING_EXAMPLES, 1);
            }


            //count positive and negative documents the first time around
            winnow.set_updateCounts(true);
            Iterator<Tuple> listIterator = tupleList.iterator();
            int counter=0;
            while (listIterator.hasNext()) {

                counter++;
                Tuple arguments = listIterator.next();
                String tupleCategory = arguments.getString(1);
                boolean isCategoryMember = arguments.getBoolean(2);

                if (!isCategoryMember) {
                    tupleCategory = "NOT-"+modelCategory;
                }
                int nTerms =arguments.getInteger(3);

               // Set<String> features =  new HashSet<>();
               // Tuple featuresTuple = (Tuple) arguments.getObject(4);
                Tuple featureRelevanceTupleList = (Tuple) arguments.getObject(4);
                Map<String, Double> featureRelevanceMap = new HashMap<>();

                for(int f=0;f<featureRelevanceTupleList.size(); f++){

                    Tuple featureRelevanceTuple = (Tuple)featureRelevanceTupleList.getObject(f);

                    String feature =  featureRelevanceTuple.getString(0);
                    double relevance = featureRelevanceTuple.getDouble(1);
                    featureRelevanceMap.put(feature, relevance);
                }
                /*
                if(winnow.get_nTerms() + features.size() > capacity) {
                    int excludedTerms = winnow.excludeMostNegativeTerms(percentagePruning);
                    flowProcess.increment(WorkflowCounters.NUMBER_OF_AUTO_PRUNING_EVENTS, 1);
                    flowProcess.increment(WorkflowCounters.NUMBER_OF_AUTO_PRUNED_TERMS, excludedTerms);

                } */

                if (isCategoryMember){
                    System.out.println("Winnow "+tupleCategory+" training with POSITIVE example with "+featureRelevanceMap.keySet().size()+" features");
                    winnow.set_restrict(false);
                } else {
                    System.out.println("Winnow "+tupleCategory+" training with NEGATIVE example with "+featureRelevanceMap.keySet().size()+" features");
                    winnow.set_restrict(true);
                }

                winnow.train(tupleCategory, featureRelevanceMap);
                flowProcess.increment(WorkflowCounters.NUMBER_OF_TRAINING_EXAMPLES_FIRST_PASS, 1);
                monitorMemory(flowProcess);
                monitorWinnowMemory(flowProcess, winnow);
            }

            monitorWinnowMemory(flowProcess, winnow);

            winnow.set_updateCounts(false);//turn off counting
            //now prune the winnow

            //winnow.pruneBySignificance(threshold);
            int nPruned = winnow.pruneByCummulativeWeight(weightPercentage);

            //do not add any more terms to the winnow
            winnow.set_restrict(true);
            //dump the list of excluded terms, which can be potentially be very large
            //and is not needed anymore, now that the winnow is restricted, i.e., no more
            //terms can be added to it.
            winnow.dumpExcludedTerms();

            for (int i=1; i<=nIterations-1;i++) {

                listIterator = tupleList.iterator();

                while (listIterator.hasNext()) {

                    Tuple arguments = listIterator.next();
                    String tupleCategory = arguments.getString(1);
                    boolean isCategoryMember = arguments.getBoolean(2);

                    if (!isCategoryMember) {
                        tupleCategory = "NOT-"+modelCategory;
                    }


                    int nTerms =arguments.getInteger(3);

                    //Set<String> features =  new HashSet<>();
                    Map<String, Double> features = new HashMap<>();
                    Tuple featuresTuple = (Tuple) arguments.getObject(4);

                    for(int f=0;f<featuresTuple.size(); f++){

                        Tuple featureRelevanceTuple =  (Tuple)featuresTuple.getObject(f);

                        String feature = featureRelevanceTuple.getString(0);
                        double relevance = featureRelevanceTuple.getDouble(1);
                        if (relevance > 0) {
                            features.put(feature,relevance);

                        }
                    }

                    winnow.train(tupleCategory, features);
                    monitorMemory(flowProcess);
                    monitorWinnowMemory(flowProcess, winnow);
                    flowProcess.increment(WorkflowCounters.NUMBER_OF_TRAINING_EXAMPLES_POST_AUTO_PRUNING, 1);
                }
            }

            WinnowDatum winnowDatum = new WinnowDatum(winnow.get_label(),
                                                        winnow.get_weights(),
                                                        winnow.get_nPositiveExamples(),
                                                        winnow.get_nNegativeExamples());

            Tuple tuple = winnowDatum.getTuple();
            flowProcess.increment(WorkflowCounters.NUMBER_OF_CATEGORY_MODELS, 1);
            flowProcess.increment(WorkflowCounters.NUMBER_OF_CATEGORY_MODEL_TERMS, winnow.get_nTerms());
            bufferCall.getOutputCollector().add(tuple);
        }

        @Override
        public void cleanup(FlowProcess flowProcess, OperationCall<NullContext> operationCall) {
            this.flowProcess.dumpCounters();
            super.cleanup(flowProcess, operationCall);
        }
    }

    private static void monitorWinnowMemory(LoggingFlowProcess flowProcess, BinaryWinnow winnow) {
        long  winnowTermsMb = flowProcess.getCounter(WorkflowCounters.WINNOW_TERMS_MB);
        long  currentTermsMb = (int) Math.round(((double) winnow.get_nTermBytes())/(1024.d*1024.d));


        long winnowExcludedTermsMb = flowProcess.getCounter(WorkflowCounters.WINNOW_EXCLUDED_TERMS_MB);
        long currentExcludeTermsMb = (int) Math.round(((double) winnow.get_nExcludedTermBytes())/(1024.d*1024.d));

        if (currentTermsMb > winnowTermsMb) {
            flowProcess.increment(WorkflowCounters.WINNOW_TERMS_MB, currentTermsMb - winnowTermsMb);
        }

        if (currentExcludeTermsMb > winnowExcludedTermsMb) {
            flowProcess.increment(WorkflowCounters.WINNOW_EXCLUDED_TERMS_MB, currentExcludeTermsMb - winnowExcludedTermsMb);
        }
    }

    private static void monitorMemory(LoggingFlowProcess flowProcess) {

        long  memInMb = flowProcess.getCounter(WorkflowCounters.HIGH_WATER_MEMORY_USAGE);
        Runtime runtime = Runtime.getRuntime();
        long currentMemoryUsage = runtime.totalMemory() - runtime.freeMemory();

        int currentMemInMb = (int) Math.round(((double) currentMemoryUsage)/(1024.d*1024.d));
        if (currentMemInMb > memInMb) {
            flowProcess.increment(WorkflowCounters.HIGH_WATER_MEMORY_USAGE, currentMemInMb - memInMb);
        }

        long freeMemoryMb = flowProcess.getCounter(WorkflowCounters.LOW_WATER_FREE_MEMORY);
        long currentFreeMemory = runtime.freeMemory();
        int currentFreeMemoryMb = (int) Math.round(((double) currentFreeMemory)/(1024.d*1024.d));
        if (currentFreeMemoryMb < freeMemoryMb || freeMemoryMb==0) {
            flowProcess.increment(WorkflowCounters.LOW_WATER_FREE_MEMORY, currentFreeMemoryMb - freeMemoryMb);
        }

        long totalMemoryMb = flowProcess.getCounter(WorkflowCounters.HIGH_WATER_TOTAL_MEMORY);
        long currentTotalMemory = runtime.totalMemory();
        int currentTotalMemoryMb = (int) Math.round(((double) currentTotalMemory)/(1024.d*1024.d));
        if (currentTotalMemoryMb > totalMemoryMb || totalMemoryMb==0) {
            flowProcess.increment(WorkflowCounters.HIGH_WATER_TOTAL_MEMORY, currentTotalMemoryMb - totalMemoryMb);
        }
    }

 private static Tuple buildTuple(Collection<CharSequence> s){

    Tuple tuple = new Tuple();

    for (CharSequence cs: s){
        StringBuilder sb = new StringBuilder(cs.length());
        sb.append(cs);
        String f =  sb.toString();
        tuple.addString(f);
    }

    return tuple;
 }


  public static Flow createFlow(Map<String,String> options) throws IOException, InterruptedException {

      return createFlow(options, null);
  }

  public static Flow createFlow(Map<String,String> options, Map props) throws IOException, InterruptedException {

    // Find working directory
    JobConf conf = HadoopUtils.getDefaultJobConf();
    conf.setMemoryForMapTask(1024);
    conf.setMemoryForReduceTask(2048);
    Path workingDirPath = new Path(options.get(WORKING_DIR_OPTION));
    FileSystem workingFs = workingDirPath.getFileSystem(conf);

    if (!workingFs.exists(workingDirPath)) {
      workingFs.mkdirs(workingDirPath);
    }

    FsUtils.assertPathExists(workingFs, workingDirPath, "Working directory");
    if (props  ==  null){
        props = HadoopUtils.getDefaultProperties(ClassifierFlow.class, false, conf);
    }

    // Read in Avro records and convert to Features
    Path inputFilePath = new Path(options.get(INPUT_PATH_OPTION));
    FileSystem inputFs = inputFilePath.getFileSystem(conf);
    FsUtils.assertPathExists(inputFs, inputFilePath, "Input avro files");

    Scheme sourceScheme = new AvroScheme(CategoryWebPage.SCHEMA$);
    Tap avroSource = new Hfs(sourceScheme, inputFilePath.toString(),SinkMode.KEEP);

    Pipe assembly = new Pipe("Assembly");
    //read the avro files
    assembly = new Each(assembly, new ExtractFeatures());            //1
    //out: [url][fullCategory][feature][dummy]

        //Pipe dcf = new SumBy("DCF",assembly, new Fields(DUMMY_FN, FULL_CATEGORY_FN, FEATURE_FN),           //2
        //                            new Fields(DUMMY_FN), new Fields(FEATURE_CATEGORY_COUNT_FN), long.class);

        Pipe dcf = new GroupBy("DCF",assembly, new Fields(DUMMY_FN, FULL_CATEGORY_FN,FEATURE_FN));
        dcf = new Every(dcf, new DistinctDocumentBuffer(), Fields.RESULTS);       //10

        Pipe dc = new SumBy("DC",dcf, new Fields(DUMMY_FN, FULL_CATEGORY_FN),       //3
                                new Fields(FEATURE_CATEGORY_COUNT_FN), new Fields(CATEGORY_SIZE_FN), long.class);

        Pipe d = new SumBy ("D",dc, new Fields(DUMMY_FN), new Fields(CATEGORY_SIZE_FN), new Fields(TOTAL_SIZE_FN), long.class);   //3

        Fields dcDeclared = new Fields(DUMMY_FN, FULL_CATEGORY_FN, CATEGORY_SIZE_FN, DUMMY_FN+RHS, TOTAL_SIZE_FN);

        dc = new CoGroup(dc, new Fields(DUMMY_FN), d, new Fields(DUMMY_FN),dcDeclared);          //5

        dc = new Discard(dc,new Fields(DUMMY_FN+RHS));

        Pipe df  = new SumBy("DF",dcf, new Fields(DUMMY_FN, FEATURE_FN),
                                new Fields(FEATURE_CATEGORY_COUNT_FN), new Fields(FEATURE_TOTAL_COUNT_FN), long.class);      //6

        Fields dcfDeclared = new Fields(DUMMY_FN,FULL_CATEGORY_FN,FEATURE_FN, DISTINCT_CATEGORY_DOCUMENT_COUNT_FN, FEATURE_CATEGORY_COUNT_FN,DUMMY_FN+RHS,
                                        FULL_CATEGORY_FN+RHS,CATEGORY_SIZE_FN,TOTAL_SIZE_FN);

        dcf = new CoGroup(dcf, new Fields(DUMMY_FN,FULL_CATEGORY_FN), dc, new Fields(DUMMY_FN,FULL_CATEGORY_FN),dcfDeclared);      //7

        dcf = new Discard(dcf, new Fields(DUMMY_FN+RHS,FULL_CATEGORY_FN+RHS));
        Fields dcfDeclared2 =  new Fields(DUMMY_FN,FULL_CATEGORY_FN,FEATURE_FN, DISTINCT_CATEGORY_DOCUMENT_COUNT_FN, FEATURE_CATEGORY_COUNT_FN,CATEGORY_SIZE_FN,
                                            TOTAL_SIZE_FN, DUMMY_FN+RHS,FEATURE_FN+RHS, FEATURE_TOTAL_COUNT_FN);

        dcf = new CoGroup(dcf, new Fields(DUMMY_FN, FEATURE_FN),df, new Fields(DUMMY_FN,FEATURE_FN), dcfDeclared2);         //8

        dcf = new Discard(dcf, new Fields(DUMMY_FN+RHS, FEATURE_FN+RHS));

        Fields assemblyDeclared = new Fields(DUMMY_FN, URL_FN,FULL_CATEGORY_FN,FEATURE_FN, PORTUGUESE_CATEGORY_FN, NEGATIVE_CATEGORIES_FN,DUMMY_FN+RHS, FULL_CATEGORY_FN+RHS,
            FEATURE_FN+RHS, DISTINCT_CATEGORY_DOCUMENT_COUNT_FN, FEATURE_CATEGORY_COUNT_FN, CATEGORY_SIZE_FN, TOTAL_SIZE_FN, FEATURE_TOTAL_COUNT_FN);

    assembly = new CoGroup(assembly, new Fields(DUMMY_FN, FULL_CATEGORY_FN, FEATURE_FN),                           //9
                                dcf, new Fields(DUMMY_FN,FULL_CATEGORY_FN, FEATURE_FN), assemblyDeclared);

    assembly =  new Discard(assembly, new Fields(DUMMY_FN+RHS,FULL_CATEGORY_FN+RHS, FEATURE_FN+RHS));

//    assembly = new GroupBy("Distinct Document Buffer",assembly, new Fields(FULL_CATEGORY_FN,FEATURE_FN));
//    assembly = new Every(assembly, new DistinctDocumentBuffer(), Fields.RESULTS);       //10

    assembly = new Unique( "Dedupe Features", assembly, new Fields(FULL_CATEGORY_FN, URL_FN, FEATURE_FN) );    //11

    assembly = new GroupBy("Parent Category Buffer",assembly, new Fields(FEATURE_FN));
    assembly = new Every(assembly, new ParentCategoryBuffer(), Fields.RESULTS);       //12

    String relevanceFactorStr =  options.get(RELEVANCE_FACTOR);
    double relevanceFactor = Double.parseDouble(relevanceFactorStr);

   assembly = new Each(assembly, new RelevanceFunction());               //13
   //assembly = new Each(assembly, new RelevanceFilter());               //13

      Pipe modelPipe = new Pipe("models", assembly);
    modelPipe = new GroupBy("Document Buffer",modelPipe, new Fields(URL_FN, FULL_CATEGORY_FN));

    String nTopFeaturesStr =  options.get(TOP_FEATURES_PER_DOCUMENT);
    int nTopFeatures =  Integer.parseInt(nTopFeaturesStr);

     modelPipe = new Every(modelPipe, new DocumentBuffer(nTopFeatures), Fields.SWAP);      //14

    String  nIterationsStr = options.get(N_ITERATIONS);
    int nIterations = Integer.parseInt(nIterationsStr);
    modelPipe = new GroupBy("Winnow Buffer", modelPipe, new Fields(FULL_CATEGORY_FN));

    String weightPercentageStr =  options.get(CUMMULATIVE_WEIGHT_PERCENTAGE);
    double weightPercentage = Double.parseDouble(weightPercentageStr);
    modelPipe = new Every(modelPipe, new WinnowBuffer(nIterations, weightPercentage), Fields.RESULTS);    //15

    Path modelsPath = new Path(workingDirPath, MODELS_DIR_NAME);

      if (!workingFs.exists(modelsPath)) {
          workingFs.mkdirs(modelsPath);
      }

    Scheme modelsSinkScheme = new TextDelimited(true,"\t");
    Tap modelsSink = new Hfs(modelsSinkScheme, modelsPath.toString(), SinkMode.REPLACE);

    Path featuresPath = new Path(workingDirPath,FEATURES_DIR_NAME);

    if (!workingFs.exists(featuresPath)) {
      workingFs.mkdirs(featuresPath);
    }

    Scheme featuresSinkScheme = new TextDelimited(true,"\t");
    Tap featuresSink = new Hfs(featuresSinkScheme, featuresPath.toString(), SinkMode.REPLACE);

    Map<String, Tap> sinks = new HashMap<String, Tap>();
    sinks.put(assembly.getName(), featuresSink);
    sinks.put(modelPipe.getName(), modelsSink);

    // Build and return the workflow
    FlowConnector flowConnector = new HadoopFlowConnector(props);
    return flowConnector.connect("Category Web Page Feature and Models", avroSource, sinks, assembly,modelPipe);
  }
}

