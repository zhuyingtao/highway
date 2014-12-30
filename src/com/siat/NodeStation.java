package com.siat;

/**
 * @ClassName NodeStation
 * @Description TODO
 * @author Zhu Yingtao
 * @date 2014��12��30�� ����11:09:26
 */
public class NodeStation {
	int id;
	String roadNodeName;
	int cellId;
	double length;
	int direction;

	/**
	 * @param id
	 * @param roadNodeName
	 * @param cellId
	 * @param length
	 */
	public NodeStation(int id, String roadNodeName, int cellId, double length,
			int direction) {
		this.id = id;
		this.roadNodeName = roadNodeName;
		this.cellId = cellId;
		this.length = length;
		this.direction = direction;
	}
}
