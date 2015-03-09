/**
 * 
 */
package com.siat.msg;

import java.util.logging.Logger;

import com.siat.msg.alg.SegmentSpeed;
import com.siat.msg.util.DataLogger;

/**
 * @ClassName Server
 * @Description TODO
 * @author Zhu Yingtao
 * @date 2014年12月23日 上午9:57:03
 */
public class Server {

	public void work() {
		Logger logger = DataLogger.getLogger();
		SegmentSpeed sa = new SegmentSpeed(Configuration.START_TIME);
		int i = 0;
		while (true) {
			logger.severe("==================== Number : " + i + " start");
			sa.computeAvgSpeed(Configuration.INTERVAL_TIME);
			// try {
			// Thread.sleep(10000);
			// } catch (InterruptedException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			logger.severe("=================== Number : " + i + " end \n\n");
			i++;
		}
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
