package sybyla.graph;

import static org.junit.Assert.*;

import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import sybyla.graph.Tree.Node;

public class TreeTest {

	@Test
	public void test() throws JSONException {
		RankedNode root = new RankedNode("Mick Jagger",0);
		
		long id  = root.getNodeId();
		assertTrue(id  == 0);
		
		String term  = root.getTerm();
		assertEquals("Mick Jagger", term);
		
		float rank = root.getRank();
		assertTrue(rank ==  Float.MAX_VALUE);
		
		Tree tree = new Tree(root);
		Tree.Node rootNode  = tree.getRoot();
		assertNotNull(rootNode);
		RankedNode  rn = rootNode.node;
		assertEquals(root, rn);
		
		TreeSet<Tree.Node> related  = rootNode.related;
		assertNull(related);
		
		JSONObject expected = new JSONObject();
		expected.put(Node.ID, id);
		expected.put(Node.NAME, "Mick Jagger");
		JSONObject data= new JSONObject();
		data.put(Node.RELEVANCE, 1f);
		expected.put(Node.DATA, data);
		JSONArray children = new JSONArray();
		expected.put(Node.CHILDREN,children);
		
		JSONObject json =  tree.toJSON();
		assertEquals(expected.toString(),json.toString());
		RankedNode rn1 =  new RankedNode("Jagger",1,100);
		RankedNode added = tree.addNode(root, rn1);
		assertNull(added);
		assertEquals(expected.toString(),json.toString());

		RankedNode rn2 =  new RankedNode("Jagger/Richards",2,100);
		added = tree.addNode(root, rn2);
		assertNotNull(added);
		String expectedStr = "{\"id\":0," +
							   "\"name\":\"Mick Jagger\"," +
							   "\"data\":{\"relevance\":1}," +
							   "\"children\":[{\"id\":1," +
							   				  "\"name\":\"Jagger/Richards\"," +
							   				  "\"data\":{\"relevance\":1}," +
							   				  "\"children\":[]}]}";
		json = tree.toJSON();
		assertEquals(expectedStr,json.toString());
		
		RankedNode rn3 =  new RankedNode("Keith Richards",3,100);
		added = tree.addNode(root, rn3);
		assertNotNull(added);
		expectedStr = "{\"id\":0," +
						"\"name\":\"Mick Jagger\"," +
						"\"data\":{\"relevance\":1}," +
						"\"children\":[{\"id\":1," +
							   		   "\"name\":\"Jagger/Richards\"," +
							   		   "\"data\":{\"relevance\":0.5}," +
							   		   "\"children\":[]}," +
							   		   		"{\"id\":2," +
							   		   		"\"name\":\"Keith Richards\"," +
							   		   		"\"data\":{\"relevance\":0.5}," +
							   		   		"\"children\":[]}]}";
		json = tree.toJSON();
		assertEquals(expectedStr,json.toString());

		
		
	}
	


}
