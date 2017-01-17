package org.jtang.flowrecommender.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
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

import com.sun.org.apache.bcel.internal.generic.NEW;

//数据库中增加了
public class ParallelGEDFlowRecommender {
	// long aa = System.currentTimeMillis();
	StringBuffer w = new StringBuffer();
	StringBuffer digraph = new StringBuffer();
	public static List<Fragments> allData = new ArrayList<Fragments>();

	// added by tokyo
	// comment by Coder_chenzhi
	// 这个NodeSequence可以从org.jtang.function.ParallelNodeSequence计算得到
	public static String[] allNodeSequence = "3;3,-4;4,-7;7,-8;7,-20;20,-40;40-end.1;1,-4;4,-8;4,-10;4,-35;35,-28;28,-43;43,-11;11-end.3;3,-8;3,-10;3,-4;4,-17;17,-25;25,-40;40,-43;43,-45;45,-46;46-end.2;2,-3;3,-4;4,-17;17,-40;40,-45;45-end.2;2,-3;3,-4;4,-18;18,-25;25,-26;26,-20;20,-45;45,-46;46-end.2;2,-3;3,-4;4,-8;4,-17;17,-25;25,-27;25,-40;40,-43;43,-45;45,-46;46-end.2;2,-3;3,-4;4,-8;4,-17;4,17,-33;33,-40;40,-51;51,-46;46-end.2;2,-3;3,-4;4,-8;4,-25;25,-20;20,-23;23-end.2;2,-3;3,-4;3,-8;4,-17;17,-25;25,-33;33,-34;34,-11;11-end.2;2,-3;3,-7;7,-10;7,-17;17,-33;33,-40;40,-45;45,-46;46-end."
			.split("\\.");
	public static String[] nodeSequence = allNodeSequence[0].split("-");
	public static int firstN = 5;
	public static int nodeNo = 0;
	public static List<String> candidateList = new ArrayList<String>();
	public static int missCount = 0;
	public static int hitCount[] = new int[] { 0, 0, 0, 0, 0 };
	public static long time = 0;
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
		long start = System.currentTimeMillis();
		CostContainer costContainer = new CostContainer();

		Map<String, String> distMap = new HashMap<String, String>(5);
		// read all fragments from database
		String fromDotExpr = "digraph from{\n" + digraph.toString() + "}";
		DecoratedGraph from = GraphConverter.parse(fromDotExpr);

		DecoratedGraph to;
		int number = from.getNodeNumber();
		String nodes[] = node.split(";");

		// 获取选中节点的上流子路径
		String upstreamFrom = DecoratedGraphOperation.getUpstreamOfFrom(from,
				nodes[1].split(","));
		DecoratedGraph upstreamGraph = GraphConverter.parse(upstreamFrom);
		Set<DecoratedNode> set = upstreamGraph.getNodes();// 获取的当前工作流的节点集

