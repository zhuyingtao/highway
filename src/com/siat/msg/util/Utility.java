package com.siat.msg.util;

import java.util.Date;

/**
 * @ClassName Utility
 * @Description TODO
 * @author Zhu Yingtao
 * @date 2015��3��19�� ����10:52:19
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
}
