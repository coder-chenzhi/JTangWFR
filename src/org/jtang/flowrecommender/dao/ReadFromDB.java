package org.jtang.flowrecommender.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.jtang.flowrecommender.bean.Pattern;
import org.jtang.frequency.gspan.Fragments;

public class ReadFromDB {

	private String driver = "com.mysql.jdbc.Driver";
	private String url = "jdbc:mysql://121.42.201.251:3306/test" ;
	private String id = "jtangwfr" ;
	private String pwd = "sa" ;
	private Connection con ;
	private Statement s ;
	private PreparedStatement ps ;
	private ResultSet rs ;
	
	public ReadFromDB(){
		try {
//			连接驱动
			Class.forName(driver) ;
//			连接数据库
			con = DriverManager.getConnection(url,id,pwd) ;
//			获取数据库操作对象
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
//从数据库中读取所有包含节点v的工作流
	public List<String> readFlow(String str){
		List<String> l = new ArrayList<String>() ;
		String sql = "select * from flow where content like ?" ;
		try {
			ps = con.prepareStatement(sql) ;
			ps.setString(1, "_%"+str+"%") ;
			//System.out.println("%"+str+"%");
			rs = ps.executeQuery() ;
			while(rs.next()){
				l.add(rs.getString(1)) ;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//close() ;
		return l ;
	}
//	统计出现的次数
	public int getCount(String str){
		int i=0 ;
		String sql = "select count(*) from flow where content like ?" ;
		try {
			ps = con.prepareStatement(sql) ;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			ps.setString(1, "%"+str+"%") ;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			//System.out.println("%"+str+"%");
			rs = ps.executeQuery() ;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			while(rs.next()){
				i = rs.getInt(1) ;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//close() ;
		return i ;
	}
//	统计节点出点的次数，以降序排列
	public int getNodeCount(String str){
		int i=0 ;
		String sql = "" ;
		try {
			
			rs = ps.executeQuery() ;
			while(rs.next()){
				i = rs.getInt(1) ;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//close() ;
		return i ;
	}
	//从数据库中读取所有包含子路径sub的节点
	public List readNodes(String str){
		List l = new ArrayList() ;
		String sql = "select * from pattern where subpath=?" ;
		try {
			ps = con.prepareStatement(sql) ;
			ps.setString(1, str) ;
			//System.out.println("%"+str+"%");
			rs = ps.executeQuery() ;
			while(rs.next()){
				Pattern p = new Pattern() ;
				p.setNode(rs.getString(1)) ;
				p.setSubpath(rs.getString(2)) ;
				p.setBegindist(rs.getInt(3)) ;
				p.setEnddist(rs.getInt(4)) ;
				p.setNum(rs.getInt(5)) ;
				p.setCounttogether(rs.getInt(6)) ;
				p.setCountsub(rs.getInt(7)) ;
				p.setConf(rs.getDouble(8)) ;
				l.add(p) ;
				//System.out.println(rs.getString(1));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//close() ;
		return l ;
	}
	
	//从数据库中读取所有包含子路径sub的节点
	public List<Fragments> readData(int subnodenumber,double confident){
		List<Fragments> l = new ArrayList<Fragments>() ;
		String sql = "select * from patterntemp where subnodenumber<=? and confident>=?" ;
		//changed by tokyo @ 2012-4-20
//		String sql = "select * from patterntemp where subnodenumber=? and confident>=?" ;
		try {
			ps = con.prepareStatement(sql) ;
			ps.setInt(1, subnodenumber) ;
			ps.setDouble(2, confident) ;
			rs = ps.executeQuery() ;
			while(rs.next()){
				Fragments frag = new Fragments() ;
				frag.setNode(rs.getString(1)) ;
				frag.setSubpath(rs.getString(2)) ;
				frag.setFragment(rs.getString(3)) ;
				frag.setNodeNumber(rs.getInt(4)) ;
				frag.setSubNodeNumber(rs.getInt(5)) ;
				frag.setTailNodeNumber(rs.getInt(6)) ;
				frag.setFragmentcount(rs.getInt(7)) ;
				frag.setSubpathcount(rs.getInt(8)) ;
				frag.setConfident(rs.getDouble(9)) ; 
				frag.setDfscode(rs.getString(10)) ; //added by tokyo @ 2012-4-20
				l.add(frag) ;
				//System.out.println(rs.getString(1));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//close() ;
		return l ;
	}
	/**
	 * read data whose subnodenumber equal the given subnodenumber
	 * @param subnodenumber
	 * @param confident
	 * @return
	 */
	public List<Fragments> readDataBySize(int subnodenumber,double confident){
		List<Fragments> l = new ArrayList<Fragments>() ;
//		String sql = "select * from patterntemp where subnodenumber<=? and confident>=?" ;
		//changed by tokyo @ 2012-4-20
		String sql = "select * from patterntemp where subnodenumber=? and confident>=?" ;
		try {
			ps = con.prepareStatement(sql) ;
			ps.setInt(1, subnodenumber) ;
			ps.setDouble(2, confident) ;
			rs = ps.executeQuery() ;
			while(rs.next()){
				Fragments frag = new Fragments() ;
				frag.setNode(rs.getString(1)) ;
				frag.setSubpath(rs.getString(2)) ;
				frag.setFragment(rs.getString(3)) ;
				frag.setNodeNumber(rs.getInt(4)) ;
				frag.setSubNodeNumber(rs.getInt(5)) ;
				frag.setTailNodeNumber(rs.getInt(6)) ;
				frag.setFragmentcount(rs.getInt(7)) ;
				frag.setSubpathcount(rs.getInt(8)) ;
				frag.setConfident(rs.getDouble(9)) ; 
				frag.setDfscode(rs.getString(10)) ; //added by tokyo @ 2012-4-20
				l.add(frag) ;
				//System.out.println(rs.getString(1));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//close() ;
		return l ;
	}
	
//	读取输入类型与指定节点的输出类型相符的节点，返回list
	public List<String> readNodeByType(String node){
		List<String> l = new ArrayList<String>() ;
		String sql = "select node from node where intype=(select outtype from node where node=?) and node<>?" ;
		try {
			ps = con.prepareStatement(sql) ;
			ps.setString(1, node) ;
			ps.setString(2, node) ;
			rs = ps.executeQuery() ;
			while(rs.next()){
				l.add(rs.getString(1)) ;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return l ;
	}
	
//	通过节点，读取所有有影响的上流子路径
	public List<Pattern> readInfluenceSubStrings(String node){
		List<Pattern> l = new ArrayList<Pattern>() ;
		String sql="select * from pattern where node=?" ;
		try {
			ps = con.prepareStatement(sql) ;
			ps.setString(1, node) ;
			rs = ps.executeQuery() ;
			while(rs.next()){
				Pattern p = new Pattern() ;
				p.setNode(rs.getString(1)) ;
				p.setSubpath(rs.getString(2)) ;
				p.setBegindist(rs.getInt(3)) ;
				p.setEnddist(rs.getInt(4)) ;
				p.setNum(rs.getInt(5)) ;
				
				p.setConf(rs.getDouble(8)) ;
				l.add(p) ;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return l ;
	}
	
//	关闭
	public void close(){
		try {
			if(rs!=null){
				rs.close();
			}
			if(s!=null){
				s.close() ;
			}
			if(ps!=null){
				ps.close() ;
			}
			con.close() ;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
