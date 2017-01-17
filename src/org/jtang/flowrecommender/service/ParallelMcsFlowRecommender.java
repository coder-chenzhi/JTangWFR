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
import java.util.TreeSet;

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
import org.jtang.distance.mcs.Mcs;
import org.jtang.distance.mcs.PreProcess;
import org.jtang.distance.mcs.SubGraphDFSCode;
import org.jtang.flowrecommender.dao.ReadFromDB;
import org.jtang.frequency.gspan.Fragments;
import org.jtang.frequency.gspan.WriteToDB;

//数据库中增加了
public class ParallelMcsFlowRecommender {
	StringBuffer w = new StringBuffer();
	StringBuffer digraph = new StringBuffer();
	// public static List<Fragments> allData = new ArrayList<Fragments>();
	// added by tokyo @ 2012-4-20
	public static List<Fragments>[] allData = new List[11];
	public static int[] a = new int[10];
	public static String[] allNodeSequence = "3;3,-4;4,-7;7,-8;7,-20;20,-40;40-end.1;1,-4;4,-8;4,-10;4,-35;35,-28;28,-43;43,-11;11-end.3;3,-8;3,-10;3,-4;4,-17;17,-25;25,-40;40,-43;43,-45;45,-46;46-end.2;2,-3;3,-4;4,-17;17,-40;40,-45;45-end.2;2,-3;3,-4;4,-18;18,-25;25,-26;26,-20;20,-45;45,-46;46-end.2;2,-3;3,-4;4,-8;4,-17;17,-25;25,-27;25,-40;40,-43;43,-45;45,-46;46-end.2;2,-3;3,-4;4,-8;4,-17;4,17,-33;33,-40;40,-51;51,-46;46-end.2;2,-3;3,-4;4,-8;4,-25;25,-20;20,-23;23-end.2;2,-3;3,-4;3,-8;4,-17;17,-25;25,-33;33,-34;34,-11;11-end.2;2,-3;3,-7;7,-10;7,-17;17,-33;33,-40;40,-45;45,-46;46-end.".split("\\.");
	public static String[] nodeSequence = allNodeSequence[8].split("-");

	public static int firstN = 5;
	public static int nodeNo = 0;
	public static List<String> candidateList = new ArrayList<String>();
	public static int missCount = 0;
	public static int hitCount[] = new int[] { 0, 0, 0, 0, 0 };
	public static long start = 0, end = 0;
	public static long[] duration = { 0, 0, 0, 0, 0 };

