package org.jtang.function;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author Bin
 * @version Creation time：2012-2-17
 */
public class getNodeSequence {

	public static int TestSetSize = 10;
	public static int numberOfFlows = 500;
	public static int numberOfTotalNodes = 50;
	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
//		int TestSetSize = 10;
//		int numberOfFlows = 100;
//		int numberOfTotalNodes = 10;
		
		long t0 = System.currentTimeMillis();
		String inputName = System.getProperty("user.dir") + "\\" + "workflow"
				+ "\\" + "test" + "-" + numberOfTotalNodes + "-"
				+ numberOfFlows + "-" + TestSetSize + ".sdg";
		String outputName = System.getProperty("user.dir") + "\\" + "workflow"
				+ "\\" + "test" + "-" + numberOfTotalNodes + "-"
				+ numberOfFlows + "-" + TestSetSize + ".dat";
		File f = new File(inputName);
		File file = new File(outputName);
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
//				sb.append("digraph to {\n");
				
				//判断是否只有一个节点
				if(rows.length>=2){
					
					for (int row = 0; row < rows.length - 1; row++) {
					      String[] cols = rows[row + 1].split(" ");
					      int node = Integer.parseInt(cols[row]);
					      sb.append(node+",");
					    }
				}else{
					//如果只有一个节点，则将节点写入
					String node = rows[1].split(" ")[0] ;
					sb.append(node) ;
				}
				sb.append("end;");
				
				//添加次数
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
		//System.out.println("the parse time is:"+(t1-t0)/1000.0);
	}

}
