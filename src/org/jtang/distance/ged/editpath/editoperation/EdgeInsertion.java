package org.jtang.distance.ged.editpath.editoperation;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.jtang.distance.ged.graph.DecoratedNode;
import org.jtang.distance.ged.processor.CostContainer;

/**
 * Edge insertion edit operation.
 * 
 * @author Roman Tekhov
 */
public class EdgeInsertion extends EditOperation {
	
	private final List<DecoratedNode> insertedEdgeNodes = new ArrayList<DecoratedNode>(2);
	
	
	public EdgeInsertion(DecoratedNode first, DecoratedNode second, 
			CostContainer costContainer) {
		super(costContainer);
		
		insertedEdgeNodes.add(first);
		insertedEdgeNodes.add(second);
	}

	
	public List<DecoratedNode> getAddedEdgeNodes() {
		return insertedEdgeNodes;
	}

	
	@Override
	protected BigDecimal doGetCost(CostContainer costContainer) {
		return costContainer.getEdgeInsertionCost();
	}

	
	@Override
	public String toString() {
		return new StringBuilder("Insertion of edge ").
			append(insertedEdgeNodes).
			append(" (").append(getCost()).append(")").
			toString();
	}
}
