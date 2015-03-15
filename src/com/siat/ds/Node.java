package com.siat.ds;

/**
 * @ClassName Node
 * @Description �������ڵ���Ϣ
 * @author Zhu Yingtao
 * @date 2014��12��30�� ����11:09:26
 */
public class Node {
	int id;
	public String roadNodeName;
	public int cellId;
	double length;
	int direction;

	/**
	 * @param id
	 * @param roadNodeName
	 * @param cellId
	 * @param length
	 */
	public Node(int id, String roadNodeName, int cellId, double length,
			int direction) {
		this.id = id;
		this.roadNodeName = roadNodeName;
		this.cellId = cellId;
		this.length = length;
		this.direction = direction;
	}
}