		// long start = System.currentTimeMillis();
		for (int i = 0; i < allData.size(); i++) {
			Fragments frag = allData.get(i);
			if (validate(frag, number, set)) {
				String toDotExpr = getNormalSubpath(frag.getSubpath());// 数据库中的

				try {
					to = GraphConverter.parse(toDotExpr);

					Distance distance = new Distance();
					// node number distance
					double nodeCountDistance = NodeCountDist.getNodeCountDist(
							upstreamGraph, to);
					double sameNodeCount = from.getNodeNumber()
							- nodeCountDistance;
					if (sameNodeCount == 0) {
						continue;
					}
					distance.setNodeCountDistance(nodeCountDistance);

					// GED distance
					BigDecimal graphEditDistance = EditPathFinder.find(
							upstreamGraph, to, costContainer).getCost();
					if (graphEditDistance.intValue() == 0) {
						distance.setGraphEditDistance(graphEditDistance
								.intValue());
						distance.setPositionDistance(0);
					} else {
						distance.setGraphEditDistance(graphEditDistance
								.intValue());
						int positionDistance = PositionDist.getPositionDist(
								upstreamGraph, to, nodes[1].split(","));
						distance.setPositionDistance(positionDistance);
					}

					double dist = 1 * distance.getNodeCountDistance() + 1
							* distance.getGraphEditDistance() + 6
							* distance.getPositionDistance();

					if (distMap.containsKey(frag.getNode())) {
						String value[] = distMap.get(frag.getNode()).split(",");
						double lastDist = Double.parseDouble(value[0]);
						double lastConf = Double.parseDouble(value[1]);

						if (lastDist > dist) {
							double newDist = dist;
							double newConf = lastConf + frag.getConfident();
							distMap.put(frag.getNode(), newDist + "," + newConf);
						} else {
							double newDist = lastDist;
							double newConf = lastConf + frag.getConfident();
							distMap.put(frag.getNode(), newDist + "," + newConf);
						}
					} else {
						distMap.put(frag.getNode(),
								dist + "," + frag.getConfident());
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
		// 将TreeMap中的元素放到ArrayList中
		// modify by chr
		// 将TreeMap中的元素放到ArrayList中
		ArrayList<Map.Entry<String, String>> tmList = new ArrayList<Map.Entry<String, String>>(
				distMap.entrySet());
		// 利用Collections.sort方法排序
		Collections.sort(tmList, new Comparator<Map.Entry<String, String>>() {

			public int compare(Map.Entry<String, String> o1,
					Map.Entry<String, String> o2) {
				// System.out.println("o1:"+o1.getKey()+" ; "+o1.getValue()) ;
				// System.out.println("o2:"+o2.getKey()+" ; "+o2.getValue()) ;
				String value1[] = o1.getValue().split(",");
				String value2[] = o2.getValue().split(",");
				Double dist1 = Double.parseDouble(value1[0]);
				Double dist2 = Double.parseDouble(value2[0]);
				Double conf1 = Double.parseDouble(value1[1]);
				Double conf2 = Double.parseDouble(value2[1]);
				// System.out.println(dist1+","+dist2+","+conf1+","+conf2);
				if (dist1 < dist2) {
					return -1;
				} else if (dist1 > dist2) {
					return 1;
				} else {
					if (conf1 < conf2) {
						return 1;
					} else if (conf1 > conf2) {
						return -1;
					} else {
						return 0;
					}
				}
			}
		});
		long end = System.currentTimeMillis();
		time += (end - start);
		// 输出排序后的数据
		candidateList = new ArrayList<String>();// added by tokyo
		int i = 0;
		for (Map.Entry<String, String> entry : tmList) {
			i++;
			String tempNode = entry.getKey().replace(",", "");
			// added by tokyo
			candidateList.add(tempNode);
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
		String str01 = nodeSequence[nodeNo];
		/*
		 * if (nodeSequence[nodeNo].equals("end")) { str01 = "end"; } else {
		 * str01 = nodeSequence[nodeNo] + ";" + nodeSequence[nodeNo]; }
		 */

		boolean b = false;
		String node[] = str01.split(";");

		// added by tokyo
		// if (candidateSet.contains(node[0]) || nodeNo == 0) {
		if (candidateList.contains(node[0])) {
			// System.out.println((nodeNo + 1) + ":1");
			// if (nodeNo < 5 && nodeNo > 0) {
			// System.out.println((nodeNo + 1) + ":1");
			// System.out.println("Hit");
			int position = candidateList.indexOf(node[0]);
			// System.out.println(candidateList.indexOf(node[0])+1);
			// }
			for (int i = position; i <= 4; i++) {
				hitCount[i]++;

			}
		} else {
			if (!node[0].equals("end")) {
				// if (nodeNo < 5 && nodeNo > 0) {
				// // System.out.println((nodeNo + 1) + ":1");
				// System.out.println("miss");
				// }
				// // System.out.println((nodeNo + 1) + ":0");
				// missCount++;
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
				// check(node[1].substring(0,node[1].length()-1));
			}
		} else if (b && node[0].split(",").length >= 1
				&& prenode.split(",").length == 1) {
			// 并行分开或串行

			for (String n : node[0].split(",")) {
				digraph.append(prenode.split(",")[0] + "->" + n + ";\n");
			}

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
				// check(node[1].substring(0,node[1].length()-1));
			}
		} else if (b && node[0].split(",").length == 1
				&& prenode.split(",").length > 1) {
			// 分支合并
			for (String n : prenode.split(",")) {
				digraph.append(n + "->" + node[0] + ";\n");
			}

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
				// check(node[1].substring(0,node[1].length()-1));
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
		int j = 0;
		nodeNo = 0;
		time = 0;
		hitCount = new int[] { 0, 0, 0, 0, 0 };
		nodeSequence = allNodeSequence[j].split("-");
		ParallelGEDFlowRecommender fr = new ParallelGEDFlowRecommender();
		allData = new ReadFromDB().readData(3, 0.15);
		//
		fr.start();
		/*
		 * for (int i = 0; i < hitCount.length; i++) {
		 * 
		 * System.out.println(i + " hitcount is:" + hitCount[i]); }
		 * System.out.println(" recommended node number is:" + (nodeNo - 2));
		 * System.out.println("the recommending time is:" + time);
		 */
		candidateList = new ArrayList<String>();
		for (j = 0; j < 10; j++) {
			nodeNo = 0;
			time = 0;
			hitCount = new int[] { 0, 0, 0, 0, 0 };
			nodeSequence = allNodeSequence[j].split("-");
			fr = new ParallelGEDFlowRecommender();
			allData = new ReadFromDB().readData(3, 0.15);

			fr.start();
			for (int i = 0; i < hitCount.length; i++) {

				System.out.println(i + " hitcount is:" + hitCount[i]);
			}
			System.out.println("recommended node number is:" + (nodeNo - 2));
			System.out.println("the recommending time is:" + time);
			candidateList = new ArrayList<String>();
		}
	}
}
