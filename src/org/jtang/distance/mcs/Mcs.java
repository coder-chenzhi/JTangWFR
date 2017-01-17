package org.jtang.distance.mcs;

import static de.parsemis.miner.environment.Debug.INFO;
import static de.parsemis.miner.environment.Debug.err;
import static de.parsemis.miner.environment.Debug.out;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.jtang.distance.dfscode.DFSCodeSED;
import org.jtang.distance.dfscode.DFSMiner;
import org.jtang.distance.dfscode.GraphDatabaseUtil;
import org.jtang.distance.ged.graph.DecoratedGraph;
import org.jtang.distance.ged.graph.DecoratedNode;
import org.jtang.distance.ged.graph.DotParseException;
import org.jtang.distance.ged.graph.GraphConverter;
import org.jtang.flowrecommender.service.Time;

import de.parsemis.DFSCodeUtil;
import de.parsemis.Miner;
import de.parsemis.miner.environment.Settings;

/**
 * 
 * @author tokyo
 * 
 */
public class Mcs {

	/**
	 * translate the dot (ged )graph to matrix
	 * 
	 * @param ged
	 * @return
	 */
	public static String dotToMatrix(String ged) {
		String temp[] = ged.split("\n");
		Set<String> nodeSet = new HashSet<String>();
		Set<String> edgeSet = new HashSet<String>();
		String aString;

		// get all the nodes from the ged diagraph
		for (int i = 1; i < temp.length - 1; i++) {
			String[] bytes = temp[i].split("->");
			if (bytes.length >= 2) {
				nodeSet.add(bytes[0].trim());
				nodeSet.add(bytes[1].replace(";", "").trim());
				edgeSet.add(temp[i]);
			} else {
				String node[] = temp[i].split(";");
				nodeSet.add(node[0]);
				nodeSet.add("53");
				edgeSet.add(node[0] + " -> 53;");
			}
		}

		// initialize the matrix
		String matrix[][] = new String[nodeSet.size()][nodeSet.size()];
		for (int m = 0; m < nodeSet.size(); m++) {
			for (int n = 0; n < nodeSet.size(); n++) {
				matrix[m][n] = "-";
			}
		}

		// add all the nodes into the matrix
		Iterator<String> nodeIterator = nodeSet.iterator();
		int k = 0;
		while (nodeIterator.hasNext()) {
			String node = nodeIterator.next();
			matrix[k][k] = node;
			k++;
		}

		// add all the edges into the matrix
		Iterator<String> edgeIterator = edgeSet.iterator();
		while (edgeIterator.hasNext()) {
			String edge = edgeIterator.next();
			String bytes[] = edge.split("->");
			String begin = bytes[0].trim();
			String end = bytes[1].replace(";", "").trim();
			int row = 0;
			int col = 0;
			for (int m = 0; m < nodeSet.size(); m++) {
				if (matrix[m][m].equals(begin)) {
					row = m;
				} else if (matrix[m][m].equals(end)) {
					col = m;
				}
			}
			matrix[row][col] = "1";
		}

		// change the matrix to String
		StringBuffer result = new StringBuffer();
		for (int m = 0; m < nodeSet.size(); m++) {
			for (int n = 0; n < nodeSet.size(); n++) {
				result.append(matrix[m][n] + " ");
			}
			result.append("\n");
		}
		return result.toString();
	}

	/**
	 * 
	 * @param upstreamDFSCode
	 * @param upstreamGraphSize
	 * @param upstreamSize
	 * @param constructingSubDFSCode
	 * @param constructingGraphSize
	 * @param constructingSize
	 * @return -1 if these two dfscodes are equal;a positive number between 0
	 *         and 1 if equal
	 */
	public static double getMcsDistance(String upstreamDFSCode,
			int upstreamGraphSize, String constructingSubDFSCode,
			int constructingGraphSize) {
		if (upstreamDFSCode.equals(constructingSubDFSCode)) {
			return 1.00 - (upstreamGraphSize * 1.00 / constructingGraphSize);
		}
		return -1;

	}

