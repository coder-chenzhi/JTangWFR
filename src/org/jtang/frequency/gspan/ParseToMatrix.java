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

public class ParseToMatrix {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		final int MAXNODENUM = 52;
		long t0 = System.currentTimeMillis();
		File f = new File(System.getProperty("user.dir") + "\\"
				+ "frequentFrags.dat");
		File file = new File(System.getProperty("user.dir") + "\\"
				+ "MatrixWithoutNodeInfo.dat");
		InputStream in = new FileInputStream(f);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));

		String line = br.readLine();
		int count = Integer.parseInt(line);// 文件中所包含的频繁子图的个数

		StringBuffer buf = new StringBuffer(2048);
		StringBuffer sb = new StringBuffer();
		// int maxNodeNum = 0;
		while ((line = br.readLine()) != null) {
			// 开始处理一个图
			// int[][] tempMatrix = new int[MAXNODENUM][MAXNODENUM];

			if (line.startsWith("#")) {
				String[] rows = buf.toString().split("\n");
				// 子图id
				String subGraphID = rows[0];
				sb.append("Matrix of " + subGraphID + " is :\n");
				int[] nodes = new int[rows.length - 1];
				int[][] tempMatrix;
				// 判断是否只有一个节点
				if (rows.length != 2) {
					for (int row = 0; row < rows.length - 1; row++) {
						String[] cols = rows[row + 1].split(" ");
						// 对角线上的数据；最后nodes数组包含该图所含的所有节点
						nodes[row] = Integer.parseInt(cols[row]);
						// 记录最大出现的节点
						// maxNodeNum = Math.max(maxNodeNum, nodes[row]);
					}
					int nodesSize = nodes.length;
					tempMatrix = new int[nodesSize][nodesSize];

					for (int row = 0; row < rows.length - 1; row++) {
						String[] cols = rows[row + 1].split(" ");
						for (int col = cols.length - 1; col >= 0; col--) {
							if ((row != col) && (cols[col].charAt(0) != '-')) {
								int startNode = nodes[row];
								int endNode = nodes[col];
								// tempMatrix[startNode - 1][endNode - 1] = 1;
								tempMatrix[row][col] = 1;
							}
						}
					}

				} else {
					// 如果只有一个节点???
					int nodesSize = 1;
					tempMatrix = new int[nodesSize][nodesSize];
					String node = rows[1].split(" ")[0];
					int nodeNum = Integer.parseInt(node);
					tempMatrix[0][0] = 1;
				}
				// 将矩阵信息写到文件
				for (int i = 0; i < tempMatrix.length; i++) {
					for (int j = 0; j < tempMatrix[0].length; j++) {
						sb.append(tempMatrix[i][j]).append(
								j == tempMatrix[0].length - 1 ? "\n" : ' ');
					}
				}
				sb.append("\n");
				// 刷新buf
				buf = new StringBuffer(2048);
			} else {
				buf.append(line).append('\n');
			}
		}
		// System.out.println("The maxNodeNum is: " + maxNodeNum);

		FileOutputStream fos = new FileOutputStream(file);
		fos.write(sb.toString().getBytes());
		fos.flush();
		fos.close();
		// System.out.println("over-------------");
		long t1 = System.currentTimeMillis();
		System.out.println("the parse time is:" + (t1 - t0) / 1000.0);
	}

}
