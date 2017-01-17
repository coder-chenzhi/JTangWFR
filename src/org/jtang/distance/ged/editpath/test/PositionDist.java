package org.jtang.distance.ged.editpath.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.List;

import org.jtang.distance.ged.graph.DecoratedGraph;
import org.jtang.distance.ged.graph.DecoratedNode;
import org.jtang.distance.ged.graph.DotParseException;
import org.jtang.distance.ged.graph.GraphConverter;

public class PositionDist {

	/**
	 * 
	 * @param node
	 * @param fromNodesPosition
	 * @return true if this node's position only has relation with the nodes
	 *         before it
	 */
	private static boolean forwardCalculateNode(DecoratedNode node, Map<DecoratedNode, Integer> fromNodesPosition) {
		if (fromNodesPosition.containsKey(node)) {
			return false;
		}
		Set<DecoratedNode> nextNodes = node.getAdjacent();
		if (!nextNodes.isEmpty()) {
			for (Iterator<DecoratedNode> iterator = nextNodes.iterator(); iterator.hasNext();) {
				DecoratedNode decoratedNode = iterator.next();
				if (forwardCalculateNode(decoratedNode, fromNodesPosition) == false) {
					return false;
				}
			}
		}

		return true;
	}

	private static void processForwardCalculateNode(Set<DecoratedNode> node,
			Map<DecoratedNode, Integer> fromNodesPosition, int previousPosition) {
		// TODO Auto-generated method stub
		previousPosition--;
		for (Iterator<DecoratedNode> iterator = node.iterator(); iterator.hasNext();) {
			DecoratedNode decoratedNode = iterator.next();
			if (previousPosition < 1) {
				previousPosition = 1;
			}
			fromNodesPosition.put(decoratedNode, previousPosition);
			Set<DecoratedNode> nextNodes = decoratedNode.getAdjacent();
			if (!nextNodes.isEmpty()) {
				processForwardCalculateNode(nextNodes, fromNodesPosition, previousPosition);
			}
		}
	}

	private static int processBackwardCalculateNode(DecoratedNode previousNode, DecoratedNode backwardCalculateNode,
			Map<DecoratedNode, Integer> fromNodesPosition) {
		int position = 0;
		position = getPositionByNextNodes(backwardCalculateNode, fromNodesPosition, previousNode);
		fromNodesPosition.put(backwardCalculateNode, position);
		return position;
	}

	/**
	 * get Position By find max of NextNodes's positions
	 * 
	 * @param currentNode
	 * @param fromNodesPosition
	 *            map
	 * @param previousNode
	 * @return this node's position
	 */
	private static int getPositionByNextNodes(DecoratedNode currentNode, Map<DecoratedNode, Integer> fromNodesPosition,
			DecoratedNode previousNode) {
		int position = 0;
		int maxPosition = 0;
		Set<DecoratedNode> forwardCalculateNodes = new HashSet<DecoratedNode>();
		// if this node is a forward Calculate Node
		if (forwardCalculateNode(currentNode, fromNodesPosition)) {
			forwardCalculateNodes.add(currentNode);//

			// position = fromNodesPosition.get(previousNode);
			// fromNodesPosition.put(currentNode, position);
		}
		Set<DecoratedNode> decoratedNodes = currentNode.getAdjacent();

		for (Iterator iterator = decoratedNodes.iterator(); iterator.hasNext();) {

			DecoratedNode decoratedNode = (DecoratedNode) iterator.next();
			if (fromNodesPosition.containsKey(decoratedNode)) {
				position = fromNodesPosition.get(decoratedNode);
				maxPosition = (maxPosition < position) ? position : maxPosition;
			} else {
				position = getPositionByNextNodes(decoratedNode, fromNodesPosition, currentNode);
				maxPosition = (maxPosition < position) ? position : maxPosition;
				fromNodesPosition.put(decoratedNode, position);
			}
		}
		maxPosition++;
		if (!forwardCalculateNodes.isEmpty()) {
			processForwardCalculateNode(forwardCalculateNodes, fromNodesPosition, maxPosition);
		}
		return maxPosition;

	}

