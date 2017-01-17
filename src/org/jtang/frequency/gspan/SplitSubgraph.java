package org.jtang.frequency.gspan;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jtang.distance.ged.graph.DecoratedNode;
import org.jtang.distance.ged.graph.DotParseException;

import att.grappa.Edge;
import att.grappa.Graph;
import att.grappa.Node;
import att.grappa.Parser;

public class SplitSubgraph {

	private static String driver = "com.mysql.jdbc.Driver";
	private static String url = "jdbc:mysql://121.42.201.251:3306/test";
	private static String id = "jtangwfr";
	private static String pwd = "sa";
	
	@SuppressWarnings("unchecked")
	public static String split(String dotExpr) throws DotParseException {
		if (dotExpr == null || "".equals(dotExpr)) {
			throw new DotParseException("Empty DOT input!");
		}

		StringBuffer graphStringBuffer = new StringBuffer();

		// Parse the DOT expression using Grappa parser.

		InputStream inputStream = new ByteArrayInputStream(dotExpr.getBytes());

		Parser grappaParser = new Parser(inputStream);

		Graph grappaGraph = null;
		try {
			grappaParser.parse();
			grappaGraph = grappaParser.getGraph();
		} catch (Exception e) {
			throw new DotParseException(e.getMessage());
		}

		// Build node adjacency lists and wrap the Grappa elements into
		// decorated elements.

		Enumeration<Node> grappaNodes = grappaGraph.nodeElements();

		Set<DecoratedNode> nodes = new HashSet<DecoratedNode>();
		Map<Node, DecoratedNode> nodeMapping = new HashMap<Node, DecoratedNode>();
		// 将node添加到数据结构里边
		while (grappaNodes.hasMoreElements()) {
			Node grappaNode = grappaNodes.nextElement();

			DecoratedNode node = new DecoratedNode(grappaNode);

			nodeMapping.put(grappaNode, node);
			nodes.add(node);
		}

		if (nodes.isEmpty()) {
			throw new DotParseException("The graph has no nodes!");
		}

		// Set<MyEdge> edgeSet = new HashSet() ;//存放所有边的集合
		List<MyEdge> edgeSet = new ArrayList<MyEdge>();
		Set<Edge> removeEdgeset = new HashSet();// 存放移除的边
		Set<Node> nodeSetOfRemoveEdge = new HashSet();// 存放末尾节点的grappaNode的集合
		Set<Node> nodeSet = new HashSet();// 所有节点的集合
		Set<Node> nodeSetOfNotRemoveEdge = new HashSet();// 未删除的边的所有节点的集合
		String edges[] = dotExpr.split("\n");
		for (int i = 0; i < edges.length; i++) {
			if (edges[i].matches("[^A-Z]*->[^A-Z]*")) {
				MyEdge me = new MyEdge();
				me.setEdge(edges[i]);
				edgeSet.add(me);
			}
		}

		/*
		 * Enumeration<Edge> graphEdges = grappaGraph.edgeElements(); while
		 * (graphEdges.hasMoreElements()) { Edge graphEdge =
		 * graphEdges.nextElement();
		 * 
		 * edgeSet.add(graphEdge) ; }
		 */
		//System.out.println(dotExpr);
		// 找到每个节点的“出边”，然后找出邻接节点并添加
		for (DecoratedNode node : nodes) {
			Node grappaNode = node.getGrappaNode();
			// this node has no outEdge, then it is the last node in this
			// subgraph
			if (!grappaNode.outEdgeElements().hasMoreElements()) {
				graphStringBuffer.append(node.getLabel() + ",");// 输出最后一个节点

				grappaNode.inEdgeElements();
				Enumeration<Edge> graphEdgesEnumeration = grappaGraph.edgeElements();
				while (graphEdgesEnumeration.hasMoreElements()) {
					// while (edgeSetIterator.hasNext()){
					Edge graphEdge = graphEdgesEnumeration.nextElement();
					// MyEdge me = edgeSetIterator.next() ;

					Iterator<MyEdge> edgeSetIterator = edgeSet.iterator();
					Node head = graphEdge.getHead();
					Node tail = graphEdge.getTail();
					nodeSet.add(head);
					nodeSet.add(tail);
					// 判断，如果边包含最后一个节点，则在边的集合中删除这条边
					// System.out.println(graphEdge.toString());
					if (tail.equals(grappaNode) || head.equals(grappaNode)) {
						while (edgeSetIterator.hasNext()) {
							MyEdge me = edgeSetIterator.next();
							if ((graphEdge.toString() + ";").equals(me.getEdge())) {
								edgeSet.remove(me);
								break;
							}
						}
						removeEdgeset.add(graphEdge);
						nodeSetOfRemoveEdge.add(tail);
						// System.out.println("size:"+nodeSetOfNotRemoveEdge.size())
						// ;
					} else {
						nodeSetOfNotRemoveEdge.add(head);
						nodeSetOfNotRemoveEdge.add(tail);
						// System.out.println(head.toString()+","+tail.toString())
						// ;
						// System.out.println("sizeplus:"+nodeSetOfNotRemoveEdge.size())
						// ;
					}
					// System.out.println("-------------------") ;
				}
			}
		}

		// graphStringBuffer.append("\n");
		graphStringBuffer.append("digraph to {\n");// 开始输出图
		// 先将边输出，然后再输出节点

		for (int i = 0; i < edgeSet.size(); i++) {
			MyEdge myGraphEdge = edgeSet.get(i);
			graphStringBuffer.append(myGraphEdge.getEdge() + "\n");
		}

		Iterator<Node> nodeIterator = nodeSetOfRemoveEdge.iterator();
		while (nodeIterator.hasNext()) {
			Node node = nodeIterator.next();
			graphStringBuffer.append(node.toString() + ";\n");
		}

		graphStringBuffer.append("}\n");
		graphStringBuffer.append("," + nodeSet.size());
		graphStringBuffer.append("," + nodeSetOfRemoveEdge.size());
		graphStringBuffer.append("," + nodeSetOfNotRemoveEdge.size());
		return graphStringBuffer.toString();
	}

