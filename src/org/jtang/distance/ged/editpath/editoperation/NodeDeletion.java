package org.jtang.distance.ged.editpath.editoperation;


import java.math.BigDecimal;

import org.jtang.distance.ged.graph.DecoratedNode;
import org.jtang.distance.ged.processor.CostContainer;

/**
 * Node deletion edit operation.
 * 
 * @author Roman Tekhov
 */
public class NodeDeletion extends EditOperation {
	
	private DecoratedNode deletedNode;

	
	public NodeDeletion(DecoratedNode deletedNode, CostContainer costContainer) {
		super(costContainer);
		
		this.deletedNode = deletedNode;
	}

	
	public void setDeletedNode(DecoratedNode deletedNode) {
		this.deletedNode = deletedNode;
	}

	public DecoratedNode getDeletedNode() {
		return deletedNode;
	}
	
	
	@Override
	protected BigDecimal doGetCost(CostContainer costContainer) {
		return costContainer.getNodeDeletionCost();
	}


	@Override
	public String toString() {
		return new StringBuilder("Deletion of node ").
			append(deletedNode.getLabel()).
			append(" (").append(getCost()).append(")").
			toString();
	}
	
}