	/**
	 * add a unrelated node to a graph(a->b => a->b->53)
	 * 
	 * @param ged
	 * @return
	 * @throws DotParseException
	 */
	public static String addUnrelatedNode(String ged,String nodes) throws DotParseException {
		Set<String> edgeSet = new HashSet<String>();

		// get all the nodes from the ged diagraph
		String[] temp = ged.split("\n");
		for (int i = 1; i < temp.length - 1; i++) {
			String[] bytes = temp[i].split("->");
			if (bytes.length >= 2) {
				edgeSet.add(temp[i]);
			}
		}

		StringBuffer diagram = new StringBuffer("digraph from{\n");
		for (String edge : edgeSet) {
			diagram.append(edge + "\n");
		}
		String[] node = nodes.split(",") ;
		for (String tempNode : node) {
				diagram.append(tempNode + "->" + "53" + ";\n");// " -> "
		}
		diagram.append("}");
		return diagram.toString();
	}
	/**
	 * 
	 * @param digraph
	 * @param node
	 * @return
	 */
	public static String addUnrelatedNode(StringBuffer digraph,String nodes){
		String[] node = nodes.split(",") ;
		String newDigraph = "" ;
		if(digraph.length()<5){
			return node[0]+" -> 53;\n" ;
		}else{
			newDigraph += digraph.toString() ;
			for(String lastnode:node){
				newDigraph += lastnode+" -> 53;\n" ;
			}
			return newDigraph;
		}
		
	}
	/**
	 * get all subgraphs' dfscodes
	 * 
	 * @param nodes
	 * @return
	 */
	public static Set<SubGraphDFSCode> resolveConstructingGraph(String lastNodes) {
		DFSCodeUtil.fragcodes = new HashMap();
		Set<SubGraphDFSCode> subGraphDFSSet = new TreeSet<SubGraphDFSCode>();
		String envFile = Miner.getEnvFile();
		// 获取配置环境
		DFSMiner.getDFSEnv(envFile);
		
		Time.start = System.currentTimeMillis();
		String[] lastNodesArray = lastNodes.split(",");
		String[] lastNodesDFSCodeArray = getNodeDFSCode(lastNodesArray,GraphDatabaseUtil.nodes);
		Set<String> dfscodeString = new HashSet<String>();
		dfscodeString.addAll(Arrays.asList(lastNodesDFSCodeArray));
		try {
			// begin = System.currentTimeMillis();

			DFSMiner.run(constructArgs());// 开始提取待匹配图的DFSCode
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		Map<String, ?> currentCodeMap = DFSCodeSED.parseDFSCodeMap(DFSCodeUtil.fragcodes);
		Set<String> key = currentCodeMap.keySet();
		for (Iterator it = key.iterator(); it.hasNext();) {

			String id = (String) it.next();
			String dfscode = (String) currentCodeMap.get(id);
			int size = 0;
			Set<Character> nodes = new HashSet<Character>();
			char[] temp = dfscode.toCharArray();
			for (char a : temp) {
				if (a == '+' || a == '-')
					size++;
				else {
					nodes.add(a);
				}
			}
			size++;// edges'
			int nodeNumber = nodes.size();
			boolean add = true;
			for (String dfsNode : dfscodeString) {
				if (!dfscode.contains(dfsNode)) {
					add = false;
					break;
				}
			}
			if (!dfscode.contains("0")) {
				add = false;
			}
			if (add == true) {
//				subGraphDFSSet.add(new SubGraphDFSCode(size, id, dfscode));
				subGraphDFSSet.add(new SubGraphDFSCode(nodeNumber, id, dfscode));
			}
		}
		Time.end = System.currentTimeMillis();
		Time.time += (Time.end-Time.start);
		return subGraphDFSSet;
	}
	private static String[] getNodeDFSCode(String[] nodeLabels, List nodesList) {
		String envFile = Miner.getEnvFile();
		String dfsNode[] = new String[nodeLabels.length];
			for (int i = 0; i < nodeLabels.length; i++) {
				Integer temp = (Integer) nodesList.indexOf(Integer.parseInt(nodeLabels[i]));
//				Integer temp = (Integer) nodeslist.get(Integer.parseInt(nodeLabels[i]));
				dfsNode[i] = String.valueOf(alpha[temp]);
			}

		return dfsNode;
	}

	private static String[] constructArgs() {
		String graph = System.getProperty("user.dir") + "\\" + "DFSCodeData"
				+ "\\" + "tempMatrix.sdg";
		String[] args = new String[3];
		args[0] = "--graphFile=" + graph;
		args[1] = "--minimumFrequency=0";
		// Algorithm4DFS
		args[2] = "--algorithm=gspan4dfscode";// 挖掘待匹配图的gSpan算法，该算法共享了线下挖掘频繁子图时的图数据库，即节点与边的顺序
		// RecursiveStrategy4DFSCode
//		args[3] = "--distribution=dfscodes";// 提取待匹配图的DFScode，得到自身和子图的DFSCode
		return args;
	}

	/** store all subgraphs's dfscode of current constructing graph */
	// public static Set<SubGraphDFSCode> subGraphDFSSet = new
	// TreeSet<SubGraphDFSCode>();

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		// Set<SubGraphDFSCode> subGraphDFSSet = resolveConstructingGraph("26");
		// int size = subGraphDFSSet.size();
		// for (int i = 0; i < size; i++) {
		// SubGraphDFSCode code = ((TreeSet<SubGraphDFSCode>)
		// subGraphDFSSet).first();
		// System.out.println(code.getSize() + "," + code.getId() + "," +
		// code.getDFSCode());
		// subGraphDFSSet.remove(code);
		// }
		// System.out.println("end");

		// String dotExprString =
		// "digraph from{\n 1->2;\n 2->3;\n 2->4;\n 4->5;\n}";
		// String dotExprString = "digraph from{\n 1->2;\n 2->3;\n}";
		// try {
		// String changedGED = addUnrelatedNode(dotExprString);
		// System.out.println(changedGED);
		// } catch (DotParseException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
//		String[] lastString = new String[]{"1"};
		String lastString = "1";
//		Set<SubGraphDFSCode> string = resolveConstructingGraph(lastString);
	}

	private static char[] alpha = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0".toCharArray();
}
