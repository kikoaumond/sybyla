package sybyla.nlp;

public class OpenNLPAnalyzerPool {
    
    private static int nAnalyzers=10;
    private static int nTries=10;
    private static OpenNLPAnalyzer3[] pool;
    private static PortugueseOpenNLPAnalyzer[] portuguesePool;

    private static final String OPEN_NLP_ANALYZER_POOL_SIZE_KEY="open.nlp.analyzer.pool.size";
    
    private OpenNLPAnalyzerPool(){}
    
    public synchronized static void init() {
        
        String poolSize = System.getProperty(OPEN_NLP_ANALYZER_POOL_SIZE_KEY);
        if (poolSize != null) {
            nAnalyzers =  Integer.parseInt(poolSize);
        }
        init(nAnalyzers);
    }
    
    public synchronized static void init(int nAnalyzers) {
        pool =  new OpenNLPAnalyzer3[nAnalyzers];
        for (int i=0; i<pool.length; i++) {
            pool[i] = new OpenNLPAnalyzer3();
        }
        portuguesePool = new PortugueseOpenNLPAnalyzer[nAnalyzers];
        for (int i=0; i<portuguesePool.length; i++) {
            portuguesePool[i] = new PortugueseOpenNLPAnalyzer();
        }
    }
      
    public static synchronized OpenNLPAnalyzer3 getAnalyzer() {
        
        int n=1;
        while (n <= nTries) {
            for(OpenNLPAnalyzer3 analyzer: pool) {
                if (analyzer.isAvailable()) {
                    analyzer.setAvailable(false);
                    return analyzer;
                }
            }
            n++;
        }
        return null;
    }
    
    public static void returnAnalyzer(OpenNLPAnalyzer3 analyzer) {
        analyzer.setAvailable(true);
    }
}
