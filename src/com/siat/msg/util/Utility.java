package com.siat.msg.util;

import java.util.Date;

/**
 * @author Zhu Yingtao
 * @ClassName Utility
 * @Description TODO
 * @date 2015年3月19日 上午10:52:19
 */
public class Utility {

    /**
     * @param start the start time
     * @param end   the end time
     * @return the interval second
     * @Title: intervalTime
     * @Description: compute the interval second between start and end;
     */
    public static long intervalTime(Date start, Date end) {
        long d1 = start.getTime();
        long d2 = end.getTime();
        return (d2 - d1) / 1000;
    }

    /**
     * @param second the specific time (s) want to sleep;
     * @Title: sleep
     * @Description: sleep for some time;
     */
    public static void sleep(double second) {
        try {
            Thread.sleep((long) (second * 1000));
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