	/**
	 * put the position of nodes in the map
	 * 
	 * @param fromNodesPosition
	 * @param currentNode
	 * @param nextNodes
	 */
	private static void getPosition(Map<DecoratedNode, Integer> fromNodesPosition, DecoratedNode currentNode,
			Set<DecoratedNode> nextNodes) {
		int maxPosition = 0;
		int position = 0;
		Set<DecoratedNode> forwardCalculateNodes = new HashSet<DecoratedNode>();
		// Set<DecoratedNode> backwardCalculateNodes = new
		// HashSet<DecoratedNode>();
		for (Iterator<DecoratedNode> iterator = nextNodes.iterator(); iterator.hasNext();) {
			DecoratedNode node = iterator.next();
			if (fromNodesPosition.containsKey(node)) {
				// position += fromNodesPosition.get(node);
				position = fromNodesPosition.get(node);
				maxPosition = (maxPosition >= position) ? maxPosition : position;
			} else {
				if (forwardCalculateNode(node, fromNodesPosition)) {
					forwardCalculateNodes.add(node);// processNoJointNode(node,fromNodesPosition);
				} else {
					position = processBackwardCalculateNode(currentNode, node, fromNodesPosition);
					maxPosition = (maxPosition >= position) ? maxPosition : position;
				}
			}
		}

		maxPosition++;
		fromNodesPosition.put(currentNode, maxPosition);

		if (!forwardCalculateNodes.isEmpty()) {
			processForwardCalculateNode(forwardCalculateNodes, fromNodesPosition, maxPosition);
		}

	}

	/**
	 * tranverse the From graph
	 * 
	 * @param fromNodesPosition
	 *            Map between node and its positon
	 * @param previousNodes
	 *            nodes before the given last node
	 */
	private static void traverseNodes(Map<DecoratedNode, Integer> fromNodesPosition, Set<DecoratedNode> previousNodes) {
		for (Iterator<DecoratedNode> iterator = previousNodes.iterator(); iterator.hasNext();) {
			DecoratedNode currentNode = iterator.next();
			Set<DecoratedNode> nextNodes = currentNode.getAdjacent();
			getPosition(fromNodesPosition, currentNode, nextNodes);

			Set<DecoratedNode> currentPreviousNodes = currentNode.getAccessedBy();
			if (currentPreviousNodes != null) {
				traverseNodes(fromNodesPosition, currentPreviousNodes);
			} else {
				return;
			}

		}
	}

	private static void arrayToSet(String[] lastNodeLabels, Set<String> lastNodeLabelsSet) {
		for (String nodeLabel : lastNodeLabels) {
			lastNodeLabelsSet.add(nodeLabel);
		}
	}

	/**
	 * get the map from From's node label to position in its own graph
	 * 
	 * @param from
	 * @param lastNodeLabels
	 * @return
	 */

