package com.siat.ds;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @ClassName NodeSegment
 * @Description the segment of Node and Node
 * @author Zhu Yingtao
 * @date 2014年12月30日 上午10:59:21
 */
public class NodeSegment {
	public int id;
	String sectionName;
	public int startNode;
	public int endNode;
	public int direction; // 1 is forward; 2 is reverse;
	double length;

	public int avgSpeed;
	public int speedNum;
	public int maxSpeed;
	public int minSpeed;

	/**
	 * @param id
	 * @param startNode
	 * @param endNode
	 * @param direction
	 */
	public NodeSegment(int id, int startNode, int endNode, int direction,
			double length) {
		this.id = id;
		this.startNode = startNode;
		this.endNode = endNode;
		this.direction = direction;
		this.length = length;
	}

	public static ArrayList<NodeSegment> initial() {
		ArrayList<Node> nodes = new ArrayList<>();
		ArrayList<NodeSegment> sections = new ArrayList<>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(
					"data/服务区2.txt"));
			br.readLine();
			String line = br.readLine();
			while (line != null) {
				String[] parts = line.split("\t");
				// int id = Integer.parseInt(parts[0]);
				int nodeId = Integer.parseInt(parts[1]);
				String nodeName = parts[2];
				int direction = Integer.parseInt(parts[4]);
				int cellId = Integer.parseInt(parts[8]);
				double length = Double.parseDouble(parts[9]);

				// 现在只用一个节点测试
				// Node ns = new Node(nodeId, nodeName, cellId, length,
				// direction);
				Node ns = null;

				nodes.add(ns);
				br.readLine();
				line = br.readLine();
			}
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// for (int j = 0; j < 2; j++) {
		// for (int i = 0; i < 24; i++) {
		// Node start = nodes.get(i);
		// Node end = nodes.get(i + 1);
		// int direction = (j == 0 ? 1 : 2);
		// NodeSegment rs = new NodeSegment(i, start, end, direction);
		// sections.add(rs);
		// }
		// }
		return sections;
	}

	public static void main(String[] args) {
		System.out.println(NodeSegment.initial().size());
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

	public int getSpeedNum() {
		return speedNum;
	}

	public void setSpeedNum(int speedNum) {
		this.speedNum = speedNum;
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
}
