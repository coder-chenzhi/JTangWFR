package org.jtang.flowrecommender.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.jtang.flowrecommender.bean.Pattern;
import org.jtang.flowrecommender.dao.ReadFromDB;


//���ݿ���������
public class FlowRecommender {
	String w = "" ;
	//��ȡ����д�������
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
	
	//ͨ���Ѿ������Ĺ����������ڵ���й������Ƽ�
	/**
	 * w:��ǰ������
	 * node:���һ������Ľڵ�
	 */
	public String getRecommender(String w,String node){
		//ͨ�����ݿ��ȡ�������ͺ͵�ǰ��������һ���ڵ������������Ľڵ�
		List<String> list_node = new ReadFromDB().readNodeByType(node) ;
		System.out.println(list_node.size());
		if(list_node.size()==0){
			System.out.println("û���뵱ǰ�ڵ�����Ľڵ㣬�Ƽ�����") ;
			return "finish" ;
		}else{
			/*//����ǰ������wת��������
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
				//��ÿ������Ҫ��Ľڵ㣬�ҵ���Ӱ���������·��
				List<Pattern> list_substring = new ReadFromDB().readInfluenceSubStrings(n) ;
				if(list_substring.size()!=0){
					for(int j=0;j<list_substring.size();j++){
						Pattern p = list_substring.get(j) ;
						String present_p[] = p.getSubpath().split("-->") ;
						//��������·���뵱ǰ�������ľ���
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
					//����ڵ�û����Ӱ���������·��
				}
			}
			if(l.size()==0){
				//��ǰ�ڵ�û�п��Ƽ��Ľڵ㣬����������ƥ��Ľڵ���ʾ����
				System.out.println("��ǰû�п��Ƽ��Ľڵ㡣") ;
				System.out.println("��������ƥ��Ľڵ㣺") ;
				for(int i=0;i<list_node.size();i++){
					System.out.println(list_node.get(i)+" ");
				}
			}else{
				System.out.println("�Ƽ��Ľڵ�") ;
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
				//����Ϊ-1��˵����������û������ڵ�
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
		System.out.println("�������Ƽ���ʼ...") ;
		System.out.print("�����뿪ʼ�ڵ㣺") ;
		check() ;
	}	
	
	public void check(){
		String str01 = SystemRead() ;
		if(str01.matches("[a-j]")){
			w += str01 ;
			System.out.println("��ǰ�Ѿ�����Ĺ�����Ϊ��"+w);
			//���÷�������Ƽ��Ľڵ�
			String command = getRecommender(w,str01) ;
			if(command.equals("finish")){
				System.out.println("�Ƽ�����...");
				System.out.println("�������Ϊ��"+w);
			}else{
				check() ;
			}
		}else if(str01.matches("end")){
			System.out.println("�Ƽ�����...");
			System.out.println("�������Ϊ��"+w);
		}else{
			System.out.println("�ڵ㲻���ڣ�����������...") ;
			check() ;
		}
	}	
	public static void main(String args[]) throws Exception{
		FlowRecommender fr = new FlowRecommender() ;
		fr.start() ;
	}
}
