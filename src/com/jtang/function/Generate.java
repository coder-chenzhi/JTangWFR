package com.jtang.function;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class Generate {
	public static final int TestSetSize = 10;
	private File trainFile;
	private FileWriter trainFileOut;
	private File testFile;
	private FileWriter testFileOut;
	private int numberOfFlows;
	private int numberOfTotalNodes;
	private int maxNodes;
	private int minNodes;
	private int[][] workflowMatrix;

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
	public Generate(int numberOfFlows, int numberOfTotalNodes, int minNodes,
			int maxNodes) throws IOException {
		String trainSetName = System.getProperty("user.dir") + "\\"
				+ "workflow" + "\\" + "train" + "-" + numberOfTotalNodes + "-"
				+ numberOfFlows + "-" + (numberOfFlows - TestSetSize) + ".sdg";
		String testSetName = System.getProperty("user.dir") + "\\" + "workflow"
				+ "\\" + "test" + "-" + numberOfTotalNodes + "-"
				+ numberOfFlows + "-" + TestSetSize + ".sdg";
		trainFile = new File(trainSetName);
		trainFileOut = new FileWriter(trainFile, true);
		testFile = new File(testSetName);
		testFileOut = new FileWriter(testFile, true);
		this.numberOfFlows = numberOfFlows;
		this.numberOfTotalNodes = numberOfTotalNodes;
		this.maxNodes = maxNodes;
		this.minNodes = minNodes;
		workflowMatrix = new int[numberOfTotalNodes][numberOfTotalNodes];
	}

	public void generateMatrix() {
		Random rand = new Random();
		for (int i = 0; i < numberOfTotalNodes; i++) {
			for (int j = 0; j < numberOfTotalNodes; j++) {
				// if (i == j)
				// workflowMatrix[i][j] = 0;
				// else{
				// int position = rand.nextInt(5);
				// if(position == 1)
				// workflowMatrix[i][j] =1;
				// else
				// workflowMatrix[i][j] = 0;
				// }
				workflowMatrix[i][j] = 0;
				// System.out.print(workflowMatrix[i][j] + " ");
			}
		}
		for (int i = 0; i < numberOfTotalNodes; i++) {
			int j = 0;
			int oldpositon = -1;
			int position = 0;
			while (j < 2) {
				position = rand.nextInt(numberOfTotalNodes);
				if (position != i && position != oldpositon) {//
					workflowMatrix[i][position] = 1;
					oldpositon = position;
					j++;
				}

			}
		}
		printMatrix(workflowMatrix);
	}

	private void printMatrix(int[][] matrix) {
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix.length; j++) {
				System.out.print(workflowMatrix[i][j] + " ");
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

	public void instanceMatrics(String[][] matrix) {
		Set<Integer> usedNodeLabel = new HashSet<Integer>();
		int length = matrix.length;
		int[] num = new int[length];
		String[] trueNodes = new String[length];
		boolean[] bool = new boolean[numberOfTotalNodes];
		Random rand = new Random();
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				matrix[i][j] = "-";
			}
		}

		int n = rand.nextInt(numberOfTotalNodes);
		// matrix[0][0] = String.valueOf(n);
		usedNodeLabel.add(n);
		num[0] = n;
		bool[n] = true;
		trueNodes[0] = String.valueOf(n + 1);

		for (int j = 1; j < matrix.length; j++) {
			int x;
			do {
				x = rand.nextInt(numberOfTotalNodes);
			} while ((workflowMatrix[num[j - 1]][x] == 0 && bool[x])
					|| (usedNodeLabel.contains(x)));
			num[j] = x;
			bool[x] = true;
			// matrix[j][j] = String.valueOf(x);
			usedNodeLabel.add(x);
			trueNodes[j] = String.valueOf(x + 1);
		}
		for (int j = 0; j < matrix.length; j++) {
			matrix[j][j] = trueNodes[j];
		}
		for (int i = 0; i < matrix.length - 1; i++) {
			matrix[i][i + 1] = "1";
		}
	}

	public void GenerateFlows() throws IOException {
		int trainSetSize = numberOfFlows - TestSetSize;
		trainFileOut.write(String.valueOf(trainSetSize) + "\r\n");
		System.out.println("-----------------------");
		Random rand = new Random();
		for (int i = 0; i < trainSetSize; i++) {
			// the size of the workflow is larger than 1
			int iNodes = rand.nextInt(maxNodes - minNodes + 1) + minNodes;
			String[][] matrix = new String[iNodes][iNodes];
			instanceMatrics(matrix);
			trainFileOut.write('G');
			trainFileOut.write(String.valueOf(i + 1) + "\r\n");
			writeMatricToFile(matrix, trainFileOut);
		}
		trainFileOut.close();
		System.out.println("-----------------------");

		testFileOut.write(String.valueOf(TestSetSize) + "\r\n");

		for (int i = trainSetSize; i < numberOfFlows; i++) {
			// the size of the workflow is larger than 1
			int iNodes = rand.nextInt(maxNodes - minNodes + 1) + minNodes;
			String[][] matrix = new String[iNodes][iNodes];
			instanceMatrics(matrix);
			testFileOut.write('G');
			testFileOut.write(String.valueOf(i + 1) + "\r\n");
			writeMatricToFile(matrix, testFileOut);
		}

		testFileOut.close();
		System.out.println("-----------------------");
	}

	public static void main(String[] args) throws IOException {
		Generate ge = new Generate(100, 10, 5, 10);

		ge.generateMatrix();
		ge.GenerateFlows();
	}

}
