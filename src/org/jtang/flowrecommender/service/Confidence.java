package org.jtang.flowrecommender.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

import org.jtang.flowrecommender.newrec.GetFlowsFromFile;
import org.jtang.flowrecommender.originalrecommender.CalculateConfidence;
import org.jtang.flowrecommender.bean.Pattern;
import org.jtang.flowrecommender.dao.ReadFromDB;
import org.jtang.flowrecommender.dao.WriteToDB;

//获得有影响的上游子路径
public class Confidence {
	public void register() throws Exception {

		int numberOfTotalNodes = GetFlowsFromFile.numberOfTotalNodes;
		String driver = "com.mysql.jdbc.Driver";
		String url = "jdbc:mysql://121.42.201.251:3306/test";
		String id = "jtangwfr";
		String pwd = "sa";

		List<Pattern> listForPattern = new ArrayList<Pattern>() ;
		// 对节点v，找到所有包含节点v在内的工作流
		for (int i = 0; i < numberOfTotalNodes; i++) {
			char c = (char) (97 + i);
			String str = "";
			str += c;
			// System.out.println("##########################"+str) ;
			List<String> list = new ArrayList<String>();
			ReadFromDB rDb = new ReadFromDB();
			list = rDb.readFlow(str);
			for (int j = 0; j < list.size(); j++) {
				// 得到单个工作流
				String w = list.get(j);
				// System.out.println(w) ;
				String sub = w.split(str)[0];
				// System.out.println("sub strings:"+sub);
				List subList = new ArrayList();
				subList = new SubStrings().getAllSubStrings(sub);
				// 循环
				for (int a = 0; a < subList.size(); a++) {
					// 得到其中一条的上流子路径
					String subString = (String) subList.get(a);
					// 计算置信度和支持度
					String temp[] = subString.split("-->");
					// System.out.println(temp[0]) ;
					String s = "";
					int k = 0;
					String temp01[] = temp[0].split(",");
					String temp03[] = temp[temp.length - 1].split(",");
					k = Integer.parseInt(temp01[1]);
					s += temp01[0];
					// 得到上流子路径
					for (int m = 1; m < temp.length; m++) {
						String temp02[] = temp[m].split(",");
						int distance = k - Integer.parseInt(temp02[1]);
						k = Integer.parseInt(temp02[1]);
						for (int n = 0; n < distance - 1; n++) {
							s += "_";
						}
						s += temp02[0];
					}

					int freq_p = rDb.getCount(s);
					String b = "";
					for (int m = 0; m < k - 1; m++) {
						b += "_";
					}
					int freq_vp = rDb.getCount(s + b + str);
					// System.out.println(freq_p);
					// System.out.println(freq_vp);
					// System.out.println((double)freq_vp/(double)freq_p);
					double conf = (double) freq_vp / (double) freq_p;

					// 在此处添加支持度，如果子路径只出现过一次，我们不认为它是有影响的上游子路径
					if (conf >= 0.15) {
						Pattern p = new Pattern() ;
						p.setBegindist(Integer.parseInt(temp01[1])) ;
						p.setConf(conf) ;
						p.setCountsub(freq_p) ;
						p.setCounttogether(freq_vp) ;
						p.setEnddist(Integer.parseInt(temp03[1])) ;
						p.setNode(str) ;
						p.setNum(temp.length) ;
						p.setSubpath(subString) ;
						listForPattern.add(p) ;
					} else {
					}
				}

			}
			rDb.close() ;
		}

		
		// 连接驱动
		Class.forName(driver);
		// 连接数据库
		Connection con = DriverManager.getConnection(url, id, pwd);
		// 获取数据库操作对象
		con.setAutoCommit(false);
		PreparedStatement ps = (PreparedStatement) con
				.prepareStatement("insert into pattern values (?,?,?,?,?,?,?,?)");
		
		for(int i=0;i<listForPattern.size();i++){
			Pattern pattern = listForPattern.get(i) ;
			
			ps.setString(1, pattern.getNode());
			ps.setString(2, pattern.getSubpath());
			ps.setInt(3, pattern.getBegindist());
			ps.setInt(4, pattern.getEnddist());
			ps.setInt(5, pattern.getNum());
			ps.setInt(6, pattern.getCounttogether());
			ps.setInt(7, pattern.getCountsub());
			ps.setDouble(8, pattern.getConf());
			// 把一个SQL命令加入命令列表
			ps.addBatch();
		}
		
		try {
			ps.executeBatch();
		} catch (Exception e) {
			// TODO: handle exception
		}
		// 语句执行完毕，提交本事务
		con.commit();
		ps.close();
		con.close();

	}

	public static void main(String args[]) throws Exception {
		long start = System.currentTimeMillis();
		Confidence c = new Confidence();
		c.register();
		long end = System.currentTimeMillis();
		System.out.print(end - start);
	}
}
