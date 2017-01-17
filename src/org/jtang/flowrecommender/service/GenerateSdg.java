package org.jtang.flowrecommender.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

public class GenerateSdg {

	/**
	 * file		：存放工作流的文件
	 * nodeNumber：当前工作流的节点总数
	 * edgeNumber：当前工作流的边总数
	 */
	private File file ;
	private File testFile ;
	private FileWriter fileWriter ;
	private FileWriter testFileWriter ;
	private int nodeFrom ;
	private int nodeTo ;
	private int flowNumber ;
	private int minNodenumber ;
	private int maxNodenumber ;
	private int[][] matric ;
	
	/**
	 * 
	 * @param nodeFrom	：节点开始的范围
	 * @param nodeTo	：节点结束的范围
	 * @param flowNumber：生成的工作流的数目
	 */
	public GenerateSdg(int nodeFrom,int nodeTo,int flowNumber,int minNodenumber,int maxNodenumber){

		this.nodeFrom = nodeFrom ;
		this.nodeTo = nodeTo ;
		this.flowNumber = flowNumber ;
		this.minNodenumber = minNodenumber ;
		this.maxNodenumber = maxNodenumber ;
		

	}
	
	/**
	 * 生成规则矩阵
	 */
	public void generateMatric(){
		Random rand = new Random();
		int nodeNumber = minNodenumber + rand.nextInt(maxNodenumber-minNodenumber+1) ;
		matric = new int[nodeNumber][nodeNumber] ;
		
		//随即生成工作流节点
		Set <Integer> nodeSet = new HashSet<Integer>() ;
		int index = 0 ;
		
		while(nodeSet.size()<nodeNumber){
			int m = 1 + rand.nextInt(nodeTo) ;
			if(!nodeSet.contains(m)){
				nodeSet.add(m) ;
				matric[index][index] = m ;
				index++ ;
			}
		}		
		
		//随机生成边
		int rowEdge[] = new int[nodeNumber] ;//统计横向边的数量
		int colEdge[] = new int[nodeNumber] ;//统计纵向边的数量
		//保证只有一个起点，每条纵向边都必须至少有一个为1
		for(int col=1;col<nodeNumber;col++){
			int edgeNumber = 1 + rand.nextInt(2) ;
			if(col==1||col==2){
				edgeNumber = 1 ;
			}else if(col==nodeNumber-1){
				//用于保证工作流只有一个终点
				matric[col-1][col] = 1 ;
				edgeNumber = rand.nextInt(2) ;
			}
			while(edgeNumber>0){
				int row = rand.nextInt(col) ;//随机某行
				if(matric[row][col]!=1&&rowEdge[row]<2){
					matric[row][col] = 1 ;
					edgeNumber-- ;
					rowEdge[row]++ ;
				}
			}
		}
		
		//保证只有一个终点，每条横向边都必须至少有一个为1
		for(int row=0;row<nodeNumber-1;row++){
			if(rowEdge[row]==0){
				int col = row + 1 + rand.nextInt(nodeNumber-row-1) ;
				matric[row][col] = 1 ;
			}
		}
	}
		

	
	/**
	 * 写入文件
	 * @param flowNumber：生成的工作流的数目
	 */
	public void start(){
		try {
			file = new File(System.getProperty("user.dir")+"\\"+"graphSet.sdg") ;
			try {
				fileWriter = new FileWriter(file, true) ;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			fileWriter.write(String.valueOf(flowNumber-10)) ;
			fileWriter.write("\n") ;
			for(int i=0;i<flowNumber-10;i++){
				String str = generateFlow(i+1) ;
				fileWriter.write(str) ;
			}
			//关闭输入流，写入文件
			fileWriter.close() ;
			
			
			testFile = new File(System.getProperty("user.dir")+"\\"+"graphSetTest.sdg") ;
			testFileWriter = new FileWriter(testFile, true) ;
			testFileWriter.write(String.valueOf(10)) ;
			testFileWriter.write("\n") ;
			
			for(int i=flowNumber-10;i<flowNumber;i++){
				String str = generateFlow(i+1) ;
				testFileWriter.write(str) ;
			}
			//关闭输入流，写入文件
			testFileWriter.close() ;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String generateFlow(int i){
		StringBuffer graph = new StringBuffer() ;
		
		graph.append("G" + i + "\n") ;
		generateMatric() ;
		for(int[] row:matric){
			for(int col:row){
				if(col==0){
					graph.append("- ") ;
				}else{
					graph.append(col + " ") ;
				}
			}
			graph.append("\n") ;
		}
		graph.append("#\n") ;
		
		return graph.toString() ;
	}
	
	public static void main(String args[]){
		GenerateSdg gsTest = new GenerateSdg(1,10,1000,5,10) ;
		gsTest.start() ;
	}
}
