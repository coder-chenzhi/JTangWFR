package org.jtang.distance.ged.processor;

import org.jtang.distance.ged.editpath.CostLimitExceededException;
import org.jtang.distance.ged.editpath.EditPath;
import org.jtang.distance.ged.editpath.EditPathFinder;
import org.jtang.distance.ged.graph.DecoratedGraph;
import org.jtang.distance.ged.graph.DotParseException;
import org.jtang.distance.ged.graph.GraphConverter;

import att.grappa.Graph;

/**
 * Component responsible for evaluating the entire edit distance computation
 * procedure together with related pre- and post-activities.
 * 
 * @author Roman Tekhov
 */
public class Processor {
	
	/**
	 * Converts the input data to graph structures, executes the edit distance
	 * finding algorithm, prepares and returns the output.
	 * 
	 * @param inputContainer input data 
	 * @return output data
	 * 
	 * @throws DotParseException in case of DOT to graph parsing errors
	 * @throws CostLimitExceededException in case when an acceptable cost
	 * 			limit has been exceeded
	 */
	public static OutputContainer process(InputContainer inputContainer) 
				throws DotParseException, CostLimitExceededException {
		
		// Parse the input graphs
		DecoratedGraph first = GraphConverter.parse(inputContainer.getFromDot());
		DecoratedGraph second = GraphConverter.parse(inputContainer.getToDot());
		
		if(first.isDirected() && !second.isDirected() || 
				!first.isDirected() && second.isDirected()) {
			throw new DotParseException("Can't compare directed and undirected graphs!");
		}
		
		// Calculate the edit path
		EditPath editPath = EditPathFinder.find(first, second, inputContainer.getCostContainer());
		
		// Form the combined result graph
		Graph graph = GraphConverter.combine(editPath);
		
		// Display result
		return new OutputContainer(graph, editPath);
	}

}
