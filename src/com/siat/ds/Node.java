package com.siat.ds;

/**
 * @ClassName Node
 * @Description �������ڵ���Ϣ
 * @author Zhu Yingtao
 * @date 2014��12��30�� ����11:09:26
 */
public class Node {

	int id;
	public String name;
	public int cellId;
	int direction;
	double length;

	/**
	 * @param id
	 * @param name
	 * @param cellId
	 * @param length
	 */
	public Node(int id, int cellId, double length, int direction) {
		this.id = id;
		this.cellId = cellId;
		this.length = length;
		this.direction = direction;
	}
}
