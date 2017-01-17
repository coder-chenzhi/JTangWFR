package org.jtang.flowrecommender.service;

import java.util.ArrayList;
import java.util.List;

public class SubStrings {
	public List getAllSubStrings(String s){
		List l = new ArrayList() ;
		char c[] = s.toCharArray() ;
		String ss[] = new String[c.length] ;
		for(int i=0;i<c.length;i++){
			ss[i] = c[i] + ","+(c.length-i)+"-->" ;
			//System.out.println(ss[i]);
		}
		int length = c.length ;
		for(int i=0;i<Math.pow(2, Math.min(3, c.length));i++){
			//放二进制字符串
			 String s01 = "";
			 int m=i ;
			  while (m > 0) {
				  s01 = m % 2 + s01;
				  m = m / 2;
			  }
			  if(!"".equals(s01)){
				  //定义一个数组k来存放二进制数据
				  if(s01.length()<Math.min(3,c.length)){
					  //位数不足则补齐
					  String s02 = "" ;
					  for(int num=0;num<(Math.min(3,c.length)-s01.length());num++){
						  s02+="0" ;
					  }
					  s01 = s02+s01 ;
				  }

				  //System.out.println(s01);
				  char k[] = s01.toCharArray() ;
				  //通过字符串来得到上流子路径，存放在list列表中
				  String path = "" ;
				  for(int num=0;num<k.length;num++){
					  //System.out.println(k[num]);
					  if(k[num]=='1'){
						  path += ss[ss.length-k.length+num] ;
					  }
				  }
				  //System.out.println(path) ;
				  l.add(path) ;
			  }
		}
		return l ;
	}
/*	public static void main(String args[]){
		SubStrings s = new SubStrings() ;
		String sss = "jab" ;
		List list = s.getAllSubStrings(sss) ;
		for(int i=0;i<list.size();i++){
			System.out.println(list.get(i));
		}
	}*/
}
