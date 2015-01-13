/**
 * 
 */
package com.siat;

/**
 * @ClassName Configuration
 * @Description TODO
 * @author Zhu Yingtao
 * @date 2014年12月16日 下午3:15:33
 */
public class Configuration {

	// 数据中最早的时间
	public static String START_TIME = "2014/12/08 00:00:00";
	// 每一批数据的时间间隔（秒）
	public static final int INTERVAL_TIME = 5 * 60;
	// 数据缓存时间长度（秒）
	public static final int CACHE_TIME = 30 * 60;

	public static int[] unusedId = { 13913, 15281, 17141, 32803, 31162, 59131,
			10753, 15851, 29521, 38721, 24541, 10751, 39421, 11171, 31292,
			32801, 17152, 13912, 32801, 34991, 41401, 33602, 14841, 34001,
			11402, 39422, 24542, 17151, 60061, 32802, 34821, 12211, 10752,
			11172, 44101, 10761, 47461 };
}
