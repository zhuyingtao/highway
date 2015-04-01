package com.siat.ds;

import java.util.List;

/**
 * @ClassName NodeSegment
 * @Description the segment of Node and Node
 * @author Zhu Yingtao
 * @date 2014年12月30日 上午10:59:21
 */
public class NodeSegment {
	public int id;
	String sectionName;
	public int startNodeId;
	public int endNodeId;
	public int direction; // 1 is forward; 2 is reverse;
	double length;

	public Node startNode;
	public Node endNode;

	private int avgSpeed = 80; // default is 80;
	private int maxSpeed = -1;
	private int minSpeed = 100;
	private int realNum = 0;

	// private int expectedNum = 0; // this variable is INACCURATE

	/**
	 * @param id
	 * @param startNodeId
	 * @param endNodeId
	 * @param direction
	 */
	public NodeSegment(int id, int startNodeId, int endNodeId, int direction,
			double length) {
		this.id = id;
		this.startNodeId = startNodeId;
		this.endNodeId = endNodeId;
		this.direction = direction;
		this.length = length;
	}

	public void initStart(List<Node> list) {
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).id == this.startNodeId) {
				this.startNode = list.get(i);
				break;
			}
		}
	}

	public void initEnd(List<Node> list) {
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).id == this.endNodeId) {
				this.endNode = list.get(i);
				break;
			}
		}
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getNodeName() {
		// this.sectionName = this.startNode.roadNodeName + " -- "
		// + this.endNode.roadNodeName;
		return sectionName;
	}

	public void setNodeName(String sectionName) {
		this.sectionName = sectionName;
	}

	public int getDirection() {
		return direction;
	}

	public void setDirection(int direction) {
		this.direction = direction;
	}

	public int getAvgSpeed() {
		return avgSpeed;
	}

	public void setAvgSpeed(int avgSpeed) {
		this.avgSpeed = avgSpeed;
	}

	public int getMaxSpeed() {
		return maxSpeed;
	}

	public void setMaxSpeed(int maxSpeed) {
		this.maxSpeed = maxSpeed;
	}

	public int getMinSpeed() {
		return minSpeed;
	}

	public void setMinSpeed(int minSpeed) {
		this.minSpeed = minSpeed;
	}

	public int getRealNum() {
		return realNum;
	}

	public void setRealNum(int realNum) {
		this.realNum = realNum;
	}

	// public int getExpectedNum() {
	// return expectedNum;
	// }
	//
	// public void setExpectedNum(int expectedNum) {
	// this.expectedNum = expectedNum;
	// }
}
