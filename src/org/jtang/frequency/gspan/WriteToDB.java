package org.jtang.frequency.gspan;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.jtang.flowrecommender.originalrecommender.Confidence;
import org.jtang.flowrecommender.service.FlowRecommender;
import org.jtang.flowrecommender.service.ParallelGEDFlowRecommender;
import org.omg.CORBA.Current;

public class WriteToDB {

	private String driver = "com.mysql.jdbc.Driver";
	private static String url = "jdbc:mysql://121.42.201.251:3306/test";
	private static String id = "jtangwfr";
	private static String pwd = "sa";
	private static Connection con;
	private Statement s;
	private static PreparedStatement ps;
	private ResultSet rs;

	public WriteToDB() {
		try {
			// 连接驱动
			Class.forName(driver);
			// 连接数据库
			con = DriverManager.getConnection(url, id, pwd);
			// 获取数据库操作对象
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// 添加节点和有影响的上游子路径到数据库
	public void addIntoPatternTable(String node, String path, String fragment, int nodeNumber, int subNodeNumber,
			int tailNodeNumber, int count) {
		String sql = "insert into patterntemp values (?,?,?,?,?,?,?,0,0)";
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, node);
			ps.setString(2, path);
			ps.setString(3, fragment);
			ps.setInt(4, nodeNumber);
			ps.setInt(5, subNodeNumber);
			ps.setInt(6, tailNodeNumber);
			ps.setInt(7, count);
			ps.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//close();
	}

	public void copy(Fragments frag) {
		String sql = "insert into patternforgspan values (?,?,?)";
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, frag.getNode());
			ps.setString(2, frag.getSubpath());
			ps.setDouble(3, frag.getConfident());
			ps.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		close();
	}