	private static Map<String, Integer> ProcessGraphFrom(DecoratedGraph from, String... lastNodeLabels) {
		Map<DecoratedNode, Integer> fromNodesPosition = new HashMap<DecoratedNode, Integer>();
		Set<DecoratedNode> nodesinFrom = from.getNodes();
		Set<String> lastNodeLabelsSet = new HashSet<String>();
		arrayToSet(lastNodeLabels, lastNodeLabelsSet);
		DecoratedNode currentNode = null;
		if (lastNodeLabels.length > 1) {
			for (Iterator<DecoratedNode> iterator = nodesinFrom.iterator(); iterator.hasNext();) {
				currentNode = iterator.next();
				if (lastNodeLabelsSet.contains(currentNode.getLabel())) {
					fromNodesPosition.put(currentNode, 1);
					lastNodeLabelsSet.remove(currentNode.getLabel());
					if (lastNodeLabelsSet.isEmpty()) {
						break;
					}
				}
			}
		} else {
			for (Iterator<DecoratedNode> iterator = nodesinFrom.iterator(); iterator.hasNext();) {
				currentNode = iterator.next();
				if (currentNode.getLabel().equals(lastNodeLabels[0])) {
					// if (!currentNode.getAdjacent().isEmpty()) {
					// //judge if the given node is last node of From. shall we
					// throw Exception?
					// }
					fromNodesPosition.put(currentNode, 1);
					break;
				}
			}

		}
		traverseNodes(fromNodesPosition, currentNode.getAccessedBy());
		// System.out.println(currentNode.toString());
		// convertMap Map<String, Integer> to <DecoratedNode, Integer>
		Map<String, Integer> finalFromNodesPosition = new HashMap<String, Integer>();
		Collection<DecoratedNode> nodes = fromNodesPosition.keySet();
		for (Iterator<DecoratedNode> iterator = nodes.iterator(); iterator.hasNext();) {
			DecoratedNode decoratedNode = iterator.next();
			finalFromNodesPosition.put(decoratedNode.getLabel(), fromNodesPosition.get(decoratedNode));
		}
		return finalFromNodesPosition;
	}

	private static void getPositionOfToNode(Map<DecoratedNode, Integer> fromNodesPosition, DecoratedNode currentNode,
			Set<DecoratedNode> previousNodes) {
		int maxPosition = 0;

		for (Iterator<DecoratedNode> iterator = previousNodes.iterator(); iterator.hasNext();) {
			DecoratedNode node = iterator.next();
			if (fromNodesPosition.containsKey(node)) {
				// position += fromNodesPosition.get(node);
				int position = fromNodesPosition.get(node);
				maxPosition = (maxPosition >= position) ? maxPosition : position;
			} else {
				return;
			}
		}
		maxPosition++;
		fromNodesPosition.put(currentNode, maxPosition);
	}

	/**
	 * 
	 * @param fromNodesPosition
	 * @param previousNodes
	 */
	private static void traverseNodeOfTo(Map<DecoratedNode, Integer> fromNodesPosition, Set<DecoratedNode> previousNodes) {
		for (Iterator<DecoratedNode> iterator = previousNodes.iterator(); iterator.hasNext();) {
			DecoratedNode currentNode = iterator.next();
			Set<DecoratedNode> nextNodes = currentNode.getAdjacent();
			getPositionOfToNode(fromNodesPosition, currentNode, nextNodes);

			Set<DecoratedNode> currentPreviousNodes = currentNode.getAccessedBy();
			if (currentPreviousNodes != null) {
				traverseNodeOfTo(fromNodesPosition, currentPreviousNodes);
			} else {
				return;
			}

		}
	}

	/**
	 * label the nodes of To with its traverse position
	 * 
	 * @param from
	 * @return
	 */
	private static Map<String, Integer> ProcessGraphTo(DecoratedGraph from) {
		Map<DecoratedNode, Integer> fromNodesPosition = new HashMap<DecoratedNode, Integer>();
		Set<DecoratedNode> nodesinFrom = from.getNodes();
		Set<DecoratedNode> currentNodes = new HashSet<DecoratedNode>();
		DecoratedNode currentNode = null;
		for (Iterator<DecoratedNode> iterator = nodesinFrom.iterator(); iterator.hasNext();) {
			currentNode = iterator.next();
			if (currentNode.getAdjacent().isEmpty()) {
				fromNodesPosition.put(currentNode, 1);
				currentNodes.add(currentNode);
			}
		}
		// all last nodes
		for (Iterator<DecoratedNode> iterator = currentNodes.iterator(); iterator.hasNext();) {
			currentNode = iterator.next();
			traverseNodeOfTo(fromNodesPosition, currentNode.getAccessedBy());
		}
		// convertMap Map<String, Integer> to <DecoratedNode, Integer>
		Map<String, Integer> finalFromNodesPosition = new HashMap<String, Integer>();
		Collection<DecoratedNode> nodes = fromNodesPosition.keySet();
		for (Iterator<DecoratedNode> iterator = nodes.iterator(); iterator.hasNext();) {
			DecoratedNode decoratedNode = iterator.next();
			finalFromNodesPosition.put(decoratedNode.getLabel(), fromNodesPosition.get(decoratedNode));
		}
		return finalFromNodesPosition;
	}

