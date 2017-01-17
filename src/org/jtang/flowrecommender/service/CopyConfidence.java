package org.jtang.flowrecommender.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

import org.jtang.flowrecommender.newrec.GetFlowsFromFile;
import org.jtang.flowrecommender.originalrecommender.CalculateConfidence;
import org.jtang.flowrecommender.dao.ReadFromDB;
import org.jtang.flowrecommender.dao.WriteToDB;

//获得有影响的上游子路径
public class CopyConfidence {
	public void register() throws Exception{

		int numberOfTotalNodes = GetFlowsFromFile.numberOfTotalNodes;
		String driver = "com.mysql.jdbc.Driver";
		String url = "jdbc:mysql://121.42.201.251:3306/test";
		String id = "jtangwfr";
		String pwd = "sa";
		// 连接驱动
		Class.forName(driver);
		// 连接数据库
		Connection con = DriverManager.getConnection(url, id, pwd);
		// 获取数据库操作对象
		con.setAutoCommit(false);
		PreparedStatement ps = (PreparedStatement) con.prepareStatement("insert into pattern values (?,?,?,?,?,?,?,?)");
		
		ReadFromDB rDb = new ReadFromDB() ;
		//对节点v，找到所有包含节点v在内的工作流
		for(int i=0;i<numberOfTotalNodes;i++){
			char c = (char)(97+i) ;
			String str = "" ;
			str += c ;
			//System.out.println("##########################"+str) ;
			List<String> list = new ArrayList<String>() ;
			list = rDb.readFlow(str) ;
			/*for(int i=0;i<list.size();i++){
				System.out.println(list.get(i));
			}*/
			//System.out.println(list.get(0)) ;
			for(int j=0;j<list.size();j++){
				//得到单个工作流
				String w = list.get(j) ;
				//System.out.println(w) ;
				String sub  = w.split(str)[0] ;
				//System.out.println("sub strings:"+sub);
				List subList = new ArrayList() ;
				subList = new SubStrings().getAllSubStrings(sub) ;
				//循环
				for(int a=0;a<subList.size();a++){
					//得到其中一条的上流子路径
					String subString = (String)subList.get(a) ;
					//计算置信度和支持度
					new CalculateConfidence().getConfidence(subString, str, rDb, ps) ;
				}
			}
		}
		try{
			ps.executeBatch() ;
		}catch (Exception e) {
			// TODO: handle exception
		}
		// 语句执行完毕，提交本事务
		con.commit();
		ps.close() ;
		con.close() ;
		rDb.close();
		
	}
	public static void main(String args[]) throws Exception{
		long start = System.currentTimeMillis();
		CopyConfidence c = new CopyConfidence() ;
		c.register() ;
		long end = System.currentTimeMillis();
		System.out.print(end-start) ;
	}
}
