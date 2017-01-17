package org.jtang.distance.ged.editpath.editoperation;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.jtang.distance.ged.graph.DecoratedNode;
import org.jtang.distance.ged.processor.CostContainer;

/**
 * Edge deletion edit operation.
 * 
 * @author Roman Tekhov
 */
public class EdgeDeletion extends EditOperation {
	
	private final List<DecoratedNode> deletedEdgeNodes = new ArrayList<DecoratedNode>(2);

	
	public EdgeDeletion(DecoratedNode first, DecoratedNode second, 
				CostContainer costContainer) {
		super(costContainer);
		
		deletedEdgeNodes.add(first);
		deletedEdgeNodes.add(second);
	}
	
		
	public List<DecoratedNode> getDeletedEdgeNodes() {
		return deletedEdgeNodes;
	}
	
	
	@Override
	protected BigDecimal doGetCost(CostContainer costContainer) {
		return costContainer.getEdgeDeletionCost();
	}
	
	
	@Override
	public String toString() {
		return new StringBuilder("Deletion of edge ").
			append(deletedEdgeNodes).
			append(" (").append(getCost()).append(")").
			toString();
	}
}
