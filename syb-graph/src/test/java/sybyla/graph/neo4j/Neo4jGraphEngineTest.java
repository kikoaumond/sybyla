package sybyla.graph.neo4j;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.neo4j.graphdb.Node;
import org.neo4j.kernel.impl.util.FileUtils;

import sybyla.graph.GraphLoader;
import sybyla.graph.Tree;

public class Neo4jGraphEngineTest {

	@Test
	public void testLoad()  {
		String dbPath = "test/resources/graph.db";
		int n = 1000;
		try{
			Neo4jGraphEngine.setDBPath(dbPath);
			try {
				Neo4jGraphEngine.init();
			} catch (IOException e) {
				fail();
			}
			GraphLoader gl = new Neo4jGraphEngine();
			long start = new Date().getTime();
			int nEdges= 0;
			for (int i=1; i<=n; i++) {
				int j = i+1;
				String node1 =  "node"+i;
				String node2 = "node"+j;
				gl.insert(i-1, node1, i, node2, j);
				nEdges++;
			}
			long stop = new Date().getTime();
			long interval = stop - start;
			System.out.println(Neo4jGraphEngine.getnInsertedNodes() + " nodes inserted in graph DB");
		
			System.out.println("Done inserting "+nEdges+" edges in "+interval+" ms");
    	
			Neo4jGraphEngine.shutdown();
			Neo4jGraphEngine.start();
    	
			for (int i=1; i<=n; i++) {
				String nd = "node"+i;
				Node node = Neo4jGraphEngine.getNode(nd);
				assertNotNull(node);
				long id = node.getId();
				assertTrue(id == i);
        	
			}
		}catch(Throwable t){
			System.out.println(t);
			fail();
			
		} finally{
			try {
				Neo4jGraphEngine.shutdown();
				FileUtils.deleteRecursively( new File( dbPath ) );
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
    }

	@Test
	public void testLoadBatch() throws IOException {
		String dbPath = "test/resources/graph.db";

		try{
		int nElements=100000;
		Neo4jGraphEngine.setDBPath(dbPath);
    	try {
			Neo4jGraphEngine.initBatchMode();
		} catch (IOException e) {
			fail();
		}
    	GraphLoader gl = new Neo4jGraphEngine();
    	long start = new Date().getTime();
    	int nEdges= 0;
    	for (int i=1; i<nElements; i++) {
    		int j = i+1;
    		String node1 =  "node"+i;
    		String node2 = "node"+j;
    		gl.insertBatch(i-1, node1, i, node2, j);
    		nEdges++;
    	}
    	long stop = new Date().getTime();
    	long interval = stop - start;
    	//"FB" and "Ea" have the same hashcode
    	System.out.println("Done inserting "+nEdges+" edges in "+interval+" ms");
    	
    	
		}catch(Throwable t){
			System.out.println(t);
			fail();
		
		} finally{
			try {
				Neo4jGraphEngine.shutdown();
				FileUtils.deleteRecursively( new File( dbPath ) );
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
    }
	
	
	@Test
	public void testQuery() throws IOException, JSONException {
		
		String dbPath ="/mnt/data/current/graph/neo4j";
		Neo4jGraphEngine.setDBPath(dbPath);
		Neo4jGraphEngine.start();
		
		long start = new Date().getTime();
		Tree tree = Neo4jGraphEngine.getRelatedTree("Bill Clinton", -1, 2);
		long end = new Date().getTime();
		long interval = end-start;
		System.out.println("Tree obtained in "+interval+ " ms");

		assertNotNull(tree);
		JSONObject json = tree.toJSON();
		assertNotNull(json);
		String prettyPrint = tree.prettyPrint();
		System.out.println(prettyPrint);
		JSONObject json2 = new JSONObject(prettyPrint);
		assertEquals(json2.toString(),json.toString());
		

		String[] expectedChildren={"George W. Bush",
				 "President of the United States",
				 "White House","Barack Obama",
				 "United States Senate",
				 "Democratic Party",
				 "President",
				 "Ronald Reagan",
				 "Hillary Clinton",
				 "Jimmy Carter",
				 "George H. W. Bush",
				 "Al Gore",
				 "Republican Party",
				 "Juris Doctor",
				 "United States Congress",
				 "Richard Nixon",
				 "John McCain",
				 "CNN",
				 "Washington Post",
				 "John Kerry",
				 "Hillary Rodham Clinton",
				 "J.D.",
				 "Impeachment of Bill Clinton"
				};
		
		String rootName = json.getString(Tree.Node.NAME);
		assertEquals(rootName,"Bill Clinton");
		JSONArray children = json.getJSONArray(Tree.Node.CHILDREN);
		for(int i=0;i<children.length();i++){
			JSONObject child  = children.getJSONObject(i);
			String childName = child.getString(Tree.Node.NAME);
			assertEquals(expectedChildren[i],childName);
		}
		
		start = new Date().getTime();
		tree = Neo4jGraphEngine.getRelatedTree("Bill Clinton", 4, 2,10);
		end = new Date().getTime();
		interval = end-start;
		System.out.println("Tree obtained in "+interval+ " ms");

		assertNotNull(tree);
		json = tree.toJSON();
		assertNotNull(json);
		prettyPrint = tree.prettyPrint();
		System.out.println(prettyPrint);
		json2 = new JSONObject(prettyPrint);
		assertEquals(json2.toString(),json.toString());
		Neo4jGraphEngine.shutdown();

    }
	
	
	@Test
	public void testQuery2() throws IOException, JSONException {
		
		String dbPath ="/mnt/data/current/graph/neo4j";
		Neo4jGraphEngine.setDBPath(dbPath);
		Neo4jGraphEngine.start();
		
		long start = new Date().getTime();
		Tree tree = Neo4jGraphEngine.getRelatedTree("Renato Aragão", -1, 2);
		long end = new Date().getTime();
		long interval = end-start;
		System.out.println("Tree obtained in "+interval+ " ms");

		assertNotNull(tree);
		JSONObject json = tree.toJSON();
		assertNotNull(json);
		String prettyPrint = tree.prettyPrint();
		System.out.println(prettyPrint);
		JSONObject json2 = new JSONObject(prettyPrint);
		assertEquals(json2.toString(),json.toString());
		

		String[] expectedChildren={"Os Trapalhões", "Mussum", "Dedé Santana", "Zacarias"};
		
		String rootName = json.getString(Tree.Node.NAME);
		assertEquals(rootName,"Renato Aragão");
		JSONArray children = json.getJSONArray(Tree.Node.CHILDREN);
		for(int i=0;i<children.length();i++){
			JSONObject child  = children.getJSONObject(i);
			String childName = child.getString(Tree.Node.NAME);
			boolean found=false;
			for (int j=0;j<expectedChildren.length;j++){
				if (childName.contains(expectedChildren[j])){
					found=true;
					break;
				}
			}
			assertTrue(found);
		}
		
		tree = Neo4jGraphEngine.getRelatedTree("Renato Aragão", 4, 2,3);
		end = new Date().getTime();
		interval = end-start;
		System.out.println("Tree obtained in "+interval+ " ms");

		assertNotNull(tree);
		json = tree.toJSON();
		assertNotNull(json);
		prettyPrint = tree.prettyPrint();
		System.out.println(prettyPrint);
		json2 = new JSONObject(prettyPrint);
		assertEquals(json2.toString(),json.toString());
		

		String[] expectedChildren2={"Os Trapalhões", "Mussum", "Zacarias"};
		
		 rootName = json.getString(Tree.Node.NAME);
		assertEquals(rootName,"Renato Aragão");
		children = json.getJSONArray(Tree.Node.CHILDREN);
		for(int i=0;i<children.length();i++){
			JSONObject child  = children.getJSONObject(i);
			JSONArray grandchildren = child.getJSONArray(Tree.Node.CHILDREN);
			int size = grandchildren.length();
			assertTrue((size>=0)&& (size<=4));
			String childName = child.getString(Tree.Node.NAME);
			boolean found=false;
			for (int j=0;j<expectedChildren2.length;j++){
				if (childName.contains(expectedChildren2[j])){
					found=true;
					break;
				}
			}
			assertTrue(found);
		}
		Neo4jGraphEngine.shutdown();

    }
	
	@Test
	public void testQuery3() throws IOException, JSONException {
		
		String dbPath ="/mnt/data/current/graph/neo4j";
		Neo4jGraphEngine.setDBPath(dbPath);
		Neo4jGraphEngine.start();
		
		long start = new Date().getTime();
		Tree tree = Neo4jGraphEngine.getRelatedTree("Rolling Stones", -1, 2);
		long end = new Date().getTime();
		long interval = end-start;
		System.out.println("Tree obtained in "+interval+ " ms");

		assertNotNull(tree);
		JSONObject json = tree.toJSON();
		assertNotNull(json);
		String prettyPrint = tree.prettyPrint();
		System.out.println(prettyPrint);
		JSONObject json2 = new JSONObject(prettyPrint);
		assertEquals(json2.toString(),json.toString());
		

		String[] expectedChildren={"Mick Jagger", "Keith Richards", "Charlie Watts"};
		
		String rootName = json.getString(Tree.Node.NAME);
		assertEquals(rootName,"Rolling Stones");
		JSONArray children = json.getJSONArray(Tree.Node.CHILDREN);
		for(int i=0;i<children.length();i++){
			JSONObject child  = children.getJSONObject(i);
			String childName = child.getString(Tree.Node.NAME);
			for (int j=0;j<expectedChildren.length;j++){
				if (expectedChildren[j]!=null && childName.contains(expectedChildren[j])){
					expectedChildren[j] =  null;
					break;
				}
			}
		}
		for(String expectedChild:expectedChildren){
			assertNull(expectedChild);
		}
		
		tree = Neo4jGraphEngine.getRelatedTree("Rolling Stones", 4, 2,10);
		end = new Date().getTime();
		interval = end-start;
		System.out.println("Tree obtained in "+interval+ " ms");

		assertNotNull(tree);
		json = tree.toJSON();
		assertNotNull(json);
		prettyPrint = tree.prettyPrint();
		System.out.println(prettyPrint);
		json2 = new JSONObject(prettyPrint);
		assertEquals(json2.toString(),json.toString());
		
		rootName = json.getString(Tree.Node.NAME);
		assertEquals(rootName,"Rolling Stones");
		
		children = json.getJSONArray(Tree.Node.CHILDREN);
		assertTrue(children.length()<=10);
		String[] expectedChildren2={"Mick Jagger", "Keith Richards", "Charlie Watts", "Ron Wood", "Bill Wyman", "Mick Taylor"};
		for(int i=0;i<children.length();i++){
			JSONObject child  = children.getJSONObject(i);
			JSONArray grandchildren = child.getJSONArray(Tree.Node.CHILDREN);
			for(int k=0; k<grandchildren.length();k++){
				JSONObject grandchild  = grandchildren.getJSONObject(k);
				String grandchildName = grandchild.getString(Tree.Node.NAME);
				for (int t=0;t<expectedChildren2.length;t++){
					if (expectedChildren2[t]!=null && grandchildName.contains(expectedChildren2[t])){
						expectedChildren2[t]=null;
						break;
					}
				}
			}
			int size = grandchildren.length();
			assertTrue((size>=0)&& (size<=4));
			String childName = child.getString(Tree.Node.NAME);
			for (int j=0;j<expectedChildren2.length;j++){
				if (expectedChildren2[j]!=null && childName.contains(expectedChildren2[j])){
					expectedChildren2[j]=null;
					break;
				}
			}
		}
		for(String expectedChild:expectedChildren2){
			assertNull(expectedChild);
		}
		Neo4jGraphEngine.shutdown();

    }

	@Test
	public void testQuery4() throws IOException, JSONException {
		
		String dbPath ="/mnt/data/current/graph/neo4j";
		Neo4jGraphEngine.setDBPath(dbPath);
		Neo4jGraphEngine.start();
		
		long start = new Date().getTime();
		Tree tree = Neo4jGraphEngine.getRelatedTree("Amon Tobin", 4, 2, 10);
		long end = new Date().getTime();
		long interval = end-start;
		System.out.println("Tree obtained in "+interval+ " ms");
	
		assertNotNull(tree);
		JSONObject json = tree.toJSON();
		assertNotNull(json);
		String prettyPrint = tree.prettyPrint();
		System.out.println(prettyPrint);
		JSONObject json2 = new JSONObject(prettyPrint);
		assertEquals(json2.toString(),json.toString());
				
		String rootName = json.getString(Tree.Node.NAME);
		assertEquals(rootName,"Amon Tobin");
		
		JSONArray children = json.getJSONArray(Tree.Node.CHILDREN);
		assertTrue(children.length()<=10);
		for(int i=0;i<children.length();i++){
			JSONObject child  = children.getJSONObject(i);
			JSONArray grandchildren = child.getJSONArray(Tree.Node.CHILDREN);
			
			int size = grandchildren.length();
			assertTrue((size>=0)&& (size<=4));			
		}
		
		Neo4jGraphEngine.shutdown();
	
	}
}
