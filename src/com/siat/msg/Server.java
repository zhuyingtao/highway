/**
 * 
 */
package com.siat.msg;

import java.io.File;
import java.util.Arrays;
import java.util.logging.Logger;

import com.siat.msg.alg.SegmentSpeed;
import com.siat.msg.util.DataLogger;

/**
 * @ClassName Server
 * @Description TODO
 * @author Zhu Yingtao
 * @date 2014年12月23日 上午9:57:03
 */
public class Server extends Object {

	public void work() {
		this.clearLog(); // delete the previous log;
		Logger logger = DataLogger.getLogger();
		SegmentSpeed sa = new SegmentSpeed(Configuration.START_TIME);
		int i = 0;
		String startTime = Configuration.startTime;
		String endTime = Configuration.endTime;
		int rate = 1;
		while (true) {
			int request = this.checkRequest();
			if (request == 0) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (request == 1) {
				logger.info("=============== History speed ================");
				sa.computeHistorySpeed(startTime, endTime, rate);
			} else if (request == 2) {
				logger.severe("==================== Number : " + i + " start");
				sa.computeAvgSpeed(Configuration.INTERVAL_TIME);
				logger.severe("=================== Number : " + i + " end \n\n");
				i++;
			}
		}
	}

	// check whether there is a new request according to the flag selected from
	// the database;
	// the flag has 3 values:
	// 0, means no new request;
	// 1, means history_speed request;
	// 2, means real_time_speed request;
	public int checkRequest() {
		int flag = 1;

		return flag;
	}

	public void clearLog() {
		File f = new File(".");
		String[] files = f.list();
		int count = 0;
		for (int i = 0; i < files.length; i++) {
			if (files[i].matches(".*log.*")) {
				File file = new File(files[i]);
				file.delete();
				count++;
			}
		}
		System.out.println("delete log : " + count);
	}

	/**
	 * @Title: main
	 * @Description: TODO
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new Server().work();
	}
}
