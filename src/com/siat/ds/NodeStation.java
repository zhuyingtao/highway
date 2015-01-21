package com.siat.ds;

/**
 * @ClassName NodeStation
 * @Description 服务区节点信息
 * @author Zhu Yingtao
 * @date 2014年12月30日 上午11:09:26
 */
public class NodeStation {
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
	public NodeStation(int id, String roadNodeName, int cellId, double length,
			int direction) {
		this.id = id;
		this.roadNodeName = roadNodeName;
		this.cellId = cellId;
		this.length = length;
		this.direction = direction;
	}
}
