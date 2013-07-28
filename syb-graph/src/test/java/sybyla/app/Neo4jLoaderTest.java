package sybyla.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.neo4j.graphdb.Node;

import sybyla.graph.Tree;
import sybyla.graph.neo4j.Neo4jGraphEngine;

public class Neo4jLoaderTest {

	@Test
	public void test() throws IOException, JSONException {
		String graphFilesPath = "src/test/resources/graph/";
		String dbPath = "test/resources/neo4j/graph.db";
		Neo4jLoader.runBatch(graphFilesPath, dbPath);
		Neo4jGraphEngine.shutdown();
		Neo4jGraphEngine.start();
		Neo4jGraphEngine.listGraph(System.out);
		Node starr = Neo4jGraphEngine.getNode("Starr");
		assertNotNull(starr);
		String t =  (String)starr.getProperty(Neo4jGraphEngine.TERM);
		System.out.println(t);
		assertEquals("Kenneth Starr",t);
		

		Node clinton = Neo4jGraphEngine.getNode("Clinton");
		assertNotNull(clinton);
		t =  (String)clinton.getProperty(Neo4jGraphEngine.TERM);
		System.out.println(t);
		assertEquals("Clinton",t);
		
		
		clinton = Neo4jGraphEngine.getNode("bill cLINton");
		assertNotNull(clinton);
		t =  (String)clinton.getProperty(Neo4jGraphEngine.TERM);
		System.out.println(t);
		assertEquals("Bill Clinton",t);
		long start = new Date().getTime();
		Tree tree = Neo4jGraphEngine.getRelatedTree("bill cLINton", -1, 2);
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
				 "White House",
				 "Barack Obama",
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
		tree = Neo4jGraphEngine.getRelatedTree("biLL Clinton", 4, 2, -1);
		end = new Date().getTime();
		interval = end-start;
		System.out.println("Tree obtained in "+interval+ " ms");
		assertNotNull(tree);
		json = tree.toJSON();
		assertNotNull(json);
		prettyPrint = tree.prettyPrint();
		System.out.println(prettyPrint);
		
		Iterable<Node> it = Neo4jGraphEngine.getAllNodes();
		Iterator<Node> i = it.iterator();
		while (i.hasNext()){
			Node n = i.next();
			Long id  = n.getId();
			if (id == 0){
				continue;
			}
			String term =  (String) n.getProperty(Neo4jGraphEngine.TERM);
			System.out.print(term+": \t");
			Iterable<String> it2 = n.getPropertyKeys();
			Iterator<String> i2= it2.iterator();
			while(i2.hasNext()){
				String property = i2.next();
				assertEquals(Neo4jGraphEngine.TERM, property);
				String value = (String) n.getProperty(property);
				System.out.print(property +"="+value+"\t");
			}
			System.out.println();
		}
		
	}

}
