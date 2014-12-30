/**
 * 
 */
package com.siat;

import java.util.logging.Logger;

/**
 * @ClassName Server
 * @Description TODO
 * @author Zhu Yingtao
 * @date 2014��12��23�� ����9:57:03
 */
public class Server {

	public void work() {
		Logger logger = DataLogger.getLogger();
		SpeedAlgorithm sa = new SpeedAlgorithm(Configuration.START_TIME);
		int i = 0;
		while (true) {
			logger.info("==================== Number : " + i + " start");
			sa.computeAvgSpeed(Configuration.INTERVAL_TIME);
			// try {
			// Thread.sleep(10000);
			// } catch (InterruptedException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			logger.info("=================== Number : " + i + " end \n\n");
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
