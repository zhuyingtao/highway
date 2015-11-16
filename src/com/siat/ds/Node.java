package com.siat.ds;

/**
 * @author Zhu Yingtao
 * @ClassName Node
 * @Description 服务区节点信息
 * @date 2014年12月30日 上午11:09:26
 */
public class Node {

    int id;
    public String name;
    public int cellId;
    public int direction;
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
