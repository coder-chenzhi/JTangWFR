package org.jtang.flowrecommender.bean;

public class Pattern {
	private String node ;
	private String subpath ;
	private int begindist ;
	private int enddist ;
	private int num ;
	private int counttogether ;
	private int countsub ;
	private double conf ;
	private double dist ;
	public double getDist() {
		return dist;
	}
	public void setDist(double dist) {
		this.dist = dist;
	}
	public int getNum() {
		return num;
	}
	public void setNum(int num) {
		this.num = num;
	}
	public String getNode() {
		return node;
	}
	public void setNode(String node) {
		this.node = node;
	}
	public String getSubpath() {
		return subpath;
	}
	public void setSubpath(String subpath) {
		this.subpath = subpath;
	}
	public int getBegindist() {
		return begindist;
	}
	public void setBegindist(int begindist) {
		this.begindist = begindist;
	}
	public int getEnddist() {
		return enddist;
	}
	public void setEnddist(int enddist) {
		this.enddist = enddist;
	}
	public int getCounttogether() {
		return counttogether;
	}
	public void setCounttogether(int counttogether) {
		this.counttogether = counttogether;
	}
	public int getCountsub() {
		return countsub;
	}
	public void setCountsub(int countsub) {
		this.countsub = countsub;
	}
	public double getConf() {
		return conf;
	}
	public void setConf(double conf) {
		this.conf = conf;
	}
}
