package org.jtang.distance.mcs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jtang.distance.dfscode.DFSCodeSED;
import org.jtang.distance.dfscode.DFSMiner;
import org.jtang.distance.dfscode.GraphDatabaseUtil;

import de.parsemis.DFSCodeUtil;
import de.parsemis.Miner;

/**
 * @author
 */
public class PreProcess {

	private static String driver = "com.mysql.jdbc.Driver";
	private static String url = "jdbc:mysql://121.42.201.251:3306/test";
	private static String id = "jtangwfr";
	private static String pwd = "sa";
	private static Connection con;
	private static PreparedStatement ps;
	private static ResultSet rs;

	public static String addUnrelatedNode(StringBuffer digraph,String nodes){
		String[] node = nodes.split(",") ;
		String newDigraph = "" ;
		if(digraph.length()<5){
			return node[0]+" -> 53;\n" ;
		}else{
			newDigraph += digraph.toString() ;
			for(String lastnode:node){
				newDigraph += lastnode+" -> 53;\n" ;
			}
			return newDigraph;
		}
		
	}
	/**
	 * translate the ged graph to matrix
	 * @param ged
	 * @return
	 */
	public static String gedToMatrix(String ged) {
		String temp[] = ged.split("\n") ;
		Set<String> nodeSet = new HashSet<String>() ;
		Set<String> edgeSet = new HashSet<String>() ;
		
		//get all the nodes from the ged diagraph
		for(int i=1;i<temp.length-1;i++){
			String[] bytes = temp[i].split(" ") ;
			if(bytes.length>2){
				nodeSet.add(bytes[0]) ;
				nodeSet.add(bytes[2].split(";")[0]) ;
				edgeSet.add(temp[i]) ;
			}else{
				String node[] = temp[i].split(";") ;
				nodeSet.add(node[0]) ;
				nodeSet.add("53") ;
				edgeSet.add(node[0] + " -> 53;") ;
			}
		}
		
		//initialize the matrix
		String matrix[][] = new String[nodeSet.size()][nodeSet.size()] ;
		for(int m=0;m<nodeSet.size();m++){
			for(int n=0;n<nodeSet.size();n++){
				matrix[m][n] = "-" ;
			}
		}
		
		//add all the nodes into the matrix
		Iterator<String> nodeIterator = nodeSet.iterator() ;
		int k = 0 ;
		while(nodeIterator.hasNext()){
			String node = nodeIterator.next() ;
			matrix[k][k] = node ;
			k++ ;
		}
		
		//add all the edges into the matrix
		Iterator<String> edgeIterator = edgeSet.iterator() ;
		while(edgeIterator.hasNext()){
			String edge = edgeIterator.next() ;
			String bytes[] = edge.split(" ") ;
			String begin =  bytes[0];
			String end = bytes[2].split(";")[0];
			int row = 0 ;
			int col = 0 ;
			for(int m=0;m<nodeSet.size();m++){
				if(matrix[m][m].equals(begin)){
					row = m ;
				}else if(matrix[m][m].equals(end)){
					col = m ;
				}
			}
			matrix[row][col] = "1" ;
		}
		
		//change the matrix to String
		StringBuffer result = new StringBuffer() ;
		for(int m=0;m<nodeSet.size();m++){
			for(int n=0;n<nodeSet.size();n++){
				result.append(matrix[m][n] + " ") ;
			}
			result.append("\n") ;
		}
		return result.toString();
	}
	
	public static String MatrixToDFSCode(String matrix){
		
		writeToFile(matrix);
		String envFile = Miner.getEnvFile();
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
		
		Map currentCodeMap = DFSCodeSED.parseDFSCodeMap(DFSCodeUtil.fragcodes);
		DFSCodeUtil.fragcodes = new HashMap() ;
		if (currentCodeMap.size() == 0) {
			System.out.println("There is no dfscode generated!!");
//			System.exit(-1);// 非正常退出
			return null ;
		} 
		else if (currentCodeMap.size() > 1) {
			System.out.println("The dfscode for the graph which is matched should not exceed one!!");
//			System.exit(-1);// 非正常退出
			return null ;
		}
		return currentCodeMap.values().toArray()[0].toString();
	}
	private static String[] constructArgs() {
		String graph = System.getProperty("user.dir") + "\\"
		+ "DFSCodeData" + "\\"+"tempMatrix.sdg";
		String[] args = new String[4];
		args[0] = "--graphFile="+graph;
		args[1] = "--minimumFrequency=0";
//		args[2] = "--outputFile=d:/dfscode4partialgraph.dat";
		//Algorithm4DFS
		args[2] = "--algorithm=gspan4dfscode";//挖掘待匹配图的gSpan算法，该算法共享了线下挖掘频繁子图时的图数据库，即节点与边的顺序
		//RecursiveStrategy4DFSCode
		args[3] = "--distribution=dfscode";//使用新的策略提取待匹配图的DFScode，即过滤掉子图的DFSCode，只保留自身的DFSCode
		return args;

	}
	public static void writeToFile(String content){
		FileWriter fw;
		try {
			String graph = System.getProperty("user.dir") + "\\"
			+ "DFSCodeData" + "\\"+"tempMatrix.sdg";
			fw = new FileWriter(graph,false);
			fw.write(content,0,content.length());
			fw.flush();
			fw.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static List<Dfscode> process() {
		List<Dfscode> l = new ArrayList<Dfscode>();
		try {
			// 连接驱动
			Class.forName(driver);
			// 连接数据库
			con = DriverManager.getConnection(url, id, pwd);
			// 获取数据库操作对象
			String sql = "select * from patterntemp";

			ps = con.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				Dfscode d = new Dfscode() ;
				d.setSubpath(rs.getString(2));
				l.add(d);
			}
			rs.close() ;
			ps.close() ;
			con.close() ;
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return l ;
	}
	
	public static void writeToDbByBatch(List<Dfscode> list){
		
		try {
			// 连接驱动
			Class.forName(driver);
			// 连接数据库
			con = DriverManager.getConnection(url, id, pwd);
			// 获取数据库操作对象
			con.setAutoCommit(false);		
			ps = con.prepareStatement("update patterntemp set dfscode=? where subpath=?");

			for(int i=0;i<list.size();i++){
				Dfscode dfs = list.get(i) ;
				if(dfs.getDfscode()!=null||!"".equals(dfs.getDfscode())){
					ps.setString(1, dfs.getDfscode());
					ps.setString(2, dfs.getSubpath());
					//把一个Sql命令加入命令列表
					ps.addBatch() ;
				}
			}
			ps.executeBatch() ;
			con.commit() ;

			ps.close() ;
			con.close() ;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		List<Dfscode> list = new ArrayList<Dfscode>();
		list = process() ;
		for(int i=0;i<list.size();i++){
			Dfscode d = new Dfscode() ;
			d = list.get(i) ;
			String ged = d.getSubpath() ;
			String matrix = "1\n1\n"+gedToMatrix(ged) +"#";
			String s =  MatrixToDFSCode(matrix);
			d.setDfscode(s) ;
		}
		writeToDbByBatch(list) ;
			
		//test program
//		String ged = "digraph to{\n1 -> 2;\n3 -> 4;\n4;\n2;\n}" ;
//		System.out.println(ged) ;
//		String matrix = "1\n1\n"+gedToMatrix(ged) +"#";
//		System.out.println(matrix);
	}

}