	/**
	 * initialize allData with the data in database
	 */
	private static void initializeAllData() {
		for (int i = 0; i < allData.length; i++) {
			allData[i] = new ReadFromDB().readDataBySize(i, 0.15);
		}
	}

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
		return str;//
	}

	/**
	 * recommend node based on the current work flow 通过已经产生的工作流和最后节点进行工作流推荐
	 * 
	 * @param digraph
	 * @param node
	 * @return
	 * @throws Exception
	 */
	public String getRecommender(StringBuffer digraph, String inputNodes)
			throws Exception {
		// System.out.println("1"+System.currentTimeMillis());
		// start = System.currentTimeMillis();
		Time.start = System.currentTimeMillis();
		Map<String, String> distMap = new HashMap<String, String>(5);
		String fromDotExpr = "digraph from{\n" + digraph.toString() + "}";
		DecoratedGraph from = GraphConverter.parse(fromDotExpr);
		int number = from.getNodeNumber();
		// System.out.println("2"+System.currentTimeMillis());
		// end = System.currentTimeMillis();
		// duration[0]+=(end-start);

		// start = System.currentTimeMillis();
		String[] inputNodesArray = inputNodes.split(";");
		// 获取选中节点的上流子路径
		String upstreamFrom = DecoratedGraphOperation.getUpstreamOfFrom(from,
				inputNodesArray[1].split(","));
		// String changedUpstreamFrom = Mcs.addUnrelatedNode(upstreamFrom,from);
		String changedUpstreamFrom = Mcs.addUnrelatedNode(upstreamFrom,
				inputNodesArray[1]);
		Time.end = System.currentTimeMillis();
		Time.time += (Time.end - Time.start);
		/*
		 * end = System.currentTimeMillis(); duration[1]+=(end-start); start =
		 * System.currentTimeMillis();
		 */
		// System.out.println("3"+System.currentTimeMillis());
		String matrix = "1\n1\n" + Mcs.dotToMatrix(changedUpstreamFrom) + "#";
		PreProcess.writeToFile(matrix);
		TreeSet<SubGraphDFSCode> subGraphDFSSet = (TreeSet<SubGraphDFSCode>) Mcs
				.resolveConstructingGraph(inputNodesArray[1]);
		/*
		 * end = System.currentTimeMillis(); duration[2]+=(end-start);
		 */
		// System.out.println("4"+System.currentTimeMillis());

		/*
		 * start = System.currentTimeMillis();
		 */
		Time.start = System.currentTimeMillis();
		int size = subGraphDFSSet.size();
		// System.out.println("subgraph number is:"+size);
		int constructingGraphSize = number + 1;
		// System.out.println(size);
		for (int i = 0; i < size; i++) {
			boolean breakFor = false;
			SubGraphDFSCode code = subGraphDFSSet.first();
			// System.out.println("code is:"+code);
			String constructingGraphDFSCode = code.getDFSCode();
			int graphSize = code.getSize() - 1;
			// int graphSize = code.getSize();

			List<Fragments> Data = allData[graphSize];
			int dataSize = Data.size();
			for (int j = 0; j < dataSize; j++) {
				Fragments frag = Data.get(j);
				String upstreamDFSCode = frag.getDfscode();
				int upstreamGraphSize = frag.getSubNodeNumber() + 1;
				Distance distance = new Distance();
				double mcsDistance = Mcs.getMcsDistance(upstreamDFSCode,
						upstreamGraphSize, constructingGraphDFSCode,
						constructingGraphSize);
				if (mcsDistance == -1.00) {
					continue;
				}
				distance.setMcsDistance(mcsDistance);

				/*
				 * int positionDistance =
				 * PositionDist.getPositionDist(upstreamGraph, to,
				 * inputNodesArray[1].split(","));
				 * distance.setPositionDistance(positionDistance); double
				 * nodeCountDistance =
				 * NodeCountDist.getNodeCountDist(upstreamGraph, to);
				 * distance.setNodeCountDistance(nodeCountDistance);
				 */

				double dist = distance.getMcsDistance();

				// update the data of the map
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
			}
			if (breakFor) {
				break;
			}
			// System.out.println(code.getSize() + "," + code.getId() + "," +
			// code.getDFSCode());
			subGraphDFSSet.remove(code);
		}
		/*
		 * end = System.currentTimeMillis(); duration[3]+=(end-start); start =
		 * System.currentTimeMillis();
		 */
		// System.out.println("5"+System.currentTimeMillis());
		// }
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

		// 输出排序后的数据
		// candidateSet = new HashSet<String>();// added by tokyo

		// System.out.println(time);
		// added by tokyo 2012.4.24 for auto-test
		candidateList = new ArrayList<String>();
		int i = 0;
		for (Map.Entry<String, String> entry : tmList) {
			i++;
			String tempNode = entry.getKey().replace(",", "");

			// added by tokyo 2012.4.24 for auto-test
			candidateList.add(tempNode);
			if (i >= firstN) {
				break;
			}
		}
		/*
		 * end = System.currentTimeMillis(); duration[4]+=(end-start);
		 */
		Time.end = System.currentTimeMillis();

		Time.time += (Time.end - Time.start);
		// System.out.println("6"+System.currentTimeMillis());
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
		// String str01 = SystemRead();
		String str01 = nodeSequence[nodeNo];
		boolean b = false;

		// added by tokyo @ 2012.4.24
		// String str01 = null;
		// if (nodeSequence[nodeNo].equals("end")) {
		// str01 = "end";
		// } else {
		// // str01 = "" + (char) (Integer.parseInt(nodeSequence[nodeNo]) + 96);
		// str01 = nodeSequence[nodeNo] + ";" + nodeSequence[nodeNo];
		// }
		String node[] = str01.split(";");
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
				// System.out.println("Miss");
			}

		}
		nodeNo++;
		// validate the input
		b = validateInput(node);
		// the first node
		if (b && prenode == null) {
			w.append(node[0]).append(";\n");
			System.out.println("当前已经定义的工作流为：" + w);
			// 调用方法获得推荐的节点
			String command = getRecommender(w, str01);
			if (command.equals("finish")) {
				System.out.println("推荐结束...");
			} else {
				w.append("->");
				check(node[1]);
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

			// 调用方法获得推荐的节点
			String command = getRecommender(digraph, str01);
			if (command.equals("finish")) {
				System.out.println("推荐结束...");
				System.out.println("最后工作流为：" + w);
			} else {
				w.append("->");
				check(node[1]);
			}
		} else if (str01.matches("join")) {

		} else if (str01.matches("end")) {
			System.out.println("推荐结束...");
			System.out.println("最后工作流为：" + digraph);

		} else {
			// System.out.println("节点不存在，请重新输入...");
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
		initializeAllData();
		int j = 0;
		Time.time = 0;
		hitCount = new int[] { 0, 0, 0, 0, 0 };
		nodeSequence = allNodeSequence[j].split("-");
		ParallelMcsFlowRecommender fr = new ParallelMcsFlowRecommender();
		fr.start();

		for (j = 0; j < 10; j++) {
			for (int i = 0; i < 5; i++) {
				duration[i] = 0;
			}
			candidateList = new ArrayList<String>();
			nodeNo = 0;
			Time.time = 0;
			hitCount = new int[] { 0, 0, 0, 0, 0 };
			nodeSequence = allNodeSequence[j].split("-");
			fr = new ParallelMcsFlowRecommender();

			fr.start();
			for (int i = 0; i < hitCount.length; i++) {

				System.out.println(i + " hitcount is:" + hitCount[i]);
			}
			System.out.println(" recommended node number is:" + (nodeNo - 2));
			System.out.println("the recommending time is:" + Time.time);
			/*
			 * for (int i = 0; i < duration.length; i++) { System.out.println(i
			 * + " duration time is:" + duration[i]); }
			 */

		}

	}
}
