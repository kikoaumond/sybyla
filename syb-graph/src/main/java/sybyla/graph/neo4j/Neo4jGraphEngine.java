package sybyla.graph.neo4j;


import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.graphdb.index.UniqueFactory;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.helpers.collection.MapUtil;
import org.neo4j.index.lucene.unsafe.batchinsert.LuceneBatchInserterIndexProvider;
import org.neo4j.kernel.EmbeddedReadOnlyGraphDatabase;
import org.neo4j.kernel.Traversal;
import org.neo4j.kernel.Uniqueness;
import org.neo4j.kernel.impl.util.FileUtils;
import org.neo4j.tooling.GlobalGraphOperations;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.neo4j.unsafe.batchinsert.BatchInserterIndex;
import org.neo4j.unsafe.batchinsert.BatchInserterIndexProvider;
import org.neo4j.unsafe.batchinsert.BatchInserters;

import sybyla.graph.GraphLoader;
import sybyla.graph.RankedNode;
import sybyla.graph.Tree;

public class Neo4jGraphEngine implements GraphLoader
{
	private static final Logger LOGGER = Logger.getLogger(Neo4jGraphEngine.class);
    private static final String DEFAULT_DB_PATH = "/mnt/data/current/graph/neo4j/";
    private static Map<String, String> batchInsertConfig;
    private static Map<String, Object> nodeProperties;
    private static Map<String, Object> relationshipProperties;

    public static final String TERM="T";
    
    protected static final String C="c";
    public static final String T="t";
    public static final String R="r";
    
    public static GraphDatabaseService graphDb;
    public static Index<Node> nodeIndex;
    
    private static BatchInserter inserter;
    private static BatchInserterIndexProvider indexProvider;
    private static BatchInserterIndex batchNodeIndex;
    
    private static final String TERM_INDEX_NAME="termIndexFullTextCaseInsensitive";
    private static final int DEFAULT_INDEX_CACHE_CAPACITY=3000000;
    private static int indexCacheCapacity= DEFAULT_INDEX_CACHE_CAPACITY;
 
    private static Neo4jGraphEngine graphEngine=  new Neo4jGraphEngine();
            
    private static String dbPath = DEFAULT_DB_PATH;
    
    private static int nInsertedNodes=0;
    
    private static final TraversalDescription BREADTH_FIRST_TRAVERSAL =  
    											Traversal.description()
    											.breadthFirst()
    											.relationships(RelTypes.IS_RELATED_TO, Direction.OUTGOING)
    											.uniqueness(Uniqueness.NODE_RECENT, 111)
    											.evaluator(Evaluators.toDepth(1));

    private static Map<String,Long> batchIdMap;
    private static long currentNodeId = 1;
    
    public static Neo4jGraphEngine getNeo4jGraphEngine(){
    	return graphEngine;
    }
    
	public static void setDBPath(String path){
		if (path != null && !path.trim().equals("")){
	    	dbPath = path;
		}
    }
    
    private static enum RelTypes implements RelationshipType
    {
        IS_RELATED_TO
    }
    public static void init() throws IOException{
    	init(null);
    }
    /**
     * Initializes a brand new database, deleting the existing one 
     * @throws IOException
     */
    public static void init(String db) throws IOException{
    	if (db != null){
    		dbPath = db;
    	}
    	LOGGER.info("Initializing Neo4j database in "+ dbPath);
    	LOGGER.info("deleting files in " +dbPath);
    	FileUtils.deleteRecursively( new File( dbPath ) );
    	graphDb = new GraphDatabaseFactory().newEmbeddedDatabase( dbPath );
        nodeIndex = graphDb.index().forNodes( TERM_INDEX_NAME, 
				 							   MapUtil.stringMap( IndexManager.PROVIDER, "lucene", 
				 										"type", "fulltext",
				 										"to_lower_case","true"));
        inserter=null;
        registerShutdownHook();
    } 
    
