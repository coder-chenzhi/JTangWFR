package org.jtang.distance.ged.editpath.editoperation;


import java.math.BigDecimal;

import org.jtang.distance.ged.processor.CostContainer;

/**
 * An edit operation between two graph elements that is 
 * assigned a certain cost.
 * 
 * @author Roman Tekhov
 */
public abstract class EditOperation {
	
	private CostContainer costContainer;
	
	
	public EditOperation(CostContainer costContainer) {
		this.costContainer = costContainer;
	}

	
	/**
	 * @return cost of this edit operation
	 */
	public BigDecimal getCost() {
		return doGetCost(costContainer).setScale(2);
	}

	
	/**
	 * Calculates the actual edit operation cost possibly using
	 * the provided {@link CostContainer} instance.
	 * 
	 * @param costContainer cost container
	 * 
	 * @return calculated cost
	 */
	protected abstract BigDecimal doGetCost(CostContainer costContainer);
	
}
