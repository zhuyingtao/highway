package com.siat.gps;

/**
 * @ClassName Point
 * @Description TODO
 * @author Zhu Yingtao
 * @date 2015年1月29日 下午11:50:38
 */
public class Point {
	double x;
	double y;

	public Point(double x, double y) {
		// TODO Auto-generated constructor stub
		this.x = x;
		this.y = y;
	}

	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		Point p = (Point) obj;
		return this.x == p.x && this.y == p.y;
	}
}
