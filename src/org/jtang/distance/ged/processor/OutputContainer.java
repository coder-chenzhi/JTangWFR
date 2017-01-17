package org.jtang.distance.ged.processor;

import org.jtang.distance.ged.editpath.EditPath;

import att.grappa.Graph;

/**
 * Output data container. Output data includes the edit path
 * and the combined Grappa graph.
 * 
 * @author Roman Tekhov
 */
public class OutputContainer {
	
	private Graph graph;
	private EditPath editPath;
	
	
	public OutputContainer(Graph graph, EditPath editPath) {
		this.graph = graph;
		this.editPath = editPath;
	}

	
	public Graph getGraph() {
		return graph;
	}
	
	public EditPath getEditPath() {
		return editPath;
	}
	
}
