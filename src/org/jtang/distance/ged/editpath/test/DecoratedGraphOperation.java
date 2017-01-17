package org.jtang.distance.ged.editpath.test;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.jtang.distance.ged.graph.DecoratedGraph;
import org.jtang.distance.ged.graph.DecoratedNode;
import org.jtang.distance.ged.graph.DotParseException;
import org.jtang.distance.ged.graph.GraphConverter;

public class DecoratedGraphOperation {
	/**
	 * get the upstream of some given nodes in from
	 * @param from
	 * @param givenNodes
	 * @return
	 */
	public static String getUpstreamOfFrom(DecoratedGraph from, String... givenNodes) {
		
		if (from.getNodeNumber() == 1) {
			DecoratedNode node = from.getNodes().iterator().next();
			return "digraph from{"+"\n"+node.getLabel()+";"+"\n"+"}";
		}
		//added by tokyo @2012.12.22 验证是否开始节点就出现分支，导致没有上游子路径,如果是的，则将givenNodes作为图。
		boolean noUpstreamSubgraph = true;
		for (String label : givenNodes) {
			DecoratedNode currentNode = getNodeByLabel(label, from);
			if (currentNode == null) {
				return null;
			} else if(!currentNode.getAccessedBy().isEmpty()){
				noUpstreamSubgraph = false;
			}
		}
		if(noUpstreamSubgraph == true){
			StringBuffer upStreamSubgraph = new StringBuffer("digraph from{"+"\n");
			for (String label : givenNodes) {
//				DecoratedNode currentNode = getNodeByLabel(label, from);
				upStreamSubgraph.append(label+";"+"\n");
			}
			upStreamSubgraph.append("}");
			return upStreamSubgraph.toString();
		}
		
		
		Set<DecoratedNode> nodesOfUpstreamSet = new HashSet<DecoratedNode>();
		Set<String> edgesSet = new HashSet<String>();
		for (String label : givenNodes) {
			DecoratedNode currentNode = getNodeByLabel(label, from);
			if (currentNode == null) {
				return null;
			} else {
				constructUpstream(currentNode, nodesOfUpstreamSet, edgesSet);
			}
		}
		return setToString(edgesSet);
	}

	/**
	 * convert set to specific String
	 * 
	 * @param edgesSet
	 * @return specific String
	 */
	private static String setToString(Set<String> edgesSet) {
		// TODO Auto-generated method stub
		StringBuffer diagram = new StringBuffer("digraph from{\n");
		for (String edge : edgesSet) {
			diagram.append(edge).append("\n");
		}
		diagram.append("}\n");
		return diagram.toString();
	}

	/**
	 * recusively construct the upstream of currentNode
	 * 
	 * @param currentNode
	 *            currentNode
	 * @param nodesOfUpstreamSet
	 *            set contains nodes of upstream
	 * @param edgesSet
	 *            set contains edges of upstream
	 */
	private static void constructUpstream(DecoratedNode currentNode, Set<DecoratedNode> nodesOfUpstreamSet,
			Set<String> edgesSet) {
		// TODO Auto-generated method stub
		Set<DecoratedNode> forwardNodesSet = currentNode.getAccessedBy();
		if (forwardNodesSet.isEmpty()) {
			return;
		}
//		if (nodesOfUpstreamSet.containsAll(forwardNodesSet)) {
//			return;
//		}
		for (DecoratedNode decoratedNode : forwardNodesSet) {
			edgesSet.add(decoratedNode.getLabel() + "->" + currentNode.getLabel() + ";");
			if (!nodesOfUpstreamSet.contains(decoratedNode)) {
				constructUpstream(decoratedNode, nodesOfUpstreamSet,edgesSet);
			}
		}
		return;
	}

	private static DecoratedNode getNodeByLabel(String label, DecoratedGraph from) {
		Set<DecoratedNode> nodesOfFromSet = from.getNodes();
		for (Iterator iterator = nodesOfFromSet.iterator(); iterator.hasNext();) {
			DecoratedNode decoratedNode = (DecoratedNode) iterator.next();
			if (decoratedNode.getLabel().equals(label)) {
				return decoratedNode;
			}
		}
		return null;
	}

	/**
	 * @param args
	 * @throws DotParseException
	 */
	public static void main(String[] args) throws DotParseException {
		// TODO Auto-generated method stub
//		String fromString = "digraph from{\n 1->2;\n 2->4;\n 1->3;\n 3->5;\n 4->5;\n 5->6;\n 6->8;\n 5->7;\n}";
		String fromString = "digraph from{1;}";
		DecoratedGraph from = GraphConverter.parse(fromString);
		System.out.println(getUpstreamOfFrom(from, "8"));
	}

}
