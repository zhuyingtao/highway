package com.siat.ds;

/**
 * @author Zhu Yingtao
 * @ClassName Node
 * @Description the information of Road Node
 * @date 2014年12月30日 上午11:09:26
 */
public class Node {

    public int id;
    public String name;
    public int cellId;
    public int direction;
    public double length;

    public Node(int id, int cellId, double length, int direction) {
        this.id = id;
        this.cellId = cellId;
        this.length = length;
        this.direction = direction;
    }
}
