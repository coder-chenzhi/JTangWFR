package org.jtang.flowrecommender.dfscode.online;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jtang.distance.dfscode.DFSCodeDistance;
import org.jtang.distance.ged.editpath.test.DecoratedGraphOperation;
import org.jtang.distance.ged.graph.DecoratedGraph;
import org.jtang.distance.ged.graph.DecoratedNode;
import org.jtang.distance.ged.graph.GraphConverter;
import org.jtang.distance.ged.processor.CostContainer;
import org.jtang.flowrecommender.dao.ReadFromDB;
import org.jtang.flowrecommender.dfscode.offline.PreProcess;
import org.jtang.flowrecommender.service.Time;
import org.jtang.frequency.gspan.Fragments;

/**
 * flow recommender based on graph edit distance only use for serial data test
 * 
 * @author chr
 * 
 */
public class SerialRecommenderBasedOnDfsCode {
	StringBuffer w = new StringBuffer();
	StringBuffer digraph = new StringBuffer();
	public static List<Fragments> allData = new ArrayList<Fragments>();

	// added by tokyo
	// test setst
	public static String[] allNodeSequence = "6,30,22,1,19,36,39,end;50,43,41,47,15,45,end;20,24,22,42,5,end;31,50,3,26,23,13,17,end;9,32,47,37,50,44,end;38,4,10,50,44,7,end;33,21,49,13,17,end;30,22,1,9,32,end;20,24,22,42,7,35,8,end;20,24,11,38,8,41,end;"
			.split(";");
	public static String[] nodeSequence = allNodeSequence[0].split(",");
	public static int firstN = 5;
	public static int nodeNo = 0;
	public static List<String> candidateList = new ArrayList<String>();
	public static int missCount = 0;
	public static int hitCount[] = new int[] { 0, 0, 0, 0, 0 };
	public static long distTime = 0;

	// public static long t0 = 0;

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
		Time.start = System.currentTimeMillis();
		Map<String, String> distMap = new HashMap<String, String>();

		// System.out.println(digraph);
		String nodes[] = node.split(";");
		// add unrelated node
		String digraphFrom = "digraph from{\n"
				+ PreProcess.addUnrelatedNode(digraph, nodes[1]) + "}";
		String fromDotExpr = "digraph from{\n" + digraph.toString() + "}";
		Time.end = System.currentTimeMillis();
		Time.time += (Time.end - Time.start);

		String dfsCodeFrom = SerialRecommenderBasedOnDfsCode
				.changeDigraphToDfsCode(digraphFrom);

		Time.start = System.currentTimeMillis();
		DecoratedGraph from = GraphConverter.parse(fromDotExpr);
		int number = from.getNodeNumber();

		// System.out.println("current number :"+ number);
		// 获取选中节点的上流子路径
		String upstreamFrom = DecoratedGraphOperation.getUpstreamOfFrom(from,
				nodes[1].split(","));
		// System.out.println(upstreamFrom);
		DecoratedGraph upstreamGraph = GraphConverter.parse(upstreamFrom);
		Set<DecoratedNode> set = upstreamGraph.getNodes();// 获取的当前工作流的节点集

