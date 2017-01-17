package org.jtang.distance.ged.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import att.grappa.Graph;

/**
 * A decorator wrapping an instance of Grappa {@link Graph}.
 * 
 * @author Roman Tekhov
 */
public class DecoratedGraph {
	
	private String name;
	private Set<DecoratedNode> nodes;
	private Graph grappaGraph;
	
	
	public DecoratedGraph(String name, Set<DecoratedNode> nodes, Graph grappaGraph) {
		this.name = name;
		this.nodes = nodes;
		this.grappaGraph = grappaGraph;
	}

	
	public Set<DecoratedNode> getNodes() {
		return nodes;
	}
	
	
	/**
	 * @return total number of nodes in this graph
	 */
	public int getNodeNumber() {
		return nodes.size();
	}
	
	
	/**
	 * Returns the next node relative to the given nodes, i.e. the node
	 * that belongs to this graph but not to the given nodes.
	 * 
	 * If possible returns the node adjacent to some of the given nodes.
	 * If there are no such nodes left returns the next random node of
	 * the graph.
	 * 
	 * @param nodes nodes to use while searching for the next node
	 * 
	 * @return next node
	 */
	public DecoratedNode getNextNode(Collection<DecoratedNode> nodes) {
		if(nodes != null) {
			for(DecoratedNode fromNode : nodes) {
				for(DecoratedNode adjacent : fromNode.getAdjacent()) {
					if(!nodes.contains(adjacent)) {
						return adjacent;
					}
				}
			}
		}		
		
		for(DecoratedNode node : this.nodes) {
			if(nodes == null || !nodes.contains(node)) {
				return node;
			}
		}
		
		return null;
	}
	
	
	/**
	 * Gets all other nodes of this graph.
	 * 
	 * @param nodes nodes to exclude
	 * 
	 * @return the rest nodes
	 */
	public Collection<DecoratedNode> getRestNodes(Collection<DecoratedNode> nodes) {
		List<DecoratedNode> restNodes = new ArrayList<DecoratedNode>();
		
		for(DecoratedNode node : this.nodes) {
			if(!nodes.contains(node)) {
				restNodes.add(node);
			}
		}
		
		return restNodes;
	}


	public Graph getGrappaGraph() {
		return grappaGraph;
	}
	
	
	public boolean isDirected() {
		return grappaGraph.isDirected();
	}
	
	
	@Override
	public String toString() {
		return name + " " + nodes;
	}

}