	/**
	 * convert Map<String, Integer> to Map<Integer, List<String>>
	 * 
	 * @param positionMap
	 *            map from node to position in To
	 * @return Map<Integer, List<String>> map from position to nodes in From
	 */
	private static Map<Integer, List<String>> convertMap(Map<String, Integer> positionMap) {
		Map<Integer, List<String>> convertedPosition = new HashMap<Integer, List<String>>();
		Set<String> nodeLabelsinTo = positionMap.keySet();
		for (Iterator<String> iterator = nodeLabelsinTo.iterator(); iterator.hasNext();) {
			String label = iterator.next();
			int position = positionMap.get(label);
			if (convertedPosition.containsKey(position)) {
				convertedPosition.get(position).add(label);
			} else {
				List<String> tempList = new ArrayList<String>();
				tempList.add(label);
				convertedPosition.put(position, tempList);
			}
		}
		return convertedPosition;
	}

	/**
	 * get the position of To in From
	 * 
	 * @param convertedPosition
	 *            map from position to nodes
	 * @param finalFromNodesPosition
	 *            map from node label to position in its own graph
	 * @return distance
	 */
	private static int getPositionInFrom(Map<Integer, List<String>> convertedPosition,
			Map<String, Integer> finalFromNodesPosition) {
		Set<Integer> positionSet = convertedPosition.keySet();
		int positionSetSize = positionSet.size();
		for (int i = 1; i <= positionSetSize; i++) {
			List<String> labels = convertedPosition.get(i);
			int minPostion = Integer.MAX_VALUE;
			for (Iterator<String> iterator2 = labels.iterator(); iterator2.hasNext();) {
				String label = (String) iterator2.next();
				if (finalFromNodesPosition.containsKey(label)) {
					// if (finalFromNodesPosition.containsKey(label) &&
					// !(label.equals("26"))) {//modified by tokyo @ 2012-4-19
					int positionInFrom = finalFromNodesPosition.get(label);
					minPostion = (minPostion > positionInFrom) ? positionInFrom : minPostion;
				}
			}
			if (minPostion != Integer.MAX_VALUE) {
				return minPostion;
			}
		}
		return finalFromNodesPosition.size() + 1;// if return the max position
													// value in From better
	}

	/**
	 * get the position of To in From
	 * 
	 * @param convertedPosition
	 *            map from position to nodes
	 * @param finalFromNodesPosition
	 *            map from node label to position in its own graph
	 * @return distance
	 */
	private static int getPositionInFromInMcs(Map<Integer, List<String>> convertedPosition,
			Map<String, Integer> finalFromNodesPosition) {
		Set<Integer> positionSet = convertedPosition.keySet();
		int positionSetSize = positionSet.size();
		for (int i = 1; i <= positionSetSize; i++) {
			List<String> labels = convertedPosition.get(i);
			int minPostion = Integer.MAX_VALUE;
			for (Iterator<String> iterator2 = labels.iterator(); iterator2.hasNext();) {
				String label = (String) iterator2.next();
				 if (finalFromNodesPosition.containsKey(label)) {
//				if (finalFromNodesPosition.containsKey(label) && !(label.equals("26"))) {// modified
																							// by
																							// tokyo
																							// @
																							// 2012-4-19
					int positionInFrom = finalFromNodesPosition.get(label);
					minPostion = (minPostion > positionInFrom) ? positionInFrom : minPostion;
				}
			}
			if (minPostion != Integer.MAX_VALUE) {
				return minPostion;
			}
		}
		return finalFromNodesPosition.size() + 1;// if return the max position
													// value in From better
	}