		for (int i = 0; i < allData.size(); i++) {
			Fragments frag = allData.get(i);
			if (validate(frag, number, set)) {
				// 将数据库中的digraph转换成标准的digraph输入
				String digraphTo = getNormalSubpath(frag.getSubpath());
				String dfsCodeTo = frag.getDfscode();
				// get SED
				long start = System.currentTimeMillis();
				double dist = DFSCodeDistance.getDFSCodeDistance(from,
						digraphTo, dfsCodeFrom, dfsCodeTo, nodes);
				long end = System.currentTimeMillis();
				distTime += (end - start);

				if (dist != -1) {
					// modify by chr
					// update the data of the map

					// validate if the key is exist
					if (distMap.containsKey(frag.getNode())) {
						// get value
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
					// modify complete
				}
			}
			// System.out.println("ged:"+duration+","+duration/allData.size());
		}
		// long end = System.currentTimeMillis();
		// System.out.println("it takes " + (end - start));
		// recommendTimeSum += (end - start);
		// modify by chr
		// 将TreeMap中的元素放到ArrayList中
		ArrayList<Map.Entry<String, String>> tmList = new ArrayList<Map.Entry<String, String>>(
				distMap.entrySet());
		// 利用Collections.sort方法排序
		Collections.sort(tmList, new Comparator<Map.Entry<String, String>>() {

			public int compare(Map.Entry<String, String> o1,
					Map.Entry<String, String> o2) {
				String value1[] = o1.getValue().split(",");
				String value2[] = o2.getValue().split(",");
				Double dist1 = Double.parseDouble(value1[0]);
				Double dist2 = Double.parseDouble(value2[0]);
				Double conf1 = Double.parseDouble(value1[1]);
				Double conf2 = Double.parseDouble(value2[1]);
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
		// modify complete
		// 输出排序后的数据
		candidateList = new ArrayList<String>();// added by tokyo

		// System.out.println(time);
		int i = 0;
		for (Map.Entry<String, String> entry : tmList) {
			i++;
			// String tempNode = entry.getKey().replace(",", "");
			String tempNode = entry.getKey().replace(",", "");
			// added by tokyo
			candidateList.add(tempNode);

			if (i >= firstN) {
				break;
			}
		}
		Time.end = System.currentTimeMillis();
		Time.time += (Time.end - Time.start);
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
		// 数据库中的节点数目必须小于当前工作流的节点数
		if (frag.getSubNodeNumber() <= number && frag.getSubNodeNumber() > 0) {
			String nodes[] = frag.getNode().split(",");
			// 由于节点不能重复，数据库中的工作流推荐的节点不能出现是当前工作流已有的节点
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
	 *            standard keyboard input
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

	private static String changeDigraphToDfsCode(String digraph) {

		String matrix = "1\n1\n" + PreProcess.gedToMatrix(digraph) + "#";
		String dfsCode = PreProcess.MatrixToDFSCode(matrix);

		return dfsCode;
	}

	public void start() throws Exception {
		System.out.println("工作流推荐开始...");
		System.out.println("请输入开始节点：");
		check(null);

	}

	public void check(String prenode) throws Exception {
		// long t0 = System.currentTimeMillis();
		// String str01 = SystemRead();
		// added by tokyo
		String str01 = null;
		if (nodeSequence[nodeNo].equals("end")) {
			str01 = "end";
		} else {
			str01 = nodeSequence[nodeNo] + ";" + nodeSequence[nodeNo];
		}

		boolean b = false;
		String node[] = str01.split(";");

		if (candidateList.contains(node[0])) {
			// System.out.println("Hit");
			int position = candidateList.indexOf(node[0]);
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

		if (b && prenode == null) {
			// w.append(node[0]).append(";\n");
			w.append(node[0]).append(";\n");
			// System.out.println("当前已经定义的工作流为：" + w);
			// 调用方法获得推荐的节点
			String command = getRecommender(w, str01);
			if (command.equals("finish")) {
				// System.out.println("推荐结束...");
			} else {
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
				// System.out.println("推荐结束...");
				// System.out.println("最后工作流为：" + w);
			} else {
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
			String command = getRecommender(digraph, str01);
			if (command.equals("finish")) {
				// System.out.println("推荐结束...");
				// System.out.println("最后工作流为：" + w);
			} else {
				check(node[1]);
			}
		} else if (str01.matches("end")) {
			// System.out.println("推荐结束...");
			// System.out.println("最后工作流为：" + digraph);
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
		int j=0;
		nodeNo = 0;
		Time.time = 0;
		distTime=0;
		hitCount = new int[] { 0, 0, 0, 0, 0 };
		nodeSequence = allNodeSequence[j].split(",");
		SerialRecommenderBasedOnDfsCode fr = new SerialRecommenderBasedOnDfsCode();
		allData = new ReadFromDB().readData(3, 0.15);

		fr.start();
		/*System.out.println(j);
		for (int i = 0; i < hitCount.length; i++) {

			System.out.println(i + " hitcount is:" + hitCount[i]);
		}
		System.out.println(" recommended node number is:" + (nodeNo - 2));
		System.out.println("the recommending time is:" + Time.time);
		System.out.println("the dist time is:" + distTime);*/
		candidateList = new ArrayList<String>();
		
		// long start = System.currentTimeMillis();
		for (j = 0; j < 10; j++) {
			nodeNo = 0;
			Time.time = 0;
			distTime=0;
			hitCount = new int[] { 0, 0, 0, 0, 0 };
			nodeSequence = allNodeSequence[j].split(",");
			fr = new SerialRecommenderBasedOnDfsCode();
			allData = new ReadFromDB().readData(3, 0.15);

			fr.start();
			System.out.println(j);
			for (int i = 0; i < hitCount.length; i++) {

				System.out.println(i + " hitcount is:" + hitCount[i]);
			}
			System.out.println(" recommended node number is:" + (nodeNo - 2));
			System.out.println("the recommending time is:" + Time.time);
//			System.out.println("the dist time is:" + distTime);
			candidateList = new ArrayList<String>();

		}

	}
}
