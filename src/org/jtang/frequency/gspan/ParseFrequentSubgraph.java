package org.jtang.frequency.gspan;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;

import sun.awt.windows.ThemeReader;

/**
 * @author Bin
 * @version Creation time：2012-2-17
 */
public class ParseFrequentSubgraph {

	/**
	 * @param args
	 * @throws IOException
	 * 基本原理是，除了对角线之外，所有为1的位置，表示从数字1所在行对应的非1节点，
	 * 到数字1所在列对应的非1节点，之间存在一条边
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		long t0 = System.currentTimeMillis();
		File f = new File(System.getProperty("user.dir")+"\\result\\"+"frequentFrags.dat");
		File file = new File(System.getProperty("user.dir")+"\\result\\"+"editInput.dat");
		InputStream in = new FileInputStream(f);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));

		String line = br.readLine();
		int count = Integer.parseInt(line);// 文件中所包含的频繁子图的个数

		StringBuffer buf = new StringBuffer(2048);
		StringBuffer sb = new StringBuffer();
		while ((line = br.readLine()) != null) {
			if (line.startsWith("#")) {
				String[] rows = buf.toString().split("\n");
				String subGraphID = rows[0];
				sb.append("digraph to {\n");
				int[] nodes = new int[rows.length - 1];
				
				//判断是否只有一个节点
				if(rows.length!=2){
					for (int row = 0; row < rows.length - 1; row++) {
					      String[] cols = rows[row + 1].split(" ");
					      nodes[row] = Integer.parseInt(cols[row]);
					    }
					for (int row = 0; row < rows.length - 1; row++) {
						String[] cols = rows[row + 1].split(" ");
						for (int col = cols.length - 1; col >= 0; col--) {
							if ((row != col) && (cols[col].charAt(0) != '-')) {
								sb.append(nodes[row] + " -> " + nodes[col] + ";\n");
							}
						}
					}
				}else{
					//如果只有一个节点，则将节点写入
					String node = rows[1].split(" ")[0] ;
					sb.append(node + ";\n") ;
				}
				sb.append("}\n");
				
				//添加次数
				sb.append(line.split(" ")[2]).append("\n") ;
				buf = new StringBuffer(2048);
			} else {
				buf.append(line).append('\n');
			}
		}
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(sb.toString().getBytes());
		fos.flush();
		fos.close();
		//System.out.println("over-------------");
		long t1 = System.currentTimeMillis();
		System.out.println("the parse time is:"+(t1-t0)/1000.0);
	}

}
