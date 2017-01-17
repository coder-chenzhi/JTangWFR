package org.jtang.flowrecommender.dao;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class WriteToDB {

	private String driver = "com.mysql.jdbc.Driver";
	private String url = "jdbc:mysql://121.42.201.251:3306/test" ;
	private String id = "jtangwfr" ;
	private String pwd = "sa" ;
	private Connection con ;
	private Statement s ;
	private PreparedStatement ps ;
	private ResultSet rs ;
	
	public WriteToDB(){
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
			//e.printStackTrace();
		}
	}
	
//添加到数据库
	public void add(String str){
		String sql = "insert into flow values (?)" ;
		try {
			ps = con.prepareStatement(sql) ;
			ps.setString(1, str) ;
			ps.executeUpdate() ;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		//close() ;
	}
	//添加到数据库
	public void addvp(String str,String node,int begindist,int enddist, int number,int together,int sub,double conf){
		String sql = "insert into pattern values (?,?,?,?,?,?,?,?)" ;
		try {
			ps = con.prepareStatement(sql) ;
			ps.setString(1, node) ;
			ps.setString(2, str) ;
			ps.setInt(3, begindist) ;
			ps.setInt(4, enddist) ;
			ps.setInt(5, number) ;
			ps.setInt(6, together) ;
			ps.setInt(7, sub) ;
			ps.setDouble(8, conf) ;
			ps.executeUpdate() ;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		//close() ;
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
			//e.printStackTrace();
		}
		
	}
}
