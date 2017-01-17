package org.jtang.distance.ged.graph;


import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jtang.distance.ged.editpath.EditPath;
import org.jtang.distance.ged.editpath.NodeEditPath;

import com.sun.xml.internal.messaging.saaj.packaging.mime.Header;

import att.grappa.Attribute;
import att.grappa.Edge;
import att.grappa.Element;
import att.grappa.Graph;
import att.grappa.GrappaConstants;
import att.grappa.GrappaStyle;
import att.grappa.GrappaSupport;
import att.grappa.Node;
import att.grappa.Parser;
import att.grappa.Subgraph;

/**
 * Component responsible for various graph conversions.
 * 
 * @author Roman Tekhov
 */
public class GraphConverter {
	
	
	/**
	 * Parses the given DOT expression to a {@link DecoratedNode} instance.
	 * Delegates to Grappa {@link Parser} to do the actual conversion and then 
	 * post-processes the obtained Grappa graph to produce a {@link DecoratedNode}.
	 * 
	 * @param dotExpr DOT expression to parse
	 * 
	 * @return parsed graph
	 * 
	 * @throws DotParseException in case of parsing errors
	 */
	@SuppressWarnings("unchecked")
	public static DecoratedGraph parse(String dotExpr) throws DotParseException {
		if(dotExpr == null || "".equals(dotExpr)) {
			throw new DotParseException("Empty DOT input!");
		}
		
		// Parse the DOT expression using Grappa parser.
		
		InputStream inputStream = new ByteArrayInputStream(dotExpr.getBytes());
		
		Parser grappaParser = new Parser(inputStream);
		
		Graph grappaGraph = null;
		try {
			grappaParser.parse();
			grappaGraph = grappaParser.getGraph();
		} catch (Exception e) {
			throw new DotParseException(e.getMessage());
		}		
		
		// Build node adjacency lists and wrap the Grappa elements into decorated elements.
		
		Enumeration<Node> grappaNodes = grappaGraph.nodeElements();
		
		Set<DecoratedNode> nodes = new HashSet<DecoratedNode>();
		Map<Node, DecoratedNode> nodeMapping = new HashMap<Node, DecoratedNode>();
		//将node添加到数据结构里边
		while(grappaNodes.hasMoreElements()) {
			Node grappaNode = grappaNodes.nextElement();
			
			DecoratedNode node = new DecoratedNode(grappaNode);
			
			nodeMapping.put(grappaNode, node);
			nodes.add(node);
		}
		
		if(nodes.isEmpty()) {
			throw new DotParseException("The graph has no nodes!");
		}
		//找到每个节点的“出边”，然后找出邻接节点并添加
		for(DecoratedNode node : nodes) {
			Node grappaNode = node.getGrappaNode();
			
			Enumeration<Edge> grappaEdges = grappaGraph.isDirected() ? 
					grappaNode.outEdgeElements() : grappaNode.edgeElements();
			
			while(grappaEdges.hasMoreElements()) {
				Edge grappaEdge = grappaEdges.nextElement();
				
				Node head = grappaEdge.getHead();
				Node tail = grappaEdge.getTail();
				//若边的头节点与当前节点相等则把边的尾节点做为邻近节点
				Node adjacentGrappaNode = head.equals(grappaNode) ? tail : head;
				
				node.addAdjacent(nodeMapping.get(adjacentGrappaNode));
			}
			
			//有向图解析时只考虑出去的边，所以下面的工作要把进入的边也要考虑
			if(grappaGraph.isDirected()) {
				Enumeration<Edge> grappaInEdges = grappaNode.inEdgeElements();
				
				while(grappaInEdges.hasMoreElements()) {
					Edge grappaInEdge = grappaInEdges.nextElement();
					
					Node head = grappaInEdge.getHead();
					Node tail = grappaInEdge.getTail();
					
					Node adjacentGrappaNode = head.equals(grappaNode) ? tail : head;
					
					node.addAccessedBy(nodeMapping.get(adjacentGrappaNode));
				}
			}
		}
		
		return new DecoratedGraph(grappaGraph.getName(), nodes, grappaGraph);
	}
	
	
	
