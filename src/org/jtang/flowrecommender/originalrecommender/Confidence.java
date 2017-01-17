package org.jtang.flowrecommender.originalrecommender;

import java.util.ArrayList;
import java.util.List;

import org.jtang.flowrecommender.dao.ReadFromDB;
import org.jtang.flowrecommender.dao.WriteToDB;
//获得有影响的上游子路径
public class Confidence {
	public void register(){
		//对节点v，找到所有包含节点v在内的工作流
		for(int i=0;i<10;i++){
			char c = (char)(97+i) ;
			String str = "" ;
			str += c ;
			System.out.println("##########################"+str) ;
			List<String> list = new ArrayList<String>() ;
			ReadFromDB rfd = new ReadFromDB() ;
			list = rfd.readFlow(str) ;
			/*for(int i=0;i<list.size();i++){
				System.out.println(list.get(i));
			}*/
			//System.out.println(list.get(0)) ;
			for(int j=0;j<list.size();j++){
				//得到单个工作流
				String w = list.get(j) ;
				System.out.println(w) ;
				String sub  = w.split(str)[0] ;
				System.out.println("sub strings:"+sub);
				List subList = new ArrayList() ;
				//subList = new CalculateConfidence().getAllSubStrings(sub) ;
				//循环
				for(int a=0;a<subList.size();a++){
					//得到其中一条的上流子路径
					String subString = (String)subList.get(a) ;
					//计算置信度和支持度
					//int result = new CalculateConfidence().getConfidence(subString, str,new ReadFromDB(),new WriteToDB()) ;
					//if(result==1){
					//	break ;
					//}
				}
			}
		}
		
	}
	public static void main(String args[]){
		Confidence c = new Confidence() ;
		c.register() ;
	}
}
