package org.jtang.distance.ged.editpath.editoperation;


import java.math.BigDecimal;

import org.jtang.distance.ged.graph.DecoratedNode;
import org.jtang.distance.ged.processor.CostContainer;
import org.jtang.distance.ged.processor.StringEditDistance;

/**
 * Node substitution edit operation.
 * 
 * @author Roman Tekhov
 */
public class NodeSubstitution extends EditOperation {
	
	private DecoratedNode from;
	
	private DecoratedNode to;
	
	
	public NodeSubstitution(DecoratedNode from, DecoratedNode to, CostContainer costContainer) {
		super(costContainer);
		
		this.from = from;
		this.to = to;
	}

	
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
	
	
	@Override
	protected BigDecimal doGetCost(CostContainer costContainer) {
		/*
		 * Node substitution cost is calculated by computing the 
		 * similarity coefficient of node labels and multiplying it
		 * with the maximum predefined cost.
		 */		
		BigDecimal coefficient = StringEditDistance.calculateCoefficient(from.getLabel(), to.getLabel());
		
		return costContainer.getNodeSubstitutionCost().multiply(coefficient);
	}
	
	
	@Override
	public String toString() {
		return new StringBuilder("Substitution of node ").
			append(from.getLabel()).
			append(" with node ").append(to.getLabel()).
			append(" (").append(getCost()).append(")").
			toString();
	}

}
