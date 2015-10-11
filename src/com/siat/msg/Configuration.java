/**
 * 
 */
package com.siat.msg;

import java.util.logging.Level;

/**
 * @ClassName Configuration
 * @Description TODO
 * @author Zhu Yingtao
 * @date 2014��12��16�� ����3:15:33
 */
public class Configuration {

	// �����������ʱ��
	// public static String START_TIME = "2014/12/08 00:00:00";
	public static String START_TIME = null;
	// ÿһ�����ݵ�ʱ�������룩
	public static int INTERVAL_TIME = 2 * 60;
	// ���ݻ���ʱ�䳤�ȣ��룩
	public static final int CACHE_TIME = 30 * 60;
	// ��ǰ������û���õ��Ļ�վID
	public static int[] unusedId = { 13913, 15281, 17141, 32803, 31162, 59131,
			10753, 15851, 29521, 38721, 24541, 10751, 39421, 11171, 31292,
			32801, 17152, 13912, 32801, 34991, 41401, 33602, 14841, 34001,
			11402, 39422, 24542, 17151, 60061, 32802, 34821, 12211, 10752,
			11172, 44101, 10761, 47461 };

	public static int[] unused = { 15851, 34361 };

	// used for computing history speed;
	public static String startTime = "2015-01-22 07:00:00";
	public static String endTime = "2015-01-22 12:00:00";
	// the time of request coming;
	public static String setTime = "2015-01-22 07:00:00";
	// the rate of computing, the default value is 1 (based on INTERVAL_TIME);
	public static int rate = 1;

	public static final Level logLevel = Level.INFO;

	// whether write the speed results to database;
	public static final boolean WRITE_TO_DATABASE = true;
	// whether write the speed results to file;
	public static final boolean WRITE_TO_FILE = false;

	// interface for the database;
	public static String URL = "jdbc:oracle:thin:@210.75.252.44:1521:ORCL";
	public static String USER = "hw";
	public static String PASSWORD = "hw";

	public static String USER_URL = null;
	public static String USER_USER = null;
	public static String USER_PASSWORD = null;
	public static String USER_TABLE = null;

	// public static String Configuration_Path = "D:/highway/Configuration.ini";
	public static String Configuration_Path = "Configuration.ini";

	public static final int DEFAULT_SPEED = 90;
	public static final int LIMIT_SEGMENT_SPEED = 110;

}
