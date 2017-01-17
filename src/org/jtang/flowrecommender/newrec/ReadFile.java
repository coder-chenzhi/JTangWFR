package org.jtang.flowrecommender.newrec;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
//从指定文件中读取数据
public class ReadFile {
	public String readFile(String s){
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
		return str ;
	}
/*	public static void main(String args[]){
		ReadFile read = new ReadFile() ;
		String s = read.readFile("D:\\workflow.sdg") ;
		System.out.println(s) ;
	}*/
}
