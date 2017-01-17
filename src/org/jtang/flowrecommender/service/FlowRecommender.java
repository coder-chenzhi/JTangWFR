package org.jtang.flowrecommender.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.jtang.flowrecommender.bean.Pattern;
import org.jtang.flowrecommender.dao.ReadFromDB;


//数据库中增加了
public class FlowRecommender {
	String w = "" ;
	//读取界面写入的数据
	public String SystemRead(){
		BufferedReader buff = new BufferedReader(new InputStreamReader(System.in)) ;
		String str = null;
		try {
			str = buff.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return str ;
	}
	
	//通过已经产生的工作流和最后节点进行工作流推荐
	/**
	 * w:当前工作流
	 * node:最后一个输入的节点
	 */
	public String getRecommender(String w,String node){
		//通过数据库读取输入类型和当前输入的最后一个节点输出类型相符的节点
		List<String> list_node = new ReadFromDB().readNodeByType(node) ;
		System.out.println(list_node.size());
		if(list_node.size()==0){
			System.out.println("没有与当前节点相符的节点，推荐结束") ;
			return "finish" ;
		}else{
			/*//将当前工作流w转换成数组
			String present_w[] = new String[w.length()] ;
			for(int i=0;i<w.length();i++){
				present_w[i] = w.charAt(i) + "," +(w.length()-i) ;
				//System.out.println(present_w[i]);
			}*/
			List<Pattern> l = new ArrayList<Pattern>() ;
			double strenth[] = new double[10] ;
			for(int i=0;i<list_node.size();i++){
				String n = list_node.get(i) ;
				//System.out.println(n);
				//对每个符合要求的节点，找到有影响的上游子路径
				List<Pattern> list_substring = new ReadFromDB().readInfluenceSubStrings(n) ;
				if(list_substring.size()!=0){
					for(int j=0;j<list_substring.size();j++){
						Pattern p = list_substring.get(j) ;
						String present_p[] = p.getSubpath().split("-->") ;
						//并计算子路径与当前工作流的距离
						double dist = getDist(present_p,w) ;
						if(dist<1.0){
							p.setDist(dist) ;
							/*System.out.println(dist);
							System.out.println(p.getNode()) ;
							System.out.println(p.getSubpath());*/
							strenth[p.getNode().toCharArray()[0]-97] += p.getConf() ;
							l.add(p) ;
						}
					}
				}else{
					//这个节点没有有影响的上游子路径
				}
			}
			if(l.size()==0){
				//当前节点没有可推荐的节点，将所有类型匹配的节点显示出来
				System.out.println("当前没有可推荐的节点。") ;
				System.out.println("符合类型匹配的节点：") ;
				for(int i=0;i<list_node.size();i++){
					System.out.println(list_node.get(i)+" ");
				}
			}else{
				System.out.println("推荐的节点") ;
				double sum = 0.0 ;
				for(int i=0;i<10;i++){
					sum += strenth[i] ; 
				}
				java.text.DecimalFormat df =new java.text.DecimalFormat("#.00");  
				
				for(int i=0;i<10;i++){
					if(strenth[i]!=0){
						double strength = strenth[i]/sum ;
						System.out.println((char)(i+97)+"("+df.format(strength)+")");
					}
				}
			}
			return "go" ;
		}
	}
	
	public double getDist(String p[],String flow){
		double dist = 0.0 ;
		for(int i=0;i<p.length;i++){
			String node = p[i].split(",")[0] ;
			if(flow.indexOf(node)==-1){
				//索引为-1，说明工作流中没有这个节点
				dist=5 ;
				break ;
			}else{
				int x = (flow.length()-flow.indexOf(node))-Integer.parseInt(p[i].split(",")[1]) ;
				if(x>=0){
					dist += Double.parseDouble(Integer.toString(x))/(flow.length()*p.length) ;
				}else{
					dist += Double.parseDouble(Integer.toString((-x)))/(flow.length()*p.length) ;
				}
			}
		}
		return dist ;
	}
	public void start(){
		System.out.println("工作流推荐开始...") ;
		System.out.print("请输入开始节点：") ;
		check() ;
	}	
	
	public void check(){
		String str01 = SystemRead() ;
		if(str01.matches("[a-j]")){
			w += str01 ;
			System.out.println("当前已经定义的工作流为："+w);
			//调用方法获得推荐的节点
			String command = getRecommender(w,str01) ;
			if(command.equals("finish")){
				System.out.println("推荐结束...");
				System.out.println("最后工作流为："+w);
			}else{
				check() ;
			}
		}else if(str01.matches("end")){
			System.out.println("推荐结束...");
			System.out.println("最后工作流为："+w);
		}else{
			System.out.println("节点不存在，请重新输入...") ;
			check() ;
		}
	}	
	public static void main(String args[]) throws Exception{
		FlowRecommender fr = new FlowRecommender() ;
		fr.start() ;
	}
}
