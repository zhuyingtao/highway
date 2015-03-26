/**
 * 
 */
package com.siat.msg;

import java.io.File;
import java.util.logging.Logger;

import com.siat.msg.alg.SpeedAlgorithm;
import com.siat.msg.db.DBServiceForOracle;
import com.siat.msg.util.DataLogger;
import com.siat.msg.util.Utility;

/**
 * @ClassName Server
 * @Description TODO
 * @author Zhu Yingtao
 * @date 2014年12月23日 上午9:57:03
 */
public class Server {

	DBServiceForOracle db = new DBServiceForOracle();

	public void work() {
		this.clearLog(); // delete the previous log;
		Logger logger = DataLogger.getLogger();
		SpeedAlgorithm sa = new SpeedAlgorithm();
		// initial all the station segments and node segments;
		sa.initSegments();
		while (true) {
			int request = this.checkRequest();
			if (request == 0) {
				// wait 1 second;
				Utility.sleep(1);
			} else if (request == 1) {
				logger.info("=============== History speed ================");
				String[] strs = { "00", "01", "02", "03", "04", "05", "06",
						"07", "08", "09", "10", "11", "12", "13", "14", "15",
						"16", "17", "18", "19", "20", "21", "22", "23" };
				for (int j = 0; j < 23; j++) {
					String startTime = "2015-02-18 " + strs[j] + ":00:00";
					String endTime = "2015-02-18 " + strs[j + 1] + ":00:00";
					sa.computeHistorySpeed(startTime, endTime,
							Configuration.rate);
				}
				break;
				// sa.computeHistorySpeed(Configuration.startTime,
				// Configuration.endTime, Configuration.rate);
			} else if (request == 2) {
				// logger.severe("==================== Number : " + i +
				// " start");
				// sa.computeAvgSpeed(Configuration.INTERVAL_TIME);
				// logger.severe("=================== Number : " + i +
				// " end \n\n");
				// i++;
			}
		}
	}

	// check whether there is a new request based on the flag selected from
	// the database;
	// the flag has 3 values:
	// 0, means no new request;
	// 1, means history_speed request;
	// 2, means real_time_speed request;
	public int checkRequest() {
		int flag = 0;
		flag = db.checkRequest();
		return flag;
	}

	public void clearLog() {
		File f = new File(".");
		String[] files = f.list();
		int count = 0;
		for (int i = 0; i < files.length; i++) {
			if (files[i].matches("(.*)\\.log(.*)")
					|| files[i].matches(".*speeds\\.txt")) {
				File file = new File(files[i]);
				file.delete();
				System.out.println(file.getName() + " deleted ! ");
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