    public static void initBatchMode(Map<String,String> batchConfig) throws IOException{
    	initBatchMode(null, batchConfig);
    }
    /**
     * Initializes a brand new database in batch mode, deleting the existing one if it exists
     * and creates a BatchInserter for fast loading of graph nodes
     * @throws IOException
     */
    public static void initBatchMode(String db, Map<String,String> batchConfig) throws IOException{
    	if (db!=null){
    		dbPath=db;
    	}
    	LOGGER.info("Initializing Neo4JGraphEngine in batch mode");
    	LOGGER.info("Initializing Neo4j database in "+ dbPath);
    	LOGGER.info("deleting files in " +dbPath);
    	FileUtils.deleteRecursively( new File( dbPath ) );
    	
    	if (batchConfig == null){
    		batchInsertConfig = new HashMap<String,String>();
    		batchInsertConfig.put("neostore.nodestore.db.mapped_memory","90M");
    		batchInsertConfig.put("neostore.relationshipstore.db.mapped_memory","3G");
    		batchInsertConfig.put("neostore.propertystore.db.mapped_memory","50M");
    		batchInsertConfig.put("neostore.propertystore.db.strings.mapped_memory","100M");
    		batchInsertConfig.put("neostore.propertystore.db.arrays.mapped_memory","0M");
    	}
    	
        inserter = BatchInserters.inserter( dbPath, batchInsertConfig );
        indexProvider = new LuceneBatchInserterIndexProvider( inserter );
        
        batchNodeIndex = 
        		indexProvider.nodeIndex(TERM_INDEX_NAME, 
        								 MapUtil.stringMap( IndexManager.PROVIDER, "lucene", 
        												  	"type", "fulltext",
        												  	"to_lower_case","true") );
        
        batchNodeIndex.setCacheCapacity( TERM, indexCacheCapacity );
        
        nodeProperties = new HashMap<String,Object>();
        relationshipProperties = new HashMap<String,Object>();
        
        batchIdMap =  new HashMap<String,Long>();

        registerShutdownHook();
    }
    
    public static void initBatchMode() throws IOException{
    	initBatchMode(null);
    }

    
    /**
     * Starts a pre-existing database for querying
     * 
     */
    public static void start(){
    	LOGGER.info("Starting Neo4j graph database");
    	
    	graphDb = new EmbeddedReadOnlyGraphDatabase(dbPath );
    	IndexManager index = graphDb.index();
    	boolean indexExists = index.existsForNodes( TERM_INDEX_NAME );
    	if (!indexExists){
    		LOGGER.error("No index "+TERM_INDEX_NAME+ " found.  Exiting");
    		System.exit(-1);
    	}
    	LOGGER.info("Graph DB: "+graphDb.toString());
        nodeIndex = graphDb.index().forNodes(TERM_INDEX_NAME );
        
        LOGGER.info("Index: "+nodeIndex.getName());
        registerShutdownHook();
    }
    
    public static Iterable<Node> getAllNodes(){
    	if (graphDb == null){
    		return null;
    	}
    	Iterable<Node> it = GlobalGraphOperations.at(graphDb).getAllNodes();
    	return it;
    }
    
    public static Iterable<Relationship> getAllRelationships(){
    	if (graphDb == null){
    		return null;
    	}
    	Iterable<Relationship> it = GlobalGraphOperations.at(graphDb).getAllRelationships();
    	return it;
    }

    
	@Override
	public boolean insert(int c, String term, int t, String related, int r) {
		Transaction tx = graphDb.beginTx();
        try
        {
        	Node termNode = createAndIndexTerm( term );
        	Node relatedNode = createAndIndexTerm( related );	
        	
        	Relationship termToRelatedEdge = termNode.createRelationshipTo(relatedNode, RelTypes.IS_RELATED_TO);
        	termToRelatedEdge.setProperty(C, c);
        	termToRelatedEdge.setProperty(T, t);
        	
        	Relationship relatedToTermEdge = relatedNode.createRelationshipTo(termNode, RelTypes.IS_RELATED_TO);
        	relatedToTermEdge.setProperty(C, c);
        	relatedToTermEdge.setProperty(T, r);
            
            tx.success();
            nInsertedNodes++;
			if (nInsertedNodes%10000==0){
				LOGGER.info(nInsertedNodes + " nodes inserted in graph DB");
			}
            
        }
        catch (Throwable thr){
        	LOGGER.error("Error inserting elements in graph",thr);
        	return false;
        }
        finally
        {
            tx.finish();

        }
        
        return true;
	}
	