	/**
	 * Combines the source and destination graphs of the given
	 * edit path to a single combined Grappa {@link Graph}. 
	 * 
	 * Also creates additional edges between substituted nodes of 
	 * the graphs using the information from the edit path.
	 *  
	 * @param editPath edit path to use during the combination process
	 * 
	 * @return a Grappa graph representing the combined result
	 */
	public static Graph combine(EditPath editPath) {		
		Graph from = editPath.getFrom().getGrappaGraph();
		Graph to = editPath.getTo().getGrappaGraph();
			
		Graph combined = new Graph("Cost " + editPath.getCost(), 
				from.isDirected(), false);
		combined.setShowSubgraphLabels(true);
		combined.setAttribute(Attribute.LABEL_ATTR, combined.getName());
		
		// Each input graph represents a subgraph cluster of the combined graph.
		Subgraph fromSubgraph = new Subgraph(combined, "cluster1_" + from.getName());
		Subgraph toSubgraph = new Subgraph(combined, "cluster2_" + to.getName());
		
		// Copy attributes, nodes and edges to the combined graph.
		copyAttributes(from, fromSubgraph);
		copyAttributes(to, toSubgraph);
		
		fromSubgraph.setAttribute(Attribute.LABEL_ATTR, from.getName());
		toSubgraph.setAttribute(Attribute.LABEL_ATTR, to.getName());
		
		Map<Node, Node> nodeMap = new HashMap<Node, Node>();
		
		copyNodes(from, fromSubgraph, nodeMap, 1);
		copyNodes(to, toSubgraph, nodeMap, 2);
				
		copyEdges(from, fromSubgraph, nodeMap);
		copyEdges(to, toSubgraph, nodeMap);
		
		// Create additional styles for inserted and deleted nodes.
		// Create additional edges between substituted nodes.
		for(NodeEditPath nodeEditPath : editPath.getNodeEditPaths()) {
			BigDecimal cost = nodeEditPath.getCost();
			
			DecoratedNode nodeFrom = nodeEditPath.getFrom();
			DecoratedNode nodeTo = nodeEditPath.getTo();
			
			Node oldNodeFrom = nodeFrom == null ? null : nodeFrom.getGrappaNode();
			Node oldNodeTo = nodeTo == null ? null : nodeTo.getGrappaNode();
			
			Node newNodeFrom = oldNodeFrom == null ? null : nodeMap.get(oldNodeFrom);
			Node newNodeTo = oldNodeTo == null ? null : nodeMap.get(oldNodeTo);
			
			if(newNodeFrom == null) {
				// Make all inserted nodes green.
				configureInsertedOrDeleted(newNodeTo, Color.GREEN, cost);
			} else if(newNodeTo == null) {
				// Make all deleted nodes red.
				configureInsertedOrDeleted(newNodeFrom, Color.RED, cost);
			} else {
				// Make all substituted nodes yellow and create additional  
				// dashed edges between them with substitution cost as label.
				setNodeColor(newNodeFrom, Color.YELLOW);
				setNodeColor(newNodeTo, Color.YELLOW);
				
				Edge edge = new Edge(combined, newNodeFrom, newNodeTo);
				edge.setAttribute(Attribute.LABEL_ATTR, String.valueOf(cost));
				edge.setAttribute(Attribute.STYLE_ATTR, new GrappaStyle(GrappaConstants.EDGE, "dashed"));
				edge.setAttribute("len", "2");
				
				combined.addEdge(edge);
			}
		}
		
		// Add layout information (Grappa doesn't provide any).
		createLayout(combined);
						
		return combined;
	}

	
	@SuppressWarnings("unchecked")
	private static void copyNodes(Graph initialGraph,
			Subgraph subgraph, Map<Node, Node> nodeMap,
			int suffix) {
		
		Enumeration<Node> fromNodes = initialGraph.nodeElements();
		
		while(fromNodes.hasMoreElements()) {
			Node oldNode = fromNodes.nextElement();
			
			Node newNode = new Node(subgraph, oldNode.getName() + "_" + suffix);
			subgraph.addNode(newNode);
			
			copyAttributes(oldNode, newNode);
			
			newNode.setAttribute(Attribute.LABEL_ATTR, oldNode.getName());
			
			nodeMap.put(oldNode, newNode);
		}
	}
	
	
	@SuppressWarnings("unchecked")
	private static void copyEdges(Graph initialGraph,
			Subgraph subgraph, Map<Node, Node> nodeMap) {
		Enumeration<Edge> fromEdges = initialGraph.edgeElements();
		
		while(fromEdges.hasMoreElements()) {
			Edge oldEdge = fromEdges.nextElement();
			
			Node oldTail = oldEdge.getTail();
			Node oldHead = oldEdge.getHead();
			
			Node newTail = nodeMap.get(oldTail);
			Node newHead = nodeMap.get(oldHead);
			
			Edge newEdge = new Edge(subgraph, newTail, newHead);
			subgraph.addEdge(newEdge);
			
			copyAttributes(oldEdge, newEdge);
		}
	}

	
	@SuppressWarnings("unchecked")
	private static void copyAttributes(Element from, Element to) {
		Enumeration<Attribute> attributes = from.getAttributePairs();
		
		while(attributes.hasMoreElements()) {
			Attribute attribute = attributes.nextElement();
			
			to.setAttribute(attribute);
		}
	}
	
	
	private static void setNodeColor(Node node, Color color) {
		node.setAttribute(Attribute.COLOR_ATTR, color);
	}
	
	
	private static void configureInsertedOrDeleted(Node node, Color color, BigDecimal cost) {
		setNodeColor(node, color);
		
		// Add insertion or deletion cost to node's label.
		node.setAttribute(Attribute.LABEL_ATTR, 
				node.getAttributeValue(Attribute.LABEL_ATTR).toString() + 
					"(" + cost + ")");
	}
	
	
	private static void createLayout(Graph grappaGraph) {
		// Use an external program to produce layout information.
		// For directed graphs use "dot", for undirected graphs use "neato".
		
		ProcessBuilder pb;
		
		if(grappaGraph.isDirected()) {
			pb = new ProcessBuilder("dot", "-Tdot");
		} else {
			pb = new ProcessBuilder("neato", "-Goverlap=false", "-Gsplines=true", 
					"-Gsep=.1", "-Tdot");
		}
				
		Process dotProcess;
		try {
			dotProcess = pb.start();
			
			GrappaSupport.filterGraph(grappaGraph, dotProcess);
			
			dotProcess.destroy();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
