package org.jtang.distance;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 
 * @author Bin  
 * @version Creation time：2012-4-3  
 */
public class EditDistanceUtil {

	/**
	 * 返回前n个最小的编辑距离
	 * 
	 * @param sedmap
	 * @param n
	 * @return
	 */
	public static List<Map<String, BigDecimal>> getFirstEDs(
			Map<String, BigDecimal> sedmap, int n) {
		List<Map<String, BigDecimal>> sedlist = getAllOrderedEDs(sedmap);
		sedlist = sedlist.subList(0, n);
		return sedlist;
	}

	/**
	 * 返回按升序排列的编辑距离
	 * 
	 * @param sedmap
	 * @return
	 */
	public static List<Map<String, BigDecimal>> getAllOrderedEDs(
			Map<String, BigDecimal> sedmap) {
		List<Map<String, BigDecimal>> list = new ArrayList<Map<String, BigDecimal>>();
		Object[] distances = sedmap.values().toArray();
		Object[] subgraphIDs = sedmap.keySet().toArray();
		BigDecimal tmp = null;
		String temp;
		// 冒泡排序
		for (int i = 0; i < subgraphIDs.length; i++) {
			for (int j = 0; j < subgraphIDs.length - i - 1; j++) {
				BigDecimal value1 = (BigDecimal) distances[j];
				BigDecimal value2 = (BigDecimal) distances[j + 1];
				if (value2.compareTo(value1) == -1) {
					tmp = (BigDecimal) distances[j + 1];
					distances[j + 1] = distances[j];
					distances[j] = tmp;

					temp = (String) subgraphIDs[j + 1];
					subgraphIDs[j + 1] = subgraphIDs[j];
					subgraphIDs[j] = temp;
				}
			}
		}
		Map<String, BigDecimal> map;
		for (int i = 0; i < subgraphIDs.length; i++) {
			map = new HashMap<String, BigDecimal>();
			map.put((String)subgraphIDs[i], (BigDecimal)distances[i]);
			list.add(map);
		}
		return list;
	}
}