	private static Long getId(String term){
		Long id = batchIdMap.get(term);
		
		return id;
	}
	
	private static Long setId(String term){
		Long id = batchIdMap.get(term);
		if (id == null){
			batchIdMap.put(term, currentNodeId);
			id = currentNodeId;
			currentNodeId++;
			return id;
		}
		return id;
	}
	
	@Override
	public boolean insertBatch(int c, String term, int t, String related, int r) {
		try {
			nodeProperties.clear();
			nodeProperties.put(TERM, term);
			
			Long termNodeId = getId(term);
			if (termNodeId ==  null){
				termNodeId =  setId(term);
				inserter.createNode(termNodeId,nodeProperties);
				batchNodeIndex.add(termNodeId, nodeProperties);
			}
				
			nodeProperties.clear();
			nodeProperties.put(TERM, related);
			
			Long relatedNodeId = getId(related);
			if (relatedNodeId ==  null){
				relatedNodeId = setId(related);
				inserter.createNode(relatedNodeId,nodeProperties);
				batchNodeIndex.add(relatedNodeId, nodeProperties);
			}
			
			relationshipProperties.clear();
			relationshipProperties.put(T, t);
			relationshipProperties.put(C, c);

			inserter.createRelationship(termNodeId, relatedNodeId, 
										RelTypes.IS_RELATED_TO, 
										relationshipProperties);
			
			relationshipProperties.clear();
			relationshipProperties.put(T, r);
			relationshipProperties.put(C, c);

			inserter.createRelationship(relatedNodeId, termNodeId, 
										RelTypes.IS_RELATED_TO, 
										relationshipProperties);
			nInsertedNodes++;
			if (nInsertedNodes%10000==0){
				batchNodeIndex.flush();
				LOGGER.info(nInsertedNodes + " edges inserted in graph DB");
			}
			return true;
		} catch(Throwable thr){
			LOGGER.error("Error inserting elements in batch mode in graph",thr);
			return false;
		}

	}
	
	public static Node getNode(String term){
		
		Node n  = nodeIndex.get(TERM, term).getSingle();
		if (n!= null){
			return n;
		}
	
		IndexHits<Node>	hits = nodeIndex.query( TERM, "\"" + term + "\"" );
		
		if (hits.size()==0){
			hits = nodeIndex.query( TERM, "\"*" + term + "*\"" );
			if (hits.size() == 0){
				String[] tokens = term.split("\\s");
				if (tokens.length == 0){
					return null;
				}
				for(String token: tokens){
					hits = nodeIndex.query( TERM, "\"*" + token + "*\"" );
					if (hits != null){
						break;
					}
				}
				if (hits ==  null){
					return null;
				}
			}
		}
		Iterator<Node> it =  hits.iterator();
		if (it.hasNext()){
			n = it.next();
			hits.close();
			return n;
		}
		
		return null;
	}
	
	public static Set<Node>  getRelatedNodes(Node node){
		
		LOGGER.debug("Nodes related to "+toString(node));
		
		Traverser traverser = BREADTH_FIRST_TRAVERSAL.traverse(node);
		Set<Node> relatedNodes = 
									new HashSet<Node>();
		
		for ( Relationship relationship : traverser.relationships() )
        {
            Node relatedNode = relationship.getEndNode();
            
            relatedNodes.add(relatedNode);
        }
		return relatedNodes;
	}
	
	public static SortedSet<RankedNode>  getSortedRelatedNodes(Node node, Map<Long,Node> visited){
		
		LOGGER.debug("Nodes related to "+toString(node));
		
		Traverser traverser = BREADTH_FIRST_TRAVERSAL.traverse(node);
		SortedSet<RankedNode> relatedNodes = 
									new TreeSet<RankedNode>(Collections.reverseOrder());
		
		for ( Relationship relationship : traverser.relationships() )
        {
            Node related = relationship.getEndNode();
            long relatedId =  related.getId();
            if (visited.containsKey(relatedId)){
            	continue;
            }
            visited.put(relatedId, related);
            float rank =  getRelevance(relationship);
            String term =  term(related);
            LOGGER.debug(term+" relevance: "+ rank);
            RankedNode rn = new RankedNode(term,relatedId,rank);
            relatedNodes.add(rn);
        }
		return relatedNodes;
	}
	
