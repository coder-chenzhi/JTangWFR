package org.jtang.flowrecommender.service;

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
import java.util.Map.Entry;
import java.util.Set;

import org.jtang.flowrecommender.bean.Pattern;
import org.jtang.flowrecommender.dao.ReadFromDB;

//数据库中增加了
public class OriginalFlowRecommender {
	String w = "";
	public static String[] allNodeSequence = "                                                                                              ,end;5,19,12,14,15,end;16,12,17,4,1,end;1,3,16,17,6,end;7,8,21,17,6,end;5,19,12,3,11,end;7,5,19,12,3,11,end;13,4,19,7,8,21,end;21,7,5,14,19,12,17,end;1,16,4,19,7,end;"
			.split(";");
	public static String[] nodeSequence = allNodeSequence[9].split(",");
	public static int firstN = 5;
	public static int nodeNo = 0;
	// public static Set<String> candidateList = new HashSet<String>();
	public static Map<String, Double> candidateMap = new HashMap<String, Double>();
	public static int missCount = 0;
	public static int[] hitCount = new int[] { 0, 0, 0, 0, 0 };
	public static List<String> candidateList = new ArrayList<String>();
	// 通过数据库读取输入类型和当前输入的最后一个节点输出类型相符的节点
	List<String> list_node = new ReadFromDB().readNodeByType("1");
	public static long duration = 0;

	// 读取界面写入的数据
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
	 * w:当前工作流 node:最后一个输入的节点
	 */
	public String getRecommender(String w, String node) {
		Time.start = System.currentTimeMillis();
		candidateMap = new HashMap<String, Double>();
		list_node.remove(node);
		// System.out.println(list_node.size());
		if (list_node.size() == 0) {
			System.out.println("没有与当前节点相符的节点，推荐结束");
			return "finish";
		} else {
			/*
			 * //将当前工作流w转换成数组 String present_w[] = new String[w.length()] ;
			 * for(int i=0;i<w.length();i++){ present_w[i] = w.charAt(i) + ","
			 * +(w.length()-i) ; //System.out.println(present_w[i]); }
			 */
			List<Pattern> l = new ArrayList<Pattern>();
			double strenth[] = new double[100];
			for (int i = 0; i < list_node.size(); i++) {
				String n = list_node.get(i);
				
//				Time.end = System.currentTimeMillis();
//				Time.time += (Time.end - Time.start);
				long start = System.currentTimeMillis();
				
				// 对每个符合要求的节点，找到有影响的上游子路径
				List<Pattern> list_substring = new ReadFromDB()
						.readInfluenceSubStrings(n);
				
				long end = System.currentTimeMillis();		
//				Time.start = System.currentTimeMillis();

				duration += (end - start);
				if (list_substring.size() != 0) {
					for (int j = 0; j < list_substring.size(); j++) {
						Pattern p = list_substring.get(j);
						String present_p[] = p.getSubpath().split("-->");
						// 并计算子路径与当前工作流的距离
						double dist = getDist(present_p, w);
						// if(dist<0.2){
						p.setDist(dist);
						/*
						 * System.out.println(dist);
						 * System.out.println(p.getNode()) ;
						 * System.out.println(p.getSubpath());
						 */
						strenth[p.getNode().toCharArray()[0] - 96] += p
								.getConf();
						l.add(p);
						if (candidateMap.containsKey(String.valueOf(p.getNode()
								.toCharArray()[0] - 96))) {
							double lastDist = candidateMap.get(String.valueOf(p
									.getNode().toCharArray()[0] - 96));
							if (lastDist > dist) {
								candidateMap.put(String.valueOf(p.getNode()
										.toCharArray()[0] - 96), dist);
							}
						} else {
							candidateMap.put(String.valueOf(p.getNode()
									.toCharArray()[0] - 96), dist);
						}
						// }
					}
				} else {
					// 这个节点没有有影响的上游子路径
				}
			}

			Time.end = System.currentTimeMillis();
			Time.time += (Time.end - Time.start);
//			Time.time -= duration;
			return "go";
		}
	}

	public double getDist(String p[], String flow) {
		double dist = 0.0;
		for (int i = 0; i < p.length; i++) {
			String node = p[i].split(",")[0];
			if (flow.indexOf(node) == -1) {
				// 索引为-1，说明工作流中没有这个节点
				dist = 5;
				break;
			} else {
				int x = (flow.length() - flow.indexOf(node))
						- Integer.parseInt(p[i].split(",")[1]);
				if (x >= 0) {
					dist += Double.parseDouble(Integer.toString(x))
							/ (flow.length() * p.length);
				} else {
					dist += Double.parseDouble(Integer.toString((-x)))
							/ (flow.length() * p.length);
				}
			}
		}
		return dist;
	}

