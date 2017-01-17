package org.jtang.distance.ged.processor;


/**
 * Input data container.
 * 
 * @author Roman Tekhov
 */
public class InputContainer {
	
	private CostContainer costContainer;
	
	private String fromDot;
	private String toDot;
	
	
	public InputContainer(CostContainer costContainer, String fromDot,
			String toDot) {
		this.costContainer = costContainer;
		this.fromDot = fromDot;
		this.toDot = toDot;
	}


	public CostContainer getCostContainer() {
		return costContainer;
	}

	public String getFromDot() {
		return fromDot;
	}

	public String getToDot() {
		return toDot;
	}
	
}