	// 更新数据库
	public void updatePatternTable(Fragments frag) {
		// System.out.println(frag.getConfident()) ;
		String sql = "update patterntemp set subpathcount=?, confident = ? where fragment=?";
		try {
			ps = con.prepareStatement(sql);
			ps.setInt(1, frag.getSubpathcount());
			ps.setDouble(2, frag.getConfident());
			ps.setString(3, frag.getFragment());
			ps.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// close() ;
	}

	// 更新数据库
	public void updatePatternTableTwo(Fragments frag) {
		String sql01 = "select fragmentcount from patterntemp where fragment=?" ;
		String sql = "update patterntemp set subpathcount=?, confident = ? where fragment=?";
		try {
			ps = con.prepareStatement(sql01) ;
			ps.setString(1, new ParallelGEDFlowRecommender().getNormalSubpath(frag.getSubpath()));
			rs = ps.executeQuery() ;
			rs.next() ;
			int subpathcount = rs.getInt(1) ;
			ps = con.prepareStatement(sql);
			ps.setInt(1, subpathcount);
			ps.setDouble(2, (double)frag.getFragmentcount()/(double)subpathcount);
			ps.setString(3, frag.getFragment());
			ps.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// close() ;
	}
	
	//
	public List<Fragments> getFragmentsBySubNodeNumber(int i) {
		List<Fragments> fragmentList = new ArrayList<Fragments>();
		String sql = "select * from patterntemp where subnodenumber=? and tailnodenumber=1";
		try {
			ps = con.prepareStatement(sql);
			ps.setInt(1, i);
			rs = ps.executeQuery();
			while (rs.next()) {
				Fragments frag = new Fragments();
				frag.setNode(rs.getString(1));
				frag.setSubpath(rs.getString(2));
				frag.setFragment(rs.getString(3));
				frag.setNodeNumber(rs.getInt(4));
				frag.setSubNodeNumber(rs.getInt(5));
				frag.setTailNodeNumber(rs.getInt(6));
				frag.setFragmentcount(rs.getInt(7));
				frag.setSubpathcount(rs.getInt(8));
				fragmentList.add(frag);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// close() ;
		return fragmentList;
	}

	public List<Fragments> getAllFragments() {
		List<Fragments> fragmentList = new ArrayList<Fragments>();
		String sql = "select * from patterntemp where tailnodenumber=1";
		try {
			ps = con.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				Fragments frag = new Fragments();
				frag.setNode(rs.getString(1));
				frag.setSubpath(rs.getString(2));
				frag.setFragment(rs.getString(3));
				frag.setNodeNumber(rs.getInt(4));
				frag.setSubNodeNumber(rs.getInt(5));
				frag.setTailNodeNumber(rs.getInt(6));
				frag.setFragmentcount(rs.getInt(7));
				frag.setSubpathcount(rs.getInt(8));
				fragmentList.add(frag);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// close() ;
		return fragmentList;
	}
	
	public List<Fragments> getFragmentsByNodeNumber(int i) {
		List<Fragments> fragmentList = new ArrayList<Fragments>();
		String sql = "select * from patterntemp where nodenumber=?";
		try {
			ps = con.prepareStatement(sql);
			ps.setInt(1, i);
			rs = ps.executeQuery();
			while (rs.next()) {
				Fragments frag = new Fragments();
				frag.setNode(rs.getString(1));
				frag.setSubpath(rs.getString(2));
				frag.setFragment(rs.getString(3));
				frag.setNodeNumber(rs.getInt(4));
				frag.setSubNodeNumber(rs.getInt(5));
				frag.setTailNodeNumber(rs.getInt(6));
				frag.setFragmentcount(rs.getInt(7));
				frag.setSubpathcount(rs.getInt(8));
				fragmentList.add(frag);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// close() ;
		return fragmentList;
	}

	public List<Fragments> getFragmentsByTailNodeNumber() {
		List<Fragments> fragmentList = new ArrayList<Fragments>();
		String sql = "select * from patterntemp where tailnodenumber>1";
		try {
			ps = con.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				Fragments frag = new Fragments();
				frag.setNode(rs.getString(1));
				frag.setSubpath(rs.getString(2));
				frag.setFragment(rs.getString(3));
				frag.setNodeNumber(rs.getInt(4));
				frag.setSubNodeNumber(rs.getInt(5));
				frag.setTailNodeNumber(rs.getInt(6));
				frag.setFragmentcount(rs.getInt(7));
				frag.setSubpathcount(rs.getInt(8));
				fragmentList.add(frag);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//close();
		return fragmentList;
	}

	public int getCount(Fragments frag) {
		String sql = "select sum(fragmentcount) from patterntemp where subpath=?";
		int count = 0;
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, frag.getSubpath());
			rs = ps.executeQuery();
			rs.next();
			count = rs.getInt(1);

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//close();
		return count;
	}

	// 关闭
	public void close() {
		try {
			if (rs != null) {
				rs.close();
			}
			if (s != null) {
				s.close();
			}
			if (ps != null) {
				ps.close();
			}
			con.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	

	public static void main(String argsp[]) throws Exception {
		long start = System.currentTimeMillis();
		WriteToDB wtdb = new WriteToDB();

		// 写入数据库
		
		Connection connection = DriverManager.getConnection(url, id, pwd);
		connection.setAutoCommit(false);
		PreparedStatement pst = connection.prepareStatement("update patterntemp set subpathcount=?, confident=? where fragment=?");
		
		// 对只有一个末尾节点的上游子路径，我们找与其一样的频繁子图的个数
		for (int i = 9; i > 0; i--) {
			List<Fragments> list1 = wtdb.getFragmentsBySubNodeNumber(i);
			List<Fragments> list2 = wtdb.getFragmentsByNodeNumber(i);

			// System.out.println(list2.size());
			for (int j = 0; j < list1.size(); j++) {
				Fragments frag1 = list1.get(j);
				for (int k = 0; k < list2.size(); k++) {
					Fragments frag2 = list2.get(k);
					// System.out.println(wtdb.getNormalSubpath(frag1.getSubpath()));
					// System.out.println(frag2.getFragment());
					if ((new ParallelGEDFlowRecommender().getNormalSubpath(frag1.getSubpath())).equals(frag2.getFragment())) {
						// System.out.println("------------OK--------") ;
						// System.out.println(frag2.getFragmentcount()) ;
						frag1.setSubpathcount(frag2.getFragmentcount());
						double confident = (double) frag1.getFragmentcount() / (double) frag1.getSubpathcount();
						// System.out.println("confident"+confident);
						frag1.setConfident(confident);
						
						try {
							pst.setInt(1, frag1.getSubpathcount());
							pst.setDouble(2, frag1.getConfident());
							pst.setString(3, frag1.getFragment());
							//把一个Sql命令加入命令列表
							pst.addBatch() ;
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					
						
						/*if(frag1.getConfident()>=0.5){ 
							new WriteToDB().copy(frag1) ; 
							}*/
						 
						break;
					}
				}
				// System.out.println("---------------------------------");
			}
		}
		
		/*//方法二：
		List<Fragments> list = wtdb.getAllFragments() ;

		// System.out.println(list2.size());
		for (int j = 0; j < list.size(); j++) {
			Fragments frag1 = list.get(j);
			wtdb.updatePatternTableTwo(frag1);	
		}*/
		
		
		// 对有多个末尾节点的上游子路径，
		List<Fragments> list1 = wtdb.getFragmentsByTailNodeNumber();
		for (int i = 0; i < list1.size(); i++) {
			Fragments frag1 = list1.get(i);
			if (frag1.getNodeNumber() != 1 && frag1.getSubpathcount() == 0) {
				frag1.setSubpathcount(wtdb.getCount(frag1));
				double confident = (double) frag1.getFragmentcount() / (double) frag1.getSubpathcount();
				// System.out.println("confident"+confident);
				frag1.setConfident(confident);
				try {
					pst.setInt(1, frag1.getSubpathcount());
					pst.setDouble(2, frag1.getConfident());
					pst.setString(3, frag1.getFragment());
					//把一个Sql命令加入命令列表
					pst.addBatch() ;
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				/*
				 * if(frag1.getConfident()>=0.5){ new WriteToDB().copy(frag1) ;
				 * }
				 */
			}
		}

		try {
			//批量执行命令
			pst.executeBatch() ;
			connection.commit() ;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		pst.close();
		connection.close();
		wtdb.close();
		long end = System.currentTimeMillis();
		System.out.println("computing Confidence takes:"+(end-start)/1000.0);
		System.out.println("close");
	}
}
