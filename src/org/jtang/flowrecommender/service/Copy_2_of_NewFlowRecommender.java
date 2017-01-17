package org.jtang.flowrecommender.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jtang.distance.ged.editpath.CostLimitExceededException;
import org.jtang.distance.ged.editpath.EditPathFinder;
import org.jtang.distance.ged.editpath.test.DecoratedGraphOperation;
import org.jtang.distance.ged.editpath.test.Distance;
import org.jtang.distance.ged.editpath.test.NodeCountDist;
import org.jtang.distance.ged.editpath.test.PositionDist;
import org.jtang.distance.ged.graph.DecoratedGraph;
import org.jtang.distance.ged.graph.DecoratedNode;
import org.jtang.distance.ged.graph.DotParseException;
import org.jtang.distance.ged.graph.GraphConverter;
import org.jtang.distance.ged.processor.CostContainer;
import org.jtang.flowrecommender.dao.ReadFromDB;
import org.jtang.frequency.gspan.Fragments;
import org.jtang.frequency.gspan.WriteToDB;

//数据库中增加了
public class Copy_2_of_NewFlowRecommender {
	// long aa = System.currentTimeMillis();
	StringBuffer w = new StringBuffer();
	StringBuffer digraph = new StringBuffer();
	public static List<Fragments> allData = new ArrayList<Fragments>();

	// added by tokyo
	// public static String[] nodeSequence = "16,9,18,5,14,end".split(",");//
	// test setst
	public static String[] allNodeSequence = "13,17,8,18,5,4,end;14,9,7,18,10,20,end;8,4,9,5,7,2,end;5,3,6,2,7,end;5,2,8,4,9,1,end;7,5,3,1,2,9,end;8,6,9,5,2,7,end;10,8,9,1,2,7,6,end;1,9,4,5,2,7,6,end;2,7,6,5,3,1,4,end;".split(";");
	public static String[] nodeSequence = allNodeSequence[0].split(",");
	public static int firstN = 5;
	// public static String[] nodeSequence =
	// "9,15,7,3,10,end".split(",");//train set
	public static int nodeNo = 0;
	public static Set<String> candidateSet = new HashSet<String>();
	public static int missCount = 0;
	public static int hitCount = 0;

	// public static long t0 = 0;
	public static long t1 = 0;
	public static long recommendTimeSum = 0;

