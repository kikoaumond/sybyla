package sybyla.graph;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import sybyla.jaxb.ObjectFactory;
import sybyla.jaxb.TreeResult;

public class Tree {
	public static final Logger LOGGER =  Logger.getLogger(Tree.class);
		
	private Node root;
	private Map<RankedNode, Node> allNodes =  new HashMap<RankedNode, Node>();
	private Map<String, Node> synonyms =  new HashMap<String, Node>();

	private int order = -1;
	private int maxDepth = -1;
	private int firstDegreeOrder=Integer.MAX_VALUE;
	
	public Tree(RankedNode root){
		Node r =  new Node(root, 0);
		this.root = r;
		allNodes.put(root, r);
		root.setRelevance(1);
	}
	
	public Tree(RankedNode root, int order){
		
		this(root);
		this.order = order;
	}
	
	public Tree(RankedNode root, int order, int depth){
		
		this(root, order);
		this.maxDepth = depth;
	}
	
	public Tree(RankedNode root, int order, int depth, int firstDegreeOrder){
		
		this(root, order, depth);
		this.firstDegreeOrder = firstDegreeOrder;
	}
	
	public Node getRoot(){
		return root;
	}
	
	public RankedNode getRankedNode(RankedNode node){
		
		Node n =  allNodes.get(node);
		
		if (n ==  null){
			return null;
		}
		
		RankedNode t = n.node;
		return t;
	}
	
	public boolean contains(RankedNode rn){
		boolean b =  allNodes.containsKey(rn);
		return b;
	}
	
	public Node getNode(RankedNode rankedNode){
		
		Node n = allNodes.get(rankedNode);
		return n;
	}
	
	public RankedNode addNode(RankedNode parent, RankedNode node){
		
		Node n = getNode(parent);
		
		if (n ==  null){
			String term = parent.getTerm();
			n = synonyms.get(term);
		}
		
		if(n==null){
			return null;
		}
		
		if (n.depth == maxDepth){
			return null;
		}
		
		String term = node.getTerm();
		boolean alreadyThere =  isCanonicalForm(term);
		if (alreadyThere){
			return null;
		}
		
		
		
		int id= allNodes.size();
		Node c =  new Node(node, id);
			
		if (n.related == null){
			n.related =  new TreeSet<Node>(Collections.reverseOrder());
		}
			
		TreeSet<Node> t = n.related;
			
		if (t.contains(c)){
			return null;
		}	
			
		t.add(c);
		c.depth = n.depth+1;
		allNodes.put(node, c);
		return node;					
	}
	
	public Set<RankedNode> trim(){
		Set<RankedNode> removedNodes =  new HashSet<RankedNode>();
		boolean recursive=true;
		trim(root, removedNodes, recursive);
		return removedNodes;
	}
	
	public int getNodeOrder(RankedNode parent){
		Node n = getNode(parent);
		return getNodeOrder(n);
	}
	
	public int getNodeOrder(Node n){
		int nodeOrder=order;
		if (n.depth==0){
			nodeOrder=firstDegreeOrder;
			if(nodeOrder == Integer.MAX_VALUE){
				nodeOrder = order;
			}
		} else{
			nodeOrder=order;
		}
		return nodeOrder;
	}
	
	public void trimChildren(RankedNode parent){
		Node n = getNode(parent);
		
		if (n ==  null || n.related == null){
			return;
		}
		
		int nodeOrder=getNodeOrder(n);
		
		if (nodeOrder>0){
			while (n.related.size() > nodeOrder){
				RankedNode r = n.related.last().node;
				allNodes.remove(r);
				n.related.remove(n.related.last());
			}
		}
	}
	
