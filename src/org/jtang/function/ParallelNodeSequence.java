package org.jtang.function;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.jtang.flowrecommender.newrec.ReadFile;

public class ParallelNodeSequence {
	public static int TestSetSize = 10;
	public static int numberOfFlows = 500;
	public static int numberOfTotalNodes = 20;

	public static String matrixToString(String[][] matrix){
		String sequenceString = "";
		sequenceString += (matrix[0][0]+";") ;
		for(int i=1;i<matrix.length;i++){
			for(int j=0;j<i;j++){
				if(matrix[j][i].equals("1")){
					sequenceString += (matrix[j][j]+",") ;
				}
			}
			sequenceString += ("-"+matrix[i][i]+";") ;
		}
		sequenceString += (matrix[matrix.length-1][matrix.length-1]+"-end.") ;
		System.out.println("sequenceString is"+ sequenceString);
		return sequenceString ;
	}
	
	public static void readFile() throws Exception{
		
//		String inputName = System.getProperty("user.dir") + "\\" + "workflow"
//				+ "\\" + "test" + "-" + numberOfTotalNodes + "-"
//				+ numberOfFlows + "-" + TestSetSize + ".sdg";
//		String outputName = System.getProperty("user.dir") + "\\" + "workflow"
//				+ "\\" + "test" + "-" + numberOfTotalNodes + "-"
//				+ numberOfFlows + "-" + TestSetSize + ".dat";
		
		String inputName = System.getProperty("user.dir") + "\\" + "workflow"
				+ "\\" +  "test.sdg";
		String outputName = System.getProperty("user.dir") + "\\" + "workflow"
				+ "\\" +"test.dat";
		
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
				String[][] matrix = new String[rows.length-1][rows.length-1] ;
				
//				System.out.println(rows.length);
				System.out.println(buf);
				//判断是否只有一个节点
				if(rows.length>=2){
					
					for (int row = 0; row < rows.length - 1; row++) {
						matrix[row] = rows[row + 1].split(" ");
//						System.out.println(matrix[row][rows.length-2]);
					}
					String sequenceString = matrixToString(matrix) ;
					sb.append(sequenceString) ;
//					for(int i=0;i<rows.length-1;i++){
//						for(int j=0;j<rows.length-1;j++){
//							System.out.print(matrix[i][j]+" ") ;
//						}
//					}
				}else{
					//如果只有一个节点，则将节点写入
					String node = rows[1].split(" ")[0] ;
					sb.append(node) ;
				}
//				System.out.println(sb);
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
	}
	
	public static void main(String args[]) throws Exception{

		readFile() ;
		
	}
}
