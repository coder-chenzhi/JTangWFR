package org.jtang.distance.ged.editpath.test;

public class Distance {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	public int getGraphEditDistance() {
		return graphEditDistance;
	}
	public void setGraphEditDistance(int graphEditDistance) {
		this.graphEditDistance = graphEditDistance;
	}
	public int getPositionDistance() {
		return positionDistance;
	}
	public void setPositionDistance(int positionDistance) {
		this.positionDistance = positionDistance;
	}
	public double getNodeCountDistance() {
		return nodeCountDistance;
	}
	public void setNodeCountDistance(double nodeCountDistance) {
		this.nodeCountDistance = nodeCountDistance;
	}
	

	public double getMcsDistance() {
		return mcsDistance;
	}
	public void setMcsDistance(double mcsDistance) {
		this.mcsDistance = mcsDistance;
	}
	
	@Override
	public String toString(){
		return graphEditDistance+";"+positionDistance+";"+nodeCountDistance+";"+mcsDistance+".";
	}
	private int graphEditDistance = 0;
	private int positionDistance = 0;
	private double nodeCountDistance = 0.0;
	private double mcsDistance = 0.0;
}
