//package com.siat.gps;
//
///**
// * @ClassName Utility
// * @Description This class is an utility class. It contains a series methods for
// *              public use.
// * @author Zhu Yingtao, Tan Vicky
// * @date 2014年11月6日 上午10:43:14
// */
//public class Utility {
//
//	/**
//	 * @Title: azimuth
//	 * @Description: compute the azimuth of two points with radian measure;
//	 * @param point1
//	 * @param point2
//	 * @return
//	 */
//	public static double getAzimuth(Point point1, Point point2) {
//		// TODO ===================================divide 0 exception
//		if (point1.getX() == point2.getX()) {
//			System.out.println("------------------------- change");
//			return Math.PI / 2;
//		}
//		double tandegree = (point1.getY() - point2.getY())
//				/ (point1.getX() - point2.getX());
//		return Math.atan(tandegree);
//	}
//
//	/**
//	 * @Title: getDistance
//	 * @Description: compute the REAL distance of two points.
//	 * @param point1
//	 * @param point2
//	 * @return the real distance
//	 */
//	public static double getDistance(Point pt1, Point pt2) {
//		// double x2 = Math.pow(((point1.getX() - point2.getX()) * 110), 2);
//		// double y2 = Math.pow(((point1.getY() - point2.getY()) * 110
//		// *0.9227),2);
//		// return Math.sqrt(x2 + y2);
//		if (pt1.getX() == pt2.getX() && pt1.getY() == pt2.getY())
//			return 0;
//		double lamd1 = pt1.getX() * Math.PI / 180;// 经度弧度
//		double fi1 = pt1.getY() * Math.PI / 180; // 纬度弧度
//		double lamd2 = pt2.getX() * Math.PI / 180;
//		double fi2 = pt2.getY() * Math.PI / 180;
//
//		double dDistance = Math.acos(Math.sin(fi1) * Math.sin(fi2)
//				+ Math.cos(fi1) * Math.cos(fi2) * Math.cos(lamd1 - lamd2));
//		// System.out.println("ddd"+dDistance);
//
//		return dDistance * 6371.110; // 千米
//	}
//
//	public static double disSquare(Point pt1, Point pt2) {
//		// return Math.pow((point1.getX() - point2.getX()) * 110 * 0.9227, 2)
//		// + Math.pow((point1.getY() - point2.getY()) * 110, 2);
//		double lamd1 = pt1.getX() * Math.PI / 180;// 经度弧度
//		double fi1 = pt1.getY() * Math.PI / 180; // 纬度弧度
//		double lamd2 = pt2.getX() * Math.PI / 180;
//		double fi2 = pt2.getY() * Math.PI / 180;
//
//		double dDistance = Math.acos(Math.sin(fi1) * Math.sin(fi2)
//				+ Math.cos(fi1) * Math.cos(fi2) * Math.cos(lamd1 - lamd2));
//		return Math.pow(dDistance * 6371.110, 2); // 千米
//	}
//
//	public static double disSquare(Point point, RoadNode node) {
//		Point pt1 = node.start;
//		Point pt2 = node.end;
//		double disPt1 = disSquare(point, pt1);
//		double disPt2 = disSquare(point, pt2);
//		double c = disSquare(pt1, pt2);
//
//		// the pedal to the node id in the node line;
//		if (((disPt1 + c) > disPt2) && ((disPt2 + c) >= disPt1)) {
//			double c1 = (disPt1 + c - disPt2) / (2 * Math.sqrt(c));
//			return disPt1 - c1 * c1;
//		} else {
//			// the pedal to the node id out of the node line;
//			return disPt1 < disPt2 ? disPt1 : disPt2;
//		}
//	}
//
//	public static void sleep(long time) {
//		try {
//			Thread.sleep(time);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
// }
