package org.jtang.distance.dfscode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.jtang.distance.ged.editpath.test.NodeCountDist;
import org.jtang.distance.ged.editpath.test.PositionDist;
import org.jtang.distance.ged.graph.DecoratedGraph;
import org.jtang.distance.ged.graph.DotParseException;
import org.jtang.distance.ged.graph.GraphConverter;

public class DFSCodeDistance {

	/**
	 * Calculate the distance of the two DFSCode. If the sedDistance equals 0,
	 * means the structures are same. If the sedDistance does not equal 0, we
	 * separated into two situations. First, length(x,y)==0. The numbers of
	 * edges are the same, Dist(x,y)=sedDistance + maxNodenumber(x,y) -
	 * sameNodenumber(x,y). Second, length(x,y)!=0. Dist(x,y)=sedDistance +
	 * maxNodenumber(x,y) - length(x,y) + length(x,y)/3.
	 * 流程推荐时用，包括：相同节点个数，模式位置，SED
	 * @param from
	 * @param to
	 * @return
	 */
	public static double getDFSCodeDistance(DecoratedGraph from, String digraphTo, String dfsCodeFrom, String dfsCodeTo, String nodes[]) {

		double distance = 0;
		double nodeCountDistance;
		double sameNodeCount ;

		DecoratedGraph to = null ;
		int positionDistance = 0 ;
		try {
			to = GraphConverter.parse(digraphTo);
			
		} catch (DotParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		nodeCountDistance = NodeCountDist.getNodeCountDist(from, to);
		sameNodeCount = from.getNodeNumber()-nodeCountDistance ;
		//先判断是否有相同节点，由于所有图都加了一个无关节点，所以相同节点数大于1时我们才认为有相同节点
		if(sameNodeCount==0){
			return -1;
		}else{

			//reconstruct the dfscode
			List<String> list = new ArrayList<String>() ;
			list = reconstructDfsCodes(dfsCodeFrom,dfsCodeTo) ;
			String newDfsCodeFrom = list.get(0) ;
			String newDfsCodeTo = list.get(1) ;
			
			//get Levenshtein distance
			double sedDistance = SEDCalculation.getLevenshteinDistance(newDfsCodeFrom,
					newDfsCodeTo);
//			System.out.println("SED distance is:" + sedDistance);
	
			double length = 0;
			if (sedDistance == 0) {
				return sedDistance;
			} else {
				length = dfsCodeFrom.length() - dfsCodeTo.length();
				if (length < 0) {
					length = -length;
				}
				if (length == 0) {
//					double nodeCountDistance = 0;
//					nodeCountDistance = nodeNum[1] ;
					distance = sedDistance + nodeCountDistance;
				} else {
					distance = sedDistance - length + length / 3;
				}

				//calculate the position distance 
				positionDistance = 0;
				positionDistance = PositionDist.getPositionDist(
						from, to, nodes[1].split(","));
				
				distance += 6*positionDistance ;
				return distance;
			}
		}
	}

	private static Set<String> getEdges(String dfsCode){
		Set<String> edgeSet = new TreeSet<String>() ;
		for(int i=0;i<dfsCode.length()/3;i++){
			String subString = dfsCode.substring(3*i, 3*i+3) ;
			char sign = subString.charAt(0) ;
			if(sign=='+'){
				String edge = subString.substring(1, 3) ;
//				System.out.println(edge) ;
				edgeSet.add(edge) ;
			}else if(sign=='-'){
				String edge = "" + subString.charAt(2) + subString.charAt(1) ;
//				System.out.println(edge) ;
				edgeSet.add(edge) ;
			}else{
				System.out.println("The dfscode is illegal!") ;
			}
		}
		
		return edgeSet ;
	}
	
	/**
	 * 仅计算流程图的相似度，不考虑模式位置
	 * @param dfsCodeFrom
	 * @param dfsCodeTo
	 * @return
	 */
	public static double getDFSCodeDistanceForTest(String dfsCodeFrom,
			String dfsCodeTo) {

		double distance = 0;
		int[] nodeNum;

//		matrixFrom = "1\n1\n" + matrixFrom + "#";
//		String dfsCodeFrom = PreProcess.MatrixToDFSCode(matrixFrom);
//		matrixTo = "1\n1\n" + matrixTo + "#";
//		String dfsCodeTo = PreProcess.MatrixToDFSCode(matrixTo);

		//reconstruct the dfscode
		List<String> list = new ArrayList<String>() ;
		list = reconstructDfsCodes(dfsCodeFrom,dfsCodeTo) ;
		String newDfsCodeFrom = list.get(0) ;
		String newDfsCodeTo = list.get(1) ;
		
		nodeNum = calculateDifferentNodeNumber(dfsCodeFrom, dfsCodeTo);
		//先判断是否有相同节点
		if(nodeNum[0]==0){
			return -1;
		}else{
			//get Levenshtein distance
			double sedDistance = SEDCalculation.getLevenshteinDistance(newDfsCodeFrom,
					newDfsCodeTo);
//			System.out.println("SED distance is:" + sedDistance);

			double length = 0;
			if (sedDistance == 0) {
				return sedDistance;
			} else {
				length = dfsCodeFrom.length() - dfsCodeTo.length();
				if (length < 0) {
					length = -length;
				}
				if (length == 0) {
//					double nodeCountDistance = 0;
//					nodeCountDistance = calculateDifferentNodeNumber(dfsCodeFrom, dfsCodeTo);
//					System.out.println("node count distance:" + nodeCountDistance);
					distance = sedDistance + nodeNum[1];
					return distance;
				} else {
					distance = sedDistance - length + length / 3;
					return distance;
				}
			}
		}
	}

	/**
	 * delete and reconstruct the two dfscodes
	 * @param edgeSetOne
	 * @param edgeSetTwo
	 * @return
	 */
	private static List<String> reconstructDfsCodes(String dfsCodeFrom,String dfsCodeTo){
		Set<String> edgesFrom = getEdges(dfsCodeFrom) ;
		Set<String> edgesTo = getEdges(dfsCodeTo) ;
		List<String> list = new ArrayList<String>() ;
		
		//delete the same edges
		Iterator<String> edgeFromIterator = edgesFrom.iterator() ;
		while(edgeFromIterator.hasNext()){
			String edgeFrom = edgeFromIterator.next() ;
			Iterator<String> edgeToIterator = edgesTo.iterator() ;
			while(edgeToIterator.hasNext()){
				String edgeTo = edgeToIterator.next() ;
				if(edgeFrom.equals(edgeTo)){
					edgesFrom.remove(edgeFrom) ;
					edgesTo.remove(edgeTo) ;
					edgeFromIterator = edgesFrom.iterator() ;
//					System.out.println("remove edge is:" + edgeFrom);
					break ;
				}
			}
		}
		
		//reconstruct the edges
		Iterator<String> newEdgeFromIterator = edgesFrom.iterator() ;
		Iterator<String> newEdgeToIterator = edgesTo.iterator() ;
		String newDfsCodeFrom = "" ;
		String newDfsCodeTo = "" ;
		while(newEdgeFromIterator.hasNext()){
			newDfsCodeFrom += "+" + newEdgeFromIterator.next() ;
		}
		while(newEdgeToIterator.hasNext()){
			newDfsCodeTo += "+" + newEdgeToIterator.next() ;
		}
		
		list.add(newDfsCodeFrom) ;
		list.add(newDfsCodeTo) ;
		return list ;
	}
	
	private static int[] calculateDifferentNodeNumber(String dfsCodeFrom, String dfsCodeTo){
		int[] result = new int[2];
		int sameNodeCount = 0 ;
		Set<String> nodeSetFrom = getNodes(dfsCodeFrom) ;
		Set<String> nodeSetTo = getNodes(dfsCodeTo) ;
		
		int totalNodeNumber = (nodeSetFrom.size()>nodeSetTo.size())?nodeSetFrom.size():nodeSetTo.size() ;
		Iterator<String> nodeFromIterator = nodeSetFrom.iterator() ;
		while(nodeFromIterator.hasNext()){
			String nodeFrom = nodeFromIterator.next() ;
			Iterator<String> nodeToIterator = nodeSetTo.iterator() ;
			while(nodeToIterator.hasNext()){
				String nodeTo = nodeToIterator.next() ;
				if(nodeFrom.equals(nodeTo)){
					nodeSetFrom.remove(nodeFrom) ;
					nodeSetTo.remove(nodeTo) ;
					nodeFromIterator = nodeSetFrom.iterator() ;
					sameNodeCount++ ;
					break ;
				}
			}
		}
		result[0]=sameNodeCount;
		result[1]=totalNodeNumber-sameNodeCount;
		return result;
		
	}
	
	//get all the nodes from the dfscode
	private static Set<String> getNodes(String dfscode){
		Set<String> nodeSet = new TreeSet<String>() ;
		for(int i=0;i<dfscode.length();i++){
			String subString = dfscode.substring(i, i+1) ;
			if(subString.equals("+")||subString.equals("-")){
			}else if(subString.matches("[a-z]|[A-Z]")){
				nodeSet.add(subString) ;
			}else{
				System.out.println("The dfscode is illegal!") ;
			}
		}
		
		return nodeSet ;
	}

	public static void main(String args[]) {

		// toDotExpr[0] = "digraph to{\n1 -> 2;\n2 -> 3;\n5 -> 3;\n}";
		// toDotExpr[1] = "digraph to{\n1 -> 2;\n2 -> 5;\n4 - >5;\n}";
		// toDotExpr[2] = "digraph to{\n1 -> 5;\n5 -> 3;\n4 -> 3;\n}";
		// toDotExpr[3] = "digraph to{\n1 -> 2;\n2 -> 4;\n3 -> 4;\n}";
		// toDotExpr[4] = "digraph to{\n1 -> 2;\n2 -> 3;\n2 -> 4;\n}";
		// toDotExpr[5] = "digraph to{\n1 -> 2;\n2 -> 3;\n4 -> 2;\n}";
		// toDotExpr[6] = "digraph to{\n1 -> 2;\n2 -> 5;\n6 -> 5;\n}";
		// toDotExpr[7] = "digraph to{\n1 -> 5;\n5 -> 3;\n5 -> 4;\n}";
		// toDotExpr[8] = "digraph to{\n1 -> 2;\n2 -> 3;\n4 -> 3;\n}";
		// toDotExpr[9] = "digraph to{\n1 -> 4;\n4 -> 3;\n}";

		String from = "1 1 - \n- 2 1 \n- - 3 \n";
		String to = "1 1 - \n- 2 1 \n- - 4 \n";
		double distance = DFSCodeDistance.getDFSCodeDistanceForTest(from, to);
		System.out.println("distance is " + distance);
		
	}
}