	/**
	 * get the position distance between From and to
	 * 
	 * @param from
	 *            from graph
	 * @param to
	 *            To graph
	 * @param lastNodeLabels
	 *            the given node label
	 * @return position distance
	 */
	public static int getPositionDist(DecoratedGraph from, DecoratedGraph to, String... lastNodeLabels) {
		int minPositionDist = 0;
		// no given nodes
		if (lastNodeLabels.length < 1) {
			return 0;
		}
		if (!from.equals(fromGraph)) {
			fromGraph = from;
			finalFromNodesPosition = PositionDist.ProcessGraphFrom(from, lastNodeLabels);
		}
		Map<String, Integer> positionMap = ProcessGraphTo(to);
		Map<Integer, List<String>> convertedPosition = convertMap(positionMap);

		minPositionDist = getPositionInFrom(convertedPosition, finalFromNodesPosition);
		return minPositionDist;
	}

	public static int getPositionDistInMcs(DecoratedGraph from, DecoratedGraph to, String... lastNodeLabels) {
		int minPositionDist = 0;
		// no given nodes
		if (lastNodeLabels.length < 1) {
			return 0;
		}
		if (!from.equals(fromGraph)) {
			fromGraph = from;
			finalFromNodesPosition = PositionDist.ProcessGraphFrom(from, lastNodeLabels);
		}
		Map<String, Integer> positionMap = ProcessGraphTo(to);
		Map<Integer, List<String>> convertedPosition = convertMap(positionMap);

		minPositionDist = getPositionInFromInMcs(convertedPosition, finalFromNodesPosition);
		return minPositionDist - 1;
	}

