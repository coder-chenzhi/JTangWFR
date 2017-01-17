package org.jtang.function;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		if("A->Z".matches("[^A-Z]*->[^A-Z]*"))
			System.out.println("yes!");
		System.out.println("no");
	}

}