	public static SortedSet<RankedNode>  getSortedRelatedNodes(Node node){
		
		LOGGER.debug("Nodes related to "+toString(node));
		
		Traverser traverser = BREADTH_FIRST_TRAVERSAL.traverse(node);
		SortedSet<RankedNode> relatedNodes = 
									new TreeSet<RankedNode>(Collections.reverseOrder());
		
		for ( Relationship relationship : traverser.relationships() )
        {
            Node related = relationship.getEndNode();
            long relatedId =  related.getId();
            float rank =  getRelevance(relationship);
            String term =  term(related);
            LOGGER.debug(term+" relevance: "+ rank);
            RankedNode rn = new RankedNode(term,relatedId,rank);
            relatedNodes.add(rn);
        }
		return relatedNodes;
	}
	
	public static Tree getRelatedTree(String term, int depth){
		Node node = getNode(term);
		return getRelatedTree(node,-1,depth,-1);
	}
	
	public static Tree getRelatedTree(String term, int order, int depth){
		Node node = getNode(term);
		return getRelatedTree(node,order,depth,-1);
	}
	
	public static Tree getRelatedTree(String term, int order, int depth, int firstDegreeOrder){
		Node node = getNode(term);
		if(node ==null){
			RankedNode rootNode =  new RankedNode(term,1);
			Tree tree = new Tree(rootNode,order,depth,firstDegreeOrder);
			return tree;
		}
		return getRelatedTree(node,order,depth,firstDegreeOrder);
	}
	
	public static Tree getRelatedTree(Node node, int order, int depth, int firstDegreeOrder){
		if (node ==  null){
			RankedNode rootNode =  new RankedNode(null,0);
			
			Tree tree = new Tree(rootNode,order,depth,firstDegreeOrder);
			return tree;
		}
		
		Map<Long, Node> visited = new HashMap<Long,Node>();
		
		String term = term(node);
		long nodeId  = node.getId();
		visited.put(nodeId, node);
		RankedNode rootNode =  new RankedNode(term,nodeId);
		
		Tree tree = new Tree(rootNode,order, depth, firstDegreeOrder);

		getRecursive( rootNode, tree,  1, visited);
				
		return tree;
	}
	
	private static void getRecursive(RankedNode parent, Tree tree, int depth, Map<Long, Node> visited){
		
		if (!tree.contains(parent)){
			return;
		}
		
		int maxDepth = tree.getMaxDepth();
		
		if (maxDepth > 0 && depth > maxDepth){
			return;
		}
		
		long nodeId = parent.getNodeId();
		
		Node node = visited.get(nodeId);
		int order = tree.getNodeOrder(parent);
		
		SortedSet<RankedNode> sortedRelatedNodes = getSortedRelatedNodes(node,visited);
		Iterator<RankedNode> iterator = sortedRelatedNodes.iterator();
		
		int nAdded=0;
		int limit = Integer.MAX_VALUE;
		if(order >= 0){
			limit =  order;
		}
		while(iterator.hasNext() & nAdded<limit){
	        RankedNode rn =  iterator.next();
	        RankedNode added = tree.addNode(parent, rn);
	        if (added!=null){
	        	nAdded++;
	        }
		}
		
		//trim the tree as we go
		tree.trimChildren(parent);
		
		iterator = sortedRelatedNodes.iterator();
		while(iterator.hasNext()){
			parent =  iterator.next();
			getRecursive( parent, tree, depth+1, visited);
		}
	}
	
