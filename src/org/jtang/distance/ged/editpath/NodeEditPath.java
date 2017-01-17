package org.jtang.distance.ged.editpath;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.jtang.distance.ged.editpath.editoperation.EditOperation;
import org.jtang.distance.ged.graph.DecoratedNode;

/**
 * An edit path between a source graph node and a destination
 * graph node involving a series of edit operations. The total
 * cost of node edit path is the sum of costs of all its edit
 * operations.
 * 
 * @author Roman Tekhov
 */
public class NodeEditPath {
	
	private DecoratedNode from;
	private DecoratedNode to;
	
	private final List<EditOperation> editOperations = new ArrayList<EditOperation>();
	
	private BigDecimal cost;
	
	
	public DecoratedNode getFrom() {
		return from;
	}

	public void setFrom(DecoratedNode from) {
		this.from = from;
	}


	public DecoratedNode getTo() {
		return to;
	}

	public void setTo(DecoratedNode to) {
		this.to = to;
	}

	
	/**
	 * @return this node edit path's edit operations
	 */
	public List<EditOperation> getEditOperations() {
		return editOperations;
	}
	
	/**
	 * Adds a new edit operation to this path.
	 * 
	 * @param editOperation new edit operation
	 */
	void addEditOperation(EditOperation editOperation) {
		editOperations.add(editOperation);
	}
	
	
	/**
	 * @return overall cost of this node edit path
	 */
	public BigDecimal getCost() {
		if(cost == null) {
			cost = new BigDecimal("0.00");
			
			for(EditOperation editOperation : editOperations) {
				cost = cost.add(editOperation.getCost()).setScale(2);
			}
		}
		
		return cost;
	}
	
	
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		if(from == null && to != null) {
			sb.append("Insertion of node '").
				append(to.getLabel());
		} else if(to == null && from != null) {
			sb.append("Deletion of node '").
			append(from.getLabel());
		} else if(from != null && to != null) {
			sb.append("Substitution of nodes '").
				append(from.getLabel()).
				append("' and '").
				append(to.getLabel());
		}
		
		return sb.append("' (").append(getCost()).append(")").
			toString();
	}

}