	// read standard keyboard input
	public String SystemRead() {
		BufferedReader buff = new BufferedReader(new InputStreamReader(
				System.in));
		String str = null;
		try {
			str = buff.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return str;
	}

	// 通过已经产生的工作流和最后节点进行工作流推荐

	/**
	 * recommend node based on the current work flow
	 */
	public String getRecommender(StringBuffer digraph, String node)
			throws Exception {
		double duration = 0;

		CostContainer costContainer = new CostContainer();
		Map<String, Double> nodeMap = new HashMap<String, Double>();
		Map<String, Double> distMap = new HashMap<String, Double>(5);
		// read all fragments from database

		String getEdges[] = digraph.toString().split("\n");
		String getLastTwoEdges = "";
		if (getEdges.length > 2) {
			for (int i = getEdges.length - 2; i < getEdges.length; i++) {
				getLastTwoEdges = getEdges[i] + "\n";
			}
		} else {
			getLastTwoEdges = digraph.toString();
		}
		String fromDotExpr = "digraph from{\n" + digraph.toString() + "}";
		DecoratedGraph from = GraphConverter.parse(fromDotExpr);

		DecoratedGraph to;
		int number = from.getNodeNumber();
		String nodes[] = node.split(";");

		// System.out.println(fromDotExpr);
		// 获取选中节点的上流子路径
		String upstreamFrom = DecoratedGraphOperation.getUpstreamOfFrom(from,
				nodes[1].split(","));
		// System.out.println(upstreamFrom);
		DecoratedGraph upstreamGraph = GraphConverter.parse(upstreamFrom);
		Set<DecoratedNode> set = upstreamGraph.getNodes();// 获取的当前工作流的节点集

		// long start = System.currentTimeMillis();
		for (int i = 0; i < allData.size(); i++) {
			Fragments frag = allData.get(i);
			if (validate(frag, number, set)) {
				String toDotExpr = getNormalSubpath(frag.getSubpath());// 数据库中的

				try {
					// long gedstart = System.currentTimeMillis();
					to = GraphConverter.parse(toDotExpr);

					Distance distance = new Distance();
					// GED distance
					BigDecimal graphEditDistance = EditPathFinder.find(
							upstreamGraph, to, costContainer).getCost();
					distance.setGraphEditDistance(graphEditDistance.intValue());
					// position distance
//					int positionDistance = PositionDist.getPositionDist(
//							upstreamGraph, to, nodes[1].split(","));
					int positionDistance = PositionDist.getPositionDist(
							upstreamGraph, to, nodes[1].split(","));
//					int positionDistance = PositionDist.getPositionDistTest(
//							upstreamGraph, to, nodes[1].split(","));
					distance.setPositionDistance(positionDistance);
					// node number distance
					double nodeCountDistance = NodeCountDist.getNodeCountDist(
							upstreamGraph, to);
					distance.setNodeCountDistance(nodeCountDistance);

					double dist = 1*distance.getNodeCountDistance()
							+ 1*distance.getGraphEditDistance()
							+ 6*distance.getPositionDistance();

					// long gedend = System.currentTimeMillis();
					// duration += gedend - gedstart;

					// if (dist < 10 * from.getNodeNumber()) {
					// if (dist < 5 * from.getNodeNumber()) {
					// System.out.println("-------------------------");
					// System.out.println(frag.getNode()+toDotExpr+dist) ;
					// System.out.println(distance.getNodeCountDistance()+","+distance.getGraphEditDistance()+","+distance.getPositionDistance());
					if (distMap.containsKey(frag.getNode())) {
						double lastDist = distMap.get(frag.getNode());
						if (lastDist > dist) {
							distMap.put(frag.getNode(), dist);
						}
					} else {
						distMap.put(frag.getNode(), dist);
					}

					if (nodeMap.containsKey(frag.getNode())) {
						double lastConf = nodeMap.get(frag.getNode());
						nodeMap.put(frag.getNode(), frag.getConfident()
								+ lastConf);
					} else {
						nodeMap.put(frag.getNode(), frag.getConfident());
					}
				} catch (DotParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (CostLimitExceededException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			// System.out.println("ged:"+duration+","+duration/allData.size());
		}
		// long end = System.currentTimeMillis();
		// System.out.println("it takes " + (end - start));
		// recommendTimeSum += (end - start);
		// 将TreeMap中的元素放到ArrayList中
		ArrayList<Map.Entry<String, Double>> tmList = new ArrayList<Map.Entry<String, Double>>(
				distMap.entrySet());
		// 利用Collections.sort方法排序
		Collections.sort(tmList, new Comparator<Map.Entry<String, Double>>() {

			public int compare(Map.Entry<String, Double> o1,
					Map.Entry<String, Double> o2) {
				return o1.getValue().compareTo(o2.getValue());
			}
		});
		// 输出排序后的数据
		candidateSet = new HashSet<String>();// added by tokyo
		int i = 0;
		for (Map.Entry<String, Double> entry : tmList) {
			i++;
			String tempNode = entry.getKey().replace(",", "");
			// added by tokyo
			candidateSet.add(tempNode);
			// added by tokyo

			// System.out.print("node:" + tempNode + "dist:" + entry.getValue()
			// + ",");
			// Double conf = nodeMap.get(entry.getKey());
			// System.out.println("confident:" + conf);

			if (i >= firstN) {
				// if (i >= 3) { // choose the first n results
				break;
			}
		}
		// System.out.println(candidateSet.toString());

		/*
		 * for(int i=0;i<11;i++){ if(a[i]!=0){
		 * System.out.println("node:"+i+",confident:"+a[i]); } }
		 */

		// System.out.println("over");
		// System.out.println("size of data " + allData.size()) ;

		return "go";
	}

	public Map<String, Double> compareTo(Map<String, Double> distMap,
			Fragments frag, Double dist) {

		if (distMap.containsKey(frag.getNode())) {
			double lastDist = distMap.get(frag.getNode());
			if (lastDist > dist) {
				distMap.put(frag.getNode(), dist);
			}
		} else if (distMap.size() < 5) {

		} else {

		}
		return distMap;
	}

	/**
	 * validate whether the fragment has the possibility of being the influence
	 * substream of current work flow
	 * 
	 * @param frag
	 * @param number
	 * @param set
	 * @return
	 */
	public boolean validate(Fragments frag, int number, Set<DecoratedNode> set) {
		boolean b = true;
		if (frag.getSubNodeNumber() <= number && frag.getSubNodeNumber() > 0) {
			String nodes[] = frag.getNode().split(",");
			for (int i = 0; i < nodes.length; i++) {
				Iterator<DecoratedNode> iterator = set.iterator();
				while (iterator.hasNext()) {
					DecoratedNode node = iterator.next();
					if (node.toString().equals(nodes[i])) {
						b = false;
						break;
					}
				}
			}
		} else {
			b = false;
		}

		return b;
	}

	/**
	 * validate whether the input is legal
	 * 
	 * @param input
	 *            : standard keyboard input
	 * @return
	 */
	public boolean validateInput(String[] input) {
		boolean b = false;

		if (input.length < 2) {
			return false;
		} else {
			// validate all the number ranges from 1-10
			for (String n : input) {
				for (String m : n.split(",")) {
					// if(m.matches("[1-9]|10")){"[0-9]+"
					if (m.matches("[0-9]+")) {
						b = true;
					} else {
						b = false;
						break;
					}
				}
			}
			return b;
		}
	}

	public void start() throws Exception {
		System.out.println("工作流推荐开始...");
		System.out.print("请输入开始节点：");
		check(null);

	}

	public void check(String prenode) throws Exception {
		// long t0 = System.currentTimeMillis();
		// String str01 = SystemRead() ;
		// added by tokyo
		String str01 = null;
		if (nodeSequence[nodeNo].equals("end")) {
			str01 = "end";
		} else {
			str01 = nodeSequence[nodeNo] + ";" + nodeSequence[nodeNo];
		}

		boolean b = false;
		String node[] = str01.split(";");

		// added by tokyo
		if (candidateSet.contains(node[0]) || nodeNo == 0) {
//			System.out.println((nodeNo + 1) + ":1");
			if (nodeNo<5&&nodeNo>0) {
//				System.out.println((nodeNo + 1) + ":1");
				System.out.println("1");
			}
			hitCount++;
		} else {
			if (!node[0].equals("end")) {
				if (nodeNo<5&&nodeNo>0) {
//					System.out.println((nodeNo + 1) + ":1");
					System.out.println("0");
				}
//				System.out.println((nodeNo + 1) + ":0");
				missCount++;
			}

		}
		nodeNo++;

		// validate the input
		b = validateInput(node);

		if (b && prenode == null) {
			// w.append(node[0]).append(";\n");
			w.append(node[0]).append(";\n");
			System.out.println("当前已经定义的工作流为：" + w);
			// 调用方法获得推荐的节点
			String command = getRecommender(w, str01);
			if (command.equals("finish")) {
				System.out.println("推荐结束...");
				// System.out.println("最后工作流为：" + w);

				// t0 = System.currentTimeMillis() - t0;
				// t1 += t0;

			} else {
				w.append("->");
				check(node[1]);
			}
		} else if (b && node[0].split(",").length >= 1
				&& prenode.split(",").length == 1) {
			// 并行分开或串行
			for (String n : node[0].split(",")) {
				digraph.append(prenode + "->" + n + ";\n");
			}

			// System.out.println("digraph is:" + digraph);
			// 调用方法获得推荐的节点
			String command = getRecommender(digraph, str01);
			if (command.equals("finish")) {
				System.out.println("推荐结束...");
				System.out.println("最后工作流为：" + w);

				// t0 = System.currentTimeMillis() - t0;
				// t1 += t0;

			} else {
				w.append("->");
				check(node[1]);
			}
		} else if (b && node[0].split(",").length == 1
				&& prenode.split(",").length > 1) {
			// 分支合并
			for (String n : prenode.split(",")) {
				digraph.append(n + "->" + node[0] + ";\n");
			}

			// System.out.println("digraph is:" + digraph);
			// 调用方法获得推荐的节点
			String command = getRecommender(w, str01);
			if (command.equals("finish")) {
				System.out.println("推荐结束...");
				System.out.println("最后工作流为：" + w);

				// t0 = System.currentTimeMillis() - t0;
				// t1 += t0;

			} else {
				w.append("->");
				check(node[1]);
			}
		} else if (str01.matches("join")) {

		} else if (str01.matches("end")) {
			System.out.println("推荐结束...");
			// System.out.println("最后工作流为：" + w);
			System.out.println("最后工作流为：" + digraph);
			// t0 = System.currentTimeMillis() - t0;
			// t1 += t0;

		} else {
			System.out.println("节点不存在，请重新输入...");
			check(prenode);
		}
	}

	public String getNormalSubpath(String subpath) {
		int count = 0;
		String subpathsplit[] = subpath.split("\n");
		StringBuffer subfragment = new StringBuffer();
		subfragment.append("digraph to {\n");
		for (int i = 0; i < subpathsplit.length; i++) {
			if (subpathsplit[i].matches("[^A-Z]*->[^A-Z]*")) {
				subfragment.append(subpathsplit[i]).append("\n");
				count++;
			}
		}
		subfragment.append("}\n");
		if (count == 0) {
			return subpath;
		} else {
			return subfragment.toString();
		}
	}

	public static void main(String args[]) throws Exception {
		long start = System.currentTimeMillis();
		Copy_2_of_NewFlowRecommender fr = new Copy_2_of_NewFlowRecommender();
		allData = new ReadFromDB().readData(3, 0.1);

		fr.start();
		System.out.println("hitcount is:" + (hitCount - 1));
		System.out.println("miscount is:" + missCount);
		// System.out.println("the recommending time is:"+t1);
		// System.out.println("the recommending time is:" + recommendTimeSum /
		// 1000.0);
		long end = System.currentTimeMillis();
		System.out
				.println("the recommending time is:" + (end - start) / 1000.0);

	}
}