	/**
	 * 冒泡排序
	 * 
	 * @param workflow
	 * @return
	 */
	public static String sort(String workflow) {
		StringBuffer result = new StringBuffer();
		String a[] = workflow.split("\n");
		//System.out.println(a.length);
		for (int i = 1; i < a.length - 1; i++) {
			for (int j = 2; j < a.length - i; j++) {
				String str1[] = a[j].split("( -> )|;");
				String str2[] = a[j - 1].split("( -> )|;");
				if (Integer.parseInt(str1[0]) < Integer.parseInt(str2[0])) {
					String temp = a[j];
					a[j] = a[j - 1];
					a[j - 1] = temp;
				} else if (Integer.parseInt(str1[0]) == Integer.parseInt(str2[0])) {
					if (Integer.parseInt(str1[1]) < Integer.parseInt(str2[1])) {
						String temp = a[j];
						a[j] = a[j - 1];
						a[j - 1] = temp;
						//System.out.println("change" + a[j] + a[j - 1]);
					}
				} else {

				}
			}
		}

		for (int i = 0; i < a.length; i++) {
			result.append(a[i]).append("\n");
		}
		//System.out.println(result);
		return result.toString();
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
//		long t0 = System.currentTimeMillis();
		long timeOfDB = 0;
		String inputFilename = System.getProperty("user.dir") + "\\result\\" + "editInput.dat";
		String outputFilename = System.getProperty("user.dir") + "\\result\\" + "bye";
		File file = new File(inputFilename);
		BufferedReader bf = new BufferedReader(new FileReader(file));

		RandomAccessFile randomFile = new RandomAccessFile(outputFilename, "rw");

		long start = System.currentTimeMillis();
		//WriteToDB db = new WriteToDB() ;
		String line = "";
		StringBuffer buf = new StringBuffer(2048);
		StringBuffer graphStringBuffer = new StringBuffer();
		
		// 连接驱动
		Class.forName(driver);
		// 连接数据库
		Connection con = DriverManager.getConnection(url, id, pwd);
		// 获取数据库操作对象
		con.setAutoCommit(false);
		PreparedStatement ps = (PreparedStatement) con.prepareStatement("insert into patterntemp values (?,?,?,?,?,?,?,0,0,null)");
		while ((line = bf.readLine()) != null) {
			if (line.startsWith("}")) {
				// graphs[count++] = parse(buf.toString(), factory);
				buf.append(line).append('\n');
				// 对工作流进行排序
				String workflow = sort(buf.toString());
				String splitresult = split(workflow);
				graphStringBuffer.append(splitresult);

				// 统计频繁子图出现的次数
				line = bf.readLine();
				int count = Integer.parseInt(line.split("\\[")[0]);

				// 解析split之后返回的结果，将节点和路径分开
				String result[] = splitresult.split(",");
				int nodeNumber = Integer.parseInt(result[result.length - 3]);// 节点总数
				int tailnodeNumber = Integer.parseInt(result[result.length - 2]);// 末尾节点数
				int leftnodeNumber = Integer.parseInt(result[result.length - 1]);// 上游子路径的节点数
				if (nodeNumber == 0) {
					nodeNumber = 1;
				}
				String path = result[result.length - 4];
				String node = "";
				for (int i = 0; i < result.length - 4; i++) {
					node += result[i] + ",";
				}

				//System.out.println("------>" + node + "," + leftnodeNumber + "," + (result.length - 4));
				// 写入数据库  条件是末尾节点数小于1或者result.length<=5
//				long tempTime = System.currentTimeMillis();
				if (!(tailnodeNumber > 1 && (result.length - 4) > 1)) {
//					db.addIntoPatternTable(node, path, workflow, nodeNumber,
//							(nodeNumber - result.length + 4), tailnodeNumber, count);
					ps.setString(1, node);
					ps.setString(2, path);
					ps.setString(3, workflow);
					ps.setInt(4, nodeNumber);
					ps.setInt(5, (nodeNumber - result.length + 4));
					ps.setInt(6, tailnodeNumber);
					ps.setInt(7, count);
					// 把一个SQL命令加入命令列表
					ps.addBatch() ;
				}
//				tempTime = System.currentTimeMillis() - tempTime;
//				timeOfDB += tempTime;
				buf = new StringBuffer(2048);
			} else {
				buf.append(line).append('\n');
			}
		}
		try{
			// 执行批量更新
			ps.executeBatch();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		// 语句执行完毕，提交本事务
		con.commit();
		ps.close() ;
		con.close() ;
		//db.close() ;
		bf.close();

		randomFile.writeBytes(graphStringBuffer.toString());
		randomFile.close();
		// sort("digraph to {\n9 -> 7;\n5 -> 7;\n}\n") ;
//		long t1 = System.currentTimeMillis();
		long end = System.currentTimeMillis();
		System.out.println("split graph time is:" + (end - start) / 1000.0);
//		System.out.println("DB operation time is:"+timeOfDB);
//		System.out.println("split time is:" + (t1 - t0-timeOfDB) / 1000.0);
	}

}
