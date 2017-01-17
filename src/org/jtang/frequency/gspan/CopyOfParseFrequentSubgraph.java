package org.jtang.frequency.gspan;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import sun.awt.windows.ThemeReader;

/**
 * @author Bin
 * @version Creation time：2012-2-17
 */
public class CopyOfParseFrequentSubgraph {

	public static void parseFrequentSubgraph(String frequentFragsFile,
			String editInputFile) throws Exception {
		File frequentfrags = new File(frequentFragsFile);
		File editinput = new File(editInputFile);
		InputStream in = new FileInputStream(frequentfrags);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));

		String line = br.readLine();
		int count = Integer.parseInt(line);// 文件中所包含的频繁子图的个数

		StringBuffer buf = new StringBuffer(2048);
		StringBuffer sb = new StringBuffer();
		sb.append(count + "\n");
		int i = 0;
		while ((line = br.readLine()) != null) {
			if (line.startsWith("#")) {
				String[] rows = buf.toString().split("\n");
				String subGraphID = rows[0];
				sb.append(subGraphID + "\n");
				sb.append("digraph to {\n");
				int[] nodes = new int[rows.length - 1];

				// 判断是否只有一个节点
				if (rows.length != 2) {
					for (int row = 0; row < rows.length - 1; row++) {
						String[] cols = rows[row + 1].split(" ");
						nodes[row] = Integer.parseInt(cols[row]);
					}
					for (int row = 0; row < rows.length - 1; row++) {
						String[] cols = rows[row + 1].split(" ");
						for (int col = cols.length - 1; col >= 0; col--) {
							if ((row != col) && (cols[col].charAt(0) != '-')) {
								sb.append(nodes[row] + " -> " + nodes[col]
										+ ";\n");
							}
						}
					}
				} else {
					// 如果只有一个节点，则将节点写入
					String node = rows[1].split(" ")[0];
					sb.append(node + ";\n");
				}
				sb.append("}\n");
				// 添加次数
				// sb.append(line.split(" ")[2]).append("\n") ;
				buf = new StringBuffer(2048);
			} else {
				buf.append(line).append('\n');
			}
		}
		FileOutputStream fos = new FileOutputStream(editinput);
		fos.write(sb.toString().getBytes());
		fos.flush();
		fos.close();
		// System.out.println("over-------------");
	}
	
	/**
	 * 该方法是为了测试DFSCode时方便生成整数数据集
	 * @param frequentFragsFile
	 * @param editInputFile
	 * @throws Exception
	 */
	public static void parseFrequentSubgraph4dfstest(String frequentFragsFile,
			String editInputFile) throws Exception {
		File frequentfrags = new File(frequentFragsFile);
		File editinput = new File(editInputFile);
		InputStream in = new FileInputStream(frequentfrags);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));

		String line = br.readLine();
		int count = Integer.parseInt(line);// 文件中所包含的频繁子图的个数
		count = (int) Math.floor((count/1000))*1000;
		StringBuffer buf = new StringBuffer(2048);
		StringBuffer sb = new StringBuffer();
		sb.append(count + "\n");
		int i = 0;
		while ((line = br.readLine()) != null) {
			if (line.startsWith("#")) {
				String[] rows = buf.toString().split("\n");
				String subGraphID = rows[0];
				sb.append(subGraphID + "\n");
				sb.append("digraph to {\n");
				int[] nodes = new int[rows.length - 1];

				// 判断是否只有一个节点
				if (rows.length != 2) {
					for (int row = 0; row < rows.length - 1; row++) {
						String[] cols = rows[row + 1].split(" ");
						nodes[row] = Integer.parseInt(cols[row]);
					}
					for (int row = 0; row < rows.length - 1; row++) {
						String[] cols = rows[row + 1].split(" ");
						for (int col = cols.length - 1; col >= 0; col--) {
							if ((row != col) && (cols[col].charAt(0) != '-')) {
								sb.append(nodes[row] + " -> " + nodes[col]
										+ ";\n");
							}
						}
					}
				} else {
					// 如果只有一个节点，则将节点写入
					String node = rows[1].split(" ")[0];
					sb.append(node + ";\n");
				}
				sb.append("}\n");
				//取整数的频繁子图
				i++;
				if(i==count){
					break;
				}

				// 添加次数
				// sb.append(line.split(" ")[2]).append("\n") ;
				buf = new StringBuffer(2048);
			} else {
				buf.append(line).append('\n');
			}
		}
		FileOutputStream fos = new FileOutputStream(editinput);
		fos.write(sb.toString().getBytes());
		fos.flush();
		fos.close();
		// System.out.println("over-------------");
	}

	/**
	 * 解析频繁子图到Map中
	 * 
	 * @param frequentFragsFile
	 * @return
	 * @throws Exception
	 */
	public static Map<String, String> parseFromFragment(String frequentFragsFile)
			throws Exception {
		Map<String, String> map = new HashMap<String, String>();
		File frequentfrags = new File(frequentFragsFile);
		InputStream in = new FileInputStream(frequentfrags);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));

		String line = br.readLine();
		int count = Integer.parseInt(line);// 文件中所包含的频繁子图的个数

		StringBuffer buf = new StringBuffer(2048);
		StringBuffer sb = new StringBuffer();
		// sb.append(count + "\n");
		while ((line = br.readLine()) != null) {
			if (line.startsWith("#")) {
				String[] rows = buf.toString().split("\n");
				String subGraphID = rows[0];
				sb.append("digraph from {\n");
				int[] nodes = new int[rows.length - 1];

				// 判断是否只有一个节点
				if (rows.length != 2) {
					for (int row = 0; row < rows.length - 1; row++) {
						String[] cols = rows[row + 1].split(" ");
						nodes[row] = Integer.parseInt(cols[row]);
					}
					for (int row = 0; row < rows.length - 1; row++) {
						String[] cols = rows[row + 1].split(" ");
						for (int col = cols.length - 1; col >= 0; col--) {
							if ((row != col) && (cols[col].charAt(0) != '-')) {
								sb.append(nodes[row] + " -> " + nodes[col]
										+ ";\n");
							}
						}
					}
				} else {
					// 如果只有一个节点，则将节点写入
					String node = rows[1].split(" ")[0];
					sb.append(node + ";\n");
				}
				sb.append("}\n");
				map.put(subGraphID, sb.toString());

				// 添加次数
				// sb.append(line.split(" ")[2]).append("\n") ;
				buf = new StringBuffer(2048);
				sb = new StringBuffer();
			} else {
				buf.append(line).append('\n');
			}
		}
		if (count != map.size()) {
			System.out.println("Errors happened in parsing "
					+ frequentFragsFile + " !!!");
			System.exit(-1);
		}
		return map;
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		String frequentFragsFile = System.getProperty("user.dir") + "\\"
				+ "frequentFrags.dat";
		String editInputFile = System.getProperty("user.dir") + "\\"
				+ "DFSCodeData" + "\\" + "offline_decoratedgraphs.dat";
		parseFrequentSubgraph4dfstest(frequentFragsFile, editInputFile);
		System.out
				.println("Frequent subgraphs are all parsed to decorated graphs!!!");

		/*
		 * 写BPM2012论文时用的 long t0 = System.currentTimeMillis(); File f = new
		 * File(System.getProperty("user.dir")+"\\"+"frequentFrags.dat"); File
		 * file = new File(System.getProperty("user.dir")+"\\"+"editInput.dat");
		 * InputStream in = new FileInputStream(f); BufferedReader br = new
		 * BufferedReader(new InputStreamReader(in));
		 * 
		 * String line = br.readLine(); int count = Integer.parseInt(line);//
		 * 文件中所包含的频繁子图的个数
		 * 
		 * StringBuffer buf = new StringBuffer(2048); StringBuffer sb = new
		 * StringBuffer(); while ((line = br.readLine()) != null) { if
		 * (line.startsWith("#")) { String[] rows = buf.toString().split("\n");
		 * String subGraphID = rows[0]; sb.append("digraph to {\n"); int[] nodes
		 * = new int[rows.length - 1];
		 * 
		 * //判断是否只有一个节点 if(rows.length!=2){ for (int row = 0; row < rows.length
		 * - 1; row++) { String[] cols = rows[row + 1].split(" "); nodes[row] =
		 * Integer.parseInt(cols[row]); } for (int row = 0; row < rows.length -
		 * 1; row++) { String[] cols = rows[row + 1].split(" "); for (int col =
		 * cols.length - 1; col >= 0; col--) { if ((row != col) &&
		 * (cols[col].charAt(0) != '-')) { sb.append(nodes[row] + " -> " +
		 * nodes[col] + ";\n"); } } } }else{ //如果只有一个节点，则将节点写入 String node =
		 * rows[1].split(" ")[0] ; sb.append(node + ";\n") ; } sb.append("}\n");
		 * 
		 * //添加次数 sb.append(line.split(" ")[2]).append("\n") ; buf = new
		 * StringBuffer(2048); } else { buf.append(line).append('\n'); } }
		 * FileOutputStream fos = new FileOutputStream(file);
		 * fos.write(sb.toString().getBytes()); fos.flush(); fos.close();
		 * //System.out.println("over-------------"); long t1 =
		 * System.currentTimeMillis();
		 * System.out.println("the parse time is:"+(t1-t0)/1000.0);
		 */
	}

}
