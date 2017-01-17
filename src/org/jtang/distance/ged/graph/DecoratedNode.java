package org.jtang.distance.ged.graph;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import att.grappa.Node;

/**
 * A decorator wrapping an instance of Grappa {@link Node}.
 * 
 * @author Roman Tekhov
 */
public class DecoratedNode implements Comparable<DecoratedNode> {

	private String label;
	
	private final Set<DecoratedNode> adjacent = new HashSet<DecoratedNode>();
	
	private final Set<DecoratedNode> accessedBy = new HashSet<DecoratedNode>();
	
	private Node grappaNode;
	
	
	public DecoratedNode(Node grappaNode) {
		this.grappaNode = grappaNode;
		
		label = grappaNode.getName();
	}

	
	public String getLabel() {
		return label;
	}
	/**
	 *  get the next node
	 * @return
	 */
	public Set<DecoratedNode> getAdjacent() {
		return adjacent;
	}
	
	public void addAdjacent(DecoratedNode node) {
		adjacent.add(node);
	}
	
	public boolean isAdjacent(DecoratedNode node) {
		return adjacent.contains(node);
	}
	
	
	/**
	 *  get the previous node
	 * @return
	 */
	public Set<DecoratedNode> getAccessedBy() {
		return accessedBy;
	}
	
	public void addAccessedBy(DecoratedNode node) {
		accessedBy.add(node);
	}
	
	public boolean isAccessedBy(DecoratedNode node) {
		return accessedBy.contains(node);
	}	

	
	public Node getGrappaNode() {
		return grappaNode;
	}
	
	
	@Override
	public String toString() {
		return label;
	}


	@Override
	public int compareTo(DecoratedNode node) {
		return new CompareToBuilder().
			append(getGrappaNode().getGraph(), 
					node.getGrappaNode().getGraph()).
			append(getLabel(), node.getLabel()).
			toComparison();
	}


	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof DecoratedNode)) {
			return false;
		}
		
		DecoratedNode node = (DecoratedNode)obj;
		
		return new EqualsBuilder().
			append(getGrappaNode().getGraph(), 
				node.getGrappaNode().getGraph()).
			append(getLabel(), node.getLabel()).
			isEquals();
	}


	@Override
	public int hashCode() {
		return new HashCodeBuilder().
			append(getGrappaNode().getGraph()).
			append(getLabel()).
			toHashCode();
	}

}
