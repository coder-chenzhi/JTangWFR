package org.jtang.function;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import com.sun.xml.internal.bind.v2.schemagen.xmlschema.List;

public class GenerateAllSimple {
	public static final int TestSetSize = 10;
	private File ruleFile;
	private FileWriter ruleFileOut;
	private File trainFile[] = new File[6];
	private FileWriter[] trainFileOut = {null,null,null,null,null,null};
	private File testFile;
	private FileWriter testFileOut;
	private int numberOfFlows;
	private int numberOfTotalNodes;
	private int maxNodes;
	private int minNodes;
	/**rule matrix*/
	private String[][] workflowMatrix;

	/**
	 * configure the arguments; make sure maxNodes is no bigger than
	 * numberOfTotalNodes; make sure maxNodes is bigger than minNodes
	 * 
	 * @param numberOfFlows
	 * @param numberOfTotalNodes
	 * @param maxNodes
	 * @param minNodes
	 * @throws IOException
	 */
	public GenerateAllSimple(int numberOfFlows, int numberOfTotalNodes, int minNodes,
			int maxNodes) throws IOException {
		String trainSetName[] = new String[6];
		for (int i = 1; i < 6; i++) {
			trainSetName[i] = System.getProperty("user.dir") + "\\"
					+ "workflow" + "\\" + "train" + "-" + numberOfTotalNodes + "-"
					+ (i*200) + "-" + (i*200-10) + ".sdg";
		}
		
		String testSetName = System.getProperty("user.dir") + "\\" + "workflow"
				+ "\\" + "test" + "-" + numberOfTotalNodes + "-"
				+ "1000" + "-" + TestSetSize + ".sdg";
		String ruleMatrixName=System.getProperty("user.dir") + "\\" + "workflow"
				+ "\\" + "rule" + "-" + numberOfTotalNodes +".sdg";
		for (int i = 1; i < trainSetName.length; i++) {
			trainFile[i] = new File(trainSetName[i]);
			trainFileOut[i] = new FileWriter(trainFile[i], true);
		}
		
		testFile = new File(testSetName);
		testFileOut = new FileWriter(testFile, true);
		ruleFile = new File(ruleMatrixName);
		if (ruleFile.exists()) {
			ruleFileOut = null;
		}else {
			ruleFileOut = new FileWriter(ruleFile,true);
		}
		
		this.numberOfFlows = numberOfFlows;
		this.numberOfTotalNodes = numberOfTotalNodes;
		this.maxNodes = maxNodes;
		this.minNodes = minNodes;
		workflowMatrix = new String[numberOfTotalNodes][numberOfTotalNodes];
	}
	
