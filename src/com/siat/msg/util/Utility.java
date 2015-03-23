package com.siat.msg.util;

import java.util.Date;

/**
 * @ClassName Utility
 * @Description TODO
 * @author Zhu Yingtao
 * @date 2015年3月19日 上午10:52:19
 */
public class Utility {

	/**
	 * @Title: intervalTime
	 * @Description: TODO
	 * @param start
	 * @param end
	 * @return
	 */
	public static long intervalTime(Date start, Date end) {
		long d1 = start.getTime();
		long d2 = end.getTime();
		return (d2 - d1) / 1000;
	}

	public static void sleep(double second) {
		try {
			Thread.sleep((long) (second * 1000));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
