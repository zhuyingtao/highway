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
		int i = 0;
		while (true) {
			System.out.println("======= Number : " + i);
			sa.computeAvgSpeed(Configuration.INTERVAL_TIME);
			i++;
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