	/**
	 * generate rule matrix
	 */
	public void generateMatrix() {
		if (ruleFileOut == null) {
			BufferedReader reader = null;
	        try {
	            System.out.println("以行为单位读取文件内容，一次读一整行：");
	            reader = new BufferedReader(new FileReader(ruleFile));
	            String tempString = null;
	            int line = 0;
	            // 一次读入一行，直到读入null为文件结束
	            while ((tempString = reader.readLine()) != null) {
	            	if(!tempString.equals("#") ){
	            		workflowMatrix[line]=tempString.split(" ");
		            	line++;
	            	}
	            	
	            }
	            reader.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	        } finally {
	            if (reader != null) {
	                try {
	                    reader.close();
	                } catch (IOException e1) {
	                }
	            }
	        }
	        printMatrix(workflowMatrix);
			return;
		}
		
//		int nextAvailableNodesNumber = 3;
		int nextAvailableNodesNumber = 4;
		Random rand = new Random();
		for (int i = 0; i < numberOfTotalNodes; i++) {
			for (int j = 0; j < numberOfTotalNodes; j++) {
				workflowMatrix[i][j] = "0";
			}
		}
		// added by tokyo
		for (int i = 0; i < numberOfTotalNodes; i++) {
			int j = 0;
			int oldpositon = -1;
			int position = 0;
			while (j < nextAvailableNodesNumber) {
				position = rand.nextInt(numberOfTotalNodes);
				if (position != i && position != oldpositon) {//
					workflowMatrix[i][position] = "1";
					oldpositon = position;
					j++;
				}

			}
		}
		try {
			writeMatricToFile(workflowMatrix,ruleFileOut);
			ruleFileOut.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		printMatrix(workflowMatrix);
	}

	private void printMatrix(String[][] matrix) {
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix.length; j++) {
				System.out.print(workflowMatrix[i][j]);
			}
			System.out.println();
		}
	}

	public void writeMatricToFile(String[][] matrix, FileWriter writer)
			throws IOException {
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				writer.write(matrix[i][j]);
				writer.write(' ');
			}
			writer.write("\r\n");
		}
		writer.write("#\r\n");
	}

	public void instanceMatrics(String[][] matrix, ArrayList<Integer> tempList) {
		// Set<Integer> usedNodeLabel = new HashSet<Integer>();
		int length = matrix.length;
		for (int i = 0; i < length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				matrix[i][j] = "-";
			}
		}
		
		for (int j = 0; j < length; j++) {
			matrix[j][j] = String.valueOf(tempList.get(j)+1);
		}
		for (int i = 0; i < length - 1; i++) {
			matrix[i][i + 1] = "1";
		}
	}

	public void GenerateFlows() throws IOException {
		for (int i = 1; i < 6; i++) {
			int trainSetSize = i*200-10;
			trainFileOut[i].write(String.valueOf(trainSetSize) + "\r\n");
		}
		
		System.out.println("-----------------------");
		for (int i = 0; i < 990; i++) {
			// the size of the workflow is larger than 1
			ArrayList<Integer> tempList = getNodeSequence();
			int iNodes = tempList.size();
			String[][] matrix = new String[iNodes][iNodes];
			instanceMatrics(matrix,tempList);
			for(int j=1;j<6;j++){
				if(i<(j*200-10)){
					trainFileOut[j].write('G');
					trainFileOut[j].write(String.valueOf(i + 1) + "\r\n");
					writeMatricToFile(matrix, trainFileOut[j]);
				}
			}
			
		}
		
		for (int i = 1; i < 6; i++) {
			trainFileOut[i].close();
		}
		System.out.println("-----------------------");

		testFileOut.write(String.valueOf(TestSetSize) + "\r\n");

		for (int i = 990; i < numberOfFlows; i++) {
			// the size of the workflow is larger than 1
			ArrayList<Integer> tempList = getNodeSequence();
			int iNodes = tempList.size();
			String[][] matrix = new String[iNodes][iNodes];
			instanceMatrics(matrix,tempList);
			testFileOut.write('G');
			testFileOut.write(String.valueOf(i + 1) + "\r\n");
			writeMatricToFile(matrix, testFileOut);
		}

		testFileOut.close();
		System.out.println("-----------------------");
	}

	public ArrayList<Integer> getNodeSequence() {
		ArrayList<Integer> tempList;
		do {
			Random random = new Random();
			
			tempList = new ArrayList<Integer>();
			int node = random.nextInt(numberOfTotalNodes);
			tempList.add(node);
			int[] num = new int[10];
			num[0] = node;
			int workflowSize = random.nextInt(maxNodes-minNodes+1)+minNodes;
			for (int j = 1; j < workflowSize; j++) {
				int lastNode = 0;
				int count = 0;
				do {
					node = random.nextInt(numberOfTotalNodes);
					lastNode = num[j - 1];
					count++;
					if (count > 20) {
						break;
					}
				} while (tempList.contains(node)
						|| workflowMatrix[lastNode][node].equals("0"));
				if (count > 20)
					break;
				num[j] = node;
				lastNode = node;
				tempList.add(node);
			}
		} while (tempList.size() < minNodes);	
		return tempList;
	}

	public static void main(String[] args) throws IOException {
		//流程数目  可用节点总数    最小流程      最大流程
		GenerateAllSimple ge = new GenerateAllSimple(1000, 20, 5, 10);
		
		ge.generateMatrix();
//		ge.getNodeSequence();
		 ge.GenerateFlows();
	}

}
