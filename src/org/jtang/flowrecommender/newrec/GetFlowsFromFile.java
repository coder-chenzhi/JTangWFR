package org.jtang.flowrecommender.newrec;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.jtang.flowrecommender.dao.WriteToDB;
import org.jtang.function.getNodeSequence;


//通过读取出来的字符串生成工作流源，并写入数据库（MySql）
public class GetFlowsFromFile {
	String flow="" ;
	String join="" ;

	//通过文件得到字符串
	public static int numberOfTotalNodes = 52;
	public static int numberOfFlows = 500;
	
	public static int TestSetSize = 10;
	
	public void getFlows(){
		ReadFile read = new ReadFile() ;
		//通过文件得到字符串
		String str = read.readFile("") ;//modify
		//将字符串通过“#”分割，得到的数据放到数组中
		String a[] = str.split("#") ;
		System.out.println("矩阵的数量为："+a.length) ;
		System.out.println(a[3]) ;
		
		//对每个矩阵进行循环，这里用a[1]作为例子
		for(int m=0;m<a.length;m++){
			String string  = a[m] ;
			//String string  = a[3] ;
			String b[] = string.split("\n") ;
			//System.out.println(b.length) ;
			
			//创建一个二维数组来存放矩阵
			String c[][] = new String[b.length-1][b.length-1] ;
			for(int i=1;i<b.length;i++ ){
				c[i-1] = b[i].split(" ") ;
			}
			//将二维数组显示出来，判断是否提取成功
			/*for(int i=0;i<c.length;i++){
				for(int j=0;j<c.length;j++){
					System.out.print(c[i][j]) ;
				}
			}*/
			
			//得到工作流的起始位置，即没有前一个节点的工作流
			String begin = "" ;
			boolean bool = false ;
			for(int i=0;i<c.length;i++){
				for(int j=i+1;j<c.length;j++){
					//System.out.println("node:"+c[j][i]);
					if(c[j][i].equals("-")){
						bool = true ;
						//System.out.println("1节点为："+c[j][i]+",布尔值为："+bool) ;
					}else{
						//存在上一个节点
						bool = false ;
						//System.out.println("2节点为："+c[j][i]+",布尔值为："+bool) ;
						break ;					
					}
				}
				if(bool==true){
					//System.out.println(c[i][i]);
					begin = c[i][i] ;
					break ;
				}
			}
			System.out.println("开始节点为："+begin) ;
			flow=c[0][0] ;
			getFlow(c,0) ;
			System.out.println("这是第"+(m+1)+"个矩阵，最后的工作流为"+flow) ;
			WriteToDB wtd = new WriteToDB() ;
			wtd.add(flow) ;
			//System.out.println("这是第4个矩阵，用于测试，最后的工作流为"+flow) ;
		}
	}
	
	public void getFlow(String c[][],int n){
		for(int i=n+1;i<c.length;i++){
			//System.out.println("现在是第"+n+"行，n:"+n+"i:"+i+"节点数据为:"+c[n][i]);
			if(c[n][i].equals("-")){
				//System.out.println("-获得的工作流为"+flow);
			}else if(c[n][i].equals("1")){
				flow += c[i][i] ;
				//System.out.println("1获得的工作流为"+flow);
				getFlow(c,i) ;
				break ;
			}else if(c[n][i].equals("2")){
				flow += "AND(" +c[i][i];
				getFlow(c,i) ;
				//得到所有内容为2的点
				for(int k=i+1;k<c.length;k++){
					if(c[n][k].equals("2")){
						flow += ","+c[k][k];
						getFlow(c,k) ;
					}
				}
				flow += ")" ;
				flow += join ;
				
				//System.out.println("2获得的工作流为"+flow);
				break ;
			}else if(c[n][i].equals("3")){
				join = c[i][i];
				//System.out.println("3获得的工作流为"+flow);
				break ;
			}
		}
	}
	
	//通过文件获得工作流，没有分支结构，写入数据库
	public void getFlowFromFileNoBranch(){
		ReadFile read = new ReadFile() ;
		String trainSetName = System.getProperty("user.dir") + "\\" + "workflow" + "\\" + "train"+"-"+numberOfTotalNodes+"-"+ numberOfFlows + "-"
				+ (numberOfFlows - TestSetSize) + ".sdg";
		String str = read.readFile(trainSetName) ;
		//System.out.println(str) ;
		//将字符串通过“#”分割，得到的数据放到数组中
		String a[] = str.split("#") ;
		
		String driver = "com.mysql.jdbc.Driver";
		String url = "jdbc:mysql://121.42.201.251:3306/test";
		String id = "jtangwfr";
		String pwd = "sa";
		PreparedStatement ps ;
		Connection con ;
		try {
			// 连接驱动
			Class.forName(driver);// 连接数据库
			con = DriverManager.getConnection(url, id, pwd);
			// 获取数据库操作对象
			con.setAutoCommit(false);
			ps = (PreparedStatement) con.prepareStatement("insert into flow values (?)");
			//对每个矩阵进行循环，这里用a[1]作为例子
			for(int m=0;m<a.length;m++){
				String string  = a[m] ;
				//System.out.println("这里是第"+(m+1)+"个数组"+a[m]);
				String b[] = string.split("\n") ;
				//System.out.println(b.length) ;
				
				if(b.length<2){
					//System.out.println("该数组不符合，舍去");
				}else{
					//创建一个二维数组来存放矩阵
					String c[][] = new String[b.length-2][b.length-2] ;
					for(int i=2;i<b.length;i++ ){
						c[i-2] = b[i].split(" ") ;
					}
					//将二维数组显示出来，判断是否提取成功
					/*for(int i=0;i<c.length;i++){
						for(int j=0;j<c.length;j++){
							System.out.print(c[i][j]) ;
						}
					}
					System.out.println() ;*/
					
					//得到工作流
					String flow = "" ;
					for(int i=0;i<c.length;i++){
						flow += (char)(Integer.parseInt(c[i][i])+96) ;
					}					
					ps.setString(1, flow) ;
					// 把一个SQL命令加入命令列表
					ps.addBatch() ;
					//System.out.println("这是第4个矩阵，用于测试，最后的工作流为"+flow) ;
		
				}
			}
			try {
				ps.executeBatch() ;
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{
				ps.executeBatch() ;
			}
				// 语句执行完毕，提交本事务
				con.commit();
				ps.close() ;
				con.close() ;
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}
	
//	主方法，将所有数据存放到数据库
	public static void main(String args[]){
		long start = System.currentTimeMillis();
		new GetFlowsFromFile().getFlowFromFileNoBranch() ;
		long end = System.currentTimeMillis();
		System.out.print(end-start);
	}
}
