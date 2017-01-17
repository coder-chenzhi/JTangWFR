package org.jtang.flowrecommender.originalrecommender;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.jtang.flowrecommender.dao.ReadFromDB;
import org.jtang.flowrecommender.dao.WriteToDB;
//置信度计算
public class CalculateConfidence {
	public int getConfidence(String str,String node,ReadFromDB rDb,PreparedStatement ps) throws Exception{
		String temp[] = str.split("-->") ;
		//System.out.println(temp[0])  ;
		String s = "" ;
		int k=0 ;
		String temp01[] = temp[0].split(",") ;
		String temp03[] = temp[temp.length-1].split(",") ;
		k = Integer.parseInt(temp01[1]) ;
		s += temp01[0] ;
		//得到上流子路径
		for(int i=1;i<temp.length;i++){
			String temp02[] = temp[i].split(",") ;
			int distance = k-Integer.parseInt(temp02[1]) ;
			k = Integer.parseInt(temp02[1]) ;
			for(int j=0;j<distance-1;j++){
				s += "_" ;
			}
			s += temp02[0] ;
		}
		int freq_p = rDb.getCount(s) ;
		String b = "" ;
		for(int i=0;i<k-1;i++){
			b+="_" ;
		}
		int freq_vp = rDb.getCount(s+b+node) ;
		//System.out.println(freq_p);
		//System.out.println(freq_vp);
		//System.out.println((double)freq_vp/(double)freq_p);
		double conf = (double)freq_vp/(double)freq_p ;
		
		//在此处添加支持度，如果子路径只出现过一次，我们不认为它是有影响的上游子路径
		if(conf>=0.15){
			//将信息写入pattern table中
			//wDb.addvp(str, node, Integer.parseInt(temp01[1]),Integer.parseInt(temp03[1]),temp.length,freq_vp,freq_p, conf) ;
			ps.setString(1, node) ;
			ps.setString(2, str) ;
			ps.setInt(3, Integer.parseInt(temp01[1])) ;
			ps.setInt(4, Integer.parseInt(temp03[1])) ;
			ps.setInt(5, temp.length) ;
			ps.setInt(6, freq_vp) ;
			ps.setInt(7, freq_p) ;
			ps.setDouble(8, conf) ;
			// 把一个SQL命令加入命令列表
			ps.addBatch() ;
			return 1 ;
		}else{
			return 0 ;
		}
		
	}
/*	public static void main(String args[]){
		String str = "j,3-->a,2-->b,1-->" ;
		new CalculateConfidence().getConfidence(str, "i") ;
	}*/
}
