package org.jtang.distance.ged.editpath;

import java.math.BigDecimal;

/**
 * Thrown to indicate that an acceptance cost limit has be exceeded.
 * 
 * @author Roman Tekhov
 */
public class CostLimitExceededException extends Exception {

	private static final long serialVersionUID = 1L;
	
	private BigDecimal cost;
	
	
	public CostLimitExceededException(BigDecimal cost) {
		this.cost = cost;
	}


	public BigDecimal getCost() {
		return cost;
	}

}
