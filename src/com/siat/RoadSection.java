package com.siat;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @ClassName Section
 * @Description TODO
 * @author Zhu Yingtao
 * @date 2014年12月30日 上午10:59:21
 */
public class RoadSection {
	int id;
	String sectionName;
	NodeStation startNode;
	NodeStation endNode;
	int direction;
	double length;
	double speed;

	/**
	 * @param id
	 * @param startNode
	 * @param endNode
	 * @param direction
	 */
	public RoadSection(int id, NodeStation startNode, NodeStation endNode,
			int direction) {
		super();
		this.id = id;
		this.startNode = startNode;
		this.endNode = endNode;
		this.direction = direction;
	}

	public static ArrayList<RoadSection> initial() {
		ArrayList<NodeStation> nodes = new ArrayList<>();
		ArrayList<RoadSection> sections = new ArrayList<>();
		try {
			BufferedReader br = new BufferedReader(new FileReader("data/服务区2.txt"));
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
				NodeStation ns = new NodeStation(nodeId, nodeName, cellId,
						length, direction);
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

		for (int j = 0; j < 2; j++) {
			for (int i = 0; i < 24; i++) {
				NodeStation start = nodes.get(i);
				NodeStation end = nodes.get(i + 1);
				int direction = (j == 0 ? 1 : 2);
				RoadSection rs = new RoadSection(i, start, end, direction);
				sections.add(rs);
			}
		}
		return sections;
	}

	public static void main(String[] args) {
		System.out.println(RoadSection.initial().size());
	}
}
