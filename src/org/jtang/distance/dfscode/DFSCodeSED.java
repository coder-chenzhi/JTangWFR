package org.jtang.distance.dfscode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jtang.distance.EditDistanceUtil;

import de.parsemis.DFSCodeUtil;

/**
 * 计算两个图的DFSCode的字符串距离(String edit distance)
 * 
 * @author Bin
 * @version Creation time：2012-4-1
 */
public class DFSCodeSED {

	private static String[] num2char = { "a", "b", "c", "d", "e", "f", "g",
			"h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t",
			"u", "v", "w", "x", "y", "z", "A", "B", "C", "D", "E", "F", "G",
			"H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
			"U", "V", "W", "X", "Y", "Z","0"};

	/**
	 * 计算待匹配图与图数据库中的所有DFSCode距离并返回
	 * 
	 * @param frag_dfscode
	 * @param dfscode
	 * @return map<subgraphID, SED>
	 */
	public static Map<String, BigDecimal> getDFSCodeSEDs(
			Map<String, String> frag_dfscode, String dfscode) {
		Map<String, BigDecimal> map = new HashMap<String, BigDecimal>();
		Object[] subgraphIDs = frag_dfscode.keySet().toArray();
		BigDecimal bd;
		for (Object s : subgraphIDs) {
			bd = SEDCalculation.calculateCoefficient(frag_dfscode.get(s)
					.toString(), dfscode);
			map.put(s.toString(), bd);
		}
		return map;

	}

	/**
	 * 根据gSpan挖掘出的DFSCode的map进行解析，该方法主要是针对 待匹配图的DFSCode进行解析，因为其挖掘结果存在一个Map数据结构中
	 * 
	 * @param frag_dfscode
	 * @return
	 */
	public static Map parseDFSCodeMap(Map frag_dfscode) {
		Map map = new HashMap();
		Object[] subgraphIDs = frag_dfscode.keySet().toArray();
		String code;
		for (Object o : subgraphIDs) {
			code = parseDFSCode(frag_dfscode.get(o).toString());
			map.put(o, code);
		}
		return map;
	}

	/**
	 * 读取DFSCode库中的编码并解析，
	 * @param DFSCodeBaseFile
	 * @return
	 */
	public static Map<String, String> parseDFSCodeBase(String DFSCodeBaseFile) {
		File file = new File(DFSCodeBaseFile);
		InputStream in = null;
		Map<String, String> map = new HashMap<String, String>();
		try {
			in = new FileInputStream(file);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String line;
			String subgraphID;
			String code;
			int index;
			// String[] tmp=new String[2];
			while ((line = br.readLine()) != null) {
				index = line.indexOf('=');
				subgraphID = line.substring(0, index);
				code = line.substring(index + 1);
				code = parseDFSCode(code);//把每条边都解析出来
				map.put(subgraphID, code);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return map;
	}

	/**
	 * 解析一个频繁子图的DFSCode
	 * 
	 * @param dfscode
	 * @return ("direction,nodeA,nodeB")中间没有“,”
	 */
	public static String parseDFSCode(String dfscode) {
		String s = "\\(\\d+.\\d+:.-?1.\\d+.\\d+.\\d+\\)";
		Pattern p = Pattern.compile(s);
		Matcher m = p.matcher(dfscode);// 替换空格
		// Pattern p2 = Pattern.compile(":");
		StringBuffer bs = new StringBuffer();
		String tmp0;
		String tmp1;
		String[] temp;
		int index;
		while (m.find()) {
			tmp0 = m.group().substring(1, m.group().length() - 1);// 过滤掉左右括号
			index = tmp0.indexOf(":");
			tmp1 = tmp0.substring(index + 1);
			temp = tmp1.trim().split(" ");
			if (temp[0].equals("1")) {
				bs.append("+");// 正向边

			} else if (temp[0].equals("-1")) {
				bs.append("-");// 反向边
			} else {
				System.out.println("Direction label is missing!!!!");
				System.exit(-1);
			}
			bs.append(DFSCodeSED.num2char[Integer.parseInt(temp[1])]);// 返回DFSCode的,nodeA
			bs.append(DFSCodeSED.num2char[Integer.parseInt(temp[3])]);// 返回DFSCode的nodeB
		}
		return bs.toString();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println(DFSCodeSED.parseDFSCode("(0 11: 1 21 0 2)"));
		// Map<String, String> codebaseMap = parseDFSCodeBase("d:/dfscode.dat");
		// Map currentCodeMap = parseDFSCodeMap(DFSCodeUtil.fragcodes);
		// if (currentCodeMap.size() == 0) {
		// System.out.println("There is no dfscode generated!!");
		// System.exit(-1);// 非正常退出
		// } else if (currentCodeMap.size() > 1) {
		// System.out
		// .println("The dfscode for the graph which is matched should not exceed one!!");
		// System.exit(-1);// 非正常退出
		// }
		// Map<String, BigDecimal> dfscodeSEDs = getDFSCodeSEDs(codebaseMap,
		// currentCodeMap.values().toArray()[0].toString());
		// List<Map<String, BigDecimal>> results = EditDistanceUtil.getFirstEDs(
		// dfscodeSEDs, 3);
		// Map<String, BigDecimal> map;
		// String subgraphID = "";
		// BigDecimal distance = null;
		// for (int i = 0; i < results.size(); i++) {
		// map = results.get(i);
		// if (map.size() == 1) {
		// subgraphID = (String) map.keySet().toArray()[0];
		// distance = (BigDecimal) map.values().toArray()[0];
		// } else {
		// System.out
		// .println("The size of each map in results list is not equal to one!");
		// System.exit(-1);
		// }
		// System.out.println("The distance for subgraph " + subgraphID
		// + " is " + distance);
		// }

		// Iterator it = map.entrySet().iterator();
		// while (it.hasNext()) {
		// System.out.println(it.next().toString());
		// }

		// String s = "\\(\\d.\\d:.-?1.\\d+.\\d+.\\d+\\)";
		// String s = "\\d";
		// Pattern p = Pattern.compile(s);
		// Matcher m = p.matcher("(0 1: 1 0 0 2)");
		// while(m.find()){
		// System.out.println(m.group());
		// }
		// String s1 = " -1 0 0 2";
		// String[] temp = s1.trim().split(" ");
		// System.out.println(temp.length);
		// for(String s:temp){
		// System.out.println(s);
		// }
	}

}
