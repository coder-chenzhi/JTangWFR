package org.jtang.distance.dfscode;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.jtang.distance.EditDistanceUtil;

import de.parsemis.DFSCodeUtil;
import de.parsemis.Miner;

/** 
 * @author Bin  
 * @version Creation time：2012-4-3  
 */
public class Executor {
	private static String[] constructArgs() {
		String graph = System.getProperty("user.dir") + "\\"
		+ "DFSCodeData" + "\\"+"partialGraph.sdg";
		String[] args = new String[4];
		args[0] = "--graphFile="+graph;
		args[1] = "--minimumFrequency=0";
//		args[2] = "--outputFile=d:/dfscode4partialgraph.dat";
		//Algorithm4DFS
		args[2] = "--algorithm=gspan4dfscode";//挖掘待匹配图的gSpan算法，该算法共享了线下挖掘频繁子图时的图数据库，即节点与边的顺序
		//RecursiveStrategy4DFSCode
		args[3] = "--distribution=dfscodes";//使用新的策略提取待匹配图的DFScode，即过滤掉子图的DFSCode，只保留自身的DFSCode
		return args;

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String envFile = Miner.getEnvFile();
		String dfscodeBase = Miner.getDfscodeFile();
		
		//解析线下阶段生成的DFSCode库，即频繁子图DFSCode，该解析也属于线下解析
		Map<String, String> codebaseMap = DFSCodeSED.parseDFSCodeBase(dfscodeBase);
		
		long begin = 0;
		try {
			//获取配置环境
			DFSMiner.getDFSEnv(envFile);
//			begin = System.currentTimeMillis();
			DFSMiner.run(constructArgs());//开始提取待匹配图的DFSCode
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		//解析线上阶段生成的待匹配图的DFSCode
		
		Map<?, ?> currentCodeMap = DFSCodeSED.parseDFSCodeMap(DFSCodeUtil.fragcodes);
		if (currentCodeMap.size() == 0) {
			System.out.println("There is no dfscode generated!!");
			System.exit(-1);// 非正常退出
		} else if (currentCodeMap.size() > 1) {
			System.out.println("The dfscode for the graph which is matched should not exceed one!!");
			System.exit(-1);// 非正常退出
		}
		
		//开始线上匹配工作
		begin = System.currentTimeMillis();
		//计算待匹配图与线下频繁子图的DFSCode距离
		Map<String, BigDecimal> dfscodeSEDs = DFSCodeSED.getDFSCodeSEDs(codebaseMap,
				currentCodeMap.values().toArray()[0].toString());
		
		//获取前n个DFSCode字符串距离
		List<Map<String, BigDecimal>> results = EditDistanceUtil.getFirstEDs(
				dfscodeSEDs, 1);
		
		//线上匹配工作结束
		long end = System.currentTimeMillis();
		
		System.out.println("DFSCode Time cost: "+(end-begin)+" ms");
		
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
