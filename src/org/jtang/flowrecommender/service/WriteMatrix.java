package org.jtang.flowrecommender.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

import org.jtang.flowrecommender.bean.Matrix;

public class WriteMatrix{
	public void writeFile(File f){
		Matrix m = new Matrix() ;
		Random r = new Random() ;
		String str = "" ;
		//随机生成100个数据，放入到文件中
		for(int i=0;i<100;i++){
			int s = r.nextInt(2) ;
			str += s+"," ;
		}
		m.setStr(str) ;
		//写入文件
		try {
			OutputStream os = new FileOutputStream(f) ;
			os.write(str.getBytes()) ;
			os.close() ;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String args[]){
		File f1 = new File("D:\\1.txt") ;
		File f2 = new File("D:\\2.txt") ;
		File f3 = new File("D:\\3.txt") ;
		//如果不存在这个文件，则创建一个
		try {
			f1.createNewFile() ;
			f2.createNewFile() ;
			f3.createNewFile() ;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		WriteMatrix g = new WriteMatrix() ;
		g.writeFile(f1) ;
		g.writeFile(f2) ;
		g.writeFile(f3) ;
	}
}
