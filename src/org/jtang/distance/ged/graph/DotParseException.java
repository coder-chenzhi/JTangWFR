package org.jtang.distance.ged.graph;

/**
 * Thrown to indicate that a DOT expression parsing error occured.
 * 
 * @author Roman Tekhov
 */
public class DotParseException extends Exception {

	private static final long serialVersionUID = 1L;
	
	
	public DotParseException(String message) {
		super(message);
	}

}
