/**
 * 
 */
package com.siat;

/**
 * @ClassName Server
 * @Description TODO
 * @author Zhu Yingtao
 * @date 2014年12月23日 上午9:57:03
 */
public class Server {

	public void work() {
		SpeedAlgorithm sa = new SpeedAlgorithm(Configuration.START_TIME);
		while (true) {
			sa.computeAvgSpeed(Configuration.INTERVAL_TIME);
		}
	}

	/**
	 * @Title: main
	 * @Description: TODO
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
