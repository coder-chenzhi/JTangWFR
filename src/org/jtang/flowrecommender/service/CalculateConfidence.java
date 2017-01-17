package org.jtang.flowrecommender.service;

import org.jtang.flowrecommender.dao.ReadFromDB;
import org.jtang.flowrecommender.dao.WriteToDB;
//置信度计算
public class CalculateConfidence {
	public void getConfidence(String str,String node){
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
		int freq_p = new ReadFromDB().getCount(s) ;
		String b = "" ;
		for(int i=0;i<k-1;i++){
			b+="_" ;
		}
		int freq_vp = new ReadFromDB().getCount(s+b+node) ;
		//System.out.println(freq_p);
		//System.out.println(freq_vp);
		//System.out.println((double)freq_vp/(double)freq_p);
		double conf = (double)freq_vp/(double)freq_p ;
		
		//在此处添加支持度
		if(conf>=0.3){
			//将信息写入pattern table中
			new WriteToDB().addvp(str, node, Integer.parseInt(temp01[1]),Integer.parseInt(temp03[1]),temp.length,freq_vp,freq_p, conf) ;
		}
		
	}
/*	public static void main(String args[]){
		String str = "j,3-->a,2-->b,1-->" ;
		new CalculateConfidence().getConfidence(str, "i") ;
	}*/
}
