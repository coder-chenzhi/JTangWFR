package org.jtang.flowrecommender.dfscode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class Test {

	public static Map distMap = new HashMap() ;
	
	public Test(){
		distMap.put("a", "0,3") ;
		distMap.put("b", "1,0") ;
		distMap.put("c", "0,0") ;
		distMap.put("d", "0,2") ;
		distMap.put("e", "2,0") ;
		distMap.put("f", "12,1") ;
		distMap.put("g", "2,0") ;
		distMap.put("h", "3,0") ;
		distMap.put("i", "0,14") ;
		distMap.put("j", "0,13") ;
		distMap.put("k", "0,5") ;
		
	}
	
	
	public static void main(String args[]){
		Test t = new Test() ;
		ArrayList<Map.Entry<String, String>> tmList = new ArrayList<Map.Entry<String, String>>(
				distMap.entrySet());
		// 利用Collections.sort方法排序
		Collections.sort(tmList, new Comparator<Map.Entry<String, String>>() {

			public int compare(Map.Entry<String, String> o1,
					Map.Entry<String, String> o2) {
//				System.out.println("o1:"+o1.getKey()+" ; "+o1.getValue()) ;
//				System.out.println("o2:"+o2.getKey()+" ; "+o2.getValue()) ;
				String value1[] = o1.getValue().split(",") ;
				String value2[] = o2.getValue().split(",") ;
				Double dist1 = Double.parseDouble(value1[0]) ;
				Double dist2 = Double.parseDouble(value2[0]) ;
				Double conf1 = Double.parseDouble(value1[1]) ;
				Double conf2 = Double.parseDouble(value2[1]) ;
//				System.out.println(dist1+","+dist2+","+conf1+","+conf2);
				if(dist1<dist2){
					return -1 ;
				}else if(dist1>dist2){
					return 1 ;
				}else{
					if(conf1<conf2){
						return 1 ;
					}else if(conf1>conf2){
						return -1 ;
					}else{
						return 0 ;
					}
				}
			}
		});
		
		
		int i = 0;
		for (Map.Entry<String, String> entry : tmList) {
			i++;
//			String tempNode = entry.getKey().replace(",", "");
			String tempNode = entry.getKey();
			// added by tokyo
//			candidateSet.add(tempNode);
			// added by tokyo

			 System.out.println("node:" + tempNode + " dist:" + entry.getValue() + ",");
//			 System.out.println("node:" + tempNode);

//			if (i >= firstN) {
				 if (i >= 100) { // choose the first n results
				break;
			}
		}
	}
}
