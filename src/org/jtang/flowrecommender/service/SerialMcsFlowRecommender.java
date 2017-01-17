package org.jtang.flowrecommender.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
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
public class SerialMcsFlowRecommender {
	StringBuffer w = new StringBuffer();
	StringBuffer digraph = new StringBuffer();
	/** all data in table pattern temp in db. */
	public static List<Fragments>[] allData = new List[11];
	public static int[] a = new int[10];
	//
	public static String[] allNodeSequence = "6,30,22,1,19,36,39,end;50,43,41,47,15,45,end;20,24,22,42,5,end;31,50,3,26,23,13,17,end;9,32,47,37,50,44,end;38,4,10,50,44,7,end;33,21,49,13,17,end;30,22,1,9,32,end;20,24,22,42,7,35,8,end;20,24,11,38,8,41,end;"
			.split(";");
	public static String[] nodeSequence = allNodeSequence[9].split(",");

	public static int firstN = 5;
	public static int nodeNo = 0;
	public static List<String> candidateList = new ArrayList<String>();
	public static int missCount = 0;
	public static int hitCount[] = new int[] { 0, 0, 0, 0, 0 };
	public static long distTime = 0;

	/**
	 * initialize allData with the data in database separately
	 */
	private static void initializeAllData() {
		for (int i = 0; i < allData.length; i++) {
			allData[i] = new ReadFromDB().readDataBySize(i, 0.15);
		}
	}

	/**
	 * read standard keyboard input
	 * 
	 * @return
	 */
	private String SystemRead() {
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
	 * @param inputNodes
	 * @return
	 * @throws Exception
	 */
	public String getRecommender(StringBuffer digraph, String inputNodes)
			throws Exception {
		Time.start = System.currentTimeMillis();
		Map<String, String> distMap = new HashMap<String, String>(5);
		String fromDotExpr = "digraph from{\n" + digraph.toString() + "}";
		DecoratedGraph from = GraphConverter.parse(fromDotExpr);
		int number = from.getNodeNumber();

		String[] inputNodesArray = inputNodes.split(";");
		DecoratedGraph to;
		// 获取选中节点的上流子路径
		String upstreamFrom = DecoratedGraphOperation.getUpstreamOfFrom(from,
				inputNodesArray[1].split(","));
		String changedUpstreamFrom = Mcs.addUnrelatedNode(upstreamFrom,
				inputNodesArray[1]);
		Time.end = System.currentTimeMillis();
		Time.time += (Time.end - Time.start);

		String matrix = "1\n1\n" + Mcs.dotToMatrix(changedUpstreamFrom) + "#";
		PreProcess.writeToFile(matrix);
		TreeSet<SubGraphDFSCode> subGraphDFSSet = (TreeSet<SubGraphDFSCode>) Mcs
				.resolveConstructingGraph(inputNodesArray[1]);

		Time.start = System.currentTimeMillis();
		int size = subGraphDFSSet.size();
		int constructingGraphSize = number + 1;
		// System.out.println(size);
		for (int i = 0; i < size; i++) {
			boolean breakFor = false;
			SubGraphDFSCode code = subGraphDFSSet.first();
			String constructingGraphDFSCode = code.getDFSCode();
			int graphSize = code.getSize() - 1;

			List<Fragments> Data = allData[graphSize];
			int dataSize = Data.size();
			for (int j = 0; j < dataSize; j++) {
				Fragments frag = Data.get(j);
				String upstreamDFSCode = frag.getDfscode();
				int upstreamGraphSize = frag.getSubNodeNumber() + 1;

				Distance distance = new Distance();
				long start = System.currentTimeMillis();

				double mcsDistance = Mcs.getMcsDistance(upstreamDFSCode,
						upstreamGraphSize, constructingGraphDFSCode,
						constructingGraphSize);
				long end = System.currentTimeMillis();
				distTime += (end - start);
				if (mcsDistance == -1.00) {
					continue;
				}
				distance.setMcsDistance(mcsDistance);

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
				// if we found the mcs ,the break
				// breakFor = true;
			}
			if (breakFor) {
				break;
			}
			// System.out.println(code.getSize() + "," + code.getId() + "," +
			// code.getDFSCode());
			subGraphDFSSet.remove(code);
		}

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
		Time.end = System.currentTimeMillis();
		Time.time += (Time.end - Time.start);
		// allTime+=(end-start);
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
		boolean b = false;

		// added by tokyo @ 2012.4.24
		String str01 = null;
		if (nodeSequence[nodeNo].equals("end")) {
			str01 = "end";
		} else {
			// str01 = "" + (char) (Integer.parseInt(nodeSequence[nodeNo]) +
			// 96);
			str01 = nodeSequence[nodeNo] + ";" + nodeSequence[nodeNo];
		}
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
				digraph.append(prenode + "->" + n + ";\n");
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
		// read all data whose node number <= 10 changed by tokyo @2012-4-20
		initializeAllData();
		int j = 0;
		Time.time = 0;
		distTime = 0;
		hitCount = new int[] { 0, 0, 0, 0, 0 };
		nodeSequence = allNodeSequence[j].split(",");
		SerialMcsFlowRecommender fr = new SerialMcsFlowRecommender();
		fr.start();

		for (j = 0; j < 10; j++) {
			candidateList = new ArrayList<String>();
			nodeNo = 0;
			Time.time = 0;
			distTime = 0;
			hitCount = new int[] { 0, 0, 0, 0, 0 };
			nodeSequence = allNodeSequence[j].split(",");
			fr = new SerialMcsFlowRecommender();

			fr.start();
			for (int i = 0; i < hitCount.length; i++) {

				System.out.println(i + " hitcount is:" + hitCount[i]);
			}
			System.out.println(" recommended node number is:" + (nodeNo - 2));
			System.out.println("the recommending time is:" + Time.time);

		}
	}
}