	/**
	 * @param args
	 * @throws DotParseException
	 */
	public static void main(String[] args) throws DotParseException {
		String fromDotExpr = "digraph from{\n 1->2;\n 2->3;\n 2->4;\n 4->5;\n 3->26;\n 5->26;\n}";
		String[] toDotExpr = new String[8];
		toDotExpr[0] = "digraph to{\n 1->2;\n 2->3;\n 2->4;\n 3->26;\n 4->26;\n}";
		toDotExpr[1] = "digraph from{\n 1->2;\n 2->3;\n 2->4;\n 4->5;\n 3->26;\n 5->26;\n}";
		toDotExpr[2] = "digraph to{\n 1->2;\n 2->3;\n 2->4;\n 3->5;\n 4->5;\n}";
		toDotExpr[3] = "digraph to{\n 1->2;\n 2->3;\n 2->4;\n 4->5;\n 3->5;\n 5->6;\n}";
		toDotExpr[4] = "digraph to{\n 1->2;\n 2->3;\n 2->4;\n 3->5;\n 4->6;\n 5->6;\n 6->7;\n}";
		toDotExpr[5] = "digraph to{\n 1->3;\n 2->3;\n 3->4;\n}";
		toDotExpr[6] = "digraph to{\n 1->2;\n 2->4;\n 3->4;\n 4->5;\n}";
		toDotExpr[7] = "digraph to{\n 1->2;\n 2->3;\n 2->9;\n}";
		long start = System.currentTimeMillis();

		DecoratedGraph from = GraphConverter.parse(fromDotExpr);
		// for(int i=0;i<8;i++){
		// DecoratedGraph to = GraphConverter.parse(toDotExpr[i]);
		// PositionDist.getPositionDist(from, to, lastNodeLabels)
		// }
		String[] lastNodeLabels = { "26" };
		DecoratedGraph to = GraphConverter.parse(toDotExpr[0]);
		int distance = PositionDist.getPositionDist(from, to, lastNodeLabels);
		System.out.println(distance);
		distance = PositionDist.getPositionDistInMcs(from, to, lastNodeLabels);
		System.out.println(distance);
		long end = System.currentTimeMillis();
		System.out.println((end - start) / 1000.0);

		// DecoratedGraph from = GraphConverter.parse(fromDotExpr);

		// for (int i = 0; i < toDotExpr.length; i++) {
		// DecoratedGraph to = GraphConverter.parse(toDotExpr[i]);
		// int dist = getPositionDist(from, to, null);
		// System.out.println(dist);
		// }
		// String tempFrom =
		// "digraph to{\n 1->2;\n 1->3;\n 2->4;\n 4->5;\n 3->5;\n 5->6;\n 5->7;\n 6->8;\n 7->9;\n 6->10;\n 10->11;\n 10->12;\n 6->13;\n 6->14;\n 14->15;\n}";
		// String tempTo =
		// "digraph to{\n 10->8;\n 8->5;\n 4->5;\n 3->5;\n 5->6;\n 5->7;\n 6->22;\n 6->20;\n 20->21;\n 7->23;\n}";
		// String tempTo1 =
		// "digraph to{\n 10->8;\n 8->5;\n 4->5;\n 3->5;\n 5->6;\n 5->7;\n 6->22;\n 6->20;\n 20->21;\n 7->23;\n}";
		// String tempFrom =
		// "digraph to{\n 1->2;\n 1->3;\n 2->4;\n 4->5;\n 3->5;\n 5->6;\n 5->7;\n 6->8;\n 7->9;\n 6->10;\n 10->11;\n 10->12;\n 6->13;\n 6->14;\n 14->15;\n}";
		// String tempFrom =
		// "digraph to{ 1->2; 1->3; 2->4; 4->5; 3->5; 5->6; 5->7; 6->8; 7->9; 6->10; 10->11;10->12;6->13;6->14;14->15;}";

		// DecoratedGraph from = GraphConverter.parse(tempFrom);
		// DecoratedGraph to = GraphConverter.parse(toDotExpr[0]);
		// String[] givenLabels = { "15" };
		// String[] givenLabels = { "8"};
		// String[] givenLabels = { "8", "15" };
		// String[] givenLabels = { "8", "12", "13" };
		// System.out.println(PositionDist.ProcessGraphFrom(from, givenLabels));
		// String[] givenLabels = { "5"};
		// System.out.println(getPositionDist(from, to, givenLabels));
		// System.out.println(getPositionDist(from,to, "8"));
		// String fromDotExpr =
		// "digraph from{\n 1->2;\n 2->3;\n 3->4;\n 4->5;\n}";
		// String[] toDotExpr = new String[15];
		// toDotExpr[0] = "digraph from{\n 1->2;\n 2->3;\n 3->4;\n 4->5;\n}";
		// toDotExpr[1] = "digraph from{\n 1->2;\n}";
		// toDotExpr[2] = "digraph from{\n 2->3;\n}";
		// toDotExpr[3] = "digraph from{\n 3->4;\n}";
		// toDotExpr[4] = "digraph from{\n 4->5;\n}";
		// toDotExpr[5] = "digraph from{\n 1;\n}";
		// toDotExpr[6] = "digraph from{\n 2;\n}";
		// toDotExpr[7] = "digraph from{\n 3;\n}";
		// toDotExpr[8] = "digraph from{\n 4;\n}";
		// toDotExpr[9] = "digraph from{\n 5;\n}";
		// toDotExpr[10] = "digraph from{\n 1->2;\n 2->3;\n}";
		// toDotExpr[11] = "digraph from{\n 2->3;\n 3->4;\n}";
		// toDotExpr[12] = "digraph from{\n 3->4;\n 4->5;\n}";
		// toDotExpr[13] = "digraph from{\n 1->2;\n 2->3;\n 3->4;\n}";
		// toDotExpr[14] = "digraph from{\n 2->3;\n 3->4;\n 4->5;\n}";
		// DecoratedGraph from = GraphConverter.parse(fromDotExpr);
		// for (int i = 0; i < toDotExpr.length; i++) {
		// DecoratedGraph to = GraphConverter.parse(toDotExpr[i]);
		// int dist = getPositionDist(from,to,null);
		// System.out.println(dist);
		// }
	}

	private static Map<String, Integer> finalFromNodesPosition = null;
	private static DecoratedGraph fromGraph = null;
}
