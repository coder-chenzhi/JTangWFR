package org.jtang.distance.ged;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jtang.distance.EditDistanceUtil;
import org.jtang.distance.dfscode.DFSCodeSED;
import org.jtang.distance.ged.editpath.EditPathFinder;
import org.jtang.distance.ged.editpath.test.Distance;
import org.jtang.distance.ged.graph.DecoratedGraph;
import org.jtang.distance.ged.graph.DotParseException;
import org.jtang.distance.ged.graph.GraphConverter;
import org.jtang.distance.ged.processor.CostContainer;
import org.jtang.frequency.gspan.CopyOfParseFrequentSubgraph;

/**
 * 执行图编辑距离的匹配动作
 * 
 * @author Bin
 * @version Creation time：2012-4-3
 */
public class Executor {

	/**
	 * 获取待匹配图与频繁子图的图编辑距离
	 * 
	 * @param from
	 * @param tos
	 * @param costContainer
	 * @return
	 * @throws Exception
	 */
	public static Map<String, BigDecimal> getGEDs(DecoratedGraph from,
			Map<String, String> tos, CostContainer costContainer)
			throws Exception {
		Object[] keyArray = tos.keySet().toArray();
		DecoratedGraph to;
		Map<String, BigDecimal> mapresult = new HashMap<String, BigDecimal>();
		for (Object o : keyArray) {
			to = GraphConverter.parse(tos.get(o));
			BigDecimal graphEditDistance = EditPathFinder.find(from, to,
					costContainer).getCost();
			mapresult.put((String) o, graphEditDistance);
		}
		return mapresult;
	}

	/**
	 * 解析decoratedgraph到Map对象中
	 * 
	 * @param editInputFile
	 * @return
	 * @throws Exception
	 */
	public static Map<String, String> getDigraphBase(String editInputFile)
			throws Exception {
		Map<String, String> map = new HashMap<String, String>();
		File input = new File(editInputFile);
		InputStream in = new FileInputStream(input);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String line = br.readLine();
		StringBuffer buf = new StringBuffer(2048);
		StringBuffer sb = new StringBuffer();
		int count = Integer.parseInt(line);
		while ((line = br.readLine()) != null) {
			buf.append(line).append("\n");
			if (line.startsWith("}")) {
				String[] rows = buf.toString().split("\n");
				String subGraphID = rows[0];
				for (int i = 1; i < rows.length; i++) {
					sb.append(rows[i]);
				}
				map.put(subGraphID, sb.toString());
				buf = new StringBuffer(2048);
				sb = new StringBuffer();
			}
		}
		if (count != map.size()) {
			System.out
					.println("The count of the edit input file is not equal to actual num");
			;
			System.exit(-1);

		}
		return map;
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		String editInputFile = System.getProperty("user.dir") + "\\"
				+ "DFSCodeData" + "\\" + "offline_decoratedgraphs.dat";
		String partialGraphFile = System.getProperty("user.dir") + "\\"
				+ "DFSCodeData" + "\\" + "partialGraph.sdg";
		CostContainer costContainer = new CostContainer();
		// String fromDotExpr =
		// "digraph from{\n 13 -> 3;\n 20 -> 13;\n 9 -> 20;\n 7 -> 9;\n 18 -> 7;\n 18 -> 13;\n 18 -> 3;\n}";
		// String fromDotExpr
		// ="digraph to { \n 4 -> 1; \n 4 -> 3;\n 1 -> 2;\n}";
		String fromDotExpr = (String) CopyOfParseFrequentSubgraph.parseFromFragment(
				partialGraphFile).values().toArray()[0];
		DecoratedGraph from = GraphConverter.parse(fromDotExpr);
		Map<String, String> tos = getDigraphBase(editInputFile);

		// 开始线上GED距离的计算
		long begin = System.currentTimeMillis();
		Map<String, BigDecimal> mapresult = getGEDs(from, tos, costContainer);
		List<Map<String, BigDecimal>> results = EditDistanceUtil.getFirstEDs(
				mapresult, 1);

		// 线上GED距离计算完成
		long end = System.currentTimeMillis();
		System.out.println("GED Time cost: " + (end - begin) + " ms");

		Map<String, BigDecimal> map;
		String subgraphID = "";
		BigDecimal distance = null;
		for (int i = 0; i < results.size(); i++) {
			map = results.get(i);
			if (map.size() == 1) {
				subgraphID = (String) map.keySet().toArray()[0];
				distance = (BigDecimal) map.values().toArray()[0];
			} else {
				System.out
						.println("The size of each map in results list is not equal to one!");
				System.exit(-1);
			}
			System.out.println("The distance for subgraph " + subgraphID
					+ " is " + distance);
		}

	}

}
