package org.jtang.distance.ged.editpath.test;

import java.math.BigDecimal;

import org.jtang.distance.ged.editpath.EditPathFinder;
import org.jtang.distance.ged.graph.DecoratedGraph;
import org.jtang.distance.ged.graph.GraphConverter;
import org.jtang.distance.ged.processor.CostContainer;

/**
 * Test class.
 * 
 * @author Roman Tekhov
 */
public class Test {

	public static void main(String[] args) {
		test(5, 5);
	}

	private static void test(int fromNodeCount, int toNodeCount) {
		try {
			double duration = 0;

			int n = 10;

			// String fromDotExpr = DotGenerator.generate(fromNodeCount,
			// fromNodeCount * 3, 4, "from", true);
			// String toDotExpr = DotGenerator.generate(toNodeCount, toNodeCount
			// * 3, 4, "to", true);

			CostContainer costContainer = new CostContainer();
			Distance distance = new Distance();
			String fromDotExpr = "digraph from{\n 1->2;\n 2->3;\n}";
			DecoratedGraph from = GraphConverter.parse(fromDotExpr);

			String[] toDotExpr = new String[30];
			toDotExpr[0] = "digraph to{\n 1->2;\n 2->3;\n}";
			toDotExpr[1] = "digraph to{\n 1->2;\n}";
			toDotExpr[2] = "digraph to{\n 2->3;\n}";
			toDotExpr[3] = "digraph to{\n 1->3;\n}";
			toDotExpr[4] = "digraph to{\n 1;\n}";
			toDotExpr[5] = "digraph to{\n 2;\n}";
			toDotExpr[6] = "digraph to{\n 3;\n}";
			toDotExpr[7] = "digraph to{\n 4;\n}";
			toDotExpr[8] = "digraph to{\n 1->2;\n 2->4;\n}";
			toDotExpr[9] = "digraph to{\n 1->4;\n 4->3;\n}";
			toDotExpr[10] = "digraph to{\n 4->2;\n 2->3;\n}";
			toDotExpr[11] = "digraph from{\n 2->4;\n}";
			toDotExpr[12] = "digraph from{\n 4->3;\n}";
			toDotExpr[13] = "digraph from{\n 1->3;\n 3->4;\n}";
			toDotExpr[14] = "digraph from{\n 2->3;\n 3->4;\n 4->5;\n}";

			String toDotExpr1 = "digraph from{\n 1£»\n2;\n}";
			String toDotExpr2 = "digraph from{\n 1->2;\n 1£»\n2;\n}";
			String toDotExpr3 = "digraph from{\n 1->2;\n}";
			String toDotExpr4 = "digraph from{\n 1->2;\n 2->3;\n 3->4;\n 1;\n 2;\n 3;\n 4\n}";
			String toDotExpr5 = "digraph from{\n 1->2;\n 2->3;\n 3->4;\n}";
			String toDotExpr6 = "digraph from{\n 1;\n 2;\n 3;\n 4;\n}";
			String[] labelStrings = { "3" };
			for (int i = 0; i < 14; i++) {
				DecoratedGraph to = GraphConverter.parse(toDotExpr[i]);
				BigDecimal graphEditDistance = EditPathFinder.find(from, to,
						costContainer).getCost();
				distance.setGraphEditDistance(graphEditDistance.intValue());

				int positionDistance = PositionDist.getPositionDist(from, to,
						labelStrings);
				distance.setPositionDistance(positionDistance);

				double nodeCountDistance = NodeCountDist.getNodeCountDist(from,
						to);
				
				distance.setNodeCountDistance(nodeCountDistance);
				System.out.println(i+":"+distance.getGraphEditDistance() + ";"+Math.atan(distance.getGraphEditDistance())+"..."
						+ distance.getNodeCountDistance() + ";"
						+ distance.getPositionDistance());

			}
			// DecoratedGraph to = GraphConverter.parse(toDotExpr[10]);
			// BigDecimal graphEditDistance = EditPathFinder.find(from, to,
			// costContainer).getCost();
			// distance.setGraphEditDistance(graphEditDistance.intValue());
			// String[] labels = { "3" };
			// int positionDistance = PositionDist.getPositionDist(from, to,
			// labels);
			// distance.setPositionDistance(positionDistance);
			//
			// double nodeCountDistance = NodeCountDist.getNodeCountDist(from,
			// to);
			// distance.setNodeCountDistance(nodeCountDistance);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
