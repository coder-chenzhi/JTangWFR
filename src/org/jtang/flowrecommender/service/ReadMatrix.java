package org.jtang.flowrecommender.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class ReadMatrix {
	public int[][] readFile(String s){
		int a[][] = new int[10][10] ;
		String str = "" ;
		InputStream is;
		File f = new File(s) ;
		try {
			is = new FileInputStream(f);
			byte[] b = new byte[(int)new File(s).length()] ;
			is.read(b) ;
			//System.out.println(new String(b)) ;
			is.close() ;
			str = new String(b) ;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println(str);
		String l[] = str.split(",") ;
		//System.out.println(b.length);
		int m = 0 ;
		for(int i=0;i<10;i++){
			for(int j=0;j<10;j++){
				//System.out.println(""+i+j+m) ;
				a[i][j] = Integer.parseInt(l[m]) ;
				m++ ;
				//System.out.println(a[i][j]);
			}
		}
		return a ;
	}
	public static void main(String args[]){
		ReadMatrix read = new ReadMatrix() ;
		int a[][] = read.readFile("D:\\1.txt") ;
		
	}
}