	private void trim(Node parent, Set<RankedNode> removedNodes, boolean recursive){
		
		if (parent.depth==0 ){
			if(firstDegreeOrder > 0  &&  root.related != null){
		
				while (root.related.size() > firstDegreeOrder){
					RankedNode r = root.related.last().node;
					removedNodes.add(r);
					allNodes.remove(r);
					root.related.remove(root.related.last());
				}
			}
		} else {
			if (order > 0 & parent.related != null){
		
				while (parent.related.size() > order){
					RankedNode r = parent.related.last().node;
					removedNodes.add(r);
					allNodes.remove(r);
					parent.related.remove(parent.related.last());
				}
			
			}
		}	
		if (recursive){
			if (parent.related !=null){
				for (Node child: parent.related){
					trim(child, removedNodes,recursive);
				}
			}
		}
	}
	
	private boolean isCanonicalForm(String term){
		String t= term.toLowerCase();
		String rootTerm =  root.node.getTerm().toLowerCase();
		String[] tt = t.split("\\s");
		String[] rr = rootTerm.split("\\s");
		int lengthDiff = Math.abs(tt.length-rr.length);
		
		if (lengthDiff ==1){
			if (t.contains(" "+rootTerm) || t.contains(rootTerm+" (")){
				synonyms.put(term, root);
				return true;
			}
		}
		
		if (lengthDiff <= 2){
			if (rootTerm.contains(" "+t) || rootTerm.equals(t) ){
				synonyms.put(term, root);
				return true;
			}
		}
		
		return canonicalize(root, term);
	}
	
	public boolean canonicalize(Node parent, String term){
				
		TreeSet<Node> children =  parent.related;
		
		if (children ==  null){
			return false;		
		}
		
		Iterator<Node> iterator = children.iterator();
		while (iterator.hasNext()){
			Node n = iterator.next();
			
			RankedNode rn  = n.node;
			String childTerm  = rn.getTerm();
			String canonical = getCanonicalTerm(childTerm, term);
			
			if(canonical != null){
				if (canonical.equals(term)){
					synonyms.put(childTerm, n);
					rn.setTerm(term);
					return true;
				} else if (canonical.equals(childTerm)){
					synonyms.put(term, n);
					return true;
				}
			} 
			
		}
		
		//bread-first search
		iterator = children.iterator();
		while (iterator.hasNext()){
			Node n = iterator.next();
			if (canonicalize(n,term)){
					return true;
			}
		}
		
		
		return false;
	}
	
	public static String getCanonicalTerm(String master, String term){
		

		
		String[] tt =term.split("\\s");
		String[] mm= master.split("\\s");
		int lengthDiff =  Math.abs(mm.length - tt.length);
		if (lengthDiff > 2){
			return null;
		}
		
		String t = term.toLowerCase();
		String m = master.toLowerCase();
		
		
		if (lengthDiff ==1){
			if (t.contains(" "+m) && tt[0].length()<=3){
				return master;
			}
		}
		
		if (t.contains(m+" (")){
			return master;
		}
		
		if (m.contains(t+" (")){
			return term;
		}
		
		if (m.equals(t)){
			return master;
		}
		if (t.contains(m+" ") ){
			return term;
		}
		
		if (m.contains(" "+t) || m.contains(t+" ") ){
			return master;
		}
			
		return null;
	}
	
	public int size(){
		return allNodes.size();
	}
	
	public JSONObject toJSON(){
		JSONObject json =  root.toJSON();
		return json;
	}
	
	public TreeResult toTreeResult(){
		
		ObjectFactory o = new ObjectFactory();
		TreeResult rootNode =  root.toTreeResult(o);
		return rootNode;
	}
	
	
	public String prettyPrint(){
		return root.prettyPrint();
	}
	
	public static class Node implements Comparable<Node>{
		 public static final String ID="id";
		 public static final String NAME="name";
		 public static final String RELEVANCE="relevance";
		 public static final String RELATION="relation";
		 public static final String DATA="data";
		 public static final String CHILDREN="children";
		 
		 RankedNode node;
		 int id;
		 TreeSet<Node> related;
		 int depth=0;
		
		public Node(RankedNode node, int id){
			this.node = node;
			this.id = id;
		}
		
