package org.jtang.distance.ged.editpath.editoperation;


import java.math.BigDecimal;

import org.jtang.distance.ged.graph.DecoratedNode;
import org.jtang.distance.ged.processor.CostContainer;

/**
 * Node insertion edit operation.
 * 
 * @author Roman Tekhov
 */
public class NodeInsertion extends EditOperation {
	
	private DecoratedNode insertedNode;

		
	public NodeInsertion(DecoratedNode insertedNode, CostContainer costContainer) {
		super(costContainer);
		
		this.insertedNode = insertedNode;
	}

	
	public DecoratedNode getInsertedNode() {
		return insertedNode;
	}

	public void setInsertedNode(DecoratedNode insertedNode) {
		this.insertedNode = insertedNode;
	}


	@Override
	protected BigDecimal doGetCost(CostContainer costContainer) {
		return costContainer.getNodeInsertionCost();
	}
	
	
	@Override
	public String toString() {
		return new StringBuilder("Insertion of node ").
			append(insertedNode.getLabel()).
			append(" (").append(getCost()).append(")").
			toString();
	}
	
}