	public static SortedSet<RankedNode>  getTopRelatedNodes(Node node, int n){
		
		if (n<0) return getSortedRelatedNodes(node);
		
		SortedSet<RankedNode> rankedNodes = new TreeSet<RankedNode>(Collections.reverseOrder());
		
		for ( Relationship relationship : node.getRelationships(
                RelTypes.IS_RELATED_TO, Direction.OUTGOING ) )
        {
            Node related = relationship.getEndNode();
            long relatedId = related.getId();
            float rank =  getRelevance(relationship);
            String term = term(related);
            RankedNode rn = new RankedNode(term, relatedId, rank);
            rankedNodes.add(rn);
        }
		
		int diff =  rankedNodes.size() - n;
		if (diff > 0){
			for (int i=1;i<=diff;i++){
				rankedNodes.remove(rankedNodes.last());
			}
		}
		
		return rankedNodes;
	}
	
	

	
	public static float getRelevance(Relationship r){
		return Float.parseFloat(r.getProperty(C).toString());
	}

    public static void shutdown() {
    	
    	if (inserter != null){
    		inserter.shutdown();
    		inserter =  null;
    	}
    	
    	if (graphDb != null){
    		graphDb.shutdown();
    		graphDb = null;
    	}
    }

    private static Node createAndIndexTerm( final String term ){
    	
        Node node = getOrCreateUniqueNode( term );
        String t =  term(node);
        nodeIndex.add( node, TERM, t );  
                
        return node;
    }
    
    private static Node getOrCreateUniqueNode( String term){
    	
        UniqueFactory<Node> factory = new UniqueFactory.UniqueNodeFactory( nodeIndex ) {
            @Override
            protected void initialize( Node created, Map<String, Object> properties )
            {
                created.setProperty( TERM, properties.get( TERM ) );
            }
        };
        
        return factory.getOrCreate( TERM, term );
        
    }
    
    private static String toString(Node n){
    	if (n ==  null){
    		return "";
    	}
    	long id = n.getId();

    	StringBuffer sb = new StringBuffer();
    	sb.append("Node ").append(id).append(" => ");
    	
    	Iterable<String> properties  = n.getPropertyKeys();
    	
    	for(String property: properties){
    		String value = n.getProperty(property).toString();
    		sb.append("  ").append(property).append(" = ").append(value);
    	}
    	return sb.toString();
    }
    
    private static String toString(Relationship r){
    	long id =r.getId();
    	Node s = r.getStartNode();
    	Node e = r.getEndNode();
    	StringBuffer sb = new StringBuffer();

    	sb.append("Relationship ").append(id).append(": ").append(toString(s)).append(" => ").append(toString(e)).append("\n");
    	Iterable<String> properties  = r.getPropertyKeys();
    	
    	for(String property: properties){
    		String value = r.getProperty(property).toString();
    		sb.append("  ").append(property).append(" = ").append(value);
    	}
    	
    	return sb.toString();
    }
    
    public static void listGraph(PrintStream ps){
		Iterable<Node> nodes = Neo4jGraphEngine.getAllNodes();
		for(Node n: nodes) {
			ps.println(toString(n));
		}
		
		Iterable<Relationship> relationships = Neo4jGraphEngine.getAllRelationships();
		for(Relationship r: relationships) {
			ps.println(toString(r));
		}
    }
    
    
    private static String term(Node n){
    	Object property = null;
    	try{	
    		property = n.getProperty(TERM);
    	} catch( java.nio.BufferUnderflowException e){
    		LOGGER.error("Error looking up term in node",e);
    		return "";
    	}
    	if (property == null){
    		return "";
    	}
    	return property.toString(); 
    }
    
    private static void registerShutdownHook(){
    	
        // Registers a shutdown hook for the Neo4j and index service instances
        // so that it shuts down nicely when the VM exits (even if you
        // "Ctrl-C" the running example before it's completed)
        Runtime.getRuntime().addShutdownHook( new Thread()
        {
            @Override
            public void run()
            {
                shutdown();
            }
        } );
    }  
    
    public static void commitBatchInsert(){
    	if (batchNodeIndex != null){
    		batchNodeIndex.flush();
    		batchNodeIndex = null;
    	}
    	
    	if (indexProvider != null){
    		indexProvider.shutdown();
    		indexProvider =  null;
    	}
    	if (inserter != null){
    		inserter.shutdown();
    		inserter = null;
    	}
    	shutdown();
    }

	public static int getnInsertedNodes() {
		return nInsertedNodes;
	}
}