		public int getNChildren(){
			if (related == null) {
				return 0;
			}
			return related.size();
		}
		
		private void computeRelevances() {
			if (related  ==  null){
				return;
			}
			
			float total = 0;

			for(Node n: related){
				RankedNode rn = n.node;
				total += rn.getRank();
			}
			
			for(Node n: related){
				RankedNode rn = n.node;
				rn.setRelevance(rn.getRank()/total);
			}
		}
		
		public TreeResult toTreeResult(ObjectFactory o){
			computeRelevances();
			if (o==null){
				o = new ObjectFactory();
			}
			TreeResult n = o.createTreeResult();
			n.setId(id);
			n.setD(depth);
			RankedNode rn = node;
			n.setName(rn.getTerm());
			sybyla.jaxb.Data d = o.createData();
			d.setRelevance(new Float(new DecimalFormat("#.##").format(rn.getRelevance())));
			//d.setRelation("");
			n.setData(d);
			if (related != null){
				for(Node child: related){
					TreeResult childNode = child.toTreeResult(o);
					n.getChildren().add(childNode);
				}
			}
			return n;
		}
		
		public JSONObject toJSON(){
			computeRelevances();
			JSONObject json= new JSONObject();
			
			try {
				RankedNode rn = node;
				json.put(ID, id);
				json.put(NAME, rn.getTerm());
				JSONObject data= new JSONObject();
				data.put(RELEVANCE, new Float(new DecimalFormat("#.##").format(rn.getRelevance())));
				//data.put(RELATION, "");
				json.put(DATA, data);
				JSONArray children = new JSONArray();
				if (related !=null){
					for(Node n: related){
						JSONObject c =  n.toJSON();
						children.put(c);
					}
				}
				json.put(CHILDREN, children);
					
			} catch (JSONException e) {
				LOGGER.error("Error generating JSON",e);
			}
			return json;
		}
		@Override
		public boolean equals(Object o){
			if (o instanceof Node){
				return(node.equals(((Node) o).node));
			}
			return false;
		}
		@Override
		public int compareTo(Node o) {
			
			int rn=0;
			if (this.node.rank > o.node.rank){
				rn= 1;
			} else if (this.node.rank < o.node.rank){
				rn= -1;
			} 
			
			if (rn == 0){
				if (this.id > o.id){
					return -1;
				} else if (this.id < o.id){
					return 1;
				} 
				return 0;
			}
			
			return rn;
		}
		
		public String toString(){
			return id+": "+node.toString();
		}
		
		public String prettyPrint(){
			return prettyPrint(0);
		}
		
		public String prettyPrint(int indent){
			computeRelevances();
			
			StringBuffer sb =  new StringBuffer();
			StringBuffer tab =  new StringBuffer();
			String spaces = "            ";
			for(int i=1;i<=indent;i++){
				tab.append(spaces);
			}
			
			sb.append("{\"").append(ID).append("\":").append(id).append(",\"").append(NAME).
				append("\":\"").append(node.getTerm()).append("\",\"").append(DATA).append("\":{\"").
				append(RELEVANCE).append("\":").append(new DecimalFormat("#.##").format(node.getRelevance()))
				.append("},");
			String comma="";
			if (related != null && related.size() > 0){
				sb.append("\n").append(tab).append("\"").append(CHILDREN).append("\":[");
								
				for(Node node:related){
					String n = node.prettyPrint(indent+1);
					sb.append(comma).append(n);
					comma=",\n"+tab.toString()+spaces;
				}
				indent--;
				sb.append("]}");
			} else{
				sb.append("\"").append(CHILDREN).append("\":[]}");
			}
			return sb.toString();
		}

		public SortedSet<Node> getRelated() {
			return related;
		}
	}
	
	

	public int getMaxDepth() {
		return maxDepth;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public void setMaxDepth(int maxDepth) {
		this.maxDepth = maxDepth;
	}
	
	public String toString(){
		return prettyPrint();
	}
	
}