	public void start() {
		System.out.println("工作流推荐开始...");
		System.out.print("请输入开始节点：");
		check();
	}

	public void check() {
		// String str01 = SystemRead() ;

		String str01 = null;
		if (nodeSequence[nodeNo].equals("end")) {
			str01 = "end";
		} else {
			str01 = "" + (char) (Integer.parseInt(nodeSequence[nodeNo]) + 96);
		}

		boolean b = false;

		/*
		 * System.out.println("input:"+nodeSequence[nodeNo]); Iterator iterator
		 * = candidateList.iterator() ; while(iterator.hasNext()){
		 * System.out.print(iterator.next()+",") ; }
		 */

		// added by tokyo
		// getFirstNCandidates(3,candidateList);//choose 3 or 5
		// String node[] = str01.split(";");
		candidateList = getFirstNCandidates(firstN, candidateMap);
		if (candidateList.contains(nodeSequence[nodeNo])) {
			// if (nodeNo<5) {
			// System.out.println((nodeNo + 1) + ":1");
			// System.out.println("Hit");
			int position = candidateList.indexOf(nodeSequence[nodeNo]);
			for (int i = position; i <= 4; i++) {
				hitCount[i]++;

			}
		} else {
			if (!str01.equals("end")) {
				if (nodeNo < 5) {
					// System.out.println("0");
				}

				missCount++;
			}

		}
		nodeNo++;

		if (str01.matches("end")) {
			System.out.println("推荐结束...");
			System.out.println("最后工作流为：" + w);
		} else {

			if (str01.matches("[a-z]")) {

				w += str01;
				// System.out.println("当前已经定义的工作流为："+w);
				// 调用方法获得推荐的节点
				String command = getRecommender(w, str01);
				if (command.equals("finish")) {
					System.out.println("推荐结束...");
					System.out.println("最后工作流为：" + w);
				} else {
					check();
				}
			}
		}
	}

	private static List<String> getFirstNCandidates(int n,
			Map<String, Double> tempCandidateMap) {
		List<Map.Entry<String, Double>> info = new ArrayList<Map.Entry<String, Double>>(
				tempCandidateMap.entrySet());
		List<String> firstNCandidateList = new ArrayList<String>();
		Collections.sort(info, new Comparator<Map.Entry<String, Double>>() {
			public int compare(Entry<String, Double> o1,
					Entry<String, Double> o2) {
				double result = o1.getValue() - o2.getValue();
				if (result > 0) {
					return 1;
				} else if (result < 0) {
					return -1;
				} else {
					return 0;
				}
			}
		});

		for (int j = 0; j < info.size() && j < n; j++) {
			// System.out.println(info.get(j).getKey() + "------->"+
			// info.get(j).getValue());
			firstNCandidateList.add(info.get(j).getKey());
		}
		return firstNCandidateList;
	}

	public static void main(String args[]) {
		int j = 0;
		nodeNo = 0;
		Time.time = 0;
		duration = 0;
		hitCount = new int[] { 0, 0, 0, 0, 0 };
		nodeSequence = allNodeSequence[j].split(",");
		OriginalFlowRecommender fr = new OriginalFlowRecommender();
		fr.start();
		/*
		 * for (int i = 0; i < hitCount.length; i++) {
		 * 
		 * System.out.println(i + " hitcount is:" + hitCount[i]); }
		 * System.out.println(" recommended node number is:" + (nodeNo - 2));
		 * System.out.println(time);
		 */
		candidateList = new ArrayList<String>();
		candidateMap = new HashMap<String, Double>();
		for (j = 0; j < 10; j++) {
			nodeNo = 0;
			duration = 0;
			Time.time = 0;
			hitCount = new int[] { 0, 0, 0, 0, 0 };
			nodeSequence = allNodeSequence[j].split(",");
			fr = new OriginalFlowRecommender();
			fr.start();
			for (int i = 0; i < hitCount.length; i++) {

				System.out.println(i + " hitcount is:" + hitCount[i]);
			}
			System.out.println(" recommended node number is:" + (nodeNo - 2));
			System.out.println("the recommending time is:" + (Time.time-duration));
//			System.out.println("the recommending time is:" + (Time.time));
			candidateList = new ArrayList<String>();
			candidateMap = new HashMap<String, Double>();
		}
	}
}
