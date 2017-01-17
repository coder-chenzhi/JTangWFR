package org.jtang.flowrecommender.service;

import java.util.Random;

import org.jtang.flowrecommender.dao.WriteToDB;

//模拟一些工作流，保存到数据库中
public class GetFlow {
	//得到一个在一定范围内的随机数
	public int random(int begin,int length){
		Random r = new Random() ;
		int i = begin + r.nextInt(length) ;
		return i ;
	}
	//得到一个长度在2-10范围内随机的工作流
	public String getflow(){
		//得到长度的随机数（3-10）
		int length = random(4,7) ;
		String str = "" ;
		//随机得到a-j之间的一个字母，当做工作流的开始
		int a = random(0, 10) ;
		//System.out.println(a);
		char begin = (char)(a+97) ;
		//System.out.println(c);
		char c[] = new char[length] ;
		c[0] = begin ;
		//System.out.println(c.length);
		//System.out.println(c[0]);
		//随机得到一个1-3之间的数字，判断读取哪个文件
		for(int i=0;i<length-1;i++){
			int filenum ;
			if(i==0){
				filenum = 1 ;
			}else if(i==1){
				filenum = random(1,2) ;
			}else{
				filenum = random(1,3) ;
			}
			ReadMatrix r = new ReadMatrix() ;
			int m[][] = r.readFile("D://"+filenum+".txt") ;
			int x = c[i-filenum+1]-97 ;//上一个获得的节点
			//System.out.println("+++++="+x);
			int temp[] = m[x] ;//得到上一个节点的在文件中的所有的数组
			int num = 0 ;
			for(int t=0;t<temp.length;t++){
				//定义数组p，用来存放符合条件的节点
				//System.out.print(temp[t]) ;
				if(t!=x&&temp[t]!=0){
					boolean b = true ;
					for(int n:c){
						if((n-97)==t){
							b = false ;
						}
					}
					if(b==true){
						num++ ;
					}
				}
				//System.out.println("num="+num);
			}
			if(num==0){
				break ;
			}else{			
				int d = 0 ;
				int p[]=new int[num] ;
				for(int t=0;t<temp.length;t++){
					//定义数组p，用来存放符合条件的节点
					//System.out.print(t) ;
					if(t!=x&&temp[t]!=0){
						boolean b = true ;
						for(int n:c){
							if((n-97)==t){
								b = false ;
							}
						}
						if(b==true){
							p[d] = t ;
							d++ ;
						}
					}
				}
				int s = random(0, num) ;
				//System.out.println("p[s]="+p[s]) ;
				c[i+1] = (char)(p[s]+97) ;
			}
		}
		for(char k:c){
			//System.out.print(k)  ;
			str += k ;
		}
		return str ;
	}
/*	public static void main(String argsp[]){
		for(int i=0;i<20;i++){
			GetFlow g = new GetFlow() ;
			String flow = g.getflow() ;
			WriteToDB wtd = new WriteToDB() ;
			wtd.add(flow) ;
		}
	}*/
}
