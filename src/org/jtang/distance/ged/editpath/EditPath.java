package org.jtang.distance.ged.editpath;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.TreeBidiMap;
import org.jtang.distance.ged.graph.DecoratedGraph;
import org.jtang.distance.ged.graph.DecoratedNode;

/**
 * A partial or complete mapping between two graphs.
 * Wraps a collection of individual {@link NodeEditPath}
 * instances. The total cost of the edit path is the sum 
 * of all node edit path costs.
 * 
 * @author Roman Tekhov
 */
public class EditPath implements Comparable<EditPath> {
		
	private DecoratedGraph from;
	private DecoratedGraph to;
	
	private final List<NodeEditPath> nodeEditPaths = new ArrayList<NodeEditPath>();
	
	private BigDecimal cost;
	
	private boolean complete;
		
	private final BidiMap nodeMap = new TreeBidiMap();;
	
	
	EditPath(DecoratedGraph from, DecoratedGraph to) {
		this.from = from;
		this.to = to;
	}
	
	
	/**
	 * @return all individual node edit paths
	 */
	public List<NodeEditPath> getNodeEditPaths() {
		return nodeEditPaths;
	}
	
	/**
	 * Adds a new individual node edit path to this edit path
	 * @param nodeEditPath
	 */
	void addNodeEditPath(NodeEditPath nodeEditPath) {
		nodeEditPaths.add(nodeEditPath);
		
		if(nodeEditPath.getFrom() != null && nodeEditPath.getTo() != null) {
			nodeMap.put(nodeEditPath.getFrom(), nodeEditPath.getTo());
		}
	}
	
	
	/**
	 * @return overall cost of this edit path
	 */
	public BigDecimal getCost() {
		if(cost == null) {
			cost = new BigDecimal("0.00");
			
			for(NodeEditPath nodeEditPath : nodeEditPaths) {
				cost = cost.add(nodeEditPath.getCost()).setScale(2);
			}
		}
		
		return cost;
	}
	
	
	public int compareTo(EditPath editPath) {
		return getCost().compareTo(editPath.getCost());
	}
	
	
	/**
	 * @return a boolean value indicating whether this edit path is complete
	 * 			or not (i.e. it contains mappings for all nodes)
	 */
	boolean isComplete() {
		return complete;
	}

	void setComplete(boolean complete) {
		this.complete = complete;
	}
	
	
	/**
	 * Creates and returns a copy of this edit path which
	 * contains the same set of individual node edit paths.
	 * 
	 * @return copy of this edit path
	 */
	@SuppressWarnings("unchecked")
	EditPath copy() {
		EditPath copy = new EditPath(getFrom(), getTo());
		
		copy.nodeEditPaths.addAll(nodeEditPaths);
		copy.nodeMap.putAll(nodeMap);
		
		return copy;
	}
	
	
	
	/**
	 * Gets a mapped node of the destination graph which
	 * corresponds to the given node of the source graph.
	 * 
	 * @param from source graph node
	 * 
	 * @return corresponding destination graph node or 
	 * 			<code>null</code> if not found
	 */
	public DecoratedNode getMapped(DecoratedNode from) {
		return (DecoratedNode)nodeMap.get(from);
	}
	
	
	/**
	 * Gets a mapped node of the source graph which
	 * corresponds to the given node of the destination 
	 * graph.
	 * 
	 * @param to destination graph node
	 * 
	 * @return corresponding source graph node or 
	 * 			<code>null</code> if not found
	 */
	public DecoratedNode getMappedReverse(DecoratedNode to) {
		return (DecoratedNode)nodeMap.getKey(to);
	}

	
	
	public DecoratedGraph getFrom() {
		return from;
	}

	public DecoratedGraph getTo() {
		return to;
	}


	
	@Override
	public String toString() {		
		return new StringBuilder("Edit path from '").
			append(from.getGrappaGraph().getName()).
			append("' to '").append(to.getGrappaGraph().getName()).
			append("' (").append(getCost()).append(")").
			toString();
	}
		
}
