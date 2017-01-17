package org.jtang.distance.mcs;

import java.util.TreeSet;

/**
 * 
 * @author tokyo
 * @version 2012-4-16 this is a DFSCode representation of a subgraph
 */

public class SubGraphDFSCode implements Comparable<SubGraphDFSCode> {

	public SubGraphDFSCode(int size, String id, String DFSCode) {
		this.size = size;
		this.id = id;
		this.DFSCode = DFSCode;
	}

	@Override
	/**
	 * sort in descending order
	 */
	public int compareTo(SubGraphDFSCode o) {
		// TODO Auto-generated method stub
		if (this.size != o.size) {
			return o.size - this.size;
		} else {
			if (!this.id.equals(o.id)) {
				return o.id.compareTo(this.id);
			} else {
				return o.DFSCode.compareTo(this.DFSCode);
			}

		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		TreeSet<SubGraphDFSCode> temp = new TreeSet<SubGraphDFSCode>();
		temp.add(new SubGraphDFSCode(1, "1", "a"));
		temp.add(new SubGraphDFSCode(1, "2", "a"));
		temp.add(new SubGraphDFSCode(3, "1", "b"));
		temp.add(new SubGraphDFSCode(2, "1", "a"));
		temp.add(new SubGraphDFSCode(1, "4", "c"));
		temp.add(new SubGraphDFSCode(3, "2", "a"));
		temp.add(new SubGraphDFSCode(3, "1", "d"));
		int size = temp.size();
		System.out.println(size);
		for (int i = 0; i < size; i++) {
			SubGraphDFSCode code = temp.first();
			System.out.println(code.getSize() + "," + code.getId() + "," + code.getDFSCode());
			temp.remove(code);
		}
	}
	@Override
	public String toString(){
		return size+";"+id+";"+DFSCode;
		
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDFSCode() {
		return DFSCode;
	}

	public void setDFSCode(String dFSCode) {
		DFSCode = dFSCode;
	}

	private int size;
	private String id;
	private String DFSCode;

}
