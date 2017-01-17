package org.jtang.distance.ged.editpath.test;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.jtang.distance.ged.graph.DecoratedGraph;
import org.jtang.distance.ged.graph.DecoratedNode;
import org.jtang.distance.ged.graph.DotParseException;
import org.jtang.distance.ged.graph.GraphConverter;

public class NodeCountDist {

	public static double getNodeCountDist(DecoratedGraph from, DecoratedGraph to) {
		double sameNodeCount = 0;
		Set<DecoratedNode> nodesinFrom = from.getNodes();
		Set<String> labelsinFrom = new HashSet<String>();
		for (Iterator<DecoratedNode> iterator = nodesinFrom.iterator(); iterator.hasNext();) {
			DecoratedNode decoratedNode = (DecoratedNode) iterator.next();
			labelsinFrom.add(decoratedNode.getLabel());
		}
		Set<DecoratedNode> nodesinTo = to.getNodes();
		for (Iterator<DecoratedNode> iterator = nodesinTo.iterator(); iterator.hasNext();) {
			DecoratedNode decoratedNode = (DecoratedNode) iterator.next();
			if (labelsinFrom.contains(decoratedNode.getLabel())) {
				sameNodeCount++;
			}
		}

//		return from.getNodeNumber()/sameNodeCount;
		return from.getNodeNumber()-sameNodeCount;
	}
 
	/**
	 * @param args
	 * @throws DotParseException 
	 */
	public static void main(String[] args) throws DotParseException {
		// TODO Auto-generated method stub
		String fromDotExpr = "digraph from{\n 1->2;\n 2->3;\n}";
		String[] toDotExpr = new String[15];
		toDotExpr[0] = "digraph from{\n 1->2;\n 2->3;\n}";
		toDotExpr[1] = "digraph from{\n 1->2;\n}";
		toDotExpr[2] = "digraph from{\n 2->3;\n}";
		toDotExpr[3] = "digraph from{\n 3->4;\n}";
		toDotExpr[4] = "digraph from{\n 4->5;\n}";
		toDotExpr[5] = "digraph from{\n 1;\n}";
		toDotExpr[6] = "digraph from{\n 2;\n}";
		toDotExpr[7] = "digraph from{\n 3;\n}";
		toDotExpr[8] = "digraph from{\n 4;\n}";
		toDotExpr[9] = "digraph from{\n 5;\n}";
		toDotExpr[10] = "digraph from{\n 1->2;\n 2->3;\n}";
		toDotExpr[11] = "digraph from{\n 2->3;\n 3->4;\n}";
		toDotExpr[12] = "digraph from{\n 3->4;\n 4->5;\n}";
		toDotExpr[13] = "digraph from{\n 1->2;\n 2->3;\n 3->4;\n}";
		toDotExpr[14] = "digraph from{\n 2->3;\n 3->4;\n 4->5;\n}";
		DecoratedGraph from = GraphConverter.parse(fromDotExpr);
		for (int i = 0; i < toDotExpr.length; i++) {
			DecoratedGraph to = GraphConverter.parse(toDotExpr[i]);
			double count = getNodeCountDist(from, to);
			System.out.println(count);
		}
	}

}
